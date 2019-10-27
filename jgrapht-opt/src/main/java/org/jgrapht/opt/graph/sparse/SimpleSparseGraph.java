/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.sparse;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.DefaultGraphType.Builder;

/**
 * A simple sparse graph implementation.
 * 
 * @author Dimitrios Michail
 */
public class SimpleSparseGraph
    extends
    AbstractGraph<Integer, Integer>
    implements
    Serializable
{
    private static final long serialVersionUID = 6479438435197401164L;

    protected static final String UNMODIFIABLE = "this graph is unmodifiable";

    /**
     * Number of vertices
     */
    protected int n;

    /**
     * Number of edges
     */
    protected int m;

    /**
     * Directed or not
     */
    protected boolean directed;

    /**
     * Source vertex of edge
     */
    protected Integer[] from;

    /**
     * Target vertex of edge
     */
    protected Integer[] to;

    /**
     * Edges sorted with key (from, to)
     */
    protected Integer[] outIndex;

    /**
     * Edges sorted with key (to, from)
     */
    protected Integer[] inIndex;

    /**
     * Prefix sum of out-degrees
     */
    protected Integer[] outPrefixScan;

    /**
     * Prefix sum of in-degrees
     */
    protected Integer[] inPrefixScan;

    /**
     * Create a graph
     * 
     * @param directed whether the graph is directed or not
     * @param numVertices the number of vertices
     * @param edges the edge list
     */
    public SimpleSparseGraph(boolean directed, int numVertices, List<Pair<Integer, Integer>> edges)
    {
        this.n = numVertices;
        this.m = edges.size();
        this.directed = directed;
        this.from = new Integer[m];
        this.to = new Integer[m];

        /*
         * Write edge list
         */
        int i = 0;
        for (Pair<Integer, Integer> e : edges) {
            if (directed || e.getFirst() > e.getSecond()) {
                from[i] = e.getFirst();
                to[i] = e.getSecond();
            } else {
                from[i] = e.getSecond();
                to[i] = e.getFirst();
            }
            i++;
        }

        /*
         * Indirect sort by (source,target) and (target,source)
         */
        this.outIndex = new Integer[m];
        this.inIndex = new Integer[m];
        for (i = 0; i < m; i++) {
            outIndex[i] = inIndex[i] = i;
        }
        Arrays.parallelSort(outIndex, new IndirectComparator(from, to));
        Arrays.parallelSort(inIndex, new IndirectComparator(to, from));

        /*
         * Count degrees (shifted by one) and prefix sum
         */
        this.outPrefixScan = new Integer[n + 1];
        this.inPrefixScan = new Integer[n + 1];
        for (i = 0; i < n + 1; i++) {
            outPrefixScan[i] = inPrefixScan[i] = 0;
        }
        for (i = 0; i < m; i++) {
            outPrefixScan[from[outIndex[i]] + 1]++;
            inPrefixScan[to[inIndex[i]] + 1]++;
        }
        Arrays.parallelPrefix(outPrefixScan, (x, y) -> x + y);
        Arrays.parallelPrefix(inPrefixScan, (x, y) -> x + y);
    }

    @Override
    public Set<Integer> getAllEdges(Integer sourceVertex, Integer targetVertex)
    {
        if (sourceVertex < 0 || sourceVertex >= n) {
            return null;
        }
        if (targetVertex < 0 || targetVertex >= n) {
            return null;
        }

        Set<Integer> result = new HashSet<>();

        Iterator<Integer> it = outgoingEdgesOf(sourceVertex).iterator();
        while (it.hasNext()) {
            int eId = it.next();

            if (directed) {
                if (getEdgeTarget(eId) == targetVertex) {
                    result.add(eId);
                }
            } else {
                if (getEdgeSource(eId) == sourceVertex && getEdgeTarget(eId) == targetVertex
                    || getEdgeSource(eId) == targetVertex && getEdgeTarget(eId) == sourceVertex)
                {
                    result.add(eId);
                }
            }
        }
        return result;
    }

    @Override
    public Integer getEdge(Integer sourceVertex, Integer targetVertex)
    {
        if (sourceVertex < 0 || sourceVertex >= n) {
            return null;
        }
        if (targetVertex < 0 || targetVertex >= n) {
            return null;
        }

        if (!directed && sourceVertex < targetVertex) {
            int tmp = sourceVertex;
            sourceVertex = targetVertex;
            targetVertex = tmp;
        }

        int e;
        if (outPrefixScan[sourceVertex + 1]
            - outPrefixScan[sourceVertex] < inPrefixScan[targetVertex + 1]
                - inPrefixScan[targetVertex])
        {
            e = binarySearchWithIndex(
                outPrefixScan[sourceVertex], outPrefixScan[sourceVertex + 1], targetVertex, to,
                outIndex);
        } else {
            e = binarySearchWithIndex(
                inPrefixScan[targetVertex], inPrefixScan[targetVertex + 1], sourceVertex, from,
                inIndex);
        }

        if (e == -1) {
            return null;
        }
        return e;

    }

    @Override
    public Supplier<Integer> getVertexSupplier()
    {
        return null;
    }

    @Override
    public Supplier<Integer> getEdgeSupplier()
    {
        return null;
    }

    @Override
    public Integer addEdge(Integer sourceVertex, Integer targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean addEdge(Integer sourceVertex, Integer targetVertex, Integer e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Integer addVertex()
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean addVertex(Integer v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean containsEdge(Integer e)
    {
        return e >= 0 && e < m;
    }

    @Override
    public boolean containsVertex(Integer v)
    {
        return v >= 0 && v < n;
    }

    @Override
    public Set<Integer> edgeSet()
    {
        return new IntegerSet(m);
    }

    @Override
    public int degreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return inPrefixScan[vertex + 1] - inPrefixScan[vertex] + outPrefixScan[vertex + 1]
            - outPrefixScan[vertex];
    }

    @Override
    public Set<Integer> edgesOf(Integer vertex)
    {
        assertVertexExist(vertex);

        return edgeStream(vertex, true, true).collect(Collectors.toSet());
    }

    @Override
    public int inDegreeOf(Integer vertex)
    {
        assertVertexExist(vertex);

        int d = inPrefixScan[vertex + 1] - inPrefixScan[vertex];
        if (!directed) {
            d += outPrefixScan[vertex + 1] - outPrefixScan[vertex];
        }
        return d;
    }

    @Override
    public Set<Integer> incomingEdgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return edgeStream(vertex, directed ? false : true, true).collect(Collectors.toSet());
    }

    @Override
    public int outDegreeOf(Integer vertex)
    {
        assertVertexExist(vertex);

        int d = outPrefixScan[vertex + 1] - outPrefixScan[vertex];
        if (!directed) {
            d += inPrefixScan[vertex + 1] - inPrefixScan[vertex];
        }
        return d;
    }

    @Override
    public Set<Integer> outgoingEdgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return edgeStream(vertex, true, directed ? false : true).collect(Collectors.toSet());
    }

    @Override
    public Integer removeEdge(Integer sourceVertex, Integer targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeEdge(Integer e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeVertex(Integer v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Set<Integer> vertexSet()
    {
        return new IntegerSet(n);
    }

    @Override
    public Integer getEdgeSource(Integer e)
    {
        assertEdgeExist(e);
        if (directed || from[e] <= to[e]) {
            return from[e];
        } else {
            return to[e];
        }
    }

    @Override
    public Integer getEdgeTarget(Integer e)
    {
        assertEdgeExist(e);
        if (directed || from[e] <= to[e]) {
            return to[e];
        } else {
            return from[e];
        }
    }

    @Override
    public GraphType getType()
    {
        Builder builder = new DefaultGraphType.Builder();
        builder = (directed) ? builder.directed() : builder.undirected();
        return builder
            .weighted(false).modifiable(false).allowMultipleEdges(true).allowSelfLoops(true)
            .build();
    }

    @Override
    public double getEdgeWeight(Integer e)
    {
        return Graph.DEFAULT_EDGE_WEIGHT;
    }

    @Override
    public void setEdgeWeight(Integer e, double weight)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * Ensures that the specified vertex exists in this graph, or else throws exception.
     *
     * @param v vertex
     * @return <code>true</code> if this assertion holds.
     * @throws IllegalArgumentException if specified vertex does not exist in this graph.
     */
    protected boolean assertVertexExist(Integer v)
    {
        if (v >= 0 && v < n) {
            return true;
        } else {
            throw new IllegalArgumentException("no such vertex in graph: " + v.toString());
        }
    }

    /**
     * Ensures that the specified edge exists in this graph, or else throws exception.
     *
     * @param e edge
     * @return <code>true</code> if this assertion holds.
     * @throws IllegalArgumentException if specified edge does not exist in this graph.
     */
    protected boolean assertEdgeExist(Integer e)
    {
        if (e >= 0 && e < m) {
            return true;
        } else {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
    }

    private class IndirectComparator
        implements
        Comparator<Integer>
    {

        private Integer[] key1;
        private Integer[] key2;

        public IndirectComparator(Integer[] key1, Integer[] key2)
        {
            this.key1 = key1;
            this.key2 = key2;
        }

        @Override
        public int compare(Integer o1, Integer o2)
        {
            int k1 = key1[o1];
            int k2 = key1[o2];
            if (k1 < k2) {
                return -1;
            } else if (k1 > k2) {
                return 1;
            }

            k1 = key2[o1];
            k2 = key2[o2];
            if (k1 < k2) {
                return -1;
            } else if (k1 > k2) {
                return 1;
            }
            return 0;
        }

    }

    /**
     * Perform binary search on an array using an external index.
     * 
     * @param start the start of the array
     * @param end the end of the array
     * @param value the value to look for
     * @param array the array
     * @param sortedIndex the index
     * @return the position found or -1 if not found
     */
    private int binarySearchWithIndex(
        int start, int end, int value, Integer[] array, Integer[] sortedIndex)
    {
        final int fixedEnd = end;
        while (start < end) {
            int mid = start + (end - start) / 2;
            int e = sortedIndex[mid];
            if (array[e] < value) {
                start = mid + 1;
            } else {
                end = mid;
            }
        }
        if (start < fixedEnd) {
            int e = sortedIndex[start];
            if (array[e] == value) {
                return e;
            }
        }
        return -1;
    }

    private Stream<Integer> edgeStream(int vertex, boolean outgoing, boolean incoming)
    {
        Stream<Integer> s;
        if (outgoing && !incoming) {
            s = Arrays.stream(outIndex, outPrefixScan[vertex], outPrefixScan[vertex + 1]);
        } else if (!outgoing && incoming) {
            s = Arrays.stream(inIndex, inPrefixScan[vertex], inPrefixScan[vertex + 1]);
        } else {
            s = Stream
                .concat(
                    Arrays.stream(outIndex, outPrefixScan[vertex], outPrefixScan[vertex + 1]),
                    Arrays.stream(inIndex, inPrefixScan[vertex], inPrefixScan[vertex + 1]));
        }
        return s;
    }

}
