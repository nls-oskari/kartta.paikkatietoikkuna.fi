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
import fi.nls.paikkatietoikkuna.coordtransform.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;


/**
 * Handles long polling requests for coordinate transformation.
 * Timeouts with the job id, or returns transformation result.
 */
@Controller
public class CoordinateTransformationAsyncController {
    protected static final Logger log = LogFactory.getLogger(CoordinateTransformationAsyncController.class);

    protected static final String RESPONSE_COORDINATES = "resultCoordinates";
    protected static final String RESPONSE_DIMENSION = "dimension";
    protected static final String RESPONSE_JOB_ID = "jobId";

    protected static final String FILE_EXT = "txt";
    protected static final String FILE_TYPE = "text/plain";

    private static long POLLING_TIMEOUT_MS = 45000;
    private static final String RESULT_TIMEOUT = "timeout";
    private static final String ROUTE = "/coordinatetransform/watch/";
    private JsonFactory jf;
    private CoordFileHelper fileHelper;
    private CoordTransWorker worker;

    public CoordinateTransformationAsyncController() {
        this.jf = new JsonFactory();
        this.fileHelper = new CoordFileHelper();
        this.worker = OskariComponentManager.getComponentOfType(CoordTransWorker.class);
    }

    @RequestMapping(ROUTE + "/{jobId}")
    public @ResponseBody DeferredResult<CoordinatesPayload> watchJob(@PathVariable("jobId") String jobId, @OskariParam ActionParameters params) {
        if (jobId == null) {
            handleActionParamsException(new ActionParamsException(
                    "Missing job id", TransformParams.createErrorResponse("no_job_key")),
                    params);
            return null;
        }
        // Get the result from worker
        CoordinateTransformationJob job = worker.getTransformJob(jobId);
        if (job == null) {
            handleActionParamsException(new ActionParamsException(
                    "No active job", TransformParams.createErrorResponse("no_job")),
                    params);
            return null;
        }
        // Handle finished transform job
        if (job.isCompleted()) {
            handleTransformResult(job, params);
            return null;
        }
        // Keep watching for the result
        DeferredResult<CoordinatesPayload> async =
                new DeferredResult<>(POLLING_TIMEOUT_MS, RESULT_TIMEOUT);
        async.onTimeout(() -> handleTimeout(jobId, params));
        worker.watchJob(jobId, async, __ -> handleTransformResult(job, params));
        return async;
    }

    private void handleTimeout(String jobId, ActionParameters params) {
        try {
            // Send job id to client to keep polling
            writeJobIdResponse(params.getResponse(), jobId);
        } catch (Exception ex) {
            handleActionException(ex, params);
        }
    }

    private void handleTransformResult(CoordinateTransformationJob job, ActionParameters params) {
        try {
            final Object result = job.getResult();
            if (result instanceof Exception) {
                handleException((Exception)result, params);
                worker.clearJob(job.getId());
                return;
            }
            writeResponse(params, job.getParams(), (CoordinatesPayload)result);
            worker.clearJob(job.getId());
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
                fileHelper.writeFileResponse(out, coords, targetDimension, targetCrs);
            } else {
                response.setContentType("application/json;charset=utf-8");
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
        if (name.endsWith("." + FILE_EXT)) {
            return name;
        }
        if (name.endsWith(".")) {
            return name + FILE_EXT;
        }
        return name + "." + FILE_EXT;
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
        response.setContentType("application/json;charset=utf-8");
        try (JsonGenerator json = jf.createGenerator(response.getOutputStream())) {
            json.writeStartObject();
            json.writeStringField(RESPONSE_JOB_ID, id);
            json.writeEndObject();
        } catch (IOException e) {
            throw new ActionException("Failed to write JSON");
        }
    }

    protected void writeErrorResponse(HttpServletResponse response, String message, int errCode, JSONObject info) {
        response.setStatus(errCode);
        response.setContentType("application/json;charset=utf-8");
        try (Writer writer = new OutputStreamWriter(response.getOutputStream())) {
            JSONObject jsonResponse = JSONHelper.createJSONObject("error", message);
            if (info != null) {
                jsonResponse.put("info", info);
            }
            writer.write(jsonResponse.toString());
        } catch (Exception e) {
            log.error("Failed to write error JSON response");
        }
    }

}
