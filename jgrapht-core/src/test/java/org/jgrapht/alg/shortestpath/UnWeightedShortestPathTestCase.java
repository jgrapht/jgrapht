/*
 * (C) Copyright 2018-2019, by Karri Sai Satish Kumar Reddy and Contributors.
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
package org.jgrapht.alg.shortestpath;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 *This test file can be used for testing any unweighted graph related algorithms.
 *
 * @author Karri Sai Satish Kumar Reddy
 */
public abstract class UnWeightedShortestPathTestCase
{
 
    // ~ Static fields/initializers ---------------------------------------------

    static final String V1 = "v1";
    static final String V2 = "v2";
    static final String V3 = "v3";
    static final String V4 = "v4";
    static final String V5 = "v5";
    
    // ~ Instance fields --------------------------------------------------------

    DefaultEdge e12;
    DefaultEdge e13;
    DefaultEdge e35;
    DefaultEdge e24;
    DefaultEdge e45;
    
    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    @Test
    public void testPathBetween()
    {
        List<DefaultEdge> path;
        Graph<String, DefaultEdge> g = create();

        path = findPathBetween(g, V1, V2);
        assertEquals(Arrays.asList(new DefaultEdge[] { e12 }), path);

        path = findPathBetween(g, V1, V4);
        assertEquals(Arrays.asList(new DefaultEdge[] { e12, e24 }), path);

        path = findPathBetween(g, V1, V5);
        assertEquals(Arrays.asList(new DefaultEdge[] { e13, e35 }), path);

        path = findPathBetween(g, V4, V3);
        assertEquals(Arrays.asList(new DefaultEdge[] { }), path);
    }
    
    
    /*
     * To test all the paths from a single source
     */
    
    @Test
    public void testAllPaths()
    {
        List<DefaultEdge> path;
        Graph<String, DefaultEdge> g = create();
        
        SingleSourcePaths<String, DefaultEdge> tree = getPathsFrom(g,V1);
        
        path = findPathTo(tree,V1);
        assertEquals(Arrays.asList(new DefaultEdge[] { }), path);
        
        path = findPathTo(tree,V2);
        assertEquals(Arrays.asList(new DefaultEdge[] {e12}), path);
        
        path = findPathTo(tree,V3);
        assertEquals(Arrays.asList(new DefaultEdge[] {e13 }), path);
        
        path = findPathTo(tree,V4);
        assertEquals(Arrays.asList(new DefaultEdge[] {e12, e24 }), path);
        
        path = findPathTo(tree,V5);
        assertEquals(Arrays.asList(new DefaultEdge[] {e13, e35 }), path);
        
    }
    
    protected abstract List<DefaultEdge> findPathBetween(
        Graph<String, DefaultEdge> g, String src, String dest);
    
    protected abstract List<DefaultEdge> findPathTo(
        SingleSourcePaths<String, DefaultEdge> tree,String dest);
    
    protected abstract SingleSourcePaths<String, DefaultEdge> getPathsFrom(
        Graph<String, DefaultEdge> g, String src);
    
    protected Graph<String, DefaultEdge> create()
    {
        Graph<String, DefaultEdge> g;
        
        g = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        g.addVertex(V4);
        g.addVertex(V5);

        e12 = Graphs.addEdgeWithVertices(g, V1, V2);

        e13 = Graphs.addEdgeWithVertices(g, V1, V3);

        e24 = Graphs.addEdgeWithVertices(g, V2, V4);

        e35 = Graphs.addEdgeWithVertices(g, V3, V5);

        e45 = Graphs.addEdgeWithVertices(g, V4, V5);

        
        return g;
        
    }


}
