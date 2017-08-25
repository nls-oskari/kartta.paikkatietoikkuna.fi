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
    private static final int CHUNK_SIZE = 10;

    public static void scheduledServiceCall() throws Exception {
        LOG.info("Starting the Spatineo Serval update service call...");

        final String endPoint = PropertyUtil.getNecessary(PROP_SERVAL_URL, 
                "Spatineo Serval API requires an end point address. Calls to API disabled.");
        final SpatineoServalDao spatineoServalDao = new SpatineoServalDao(endPoint);
        
        final MapLayerDao mapLayerDao = new MapLayerDao();
        final BackendStatusService statusService = new BackendStatusServiceMyBatisImpl();

        final List<BackendStatus> statuses = new ArrayList<>();

        for (List<MapLayer> chunk : partition(mapLayerDao.findWMSMapLayers(), CHUNK_SIZE)) {
            handlePartition(spatineoServalDao, chunk, statuses, ServalServiceType.WMS);
        }
        for (List<MapLayer> chunk : partition(mapLayerDao.findWFSMapLayers(), CHUNK_SIZE)) {
            handlePartition(spatineoServalDao, chunk, statuses, ServalServiceType.WFS);
        }
        
        statusService.insertAll(statuses);

        LOG.info("Done with the Spatineo Serval update service call");
    }

    public static <T> List<List<T>> partition(final List<T> list, final int partitionSize) {
        final List<List<T>> parent = new ArrayList<>();
        if (list == null || partitionSize <= 0) {
            return parent;
        }
        
        List<T> tmp = null;
        int size = partitionSize;
    
        for (T t : list) {
            if (size == partitionSize) {
                tmp = new ArrayList<>(partitionSize);
                parent.add(tmp);
                size = 0;
            }
            tmp.add(t);
            size++;
        }
        return parent;
    }

    private static void handlePartition(SpatineoServalDao spatineoServalDao,
            List<MapLayer> layers, List<BackendStatus> statuses, ServalServiceType type) {
        final List<ServalService> services = toServices(layers, type);
        final ServalResponse response = spatineoServalDao.query(services);
        if (response == null) {
            LOG.debug("Failed to get response from Spatineo Serval");
            return;
        }
        if (!"OK".equals(response.getStatus())) {
            LOG.info("Received Response with status: ", response.getStatus(), 
                    " statusMessage: ", response.getStatusMessage());
            return;
        }
        final List<ServalResult> results = response.getResult();
        for (int i = 0; i < layers.size(); i++) {
            MapLayer layer = layers.get(i);
            ServalResult result = results.get(i);
            statuses.add(new BackendStatus(layer.getId(), 
                    result.getStatus(), 
                    result.getStatusMessage(), 
                    result.getInfoUrl()));
        }
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
