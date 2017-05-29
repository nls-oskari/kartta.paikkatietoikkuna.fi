package flyway.ptimigration;

/**
 * Created by SMAKINEN on 26.5.2017.
 */
public class V1_2__drop_myplaces_config extends ConfigMigration {

    public String getBundle() {
        return "myplaces2";
    }

    public String getModifiedConfig(final String myplaces) throws Exception {
        return "{ \"measureTools\" : true }";
    }
}