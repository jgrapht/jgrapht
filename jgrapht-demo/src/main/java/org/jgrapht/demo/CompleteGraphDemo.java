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
/* --------------------
 * CompleteGraphDemo.java
 * --------------------
 * (C) Copyright 2003-2008, by Tim Shearouse and Contributors.
 *
 * Original Author:  Tim Shearouse
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 *
 */
package org.jgrapht.demo;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;


public final class CompleteGraphDemo
{
    

    static Graph<Object, DefaultEdge> completeGraph;

    //Number of vertices
    static int size = 10;

    

    public static void main(String [] args)
    {
        //Create the graph object; it is null at this point
        completeGraph = new SimpleGraph<Object, DefaultEdge>(DefaultEdge.class);

        //Create the CompleteGraphGenerator object
        CompleteGraphGenerator<Object, DefaultEdge> completeGenerator =
            new CompleteGraphGenerator<Object, DefaultEdge>(size);

        //Create the VertexFactory so the generator can create vertices
        VertexFactory<Object> vFactory =
            new ClassBasedVertexFactory<Object>(Object.class);

        //Use the CompleteGraphGenerator object to make completeGraph a
        //complete graph with [size] number of vertices
        completeGenerator.generateGraph(completeGraph, vFactory, null);

        //Now, replace all the vertices with sequential numbers so we can ID
        //them
        Set<Object> vertices = new HashSet<Object>();
        vertices.addAll(completeGraph.vertexSet());
        Integer counter = 0;
        for (Object vertex : vertices) {
            replaceVertex(vertex, (Object) counter++);
        }

        //Print out the graph to be sure it's really complete
        Iterator<Object> iter =
            new DepthFirstIterator<Object, DefaultEdge>(completeGraph);
        Object vertex;
        while (iter.hasNext()) {
            vertex = iter.next();
            System.out.println(
                "Vertex " + vertex.toString() + " is connected to: "
                + completeGraph.edgesOf(vertex).toString());
        }
    }

    public static boolean replaceVertex(Object oldVertex, Object newVertex)
    {
        if ((oldVertex == null) || (newVertex == null)) {
            return false;
        }
        Set<DefaultEdge> relatedEdges = completeGraph.edgesOf(oldVertex);
        completeGraph.addVertex(newVertex);

        Object sourceVertex;
        Object targetVertex;
        for (DefaultEdge e : relatedEdges) {
            sourceVertex = completeGraph.getEdgeSource(e);
            targetVertex = completeGraph.getEdgeTarget(e);
            if (sourceVertex.equals(oldVertex)
                && targetVertex.equals(oldVertex))
            {
                completeGraph.addEdge(newVertex, newVertex);
            } else {
                if (sourceVertex.equals(oldVertex)) {
                    completeGraph.addEdge(newVertex, targetVertex);
                } else {
                    completeGraph.addEdge(sourceVertex, newVertex);
                }
            }
        }
        completeGraph.removeVertex(oldVertex);
        return true;
    }
}

// End CompleteGraphDemo.java
