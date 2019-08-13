package flyway.ptistats;

import fi.mml.portti.domain.permissions.Permissions;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.PermissionServiceMybatisImpl;
import org.oskari.permissions.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Inserts viewlayer permission for the kunta & seutukunta layers used as regionsets for the new thematic maps
 */
public class V1_2__link_roles_to_layers implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_2__link_roles_to_layers.class);

    public void migrate(Connection connection)
            throws SQLException {
        PermissionService service = new PermissionServiceMybatisImpl();
        for(Resource resToUpdate : getResources()) {
            Optional<Resource> dbRes = service.findResource(ResourceType.maplayer, resToUpdate.getMapping());
            if(dbRes.isPresent()) {
                resToUpdate = dbRes.get();
            }
            for(Role role : getRoles()) {
                if(resToUpdate.hasPermission(role, PermissionType.VIEW_LAYER)) {
                    // already had the permission
                    continue;
                }
                final Permission permission = new Permission();
                permission.setRoleId((int) role.getId());
                permission.setType(PermissionType.VIEW_LAYER);
                resToUpdate.addPermission(permission);
            }
            service.saveResource(resToUpdate);
        }
    }

    // statslayers described as layer resources for permissions handling
    private List<Resource> getResources() {
        List<Resource> list = new ArrayList<>();
        list.add(new OskariLayerResource(OskariLayer.TYPE_STATS, "http://geo.stat.fi/geoserver/wms", "tilastointialueet:kunta4500k_2017"));
        list.add(new OskariLayerResource(OskariLayer.TYPE_STATS, "http://geo.stat.fi/geoserver/wms", "tilastointialueet:seutukunta1000k"));
        return list;
    }

    private List<Role> getRoles() {
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
