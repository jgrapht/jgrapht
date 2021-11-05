/*
 * (C) Copyright 2020-2021, by Timofey Chudakov and Contributors.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link TreeDynamicConnectivity}
 *
 * @author Timofey Chudakov
 */
public class TreeDynamicConnectivityTest
{
    private static final Random RANDOM = new Random(17L);

    @Test
    public void testTreeDynamicConnectivity_addNode_removeNode()
    {
        for (int treeSize = 1; treeSize < 50; treeSize++) {
            TreeDynamicConnectivity<Integer> connectivity = new TreeDynamicConnectivity<>();
            for (int node = 0; node < treeSize; node++) {
                assertFalse(connectivity.contains(node));
                assertTrue(connectivity.add(node));
                assertTrue(connectivity.contains(node));
                assertFalse(connectivity.add(node));
                assertTrue(connectivity.remove(node));
                assertFalse(connectivity.contains(node));
                assertFalse(connectivity.remove(node));
            }
        }
    }

    @Test
    public void testTreeDynamicConnectivity_2Trees()
    {
        for (int firstTreeSize = 1; firstTreeSize < 50; firstTreeSize++) {
            for (int secondTreeSize = 1; secondTreeSize < 50; secondTreeSize++) {
                // System.out.printf("First size = %d, second size = %d\n", firstTreeSize,
                // secondTreeSize);

                Graph<Integer, DefaultEdge> firstTree = generateTree(firstTreeSize);
                Graph<Integer, DefaultEdge> secondTree =
                    generateTree(secondTreeSize, firstTreeSize);

                TreeDynamicConnectivity<Integer> connectivity = new TreeDynamicConnectivity<>();

                connectTree(firstTree, connectivity);
                connectTree(secondTree, connectivity);

                for (int v1 : firstTree.vertexSet()) {
                    for (int v2 : secondTree.vertexSet()) {
                        assertFalse(connectivity.connected(v1, v2));

                        assertTrue(connectivity.link(v1, v2));
                        assertTrue(connectivity.connected(v1, v2));

                        assertFalse(connectivity.link(v1, v2));
                        assertTrue(connectivity.connected(v1, v2));

                        assertTrue(connectivity.cut(v1, v2));
                        assertFalse(connectivity.connected(v1, v2));

                        assertFalse(connectivity.cut(v1, v2));
                        assertFalse(connectivity.connected(v1, v2));
                    }
                }

                destroyTree(firstTree, connectivity);
                destroyTree(secondTree, connectivity);
            }
        }
    }

    private void destroyTree(
        Graph<Integer, DefaultEdge> graph, TreeDynamicConnectivity<Integer> connectivity)
    {
        for (int v : graph.vertexSet()) {
            assertTrue(connectivity.contains(v));
            assertTrue(connectivity.remove(v));
            assertFalse(connectivity.contains(v));
        }
    }

    private void connectTree(
        Graph<Integer, DefaultEdge> graph, TreeDynamicConnectivity<Integer> connectivity)
    {
        for (Integer v : graph.vertexSet()) {
            assertFalse(connectivity.contains(v));
            assertTrue(connectivity.add(v));
            assertTrue(connectivity.contains(v));
        }
        for (DefaultEdge e : graph.edgeSet()) {
            int source = graph.getEdgeSource(e), target = graph.getEdgeTarget(e);
            assertFalse(connectivity.connected(source, target));
            assertTrue(connectivity.link(source, target));
            assertTrue(connectivity.connected(source, target));
        }
    }

    private Graph<Integer, DefaultEdge> generateTree(int nodeNum)
    {
        return generateTree(nodeNum, 0);
    }

    private Graph<Integer, DefaultEdge> generateTree(int nodeNum, int start)
    {
        Graph<Integer,
            DefaultEdge> tree = new DefaultUndirectedGraph<>(
                SupplierUtil.createIntegerSupplier(start), SupplierUtil.createDefaultEdgeSupplier(),
                false);

        BarabasiAlbertForestGenerator<Integer, DefaultEdge> gen =
            new BarabasiAlbertForestGenerator<>(1, nodeNum, RANDOM);
        gen.generateGraph(tree);
        return tree;
    }

}
