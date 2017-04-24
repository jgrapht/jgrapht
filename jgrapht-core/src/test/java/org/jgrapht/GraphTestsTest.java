/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht;

import java.util.*;

import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.junit.*;

/**
 * Test class GraphTests.
 * 
 * @author Dimitrios Michail
 * @author David Janos Csillik
 */
public class GraphTestsTest
{
	/*
	 * Tests for public static <V, E> boolean isEmpty(Graph<V, E> graph)
	 */
	@Test
	public void isEmptyWithEmptyGraphWithoutVerticesTest() {
		SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		Assert.assertTrue(GraphTests.isEmpty(graph));
	}

	@Test
	public void isEmptyWithNotEmptyGraphWithVerticesTest() {
		SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		graph.addVertex(1);
		graph.addVertex(2);

		Assert.assertTrue(GraphTests.isEmpty(graph));
	}

	@Test
	public void isEmptyWithNotEmptyGraph2Test() {
		SimpleGraph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		graph.addVertex(1);
		graph.addVertex(2);

		graph.addEdge(1, 2);

		Assert.assertFalse(GraphTests.isEmpty(graph));
	}
	
	/*
	 * Tests for public static <V, E> boolean isSimple(Graph<V, E> graph)
	 */
	@Test
	public void isSimpleWithSimpleGraphTest() {
		Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
		
		graph.addVertex(1);
		graph.addVertex(2);

		graph.addEdge(1, 2);
		graph.addEdge(2, 1);

		Assert.assertTrue(GraphTests.isSimple(graph));
	}
	
	@Test
	public void isSimpleWithSimpleDefaultDirectedGraphTest() {
		Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		graph.addVertex(1);
		graph.addVertex(2);

		graph.addEdge(1, 2);
		graph.addEdge(1, 2);

		Assert.assertTrue(GraphTests.isSimple(graph));
	}
	
	@Test
	public void isSimpleWithDefaultDirectedGraphWithLoopTest() {
		Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		graph.addVertex(1);
		graph.addVertex(2);

		graph.addEdge(1, 2);
		graph.addEdge(2, 1);

		Assert.assertTrue(GraphTests.isSimple(graph));
	}
	
	@Test
	public void isSimpleWithDefaultDirectedGraphWithSelfLoopTest() {
		Graph<Integer, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		graph.addVertex(1);
		graph.addVertex(2);

		graph.addEdge(1, 2);
		DefaultEdge edge = graph.addEdge(2, 2);

		Assert.assertNotNull(edge);
		Assert.assertFalse(GraphTests.isSimple(graph));
	}
	
	@Test
	public void isSimpleWithSimpleMultigraphTest() {
		Graph<Integer, DefaultEdge> graph = new Multigraph<>(DefaultEdge.class);
		
		graph.addVertex(1);
		graph.addVertex(2);

		graph.addEdge(1, 2);

		Assert.assertTrue(GraphTests.isSimple(graph));
	}
	
