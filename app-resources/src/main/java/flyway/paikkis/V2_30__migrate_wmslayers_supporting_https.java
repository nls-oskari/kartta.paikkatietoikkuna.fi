package flyway.paikkis;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

/**
 * Change oskari_maplayer.url for WMS layers with http:// urls to https:// if they support it
 * Also update oskari_resources accordingly
 */
public class V2_30__migrate_wmslayers_supporting_https extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V2_30__migrate_wmslayers_supporting_https.class);
    private static final int TIMEOUT_MS_CONNECT = 1000;
    private static final int TIMEOUT_MS_READ = 5000;

    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        List<WMSLayer> httpWMSLayers = getHTTPWMSLayers(conn);
        Map<String, List<WMSLayer>> layersByURL = httpWMSLayers.stream()
                .filter(layer -> !isMultipleURL(layer))
                .map(layer -> normalizeURL(layer))
                .collect(Collectors.groupingBy(WMSLayer::getModifiedURL));

        List<WMSLayer> layersSupportingHTTPS = new ArrayList<>();
        for (String url : layersByURL.keySet()) {
            List<WMSLayer> layers = layersByURL.get(url);
            String username = layers.stream().findAny().get().getUsername();
            String password = layers.stream().findAny().get().getPassword();
            boolean supportsHTTPS = checkForHTTPSsupport(url, username, password);
            if (supportsHTTPS) {
                layersSupportingHTTPS.addAll(layers);
            }
        }

        LOG.debug("Changing url for following layers:", layersSupportingHTTPS);
        for (WMSLayer layer : layersSupportingHTTPS) {
            updateURL(conn, layer);
            updateResource(conn, layer);
        }
    }

    private List<WMSLayer> getHTTPWMSLayers(Connection conn) throws SQLException {
        String sql = "SELECT id, name, type, url, username, password "
                + "FROM oskari_maplayer "
                + "WHERE type = ? "
                + "AND url LIKE 'http://%' ";

        List<WMSLayer> layers = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, OskariLayer.TYPE_WMS);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    layers.add(parseLayer(rs));
                }
            }
        }

        return layers;
    }

    private WMSLayer parseLayer(ResultSet rs) throws SQLException {
        WMSLayer layer = new WMSLayer();
        layer.setId(rs.getInt("id"));
        layer.setName(rs.getString("name"));
        layer.setType(rs.getString("type"));
        layer.setUrl(rs.getString("url"));
        layer.setUsername(rs.getString("username"));
        layer.setPassword(rs.getString("password"));
        return layer;
    }

    private boolean isMultipleURL(WMSLayer layer) {
        // URL like http://a.foo.bar,http://b.foo.bar,http://c.foo.bar
        return layer.getUrl().indexOf("http://", 5) > 0;
    }

    private WMSLayer normalizeURL(WMSLayer layer) {
        String url = layer.getUrl();
        int i = url.indexOf('?');
        if (i < 0) {
            if (url.charAt(url.length() - 1) == '/') {
                // Replace last character with '?'
                url = url.substring(0,  url.length() - 1) + "?";
            } else {
                url = url + "?";
            }
        }
        url = url.replace("http://", "https://");
        layer.setModifiedURL(url);
        if (!url.equals(layer.getUrl())) {
            LOG.debug("Changed URL", layer.getUrl(), "to", url);
        }
        return layer;
    }

    /**
     * Tests if service responds with 200 OK and with a content type that contains 'xml' (case-insensitive)
     */
    private static boolean checkForHTTPSsupport(String url, String username, String password) {
        try {
            String getCapabilities = url + "service=WMS&request=GetCapabilities";
            HttpURLConnection conn = IOHelper.getConnection(getCapabilities, username, password);
            conn.setConnectTimeout(TIMEOUT_MS_CONNECT);
            conn.setReadTimeout(TIMEOUT_MS_READ);
            int sc = conn.getResponseCode();
            if (sc != 200) {
                LOG.debug(getCapabilities, "returned status:", sc);
                return false;
            }
            String contentType = conn.getContentType();
            if (!contentType.toLowerCase().contains("xml")) {
                LOG.debug(getCapabilities, "returned unexpected content type:", contentType);
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.info(e.getMessage());
            return false;
        }
    }

    private void updateURL(Connection conn, WMSLayer layer) throws SQLException {
        String sql = "UPDATE oskari_maplayer SET url=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, layer.getModifiedURL());
            ps.setInt(2, layer.getId());
            int n = ps.executeUpdate();
            if (n == 0) {
                LOG.info("Updated zero rows oskari_maplayer WHERE id=:", layer.getId());
            }
        }
    }

    private void updateResource(Connection conn, WMSLayer layer) throws SQLException {
        String sql = "UPDATE oskari_resource SET resource_mapping=? WHERE resource_mapping=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String newMapping = getResourceMapping(layer.getType(), layer.getModifiedURL(), layer.getName());
            String oldMapping = getResourceMapping(layer.getType(), layer.getUrl(), layer.getName());
            LOG.debug("Updating resource_mapping:", oldMapping, "=>", newMapping);
            ps.setString(1, newMapping);
            ps.setString(2, oldMapping);
            int n = ps.executeUpdate();
            if (n == 0) {
                LOG.info("Updated zero rows oskari_resource WHERE resource_mapping=:", oldMapping);
            }
        }
    }

    private String getResourceMapping(String type, String url, String name) {
        return String.format("%s+%s+%s", type, url, name);
    }

    protected static class WMSLayer {

        int id;
        String name;
        String type;
        String url;
        String modifiedURL;
        String username;
        String password;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getModifiedURL() {
            return modifiedURL;
        }

        public void setModifiedURL(String modifiedURL) {
            this.modifiedURL = modifiedURL;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

}
