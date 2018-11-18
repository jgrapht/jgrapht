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

import org.jgrapht.alg.util.Pair;

/**
 * A collection of utilities to assist with rectangles manipulation.
 * 
 * @author Dimitrios Michail
 */
public abstract class Rectangles
{
    /**
     * Test whether a rectangle contains a point.
     * 
     * @param rectangle the rectangle
     * @param p the point
     * @return true if the point is contained inside the rectangle, false otherwise
     */
    public static boolean containsPoint(Rectangle2D<Double> rectangle, Point2D<Double> p)
    {
        double maxX = rectangle.getMinX() + rectangle.getWidth();
        if (p.getX() > maxX) {
            return false;
        }
        if (p.getX() < rectangle.getMinX()) {
            return false;
        }
        double maxY = rectangle.getMinY() + rectangle.getHeight();
        if (p.getY() > maxY) {
            return false;
        }
        if (p.getY() < rectangle.getMinY()) {
            return false;
        }
        return true;
    }

    /**
     * Test whether a rectangle contains a point.
     * 
     * @param rectangle the rectangle
     * @param p the point
     * @return true if the point is contained inside the rectangle, false otherwise
     */
    public static boolean containsPoint(Rectangle3D<Double> rectangle, Point3D<Double> p)
    {
        double maxX = rectangle.getMinX() + rectangle.getWidth();
        if (p.getX() > maxX) {
            return false;
        }
        if (p.getX() < rectangle.getMinX()) {
            return false;
        }
        double maxY = rectangle.getMinY() + rectangle.getHeight();
        if (p.getY() > maxY) {
            return false;
        }
        if (p.getY() < rectangle.getMinY()) {
            return false;
        }
        double maxZ = rectangle.getMinZ() + rectangle.getDepth();
        if (p.getZ() > maxZ) {
            return false;
        }
        if (p.getZ() < rectangle.getMinZ()) {
            return false;
        }
        return true;
    }

    /**
     * Split a rectangle along the x axis into two equal rectangles.
     * 
     * @param rectangle the rectangle to split
     * @return a pair with the two resulting rectangles
     */
    public static Pair<Rectangle2D<Double>, Rectangle2D<Double>> splitAlongXAxis(
        Rectangle2D<Double> rectangle)
    {
        double newWidth = rectangle.getWidth() / 2d;
        double height = rectangle.getHeight();
        return Pair
            .of(
                DoubleRectangle2D.of(rectangle.getMinX(), rectangle.getMinY(), newWidth, height),
                DoubleRectangle2D
                    .of(rectangle.getMinX() + newWidth, rectangle.getMinY(), newWidth, height));
    }

    /**
     * Split a rectangle along the y axis into two equal rectangles.
     * 
     * @param rectangle the rectangle to split
     * @return a pair with the two resulting rectangles
     */
    public static Pair<Rectangle2D<Double>, Rectangle2D<Double>> splitAlongYAxis(
        Rectangle2D<Double> rectangle)
    {
        double width = rectangle.getWidth();
        double newHeight = rectangle.getHeight() / 2d;
        return Pair
            .of(
                DoubleRectangle2D.of(rectangle.getMinX(), rectangle.getMinY(), width, newHeight),
                DoubleRectangle2D
                    .of(rectangle.getMinX(), rectangle.getMinY() + newHeight, width, newHeight));
    }

}
