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
    protected static final String PROPERTY_DEM_COVERAGE_ID= "terrain.profile.wcs.demCoverageId";

    protected static final String JSON_PROPERTY_RESOLUTION = "resolution";
    protected static final String JSON_PROPERTY_DISTANCE_FROM_START = "distanceFromStart";

    private final ObjectMapper om;
    private final TerrainProfileService tps;

    public TerrainProfileHandler() throws ServiceException {
        this(new ObjectMapper(), new TerrainProfileService(
                PropertyUtil.get(PROPERTY_ENDPOINT),
                PropertyUtil.get(PROPERTY_DEM_COVERAGE_ID)));
    }

    public TerrainProfileHandler(ObjectMapper om, TerrainProfileService tps) {
        this.om = om;
        this.tps = tps;
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        String routeStr = params.getRequiredParam(PARAM_ROUTE);
        Feature route = parseFeature(routeStr);
        LineString geom = (LineString) route.getGeometry();
        double[] points = GeoJSONHelper.getCoordinates2D(geom);
        double resolution = getResolution(route);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of coords:", (points.length / 2),
                    "line:", Arrays.toString(points), "resolution", resolution);
        }

        try {
            List<DataPoint> dp = tps.getTerrainProfile(points);
            Feature multiPoint = new Feature();
            multiPoint.setGeometry(GeoJSONHelper.toMultiPoint3D(dp));
            multiPoint.setProperty(JSON_PROPERTY_RESOLUTION, resolution);
            multiPoint.setProperty(JSON_PROPERTY_DISTANCE_FROM_START,
                    dp.stream().mapToDouble(DataPoint::getDistFromStart).toArray());

            writeResponse(params, multiPoint);
        } catch (ServiceException e) {
            throw new ActionException(e.getMessage());
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
                throw new ActionParamsException("Invalid input - expected LineString geometry");
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

}
