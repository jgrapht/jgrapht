/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* ------------------------
 * SimpleGraphPathTest.java
 * ------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Rodrigo López Dato
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 22-Jan-2014 : Initial revision;
 */

package org.jgrapht.builder;

import java.util.Arrays;

import org.jgrapht.EnhancedTestCase;
import org.jgrapht.Graph;
import org.jgrapht.builder.SimpleGraphBuilder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Tests for the SimpleGraphBuilder.
 * 
 * @author Thomas Feichtinger
 *
 */
public class SimpleGraphBuilderTests extends EnhancedTestCase {
	private static final String V1 = "v1";
	private static final String V2 = "v2";
	private static final String V3 = "v3";
	private static final String V4 = "v4";

	private Graph<String, DefaultEdge> simpleGraph;
	private Graph<String, DefaultEdge> regularSimpleGraph;

	@Override
	public void setUp() {
		simpleGraph = new SimpleGraphBuilder<String, DefaultEdge>(
				DefaultEdge.class).edge(V1, V2).edge(V1, V3).edge(V3, V4)
				.edge(V2, V4).build();

		regularSimpleGraph = new SimpleGraph<String, DefaultEdge>(
				DefaultEdge.class);
		regularSimpleGraph.addVertex(V1);
		regularSimpleGraph.addVertex(V2);
		regularSimpleGraph.addVertex(V3);
		regularSimpleGraph.addVertex(V4);
		regularSimpleGraph.addEdge(V1, V2);
		regularSimpleGraph.addEdge(V1, V3);
		regularSimpleGraph.addEdge(V3, V4);
		regularSimpleGraph.addEdge(V2, V4);
	}

	public void testVertices() {
		assertTrue(simpleGraph.vertexSet().containsAll(
				Arrays.asList(V1, V2, V3, V4)));
	}

	public void testEdges() {
		assert simpleGraph.edgeSet().size() == 4;
		assert simpleGraph.containsEdge(V1, V2);
		assert simpleGraph.containsEdge(V1, V3);
		assert simpleGraph.containsEdge(V3, V4);
		assert simpleGraph.containsEdge(V2, V4);
	}

	public void testCompareGraphs() {
		assert simpleGraph.equals(regularSimpleGraph);
	}
}
