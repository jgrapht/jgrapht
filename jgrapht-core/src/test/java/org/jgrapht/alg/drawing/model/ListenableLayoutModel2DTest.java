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
package org.jgrapht.alg.drawing.model;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;
import java.util.function.*;

import static org.junit.Assert.*;

/**
 * Test {@link ListenableLayoutModel2D}.
 * 
 * @author Dimitrios Michail
 */
public class ListenableLayoutModel2DTest
{

    @Test
    public void testGeneral()
    {
        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.createDefaultEdgeSupplier()).buildGraph();

        String v1 = graph.addVertex();
        String v2 = graph.addVertex();

        MapLayoutModel2D<String> delegate = new MapLayoutModel2D<>(Box2D.of(0, 0, 2d, 2d));

        ListenableLayoutModel2D<String> model = new ListenableLayoutModel2D<>(delegate);

        CountListener listener = new CountListener();
        model.addListener(listener);

        assertEquals(Box2D.of(0d, 0d, 2d, 2d), model.getDrawableArea());

        assertNull(model.get(v1));
        model.put(v1, Point2D.of(3, 5));
        assertEquals(model.get(v1), Point2D.of(3, 5));
        assertFalse(model.isFixed(v1));
        assertEquals(1, listener.getCalled());
        assertEquals(Point2D.of(3, 5), listener.getLastPoint());
        assertEquals(v1, listener.getLastVertex());
        model.setFixed(v1, true);
        assertTrue(model.isFixed(v1));
        model.put(v1, Point2D.of(10, 20));
        assertEquals(model.get(v1), Point2D.of(3, 5));
        assertEquals(1, listener.getCalled());
        assertEquals(Point2D.of(3, 5), listener.getLastPoint());
        assertEquals(v1, listener.getLastVertex());
        model.setFixed(v1, false);
        assertFalse(model.isFixed(v1));
        model.put(v1, Point2D.of(10, 20));
        assertEquals(model.get(v1), Point2D.of(10, 20));
        assertEquals(2, listener.getCalled());
        assertEquals(Point2D.of(10, 20), listener.getLastPoint());
        assertEquals(v1, listener.getLastVertex());

        model.put(v2, Point2D.of(5, 7));
        assertEquals(3, listener.getCalled());
        assertEquals(Point2D.of(5, 7), listener.getLastPoint());
        assertEquals(v2, listener.getLastVertex());

        Map<String, Point2D> all = model.collect();
        assertEquals(all.get(v1), Point2D.of(10, 20));
        assertEquals(all.get(v2), Point2D.of(5, 7));
    }

    private static class CountListener
        implements
        BiConsumer<String, Point2D>
    {

        private int called = 0;
        private Point2D lastPoint;
        private String lastVertex;

        @Override
        public void accept(String t, Point2D u)
        {
            lastPoint = u;
            lastVertex = t;
            called++;
        }

        public int getCalled()
        {
            return called;
        }

        public Point2D getLastPoint()
        {
            return lastPoint;
        }

        public String getLastVertex()
        {
            return lastVertex;
        }

    }

}
