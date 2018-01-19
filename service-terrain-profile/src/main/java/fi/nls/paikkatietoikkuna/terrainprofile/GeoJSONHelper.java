package fi.nls.paikkatietoikkuna.terrainprofile;

import java.util.List;
import org.geojson.LineString;
import org.geojson.LngLatAlt;

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

}
