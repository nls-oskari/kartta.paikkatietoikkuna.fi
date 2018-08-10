package fi.nls.paikkatietoikkuna.coordtransform;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles CoordinateTransformation action_route requests
 */
@OskariActionRoute("CoordinateTransformation")
public class CoordinateTransformationActionHandler extends RestActionHandler {

    private static final String PROP_END_POINT = "coordtransform.endpoint";

    protected static final String PARAM_SOURCE_CRS = "sourceCrs";
    protected static final String PARAM_SOURCE_H_CRS = "sourceHeightCrs";
    protected static final String PARAM_TARGET_CRS = "targetCrs";
    protected static final String PARAM_TARGET_H_CRS = "targetHeightCrs";

    private JsonFactory jf;
    private String endPoint;

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
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        String sourceCrs = getSourceCrs(params);
        String targetCrs = getTargetCrs(params);
        int dimension = sourceCrs.indexOf(',') > 0 ? 3 : 2;

        List<Coordinate> coords;
        try (InputStream in = params.getRequest().getInputStream()) {
            coords = parseInputCoordinates(in, dimension);
        } catch (IOException e) {
            throw new ActionException("Failed to parse input JSON!");
        }

        String query = CoordTransService.createQuery(sourceCrs, targetCrs, coords, dimension);

        HttpURLConnection conn;
        try {
            conn = IOHelper.getConnection(endPoint + query);
        } catch (IOException e) {
            throw new ActionException("Failed to connect to CoordTrans service");
        }

        byte[] serviceResponseBytes;
        try {
            serviceResponseBytes = IOHelper.readBytes(conn);
        } catch (IOException e) {
            throw new ActionException("Failed to read response from CoordTrans service");
        }

        try {
            // Reuse the double array
            CoordTransService.parseResponse(serviceResponseBytes, coords, dimension);
        } catch (IllegalArgumentException e) {
            throw new ActionException(e.getMessage());
        }

        HttpServletResponse response = params.getResponse();
        response.setContentType(IOHelper.CONTENT_TYPE_JSON);
        try (OutputStream out = response.getOutputStream()) {
            writeJsonResponse(out, coords, dimension);
        } catch (IOException e) {
            throw new ActionException("Failed to write JSON to client");
        }
    }

    private String getSourceCrs(ActionParameters params) throws ActionParamsException {
        String sourceCrs = params.getRequiredParam(PARAM_SOURCE_CRS);
        String sourceHeightCrs = params.getHttpParam(PARAM_SOURCE_H_CRS);
        if (sourceHeightCrs != null && !sourceHeightCrs.isEmpty()) {
            return sourceCrs + ',' + sourceHeightCrs;
        }
        return sourceCrs;
    }

    private String getTargetCrs(ActionParameters params) throws ActionParamsException {
        String targetCrs = params.getRequiredParam(PARAM_TARGET_CRS);
        String targetHeightCrs = params.getHttpParam(PARAM_TARGET_H_CRS);
        if (targetHeightCrs != null && !targetHeightCrs.isEmpty()) {
            return targetCrs + ',' + targetHeightCrs;
        }
        return targetCrs;
    }


    protected List<Coordinate> parseInputCoordinates(final InputStream in, final int dimension)
            throws IOException, ActionParamsException {
        try (JsonParser parser = jf.createParser(in)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new ActionParamsException("Expected input starting with an array");
            }

            List<Coordinate> coordinates = new ArrayList<>(8);
            JsonToken token;

            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                if (token != JsonToken.START_ARRAY) {
                    throw new ActionParamsException("Expected array opening");
                }

                assertNumber(parser.nextToken(), "Expected a number");
                double x = parser.getDoubleValue();
                assertNumber(parser.nextToken(), "Expected a number");
                double y = parser.getDoubleValue();
                if (dimension == 2) {
                    coordinates.add(new Coordinate(x, y));
                } else {
                    assertNumber(parser.nextToken(), "Expected a number");
                    double z = parser.getDoubleValue();
                    coordinates.add(new Coordinate(x, y, z));
                }

                if (parser.nextToken() != JsonToken.END_ARRAY) {
                    throw new ActionParamsException("Expected array closing");
                }
            }

            return coordinates;
        }
    }

    private void assertNumber(JsonToken token, String err) throws ActionParamsException {
        if (token != JsonToken.VALUE_NUMBER_FLOAT && token != JsonToken.VALUE_NUMBER_INT) {
            throw new ActionParamsException(err);
        }
    }

    protected void writeJsonResponse(OutputStream out, List<Coordinate> coords, final int dimension)
            throws ActionException {
        try (JsonGenerator json = jf.createGenerator(out)) {
            json.writeStartObject();
            json.writeNumberField("dimension", dimension);
            json.writeFieldName("coordinates");
            json.writeStartArray();
            for (Coordinate coord : coords) {
                json.writeStartArray();
                json.writeNumber(coord.x);
                json.writeNumber(coord.y);
                if (dimension == 3) {
                    json.writeNumber(coord.z);
                }
                json.writeEndArray();
            }
            json.writeEndArray();
            json.writeEndObject();
        } catch (IOException e) {
            throw new ActionException("Failed to write JSON");
        }
    }

}
