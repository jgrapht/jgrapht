/*
 * (C) Copyright 2003-2016, by Barak Naveh and Contributors.
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
package org.jgrapht.alg;

import org.jgrapht.graph.*;

<<<<<<< HEAD
=======
import junit.framework.*;

>>>>>>> 213a915e30ba0f68cd29f1b7ca2a53588539403a
/**
 * .
 * 
 * @author Barak Naveh
 */
<<<<<<< HEAD
public class ConnectivityInspectorTest extends TestCase {
	// ~ Static fields/initializers
	// ---------------------------------------------

	private static final String V1 = "v1";
	private static final String V2 = "v2";
	private static final String V3 = "v3";
	private static final String V4 = "v4";
	
	private static final String[] Vertices = {V1, V2, V3, V4};
	

	// ~ Instance fields
	// --------------------------------------------------------

	//
	DefaultEdge e1;
	DefaultEdge e2;
	DefaultEdge e3;
	DefaultEdge e3_b;
	DefaultEdge u;
	
	ConnectivityInspector<String, DefaultEdge> inspector;

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * .
	 * 
	 * @return a graph
	 */
	public Pseudograph<String, DefaultEdge> create() {
		Pseudograph<String, DefaultEdge> g = new Pseudograph<String, DefaultEdge>(
				DefaultEdge.class);

		assertEquals(0, g.vertexSet().size());
		g.addVertex(V1);
		assertEquals(1, g.vertexSet().size());
		g.addVertex(V2);
		assertEquals(2, g.vertexSet().size());
		g.addVertex(V3);
		assertEquals(3, g.vertexSet().size());
		g.addVertex(V4);
		assertEquals(4, g.vertexSet().size());

		assertEquals(0, g.edgeSet().size());

		e1 = g.addEdge(V1, V2);
		assertEquals(1, g.edgeSet().size());

		e2 = g.addEdge(V2, V3);
		assertEquals(2, g.edgeSet().size());

		e3 = g.addEdge(V3, V1);
		assertEquals(3, g.edgeSet().size());

		e3_b = g.addEdge(V3, V1);
		assertEquals(4, g.edgeSet().size());
		assertNotNull(e3_b);

		u = g.addEdge(V1, V1);
		assertEquals(5, g.edgeSet().size());
		u = g.addEdge(V1, V1);
		assertEquals(6, g.edgeSet().size());

		return g;
	}

	/**
	 * .
	 */
	public void testDirectedGraph() {
		ListenableDirectedGraph<String, DefaultEdge> g = new ListenableDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);
		g.addVertex(V1);
		g.addVertex(V2);
		g.addVertex(V3);

		g.addEdge(V1, V2);

		inspector = new ConnectivityInspector<String, DefaultEdge>(
				g);
		g.addGraphListener(inspector);

		assertEquals(false, inspector.isGraphConnected());

		g.addEdge(V1, V3);

