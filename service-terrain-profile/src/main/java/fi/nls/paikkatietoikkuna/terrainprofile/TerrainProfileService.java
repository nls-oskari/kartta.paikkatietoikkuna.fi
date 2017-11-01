package fi.nls.paikkatietoikkuna.terrainprofile;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.coverage.RectifiedGridCoverage;
import org.oskari.wcs.geotiff.IFD;
import org.oskari.wcs.geotiff.TIFFReader;
import org.oskari.wcs.parser.CapabilitiesParser;
import org.oskari.wcs.parser.CoverageDescriptionsParser;
import org.oskari.wcs.request.DescribeCoverage;
import org.oskari.wcs.request.GetCapabilities;
import org.oskari.wcs.request.GetCoverage;
import org.xml.sax.SAXException;

public class TerrainProfileService {

    private static final String FORMAT_TIFF = "image/tiff";
    private static final double STEP = 10.0;

    private final String endPoint;
    private final String demCoverageId;
    protected final Capabilities caps;
    protected final RectifiedGridCoverage desc;

    public TerrainProfileService(String endPoint, String demCoverageId)
            throws ServiceException {
        try {
            this.endPoint = endPoint;
            this.demCoverageId = demCoverageId;
            caps = getCapabilities();
            CoverageDescription tmp = describeCoverage();
            if (!(tmp instanceof RectifiedGridCoverage)) {
                throw new ServiceException("Expected coverage to be of type RectifiedGridCoverage");
            }
            desc = (RectifiedGridCoverage) tmp;
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ServiceException("Failed to initialize", e);
        }
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
    public List<DataPoint> getTerrainProfile(double[] coordinates) throws ServiceException {
        double[] envelope = GeomUtil.getEnvelope(coordinates);
        // Round to nearest step
        envelope[0] = STEP * Math.round(envelope[0] / STEP);
        envelope[1] = STEP * Math.round(envelope[1] / STEP);
        envelope[2] = STEP * Math.round(envelope[2] / STEP);
        envelope[3] = STEP * Math.round(envelope[3] / STEP);

        // Add one step extra to end, since the condition is interpreted as [start,end[
        // Only change this for the request, not for the envelope,
        // envelope is later used for calculating the coordinate<->pixel values
        Map<String, String[]> getCoverageKVP = new GetCoverage(caps, desc, FORMAT_TIFF)
            .subset("E", envelope[0], envelope[2] + STEP)
            .subset("N", envelope[1], envelope[3] + STEP)
            .toKVP();
        String queryString = IOHelper.getParamsMultiValue(getCoverageKVP);
        String request = endPoint + "?" + queryString;
        byte[] response;
        try {
            HttpURLConnection conn = IOHelper.getConnection(request);
            response = IOHelper.readBytes(conn);
        } catch (IOException e) {
            throw new ServiceException("Failed to retrieve data from WCS", e);
        }

        try {
            final TIFFReader tiff = new TIFFReader(ByteBuffer.wrap(response));
            final int ifdIdx = 0; // First image in TIFF file
            final IFD ifd = tiff.getIFD(ifdIdx);
            final int w = ifd.getWidth();
            final int h = ifd.getHeight();
            final int tw = ifd.getTileWidth();
            final int th = ifd.getTileHeight();
            final int tilesAcross = (w + tw - 1) / tw;
            final int tilesDown = (h + th - 1) / th;
            final int tileCount = tilesAcross * tilesDown;

            final float[][] tileCache = new float[tileCount][];

            List<DataPoint> points = createDataPoints(coordinates,
                    envelope[0], envelope[3],
                    tw, th, tilesAcross);
            points.sort(new Comparator<DataPoint>() {
                @Override
                public int compare(DataPoint o1, DataPoint o2) {
                    int d = o1.getTileIdx() - o2.getTileIdx();
                    return d != 0 ? d : o1.getOffsetInTile() - o2.getOffsetInTile();
                }
            });

            for (DataPoint point : points) {
                int tileIdx = point.getTileIdx();
                float[] tile = tileCache[tileIdx];
                if (tile == null) {
                    // Only "read" the tiles we need
                    tile = new float[tw * th];
                    tiff.readTile(ifdIdx, tileIdx, tile);
                    tileCache[tileIdx] = tile;
                }
                float altitude = tileCache[tileIdx][point.getOffsetInTile()];
                point.setAltitude(altitude);
            }

            points.sort(new Comparator<DataPoint>() {
                @Override
                public int compare(DataPoint o1, DataPoint o2) {
                    return Double.compare(o1.getDistFromStart(), o2.getDistFromStart());
                }
            });
            return points;
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Unexpected TIFF file", e);
        }
    }

    private List<DataPoint> createDataPoints(double[] coordinates,
            double eastMin, double northMax,
            int tileWidth, int tileHeight, int tilesAcross) {
        List<DataPoint> points = new ArrayList<>();

        double e1 = coordinates[0];
        double n1 = coordinates[1];
        double distanceFromStart = 0.0;

        int x = (int) Math.round((e1 - eastMin) / STEP);
        int y = (int) Math.round((northMax - n1) / STEP);
        int tileCol = x / tileWidth;
        int tileRow = y / tileHeight;
        int tileIdx = tileRow * tilesAcross + tileCol;
        int tileOffsetX = x % tileWidth;
        int tileOffsetY = y % tileHeight;
        int offsetInTile = tileOffsetY * tileWidth + tileOffsetX;
        points.add(getDataPoint(e1, n1, distanceFromStart, tileIdx, offsetInTile));

        for (int i = 2; i < coordinates.length; i += 2) {
            double e2 = coordinates[i];
            double n2 = coordinates[i + 1];
            distanceFromStart += GeomUtil.getDistance(e1, n1, e2, n2);
            x = (int) Math.round((e2 - eastMin) / STEP);
            y = (int) Math.round((northMax - n2) / STEP);
            tileCol = x / tileWidth;
            tileRow = y / tileHeight;
            tileIdx = tileRow * tilesAcross + tileCol;
            tileOffsetX = x % tileWidth;
            tileOffsetY = y % tileHeight;
            offsetInTile = tileOffsetY * tileWidth + tileOffsetX;
            points.add(getDataPoint(e2, n2, distanceFromStart, tileIdx, offsetInTile));
            e1 = e2;
            n1 = n2;
        }

        return points;
    }

    private DataPoint getDataPoint(double e, double n, double distanceFromStart, int tileIdx, int offsetInTile) {
        DataPoint dp = new DataPoint();
        dp.setE(e);
        dp.setN(n);
        dp.setDistFromStart(distanceFromStart);
        dp.setTileIdx(tileIdx);
        dp.setOffsetInTile(offsetInTile);
        return dp;
    }

}
