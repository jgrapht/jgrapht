/*
 * (C) Copyright 2019-2019, by Brooks Bockman and Contributors.
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
package org.jgrapht.alg.interfaces;

import java.util.*;

import org.jgrapht.*;
/**
 * Interface for an admissible but inconsistent heuristic used in A* search. This interface is 
 * based primarily on {@link AStarAdmissibleHeuristic}, but includes additional methods to update 
 * inconsistent heuristics, helping to optimize A* search with inconsistent heuristics.  This 
 * interface is meant to implement a Bidirection Pathmax (BPMX) style update step
 * (<a href="https://www.aaai.org/Papers/AAAI/2007/AAAI07-192.pdf">Inconsistent Heuristics</a>).
 * This update essentially checks the heuristic values of a vertices neighbors to determine 
 * consistency, and if it is inconsistent the heuristic estimate is increased to the lowest value 
 * that would provide consistency. Note, this BPMX approach does not fully convert a inconsistent 
 * heuristic into a consistent one.
 * 
 * <p>
 * Given two nodes $i$, $j$, a heuristic is said to to be inconsistent if $|h(i)-h(j)| &gt; dist(i,j)$.
 * This is similar to the definition of admissibility where $h(i) &lt;= dist(i, tar)$ must hold.  That 
 * is an admissible heuristic displays consistancy between any given node and the target.  While a 
 * consistent heuristic displays consistancy between any pair of two arbitrary nodes.
 * 
 * For a basic implementation of this interface refer to the test repository
 * class ALTInconsistentHeuristic.
 *
 * @param <V> vertex type
 * @author Brooks Bockman 
 */
public interface AStarInconsistentHeuristic<V>
    extends
    AStarAdmissibleHeuristic<V>
{
    /**
     * Updates the heuristic estimate for the node being expanded by A* search.
     * 
     * When a node is expanded its heuristic is checked relative to its ancestors.
     * If the current node heuristic is too low to be consistent based on this, the
     * value is increased, reducing the number of total node expansions required.
     * 
     * Note that the updated heuristic must be placed into the hScoreMap for future use.
     *
     * @param graph the graph that is being searched
     * @param <E> graph edge type
     * @param expandedVertex the vertex being expanded
     * @param targetVertex the target vertex in the search
     * @param hScoreMap mapping for current heuristic values
     * @return updated value for the heuristic for the expandedVertex
     */
    default <E> double updateExpandedHeuristic(Graph<V, E> graph, V expandedVertex, V targetVertex, Map<V, Double> hScoreMap)
    {
        
        Set<E> incomingEdges = graph.incomingEdgesOf(expandedVertex);
        
        double HighH = 0d;
        
        for (E edge : incomingEdges) {
            V parent = Graphs.getOppositeVertex(graph, edge, expandedVertex);
            if (expandedVertex.equals(parent)) //ignore self-loops
                continue;
            
            if (!hScoreMap.containsKey(parent)) {
                hScoreMap.put(parent, getCostEstimate(parent, targetVertex));
            }
            
            HighH = Math.max(HighH, hScoreMap.get(parent) - graph.getEdgeWeight(edge));
        }
        
        if (!hScoreMap.containsKey(expandedVertex)) {
            hScoreMap.put(expandedVertex, getCostEstimate(expandedVertex, targetVertex));
        }
        
        HighH = Math.max(HighH, hScoreMap.get(expandedVertex));
        hScoreMap.put(expandedVertex, HighH);
        
        return HighH;
    }
    
    /**
     * Updates the heuristic estimate for a successor node during node expansion
     * in A* search.
     * 
     * When an expanded nodes successors heuristics are looked at, they  can
     * have their value increased if their heuristic is too low to be consistent
     * with the expanded node.  This helps reduce the total number of node
     * expansions.
     * 
     * Note that the updated heuristic must be placed into the hScoreMap for future use;
     * 
     * @param successorVertex the successor vertex being looked at
     * @param targetVertex the target vertex in the search
     * @param hParent heuristic value of the parent vertex
     * @param edgeWeight edge weight between parent and successor vertices
     * @param hScoreMap mapping for current heuristic values
     * 
     * @return returns true if the heuristic was successfully changed
     */
    default boolean updateSuccessorHeuristic(V successorVertex, V targetVertex, double hParent, double edgeWeight, Map<V, Double> hScoreMap)
    {
        // If H is not consistent, attempt to "pull up" successor H value 
        if (!hScoreMap.containsKey(successorVertex)){
            hScoreMap.put(successorVertex, 
                getCostEstimate(successorVertex, targetVertex));
        }
        if (hScoreMap.get(successorVertex) < hParent - edgeWeight) {
            hScoreMap.put(successorVertex, hParent - edgeWeight);
            return true;
        }
        return false;
    }
    
    /**
     * It is assumed that this type will only be used with inconsistent heuristics.
     * If a consistent or undetermined heuristic is to be used, an implementation of 
     * {@link AStarAdmissibleHeuristic} is advised. 
     *
     * @param graph graph to test heuristic on
     * @param <E> graph edges type
     * @return false by assumption
     */
    default <E> boolean isConsistent(Graph<V, E> graph)
    {
        return false;
    }
}
