/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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

import java.math.BigDecimal;

import org.jgrapht.alg.drawing.model.BigDecimalPoint3D;
import org.junit.Test;

/**
 * Test {@link BigDecimalPoint3D}.
 * 
 * @author Dimitrios Michail
 */
public class BigDecimalPoint3DTest
{

    @Test
    public void testDefaultConstructor()
    {
        BigDecimalPoint3D p = new BigDecimalPoint3D();
        assertEquals(p.getX(), BigDecimal.ZERO);
        assertEquals(p.getY(), BigDecimal.ZERO);
        assertEquals(p.getZ(), BigDecimal.ZERO);
    }

    @Test
    public void testConstructorAndGetterSetters()
    {
        BigDecimalPoint3D p = new BigDecimalPoint3D(BigDecimal.valueOf(3d), BigDecimal.valueOf(2d), BigDecimal.valueOf(9d));
        assertEquals(p.getX(), BigDecimal.valueOf(3d));
        assertEquals(p.getY(), BigDecimal.valueOf(2d));
        assertEquals(p.getZ(), BigDecimal.valueOf(9d));

        p.setX(BigDecimal.valueOf(5d));
        assertEquals(p.getX(), BigDecimal.valueOf(5d));
        assertEquals(p.getY(), BigDecimal.valueOf(2d));
        assertEquals(p.getZ(), BigDecimal.valueOf(9d));

        p.setY(BigDecimal.valueOf(13d));
        assertEquals(p.getX(), BigDecimal.valueOf(5d));
        assertEquals(p.getY(), BigDecimal.valueOf(13d));
        assertEquals(p.getZ(), BigDecimal.valueOf(9d));
        
        p.setZ(BigDecimal.valueOf(11d));
        assertEquals(p.getX(), BigDecimal.valueOf(5d));
        assertEquals(p.getY(), BigDecimal.valueOf(13d));
        assertEquals(p.getZ(), BigDecimal.valueOf(11d));
    }

}
