package fi.nls.paikkatietoikkuna.coordtransform;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;


/**
 * Handles CoordinateTransformation action_route requests
 */
@OskariActionRoute("CoordinateTransformation")
public class CoordinateTransformationActionHandler extends RestActionHandler {
    protected static final Logger log = LogFactory.getLogger(CoordinateTransformationActionHandler.class);

    private static final String PROP_END_POINT = "coordtransform.endpoint";
    private static final String PROP_MAX_FILE_SIZE_MB = "coordtransform.max.filesize.mb";
    private static final String PROP_MAX_COORDS_FILE_TO_ARRAY = "coordtransform.max.coordinates.array";

    protected static final String PARAM_SOURCE_CRS = "sourceCrs";
    protected static final String PARAM_SOURCE_H_CRS = "sourceHeightCrs";
    protected static final String PARAM_TARGET_CRS = "targetCrs";
    protected static final String PARAM_TARGET_H_CRS = "targetHeightCrs";
    protected static final String PARAM_TRANSFORM_TYPE = "transformType";
    protected static final String PARAM_SOURCE_DIMENSION = "sourceDimension";
    protected static final String PARAM_TARGET_DIMENSION = "targetDimension";
    protected static final String KEY_IMPORT_SETTINGS = "importSettings";
    protected static final String KEY_EXPORT_SETTINGS = "exportSettings";

    protected static final String RESPONSE_COORDINATES = "resultCoordinates";
    protected static final String RESPONSE_INPUT_COORDINATES = "inputCoordinates";
    protected static final String RESPONSE_DIMENSION = "dimension";

    protected static final String DEGREE = "degree";
    protected static final String METRIC = "metric";
    protected static final String FILE_EXT = "txt";
    protected static final String FILE_TYPE = "text/plain";
    protected static final String KEY_FOR_ERRORS = "errorKey";

    protected final Map <String, String> lineSeparators = new HashMap<String, String>();
    protected final Map <String, String> coordinateSeparators = new HashMap<String, String>();

    private JsonFactory jf;
    private String endPoint;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int MB = 1024 * 1024;
    private final int maxFileSize = PropertyUtil.getOptional(PROP_MAX_FILE_SIZE_MB, 50) * MB;
    private final int maxCoordsF2A = PropertyUtil.getOptional(PROP_MAX_COORDS_FILE_TO_ARRAY, 100);

    // Store files smaller than 128kb in memory instead of writing them to disk
    private static final int MAX_SIZE_MEMORY = 128 * 1024;
    private final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(MAX_SIZE_MEMORY, null);

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
        lineSeparators.put("win","\r\n");
        lineSeparators.put("mac","\n");
        lineSeparators.put("unix","\r");

        coordinateSeparators.put("space", " ");
        coordinateSeparators.put("tab", "\t");
        coordinateSeparators.put("comma", ",");
        coordinateSeparators.put("semicolon", ";");
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        String transformType = params.getHttpParam(PARAM_TRANSFORM_TYPE);
        if ("F2R".equals(transformType)){ // parse file to array without transformation
            readFileToJsonResonse(params);
            return;
        }
        String sourceCrs = getSourceCrs(params);
        String targetCrs = getTargetCrs(params);

        int sourceDimension = params.getRequiredParamInt(PARAM_SOURCE_DIMENSION);
        int targetDimension = params.getRequiredParamInt(PARAM_TARGET_DIMENSION);
        boolean transformToFile = false;
        boolean hasMoreCoordinates = false;
        int queryDimension = sourceDimension;
        boolean addZeroes = false;
        if (sourceDimension == 2 && targetDimension == 3){
            addZeroes = true;
            queryDimension = 3; //parse added zeroes to query
            sourceCrs = sourceCrs + ",EPSG:3900"; //add N2000 that coordtrans service doesn't fail
        }
        List<Coordinate> coords;

