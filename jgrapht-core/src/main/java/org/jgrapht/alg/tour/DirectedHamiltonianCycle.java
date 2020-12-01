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
package org.jgrapht.alg.tour;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple algorithm for computing Hamiltonian cycles in directed graphs as described in
 * <a href="https://youtu.be/dQr4wZCiJJ4">this YouTube video</a>. The algorithm's pseudo code can be seen starting from
 * <a href="https://youtu.be/dQr4wZCiJJ4?t=348">time index 5:48</a>. Runtime grows exponentially(!) with the number of
 * vertices in the graph.
 * <p></p>
 * A Hamiltonian cycle, also called a Hamiltonian circuit, Hamilton cycle, or Hamilton circuit, is a
 * graph cycle (i.e., closed loop) through a graph that visits each node exactly once (Skiena 1990,
 * p. 196).
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Alexander Kriegisch
 * @see <a href="https://en.wikipedia.org/wiki/Hamiltonian_path">Wikipedia</a> for a definition of Hamiltonian path and
 * Hamiltonian cycle
 */
public class DirectedHamiltonianCycle<V, E> extends HamiltonianCycleAlgorithmBase<V, E> {
  private Graph<V, E> graph;
  private boolean cycleFound;
  private List<V> vertices;
  private int vertexCount;
  private int maxIndex;
  private int[] tourIndices;

  // Implementation note: For whatever reason, int[] outperforms boolean[] for test graphs with several 100 vertices
  // and several 1,000 edges. It outperforms BitSet even more dramatically. This might change for very large graphs.
  // Background info about boolean[] vs. BitSet: https://www.baeldung.com/java-boolean-array-bitset-performance
  private int[] adjacencyMatrix;

  /**
   * Computes a Hamiltonian tour
   *
   * @param graph input graph which must be directed, unweighted, have 3+ vertices, not allow self-loops and not allow
   *              multiple edges
   * @return Hamiltonian cycle (HC) if the graph is strongly connected, null otherwise. The HC is represented as a
   * {@code GraphPath} in which the first and last vertices are identical, i.e. size is number of graph vertices + 1.
   * @throws IllegalArgumentException if graph is {@code null}
   * @throws IllegalArgumentException if graph is undirected
   * @throws IllegalArgumentException if graph contains &lt; 3 vertices (cycle impossible)
   * @throws IllegalArgumentException if graph allows self-loops
   * @throws IllegalArgumentException if graph is weighted
   * @throws IllegalArgumentException if graph allows multiple edges between two vertices
   */
  @Override
  public GraphPath<V, E> getTour(Graph<V, E> graph) {
    this.graph = graph;
    checkGraph();
    // If graph is not strongly connected, there cannot be a Hamiltonian cycle
    if (!new KosarajuStrongConnectivityInspector<>(graph).isStronglyConnected())
      return null;
    cycleFound = false;
    // Create ordered vertex list with indices identical to the adjacency matrix
    vertices = new ArrayList<>(graph.vertexSet());
    vertexCount = vertices.size();
    maxIndex = vertexCount - 1;
    // Initialise tour with [0, -1, -1, ..., -1], i.e. first vertex is fixed as the starting point
    tourIndices = new int[vertexCount];
    Arrays.fill(tourIndices, 1, maxIndex, -1);

    // Create adjacency matrix to operate upon (called G in original algorithm).
    adjacencyMatrix = new int[vertexCount * vertexCount];
    for (final E edge : graph.edgeSet()) {
      final int sourceIndex = vertices.indexOf(graph.getEdgeSource(edge));
      final int targetIndex = vertices.indexOf(graph.getEdgeTarget(edge));
      adjacencyMatrix[sourceIndex * vertexCount + targetIndex] = 1;
    }

    // Calculate Hamiltonian cycle, starting at index 1 (0 is fixed)
    hamiltonian(1);

    if (!cycleFound)
      throw new RuntimeException("No Hamiltonian cycle (HC) found. " +
        "This should never happen and indicates an error in the algorithm," +
        "because the graph is strongly connected and a HC must therefore exist."
      );
    return toGraphPath();
  }

