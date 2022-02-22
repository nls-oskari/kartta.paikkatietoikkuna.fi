package fi.nls.paikkatietoikkuna.terrainprofile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.paikkatietoikkuna.terrainprofile.dem.FloatAsIsValueExtractor;
import fi.nls.paikkatietoikkuna.terrainprofile.dem.ScaledGrayscaleValueExtractor;
import fi.nls.paikkatietoikkuna.terrainprofile.dem.TileValueExtractor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.operation.TransformException;
import org.oskari.geojson.GeoJSONReader;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

@OskariActionRoute("TerrainProfile")
public class TerrainProfileHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(TerrainProfileHandler.class);

    protected static final String PARAM_ROUTE = "route";

    protected static final String PROPERTY_ENDPOINT = "terrain.profile.wcs.endPoint";
    protected static final String PROPERTY_ENDPOINT_SRS = "terrain.profile.wcs.srs";
    protected static final String PROPERTY_DEM_COVERAGE_ID = "terrain.profile.wcs.demCoverageId";
    protected static final String PROPERTY_NODATA_VALUE = "terrain.profile.wcs.noData";
    protected static final String PROPERTY_DEM_TYPE = "terrain.profile.wcs.demType";
    protected static final String PROPERTY_DEM_SCALE = "terrain.profile.wcs.demScale";
    protected static final String PROPERTY_DEM_OFFSET = "terrain.profile.wcs.demOffset";

    protected static final String JSON_PROPERTY_PROPERTIES = "properties";
    protected static final String JSON_PROPERTY_NUM_POINTS = "numPoints";
    protected static final String JSON_PROPERTY_SCALE_FACTOR = "scaleFactor";
    protected static final String JSON_PROPERTY_DISTANCE_FROM_START = "distanceFromStart";

    private static final int NUM_POINTS_MAX = 1000;
    private static final String DEFAULT_SRS = "EPSG:3067";

    private final ObjectMapper om;
    private TerrainProfileService tps;
    private String serviceSrs;

    public TerrainProfileHandler() {
        this(new ObjectMapper(), null);
    }

    public TerrainProfileHandler(ObjectMapper om, TerrainProfileService tps) {
        this.om = om;
        this.tps = tps;
    }

    @Override
    public void init() {
        try {
            tps = getService();
        } catch (NoSuchElementException propertyMissing) {
            // fatal, throw an exception so this route is not added to available actions
            throw new ServiceRuntimeException(
                    "Failed to init TerrainProfileService: " + propertyMissing.getMessage());
        } catch (ServiceException ex) {
            // not fatal, proceed with init and try again later
            LOG.error("Failed to init TerrainProfileService: " + ex.getMessage(), ex);
        }
        serviceSrs = PropertyUtil.get(PROPERTY_ENDPOINT_SRS, DEFAULT_SRS).toUpperCase();
    }

    protected synchronized TerrainProfileService getService() throws ServiceException {
        if (tps == null) {
            tps = new TerrainProfileService(
                    PropertyUtil.getNecessary(PROPERTY_ENDPOINT),
                    PropertyUtil.getNecessary(PROPERTY_DEM_COVERAGE_ID),
                    getTileValueExtractor());
        }
        return tps;
    }

    private Supplier<TileValueExtractor> getTileValueExtractor() {
        String type = PropertyUtil.get(PROPERTY_DEM_TYPE, FloatAsIsValueExtractor.ID);

        switch (type) {
        case ScaledGrayscaleValueExtractor.ID:
            double scale = Double.parseDouble(PropertyUtil.getNecessary(PROPERTY_DEM_SCALE));
            double offset = Double.parseDouble(PropertyUtil.getNecessary(PROPERTY_DEM_OFFSET));
            short noDataS = getNoDataValue(Short::parseShort).shortValue();
            return () -> new ScaledGrayscaleValueExtractor(offset, scale, noDataS);

        case FloatAsIsValueExtractor.ID:
        default:
            float noDataF = getNoDataValue(Float::parseFloat).floatValue();
            return () -> new FloatAsIsValueExtractor(noDataF);
        }
    }

    private Number getNoDataValue(Function<String, Number> parser) {
        String noDataStr = PropertyUtil.getOptional(PROPERTY_NODATA_VALUE);
        if (noDataStr != null && !noDataStr.isEmpty()) {
            try {
                Number noDataValue = parser.apply(noDataStr);
                LOG.debug("NODATA value:", noDataValue);
                return noDataValue;
            } catch (NumberFormatException e) {
                LOG.warn("Could not parse NODATA value from " + noDataStr);
            }
        }
        return Double.NaN;
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        JSONObject route = JSONHelper.createJSONObject(params.getRequiredParam(PARAM_ROUTE));

        String targetSRS = params.getHttpParam(ActionConstants.PARAM_SRS, DEFAULT_SRS);
        boolean reproject = !targetSRS.equals(serviceSrs);

        double[] points = getRoutePoints(route);
        if (reproject) {
            transformInPlace(points, serviceSrs, targetSRS);
        }

        JSONObject properties = route.optJSONObject(JSON_PROPERTY_PROPERTIES);
        int numPoints = getNumPoints(properties);
        double scaleFactor = getScaleFactor(properties);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of coords:", points.length / 2,
                    "line:", Arrays.toString(points), "numPoints", numPoints);
        }

        try {
            List<DataPoint> dp = getService().getTerrainProfile(points, numPoints, scaleFactor);
            writeResponse(params, dp, reproject);
        } catch (ServiceException e) {
            throw new ActionException(e.getMessage(), e);
        }
    }

    private double[] getRoutePoints(JSONObject route) throws ActionException {
        LineString geom = getGeometry(route);
        return getXYCoordinates(geom);
    }

    protected LineString getGeometry(JSONObject route) throws ActionParamsException {
        try {
            Geometry geom = GeoJSONReader.toGeometry(route.getJSONObject("geometry"));
            if (geom.getNumPoints() > NUM_POINTS_MAX) {
                throw new ActionParamsException("Invalid input - too many coordinates, maximum is " + NUM_POINTS_MAX);
            }
            if (!(geom instanceof LineString)) {
                throw new ActionParamsException("Invalid input - expected LineString geometry");
            }
            return (LineString) geom;
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid input - expected GeoJSON feature", e);
        } catch (IllegalArgumentException e) {
            throw new ActionParamsException("Invalid input - expected LineString with atleast two coordinates");
        }
    }

    private double[] getXYCoordinates(LineString line) {
        final CoordinateSequence csq = line.getCoordinateSequence();
        final int len = csq.size();
        final double[] xy = new double[len * 2];
        for (int ci = 0, i = 0; ci < len; ci++) {
            xy[i++] = csq.getX(ci);
            xy[i++] = csq.getY(ci);
        }
        return xy;
    }

    protected void transformInPlace(double[] xy, String fromSrs, String toSrs) throws ActionException {
        MathTransform transform = getTransform(fromSrs, toSrs);
        try {
            transform.transform(xy, 0, xy, 0, xy.length / 2);
        } catch (TransformException e) {
            throw new ActionException(e.getMessage(), e);
        }
    }

    protected MathTransform getTransform(String fromSrs, String toSrs) throws ActionException {
        CoordinateReferenceSystem fromCRS;
        CoordinateReferenceSystem toCRS;
        try {
            fromCRS = CRS.decode(fromSrs);
            toCRS = CRS.decode(toSrs);
            boolean lenient = true; // allow for some error due to different datums
            return CRS.findMathTransform(fromCRS, toCRS, lenient);
        } catch (FactoryException e) {
            throw new ActionParamsException("Invalid " + ActionConstants.PARAM_SRS);
        }
    }

    protected int getNumPoints(JSONObject props) throws ActionParamsException {
        if (props == null || !props.has(JSON_PROPERTY_NUM_POINTS)) {
            return 0;
        }
        try {
            return Math.min(props.getInt(JSON_PROPERTY_NUM_POINTS), NUM_POINTS_MAX);
        } catch (JSONException e) {
            // Throwing ActionParamsException below
        }
        throw new ActionParamsException(String.format(
                "Invalid property value '%s'", JSON_PROPERTY_NUM_POINTS));
    }

    protected double getScaleFactor(JSONObject props) {
        if (props != null && props.has(JSON_PROPERTY_SCALE_FACTOR)) {
            try {
                return props.getDouble(JSON_PROPERTY_SCALE_FACTOR);
            } catch (JSONException e) {
                // Ignore
            }
        }
        return 0;
    }

    protected void writeResponse(ActionParameters params, List<DataPoint> dp, boolean reproject) throws ActionException {
        if (reproject) {
            String targetSRS = params.getHttpParam(ActionConstants.PARAM_SRS, DEFAULT_SRS);
            MathTransform transform = getTransform(serviceSrs, targetSRS);
            try {
                double[] xy = new double[2];
                for (DataPoint cur : dp) {
                    xy[0] = cur.getE();
                    xy[1] = cur.getN();
                    transform.transform(xy, 0, xy, 0, 1);
                    cur.setE(xy[0]);
                    cur.setN(xy[1]);
                }
            } catch (TransformException e) {
                throw new ActionException(e.getMessage(), e);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (JsonGenerator json = om.getFactory().createGenerator(baos)) {
            writeMultiPointFeature(dp, json);
        } catch (IOException e) {
            throw new ActionException("Failed to encode GeoJSON", e);
        }
        ResponseHelper.writeResponse(params, 200, IOHelper.CONTENT_TYPE_JSON, baos);
    }

    protected static void writeMultiPointFeature(List<DataPoint> dp,
            JsonGenerator json) throws IOException {
        json.writeStartObject();
        json.writeStringField("type", "Feature");

        json.writeFieldName("geometry");
        json.writeStartObject();
        json.writeStringField("type", "MultiPoint");
        json.writeFieldName("coordinates");
        json.writeStartArray();
        for (DataPoint p : dp) {
            json.writeStartArray();
            json.writeNumber(p.getE());
            json.writeNumber(p.getN());
            double alt = p.getAltitude();
            if (Double.isNaN(alt)) {
                json.writeNull();
            } else {
                json.writeNumber(alt);
            }
            json.writeEndArray();
        }
        json.writeEndArray();
        json.writeEndObject();

        json.writeFieldName("properties");
        json.writeStartObject();
        json.writeNumberField(JSON_PROPERTY_NUM_POINTS, dp.size());
        json.writeFieldName(JSON_PROPERTY_DISTANCE_FROM_START);
        json.writeStartArray();
        for (DataPoint p : dp) {
            json.writeNumber(p.getDistFromStart());
        }
        json.writeEndArray();
        json.writeEndObject();

        json.writeEndObject();
    }

}
