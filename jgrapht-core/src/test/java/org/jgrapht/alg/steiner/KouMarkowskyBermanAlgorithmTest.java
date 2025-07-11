/*
 * (C) Copyright 2025, by Lena Büttel and Contributors.
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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.SteinerTreeAlgorithm.SteinerTree;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.jgrapht.alg.interfaces.SteinerTreeAlgorithm.SteinerTree;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class KouMarkowskyBermanAlgorithmTest {

	@Test
	public void testExampleGraphSteinerTree() {
		List<String> exampleVertices = Arrays.asList("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9");
		SimpleWeightedGraph<String, DefaultWeightedEdge> exampleGraph = new SimpleWeightedGraph<>(
				DefaultWeightedEdge.class);

		for (String v : exampleVertices) {
			exampleGraph.addVertex(v);
		}

		// Add edges with weights
		setEdgeWithWeight(exampleGraph, "v1", "v2", 10);
		setEdgeWithWeight(exampleGraph, "v2", "v3", 8);
		setEdgeWithWeight(exampleGraph, "v3", "v4", 9);
		setEdgeWithWeight(exampleGraph, "v4", "v5", 2);
		setEdgeWithWeight(exampleGraph, "v3", "v5", 2);
		setEdgeWithWeight(exampleGraph, "v6", "v5", 1);
		setEdgeWithWeight(exampleGraph, "v9", "v5", 1);
		setEdgeWithWeight(exampleGraph, "v2", "v6", 1);
		setEdgeWithWeight(exampleGraph, "v6", "v7", 1.0);
		setEdgeWithWeight(exampleGraph, "v1", "v9", 1);
		setEdgeWithWeight(exampleGraph, "v7", "v8", 0.5);
		setEdgeWithWeight(exampleGraph, "v8", "v9", 0.5);

		Set<String> terminals = new HashSet<>(Arrays.asList("v1", "v2", "v3", "v4"));

		KouMarkowskyBermanAlgorithm<String, DefaultWeightedEdge> steinerAlg = new KouMarkowskyBermanAlgorithm<>(
				exampleGraph);

		SteinerTree<DefaultWeightedEdge> steinerTree = steinerAlg.getSteinerTree(terminals);

		/* Optional debug output:
		System.out.println("Edges in Steiner Tree:");
		for (DefaultWeightedEdge edge : steinerTree.getEdges()) {
			String src = exampleGraph.getEdgeSource(edge);
			String tgt = exampleGraph.getEdgeTarget(edge);
			double weight = exampleGraph.getEdgeWeight(edge);
			System.out.printf("%s -- %s (%.2f)%n", src, tgt, weight);
		}*/
		assertEquals(8.0, steinerTree.getWeight(), 0.001);
		
		Set<String> exampleTreeVertices = steinerTree.getEdges().stream()
			    .flatMap((DefaultWeightedEdge e) -> {
			        String source = exampleGraph.getEdgeSource(e);
			        String target = exampleGraph.getEdgeTarget(e);
			        return Stream.of(source, target);
			    })
			    .collect(Collectors.toSet());


			for (String terminal : terminals) {
			    assertTrue( "Missing terminal: " + terminal, exampleTreeVertices.contains(terminal));
			}
			
		assertEquals(exampleTreeVertices.size() - 1, steinerTree.getEdges().size());

	}
	
	@Test
	public void testRandomGraphSteinerTree() {

        Graph<String, DefaultWeightedEdge> gnpGraph = GraphTypeBuilder.undirected().weighted(true).edgeClass(DefaultWeightedEdge.class).vertexSupplier(SupplierUtil.createStringSupplier()).buildGraph(); 
		
        GnpRandomGraphGenerator<String, DefaultWeightedEdge> gnpRandomGraphGenerator =  new GnpRandomGraphGenerator <> (25, 0.5);
        gnpRandomGraphGenerator.generateGraph(gnpGraph);
        
        Random rand = new Random();
		
		for(DefaultWeightedEdge edge : gnpGraph.edgeSet()) {
            double weight = rand.nextInt(10) + ((1.0 + rand.nextInt(10)) / 10);
			gnpGraph.setEdgeWeight(edge, weight);
		}
		
		KouMarkowskyBermanAlgorithm<String, DefaultWeightedEdge> steinerAlg = new KouMarkowskyBermanAlgorithm<>(
				gnpGraph);
		
		List<String> shuffled = new ArrayList<>(gnpGraph.vertexSet());
        Collections.shuffle(shuffled);

        Set<String> selected = new HashSet<> (shuffled.subList(0, 10));

		SteinerTree<DefaultWeightedEdge> steinerTree = steinerAlg.getSteinerTree(selected);

		Set<String> gnpTreeVertices = steinerTree.getEdges().stream()
			    .flatMap((DefaultWeightedEdge e) -> {
			        String source = gnpGraph.getEdgeSource(e);
			        String target = gnpGraph.getEdgeTarget(e);
			        return Stream.of(source, target);
			    })
			    .collect(Collectors.toSet());


			for (String vertex : selected) {
			    assertTrue( "Missing terminal: " + vertex, gnpTreeVertices.contains(vertex));
			}
			
		assertEquals(gnpTreeVertices.size() - 1, steinerTree.getEdges().size());

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
