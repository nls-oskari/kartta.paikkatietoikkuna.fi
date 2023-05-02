package flyway.ptistats;

import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Inserts viewlayer permission for the hyvinvointialue4500k layer used as regionset for the new thematic maps
 */
public class V1_50__link_roles_to_hyvinvointi extends V1_2__link_roles_to_layers {

    // statslayers described as layer resources for permissions handling
    protected List<String> getLayerIds() {
        Set<String> names = new HashSet<>(4);
        names.add("tilastointialueet:hyvinvointialue4500k");

        final String url = "https://geo.stat.fi/geoserver/ows";
        OskariLayerService service = OskariComponentManager.getComponentOfType(OskariLayerService.class);
        List<String> list = names.stream()
                .flatMap(name -> service.findByUrlAndName(url, name).stream())
                .filter(l -> "statslayer".equals(l.getType()))
                .map(l -> Integer.toString(l.getId())).collect(Collectors.toList());
        return list;
    }
}
