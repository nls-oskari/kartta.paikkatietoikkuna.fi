package org.oskari.spatineo.serval.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
        this.om = om;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om;
    }

    public ServalResponse query(final List<ServalService> services) {
        String requestBody = buildRequest(services);
        byte[] b = requestBody.getBytes(StandardCharsets.UTF_8);
        
        try {
            URL url = new URL(this.endPoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", IOHelper.CONTENTTYPE_FORM_URLENCODED);
            conn.setRequestProperty("Content-Length", Integer.toString(b.length));
            try (OutputStream out = conn.getOutputStream()) {
                out.write(b);
            }
            int sc = conn.getResponseCode();
            if (sc != 200) {
                LOG.info("Received status code: ", sc);
            }
            boolean useInputStream = sc / 100 == 2 || sc == 304;
            try (InputStream in = useInputStream ? conn.getInputStream() : conn.getErrorStream()) {
                return om.readValue(in, ServalResponse.class);
            }
        } catch (IOException e) {
            LOG.warn(e);
        }
        return null;
    }
    
    private static String buildRequest(final List<ServalService> services) {
        final Map<String, String> params = new HashMap<>();
        int i = 0;
        for (ServalService s : services) {
            params.put("service[" + i + "][type]", s.getType());
            params.put("service[" + i + "][url]", s.getUrl());
            params.put("service[" + i + "][offering]", s.getOffering());
            i++;
        }
        return formURLEncode(params);
    }

    public static String formURLEncode(final Map<String, String> params) {
        final StringBuilder sb = new StringBuilder();
        if (params != null) {
            boolean first = true;
            for (Entry<String, String> p : params.entrySet()) {
                try {
                    String key = URLEncoder.encode(p.getKey(), "UTF-8");
                    String value = URLEncoder.encode(p.getValue(), "UTF-8");
                    if (!first) {
                        sb.append('&');
                    }
                    sb.append(key).append('=').append(value);
                    first = false;
                } catch (UnsupportedEncodingException e) {
                    // Ignore, UTF-8 is supported
                }
            }
        }
        return sb.toString();
    }

}
