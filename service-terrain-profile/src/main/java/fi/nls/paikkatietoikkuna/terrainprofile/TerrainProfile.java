package fi.nls.paikkatietoikkuna.terrainprofile;

public class TerrainProfile {

    /**
     * @param coordinates
     *      array of doubles [e1,n1,...,eN,nN]
     *      e = east (m), n = north (m)
     * @param resolution
     *      meters per pixel (used to choose appropriate level of detail)
     * @return
     *      array of doubles [e1,n1,a1,...,eN,nN,aN]
     *      e = east (m), n = north (m), a = altitude (m)
     */
    public static double[] getTerrainProfile(double[] coordinates, double resolution) {
        return coordinates;
    }

    /**
     *
     * @param terrainProfile
     *      array of doubles [e1,n1,a1,...,eN,nN,aN]
     *      where: e = east (m), n = north (m), a = altitude (m)
     * @return
     *      array of doubles [d1,d2,...,dN]
     *      where: d = distance from start (in 2D)
     */
    public static double[] calculateDistanceFromStart(double[] terrainProfile) {
        return null;
    }
}
