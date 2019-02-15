package fi.nls.paikkatietoikkuna.coordtransform;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.NLSFIProjections;
import fi.nls.oskari.domain.geo.Point;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.NLSFIPointTransformer;

import java.util.Arrays;
import java.util.List;

public class PointTransformerImpl extends NLSFIPointTransformer {

    protected static final Logger log = LogFactory.getLogger(PointTransformerImpl.class);
    private CoordTransWorker worker;
    private String endPoint;

    @Override
    public Point reproject(Point point, String sourceCrs, String targetCrs) {
        if (point == null || sourceCrs == null || targetCrs == null) {
            return null;
        }
        Point pt = null;
        try {
            CoordTransQueryBuilder queryBuilder = getQueryBuilder(sourceCrs, targetCrs);
            List<Coordinate> coordList = Arrays.asList(new Coordinate(point.getLon(), point.getLat()));
            worker.transform(queryBuilder, coordList);
            Coordinate coord = coordList.get(0);
            return new Point(coord.x, coord.y);
        } catch (Exception e) {
            log.warn("Reprojecting point", point, "from", sourceCrs, "to", targetCrs, "failed", e);
        }
        if (pt == null) {
            pt = super.reproject(point, sourceCrs, targetCrs);
        }
        return pt;
    }

    private CoordTransQueryBuilder getQueryBuilder(String sourceCrs, String targetCrs) {
        if (worker == null) {
            worker = OskariComponentManager.getComponentOfType(CoordTransWorker.class);
        }
        if (endPoint == null) {
            endPoint = PropertyUtil.getNecessary(CoordinateTransformationActionHandler.PROP_END_POINT);
        }
        return new CoordTransQueryBuilder(endPoint, sourceCrs, targetCrs);
    }

}
