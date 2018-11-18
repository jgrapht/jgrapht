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
 * A 2-dimensional point.
 * 
 * @author Dimitrios Michail
 *
 * @param <N> the number type
 */
public abstract class Point2D<N>
    extends
    Point<N>
{
    private static final long serialVersionUID = -5410937389829502498L;

    /**
     * Create a new point
     * 
     * @param coordinates the coordinates
     */
    public Point2D(N[] coordinates)
    {
        super(coordinates);
        assert coordinates.length == 2;
    }

    /**
     * Get the x coordinate
     * 
     * @return the x coordinate
     */
    public N getX()
    {
        return coordinates[0];
    }

    /**
     * Get the y coordinate
     * 
     * @return the y coordinate
     */
    public N getY()
    {
        return coordinates[1];
    }

}
