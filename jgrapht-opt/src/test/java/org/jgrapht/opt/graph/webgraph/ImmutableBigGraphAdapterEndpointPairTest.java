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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.google.common.graph.EndpointPair;

import it.unimi.dsi.big.webgraph.EFGraph;
import it.unimi.dsi.big.webgraph.ImmutableGraph;
import it.unimi.dsi.big.webgraph.LazyLongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.webgraph.ArrayListMutableGraph;
import it.unimi.dsi.webgraph.Transform;
import it.unimi.dsi.webgraph.examples.ErdosRenyiGraph;

public class ImmutableBigGraphAdapterEndpointPairTest {
	@Test
	public void testSmallRandom() {
		for (final int size : new int[] { 10, 100, 500 }) {
			final it.unimi.dsi.webgraph.ImmutableGraph mg = new ArrayListMutableGraph(new ErdosRenyiGraph(size, .1, 0L, true)).immutableView();
			final ImmutableGraph g = ImmutableGraph.wrap(mg);
			final ImmutableBigGraphAdapterEndpointPair a = new ImmutableBigGraphAdapterEndpointPair(g, ImmutableGraph.wrap(Transform.transpose(mg)));

			assertEquals(g.numNodes(), a.vertexSet().size());
			assertEquals(g.numNodes(), a.iterables().vertexCount());
			assertEquals(g.numArcs(), a.iterables().edgeCount());
			for (long x = 0L; x < size; x++) {
				final LazyLongIterator successors = g.successors(x);
				for (long y; (y = successors.nextLong()) != -1L;) assertTrue(a.containsEdge(x, y));
			}

			assertNull(a.getAllEdges(0L, -1L));
			assertNull(a.getAllEdges(-1L, -1L));
			assertNull(a.getAllEdges(-1L, 0L));
			assertNull(a.getAllEdges(0L, null));
			assertNull(a.getAllEdges(null, 0L));
			assertNull(a.getAllEdges(null, null));
		}
	}

	public static File storeTempGraph(final ImmutableGraph g) throws IOException, IllegalArgumentException, SecurityException {
		final File basename = File.createTempFile(ImmutableBigGraphAdapterEndpointPairTest.class.getSimpleName(), "test");
		EFGraph.store(g, basename.toString());
		basename.deleteOnExit();
		new File(basename + EFGraph.GRAPH_EXTENSION).deleteOnExit();
		new File(basename + EFGraph.PROPERTIES_EXTENSION).deleteOnExit();
		new File(basename + EFGraph.OFFSETS_EXTENSION).deleteOnExit();
		return basename;
	}

