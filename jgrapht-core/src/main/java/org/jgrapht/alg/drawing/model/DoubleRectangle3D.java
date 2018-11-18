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
 * A 3-dimensional rectangle using {@link Double}.
 * 
 * @author Dimitrios Michail
 */
public class DoubleRectangle3D
    extends
    Rectangle3D<Double>
{
    private static final long serialVersionUID = 5504903679994699674L;

    /**
     * Create a new rectangle
     * 
     * @param width the width
     * @param height the height
     * @param depth the depth
     */
    public DoubleRectangle3D(double width, double height, double depth)
    {
        this(0d, 0d, 0d, width, height, depth);
    }

    /**
     * Create a new rectangle
     * 
     * @param x the x coordinate of the lower-left corner
     * @param y the y coordinate of the lower-left corner
     * @param z the y coordinate of the lower-left corner
     * @param width the width
     * @param height the height
     * @param depth the depth
     */
    public DoubleRectangle3D(
        double x, double y, double z, double width, double height, double depth)
    {
        super(new Double[3], new Double[3]);
        assert width >= 0d && height >= 0d && depth >= 0d;
        coordinates[0] = x;
        coordinates[1] = y;
        coordinates[2] = z;
        sides[0] = width;
        sides[1] = height;
        sides[2] = depth;
    }

    /**
     * Create a new rectangle
     * 
     * @param width the width
     * @param height the height
     * @param depth the depth
     * @return the rectangle
     */
    public static DoubleRectangle3D of(double width, double height, double depth)
    {
        return new DoubleRectangle3D(width, height, depth);
    }

    /**
     * Create a new rectangle
     * 
     * @param x the x coordinate of the lower-left corner
     * @param y the y coordinate of the lower-left corner
     * @param z the y coordinate of the lower-left corner
     * @param width the width
     * @param height the height
     * @param depth the depth
     * @return the rectangle
     */
    public static DoubleRectangle3D of(
        double x, double y, double z, double width, double height, double depth)
    {
        return new DoubleRectangle3D(x, y, z, width, height, depth);
    }

}
