/*
 * (C) Copyright 2018-2018, by Ira Justus Fesefeldt and Contributors.
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
package org.jgrapht.alg.decomposition;


import java.util.*;
import java.util.Map.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * Tests for the {@link NiceTreeDecompositionBase}
 *
 * @author Ira Justus Fesefeldt (PhoenixIra)
 */
public final class NiceTreeDecompositionTestUtil
{
    //static class
    private NiceTreeDecompositionTestUtil() {}
    
    /**
     * Tests whether the tree decomposition, consisting of the {@code decomposition} and the {@code map} 
     * with the given {@code root} is nice.
     * 
     * @param decomposition the tree decomposition to check
     * @param map the map of the tree decomposition
     * @param root the root of the tree decomposition
     */
    public static <V,E,W> boolean isNiceDecomposition(Graph<V,E> decomposition, Map<V,Set<W>> map, V root){
        
        //empty graph
        if(decomposition.vertexSet().isEmpty())
            return true;
        
        Queue<V> queue = new ArrayDeque<>();
        
        //test and add root
        if(map.get(root).size() != 1)
            return false;
       queue.add(root);
        
        while(!queue.isEmpty())
        {
            V current = queue.poll();
            List<V> successor = Graphs.successorListOf(decomposition, current);
            if(successor.size() == 0 && map.get(current).size() == 1) continue; //leaf node
            if(successor.size() == 1) //forget or introduce
            {
                V next = successor.get(0);
                queue.add(next);
                Set<W> union = new HashSet<>(map.get(current));
                union.addAll(map.get(next));
                if(union.size() == map.get(next).size() || union.size() == map.get(current).size())
                {
                    if(map.get(current).size() == map.get(next).size()-1) continue; //introduce
                    else if(map.get(current).size()-1 == map.get(next).size()) continue; //forget
                }
            }
            if(successor.size() == 2) //join
            {
                V first = successor.get(0);
                V second = successor.get(1);
                queue.add(first);
                queue.add(second);
                Set<W> union = new HashSet<>(map.get(current));
                union.addAll(map.get(first));
                union.addAll(map.get(second));
                if(union.size() == map.get(current).size() 
                && union.size() == map.get(first).size() 
                && union.size() == map.get(second).size()) 
                    continue; //join node!
            }
            //does not fit in any category
            return false;
        }
        //everything fine
        return true;
    }
    
    /**
     * Tests whether the tree decomposition, consisting of the {@code decomposition} and the {@code map}
     * with the given {@code root} is a tree decomposition of the graph {@code oldGraph}.
     * 
     * @param oldGraph the graph of the tree decomposition
     * @param decomposition the tree decomposition
     * @param map the map from vertices to the tree decomposition to the sets of the tree decomposition
     */
    public static <V,E,W,F> boolean isTreeDecomposition(Graph<W,F> oldGraph, Graph<V,E> decomposition, Map<V,Set<W>> map){
        Set<F> edgeSet = oldGraph.edgeSet();
        Set<V> nodeSet = decomposition.vertexSet();
        //Every edge is represented
        for(F e : edgeSet)
        {
            boolean hasNode = false;
            for(V v : nodeSet)
            {
                if(map.get(v).contains(oldGraph.getEdgeSource(e)) && map.get(v).contains(oldGraph.getEdgeTarget(e))) {
                    hasNode = true;
                    break; 
                }
            }
            if(!hasNode)
                return false;
        }
        //every vertex has non-empty connected set of vertex sets
        Set<W> oldVertexSet = oldGraph.vertexSet();
        for(W w : oldVertexSet)
        {
            Set<V> keySet = new HashSet<>();
            for(Entry<V,Set<W>> entry : map.entrySet())
            {
                if(entry.getValue().contains(w)) {
                    keySet.add(entry.getKey());
                }
            }
            //not empty
            if(keySet.isEmpty())
                return false;
            //connected
            Graph<V,E> subgraph = new AsSubgraph<>(decomposition,keySet);
            if(!GraphTests.isConnected(subgraph))
                return false;
        }
        //everything fine!
        return true;
    }
    
}
