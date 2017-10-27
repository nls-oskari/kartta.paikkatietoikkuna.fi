package fi.nls.paikkatietoikkuna.terrainprofile;

public class GeomUtil {

    /**
    *
    * @param terrainProfile
    *      array of doubles [e1,n1,a1,...,eN,nN,aN]
    *      where: e = east (m), n = north (m), a = altitude (m)
    * @return
    *      array of doubles [d1,d2,...,dN]
    *      where: d = distance from start (in 2D)
    */
   public static float[] calculateDistanceFromStart(float[] terrainProfile) {
       return null;
   }
   
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
    
}
