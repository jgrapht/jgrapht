/*
 * (C) Copyright 2017-2017, by Karolina Rezkova and Contributors.
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
package org.jgrapht.alg.planarity;

import junit.framework.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Karolina
 */
public class HopcroftTarjanPlanarityInspectorTest extends TestCase{
    
    private static final String V0 = "v0";
    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";
    private static final String V5 = "v5";
    private static final String V6 = "v6";
    private static final String V7 = "v7";
    private static final String V8 = "v8";
    private static final String V9 = "v9";
    
    /**
     * creates graph containing $K_{3,3}$  - 
     * subgraph having vertex set $V=\{V0, V2, V4, V3, V8, V9\}$
     * 
     * @param g input graph
     */
   
    public void createNonplanarGraph1(Graph<String, DefaultEdge> g){
        
        g.addVertex(V0);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);
        g.addVertex(V6);
        g.addVertex(V7);
        g.addVertex(V8);
        g.addVertex(V9);
        
        g.addEdge(V0, V1);
        g.addEdge(V0, V2);
        g.addEdge(V0, V3);
        g.addEdge(V0, V4);
        g.addEdge(V0, V6);
        g.addEdge(V0, V8);
        g.addEdge(V0, V9);
        
        g.addEdge(V1, V2);
        
        g.addEdge(V2, V3);
        g.addEdge(V2, V4);
        g.addEdge(V2, V6);
        g.addEdge(V2, V7);
        g.addEdge(V2, V8);
        g.addEdge(V2, V9);
        
        g.addEdge(V4, V3);
        g.addEdge(V3, V5);
        g.addEdge(V3, V6);
        
        g.addEdge(V4, V8);
        g.addEdge(V4, V7);
        g.addEdge(V4, V9);
    
        
        
    }
    
    /**
     * creates graph containing $K_{5}$  - 
     * subgraph having vertex set $V=\{V2, V3, V4, V6, V7\}$
     * 
     * @param g input graph
     */
    public void createNonplanarGraph2(Graph<String, DefaultEdge> g){
        
        g.addVertex(V0);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);
        g.addVertex(V6);
        g.addVertex(V7);
        g.addVertex(V8);
        g.addVertex(V9);
        
      
        g.addEdge(V0, V3);
        g.addEdge(V0, V4);
        g.addEdge(V0, V5);
        g.addEdge(V0, V7);
        
        g.addEdge(V1, V6);
        g.addEdge(V1, V9);
        
        g.addEdge(V2, V3);
        g.addEdge(V2, V4);
        g.addEdge(V2, V6);
        g.addEdge(V2, V7);
       
        
        g.addEdge(V4, V3);
        g.addEdge(V3, V7);
        g.addEdge(V3, V6);
        g.addEdge(V3, V8);
        
        g.addEdge(V4, V6);
        g.addEdge(V4, V7);
        g.addEdge(V4, V9);
        
        g.addEdge(V5, V7);
        
        g.addEdge(V6, V7);
        
        g.addEdge(V8, V9);
    }
        
        
    /**
     * creates planar graph
     * 
     * @param g input graph
     */
    public void createPlanarGraph(Graph<String, DefaultEdge> g)
        {
        g.addVertex(V0);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);
        g.addVertex(V6);
        g.addVertex(V7);
        g.addVertex(V8);
        g.addVertex(V9);
        
      
        g.addEdge(V0, V1);
        g.addEdge(V0, V2);
        g.addEdge(V0, V8);
        g.addEdge(V0, V9);
        
        g.addEdge(V1, V2);
        g.addEdge(V1, V8);
        
        g.addEdge(V2, V3);
        g.addEdge(V2, V4);
        g.addEdge(V2, V5);
        g.addEdge(V2, V8);
        g.addEdge(V2, V9);
       
        g.addEdge(V3, V6);
        g.addEdge(V3, V9);
        
        g.addEdge(V4, V6);
        
        g.addEdge(V5, V6);
        g.addEdge(V5, V8);
        
        g.addEdge(V6, V7);
        g.addEdge(V8, V6);
        
        g.addEdge(V7, V9);
    }  
    

    /**
     * Test of isPlanar method, of class HopcroftTarjanPlanarityInspector.
     */
    @Test
    public void testIsPlanar() {
        System.out.println("isPlanar");
        
        Graph<String, DefaultEdge> g1 = new SimpleGraph<>(DefaultEdge.class);
        createNonplanarGraph1(g1);
        assertFalse(new HopcroftTarjanPlanarityInspector(g1).isPlanar());
        
        Graph<String, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
        createNonplanarGraph2(g2);
        assertFalse(new HopcroftTarjanPlanarityInspector(g2).isPlanar());
        
        
        Graph<String, DefaultEdge> g3 = new SimpleGraph<>(DefaultEdge.class);
        createPlanarGraph(g3);
        assertTrue(new HopcroftTarjanPlanarityInspector(g3).isPlanar());
        
    }
        
    
    
    
    
}
