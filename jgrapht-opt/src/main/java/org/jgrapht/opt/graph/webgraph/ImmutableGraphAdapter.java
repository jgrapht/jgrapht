/*
 * (C) Copyright 2020, by Sebastiano Vigna.
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

package org.jgrapht.opt.graph.webgraph;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.GraphIterables;
import org.jgrapht.graph.AbstractGraph;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIntSortedPair;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.lang.FlyweightPrototype;
import it.unimi.dsi.webgraph.Check;
import it.unimi.dsi.webgraph.EFGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.LazyIntSkippableIterator;
import it.unimi.dsi.webgraph.Transform;

/**
 * A graph adapter class using <a href="http://webgraph.di.unimi.it/">WebGraph</a>'s
 * {@link ImmutableGraph}.
 *
 * <p>
 * Nodes are instance of {@link Integer} corresponding to the index of a node in WebGraph. Edges are
 * represented by an {@link IntIntPair}, for directed graph, or by an {@link IntIntSortedPair}, for
 * an undirected graph. In directed, the left and right element are the source and the target of the
 * edge. In the undirected case, edges are canonicalized so that the left element is always smaller
 * than or equal to the right element. Since the underlying graph is immutable, the resulting graph
 * is unmodifiable. Edges are immutable and can be tested for equality (e.g., stored in a
 * dictionary).
 *
 * <p>
 * WebGraph provides methods for successors only, so to adapt a directed graph you must provide both
 * a graph and its transpose (methods to compute the transpose are available in {@link Transform}).
 *
 * You need to load an {@link ImmutableGraph} and its transpose using one of the load methods
 * available, and use the {@link #directed(ImmutableGraph, ImmutableGraph)} factory method.
 *
 * <pre>
 * immutableGraph = ImmutableGraph.loadMapped("mygraph");
 * immutableTranspose = ImmutableGraph.loadMapped("mygraph-t");
 * adapter = ImmutableGraphAdapterEndpointPair.directed(immutableGraph, immutableTranspose);
 * </pre>
 *
 * <p>
 * It is your responsibility that the two provided graphs are one the transpose of the other (for
 * each arc <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> in a graph there must be an arc
 * <var>y</var>&nbsp;&rarr;&nbsp;<var>x</var> in the other). No check will be performed. Note that
 * {@linkplain GraphIterables#edgeCount() computing the number of edges of an directed graph}
 * requires a full scan of the edge set if {@link ImmutableGraph#numArcs()} is not supported (the
 * first time&mdash;then it will be cached).
 *
 * <p>
 * If you use a load method that does not provide random access, most methods will throw an
 * {@link UnsupportedOperationException}. The first graph will be used to implement
 * {@link #outgoingEdgesOf(Integer)}, and the second graph to implement
 * {@link #incomingEdgesOf(Integer)}.
 *
 * <p>
 * If you know that you will never used methods based on incoming edges
 * ({@link #incomingEdgesOf(Integer)}, {@link #inDegreeOf(Integer)}, {@link #edgesOf(Integer)},
 * {@link #degreeOf(Integer)}), you can also use the factory method using just a graph, but all such
 * methods will throw a {@link NullPointerException}:
 *
 * <pre>
 * immutableGraph = ImmutableGraph.loadMapped("mygraph");
 * adapter = ImmutableGraphAdapter.directed(immutableGraph);
 * </pre>
 *
 * <p>
 * If your graph is symmetric, you can adapt it as an undirected graph:
 *
 * <pre>
 * immutableGraph = ImmutableGraph.loadMapped("mygraph");
 * adapter = ImmutableGraphAdapter.undirected(immutableGraph);
 * </pre>
 *
 * <p>
 * It is your responsibility that the provided graph is symmetric (for each arc
 * <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> there is an arc&nbsp;<var>y</var>&nbsp;&rarr;
 * <var>x</var>). No check will be performed, but you can use the {@link Check} class to this
 * purpose. Note that {@linkplain GraphIterables#edgeCount() computing the number of edges of an
 * undirected graph} requires a full scan of the edge set (the first time&mdash;then it will be
 * cached).
 *
 * <p>
 * If necessary, you can adapt a {@linkplain it.unimi.dsi.big.webgraph.ImmutableGraph big WebGraph
 * graph} with less than {@link Integer#MAX_VALUE} vertices using the suitable
 * {@linkplain it.unimi.dsi.big.webgraph.ImmutableGraph#wrap(ImmutableGraph) wrapper}.
 *
 * <h2>Thread safety</h2>
 *
 * <p>
 * This class is not thread safe: following the {@link FlyweightPrototype} pattern, users can access
 * concurrently the graph {@linkplain #copy() by getting lightweight copies}.
 *
 * <h2>Fast adjacency check</h2>
 *
 * <p>
 * As it happens for the sparse representation of JGraphT, usually a WebGraph compressed
 * representation requires scanning the adjacency list of a node to
 * {@linkplain #getEdge(Integer, Integer) test whether a specific arc exists}. However, if you adapt
 * a WebGraph class (such as {@link EFGraph}) which provides {@linkplain LazyIntSkippableIterator
 * skippable iterators} with fast skipping, adjacency can be tested more quickly (e.g., essentially
 * in constant time in the case of {@link EFGraph}).
 *
 * @see ImmutableBigGraphAdapter
 * @author Sebastiano Vigna
 */

