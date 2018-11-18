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

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A 2-dimensional point using {@link BigDecimal}.
 * 
 * @author Dimitrios Michail
 */
public class BigDecimalPoint2D
    extends
    Point2D<BigDecimal>
{
    private static final long serialVersionUID = 1781118585980187727L;

    /**
     * Create a new point located at the origin.
     */
    public BigDecimalPoint2D()
    {
        this(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public BigDecimalPoint2D(BigDecimal x, BigDecimal y)
    {
        super(new BigDecimal[2]);
        coordinates[0] = Objects.requireNonNull(x);
        coordinates[1] = Objects.requireNonNull(y);
    }
    
    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the new point
     */
    public static BigDecimalPoint2D of(BigDecimal x, BigDecimal y)
    {
        return new BigDecimalPoint2D(x, y);
    }

}
