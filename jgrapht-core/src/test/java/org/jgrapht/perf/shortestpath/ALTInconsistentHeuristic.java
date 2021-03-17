/*
 * (C) Copyright 2016-2018, by Brooks Bockman and Contributors.
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
package org.jgrapht.perf.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.shortestpath.*;

import java.util.*;

/**
 * An admissible and inconsistent heuristic for the A* algorithm using a set of 
 * landmarks and the triangle inequality. Assumes that the graph contains non-negative edge 
 * weights.
 * 
 * This class is based on {@link ALTAdmissibleHeuristic} but when given multiple landmarks
 * the heuristic value for each node will be based on a random landmark instead of the
 * maximal landmark.  This does not guarantee inconsistency, but with 5 landmarks 
 * and a large graph it is likely.  This is not necessarily a good inconsistent heuristic 
 * and this class has been created primarily for testing.
 * 
 * This class also implements {@link AStarInconsistentHeuristic} in order to benefit from a BPMX
 * style heuristic update set. For more details on this algorithm refer to the aforementioned
 * interface.
 *
 * @author Brooks Bockman
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class ALTInconsistentHeuristic<V, E>
    extends
    ALTAdmissibleHeuristic<V, E>
    implements
    AStarInconsistentHeuristic<V>
{
    private final V[] keySet;
    private final Random rng;
    private Map<V, V> landmarkSelection;

    /**
     * Constructs a new {@link AStarInconsistentHeuristic} using a set of landmarks.
     *
     * @param graph The graph.
     * @param landmarks A set of vertices of the graph which will be used as landmarks.
     * @param seed Seed for random landmark selection.
     *
     * @throws IllegalArgumentException if no landmarks are provided
     * @throws IllegalArgumentException if the graph contains edges with negative weights
     */
    
    public ALTInconsistentHeuristic(Graph<V, E> graph, Set<V> landmarks, long seed)
    {
        super(graph, landmarks);
        this.rng = new Random(seed);
        landmarkSelection = new HashMap<>();
        
        @SuppressWarnings("unchecked")
        V[] temp = (V[]) fromLandmark.keySet().toArray();
        keySet = temp;
    }

    /**
     * An admissible heuristic estimate from a source vertex to a target vertex. The estimate is
     * always non-negative and never overestimates the true distance.
     *
     * @param u the source vertex
     * @param t the target vertex
     *
     * @return an admissible heuristic estimate
     */
    @Override
    public double getCostEstimate(V u, V t)
    {   
        
        double maxEstimate = 0d;

        //Special case, source equals target
        if (u.equals(t)) {
            return maxEstimate;
        }

        //Compute from landmarks
        if(!landmarkSelection.containsKey(u)) {
           int index = rng.nextInt(keySet.length);
           landmarkSelection.put(u, keySet[index]);
        }
        V l = landmarkSelection.get(u);
        
        double estimate;
        Map<V, Double> from = fromLandmark.get(l);
        if (directed) {
            Map<V, Double> to = toLandmark.get(l);
            estimate = Math.max(to.get(u) - to.get(t), from.get(t) - from.get(u));
        } else {
            estimate = Math.abs(from.get(u) - from.get(t));
        }

        // ensure no overflow 
        if (Double.isFinite(estimate)) {
            maxEstimate = Math.max(maxEstimate, estimate);
        }
        
        return maxEstimate;
    }
}
