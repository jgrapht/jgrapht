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
 * A 2-dimensional box (rectangle) using {@link Double}.
 * 
 * @author Dimitrios Michail
 */
public class DoubleBox2D
    extends
    Box2D<Double>
{
    private static final long serialVersionUID = 8085178618558249008L;

    /**
     * Create a new box
     * 
     * @param width the width
     * @param height the height
     */
    public DoubleBox2D(double width, double height)
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
    public DoubleBox2D(double x, double y, double width, double height)
    {
        super(new Double[2], new Double[2]);
        assert width >= 0d && height >= 0d;
        coordinates[0] = x;
        coordinates[1] = y;
        sides[0] = width;
        sides[1] = height;
    }
    
    /**
     * Create a new box
     * 
     * @param width the width
     * @param height the height
     * @return the box
     */
    public static DoubleBox2D of(double width, double height)
    {
        return new DoubleBox2D(width, height);
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
    public static DoubleBox2D of(double x, double y, double width, double height)
    {
        return new DoubleBox2D(x, y, width, height);
    }

}
