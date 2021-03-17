/*
 * (C) Copyright 2015-2021, by Joris Kinable, Jon Robison, Thomas Breitbart and Contributors.
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
import org.jheaps.*;
import org.jheaps.tree.*;

import java.util.*;
import java.util.function.*;

/**
 * A* shortest path.
 * <p>
 * An implementation of <a href="http://en.wikipedia.org/wiki/A*_search_algorithm">A* shortest path
 * algorithm</a>. This class works for directed and undirected graphs, as well as multi-graphs and
 * mixed-graphs. The graph can also change between invocations of the
 * {@link #getPath(Object, Object)} method; no new instance of this class has to be created. The
 * heuristic is implemented using a PairingHeap data structure by default to maintain the set of
 * open nodes. However, there still exist several approaches in literature to improve the
 * performance of this heuristic which one could consider to implement. Custom heap implementation
 * can be specified during the construction time. Another issue to take into consideration is the
 * following: given two candidate nodes, $i$, $j$ to expand, where $f(i)=f(j)$, $g(i)$ &gt; $g(j)$,
 * $h(i)$ &lt; $g(j)$, $f(i)=g(i)+h(i)$, $g(i)$ is the actual distance from the source node to $i$,
 * $h(i)$ is the estimated distance from $i$ to the target node. Usually a depth-first search is
 * desired, so ideally we would expand node $i$ first. Using the PairingHeap, this is not
 * necessarily the case though. This could be improved in a later version.
 *
 * <p>
 * Note: This implementation works with both consistent and inconsistent admissible heuristics. 
 * Given two nodes $i$, $j$, a heuristic is said to to be inconsistent if $|h(i)-h(j)| &gt; dist(i,j)$.
 * This is similar to the definition of admissibility where $h(i) &lt;= dist(i, tar)$ must hold.  That 
 * is an admissible heuristic displays consistancy between any given node and the target.  While a 
 * consistent heuristic displays consistancy between any pair of two arbitrary nodes.
 *
 * <p>
 * For most classes of graphs a consistent heuristic will be most appropriate, and is the industry
 * standard.  There are however a few situations when an inconsistent heuristic can perform better
 * in practice.  These situations usually arise when there are a number of different heuristics to 
 * choose from and various nodes are better suited to different instances of these heuristics.  
 * Even in this case, multiple heuristics can be combined in a consisntent fashion, by always using 
 * the maximum value amongst all heuristics.  When the best heuristic choice is unknown this tends to 
 * be the situation in practice when an inconsistent heurstic becomes preferable, as it allows one to 
 * combine various heuristics in an arbitrary manner.
 *
 *<p>
 * The user can choose to use the standard A* implementation by passing to the constructor any 
 * {@link AStarAdmissibleHeuristic} implementation that is not an instance of 
 * {@link AStarInconsistentHeuristic}.  If an inconsistent heuristic is to be used
 * the user can elect to use the inconsistent optimizations by passing in an instance of
 * {@link AStarInconsistentHeuristic} instead.  This optimization uses a Bidirectional Pathmax (BPMX) 
 * style algorithm (<a href="https://www.aaai.org/Papers/AAAI/2007/AAAI07-192.pdf">Inconsistent Heuristics</a>).
 * Standard A* when supplied with an inconsistent heuristic can result in a worst case exponential
 * number of node expansions.  This BPMX optimization reduces this worst case to quadratic.
 * This interface should however not be used with consistent heuristics as it adds additional
 * computational overhead.  It is advised to start with the standard herustic interface and
 * move to the optimization only where excessive node expansions are causing noteable performance 
 * issues.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Joris Kinable
 * @author Jon Robison
 * @author Thomas Breitbart
 * @author Brooks Bockman
 */
public class AStarShortestPath<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{
    // Supplier of the preferable heap implementation
    protected final Supplier<AddressableHeap<Double, V>> heapSupplier;
    // List of open nodes
    protected AddressableHeap<Double, V> openList;
    protected Map<V, AddressableHeap.Handle<Double, V>> vertexToHeapNodeMap;

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
    
    // Mapping for updated heuristic values
    protected Map<V, Double> hScoreMap;
    
    // Flag whether or not to use inconsistent optimization
    protected boolean inconsistent;

    /**
     * Create a new instance of the A* shortest path algorithm.
     *
     * @param graph the input graph
     * @param admissibleHeuristic admissible heuristic which estimates the distance from a node to
     *        the target node. The heuristic must never overestimate the distance.
     */
    public AStarShortestPath(Graph<V, E> graph, AStarAdmissibleHeuristic<V> admissibleHeuristic)
    {
        this(graph, admissibleHeuristic, PairingHeap::new);
    }

