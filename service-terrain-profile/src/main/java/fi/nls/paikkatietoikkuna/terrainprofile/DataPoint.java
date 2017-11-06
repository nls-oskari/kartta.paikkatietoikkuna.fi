package fi.nls.paikkatietoikkuna.terrainprofile;

public class DataPoint {

    private double e;
    private double n;
    private double altitude;
    private double distFromStart;
    private int tileIdx;
    private int offsetInTile;

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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getDistFromStart() {
        return distFromStart;
    }

    public void setDistFromStart(double distFromStart) {
        this.distFromStart = distFromStart;
    }

    public int getTileIdx() {
        return tileIdx;
    }

    public void setTileIdx(int tileIdx) {
        this.tileIdx = tileIdx;
    }

    public int getOffsetInTile() {
        return offsetInTile;
    }

    public void setOffsetInTile(int offsetInTile) {
        this.offsetInTile = offsetInTile;
    }

}
