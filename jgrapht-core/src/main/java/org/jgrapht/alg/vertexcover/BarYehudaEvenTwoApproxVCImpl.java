/*
 * (C) Copyright 2016-2016, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.interfaces.MinimumWeightedVertexCoverAlgorithm;
import org.jgrapht.alg.util.WeightedVertexSet;
import org.jgrapht.graph.UndirectedSubgraph;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the 2-opt algorithm for a minimum weighted vertex cover by
 * R. Bar-Yehuda and S. Even. A linear time approximation algorithm for the weighted vertex cover problem. J. of
 * Algorithms 2:198-203, 1981.
 * The solution is guaranteed to be within 2 times the optimum solution.
 * An easier-to-read version of this algorithm can be found here: <a href="https://www.cs.umd.edu/class/spring2011/cmsc651/vc.pdf">https://www.cs.umd.edu/class/spring2011/cmsc651/vc.pdf/a>
 *
 * Note: this class supports pseudo-graphs
 * Runtime: O(|E|)
 * This is a fast algorithm, guaranteed to give a 2-approximation. A solution of higher quality (same approximation ratio) at the expensive of a higher runtime can be obtained using
 * {@link BarYehudaEvenTwoApproxVCImpl}.
 *
 *
 * TODO: Remove the UndirectedSubgraph dependency! Querying vertex degrees on these graphs is actually slow! This does affect the runtime complexity. Better would be to just work on a clone of the original graph!
 *
 * @author Joris Kinable
 *
 */
public class BarYehudaEvenTwoApproxVCImpl<V,E> implements MinimumWeightedVertexCoverAlgorithm<V,E> {

    @Override
    public WeightedVertexSet<V> getVertexCover(UndirectedGraph<V, E> graph, Map<V, Double> vertexWeightMap) {

        Set<V> cover=new LinkedHashSet<>();
        double weight=0;
        UndirectedGraph<V,E> copy= new UndirectedSubgraph<>(graph, null, null);
        Map<V, Double> W=new HashMap<>();
        for(V v : graph.vertexSet())
            W.put(v, vertexWeightMap.get(v));

        //Main loop
        Set<E> edgeSet=copy.edgeSet();
        while(!edgeSet.isEmpty()){
            //Pick arbitrary edge
            E e =edgeSet.iterator().next();
            V p=copy.getEdgeSource(e);
            V q=copy.getEdgeTarget(e);

            if(W.get(p) <= W.get(q)){
                W.put(q, W.get(q)-W.get(p));
                cover.add(p);
                weight+=vertexWeightMap.get(p);
                copy.removeVertex(p);
            }else{
                W.put(p, W.get(p)-W.get(q));
                cover.add(q);
                weight+=vertexWeightMap.get(q);
                copy.removeVertex(q);
            }
        }

        return new WeightedVertexSet<>(cover, weight);
    }
}
