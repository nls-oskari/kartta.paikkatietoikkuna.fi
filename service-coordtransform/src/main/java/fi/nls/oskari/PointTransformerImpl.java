package fi.nls.oskari;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.paikkatietoikkuna.coordtransform.CoordTransQueryBuilder;
import fi.nls.paikkatietoikkuna.coordtransform.CoordTransWorker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointTransformerImpl extends NLSFIPointTransformer {

    private static final Logger log = LogFactory.getLogger(PointTransformerImpl.class);
    private static final String PROP_END_POINT = "coordtransform.endpoint";
    private static final List<String> UNSUPPORTED = Arrays.asList("EPSG:4326");
    private static final String EPSG_ETRS89_GEOGRAPHIC = "EPSG:4258";
    private static final double GK_ZONE_OFF_MINUTUES = 30;
    private static final int EPSG_CODE_GK19FIN = 3873;
    private static final int GKFIN_ZONE_START = 19;
    private static final int GKFIN_ZONE_END = 31;
    private static final String PREFIX_EPSG = "EPSG:";
    private static final String NLSFI_GKn = "NLSFI:etrs_gk";
    private static final Map<String, String> EPSG_CODE_MAP = initEpsgCodeMap();
    private static final Map<String, Boolean> AXIS_ORDER_MAP = initAxisOrderMap();
    private CoordTransWorker worker;
    private String endPoint;

    private static Map<String, String> initEpsgCodeMap() {
        Map<String,String> codeMap = new HashMap<>();
        codeMap.put("LATLON:kkj", PREFIX_EPSG + "4123");
        codeMap.put("NLSFI:ykj", PREFIX_EPSG + "2393");
        return codeMap;
    }

    private static Map<String, Boolean> initAxisOrderMap() {
        Map<String,Boolean> axisIsNorthFirstMap = new HashMap<>();
        axisIsNorthFirstMap.put(PREFIX_EPSG + "4123", true);
        return axisIsNorthFirstMap;
    }

    @Override
    public Point reproject(Point point, String sourceSRS, String targetSRS) {
        if (point == null || sourceSRS == null || targetSRS == null) {
            return null;
        }
        try {
            String sourceEPSG = getEpsgCode(sourceSRS);
            String targetEPSG = getEpsgCode(targetSRS);

            if (sourceSRS.equals(NLSFI_GKn)) {
                int zone = (int)(point.getLon() / 1E6);
                sourceEPSG = getEPSGForGKnFIN(getGKnFINZone(zone));
            }
            if (targetSRS.equals(NLSFI_GKn)) {
                targetEPSG = getEPSGForGKnFIN(getGKnFINZone(point, sourceEPSG));
            }

            if (isSupported(sourceEPSG, targetEPSG)) {
                CoordTransQueryBuilder queryBuilder = getQueryBuilder(sourceEPSG, targetEPSG);
                Coordinate coord = getCoordinate(point, isNorthAxisFirst(sourceEPSG));
                getWorker().transform(queryBuilder, Arrays.asList(coord));
                return getPoint(coord, isNorthAxisFirst(targetEPSG));
            }
        } catch (Exception e) {
            log.debug("Reprojecting point", point, "from", sourceSRS, "to", targetSRS, "failed", e);
        }

        return super.reproject(point, sourceSRS, targetSRS);
    }

    /**
     *  Transform source to geographic ETRS-89 to calculate gk zone.
     */
    private int getGKnFINZone(Point pt, String sourceEPSG) {
        Point geographicPt = reproject(pt, sourceEPSG, EPSG_ETRS89_GEOGRAPHIC);
        return getGKnFINZone(geographicPt.getLon());
    }

    private int getGKnFINZone(double etrs89Lon) {
        int degrees = (int)etrs89Lon;
        double minutes = (etrs89Lon - (double)degrees) * 60.0;
        int zone = degrees;
        if (minutes >= GK_ZONE_OFF_MINUTUES) {
            zone++;
        }
        return zone;
    }
    private String getEPSGForGKnFIN(int gkZone) {
        if (gkZone < GKFIN_ZONE_START || gkZone > GKFIN_ZONE_END) {
            return null;
        }
        return PREFIX_EPSG + (EPSG_CODE_GK19FIN + gkZone - GKFIN_ZONE_START);
    }

    private String getEpsgCode(String srsCode) {
        if (srsCode == null) {
            return null;
        }
        if (srsCode.startsWith(PREFIX_EPSG)) {
            return srsCode;
        }
        return EPSG_CODE_MAP.get(srsCode);
    }

    private boolean isSupported (String sourceSRS, String targetCRS) {
        if (sourceSRS == null || targetCRS == null ||
                !(sourceSRS.startsWith(PREFIX_EPSG) && targetCRS.startsWith(PREFIX_EPSG))) {
            return false;
        }
        return !(UNSUPPORTED.contains(sourceSRS) || UNSUPPORTED.contains(targetCRS));
    }

    private Coordinate getCoordinate(Point pt, boolean isNorthAxisFirst) {
        if(isNorthAxisFirst) {
            return new Coordinate(pt.getLat(), pt.getLon());
        }
        return new Coordinate(pt.getLon(), pt.getLat());
    }

    private Point getPoint(Coordinate coord, boolean isNorthAxisFirst) {
        if(isNorthAxisFirst) {
            return new Point(coord.y, coord.x);
        }
        return new Point(coord.x, coord.y);
    }

    private CoordTransQueryBuilder getQueryBuilder(String sourceCrs, String targetCrs) {
        if (endPoint == null) {
            endPoint = PropertyUtil.getNecessary(PROP_END_POINT);
        }
        return new CoordTransQueryBuilder(endPoint, sourceCrs, targetCrs);
    }

    private CoordTransWorker getWorker() {
        if (worker == null) {
            worker = OskariComponentManager.getComponentOfType(CoordTransWorker.class);
        }
        return worker;
    }

    private boolean isNorthAxisFirst(String epsgCode) {
        NLSFIProjections srs = NLSFIProjections.forCode(epsgCode);
        if (!srs.equals(NLSFIProjections.UNSUPPORTED)) {
            return srs.northFirst;
        }
        Boolean northFirst = AXIS_ORDER_MAP.get(epsgCode);
        if (northFirst == null) {
            return true;
        }
        return northFirst;
    }

}
