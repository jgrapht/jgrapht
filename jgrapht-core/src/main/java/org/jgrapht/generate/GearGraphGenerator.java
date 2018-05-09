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
 * Generates a <a href="http://mathworld.wolfram.com/GearGraph.html"> gear graph</a> of any size.
 * This  graph is a wheel graph with a graph vertex added between each pair of adjacent graph vertices.
 *
 * @author Joris Kinable
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
  public class GearGraphGenerator<V, E>
    implements GraphGenerator<V, E, V>
 {
      int n;
  /**
 * Creates a new GearGraphGenerator object.
 *
 * @param n order of graph
 */
 public GearGraphGenerator(int n)
 {    
      this.n=n;
  }
    
     /**
     * {@inheritDoc}
     */
    @Override
    public void generateGraph(
        Graph<V, E> target, VertexFactory<V> vertexFactory, Map<String, V> resultMap)
  {
     if (n<2)
     { return;
     }
// create vertices

Map<Integer, V> map1 = new HashMap<>();

      
    
