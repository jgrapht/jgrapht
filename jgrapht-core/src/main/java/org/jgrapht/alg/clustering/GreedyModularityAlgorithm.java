/*
 * (C) Copyright 2021-2021, by Antonia Tsiftsi and Contributors.
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
package org.jgrapht.alg.clustering;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.PairingHeap;

/**
 * The Greedy Modularity algorithm.
 * 
 * <p>
 * The algorithm is capable of detecting communities in a graph by calculating 
 * the <a href="https://en.wikipedia.org/wiki/Modularity_(networks)">modularity</a>
 * of possible communities. It takes as input a graph and returns the communities 
 * of the graph which produce the highest modularity.
 * </p>
 * 
 * @author Antonia Tsiftsi
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class GreedyModularityAlgorithm<V, E> implements ClusteringAlgorithm<V>
{
    private final Graph<V, E> graph;

    /**
     * Create a new clustering algorithm.
     * 
     * @param graph the graph
     */
    public GreedyModularityAlgorithm(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireUndirected(graph);
    }

    @Override
    public ClusteringAlgorithm.Clustering<V> getClustering() {
        int ki, kj;
        int m = graph.edgeSet().size();
        
        // create one community for each node
        List<Set<V>> communities = new ArrayList<>();
        for (V v : graph.iterables().vertices()) {
            Set<V> set = Set.of(v);
            communities.add(set);
        }
        
        // initialization of Q
        UndirectedModularityMeasurer<V,E> measurer = new UndirectedModularityMeasurer<>(graph);
        double Q = measurer.modularity(communities);
        
        //1: Map of DQs
        Map<V, Map<V,Double>> DQ = new HashMap<>();
        for(E e: graph.edgeSet()){
            V vi = graph.getEdgeSource(e);
            V vj = graph.getEdgeTarget(e);
            ki = graph.degreeOf(vi);
            kj = graph.degreeOf(vj);
            // calculation of DQ
            double dq = (1.0 / (2*m)) - ((ki*kj)/((4.0*m*m)));
            Map<V, Double> columns = DQ.get(vi);
            if(columns == null){
                columns = new TreeMap<>();
                DQ.put(vi, columns);
            }
            columns.put(vj, dq);
        }
       
        //1: Pairing Heap of DQs
        Map<V, AddressableHeap<Double, Pair<V, V>>> DQHeap = new HashMap<>();
        for(V vi: DQ.keySet()){
            AddressableHeap<Double, Pair<V,V>> heap = new PairingHeap<>(Comparator.reverseOrder());
            Map<V, Double> columns =DQ.get(vi);
            for(V vj: columns.keySet()){
                double dq = columns.get(vj);
                Pair<V,V> pair = new Pair<>(vi, vj);
                heap.insert(dq,pair);
            }
            DQHeap.put(vi, heap);
        }
       
        //2: Pairing Heap - max DQ of each row
        AddressableHeap<Double, Pair<V,V>> maxHeapH = new PairingHeap<>(Comparator.reverseOrder());
        for(V vi: graph.vertexSet()){
            double dq = DQHeap.get(vi).findMin().getKey();
            Pair<V,V> pair = DQHeap.get(vi).findMin().getValue();
            maxHeapH.insert(dq, pair);
        }
        
        //3: Map of ai
        Map<V, Double> Ai = new HashMap<>();
        for(V v:graph.vertexSet()){
            int k = graph.degreeOf(v);
            double a = k/(2*m);
            Ai.put(v,a);
        }
       
        while(DQ.size()!=1){
            // initialization
            AddressableHeap.Handle<Double, Pair<V, V>> max = maxHeapH.findMin();
            V i = max.getValue().getFirst();
            V j = max.getValue().getSecond();
            
            Map<V,Double> communityI = DQ.get(i);
            Map<V,Double> communityJ = DQ.get(j);
            
            Set<V> nbrsI = communityI.keySet();
            Set<V> nbrsJ = communityJ.keySet();
            
            Set<V> allNbrs = new HashSet<V>(nbrsI);
            allNbrs.addAll(nbrsJ);
            allNbrs.remove(i);
            allNbrs.remove(j);
            
            Set<V> bothNbrs = new HashSet<V>(nbrsI);
            bothNbrs.retainAll(nbrsJ);
            
            // update DQ 
            for(V k:allNbrs){
                Double DQik = communityI.get(k);
                Double DQjk = communityJ.get(k);
                if(bothNbrs.contains(k)){ //k community connected to both i and j
                    DQjk += DQik;
                }else if(nbrsI.contains(k)){ //k community connected only to i
                    DQjk = DQik - 2 * Ai.get(i) * Ai.get(k);
                }else { //k community connected only to j
                    DQjk -= 2 * Ai.get(i) * Ai.get(k);
                }
                communityJ.put(k, DQjk);
            }
            
             // join communities
            communityJ.putAll(communityI);
            communityI.clear();
            
            // update DQHeap
            for(V vi: allNbrs){
                AddressableHeap<Double, Pair<V,V>> heap = new PairingHeap<>(Comparator.reverseOrder());
                Map<V, Double> columns =DQ.get(vi);
                for(V vj: columns.keySet()){
                    double dq = columns.get(vj);
                    Pair<V,V> pair = new Pair<>(vi, vj);
                    heap.insert(dq,pair);
                }
                DQHeap.put(vi, heap);
            }
            
            // update maxHeapH
            maxHeapH.deleteMin();
            for(V vi: allNbrs){
                double dq = DQHeap.get(vi).findMin().getKey();
                Pair<V,V> pair = DQHeap.get(vi).findMin().getValue();
                maxHeapH.insert(dq, pair);
            }
            
            // update Ai
            double ai=0;
            double aj = Ai.get(j);
            double a = ai + aj;
            Ai.put(i, ai);
            Ai.put(j, a); 
            
            // increment Q by DQ
            Q += max.getKey();
        }
       
        ClusteringAlgorithm.ClusteringImpl<V> clustering = new ClusteringAlgorithm.ClusteringImpl<>(communities);
        return clustering;
    }
}

