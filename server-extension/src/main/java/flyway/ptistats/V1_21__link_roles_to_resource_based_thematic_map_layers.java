package flyway.ptistats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.PermissionServiceMybatisImpl;
import org.oskari.permissions.model.*;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;

/**
 * @see V1_20__use_resources_for_thematic_map_layers
 */
public class V1_21__link_roles_to_resource_based_thematic_map_layers implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_21__link_roles_to_resource_based_thematic_map_layers.class);

    private static final String RESOURCES_URL_PREFIX = "resources://";
    private static final String DIR = "stats-regionsets/";

    private enum ResourceStatLayer {

        NUTS1("dummy:nuts1", "nuts1_2017.json"),
        ERVA("dummy:erva", "erva_2017.json"),
        SHP("dummy:sairaanhoitopiiri", "sairaanhoitopiirit_2017.json");

        final String name;
        final String path;

        private ResourceStatLayer(String name, String path) {
            this.name = name;
            this.path = path;
        }

        private String getUrl() {
            return RESOURCES_URL_PREFIX + DIR + path;
        }

        private Resource getResourceTemplate() {
            return new OskariLayerResource(OskariLayer.TYPE_STATS, getUrl(), name);
        }

    }

    public void migrate(Connection connection) throws SQLException {
        ResourceStatLayer[] layers = ResourceStatLayer.values();
        updateLayerUrls(connection, layers);
        updatePermissions(connection, layers);
    }

    private void updateLayerUrls(Connection connection, ResourceStatLayer[] layers) throws SQLException {
        String sql = "UPDATE oskari_maplayer "
                + "SET url = ? "
                + "WHERE type = ? AND name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (ResourceStatLayer layer : layers) {
                ps.setString(1, layer.getUrl());
                ps.setString(2, OskariLayer.TYPE_STATS);
                ps.setString(3, layer.name);
                LOG.debug("Executing:", ps.toString());
                int i = ps.executeUpdate();
                LOG.debug("Update result:", i);
            }
        }
    }

    private void updatePermissions(Connection connection, ResourceStatLayer[] layers) {
        PermissionService service = new PermissionServiceMybatisImpl();
        List<Role> roles = getRoles();
        for (ResourceStatLayer layer : layers) {
            Resource resource = layer.getResourceTemplate();
            for (Role role : roles) {
                if (!resource.hasPermission(role, PermissionType.EDIT_LAYER)) {
                    Permission permission = getPermission(role);
                    resource.addPermission(permission);
                }
            }
            service.saveResource(resource);
            LOG.debug("Saved resource, id:", resource.getId(),
                    "type:", resource.getType(),
                    "mapping:", resource.getMapping());
        }
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

    private Permission getPermission(Role role) {
        Permission permission = new Permission();
        permission.setType(PermissionType.VIEW_LAYER);
        permission.setRoleId((int)role.getId());
        return permission;
    }

}
