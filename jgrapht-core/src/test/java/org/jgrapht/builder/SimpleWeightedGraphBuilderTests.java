package org.jgrapht.builder;

import java.util.Arrays;

import org.jgrapht.EnhancedTestCase;
import org.jgrapht.builder.SimpleWeightedGraphBuilder;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * Tests for the SimpleWeightedGraphBuilder.
 * @author Thomas Feichtinger
 *
 */
public class SimpleWeightedGraphBuilderTests extends EnhancedTestCase {
	private static final String V1 = "v1";
	private static final String V2 = "v2";
	private static final String V3 = "v3";
	private static final String V4 = "v4";

	private SimpleWeightedGraph<String, DefaultWeightedEdge> weightedGraph;
	private SimpleWeightedGraph<String, DefaultWeightedEdge> regularWeightedGraph;

	@Override
	public void setUp() {
		weightedGraph = new SimpleWeightedGraphBuilder<String, DefaultWeightedEdge>(
				DefaultWeightedEdge.class).edge(V1, V2, 1.0).edge(V1, V3, 2.0)
				.edge(V3, V4, 3.0).edge(V2, V4, 4.0).build();

		regularWeightedGraph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);

		regularWeightedGraph.addVertex(V1);
		regularWeightedGraph.addVertex(V2);
		regularWeightedGraph.addVertex(V3);
		regularWeightedGraph.addVertex(V4);

		DefaultWeightedEdge e;
		e = regularWeightedGraph.addEdge(V1, V2);
		regularWeightedGraph.setEdgeWeight(e, 1.0);
		e = regularWeightedGraph.addEdge(V1, V3);
		regularWeightedGraph.setEdgeWeight(e, 2.0);
		e = regularWeightedGraph.addEdge(V3, V4);
		regularWeightedGraph.setEdgeWeight(e, 3.0);
		e = regularWeightedGraph.addEdge(V2, V4);
		regularWeightedGraph.setEdgeWeight(e, 4.0);

	}

	public void testVertices() {
		assertTrue(weightedGraph.vertexSet().containsAll(
				Arrays.asList(V1, V2, V3, V4)));
	}

	public void testEdges() {
		assert weightedGraph.edgeSet().size() == 4;
		assert weightedGraph.containsEdge(V1, V2);
		assert weightedGraph.containsEdge(V1, V3);
		assert weightedGraph.containsEdge(V3, V4);
		assert weightedGraph.containsEdge(V2, V4);
	}

	public void testCompareGraphs() {
		assert weightedGraph.equals(regularWeightedGraph);
	}
}
