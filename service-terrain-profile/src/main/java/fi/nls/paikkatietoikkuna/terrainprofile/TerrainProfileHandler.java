package fi.nls.paikkatietoikkuna.terrainprofile;

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

    protected static final String JSON_PROPERTY_RESOLUTION = "resolution";
    protected static final String JSON_PROPERTY_NUM_POINTS = "numPoints";
    protected static final String JSON_PROPERTY_DISTANCE_FROM_START = "distanceFromStart";

    private static final int NUM_POINTS_MAX = 1000;

    private final ObjectMapper om;
    private TerrainProfileService tps;

    public TerrainProfileHandler() {
        // ServiceLoader/annotation based setup requires a no-param constructor
        this(new ObjectMapper(), null);
    }

    public TerrainProfileHandler(ObjectMapper om, TerrainProfileService tps) {
        this.om = om;
        this.tps = tps;
    }

    @Override
    public void init() {
        super.init();
        // service needs to be created here since exceptions in constructors break the annotation based setup
        if(tps == null) {
            try {
                tps = new TerrainProfileService(
                        PropertyUtil.get(PROPERTY_ENDPOINT),
                        PropertyUtil.get(PROPERTY_DEM_COVERAGE_ID));
            } catch (ServiceException ex) {
                LOG.warn("Unable to init TerrainProfile action route");
                throw new ServiceRuntimeException(ex.getMessage());
            }
        }
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        String routeStr = params.getRequiredParam(PARAM_ROUTE);
        Feature route = parseFeature(routeStr);
        LineString geom = (LineString) route.getGeometry();
        double[] points = GeoJSONHelper.getCoordinates2D(geom);
        int numPoints = Math.min(getNumPoints(route), NUM_POINTS_MAX);
        // double resolution = getResolution(route);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of coords:", (points.length / 2),
                    "line:", Arrays.toString(points), "numPoints", numPoints);
        }

        try {
            List<DataPoint> dp = tps.getTerrainProfile(points, numPoints);
            Feature multiPoint = new Feature();
            multiPoint.setGeometry(GeoJSONHelper.toMultiPoint3D(dp));
            multiPoint.setProperty(JSON_PROPERTY_NUM_POINTS, dp.size());
            // multiPoint.setProperty(JSON_PROPERTY_RESOLUTION, resolution);
            multiPoint.setProperty(JSON_PROPERTY_DISTANCE_FROM_START,
                    dp.stream().mapToDouble(DataPoint::getDistFromStart).toArray());

            writeResponse(params, multiPoint);
        } catch (ServiceException e) {
            throw new ActionException(e.getMessage(), e);
        }
    }

    private void writeResponse(ActionParameters params, Feature multiPoint) throws ActionException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            om.writeValue(baos, multiPoint);
            ResponseHelper.writeResponse(params, 200, IOHelper.CONTENT_TYPE_JSON, baos);
        } catch (IOException e) {
            throw new ActionException("Failed to encode GeoJSON", e);
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

    protected double getResolution(Feature route) throws ActionParamsException {
        Object resolution = route.getProperty(JSON_PROPERTY_RESOLUTION);
        if (resolution == null) {
            throw new ActionParamsException(String.format(
                    "Required property '%s' missing!", JSON_PROPERTY_RESOLUTION));
        }
        if (resolution instanceof Number) {
            return ((Number) resolution).doubleValue();
        }
        if (resolution instanceof String) {
            try {
                return Double.parseDouble((String) resolution);
            } catch (NumberFormatException ignore) {}
        }
        throw new ActionParamsException(String.format(
                "Invalid property value '%s'", JSON_PROPERTY_RESOLUTION));
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

}
