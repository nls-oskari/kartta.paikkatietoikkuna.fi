package flyway.ptistats;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Inserts viewlayer permission for the kunta & seutukunta layers used as regionsets for the new thematic maps
 */
public class V1_36__link_roles_to_hki_layers extends V1_2__link_roles_to_layers {
    private static final Logger LOG = LogFactory.getLogger(V1_36__link_roles_to_hki_layers.class);

    // statslayers described as layer resources for permissions handling
    protected List<String> getLayerIds() {
        Set<String> names = new HashSet<>(4);
        names.add("seutukartta:Seutu_tilastoalueet");
        names.add("seutukartta:Helsinki_osa-alueet");
        names.add("hel:Helsinki_peruspiirit");
        names.add("hel:Helsinki_suurpiirit");

        final String url = "http://geoserver.hel.fi/geoserver/wms";
        OskariLayerService service = OskariComponentManager.getComponentOfType(OskariLayerService.class);
        List<String> list = names.stream()
                .flatMap(name -> service.findByUrlAndName(url, name).stream())
                .map(l -> Integer.toString(l.getId())).collect(Collectors.toList());
        return list;
    }
}
