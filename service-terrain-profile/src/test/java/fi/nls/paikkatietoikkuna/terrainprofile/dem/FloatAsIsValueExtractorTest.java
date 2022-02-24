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

public class FloatAsIsValueExtractorTest {

    @Test
    public void testScaleOffset() {
        float noData = -9999.9f;
        FloatAsIsValueExtractor e = new FloatAsIsValueExtractor(noData);

        double z0 = 3527.125;
        double z1 = -534.375;

        int tileWidth = 256;
        int tileHeight = 256;
        float[] tile = new float[tileWidth * tileHeight];
        TIFFReader tiffReader = mock(TIFFReader.class);
        when(tiffReader.readTile(anyInt(), anyInt(), any(float[].class))).thenReturn(tile);

        tile[0] = (float) z0;
        tile[1] = (float) z1;
        tile[2] = noData;

        IFD ifd = mock(IFD.class);
        when(ifd.getTileWidth()).thenReturn(tileWidth);
        when(ifd.getTileHeight()).thenReturn(tileHeight);

        assertEquals(z0, e.getTileValue(tiffReader, ifd, 0, 0, 0), 0);
        assertEquals(z1, e.getTileValue(tiffReader, ifd, 0, 0, 1), 0);
        assertTrue(Double.isNaN(e.getTileValue(tiffReader, ifd, 0, 0, 2)));
    }

}
