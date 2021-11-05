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

/**
 * A 2-dimensional point in Euclidean space.
 * 
 * @author Dimitrios Michail
 */
public class Point2D
    implements
    Serializable
{
    private static final long serialVersionUID = -5410937389829502498L;

    protected double x, y;

    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Point2D(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Get the number of dimensions of the point
     * 
     * @return the number of dimensions of the point
     */
    public int getNumDimensions()
    {
        return 2;
    }

    /**
     * Get the x coordinate
     * 
     * @return the x coordinate
     */
    public double getX()
    {
        return x;
    }

    /**
     * Get the y coordinate
     * 
     * @return the y coordinate
     */
    public double getY()
    {
        return y;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        Point2D other = (Point2D) obj;
        if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
            return false;
        if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
            return false;
        return true;
    }

    /**
     * Create a new point
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the point
     */
    public static Point2D of(double x, double y)
    {
        return new Point2D(x, y);
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }

}
