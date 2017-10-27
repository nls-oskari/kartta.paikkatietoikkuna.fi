package fi.nls.paikkatietoikkuna.coordtransform;

import java.nio.charset.StandardCharsets;

/**
 * Utility functions for using the remote CoordTrans service
 */
public class CoordTransService {

    private static final byte SEP_COORDINATE_PART = ',';
    private static final byte SEP_COORDINATE = ';';
    private static final char SEP_COORDINATE_PART_C = ',';
    private static final char SEP_COORDINATE_C = ';';

    public static String createQuery(String sourceCrs, String targetCrs,
            final int dimension, final double[] coords) {
        StringBuilder sb = new StringBuilder();
        sb.append("?sourceCRS=").append(sourceCrs);
        sb.append("&targetCRS=").append(targetCrs);
        sb.append("&coords=");
        for (int i = 0; i < coords.length;) {
            if (i > 0) {
                sb.append(SEP_COORDINATE_C);
            }
            sb.append(coords[i++]).append(',').append(coords[i++]);
            if (dimension == 3) {
                sb.append(SEP_COORDINATE_PART_C).append(coords[i++]);
            }
        }
        return sb.toString();
    }

    public static void parseResponse(final byte[] resp, final double[] coords, final int dimension) {
        if (resp[0] == 'V') {
            // "Virhe: " - send only the part after prefix
            throw new IllegalArgumentException(new String(resp, 7, resp.length - 7, StandardCharsets.UTF_8));
        }

        if (dimension == 3) {
            parseResponse3D(resp, coords);
        } else {
            parseResponse2D(resp, coords);
        }
    }

    protected static void parseResponse3D(final byte[] resp, final double[] coords)
            throws IllegalArgumentException {
        final int len = resp.length;

        int j = 0;
        int k;
        for (int i = 0; i < coords.length - 3;) {
            k = indexOf(resp, SEP_COORDINATE_PART, j, len);
            if (k < 0) {
                throw new IllegalArgumentException("Invalid response from service");
            }
            coords[i++] = parseAsciiDouble(resp, j, k);
            j = k + 1;
            k = indexOf(resp, SEP_COORDINATE_PART, j, len);
            if (k < 0) {
                throw new IllegalArgumentException("Invalid response from service");
            }
            coords[i++] = parseAsciiDouble(resp, j, k);
            j = k + 1;
            k = indexOf(resp, SEP_COORDINATE, j, len);
            if (k < 0) {
                throw new IllegalArgumentException("Invalid response from service");
            }
            coords[i++] = parseAsciiDouble(resp, j, k);
            j = k + 1;
        }
        // Last coordinate
        k = indexOf(resp, SEP_COORDINATE_PART, j, len);
        if (k < 0) {
            throw new IllegalArgumentException("Invalid response from service");
        }
        coords[coords.length - 3] = parseAsciiDouble(resp, j, k);
        j = k + 1;
        k = indexOf(resp, SEP_COORDINATE_PART, j, len);
        if (k < 0) {
            throw new IllegalArgumentException("Invalid response from service");
        }
        coords[coords.length - 2] = parseAsciiDouble(resp, j, k);
        coords[coords.length - 1] = parseAsciiDouble(resp, k + 1, len);
    }

    protected static void parseResponse2D(final byte[] resp, final double[] coords)
            throws IllegalArgumentException {
        final int len = resp.length;

        int j = 0;
        int k;
        for (int i = 0; i < coords.length - 2;) {
            k = indexOf(resp, SEP_COORDINATE_PART, j, len);
            if (k < 0) {
                throw new IllegalArgumentException("Invalid response from service");
            }
            coords[i++] = parseAsciiDouble(resp, j, k);
            j = k + 1;
            k = indexOf(resp, SEP_COORDINATE, j, len);
            if (k < 0) {
                throw new IllegalArgumentException("Invalid response from service");
            }
            coords[i++] = parseAsciiDouble(resp, j, k);
            j = k + 1;
        }
        // Last coordinate
        k = indexOf(resp, SEP_COORDINATE_PART, j, len);
        if (k < 0) {
            throw new IllegalArgumentException("Invalid response from service");
        }
        coords[coords.length - 2] = parseAsciiDouble(resp, j, k);
        coords[coords.length - 1] = parseAsciiDouble(resp, k + 1, len);
    }

    private static int indexOf(byte[] arr, byte b, int i, int j) {
        for (; i < j; i++) {
            if (arr[i] == b) {
                return i;
            }
        }
        return -1;
    }

    protected static double parseAsciiDouble(byte[] resp, int start, int end) {
        return Double.parseDouble(new String(resp, start, end - start, StandardCharsets.US_ASCII));
    }
}
