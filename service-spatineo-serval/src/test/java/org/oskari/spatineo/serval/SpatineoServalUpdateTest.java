package org.oskari.spatineo.serval;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SpatineoServalUpdateTest {

    @Test
    public void partitionNullListReturnsEmptyList() {
        List<List<Object>> list = SpatineoServalUpdateJob.partition(null, 0);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void partitioningWithNegativePartitionSizeReturnsEmptyList() {
        List<String> list = Arrays.asList("foo");
        List<List<String>> parts = SpatineoServalUpdateJob.partition(list, -1);
        assertNotNull(parts);
        assertTrue(parts.isEmpty());
    }

    @Test
    public void partitioningWithPartitionSizeZeroReturnsEmptyList() {
        List<String> list = Arrays.asList("foo");
        List<List<String>> parts = SpatineoServalUpdateJob.partition(list, 0);
        assertNotNull(parts);
        assertTrue(parts.isEmpty());
    }

    @Test
    public void partitioningWithExactlyListSizeReturnsListWithOneList() {
        List<String> list = Arrays.asList("foo", "bar");
        List<List<String>> parts = SpatineoServalUpdateJob.partition(list, 2);
        assertNotNull(parts);
        assertEquals(1, parts.size());
    }

    @Test
    public void partitioningWithExactlyThreeTimesTheListSizeReturnsListWithThreeLists() {
        List<String> list = Arrays.asList("foo", "bar", "baz", "qux", "yyy", "eee");
        List<List<String>> parts = SpatineoServalUpdateJob.partition(list, 2);
        assertNotNull(parts);
        assertEquals(3, parts.size());
    }

    @Test
    public void partitioningWithLargerPartitionSizeThanTotalEntriesReturnsOneList() {
        List<String> list = Arrays.asList("foo", "bar");
        List<List<String>> parts = SpatineoServalUpdateJob.partition(list, 4);
        assertNotNull(parts);
        assertEquals(1, parts.size());
        assertEquals(list.size(), parts.get(0).size());
    }

    @Test
    public void partitioningRegularCase() {
        List<String> list = Arrays.asList("foo", "bar", "baz", "qux");
        List<List<String>> parts = SpatineoServalUpdateJob.partition(list, 3);
        assertNotNull(parts);
        assertEquals(2, parts.size());
        assertEquals(3, parts.get(0).size());
        assertEquals(1, parts.get(1).size());
    }

}
