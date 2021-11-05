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
public class CircularLayoutAlgorithm2DTest
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
    }

    @Test
    public void testWithOrder()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = "4";
        graph.addVertex(v1);
        String v2 = "3";
        graph.addVertex(v2);
        String v3 = "2";
        graph.addVertex(v3);
        String v4 = "1";
        graph.addVertex(v4);

        CircularLayoutAlgorithm2D<String, DefaultEdge> alg =
            new CircularLayoutAlgorithm2D<>(1d, (a, b) -> a.compareTo(b));
        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(0d, 0d, 2d, 2d));

        alg.layout(graph, model);

        assertTrue(Points.equals(Point2D.of(2d, 1d), model.get(v4)));
        assertTrue(Points.equals(Point2D.of(1d, 2d), model.get(v3)));
        assertTrue(Points.equals(Point2D.of(0d, 1d), model.get(v2)));
        assertTrue(Points.equals(Point2D.of(1d, 0d), model.get(v1)));
    }

}
