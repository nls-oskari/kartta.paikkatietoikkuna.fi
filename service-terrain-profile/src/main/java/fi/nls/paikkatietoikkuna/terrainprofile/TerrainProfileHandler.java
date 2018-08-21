package fi.nls.paikkatietoikkuna.terrainprofile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.geojson.Feature;
import org.geojson.LineString;

@OskariActionRoute("TerrainProfile")
public class TerrainProfileHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(TerrainProfileHandler.class);

    protected static final String PARAM_ROUTE = "route";

    protected static final String PROPERTY_ENDPOINT = "terrain.profile.wcs.endPoint";
    protected static final String PROPERTY_DEM_COVERAGE_ID = "terrain.profile.wcs.demCoverageId";
    protected static final String PROPERTY_NODATA_VALUE = "terrain.profile.wcs.noData";

    protected static final String JSON_PROPERTY_NUM_POINTS = "numPoints";
    protected static final String JSON_PROPERTY_SCALE_FACTOR = "scaleFactor";
    protected static final String JSON_PROPERTY_DISTANCE_FROM_START = "distanceFromStart";

    private static final int NUM_POINTS_MAX = 1000;

    private final ObjectMapper om;
    private TerrainProfileService tps;
    private float noDataValue;

    public TerrainProfileHandler() {
        this(new ObjectMapper(), null);
    }

    public TerrainProfileHandler(ObjectMapper om, TerrainProfileService tps) {
        this.om = om;
        this.tps = tps;
    }

    @Override
    public void init() {
        if (tps == null) {
            try {
                tps = new TerrainProfileService(
                        PropertyUtil.get(PROPERTY_ENDPOINT),
                        PropertyUtil.get(PROPERTY_DEM_COVERAGE_ID));
            } catch (ServiceException ex) {
                throw new ServiceRuntimeException(
                        "Failed to init TerrainProfileService: " + ex.getMessage(), ex);
            }
        }
        noDataValue = getNoDataValue();
        LOG.debug("NODATA value:", noDataValue);
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
        String routeStr = params.getRequiredParam(PARAM_ROUTE);
        Feature route = parseFeature(routeStr);
        LineString geom = (LineString) route.getGeometry();
        double[] points = GeoJSONHelper.getCoordinates2D(geom);
        int numPoints = Math.min(getNumPoints(route), NUM_POINTS_MAX);
        double scaleFactor = getScaleFactor(route);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of coords:", (points.length / 2),
                    "line:", Arrays.toString(points), "numPoints", numPoints);
        }

        try {
            writeResponse(params, tps.getTerrainProfile(points, numPoints, scaleFactor));
        } catch (ServiceException e) {
            throw new ActionException(e.getMessage(), e);
        }
    }

    protected Feature parseFeature(String routeStr) throws ActionParamsException {
        try {
            Feature route = om.readValue(routeStr, Feature.class);
            if (!(route.getGeometry() instanceof LineString)) {
                throw new ActionParamsException("Invalid input"
                        + " - expected LineString geometry");
            }
            LineString ls = (LineString) route.getGeometry();
            int numPoints = ls.getCoordinates().size();
            if (numPoints < 2) {
                throw new ActionParamsException("Invalid input"
                        + " - expected LineString with atleast two coordinates");
            }
            if (numPoints > NUM_POINTS_MAX) {
                throw new ActionParamsException("Invalid input"
                        + " - too many coordinates, maximum is " + NUM_POINTS_MAX);
            }
            return route;
        } catch (IllegalArgumentException | IOException e) {
            throw new ActionParamsException("Invalid input - expected GeoJSON feature", e);
        }
    }

    protected int getNumPoints(Feature route) throws ActionParamsException {
        Object numPoints = route.getProperty(JSON_PROPERTY_NUM_POINTS);
        if (numPoints == null) {
            return 0;
        }
        if (numPoints instanceof Number) {
            return ((Number) numPoints).intValue();
        }
        if (numPoints instanceof String) {
            try {
                return Integer.parseInt((String) numPoints);
            } catch (NumberFormatException ignore) {}
        }
        throw new ActionParamsException(String.format(
                "Invalid property value '%s'", JSON_PROPERTY_NUM_POINTS));
    }

    protected double getScaleFactor(Feature route) {
        Object scaleFactor = route.getProperty(JSON_PROPERTY_SCALE_FACTOR);
        if (scaleFactor != null) {
            if (scaleFactor instanceof Number) {
                return ((Number) scaleFactor).doubleValue();
            }
            if (scaleFactor instanceof String) {
                try {
                    return Double.parseDouble((String) scaleFactor);
                } catch (NumberFormatException ignore) {
                    // Ignore
                }
            }
        }
        return 0;
    }

    protected void writeResponse(ActionParameters params, List<DataPoint> dp) throws ActionException {
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
