package fi.nls.oskari.spring;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.spring.extension.OskariParam;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.paikkatietoikkuna.coordtransform.*;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Handles CoordinateTransformation endpoint for watching transform jobs
 */
@Controller
public class CoordinateTransformationJobHandler {
    protected static final Logger log = LogFactory.getLogger(CoordinateTransformationJobHandler.class);

    protected static final String RESPONSE_COORDINATES = "resultCoordinates";
    protected static final String RESPONSE_DIMENSION = "dimension";
    protected static final String RESPONSE_JOB_ID = "jobId";

    protected static final String DEGREE = "degree";
    protected static final String METRIC = "metric";
    protected static final String FILE_EXT = "txt";
    protected static final String FILE_TYPE = "text/plain";

    protected final Map<String, String> lineSeparators = new HashMap<>();
    protected final Map<String, String> coordinateSeparators = new HashMap<>();

    private static long POLLING_TIMEOUT = 10000l; // 45000l;
    private static final String ROUTE = "/coordinatetransform/watch/{jobId}";
    private JsonFactory jf;
    private CoordTransWorker worker;

    public CoordinateTransformationJobHandler() {
        this.jf = new JsonFactory();
        this.worker = OskariComponentManager.getComponentOfType(CoordTransWorker.class);
    }

    @RequestMapping(ROUTE)
    public @ResponseBody DeferredResult<ResponseEntity> watchJob(@PathVariable("jobId") String jobId, @OskariParam ActionParameters params) {
        if (jobId == null) {
            handleActionParamsException(new ActionParamsException(
                    "Missing job id", TransformParams.createErrorResponse("no_job_key")),
                    params);
            return null;
        }
        // Get the job from worker
        DeferredResult transformJob = worker.getTransformJob(jobId);
        TransformParams transformParams = worker.getTransformParams(jobId);

        if (transformJob == null) {
            handleActionParamsException(new ActionParamsException(
                    "No active job", TransformParams.createErrorResponse("no_job")),
                    params);
            return null;
        }
        // If job has already finished
        if (transformJob.hasResult()) {
            handleTransformResult(jobId, transformJob.getResult(), params, transformParams);
            return null;
        }
        DeferredResult result = new DeferredResult(POLLING_TIMEOUT, null);
        result.onTimeout(() -> handleTimeout(jobId, params));
        worker.watchJob(jobId, result, obj -> handleTransformResult(jobId, obj, params, transformParams));
        return result;
    }

    private void handleTimeout(String jobId, ActionParameters params) {
        try {
            // Send job id to client to keep polling
            writeJobIdResponse(params.getResponse(), jobId);
            worker.watchJob(jobId, new DeferredResult(), null);
        } catch (Exception ex) {
            handleActionException(ex, params);
        }
    }

    private void handleTransformResult(String jobId, Object result, ActionParameters params,
                                       TransformParams transformParams) {
        worker.clearJob(jobId);
        try {
            if (result instanceof Exception) {
                handleException((Exception)result, params);
            }
            writeResponse(params, transformParams, (CoordinatesPayload)result);
        } catch (Exception e) {
            handleActionException(e, params);
        }
    }

    private void writeResponse(ActionParameters params, TransformParams transformParams,
                               CoordinatesPayload coords) throws ActionException {
        String targetCrs = transformParams.targetCRS;
        int targetDimension = transformParams.outputDimensions;

        HttpServletResponse response = params.getResponse();
        if (transformParams.type.isFileOutput()) {
            String fileName = addFileExt(transformParams.exportSettings.getFileName());
            response.setContentType(FILE_TYPE);
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        } else {
            response.setContentType(IOHelper.CONTENT_TYPE_JSON);
        }
        try (OutputStream out = response.getOutputStream()) {
            if (transformParams.type.isFileOutput()) {
                writeFileResponse(out, coords.getCoords(), targetDimension, coords.getExportSettings(), targetCrs);
            } else {
                writeJsonResponse(out, coords.getCoords(), targetDimension, coords.hasMore());
            }
        } catch (IOException e) {
            throw new ActionException("Failed to write JSON to client", e);
        }
    }

    private void handleException(Exception e, ActionParameters params) {
        if (e instanceof ActionParamsException) {
            handleActionParamsException((ActionParamsException)e, params);
        } else {
            handleActionException(e, params);
        }
    }
    private void handleActionException(Exception e, ActionParameters params) {
        Throwable error = e;
        if(e.getCause() != null) {
            error = e.getCause();
        }
        log.error(error,
                "Couldn't handle action:", ROUTE,
                "Message: ", e.getMessage(),
                ". Parameters: ", params.getRequest().getParameterMap());
        writeErrorResponse(params.getResponse(), e.getMessage(),  HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
    }
    private void handleActionParamsException(ActionParamsException e, ActionParameters params) {
        log.error("Couldn't handle action: " + ROUTE,
                "Message: ", e.getMessage(),
                ". Parameters: ", params.getRequest().getParameterMap());
        writeErrorResponse(params.getResponse(), e.getMessage(),  HttpServletResponse.SC_NOT_IMPLEMENTED, e.getOptions());
    }

    //add .txt if missing
    private String addFileExt(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0 || i + 1 == name.length()) {
            return name + "." + FILE_EXT;
        }
        if (FILE_EXT.equals(name.substring(i + 1))) {
            return name;
        } else {
            return name + "." + FILE_EXT;
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

    protected void writeJobIdResponse(HttpServletResponse response, final String id)
            throws ActionException {
        try (JsonGenerator json = jf.createGenerator(response.getOutputStream())) {
            json.writeStartObject();
            json.writeStringField(RESPONSE_JOB_ID, id);
            json.writeEndObject();
        } catch (IOException e) {
            throw new ActionException("Failed to write JSON");
        }
    }

    protected void writeErrorResponse(HttpServletResponse response, String message, int errCode, Object info) {
        response.setStatus(errCode);
        response.setContentType("application/json;charset=utf-8");
        try (JsonGenerator json = jf.createGenerator(response.getOutputStream())) {
            json.writeStartObject();
            json.writeStringField("error", message);
            if (info != null) {
                json.writeObjectField("info", info);
            }
            json.writeEndObject();
        } catch (IOException e) {
            log.error("Failed to write error JSON response");
        }
    }

    protected void writeFileResponse(OutputStream out, List<Coordinate> coords, final int dimension, CoordTransFileSettings opts, String crs)
            throws ActionException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out))) {
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
            List<String> ids = opts.getIds();
            List<String> lineEndings = opts.getLineEnds();
            boolean writeEndings = opts.isWriteLineEndings() && !lineEndings.isEmpty();
            String unit = opts.getUnit();
            boolean transformUnit = false;
            if (unit != null && !unit.equals(DEGREE) && !unit.equals(METRIC)) {
                transformUnit = true;
            }
            if (opts.isPrefixId()) {
                if (ids.isEmpty()) {
                    prefixWithIndex = true;
                }
            }
            // TODO: should we add only: Coordinate Reference System: KKJ
            // if we want localized header then frontend should send header String instead of boolean
            if (opts.isWriteHeader()) {
                bw.write("Coordinate Reference System:" + crs);
                bw.write(lineSeparator);
                for (String headerRow : opts.getHeaderRows()) {
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
                if (prefixId && prefixWithIndex) {
                    bw.write((i + 1) + coordSeparator);
                } else if (prefixId) {
                    bw.write(ids.get(i) + coordSeparator);
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
}
