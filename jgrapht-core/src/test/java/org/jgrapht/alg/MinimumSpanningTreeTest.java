/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://org.org.jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
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
/* ------------------------------
 * MinimumSpanningTreeTest.java
 * ------------------------------
 * (C) Copyright 2010-2010, by Tom Conerly and Contributors.
 *
 * Original Author:  Tom Conerly
 * Contributor(s):   -
 *
 * Changes
 * -------
 * 02-Feb-2010 : Initial revision (TC);
 *
 */
package org.jgrapht.alg;

import junit.framework.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.Arrays;
import java.util.Collection;


public class MinimumSpanningTreeTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";
    private static final String E = "E";
    private static final String F = "F";
    private static final String G = "G";
    private static final String H = "H";

    //~ Instance fields --------------------------------------------------------

    private DefaultWeightedEdge AB;
    private DefaultWeightedEdge AC;
    private DefaultWeightedEdge AE;
    private DefaultWeightedEdge BD;
    private DefaultWeightedEdge CD;
    private DefaultWeightedEdge DE;
    private DefaultWeightedEdge EF;
    private DefaultWeightedEdge EG;
    private DefaultWeightedEdge GH;
    private DefaultWeightedEdge FH;

    //~ Methods ----------------------------------------------------------------


    public void testKruskal() {

        testMinimumSpanningTreeBuilding(
                new KruskalMinimumSpanningTree<String, DefaultWeightedEdge>(createSimpleConnectedWeightedGraph()),
                Arrays.asList(AB, AC, BD, DE),
                15.0
        );

        testMinimumSpanningTreeBuilding(
                new KruskalMinimumSpanningTree<String, DefaultWeightedEdge>(createSimpleDisconnectedWeightedGraph()),
                Arrays.asList(AB, AC, BD, EG, GH, FH),
                60.0
        );

        testKruskalMinimumSpanningTreeBuildingBackwardCompatibility(
                new KruskalMinimumSpanningTree<String, DefaultWeightedEdge>(createSimpleConnectedWeightedGraph()),
                Arrays.asList(AB, AC, BD, DE),
                15.0
        );

    }

    public void testPrim() {

        testMinimumSpanningTreeBuilding(
                new PrimMinimumSpanningTree<String, DefaultWeightedEdge>(createSimpleConnectedWeightedGraph()),
                Arrays.asList(AB, AC, BD, DE),
                15.0
        );

        testMinimumSpanningTreeBuilding(
                new PrimMinimumSpanningTree<String, DefaultWeightedEdge>(createSimpleDisconnectedWeightedGraph()),
                Arrays.asList(AB, AC, BD, EG, GH, FH),
                60.0
        );

    }


    protected <V, E> void testMinimumSpanningTreeBuilding(
        final MinimumSpanningTree<V, DefaultWeightedEdge> mst,
        final Collection<E> edgeSet,
        final double weight) {

            assertEquals(weight, mst.getMinimumSpanningTreeTotalWeight());
            assertTrue(mst.getMinimumSpanningTreeEdgeSet().containsAll(edgeSet));

    }

    protected <V, E> void testKruskalMinimumSpanningTreeBuildingBackwardCompatibility(
            final KruskalMinimumSpanningTree<V, DefaultWeightedEdge> mst,
            final Collection<E> edgeSet,
            final double weight) {

        assertEquals(weight, mst.getSpanningTreeCost());
        assertTrue(mst.getEdgeSet().containsAll(edgeSet));

    }

    protected Graph<String, DefaultWeightedEdge> createSimpleDisconnectedWeightedGraph() {

        Graph<String, DefaultWeightedEdge> g =
                new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        /**
         *
         * A -- B   E -- F
         * |    |   |    |
         * C -- D   G -- H
         *
         */

        g.addVertex(A);
        g.addVertex(B);
        g.addVertex(C);
        g.addVertex(D);

        AB = Graphs.addEdge(g, A, B, 5);
        AC = Graphs.addEdge(g, A, C, 10);
        BD = Graphs.addEdge(g, B, D, 15);
        CD = Graphs.addEdge(g, C, D, 20);

        g.addVertex(E);
        g.addVertex(F);
        g.addVertex(G);
        g.addVertex(H);

        EF = Graphs.addEdge(g, E, F, 20);
        EG = Graphs.addEdge(g, E, G, 15);
        GH = Graphs.addEdge(g, G, H, 10);
        FH = Graphs.addEdge(g, F, H, 5);

        return g;
    }

    protected Graph<String, DefaultWeightedEdge> createSimpleConnectedWeightedGraph() {

        Graph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        double bias = 1;

        g.addVertex(A);
        g.addVertex(B);
        g.addVertex(C);
        g.addVertex(D);
        g.addVertex(E);

        AB = Graphs.addEdge(g, A, B, bias * 2);
        AC = Graphs.addEdge(g, A, C, bias * 3);
        BD = Graphs.addEdge(g, B, D, bias * 5);
        CD = Graphs.addEdge(g, C, D, bias * 20);
        DE = Graphs.addEdge(g, D, E, bias * 5);
        AE = Graphs.addEdge(g, A, E, bias * 100);

        return g;
    }

}

// End MinimumSpanningTreeTest.java
