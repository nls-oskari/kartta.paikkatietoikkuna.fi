package fi.nls.paikkatietoikkuna.terrainprofile;

public class DataPoint {

    private double e;
    private double n;
    private double distFromStart;
    private int gridTileX;
    private int gridTileY;
    private int gridTileOffset;
    private double altitude;

    public double getE() {
        return e;
    }

    public void setE(double e) {
        this.e = e;
    }

    public double getN() {
        return n;
    }

    public void setN(double n) {
        this.n = n;
    }

    public double getDistFromStart() {
        return distFromStart;
    }

    public void setDistFromStart(double distFromStart) {
        this.distFromStart = distFromStart;
    }

    public int getGridTileX() {
        return gridTileX;
    }

    public void setGridTileX(int gridTileX) {
        this.gridTileX = gridTileX;
    }

    public int getGridTileY() {
        return gridTileY;
    }

    public void setGridTileY(int gridTileY) {
        this.gridTileY = gridTileY;
    }

    public int getGridTileOffset() {
        return gridTileOffset;
    }

    public void setGridTileOffset(int gridTileOffset) {
        this.gridTileOffset = gridTileOffset;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

}
