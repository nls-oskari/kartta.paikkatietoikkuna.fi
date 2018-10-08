package fi.nls.paikkatietoikkuna.coordtransform;

import com.vividsolutions.jts.geom.Coordinate;
import fi.nls.oskari.control.ActionException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility functions for using the remote CoordTrans service
 */
public class CoordTransService {

    private static final String SEP_COORD = ";";
    private static final String SEP_COORD_PART = ",";
    private static final String GRADIAN = "gradian";
    private static final String RADIAN = "radian";
    private static final MathContext DECIMAL_PRECISION = MathContext.DECIMAL128; //DECIMAL64 -> 16 decimals, DECIMAL128 -> 34 decimals
    private static final BigDecimal HOUR_TO_MIN = new BigDecimal(60);
    private static final BigDecimal HOUR_TO_SEC = new BigDecimal(3600);
    private static final BigDecimal PI2 = new BigDecimal("6.283185307179586476925286766559"); //2*pi
    private static final BigDecimal DEC_TO_GRAD = BigDecimal.TEN.divide(new BigDecimal(9), DECIMAL_PRECISION);
    private static final BigDecimal DEC_TO_RAD = PI2.divide(new BigDecimal(360), DECIMAL_PRECISION);

    public static void parseResponse(byte[] resp, List<Coordinate> coords, final int dimension) {
        if (resp[0] == 'V') {
            // "Virhe: " - send only the part after prefix
            throw new IllegalArgumentException(new String(resp, 7, resp.length - 7, StandardCharsets.UTF_8));
        }

        String response = new String(resp, StandardCharsets.US_ASCII);
        String[] coordinates = response.split(SEP_COORD);
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate coord = coords.get(i);
            String[] coordinateParts = coordinates[i].split(SEP_COORD_PART);
            coord.x = Double.parseDouble(coordinateParts[0]);
            coord.y = Double.parseDouble(coordinateParts[1]);
            if (dimension == 3) {
                // FIXME: just check if coordinateParts.length > 3 instead of sending dimension?
                coord.z = Double.parseDouble(coordinateParts[2]);
            }
        }
    }

    public static double transformUnitToDegree(String coord, String unit) throws ActionException {
        coord = coord.trim();
        BigDecimal value;
        if (GRADIAN.equals(unit)) {
            value = new BigDecimal(coord);
            value = value.divide(DEC_TO_GRAD, DECIMAL_PRECISION);
            return value.doubleValue();
        } else if (RADIAN.equals(unit)) {
            value = new BigDecimal(coord);
            value = value.divide(DEC_TO_RAD, DECIMAL_PRECISION);
            return value.doubleValue();
        }

        BigDecimal dd = new BigDecimal(coord.substring(0, 2));
        BigDecimal mm;
        BigDecimal ss;
        switch (unit) {
            case "DDMM":
                mm = new BigDecimal(coord.substring(2));
                dd = dd.add(mm.divide(HOUR_TO_MIN, DECIMAL_PRECISION));
                return dd.doubleValue();
            case "DD MM":
                mm = new BigDecimal(coord.substring(3));
                dd = dd.add(mm.divide(HOUR_TO_MIN, DECIMAL_PRECISION));
                return dd.doubleValue();
            case "DDMMSS":
                mm = new BigDecimal(coord.substring(2, 4));
                ss = new BigDecimal(coord.substring(4));
                dd = dd.add(mm.divide(HOUR_TO_MIN, DECIMAL_PRECISION));
                dd = dd.add(ss.divide(HOUR_TO_SEC, DECIMAL_PRECISION));
                return dd.doubleValue();
            case "DD MM SS":
                mm = new BigDecimal(coord.substring(3, 5));
                ss = new BigDecimal(coord.substring(6));
                dd = dd.add(mm.divide(HOUR_TO_MIN, DECIMAL_PRECISION));
                dd = dd.add(ss.divide(HOUR_TO_SEC, DECIMAL_PRECISION));
                return dd.doubleValue();
            default:
                throw new ActionException("Invalid unit");
        }
    }

    public static String transformDegreeToUnit(double coord, String unit, int decimals) throws ActionException {
        BigDecimal value = new BigDecimal(coord, DECIMAL_PRECISION);
        BigDecimal fractPart;
        String separator = "";
        String result;
        switch (unit) {
            case RADIAN:
                value = value.multiply(DEC_TO_RAD, DECIMAL_PRECISION).setScale(decimals, RoundingMode.HALF_UP);
                return value.toPlainString();
            case GRADIAN:
                value = value.multiply(DEC_TO_GRAD, DECIMAL_PRECISION).setScale(decimals, RoundingMode.HALF_UP);
                return value.toPlainString();
            case "DD":
                return getFormatedValue(value, decimals); //DD.dd
            case "DD MM":
                separator = " ";
            case "DDMM":
                result = getPrefixedIntPart(value); //DD
                fractPart = value.remainder(BigDecimal.ONE);
                value = fractPart.multiply(HOUR_TO_MIN, DECIMAL_PRECISION);
                return result + separator + getFormatedValue(value, decimals);// add MM.mm
            case "DD MM SS":
                separator = " ";
            case "DDMMSS":
                result = getPrefixedIntPart(value); //DD
                fractPart = value.remainder(BigDecimal.ONE);
                value = fractPart.multiply(HOUR_TO_MIN, DECIMAL_PRECISION);
                result += separator + getPrefixedIntPart(value); //add MM
                fractPart = value.remainder(BigDecimal.ONE);
                value = fractPart.multiply(HOUR_TO_MIN, DECIMAL_PRECISION);
                return result + separator + getFormatedValue(value, decimals); //add SS.ss
            default:
                throw new ActionException("Invalid unit");
        }
    }

    public static String round(double value, int decimals) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.toPlainString();
    }

    //Min and sec values cannot be negative so we can use this for formating them also
    private static String getFormatedValue(BigDecimal value, int decimals) {
        value = value.setScale(decimals, RoundingMode.HALF_UP);
        // 10 > value < -10
        if (value.compareTo(BigDecimal.TEN) >= 0 || value.compareTo(BigDecimal.TEN.negate()) <= 0) {
            return value.toPlainString();
            //value -9 - 0
        } else if (value.compareTo(BigDecimal.ZERO) == -1) {
            return "-0" + value.negate().toPlainString();
        }
        //value 0 - 9
        return "0" + value.toPlainString();
    }

    private static String getPrefixedIntPart(BigDecimal value) {
        int intPart = value.intValue();
        if (intPart >= 10 || intPart <= -10) {
            return Integer.toString(intPart);
        } else if (intPart < 0) {
            return "-0" + intPart * -1;
        }
        return "0" + intPart;
    }
}
