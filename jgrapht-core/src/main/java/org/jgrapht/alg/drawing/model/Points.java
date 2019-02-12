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

import java.util.function.BiFunction;

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
     * Compute the length of a vector
     * 
     * @param v the vector
     * @return the length of a vector
     */
    public static double length(Point3D<Double> v)
    {
        return Math.sqrt(v.getX() * v.getX() + v.getY() * v.getY() + v.getZ() * v.getZ());
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
     * Add 3-dimensional vectors
     * 
     * @param a the first vector
     * @param b the second vector
     * @return the vector $a+b$
     */
    public static Point3D<Double> add(Point3D<Double> a, Point3D<Double> b)
    {
        return DoublePoint3D.of(a.getX() + b.getX(), a.getY() + b.getY(), a.getZ() + b.getZ());
    }

    /**
     * Subtract 2-dimensional vectors
     * 
     * @param a the first vector
     * @param b the second vector
     * @return the vector $a-b$
     */
    public static Point2D<Double> subtract(Point2D<Double> a, Point2D<Double> b)
    {
        return DoublePoint2D.of(a.getX() - b.getX(), a.getY() - b.getY());
    }
    
    /**
     * Subtract 3-dimensional vectors
     * 
     * @param a the first vector
     * @param b the second vector
     * @return the vector $a-b$
     */
    public static Point3D<Double> subtract(Point3D<Double> a, Point3D<Double> b)
    {
        return DoublePoint3D.of(a.getX() - b.getX(), a.getY() - b.getY(), a.getZ() - b.getZ());
    }

    /**
     * Given a vector $a$ compute $-a$.
     * 
     * @param a the vector
     * @return the vector $-a$
     */
    public static Point2D<Double> minus(Point2D<Double> a)
    {
        return scalarMultiply(a, -1.0);
    }

    /**
     * Given a vector $a$ compute $-a$.
     * 
     * @param a the vector
     * @return the vector $-a$
     */
    public static Point3D<Double> minus(Point3D<Double> a)
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
    public static Point2D<Double> scalarMultiply(Point2D<Double> a, double scalar)
    {
        return scalarMultiply(a, scalar, (x, s) -> x * s);
    }

    /**
     * Multiply a vector with a scalar.
     * 
     * @param a the vector
     * @param scalar the scalar
     * @return the result of scalar multiplication
     */
    public static Point3D<Double> scalarMultiply(Point3D<Double> a, double scalar)
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
     * @param <N> the number type
     * @param <S> the scalar type
     */
    public static <N, S> Point2D<N> scalarMultiply(Point2D<N> a, S scalar, BiFunction<N, S, N> mult)
    {
        return new Point2D<N>(mult.apply(a.getX(), scalar), mult.apply(a.getY(), scalar));
    }

    /**
     * Multiply a vector with a scalar.
     * 
     * @param a the vector
     * @param scalar the scalar
     * @param mult the multiplication operator
     * @return the result of scalar multiplication
     * 
     * @param <N> the number type
     * @param <S> the scalar type
     */
    public static <N, S> Point3D<N> scalarMultiply(Point3D<N> a, S scalar, BiFunction<N, S, N> mult)
    {
        return new Point3D<N>(
            mult.apply(a.getX(), scalar), mult.apply(a.getY(), scalar),
            mult.apply(a.getZ(), scalar));
    }

}
