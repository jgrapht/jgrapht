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
package org.jgrapht.graph;

import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Immutable data object containing basic information about an edge in a graph: the edge itself, the source vertex and
 * the target vertex.
 * <p></p>
 * The class is useful when backing up edge information from a graph via {@link #getEdgeInfos(Graph)}, e.g. before
 * removing many or all edges and later re-inserting (a subset of) the same edges into the graph via
 * {@link Graph#addEdge(Object, Object, Object)}. This helps to preserve original edge instances instead of creating new
 * ones, avoiding potential loss of information if the corresponding edge class instance stores some additional state.
 *
 * @param <V> vertex type
 * @param <E> edge type
 */
public class EdgeInfo<V, E> {
  private final E edge;
  private final V sourceVertex;
  private final V targetVertex;

  public EdgeInfo(E edge, V sourceVertex, V targetVertex) {
    this.edge = edge;
    this.sourceVertex = sourceVertex;
    this.targetVertex = targetVertex;
  }

  public E getEdge() {
    return edge;
  }

  public V getSourceVertex() {
    return sourceVertex;
  }

  public V getTargetVertex() {
    return targetVertex;
  }

  /**
   * Gathers all edge infos for the given graph (think "backup")
   *
   * @param graph graph to back up edge infos from
   * @param <V> vertex type
   * @param <E> edge type
   * @return list of edge info instances in no particular order
   */
  public static <V, E> List<EdgeInfo<V, E>> getEdgeInfos(Graph<V, E> graph) {
    Set<E> edges = graph.edgeSet();
    List<EdgeInfo<V, E>> edgeInfos = new ArrayList<>(edges.size());
    edges.stream()
      .map(edge -> new EdgeInfo<>(edge, graph.getEdgeSource(edge), graph.getEdgeTarget(edge)))
      .forEach(edgeInfos::add);
    return edgeInfos;
  }
}
