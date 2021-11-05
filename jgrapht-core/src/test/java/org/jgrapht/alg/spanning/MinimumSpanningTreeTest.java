/*
 * (C) Copyright 2010-2021, by Tom Conerly and Contributors.
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
package org.jgrapht.alg.spanning;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class MinimumSpanningTreeTest
{

    abstract SpanningTreeAlgorithm<DefaultWeightedEdge> createSolver(
        Graph<Integer, DefaultWeightedEdge> network);

    // ~ Static fields/initializers ---------------------------------------------

    public static final Integer A = "A".codePointAt(0);
    public static final Integer B = "B".codePointAt(0);
    public static final Integer C = "C".codePointAt(0);
    public static final Integer D = "D".codePointAt(0);
    public static final Integer E = "E".codePointAt(0);
    public static final Integer F = "F".codePointAt(0);
    public static final Integer G = "G".codePointAt(0);
    public static final Integer H = "H".codePointAt(0);

    // ~ Instance fields --------------------------------------------------------

    public static DefaultWeightedEdge ab;
    public static DefaultWeightedEdge ac;
    public static DefaultWeightedEdge bd;
    public static DefaultWeightedEdge de;
    public static DefaultWeightedEdge eg;
    public static DefaultWeightedEdge gh;
    public static DefaultWeightedEdge fh;

    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testSimpleDisconnectedWeightedGraph()
    {
        testMinimumSpanningTreeBuilding(
            createSolver(createSimpleDisconnectedWeightedGraph()).getSpanningTree(),
            Arrays.asList(ab, ac, bd, eg, gh, fh), 60.0);
    }

    @Test
    public void testSimpleConnectedWeightedGraph()
    {
        testMinimumSpanningTreeBuilding(
            createSolver(createSimpleConnectedWeightedGraph()).getSpanningTree(),
            Arrays.asList(ab, ac, bd, de), 15.0);
    }

    @Test
    public void testRandomInstances()
    {
        final Random rng = new Random(33);
        final double edgeProbability = 0.5;
        final int numberVertices = 200;
        final int repeat = 100;

        GraphGenerator<Integer, DefaultWeightedEdge, Integer> gg =
            new GnpRandomGraphGenerator<>(numberVertices, edgeProbability, rng, false);

        for (int i = 0; i < repeat; i++) {
            WeightedPseudograph<Integer, DefaultWeightedEdge> g = new WeightedPseudograph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);
            gg.generateGraph(g);

            for (DefaultWeightedEdge e : g.edgeSet()) {
                g.setEdgeWeight(e, rng.nextDouble());
            }

            SpanningTreeAlgorithm<DefaultWeightedEdge> alg1 = createSolver(g);
            SpanningTreeAlgorithm<DefaultWeightedEdge> alg2;

            if (alg1 instanceof KruskalMinimumSpanningTree)
                alg2 = new PrimMinimumSpanningTree<>(g);
            else
                alg2 = new KruskalMinimumSpanningTree<>(g);

            assertEquals(
                alg1.getSpanningTree().getWeight(), alg2.getSpanningTree().getWeight(), 1e-9);
        }
    }

    public static <V, E> void testMinimumSpanningTreeBuilding(
        final SpanningTree<E> mst, final Collection<E> edgeSet, final double weight)
    {
        assertEquals(weight, mst.getWeight(), 0);
        assertTrue(mst.getEdges().containsAll(edgeSet));
    }

    public static Graph<Integer, DefaultWeightedEdge> createSimpleDisconnectedWeightedGraph()
    {

        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        /*
         * 
         * A -- B E -- F | | | | C -- D G -- H
         * 
         */

        g.addVertex(A);
        g.addVertex(B);
        g.addVertex(C);
        g.addVertex(D);

        ab = Graphs.addEdge(g, A, B, 5);
        ac = Graphs.addEdge(g, A, C, 10);
        bd = Graphs.addEdge(g, B, D, 15);
        Graphs.addEdge(g, C, D, 20);

        g.addVertex(E);
        g.addVertex(F);
        g.addVertex(G);
        g.addVertex(H);

        Graphs.addEdge(g, E, F, 20);
        eg = Graphs.addEdge(g, E, G, 15);
        gh = Graphs.addEdge(g, G, H, 10);
        fh = Graphs.addEdge(g, F, H, 5);

        return g;
    }

    public static Graph<Integer, DefaultWeightedEdge> createSimpleConnectedWeightedGraph()
    {

        Graph<Integer, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        double bias = 1;

        g.addVertex(A);
        g.addVertex(B);
        g.addVertex(C);
        g.addVertex(D);
        g.addVertex(E);

        ab = Graphs.addEdge(g, A, B, bias * 2);
        ac = Graphs.addEdge(g, A, C, bias * 3);
        bd = Graphs.addEdge(g, B, D, bias * 5);
        Graphs.addEdge(g, C, D, bias * 20);
        de = Graphs.addEdge(g, D, E, bias * 5);
        Graphs.addEdge(g, A, E, bias * 100);

        return g;
    }

}
