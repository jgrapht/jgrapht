/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tests for {@link VertexToIntegerMapping}
 *
 * @author Alexandru Valeanu
 */
public class VertexToIntegerMappingTest {

    @Test(expected = NullPointerException.class)
    public void testNullSet(){
        VertexToIntegerMapping<Integer> mapping = new VertexToIntegerMapping<>((Set<Integer>) null);
    }

    @Test
    public void testEmptySet(){
        VertexToIntegerMapping<Integer> mapping = new VertexToIntegerMapping<>(new HashSet<>());

        Assert.assertTrue(mapping.getIndexList().isEmpty());
        Assert.assertTrue(mapping.getVertexMap().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotUniqueElements(){
        VertexToIntegerMapping<Integer> mapping = new VertexToIntegerMapping<>(Arrays.asList(1, 2, 1));
    }

    @Test
    public void testRandomInstances(){
        Random random = new Random(0x88);
        final int NUM_TESTS = 1024;
        Supplier<String> supplier =
                SupplierUtil.createStringSupplier(random.nextInt(100));

        for (int test = 0; test < NUM_TESTS; test++) {
            final int N = 10 + random.nextInt(1024);

            Set<String> vertices = IntStream.range(0, N).mapToObj(x -> supplier.get()).collect(Collectors.toSet());
            VertexToIntegerMapping<String> mapping = new VertexToIntegerMapping<>(vertices);

            Map<String, Integer> vertexMap = mapping.getVertexMap();
            List<String> indexList = mapping.getIndexList();

            Assert.assertEquals(N, vertexMap.size());
            Assert.assertEquals(N, indexList.size());

            for (int i = 0; i < indexList.size(); i++) {
                Assert.assertEquals(i, vertexMap.get(indexList.get(i)).intValue());
            }

            for (Map.Entry<String, Integer> entry: vertexMap.entrySet()){
                Assert.assertEquals(indexList.get(entry.getValue()), entry.getKey());
            }
        }
    }
}