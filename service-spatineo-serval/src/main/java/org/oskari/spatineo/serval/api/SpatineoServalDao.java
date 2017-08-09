package org.oskari.spatineo.serval.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;

public class SpatineoServalDao {

    private static final Logger LOG = LogFactory.getLogger(SpatineoServalDao.class);

    private final String endPoint;
    private final ObjectMapper om;

    public SpatineoServalDao(String endPoint) {
        this(endPoint, createObjectMapper());
    }

    public SpatineoServalDao(String endPoint, ObjectMapper om) {
        this.endPoint = endPoint;
        this.om = om == null ? createObjectMapper() : om;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om;
    }

    public ServalResponse query(final List<ServalService> services) {
        try {
            HttpURLConnection conn = IOHelper.postForm(endPoint, buildRequest(services));
            int sc = conn.getResponseCode();
            LOG.debug("Received status code: ", sc);
            try (InputStream in = IOHelper.getInputStream(conn)) {
                return parse(in);
            }
        } catch (IOException e) {
            LOG.warn(e);
            return null;
        }
    }

    protected ServalResponse parse(final InputStream in) throws IOException {
        return om.readValue(in, ServalResponse.class);
    }

    private static Map<String, String> buildRequest(final List<ServalService> services) {
        final Map<String, String> params = new HashMap<>();
        int i = 0;
        for (ServalService s : services) {
            params.put("service[" + i + "][type]", s.getType());
            params.put("service[" + i + "][url]", s.getUrl());
            params.put("service[" + i + "][offering]", s.getOffering());
            i++;
        }
        return params;
    }

}
