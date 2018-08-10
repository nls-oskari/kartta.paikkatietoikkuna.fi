package fi.nls.paikkatietoikkuna.coordtransform;

import static org.junit.Assert.assertEquals;

import com.vividsolutions.jts.geom.Coordinate;

import fi.nls.oskari.control.ActionException;

import java.math.BigDecimal;
import java.math.MathContext;
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

    @Test
    public void testTransformToDegree () throws ActionException{
        String [] units = {"radian", "gradian", "DDMMSS","DD MM SS", "DDMM", "DD MM"};
        String [] coords = {"1","100","603012.1451345652","06 00 00.0", "6030.1451345652", "06 00.0"};
        double [] results = {180.0/Math.PI, 90.0, 60.503373648, 6.0, 60.502418909, 6.0};
        for (int i = 0; i < units.length ; i ++ ){
            assertEquals ("Unit: " + units[i], results[i],  CoordTransService.transformUnitToDegree(coords[i], units[i]), 0.000000001);
        }
    }
    @Test
    public void testTransformToUnit () throws ActionException{
        double coord = 1.99999;
        String [] units = {"DD", "DDMMSS", "DD MM SS", "DDMM", "DD MM"};
        String [] results = {"01.999990", "015959.964000", "01 59 59.964000", "0159.999400", "01 59.999400"};
        for (int i = 0; i < units.length ; i ++ ){
            assertEquals ("Unit: " + units[i] + " should have 6 decimals", results[i],  CoordTransService.transformDegreeToUnit(coord, units[i], 6));
        }
        assertEquals ("Unit: radian", "2.00000000000000",  CoordTransService.transformDegreeToUnit(360/Math.PI, "radian", 14));
        assertEquals ("Unit: gradian", "400.00000000000000",  CoordTransService.transformDegreeToUnit(360, "gradian", 14));
        double [] coords = {-100, -10, -1, 0, 1, 10, 100};
        //DD.sss
        results = new String [] {"-100.000", "-10.000", "-01.000", "00.000", "01.000", "10.000", "100.000"};
        for (int i = 0; i < coords.length ; i ++ ){
            assertEquals ("DD and should have 3 decimals", results[i],  CoordTransService.transformDegreeToUnit(coords[i], "DD", 3));
        }
        //DD MM SS.sss
        results = new String [] {"-100 00 00.000", "-10 00 00.000", "-01 00 00.000", "00 00 00.000", "01 00 00.000", "10 00 00.000", "100 00 00.000"};
        for (int i = 0; i < coords.length ; i ++ ){
            assertEquals ("DD MM SS and should have 3 decimals", results[i],  CoordTransService.transformDegreeToUnit(coords[i], "DD MM SS", 3));
        }
    }
    @Test
    public void testTransform () throws ActionException{
        double coord = 60.503373648013675;
        String [] units = {"DDMMSS", "DD MM SS", "DDMM", "DD MM"};
        for (int i = 0; i < units.length ; i ++ ){
            String unit = CoordTransService.transformDegreeToUnit(coord, units[i], 14);
            double deg = CoordTransService.transformUnitToDegree(unit, units[i]);
            assertEquals ("Unit: " + units[i], coord,  deg, 0.00000000000001);
        }
    }
}
