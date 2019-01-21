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
 * A collection of utilities to assist with point manipulation.
 * 
 * @author Dimitrios Michail
 */
public abstract class Points
{
    /**
     * Compute the length of a vector
     * 
     * @param v the vector
     * @return the length of a vector
     */
    public static double length(Point2D<Double> v)
    {
        return Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY());
    }

    /**
     * Add 2-dimensional vectors
     * 
     * @param a the first vector
     * @param b the second vector
     * @return the vector $a+b$
     */
    public static Point2D<Double> add(Point2D<Double> a, Point2D<Double> b)
    {
        return DoublePoint2D.of(a.getX() + b.getX(), a.getY() + b.getY());
    }

    /**
     * Subtract 2-dimensional vectors
     * 
     * @param a the first vector
     * @param b the second vector
     * @return the vector $a-b$
     */
    public static Point2D<Double> sub(Point2D<Double> a, Point2D<Double> b)
    {
        return DoublePoint2D.of(a.getX() - b.getX(), a.getY() - b.getY());
    }

    /**
     * Given a vector $a$ compute $-a$.
     * 
     * @param a the vector
     * @return the vector $-a$
     */
    public static Point2D<Double> minus(Point2D<Double> a)
    {
        return DoublePoint2D.of(-1 * a.getX(), -1 * a.getY());
    }

    /**
     * Multiply a vector with a scalar.
     * 
     * @param a the vector
     * @param scalar the scalar
     * @return the result of scalar multiplication
     */
    public static Point2D<Double> scalarMultiply(Point2D<Double> a, double scalar)
    {
        return DoublePoint2D.of(a.getX() * scalar, a.getY() * scalar);
    }
    
}
