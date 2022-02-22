package fi.nls.paikkatietoikkuna.terrainprofile.dem;

import org.oskari.wcs.geotiff.IFD;
import org.oskari.wcs.geotiff.TIFFReader;

public interface TileValueExtractor {

    public void validate(IFD ifd) throws IllegalArgumentException;

    /**
     * @return value at tileIndex, tileOffset or Double.NaN if the value is NO_DATA
     */
    public double getTileValue(TIFFReader r, IFD ifd, int ifdIdx, int tileIndex, int tileOffset);


}
