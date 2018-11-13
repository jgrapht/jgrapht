/*
 * (C) Copyright 2015-2018, by Joris Kinable, Jon Robison, Thomas Breitbart and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * A* shortest path.
 * 
 * An implementation of <a href="http://en.wikipedia.org/wiki/A*_search_algorithm">A* shortest path
 * algorithm</a>. This class works for directed and undirected graphs, as well as multi-graphs and
 * mixed-graphs. The graph can also change between invocations of the
 * {@link #getPath(Object, Object)} method; no new instance of this class has to be created. The
 * heuristic is implemented using a FibonacciHeap data structure to maintain the set of open nodes.
 * However, there still exist several approaches in literature to improve the performance of this
 * heuristic which one could consider to implement. Another issue to take into consideration is the
 * following: given two candidate nodes, $i$, $j$ to expand, where $f(i)=f(j)$, $g(i)$ &gt; $g(j)$,
 * $h(i)$ &lt; $g(j)$, $f(i)=g(i)+h(i)$, $g(i)$ is the actual distance from the source node to $i$,
 * $h(i)$ is the estimated distance from $i$ to the target node. Usually a depth-first search is
 * desired, so ideally we would expand node $i$ first. Using the FibonacciHeap, this is not
 * necessarily the case though. This could be improved in a later version.
 * 
 * <p>
 * Note: This implementation works with both consistent and inconsistent admissible heuristics. For
 * details on consistency, refer to the description of the method
 * {@link #isConsistentHeuristic(AStarAdmissibleHeuristic)}. However, this class is <i>not</i>
 * optimized for inconsistent heuristics. Several opportunities to improve both worst case and
 * average runtime complexities for A* with inconsistent heuristics described in literature can be
 * used to improve this implementation!
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 * @author Jon Robison
 * @author Thomas Breitbart
 */
public class AStarShortestPath<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{
    // List of open nodes
    protected FibonacciHeap<V> openList;
    protected Map<V, FibonacciHeapNode<V>> vertexToHeapNodeMap;

    // List of closed nodes
    protected Set<V> closedList;

    // Mapping of nodes to their g-scores (g(x)).
    protected Map<V, Double> gScoreMap;

    // Predecessor map: mapping of a node to an edge that leads to its
    // predecessor on its shortest path towards the targetVertex
    protected Map<V, E> cameFrom;

    // Reference to the admissible heuristic
    protected AStarAdmissibleHeuristic<V> admissibleHeuristic;

    // Counter which keeps track of the number of expanded nodes
    protected int numberOfExpandedNodes;

    // Comparator for comparing doubles with tolerance
    protected Comparator<Double> comparator;

    /**
     * Create a new instance of the A* shortest path algorithm.
     * 
     * @param graph the input graph
     * @param admissibleHeuristic admissible heuristic which estimates the distance from a node to
     *        the target node. The heuristic must never overestimate the distance.
     */
    public AStarShortestPath(Graph<V, E> graph, AStarAdmissibleHeuristic<V> admissibleHeuristic)
    {
        super(graph);
        this.admissibleHeuristic =
            Objects.requireNonNull(admissibleHeuristic, "Heuristic function cannot be null!");
        this.comparator = new ToleranceDoubleComparator();
    }

    /**
     * Initializes the data structures
     *
     * @param admissibleHeuristic admissible heuristic
     */
    private void initialize(AStarAdmissibleHeuristic<V> admissibleHeuristic)
    {
        this.admissibleHeuristic = admissibleHeuristic;
        openList = new FibonacciHeap<>();
        vertexToHeapNodeMap = new HashMap<>();
        closedList = new HashSet<>();
        gScoreMap = new HashMap<>();
        cameFrom = new HashMap<>();
        numberOfExpandedNodes = 0;
    }

    /**
     * Calculates (and returns) the shortest path from the sourceVertex to the targetVertex. Note:
     * each time you invoke this method, the path gets recomputed.
     *
     * @param sourceVertex source vertex
     * @param targetVertex target vertex
     * @return the shortest path from sourceVertex to targetVertex
     */
    @Override
    public GraphPath<V, E> getPath(V sourceVertex, V targetVertex)
    {
        if (!graph.containsVertex(sourceVertex) || !graph.containsVertex(targetVertex)) {
            throw new IllegalArgumentException(
                "Source or target vertex not contained in the graph!");
        }

        if (sourceVertex.equals(targetVertex)) {
            return createEmptyPath(sourceVertex, targetVertex);
        }

        this.initialize(admissibleHeuristic);
        gScoreMap.put(sourceVertex, 0.0);
        FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<>(sourceVertex);
        openList.insert(heapNode, 0.0);
        vertexToHeapNodeMap.put(sourceVertex, heapNode);

        do {
            FibonacciHeapNode<V> currentNode = openList.removeMin();

            // Check whether we reached the target vertex
            if (currentNode.getData().equals(targetVertex)) {
                // Build the path
                return this.buildGraphPath(sourceVertex, targetVertex, currentNode.getKey());
            }

            // We haven't reached the target vertex yet; expand the node
            expandNode(currentNode, targetVertex);
            closedList.add(currentNode.getData());
        } while (!openList.isEmpty());

        // No path exists from sourceVertex to TargetVertex
        return createEmptyPath(sourceVertex, targetVertex);
    }

