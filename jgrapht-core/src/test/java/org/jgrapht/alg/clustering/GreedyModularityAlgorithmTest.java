/*
 * (C) Copyright 2020-2021, by Antonia Tsiftsi and Contributors.
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
package org.jgrapht.alg.clustering;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Tests
 *
 * @author Antonia Tsiftsi
 */
public class GreedyModularityAlgorithmTest {
	@Test
	public void test1() {
		Graph<Integer, DefaultEdge> g = GraphTypeBuilder.undirected()
				.vertexSupplier(SupplierUtil.createIntegerSupplier())
				.edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).weighted(true).allowingSelfLoops(true)
				.allowingMultipleEdges(true).buildGraph();

		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		g.addVertex(7);

		g.addEdge(1, 2);
		g.addEdge(2, 3);
		g.addEdge(3, 1);
		g.addEdge(3, 4);
		g.addEdge(4, 5);
		g.addEdge(4, 6);
		g.addEdge(5, 6);
		g.addEdge(6, 7);
		g.addEdge(7, 5);

		// System.out.println("Graph: "+g);

		ClusteringAlgorithm<Integer> alg = new GreedyModularityAlgorithm<>(g);
		ClusteringAlgorithm.Clustering<Integer> clustering = alg.getClustering();

//        System.out.println("Number of clusters: "+clustering.getNumberClusters());
//        assertEquals(2, clustering.getNumberClusters());
//        System.out.println("Clusters: "+clustering.getClusters());
//        assertEquals(Set.of(1, 2, 3), clustering.getClusters().get(0));
//        assertEquals(Set.of(4, 5, 6, 7), clustering.getClusters().get(1));
	}

	@Test
	public void test2() {
		Graph<Integer, DefaultEdge> g = GraphTypeBuilder.undirected()
				.vertexSupplier(SupplierUtil.createIntegerSupplier())
				.edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).weighted(true).allowingSelfLoops(true)
				.allowingMultipleEdges(true).buildGraph();

		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		g.addVertex(7);
		g.addVertex(8);
		g.addVertex(9);
		g.addVertex(10);
		g.addVertex(11);
		g.addVertex(12);
		g.addVertex(13);
		g.addVertex(14);
		g.addVertex(15);

		g.addEdge(0, 2);
		g.addEdge(0, 3);
		g.addEdge(0, 4);
		g.addEdge(0, 5);
		g.addEdge(1, 2);
		g.addEdge(1, 4);
		g.addEdge(1, 7);
		g.addEdge(2, 4);
		g.addEdge(2, 5);
		g.addEdge(2, 6);
		g.addEdge(3, 7);
		g.addEdge(4, 10);
		g.addEdge(5, 7);
		g.addEdge(5, 11);
		g.addEdge(6, 7);
		g.addEdge(6, 11);
		g.addEdge(8, 9);
		g.addEdge(8, 10);
		g.addEdge(8, 11);
		g.addEdge(8, 14);
		g.addEdge(8, 15);
		g.addEdge(9, 12);
		g.addEdge(9, 14);
		g.addEdge(10, 11);
		g.addEdge(10, 12);
		g.addEdge(10, 13);
		g.addEdge(10, 14);
		g.addEdge(11, 13);

		System.out.println("Graph: " + g);

		ClusteringAlgorithm<Integer> alg = new GreedyModularityAlgorithm<>(g);
		ClusteringAlgorithm.Clustering<Integer> clustering = alg.getClustering();

		System.out.println("Number of clusters: " + clustering.getNumberClusters());
		// assertEquals(3, clustering.getNumberClusters());
		System.out.println("Clusters: " + clustering.getClusters());
		// assertEquals(Set.of(1, 2, 3), clustering.getClusters().get(0));
		// assertEquals(Set.of(4, 5, 6, 7), clustering.getClusters().get(1));
	}
}