        List<FileItem> fileItems;
        Map<String, String> formParams;
        CoordTransFile importSettings = null;
        CoordTransFile exportSettings = null;
        FileItem file;
        //TODO: is there better way to get transformation type??
        switch(transformType){
            case "A2A": //From coordinate array to coordinate array transform
                coords = getCoordsFromJsonArray (params, sourceDimension, addZeroes);
                break;
            case "A2F": //From coordinate array to file transform
                transformToFile = true;
                try {
                    exportSettings = mapper.readValue(params.getHttpParam(KEY_EXPORT_SETTINGS), CoordTransFile.class);
                } catch (Exception e) {
                    throw new ActionParamsException("Invalid export file settings", createErrorResponse("invalid_export_settings", e));
                }
                coords = getCoordsFromJsonArray (params, sourceDimension, addZeroes);
                break;
            case "F2A": //From file to coordinate array transform
                fileItems = getFileItems(params.getRequest());
                formParams = getFormParams(fileItems);
                file = getFile(fileItems);
                importSettings = getFileSettings(formParams, KEY_IMPORT_SETTINGS);
                coords = getCoordsFromFile(importSettings, file, sourceDimension, addZeroes, false, maxCoordsF2A);
                hasMoreCoordinates = importSettings.isHasMoreCoordinates();
                break;
            case "F2F": //From file to file transform
                transformToFile = true;
                fileItems = getFileItems(params.getRequest());
                formParams = getFormParams(fileItems);
                file = getFile(fileItems);
                importSettings = getFileSettings(formParams, KEY_IMPORT_SETTINGS);
                exportSettings = getFileSettings(formParams, KEY_EXPORT_SETTINGS);
                coords = getCoordsFromFile(importSettings, file, sourceDimension, addZeroes, exportSettings.isWriteLineEndings(), Integer.MAX_VALUE);
                exportSettings.copyArrays(importSettings);
                break;
            default:
                throw new ActionParamsException("Unknown transform type");
        }

        if (coords.isEmpty()){
            throw new ActionParamsException("No coordinates", createErrorResponse("no_coordinates"));
        }

        transform(sourceCrs, targetCrs, queryDimension, targetDimension, coords);

        HttpServletResponse response = params.getResponse();
        if (transformToFile){
            String fileName = addFileExt(exportSettings.getFileName());
            response.setContentType(FILE_TYPE);
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        } else {
            response.setContentType(IOHelper.CONTENT_TYPE_JSON);
        }

