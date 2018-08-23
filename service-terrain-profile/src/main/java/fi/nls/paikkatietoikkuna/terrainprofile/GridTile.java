package fi.nls.paikkatietoikkuna.terrainprofile;

public class GridTile {

    private final int tileX;
    private final int tileY;

    public GridTile(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public int getTileX() {
        return tileX;
    }

    public int getTileY() {
        return tileY;
    }

    public int hashCode() {
        return tileX * 31 + tileY;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof GridTile)) {
            return false;
        }
        GridTile t = (GridTile) o;
        if (tileX != t.tileX) {
            return false;
        }
        if (tileY != t.tileY) {
            return false;
        }
        return true;
    }

}
