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

import java.util.*;
import java.util.Map.*;

/**
 * A layout model which uses a hashtable to store the vertices' locations.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the vertex type
 */
public class MapLayoutModel2D<V>
    implements
    LayoutModel2D<V>
{
    protected Box2D drawableArea;
    protected Map<V, Point2D> points;
    protected Set<V> fixed;

    /**
     * Create a new model.
     * 
     * @param drawableArea the drawable area
     */
    public MapLayoutModel2D(Box2D drawableArea)
    {
        this.drawableArea = drawableArea;
        this.points = new LinkedHashMap<>();
        this.fixed = new HashSet<>();
    }

    @Override
    public Box2D getDrawableArea()
    {
        return drawableArea;
    }

    @Override
    public void setDrawableArea(Box2D drawableArea)
    {
        this.drawableArea = drawableArea;
    }

    @Override
    public Iterator<Entry<V, Point2D>> iterator()
    {
        return points.entrySet().iterator();
    }

    @Override
    public Point2D get(V vertex)
    {
        return points.get(vertex);
    }

    @Override
    public Point2D put(V vertex, Point2D point)
    {
        boolean isFixed = fixed.contains(vertex);
        if (!isFixed) {
            return points.put(vertex, point);
        }
        return points.putIfAbsent(vertex, point);
    }

    @Override
    public void setFixed(V vertex, boolean fixed)
    {
        if (fixed) {
            this.fixed.add(vertex);
        } else {
            this.fixed.remove(vertex);
        }
    }

    @Override
    public boolean isFixed(V vertex)
    {
        return fixed.contains(vertex);
    }

}
