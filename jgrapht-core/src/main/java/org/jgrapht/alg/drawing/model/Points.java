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

import org.jgrapht.alg.util.*;

import java.util.function.*;

/**
 * A collection of utilities to assist with point manipulation.
 * 
 * @author Dimitrios Michail
 */
public abstract class Points
{
    private static final ToleranceDoubleComparator TOLERANCE_DOUBLE_COMPARATOR =
        new ToleranceDoubleComparator(1e-9);

    /**
     * Compute the length of a vector. The length of vector $(x,y)$ is given by $\sqrt{x^2+y^2}$.
     * 
     * @param v the vector
     * @return the length of a vector
     */
    public static double length(Point2D v)
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
    public static Point2D add(Point2D a, Point2D b)
    {
        return Point2D.of(a.getX() + b.getX(), a.getY() + b.getY());
    }

    /**
     * Subtract 2-dimensional vectors
     * 
     * @param a the first vector
     * @param b the second vector
     * @return the vector $a-b$
     */
    public static Point2D subtract(Point2D a, Point2D b)
    {
        return Point2D.of(a.getX() - b.getX(), a.getY() - b.getY());
    }

    /**
     * Given a vector $a$ compute $-a$.
     * 
     * @param a the vector
     * @return the vector $-a$
     */
    public static Point2D negate(Point2D a)
    {
        return scalarMultiply(a, -1.0);
    }

    /**
     * Multiply a vector with a scalar.
     * 
     * @param a the vector
     * @param scalar the scalar
     * @return the result of scalar multiplication
     */
    public static Point2D scalarMultiply(Point2D a, double scalar)
    {
        return scalarMultiply(a, scalar, (x, s) -> x * s);
    }

    /**
     * Multiply a vector with a scalar.
     * 
     * @param a the vector
     * @param scalar the scalar
     * @param mult the multiplication operator
     * @return the result of scalar multiplication
     * 
     * @param <S> the scalar type
     */
    public static <
        S> Point2D scalarMultiply(Point2D a, S scalar, BiFunction<Double, S, Double> mult)
    {
        return Point2D.of(mult.apply(a.getX(), scalar), mult.apply(a.getY(), scalar));
    }

    /**
     * Compare two points for equality using tolerance 1e-9.
     * 
     * @param p1 the first point
     * @param p2 the second point
     * @return whether the two points are equal or not
     */
    public static boolean equals(Point2D p1, Point2D p2)
    {
        int xEquals = TOLERANCE_DOUBLE_COMPARATOR.compare(p1.getX(), p2.getX());
        if (xEquals != 0) {
            return false;
        }
        return TOLERANCE_DOUBLE_COMPARATOR.compare(p1.getY(), p2.getY()) == 0;
    }

}
