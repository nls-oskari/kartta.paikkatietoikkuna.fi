package fi.nls.paikkatietoikkuna.terrainprofile;

import java.util.List;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.MultiPoint;

public class GeoJSONHelper {

    public static double[] getCoordinates2D(LineString geom) {
        List<LngLatAlt> list = geom.getCoordinates();
        double[] arr = new double[list.size() * 2];
        int i = 0;
        for (LngLatAlt coord : list) {
            arr[i++] = coord.getLongitude();
            arr[i++] = coord.getLatitude();
        }
        return arr;
    }

    public static MultiPoint toMultiPoint3D(double[] terrainProfile) {
        int numPoints = terrainProfile.length / 3;
        LngLatAlt[] points = new LngLatAlt[numPoints];
        for (int i = 0, j = 0; i < numPoints; i++) {
            double e = terrainProfile[j++];
            double n = terrainProfile[j++];
            double a = terrainProfile[j++];
            points[i] = new LngLatAlt(e, n, a);
        }
        return new MultiPoint(points);
    }

}
