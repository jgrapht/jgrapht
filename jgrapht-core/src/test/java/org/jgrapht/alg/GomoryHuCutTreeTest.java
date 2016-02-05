/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2016, by Barak Naveh and Contributors.
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
/* -------------------------
 * GomoryHuCutTreeTest.java
 * -------------------------
 * (C) Copyright 2016, by Mads Jensen
 *
 * Original Author:  Mads Jensen
 * Contributor(s):
 *
 * Changes
 * -------
 * Jan-2016 : Initial version;
 *
 */
package org.jgrapht.alg;

import java.util.*;

import junit.framework.*;
import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.graph.*;

public class GomoryHuCutTreeTest extends TestCase {
    static final String V0 = "v0";
    static final String V1 = "v1";
    static final String V2 = "v2";
    static final String V3 = "v3";
    static final String V4 = "v4";
    static final String V5 = "v5";

    DefaultWeightedEdge e01;
    DefaultWeightedEdge e02;
    DefaultWeightedEdge e13;
    DefaultWeightedEdge e14;
    DefaultWeightedEdge e24;
    DefaultWeightedEdge e34;
    DefaultWeightedEdge e35;
    DefaultWeightedEdge e45;

    private void createGraph(SimpleWeightedGraph<String, DefaultWeightedEdge> graph)
    {
        graph.addVertex(V0);
        graph.addVertex(V1);
        graph.addVertex(V2);
        graph.addVertex(V3);
        graph.addVertex(V4);
        graph.addVertex(V5);

        e01 = Graphs.addEdge(graph, V0, V1, 1);
        e02 = Graphs.addEdge(graph, V0, V2, 7);
        e13 = Graphs.addEdge(graph, V1, V3, 3);
        e14 = Graphs.addEdge(graph, V1, V4, 2);
        e24 = Graphs.addEdge(graph, V2, V4, 4);
        e34 = Graphs.addEdge(graph, V3, V4, 1);
        e35 = Graphs.addEdge(graph, V3, V5, 6);
        e45 = Graphs.addEdge(graph, V4, V5, 2);
    }

    public void testWikipediaGraph(){
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        createGraph(graph);

        SimpleWeightedGraph<String, DefaultWeightedEdge> outputGraph =
            new SimpleWeightedGraph(DefaultWeightedEdge.class);

        GomoryHuCutTree<String, DefaultWeightedEdge> gomoryHuCutTree =
            new GomoryHuCutTree<String, DefaultWeightedEdge>(graph);
        Set<DefaultWeightedEdge> cutEdges = gomoryHuCutTree.getCutTreeEdges();

        outputGraph.addVertex(V0);
        outputGraph.addVertex(V1);
        outputGraph.addVertex(V2);
        outputGraph.addVertex(V3);
        outputGraph.addVertex(V4);
        outputGraph.addVertex(V5);

        DefaultWeightedEdge e1 = Graphs.addEdge(outputGraph, V0, V2, 8);
        DefaultWeightedEdge e2 = Graphs.addEdge(outputGraph, V2, V4, 6);
        DefaultWeightedEdge e3 = Graphs.addEdge(outputGraph, V4, V1, 7);
        DefaultWeightedEdge e4 = Graphs.addEdge(outputGraph, V1, V3, 6);
        DefaultWeightedEdge e5 = Graphs.addEdge(outputGraph, V3, V5, 8);

        HashSet<DefaultWeightedEdge> edges = new HashSet<DefaultWeightedEdge>();
        edges.add(e1);
        edges.add(e2);
        edges.add(e3);
        edges.add(e4);
        edges.add(e5);
        Iterator<DefaultWeightedEdge> it = cutEdges.iterator();
        while(it.hasNext()){
            assertTrue(cutEdges.contains(it.next()));
        }
    }
}
