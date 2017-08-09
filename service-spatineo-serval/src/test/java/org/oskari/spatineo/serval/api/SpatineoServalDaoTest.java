package org.oskari.spatineo.serval.api;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
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

}
