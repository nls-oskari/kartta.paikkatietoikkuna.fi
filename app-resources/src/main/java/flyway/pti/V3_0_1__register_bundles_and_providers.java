package flyway.pti;

import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;
import org.oskari.helpers.BundleHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Registers app-specific bundles and dataproviders for paikkatietoikkuna if they are not registered yet.
 * These could be run as sql migrations more easily but we need to check if the data already exists before writing it.
 */
public class V3_0_1__register_bundles_and_providers extends BaseJavaMigration {

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        if (!AppSetupHelper.getSetupsForApplicationByType(connection, "geoportal", ViewTypes.DEFAULT).isEmpty()) {
            // database has the default appsetups no need to add them!
            return;
        }
        String[] bundles = {"lang-overrides", "register", "terrain-profile", "telemetry", "coordinatetransformation"};
        for(String name : bundles) {
            BundleHelper.registerBundle(connection, name);
        }

        String[] dataProviders = {"Maanmittauslaitos", "Yhteistyöaineistot", "Helsingin kaupunki"};
        for(String name : dataProviders) {
            insertDataprovider(connection, createLocale(name));
        }
        // referenced by layers/statistical regionsets
        String[] groups = {"Taustakartat", "Ortoilmakuvat", "Statistical units"};
        for(String name : groups) {
            insertGroup(connection, createLocale(name));
        }
        insertRoles(connection);
    }

    protected static JSONObject createLocale(String name)
            throws JSONException {
        JSONObject locale = new JSONObject();
        JSONObject defaultLang = new JSONObject();

        defaultLang.put("name", name);
        locale.put(PropertyUtil.getDefaultLanguage(), defaultLang);
        return locale;
    }

    protected static void insertDataprovider(Connection connection, JSONObject locale)
            throws SQLException {
        final String sql = "INSERT INTO oskari_dataprovider (locale) values (?)";
        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, locale.toString());
            statement.execute();
        }
    }

    protected static void insertGroup(Connection connection, JSONObject locale)
            throws SQLException {
        final String sql = "INSERT INTO oskari_maplayer_group (locale) values (?)";
        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, locale.toString());
            statement.execute();
        }
    }
    // TODO: get rid of configurable role names...
    protected static void insertRoles(Connection connection)
            throws SQLException {
        final String userRole = PropertyUtil.get("oskari.user.role.loggedIn", "User").trim();
        if (!"User".equals(userRole)) {
            final String sql = "INSERT INTO oskari_roles (name, is_guest) values (?, false)";
            try (final PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userRole);
                statement.execute();
            }
        }
        final String adminRole = PropertyUtil.get("oskari.user.role.admin", "Admin").trim();
        if (!"Admin".equals(adminRole)) {
            final String sql = "INSERT INTO oskari_roles (name, is_guest) values (?, false)";
            try (final PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, adminRole);
                statement.execute();
            }
        }
    }
}
