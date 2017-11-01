package fi.nls.paikkatietoikkuna.terrainprofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.junit.Test;

public class GeoJSONHelperTest {

    @Test(expected=NullPointerException.class)
    public void whenGivenNullThrowsNullPointerException() {
        GeoJSONHelper.getCoordinates2D(null);
        fail();
    }

    @Test
    public void whenGivenZeroCoordinatesReturnsZeroCoordinates() {
        LineString ls = new LineString();
        double[] a = GeoJSONHelper.getCoordinates2D(ls);
        assertEquals(0, a.length);
    }

    @Test
    public void whenGivenOneCoordinateReturnsOneCoordinate() {
        LineString ls = new LineString(new LngLatAlt(100.0, 200.0));
        double[] a = GeoJSONHelper.getCoordinates2D(ls);
        assertEquals(2, a.length);
        assertEquals(100.0, a[0], 0.0);
        assertEquals(200.0, a[1], 0.0);
    }

    @Test
    public void testGetCoordinates2DReturnsLngLatOrder() {
        LineString ls = new LineString(
                new LngLatAlt(100.0, 200.0),
                new LngLatAlt(300.0, 400.0));
        double[] a = GeoJSONHelper.getCoordinates2D(ls);
        assertEquals(4, a.length);
        assertEquals(100.0, a[0], 0.0);
        assertEquals(200.0, a[1], 0.0);
        assertEquals(300.0, a[2], 0.0);
        assertEquals(400.0, a[3], 0.0);
    }

    @Test
    public void whenGiven3DCoordinatesGetCoordinates2DReturnsOnlyFirstTwoDimensions() {
        LineString ls = new LineString(
                new LngLatAlt(100.0, 200.0, 300.0),
                new LngLatAlt(400.0, 500.0, 600.0));
        double[] a = GeoJSONHelper.getCoordinates2D(ls);
        assertEquals(4, a.length);
        assertEquals(100.0, a[0], 0.0);
        assertEquals(200.0, a[1], 0.0);
        assertEquals(400.0, a[2], 0.0);
        assertEquals(500.0, a[3], 0.0);
    }

}
