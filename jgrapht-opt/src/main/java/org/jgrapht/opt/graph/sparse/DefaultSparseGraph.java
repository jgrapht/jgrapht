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
import java.util.stream.IntStream;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.DefaultGraphType.Builder;

/**
 * A simple and efficient sparse graph implementation.
 * 
 * <p>
 * Assuming the graph has $n$ vertices, the vertices are numbered from $0$ to $n-1$. Similarly,
 * edges are numbered from $0$ to $m-1$ where $m$ is the total number of edges.
 * 
 * <p>
 * The representation uses six integer arrays, four of them have size $m$ where $m$ is the number of
 * edges and two of them have size $n+1$ where $n$ is the number of vertices. The first two contain
 * the source and target vertices of the edges. The third contains an index of the edge identifiers
 * if sorted by the composite key (source, target), while the fourth contains an index of the edge
 * identifiers if sorted by the composite key (target, source). The last two arrays contain the
 * cumulative sum of the outgoing and incoming vertex degrees respectively.
 *
 * <p>
 * This implementation supports both directed and undirected graphs. The graph is initialized from
 * the constructor and cannot be modified afterwards.
 *
 * @author Dimitrios Michail
 */
public class DefaultSparseGraph
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
    protected int[] source;

    /**
     * Target vertex of edge
     */
    protected int[] target;

    /**
     * Edges sorted with key (source, target)
     */
    protected int[] outIndex;

    /**
     * Edges sorted with key (target, source)
     */
    protected int[] inIndex;

    /**
     * Prefix sum of out-degrees
     */
    protected int[] outPrefixScan;

    /**
     * Prefix sum of in-degrees
     */
    protected int[] inPrefixScan;

    /**
     * Create a graph
     * 
     * @param directed whether the graph is directed or not
     * @param numVertices the number of vertices
     * @param edges the edge list
     */
    public DefaultSparseGraph(boolean directed, int numVertices, List<Pair<Integer, Integer>> edges)
    {
        this.n = numVertices;
        this.m = edges.size();
        this.directed = directed;

        /*
         * Write edge list
         */
        this.source = new int[m];
        this.target = new int[m];
        int i = 0;
        for (Pair<Integer, Integer> e : edges) {
            if (directed || e.getFirst() > e.getSecond()) {
                source[i] = e.getFirst();
                target[i] = e.getSecond();
            } else {
                source[i] = e.getSecond();
                target[i] = e.getFirst();
            }
            i++;
        }

        /*
         * Indirect sort with key (source,target)
         */
        Integer[] tmp = new Integer[m];
        for (i = 0; i < m; i++) {
            tmp[i] = i;
        }
        Arrays.parallelSort(tmp, new IndirectComparator(source, target));
        this.outIndex = new int[m];
        for (i = 0; i < m; i++) {
            outIndex[i] = tmp[i];
        }

        /*
         * Indirect sort with key (target,source)
         */
        for (i = 0; i < m; i++) {
            tmp[i] = i;
        }
        Arrays.parallelSort(tmp, new IndirectComparator(target, source));
        this.inIndex = new int[m];
        for (i = 0; i < m; i++) {
            inIndex[i] = tmp[i];
        }

        /*
         * Count degrees (shifted by one) and prefix sum
         */
        this.outPrefixScan = new int[n + 1];
        this.inPrefixScan = new int[n + 1];
        for (i = 0; i < m; i++) {
            outPrefixScan[source[outIndex[i]] + 1]++;
            inPrefixScan[target[inIndex[i]] + 1]++;
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
                outPrefixScan[sourceVertex], outPrefixScan[sourceVertex + 1], targetVertex, target,
                outIndex);
        } else {
            e = binarySearchWithIndex(
                inPrefixScan[targetVertex], inPrefixScan[targetVertex + 1], sourceVertex, source,
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

        return edgeStream(vertex, true, true).boxed().collect(Collectors.toSet());
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
        return edgeStream(vertex, directed ? false : true, true)
            .boxed().collect(Collectors.toSet());
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
        return edgeStream(vertex, true, directed ? false : true)
            .boxed().collect(Collectors.toSet());
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
        if (directed || source[e] <= target[e]) {
            return source[e];
        } else {
            return target[e];
        }
    }

    @Override
    public Integer getEdgeTarget(Integer e)
    {
        assertEdgeExist(e);
        if (directed || source[e] <= target[e]) {
            return target[e];
        } else {
            return source[e];
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

    /**
     * A comparator for indirect sort.
     */
    private class IndirectComparator
        implements
        Comparator<Integer>
    {
        private int[] key1;
        private int[] key2;

        public IndirectComparator(int[] key1, int[] key2)
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
    private int binarySearchWithIndex(int start, int end, int value, int[] array, int[] sortedIndex)
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

    /**
     * Create a stream of a vertex incident edges. The stream may contain duplicate entries in case
     * or loops, etc.
     * 
     * @param vertex the vertex
     * @param outgoing whether to include outgoing edges
     * @param incoming whether to include incoming edges
     * @return an edge stream
     */
    private IntStream edgeStream(int vertex, boolean outgoing, boolean incoming)
    {
        if (outgoing && !incoming) {
            return Arrays.stream(outIndex, outPrefixScan[vertex], outPrefixScan[vertex + 1]);
        } else if (!outgoing && incoming) {
            return Arrays.stream(inIndex, inPrefixScan[vertex], inPrefixScan[vertex + 1]);
        } else if (outgoing && incoming) {
            return IntStream
                .concat(
                    Arrays.stream(outIndex, outPrefixScan[vertex], outPrefixScan[vertex + 1]),
                    Arrays.stream(inIndex, inPrefixScan[vertex], inPrefixScan[vertex + 1]));
        } else {
            return IntStream.empty();
        }
    }

}
