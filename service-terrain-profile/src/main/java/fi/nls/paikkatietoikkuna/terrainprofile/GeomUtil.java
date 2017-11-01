package fi.nls.paikkatietoikkuna.terrainprofile;

public class GeomUtil {

    public static double[] getEnvelope(double[] coordinates) {
        double x1 = Double.MAX_VALUE;
        double y1 = Double.MAX_VALUE;
        double x2 = Double.MIN_VALUE;
        double y2 = Double.MIN_VALUE;
        for (int i = 0; i < coordinates.length;) {
            double x = coordinates[i++];
            double y = coordinates[i++];
            if (x < x1) {
                x1 = x;
            }
            if (x > x2) {
                x2 = x;
            }
            if (y < y1) {
                y1 = y;
            }
            if (y > y2)  {
                y2 = y;
            }
        }
        return new double[] { x1, y1, x2, y2 };
    }

    /**
     * 2D Euclidean distance
     */
    public static double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

}
