package fi.nls.paikkatietoikkuna.terrainprofile.dem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.oskari.wcs.geotiff.IFD;
import org.oskari.wcs.geotiff.TIFFReader;

public class ScaledGrayscaleValueExtractorTest {

    @Test
    public void testScaleOffset() {
        double scale = 32.0;
        double offset = -1000;
        int noData = 0;
        ScaledGrayscaleValueExtractor e = new ScaledGrayscaleValueExtractor(scale, offset, noData);

        double z0 = 527.125;
        double z1 = -425.375;

        int tileWidth = 256;
        int tileHeight = 256;
        short[] tile = new short[tileWidth * tileHeight];
        TIFFReader tiffReader = mock(TIFFReader.class);
        when(tiffReader.readTile(anyInt(), anyInt(), any(short[].class))).thenReturn(tile);

        tile[0] = (short) Math.round(z0 * scale + offset);
        tile[1] = (short) Math.round(z1 * scale + offset);
        tile[2] = (short) noData;

        IFD ifd = mock(IFD.class);
        when(ifd.getTileWidth()).thenReturn(tileWidth);
        when(ifd.getTileHeight()).thenReturn(tileHeight);

        assertEquals(z0, e.getTileValue(tiffReader, ifd, 0, 0, 0), 0);
        assertEquals(z1, e.getTileValue(tiffReader, ifd, 0, 0, 1), 0);
        assertTrue(Double.isNaN(e.getTileValue(tiffReader, ifd, 0, 0, 2)));
    }

}
