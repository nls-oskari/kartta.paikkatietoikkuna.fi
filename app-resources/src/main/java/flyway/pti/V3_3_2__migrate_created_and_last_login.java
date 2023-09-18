package flyway.pti;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class V3_3_2__migrate_created_and_last_login extends BaseJavaMigration {
    class User {
        Long id;
        String attributes;
        String userName;
        OffsetDateTime created;
        OffsetDateTime lastLogin;
    }

    private Logger log = LogFactory.getLogger(V3_3_2__migrate_created_and_last_login.class);

    private final String CREATED_JSON_KEY = "created";
    private final String LAST_LOGIN_JSON_KEY = "lastLogin";
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<User> users = getUsers(connection);
        users.forEach(user -> {
            JSONObject attributes = JSONHelper.createJSONObject(user.attributes);
            try {

                String created = null;
                String lastLogin = null;
                if (attributes.has(CREATED_JSON_KEY)) {
                    created = attributes.getString(CREATED_JSON_KEY);
                    attributes.remove(CREATED_JSON_KEY);
                }
                if (attributes.has(LAST_LOGIN_JSON_KEY)) {
                    lastLogin = attributes.getString(LAST_LOGIN_JSON_KEY);
                    attributes.remove(LAST_LOGIN_JSON_KEY);
                }

                user.attributes = attributes.toString();

                if (created != null) {
                    user.created = this.formatDateTime(created);
                }

                if (lastLogin != null) {
                    user.lastLogin = this.formatDateTime(lastLogin);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        saveChanges(connection, users);
    }

    public List<User> getUsers(Connection conn) throws Exception {
        List<User> users = new ArrayList<>();
        final String sql = "SELECT id, user_name, attributes, created, last_login FROM oskari_users";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    User user = new User();
                    user.id = rs.getLong("id");
                    user.attributes = rs.getString("attributes");
                    OffsetDateTime created = rs.getObject("created", OffsetDateTime.class);
                    OffsetDateTime lastLogin = rs.getObject("last_login", OffsetDateTime.class);
                    if (created != null) {
                        user.created = created;
                    }
                    if (lastLogin != null) {
                        user.lastLogin = lastLogin;
                    }
                    users.add(user);
                }
            }
        }
        return users;
    }

    public OffsetDateTime formatDateTime(String stringDate) {
        if (stringDate == null || stringDate.equals("null")) {
            return null;
        }

        String format = stringDate.indexOf('T') > -1 ? "yyyy-MM-dd'T'HH:mm:ss" : "yyyy-MM-dd HH:mm:ss.SSS";
        OffsetDateTime offsetDateTime =
            OffsetDateTime.of(LocalDateTime.parse(stringDate, DateTimeFormatter.ofPattern(format)), ZoneOffset.UTC);

        return offsetDateTime;
    }

    private void saveChanges(Connection conn, List<User> users) throws SQLException {
        final String sql = "UPDATE oskari_users SET attributes = ?, created = ? , last_login = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (User user : users) {
                ps.setString(1, user.attributes);
                ps.setObject(2, user.created);
                ps.setObject(3, user.lastLogin);
                ps.setLong(4, user.id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
