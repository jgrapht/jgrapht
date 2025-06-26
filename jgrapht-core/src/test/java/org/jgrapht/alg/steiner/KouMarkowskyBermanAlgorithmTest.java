/*
 * (C) Copyright 2024, by TODO and Contributors.
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
package org.jgrapht.alg.steiner;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.alg.interfaces.SteinerTreeAlgorithm.SteinerTree;

import org.junit.*;


public class KouMarkowskyBermanAlgorithmTest {

	@Test
	public void testExampleGraphSteinerTree() {
		List<String> exampleVertices = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i");
		SimpleWeightedGraph<String, DefaultWeightedEdge> exampleGraph = new SimpleWeightedGraph<>(
				DefaultWeightedEdge.class);

		for (String v : exampleVertices) {
			exampleGraph.addVertex(v);
		}

		// Add edges with weights
		setEdgeWithWeight(exampleGraph, "a", "b", 10);
		setEdgeWithWeight(exampleGraph, "b", "c", 8);
		setEdgeWithWeight(exampleGraph, "c", "d", 9);
		setEdgeWithWeight(exampleGraph, "d", "e", 2);
		setEdgeWithWeight(exampleGraph, "c", "e", 2);
		setEdgeWithWeight(exampleGraph, "f", "e", 1);
		setEdgeWithWeight(exampleGraph, "i", "e", 1);
		setEdgeWithWeight(exampleGraph, "b", "f", 1);
		setEdgeWithWeight(exampleGraph, "f", "g", 0.5);
		setEdgeWithWeight(exampleGraph, "a", "i", 1);
		setEdgeWithWeight(exampleGraph, "g", "h", 0.5);
		setEdgeWithWeight(exampleGraph, "h", "i", 0.5);

		Set<String> terminals = new HashSet<>(Arrays.asList("a", "c", "e", "g"));

		KouMarkowskyBermanAlgorithm<String, DefaultWeightedEdge> steinerAlg = new KouMarkowskyBermanAlgorithm<>(
				exampleGraph);

		SteinerTree<DefaultWeightedEdge> steinerTree = steinerAlg.getSpanningTree(terminals);

		// Optional debug output:
		System.out.println("Edges in Steiner Tree:");
		for (DefaultWeightedEdge edge : steinerTree.getEdges()) {
			String src = exampleGraph.getEdgeSource(edge);
			String tgt = exampleGraph.getEdgeTarget(edge);
			double weight = exampleGraph.getEdgeWeight(edge);
			System.out.printf("%s -- %s (%.2f)%n", src, tgt, weight);
		}

		assertEquals(5.5, steinerTree.getWeight(), 0.001);
	}

	private void setEdgeWithWeight(Graph<String, DefaultWeightedEdge> graph, String source, String target,
			double weight) {
		graph.addVertex(source);
		graph.addVertex(target);
		DefaultWeightedEdge edge = graph.addEdge(source, target);
		if (edge != null) {
			graph.setEdgeWeight(edge, weight);
		}
	}
}
