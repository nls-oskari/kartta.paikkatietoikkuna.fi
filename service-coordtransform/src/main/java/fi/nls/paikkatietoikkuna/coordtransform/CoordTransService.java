package fi.nls.paikkatietoikkuna.coordtransform;

import com.vividsolutions.jts.geom.Coordinate;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility functions for using the remote CoordTrans service
 */
public class CoordTransService {

    private static final String SEP_COORD = ";";
    private static final String SEP_COORD_PART = ",";

    public static String createQuery(String sourceCrs, String targetCrs,
            final List<Coordinate> coords, final int dimension) {
        StringBuilder sb = new StringBuilder();
        sb.append("?sourceCRS=").append(sourceCrs);
        sb.append("&targetCRS=").append(targetCrs);
        sb.append("&coords=");
        for (int i = 0; i < coords.size(); i++) {
            if (i > 0) {
                sb.append(SEP_COORD);
            }
            Coordinate coord = coords.get(i);
            sb.append(coord.x).append(SEP_COORD_PART).append(coord.y);
            if (dimension == 3) {
                sb.append(SEP_COORD_PART).append(coord.z);
            }
        }
        return sb.toString();
    }

    public static void parseResponse(byte[] resp, List<Coordinate> coords, final int dimension) {
        if (resp[0] == 'V') {
            // "Virhe: " - send only the part after prefix
            throw new IllegalArgumentException(new String(resp, 7, resp.length - 7, StandardCharsets.UTF_8));
        }

        String response = new String(resp, StandardCharsets.US_ASCII);
        String[] coordinates = response.split(SEP_COORD);
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate coord = coords.get(i);
            String[] coordinateParts = coordinates[i].split(SEP_COORD_PART);
            coord.x = Double.parseDouble(coordinateParts[0]);
            coord.y = Double.parseDouble(coordinateParts[1]);
            if (dimension == 3) {
                coord.z = Double.parseDouble(coordinateParts[2]);
            }
        }
    }

}