    /**
     * Create a new instance of the A* shortest path algorithm.
     *
     * @param graph the input graph
     * @param admissibleHeuristic admissible heuristic which estimates the distance from a node to
     *        the target node. The heuristic must never overestimate the distance.
     * @param heapSupplier supplier of the preferable heap implementation
     */
    public AStarShortestPath(
        Graph<V, E> graph, AStarAdmissibleHeuristic<V> admissibleHeuristic,
        Supplier<AddressableHeap<Double, V>> heapSupplier)
    {
        super(graph);
        this.admissibleHeuristic =
            Objects.requireNonNull(admissibleHeuristic, "Heuristic function cannot be null!");
        this.comparator = new ToleranceDoubleComparator();
        this.heapSupplier = Objects.requireNonNull(heapSupplier, "Heap supplier cannot be null!");
        
        //flag to decide whether to use inconsistent optimization
        inconsistent = admissibleHeuristic instanceof AStarInconsistentHeuristic;
    }

    /**
     * Initializes the data structures.
     *
     * @param admissibleHeuristic admissible heuristic
     */
    private void initialize(AStarAdmissibleHeuristic<V> admissibleHeuristic)
    {
        this.admissibleHeuristic = admissibleHeuristic;
        openList = heapSupplier.get();
        vertexToHeapNodeMap = new HashMap<>();
        closedList = new HashSet<>();
        gScoreMap = new HashMap<>();
        hScoreMap = new HashMap<>();
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
        AddressableHeap.Handle<Double, V> heapNode = openList.insert(0.0, sourceVertex);
        vertexToHeapNodeMap.put(sourceVertex, heapNode);

        do {
            AddressableHeap.Handle<Double, V> currentNode = openList.deleteMin();

            // Check whether we reached the target vertex
            if (currentNode.getValue().equals(targetVertex)) {
                // Build the path
                return this.buildGraphPath(sourceVertex, targetVertex, currentNode.getKey());
            }

            // We haven't reached the target vertex yet; expand the node
            expandNode(currentNode, targetVertex);
            closedList.add(currentNode.getValue());
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
    
    /** Expand node, update cost estimates, and  add neighbors to heap where appropriate
     * 
     * @param currentNode Node being expanded.
     * @param endVertex Target node.
     */
    private void expandNode(AddressableHeap.Handle<Double, V> currentNode, V endVertex)
    {
        numberOfExpandedNodes++;
        
        V currentVertex = currentNode.getValue();

        // If if using inconsistent heuristic "pull-up" parent h value 
        double hParent = 0d;
        if (inconsistent) {
            hParent = ((AStarInconsistentHeuristic<V>) admissibleHeuristic)
                .updateExpandedHeuristic(graph, currentVertex, endVertex, hScoreMap);
        }
        
        Set<E> outgoingEdges = graph.outgoingEdgesOf(currentVertex);
        
        for (E edge : outgoingEdges) {
            V successor = Graphs.getOppositeVertex(graph, edge, currentVertex);

            if (successor.equals(currentVertex)) // Ignore self-loop
                continue;

            double edgeWeight = graph.getEdgeWeight(edge);
            double tentativeGScore = gScoreMap.get(currentVertex) + edgeWeight;
            double fScore;
            boolean improvedH = false;
            
            // If using inconsistent heuristic "pull up" successor h value 
            if (inconsistent) {
                improvedH = ((AStarInconsistentHeuristic<V>) admissibleHeuristic)
                    .updateSuccessorHeuristic(successor, endVertex, hParent, edgeWeight, hScoreMap);
                fScore = tentativeGScore + hScoreMap.get(successor);
            }else {
                fScore = tentativeGScore + admissibleHeuristic.getCostEstimate(successor, endVertex); 
            }

            if (vertexToHeapNodeMap.containsKey(successor)) { // We re-encountered a vertex. It's
                // either in the open or closed list.
                boolean improvedG = false;
                if (tentativeGScore < gScoreMap.get(successor)) { // If this is improving, update
                    cameFrom.put(successor, edge);
                    gScoreMap.put(successor, tentativeGScore);
                    improvedG = true;
                }

                if (closedList.contains(successor)) { // it's in the closed list. Move node back to
                    // open list if we discovered a shorter path to this node
                    if (improvedG) {
                        closedList.remove(successor);
                        vertexToHeapNodeMap.put(successor, openList.insert(fScore, successor));
                    }

                } else { // It's in the open list, if H or G improved update its heap key
                    if (improvedH || improvedG) {
                        AddressableHeap.Handle<Double, V> node = vertexToHeapNodeMap.get(successor);
                        fScore = fScore - tentativeGScore + gScoreMap.get(successor);
                        if (node.getKey() < fScore) {
                            node.delete();
                            vertexToHeapNodeMap.put(successor, openList.insert(fScore, successor));
                        } else {
                            node.decreaseKey(fScore);
                        }
                    }
                }

            } else { // We've encountered a new vertex.
                cameFrom.put(successor, edge);
                gScoreMap.put(successor, tentativeGScore);
                vertexToHeapNodeMap.put(successor, openList.insert(fScore, successor));
            }
        }
    }

    /**
     * Builds the graph path
     *
     * @param startVertex starting vertex of the path
     * @param targetVertex ending vertex of the path
     * @param pathLength length of the path
     * @return the shortest path from startVertex to endVertex
     */
    protected GraphPath<V, E> buildGraphPath(V startVertex, V targetVertex, double pathLength)
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
