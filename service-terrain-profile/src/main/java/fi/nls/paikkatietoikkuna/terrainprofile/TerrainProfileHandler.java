package fi.nls.paikkatietoikkuna.terrainprofile;

import org.hamcrest.core.Is;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.ResponseHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.geojson.Feature;
import org.geojson.LineString;

@OskariActionRoute("TerrainProfile")
public class TerrainProfileHandler extends ActionHandler {

    private static final Logger LOG = LogFactory.getLogger(TerrainProfileHandler.class);

    protected static final String PARAM_ROUTE = "route";

    protected static final String PROPERTY_RESOLUTION = "resolution";
    protected static final String PROPERTY_DISTANCE_FROM_START = "distanceFromStart";

    private final ObjectMapper om;

    public TerrainProfileHandler() {
        this(new ObjectMapper());
    }

    public TerrainProfileHandler(ObjectMapper om) {
        this.om = om;
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionParamsException {
        String routeStr = params.getRequiredParam(PARAM_ROUTE);
        Feature route = parseFeature(routeStr);
        LineString geom = (LineString) route.getGeometry();
        double[] points = GeoJSONHelper.getCoordinates2D(geom);
        double resolution = getResolution(route);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Number of coords:", (points.length / 2),
                    "line:", Arrays.toString(points), "resolution", resolution);
        }

        double[] terrainProfile = TerrainProfile.getTerrainProfile(points, resolution);
        double[] distanceFromStart = TerrainProfile.calculateDistanceFromStart(terrainProfile);

        Feature multiPoint = new Feature();
        multiPoint.setGeometry(GeoJSONHelper.toMultiPoint3D(terrainProfile));
        multiPoint.setProperty(PROPERTY_RESOLUTION, resolution);
        multiPoint.setProperty(PROPERTY_DISTANCE_FROM_START, distanceFromStart);

        writeResponse(params, multiPoint);
    }

    private void writeResponse(ActionParameters params, Feature multiPoint) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            om.writeValue(baos, multiPoint);
        } catch (IOException e) {
            ResponseHelper.writeError(params, "Failed to encode GeoJSON");
            return;
        }
        ResponseHelper.writeResponse(params, 200, IOHelper.CONTENT_TYPE_JSON, baos);
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
        Object resolution = route.getProperty(PROPERTY_RESOLUTION);
        if (resolution == null) {
            throw new ActionParamsException(String.format(
                    "Required property '%s' missing!", PROPERTY_RESOLUTION));
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
                "Invalid property value '%s'", PROPERTY_RESOLUTION));
    }

}
