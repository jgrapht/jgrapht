/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.MapLayoutModel2D;
import org.jgrapht.alg.drawing.model.Point2D;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Test {@link TwoLayeredBipartiteLayout2D}.
 * 
 * @author Dimitrios Michail
 */
public class TwoLayeredBipartiteLayout2DTest
{

    @Test
    public void testVertical()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();
        String v2 = graph.addVertex();
        String v3 = graph.addVertex();
        String v4 = graph.addVertex();
        String v5 = graph.addVertex();
        String v6 = graph.addVertex();

        graph.addEdge(v1, v4);
        graph.addEdge(v1, v5);
        graph.addEdge(v1, v6);
        graph.addEdge(v2, v4);
        graph.addEdge(v2, v5);
        graph.addEdge(v3, v5);
        graph.addEdge(v3, v6);

        TwoLayeredBipartiteLayout2D<String, DefaultEdge> alg = new TwoLayeredBipartiteLayout2D<>();

        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(0d, 0d, 3d, 10d));
        alg.layout(graph, model);

        assertEquals(Point2D.of(0.0, 0.0), model.get(v1));
        assertEquals(Point2D.of(0.0, 5.0), model.get(v2));
        assertEquals(Point2D.of(0.0, 10.0), model.get(v3));

        assertEquals(Point2D.of(3.0, 0.0), model.get(v4));
        assertEquals(Point2D.of(3.0, 5.0), model.get(v5));
        assertEquals(Point2D.of(3.0, 10.0), model.get(v6));
    }

    @Test
    public void testHorizontal()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();
        String v2 = graph.addVertex();
        String v3 = graph.addVertex();
        String v4 = graph.addVertex();
        String v5 = graph.addVertex();
        String v6 = graph.addVertex();

        graph.addEdge(v1, v4);
        graph.addEdge(v1, v5);
        graph.addEdge(v1, v6);
        graph.addEdge(v2, v4);
        graph.addEdge(v2, v5);
        graph.addEdge(v3, v5);
        graph.addEdge(v3, v6);

        TwoLayeredBipartiteLayout2D<String, DefaultEdge> alg = new TwoLayeredBipartiteLayout2D<>();
        alg.withVertical(false);

        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(0d, 0d, 10d, 3d));
        alg.layout(graph, model);

        assertEquals(Point2D.of(0.0, 0.0), model.get(v1));
        assertEquals(Point2D.of(5.0, 0.0), model.get(v2));
        assertEquals(Point2D.of(10.0, 0.0), model.get(v3));

        assertEquals(Point2D.of(0.0, 3.0), model.get(v4));
        assertEquals(Point2D.of(5.0, 3.0), model.get(v5));
        assertEquals(Point2D.of(10.0, 3.0), model.get(v6));
    }

    @Test
    public void testWithPartition()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();
        String v2 = graph.addVertex();
        String v3 = graph.addVertex();
        String v4 = graph.addVertex();
        String v5 = graph.addVertex();
        String v6 = graph.addVertex();
        String v7 = graph.addVertex();

        graph.addEdge(v1, v4);
        graph.addEdge(v1, v5);
        graph.addEdge(v1, v6);
        graph.addEdge(v2, v4);
        graph.addEdge(v2, v5);
        graph.addEdge(v3, v5);
        graph.addEdge(v3, v6);

        TwoLayeredBipartiteLayout2D<String, DefaultEdge> alg = new TwoLayeredBipartiteLayout2D<>();
        alg.withFirstPartition(Set.of(v1, v2, v3, v7));

        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(0d, 0d, 3d, 10d));
        alg.layout(graph, model);

        assertEquals(Point2D.of(0.0, 0.0), model.get(v1));
        assertEquals(Point2D.of(0.0, 3.3333333333333335), model.get(v2));
        assertEquals(Point2D.of(0.0, 6.666666666666667), model.get(v3));
        assertEquals(Point2D.of(0.0, 10.0), model.get(v7));

        assertEquals(Point2D.of(3.0, 0.0), model.get(v4));
        assertEquals(Point2D.of(3.0, 5.0), model.get(v5));
        assertEquals(Point2D.of(3.0, 10.0), model.get(v6));
    }

}
