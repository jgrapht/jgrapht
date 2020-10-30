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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.GraphIterables;
import org.jgrapht.GraphType;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.DefaultGraphType.Builder;

import com.google.common.collect.Iterables;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;

import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.big.webgraph.LazyLongIterators;
import it.unimi.dsi.big.webgraph.LazyLongSkippableIterator;
import it.unimi.dsi.big.webgraph.NodeIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashBigSet;
import it.unimi.dsi.lang.FlyweightPrototype;

/**
 * A graph adapter class using <a href="http://webgraph.di.unimi.it/">WebGraph (big)</a>'s
 * {@link ImmutableGraph}.
 *
 * <p>
 * This class is equivalent to {@link ImmutableGraphAdapterEndpointPair}, except that nodes are
 * instances of {@link Long}. Note that due to the return value of {@link #degreeOf(Long)},
 * {@link #inDegreeOf(Long)}, and {@link #outDegreeOf(Long)} being an {@code int}, we can only
 * support graphs whose degree is at most {@link Integer#MAX_VALUE}. The class
 * {@link ImmutableBigGraphAdapterLongArray} provides a more space-efficient adapter, but it has an
 * edge representation that does not implement equality.
 *
 * <p>
 * Some care must be taken when specifying an edge using constants:
 *
 * <pre>
 * EndpointPair.ordered(0, 1)
 * adapter.getEdge(0, 1)
 *
 * EndpointPair.ordered(0L, 1L)
 * adapter.getEdge(0L, 1L)
 * </pre>
 *
 * <p>
 * The first two examples will not work, because type inference will create instances of
 * {@link Integer} instead of {@link Long}.
 *
 * <p>
 * If necessary, you can adapt a {@linkplain it.unimi.dsi.webgraph.ImmutableGraph standard WebGraph graph} using the suitable
 * {@linkplain ImmutableGraph#wrap(it.unimi.dsi.webgraph.ImmutableGraph) wrapper}.
 *
 * @see ImmutableGraphAdapterEndpointPair
 * @see ImmutableBigGraphAdapterLongArray
 * @author Sebastiano Vigna
 */

public class ImmutableBigGraphAdapterEndpointPair extends AbstractGraph<Long, EndpointPair<Long>> implements FlyweightPrototype<ImmutableBigGraphAdapterEndpointPair> {

	/** The underlying graph. */
	private final ImmutableGraph immutableGraph;
	/**
	 * The transpose of {@link #immutableGraph}, for a directed graph with full support; {@code null},
	 * for a directed graph with access to outgoing edges, only; {@link #immutableGraph}, for an
	 * undirected graph (in which case, {@link #immutableGraph} must be symmetric).
	 */
	private final ImmutableGraph immutableTranspose;
	/** The cached value of {@link #immutableGraph} != {@link #immutableTranspose}. */
	private final boolean directed;
	/** The number of nodes of {@link #immutableGraph}. */
	private final long n;
	/**
	 * The number of edges, cached, or -1 if it still unknown. This will have to be computed by
	 * enumeration for undirected graphs, as we do not know how many loops are present, and for graphs
	 * which do not support {@link ImmutableGraph#numArcs()}.
	 */
	private long m = -1;

	/**
	 * Creates an adapter for an undirected (i.e., symmetric) big immutable graph.
	 *
	 * <p>
	 * It is your responsibility that the provided graph has is symmetric (for each arc
	 * <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> there is an arc&nbsp;<var>y</var>&nbsp;&rarr;
	 * <var>x</var>). If this property is not true, results will be unpredictable.
	 *
	 * @param immutableGraph a symmetric big immutable graph.
	 * @return an {@linkplain GraphType#isUndirected() undirected} {@link Graph}.
	 */
	public static ImmutableBigGraphAdapterEndpointPair undirected(final ImmutableGraph immutableGraph) {
		return new ImmutableBigGraphAdapterEndpointPair(immutableGraph, immutableGraph);
	}

	/**
	 * Creates an adapter for a directed big immutable graph.
	 *
	 * <p>
	 * It is your responsibility that the two provided graphs are one the transpose of the other (for
	 * each arc <var>x</var>&nbsp;&rarr;&nbsp;<var>y</var> in a graph there must be an arc
	 * <var>y</var>&nbsp;&rarr;&nbsp;<var>x</var> in the other). If this property is not true, results
	 * will be unpredictable.
	 *
	 * @param immutableGraph a big immutable graph.
	 * @param immutableTranspose its transpose.
	 * @return an {@linkplain GraphType#isDirected() directed} {@link Graph}.
	 */
	public static ImmutableBigGraphAdapterEndpointPair directed(final ImmutableGraph immutableGraph, final ImmutableGraph immutableTranspose) {
		return new ImmutableBigGraphAdapterEndpointPair(immutableGraph, immutableTranspose);
	}

