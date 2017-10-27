package fi.nls.paikkatietoikkuna.terrainprofile;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.geotiff.IFD;
import org.oskari.wcs.geotiff.TIFFReader;
import org.oskari.wcs.parser.CapabilitiesParser;
import org.oskari.wcs.parser.CoverageDescriptionsParser;
import org.oskari.wcs.request.DescribeCoverage;
import org.oskari.wcs.request.GetCapabilities;
import org.oskari.wcs.request.GetCoverage;
import org.xml.sax.SAXException;

public class TerrainProfileService {

    private final String endPoint;
    private final String demCoverageId;
    private final Capabilities caps;
    private final CoverageDescription desc;

    public TerrainProfileService(String endPoint, String demCoverageId)
            throws IOException, ParserConfigurationException, SAXException {
        this.endPoint = endPoint;
        this.demCoverageId = demCoverageId;
        caps = getCapabilities();
        desc = describeCoverage();
    }

    private Capabilities getCapabilities() throws IOException, ParserConfigurationException, SAXException {
        String url = IOHelper.constructUrl(endPoint, GetCapabilities.toQueryParameters());
        HttpURLConnection conn = IOHelper.getConnection(url);
        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            return CapabilitiesParser.parse(in);
        }
    }

    private CoverageDescription describeCoverage() throws IOException, ParserConfigurationException, SAXException {
        String url = IOHelper.constructUrl(endPoint, DescribeCoverage.toQueryParameters(demCoverageId));
        HttpURLConnection conn = IOHelper.getConnection(url);
        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            return CoverageDescriptionsParser.parse(in).get(0);
        }
    }

    /**
     * @param coordinates
     *      array of doubles [e1,n1,...,eN,nN]
     *      e = east (m), n = north (m)
     * @param resolution
     *      meters per pixel (used to choose appropriate level of detail)
     * @return
     *      array of array of floats
     */
    public float[] getTerrainProfile(double[] coordinates, double resolution) throws ServiceException {
        double[] envelope = GeomUtil.getEnvelope(coordinates);
        Map<String, String[]> getCoverageKVP = new GetCoverage(caps, desc)
            .subset("E", envelope[0], envelope[2])
            .subset("N", envelope[1], envelope[3])
            .toKVP();
        String queryString = IOHelper.getParamsMultiValue(getCoverageKVP);
        String request = endPoint + "?" + queryString;
        try {
            HttpURLConnection conn = IOHelper.getConnection(request);
            byte[] response = IOHelper.readBytes(conn);
            TIFFReader tiff = new TIFFReader(ByteBuffer.wrap(response));
            IFD ifd = tiff.getIFD(0);
            int w = ifd.getWidth();
            int h = ifd.getHeight();
            BitSet plot = createPlot(coordinates, envelope[0], envelope[3], w, h, 10.0);
            int tw = ifd.getTileWidth();
            int th = ifd.getTileHeight();
            int tileCount = ifd.getTileOffsets().length;
            float[][] tiles = new float[tileCount][];
            for (int tileIdx = 0; tileIdx < tileCount; tileIdx++) {
                float[] tile = new float[tw * th];
                tiff.readTile(0, tileIdx, tile);
                tiles[tileIdx] = tile;
            }
            return null;
        } catch (IOException e) {
            throw new ServiceException("Failed to receive data from WCS", e);
        }
    }

    private List<DataPoint> createDataPoints(double[] coordinates,
            double eastMin, double northMax, double step) {
        List<DataPoint> points = new ArrayList<>();
        int x1 = (int) ((coordinates[0] - eastMin) / step);
        int y1 = (int) ((-coordinates[1] + northMax) / step); // Flip y-axis
        for (int i = 2; i < coordinates.length;) {
            int x2 = (int) ((coordinates[i++] - eastMin) / step);
            int y2 = (int) ((-coordinates[i++] + northMax) / step);
            g2d.drawLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
        }
    }
    
}
