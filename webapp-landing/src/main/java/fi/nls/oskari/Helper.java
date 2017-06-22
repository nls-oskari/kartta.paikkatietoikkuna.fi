package fi.nls.oskari;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by SMAKINEN on 22.6.2017.
 */
public class Helper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectMapper getMapper() {
        return objectMapper;
    }

    public static String getBasePath() {
        return "/legacy";
    }
}
