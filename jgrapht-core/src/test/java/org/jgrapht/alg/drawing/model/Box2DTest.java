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
 * Test {@link Box2D}.
 * 
 * @author Dimitrios Michail
 */
public class Box2DTest
{
    private static final double EPS = 1e-9;

    @Test
    public void testConstructor1()
    {
        Box2D p = new Box2D(5, 10);
        assertEquals(p.getWidth(), 5d, EPS);
        assertEquals(p.getHeight(), 10d, EPS);
        assertEquals(p.getMinX(), 0d, EPS);
        assertEquals(p.getMinY(), 0d, EPS);
        assertEquals(p.getMaxX(), 5d, EPS);
        assertEquals(p.getMaxY(), 10d, EPS);
    }

    @Test
    public void testConstructor2()
    {
        Box2D p = new Box2D(5, 4, 7, 8);
        assertEquals(p.getWidth(), 7d, EPS);
        assertEquals(p.getHeight(), 8d, EPS);
        assertEquals(p.getMinX(), 5d, EPS);
        assertEquals(p.getMinY(), 4d, EPS);
        assertEquals(p.getMaxX(), 12d, EPS);
        assertEquals(p.getMaxY(), 12d, EPS);
    }

    @Test
    public void testFactoryMethod1()
    {
        Box2D p = Box2D.of(5, 10);
        assertEquals(p.getWidth(), 5d, EPS);
        assertEquals(p.getHeight(), 10d, EPS);
        assertEquals(p.getMinX(), 0d, EPS);
        assertEquals(p.getMinY(), 0d, EPS);
        assertEquals(p.getMaxX(), 5d, EPS);
        assertEquals(p.getMaxY(), 10d, EPS);
    }

    @Test
    public void testFactoryMethod2()
    {
        Box2D p = Box2D.of(5, 4, 7, 8);
        assertEquals(p.getWidth(), 7d, EPS);
        assertEquals(p.getHeight(), 8d, EPS);
        assertEquals(p.getMinX(), 5d, EPS);
        assertEquals(p.getMinY(), 4d, EPS);
        assertEquals(p.getMaxX(), 5d + 7d, EPS);
        assertEquals(p.getMaxY(), 4d + 8d, EPS);
    }

}
