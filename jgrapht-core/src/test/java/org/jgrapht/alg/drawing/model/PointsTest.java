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

import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * Test {@link Points}.
 * 
 * @author Dimitrios Michail
 */
public class PointsTest
{

    @Test
    public void testLength()
    {
        Point2D p = Point2D.of(5, 5);
        assertEquals(Math.sqrt(50), Points.length(p), 1e-9);
    }

    @Test
    public void testAdd()
    {
        Point2D p1 = Point2D.of(5, 5);
        Point2D p2 = Point2D.of(3, 4);
        Point2D p3 = Points.add(p1, p2);
        assertEquals(8d, p3.getX(), 1e-9);
        assertEquals(9d, p3.getY(), 1e-9);
    }

    @Test
    public void testSub()
    {
        Point2D p1 = Point2D.of(5, 5);
        Point2D p2 = Point2D.of(3, 4);
        Point2D p3 = Points.subtract(p1, p2);
        assertEquals(2d, p3.getX(), 1e-9);
        assertEquals(1d, p3.getY(), 1e-9);
    }

    @Test
    public void testMinus()
    {
        Point2D p1 = Point2D.of(5, 3);
        Point2D p2 = Points.negate(p1);
        assertEquals(-5d, p2.getX(), 1e-9);
        assertEquals(-3d, p2.getY(), 1e-9);
    }

    @Test
    public void testScalarMultiply()
    {
        Point2D p1 = Point2D.of(5, 3);
        Point2D p2 = Points.scalarMultiply(p1, 2);
        assertEquals(10d, p2.getX(), 1e-9);
        assertEquals(6d, p2.getY(), 1e-9);
    }

}
