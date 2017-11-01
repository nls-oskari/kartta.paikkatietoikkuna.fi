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

    public static MultiPoint toMultiPoint3D(List<DataPoint> dataPoints) {
        int n = dataPoints.size();
        LngLatAlt[] points  = new LngLatAlt[n];
        for (int i = 0; i < n; i++) {
            DataPoint dp = dataPoints.get(i);
            points[i] = new LngLatAlt(dp.getE(), dp.getN(), dp.getAltitude());
        }
        return new MultiPoint(points);
    }

}
