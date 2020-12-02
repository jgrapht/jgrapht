/*
 * (C) Copyright 2018-2020, by Alexander Kriegisch and Contributors.
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
package org.jgrapht.alg.tour;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.SlowTests;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.jgrapht.TestUtil.*;
import static org.jgrapht.alg.CyclicTransitiveReductionTest.populateGraphWithSCCs;
import static org.jgrapht.alg.tour.TwoApproxMetricTSPTest.assertHamiltonian;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DirectedHamiltonianCycleTest {

  private static <V> Graph<V, DefaultEdge> createEmptyGraph() {
    return GraphTypeBuilder
      .<V, DefaultEdge>directed()
      .allowingMultipleEdges(false)
      .allowingSelfLoops(false)
      .edgeClass(DefaultEdge.class)
      .buildGraph();
  }

  @Test
  public void smallHamiltonianGraph() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    GraphPath<String, DefaultEdge> tour;

    addHamiltonianCycle(graph, "A", "B", "C", "D");
    tour = new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
    assertNotNull(tour);
    assertHamiltonian(graph, tour);

    addEdges(graph, "A", "C", "B", "D");
    tour = new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
    assertNotNull(tour);
    assertHamiltonian(graph, tour);
  }

  @Test
  public void smallNonHamiltonianGraph() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    GraphPath<String, DefaultEdge> tour;

    addVertices(graph, "A", "B", "C", "D");
    addEdges(graph, "A", "B", "B", "C", "B", "D");
    tour = new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
    assertNull(tour);
  }

  @Test
  public void mediumHamiltonianGraph() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    GraphPath<String, DefaultEdge> tour;

    populateGraphWithSCCs(graph, 1, 16);
    // Shuffling vertices and edges helps increase test coverage
    randomiseGraph(graph);
    tour = new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
    assertNotNull(tour);
    assertHamiltonian(graph, tour);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullGraph() {
    Graph<String, DefaultEdge> graph = null;
    new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
  }

  @Test(expected = IllegalArgumentException.class)
  public void undirectedGraph() {
    Graph<String, DefaultEdge> graph = GraphTypeBuilder
      .<String, DefaultEdge>undirected()
      .allowingMultipleEdges(false)
      .allowingSelfLoops(false)
      .edgeClass(DefaultEdge.class)
      .buildGraph();
    addHamiltonianCycle(graph, "A", "B", "C", "D");
    new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
  }

  @Test(expected = IllegalArgumentException.class)
  public void selfLoopGraph() {
    Graph<String, DefaultEdge> graph = GraphTypeBuilder
      .<String, DefaultEdge>directed()
      .allowingMultipleEdges(false)
      .allowingSelfLoops(true)
      .edgeClass(DefaultEdge.class)
      .buildGraph();
    addHamiltonianCycle(graph, "A", "B", "C", "D");
    new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
  }

  @Test(expected = IllegalArgumentException.class)
  public void weightedGraph() {
    Graph<String, DefaultEdge> graph = GraphTypeBuilder
      .<String, DefaultEdge>directed()
      .weighted(true)
      .allowingMultipleEdges(false)
      .allowingSelfLoops(false)
      .edgeClass(DefaultEdge.class)
      .buildGraph();
    addHamiltonianCycle(graph, "A", "B", "C", "D");
    new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multipleEdgesGraph() {
    Graph<String, DefaultEdge> graph = GraphTypeBuilder
      .<String, DefaultEdge>directed()
      .allowingMultipleEdges(true)
      .allowingSelfLoops(false)
      .edgeClass(DefaultEdge.class)
      .buildGraph();
    addHamiltonianCycle(graph, "A", "B", "C", "D");
    new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
  }

  @Test(expected = IllegalArgumentException.class)
  public void emptyGraph() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
  }

  @Test(expected = IllegalArgumentException.class)
  public void graphTooSmall() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();

    addVertices(graph, "A", "B");
    new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
  }

  @Test
  @Category(SlowTests.class)
  public void randomisedHamiltonianGraphsDifferentSizes() {
    Graph<String, DefaultEdge> graph;
    GraphPath<String, DefaultEdge> tour;
    for (int numVertices = 3; numVertices <= 53; numVertices++) {
      graph = createEmptyGraph();
      populateGraphWithSCCs(graph, 1, numVertices);
      // Shuffling vertices and edges helps increase test coverage
      randomiseGraph(graph);
      tour = new DirectedHamiltonianCycle<String, DefaultEdge>().getTour(graph);
      assertNotNull(tour);
      assertHamiltonian(graph, tour);
    }
  }

}