		assertEquals(true, inspector.isGraphConnected());
	}

	/**
	 * .
	 */
	public void testIsGraphConnected() {
		Pseudograph<String, DefaultEdge> g = create();
		inspector = new ConnectivityInspector<String, DefaultEdge>(
				g);

		assertEquals(false, inspector.isGraphConnected());

		g.removeVertex(V4);
		inspector = new ConnectivityInspector<String, DefaultEdge>(g);
		assertEquals(true, inspector.isGraphConnected());

		g.removeVertex(V1);
		assertEquals(1, g.edgeSet().size());

		g.removeEdge(e2);
		g.addEdge(V2, V2);
		assertEquals(1, g.edgeSet().size());

		inspector = new ConnectivityInspector<String, DefaultEdge>(g);
		assertEquals(false, inspector.isGraphConnected());
	}

	/**
	 * .
	 */
	public void testStronglyConnected1() {
		DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);
		g.addVertex(V1);
		g.addVertex(V2);
		g.addVertex(V3);
		g.addVertex(V4);

		g.addEdge(V1, V2);
		g.addEdge(V2, V1); // strongly connected

		g.addEdge(V3, V4); // only weakly connected

		StrongConnectivityInspector<String, DefaultEdge> inspector = new StrongConnectivityInspector<String, DefaultEdge>(
				g);

		// convert from List to Set because we need to ignore order
		// during comparison
		Set<Set<String>> actualSets = new HashSet<Set<String>>(
				inspector.stronglyConnectedSets());

		// construct the expected answer
		Set<Set<String>> expectedSets = new HashSet<Set<String>>();
		Set<String> set = new HashSet<String>();
		set.add(V1);
		set.add(V2);
		expectedSets.add(set);
		set = new HashSet<String>();
		set.add(V3);
		expectedSets.add(set);
		set = new HashSet<String>();
		set.add(V4);
		expectedSets.add(set);

		assertEquals(expectedSets, actualSets);

		actualSets.clear();

		List<DirectedSubgraph<String, DefaultEdge>> subgraphs = inspector
				.stronglyConnectedSubgraphs();
		for (DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
			actualSets.add(sg.vertexSet());

			StrongConnectivityInspector<String, DefaultEdge> ci = new StrongConnectivityInspector<String, DefaultEdge>(
					sg);
			assertTrue(ci.isStronglyConnected());
		}

		assertEquals(expectedSets, actualSets);
	}

	/**
	 * .
	 */
	public void testStronglyConnected2() {
		DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);
		g.addVertex(V1);
		g.addVertex(V2);
		g.addVertex(V3);
		g.addVertex(V4);

		g.addEdge(V1, V2);
		g.addEdge(V2, V1); // strongly connected

		g.addEdge(V4, V3); // only weakly connected
		g.addEdge(V3, V2); // only weakly connected

		StrongConnectivityInspector<String, DefaultEdge> inspector = new StrongConnectivityInspector<String, DefaultEdge>(
				g);

		// convert from List to Set because we need to ignore order
		// during comparison
		Set<Set<String>> actualSets = new HashSet<Set<String>>(
				inspector.stronglyConnectedSets());

		// construct the expected answer
		Set<Set<String>> expectedSets = new HashSet<Set<String>>();
		Set<String> set = new HashSet<String>();
		set.add(V1);
		set.add(V2);
		expectedSets.add(set);
		set = new HashSet<String>();
		set.add(V3);
		expectedSets.add(set);
		set = new HashSet<String>();
		set.add(V4);
		expectedSets.add(set);

		assertEquals(expectedSets, actualSets);

		actualSets.clear();

		List<DirectedSubgraph<String, DefaultEdge>> subgraphs = inspector
				.stronglyConnectedSubgraphs();
		for (DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
			actualSets.add(sg.vertexSet());

			StrongConnectivityInspector<String, DefaultEdge> ci = new StrongConnectivityInspector<String, DefaultEdge>(
					sg);
			assertTrue(ci.isStronglyConnected());
		}

		assertEquals(expectedSets, actualSets);
	}

	/**
	 * .
	 */
	public void testStronglyConnected3() {
		DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);
		g.addVertex(V1);
		g.addVertex(V2);
		g.addVertex(V3);
		g.addVertex(V4);

		g.addEdge(V1, V2);
		g.addEdge(V2, V3);
		g.addEdge(V3, V1); // strongly connected

		g.addEdge(V1, V4);
		g.addEdge(V2, V4);
		g.addEdge(V3, V4); // weakly connected

		StrongConnectivityInspector<String, DefaultEdge> inspector = new StrongConnectivityInspector<String, DefaultEdge>(
				g);

		// convert from List to Set because we need to ignore order
		// during comparison
		Set<Set<String>> actualSets = new HashSet<Set<String>>(
				inspector.stronglyConnectedSets());

		// construct the expected answer
		Set<Set<String>> expectedSets = new HashSet<Set<String>>();
		Set<String> set = new HashSet<String>();
		set.add(V1);
		set.add(V2);
		set.add(V3);
		expectedSets.add(set);
		set = new HashSet<String>();
		set.add(V4);
		expectedSets.add(set);

		assertEquals(expectedSets, actualSets);

		actualSets.clear();

		List<DirectedSubgraph<String, DefaultEdge>> subgraphs = inspector
				.stronglyConnectedSubgraphs();

		for (DirectedSubgraph<String, DefaultEdge> sg : subgraphs) {
			actualSets.add(sg.vertexSet());

			StrongConnectivityInspector<String, DefaultEdge> ci = new StrongConnectivityInspector<String, DefaultEdge>(
					sg);
			assertTrue(ci.isStronglyConnected());
		}

		assertEquals(expectedSets, actualSets);
	}

	public void testStronglyConnected4() {
		DefaultDirectedGraph<Integer, String> graph = new DefaultDirectedGraph<Integer, String>(
				new EdgeFactory<Integer, String>() {
					public String createEdge(Integer from, Integer to) {
						return (from + "->" + to).intern();
					}
				});

		new RingGraphGenerator<Integer, String>(3).generateGraph(graph,
				new VertexFactory<Integer>() {
					private int i = 0;

					public Integer createVertex() {
						return i++;
					}
				}, null);

		StrongConnectivityInspector<Integer, String> sc = new StrongConnectivityInspector<Integer, String>(
				graph);
		Set<Set<Integer>> expected = new HashSet<Set<Integer>>();
		expected.add(graph.vertexSet());
		assertEquals(expected,
				new HashSet<Set<Integer>>(sc.stronglyConnectedSets()));
	}

	
	
	// ~ Test cases for isComplete / incompleteVertices methods
	// ---------------------------------------------
	
	public void testUndirectedGraphWithOneVertex_is_Complete() {
		UndirectedGraph<String, DefaultEdge> g = 
				this.createUndirectedGraph(1);
		inspector = new ConnectivityInspector<String, DefaultEdge>(
				g);

		assertTrue(inspector.isComplete());
		
		assertTrue(inspector.incompleteVertices().isEmpty());
	}

	public void testUndirectedGraphWithTwoVerticesAndNoEdges_isNot_Complete() {
		UndirectedGraph<String, DefaultEdge> g = 
				this.createUndirectedGraph(2);
		inspector = 
				new ConnectivityInspector<String, DefaultEdge>(g);

		assertFalse(inspector.isComplete());
		
		Set<String> set = inspector.incompleteVertices();
		
		assertEquals(set.size(), g.vertexSet().size());
		assertTrue(set.contains(V1));
		assertTrue(set.contains(V2));
	}

	public void testUndirectedGraphWithTwoVertices_And_OneEdge_is_Complete() {
		UndirectedGraph<String, DefaultEdge> g = 
				this.createUndirectedGraph(2);
		g.addEdge(V1, V2);
		inspector = new ConnectivityInspector<String, DefaultEdge>(g);

		assertTrue(inspector.isComplete());
	}

	public void testDirectedGraphWithOneVertex_is_Complete() {
		DirectedGraph<String, DefaultEdge> g = this.createDirectedGraph(1);
		inspector = new ConnectivityInspector<String, DefaultEdge>(g);

		assertTrue(inspector.isComplete());
		
		Set<String> set = inspector.incompleteVertices();
		assertTrue(set.isEmpty());
	}

	public void testDirectedGraphWithTwoVertices_and_OneEdge_isNot_Complete() {
		DirectedGraph<String, DefaultEdge> g = this.createDirectedGraph(2);
		g.addEdge(V1, V2);
		inspector = new ConnectivityInspector<String, DefaultEdge>(g);

		assertFalse(inspector.isComplete());
		
		Set<String> set = inspector.incompleteVertices();
		assertEquals(1, set.size());
		assertTrue(set.contains(V2));
	}

	public void testDirectedGraphWithTwoVertices_and_twoEdges_is_Complete() {
		DirectedGraph<String, DefaultEdge> g = this.createDirectedGraph(2);
		g.addEdge(V1, V2);
		g.addEdge(V2, V1);
		inspector = new ConnectivityInspector<String, DefaultEdge>(g);

		assertTrue(inspector.isComplete());

		assertTrue(inspector.incompleteVertices().isEmpty());
	}

	public void testDirectedGraphPartlyIncomplete() {
		DirectedGraph<String, DefaultEdge> g = this.createDirectedGraph(3);
		inspector = new ConnectivityInspector<String, DefaultEdge>(g);

		g.addEdge(V1, V2);
		g.addEdge(V1, V3);
		
		g.addEdge(V2, V1);
		g.addEdge(V2, V3);
	
		Set<String> set = inspector.incompleteVertices();
		assertTrue(set.toString(), set.contains(V3));
	}

	public void testAllDirectedGeneratedCompleteGraphs_are_Complete() {

		for (int vertices = 1; vertices < 20; vertices++) {
			
			CompleteGraphGenerator<String, DefaultEdge> generator = 
					new CompleteGraphGenerator<String, DefaultEdge>(vertices);

			DirectedGraph<String, DefaultEdge> g = 
					new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

			generator.generateGraph(g, new SimpleVertexFactory(),	null);

			inspector = 
					new ConnectivityInspector<String, DefaultEdge>(g);

			assertTrue(inspector.isComplete());
		}
	}

	public void testAllUndirectedGeneratedCompleteGraphs_are_Complete() {

		for (int vertices = 1; vertices < 20; vertices++) {
			
			CompleteGraphGenerator<String, DefaultEdge> generator = 
					new CompleteGraphGenerator<String, DefaultEdge>(vertices);

			UndirectedGraph<String, DefaultEdge> g = 
					new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);

			generator.generateGraph(g, new SimpleVertexFactory(),null);

			inspector = 
					new ConnectivityInspector<String, DefaultEdge>(g);

			assertTrue(inspector.isComplete());
		}
	}
	
	public void testMultigraph_NoMultipleEdges() {
		Multigraph<String, DefaultEdge> mg = new Multigraph<String, DefaultEdge>(DefaultEdge.class);
		inspector = new ConnectivityInspector<String, DefaultEdge>(mg);

		addVertices(3, mg);
		assertEquals(3, mg.vertexSet().size());
		mg.addEdge(V1, V2);
		mg.addEdge(V1, V3);

		assertEquals(false, inspector.isComplete());

		mg.addEdge(V2, V3);
		assertEquals(true, inspector.isComplete());
	}

	public void testMultigraph_WithMultipleEdges() {
		Multigraph<String, DefaultEdge> mg = new Multigraph<String, DefaultEdge>(DefaultEdge.class);
		inspector = new ConnectivityInspector<String, DefaultEdge>(mg);

		// Empty graph is complete
		assertTrue(inspector.isComplete());
		
		addVertices(3, mg);
		
		// All vertices are incomplete
		assertEquals(3, inspector.incompleteVertices().size());
		
		mg.addEdge(V1, V2);
		mg.addEdge(V1, V2);
		mg.addEdge(V2, V1);
		mg.addEdge(V1, V3);
		mg.addEdge(V1, V3);

		Set<String> incompleteVertices = inspector.incompleteVertices();
		assertEquals(2, incompleteVertices.size());

		mg.addEdge(V2, V3);
		mg.addEdge(V2, V3);
		incompleteVertices = inspector.incompleteVertices();
		assertEquals(0, incompleteVertices.size());
	}
	
	public void testSelfLoops_in_DefaultDirectedGraph() {
		DefaultDirectedGraph<String, DefaultEdge> dg = 
				new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		inspector = new ConnectivityInspector<String, DefaultEdge>(dg);
		
		// Empty graph is complete
		assertTrue(inspector.isComplete());

		addVertices(3, dg);
		
		// All vertices are incomplete
		assertEquals(3, inspector.incompleteVertices().size());
		
		dg.addEdge(V1, V1);
		dg.addEdge(V2, V2);
		dg.addEdge(V3, V3);
		
		// All vertices are incomplete
		assertEquals(3, inspector.incompleteVertices().size());
		
		dg.addEdge(V1, V2);
		dg.addEdge(V1, V3);
		
		// V2 and V3 are incomplete
		assertEquals(2, inspector.incompleteVertices().size());

		dg.addEdge(V2, V3);
		
		// V2 and V3 are still incomplete
		assertEquals(2, inspector.incompleteVertices().size());
		
		dg.addEdge(V2, V1);
		
		// V3 is incomplete
		assertEquals(1, inspector.incompleteVertices().size());

		dg.addEdge(V3, V1);
		dg.addEdge(V3, V2);

		assertEquals(0, inspector.incompleteVertices().size());

	}


	
	// ~ Helper methods to create graphs
	// ---------------------------------------------
	
	private UndirectedGraph<String, DefaultEdge> createUndirectedGraph(int vertices) 
	{
		UndirectedGraph<String, DefaultEdge> g = 
				new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		
		addVertices(vertices, g);
		
		return g;
	}
	
	private DirectedGraph<String, DefaultEdge> createDirectedGraph(int vertices)
	{
		DirectedGraph<String, DefaultEdge> g = 
				new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		
		addVertices(vertices, g);
		
		return g;
	}
	
	private void addVertices(int vertices, Graph<String, DefaultEdge> g) {
		for (int i = 0; i < vertices; i++) {
			g.addVertex(Vertices[i]);
		}
	}

	class SimpleVertexFactory implements VertexFactory<String> {
		private int i = 0;

		public String createVertex() {
			return "Vertex " + (++i);
		}

	}
