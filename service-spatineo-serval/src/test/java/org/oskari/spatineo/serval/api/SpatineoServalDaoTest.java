package org.oskari.spatineo.serval.api;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.oskari.spatineo.serval.api.ServalService.ServalServiceType;

public class SpatineoServalDaoTest {

    static final String endPoint = "https://monitor.spatineo.com/api/public/availability-1.0";
    static SpatineoServalDao serval;

    @BeforeClass 
    public static void init() {
        serval = new SpatineoServalDao(endPoint);
    }

    @Test
    @Ignore("Performs external HTTP requests.")
    public void simpleTestCase() {
        List<ServalService> services = Arrays.asList(
                new ServalService(ServalServiceType.WMS,
                        "http://kartat.lounaispaikka.fi/ms6/maakuntakaavat/varsinais-suomi/maakuntakaava_ms6",
                        "mk_tunnelit")
                );
        ServalResponse response = serval.query(services);
        assertNotNull(response);
        assertEquals("OK", response.getStatus());
        assertNotNull(response.getResult());
        assertEquals(1, response.getResult().size());
        assertNotEquals("ERROR", response.getResult().get(0).getStatus());
    }

    @Test
    public void singleStatusOK() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/single_response_ok.json")) {
            ServalResponse response = serval.parse(in);
            assertEquals("OK", response.getStatus());
            assertEquals(1, response.getResult().size());
            assertEquals("OK", response.getResult().get(0).getStatus());
        }
    }

    @Test
    public void multipleStatusTest() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/multiple_response_partial_error.json")) {
            ServalResponse response = serval.parse(in);
            assertEquals("OK", response.getStatus());
            assertEquals(2, response.getResult().size());
            assertEquals("ERROR", response.getResult().get(0).getStatus());
            assertEquals("OK", response.getResult().get(1).getStatus());
        }
    }

}
