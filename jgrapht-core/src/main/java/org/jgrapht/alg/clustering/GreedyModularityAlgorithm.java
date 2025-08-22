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
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jheaps.AddressableHeap;
import org.jheaps.AddressableHeap.Handle;
import org.jheaps.tree.PairingHeap;

/**
 * The Greedy Modularity algorithm.
 * 
 * <p>
 * The algorithm partitions the vertices of an undirected graph into communities by greedily
 * maximizing the <a href="https://en.wikipedia.org/wiki/Modularity_(networks)">modularity</a>
 * of possible communities. Greedy modularity maximization begins with each node in its own
 * community and repeatedly joins the pair of communities that lead to the largest modularity
 * until no further increase in modularity is possible (a maximum).
 * </p>
 * 
 * <p>
 * The algorithm is due to Clauset, Newman and Moore. It is described in detail in the following
 * <a href="https://doi.org/10.1103/PhysRevE.70.066111">paper</a>:
 * <ul>
 * <li>Clauset, A., Newman, M. E., & Moore, C. “Finding community structure in very large networks.”
 * Physical Review E 70(6), 2004.
 * </li>
 * </ul> 
 * </p>
 *  
 * @author Antonia Tsiftsi
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class GreedyModularityAlgorithm<V, E>
    implements
    ClusteringAlgorithm<V>
{
    private final Graph<V, E> graph;

    /**
     * Create a new clustering algorithm.
     * 
     * @param graph the graph
     */
    public GreedyModularityAlgorithm(Graph<V, E> graph)
    {
        this.graph = graph;
    }

    @Override
    public ClusteringAlgorithm.Clustering<V> getClustering()
    {
        // create a map for communities where the merge will take place
        Map<V, Set<V>> communitiesMap = new HashMap<>();
        for (V v : graph.iterables().vertices()) {
            Set<V> set = new HashSet<>();
            set.add(v);
            communitiesMap.put(v, set);
        }

        // 1: Map of DQs
        int m = graph.edgeSet().size();
        Map<V, Map<V, Double>> dQ = new HashMap<>();
        for (E e : graph.edgeSet()) {
            V vi = graph.getEdgeSource(e);
            V vj = graph.getEdgeTarget(e);

            if (vi.equals(vj)) {
                // ignore self-loops
                continue;
            }

            int ki = graph.degreeOf(vi);
            int kj = graph.degreeOf(vj);

            // calculation of dQ
            double dq = (1.0 / (m)) - ((ki * kj) / (2.0 * m * m));

            Map<V, Double> columnsI = dQ.get(vi);
            if (columnsI == null) {
                columnsI = new TreeMap<>();
                dQ.put(vi, columnsI);
            }
            columnsI.put(vj, dq);

            Map<V, Double> columnsJ = dQ.get(vj);
            if (columnsJ == null) {
                columnsJ = new TreeMap<>();
                dQ.put(vj, columnsJ);
            }
            columnsJ.put(vi, dq);
        }

        // 1: Pairing Heap of DQs
        Map<V, AddressableHeap<Double, Pair<V, V>>> dQHeap = new HashMap<>();
        Map<V, Map<V, Handle<Double, Pair<V, V>>>> dQHeapHandles = new HashMap<>();

        // 2: Pairing Heap - max dQ of each row
        AddressableHeap<Double, Pair<V, V>> maxHeap = new PairingHeap<>(Comparator.reverseOrder());
        Map<V, Handle<Double, Pair<V, V>>> maxHeapHandles = new HashMap<>();

        // Initialization of heaps
        for (V vi : dQ.keySet()) {
            AddressableHeap<Double, Pair<V, V>> heap = new PairingHeap<>(Comparator.reverseOrder());
            dQHeap.put(vi, heap);

            Map<V, Handle<Double, Pair<V, V>>> heapHandles = new HashMap<>();
            dQHeapHandles.put(vi, heapHandles);

            Map<V, Double> columns = dQ.get(vi);
            for (Entry<V, Double> e : columns.entrySet()) {
                heapHandles.put(e.getKey(), heap.insert(e.getValue(), new Pair<>(vi, e.getKey())));
            }

            Handle<Double, Pair<V, V>> viMax = heap.findMin();
            maxHeapHandles.put(vi, maxHeap.insert(viMax.getKey(), viMax.getValue()));
        }

        // 3: Map of a
        Map<V, Double> a = new HashMap<>();
        Map<V, Double> b = new HashMap<>();
        if (graph.getType().isDirected()) {
            for (V v : graph.vertexSet()) {
                a.put(v, (double) graph.outDegreeOf(v) / (m));
                b.put(v, (double) graph.inDegreeOf(v) / (m));
            }
        } else {
            for (V v : graph.vertexSet()) {
                a.put(v, (double) graph.degreeOf(v) / (2.0 * m));
            }
            b = a;
        }

        while (dQ.size() != 1) {
            for (V v : dQ.keySet()) {
                if (dQHeap.get(v).isEmpty()) {
                    continue;
                }
                Handle<Double, Pair<V, V>> minV = dQHeap.get(v).findMin();
                Handle<Double, Pair<V, V>> handle = maxHeapHandles.get(v);
                assert minV.getValue().equals(handle.getValue());
            }

            // compute best two communities to merge
            AddressableHeap.Handle<Double, Pair<V, V>> max = maxHeap.findMin();

            // stop if we cannot increase modularity
            if (max.getKey() <= 0.0) {
                break;
            }

            V i = max.getValue().getFirst();
            V j = max.getValue().getSecond();

            Set<V> nbrsI = dQ.get(i).keySet();
            Set<V> nbrsJ = dQ.get(j).keySet();

            Set<V> allNbrs = new HashSet<>(nbrsI);
            allNbrs.addAll(nbrsJ);
            allNbrs.remove(i);
            allNbrs.remove(j);

            Set<V> bothNbrs = new HashSet<>(nbrsI);
            bothNbrs.retainAll(nbrsJ);

            // update dQ
            for (V k : allNbrs) {
                double newDQjk;
                if (bothNbrs.contains(k)) { // k community connected to both i and j
                    double dQik = dQ.get(i).get(k);
                    double dQjk = dQ.get(j).get(k);
                    newDQjk = dQik + dQjk;
                } else if (nbrsI.contains(k)) { // k community connected only to i
                    double dQik = dQ.get(i).get(k);
                    newDQjk = dQik - (a.get(j) * b.get(k) + a.get(k) * b.get(j));
                } else { // k community connected only to j
                    double dQjk = dQ.get(j).get(k);
                    newDQjk = dQjk - (a.get(i) * b.get(k) + a.get(k) * b.get(i));
                }
                // update j in dQ
                dQ.get(j).put(k, newDQjk);

                // update k in dQ
                dQ.get(k).put(j, newDQjk);

                // update j-th key in k-th heap
                Handle<Double, Pair<V, V>> jHandle = dQHeapHandles.get(k).get(j);
                if (jHandle == null) {
                    Handle<Double, Pair<V, V>> newjHandle =
                        dQHeap.get(k).insert(newDQjk, Pair.of(k, j));
                    dQHeapHandles.get(k).put(j, newjHandle);
                    if (dQHeap.get(k).findMin() == newjHandle) {
                        maxHeapHandles.get(k).delete();
                        maxHeapHandles.put(k, maxHeap.insert(newDQjk, Pair.of(k, j)));
                    }
                } else {
                    boolean isJMax = dQHeap.get(k).findMin() == jHandle;
                    jHandle.delete();
                    Handle<Double, Pair<V, V>> newjHandle =
                        dQHeap.get(k).insert(newDQjk, Pair.of(k, j));
                    dQHeapHandles.get(k).put(j, newjHandle);
                    if (dQHeap.get(k).findMin() == newjHandle) {
                        maxHeapHandles.get(k).delete();
                        maxHeapHandles.put(k, maxHeap.insert(newDQjk, Pair.of(k, j)));
                    } else if (isJMax) {
                        maxHeapHandles.get(k).delete();
                        Handle<Double, Pair<V, V>> newMax = dQHeap.get(k).findMin();
                        maxHeapHandles.put(k, maxHeap.insert(newMax.getKey(), newMax.getValue()));
                    }
                }

                // also remove i from k-th row in dQHeap
                Handle<Double, Pair<V, V>> iHandle = dQHeapHandles.get(k).get(i);
                if (iHandle != null) {
                    Handle<Double, Pair<V, V>> previousMaxK = maxHeapHandles.get(k);
                    boolean isIMax = previousMaxK.getValue().equals(iHandle.getValue());
                    iHandle.delete(); // remove i from k-th row in dQHeap
                    dQHeapHandles.get(k).remove(i);

                    if (isIMax) { // if i was max, delete and add new max in maxHeapH
                        previousMaxK.delete(); // remove max in maxHeapH
                        Handle<Double, Pair<V, V>> newMaxK = dQHeap.get(k).findMin();
                        // add new max in maxHeapH
                        maxHeapHandles.put(k, maxHeap.insert(newMaxK.getKey(), newMaxK.getValue()));
                    }
                }
            }

            // clear i row and column in dQ
            for (V v : dQ.keySet()) {
                dQ.get(v).remove(i);
            }
            dQ.remove(i);

            // remove heap for i-th row
            dQHeap.remove(i);
            dQHeapHandles.remove(i);
            // remove maxHeapH for i-th row
            maxHeapHandles.get(i).delete();
            maxHeapHandles.remove(i);

            // compute new heap for j-th row in dQHeap
            AddressableHeap<Double, Pair<V, V>> newJHeap =
                new PairingHeap<>(Comparator.reverseOrder());
            Map<V, Handle<Double, Pair<V, V>>> newJHeapHandles = new HashMap<>();
            for (Entry<V, Double> e : dQ.get(j).entrySet()) {
                newJHeapHandles
                    .put(e.getKey(), newJHeap.insert(e.getValue(), new Pair<>(j, e.getKey())));
            }
            dQHeap.put(j, newJHeap);
            dQHeapHandles.put(j, newJHeapHandles);

            // update maxHeapH for j-th row
            if (!newJHeap.isEmpty()) {
                Handle<Double, Pair<V, V>> newMaxDQj = newJHeap.findMin();
                maxHeapHandles.get(j).delete();
                maxHeapHandles.put(j, maxHeap.insert(newMaxDQj.getKey(), newMaxDQj.getValue()));
            } else {
                maxHeapHandles.get(j).delete();
                maxHeapHandles.remove(j);
            }

            // update communities by combining community i and community j
            communitiesMap.get(j).addAll(communitiesMap.get(i));
            communitiesMap.remove(i);

            // update a
            a.put(j, a.get(i) + a.get(j));
            a.put(i, 0d);
            if (graph.getType().isDirected()) {
                b.put(j, b.get(j) + b.get(i));
                b.put(i, 0d);
            }
        }

        // update communities list
        List<Set<V>> result = new ArrayList<>();
        for (Set<V> set : communitiesMap.values()) {
            result.add(set);
        }
        return new ClusteringAlgorithm.ClusteringImpl<>(result);
    }
}
