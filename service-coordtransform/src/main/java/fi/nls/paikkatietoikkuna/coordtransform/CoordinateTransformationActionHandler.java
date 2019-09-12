package fi.nls.paikkatietoikkuna.coordtransform;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.apache.commons.fileupload.FileItem;
import org.json.JSONObject;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;


/**
 * Handles CoordinateTransformation action_route requests
 */
@OskariActionRoute("CoordinateTransformation")
public class CoordinateTransformationActionHandler extends RestActionHandler {

    private static final String PROP_END_POINT = "coordtransform.endpoint";
    private static final String PROP_MAX_COORDS_FILE_TO_ARRAY = "coordtransform.max.coordinates.array";

    protected static final String KEY_FOR_ERRORS = "errorKey";

    private JsonFactory jf;
    private String endPoint;
    private CoordFileHelper fileHelper;
    protected static final ObjectMapper mapper = new ObjectMapper();
    private final int maxCoordsF2A = PropertyUtil.getOptional(PROP_MAX_COORDS_FILE_TO_ARRAY, 100);

    public CoordinateTransformationActionHandler() {
        this(null);
    }

    protected CoordinateTransformationActionHandler(String endPoint) {
        this.jf = new JsonFactory();
        this.endPoint = endPoint;
    }

    @Override
    public void init() {
        if (endPoint == null) {
            endPoint = PropertyUtil.getNecessary(PROP_END_POINT);
        }
        fileHelper = new CoordFileHelper();
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        TransformParams transformParams = new TransformParams(params);
        if (TransformationType.F2R.equals(transformParams.type)) { // parse file to array without transformation
            // basically pretty much the same as any type starting with F
            // FIXME: remove custom handling and use getCoordsFromFile()
            // don't transform F2R coordinates and can't set to Coordinate (double values) because has to be String (24 14 15,35353)
            // check if some common file parsing can be done
            fileHelper.readFileToJsonResponse(transformParams, maxCoordsF2A);
            return;
        }
        String sourceCrs = transformParams.sourceCRS;
        String targetCrs = transformParams.targetCRS;

        int targetDimension = transformParams.outputDimensions;
        boolean addZeroes = transformParams.inputDimensions == 2 && targetDimension == 3;
        if (addZeroes) {
            // add N2000 that coordtrans service doesn't fail
            // TODO: service requires 3 inputs if output is 3 axis???
            // yes, but instead of adding zeroes and source height system here, coordtransform service should add zeroes to result if 2D->3D is really needed
            // also then dimensions must not be sent by frontend
            sourceCrs = sourceCrs + ",EPSG:3900";
        }
        CoordinatesPayload coords = getCoordinatesFromPayload(transformParams);
        if (coords.isEmpty()) {
            throw new ActionParamsException("No coordinates", TransformParams.createErrorResponse("no_coordinates"));
        }
        if (addZeroes) {
            coords.getCoords().forEach(c -> c.setOrdinate(Coordinate.Z, 0));
        }

        CoordTransQueryBuilder queryBuilder = new CoordTransQueryBuilder(endPoint, sourceCrs, targetCrs);
        try {
            CoordTransWorker worker = OskariComponentManager.getComponentOfType(CoordTransWorker.class);
            String jobId = worker.transformAsync(queryBuilder, transformParams, coords);
            ResponseHelper.writeResponse(params, JSONHelper.createJSONObject("jobId", jobId));
        }
        catch (RejectedExecutionException e) {
            throw new ActionParamsException("Service busy", TransformParams.createErrorResponse("service_busy"));
        }
    }

    public CoordinatesPayload getCoordinatesFromPayload(TransformParams params) throws ActionException {
        if (!params.type.isFileInput()) {
            // payload is json
            return getCoordsFromJsonArray(params);
        }

        // There's a file to be parsed
        int resultCount = maxCoordsF2A;
        if (params.type.isFileOutput()) {
            resultCount = Integer.MAX_VALUE;
        }
        return fileHelper.getCoordsFromFile(params, resultCount);
    }

    private CoordinatesPayload getCoordsFromJsonArray(TransformParams params) throws ActionException {
        CoordinatesPayload cp = new CoordinatesPayload();
        if (params.type.isFileOutput()) {
            cp.setExportSettings(params.exportSettings);
        }
        try (InputStream in = params.actionParameters.getRequest().getInputStream()) {
            cp.addCoordinates(parseInputCoordinates(in, params.inputDimensions));
        } catch (IOException e) {
            throw new ActionException("Failed to parse input JSON!", e);
        } catch (ActionParamsException e) {
            throw new ActionParamsException(e.getMessage(), TransformParams.createErrorResponse("invalid_coord_in_array", e));
        }
        return cp;
    }

    /**
     * Parses input stream to a list of coordinates
     *
     * @param in        array of array of numbers [[1,2,3], [4,5,6]]
     * @param dimension if not 2, tries to read third oordinate as z
     * @return list of coordinates parsed from inputstream
     * @throws IOException
     * @throws ActionParamsException
     */
    protected List<Coordinate> parseInputCoordinates(final InputStream in, final int dimension)
            throws IOException, ActionParamsException {
        try (JsonParser parser = jf.createParser(in)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new ActionParamsException("Expected input starting with an array");
            }

            List<Coordinate> coordinates = new ArrayList<>();
            JsonToken token;

            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                if (token != JsonToken.START_ARRAY) {
                    throw new ActionParamsException("Expected array opening");
                }
                assertNumber(parser.nextToken(), "Expected a number");
                double x = parser.getDoubleValue();
                assertNumber(parser.nextToken(), "Expected a number");
                double y = parser.getDoubleValue();
                Coordinate parsed = new Coordinate(x, y);
                if (dimension != 2) {
                    assertNumber(parser.nextToken(), "Expected a number");
                    double z = parser.getDoubleValue();
                    parsed.setOrdinate(Coordinate.Z, z);
                }
                coordinates.add(parsed);

                if (parser.nextToken() != JsonToken.END_ARRAY) {
                    throw new ActionParamsException("Expected array closing");
                }
            }
            return coordinates;
        }
    }

    private void assertNumber(JsonToken token, String err) throws ActionParamsException {
        if (token != JsonToken.VALUE_NUMBER_FLOAT && token != JsonToken.VALUE_NUMBER_INT) {
            throw new ActionParamsException(err, "invalid_number");
        }
    }

}
