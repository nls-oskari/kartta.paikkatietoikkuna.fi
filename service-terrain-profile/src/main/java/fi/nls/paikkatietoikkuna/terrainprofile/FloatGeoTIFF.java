package fi.nls.paikkatietoikkuna.terrainprofile;

import org.oskari.wcs.geotiff.IFD;
import org.oskari.wcs.geotiff.TIFFReader;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class FloatGeoTIFF {

    private static final Logger LOG = LogFactory.getLogger(FloatGeoTIFF.class);

    private final TIFFReader r;
    private final IFD ifd;
    private final float[][] tiles;
    private final int tilesAcross;

    public FloatGeoTIFF(byte[] buf) throws IllegalArgumentException {
        this.r = new TIFFReader(buf);
        this.ifd = r.getIFD(0);
        if (ifd.getSampleFormat()[0] != 3) {
            throw new IllegalArgumentException("Unexpected sample format, expected float32");
        }
        if (ifd.getTileOffsets() == null) {
            throw new IllegalArgumentException("Unexpected TIFF file, expected Tiled TIFF");
        }

        int tw = ifd.getTileWidth();
        int th = ifd.getTileHeight();

        int tilesAcross = ifd.getWidth() / tw;
        if (tilesAcross * tw < ifd.getWidth()) {
            tilesAcross++;
        }
        int tilesDown = ifd.getHeight() / th;
        if (tilesDown * th < ifd.getHeight()) {
            tilesDown++;
        }
        this.tilesAcross = tilesAcross;

        tiles = new float[ifd.getTileOffsets().length][tw * th];
        for (int i = 0; i < ifd.getTileOffsets().length; i++) {
            r.readTile(0, i, tiles[i]);
        }

        LOG.debug("Width:", ifd.getWidth(), "Height:", ifd.getHeight(),
                "TileWidth:", tw, "TileHeight:", th,
                "TilesAcross:", tilesAcross, "TilesDown:", tilesDown,
                "NumTiles:", ifd.getTileOffsets().length);
    }

    public float getValue(int x, int y) {
        int tileX = x / ifd.getTileWidth();
        int offX = x % ifd.getTileWidth();
        int tileY = y / ifd.getTileHeight();
        int offY = y % ifd.getTileHeight();
        int tileIndex = tileY * tilesAcross + tileX;
        int tileOffset = offY * ifd.getTileWidth() + offX;
        try {
            return tiles[tileIndex][tileOffset];
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.warn("Tile X:", tileX, "Off X", offX,
                    "Tile Y:", tileY, "Off Y", offY,
                    "TileIndex:", tileIndex, "TileOffset:", tileOffset);
            return 0.0f;
        }
    }

}