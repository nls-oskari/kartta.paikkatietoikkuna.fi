package fi.nls.paikkatietoikkuna.coordtransform;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class CoordTransServiceTest {

    @Test
    public void testCreateQuery() {
        String query = CoordTransService.createQuery("EPSG:3067", "EPSG:4258", 2,
                new double[] { 500000.0, 6822000.0, 501000.0, 6823000.0 });
        assertEquals("?sourceCRS=EPSG:3067"
                + "&targetCRS=EPSG:4258"
                + "&coords=500000.0,6822000.0;501000.0,6823000.0", query);
    }

    @Test
    public void testParseAsciiDouble() {
        byte[] ascii = "12345125.123".getBytes(StandardCharsets.US_ASCII);
        double expect = 12345125.123;
        double actual = CoordTransService.parseAsciiDouble(ascii, 0, ascii.length);
        double epsilon = 0.0000001;
        assertEquals(expect, actual, epsilon);
    }

}
