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

import java.util.Objects;

/**
 * A 3-dimensional point
 * 
 * @author Dimitrios Michail
 *
 * @param <N> the number type
 */
public class Point3D<N>
    extends
    Point<N>
{
    private static final long serialVersionUID = 4886925782083650556L;

    protected N x, y, z;
    
    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public Point3D(N x, N y, N z)
    {
        this.x = Objects.requireNonNull(x);
        this.y = Objects.requireNonNull(y);
        this.z = Objects.requireNonNull(z);
    }

    /**
     * Get the x coordinate
     * 
     * @return the x coordinate
     */
    public N getX()
    {
        return x;
    }

    /**
     * Get the y coordinate
     * 
     * @return the y coordinate
     */
    public N getY()
    {
        return y;
    }

    /**
     * Get the z coordinate
     * 
     * @return the z coordinate
     */
    public N getZ()
    {
        return z;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((x == null) ? 0 : x.hashCode());
        result = prime * result + ((y == null) ? 0 : y.hashCode());
        result = prime * result + ((z == null) ? 0 : z.hashCode());
        return result;
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
        Point3D<?> other = (Point3D<?>) obj;
        if (x == null) {
            if (other.x != null)
                return false;
        } else if (!x.equals(other.x))
            return false;
        if (y == null) {
            if (other.y != null)
                return false;
        } else if (!y.equals(other.y))
            return false;
        if (z == null) {
            if (other.z != null)
                return false;
        } else if (!z.equals(other.z))
            return false;
        return true;
    }
    
}
