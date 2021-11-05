/*
 * (C) Copyright 2020-2021, by Sebastiano Vigna and Contributors.
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
package org.jgrapht.alg.scoring;

import static org.junit.Assert.assertEquals;

import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.junit.Test;

/**
 * Unit tests for eigenvector centrality
 *
 * @author Sebastiano Vigna
 */
public class EigenvectorCentralityTest
{
    @Test
    public void testGraph2Nodes()
    {
        final DirectedPseudograph<String, DefaultEdge> g =
            new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("1");
        g.addVertex("2");
        g.addEdge("1", "2");
        g.addEdge("2", "1");

        final VertexScoringAlgorithm<String, Double> pr = new EigenvectorCentrality<>(g);

        assertEquals(pr.getVertexScore("1"), 1 / Math.sqrt(2), 0.0001);
        assertEquals(pr.getVertexScore("2"), 1 / Math.sqrt(2), 0.0001);
    }

    @Test
    public void testGraph3Nodes()
    {
        final DirectedPseudograph<String, DefaultEdge> g =
            new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addEdge("1", "2");
        g.addEdge("2", "3");
        g.addEdge("3", "1");

        final VertexScoringAlgorithm<String, Double> pr = new EigenvectorCentrality<>(g);

        assertEquals(pr.getVertexScore("1"), 1 / Math.sqrt(3), 0.0001);
        assertEquals(pr.getVertexScore("2"), 1 / Math.sqrt(3), 0.0001);
        assertEquals(pr.getVertexScore("3"), 1 / Math.sqrt(3), 0.0001);
    }

    @Test
    public void testGraph1()
    {
        final DirectedPseudograph<String, DefaultEdge> g =
            new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("0");
        g.addVertex("1");
        g.addVertex("2");
        g.addVertex("3");
        g.addVertex("4");

        g.addEdge("0", "1");
        g.addEdge("0", "2");
        g.addEdge("1", "3");
        g.addEdge("1", "2");
        g.addEdge("2", "1");
        g.addEdge("2", "4");
        g.addEdge("2", "3");
        g.addEdge("3", "1");
        g.addEdge("3", "2");
        g.addEdge("3", "3");
        g.addEdge("4", "2");
        g.addEdge("4", "0");

        final VertexScoringAlgorithm<String, Double> pr = new EigenvectorCentrality<>(g);

        assertEquals(pr.getVertexScore("0"), 0.08032022089204849, 0.001);
        assertEquals(pr.getVertexScore("1"), 0.48765632797141506, 0.001);
        assertEquals(pr.getVertexScore("2"), 0.5453987490787013, 0.001);
        assertEquals(pr.getVertexScore("3"), 0.6437087676602127, 0.001);
        assertEquals(pr.getVertexScore("4"), 0.20956906939251885, 0.001);
    }

    @Test
    public void testWeightedGraph1()
    {
        final DirectedWeightedPseudograph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");

        g.setEdgeWeight(g.addEdge("a", "b"), 1. / 2);
        g.setEdgeWeight(g.addEdge("a", "c"), 1. / 3);
        g.setEdgeWeight(g.addEdge("b", "a"), 1);
        g.setEdgeWeight(g.addEdge("b", "b"), 2);
        g.setEdgeWeight(g.addEdge("b", "d"), 1. / 4);
        g.setEdgeWeight(g.addEdge("c", "a"), 1);
        g.setEdgeWeight(g.addEdge("c", "d"), 3);
        g.setEdgeWeight(g.addEdge("d", "b"), 1. / 5);
        g.setEdgeWeight(g.addEdge("d", "d"), 1);

        final VertexScoringAlgorithm<String, Double> pr = new EigenvectorCentrality<>(g);

        assertEquals(pr.getVertexScore("a"), 0.400610775759173, 0.0001);
        assertEquals(pr.getVertexScore("b"), 0.863882834704165, 0.0001);
        assertEquals(pr.getVertexScore("c"), 0.0580276877361552, 0.0001);
        assertEquals(pr.getVertexScore("d"), 0.299750298600000, 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistantVertex()
    {
        final DirectedPseudograph<String, DefaultEdge> g =
            new DirectedPseudograph<>(DefaultEdge.class);

        g.addVertex("center");
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");

        g.addEdge("center", "a");
        g.addEdge("center", "b");
        g.addEdge("center", "c");

        final VertexScoringAlgorithm<String, Double> pr = new EigenvectorCentrality<>(g);

        pr.getVertexScore("unknown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadParameters1()
    {
        final DirectedPseudograph<String, DefaultEdge> g =
            new DirectedPseudograph<>(DefaultEdge.class);

        new EigenvectorCentrality<>(g, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadParameters2()
    {
        final DirectedPseudograph<String, DefaultEdge> g =
            new DirectedPseudograph<>(DefaultEdge.class);

        new EigenvectorCentrality<>(g, 1, 0);
    }

}
