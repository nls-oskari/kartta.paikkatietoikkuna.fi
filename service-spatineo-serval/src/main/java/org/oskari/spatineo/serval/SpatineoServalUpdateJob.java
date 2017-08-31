package org.oskari.spatineo.serval;

import java.util.ArrayList;
import java.util.List;

import org.oskari.service.backendstatus.BackendStatusService;
import org.oskari.service.backendstatus.BackendStatusServiceMyBatisImpl;
import org.oskari.service.backendstatus.maplayer.MapLayer;
import org.oskari.service.backendstatus.maplayer.MapLayerDao;
import org.oskari.spatineo.serval.api.ServalResponse;
import org.oskari.spatineo.serval.api.ServalResult;
import org.oskari.spatineo.serval.api.ServalService;
import org.oskari.spatineo.serval.api.ServalService.ServalServiceType;
import org.oskari.spatineo.serval.api.SpatineoServalDao;

import fi.nls.oskari.domain.map.BackendStatus;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

public class SpatineoServalUpdateJob {

    private static final Logger LOG = LogFactory.getLogger(SpatineoServalUpdateJob.class);

    private static final String PROP_SERVAL_URL = "spatineo.serval.url";
    private static final String PROP_SERVAL_CHUNK_SIZE = "spatineo.serval.chunk.size";
    private static final String PROP_SERVAL_TRY_COUNT = "spatineo.serval.try.count";

    private static final int DEFAULT_CHUNK_SIZE = 10;
    private static final int DEFAULT_NUM_TRIES = 3;

    public static void scheduledServiceCall() {
        LOG.info("Starting the Spatineo Serval update service call...");

        final String endPoint = PropertyUtil.getNecessary(PROP_SERVAL_URL, 
                "Spatineo Serval API requires an end point address. Calls to API disabled.");
        final SpatineoServalDao spatineoServalDao = new SpatineoServalDao(endPoint);

        final int chunkSize = PropertyUtil.getOptional(PROP_SERVAL_CHUNK_SIZE, DEFAULT_CHUNK_SIZE);
        final int numTries = PropertyUtil.getOptional(PROP_SERVAL_TRY_COUNT, DEFAULT_NUM_TRIES);

        final MapLayerDao mapLayerDao = new MapLayerDao();
        final BackendStatusService statusService = new BackendStatusServiceMyBatisImpl();

        final List<BackendStatus> statuses = new ArrayList<>();

        for (List<MapLayer> chunk : ListPartition.partition(mapLayerDao.findWMSMapLayers(), chunkSize)) {
            handle(spatineoServalDao, chunk, statuses, numTries, ServalServiceType.WMS);
        }
        for (List<MapLayer> chunk : ListPartition.partition(mapLayerDao.findWFSMapLayers(), chunkSize)) {
            handle(spatineoServalDao, chunk, statuses, numTries, ServalServiceType.WFS);
        }

        statusService.insertAll(statuses);

        LOG.info("Done with the Spatineo Serval update service call");
    }

    private static boolean handle(SpatineoServalDao spatineoServalDao,
            List<MapLayer> layers, List<BackendStatus> statuses, int numTries, ServalServiceType type) {
        for (int i = 0; i < numTries; i++) {
            if (i > 0) {
                LOG.info("Re-trying to handle the same chunk", (i + 1), "/", numTries);
            }
            if (handle(spatineoServalDao, layers, statuses, type)) {
                return true;
            }
        }
        return false;
    }

    private static boolean handle(SpatineoServalDao spatineoServalDao,
            List<MapLayer> layers, List<BackendStatus> statuses, ServalServiceType type) {
        final List<ServalService> services = toServices(layers, type);
        final ServalResponse response = spatineoServalDao.query(services);
        if (response == null) {
            LOG.info("Failed to get response from Spatineo Serval");
            return false;
        }

        LOG.debug("Received Response with status:", response.getStatus(),
                " statusMessage:", response.getStatusMessage());
        if (!"OK".equals(response.getStatus())) {
            return false;
        }

        final List<ServalResult> results = response.getResult();
        if (results.size() != layers.size()) {
            LOG.warn("Received different number of statuses than queried for!",
                    "Number of layers:", layers.size(),
                    "Number of statuses:", results.size());
            return false;
        }

        for (int i = 0; i < layers.size(); i++) {
            MapLayer layer = layers.get(i);
            ServalResult result = results.get(i);
            statuses.add(new BackendStatus(layer.getId(), 
                    result.getStatus(), 
                    result.getStatusMessage(), 
                    result.getInfoUrl()));
        }
        return true;
    }

    private static List<ServalService> toServices(List<MapLayer> layers, 
            ServalServiceType type) {
        final List<ServalService> services = new ArrayList<>();
        for (MapLayer layer : layers) {
            services.add(new ServalService(type, layer.getUrl(), layer.getName()));
        }
        return services;
    }

}
