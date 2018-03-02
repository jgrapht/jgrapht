/*
 * (C) Copyright 2018-2018, by Meghana M Reddy and Contributors.
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
package org.jgrapht.alg.color;

import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexColoringAlgorithm.Coloring;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * Test for the Bipartite2Coloring class.
 * 
 * @author Meghana M Reddy
 *
 */
public class Bipartite2ColoringTest {
    /**
     * Test for an undirected bipartite graph with four vertices - a,b,c,d.
     * And the partitions being (a,b) and (c,d).
     */
    public void test1() {
        Graph<String, DefaultEdge> dg = new Pseudograph<>(DefaultEdge.class);
        String a = new String("a");
        String b = new String("b");
        String c = new String("c");
        String d = new String("d");
        
        dg.addVertex(a);
        dg.addVertex(b);
        dg.addVertex(c);
        dg.addVertex(d);
        
        dg.addEdge(a, d);
        dg.addEdge(a, c);
        dg.addEdge(b, c);
        
        Coloring<String> coloring = new Bipartite2Coloring<>(dg).getColoring();
        Map<String, Integer> colors = coloring.getColors();
        
        assertEquals(2, coloring.getNumberColors());
        assertEquals(1, colors.get(a).intValue());
        assertEquals(1, colors.get(b).intValue());
        assertEquals(0, colors.get(c).intValue());
        assertEquals(0, colors.get(d).intValue());
        
    }
    
    /**
     * Testing on a disconnected directed graph of 7 vertices. 
     * And the partitions being (a,b,c) and (d,e,f,g).
     */
    public void test2() {
        Graph<String, DefaultEdge> dg = new DefaultDirectedGraph<>(DefaultEdge.class);
    
        String a = new String("a");
        String b = new String("b");
        String c = new String("c");
        String d = new String("d");
        String e = new String("e");
        String f = new String("f");
        String g = new String("g");
        
        dg.addVertex(a);
        dg.addVertex(b);
        dg.addVertex(c);
        dg.addVertex(d);
        dg.addVertex(e);
        dg.addVertex(f);
        dg.addVertex(g);
        dg.addEdge(a, d);
        dg.addEdge(a, e);
        dg.addEdge(g, a);
        dg.addEdge(c, e);
        dg.addEdge(f, c);
        dg.addEdge(b, g);
        dg.addEdge(d, b);
        
        Coloring<String> coloring = new Bipartite2Coloring<>(dg).getColoring();
        Map<String, Integer> colors = coloring.getColors();
        assertEquals(2, coloring.getNumberColors());
        assertEquals(1, colors.get(a).intValue());
        assertEquals(1, colors.get(c).intValue());
        assertEquals(1, colors.get(b).intValue());
        assertEquals(0, colors.get(d).intValue());
        assertEquals(0, colors.get(e).intValue());
        assertEquals(0, colors.get(f).intValue());
        assertEquals(0, colors.get(g).intValue());
        
    }
    
    /**
     * Test for an edgeless graph with four vertices - a,b,c,d.
     * All the vertices will be given one color.
     */
    public void test3() {
        Graph<String, DefaultEdge> dg = new Pseudograph<>(DefaultEdge.class);
        String a = new String("a");
        String b = new String("b");
        String c = new String("c");
        String d = new String("d");
        
        dg.addVertex(a);
        dg.addVertex(b);
        dg.addVertex(c);
        dg.addVertex(d);
        
        Coloring<String> coloring = new Bipartite2Coloring<>(dg).getColoring();
        Map<String, Integer> colors = coloring.getColors();
        
        assertEquals(1, coloring.getNumberColors());
        assertEquals(0, colors.get(a).intValue());
        assertEquals(0, colors.get(b).intValue());
        assertEquals(0, colors.get(c).intValue());
        assertEquals(0, colors.get(d).intValue());   
    }
    
    /**
     * Testing on a non bipartite graph.
     * The map object returned is null.
     */
    public void test4() {
        Graph<String, DefaultEdge> dg = new Pseudograph<>(DefaultEdge.class);
        String a = new String("a");
        String b = new String("b");
        String c = new String("c");
        String d = new String("d");
        
        dg.addVertex(a);
        dg.addVertex(b);
        dg.addVertex(c);
        dg.addVertex(d);
        
        dg.addEdge(a, d);
        dg.addEdge(a, c);
        dg.addEdge(b, c);
        dg.addEdge(d, c);
        
        Coloring<String> coloring = new Bipartite2Coloring<>(dg).getColoring();
        Map<String, Integer> colors = coloring.getColors();
        
        assertEquals(null, colors); 
    }
}


