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

import java.util.Comparator;

import org.jgrapht.alg.util.ToleranceDoubleComparator;

/**
 * A 2-dimensional point using {@link Double}.
 * 
 * @author Dimitrios Michail
 */
public class DoublePoint2D
    extends
    Point2D<Double>
{
    private static final long serialVersionUID = -4392275602752913066L;

    private static final Comparator<Double> TOLERANCE_DOUBLE_COMPARATOR =
        new ToleranceDoubleComparator();

    /**
     * Create a new point located at the origin.
     */
    public DoublePoint2D()
    {
        this(0d, 0d);
    }

    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public DoublePoint2D(double x, double y)
    {
        super(x, y);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DoublePoint2D other = (DoublePoint2D) obj;

        return TOLERANCE_DOUBLE_COMPARATOR.compare(getX(), other.getX()) == 0
            && TOLERANCE_DOUBLE_COMPARATOR.compare(getY(), other.getY()) == 0;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((x == null) ? 0 : x.hashCode());
        result = prime * result + ((y == null) ? 0 : y.hashCode());
        return result;
    }

    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the point
     */
    public static DoublePoint2D of(double x, double y)
    {
        return new DoublePoint2D(x, y);
    }

}
