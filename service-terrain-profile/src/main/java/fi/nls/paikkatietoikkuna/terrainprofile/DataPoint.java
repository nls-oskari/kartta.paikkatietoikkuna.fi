package fi.nls.paikkatietoikkuna.terrainprofile;

public class DataPoint {

    private double e;
    private double n;
    private double distFromStart;
    private int gridX;
    private int gridY;
    private int tileX;
    private int tileY;
    private float altitude;

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

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public int getTileX() {
        return tileX;
    }

    public void setTileX(int tileX) {
        this.tileX = tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public void setTileY(int tileY) {
        this.tileY = tileY;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

}
