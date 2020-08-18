package fi.nls.paikkatietoikkuna.terrainprofile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.operation.TransformException;
import org.oskari.geojson.GeoJSONReader;

import org.geotools.geometry.jts.JTS;
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

    protected static final String JSON_PROPERTY_PROPERTIES = "properties";
    protected static final String JSON_PROPERTY_NUM_POINTS = "numPoints";
    protected static final String JSON_PROPERTY_SCALE_FACTOR = "scaleFactor";
    protected static final String JSON_PROPERTY_DISTANCE_FROM_START = "distanceFromStart";

    private static final int NUM_POINTS_MAX = 1000;
    private static final String DEFAULT_SRS = "EPSG:3067";

    private final ObjectMapper om;
    private TerrainProfileService tps;
    private float noDataValue;
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
        noDataValue = getNoDataValue();
        LOG.debug("NODATA value:", noDataValue);
    }

    protected synchronized TerrainProfileService getService() throws ServiceException {
        if (tps == null) {
            tps = new TerrainProfileService(
                    PropertyUtil.getNecessary(PROPERTY_ENDPOINT),
                    PropertyUtil.getNecessary(PROPERTY_DEM_COVERAGE_ID));
        }
        return tps;
    }

    private float getNoDataValue() {
        String noDataStr = PropertyUtil.getOptional(PROPERTY_NODATA_VALUE);
        if (noDataStr != null && !noDataStr.isEmpty()) {
            try {
                return Float.parseFloat(noDataStr);
            } catch (NumberFormatException e) {
                LOG.warn("Could not parse NODATA value from " + noDataStr);
            }
        }
        return Float.NaN;
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        JSONObject route = JSONHelper.createJSONObject(params.getRequiredParam(PARAM_ROUTE));
        Geometry geom = getGeometry(route);

        String targetSRS = params.getHttpParam(ActionConstants.PARAM_SRS, DEFAULT_SRS);
        boolean reproject = !targetSRS.equals(serviceSrs);
        if (reproject) {
            geom = getReprojected(geom, targetSRS, serviceSrs);
        }

        JSONObject properties = route.optJSONObject(JSON_PROPERTY_PROPERTIES);
        int numPoints = Math.min(getNumPoints(properties), NUM_POINTS_MAX);
        double scaleFactor = getScaleFactor(properties);
        double[] points = new double[geom.getNumPoints() * 2];
        int ptIndex = -1;
        for (Coordinate coord : geom.getCoordinates()) {
            points[++ptIndex] = coord.x;
            points[++ptIndex] = coord.y;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of coords:", geom.getNumPoints(),
                    "line:", Arrays.toString(points), "numPoints", numPoints);
        }

        try {
            writeResponse(params, getService().getTerrainProfile(points, numPoints, scaleFactor), reproject);
        } catch (ServiceException e) {
            throw new ActionException(e.getMessage(), e);
        }
    }

    protected Geometry getGeometry(JSONObject route) throws ActionParamsException {
        try {
            Geometry geom = GeoJSONReader.toGeometry(route.getJSONObject("geometry"));
            if (!(geom.getGeometryType().equals("LineString"))) {
                throw new ActionParamsException("Invalid input - expected LineString geometry");
            }
            if (geom.getNumPoints() > NUM_POINTS_MAX) {
                throw new ActionParamsException("Invalid input - too many coordinates, maximum is " + NUM_POINTS_MAX);
            }
            return geom;
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid input - expected GeoJSON feature", e);
        } catch (IllegalArgumentException e) {
            throw new ActionParamsException("Invalid input - expected LineString with atleast two coordinates");
        }
    }

    protected Geometry getReprojected(Geometry geom, String fromSrs, String toSrs) throws ActionException {
        MathTransform transform = getTransform(fromSrs, toSrs);
        try {
            return JTS.transform(geom, transform);
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
            return props.getInt(JSON_PROPERTY_NUM_POINTS);
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
            GeometryFactory gf = new GeometryFactory();
            try {
                for (DataPoint cur : dp) {
                    Geometry geom = gf.createPoint(new Coordinate(cur.getE(), cur.getN()));
                    geom = JTS.transform(geom, transform);
                    cur.setE(geom.getCoordinate().x);
                    cur.setN(geom.getCoordinate().y);
                }
            } catch (TransformException e) {
                throw new ActionException(e.getMessage(), e);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (JsonGenerator json = om.getFactory().createGenerator(baos)) {
            writeMultiPointFeature(dp, json, noDataValue);
        } catch (IOException e) {
            throw new ActionException("Failed to encode GeoJSON", e);
        }
        ResponseHelper.writeResponse(params, 200, IOHelper.CONTENT_TYPE_JSON, baos);
    }

    protected static void writeMultiPointFeature(List<DataPoint> dp,
            JsonGenerator json, final float noData) throws IOException {
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
            float alt = p.getAltitude();
            if (alt == noData) {
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
