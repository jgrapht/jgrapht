/*
 * (C) Copyright 2019-2020, by Dimitrios Michail and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.util.*;
import java.util.function.*;

/**
 * A sparse directed graph.
 *
 * <p>
 * Assuming the graph has $n$ vertices, the vertices are numbered from $0$ to $n-1$. Similarly,
 * edges are numbered from $0$ to $m-1$ where $m$ is the total number of edges.
 * 
 * <p>
 * It stores two boolean incidence matrix of the graph (rows are vertices and columns are edges) as
 * Compressed Sparse Rows (CSR). Constant time source and target lookups are provided by storing the
 * edge lists in arrays. This is a classic format for write-once read-many use cases. Thus, the
 * graph is unmodifiable.
 * 
 * <p>
 * The question of whether a sparse or dense representation is more appropriate is highly dependent
 * on various factors such as the graph, the machine running the algorithm and the algorithm itself.
 * Wilkinson defined a matrix as "sparse" if it has enough zeros that it pays to take advantage of
 * them. For more details see
 * <ul>
 * <li>Wilkinson, J. H. 1971. Linear algebra; part II: the algebraic eigenvalue problem. In Handbook
 * for Automatic Computation, J. H. Wilkinson and C. Reinsch, Eds. Vol. 2. Springer-Verlag, Berlin,
 * New York.</li>
 * </ul>
 * 
 * Additional information about sparse representations can be found in the
 * <a href="https://en.wikipedia.org/wiki/Sparse_matrix">wikipedia</a>.
 * 
 * @author Dimitrios Michail
 */
public class SparseIntDirectedGraph
    extends
    AbstractGraph<Integer, Integer>
{
    protected static final String UNMODIFIABLE = "this graph is unmodifiable";

    /**
     * Source vertex of edge
     */
    protected int[] source;

    /**
     * Target vertex of edge
     */
    protected int[] target;

    /**
     * Incidence matrix with outgoing edges
     */
    protected CSRBooleanMatrix outIncidenceMatrix;

    /**
     * Incidence matrix with incoming edges
     */
    protected CSRBooleanMatrix inIncidenceMatrix;

    /**
     * Create a new graph from an edge list.
     * 
     * @param numVertices the number of vertices
     * @param edges the edge list
     */
    public SparseIntDirectedGraph(int numVertices, List<Pair<Integer, Integer>> edges)
    {
        final int m = edges.size();
        source = new int[m];
        target = new int[m];

        List<Pair<Integer, Integer>> outgoing = new ArrayList<>(m);
        List<Pair<Integer, Integer>> incoming = new ArrayList<>(m);
        int eIndex = 0;
        for (Pair<Integer, Integer> e : edges) {
            source[eIndex] = e.getFirst();
            target[eIndex] = e.getSecond();
            outgoing.add(Pair.of(e.getFirst(), eIndex));
            incoming.add(Pair.of(e.getSecond(), eIndex));
            eIndex++;
        }

        outIncidenceMatrix = new CSRBooleanMatrix(numVertices, m, outgoing);
        inIncidenceMatrix = new CSRBooleanMatrix(numVertices, m, incoming);
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
        return e >= 0 && e < outIncidenceMatrix.columns();
    }

    @Override
    public boolean containsVertex(Integer v)
    {
        return v >= 0 && v < outIncidenceMatrix.rows();
    }

    @Override
    public Set<Integer> edgeSet()
    {
        return new CompleteIntegerSet(outIncidenceMatrix.columns());
    }

    @Override
    public int degreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return outIncidenceMatrix.nonZeros(vertex) + inIncidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> edgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return new UnmodifiableUnionSet<>(
            outIncidenceMatrix.nonZerosSet(vertex), inIncidenceMatrix.nonZerosSet(vertex));
    }

    @Override
    public int inDegreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return inIncidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> incomingEdgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return inIncidenceMatrix.nonZerosSet(vertex);
    }

    @Override
    public int outDegreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return outIncidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> outgoingEdgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return outIncidenceMatrix.nonZerosSet(vertex);
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
        return new CompleteIntegerSet(outIncidenceMatrix.rows());
    }

    @Override
    public Integer getEdgeSource(Integer e)
    {
        assertEdgeExist(e);
        return source[e];
    }

    @Override
    public Integer getEdgeTarget(Integer e)
    {
        assertEdgeExist(e);
        return target[e];
    }

    @Override
    public GraphType getType()
    {
        return new DefaultGraphType.Builder()
            .directed().weighted(false).modifiable(false).allowMultipleEdges(true)
            .allowSelfLoops(true).build();
    }

    @Override
    public double getEdgeWeight(Integer e)
    {
        return 1.0;
    }

    @Override
    public void setEdgeWeight(Integer e, double weight)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    /**
     * {@inheritDoc}
     * 
     * This operation costs $O(d)$ where $d$ is the out-degree of the source vertex.
     */
    @Override
    public Integer getEdge(Integer sourceVertex, Integer targetVertex)
    {
        if (sourceVertex < 0 || sourceVertex >= outIncidenceMatrix.rows()) {
            return null;
        }
        if (targetVertex < 0 || targetVertex >= outIncidenceMatrix.rows()) {
            return null;
        }

        Iterator<Integer> it = outIncidenceMatrix.nonZerosPositionIterator(sourceVertex);
        while (it.hasNext()) {
            int eId = it.next();
            if (getEdgeTarget(eId) == targetVertex) {
                return eId;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * This operation costs $O(d)$ where $d$ is the out-degree of the source vertex.
     */
    @Override
    public Set<Integer> getAllEdges(Integer sourceVertex, Integer targetVertex)
    {
        if (sourceVertex < 0 || sourceVertex >= outIncidenceMatrix.rows()) {
            return null;
        }
        if (targetVertex < 0 || targetVertex >= outIncidenceMatrix.rows()) {
            return null;
        }

        Set<Integer> result = new LinkedHashSet<>();

        Iterator<Integer> it = outIncidenceMatrix.nonZerosPositionIterator(sourceVertex);
        while (it.hasNext()) {
            int eId = it.next();

            if (getEdgeTarget(eId) == targetVertex) {
                result.add(eId);
            }
        }
        return result;
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
        if (v >= 0 && v < outIncidenceMatrix.rows()) {
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
        if (e >= 0 && e < outIncidenceMatrix.columns()) {
            return true;
        } else {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
    }

}