	/**
	 * Creates an adapter for a big directed immutable graph exposing only methods based on outgoing
	 * edges.
	 *
	 * @param immutableGraph a big immutable graph.
	 * @return an {@linkplain GraphType#isDirected() directed} {@link Graph} providing only methods
	 *         based on outgoing edges; all other methods will throw a {@link NullPointerException}.
	 */
	public static ImmutableBigGraphAdapterEndpointPair directed(final ImmutableGraph immutableGraph) {
		return new ImmutableBigGraphAdapterEndpointPair(immutableGraph, null);
	}

	protected ImmutableBigGraphAdapterEndpointPair(final ImmutableGraph immutableGraph, final ImmutableGraph immutableTranspose) {
		this.immutableGraph = immutableGraph;
		this.immutableTranspose = immutableTranspose;
		this.directed = immutableGraph != immutableTranspose;
		this.n = immutableGraph.numNodes();
		if (immutableTranspose != null && n != immutableTranspose.numNodes()) throw new IllegalArgumentException("The graph has " + n + " nodes, but the transpose has " + immutableTranspose.numNodes());
	}

	@Override
	public Set<EndpointPair<Long>> getAllEdges(final Long sourceVertex, final Long targetVertex) {
		if (sourceVertex == null || targetVertex == null) return null;
		final long x = sourceVertex;
		final long y = targetVertex;
		if (x < 0 || x >= n || y < 0 || y >= n) return null;
		return containsEdgeFast(x, y) ? Collections.singleton(directed ? EndpointPair.ordered(sourceVertex, targetVertex) : EndpointPair.unordered(sourceVertex, targetVertex)) : Collections.emptySet();
	}

	@Override
	public EndpointPair<Long> getEdge(final Long sourceVertex, final Long targetVertex) {
		if (sourceVertex == null || targetVertex == null) return null;
		return containsEdgeFast(sourceVertex.intValue(), targetVertex.intValue()) ? (directed ? EndpointPair.ordered(sourceVertex, targetVertex) : EndpointPair.unordered(sourceVertex, targetVertex)) : null;
	}

	@Override
	public Supplier<Long> getVertexSupplier() {
		return null;
	}

	@Override
	public Supplier<EndpointPair<Long>> getEdgeSupplier() {
		return null;
	}