	@Test
	public void isSimpleWithMultigraphWithMultipleEdgesTest() {
		Graph<Integer, DefaultEdge> graph = new Multigraph<>(DefaultEdge.class);
		
		graph.addVertex(1);
		graph.addVertex(2);

		graph.addEdge(1, 2);
		graph.addEdge(1, 2);

		Assert.assertFalse(GraphTests.isSimple(graph));
	}
	
	
	
	
	@Test
	public void testIsEmpty()
	{
		Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isEmpty(g));
		g.addVertex(1);
		Assert.assertTrue(GraphTests.isEmpty(g));
		g.addVertex(2);
		Assert.assertTrue(GraphTests.isEmpty(g));
		DefaultEdge e = g.addEdge(1, 2);
		Assert.assertFalse(GraphTests.isEmpty(g));
		g.removeEdge(e);
		Assert.assertTrue(GraphTests.isEmpty(g));
	}

	@Test
	public void testIsSimple()
	{
		// test empty
		Graph<Integer, DefaultEdge> g1 = new DefaultDirectedGraph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isSimple(g1));

		Graph<Integer, DefaultEdge> g2 = new SimpleGraph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isSimple(g2));

		Graph<Integer, DefaultEdge> g3 = new DirectedPseudograph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isSimple(g1));

		Graphs.addAllVertices(g3, Arrays.asList(1, 2));
		g3.addEdge(1, 2);
		g3.addEdge(2, 1);
		Assert.assertTrue(GraphTests.isSimple(g3));
		DefaultEdge g3e11 = g3.addEdge(1, 1);
		Assert.assertFalse(GraphTests.isSimple(g3));
		g3.removeEdge(g3e11);
		Assert.assertTrue(GraphTests.isSimple(g3));
		g3.addEdge(2, 1);
		Assert.assertFalse(GraphTests.isSimple(g3));

		Graph<Integer, DefaultEdge> g4 = new Pseudograph<>(DefaultEdge.class);
		Graphs.addAllVertices(g4, Arrays.asList(1, 2));
		Assert.assertTrue(GraphTests.isSimple(g4));
		DefaultEdge g4e12 = g4.addEdge(1, 2);
		g4.addEdge(2, 1);
		Assert.assertFalse(GraphTests.isSimple(g4));
		g4.removeEdge(g4e12);
		Assert.assertTrue(GraphTests.isSimple(g4));
		g4.addEdge(1, 1);
		Assert.assertFalse(GraphTests.isSimple(g4));
	}

	@Test
	public void testIsCompleteDirected()
	{
		Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isComplete(g));
		g.addVertex(1);
		Assert.assertTrue(GraphTests.isComplete(g));
		g.addVertex(2);
		Assert.assertFalse(GraphTests.isComplete(g));
		g.addEdge(1, 2);
		Assert.assertFalse(GraphTests.isComplete(g));
		g.addEdge(2, 1);
		Assert.assertTrue(GraphTests.isComplete(g));
		g.addVertex(3);
		Assert.assertFalse(GraphTests.isComplete(g));
		g.addEdge(1, 3);
		Assert.assertFalse(GraphTests.isComplete(g));
		g.addEdge(3, 1);
		Assert.assertFalse(GraphTests.isComplete(g));
		g.addEdge(2, 3);
		Assert.assertFalse(GraphTests.isComplete(g));
		g.addEdge(3, 2);
		Assert.assertTrue(GraphTests.isComplete(g));

		// check loops
		Graph<Integer, DefaultEdge> g1 = new DirectedPseudograph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isComplete(g1));
		g1.addVertex(1);
		Assert.assertTrue(GraphTests.isComplete(g1));
		g1.addVertex(2);
		Assert.assertFalse(GraphTests.isComplete(g1));
		g1.addEdge(1, 1);
		g1.addEdge(2, 2);
		Assert.assertFalse(GraphTests.isComplete(g1));

		// check multiple edges
		Graph<Integer, DefaultEdge> g2 = new DirectedPseudograph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isComplete(g2));
		Graphs.addAllVertices(g2, Arrays.asList(1, 2, 3));
		Assert.assertFalse(GraphTests.isComplete(g2));
		g2.addEdge(1, 2);
		g2.addEdge(1, 3);
		g2.addEdge(2, 3);
		g2.addEdge(1, 1);
		g2.addEdge(2, 2);
		g2.addEdge(3, 3);
		Assert.assertFalse(GraphTests.isComplete(g2));
	}

	@Test
	public void testIsCompleteUndirected()
	{
		Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isComplete(g));
		g.addVertex(1);
		Assert.assertTrue(GraphTests.isComplete(g));
		g.addVertex(2);
		Assert.assertFalse(GraphTests.isComplete(g));
		g.addEdge(1, 2);
		Assert.assertTrue(GraphTests.isComplete(g));
		g.addVertex(3);
		Assert.assertFalse(GraphTests.isComplete(g));
		g.addEdge(1, 3);
		Assert.assertFalse(GraphTests.isComplete(g));
		g.addEdge(2, 3);
		Assert.assertTrue(GraphTests.isComplete(g));

		// check loops
		Graph<Integer, DefaultEdge> g1 = new Pseudograph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isComplete(g1));
		g1.addVertex(1);
		Assert.assertTrue(GraphTests.isComplete(g1));
		g1.addVertex(2);
		Assert.assertFalse(GraphTests.isComplete(g1));
		g1.addEdge(1, 1);
		Assert.assertFalse(GraphTests.isComplete(g1));

		// check multiple edges
		Graph<Integer, DefaultEdge> g2 = new Pseudograph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isComplete(g2));
		g2.addVertex(1);
		Assert.assertTrue(GraphTests.isComplete(g2));
		g2.addVertex(2);
		Assert.assertFalse(GraphTests.isComplete(g2));
		g2.addEdge(1, 2);
		Assert.assertTrue(GraphTests.isComplete(g2));
		g2.addEdge(1, 2);
		Assert.assertFalse(GraphTests.isComplete(g2));
		g2.addVertex(3);
		g2.addEdge(1, 3);
		Assert.assertFalse(GraphTests.isComplete(g2));
	}

	@Test
	public void testIsConnectedUndirected()
	{
		Graph<Integer, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
		Assert.assertFalse(GraphTests.isConnected(g));
		g.addVertex(1);
		Assert.assertTrue(GraphTests.isConnected(g));
		g.addVertex(2);
		Assert.assertFalse(GraphTests.isConnected(g));
		g.addEdge(1, 2);
		Assert.assertTrue(GraphTests.isConnected(g));
		g.addVertex(3);
		Assert.assertFalse(GraphTests.isConnected(g));
		g.addEdge(1, 3);
		Assert.assertTrue(GraphTests.isConnected(g));
	}

	@Test
	public void testIsConnectedDirected()
	{
		Graph<Integer, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
		Assert.assertFalse(GraphTests.isWeaklyConnected(g));
		Assert.assertFalse(GraphTests.isStronglyConnected(g));
		g.addVertex(1);
		Assert.assertTrue(GraphTests.isWeaklyConnected(g));
		Assert.assertTrue(GraphTests.isStronglyConnected(g));
		g.addVertex(2);
		Assert.assertFalse(GraphTests.isWeaklyConnected(g));
		Assert.assertFalse(GraphTests.isStronglyConnected(g));
		g.addEdge(1, 2);
		Assert.assertTrue(GraphTests.isWeaklyConnected(g));
		Assert.assertFalse(GraphTests.isStronglyConnected(g));
		g.addVertex(3);
		Assert.assertFalse(GraphTests.isWeaklyConnected(g));
		Assert.assertFalse(GraphTests.isStronglyConnected(g));
		g.addEdge(2, 3);
		Assert.assertTrue(GraphTests.isWeaklyConnected(g));
		Assert.assertFalse(GraphTests.isStronglyConnected(g));
		g.addEdge(3, 1);
		Assert.assertTrue(GraphTests.isWeaklyConnected(g));
		Assert.assertTrue(GraphTests.isStronglyConnected(g));
	}

	@Test
	public void testIsTree()
	{
		Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
		Assert.assertFalse(GraphTests.isTree(g));
		g.addVertex(1);
		Assert.assertTrue(GraphTests.isTree(g));
		g.addVertex(2);
		Assert.assertFalse(GraphTests.isTree(g));
		g.addEdge(1, 2);
		Assert.assertTrue(GraphTests.isTree(g));
		g.addVertex(3);
		Assert.assertFalse(GraphTests.isTree(g));
		g.addEdge(1, 3);
		Assert.assertTrue(GraphTests.isTree(g));
		g.addEdge(2, 3);
		Assert.assertFalse(GraphTests.isTree(g));

		// disconnected but with correct number of edges
		Graph<Integer, DefaultEdge> g1 = new Pseudograph<>(DefaultEdge.class);
		Assert.assertFalse(GraphTests.isTree(g1));
		g1.addVertex(1);
		g1.addVertex(2);
		g.addEdge(1, 1);
		Assert.assertFalse(GraphTests.isTree(g1));
	}

	@Test
	public void testBipartite1()
	{
		Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
		Assert.assertTrue(GraphTests.isBipartite(g));
		g.addVertex(1);
		Assert.assertTrue(GraphTests.isBipartite(g));
		g.addVertex(2);
		Assert.assertTrue(GraphTests.isBipartite(g));
		g.addEdge(1, 2);
		Assert.assertTrue(GraphTests.isBipartite(g));
		g.addVertex(3);
		Assert.assertTrue(GraphTests.isBipartite(g));
		g.addEdge(2, 3);
		Assert.assertTrue(GraphTests.isBipartite(g));
		g.addEdge(3, 1);
		Assert.assertFalse(GraphTests.isBipartite(g));
	}

	@Test
	public void testBipartite2()
	{
		Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

		for (int i = 0; i < 100; i++) {
			g.addVertex(i);
			if (i > 0) {
				g.addEdge(i, i - 1);
			}
		}
		g.addEdge(99, 0);
		Assert.assertTrue(GraphTests.isBipartite(g));
	}

	@Test
	public void testBipartite3()
	{
		Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

		for (int i = 0; i < 101; i++) {
			g.addVertex(i);
			if (i > 0) {
				g.addEdge(i, i - 1);
			}
		}
		g.addEdge(100, 0);
		Assert.assertFalse(GraphTests.isBipartite(g));
	}

	@Test
	public void testBipartite4()
	{
		Graph<Integer, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

		for (int i = 0; i < 101; i++) {
			g.addVertex(i);
			if (i > 0) {
				g.addEdge(i, i - 1);
			}
		}
		g.addEdge(100, 0);
		Assert.assertFalse(GraphTests.isBipartite(g));
	}

	@Test
	public void testRandomBipartite()
	{
		GnpRandomBipartiteGraphGenerator<Integer, DefaultEdge> generator =
				new GnpRandomBipartiteGraphGenerator<>(10, 10, 0.8);
		for (int i = 0; i < 100; i++) {
			Graph<Integer, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);
			generator.generateGraph(g, new IntegerVertexFactory(), null);
			Assert.assertTrue(GraphTests.isBipartite(g));
		}
	}

	@Test
	public void testIsBipartitePartition()
	{
		List<Graph<Integer, DefaultEdge>> gList = new ArrayList<>();
		gList.add(new Pseudograph<>(DefaultEdge.class));
		gList.add(new DirectedPseudograph<>(DefaultEdge.class));

		for (Graph<Integer, DefaultEdge> g : gList) {
			Set<Integer> a = new HashSet<>();
			Graphs.addAllVertices(g, Arrays.asList(1, 2, 3, 4));
			a.addAll(Arrays.asList(1, 2));
			Set<Integer> b = new HashSet<>();
			b.addAll(Arrays.asList(3, 4));
			Assert.assertTrue(GraphTests.isBipartitePartition(g, a, b));
			g.addEdge(1, 3);
			g.addEdge(1, 4);
			g.addEdge(1, 3);
			g.addEdge(2, 3);
			g.addEdge(2, 4);
			g.addEdge(4, 1);
			g.addEdge(3, 1);
			Assert.assertTrue(GraphTests.isBipartitePartition(g, a, b));
			a.remove(1);
			Assert.assertFalse(GraphTests.isBipartitePartition(g, a, b));
			a.add(1);
			Assert.assertTrue(GraphTests.isBipartitePartition(g, a, b));
			DefaultEdge e11 = g.addEdge(1, 1);
			Assert.assertFalse(GraphTests.isBipartitePartition(g, a, b));
			g.removeEdge(e11);
			Assert.assertTrue(GraphTests.isBipartitePartition(g, a, b));
			DefaultEdge e44 = g.addEdge(4, 4);
			Assert.assertFalse(GraphTests.isBipartitePartition(g, a, b));
			g.removeEdge(e44);
			Assert.assertTrue(GraphTests.isBipartitePartition(g, a, b));
			g.addEdge(4, 3);
			Assert.assertFalse(GraphTests.isBipartitePartition(g, a, b));
		}
	}
}

// End GraphTestsTest.java
