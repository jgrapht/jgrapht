/*
 * (C) Copyright 2018-2019, by Dimitrios Michail and Contributors.
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

import java.util.Random;

import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.MapLayoutModel2D;
import org.jgrapht.alg.drawing.model.Point2D;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Test {@link RandomLayoutAlgorithm2D}.
 * 
 * @author Dimitrios Michail
 */
public class RandomLayoutAlgorithm2DTest
{

    @Test
    public void testRandom()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();
        String v2 = graph.addVertex();
        String v3 = graph.addVertex();
        String v4 = graph.addVertex();

        RandomLayoutAlgorithm2D<String, DefaultEdge> alg = new RandomLayoutAlgorithm2D<>(5L);

        MapLayoutModel2D<String> model = new MapLayoutModel2D<>(Box2D.of(0d, 0d, 10d, 20d));
        alg.layout(graph, model);

        Random rng = new Random(5L);
        assertEquals(Point2D.of(10 * rng.nextDouble(), 20 * rng.nextDouble()), model.get(v1));
        assertEquals(Point2D.of(10 * rng.nextDouble(), 20 * rng.nextDouble()), model.get(v2));
        assertEquals(Point2D.of(10 * rng.nextDouble(), 20 * rng.nextDouble()), model.get(v3));
        assertEquals(Point2D.of(10 * rng.nextDouble(), 20 * rng.nextDouble()), model.get(v4));
    }

}