public abstract class ImmutableGraphAdapter<E extends IntIntPair>
    extends
    AbstractGraph<Integer, E>
{

    /** The underlying graph. */
    protected final ImmutableGraph immutableGraph;
    /** The number of nodes of {@link #immutableGraph}. */
    protected final int n;
    /**
     * The number of edges, cached, or -1 if it still unknown. This will have to be computed by
     * enumeration for undirected graphs, as we do not know how many loops are present, and for
     * graphs which do not support {@link ImmutableGraph#numArcs()}.
     */
    protected long m = -1;

    protected ImmutableGraphAdapter(
        final ImmutableGraph immutableGraph)
    {
        this.immutableGraph = immutableGraph;
        this.n = immutableGraph.numNodes();
    }

    @Override
    public Set<E> getAllEdges(
        final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return null;
        final int x = sourceVertex;
        final int y = targetVertex;
        if (x < 0 || x >= n || y < 0 || y >= n)
            return null;

        return containsEdgeFast(x, y)
            ? Collections.singleton(makeEdge(x, y))
            : Collections.emptySet();
    }

    protected abstract E makeEdge(int x, int y);

    @Override
    public E getEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return null;
        final int x = sourceVertex;
        final int y = targetVertex;
        return containsEdgeFast(x, y) ? makeEdge(x, y) : null;
    }

    @Override
    public Supplier<Integer> getVertexSupplier()
    {
        return null;
    }

    @Override
    public Supplier<E> getEdgeSupplier()
    {
        return null;
    }

    @Override
    public E addEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addEdge(
        final Integer sourceVertex, final Integer targetVertex, final E e)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer addVertex()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addVertex(final Integer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        if (sourceVertex == null || targetVertex == null)
            return false;
        return containsEdgeFast(sourceVertex, targetVertex);
    }

    protected boolean containsEdgeFast(final int x, final int y)
    {
        if (x < 0 || x >= n || y < 0 || y >= n)
            return false;
        final LazyIntIterator successors = immutableGraph.successors(x);
        if (successors instanceof LazyIntSkippableIterator) {
            // Fast skipping available
            return y == ((LazyIntSkippableIterator) successors).skipTo(y);
        } else
            for (int target; (target = successors.nextInt()) != -1;)
                if (target == y)
                    return true;
        return false;
    }

    @Override
    public boolean containsVertex(final Integer v)
    {
        if (v == null)
            return false;
        final int x = v;
        return x >= 0 && x < n;
    }

    @Override
    public E removeEdge(final Integer sourceVertex, final Integer targetVertex)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeEdge(final E e)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeVertex(final Integer v)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Integer> vertexSet()
    {
        return IntSets.fromTo(0, n);
    }

    @Override
    public Integer getEdgeSource(final E e)
    {
        return e.leftInt();
    }

    @Override
    public Integer getEdgeTarget(final E e)
    {
        return e.rightInt();
    }

    @Override
    public double getEdgeWeight(final E e)
    {
        return DEFAULT_EDGE_WEIGHT;
    }

    @Override
    public void setEdgeWeight(final E e, final double weight)
    {
        if (weight != 1)
            throw new UnsupportedOperationException();
    }
}