    /**
     * Returns how many nodes have been expanded in the A* search procedure in its last invocation.
     * A node is expanded if it is removed from the open list.
     *
     * @return number of expanded nodes
     */
    public int getNumberOfExpandedNodes()
    {
        return numberOfExpandedNodes;
    }

    /**
     * Returns true if the provided heuristic is a <i>consistent</i> or <i>monotone</i> heuristic
     * wrt the graph provided at construction time. A heuristic is monotonic if its estimate is
     * always less than or equal to the estimated distance from any neighboring vertex to the goal,
     * plus the step cost of reaching that neighbor. For details, refer to <a href=
     * "https://en.wikipedia.org/wiki/Consistent_heuristic">https://en.wikipedia.org/wiki/Consistent_heuristic</a>.
     * In short, a heuristic is consistent iff <code>h(u)&le; d(u,v)+h(v)</code>, for every edge
     * $(u,v)$, where $d(u,v)$ is the weight of edge $(u,v)$ and $h(u)$ is the estimated cost to
     * reach the target node from vertex u. Most natural admissible heuristics, such as Manhattan or
     * Euclidean distance, are consistent heuristics.
     * 
     * @param admissibleHeuristic admissible heuristic
     * @return true is the heuristic is consistent, false otherwise
     */
    public boolean isConsistentHeuristic(AStarAdmissibleHeuristic<V> admissibleHeuristic)
    {
        for (V targetVertex : graph.vertexSet()) {
            for (E e : graph.edgeSet()) {
                double weight = graph.getEdgeWeight(e);
                V edgeSource = graph.getEdgeSource(e);
                V edgeTarget = graph.getEdgeTarget(e);
                double h_x = admissibleHeuristic.getCostEstimate(edgeSource, targetVertex);
                double h_y = admissibleHeuristic.getCostEstimate(edgeTarget, targetVertex);
                if (h_x > weight + h_y)
                    return false;
            }
        }
        return true;
    }

    private void expandNode(FibonacciHeapNode<V> currentNode, V endVertex)
    {
        numberOfExpandedNodes++;

        Set<E> outgoingEdges = graph.outgoingEdgesOf(currentNode.getData());

        for (E edge : outgoingEdges) {
            V successor = Graphs.getOppositeVertex(graph, edge, currentNode.getData());

            if (successor.equals(currentNode.getData())) { // Ignore self-loop
                continue;
            }

            double gScore_current = gScoreMap.get(currentNode.getData());
            double tentativeGScore = gScore_current + graph.getEdgeWeight(edge);
            double fScore =
                tentativeGScore + admissibleHeuristic.getCostEstimate(successor, endVertex);

            if (vertexToHeapNodeMap.containsKey(successor)) { // We re-encountered a vertex. It's
                                                              // either in the open or closed list.
                if (tentativeGScore >= gScoreMap.get(successor)) // Ignore path since it is
                                                                 // non-improving
                    continue;

                cameFrom.put(successor, edge);
                gScoreMap.put(successor, tentativeGScore);

                if (closedList.contains(successor)) { // it's in the closed list. Move node back to
                                                      // open list, since we discovered a shorter
                                                      // path to this node
                    closedList.remove(successor);
                    openList.insert(vertexToHeapNodeMap.get(successor), fScore);
                } else { // It's in the open list
                    openList.decreaseKey(vertexToHeapNodeMap.get(successor), fScore);
                }
            } else { // We've encountered a new vertex.
                cameFrom.put(successor, edge);
                gScoreMap.put(successor, tentativeGScore);
                FibonacciHeapNode<V> heapNode = new FibonacciHeapNode<>(successor);
                openList.insert(heapNode, fScore);
                vertexToHeapNodeMap.put(successor, heapNode);
            }
        }
    }

    /**
     * Builds the graph path
     *
     * @param startVertex starting vertex of the path
     * @param targetVertex ending vertex of the path
     * @param pathLength length of the path
     *
     * @return the shortest path from startVertex to endVertex
     */
    private GraphPath<V, E> buildGraphPath(V startVertex, V targetVertex, double pathLength)
    {
        List<E> edgeList = new ArrayList<>();
        List<V> vertexList = new ArrayList<>();
        vertexList.add(targetVertex);

        V v = targetVertex;
        while (!v.equals(startVertex)) {
            edgeList.add(cameFrom.get(v));
            v = Graphs.getOppositeVertex(graph, cameFrom.get(v), v);
            vertexList.add(v);
        }
        Collections.reverse(edgeList);
        Collections.reverse(vertexList);
        return new GraphWalk<>(graph, startVertex, targetVertex, vertexList, edgeList, pathLength);
    }

}
