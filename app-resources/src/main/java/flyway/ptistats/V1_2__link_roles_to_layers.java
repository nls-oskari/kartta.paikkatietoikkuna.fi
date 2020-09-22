package flyway.ptistats;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.PermissionServiceMybatisImpl;
import org.oskari.permissions.model.*;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Inserts viewlayer permission for the kunta & seutukunta layers used as regionsets for the new thematic maps
 */
public class V1_2__link_roles_to_layers extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_2__link_roles_to_layers.class);

    public void migrate(Context context) throws SQLException {
        PermissionService service = new PermissionServiceMybatisImpl();
        for(String layerId : getLayerIds()) {
            Resource resToUpdate;
            Optional<Resource> dbRes = service.findResource(ResourceType.maplayer, layerId);
            if(dbRes.isPresent()) {
                resToUpdate = dbRes.get();
            } else {
                resToUpdate = new Resource();
                resToUpdate.setType(ResourceType.maplayer);
                resToUpdate.setMapping(layerId);
            }
            for(Role role : getRoles()) {
                if(resToUpdate.hasPermission(role, PermissionType.VIEW_LAYER)) {
                    // already had the permission
                    continue;
                }
                final Permission permission = new Permission();
                permission.setType(PermissionType.VIEW_LAYER);
                permission.setRoleId((int) role.getId());
                resToUpdate.addPermission(permission);
            }
            service.saveResource(resToUpdate);
        }
    }

    // statslayers described as layer resources for permissions handling
    protected List<String> getLayerIds() {
        Set<String> names = new HashSet<>(4);
        names.add("tilastointialueet:kunta4500k_2017");
        names.add("tilastointialueet:seutukunta1000k");

        final String url = "http://geo.stat.fi/geoserver/wms";
        OskariLayerService service = OskariComponentManager.getComponentOfType(OskariLayerService.class);
        List<String> list = names.stream()
                .flatMap(name -> service.findByUrlAndName(url, name).stream())
                .map(l -> Integer.toString(l.getId())).collect(Collectors.toList());
        return list;
    }

    protected List<Role> getRoles() {
        List<Role> list = new ArrayList<>();
        try {
            // "logged in" user
            list.add(Role.getDefaultUserRole());
            // guest user
            User guest = UserService.getInstance().getGuestUser();
            list.addAll(guest.getRoles());
        } catch (ServiceException ex) {
            LOG.warn(ex, "Couldn't get roles listing for statistics layers permissions setup");
        }
        return list;
    }

}
