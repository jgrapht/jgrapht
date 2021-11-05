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
 * Test {@link Point2D}.
 * 
 * @author Dimitrios Michail
 */
public class Point2DTest
{

    @Test
    public void testDefaultConstructor()
    {
        Point2D p = new Point2D(0d, 0d);
        assertEquals(p.getX(), 0d, 1e-9);
        assertEquals(p.getY(), 0d, 1e-9);
    }

    @Test
    public void testConstructorAndGetters()
    {
        Point2D p = new Point2D(3d, 2d);
        assertEquals(p.getX(), 3d, 1e-9);
        assertEquals(p.getY(), 2d, 1e-9);
    }

}
