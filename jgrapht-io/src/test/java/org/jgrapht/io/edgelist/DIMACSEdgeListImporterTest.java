/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.io.edgelist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.io.ImportException;
import org.junit.Test;

/**
 * Tests
 * 
 * @author Dimitrios Michail
 */
public class DIMACSEdgeListImporterTest
{

    /**
     * Read and parse an actual instance
     */
    @Test
    public void testReadDIMACSInstance()
        throws ImportException
    {
        InputStream fstream = getClass().getClassLoader().getResourceAsStream("myciel3.col");

        DIMACSEdgeListImporter importer = new DIMACSEdgeListImporter();

        importer.addNodeCountConsumer(count -> {
            assertEquals(count, Integer.valueOf(11));
        });
        List<Pair<Integer, Integer>> collected = new ArrayList<>();
        importer.addEdgeConsumer((e, w) -> {
            assertNull(w);
            collected.add(e);
        });
        importer.importEdgeList(fstream);

        int[][] edges = { { 0, 1 }, { 0, 2 }, { 0, 3 }, { 0, 4 }, { 1, 5 }, { 1, 6 }, { 1, 7 },
            { 5, 8 }, { 5, 3 }, { 5, 9 }, { 2, 8 }, { 2, 6 }, { 2, 9 }, { 8, 7 }, { 8, 4 },
            { 6, 10 }, { 3, 10 }, { 7, 10 }, { 4, 10 }, { 9, 10 } };

        assertEquals(collected.size(), edges.length);

        for (int i = 0; i < edges.length; i++) {
            Pair<Integer, Integer> e = collected.get(i);
            assertEquals(Integer.valueOf(edges[i][0]), e.getFirst());
            assertEquals(Integer.valueOf(edges[i][1]), e.getSecond());
        }
    }

    /**
     * Read and parse an weighted instance
     */
    @Test
    public void testReadWeightedDIMACSInstance()
        throws ImportException
    {
        InputStream fstream =
            getClass().getClassLoader().getResourceAsStream("myciel3_weighted.col");

        Map<Integer, Integer> nameMap = new HashMap<>();
        nameMap.put(1, 0);
        nameMap.put(2, 1);
        nameMap.put(4, 2);
        nameMap.put(7, 3);
        nameMap.put(9, 4);
        nameMap.put(3, 5);
        nameMap.put(6, 6);
        nameMap.put(8, 7);
        nameMap.put(5, 8);
        nameMap.put(10, 9);
        nameMap.put(11, 10);

        DIMACSEdgeListImporter importer = new DIMACSEdgeListImporter();

        importer.addNodeCountConsumer(count -> {
            assertEquals(count, Integer.valueOf(11));
        });
        List<Triple<Integer, Integer, Double>> collected = new ArrayList<>();
        importer.addEdgeConsumer((e, w) -> {
            collected.add(Triple.of(e.getFirst(), e.getSecond(), w));
        });
        importer.importEdgeList(fstream);

        int[][] edges = { { 1, 2, 1 }, { 1, 4, 2 }, { 1, 7, 3 }, { 1, 9, 4 }, { 2, 3, 5 },
            { 2, 6, 6 }, { 2, 8, 7 }, { 3, 5, 8 }, { 3, 7, 9 }, { 3, 10, 10 }, { 4, 5, 11 },
            { 4, 6, 12 }, { 4, 10, 13 }, { 5, 8, 14 }, { 5, 9, 15 }, { 6, 11, 16 }, { 7, 11, 17 },
            { 8, 11, 18 }, { 9, 11, 19 }, { 10, 11, 20 } };

        int i = 0;
        for (int[] edge : edges) {
            Triple<Integer, Integer, Double> e = collected.get(i);
            assertEquals(nameMap.get(edge[0]), e.getFirst());
            assertEquals(nameMap.get(edge[1]), e.getSecond());
            assertEquals(Double.valueOf(edge[2]), e.getThird());
            i++;
        }
    }

    /**
     * Read and parse an weighted instance
     */
    @Test
    public void testReadWeightedWithourRenumberingDIMACSInstance()
        throws ImportException
    {
        InputStream fstream =
            getClass().getClassLoader().getResourceAsStream("myciel3_weighted.col");

        Map<Integer, Integer> nameMap = new HashMap<>();
        nameMap.put(1, 0);
        nameMap.put(2, 1);
        nameMap.put(3, 2);
        nameMap.put(4, 3);
        nameMap.put(5, 4);
        nameMap.put(6, 5);
        nameMap.put(7, 6);
        nameMap.put(8, 7);
        nameMap.put(9, 8);
        nameMap.put(10, 9);
        nameMap.put(11, 10);

        DIMACSEdgeListImporter importer = new DIMACSEdgeListImporter();
        importer = importer.renumberVertices(false);

        importer.addNodeCountConsumer(count -> {
            assertEquals(count, Integer.valueOf(11));
        });
        List<Triple<Integer, Integer, Double>> collected = new ArrayList<>();
        importer.addEdgeConsumer((e, w) -> {
            collected.add(Triple.of(e.getFirst(), e.getSecond(), w));
        });
        importer.importEdgeList(fstream);

        int[][] edges = { { 1, 2, 1 }, { 1, 4, 2 }, { 1, 7, 3 }, { 1, 9, 4 }, { 2, 3, 5 },
            { 2, 6, 6 }, { 2, 8, 7 }, { 3, 5, 8 }, { 3, 7, 9 }, { 3, 10, 10 }, { 4, 5, 11 },
            { 4, 6, 12 }, { 4, 10, 13 }, { 5, 8, 14 }, { 5, 9, 15 }, { 6, 11, 16 }, { 7, 11, 17 },
            { 8, 11, 18 }, { 9, 11, 19 }, { 10, 11, 20 } };

        int i = 0;
        for (int[] edge : edges) {
            Triple<Integer, Integer, Double> e = collected.get(i);
            assertEquals(nameMap.get(edge[0]), e.getFirst());
            assertEquals(nameMap.get(edge[1]), e.getSecond());
            assertEquals(Double.valueOf(edge[2]), e.getThird());
            i++;
        }
    }

}
