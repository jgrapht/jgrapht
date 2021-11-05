/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.drawing;

import org.jgrapht.*;
import org.jgrapht.alg.drawing.model.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test {@link IndexedFRLayoutAlgorithm2D}.
 * 
 * @author Dimitrios Michail
 */
public class IndexedFRLayoutAlgorithm2DTest
{

    @Test
    public void testGraph1()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();
        String v2 = graph.addVertex();
        String v3 = graph.addVertex();
        String v4 = graph.addVertex();
        String v5 = graph.addVertex();
        String v6 = graph.addVertex();
        graph.addEdge(v1, v2);
        graph.addEdge(v3, v1);
        graph.addEdge(v4, v1);
        graph.addEdge(v5, v2);
        graph.addEdge(v6, v2);

        final Random rng = new Random(17);
        final int iterations = 100;
        final double normalizationFactor = 0.5;
        final double theta = 0.5;
        IndexedFRLayoutAlgorithm2D<String, DefaultEdge> alg =
            new IndexedFRLayoutAlgorithm2D<>(iterations, theta, normalizationFactor, rng);

        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(0d, 0d, 100d, 100d));
        alg.layout(graph, model);

        Map<String, Point2D> result = model.collect();

        // @formatter:off
        //  6        4
        //   \      / 
        //    2 -- 1
        //   /      \
        //  5        3
        // @formatter:on

        assertTrue(result.get(v1).getX() > result.get(v2).getX());
        assertTrue(result.get(v1).getY() > result.get(v2).getY());

        assertTrue(result.get(v3).getX() > result.get(v1).getX());
        assertTrue(result.get(v3).getY() < result.get(v1).getY());

        assertTrue(result.get(v4).getX() > result.get(v1).getX());
        assertTrue(result.get(v4).getY() > result.get(v1).getY());

        assertTrue(result.get(v5).getX() < result.get(v2).getX());
        assertTrue(result.get(v5).getY() < result.get(v2).getY());

        assertTrue(result.get(v6).getX() < result.get(v2).getX());
        assertTrue(result.get(v6).getY() > result.get(v2).getY());

        assertEquals(80, alg.getSavedComparisons());
    }

    @Test
    public void testGraphZeroTheta()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();
        String v2 = graph.addVertex();
        String v3 = graph.addVertex();
        String v4 = graph.addVertex();
        String v5 = graph.addVertex();
        String v6 = graph.addVertex();
        graph.addEdge(v1, v2);
        graph.addEdge(v3, v1);
        graph.addEdge(v4, v1);
        graph.addEdge(v5, v2);
        graph.addEdge(v6, v2);

        final Random rng = new Random(17);
        final int iterations = 100;
        final double normalizationFactor = 0.5;
        final double theta = 0;
        IndexedFRLayoutAlgorithm2D<String, DefaultEdge> alg =
            new IndexedFRLayoutAlgorithm2D<>(iterations, theta, normalizationFactor, rng);

        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(0d, 0d, 100d, 100d));
        alg.layout(graph, model);

        Map<String, Point2D> result = model.collect();

        // @formatter:off
        //  6        4
        //   \      / 
        //    2 -- 1
        //   /      \
        //  5        3
        // @formatter:on

        assertTrue(result.get(v1).getX() > result.get(v2).getX());
        assertTrue(result.get(v1).getY() > result.get(v2).getY());

        assertTrue(result.get(v3).getX() > result.get(v1).getX());
        assertTrue(result.get(v3).getY() < result.get(v1).getY());

        assertTrue(result.get(v4).getX() > result.get(v1).getX());
        assertTrue(result.get(v4).getY() > result.get(v1).getY());

        assertTrue(result.get(v5).getX() < result.get(v2).getX());
        assertTrue(result.get(v5).getY() < result.get(v2).getY());

        assertTrue(result.get(v6).getX() < result.get(v2).getX());
        assertTrue(result.get(v6).getY() > result.get(v2).getY());

        assertEquals(0, alg.getSavedComparisons());
    }

    @Test
    public void testGraphWithIsolatedVertex()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier(1))
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();
        String v2 = graph.addVertex();
        String v3 = graph.addVertex();
        String v4 = graph.addVertex();
        String v5 = graph.addVertex();
        String v6 = graph.addVertex();
        String v7 = graph.addVertex();
        graph.addEdge(v1, v2);
        graph.addEdge(v3, v1);
        graph.addEdge(v4, v1);
        graph.addEdge(v5, v2);
        graph.addEdge(v6, v2);

        final Random rng = new Random(17);
        final int iterations = 100;
        final double normalizationFactor = 0.5;
        final double theta = 0;
        IndexedFRLayoutAlgorithm2D<String, DefaultEdge> alg =
            new IndexedFRLayoutAlgorithm2D<>(iterations, theta, normalizationFactor, rng);

        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(0d, 0d, 100d, 100d));
        alg.layout(graph, model);

        Map<String, Point2D> result = model.collect();

        assertEquals(result.get(v7).getX(), 100d, 1e-9);
        assertEquals(result.get(v7).getY(), 100d, 1e-9);
    }

}
