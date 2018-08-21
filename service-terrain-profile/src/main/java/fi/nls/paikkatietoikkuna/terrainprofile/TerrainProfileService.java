package fi.nls.paikkatietoikkuna.terrainprofile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.coverage.RectifiedGridCoverage;
import org.oskari.wcs.extension.scaling.ScaleByFactor;
import org.oskari.wcs.gml.RectifiedGrid;
import org.oskari.wcs.parser.CapabilitiesParser;
import org.oskari.wcs.parser.CoverageDescriptionsParser;
import org.oskari.wcs.request.DescribeCoverage;
import org.oskari.wcs.request.GetCapabilities;
import org.oskari.wcs.request.GetCoverage;
import org.xml.sax.SAXException;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;

public class TerrainProfileService {

    private static final String FORMAT_TIFF = "image/tiff";
    private static final int REQUEST_MAX_SIZE_METRES = 8192;
    private static final int REQUEST_SIZE_DEFAULT = 1024;
    private static final int SCALE_SIZE_THRESHOLD = 2048;
    private static final double[] SCALE_FACTORS = {
            1,
            0.5,
            0.25,
            0.125,
            0.0625,
            0.03125,
            0.015625,
            0.0078125
    };

    private final String endPoint;
    private final Capabilities caps;
    private final RectifiedGridCoverage desc;
    private final double originEast;
    private final double originNorth;
    private final double offsetVectorX;
    private final double offsetVectorY;

    public TerrainProfileService(String endPoint, String coverageId) throws ServiceException {
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
     * @param numPoints
     *      number of coordinates you want back (interpolate more points if necessary)
     * @param scaleFactor
     *      non-positive considered null, must be 1/2^n, where 0<=n<=8
     */
    public List<DataPoint> getTerrainProfile(double[] coordinates, int numPoints, double scaleFactor)
            throws ServiceException {
        double[] extent = GeomUtil.getEnvelope(coordinates);

        scaleFactor = determineScaleFactor(scaleFactor, extent);

        double dx = offsetVectorX / scaleFactor;
        double dy = offsetVectorY / scaleFactor;

        int tileSize = getTileSize(extent, dx);

        if (coordinates.length < numPoints * 2) {
            coordinates = interpolate(coordinates, numPoints);
        }

        List<DataPoint> points = createDataPoints(coordinates, tileSize, dx, dy);

        Map<GridTile, List<DataPoint>> pointsByTile = points.stream()
                .collect(Collectors.groupingBy(p -> new GridTile(p.getTileX(), p.getTileY())));
        for (List<DataPoint> pointsInTile : pointsByTile.values()) {
            setAltitudes(pointsInTile, scaleFactor, dx, dy);
        }

        points.sort(new Comparator<DataPoint>() {
            @Override
            public int compare(DataPoint o1, DataPoint o2) {
                return Double.compare(o1.getDistFromStart(), o2.getDistFromStart());
            }
        });
        return points;
    }

    private int getTileSize(double[] extent, double dx) {
        int tileSize = REQUEST_SIZE_DEFAULT;
        while (tileSize * dx > REQUEST_MAX_SIZE_METRES && tileSize > 32) {
            tileSize /= 2;
        }
        if (tileSize < 64) {
            tileSize = 1;
        }
        return tileSize;
    }

    private double determineScaleFactor(double scaleFactor, double[] extent) {
        if (scaleFactor > 0) {
            for (double temp : SCALE_FACTORS) {
                if (scaleFactor == temp) {
                    return scaleFactor;
                }
            }
        }

        double widthMetres = extent[2] - extent[0];
        double heightMetres = extent[3] - extent[1];
        for (double sf : SCALE_FACTORS) {
            double xPerPx = Math.abs(offsetVectorX / sf);
            double yPerPx = Math.abs(offsetVectorY / sf);
            int widthPx = (int) Math.round(widthMetres / xPerPx);
            int heightPx = (int) Math.round(heightMetres / yPerPx);
            if (widthPx <= SCALE_SIZE_THRESHOLD && heightPx <= SCALE_SIZE_THRESHOLD) {
                return sf;
            }
        }
        return SCALE_FACTORS[SCALE_FACTORS.length - 1];
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

    private List<DataPoint> createDataPoints(double[] coordinates, int tileSize, double dx, double dy) {
        double e0 = coordinates[0];
        double n0 = coordinates[1];
        double distFromStart = 0.0;

        List<DataPoint> points = new ArrayList<>(coordinates.length / 2);
        for (int i = 0; i < coordinates.length;) {
            double e1 = coordinates[i++];
            double n1 = coordinates[i++];
            distFromStart += GeomUtil.getDistance(e1, n1, e0, n0);

            int gridX = (int) Math.round(((e1 - originEast) / dx));
            int gridY = (int) Math.round(((n1 - originNorth) / dy));
            int tileX = gridX / tileSize;
            int tileY = gridY / tileSize;

            DataPoint dp = new DataPoint();
            dp.setE(e1);
            dp.setN(n1);
            dp.setDistFromStart(distFromStart);
            dp.setGridX(gridX);
            dp.setGridY(gridY);
            dp.setTileX(tileX);
            dp.setTileY(tileY);
            points.add(dp);

            e0 = e1;
            n0 = n1;
        }
        return points;
    }

    private void setAltitudes(List<DataPoint> pointsInTile, double scaleFactor, double dx, double dy)
            throws ServiceException {
        int minGridX = Integer.MAX_VALUE;
        int minGridY = Integer.MAX_VALUE;
        int maxGridX = Integer.MIN_VALUE;
        int maxGridY = Integer.MIN_VALUE;

        for (DataPoint p : pointsInTile)  {
            int gridX = p.getGridX();
            if (gridX < minGridX) {
                minGridX = gridX;
            }
            if (gridX > maxGridX) {
                maxGridX = gridX;
            }
            int gridY = p.getGridY();
            if (gridY < minGridY) {
                minGridY = gridY;
            }
            if (gridY > maxGridY) {
                maxGridY = gridY;
            }
        }

        double eastMin = originEast + minGridX * dx;
        double eastMax;
        if (minGridX == maxGridX) {
            eastMax = eastMin + dx;
        } else {
            eastMax = originEast + (maxGridX + 1) * dx;
        }

        double northMin = originNorth + maxGridY * dy;
        double northMax;
        if (minGridY == maxGridY) {
            northMax = northMin - dy;
        } else {
            northMax = originNorth + (minGridY - 1) * dy;
        }

        GetCoverage getCoverage = new GetCoverage(caps, desc, FORMAT_TIFF);
        getCoverage.subset("E", eastMin, eastMax);
        getCoverage.subset("N", northMin, northMax);
        getCoverage.scaling(new ScaleByFactor(scaleFactor));
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
            FloatGeoTIFF tiff = new FloatGeoTIFF(response);
            setAltitudes(pointsInTile, tiff, minGridX, minGridY);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Unexpected TIFF file", e);
        }
    }

    private void setAltitudes(List<DataPoint> pointsInTile, FloatGeoTIFF tiff, int minGridX, int minGridY) {
        for (DataPoint point : pointsInTile) {
            int x = point.getGridX() - minGridX;
            int y = point.getGridY() - minGridY;
            float alt = tiff.getValue(x, y);
            point.setAltitude(alt);
        }
    }

}
