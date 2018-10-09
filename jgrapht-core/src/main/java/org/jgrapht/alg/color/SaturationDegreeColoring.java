/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.color;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * The Dsatur greedy coloring algorithm.
 * 
 * <p>
 * This is the greedy coloring algorithm using saturation degree ordering. The saturation degree of
 * a vertex is defined as the number of different colors to which it is adjacent. The algorithm
 * selects always the vertex with the largest saturation degree. If multiple vertices have the same
 * maximum saturation degree, a vertex of maximum degree in the uncolored subgraph is selected.
 *
 * <p>
 * Note that the DSatur is not optimal in general, but is optimal for bipartite graphs. Compared to
 * other simpler greedy ordering heuristics, it is usually considered slower but more efficient
 * w.r.t. the number of used colors. See the following papers for details:
 * <ul>
 * <li>D. Brelaz. New methods to color the vertices of a graph. Communications of ACM,
 * 22(4):251–256, 1979.</li>
 * <li>The smallest hard-to-color graph for algorithm DSATUR. Discrete Mathematics, 236:151--165,
 * 2001.</li>
 * </ul>
 * 
 * <p>
 * This implementation requires $O(n^2)$ running time and space. The following paper discusses
 * possible improvements in the running time.
 * <ul>
 * <li>J. S. Turner. Almost all $k$-colorable graphs are easy to color. Journal of Algorithms.
 * 9(1):63--82, 1988.</li>
 * </ul>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class SaturationDegreeColoring<V, E>
    implements
    VertexColoringAlgorithm<V>
{
    private final Graph<V, E> graph;

    /**
     * Construct a new coloring algorithm.
     * 
     * @param graph the input graph
     */
    public SaturationDegreeColoring(Graph<V, E> graph)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Coloring<V> getColoring()
    {
        /*
         * Initialize data structures
         */
        int n = graph.vertexSet().size();
        int maxColor = -1;
        Map<V, Integer> colors = new HashMap<>(n);
        Map<V, BitSet> adjColors = new HashMap<>(n);
        Map<V, Integer> saturation = new HashMap<>(n);

        /*
         * Compute degrees, available colors, and maximum degree.
         */
        int maxDegree = 0;
        Map<V, Integer> degree = new HashMap<>(n);
        for (V v : graph.vertexSet()) {
            int d = graph.edgesOf(v).size();
            degree.put(v, d);
            maxDegree = Math.max(maxDegree, d);
            adjColors.put(v, new BitSet());
            saturation.put(v, 0);
        }

        /*
         * Initialize heap
         */
        Heap heap = new Heap(n, new DSaturComparator(saturation, degree));
        Map<V, HeapHandle> handles = new HashMap<>();
        for (V v : graph.vertexSet()) {
            handles.put(v, new HeapHandle(v));
        }
        heap.bulkInsert(
            handles.values().toArray((HeapHandle[]) Array.newInstance(HeapHandle.class, 0)));

        /*
         * Color vertices
         */
        while (heap.size() > 0) {
            V v = heap.deleteMin().vertex;

            // find first free color
            BitSet used = adjColors.get(v);
            int c = used.nextClearBit(0);
            maxColor = Math.max(maxColor, c);

            // color the vertex
            colors.put(v, c);

            // partial cleanup to save some space
            adjColors.remove(v);

            // update neighbors
            for (E e : graph.edgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);

                if (!colors.containsKey(u)) {
                    // update used colors
                    int uSaturation = saturation.get(u);
                    BitSet uAdjColors = adjColors.get(u);

                    HeapHandle uHandle = handles.get(u);
                    if (uAdjColors.get(c)) {
                        // same saturation, degree decrease
                        // remove and reinsert
                        heap.delete(uHandle);
                        degree.put(u, degree.get(u) - 1);
                        heap.insert(uHandle);
                    } else {
                        // saturation increase, degree decrease
                        uAdjColors.set(c);
                        saturation.put(u, uSaturation + 1);
                        degree.put(u, degree.get(u) - 1);

                        // simple fix upwards inside heap since priority increased
                        heap.fixup(uHandle);
                    }
                }
            }

        }

        return new ColoringImpl<>(colors, maxColor + 1);
    }

    /*
     * Special case comparator for the DSatur algorithm. Compares first by saturation and then by
     * degree (maximum is better in both cases).
     */
    private class DSaturComparator
        implements
        Comparator<V>
    {
        private Map<V, Integer> saturation;
        private Map<V, Integer> degree;

        public DSaturComparator(Map<V, Integer> saturation, Map<V, Integer> degree)
        {
            this.saturation = saturation;
            this.degree = degree;
        }

        @Override
        public int compare(V o1, V o2)
        {
            int sat1 = saturation.get(o1);
            int sat2 = saturation.get(o2);
            if (sat1 > sat2) {
                return -1;
            } else if (sat1 < sat2) {
                return 1;
            } else {
                return -1 * Integer.compare(degree.get(o1), degree.get(o2));
            }
        }
    }

    /*
     * An addressable heap handle.
     */
    private class HeapHandle
    {
        int index;
        V vertex;

        public HeapHandle(V vertex)
        {
            this.vertex = vertex;
            this.index = -1;
        }
    }

    /*
     * An addressable binary heap.
     * 
     * No checks are performed (on purpose) for invalid handle use, or capacity violations.
     */
    private class Heap
    {
        private Comparator<V> comparator;
        private int size;
        private HeapHandle[] array;

        @SuppressWarnings("unchecked")
        public Heap(int capacity, Comparator<V> comparator)
        {
            this.comparator = comparator;
            this.size = 0;
            this.array = (HeapHandle[]) Array.newInstance(HeapHandle.class, capacity + 1);
        }

        private void fixdown(int k)
        {
            HeapHandle h = array[k];
            while (2 * k <= size) {
                int j = 2 * k;
                if (j < size && comparator.compare(array[j].vertex, array[j + 1].vertex) > 0) {
                    j++;
                }
                if (comparator.compare(h.vertex, array[j].vertex) <= 0) {
                    break;
                }
                array[k] = array[j];
                array[k].index = k;
                k = j;
            }
            array[k] = h;
            h.index = k;
        }

        private void fixup(int k)
        {
            HeapHandle h = array[k];
            while (k > 1 && comparator.compare(array[k / 2].vertex, h.vertex) > 0) {
                array[k] = array[k / 2];
                array[k].index = k;
                k /= 2;
            }
            array[k] = h;
            h.index = k;
        }

        private void forceFixup(int k)
        {
            HeapHandle h = array[k];
            while (k > 1) {
                array[k] = array[k / 2];
                array[k].index = k;
                k /= 2;
            }
            array[k] = h;
            h.index = k;
        }

        public HeapHandle deleteMin()
        {
            HeapHandle result = array[1];
            if (size == 1) {
                array[1] = null;
                size = 0;
            } else {
                array[1] = array[size];
                array[size] = null;
                size--;
                fixdown(1);
            }
            result.index = -1;
            return result;
        }

        public int size()
        {
            return size;
        }

        public void fixup(HeapHandle handle)
        {
            fixup(handle.index);
        }

        public void delete(HeapHandle handle)
        {
            forceFixup(handle.index);
            deleteMin();
        }

        public void insert(HeapHandle handle)
        {
            size++;
            array[size] = handle;
            handle.index = size;
            fixup(size);
        }

        public void bulkInsert(HeapHandle[] handles)
        {
            for (int i = 0; i < handles.length; i++) {
                size++;
                array[size] = handles[i];
                handles[i].index = size;
            }
            for (int i = size / 2; i > 0; i--) {
                fixdown(i);
            }
        }

    }

}
