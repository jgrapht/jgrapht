/*
 * (C) Copyright 2021-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.clustering;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.TestUtil;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Tests for {@link GirvanNewmanClustering}.
 *
 * @author Dimitrios Michail
 */
public class GirvanNewmanClusteringTest
{

    @Test
    public void testUndirectedGraph()
    {
        Graph<Integer,
            DefaultEdge> g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(false).allowingSelfLoops(true).weighted(false)
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER)
                .vertexSupplier(SupplierUtil.createIntegerSupplier()).buildGraph();

        g = TestUtil
            .createUndirected(
                new int[][] { { 1, 2 }, { 1, 3 }, { 2, 3 }, { 3, 7 }, { 4, 6 }, { 4, 5 }, { 5, 6 },
                    { 6, 7 }, { 7, 8 }, { 8, 9 }, { 8, 12 }, { 9, 10 }, { 9, 11 }, { 12, 13 },
                    { 12, 14 }, { 10, 11 }, { 13, 14 } });

        Clustering<Integer> c1 = new GirvanNewmanClustering<>(g, 2).getClustering();
        assertEquals(c1.getNumberClusters(), 2);
        assertEquals(Set.of(1, 2, 3, 4, 5, 6, 7), c1.getClusters().get(0));
        assertEquals(Set.of(8, 9, 10, 11, 12, 13, 14), c1.getClusters().get(1));

        Clustering<Integer> c2 = new GirvanNewmanClustering<>(g, 6).getClustering();
        assertEquals(c2.getNumberClusters(), 6);
        assertEquals(Set.of(1, 2, 3), c2.getClusters().get(0));
        assertEquals(Set.of(7), c2.getClusters().get(1));
        assertEquals(Set.of(4, 5, 6), c2.getClusters().get(2));
        assertEquals(Set.of(8), c2.getClusters().get(3));
        assertEquals(Set.of(9, 10, 11), c2.getClusters().get(4));
        assertEquals(Set.of(12, 13, 14), c2.getClusters().get(5));

        Clustering<Integer> c3 =
            new GirvanNewmanClustering<>(g, g.vertexSet().size()).getClustering();
        assertEquals(c3.getNumberClusters(), g.vertexSet().size());
    }

}
