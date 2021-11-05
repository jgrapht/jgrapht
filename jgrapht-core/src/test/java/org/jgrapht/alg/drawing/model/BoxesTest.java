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

import org.jgrapht.alg.util.*;
import org.junit.*;

import static org.junit.Assert.*;

/**
 * Test {@link Boxes}.
 * 
 * @author Dimitrios Michail
 */
public class BoxesTest
{

    @Test
    public void testContainsPoint2D()
    {
        Box2D region = Box2D.of(0, 0, 10, 10);

        assertTrue(Boxes.containsPoint(region, Point2D.of(5, 5)));
        assertTrue(Boxes.containsPoint(region, Point2D.of(10, 5)));
        assertTrue(Boxes.containsPoint(region, Point2D.of(10, 10)));
        assertTrue(Boxes.containsPoint(region, Point2D.of(0, 0)));
        assertTrue(Boxes.containsPoint(region, Point2D.of(0, 10)));
        assertTrue(Boxes.containsPoint(region, Point2D.of(10, 0)));
        assertFalse(Boxes.containsPoint(region, Point2D.of(11, 0)));
        assertFalse(Boxes.containsPoint(region, Point2D.of(0, 11)));
    }

    @Test
    public void testSplitAlongXAxis()
    {
        Box2D region = Box2D.of(5, 5, 10, 10);
        Pair<Box2D, Box2D> pair = Boxes.splitAlongXAxis(region);
        Box2D westRegion = pair.getFirst();
        Box2D eastRegion = pair.getSecond();

        assertEquals(5d, westRegion.getMinX(), 1e-9);
        assertEquals(5d, westRegion.getMinY(), 1e-9);
        assertEquals(10d, westRegion.getHeight(), 1e-9);
        assertEquals(5d, westRegion.getWidth(), 1e-9);

        assertEquals(10d, eastRegion.getMinX(), 1e-9);
        assertEquals(5d, eastRegion.getMinY(), 1e-9);
        assertEquals(10d, eastRegion.getHeight(), 1e-9);
        assertEquals(5d, eastRegion.getWidth(), 1e-9);
    }

    @Test
    public void testSplitAlongYAxis()
    {
        Box2D region = Box2D.of(5, 5, 10, 10);
        Pair<Box2D, Box2D> pair = Boxes.splitAlongYAxis(region);
        Box2D southRegion = pair.getFirst();
        Box2D northRegion = pair.getSecond();

        assertEquals(5d, southRegion.getMinX(), 1e-9);
        assertEquals(5d, southRegion.getMinY(), 1e-9);
        assertEquals(5d, southRegion.getHeight(), 1e-9);
        assertEquals(10d, southRegion.getWidth(), 1e-9);

        assertEquals(5d, northRegion.getMinX(), 1e-9);
        assertEquals(10d, northRegion.getMinY(), 1e-9);
        assertEquals(5d, northRegion.getHeight(), 1e-9);
        assertEquals(10d, northRegion.getWidth(), 1e-9);
    }

}
