package fi.nls.paikkatietoikkuna.terrainprofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.service.ServiceException;
import fi.nls.test.control.MockServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import org.geojson.Feature;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiPoint;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TerrainProfileHandlerTest {

    private static ObjectMapper om;
    private static TerrainProfileHandler handler;

    @BeforeClass
    public static void init() throws IOException, ParserConfigurationException, SAXException {
        om = new ObjectMapper();
        handler = new TerrainProfileHandler(om, null);
    }

    @Test
    public void whenRouteParameterIsMissingThrowsActionParamsException() throws JsonProcessingException, ActionException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TerrainProfileHandler.PARAM_ROUTE)).thenReturn(null);
        ActionParameters params = new ActionParameters();
        params.setRequest(request);
        try {
            handler.handleAction(params);
            fail();
        } catch (ActionParamsException e) {
            assertEquals("Required parameter 'route' missing!", e.getMessage());
        }
    }

    @Test
    public void whenRouteParameterIsEmptyThrowsActionParamsException() throws JsonProcessingException, ActionException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TerrainProfileHandler.PARAM_ROUTE)).thenReturn("");
        ActionParameters params = new ActionParameters();
        params.setRequest(request);
        try {
            handler.handleAction(params);
            fail();
        } catch (ActionParamsException e) {
            assertEquals("Required parameter 'route' missing!", e.getMessage());
        }
    }

    @Test
    public void whenRouteParameterIsNotGeoJSONFeatureThrowsActionParamsException() throws JsonProcessingException, ActionException {
        LineString line = new LineString();
        line.add(new LngLatAlt(100, 100));
        line.add(new LngLatAlt(200, 100));
        String lineStr = om.writeValueAsString(line);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TerrainProfileHandler.PARAM_ROUTE)).thenReturn(lineStr);
        ActionParameters params = new ActionParameters();
        params.setRequest(request);

        try {
            handler.handleAction(params);
            fail();
        } catch (ActionParamsException e) {
            assertEquals("Invalid input - expected GeoJSON feature", e.getMessage());
        }
    }

    @Test
    public void whenGeometryOfFeatureIsNotLineStringThrowsActionParamsException() throws JsonProcessingException, ActionException {
        Feature feature = new Feature();
        MultiPoint mp = new MultiPoint();
        mp.add(new LngLatAlt(100, 100));
        mp.add(new LngLatAlt(200, 100));
        feature.setGeometry(mp);
        String routeStr = om.writeValueAsString(feature);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TerrainProfileHandler.PARAM_ROUTE)).thenReturn(routeStr);
        ActionParameters params = new ActionParameters();
        params.setRequest(request);

        try {
            handler.handleAction(params);
            fail();
        } catch (ActionParamsException e) {
            assertEquals("Invalid input - expected LineString geometry", e.getMessage());
        }
    }

    @Test
    public void whenLineHasOnePointThrowsActionParamsException() throws JsonProcessingException, ActionException {
        Feature feature = new Feature();
        LineString ls = new LineString();
        ls.add(new LngLatAlt(0, 0));
        feature.setGeometry(ls);
        String routeStr = om.writeValueAsString(feature);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TerrainProfileHandler.PARAM_ROUTE)).thenReturn(routeStr);
        ActionParameters params = new ActionParameters();
        params.setRequest(request);

        try {
            handler.handleAction(params);
            fail();
        } catch (ActionParamsException e) {
            assertEquals("Invalid input - expected LineString with atleast two coordinates", e.getMessage());
        }
    }

    @Test
    public void whenLineHasTooManyPointsThrowsActionParamsException() throws JsonProcessingException, ActionException {
        Feature feature = new Feature();
        LineString ls = new LineString();
        for (int i = 0; i < 10000; i++) {
            ls.add(new LngLatAlt(i, i));
        }
        feature.setGeometry(ls);
        String routeStr = om.writeValueAsString(feature);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TerrainProfileHandler.PARAM_ROUTE)).thenReturn(routeStr);
        ActionParameters params = new ActionParameters();
        params.setRequest(request);

        try {
            handler.handleAction(params);
            fail();
        } catch (ActionParamsException e) {
            assertTrue(e.getMessage().startsWith("Invalid input"
                    + " - too many coordinates, maximum is"));
        }
    }

    @Test
    @Ignore("Depends on an outside API")
    public void whenInputIsCorrectWePass() throws IOException, ActionException, ServiceException {
        Feature feature = new Feature();
        LineString line = new LineString();
        line.add(new LngLatAlt(500000, 6822000));
        line.add(new LngLatAlt(501000, 6823000));
        feature.setGeometry(line);
        String routeStr = om.writeValueAsString(feature);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TerrainProfileHandler.PARAM_ROUTE)).thenReturn(routeStr);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(baos));

        ActionParameters params = new ActionParameters();
        params.setRequest(request);
        params.setResponse(response);

        String endPoint = "https://beta-karttakuva.maanmittauslaitos.fi/wcs/service/ows";
        String coverageId = "korkeusmalli__korkeusmalli";
        TerrainProfileService tps = new TerrainProfileService(endPoint, coverageId);
        new TerrainProfileHandler(om, tps).handleAction(params);
    }

    @Test
    public void testWriteMultiPointFeature() throws IOException {
        DataPoint p1 = new DataPoint();
        p1.setE(0.0);
        p1.setN(0.0);
        p1.setAltitude(300.0f);
        p1.setDistFromStart(0.0);

        DataPoint p2 = new DataPoint();
        p2.setE(100.0);
        p2.setN(0.0);
        p2.setAltitude(400.0f);
        p2.setDistFromStart(100.0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (JsonGenerator json = new JsonFactory().createGenerator(baos)) {
            TerrainProfileHandler.writeMultiPointFeature(Arrays.asList(p1, p2), json, Float.NaN);
        }

        ObjectMapper om = new ObjectMapper();
        Feature feature = om.readValue(baos.toByteArray(), Feature.class);

        MultiPoint mp = (MultiPoint) feature.getGeometry();
        List<LngLatAlt> coordinates = mp.getCoordinates();
        assertEquals(2, coordinates.size());

        LngLatAlt c1 = coordinates.get(0);
        assertEquals(0.0, c1.getLongitude(), 0.0);
        assertEquals(0.0, c1.getLatitude(), 0.0);
        assertEquals(300.0, c1.getAltitude(), 0.0);

        LngLatAlt c2 = coordinates.get(1);
        assertEquals(100.0, c2.getLongitude(), 0.0);
        assertEquals(0.0, c2.getLatitude(), 0.0);
        assertEquals(400.0, c2.getAltitude(), 0.0);

        int numPoints = (Integer) feature.getProperty(TerrainProfileHandler.JSON_PROPERTY_NUM_POINTS);
        assertEquals(2, numPoints);

        List<Double> distFromStart = feature.getProperty(TerrainProfileHandler.JSON_PROPERTY_DISTANCE_FROM_START);
        assertEquals(2, distFromStart.size());
        assertEquals(0.0, distFromStart.get(0).doubleValue(), 0.0);
        assertEquals(100.0, distFromStart.get(1).doubleValue(), 0.0);
    }

}
