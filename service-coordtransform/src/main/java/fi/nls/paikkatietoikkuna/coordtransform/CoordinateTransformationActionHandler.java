package fi.nls.paikkatietoikkuna.coordtransform;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import org.apache.commons.io.IOUtils;


/**
 * Handles CoordinateTransformation action_route requests
 */
@OskariActionRoute("CoordinateTransformation")
public class CoordinateTransformationActionHandler extends ActionHandler {
    protected static final Logger log = LogFactory.getLogger(CoordinateTransformationActionHandler.class);

    private static final String PROP_END_POINT = "coordtransform.endpoint";
    private static final String PROP_MAX_FILE_SIZE_MB = "coordtransform.max.filesize.mb";
    private static final String PROP_MAX_COORDS_TO_ARRAY = "coordtransform.max.coordinates.array";
    private static final String PROP_MAX_COORDS_TO_QUERY = "coordtransform.max.coordinates.query";
    private static final String PROP_COORDS_TO_PREVIEW = "coordtransform.preview.size";

    protected static final String PARAM_SOURCE_CRS = "sourceCrs";
    protected static final String PARAM_SOURCE_H_CRS = "sourceHeightCrs";
    protected static final String PARAM_TARGET_CRS = "targetCrs";
    protected static final String PARAM_TARGET_H_CRS = "targetHeightCrs";
    protected static final String PARAM_TRANSFORM_TYPE = "transformType";
    protected static final String PARAM_SOURCE_DIMENSION = "sourceDimension";
    protected static final String PARAM_TARGET_DIMENSION = "targetDimension";
    protected static final String KEY_IMPORT_SETTINGS = "importSettings";
    protected static final String KEY_EXPORT_SETTINGS = "exportSettings";

    protected static final String RESPONSE_COORDINATES = "coordinates";
    protected static final String RESPONSE_INPUT_COORDINATES = "inputCoordinates";
    protected static final String RESPONSE_DIMENSION = "dimension";

    protected static final String[] DEGREES_TO_FORMAT = new String [] {"DD MM SS", "DD MM", "DDMMSS", "DDMM"};
    protected static final String DEGREE = "degree";
    protected static final String FILE_EXT = "txt";
    protected static final String FILE_TYPE = "text/plain";

    protected final Map <String, String> lineSeparators = new HashMap<String, String>();
    protected final Map <String, String> coordinateSeparators = new HashMap<String, String>();