=======
public class ConnectivityInspectorTest
    extends TestCase
{
    // ~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";
    private static final String V4 = "v4";

    // ~ Instance fields --------------------------------------------------------

    //
    DefaultEdge e1;
    DefaultEdge e2;
    DefaultEdge e3;
    DefaultEdge e3_b;
    DefaultEdge u;

    // ~ Methods ----------------------------------------------------------------

    /**
     * .
     *
     * @return a graph
     */
    public Pseudograph<String, DefaultEdge> create()
    {
        Pseudograph<String, DefaultEdge> g = new Pseudograph<>(DefaultEdge.class);

        assertEquals(0, g.vertexSet().size());
        g.addVertex(V1);
        assertEquals(1, g.vertexSet().size());
        g.addVertex(V2);
        assertEquals(2, g.vertexSet().size());
        g.addVertex(V3);
        assertEquals(3, g.vertexSet().size());
        g.addVertex(V4);
        assertEquals(4, g.vertexSet().size());

        assertEquals(0, g.edgeSet().size());

        e1 = g.addEdge(V1, V2);
        assertEquals(1, g.edgeSet().size());

        e2 = g.addEdge(V2, V3);
        assertEquals(2, g.edgeSet().size());

        e3 = g.addEdge(V3, V1);
        assertEquals(3, g.edgeSet().size());

        e3_b = g.addEdge(V3, V1);
        assertEquals(4, g.edgeSet().size());
        assertNotNull(e3_b);

        u = g.addEdge(V1, V1);
        assertEquals(5, g.edgeSet().size());
        u = g.addEdge(V1, V1);
        assertEquals(6, g.edgeSet().size());

        return g;
    }

    /**
     * .
     */
    public void testDirectedGraph()
    {
        ListenableDirectedGraph<String, DefaultEdge> g =
            new ListenableDirectedGraph<>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);

        g.addEdge(V1, V2);

        ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(g);
        g.addGraphListener(inspector);

        assertEquals(false, inspector.isGraphConnected());

        g.addEdge(V1, V3);

        assertEquals(true, inspector.isGraphConnected());
    }

    /**
     * .
     */
    public void testIsGraphConnected()
    {
        Pseudograph<String, DefaultEdge> g = create();
        ConnectivityInspector<String, DefaultEdge> inspector = new ConnectivityInspector<>(g);

        assertEquals(false, inspector.isGraphConnected());

        g.removeVertex(V4);
        inspector = new ConnectivityInspector<>(g);
        assertEquals(true, inspector.isGraphConnected());

        g.removeVertex(V1);
        assertEquals(1, g.edgeSet().size());

        g.removeEdge(e2);
        g.addEdge(V2, V2);
        assertEquals(1, g.edgeSet().size());

        inspector = new ConnectivityInspector<>(g);
        assertEquals(false, inspector.isGraphConnected());
    }

>>>>>>> 213a915e30ba0f68cd29f1b7ca2a53588539403a
}

// End ConnectivityInspectorTest.java
