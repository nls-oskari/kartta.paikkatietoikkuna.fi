package fi.nls.paikkatietoikkuna.coordtransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequence;

public class CoordTransQueryBuilder {

    private static final int LENGTH_LIMIT = 7500;
    private static final char SEP_COORD = ';';
    private static final char SEP_COORD_PART = ',';

    private final String endPoint;
    private final String sourceCrs;
    private final String targetCrs;
    private final StringBuilder sb;
    boolean firstCoordinate;

    public CoordTransQueryBuilder(String endPoint, String sourceCrs, String targetCrs) {
        this.endPoint = endPoint;
        this.sourceCrs = sourceCrs;
        this.targetCrs = targetCrs;
        this.sb = new StringBuilder();
        reset();
    }

    public void reset() {
        sb.setLength(0);
        sb.append(endPoint);
        sb.append("?sourceCRS=").append(sourceCrs);
        sb.append("&targetCRS=").append(targetCrs);
        sb.append("&coords=");
        firstCoordinate = true;
    }

    public boolean add(Coordinate c) {
        int len = sb.length();
        if (!firstCoordinate) {
            sb.append(SEP_COORD);
        }
        sb.append(c.x).append(SEP_COORD_PART).append(c.y);
        if (!Double.isNaN(c.getOrdinate(Coordinate.Z))) {
            sb.append(SEP_COORD_PART).append(c.z);
        }
        firstCoordinate = false;
        if (sb.length() >= LENGTH_LIMIT) {
            sb.setLength(len);
            return false;
        }
        return true;
    }

    public String build() {
        return sb.toString();
    }

}
