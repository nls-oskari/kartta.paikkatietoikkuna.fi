package fi.nls.paikkatietoikkuna.coordtransform;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.metadata.MetadataFieldHandler;
import fi.nls.oskari.domain.SelectItem;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.IOHelper;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RejectedExecutionException;

@Oskari
public class CoordTransWorker extends OskariComponent {

    private ConcurrentHashMap<String, DeferredResult<CoordinatesPayload>> clientResponseMap;
    private ConcurrentHashMap<String, TransformParams> paramsMap;
    private ConcurrentHashMap<String, DeferredResult.DeferredResultHandler> handlerMap;
    private Cache<Object> resultCache;
    private static final int READ_TIMEOUT_MS = 1000 * 60 * 15;

    public static final String RESULT_PENDING = "pending";

    public CoordTransWorker() {
        clientResponseMap = new ConcurrentHashMap<>();
        paramsMap = new ConcurrentHashMap<>();
        handlerMap = new ConcurrentHashMap<>();
        resultCache = CacheManager.getCache(CoordTransWorker.class.getCanonicalName());
        resultCache.setLimit(100);
    }

    public String transformAsync(CoordTransQueryBuilder queryBuilder, TransformParams params, CoordinatesPayload coords)
        throws RejectedExecutionException {
        String jobId = UUID.randomUUID().toString();
        resultCache.put(jobId, RESULT_PENDING);
        paramsMap.put(jobId, params);
        startTransformJob(jobId, queryBuilder, coords);
        return jobId;
    };

    public Object getTransformResult(String jobId) {
        return resultCache.get(jobId);
    }
    public TransformParams getTransformParams(String jobId) {
        return paramsMap.get(jobId);
    }
    public void clearJob(String jobId) {
        resultCache.remove(jobId);
        paramsMap.remove(jobId);
        clientResponseMap.remove(jobId);
    }

    public void watchJob(String jobId, DeferredResult<CoordinatesPayload> deferred,
                         DeferredResult.DeferredResultHandler handler) {
        clientResponseMap.put(jobId, deferred);
        handlerMap.put(jobId, handler);
    }

    private void startTransformJob(String jobId, CoordTransQueryBuilder queryBuilder, CoordinatesPayload coords) {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                transform(queryBuilder, coords.getCoords());
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

    public void transform (CoordTransQueryBuilder queryBuilder, List<Coordinate> coordinates) throws IOException {
        List<Coordinate> batch = new ArrayList<>();
        for (Coordinate c : coordinates) {
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

    public void transform (String query, List<Coordinate> batch) throws IOException, IllegalArgumentException {
        HttpURLConnection conn = IOHelper.getConnection(query);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        byte[] serviceResponseBytes = IOHelper.readBytes(conn);
        CoordTransService.parseResponse(serviceResponseBytes, batch);
    }

    private void setResult(String jobId, Object result) {
        resultCache.put(jobId, result);
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