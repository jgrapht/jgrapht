/*
 * (C) Copyright 2020-2020, by Timofey Chudakov and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.generate.BarabasiAlbertForestGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;

public class TreeDynamicConnectivityTest {
    private static final Random rng = new Random(17L);
    private static int count = 0;

    @Test
    public void testTreeDynamicConnectivity_2Trees() {
        for (int firstTreeSize = 1; firstTreeSize < 50; firstTreeSize++) {
            for (int secondTreeSize = 1; secondTreeSize < 50; secondTreeSize++) {
                System.out.printf("First size = %d, second size = %d\n", firstTreeSize, secondTreeSize);

                Graph<Integer, DefaultEdge> firstTree = generateTree(firstTreeSize, 0);
                Graph<Integer, DefaultEdge> secondTree = generateTree(secondTreeSize, firstTreeSize);

                TreeDynamicConnectivity<Integer> connectivity = new TreeDynamicConnectivity<>();

                connectTree(firstTree, connectivity);
                connectTree(secondTree, connectivity);

                for (int v1 : firstTree.vertexSet()) {
                    for (int v2 : secondTree.vertexSet()) {
//                        System.out.printf("V1 = %d, v2 = %d\n", v1, v2);
                        assertFalse(connectivity.connected(v1, v2));
                        connectivity.link(v1, v2);
                        connectivity.diagnostic();
                        assertTrue(connectivity.connected(v1, v2));
                        connectivity.cut(v1, v2);
                        connectivity.diagnostic();
                        assertFalse(connectivity.connected(v1, v2));
                    }
                }
            }
        }
    }

    private void action(){
        count += 1;
//        System.out.println("Count = " +count);
    }

    private void connectTree(Graph<Integer, DefaultEdge> graph, TreeDynamicConnectivity<Integer> connectivity) {
//        System.out.println(graph);
        for (Integer v : graph.vertexSet()) {
            connectivity.add(v);
            connectivity.diagnostic();
        }
        for (DefaultEdge e : graph.edgeSet()) {
            int source = graph.getEdgeSource(e), target = graph.getEdgeTarget(e);
            assertFalse(connectivity.connected(source, target));
            action();
            connectivity.link(source, target);
            connectivity.diagnostic();

            assertTrue(connectivity.connected(source, target));
        }
    }

    private Graph<Integer, DefaultEdge> generateTree(int nodeNum, int start) {
        Graph<Integer, DefaultEdge> tree =
                new DefaultUndirectedGraph<>(SupplierUtil.createIntegerSupplier(start),
                        SupplierUtil.createDefaultEdgeSupplier(),
                        false);

        BarabasiAlbertForestGenerator<Integer, DefaultEdge> gen = new BarabasiAlbertForestGenerator<>(1, nodeNum, rng);
        gen.generateGraph(tree);
        return tree;
    }

}