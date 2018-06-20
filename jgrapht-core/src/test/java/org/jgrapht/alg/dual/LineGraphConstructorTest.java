/*
 * (C) Copyright 2018-2018, by Nikhil Sharma and Contributors.
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
package org.jgrapht.alg.dual;

import org.jgrapht.*;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.generate.StarGraphGenerator;
import org.jgrapht.graph.*;
import org.jgrapht.util.SupplierUtil;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for LineGraphConstructor
 *
 * @author Nikhil Sharma
 */
public class LineGraphConstructorTest
{

    @Test
    public void testEmptyGraph()
    {
        // Line Graph of an empty graph should be empty
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(0, 1, 2, 3));

        LineGraphConstructor<Integer, DefaultEdge, DefaultEdge> lgc = new LineGraphConstructor<>(g);
        Graph<DefaultEdge, DefaultEdge> target = new SimpleWeightedGraph<>(DefaultEdge.class);
        lgc.generateGraph(target);

        assertTrue(GraphTests.isEmpty(g));
    }

    @Test
    public void testStarGraph()
    {
        // Line Graph of a star graph is a complete graph
        Graph<Integer, DefaultEdge> starGraph = new SimpleGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        GraphGenerator<Integer, DefaultEdge, Integer> generator = new StarGraphGenerator<>(5);
        Map<String, Integer> resultMap = new HashMap<>();
        generator.generateGraph(starGraph, resultMap);

        LineGraphConstructor<Integer, DefaultEdge, DefaultEdge> lgc = new LineGraphConstructor<>(starGraph);
        Graph<DefaultEdge, DefaultEdge> target = new SimpleGraph<>(DefaultEdge.class);
        lgc.generateGraph(target);

        assertTrue(GraphTests.isComplete(target));
    }

    @Test
    public void testUndirectedGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4, 5));
        g.addEdge(1,2);
        g.addEdge(2,5);
        g.addEdge(5,4);
        g.addEdge(4,1);
        g.addEdge(4,3);
        g.addEdge(1,3);

        LineGraphConstructor<Integer, DefaultEdge, DefaultEdge> lgc = new LineGraphConstructor<>(g);
        Graph<DefaultEdge, DefaultEdge> target = new SimpleGraph<>(DefaultEdge.class);
        lgc.generateGraph(target);

        assertTrue(target.vertexSet().equals(g.edgeSet()));
        assertEquals(9, target.edgeSet().size());

        //Constructing expected graph
        Graph<DefaultEdge, DefaultEdge> expectedGraph = new SimpleGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(expectedGraph, g.edgeSet());
        List<DefaultEdge> vertexList = new ArrayList<>(expectedGraph.vertexSet());

        expectedGraph.addEdge(vertexList.get(0), vertexList.get(1));
        expectedGraph.addEdge(vertexList.get(0), vertexList.get(3));
        expectedGraph.addEdge(vertexList.get(0), vertexList.get(5));
        expectedGraph.addEdge(vertexList.get(1), vertexList.get(2));
        expectedGraph.addEdge(vertexList.get(2), vertexList.get(3));
        expectedGraph.addEdge(vertexList.get(2), vertexList.get(4));
        expectedGraph.addEdge(vertexList.get(3), vertexList.get(5));
        expectedGraph.addEdge(vertexList.get(3), vertexList.get(4));
        expectedGraph.addEdge(vertexList.get(4), vertexList.get(5));

        List<DefaultEdge> edgeListExpected = new ArrayList<>(expectedGraph.edgeSet());
        List<DefaultEdge> edgeListActual = new ArrayList<>(target.edgeSet());

        assertEquals(edgeListExpected.size(), edgeListActual.size());
        for(int i=0; i<edgeListExpected.size(); i++) {
            assertEquals(edgeListExpected.get(i).toString(), edgeListActual.get(i).toString());
        }
    }

    @Test
    public void testDirectedGraph()
    {
        Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
        g.addEdge(1,2);
        g.addEdge(1,4);
        g.addEdge(2,3);
        g.addEdge(3,1);
        g.addEdge(3,2);
        g.addEdge(3,4);
        g.addEdge(4,3);

        LineGraphConstructor<Integer, DefaultEdge, DefaultEdge> lgc = new LineGraphConstructor<>(g);
        Graph<DefaultEdge, DefaultEdge> target = new SimpleDirectedGraph<>(DefaultEdge.class);
        lgc.generateGraph(target);

        assertTrue(target.vertexSet().equals(g.edgeSet()));
        assertEquals(12, target.edgeSet().size());

        //Constructing expected graph
        Graph<DefaultEdge, DefaultEdge> expectedGraph = new SimpleDirectedGraph<>(DefaultEdge.class);
        Graphs.addAllVertices(expectedGraph, g.edgeSet());
        List<DefaultEdge> vertexList = new ArrayList<>(expectedGraph.vertexSet());

        expectedGraph.addEdge(vertexList.get(3), vertexList.get(0));
        expectedGraph.addEdge(vertexList.get(3), vertexList.get(1));
        expectedGraph.addEdge(vertexList.get(0), vertexList.get(2));
        expectedGraph.addEdge(vertexList.get(4), vertexList.get(2));
        expectedGraph.addEdge(vertexList.get(2), vertexList.get(3));
        expectedGraph.addEdge(vertexList.get(2), vertexList.get(4));
        expectedGraph.addEdge(vertexList.get(2), vertexList.get(5));
        expectedGraph.addEdge(vertexList.get(6), vertexList.get(3));
        expectedGraph.addEdge(vertexList.get(6), vertexList.get(4));
        expectedGraph.addEdge(vertexList.get(6), vertexList.get(5));
        expectedGraph.addEdge(vertexList.get(1), vertexList.get(6));
        expectedGraph.addEdge(vertexList.get(5), vertexList.get(6));

        List<DefaultEdge> edgeListExpected = new ArrayList<>(expectedGraph.edgeSet());
        List<DefaultEdge> edgeListActual = new ArrayList<>(target.edgeSet());

        assertEquals(edgeListExpected.size(), edgeListActual.size());
        for(int i=0; i<edgeListExpected.size(); i++) {
            assertEquals(edgeListExpected.get(i).toString(), edgeListActual.get(i).toString());
        }
    }
}
