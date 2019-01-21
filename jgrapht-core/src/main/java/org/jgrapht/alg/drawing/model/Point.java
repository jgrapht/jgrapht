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
import java.util.stream.Collectors;

/**
 * A base implementation for a point in Euclidean space.
 * 
 * @author Dimitrios Michail
 *
 * @param <N> the number type
 */
public abstract class Point<N>
    implements
    Serializable
{
    private static final long serialVersionUID = -1515743447961074994L;

    /**
     * The actual coordinates
     */
    protected N[] coordinates;

    /**
     * Create a new point
     * 
     * @param coordinates the coordinates of the point
     */
    public Point(N[] coordinates)
    {
        this.coordinates = Objects.requireNonNull(coordinates);
    }

    /**
     * Get the dimensions of the point
     * 
     * @return the dimensions of the point
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
        Point<?> other = (Point<?>) obj;
        if (!Arrays.equals(coordinates, other.coordinates))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return Arrays
            .asList(coordinates).stream().map(String::valueOf)
            .collect(Collectors.joining(",", "(", ")"));
    }

}
