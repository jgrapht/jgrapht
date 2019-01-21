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

import java.util.function.Function;

/**
 * A 2d layout model which uses a hashtable to store the vertices' locations.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the vertex type
 * @param <N> the number type
 */
public class MapLayoutModel2D<V, N extends Number>
    extends
    MapLayoutModel<V, N, Point2D<N>, Box2D<N>>
{
    /**
     * Create a new model.
     * 
     * @param drawableArea the drawable area
     */
    public MapLayoutModel2D(Box2D<N> drawableArea)
    {
        super(drawableArea);
    }

    /**
     * Create a new model.
     * 
     * @param drawableArea the drawable area
     * @param initializer the vertex initializer
     */
    public MapLayoutModel2D(Box2D<N> drawableArea, Function<V, Point2D<N>> initializer)
    {
        super(drawableArea, initializer);
    }
}
