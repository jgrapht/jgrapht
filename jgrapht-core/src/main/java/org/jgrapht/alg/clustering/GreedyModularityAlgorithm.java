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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jheaps.AddressableHeap;
import org.jheaps.AddressableHeap.Handle;
import org.jheaps.tree.PairingHeap;

/**
 * The Greedy Modularity algorithm.
 * 
 * <p>
 * The algorithm is capable of detecting communities in a graph by calculating
 * the
 * <a href="https://en.wikipedia.org/wiki/Modularity_(networks)">modularity</a>
 * of possible communities. It takes as input a graph and returns the
 * communities of the graph which produce the highest modularity.
 * </p>
 * 
 * @author Antonia Tsiftsi
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class GreedyModularityAlgorithm<V, E> implements ClusteringAlgorithm<V> {
	private final Graph<V, E> graph;

	/**
	 * Create a new clustering algorithm.
	 * 
	 * @param graph the graph
	 */
	public GreedyModularityAlgorithm(Graph<V, E> graph) {
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
                
                // create a map for communities where the merge will take place
                Map<V, Set<V>> communitiesMap = new HashMap<>();
                for (V v : graph.iterables().vertices()) {
			Set<V> set = Set.of(v);
			communitiesMap.put(v, set);
		} 
                                
		// initialization of Q
		UndirectedModularityMeasurer<V, E> measurer = new UndirectedModularityMeasurer<>(graph);
		double Q = measurer.modularity(communities);

		// 1: Map of DQs
		Map<V, Map<V, Double>> DQ = new HashMap<>();
		for (E e : graph.edgeSet()) {
			V vi = graph.getEdgeSource(e);
			V vj = graph.getEdgeTarget(e);
			ki = graph.degreeOf(vi);
			kj = graph.degreeOf(vj);
			// calculation of DQ
			double dq = (1.0 / (2 * m)) - ((ki * kj) / ((4.0 * m * m)));
			
			Map<V, Double> columnsI = DQ.get(vi);
			if (columnsI == null) {
				columnsI = new TreeMap<>();
				DQ.put(vi, columnsI);
			}
			columnsI.put(vj, dq);

			Map<V, Double> columnsJ = DQ.get(vj);
			if (columnsJ == null) {
				columnsJ = new TreeMap<>();
				DQ.put(vj, columnsJ);
			}
			columnsJ.put(vi, dq);			
		}
                
		// 1: Pairing Heap of DQs
		Map<V, AddressableHeap<Double, Pair<V, V>>> DQHeap = new HashMap<>();
		for (V vi : DQ.keySet()) {
			AddressableHeap<Double, Pair<V, V>> heap = new PairingHeap<>(Comparator.reverseOrder());
			Map<V, Double> columns = DQ.get(vi);
			for (V vj : columns.keySet()) {
				double dq = columns.get(vj);
				Pair<V, V> pair = new Pair<>(vi, vj);
				heap.insert(dq, pair);
			}
			DQHeap.put(vi, heap);
		}
                
                // 2: Pairing Heap - max DQ of each row
		AddressableHeap<Double, Pair<V, V>> maxHeapH = new PairingHeap<>(Comparator.reverseOrder());
                Map<V, Handle<Double, Pair<V,V>>> handles = new HashMap<>();
                TreeSet<V> setT = new TreeSet<>(graph.vertexSet());

		for (V vi : setT.descendingSet()) { //setT.descendingSet() or graph.vertexSet()
			double dq = DQHeap.get(vi).findMin().getKey();
			Pair<V, V> pair = DQHeap.get(vi).findMin().getValue();
			handles.put(vi, maxHeapH.insert(dq, pair));
		}
                
		// 3: Map of a
		Map<V, Double> a = new HashMap<>();
		for (V v : graph.vertexSet()) {
			a.put(v, (double) graph.degreeOf(v) / (2 * m));
		}
                
                for(V v: handles.keySet()){
                    System.out.println("Vi= " + handles.get(v).getValue().getFirst() + ", Vj= " + handles.get(v).getValue().getSecond() + ", Double= "+ handles.get(v).getKey());
                }

		while (DQ.size() != 1) { 
			// initialization
			AddressableHeap.Handle<Double, Pair<V, V>> max = maxHeapH.findMin();
                        System.out.println("Max dq = " + max.getKey());
                        System.out.println("Communities = " + communitiesMap);
                        System.out.println("Q = " + (Q+max.getKey()));
                        if(max.getKey()<0){
                            break;
                        }
			V i = max.getValue().getFirst();
			V j = max.getValue().getSecond();

			Set<V> nbrsI = DQ.get(i).keySet();
			Set<V> nbrsJ = DQ.get(j).keySet();

			Set<V> allNbrs = new HashSet<>(nbrsI);
			allNbrs.addAll(nbrsJ);
			allNbrs.remove(i);
			allNbrs.remove(j);
                        System.out.println("All nbrs: "+allNbrs);

			Set<V> bothNbrs = new HashSet<>(nbrsI);
			bothNbrs.retainAll(nbrsJ);
                        
			// update DQ
			for (V k : allNbrs) {
				double newDQjk;
				if (bothNbrs.contains(k)) { // k community connected to both i and j
                                        double DQik = DQ.get(i).get(k);
                                        double DQjk = DQ.get(j).get(k);
					newDQjk = DQik + DQjk;
				} else if (nbrsI.contains(k)) { // k community connected only to i
                                        double DQik = DQ.get(i).get(k);
					newDQjk = DQik - 2 * a.get(j) * a.get(k);
				} else { // k community connected only to j
                                        double DQjk = DQ.get(j).get(k);
					newDQjk = DQjk - 2 * a.get(i) * a.get(k);
				}
                                // update DQ
                                if(nbrsJ.contains(k)){
                                    DQ.get(j).put(k, newDQjk);
                                    DQ.get(k).put(j, newDQjk);
                                }else if(nbrsI.contains(k)){
                                    DQ.get(i).put(k, newDQjk);
                                    DQ.get(k).put(i, newDQjk);
                                }
			}

			// join communities
                        for(V v: DQ.get(i).keySet()){
                            if(!v.equals(j) && !bothNbrs.contains(v)){
                                DQ.get(j).put(v, DQ.get(i).get(v));
                                DQ.get(v).put(j, DQ.get(i).get(v));
                            }
                        }
                        
                        // clear i row and column
			for(V v: DQ.keySet()){
                            if(DQ.get(v).keySet().contains(i)){
                                DQ.get(v).remove(i);
                            }
                        }
                        DQ.remove(i);
                        
                        // update communitiesMap by combining community i and community j
                        Set<V> merge = new HashSet<>();
                        Iterator<V> itI = communitiesMap.get(i).iterator();
                        while(itI.hasNext()){                         
                            merge.add(itI.next());
                        }
                        Iterator<V> itJ = communitiesMap.get(j).iterator();
                        while(itJ.hasNext()){                         
                            merge.add(itJ.next());
                        }
                        communitiesMap.replace(j, merge);
                        communitiesMap.remove(i);

			// update DQHeap for k
			for (V k : allNbrs) {
				AddressableHeap<Double, Pair<V, V>> heap = new PairingHeap<>(Comparator.reverseOrder());
				Map<V, Double> columns = DQ.get(k);
				for (V vj : columns.keySet()) {
					double dq = columns.get(vj);
					Pair<V, V> pair = new Pair<>(k, vj);
					heap.insert(dq, pair);
				}
                                // replace old heap with new heap
                                DQHeap.replace(k, heap);     
                                // remove i from maxHeapH in kth row
                                if(handles.containsKey(k)){
                                    //Handle<Double, Pair<V,V>> test = handles.get(k);
                                    handles.get(k).delete();
                                }
                        }
                        
                        // update DQHeap for j
                        AddressableHeap<Double, Pair<V, V>> heap = new PairingHeap<>(Comparator.reverseOrder());
                        Map<V, Double> columns = DQ.get(j);
                        for (V v : columns.keySet()) {
                                double dq = columns.get(v);
                                Pair<V, V> pair = new Pair<>(j, v);
                                heap.insert(dq, pair);
                        }
                        DQHeap.replace(j, heap);
                        
                        // remove i heap from DQHeap
                        if(DQHeap.containsKey(i) ){
                            DQHeap.remove(i);
                        }
                        
                        // remove i from maxHeapH in jth row    
                        if(handles.containsKey(j)){
                            handles.get(j).delete();
                        }
                        
                        // remove ith row from maxHeapH
                        handles.get(i).delete();
                        handles.remove(i);
                        
			// update maxHeapH for k
			for (V k : allNbrs) {
				double dq = DQHeap.get(k).findMin().getKey();
				Pair<V, V> pair = DQHeap.get(k).findMin().getValue();
                                handles.replace(k, maxHeapH.insert(dq,pair));
			}
                                                
                        // update maxHeapH for j
                        if(!DQHeap.get(j).isEmpty()){
                            double dq = DQHeap.get(j).findMin().getKey();
                            Pair<V, V> pair = DQHeap.get(j).findMin().getValue();
                            handles.replace(j, maxHeapH.insert(dq,pair));
                        }
                        
                        for(V v: handles.keySet()){
                            System.out.println("Vi= " + handles.get(v).getValue().getFirst() + ", Vj= " + handles.get(v).getValue().getSecond() + ", Double= "+ handles.get(v).getKey());
                        }
                                                                       
			// update a
			a.put(j, a.get(i) + a.get(j));
			a.put(i, 0d);

			// increment Q by DQ
			Q += max.getKey();
		}

                // update communities list
                communities.clear();
                for(V v: communitiesMap.keySet()){
                    Set<V> set = communitiesMap.get(v);
                    communities.add(set);
                }
                
		ClusteringAlgorithm.ClusteringImpl<V> clustering = new ClusteringAlgorithm.ClusteringImpl<>(communities);
		return clustering;
	}
}
