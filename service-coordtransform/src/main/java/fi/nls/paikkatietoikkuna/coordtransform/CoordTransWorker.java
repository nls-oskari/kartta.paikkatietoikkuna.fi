package fi.nls.paikkatietoikkuna.coordtransform;

import org.locationtech.jts.geom.Coordinate;
import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.cache.Cache;
import fi.nls.oskari.cache.CacheManager;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.service.OskariComponent;
import fi.nls.oskari.util.IOHelper;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;

@Oskari
public class CoordTransWorker extends OskariComponent {

    private Cache<CoordinateTransformationJob> jobCache;
    private static final int CONCURRENT_USERS_LIMIT = 100;

    public CoordTransWorker() {
        jobCache = CacheManager.getCache(CoordTransWorker.class.getCanonicalName());
        jobCache.setLimit(CONCURRENT_USERS_LIMIT);
    }

    public String transformAsync(CoordTransQueryBuilder queryBuilder, TransformParams params, CoordinatesPayload coords)
        throws RejectedExecutionException {
        CoordinateTransformationJob job = new CoordinateTransformationJob(params);
        jobCache.put(job.getId(), job);
        startTransformJob(job, queryBuilder, coords);
        return job.getId();
    };

    public CoordinateTransformationJob getTransformJob(String jobId) {
        return jobCache.get(jobId);
    }
    public void clearJob(String jobId) {
        jobCache.remove(jobId);
    }

    public void watchJob(String jobId, DeferredResult<CoordinatesPayload> deferred,
                         DeferredResult.DeferredResultHandler handler) {
        CoordinateTransformationJob job = getTransformJob(jobId);
        if (job == null) {
            return;
        }
        job.subscibe(deferred, handler);
    }

    private void startTransformJob(CoordinateTransformationJob job, CoordTransQueryBuilder queryBuilder, CoordinatesPayload coords) {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                transform(queryBuilder, coords.getCoords());
                job.complete(coords);
            }
            catch (IOException e) {
                job.complete(new ActionException("Failed to connect to CoordTrans service", e));
            }
            catch (IllegalArgumentException e) {
                job.complete(new ActionParamsException(
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
        byte[] serviceResponseBytes = IOHelper.readBytes(conn);
        CoordTransService.parseResponse(serviceResponseBytes, batch);
    }

}