	@Test
	public void testSmall() throws IllegalArgumentException, SecurityException, IOException {
		final ArrayListMutableGraph m = new ArrayListMutableGraph();
		m.addNodes(3);
		m.addArc(0, 1);
		m.addArc(0, 2);
		m.addArc(1, 2);

		final it.unimi.dsi.webgraph.ImmutableGraph mg = m.immutableView();
		final ImmutableGraph g = ImmutableGraph.wrap(mg);
		final ImmutableGraph t = ImmutableGraph.wrap(Transform.transpose(mg));
		final File basename = storeTempGraph(g);
		final EFGraph ef = EFGraph.load(basename.toString());

		/*
		 * Assertions.assertThrows(IllegalArgumentException.class, () -> {
		 *
		 * @SuppressWarnings("unused") final ImmutableBigGraphAdapterEndpointPair dummy =
		 * ImmutableBigGraphAdapterEndpointPair.directed(g, ImmutableGraph.wrap(new
		 * ArrayListMutableGraph().immutableView())); });
		 */

		final ImmutableBigGraphAdapterEndpointPair a = new ImmutableBigGraphAdapterEndpointPair(g, t);
		final ImmutableBigGraphAdapterEndpointPair b = new ImmutableBigGraphAdapterEndpointPair(ef, ImmutableGraph.wrap(Transform.transpose(mg)));

		assertEquals(g.numNodes(), a.vertexSet().size());
		for (long x = 0; x < g.numNodes(); x++) {
			final LazyLongIterator successors = g.successors(x);
			for (long y; (y = successors.nextLong()) != -1L;) assertTrue(a.containsEdge(x, y));
		}

		assertNull(a.getVertexSupplier());
		assertNull(a.getEdgeSupplier());

		assertFalse(a.containsVertex(null));

		assertNull(a.getEdge(0L, -1L));
		assertNull(a.getEdge(-1L, -1L));
		assertNull(a.getEdge(-1L, 0L));
		assertNull(a.getEdge(0L, g.numNodes()));
		assertNull(a.getEdge(g.numNodes(), g.numNodes()));
		assertNull(a.getEdge(g.numNodes(), 0L));
		assertNull(a.getEdge(0L, null));
		assertNull(a.getEdge(null, 0L));
		assertNull(a.getEdge(null, null));
		assertNull(a.getEdge(1L, 0L));
		assertEquals(EndpointPair.ordered(0L, 1L), a.getEdge(0L, 1L));

		assertNull(a.getAllEdges(0L, -1L));
		assertNull(a.getAllEdges(-1L, -1L));
		assertNull(a.getAllEdges(-1L, 0L));
		assertNull(a.getAllEdges(0L, null));
		assertNull(a.getAllEdges(null, 0L));
		assertNull(a.getAllEdges(null, null));
		assertEquals(Collections.emptySet(), a.getAllEdges(1L, 0L));
		assertEquals(Collections.singleton(EndpointPair.ordered(0L, 1L)), a.getAllEdges(0L, 1L));
		assertEquals(Collections.singleton(EndpointPair.ordered(0L, 1L)), new ObjectOpenHashSet<>(a.iterables().allEdges(0L, 1L).iterator()));

		assertFalse(a.containsEdge(0L, null));
		assertFalse(a.containsEdge(null, 0L));
		assertTrue(a.containsEdge(0L, 1L));

		assertTrue(b.containsVertex(0L));
		assertFalse(b.containsVertex(3L));
		assertFalse(b.containsVertex(-1L));

		assertTrue(b.containsEdge(EndpointPair.ordered(0L, 2L)));
		assertTrue(b.containsEdge(EndpointPair.ordered(1L, 2L)));
		assertFalse(b.containsEdge(EndpointPair.ordered(2L, 1L)));
		assertFalse(b.containsEdge(null));

		assertFalse(a.containsEdge(EndpointPair.unordered(0L, 1L)));

		assertEquals(2, a.degreeOf(1L));
		assertEquals(1, a.inDegreeOf(1L));
		assertEquals(1, a.outDegreeOf(1L));
		assertEquals(2, a.iterables().degreeOf(1L));
		assertEquals(1, a.iterables().inDegreeOf(1L));
		assertEquals(1, a.iterables().outDegreeOf(1L));

		assertEquals(new ObjectOpenHashSet<EndpointPair<Long>>(new EndpointPair[] { EndpointPair.ordered(0L, 1L),
				EndpointPair.ordered(0L, 2L), EndpointPair.ordered(1L, 2L) }), new ObjectOpenHashSet<>(a.edgeSet()));
		assertEquals(new ObjectOpenHashSet<EndpointPair<Long>>(new EndpointPair[] { EndpointPair.ordered(0L, 1L),
				EndpointPair.ordered(0L, 2L),
				EndpointPair.ordered(1L, 2L) }), new ObjectOpenHashSet<>(a.iterables().edges().iterator()));

		assertEquals(new ObjectOpenHashSet<EndpointPair<Long>>(new EndpointPair[] { EndpointPair.ordered(0L, 1L),
				EndpointPair.ordered(1L, 2L) }), a.edgesOf(1L));
		assertEquals(new ObjectOpenHashSet<EndpointPair<Long>>(new EndpointPair[] { EndpointPair.ordered(0L, 1L),
				EndpointPair.ordered(1L, 2L) }), new ObjectOpenHashSet<>(a.iterables().edgesOf(1L).iterator()));

		assertEquals(new ObjectOpenHashSet(new EndpointPair[] { EndpointPair.ordered(0L, 1L) }), a.incomingEdgesOf(1L));
		assertEquals(new ObjectOpenHashSet(new EndpointPair[] {
				EndpointPair.ordered(0L, 1L) }), new ObjectOpenHashSet<>(a.iterables().incomingEdgesOf(1L).iterator()));

		assertEquals(new ObjectOpenHashSet(new EndpointPair[] { EndpointPair.ordered(1L, 2L) }), a.outgoingEdgesOf(1L));
		assertEquals(new ObjectOpenHashSet(new EndpointPair[] {
				EndpointPair.ordered(1L, 2L) }), new ObjectOpenHashSet<>(a.iterables().outgoingEdgesOf(1L).iterator()));

		final Set<Long> v = a.vertexSet();
		assertTrue(v.contains(0L));
		assertFalse(v.contains(-1L));
		assertFalse(v.contains(3L));
		assertEquals(new LongOpenHashSet(v.iterator()), new LongOpenHashSet(new long[] { 0L, 1L, 2L }));

		assertEquals(1, a.getEdgeSource(EndpointPair.ordered(1L, 2L)).longValue());
		assertEquals(2, a.getEdgeTarget(EndpointPair.ordered(1L, 2L)).longValue());

		assertEquals(1, a.getEdgeWeight(EndpointPair.ordered(1L, 2L)), 0);
		a.setEdgeWeight(EndpointPair.ordered(0L, 1L), 1);

		/*
		 * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.addEdge(0L, 1L); });
		 *
		 * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.addEdge(0L, 1L,
		 * EndpointPair.ordered(0L, 1L)); });
		 *
		 * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.removeEdge(0L, 1L); });
		 *
		 * Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		 * a.removeEdge(EndpointPair.ordered(0L, 1L)); });
		 *
		 * Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		 * a.setEdgeWeight(EndpointPair.ordered(0L, 1L), 0L); });
		 *
		 * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.addVertex(); });
		 *
		 * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.addVertex(0L); });
		 *
		 * Assertions.assertThrows(UnsupportedOperationException.class, () -> { a.removeVertex(0L); });
		 */
	}

