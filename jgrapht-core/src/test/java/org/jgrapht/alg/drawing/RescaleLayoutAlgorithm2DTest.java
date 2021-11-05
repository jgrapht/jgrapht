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

import static org.junit.Assert.assertTrue;

/**
 * Test {@link CircularLayoutAlgorithm2D}.
 * 
 * @author Dimitrios Michail
 */
public class RescaleLayoutAlgorithm2DTest
{

    @Test
    public void testSimple()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();
        String v2 = graph.addVertex();
        String v3 = graph.addVertex();
        String v4 = graph.addVertex();

        CircularLayoutAlgorithm2D<String, DefaultEdge> alg = new CircularLayoutAlgorithm2D<>(1d);
        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(0d, 0d, 2d, 2d));

        alg.layout(graph, model);

        assertTrue(Points.equals(Point2D.of(2d, 1d), model.get(v1)));
        assertTrue(Points.equals(Point2D.of(1d, 2d), model.get(v2)));
        assertTrue(Points.equals(Point2D.of(0d, 1d), model.get(v3)));
        assertTrue(Points.equals(Point2D.of(1d, 0d), model.get(v4)));

        new RescaleLayoutAlgorithm2D<String, DefaultEdge>(4.0).layout(graph, model);

        assertTrue(Points.equals(Point2D.of(5d, 1.0), model.get(v1)));
        assertTrue(Points.equals(Point2D.of(1d, 5d), model.get(v2)));
        assertTrue(Points.equals(Point2D.of(-3.0d, 1d), model.get(v3)));
        assertTrue(Points.equals(Point2D.of(1d, -3d), model.get(v4)));

    }

    @Test
    public void testWithDifferentAspectRatio()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();
        String v2 = graph.addVertex();
        String v3 = graph.addVertex();
        String v4 = graph.addVertex();

        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(-1d, -1d, 2d, 2d));
        model.put(v1, Point2D.of(1.0, 0.0));
        model.put(v2, Point2D.of(-1.0, 0.0));
        model.put(v3, Point2D.of(0, 0.5));
        model.put(v4, Point2D.of(0, -0.5));

        new RescaleLayoutAlgorithm2D<String, DefaultEdge>(3.0).layout(graph, model);

        assertTrue(Points.equals(Point2D.of(3d, 0.0), model.get(v1)));
        assertTrue(Points.equals(Point2D.of(-3d, 0.0), model.get(v2)));
        assertTrue(Points.equals(Point2D.of(0, 1.5d), model.get(v3)));
        assertTrue(Points.equals(Point2D.of(0, -1.5d), model.get(v4)));
    }

    @Test
    public void testBadInput()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();

        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(-1d, -1d, 2d, 2d));
        model.put(v1, Point2D.of(0.0, 0.0));

        new RescaleLayoutAlgorithm2D<String, DefaultEdge>(3.0).layout(graph, model);

        assertTrue(Points.equals(Point2D.of(0d, 0.0), model.get(v1)));
    }

    @Test
    public void testBadInput1()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();
        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(-1d, -1d, 2d, 2d));
        new RescaleLayoutAlgorithm2D<String, DefaultEdge>(3.0).layout(graph, model);
    }

}
