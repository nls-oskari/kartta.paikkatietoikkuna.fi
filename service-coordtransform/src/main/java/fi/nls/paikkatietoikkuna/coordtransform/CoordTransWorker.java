package fi.nls.paikkatietoikkuna.coordtransform;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.IOHelper;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RejectedExecutionException;

@Oskari
public class CoordTransWorker extends OskariComponent {

    private ConcurrentHashMap<String, Object> resultMap;
    private ConcurrentHashMap<String, DeferredResult<CoordinatesPayload>> clientResponseMap;
    private ConcurrentHashMap<String, TransformParams> paramsMap;
    private ConcurrentHashMap<String, DeferredResult.DeferredResultHandler> handlerMap;

    public static final String RESULT_PENDING = "pending";

    public CoordTransWorker() {
        resultMap = new ConcurrentHashMap<>();
        clientResponseMap = new ConcurrentHashMap<>();
        paramsMap = new ConcurrentHashMap<>();
        handlerMap = new ConcurrentHashMap<>();
    }

    public String transformAsync(CoordTransQueryBuilder queryBuilder, TransformParams params, CoordinatesPayload coords)
        throws RejectedExecutionException {
        String jobId = UUID.randomUUID().toString();
        resultMap.put(jobId, RESULT_PENDING);
        paramsMap.put(jobId, params);
        // startTransformJob(jobId, queryBuilder, coords);
        startMockJob(jobId, coords);
        return jobId;
    };

    public Object getTransformResult(String jobId) {
        return resultMap.get(jobId);
    }
    public TransformParams getTransformParams(String jobId) {
        return paramsMap.get(jobId);
    }
    public void clearJob(String jobId) {
        resultMap.remove(jobId);
        paramsMap.remove(jobId);
        clientResponseMap.remove(jobId);
    }

    public void watchJob(String jobId, DeferredResult<CoordinatesPayload> deferred,
                         DeferredResult.DeferredResultHandler handler) {
        clientResponseMap.put(jobId, deferred);
        handlerMap.put(jobId, handler);
    }

    private void startMockJob(String jobId, CoordinatesPayload coords) throws RejectedExecutionException {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                double random = Math.random();
                double sleepTime = 1000.0 * 5.0 * random;
                Thread.sleep((long)sleepTime);
                if (Math.random() > 0.9) {
                    setResult(jobId, new ActionException("Just testing"));
                    return;
                }
            }
            catch (Exception ex) { }
            setResult(jobId, coords);
        });
        throw new RejectedExecutionException();
    }

    private void startMockJobFuture(String jobId, CoordinatesPayload coords) {
        ForkJoinTask future = ForkJoinPool.commonPool().submit(() -> {
            try {
                double random = Math.random();
                double sleepTime = 1000.0 * 5.0 * random;
                Thread.sleep((long)sleepTime);
                if (Math.random() > 0.9) {
                    return new ActionException("Just testing");
                }
            }
            catch (InterruptedException ex) {
                return new ActionException("Interrupted");
            }
            return coords;
        });
        resultMap.put(jobId, future);
    }


    private void startTransformJob(String jobId, CoordTransQueryBuilder queryBuilder, CoordinatesPayload coords) {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                transform(queryBuilder, coords);
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

    protected void transform (CoordTransQueryBuilder queryBuilder, CoordinatesPayload coords) throws IOException {
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
    }

    private void transform (String query, List<Coordinate> batch) throws IOException, IllegalArgumentException {
        HttpURLConnection conn = IOHelper.getConnection(query);
        byte[] serviceResponseBytes = IOHelper.readBytes(conn);
        CoordTransService.parseResponse(serviceResponseBytes, batch);
    }

    private void setResult(String jobId, Object result) {
        resultMap.put(jobId, result);
        DeferredResult<CoordinatesPayload> deferred = clientResponseMap.get(jobId);
        DeferredResult.DeferredResultHandler handler = handlerMap.get(jobId);
        if (deferred == null) {
            return;
        }
        deferred.setResultHandler(handler);
        if (result instanceof Exception) {
            deferred.setErrorResult(result);
        } else {
            deferred.setResult((CoordinatesPayload) result);
        }
    }

}