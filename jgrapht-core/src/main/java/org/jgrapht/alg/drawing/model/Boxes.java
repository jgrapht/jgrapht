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

import java.util.Comparator;
import java.util.function.BinaryOperator;

import org.jgrapht.alg.util.Pair;

/**
 * A collection of utilities to assist with boxes manipulation.
 * 
 * @author Dimitrios Michail
 */
public abstract class Boxes
{
    /**
     * Test whether a box contains a point.
     * 
     * @param box the box
     * @param p the point
     * @return true if the point is contained inside the box, false otherwise
     */
    public static boolean containsPoint(Box2D<Double> box, Point2D<Double> p)
    {
        double maxX = box.getMinX() + box.getWidth();
        if (p.getX() > maxX) {
            return false;
        }
        if (p.getX() < box.getMinX()) {
            return false;
        }
        double maxY = box.getMinY() + box.getHeight();
        if (p.getY() > maxY) {
            return false;
        }
        if (p.getY() < box.getMinY()) {
            return false;
        }
        return true;
    }

    /**
     * Test whether a box contains a point.
     * 
     * @param box the box
     * @param p the point
     * @return true if the point is contained inside the box, false otherwise
     */
    public static boolean containsPoint(Box3D<Double> box, Point3D<Double> p)
    {
        double maxX = box.getMinX() + box.getWidth();
        if (p.getX() > maxX) {
            return false;
        }
        if (p.getX() < box.getMinX()) {
            return false;
        }
        double maxY = box.getMinY() + box.getHeight();
        if (p.getY() > maxY) {
            return false;
        }
        if (p.getY() < box.getMinY()) {
            return false;
        }
        double maxZ = box.getMinZ() + box.getDepth();
        if (p.getZ() > maxZ) {
            return false;
        }
        if (p.getZ() < box.getMinZ()) {
            return false;
        }
        return true;
    }

    /**
     * Split a box along the x axis into two equal box.
     * 
     * @param box the box to split
     * @return a pair with the two resulting boxes
     */
    public static Pair<Box2D<Double>, Box2D<Double>> splitAlongXAxis(
        Box2D<Double> box)
    {
        double newWidth = box.getWidth() / 2d;
        double height = box.getHeight();
        return Pair
            .of(
                DoubleBox2D.of(box.getMinX(), box.getMinY(), newWidth, height),
                DoubleBox2D
                    .of(box.getMinX() + newWidth, box.getMinY(), newWidth, height));
    }

    /**
     * Split a box along the y axis into two equal boxes.
     * 
     * @param box the box to split
     * @return a pair with the two resulting boxes
     */
    public static Pair<Box2D<Double>, Box2D<Double>> splitAlongYAxis(
        Box2D<Double> box)
    {
        double width = box.getWidth();
        double newHeight = box.getHeight() / 2d;
        return Pair
            .of(
                DoubleBox2D.of(box.getMinX(), box.getMinY(), width, newHeight),
                DoubleBox2D
                    .of(box.getMinX(), box.getMinY() + newHeight, width, newHeight));
    }
    
    /**
     * Test whether a box contains a point.
     * 
     * @param box the box
     * @param p the point
     * @param add addition operator for the number type
     * @param comparator comparator for the number type
     * @return true if the point is contained inside the box, false otherwise
     * @param <N> the number type
     */
    public static <N> boolean containsPoint(Box2D<N> box, Point2D<N> p, BinaryOperator<N> add, Comparator<N> comparator)
    {
        N maxX = add.apply(box.getMinX(), box.getWidth());
        if (comparator.compare(p.getX(), maxX) > 0) {
            return false;
        }
        if (comparator.compare(p.getX(), box.getMinX()) < 0) {
            return false;
        }
        N maxY = add.apply(box.getMinY(), box.getHeight());
        if (comparator.compare(p.getY(), maxY) > 0) {
            return false;
        }
        if (comparator.compare(p.getY(), box.getMinY()) < 0) {
            return false;
        }
        return true;
    }
    
    /**
     * Test whether a box contains a point.
     * 
     * @param box the box
     * @param p the point
     * @param add addition operator for the number type
     * @param comparator comparator for the number type
     * @return true if the point is contained inside the box, false otherwise
     * @param <N> the number type 
     */
    public static <N> boolean containsPoint(Box3D<N> box, Point3D<N> p, BinaryOperator<N> add, Comparator<N> comparator)
    {
        N maxX = add.apply(box.getMinX(), box.getWidth());
        if (comparator.compare(p.getX(), maxX) > 0) {
            return false;
        }
        if (comparator.compare(p.getX(), box.getMinX()) < 0) {
            return false;
        }
        N maxY = add.apply(box.getMinY(), box.getHeight());
        if (comparator.compare(p.getY(), maxY) > 0) {
            return false;
        }
        if (comparator.compare(p.getY(), box.getMinY()) < 0) {
            return false;
        }
        N maxZ = add.apply(box.getMinZ(), box.getDepth());
        if (comparator.compare(p.getZ(), maxZ) > 0) {
            return false;
        }
        if (comparator.compare(p.getZ(), box.getMinZ()) < 0) {
            return false;
        }
        return true;
    }

}
