package fi.nls.paikkatietoikkuna.coordtransform;

import static org.junit.Assert.assertEquals;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class CoordTransServiceTest {

    @Test
    public void testCreateQuery() {
        List<Coordinate> coords = Arrays.asList(
                new Coordinate(500000, 6822000),
                new Coordinate(501000, 6823000)
        );
        String query = CoordTransService.createQuery("EPSG:3067", "EPSG:4258", coords, 2);
        assertEquals("?sourceCRS=EPSG:3067"
                + "&targetCRS=EPSG:4258"
                + "&coords=500000.0,6822000.0;501000.0,6823000.0", query);
    }

}