	@Test
	public void testSmallUndirected() throws IllegalArgumentException, SecurityException, IOException {
		final ArrayListMutableGraph m = new ArrayListMutableGraph();
		m.addNodes(4);
		m.addArc(0, 1);
		m.addArc(0, 2);
		m.addArc(1, 3);
		m.addArc(2, 3);
		m.addArc(1, 1);
		m.addArc(3, 3);

		final ImmutableGraph g = ImmutableGraph.load(storeTempGraph(ImmutableGraph.wrap(Transform.symmetrize(m.immutableView()))).toString());

		final ImmutableBigGraphAdapterEndpointPair a = ImmutableBigGraphAdapterEndpointPair.undirected(g);

		assertEquals(g.numNodes(), a.vertexSet().size());
		for (long x = 0; x < g.numNodes(); x++) {
			final LazyLongIterator successors = g.successors(x);
			for (long y; (y = successors.nextLong()) != -1;) assertTrue(a.containsEdge(x, y));
		}

		assertEquals(6, a.iterables().edgeCount());
		assertEquals(6, a.edgeSet().size());
		assertNull(a.getEdge(2L, 2L));
		assertEquals(EndpointPair.unordered(0L, 1L), a.getEdge(0L, 1L));
		assertFalse(a.containsEdge(EndpointPair.ordered(0L, 1L)));

		assertTrue(a.getEdgeSource(a.getEdge(0L, 1L)) == 0 && a.getEdgeTarget(a.getEdge(0L, 1L)) == 1 || a.getEdgeSource(a.getEdge(0L, 1L)) == 1 && a.getEdgeTarget(a.getEdge(0L, 1L)) == 0);

		assertEquals(new ObjectOpenHashSet<EndpointPair<Long>>(new EndpointPair[] { EndpointPair.unordered(0L, 2L),
				EndpointPair.unordered(2L, 3L) }), a.edgesOf(2L));
		assertEquals(new ObjectOpenHashSet<EndpointPair<Long>>(new EndpointPair[] { EndpointPair.unordered(0L, 2L),
				EndpointPair.unordered(2L, 3L) }), a.incomingEdgesOf(2L));
		assertEquals(new ObjectOpenHashSet<EndpointPair<Long>>(new EndpointPair[] { EndpointPair.unordered(0L, 2L),
				EndpointPair.unordered(2L, 3L) }), a.outgoingEdgesOf(2L));
		assertEquals(new ObjectOpenHashSet<EndpointPair<Long>>(new EndpointPair[] { EndpointPair.unordered(0L, 2L),
				EndpointPair.unordered(2L, 3L) }), new ObjectOpenHashSet<>(a.iterables().edgesOf(2L).iterator()));
		assertEquals(new ObjectOpenHashSet<EndpointPair<Long>>(new EndpointPair[] { EndpointPair.unordered(0L, 2L),
				EndpointPair.unordered(2L, 3L) }), new ObjectOpenHashSet<>(a.iterables().incomingEdgesOf(2L).iterator()));
		assertEquals(new ObjectOpenHashSet<EndpointPair<Long>>(new EndpointPair[] { EndpointPair.unordered(0L, 2L),
				EndpointPair.unordered(2L, 3L) }), new ObjectOpenHashSet<>(a.iterables().outgoingEdgesOf(2L).iterator()));
		assertEquals(Collections.singleton(EndpointPair.unordered(0L, 1L)), a.getAllEdges(0L, 1L));
		assertEquals(Collections.singleton(EndpointPair.unordered(0L, 1L)), new ObjectOpenHashSet<>(a.iterables().allEdges(0L, 1L).iterator()));

		assertEquals(3, a.degreeOf(1L));
		assertEquals(3, a.iterables().degreeOf(1L));
	}

