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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * A base implementation for a box in Euclidean space.
 * 
 * @author Dimitrios Michail
 *
 * @param <N> the number type
 */
public abstract class Box<N>
    implements
    Serializable
{
    private static final long serialVersionUID = -8873885953537343861L;

    /**
     * The coordinates of the lower corner
     */
    protected N[] coordinates;

    /**
     * The side lengths
     */
    protected N[] sides;

    /**
     * Create a new box
     * 
     * @param coordinates the coordinates of lower corner
     * @param sides the side lengths
     */
    public Box(N[] coordinates, N[] sides)
    {
        this.coordinates = Objects.requireNonNull(coordinates);
        this.sides = Objects.requireNonNull(sides);
        if (coordinates.length != sides.length) {
            throw new IllegalArgumentException("Rectangle dimensions do not match");
        }
    }

    /**
     * Get the dimensions of the box
     * 
     * @return the dimensions of the box
     */
    public int getDimensions()
    {
        return coordinates.length;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(coordinates);
        result = prime * result + Arrays.hashCode(sides);
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
        Box<?> other = (Box<?>) obj;
        if (!Arrays.equals(coordinates, other.coordinates))
            return false;
        if (!Arrays.equals(sides, other.sides))
            return false;
        return true;
    }

}
