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

/**
 * A 3-dimensional rectangle.
 * 
 * @author Dimitrios Michail
 *
 * @param <N> the number type
 */
public abstract class Rectangle3D<N extends Number>
    extends
    Rectangle<N>
{
    private static final long serialVersionUID = 7261288786532743819L;

    /**
     * Create a new rectangle
     * 
     * @param coordinates the lower left corner coordinates
     * @param sides width and height
     */
    public Rectangle3D(N[] coordinates, N[] sides)
    {
        super(coordinates, sides);
        assert coordinates.length == 3;
        assert sides.length == 3;
    }

    /**
     * Get the minimum x coordinate
     * 
     * @return the minimum x coordinate
     */
    public N getMinX()
    {
        return coordinates[0];
    }

    /**
     * Get the minimum y coordinate
     * 
     * @return the minimum y coordinate
     */
    public N getMinY()
    {
        return coordinates[1];
    }

    /**
     * Get the minimum y coordinate
     * 
     * @return the minimum y coordinate
     */
    public N getMinZ()
    {
        return coordinates[2];
    }

    /**
     * Get the width
     * 
     * @return the width
     */
    public N getWidth()
    {
        return sides[0];
    }

    /**
     * Get the height
     * 
     * @return the height
     */
    public N getHeight()
    {
        return sides[1];
    }

    /**
     * Get the depth
     * 
     * @return the depth
     */
    public N getDepth()
    {
        return sides[2];
    }

    @Override
    public String toString()
    {
        return "Rectangle3D [minX=" + coordinates[0] + ", minY=" + coordinates[1] + ", minZ="
            + coordinates[2] + ", width=" + sides[0] + ", height=" + sides[1] + ", depth="
            + sides[2] + "]";
    }

}
