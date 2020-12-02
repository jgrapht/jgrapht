/*
 * (C) Copyright 2018-2020, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg;

import org.jgrapht.Graph;
import org.jgrapht.SlowTests;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Random;

import static org.jgrapht.TestUtil.*;
import static org.jgrapht.util.MathUtil.naturalNumberSumGauss;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: add tests, for now there are only helper methods to be used both here and in DirectedHamiltonianCycleTest
 */
public class CyclicTransitiveReductionTest {
  private static final Random RANDOM = new Random();

  private static <V> Graph<V, DefaultEdge> createEmptyGraph() {
    return GraphTypeBuilder
      .<V, DefaultEdge>directed()
      .allowingMultipleEdges(false)
      .allowingSelfLoops(false)
      .edgeClass(DefaultEdge.class)
      .buildGraph();
  }

  /**
   * Creates a directed graph suitable for testing cyclic transitive reduction and Hamiltonian cycle (HC).
   * <p></p>
   * More precisely, a graph with <i>sccCount</i> strongly connected components (SCCs) and <i>sccSize</i> vertices per
   * SCC will be generated. Within each SCC there will of course be a HC plus edges in random directions between all
   * other remaining pairs of vertices, i.e. the graph will be dense. Furthermore, between vertex #v of SCC #s and
   * vertex #v of SCC #(s+1) there will be a forward edge, i.e. there is a number of redundant edges between
   * corresponding vertices or neighbouring SCCs. Hence, both within SCCs and between them there are redundant edges to
   * be detected and removed by a cyclic transitive reduction algorithm. The condensation graph will be a linear string
   * of SCCs.
   *
   * @param graph    graph to be populated with SCCs (ideally empty before calling this method)
   * @param sccCount number of SCCs to be generated; if equals 1, generate a single HC
   * @param sccSize  number of vertices per SCC to be generated
   * @param <E>      edge type
   * @throws IllegalArgumentException if <i>sccSize</i> < 3
   */
  public static <E> void populateGraphWithSCCs(final Graph<String, E> graph, final int sccCount, final int sccSize) {
    if (sccSize < 3)
      throw new IllegalArgumentException("SCC size (number of vertices per SCC) must be > 2");
    // Generate vertices + HCs for all SCCs
    for (int currentSCC = 0; currentSCC < sccCount; currentSCC++) {
      // Generate vertices for current SCC
      for (int currentVertex = 0; currentVertex < sccSize; currentVertex++)
        graph.addVertex(getSCCVertex(currentSCC, currentVertex));
      // Generate HC for current SCC
      for (int currentVertex = 0; currentVertex < sccSize; currentVertex++)
        graph.addEdge(
          getSCCVertex(currentSCC, currentVertex),
          getSCCVertex(currentSCC, (currentVertex + 1) % sccSize)
        );
    }

    // Generate redundant edges within SCCs
    for (int currentSCC = 0; currentSCC < sccCount; currentSCC++) {
      for (int source = 0; source < sccSize; source++) {
        String sourceVertex = getSCCVertex(currentSCC, source);
        // Generate redundant edges within SCCs in random direction
        for (int target = source + 1; target < sccSize; target++) {
          String targetVertex = getSCCVertex(currentSCC, target);
          if (sourceVertex.equals(targetVertex))
            continue;
          if (graph.containsEdge(sourceVertex, targetVertex) || graph.containsEdge(targetVertex, sourceVertex))
            continue;
          if (RANDOM.nextBoolean())
            graph.addEdge(sourceVertex, targetVertex);
          else
            graph.addEdge(targetVertex, sourceVertex);
        }
        // Add redundant forward edges to vertices with identical numbers inside next SCC
        if (currentSCC + 1 < sccCount)
          graph.addEdge(sourceVertex, getSCCVertex(currentSCC + 1, source));
      }
    }
  }

  private static String getSCCVertex(final int sccNumber, final int vertexNumber) {
    return "S" + sccNumber + "/V" + vertexNumber;
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullGraph() {
    Graph<String, DefaultEdge> graph = null;
    new CyclicTransitiveReduction<>(graph);
  }

  @Test(expected = IllegalArgumentException.class)
  public void undirectedGraph() {
    Graph<String, DefaultEdge> graph = GraphTypeBuilder
      .<String, DefaultEdge>undirected()
      .allowingMultipleEdges(false)
      .allowingSelfLoops(false)
      .edgeClass(DefaultEdge.class)
      .buildGraph();
    new CyclicTransitiveReduction<>(graph);
  }

  @Test(expected = IllegalArgumentException.class)
  public void selfLoopGraph() {
    Graph<String, DefaultEdge> graph = GraphTypeBuilder
      .<String, DefaultEdge>directed()
      .allowingMultipleEdges(false)
      .allowingSelfLoops(true)
      .edgeClass(DefaultEdge.class)
      .buildGraph();
    new CyclicTransitiveReduction<>(graph);
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
    new CyclicTransitiveReduction<>(graph);
  }

  @Test(expected = IllegalArgumentException.class)
  public void multipleEdgesGraph() {
    Graph<String, DefaultEdge> graph = GraphTypeBuilder
      .<String, DefaultEdge>directed()
      .allowingMultipleEdges(true)
      .allowingSelfLoops(false)
      .edgeClass(DefaultEdge.class)
      .buildGraph();
    new CyclicTransitiveReduction<>(graph);
  }

  @Test
  public void emptyGraph() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    new CyclicTransitiveReduction<>(graph).reduce();
    assertEquals(0, graph.vertexSet().size());
    assertEquals(0, graph.edgeSet().size());
  }

