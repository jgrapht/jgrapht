/*
 * (C) Copyright 2017-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.connectivity;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Joris Kinable
 */
public class BlockCutpointGraphTest
{

    @Test
    public void randomGraphTest()
    {
        GnpRandomGraphGenerator<Integer, DefaultEdge> gen =
            new GnpRandomGraphGenerator<>(50, .5, 0);
        for (int i = 0; i < 5; i++) {
            Graph<Integer, DefaultEdge> g = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            gen.generateGraph(g);
            this.validateGraph(g, new BlockCutpointGraph<>(g));
        }
    }

    @Test
    public void randomDirectedGraphTest()
    {
        GnpRandomGraphGenerator<Integer, DefaultEdge> gen =
            new GnpRandomGraphGenerator<>(50, .5, 0);
        for (int i = 0; i < 5; i++) {
            Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
            gen.generateGraph(g);
            this.validateGraph(g, new BlockCutpointGraph<>(g));
        }
    }

    private <V, E> void validateGraph(Graph<V, E> graph, BlockCutpointGraph<V, E> bcGraph)
    {
        assertTrue(GraphTests.isBipartite(bcGraph));
        assertTrue(GraphTests.isForest(bcGraph));

        assertEquals(
            new ConnectivityInspector<>(graph).connectedSets().size(),
            new ConnectivityInspector<>(bcGraph).connectedSets().size());

        BiconnectivityInspector<V, E> inspector = new BiconnectivityInspector<>(graph);
        Set<Graph<V, E>> blocks = inspector.getBlocks();
        Set<V> cutpoints = inspector.getCutpoints();

        assertEquals(blocks.size() + cutpoints.size(), bcGraph.vertexSet().size());

        // assert that every cutpoint is contained in the block it is attached to
        for (V cutpoint : cutpoints) {
            Graph<V, E> cpblock = bcGraph.getBlock(cutpoint);
            assertEquals(1, cpblock.vertexSet().size());
            assertTrue(cpblock.containsVertex(cutpoint));

            for (Graph<V, E> block : Graphs.neighborListOf(bcGraph, cpblock))
                assertTrue(block.containsVertex(cutpoint));
        }

        // assert that the edge set is complete, i.e. there are edges between a block and all its
        // cutpoints
        for (Graph<V, E> block : bcGraph.getBlocks()) {
            long nrCutpointInBlock = block.vertexSet().stream().filter(cutpoints::contains).count();
            assertEquals(nrCutpointInBlock, bcGraph.degreeOf(block));
        }

    }
}
