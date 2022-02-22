package fi.nls.paikkatietoikkuna.terrainprofile.dem;

import org.oskari.wcs.geotiff.IFD;
import org.oskari.wcs.geotiff.TIFFReader;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class TiledTiffDEM {

    private static final Logger LOG = LogFactory.getLogger(TiledTiffDEM.class);

    private static final int IFD_IDX = 0;

    private final TIFFReader r;
    private final IFD ifd;
    private final int tilesAcross;
    private final TileValueExtractor extractor;

    public TiledTiffDEM(TIFFReader r, TileValueExtractor extractor) throws IllegalArgumentException {
        this.r = r;
        this.ifd = r.getIFD(IFD_IDX);
        this.extractor = extractor;

        if (ifd.getTileOffsets() == null) {
            throw new IllegalArgumentException("Unexpected TIFF file, expected Tiled TIFF");
        }
        extractor.validate(ifd);

        int tw = ifd.getTileWidth();
        int th = ifd.getTileHeight();

        int tilesAcross = ifd.getWidth() / tw;
        if (tilesAcross * tw < ifd.getWidth()) {
            tilesAcross++;
        }
        this.tilesAcross = tilesAcross;

        int tilesDown = ifd.getHeight() / th;
        if (tilesDown * th < ifd.getHeight()) {
            tilesDown++;
        }

        LOG.debug("Width:", ifd.getWidth(), "Height:", ifd.getHeight(),
                "TileWidth:", tw, "TileHeight:", th,
                "TilesAcross:", tilesAcross, "TilesDown:", tilesDown,
                "NumTiles:", ifd.getTileOffsets().length);
    }

    public double getValue(int x, int y) {
        int tileX = x / ifd.getTileWidth();
        int offX = x % ifd.getTileWidth();
        int tileY = y / ifd.getTileHeight();
        int offY = y % ifd.getTileHeight();
        int tileIndex = tileY * tilesAcross + tileX;
        int tileOffset = offY * ifd.getTileWidth() + offX;
        return extractor.getTileValue(r, ifd, IFD_IDX, tileIndex, tileOffset);
    }

}