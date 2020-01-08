package fi.nls.paikkatietoikkuna.coordtransform;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.locationtech.jts.geom.Coordinate;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import org.apache.commons.fileupload.FileItem;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoordFileHelper {

    protected static final String DEGREE = "degree";
    protected static final String METRIC = "metric";
    protected static final String KEY_FOR_ERRORS = "errorKey";

    protected static final String RESPONSE_INPUT_COORDINATES = "inputCoordinates";
    protected static final String RESPONSE_DIMENSION = "dimension";

    private JsonFactory jf;
    private int maxCoordsF2A;

    private final Map<String, String> lineSeparators = new HashMap<>();
    private final Map<String, String> coordinateSeparators = new HashMap<>();

    public CoordFileHelper() {
        jf = new JsonFactory();
        lineSeparators.put("win", "\r\n");
        lineSeparators.put("mac", "\n");
        lineSeparators.put("unix", "\r");
        coordinateSeparators.put("space", " ");
        coordinateSeparators.put("tab", "\t");
        coordinateSeparators.put("comma", ",");
        coordinateSeparators.put("semicolon", ";");
    }

    public String getCoordSeparator(String key) {
        return coordinateSeparators.get(key);
    }
    public String getLineSeparator(String key) {
        return lineSeparators.get(key);
    }

    public CoordinatesPayload getCoordsFromFile(TransformParams params, int limit)
            throws ActionException {
        CoordinatesPayload cp = new CoordinatesPayload();
        CoordTransFileSettings sourceOptions = params.importSettings;
        boolean storeHeaders = false;
        boolean storeLineEnds = false;
        boolean storeIds = false;
        if(params.type.isFileOutput()) {
            cp.setExportSettings(params.exportSettings);
            // Store only if file output and included/requested
            storeLineEnds = params.exportSettings.isWriteLineEndings();
            storeHeaders = params.exportSettings.isWriteHeader();
            storeIds = sourceOptions.isPrefixId();
        }

        int dimension = params.inputDimensions;
        String line = "";
        String[] coords;
        int xIndex = 0;
        int yIndex = 1;
        int zIndex = 2;
        int coordDimension = dimension;
        int headerLineCount = sourceOptions.getHeaderLineCount();
        String coordSeparator = getCoordSeparator(sourceOptions.getCoordinateSeparator());
        if (coordSeparator == null) {
            throw new ActionParamsException("Invalid coordinate separator: " + coordSeparator);
        }
        if (sourceOptions.isAxisFlip()) {
            xIndex = 1;
            yIndex = 0;
        }
        if (sourceOptions.isPrefixId()) {
            xIndex++;
            yIndex++;
            zIndex++;
            coordDimension++;
        }


        boolean replaceCommas = false;
        if (sourceOptions.getDecimalSeparator() == ',') {
            replaceCommas = true;
        }
        String unit = sourceOptions.getUnit();
        boolean transformUnit = false;
        if (unit != null && !unit.equals(DEGREE) && !unit.equals(METRIC)) {
            transformUnit = true;
        }
        double x, y, z;
        int lineIndex = 1;
        boolean firstCoord = true; // same as "firstLine" in readFileToJsonResonse()
        try (BufferedReader br = new BufferedReader(new InputStreamReader(params.file.getInputStream()))) {
            //skip row and store row as header row
            for (int i = 0; i < headerLineCount && (line = br.readLine()) != null; i++) {
                cp.addHeaderRow(line);
                lineIndex++;
            }
            while ((line = br.readLine()) != null) {
                /* Now coordinate separator comes from frontend
                //try to get coordinate separator from first coordinate line
                if(coordSeparator==null){
                    coordSeparator = CoordTransService.getCoordSeparator(line, dimension, sourceOptions.isPrefixId());
                    sourceOptions.setCoordinateSeparator(coordSeparator);
                }*/
                //skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                //replace commas
                if (replaceCommas) {
                    line = line.replace(',', '.');
                }
                coords = line.split(coordSeparator);
                if (coords.length < coordDimension) {
                    if (firstCoord == true) {
                        throw new ActionParamsException("Couldn't parse coordinate on the first line", TransformParams.createErrorResponse("invalid_first_coord"));
                    }
                    throw new ActionParamsException("Invalid coordinate line", createErrorInLineResponse(lineIndex, line, null));
                }

                // TODO: if there is more than one coordinate separators between coordinates, transformation should still work
                // loop coords, skip if item is empty and try to find next x,y,z value (handle prefixId, axisFlip, dimension and addZeroes)
                // "25.4545  64.3434"
                if (transformUnit) {
                    x = CoordTransService.transformUnitToDegree(coords[xIndex], unit);
                    y = CoordTransService.transformUnitToDegree(coords[yIndex], unit);
                } else {
                    x = Double.valueOf(coords[xIndex]);
                    y = Double.valueOf(coords[yIndex]);
                }
                if (dimension == 3) {
                    z = Double.valueOf(coords[zIndex]);
                    cp.addCoordinate(new Coordinate(x, y, z));
                } else {
                    cp.addCoordinate(new Coordinate(x, y));
                }
                if (storeIds) {
                    cp.addId(coords[0]);
                }
                if (storeLineEnds == true) {
                    String lineEnd = "";
                    //add coordSeparator back if lineEnding string is slitted (e.g. coordSeparator is " ")
                    for (int i = coordDimension; i < coords.length; i++) {
                        if (i == coordDimension) {
                            lineEnd += coords[i];
                        } else {
                            lineEnd += coordSeparator + coords[i];
                        }
                    }
                    cp.addLineEnd(lineEnd);
                }
                if (cp.size() == limit) {
                    cp.setPartialParse(true);
                    return cp;
                }
                firstCoord = false;
                lineIndex++;
            }
        } catch (IOException e) {
            throw new ActionParamsException("Invalid file", TransformParams.createErrorResponse("invalid_file", e));
        } catch (NumberFormatException e) {
            throw new ActionParamsException("Expected a number", createErrorInLineResponse(lineIndex, line, e));
        } catch (IndexOutOfBoundsException e) {
            throw new ActionParamsException("Index out of bounds", createErrorInLineResponse(lineIndex, line, e));
        }
        return cp;
    }

    public void writeFileResponse(OutputStream out, CoordinatesPayload cp, final int dimension, String crs)
            throws ActionException {
        CoordTransFileSettings opts = cp.getExportSettings();
        List<Coordinate> coords = cp.getCoords();
        List<String> ids = cp.getIds();
        List<String> lineEndings = cp.getLineEnds();
        boolean writeEndings = opts.isWriteLineEndings() && lineEndings.size() == coords.size();
        boolean prefixWithIndex = false;
        boolean prefixId = false;
        if (opts.isPrefixId()) {
            if (ids.size() == coords.size()){
                prefixId = true;
            } else {
                prefixWithIndex = true;
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out))) {
            String xCoord;
            String yCoord;
            String zCoord;
            String lineSeparator = lineSeparators.get(opts.getLineSeparator());
            String coordSeparator = coordinateSeparators.get(opts.getCoordinateSeparator());
            int decimals = opts.getDecimalCount();
            boolean replaceCommas = opts.getDecimalSeparator() == ',';
            boolean flipAxis = opts.isAxisFlip();
            boolean writeCardinals = opts.isWriteCardinals();
            String unit = opts.getUnit();
            boolean transformUnit = false;
            if (unit != null && !unit.equals(DEGREE) && !unit.equals(METRIC)) {
                transformUnit = true;
            }
            // TODO: should we add only: Coordinate Reference System: KKJ
            // if we want localized header then frontend should send header String instead of boolean
            if (opts.isWriteHeader()) {
                bw.write("Coordinate Reference System:" + crs);
                bw.write(lineSeparator);
                for (String headerRow : cp.getHeaderRows()) {
                    bw.write(headerRow);
                    bw.write(lineSeparator);
                }
            }
            for (int i = 0; i < coords.size(); i++) {
                Coordinate coord = coords.get(i);
                if (transformUnit) {
                    xCoord = CoordTransService.transformDegreeToUnit(coord.x, unit, decimals);
                    yCoord = CoordTransService.transformDegreeToUnit(coord.y, unit, decimals);
                } else {
                    xCoord = CoordTransService.round(coord.x, decimals);
                    yCoord = CoordTransService.round(coord.y, decimals);
                }
                if (replaceCommas) {
                    xCoord = xCoord.replace('.', ',');
                    yCoord = yCoord.replace('.', ',');
                }
                if (writeCardinals) {
                    if (xCoord.indexOf('-') == 0) {
                        xCoord = xCoord.substring(1) + "W";
                    } else {
                        xCoord += "E";
                    }
                    if (yCoord.indexOf('-') == 0) {
                        yCoord = yCoord.substring(1) + "S";
                    } else {
                        yCoord += "N";
                    }
                }
                if (prefixId) {
                    bw.write(ids.get(i) + coordSeparator);
                } else if (prefixWithIndex) {
                    bw.write((i + 1) + coordSeparator);
                }
                if (flipAxis) {
                    bw.write(yCoord);
                    bw.write(coordSeparator);
                    bw.write(xCoord);
                } else {
                    bw.write(xCoord);
                    bw.write(coordSeparator);
                    bw.write(yCoord);
                }
                if (dimension == 3) {
                    zCoord = CoordTransService.round(coord.z, decimals);
                    if (replaceCommas) {
                        zCoord = zCoord.replace('.', ',');
                    }
                    bw.write(coordSeparator);
                    bw.write(zCoord);
                }
                if (writeEndings) {
                    bw.write(coordSeparator);
                    bw.write(lineEndings.get(i));
                }
                bw.write(lineSeparator);
            }
        } catch (IOException e) {
            throw new ActionException("Failed to write file", e);
        }
    }

    public void readFileToJsonResponse(TransformParams params, int maxCoordsF2A) throws ActionException {
        FileItem file = params.file;
        CoordTransFileSettings sourceOptions = params.importSettings;
        int dimension = params.inputDimensions; //input dimension is now forced to 3 in frontend (F2R), if file is selected before crs selections
        HttpServletResponse response = params.actionParameters.getResponse();
        response.setContentType(IOHelper.CONTENT_TYPE_JSON);
        boolean hasMoreCoordinates = false;

        String line;
        int xIndex = 0;
        int yIndex = 1;
        int zIndex = 2;
        int coordDimension = 2; //force to check that lines contain at least x and y
        int headerLineCount = sourceOptions.getHeaderLineCount();
        int writtenCoords = 0;
        String coordSeparator = getCoordSeparator(sourceOptions.getCoordinateSeparator());
        boolean firstLine = true;
        if (coordSeparator == null) {
            throw new ActionParamsException("Invalid coordinate separator: " + coordSeparator);
        }
        if (sourceOptions.isAxisFlip()) {
            xIndex = 1;
            yIndex = 0;
        }
        if (sourceOptions.isPrefixId()) {
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
                try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                    for (int i = 0; (line = br.readLine()) != null; i++) {
                        // skip header and empty rows
                        if (i < headerLineCount || line.trim().isEmpty()) {
                            continue;
                        }
                        String[] coord = line.split(coordSeparator);
                        if (coord.length < coordDimension) {
                            json.writeEndArray();
                            if (firstLine == true) {
                                writeJsonError(json, "invalid_first_coord");
                                throw new ActionParamsException("Couldn't parse coordinate on the first line", TransformParams.createErrorResponse("invalid_first_coord"));
                            }
                            int lineNumber = i + 1;
                            writeInLineJsonError(json, lineNumber, line);
                            throw new ActionParamsException(
                                    "Invalid coordinate line",
                                    createErrorInLineResponse(lineNumber, line, null));
                        }
                        json.writeStartArray();
                        json.writeString(coord[xIndex]);
                        json.writeString(coord[yIndex]);
                        if (coord.length > zIndex ) {
                            json.writeString(coord[zIndex]);
                        } else {
                            json.writeString("");
                        }
                        json.writeEndArray();
                        writtenCoords++;
                        firstLine = false;
                        if (writtenCoords == maxCoordsF2A) {
                            hasMoreCoordinates = true;
                            break;
                        }
                    }
                } catch (IOException e) {
                    json.writeEndArray();
                    writeJsonError(json, "invalid_file");
                    throw new ActionParamsException("Invalid file", TransformParams.createErrorResponse("invalid_file", e));
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

    private void writeInLineJsonError(JsonGenerator json, int lineNumber, String line) throws IOException {
        json.writeFieldName("error");
        json.writeStartObject();
        json.writeNumberField("lineIndex", lineNumber);
        json.writeStringField(KEY_FOR_ERRORS, "invalid_read_line");
        json.writeStringField("line", line);
        json.writeEndObject();
    }

    private void writeJsonError(JsonGenerator json, String errorKey) throws IOException {
        json.writeFieldName("error");
        json.writeStartObject();
        json.writeStringField(KEY_FOR_ERRORS, errorKey);
        json.writeEndObject();
    }

    private JSONObject createErrorInLineResponse(int lineIndex, String line, Exception e) {
        JSONObject jsonError = JSONHelper.createJSONObject(KEY_FOR_ERRORS,"invalid_coord_in_line");
        JSONHelper.putValue(jsonError, "line", line);
        JSONHelper.putValue(jsonError, "lineIndex", lineIndex);
        if (e != null) {
            JSONHelper.putValue(jsonError, "exception", e.getMessage());
        }
        return jsonError;
    }

}
