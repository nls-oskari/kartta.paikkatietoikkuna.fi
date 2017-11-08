package fi.nls.paikkatietoikkuna.terrainprofile;

import static org.junit.Assert.assertEquals;

import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.service.ServiceException;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

public class TerrainProfileServiceTest {

    @Test
    @Ignore("Depends on an outside API")
    public void offsetIsCorrect() throws IOException, ActionException, ParserConfigurationException, SAXException, ServiceException {
        String endPoint = "http://avoindata.maanmittauslaitos.fi/geoserver/wcs";
        String coverageId = "korkeusmalli_10m__korkeusmalli_10m";
        TerrainProfileService tps = new TerrainProfileService(endPoint, coverageId);
        double[] coordinates = new double[] {
                500002, 6822001,
                501003, 6821004,
                502006, 6823007,
                501003, 6822509,
                500502, 6823206
        };

        List<DataPoint> points = tps.getTerrainProfile(coordinates, 0);
        for (DataPoint p : points) {
            double e = p.getE();
            double n = p.getN();
            DataPoint single = tps.getTerrainProfile(new double[] { e, n }, 0).get(0);
            assertEquals(e, single.getE(), 0.0);
            assertEquals(n, single.getN(), 0.0);
            assertEquals(p.getAltitude(), single.getAltitude(), 0.0);
        }
    }

}