    private JsonFactory jf;
    private String endPoint;
    private static final ObjectMapper mapper = new ObjectMapper();
    private boolean hasMoreCoordinates;
    private static final int MB = 1024 * 1024;
    private final int maxFileSize = PropertyUtil.getOptional(PROP_MAX_FILE_SIZE_MB, 50) * MB;
    private final int maxCoordsToArray = PropertyUtil.getOptional(PROP_MAX_COORDS_TO_ARRAY, 100);
    private final int coordsToPreview = PropertyUtil.getOptional(PROP_COORDS_TO_PREVIEW, 10);
    private final int maxCoordsToQuery = PropertyUtil.getOptional(PROP_MAX_COORDS_TO_QUERY, 500);

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
    }

    @Override
    public void handleAction(ActionParameters params) throws ActionException {
        hasMoreCoordinates = false;
        String sourceCrs = getSourceCrs(params);
        String targetCrs = getTargetCrs(params);
        String transformType = params.getHttpParam(PARAM_TRANSFORM_TYPE);
        int sourceDimension = params.getRequiredParamInt(PARAM_SOURCE_DIMENSION);
        int targetDimension = params.getRequiredParamInt(PARAM_TARGET_DIMENSION);
        boolean transformToFile = false;
        int queryDimension = sourceDimension;
        boolean addZeroes = false;
        if (sourceDimension == 2 && targetDimension == 3){
            addZeroes = true;
            queryDimension = 3; //parse added zeroes to query
            sourceCrs = sourceCrs + ",EPSG:3900"; //add N2000 that coordtrans service doesn't fail
        }
        List<Coordinate> coords;
        List<Coordinate> inputCoords = null;

        List<FileItem> fileItems;
        Map<String, String> formParams;
        CoordTransFile importSettings;
        CoordTransFile exportSettings = null;
        FileItem file;
        //TODO: is there better way to get transformation type??
        switch(transformType){
            case "A2A":
                coords = getCoordsFromJsonArray (params, sourceDimension, addZeroes);
                break;
            case "A2F":
                transformToFile = true;
                try {
                    exportSettings = mapper.readValue(params.getHttpParam(KEY_EXPORT_SETTINGS), CoordTransFile.class);
                } catch (IOException e) {
                    throw new ActionParamsException("Invalid export file settings", "invalid_export_settings");
                }
                coords = getCoordsFromJsonArray (params, sourceDimension, addZeroes);
                break;
            case "F2A":
                fileItems = getFileItems(params.getRequest());
                formParams = getFormParams(fileItems);
                file = getFile(fileItems);
                importSettings = getFileSettings(formParams, KEY_IMPORT_SETTINGS);
                coords = getCoordsFromFile (importSettings, file, sourceDimension, addZeroes, false);
                //store input coords
                inputCoords = coords.stream().map(c -> new Coordinate (c)).collect(Collectors.toList());
                break;
            case "F2F":
                transformToFile = true;
                fileItems = getFileItems(params.getRequest());
                formParams = getFormParams(fileItems);
                file = getFile(fileItems);
                importSettings = getFileSettings(formParams, KEY_IMPORT_SETTINGS);
                exportSettings = getFileSettings(formParams, KEY_EXPORT_SETTINGS);
                coords = getCoordsFromFile (importSettings, file, sourceDimension, addZeroes, exportSettings.isWriteLineEndings());
                exportSettings.copyArrays(importSettings);
                break;
            default:
                throw new ActionParamsException("Unknown transform type");
        }

        if (coords.isEmpty()){
            throw new ActionParamsException("No coordinates", "no_coordinates");
        }
        //TODO if coords.size() > maxCoordsToQuery -> post or split to multiple queries
        String query = CoordTransService.createQuery(sourceCrs, targetCrs, coords, queryDimension);
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
            CoordTransService.parseResponse(serviceResponseBytes, coords, targetDimension);
        } catch (IllegalArgumentException e) {
            throw new ActionException(e.getMessage());
        }

        HttpServletResponse response = params.getResponse();
        try (OutputStream out = response.getOutputStream()) {
            if (transformToFile){
                String fileName = addFileExt(exportSettings.getFileName());
                writeFile (coords, targetDimension, exportSettings, targetCrs);
                response.setContentType(FILE_TYPE);
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
                response.setHeader("Content-Length", String.valueOf(new File(fileName).length()));
                FileInputStream  fis = new FileInputStream(fileName);
                IOUtils.copy(fis,out);
                out.flush();
            } else {
                response.setContentType(IOHelper.CONTENT_TYPE_JSON);
                writeJsonResponse(out, coords, inputCoords, targetDimension);
            }
        } catch (IOException e) {
            throw new ActionException("Failed to write JSON to client");
        }
    }
    private List<Coordinate> getCoordsFromJsonArray (ActionParameters params, int dimension, boolean addZeroes) throws ActionException{
        List<Coordinate> coords;
        try (InputStream in = params.getRequest().getInputStream()) {
            coords = parseInputCoordinates(in, dimension, addZeroes);
        } catch (IOException e) {
            throw new ActionException("Failed to parse input JSON!");
        }
        return coords;
    }

    protected List<Coordinate> getCoordsFromFile (CoordTransFile sourceOptions, FileItem file, int dimension, boolean addZeroes, boolean storeLineEnds) throws ActionException{
        List<Coordinate> coordinates = new ArrayList<>();
        String line;
        String[] coords;
        int xIndex = 0;
        int yIndex = 1;
        int zIndex = 2;
        int coordDimension = dimension;
        int headerLineCount = sourceOptions.getHeaderLineCount();
        String coordSeparator = coordinateSeparators.get(sourceOptions.getCoordinateSeparator());
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
        if (unit != null && !unit.equals(DEGREE)){
            transformUnit = true;
        }
        double x,y,z;
        try{
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(file.getInputStream()));
            //skip row and store row as header row
            for (int i = 0 ; i < headerLineCount && (line=br.readLine())!=null ;i++) {
                sourceOptions.addHeaderRow(line);
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
                if (coordinates.size() == maxCoordsToArray){ //TODO index
                    this.hasMoreCoordinates = true;
                    break;
                }
            }
            br.close();
        } catch (UnsupportedEncodingException e){
            throw new ActionParamsException("Encoding - Invalid file");
        } catch (IOException e){
            throw new ActionParamsException("IO - Invalid file");
        } catch (NumberFormatException e){
            throw new ActionParamsException("Expected a number");
        }
        return coordinates;
    }
    private CoordTransFile getFileSettings (Map<String, String> formParams, String key) throws ActionParamsException{
        try {
            return mapper.readValue(formParams.get(key), CoordTransFile.class);
        } catch (Exception e) {
            throw new ActionParamsException("Invalid file settings: " + key, "invalid_file_settings");
        }
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
            .orElseThrow(() -> new ActionParamsException("No file entry", "no_file"));
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


    protected List<Coordinate> parseInputCoordinates(final InputStream in, final int dimension, final boolean addZeroes)
            throws IOException, ActionParamsException {
        try (JsonParser parser = jf.createParser(in)) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new ActionParamsException("Expected input starting with an array", "invalid_coord");
            }

            List<Coordinate> coordinates = new ArrayList<>(8);
            JsonToken token;

            while ((token = parser.nextToken()) != JsonToken.END_ARRAY) {
                if (token != JsonToken.START_ARRAY) {
                    throw new ActionParamsException("Expected array opening", "invalid_coord");
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
                    throw new ActionParamsException("Expected array closing", "invalid_coord");
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

    protected void writeJsonResponse(OutputStream out, List<Coordinate> coords, List<Coordinate> inputCoords, final int dimension)
            throws ActionException {
        try (JsonGenerator json = jf.createGenerator(out)) {
            json.writeStartObject();
            json.writeNumberField(RESPONSE_DIMENSION, dimension);
            json.writeBooleanField("hasMoreCoordinates", this.hasMoreCoordinates);
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
            if (inputCoords != null){
                json.writeFieldName(RESPONSE_INPUT_COORDINATES);
                json.writeStartArray();
                for (Coordinate coord : inputCoords) {
                    json.writeStartArray();
                    json.writeNumber(coord.x);
                    json.writeNumber(coord.y);
                    if (dimension == 3) {
                        json.writeNumber(coord.z);
                    }
                    json.writeEndArray();
                }
                json.writeEndArray();
            }
            json.writeEndObject();
        } catch (IOException e) {
            throw new ActionException("Failed to write JSON");
        }
    }

    protected void writeFile(List<Coordinate> coords, final int dimension, CoordTransFile opts, String crs)
        throws ActionException {
        try {
            String fileName = addFileExt(opts.getFileName());
            BufferedWriter bw = new BufferedWriter(new FileWriter (fileName));
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
                //TODO: should we use also W, S for negative coordinates
                if (writeCardinals){
                    xCoord += "E";
                    yCoord += "N";
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
            bw.close();
        } catch (IOException e) {
            throw new ActionException("Failed to write file", e);
        }
    }
    //handle id, axisflip, dimension
    protected String getOutputLine (Coordinate coord, CoordTransFile opt){
        String line = "";
        if (",".equals(opt.getDecimalSeparator())){
            line = line.replace('.',',');
        }
        return line;
    }
}