	@Override
	public EndpointPair<Long> addEdge(final Long sourceVertex, final Long targetVertex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addEdge(final Long sourceVertex, final Long targetVertex, final EndpointPair<Long> e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long addVertex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addVertex(final Long v) {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean containsEdge(final EndpointPair<Long> e) {
		if (e == null) return false;
		final boolean ordered = e.isOrdered();
		if (directed) return ordered ? containsEdge(e.source(), e.target()) : false;
		else return ordered ? false : containsEdge(e.nodeU(), e.nodeV());
	}

	@Override
	public boolean containsEdge(final Long sourceVertex, final Long targetVertex) {
		if (sourceVertex == null || targetVertex == null) return false;
		return containsEdgeFast(sourceVertex.intValue(), targetVertex.intValue());
	}

	private boolean containsEdgeFast(final long x, final long y) {
		if (x < 0 || x >= n || y < 0 || y >= n) return false;
		final LazyLongIterator successors = immutableGraph.successors(x);
		if (successors instanceof LazyLongSkippableIterator) {
			// Fast skipping available
			return y == ((LazyLongSkippableIterator)successors).skipTo(y);
		} else for (long target; (target = successors.nextLong()) != -1;) if (target == y) return true;
		return false;
	}

	@Override
	public boolean containsVertex(final Long v) {
		if (v == null) return false;
		final long x = v;
		return x >= 0 && x < n;
	}

	@Override
	public Set<EndpointPair<Long>> edgeSet() {
		final NodeIterator nodeIterator = immutableGraph.nodeIterator();
		long m = 16; // Min hash table size
		try {
			m = immutableGraph.numArcs();
		} catch (final UnsupportedOperationException e) {}
		final ObjectOpenHashBigSet<EndpointPair<Long>> edges = new ObjectOpenHashBigSet<>(m);
		for (long i = 0; i < n; i++) {
			final long x = nodeIterator.nextLong();
			final LazyLongIterator successors = nodeIterator.successors();
			if (directed) for (long y; (y = successors.nextLong()) != -1;) edges.add(EndpointPair.ordered(x, y));
			else for (long y; (y = successors.nextLong()) != -1;) if (x <= y) edges.add(EndpointPair.unordered(x, y));
		}
		return edges;
	}


	@Override
	public int degreeOf(final Long vertex) {
		if (directed) {
			final long d = inDegreeOf(vertex) + outDegreeOf(vertex);
			if (d >= Integer.MAX_VALUE) throw new ArithmeticException();
			return (int)d;
		} else return inDegreeOf(vertex);
	}

	@Override
	public Set<EndpointPair<Long>> edgesOf(final Long source) {
		final ObjectLinkedOpenHashSet<EndpointPair<Long>> set = new ObjectLinkedOpenHashSet<>();
		if (directed) {
			final LazyLongIterator successors = immutableGraph.successors(source);
			for (long target; (target = successors.nextLong()) != -1;) set.add(EndpointPair.ordered(source, target));
			final LazyLongIterator predecessors = immutableTranspose.successors(source);
			for (long target; (target = predecessors.nextLong()) != -1;) set.add(EndpointPair.ordered(target, source));
		} else {
			final LazyLongIterator predecessors = immutableTranspose.successors(source);
			for (long target; (target = predecessors.nextLong()) != -1;) set.add(EndpointPair.unordered(target, source));
		}
		return set;
	}

	@Override
	public int inDegreeOf(final Long vertex) {
		final long d = immutableTranspose.outdegree(vertex);
		if (d >= Integer.MAX_VALUE) throw new ArithmeticException();
		return (int)d;
	}

	@Override
	public Set<EndpointPair<Long>> incomingEdgesOf(final Long vertex) {
		final ObjectLinkedOpenHashSet<EndpointPair<Long>> set = new ObjectLinkedOpenHashSet<>();
		final LazyLongIterator predecessors = immutableTranspose.successors(vertex);
		if (directed) for (long target; (target = predecessors.nextLong()) != -1;) set.add(EndpointPair.ordered(target, vertex));
		else for (long target; (target = predecessors.nextLong()) != -1;) set.add(EndpointPair.unordered(target, vertex));
		return set;
	}

	@Override
	public int outDegreeOf(final Long vertex) {
		final long d = immutableGraph.outdegree(vertex);
		if (d >= Integer.MAX_VALUE) throw new ArithmeticException();
		return (int)d;
	}

	@Override
	public Set<EndpointPair<Long>> outgoingEdgesOf(final Long vertex) {
		final ObjectLinkedOpenHashSet<EndpointPair<Long>> set = new ObjectLinkedOpenHashSet<>();
		final LazyLongIterator successors = immutableGraph.successors(vertex);
		if (directed) for (long target; (target = successors.nextLong()) != -1;) set.add(EndpointPair.ordered(vertex, target));
		else for (long target; (target = successors.nextLong()) != -1;) set.add(EndpointPair.unordered(vertex, target));
		return set;
	}

	@Override
	public EndpointPair<Long> removeEdge(final Long sourceVertex, final Long targetVertex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeEdge(final EndpointPair<Long> e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeVertex(final Long v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Long> vertexSet() {
		return new AbstractLongSet() {
			@Override
			public boolean contains(final long x) {
				return x >= 0 && x < n;
			}

			@Override
			public int size() {
				return n <= Integer.MAX_VALUE ? (int)n : -1;
			}

			@Override
			public LongIterator iterator() {
				return LongIterators.fromTo(0, n);
			}
		};
	}

	@Override
	public Long getEdgeSource(final EndpointPair<Long> e) {
		return e.isOrdered() ? e.source() : Math.min(e.nodeU(), e.nodeV());
	}

	@Override
	public Long getEdgeTarget(final EndpointPair<Long> e) {
		return e.isOrdered() ? e.target() : Math.max(e.nodeU(), e.nodeV());
	}

	@Override
	public double getEdgeWeight(final EndpointPair<Long> e) {
		return DEFAULT_EDGE_WEIGHT;
	}

	@Override
	public void setEdgeWeight(final EndpointPair<Long> e, final double weight) {
		if (weight != 1) throw new UnsupportedOperationException();
	}

	@Override
	public GraphType getType() {
		final Builder builder = new DefaultGraphType.Builder().weighted(false).modifiable(false).allowMultipleEdges(false).allowSelfLoops(true);
		return directed ? builder.directed().build() : builder.undirected().build();
	}

	@Override
	public ImmutableBigGraphAdapterEndpointPair copy() {
		if (directed) return new ImmutableBigGraphAdapterEndpointPair(immutableGraph.copy(), immutableTranspose.copy());
		final ImmutableGraph copy = immutableGraph.copy();
		return new ImmutableBigGraphAdapterEndpointPair(copy, copy);
	}

	// TODO: Replace with fastutil method
	private long size(final Iterable<?> iterable) {
		long c = 0;
		for (final Object o : iterable) c++;
		return c;
	}

	private final GraphIterables<Long, EndpointPair<Long>> ITERABLES = new GraphIterables<>() {
		@Override
		public long vertexCount() {
			return n;
		}

		@Override
		public long edgeCount() {
			if (m != -1) return m;
			if (directed) {
				try {
					return m = immutableGraph.numArcs();
				} catch (final UnsupportedOperationException e) {
				}
			}
			return m = size(edges());
		}

		// TODO: remove
		@Override
		public Iterable<EndpointPair<Long>> allEdges(final Long sourceVertex, final Long targetVertex) {
			return getAllEdges(sourceVertex, targetVertex);
		}

		@Override
		public long degreeOf(final Long vertex) {
			return directed ? inDegreeOf(vertex) + outDegreeOf(vertex) : inDegreeOf(vertex);
		}

		@Override
		public Iterable<EndpointPair<Long>> edgesOf(final Long source) {
			return directed ? Iterables.concat(outgoingEdgesOf(source), incomingEdgesOf(source)) : outgoingEdgesOf(source);
		}

		@Override
		public long inDegreeOf(final Long vertex) {
			return immutableTranspose.outdegree(vertex);
		}

		@Override
		public Iterable<EndpointPair<Long>> incomingEdgesOf(final Long vertex) {
			return () -> new Iterator<>() {
				final LazyLongIterator successors = immutableTranspose.successors(vertex);
				long y = -1;

				@Override
				public boolean hasNext() {
					if (y == -1) y = successors.nextLong();
					return y != -1;
				}

				@Override
				public EndpointPair<Long> next() {
					final EndpointPair<Long> edge = directed ? EndpointPair.ordered(y, vertex) : EndpointPair.unordered(y, vertex);
					y = -1;
					return edge;
				}
			};
		}

		@Override
		public long outDegreeOf(final Long vertex) {
			return immutableGraph.outdegree(vertex);
		}

		@Override
		public Iterable<EndpointPair<Long>> outgoingEdgesOf(final Long vertex) {
			return () -> new Iterator<>() {
				final LazyLongIterator successors = immutableGraph.successors(vertex);
				long y = -1;

				@Override
				public boolean hasNext() {
					if (y == -1) y = successors.nextLong();
					return y != -1;
				}

				@Override
				public EndpointPair<Long> next() {
					final EndpointPair<Long> edge = directed ? EndpointPair.ordered(vertex, y) : EndpointPair.unordered(vertex, y);
					y = -1;
					return edge;
				}
			};
		}

		// TODO: remove
		@Override
		public Iterable<Long> vertices() {
			return vertexSet();
		}

		@Override
		public Iterable<EndpointPair<Long>> edges() {
			return () -> new Iterator<>() {
				final NodeIterator nodeIterator = immutableGraph.nodeIterator();
				LazyLongIterator successors = LazyLongIterators.EMPTY_ITERATOR;
				long x, y = -1;

				@Override
				public boolean hasNext() {
					if (y != -1) return true;
					do {
						while ((y = successors.nextLong()) == -1) {
							if (!nodeIterator.hasNext()) return false;
							x = nodeIterator.nextLong();
							successors = nodeIterator.successors();
						}
					} while (!directed && y < x);
					return true;
				}

				@Override
				public EndpointPair<Long> next() {
					if (!hasNext()) throw new NoSuchElementException();
					final EndpointPair<Long> edge = directed ? EndpointPair.ordered(x, y) : EndpointPair.unordered(x, y);
					y = -1;
					return edge;
				}
			};
		}
	};

	@Override
	public GraphIterables<Long, EndpointPair<Long>> iterables() {
		return ITERABLES;
	}
}