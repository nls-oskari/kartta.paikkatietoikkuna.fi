package fi.nls.paikkatietoikkuna.terrainprofile;

import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.oskari.wcs.capabilities.Capabilities;
import org.oskari.wcs.coverage.CoverageDescription;
import org.oskari.wcs.geotiff.GeoTIFFReader;
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
        try (InputStream in = conn.getInputStream()) {
            return CapabilitiesParser.parse(in);
        }
    }

    private CoverageDescription describeCoverage() throws IOException, ParserConfigurationException, SAXException {
        String url = IOHelper.constructUrl(endPoint, DescribeCoverage.toQueryParameters(demCoverageId));
        HttpURLConnection conn = IOHelper.getConnection(url);
        try (InputStream in = conn.getInputStream()) {
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
     *      array of doubles [e1,n1,a1,...,eN,nN,aN]
     *      e = east (m), n = north (m), a = altitude (m)
     */
    public double[] getTerrainProfile(double[] coordinates, double resolution) throws ServiceException {
        double[] envelope = GeomUtil.getEnvelope(coordinates);
        Map<String, String[]> getCoverageKVP = new GetCoverage(caps, desc)
            .subset("i", envelope[0], envelope[2])
            .subset("j", envelope[1], envelope[3])
            .toKVP();
        String queryString = IOHelper.getParamsMultiValue(getCoverageKVP);
        String request = endPoint + "?" + queryString;
        try {
            HttpURLConnection conn = IOHelper.getConnection(request);
            try (InputStream in = conn.getInputStream()) {
                
            }
            return coordinates;
        } catch (IOException e) {
            throw new ServiceException("Failed to receive data from WCS", e);
        }
    }

}