	@Test
	public void testCopy() throws IllegalArgumentException, SecurityException, IOException {
		final ArrayListMutableGraph m = new ArrayListMutableGraph();
		m.addNodes(4);
		m.addArc(0, 1);
		m.addArc(0, 2);
		m.addArc(1, 3);
		m.addArc(2, 3);
		final it.unimi.dsi.webgraph.ImmutableGraph v = m.immutableView();
		ImmutableBigGraphAdapterEndpointPair a = ImmutableBigGraphAdapterEndpointPair.directed(ImmutableGraph.wrap(v), ImmutableGraph.wrap(Transform.transpose(v)));
		assertEquals(a, a.copy());

		final it.unimi.dsi.webgraph.ImmutableGraph g = Transform.symmetrize(v);

		a = ImmutableBigGraphAdapterEndpointPair.undirected(ImmutableGraph.wrap(g));
		assertEquals(a, a.copy());
	}

	@Test
	public void testType() throws IllegalArgumentException, SecurityException, IOException {
		final ArrayListMutableGraph m = new ArrayListMutableGraph();
		m.addNodes(4);
		m.addArc(0, 1);
		m.addArc(0, 2);
		m.addArc(1, 3);
		m.addArc(2, 3);
		final it.unimi.dsi.webgraph.ImmutableGraph v = m.immutableView();
		ImmutableBigGraphAdapterEndpointPair a = ImmutableBigGraphAdapterEndpointPair.directed(ImmutableGraph.wrap(v), ImmutableGraph.wrap(Transform.transpose(v)));
		assertTrue(a.getType().isDirected());
		assertFalse(a.getType().isUndirected());
		assertEquals(new ObjectOpenHashSet<EndpointPair<Integer>>(new EndpointPair[] { EndpointPair.ordered(0L, 2L),
				EndpointPair.ordered(0L, 1L), EndpointPair.ordered(1L, 3L),
				EndpointPair.ordered(2L, 3L) }), a.edgeSet());
		assertEquals(new ObjectOpenHashSet<EndpointPair<Integer>>(new EndpointPair[] { EndpointPair.ordered(0L, 2L),
				EndpointPair.ordered(0L, 1L), EndpointPair.ordered(1L, 3L),
				EndpointPair.ordered(2L, 3L) }), new ObjectOpenHashSet<>(a.edgeSet().iterator()));

		final it.unimi.dsi.webgraph.ImmutableGraph g = Transform.symmetrize(v);

		a = ImmutableBigGraphAdapterEndpointPair.undirected(ImmutableGraph.wrap(g));
		assertTrue(a.getType().isUndirected());
		assertFalse(a.getType().isDirected());
		assertEquals(new ObjectOpenHashSet<EndpointPair<Integer>>(new EndpointPair[] { EndpointPair.unordered(0L, 2L),
				EndpointPair.unordered(0L, 1L), EndpointPair.unordered(1L, 3L),
				EndpointPair.unordered(2L, 3L) }), a.edgeSet());
		assertEquals(new ObjectOpenHashSet<EndpointPair<Integer>>(new EndpointPair[] { EndpointPair.unordered(0L, 2L),
				EndpointPair.unordered(0L, 1L), EndpointPair.unordered(1L, 3L),
				EndpointPair.unordered(2L, 3L) }), new ObjectOpenHashSet<>(a.iterables().edges().iterator()));
	}

	@Test
	public void testAdjacencyCheck() throws IllegalArgumentException, SecurityException, IOException {
		final ArrayListMutableGraph m = new ArrayListMutableGraph();
		m.addNodes(100);
		for (int i = 0; i < 30; i++) m.addArc(0, i);
		final it.unimi.dsi.webgraph.ImmutableGraph v = m.immutableView();
		ImmutableBigGraphAdapterEndpointPair a = ImmutableBigGraphAdapterEndpointPair.directed(ImmutableGraph.wrap(v), ImmutableGraph.wrap(Transform.transpose(v)));
		assertEquals(EndpointPair.ordered(0L, 1L), a.getEdge(0L, 1L));
		assertEquals(null, a.getEdge(1L, 0L));
		assertEquals(null, a.getEdge(0L, 50L));

		a = ImmutableBigGraphAdapterEndpointPair.directed(ImmutableGraph.wrap(v));
		assertEquals(EndpointPair.ordered(0L, 1L), a.getEdge(0L, 1l));
		assertEquals(null, a.getEdge(1L, 0L));
		assertEquals(null, a.getEdge(0L, 50L));
	}

	@Test
	public void testEdgeCoherence() {
		final ImmutableGraph m = ImmutableGraph.wrap(new ArrayListMutableGraph(2, new int[][] { new int[] { 0, 1 },
				new int[] { 1, 0 } }).immutableView());
		final ImmutableBigGraphAdapterEndpointPair a = ImmutableBigGraphAdapterEndpointPair.undirected(m);

		assertEquals(a.getEdgeSource(a.getEdge(0L, 1L)), a.getEdgeSource(a.getEdge(1L, 0L)));
	}
}