  /**
   * Checks that the graph is directed, unweighted, has 3+ vertices, does not allow self-loops and does not allow
   * multiple edges
   *
   * @throws IllegalArgumentException if graph is {@code null}
   * @throws IllegalArgumentException if graph is undirected
   * @throws IllegalArgumentException if graph contains < 3 vertices (cycle impossible)
   * @throws IllegalArgumentException if graph allows self-loops
   * @throws IllegalArgumentException if graph is weighted
   * @throws IllegalArgumentException if graph allows multiple edges between two vertices
   */
  private void checkGraph() {
    if (graph == null)
      throw new IllegalArgumentException("Graph must not be null");
    GraphTests.requireDirected(graph, "Graph must be directed");
    if (graph.vertexSet().size() < 3)
      throw new IllegalArgumentException("Directed graph must have >= 3 vertices for a cycle");
    if (graph.getType().isAllowingSelfLoops())
      throw new IllegalArgumentException("Graph must not allow self-loops");
    if (graph.getType().isWeighted())
      throw new IllegalArgumentException("Graph must be unweighted");
    if (graph.getType().isAllowingMultipleEdges())
      throw new IllegalArgumentException("Graph must not allow multiple edges");
  }

  /**
   * Code from the YouTube video to recursively calculate the Hamiltonian cycle, adjusted to work for array indices
   * starting at 0 instead of 1 and returning immediately if one Hamiltonian cycle is found instead of calculating and
   * printing all of them. Variables have been renamed for better readability.
   *
   * @param currentIndex current index
   */
  private void hamiltonian(final int currentIndex) {
    do {
      nextVertex(currentIndex);
      // If dead end found, track back
      if (tourIndices[currentIndex] < 0)
        return;
      // If cycle found, set flag. Otherwise, recurse into next tour step.
      if (currentIndex == maxIndex)
        cycleFound = true;
      else
        hamiltonian(currentIndex + 1);
    } while (!cycleFound);
  }

  /**
   * Code from the YouTube video to find the next vertex, adjusted to work for array indices starting at 0 instead of 1.
   * Variables have been renamed for better readability.
   *
   * @param currentIndex current index
   */
  private void nextVertex(final int currentIndex) {
    do {
      // Select the next untried index
      tourIndices[currentIndex]++;
      // Cannot increment currentIndex beyond maxIndex -> dead end, no tour found
      if (tourIndices[currentIndex] > maxIndex) {
        tourIndices[currentIndex] = -1;
        return;
      }
      // Is there an edge from the predecessor vertex to the current one?
      if (adjacencyMatrix[tourIndices[currentIndex - 1] * vertexCount + tourIndices[currentIndex]] != 0) {
        int predecessorIndex;
        // Check if the current vertex is already part of the tour
        for (predecessorIndex = 0; predecessorIndex < currentIndex; predecessorIndex++) {
          if (tourIndices[predecessorIndex] == tourIndices[currentIndex])
            break;
        }
        // If the current vertex appears for the first time in the tour, we found our next tour step
        if (predecessorIndex == currentIndex) {
          // Tour still incomplete? -> Return to caller to let it continue the search
          if (currentIndex < maxIndex)
            return;
            // Tour complete and cycle closed? -> Return to caller to let it return the result
          else if (adjacencyMatrix[tourIndices[currentIndex] * vertexCount + tourIndices[0]] != 0)
            return;
        }
      }
    } while (true);
  }

  /**
   * @return Hamiltonian cycle representation in which the first and last vertex are identical, i.e. size is number of
   * graph vertices + 1
   */
  private GraphPath<V, E> toGraphPath() {
    List<V> tour = new ArrayList<>(vertices.size());
    for (int tourIndex : tourIndices)
      tour.add(vertices.get(tourIndex));
    return vertexListToTour(tour, graph);
  }
}
