package fi.nls.paikkatietoikkuna.terrainprofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiPoint;
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

    @Test
    public void testToMultiPoint3DWithZeroPoints() {
        float[] terrainProfile = {};
        MultiPoint mp = GeoJSONHelper.toMultiPoint3D(terrainProfile);
        List<LngLatAlt> coords = mp.getCoordinates();
        assertEquals(0, coords.size());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testToMultiPoint3DWithNumberOfOordinatesNotDivisibleBy3() {
        float[] terrainProfile = { 1.0f, 2.0f };
        GeoJSONHelper.toMultiPoint3D(terrainProfile);
        fail();
    }

    @Test
    public void testToMultiPoint3DWithOnePoint() {
        float[] terrainProfile = { 100.0f, 200.0f, 300.0f };
        MultiPoint mp = GeoJSONHelper.toMultiPoint3D(terrainProfile);
        List<LngLatAlt> coords = mp.getCoordinates();
        assertEquals(1, coords.size());

        LngLatAlt p1 = coords.get(0);
        assertEquals(100.0, p1.getLongitude(), 0.0);
        assertEquals(200.0, p1.getLatitude(), 0.0);
        assertEquals(300.0, p1.getAltitude(), 0.0);
    }

    @Test
    public void testToMultiPoint3DWithTwoPoints() {
        float[] terrainProfile = {
                100.0f, 200.0f, 300.0f,
                200.0f, 300.0f, 302.0f
        };
        MultiPoint mp = GeoJSONHelper.toMultiPoint3D(terrainProfile);
        List<LngLatAlt> coords = mp.getCoordinates();
        assertEquals(2, coords.size());

        LngLatAlt p1 = coords.get(0);
        assertEquals(100.0, p1.getLongitude(), 0.0);
        assertEquals(200.0, p1.getLatitude(), 0.0);
        assertEquals(300.0, p1.getAltitude(), 0.0);

        LngLatAlt p2 = coords.get(1);
        assertEquals(200.0, p2.getLongitude(), 0.0);
        assertEquals(300.0, p2.getLatitude(), 0.0);
        assertEquals(302.0, p2.getAltitude(), 0.0);
    }

}
