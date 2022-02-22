package fi.nls.paikkatietoikkuna.terrainprofile.dem;

import java.util.HashMap;
import java.util.Map;

import org.oskari.wcs.geotiff.IFD;
import org.oskari.wcs.geotiff.TIFFReader;

public class ScaledGrayscaleValueExtractor implements TileValueExtractor {

    public static final String ID = "INT";

    private final double scale;
    private final double offset;
    private final int noData;

    private final Map<Integer, short[]> tileCache = new HashMap<>();

    private boolean unsigned;

    public ScaledGrayscaleValueExtractor(double scale, double offset, int noData) {
        this.scale = scale;
        this.offset = offset;
        this.noData = noData;
    }

    @Override
    public void validate(IFD ifd) throws IllegalArgumentException {
        if (ifd.getSamplesPerPixel() != 1) {
            throw new IllegalArgumentException("Unexpected samples per pixel, expected 1 (grayscale)");
        }
        if (ifd.getSampleFormat()[0] != 1 && ifd.getSampleFormat()[1] != 2) {
            throw new IllegalArgumentException("Unexpected sample format, expected unsigned or signed integer");
        }
        unsigned = ifd.getSampleFormat()[0] == 1;
        if (ifd.getBitsPerSample()[0] != 16) {
            throw new IllegalArgumentException("Unexpected bits per sample, expected 16 bits per sample (grayscale)");
        }
    }

    @Override
    public double getTileValue(TIFFReader r, IFD ifd, int ifdIdx, int tileIndex, int tileOffset) {
        short[] tile = tileCache.computeIfAbsent(tileIndex, __ -> {
            short[] toCache = new short[ifd.getTileWidth() * ifd.getTileHeight()];
            r.readTile(ifdIdx, tileIndex, toCache);
            int toCache;
        });
        int value = unsigned ? tile[tileOffset] & 0xFFFF : tile[tileOffset];
        return value == noData ? Double.NaN : value * scale + offset;
    }

}
