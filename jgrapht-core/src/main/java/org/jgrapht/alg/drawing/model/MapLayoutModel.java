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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

/**
 * A layout model which uses a hashtable to store the vertices' locations.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the vertex type
 * @param <N> the number type
 * @param <P> the point type
 * @param <B> the box type
 */
public class MapLayoutModel<V, N extends Number, P extends Point<N>, B extends Box<N>>
    implements
    LayoutModel<V, N, P, B>
{
    protected B drawableArea;
    protected Function<V, P> initializer;
    protected Map<V, P> points;
    protected Set<V> fixed;

    /**
     * Create a new model.
     * 
     * @param drawableArea the drawable area
     */
    public MapLayoutModel(B drawableArea)
    {
        this(drawableArea, null);
    }

    /**
     * Create a new model.
     * 
     * @param drawableArea the drawable area
     * @param initializer the vertex initializer
     */
    public MapLayoutModel(B drawableArea, Function<V, P> initializer)
    {
        this.drawableArea = drawableArea;
        this.initializer = initializer;
        this.points = new LinkedHashMap<>();
        this.fixed = new HashSet<>();
    }

    @Override
    public B getDrawableArea()
    {
        return drawableArea;
    }

    @Override
    public void setDrawableArea(B drawableArea)
    {
        this.drawableArea = drawableArea;
    }

    @Override
    public Function<V, P> getInitializer()
    {
        return initializer;
    }

    /**
     * Set the vertex initializer
     * 
     * @param initializer the initializer
     */
    public void setInitializer(Function<V, P> initializer)
    {
        this.initializer = initializer;
    }

    @Override
    public Iterator<Entry<V, P>> iterator()
    {
        return points.entrySet().iterator();
    }

    @Override
    public P get(V vertex)
    {
        return points.get(vertex);
    }

    @Override
    public P put(V vertex, P point)
    {
        boolean isFixed = fixed.contains(vertex);
        if (!isFixed) {
            return points.put(vertex, point);
        }
        P current = points.get(vertex);
        if (current == null) {
            points.put(vertex, point);
        }
        return current;
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
