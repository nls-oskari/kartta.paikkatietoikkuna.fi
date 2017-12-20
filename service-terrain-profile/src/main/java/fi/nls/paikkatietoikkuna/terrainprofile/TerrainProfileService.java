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
import org.oskari.wcs.gml.RectifiedGrid;
import org.oskari.wcs.parser.CapabilitiesParser;
import org.oskari.wcs.parser.CoverageDescriptionsParser;
import org.oskari.wcs.request.DescribeCoverage;
import org.oskari.wcs.request.GetCapabilities;
import org.oskari.wcs.request.GetCoverage;
import org.xml.sax.SAXException;

public class TerrainProfileService {

    private static final String FORMAT_TIFF = "image/tiff";

    private final String endPoint;
    private final Capabilities caps;
    private final RectifiedGridCoverage desc;
    private final double originEast;
    private final double originNorth;
    private final double offsetVectorX;
    private final double offsetVectorY;

    public TerrainProfileService(String endPoint, String coverageId)
            throws ServiceException {
        try {
            this.endPoint = endPoint;
            caps = getCapabilities(endPoint);
            CoverageDescription tmp = describeCoverage(endPoint, coverageId);
            if (!(tmp instanceof RectifiedGridCoverage)) {
                throw new ServiceException("Expected coverage of type RectifiedGridCoverage");
            }
            desc = (RectifiedGridCoverage) tmp;
            RectifiedGrid grid = desc.getDomainSet();
            originEast = grid.getOrigin().getPos()[0];
            originNorth = grid.getOrigin().getPos()[1];
            offsetVectorX = grid.getOffsetVectors()[0].getPos()[0];
            offsetVectorY = grid.getOffsetVectors()[1].getPos()[1];
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new ServiceException("Failed to initialize", e);
        }
    }

