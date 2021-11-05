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
package org.jgrapht.alg.drawing;

import org.jgrapht.*;
import org.jgrapht.alg.drawing.model.*;

import java.util.function.*;

/**
 * A base class for a 2d layout algorithm.
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 */
abstract class BaseLayoutAlgorithm2D<V, E>
    implements
    LayoutAlgorithm2D<V, E>
{
    /**
     * A model initializer
     */
    protected Function<V, Point2D> initializer;

    /**
     * Create a new layout algorithm
     */
    public BaseLayoutAlgorithm2D()
    {
        this(null);
    }

    /**
     * Create a new layout algorithm with an initializer.
     * 
     * @param initializer the initializer
     */
    public BaseLayoutAlgorithm2D(Function<V, Point2D> initializer)
    {
        this.initializer = initializer;
    }

    /**
     * Get the initializer
     * 
     * @return the initializer
     */
    public Function<V, Point2D> getInitializer()
    {
        return initializer;
    }

    /**
     * Set the initializer
     * 
     * @param initializer the initializer
     */
    public void setInitializer(Function<V, Point2D> initializer)
    {
        this.initializer = initializer;
    }

    /**
     * Initialize a model using the initializer.
     * 
     * @param graph the graph
     * @param model the model
     */
    protected void init(Graph<V, E> graph, LayoutModel2D<V> model)
    {
        Function<V, Point2D> initializer = getInitializer();
        if (initializer != null) {
            for (V v : graph.vertexSet()) {
                Point2D value = initializer.apply(v);
                if (value != null) {
                    model.put(v, value);
                }
            }
        }
    }

}
