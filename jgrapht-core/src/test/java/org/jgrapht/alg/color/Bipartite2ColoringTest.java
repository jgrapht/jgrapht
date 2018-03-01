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

/**
 * Test for the BipartiteColoring class.
 * 
 * @author Meghana M Reddy
 *
 */
public class Bipartite2ColoringTest {
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
        dg.addEdge(c, f);
        dg.addEdge(b, g);
        dg.addEdge(b, d);
        
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
}


