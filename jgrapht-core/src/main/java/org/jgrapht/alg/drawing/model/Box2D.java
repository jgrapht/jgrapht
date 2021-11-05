/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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

import java.io.*;
import java.util.*;

/**
 * A 2-dimensional box (rectangle).
 * 
 * @author Dimitrios Michail
 *
 */
public class Box2D
    implements
    Serializable
{
    private static final long serialVersionUID = -1855277817131669241L;

    /**
     * The coordinates of the lower corner
     */
    protected double[] coordinates;

    /**
     * The side lengths
     */
    protected double[] sides;

    /**
     * Create a new box
     * 
     * @param width the width
     * @param height the height
     */
    public Box2D(double width, double height)
    {
        this(0d, 0d, width, height);
    }

    /**
     * Create a new box
     * 
     * @param x the x coordinate of the lower-left corner
     * @param y the y coordinate of the lower-left corner
     * @param width the width
     * @param height the height
     */
    public Box2D(double x, double y, double width, double height)
    {
        this(new double[2], new double[2]);
        assert width >= 0d && height >= 0d;
        coordinates[0] = x;
        coordinates[1] = y;
        sides[0] = width;
        sides[1] = height;
    }

    /**
     * Create a new box
     * 
     * @param coordinates the lower left corner coordinates
     * @param sides width and height
     */
    public Box2D(double[] coordinates, double[] sides)
    {
        assert coordinates.length == 2;
        assert sides.length == 2;

        this.coordinates = Objects.requireNonNull(coordinates);
        this.sides = Objects.requireNonNull(sides);
        if (coordinates.length != sides.length) {
            throw new IllegalArgumentException("Box dimensions do not match");
        }
    }

    /**
     * Get the minimum x coordinate
     * 
     * @return the minimum x coordinate
     */
    public double getMinX()
    {
        return coordinates[0];
    }

    /**
     * Get the minimum y coordinate
     * 
     * @return the minimum y coordinate
     */
    public double getMinY()
    {
        return coordinates[1];
    }

    /**
     * Get the width
     * 
     * @return the width
     */
    public double getWidth()
    {
        return sides[0];
    }

    /**
     * Get the height
     * 
     * @return the height
     */
    public double getHeight()
    {
        return sides[1];
    }

    /**
     * Get the maximum x coordinate
     * 
     * @return the maximum x coordinate
     */
    public double getMaxX()
    {
        return coordinates[0] + sides[0];
    }

    /**
     * Get the maximum y coordinate
     * 
     * @return the maximum y coordinate
     */
    public double getMaxY()
    {
        return coordinates[1] + sides[1];
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
        Box2D other = (Box2D) obj;
        if (!Arrays.equals(coordinates, other.coordinates))
            return false;
        if (!Arrays.equals(sides, other.sides))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "Box2D [minX=" + coordinates[0] + ", minY=" + coordinates[1] + ", width=" + sides[0]
            + ", height=" + sides[1] + "]";
    }

    /**
     * Create a new box
     * 
     * @param width the width
     * @param height the height
     * @return the box
     */
    public static Box2D of(double width, double height)
    {
        return new Box2D(new double[] { 0.0, 0.0 }, new double[] { width, height });
    }

    /**
     * Create a new box
     * 
     * @param x the x coordinate of the lower-left corner
     * @param y the y coordinate of the lower-left corner
     * @param width the width
     * @param height the height
     * @return the box
     */
    public static Box2D of(double x, double y, double width, double height)
    {
        return new Box2D(new double[] { x, y }, new double[] { width, height });
    }

}
