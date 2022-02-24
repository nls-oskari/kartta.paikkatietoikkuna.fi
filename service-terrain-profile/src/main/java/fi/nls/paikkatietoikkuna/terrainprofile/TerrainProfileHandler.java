package fi.nls.paikkatietoikkuna.terrainprofile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
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

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

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
        JsonNode route = getParamRoute(params);

        double[] points = getRoutePoints(route);
        int numPoints = getNumPoints(route.get(JSON_PROPERTY_PROPERTIES));
        double scaleFactor = getScaleFactor(route.get(JSON_PROPERTY_PROPERTIES));

        // Allow route to be GC'd
        route = null;

        MathTransform transform = getTransform(serviceSrs, params);

        if (transform != null) {
            transformInPlace(points, transform);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of coords:", points.length / 2,
                    "line:", Arrays.toString(points), "numPoints", numPoints);
        }

        try {
            List<DataPoint> dp = getService().getTerrainProfile(points, numPoints, scaleFactor);
            if (transform != null) {
                transformInPlace(dp, transform);
            }
            writeResponse(params, dp);
        } catch (ServiceException e) {
            throw new ActionException(e.getMessage(), e);
        }
    }

    private JsonNode getParamRoute(ActionParameters params) throws ActionParamsException {
        try {
            String routeJson = params.getRequiredParam(PARAM_ROUTE);
            return om.readTree(routeJson);
        } catch (JsonProcessingException e) {
            throw new ActionParamsException("Expected JSON object for param " + PARAM_ROUTE, e);
        }
    }

    protected double[] getRoutePoints(JsonNode route) throws ActionParamsException {
        JsonNode geometry = route.get("geometry");
        if (geometry == null || !geometry.isObject()) {
            throw new ActionParamsException("Invalid input - expected GeoJSON feature");
        }

        JsonNode type = geometry.get("type");
        if (type == null || !type.isTextual() || !"LineString".equals(type.textValue())) {
            throw new ActionParamsException("Invalid input - expected LineString geometry");
        }

        JsonNode coordinates = geometry.get("coordinates");
        if (coordinates == null || !coordinates.isArray()) {
            throw new ActionParamsException("Invalid input - expected LineString geometry");
        }
        int len = coordinates.size();
        if (len < 2) {
            throw new ActionParamsException("Invalid input - expected LineString with atleast two coordinates");
        } else if (len > NUM_POINTS_MAX) {
            throw new ActionParamsException("Invalid input - too many coordinates, maximum is " + NUM_POINTS_MAX);
        }

        double[] xy = new double[len * 2];
        for (int i = 0; i < len; i++) {
            JsonNode coordinate = coordinates.get(i);
            if (coordinate == null || !coordinate.isArray()) {
                throw new ActionParamsException("Invalid input - expected LineString geometry");
            }
            JsonNode x = coordinate.get(0);
            JsonNode y = coordinate.get(1);
            if (!x.isNumber() || !y.isNumber()) {
                throw new ActionParamsException("Invalid input - expected LineString geometry");
            }
            xy[i * 2 + 0] = x.asDouble();
            xy[i * 2 + 1] = y.asDouble();
        }
        return xy;
    }

    protected int getNumPoints(JsonNode props) throws ActionParamsException {
        if (props == null || !props.has(JSON_PROPERTY_NUM_POINTS)) {
            return 0;
        }
        JsonNode numPoints = props.get(JSON_PROPERTY_NUM_POINTS);
        if (!numPoints.isIntegralNumber()) {
            throw new ActionParamsException(String.format(
                    "Invalid property value '%s'", JSON_PROPERTY_NUM_POINTS));
        }
        return Math.min(numPoints.asInt(), NUM_POINTS_MAX);
    }

    protected double getScaleFactor(JsonNode props) {
        double fallback = 0.0;
        if (props == null || !props.has(JSON_PROPERTY_SCALE_FACTOR)) {
            return fallback;
        }
        return props.get(JSON_PROPERTY_SCALE_FACTOR).asDouble(fallback);
    }

    private MathTransform getTransform(String serviceCrs, ActionParameters params) throws ActionParamsException {
        try {
            String targetSRS = params.getHttpParam(ActionConstants.PARAM_SRS, DEFAULT_SRS);
            if (targetSRS.equals(serviceSrs)) {
                return null;
            }
            CoordinateReferenceSystem fromCRS = CRS.decode(serviceCrs);
            CoordinateReferenceSystem toCRS = CRS.decode(targetSRS);
            boolean lenient = true; // allow for some error due to different datums
            return CRS.findMathTransform(fromCRS, toCRS, lenient);
        } catch (FactoryException e) {
            throw new ActionParamsException("Invalid " + ActionConstants.PARAM_SRS);
        }
    }

    private void transformInPlace(double[] xy, MathTransform transform) throws ActionException {
        try {
            transform.transform(xy, 0, xy, 0, xy.length / 2);
        } catch (TransformException e) {
            throw new ActionException(e.getMessage(), e);
        }
    }

    private void transformInPlace(List<DataPoint> dp, MathTransform transform) throws ActionException {
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

    protected void writeResponse(ActionParameters params, List<DataPoint> dp) throws ActionException {
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
