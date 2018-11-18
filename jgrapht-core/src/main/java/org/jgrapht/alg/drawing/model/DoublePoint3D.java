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

import java.util.Arrays;
import java.util.Comparator;

import org.jgrapht.alg.util.ToleranceDoubleComparator;

/**
 * A 3-dimensional point using {@link Double}.
 * 
 * @author Dimitrios Michail
 */
public class DoublePoint3D
    extends
    Point3D<Double>
{
    private static final long serialVersionUID = -1928726466989546501L;

    private static final Comparator<Double> TOLERANCE_DOUBLE_COMPARATOR =
        new ToleranceDoubleComparator();

    /**
     * Create a new point located at the origin.
     */
    public DoublePoint3D()
    {
        this(0d, 0d, 0d);
    }

    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public DoublePoint3D(double x, double y, double z)
    {
        super(new Double[3]);
        coordinates[0] = x;
        coordinates[1] = y;
        coordinates[2] = z;
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
        DoublePoint3D other = (DoublePoint3D) obj;

        return TOLERANCE_DOUBLE_COMPARATOR.compare(getX(), other.getX()) == 0
            && TOLERANCE_DOUBLE_COMPARATOR.compare(getY(), other.getY()) == 0
            && TOLERANCE_DOUBLE_COMPARATOR.compare(getZ(), other.getZ()) == 0;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(coordinates);
        return result;
    }

    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the point
     */
    public static DoublePoint3D of(double x, double y, double z)
    {
        return new DoublePoint3D(x, y, z);
    }

}
