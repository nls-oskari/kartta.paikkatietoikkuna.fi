package org.oskari.spatineo.serval;

import java.util.ArrayList;
import java.util.List;

public class ListPartition {

    public static <T> List<List<T>> partition(final List<T> list, final int partitionSize) {
        final List<List<T>> parent = new ArrayList<>();
        if (list == null || partitionSize <= 0) {
            return parent;
        }

        List<T> tmp = null;
        int size = partitionSize;

        for (T t : list) {
            if (size == partitionSize) {
                tmp = new ArrayList<>(partitionSize);
                parent.add(tmp);
                size = 0;
            }
            tmp.add(t);
            size++;
        }
        return parent;
    }

}
