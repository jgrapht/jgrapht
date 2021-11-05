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
import java.util.function.*;

/**
 * A layout model wrapper which adds support for listeners.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the vertex type
 */
public class ListenableLayoutModel2D<V>
    implements
    LayoutModel2D<V>
{
    protected LayoutModel2D<V> model;
    protected List<BiConsumer<V, Point2D>> listeners;

    /**
     * Create a new model
     * 
     * @param model the underlying layout model
     */
    public ListenableLayoutModel2D(LayoutModel2D<V> model)
    {
        this.model = Objects.requireNonNull(model);
        this.listeners = new ArrayList<>();
    }

    @Override
    public Box2D getDrawableArea()
    {
        return model.getDrawableArea();
    }

    @Override
    public void setDrawableArea(Box2D drawableArea)
    {
        model.setDrawableArea(drawableArea);
    }

    @Override
    public Iterator<Entry<V, Point2D>> iterator()
    {
        return model.iterator();
    }

    @Override
    public Point2D get(V vertex)
    {
        return model.get(vertex);
    }

    @Override
    public Point2D put(V vertex, Point2D point)
    {
        if (!model.isFixed(vertex)) {
            Point2D oldValue = model.put(vertex, point);
            notifyListeners(vertex, point);
            return oldValue;
        } else {
            return model.get(vertex);
        }
    }

    @Override
    public void setFixed(V vertex, boolean fixed)
    {
        model.setFixed(vertex, fixed);
    }

    @Override
    public boolean isFixed(V vertex)
    {
        return model.isFixed(vertex);
    }

    /**
     * Add a new listener.
     * 
     * @param listener the listener to add
     * @return the newly added listener
     */
    public BiConsumer<V, Point2D> addListener(BiConsumer<V, Point2D> listener)
    {
        listeners.add(listener);
        return listener;
    }

    /**
     * Remove a listener.
     * 
     * @param listener the listener to remove
     * @return true if the listener was removed, false otherwise
     */
    public boolean removeListener(BiConsumer<V, Point2D> listener)
    {
        return listeners.remove(listener);
    }

    /**
     * Notify all registered listeners.
     * 
     * @param vertex the vertex
     * @param point the vertex location
     */
    protected void notifyListeners(V vertex, Point2D point)
    {
        for (BiConsumer<V, Point2D> listener : listeners) {
            listener.accept(vertex, point);
        }
    }

}
