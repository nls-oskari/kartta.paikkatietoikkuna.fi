package flyway.ptistats;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @see V1_20__use_resources_for_thematic_map_layers
 */
public class V1_21__link_roles_to_resource_based_thematic_map_layers extends V1_2__link_roles_to_layers {

    private static final Logger LOG = LogFactory.getLogger(V1_21__link_roles_to_resource_based_thematic_map_layers.class);

    private static final String RESOURCES_URL_PREFIX = "resources://";
    private static final String DIR = "stats-regionsets/";

    private enum ResourceStatLayer {

        NUTS1("dummy:nuts1", "nuts1_2017.json"),
        ERVA("dummy:erva", "erva_2017.json"),
        SHP("dummy:sairaanhoitopiiri", "sairaanhoitopiirit_2017.json");

        final String name;
        final String path;

        ResourceStatLayer(String name, String path) {
            this.name = name;
            this.path = path;
        }

        private String getUrl() {
            return RESOURCES_URL_PREFIX + DIR + path;
        }

    }

    // statslayers described as layer resources for permissions handling
    protected List<String> getLayerIds() {
        Set<String> names = new HashSet<>(4);
        names.add("seutukartta:Seutu_suuralueet");
        names.add("seutukartta:Seutu_pienalueet-alueet");

        final String url = "http://geoserver.hel.fi/geoserver/wms";
        OskariLayerService service = OskariComponentManager.getComponentOfType(OskariLayerService.class);
        return Arrays.stream(ResourceStatLayer.values())
                .flatMap(res -> service.findByUrlAndName(res.getUrl(), res.name).stream())
                .map(l -> Integer.toString(l.getId())).collect(Collectors.toList());

    }
}
