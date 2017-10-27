package fi.nls.paikkatietoikkuna.terrainprofile;

import fi.nls.oskari.service.ServiceException;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import fi.nls.oskari.control.ActionException;
import java.io.IOException;
import org.junit.Test;

public class TerrainProfileServiceTest {

    @Test
    public void fooBar() throws IOException, ActionException, ParserConfigurationException, SAXException, ServiceException {
        TerrainProfileService tps = new TerrainProfileService(
                "http://avoindata.maanmittauslaitos.fi/geoserver/wcs",
                "korkeusmalli_10m__korkeusmalli_10m");
        double x = 500000;
        double y = 6822000;
        double d = 10.0;
        int n = 100;
        double[] coordinates = new double[2 * n]; 
        for (int i = 0; i < n; i++) {
            double off = i * d;
            coordinates[2 * i] = x + off;
            coordinates[2 * i + 1] = y + off;
        }
        tps.getTerrainProfile(coordinates, d);
    }

}
