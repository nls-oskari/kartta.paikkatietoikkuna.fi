package fi.nls.paikkatietoikkuna.terrainprofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.service.ServiceException;
import fi.nls.test.control.MockServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public void whenResolutionIsMissingThrowsActionParamsException() throws JsonProcessingException, ActionException {
        Feature feature = new Feature();
        LineString line = new LineString();
        line.add(new LngLatAlt(100, 100));
        line.add(new LngLatAlt(200, 100));
        feature.setGeometry(line);
        String routeStr = om.writeValueAsString(feature);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TerrainProfileHandler.PARAM_ROUTE)).thenReturn(routeStr);
        ActionParameters params = new ActionParameters();
        params.setRequest(request);

        try {
            handler.handleAction(params);
            fail();
        } catch (ActionParamsException e) {
            assertEquals("Required property 'resolution' missing!", e.getMessage());
        }
    }

    @Test
    @Ignore("Depends on a outside API")
    public void whenInputIsCorrectWePass() throws IOException, ActionException, ServiceException {
        Feature feature = new Feature();
        LineString line = new LineString();
        line.add(new LngLatAlt(500000, 6822000));
        line.add(new LngLatAlt(501000, 6823000));
        feature.setGeometry(line);
        feature.setProperty(TerrainProfileHandler.JSON_PROPERTY_RESOLUTION, 1.0);
        String routeStr = om.writeValueAsString(feature);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter(TerrainProfileHandler.PARAM_ROUTE)).thenReturn(routeStr);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(baos));

        ActionParameters params = new ActionParameters();
        params.setRequest(request);
        params.setResponse(response);

        String endPoint = "http://avoindata.maanmittauslaitos.fi/geoserver/wcs";
        String coverageId = "korkeusmalli_10m__korkeusmalli_10m";
        TerrainProfileService tps = new TerrainProfileService(endPoint, coverageId);
        new TerrainProfileHandler(om, tps).handleAction(params);

        System.out.println(baos.toString());
    }

}