        try (OutputStream out = response.getOutputStream()) {
            if (transformToFile){
                writeFileResponse(out, coords, targetDimension, exportSettings, targetCrs);
            } else {
                writeJsonResponse(out, coords, targetDimension, hasMoreCoordinates);
            }
        } catch (IOException e) {
            throw new ActionException("Failed to write JSON to client", e);
        }
    }

    protected void transform(String sourceCrs, String targetCrs,
            int queryDimension, int targetDimension,
            List<Coordinate> coords) throws ActionException {
        CoordTransQueryBuilder queryBuilder = new CoordTransQueryBuilder(endPoint, sourceCrs, targetCrs, queryDimension);

        List<Coordinate> batch = new ArrayList<>();
        for (Coordinate c : coords) {
            boolean fit = queryBuilder.add(c);
            if (!fit) {
                transform(queryBuilder.build(), batch, targetDimension);
                queryBuilder.reset();
                batch.clear();
                queryBuilder.add(c);
            }
            batch.add(c);
        }
        transform(queryBuilder.build(), batch, targetDimension);
    }

    protected void transform(String query, List<Coordinate> batch, int dimension) throws ActionException {
        if (batch.size() == 0) {
            return;
        }

        HttpURLConnection conn;
        try {
            conn = IOHelper.getConnection(query);
        } catch (IOException e) {
            throw new ActionException("Failed to connect to CoordTrans service", e);
        }
        byte[] serviceResponseBytes;
        try {
            serviceResponseBytes = IOHelper.readBytes(conn);
        } catch (IOException e) {
            throw new ActionException("Failed to read response from CoordTrans service", e);
        }

        try {
            // Change Coordinate.xyz values in place
            CoordTransService.parseResponse(serviceResponseBytes, batch, dimension);
        } catch (IllegalArgumentException e) {
            throw new ActionParamsException("Failed to make transformation", createErrorResponse("transformation_error", e)); //error from transformation service
        }
    }

    private List<Coordinate> getCoordsFromJsonArray (ActionParameters params, int dimension, boolean addZeroes) throws ActionException{
        try (InputStream in = params.getRequest().getInputStream()) {
            return parseInputCoordinates(in, dimension, addZeroes);
        } catch (IOException e) {
            throw new ActionException("Failed to parse input JSON!", e);
        } catch (ActionParamsException e) {
            throw new ActionParamsException (e.getMessage(), createErrorResponse("invalid_coord_in_array", e));
        }
    }

    protected List<Coordinate> getCoordsFromFile(CoordTransFile sourceOptions, FileItem file,
            int dimension, boolean addZeroes, boolean storeLineEnds, int limit) throws ActionException {
        List<Coordinate> coordinates = new ArrayList<>();
        String line = "";
        String[] coords;
        int xIndex = 0;
        int yIndex = 1;
        int zIndex = 2;
        int coordDimension = dimension;
        int headerLineCount = sourceOptions.getHeaderLineCount();
        String coordSeparator = sourceOptions.getCoordinateSeparator();
        if (!coordinateSeparators.containsKey(coordSeparator)){
            throw new ActionParamsException("Invalid coordinate separator: " + coordSeparator);
        }
        // get actual separator
        coordSeparator = coordinateSeparators.get(coordSeparator);
        if (sourceOptions.isAxisFlip()){
            xIndex = 1;
            yIndex = 0;
        }
        if (sourceOptions.isPrefixId()){
            xIndex++;
            yIndex++;
            zIndex++;
            coordDimension++;
        }
        boolean replaceCommas = false;
        if (sourceOptions.getDecimalSeparator()==','){
            replaceCommas = true;
        }
        String unit = sourceOptions.getUnit();
        boolean transformUnit = false;
        if (unit != null && !unit.equals(DEGREE) && !unit.equals(METRIC)){
            transformUnit = true;
        }
        double x,y,z;
        int lineIndex = 1;
        boolean firstCoord = true;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            //skip row and store row as header row
            for (int i = 0 ; i < headerLineCount && (line=br.readLine())!=null ;i++) {
                sourceOptions.addHeaderRow(line);
                lineIndex++;
            }
            while ((line=br.readLine())!=null) {
                /* Now coordinate separator comes from frontend
                //try to get coordinate separator from first coordinate line
                if(coordSeparator==null){
                    coordSeparator = CoordTransService.getCoordSeparator(line, dimension, sourceOptions.isPrefixId());
                    sourceOptions.setCoordinateSeparator(coordSeparator);
                }*/
                //skip empty lines
                if (line.trim().isEmpty()){
                    continue;
                }
                //replace commas
                if (replaceCommas){
                    line = line.replace(',','.');
                }
                coords = line.split(coordSeparator);
                if (coords.length < coordDimension){
                    if (firstCoord == true){
                        throw new ActionParamsException("Couldn't parse coordinate on the first line", createErrorResponse("invalid_first_coord"));
                    }
                    throw new ActionParamsException("Invalid coordinate line", createErrorInLineResponse(lineIndex, line, null));
                }
                if (transformUnit){
                    x = CoordTransService.transformUnitToDegree (coords[xIndex], unit);
                    y = CoordTransService.transformUnitToDegree (coords[yIndex], unit);
                } else {
                    x = Double.valueOf(coords[xIndex]);
                    y = Double.valueOf(coords[yIndex]);
                }
                if (dimension == 3){
                    z = Double.valueOf(coords[zIndex]);
                    coordinates.add(new Coordinate(x, y, z));
                }else if (addZeroes == true){
                    coordinates.add(new Coordinate(x, y, 0));
                } else {
                    coordinates.add(new Coordinate(x, y));
                }
                if (sourceOptions.isPrefixId()){
                    sourceOptions.addId(coords[0]);
                }
                if (storeLineEnds == true){
                    String lineEnd = "";
                    //add coordSeparator back if lineEnding string is slitted (e.g. coordSeparator is " ")
                    for (int i = coordDimension; i < coords.length; i++){
                        if (i == coordDimension){
                            lineEnd += coords[i];
                        } else {
                            lineEnd += coordSeparator + coords[i];
                        }
                    }
                    sourceOptions.addLineEnd(lineEnd);
                }
                if (coordinates.size() == limit) {
                    sourceOptions.setHasMoreCoordinates(true);
                    break;
                }
                firstCoord = false;
                lineIndex++;
            }
        } catch (IOException e){
            throw new ActionParamsException("Invalid file", createErrorResponse("invalid_file", e));
        } catch (NumberFormatException e){
            throw new ActionParamsException("Expected a number", createErrorInLineResponse(lineIndex, line, e));
        } catch (IndexOutOfBoundsException e){
            throw new ActionParamsException("Index out of bounds", createErrorInLineResponse(lineIndex, line, e));
        }
        return coordinates;
    }

    private CoordTransFile getFileSettings(Map<String, String> formParams, String key) throws ActionParamsException {
        try {
            return mapper.readValue(formParams.get(key), CoordTransFile.class);
        } catch (Exception e) {
            throw new ActionParamsException("Invalid file settings: " + key, createErrorResponse("invalid_file_settings", e));
        }
    }
    private JSONObject createErrorInLineResponse (int lineIndex, String line, Exception e){
        JSONObject jsonError = JSONHelper.createJSONObject(KEY_FOR_ERRORS, "invalid_coord_in_line");
        JSONHelper.putValue(jsonError, "line", line);
        JSONHelper.putValue(jsonError, "lineIndex", lineIndex);
        if (e != null){
            JSONHelper.putValue(jsonError, "exception", e.getMessage());
        }
        return jsonError;
    }
    private JSONObject createErrorResponse (String errorKey, Exception e){
        JSONObject jsonError = JSONHelper.createJSONObject(KEY_FOR_ERRORS, errorKey);
        if (e != null){
            JSONHelper.putValue(jsonError, "exception", e.getMessage());
        }
        return jsonError;
    }
    private JSONObject createErrorResponse (String errorKey){
        return createErrorResponse (errorKey, null);
    }

    private List<FileItem> getFileItems(HttpServletRequest request) throws ActionException {
        try {
            request.setCharacterEncoding("UTF-8");
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
            upload.setSizeMax(maxFileSize);
            return upload.parseRequest(request);
        } catch (UnsupportedEncodingException | FileUploadException e) {
            throw new ActionException("Failed to read request", e);
        }
    }
    private Map<String, String> getFormParams(List<FileItem> fileItems) {
        return fileItems.stream()
                .filter(f -> f.isFormField())
                .collect(Collectors.toMap(
                        f -> f.getFieldName(),
                        f -> new String(f.get(), StandardCharsets.UTF_8)));
    }
    private FileItem getFile (List<FileItem> fileItems) throws ActionParamsException {
        return fileItems.stream()
            .filter(f -> !f.isFormField())
            .findAny() // If there are more files we'll get the file or fail miserably
            .orElseThrow(() -> new ActionParamsException("No file entry", createErrorResponse("no_file")));
    }
    //add .txt if missing
    private String addFileExt(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0 || i + 1 == name.length()) {
            return name + "." + FILE_EXT;
        }
        if (FILE_EXT.equals(name.substring(i + 1))){
            return name;
        } else {
            return name + "." + FILE_EXT;
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
    protected void readFileToJsonResonse (ActionParameters params) throws ActionException {
        List<FileItem> fileItems = getFileItems(params.getRequest());
        Map<String, String> formParams = getFormParams(fileItems);
        FileItem file = getFile(fileItems);
        CoordTransFile sourceOptions = getFileSettings(formParams, KEY_IMPORT_SETTINGS);
        int dimension = params.getHttpParam(PARAM_SOURCE_DIMENSION, 2);
        HttpServletResponse response = params.getResponse();
        response.setContentType(IOHelper.CONTENT_TYPE_JSON);
        boolean hasMoreCoordinates = false;

        String line;
        int xIndex = 0;
        int yIndex = 1;
        int zIndex = 2;
        int coordDimension = dimension;
        int headerLineCount = sourceOptions.getHeaderLineCount();
        String coordSeparator = sourceOptions.getCoordinateSeparator();
        boolean firstLine = true;
        if (!coordinateSeparators.containsKey(coordSeparator)){
            throw new ActionParamsException("Invalid coordinate separator: " + coordSeparator);
        }
        // get actual separator
        coordSeparator = coordinateSeparators.get(coordSeparator);
        if (sourceOptions.isAxisFlip()){
            xIndex = 1;
            yIndex = 0;
        }
        if (sourceOptions.isPrefixId()){
            xIndex++;
            yIndex++;
            zIndex++;
            coordDimension++;
        }
        try (OutputStream out = response.getOutputStream()) {
            try (JsonGenerator json = jf.createGenerator(out)) {
                json.writeStartObject();
                json.writeNumberField(RESPONSE_DIMENSION, dimension);
                json.writeFieldName(RESPONSE_INPUT_COORDINATES);
                json.writeStartArray();
                try(BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))){
                    for (int i = 0 ; (line=br.readLine())!=null ; i++) {
                        // skip header and empty rows
                        if (i < headerLineCount || line.trim().isEmpty()){
                            continue;
                        }
                        String [] coord = line.split(coordSeparator);
                        if (coord.length < coordDimension){
                            json.writeEndArray();
                            if(firstLine == true){
                                writeJsonError(json, "invalid_first_coord");
                                throw new ActionParamsException("Couldn't parse coordinate on the first line", createErrorResponse("invalid_first_coord"));
                            }
                            int lineNumber = i + 1;
                            writeInLineJsonError(json, lineNumber, line);
                            throw new ActionParamsException("Invalid coordinate line", createErrorInLineResponse(lineNumber, line, null));
                        }
                        json.writeStartArray();
                        json.writeString(coord[xIndex]);
                        json.writeString(coord[yIndex]);
                        if (dimension == 3){
                            json.writeString(coord[zIndex]);
                        }
                        json.writeEndArray();
                        firstLine = false;
                        if (i+1+headerLineCount == maxCoordsF2A){
                            hasMoreCoordinates = true;
                            break;
                        }
                    }
                } catch (IOException e){
                    json.writeEndArray();
                    writeJsonError(json, "invalid_file");
                    throw new ActionParamsException("Invalid file", createErrorResponse("invalid_file", e));
                }
                json.writeEndArray();
                json.writeBooleanField("hasMoreCoordinates", hasMoreCoordinates);
                json.writeEndObject();
            } catch (IOException e) {
                throw new ActionException("Failed to write JSON");
            }
        } catch (IOException e) {
            throw new ActionException("Failed to write JSON to client");
        }
    }
    private void writeInLineJsonError (JsonGenerator json, int lineNumber, String line) throws IOException {
        json.writeFieldName("error");
        json.writeStartObject();
        json.writeNumberField("lineIndex", lineNumber);
        json.writeStringField(KEY_FOR_ERRORS, "invalid_read_line");
        json.writeStringField("line", line);
        json.writeEndObject();
    }
    private void writeJsonError (JsonGenerator json, String errorKey) throws IOException {
        json.writeFieldName("error");
        json.writeStartObject();
        json.writeStringField(KEY_FOR_ERRORS, errorKey);
        json.writeEndObject();
    }

    protected List<Coordinate> parseInputCoordinates(final InputStream in, final int dimension, final boolean addZeroes)
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
                if (dimension == 2) {
                    if (addZeroes == true){
                        coordinates.add(new Coordinate(x, y, 0));
                    }else{
                        coordinates.add(new Coordinate(x, y));
                    }
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
            throw new ActionParamsException(err, "invalid_number");
        }
    }

    protected void writeJsonResponse(OutputStream out, List<Coordinate> coords, final int dimension, final boolean hasMoreCoordinates)
            throws ActionException {
        try (JsonGenerator json = jf.createGenerator(out)) {
            json.writeStartObject();
            json.writeNumberField(RESPONSE_DIMENSION, dimension);
            json.writeBooleanField("hasMoreCoordinates", hasMoreCoordinates);
            json.writeFieldName(RESPONSE_COORDINATES);
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


    protected void writeFileResponse(OutputStream out, List<Coordinate> coords, final int dimension, CoordTransFile opts, String crs)
        throws ActionException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out))){
            String xCoord;
            String yCoord;
            String zCoord;
            String lineSeparator = lineSeparators.get(opts.getLineSeparator());
            String coordSeparator = coordinateSeparators.get(opts.getCoordinateSeparator());
            int decimals = opts.getDecimalCount();
            boolean replaceCommas = opts.getDecimalSeparator() == ',';
            boolean prefixId = opts.isPrefixId();
            boolean flipAxis = opts.isAxisFlip();
            boolean prefixWithIndex = false;
            boolean writeCardinals = opts.isWriteCardinals();
            List <String> ids = opts.getIds();
            List <String> lineEndings = opts.getLineEnds();
            boolean writeEndings = opts.isWriteLineEndings() && !lineEndings.isEmpty();
            String unit = opts.getUnit();
            boolean transformUnit = false;
            if (unit != null && !unit.equals(DEGREE)){
                transformUnit = true;
            }
            if (opts.isPrefixId()){
                if(ids.isEmpty()){
                    prefixWithIndex = true;
                }
            }
            // TODO: should we add only: Coordinate Reference System: KKJ
            // if we want localized header then frontend should send header String instead of boolean
            if (opts.isWriteHeader()){
                bw.write("Coordinate Reference System:" + crs);
                bw.write(lineSeparator);
                for (String headerRow : opts.getHeaderRows()){
                    bw.write(headerRow);
                    bw.write(lineSeparator);
                }
            }
            for (int i = 0; i < coords.size() ; i++) {
                Coordinate coord = coords.get(i);
                if (transformUnit){
                    xCoord = CoordTransService.transformDegreeToUnit(coord.x, unit, decimals);
                    yCoord = CoordTransService.transformDegreeToUnit(coord.y, unit, decimals);
                } else {
                    xCoord = CoordTransService.round(coord.x, decimals);
                    yCoord = CoordTransService.round(coord.y, decimals);
                }
                if (replaceCommas){
                    xCoord = xCoord.replace('.',',');
                    yCoord = yCoord.replace('.',',');
                }
                if (writeCardinals){
                    if (xCoord.indexOf('-') == 0 ){
                        xCoord = xCoord.substring(1) + "W";
                    } else {
                        xCoord += "E";
                    }
                    if (yCoord.indexOf('-') == 0 ){
                        yCoord = yCoord.substring(1) + "S";
                    } else {
                        yCoord += "N";
                    }
                }
                if (prefixId && prefixWithIndex){
                    bw.write(i + coordSeparator);
                } else if (prefixId){
                    bw.write(ids.get(i) + coordSeparator);
                }
                if (flipAxis){
                    bw.write(yCoord);
                    bw.write(coordSeparator);
                    bw.write(xCoord);
                }else {
                    bw.write(xCoord);
                    bw.write(coordSeparator);
                    bw.write(yCoord);
                }
                if (dimension == 3) {
                    zCoord = CoordTransService.round(coord.z, decimals);
                    if (replaceCommas){
                        zCoord = zCoord.replace('.',',');
                    }
                    bw.write(coordSeparator);
                    bw.write(zCoord);
                }
                if (writeEndings){
                    bw.write(coordSeparator);
                    bw.write(lineEndings.get(i));
                }
                bw.write(lineSeparator);
            }
        } catch (IOException e) {
            throw new ActionException("Failed to write file", e);
        }
    }
}
