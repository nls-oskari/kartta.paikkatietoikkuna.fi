package fi.nls.paikkatietoikkuna.coordtransform;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.IOHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

@Oskari
public class CoordTransWorker extends OskariComponent {

    private ConcurrentHashMap<String, DeferredResult<List<Coordinate>>> resultMap;
    private ConcurrentHashMap<String, TransformParams> paramsMap;
    private ConcurrentHashMap<String, DeferredResult.DeferredResultHandler> handlerMap;

    public CoordTransWorker() {
        resultMap = new ConcurrentHashMap<>();
        paramsMap = new ConcurrentHashMap<>();
        handlerMap = new ConcurrentHashMap<>();
    }

    public String transformAsync(CoordTransQueryBuilder queryBuilder, TransformParams params, CoordinatesPayload coords) {
        String jobId = UUID.randomUUID().toString();
        resultMap.put(jobId, new DeferredResult<>());
        paramsMap.put(jobId, params);
        // startTransformJob(jobId, queryBuilder, coords);
        startMockJob(jobId, coords);
        return jobId;
    };

    public DeferredResult<List<Coordinate>> getTransformJob(String jobId) {
        return resultMap.get(jobId);
    }
    public TransformParams getTransformParams(String jobId) {
        return paramsMap.get(jobId);
    }
    public void clearJob(String jobId) {
        resultMap.remove(jobId);
        paramsMap.remove(jobId);
        handlerMap.remove(jobId);
    }

    public void watchJob(String jobId, DeferredResult<List<Coordinate>> deferred, DeferredResult.DeferredResultHandler handler) {
        resultMap.put(jobId, deferred);
        if (handler != null) {
            handlerMap.put(jobId, handler);
        }
    }

    private void startMockJob(String jobId, CoordinatesPayload coords) {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                Thread.sleep((long)(1000 * 10 * Math.random()));
                if (Math.random() > 0.3) {
                    setResult(jobId, new ActionException("Just testing"));
                    return;
                }
            }
            catch (Exception ex) { }
            setResult(jobId, coords);
        });
    }

    private void startTransformJob(String jobId, CoordTransQueryBuilder queryBuilder, CoordinatesPayload coords) {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                List<Coordinate> batch = new ArrayList<>();
                for (Coordinate c : coords.getCoords()) {
                    boolean fit = queryBuilder.add(c);
                    if (!fit) {
                        transform(queryBuilder.build(), batch);
                        queryBuilder.reset();
                        batch.clear();
                        queryBuilder.add(c);
                    }
                    batch.add(c);
                }
                if (batch.size() != 0) {
                    transform(queryBuilder.build(), batch);
                }
                setResult(jobId, coords);
            }
            catch (IOException e) {
                setResult(jobId, new ActionException("Failed to connect to CoordTrans service", e));
            }
            catch (IllegalArgumentException e) {
                setResult(jobId, new ActionParamsException(
                        "Failed to make transformation",
                        TransformParams.createErrorResponse("transformation_error", e)));
            }
        });
    }

    private void transform (String query, List<Coordinate> batch) throws IOException, IllegalArgumentException {
        HttpURLConnection conn = IOHelper.getConnection(query);
        byte[] serviceResponseBytes = IOHelper.readBytes(conn);
        CoordTransService.parseResponse(serviceResponseBytes, batch);
    }

    private void setResult(String jobId, Object result) {
        DeferredResult<List<Coordinate>> deferred = resultMap.get(jobId);
        DeferredResult.DeferredResultHandler handler = handlerMap.get(jobId);
        if (deferred == null) {
            return;
        }
        deferred.setResultHandler(handler);
        if (result instanceof Exception) {
            deferred.setErrorResult(
                    ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(TransformParams.createErrorResponse("transformation_error", (Exception)result)));
        } else {
            deferred.setResult((List<Coordinate>) result);
        }
    }

}