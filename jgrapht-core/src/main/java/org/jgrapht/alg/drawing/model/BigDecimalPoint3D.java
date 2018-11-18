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
 * A 3-dimensional point using {@link BigDecimal}.
 * 
 * @author Dimitrios Michail
 */
public class BigDecimalPoint3D
    extends
    Point3D<BigDecimal>
{
    private static final long serialVersionUID = -1264864509576699838L;

    /**
     * Create a new point located at the origin.
     */
    public BigDecimalPoint3D()
    {
        this(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public BigDecimalPoint3D(BigDecimal x, BigDecimal y, BigDecimal z)
    {
        super(new BigDecimal[3]);
        coordinates[0] = Objects.requireNonNull(x);
        coordinates[1] = Objects.requireNonNull(y);
        coordinates[2] = Objects.requireNonNull(z);
    }

    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the new point
     */
    public static BigDecimalPoint3D of(BigDecimal x, BigDecimal y, BigDecimal z)
    {
        return new BigDecimalPoint3D(x, y, z);
    }

}