    private Capabilities getCapabilities(String endPoint)
            throws IOException, ParserConfigurationException, SAXException {
        Map<String, String> params = GetCapabilities.toQueryParameters();
        String url = IOHelper.constructUrl(endPoint, params);
        HttpURLConnection conn = IOHelper.getConnection(url);
        try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
            return CapabilitiesParser.parse(in);
        }
    }

    private CoverageDescription describeCoverage(String endPoint, String coverageId)
            throws IOException, ParserConfigurationException, SAXException {
        Map<String, String> params = DescribeCoverage.toQueryParameters(coverageId);
        String url = IOHelper.constructUrl(endPoint, params);
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
    public List<DataPoint> getTerrainProfile(double[] coordinates, int numPoints)
            throws ServiceException {
        if (coordinates.length / 2 < numPoints) {
            coordinates = interpolate(coordinates, numPoints);
        }
        int gridTileSize = getGridTileSize(coordinates);
        List<DataPoint> points = createDataPoints(coordinates, gridTileSize);
        points.sort(new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint o1, DataPoint o2) {
                int d = o1.getGridTileY() - o2.getGridTileY();
                if (d != 0) {
                    return d;
                }
                d = o1.getGridTileX() - o2.getGridTileX();
                return d != 0 ? d : o1.getGridTileOffset() - o2.getGridTileOffset();
            }
        });

        int prevGridTileY = -1;
        int prevGridTileX = -1;
        int prevGridTileOff = -1;
        float[] grid = null;
        float altitude = 0f;

        for (DataPoint point : points) {
            int gridTileX = point.getGridTileX();
            int gridTileY = point.getGridTileY();
            int gridTileOff = point.getGridTileOffset();
            if (prevGridTileX != gridTileX || prevGridTileY != gridTileY) {
                 grid = getGrid(gridTileSize, gridTileX, gridTileY);
                 prevGridTileOff = -1;
                 prevGridTileX = gridTileX;
                 prevGridTileY = gridTileY;
            }
            // Cache the altitude to a local variable, it might be needed multiple times
            if (prevGridTileOff != gridTileOff) {
                altitude = grid[gridTileOff];
                prevGridTileOff = gridTileOff;
            }
            point.setAltitude(altitude);
        }

        points.sort(new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint o1, DataPoint o2) {
                return Double.compare(o1.getDistFromStart(), o2.getDistFromStart());
            }
        });
        return points;
    }

    private double[] interpolate(double[] coordinates, int numDataPoints) {
        double[] interpolated = new double[numDataPoints * 2];

        double segmentLength = GeomUtil.getLength(coordinates) / (numDataPoints - 1);
        double remainingSegmentLength = segmentLength;

        int i = 0;
        int j = 0;

        double x0 = coordinates[j++];
        double y0 = coordinates[j++];
        double x1 = coordinates[j++];
        double y1 = coordinates[j++];

        double dx = x1 - x0;
        double dy = y1 - y0;
        double distanceToNextPoint = Math.sqrt(dx * dx + dy * dy);
        double dxNormalized = dx / distanceToNextPoint;
        double dyNormalized = dy / distanceToNextPoint;

        // Add first point
        interpolated[i++] = x0;
        interpolated[i++] = y0;

        while (i < interpolated.length - 2) {
            if (distanceToNextPoint < remainingSegmentLength) {
                remainingSegmentLength -= distanceToNextPoint;
                x0 = x1;
                y0 = y1;
                x1 = coordinates[j++];
                y1 = coordinates[j++];
                dx = x1 - x0;
                dy = y1 - y0;
                distanceToNextPoint = Math.sqrt(dx * dx + dy * dy);
                dxNormalized = dx / distanceToNextPoint;
                dyNormalized = dy / distanceToNextPoint;
            } else {
                x0 += remainingSegmentLength * dxNormalized;
                y0 += remainingSegmentLength * dyNormalized;
                interpolated[i++] = x0;
                interpolated[i++] = y0;
                distanceToNextPoint -= remainingSegmentLength;
                remainingSegmentLength = segmentLength;
            }
        }

        // Add last point
        interpolated[interpolated.length - 2] = coordinates[coordinates.length - 2];
        interpolated[interpolated.length - 1] = coordinates[coordinates.length - 1];

        return interpolated;
    }

    /**
     * Determine grid tile size to use for this request
     *
     * Logic here is to avoid creating large requests when the
     * envelope is large, in that case we move from range requests
     * to single point requests.
     *
     * When the envelope is small enough the overhead of
     * unnecessary altitude values within the tiles
     * is less than the overhead of making more service requests
     *
     * If the width or the height of the bounding envelope is over
     * - 50k meters use use single data point requests
     * - 25k meters use  64x64  tiles
     * - 10k meters use 128x128 tiles
     * - default to 256x256 tiles
     *
     * TODO: This method and the logic behind it can be further improved
     * - Current method ignores the number of coordinates
     * - For envelopes with small width and large height (or vice versa)
     *     larger grid tile sizes could be beneficial (compared to 1 for example)
     *     if there's a lot of coordinates (if not then probably not)
     */
    private int getGridTileSize(double[] coordinates) {
        double[] envelope = GeomUtil.getEnvelope(coordinates);
        double w = envelope[2] - envelope[0];
        double h = envelope[3] - envelope[1];
        if (w > 50000 || h > 50000) {
            return 1;
        }
        if (w > 25000 || h > 25000) {
            return 64;
        }
        if (w > 10000 || h > 10000) {
            return 128;
        }
        return 256;
    }

    private List<DataPoint> createDataPoints(double[] coordinates, int gridTileSize) {
        double e0 = coordinates[0];
        double n0 = coordinates[1];
        double distFromStart = 0.0;

        List<DataPoint> points = new ArrayList<>(coordinates.length / 2);
        for (int i = 0; i < coordinates.length;) {
            double e1 = coordinates[i++];
            double n1 = coordinates[i++];
            distFromStart += GeomUtil.getDistance(e1, n1, e0, n0);

            DataPoint dp = new DataPoint();
            dp.setE(e1);
            dp.setN(n1);
            dp.setDistFromStart(distFromStart);

            int gridX = (int) ((e1 - originEast) / offsetVectorX);
            int gridY = (int) ((n1 - originNorth) / offsetVectorY);
            if (gridTileSize == 1) {
                dp.setGridTileX(gridX);
                dp.setGridTileY(gridY);
                dp.setGridTileOffset(0);
            } else {
                int gridTileX = gridX / gridTileSize;
                int gridTileY = gridY / gridTileSize;
                int gridTileOffsetX = gridX % gridTileSize;
                int gridTileOffsetY = gridY % gridTileSize;
                int gridTileOffset = gridTileOffsetY * gridTileSize + gridTileOffsetX;
                dp.setGridTileX(gridTileX);
                dp.setGridTileY(gridTileY);
                dp.setGridTileOffset(gridTileOffset);
            }
            points.add(dp);
            e0 = e1;
            n0 = n1;
        }
        return points;
    }

    private float[] getGrid(int gridTileSize, int gridTileX, int gridTileY)
            throws ServiceException {
        double e0 = gridTileX * offsetVectorX * gridTileSize + originEast;
        double n1 = gridTileY * offsetVectorY * gridTileSize + originNorth;

        GetCoverage getCoverage = new GetCoverage(caps, desc, FORMAT_TIFF);
        if (gridTileSize == 1) {
            getCoverage.subset("E", e0);
            getCoverage.subset("N", n1);
        } else {
            getCoverage.subset("E", e0, e0 + offsetVectorX * gridTileSize);
            getCoverage.subset("N", n1 + offsetVectorY * gridTileSize, n1);
        }
        Map<String, String[]> getCoverageKVP = getCoverage.toKVP();

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
            TIFFReader tiff = new TIFFReader(ByteBuffer.wrap(response));
            IFD ifd = tiff.getIFD(0);
            boolean tiled = ifd.getTileOffsets() != null;
            float[] data = new float[gridTileSize * gridTileSize];
            if (tiled) {
                tiff.readTile(0, 0, data);
            } else {
                tiff.readStrip(0, 0, data);
            }
            return data;
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Unexpected TIFF file", e);
        }
    }

}
