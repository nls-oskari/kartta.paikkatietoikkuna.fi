package fi.nls.paikkatietoikkuna.coordtransform;

import static org.junit.Assert.assertEquals;

import com.vividsolutions.jts.geom.Coordinate;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class CoordTransQueryBuilderTest {

    @Test
    public void simpleTest() {
        CoordTransQueryBuilder b = new CoordTransQueryBuilder("foo", "bar", "baz");
        List<Coordinate> coords = Arrays.asList(
                new Coordinate(500000, 6822000),
                new Coordinate(501000, 6823000)
        );
        for (Coordinate c : coords) {
            b.add(c);
        }
        String query = b.build();
        assertEquals("foo?sourceCRS=bar"
                + "&targetCRS=baz"
                + "&coords=500000.0,6822000.0;501000.0,6823000.0", query);
    }

    @Test
    public void resetTest() {
        CoordTransQueryBuilder b = new CoordTransQueryBuilder("foo", "bar", "baz");
        List<Coordinate> coords = Arrays.asList(
                new Coordinate(500000, 6822000),
                new Coordinate(501000, 6823000)
        );
        for (Coordinate c : coords) {
            b.add(c);
            b.add(c);
        }
        assertEquals("foo?sourceCRS=bar"
                + "&targetCRS=baz"
                + "&coords=500000.0,6822000.0;500000.0,6822000.0;501000.0,6823000.0;501000.0,6823000.0", b.build());

        b.reset();
        for (Coordinate c : coords) {
            b.add(c);
        }

        assertEquals("foo?sourceCRS=bar"
                + "&targetCRS=baz"
                + "&coords=500000.0,6822000.0;501000.0,6823000.0", b.build());
    }

}