  @Test
  public void oneElementGraph() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    graph.addVertex("A");
    new CyclicTransitiveReduction<>(graph).reduce();
    assertEquals(1, graph.vertexSet().size());
    assertEquals(0, graph.edgeSet().size());
  }

  @Test
  public void twoElementsGraph() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    addVertices(graph, "A", "B");
    graph.addEdge("A", "B");
    new CyclicTransitiveReduction<>(graph).reduce();
    assertEquals(2, graph.vertexSet().size());
    assertEquals(1, graph.edgeSet().size());
  }

  @Test
  public void smallHamiltonianGraph() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    addHamiltonianCycle(graph, "A", "B", "C", "D");
    new CyclicTransitiveReduction<>(graph).reduce();
    assertEquals(4, graph.vertexSet().size());
    assertEquals(4, graph.edgeSet().size());
    assertTrue(graph.containsEdge("A", "B"));
    assertTrue(graph.containsEdge("B", "C"));
    assertTrue(graph.containsEdge("C", "D"));
    assertTrue(graph.containsEdge("D", "A"));
  }

  @Test
  public void smallHamiltonianGraphWithRedundantEdges() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    addHamiltonianCycle(graph, "A", "B", "C", "D");
    addEdges(graph, "A", "C", "D", "B");
    new CyclicTransitiveReduction<>(graph).reduce();
    assertEquals(4, graph.vertexSet().size());
    assertEquals(4, graph.edgeSet().size());
  }

  @Test
  public void smallDAG() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    addVertices(graph, "A", "B", "C", "D");
    addEdges(graph, "A", "B", "B", "C", "B", "D");
    new CyclicTransitiveReduction<>(graph).reduce();
    assertEquals(4, graph.vertexSet().size());
    assertEquals(3, graph.edgeSet().size());
    assertTrue(graph.containsEdge("A", "B"));
    assertTrue(graph.containsEdge("B", "C"));
    assertTrue(graph.containsEdge("B", "D"));
  }

  @Test
  public void smallDAGWithRedundantEdges() {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    addVertices(graph, "A", "B", "C", "D", "E", "F");
    addEdges(graph, "A", "B", "B", "C", "B", "D", "C", "E", "D", "F", "B", "E", "B", "F", "A", "E", "A", "F");
    new CyclicTransitiveReduction<>(graph).reduce();
    assertEquals(6, graph.vertexSet().size());
    assertEquals(5, graph.edgeSet().size());
    assertTrue(graph.containsEdge("A", "B"));
    assertTrue(graph.containsEdge("B", "C"));
    assertTrue(graph.containsEdge("B", "D"));
    assertTrue(graph.containsEdge("C", "E"));
    assertTrue(graph.containsEdge("D", "F"));
  }

  @Test
  public void mediumCyclicalGraphNoSynthetic() {
    mediumCyclicalGraph(false);
  }

  @Test
  public void mediumCyclicalGraphSynthetic() {
    mediumCyclicalGraph(true);
  }

  private void mediumCyclicalGraph(final boolean allowSyntheticEdges) {
    Graph<String, DefaultEdge> graph = createEmptyGraph();
    addVertices(graph, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P");
    addEdges(graph,
      "A", "B", "B", "C", "C", "D", "D", "A", "A", "C", "D", "B", "A", "E", "B", "G", "E", "F", "F", "G",
      "G", "E", "E", "H", "F", "L", "H", "I", "I", "J", "J", "K", "K", "L", "L", "H", "H", "J", "I", "K",
      "J", "N", "K", "M", "M", "N", "N", "O", "O", "P", "P", "M", "M", "O", "N", "P", "G", "M", "C", "P"
    );
    assertEquals(16, graph.vertexSet().size());
    assertEquals(30, graph.edgeSet().size());
    new CyclicTransitiveReduction<>(graph).allowSyntheticEdges(allowSyntheticEdges).reduce();
    assertEquals(16, graph.vertexSet().size());
    assertEquals(19, graph.edgeSet().size());
  }

  @Test
  @Category(SlowTests.class)
  public void randomisedGraphsWithSCCsDifferentSizesNoSynthetic() {
    randomisedGraphsWithSCCsDifferentSizes(false);
  }

  @Test
  @Category(SlowTests.class)
  public void randomisedGraphsWithSCCsDifferentSizesSynthetic() {
    randomisedGraphsWithSCCsDifferentSizes(true);
  }

  private void randomisedGraphsWithSCCsDifferentSizes(final boolean allowSyntheticEdges) {
    Graph<String, DefaultEdge> graph;
    long totalCTRTime = 0;
    for (int sccCount = 3; sccCount <= 24; sccCount++) {
      graph = createEmptyGraph();
      int sccSize = sccCount;
      populateGraphWithSCCs(graph, sccCount, sccSize);
      // Shuffling vertices and edges helps increase test coverage
      randomiseGraph(graph);
      assertEquals(sccCount * sccSize, graph.vertexSet().size());
      assertEquals(sccCount * naturalNumberSumGauss(sccSize - 1) + (sccCount - 1) * sccSize, graph.edgeSet().size());
      long startTime = System.currentTimeMillis();
      new CyclicTransitiveReduction<>(graph).allowSyntheticEdges(allowSyntheticEdges).reduce();
      totalCTRTime += (System.currentTimeMillis() - startTime);
      assertEquals(sccCount * sccSize, graph.vertexSet().size());
      assertEquals(sccCount * sccCount + (sccCount - 1), graph.edgeSet().size());
    }
    System.out.println(
      "Total time cyclic transitive reduction (" +
        (allowSyntheticEdges ? "" : "no ") + "synthetic edges) = " +
        totalCTRTime + " ms"
    );
  }

}
