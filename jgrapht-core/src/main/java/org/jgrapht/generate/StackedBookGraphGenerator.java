/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
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
 package org.jgrapht.generate;

import java.util.*;

import org.jgrapht.*;

/**
 * Generates a <a href="http://mathworld.wolfram.com/StackedBookGraph.html">star graph</a> of any size.
 * This  graph is a star graph of order (m+1) stacked on (n-1) other star graph of same order .
 *
 * @author Joris Kinable
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
 public class StackedBookGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
{
    private int m;
    private int n;
/**
 * Creates a new StackedBookGraphGenerator object.
 *
 * @param m number of vertices in a star graph excluding centeral vertex
 * @param n number of star graphs to be stacked
 */
 public  StackedBookGraphGenerator(int m,int n)
    {
          this.m=m;
          this.n=n;
     }
     
     /**
     * {@inheritDoc}
     */
    @Override
    public void generateGraph(
        Graph<V, E> target, VertexFactory<V> vertexFactory, Map<String, V> resultMap)
  {
     if ((m < 1)||(n<2)) 
       {    return;
        }
       // Create other vertices
    
 Map<Integer, V> map1 = new HashMap<>();

        // Adding all vertices to the map1
        
        for (int i = 1; i <= (m * n); i++)
{
        V vertex = vertexFactory.createVertex();
        target.addVertex(vertex);
        map1.put(i , vertex);
 }
 // Create CENTER vertices
    
 Map<Integer, V> map2 = new HashMap<>();

        // Adding all vertices to the map2
        
        for (int i = 1; i <=(n); i++)
{
         V vertex = vertexFactory.createVertex();
         target.addVertex(vertex);
         map2.put(i , vertex);
}
// Connect vertices to another vertices

  for (int i=1,j=m+1;i<=m*n && j<=m*n;i++,j++){
  target.addEdge(map1.get(i), map1.get(j));
  }
     // Connect  vertices to center vertices
for(int j=1;j<=n;j++)
{
for(int i=1;i<=m;i++)
{ target.addEdge(map1.get(i), map2.get(j));
 }
}
 //  connect center vertices to another center vertices
for(int j=1;j<n;j++)
  {
       target.addEdge(map2.get(j), map2.get(j+1));
        }
   }
}
// End StackedBookGraphGenerator.java
