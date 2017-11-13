package fi.nls.paikkatietoikkuna.coordtransform;

import java.util.Arrays;

/**
 * double array that grows in size when necessary
 * - not thread-safe
 * - add only
 */
public class DoubleArray {

    private double[] arr;
    private int size;

    public DoubleArray(int initialSize) {
        this.arr = new double[initialSize];
    }

    public void add(double d) {
        if (size == arr.length) {
            grow();
        }
        arr[size++] = d;
    }

    private void grow() {
        int len = arr.length;
        double[] tmp = new double[len * 2];
        System.arraycopy(arr, 0, tmp, 0, len);
        arr = tmp;
    }

    public double[] toArray() {
        return Arrays.copyOf(arr, size);
    }

}
