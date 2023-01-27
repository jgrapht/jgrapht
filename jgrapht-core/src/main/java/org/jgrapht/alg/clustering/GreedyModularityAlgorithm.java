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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jheaps.AddressableHeap;
import org.jheaps.AddressableHeap.Handle;
import org.jheaps.tree.PairingHeap;
import java.lang.Comparable;

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
		// create a map for communities where the merge will take place
		Map<V, Set<V>> communitiesMap = new HashMap<>();
		for (V v : graph.iterables().vertices()) {
			Set<V> set = new HashSet<>();
			set.add(v);
			communitiesMap.put(v, set);
		}
               
		// 1: Map of DQs
                int m = graph.edgeSet().size();
		Map<V, Map<V, Double>> DQ = new HashMap<>();
		for (E e : graph.edgeSet()) {
			V vi = graph.getEdgeSource(e);
			V vj = graph.getEdgeTarget(e);
			int ki = graph.degreeOf(vi);
			int kj = graph.degreeOf(vj);
			// calculation of DQ
			double dq = (1.0 / (m)) - ((ki * kj) / (2.0 * m * m));
			
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
		Map<V, Map<V, Handle<Double, Pair<V, V>>>> DQHeapHandles = new HashMap<>();

		// 2: Pairing Heap - max DQ of each row
		AddressableHeap<Double, Pair<V, V>> maxHeap = new PairingHeap<>(Comparator.reverseOrder());
		Map<V, Handle<Double, Pair<V, V>>> maxHeapHandles = new HashMap<>();

                // Initialization of heaps
		for (V vi : DQ.keySet()) {
			AddressableHeap<Double, Pair<V, V>> heap = new PairingHeap<>(Comparator.reverseOrder());
			DQHeap.put(vi, heap);

			Map<V, Handle<Double, Pair<V, V>>> heapHandles = new HashMap<>();
			DQHeapHandles.put(vi, heapHandles);

			Map<V, Double> columns = DQ.get(vi);
			for (Entry<V, Double> e : columns.entrySet()) {
				heapHandles.put(e.getKey(), heap.insert(e.getValue(), new Pair<>(vi, e.getKey())));
			}
			
			Handle<Double, Pair<V, V>> viMax = heap.findMin();
			maxHeapHandles.put(vi, maxHeap.insert(viMax.getKey(), viMax.getValue()));
		}
                
		// 3: Map of a
		Map<V, Double> a = new HashMap<>();
		for (V v : graph.vertexSet()) {
			a.put(v, (double) graph.degreeOf(v) / (2.0 * m));
		}
                
		while (DQ.size() != 1) { 
//			for (V v : DQ.keySet()) {
//				Handle<Double, Pair<V, V>> minV = DQHeap.get(v).findMin();
//				Handle<Double, Pair<V, V>> handle = maxHeapHandles.get(v);
//				assert minV.getValue().equals(handle.getValue());
////				if (!minV.getValue().equals(handle.getValue())) { 
////					System.out.println("Problem on line " + v);
////					System.out.println("Min from dqheap=" + minV.getKey() + ", "+ minV.getValue());
////					System.out.println("Min from maxheap handle=" + handle.getKey() + ", "+ handle.getValue());
////				}
//			}
                        
			// compute best two communities to merge
			AddressableHeap.Handle<Double, Pair<V, V>> max = maxHeap.findMin();
                        
                        // stop if we cannot increase modularity
                        if(max.getKey() <= 0.0){
                            break;
                        } 
                        
			V i = max.getValue().getFirst();
			V j = max.getValue().getSecond();
                        System.out.println("i= "+ i +", j= "+ j);
                        
//                        if (((Comparable)i).compareTo((Comparable)j) >= 0) { 
//				V tmp = j; 
//				j = i; 
//				i = tmp;
//			};

                        System.out.println("i= "+ i +", j= "+ j);
                        
                        Set<V> nbrsI = DQ.get(i).keySet();
                        if(DQ.get(j).isEmpty()){
                            continue;
                        }
			Set<V> nbrsJ = DQ.get(j).keySet();

			Set<V> allNbrs = new HashSet<>(nbrsI);
			allNbrs.addAll(nbrsJ);
			allNbrs.remove(i);
			allNbrs.remove(j);
                        
                        if(allNbrs.isEmpty()){
                            maxHeapHandles.get(j).delete();
                        }
                        
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
                                // update j in DQ
                                DQ.get(j).put(k, newDQjk);
				
				// update k in DQ
				DQ.get(k).put(j, newDQjk);
                                
                                // update j-th key in k-th heap
				Handle<Double, Pair<V, V>> jHandle = DQHeapHandles.get(k).get(j);
				if (jHandle == null) {
					Handle<Double, Pair<V, V>> newjHandle = DQHeap.get(k).insert(newDQjk, Pair.of(k, j));
					DQHeapHandles.get(k).put(j, newjHandle);
					if (DQHeap.get(k).findMin() == newjHandle) { 
						maxHeapHandles.get(k).delete();
						maxHeapHandles.put(k, maxHeap.insert(newDQjk, Pair.of(k, j)));
					}
				} else {
					boolean isJMax = DQHeap.get(k).findMin() == jHandle;
					jHandle.delete();
					Handle<Double, Pair<V, V>> newjHandle = DQHeap.get(k).insert(newDQjk, Pair.of(k, j));
					DQHeapHandles.get(k).put(j, newjHandle);
					if (DQHeap.get(k).findMin() == newjHandle) { 
						maxHeapHandles.get(k).delete();
						maxHeapHandles.put(k, maxHeap.insert(newDQjk, Pair.of(k, j)));
					} else if (isJMax) { 
						maxHeapHandles.get(k).delete();
						Handle<Double, Pair<V, V>> newMax = DQHeap.get(k).findMin();
						maxHeapHandles.put(k,   maxHeap.insert(newMax.getKey(), newMax.getValue()));
					}
				}
				
				// also remove i from k-th row in DQHeap
				Handle<Double, Pair<V, V>> iHandle = DQHeapHandles.get(k).get(i);
				if (iHandle != null) {
					Handle<Double, Pair<V, V>> previousMaxK = maxHeapHandles.get(k);
					boolean isIMax = previousMaxK.getValue().equals(iHandle.getValue());
					iHandle.delete(); // remove i from k-th row in DQHeap
					DQHeapHandles.get(k).remove(i); 
                                        
					if (isIMax) { // if i was max, delete and add new max in maxHeapH
						previousMaxK.delete(); // remove max in maxHeapH
						Handle<Double, Pair<V, V>> newMaxK = DQHeap.get(k).findMin();
						maxHeapHandles.put(k, maxHeap.insert(newMaxK.getKey(), newMaxK.getValue())); // add new max in maxHeapH
					}
				}
			}
                        
                        // clear i row and column in DQ
			for (V v : DQ.keySet()) {
				DQ.get(v).remove(i);
			}
			DQ.remove(i);

                        // remove heap for i-th row
			DQHeap.remove(i);
			DQHeapHandles.remove(i);
			// remove maxHeapH for i-th row
			maxHeapHandles.get(i).delete();
			maxHeapHandles.remove(i);   
                        
                        // compute new heap for j-th row in DQHeap
			AddressableHeap<Double, Pair<V, V>> newJHeap = new PairingHeap<>(Comparator.reverseOrder());
			Map<V, Handle<Double, Pair<V, V>>> newJHeapHandles = new HashMap<>();
			for (Entry<V, Double> e : DQ.get(j).entrySet()) {
				newJHeapHandles.put(e.getKey(), newJHeap.insert(e.getValue(), new Pair<>(j, e.getKey())));
			}
			DQHeap.put(j, newJHeap);
			DQHeapHandles.put(j, newJHeapHandles);
                        
                        // update maxHeapH for j-th row
                        if(!newJHeap.isEmpty()){
                            Handle<Double, Pair<V, V>> newMaxDQj = newJHeap.findMin();
                            maxHeapHandles.get(j).delete();
                            maxHeapHandles.put(j, maxHeap.insert(newMaxDQj.getKey(), newMaxDQj.getValue()));
                        }
			 
			// update communities by combining community i and community j
			communitiesMap.get(j).addAll(communitiesMap.get(i));
			communitiesMap.remove(i);
                                                                       
			// update a
			a.put(j, a.get(i) + a.get(j));
			a.put(i, 0d);
		}

                // update communities list
		List<Set<V>> result = new ArrayList<>();
		for (Set<V> set : communitiesMap.values()) {
			result.add(set);
		}
                return new ClusteringAlgorithm.ClusteringImpl<>(result);
	}
}
