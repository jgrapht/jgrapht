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
package org.jgrapht.alg.drawing.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test {@link DoubleRectangle3D}.
 * 
 * @author Dimitrios Michail
 */
public class DoubleRectangle3DTest
{

    @Test
    public void testConstructor1()
    {
        DoubleRectangle3D p = new DoubleRectangle3D(5, 10, 15);
        assertEquals(p.getWidth(), 5d, 1e-9);
        assertEquals(p.getHeight(), 10d, 1e-9);
        assertEquals(p.getDepth(), 15d, 1e-9);
        assertEquals(p.getMinX(), 0d, 1e-9);
        assertEquals(p.getMinY(), 0d, 1e-9);
        assertEquals(p.getMinZ(), 0d, 1e-9);
    }

    @Test
    public void testFactoryMethod1()
    {
        DoubleRectangle3D p = DoubleRectangle3D.of(5, 10, 15);
        assertEquals(p.getWidth(), 5d, 1e-9);
        assertEquals(p.getHeight(), 10d, 1e-9);
        assertEquals(p.getDepth(), 15d, 1e-9);
        assertEquals(p.getMinX(), 0d, 1e-9);
        assertEquals(p.getMinY(), 0d, 1e-9);
        assertEquals(p.getMinZ(), 0d, 1e-9);
    }

    @Test
    public void testConstructor2()
    {
        DoubleRectangle3D p = new DoubleRectangle3D(4, 5, 6, 7, 8, 9);
        assertEquals(p.getMinX(), 4d, 1e-9);
        assertEquals(p.getMinY(), 5d, 1e-9);
        assertEquals(p.getMinZ(), 6d, 1e-9);
        assertEquals(p.getWidth(), 7d, 1e-9);
        assertEquals(p.getHeight(), 8d, 1e-9);
        assertEquals(p.getDepth(), 9d, 1e-9);
    }

    @Test
    public void testFactoryMethod2()
    {
        DoubleRectangle3D p = DoubleRectangle3D.of(4, 5, 6, 7, 8, 9);
        assertEquals(p.getMinX(), 4d, 1e-9);
        assertEquals(p.getMinY(), 5d, 1e-9);
        assertEquals(p.getMinZ(), 6d, 1e-9);
        assertEquals(p.getWidth(), 7d, 1e-9);
        assertEquals(p.getHeight(), 8d, 1e-9);
        assertEquals(p.getDepth(), 9d, 1e-9);
    }

}
