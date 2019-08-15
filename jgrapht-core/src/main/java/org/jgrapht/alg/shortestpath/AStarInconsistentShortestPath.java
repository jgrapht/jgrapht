/*
 * (C) Copyright 2015-2018, by Brooks Bockman, Joris Kinable, Jon Robison and Contributors.
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
import org.jheaps.*;

import java.util.*;
import java.util.function.*;

/**
 * A* shortest path for inconsistent heuristics.
 * <p>
 * This class is an extension of {@link AStarShortestPath}, for the case of consistent heuristics
 * this algorithm reduces to exactly that of its parent.  When heuristics are not consistent, this
 * class uses a first order bidirectional pathmax to update heuristics as described in 
 * <a href="https://www.ijcai.org/Proceedings/09/Papers/111.pdf"> 
 * A* Search with Inconsistent Heuristics</a>.
 * 
 * <p>
 * In the case of directed graphs, this algorithm first updates $h(i)$ for the node being expanded 
 * using incoming edges, then flows this value to its successors.  Alternative approaches for 
 * inconsistent heuristics do exist in literature and may be better for certain classes of graphs. 
 * If the heuristic is known to be consistent, using {@link AStarShortestPath} is advised, as it 
 * has less overhead in checking heuristic values. 
 * 
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Joris Kinable
 * @author Jon Robison
 * @author Thomas Breitbart
 * @author Brooks Bockman
 */
public class AStarInconsistentShortestPath<V, E>
    extends
    AStarShortestPath<V, E>
{
    // Mapping for updated heuristic values
    private Map<V, Double> hScoreMap;
    
    /**
     * Create a new instance of the A* shortest path algorithm for inconsistent heuristics.
     *
     * @param graph the input graph
     * @param admissibleHeuristic admissible heuristic which estimates the distance from a node to
     *        the target node. The heuristic must never overestimate the distance.
     */
    public AStarInconsistentShortestPath(Graph<V, E> graph, AStarAdmissibleHeuristic<V> admissibleHeuristic)
    {
        super(graph, admissibleHeuristic);
    }

    /**
     * Create a new instance of the A* shortest path algorithm for inconsistent heuristics.
     *
     * @param graph the input graph
     * @param admissibleHeuristic admissible heuristic which estimates the distance from a node to
     *        the target node. The heuristic must never overestimate the distance.
     * @param heapSupplier supplier of the preferable heap implementation
     */
    public AStarInconsistentShortestPath(
        Graph<V, E> graph, AStarAdmissibleHeuristic<V> admissibleHeuristic,
        Supplier<AddressableHeap<Double, V>> heapSupplier)
    {
        super(graph, admissibleHeuristic, heapSupplier);
    }

    /**
     * Initializes the data structures.
     *
     * @param admissibleHeuristic admissible heuristic
     */
    @Override
    protected void initialize(AStarAdmissibleHeuristic<V> admissibleHeuristic)
    {
        hScoreMap = new HashMap<>();
        super.initialize(admissibleHeuristic);
    }
    
    @Override
    /** Expand node, update cost estimates, and  add neighbors to heap where appropriate
     * 
     * @param currentNode Node being expanded.
     * @param endVertex Target node.
     */
    protected void expandNode(AddressableHeap.Handle<Double, V> currentNode, V endVertex)
    {
        numberOfExpandedNodes++;
        
        V currentVertex = currentNode.getValue();

        // If H is too small to be consistent, "pull up" its value to the largest minimum
        double HighH = checkAncestorHeuristics(currentVertex, endVertex);
        
        // Continue with semi-normal A* node expansion
        Set<E> outgoingEdges = graph.outgoingEdgesOf(currentVertex);
        
        for (E edge : outgoingEdges) {
            V successor = Graphs.getOppositeVertex(graph, edge, currentVertex);

            if (successor.equals(currentVertex)) // Ignore self-loop
                continue;

            double tentativeGScore = gScoreMap.get(currentVertex) + graph.getEdgeWeight(edge);
            double fScore;
            boolean improvedH = false;
            
            // If H is not consistent, attempt to "pull up" successor H value 
            if (!hScoreMap.containsKey(successor)){
                hScoreMap.put(successor, 
                    admissibleHeuristic.getCostEstimate(successor, endVertex));
            }
            if (hScoreMap.get(successor) < HighH - graph.getEdgeWeight(edge)) {
                hScoreMap.put(successor, HighH - graph.getEdgeWeight(edge));
                improvedH = true;
            }
            fScore = tentativeGScore + hScoreMap.get(successor);

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
     * Look at neighbor vertex heuristic values and determine a lower bound for consistency.
     * This allows us to increase the heuristic, yielding better performance.
     * 
     * @param currentVertex The node being expanded.
     * @param endVertex The target node.
     * @return Updated heuristic value for the current vertex.
     */
    private double checkAncestorHeuristics(V currentVertex, V endVertex) 
    {
        Set<E> incomingEdges = graph.incomingEdgesOf(currentVertex);
        
        double HighH = 0;
        
        for (E edge : incomingEdges) {
            V parent = Graphs.getOppositeVertex(graph, edge, currentVertex);
            if (currentVertex.equals(parent)) //ignore self-loops
                continue;
            
            if (!hScoreMap.containsKey(parent)) {
                hScoreMap.put(parent, admissibleHeuristic.getCostEstimate(parent, endVertex));
            }
            
            HighH = Math.max(HighH, hScoreMap.get(parent) - graph.getEdgeWeight(edge));
        }
        
        if (!hScoreMap.containsKey(currentVertex)) {
            hScoreMap.put(currentVertex, 
                admissibleHeuristic.getCostEstimate(currentVertex, endVertex));
        }
        
        HighH = Math.max(HighH, hScoreMap.get(currentVertex));
        hScoreMap.put(currentVertex, HighH);
        
        return HighH;
    }
}
