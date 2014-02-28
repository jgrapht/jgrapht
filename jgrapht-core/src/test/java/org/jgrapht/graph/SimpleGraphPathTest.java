/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* ------------------------
 * SimpleGraphPathTest.java
 * ------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Rodrigo López Dato
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 22-Jan-2014 : Initial revision;
 */

package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;

public class SimpleGraphPathTest
    extends EnhancedTestCase
{
    private SimpleGraph<String, DefaultEdge> graph;
    private GraphPath<String, DefaultEdge> path;
    private List<String> pathVertices;

    @Override
    public void setUp()
    {
        graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        graph.addVertex("v1");
        graph.addVertex("v2");
        graph.addVertex("v3");
        graph.addVertex("v4");
        graph.addEdge("v1", "v2");
        graph.addEdge("v1", "v3");
        graph.addEdge("v3", "v4");
        graph.addEdge("v2", "v4");

        pathVertices = new ArrayList<String>();
        pathVertices.add("v1");
        pathVertices.add("v2");
        pathVertices.add("v4");
        
        path = new SimpleGraphPath<String, DefaultEdge>(graph, pathVertices, 1);
    }

    public void testEdgeList()
    {
        DefaultEdge[] expectedEdges = {
            graph.getEdge("v1", "v2"),
            graph.getEdge("v2", "v4") };
        assertEquals(Arrays.asList(expectedEdges), path.getEdgeList());
    }
    
    public void testInvalidVertexList() {
        List<String> invalidPath = new ArrayList<String>();
        invalidPath.add("v1");
        invalidPath.add("v2");
        invalidPath.add("v3");
        try {
            new SimpleGraphPath<String, DefaultEdge>(graph, invalidPath, 1);
        } catch (IllegalArgumentException e) {
            assertTrue();
        }
    }
    
    public void testSingleVertexList() {
        List<String> invalidPath = new ArrayList<String>();
        invalidPath.add("v1");
        try {
            new SimpleGraphPath<String, DefaultEdge>(graph, invalidPath, 1);
        } catch (IllegalArgumentException e) {
            assertTrue();
        }
    }
    
    public void testStartVertex() {
        assertTrue(path.getStartVertex().equals("v1"));
    }
    
    public void testEndVertex() {
        assertTrue(path.getEndVertex().equals("v4"));
    }
    
    public void testPathWeight() {
        assertEquals(1.0, path.getWeight());
    }
    
}
