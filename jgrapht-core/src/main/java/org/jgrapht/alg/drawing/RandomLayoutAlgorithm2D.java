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

import java.util.*;

/**
 * Random layout. The algorithm assigns vertex coordinates uniformly at random.
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class RandomLayoutAlgorithm2D<V, E>
    extends
    BaseLayoutAlgorithm2D<V, E>
{
    private Random rng;

    /**
     * Create a new layout algorithm
     */
    public RandomLayoutAlgorithm2D()
    {
        this(new Random());
    }

    /**
     * Create a new layout algorithm
     * 
     * @param seed seed for the random number generator
     */
    public RandomLayoutAlgorithm2D(long seed)
    {
        this(new Random(seed));
    }

    /**
     * Create a new layout algorithm
     * 
     * @param rng the random number generator
     */
    public RandomLayoutAlgorithm2D(Random rng)
    {
        this.rng = Objects.requireNonNull(rng, "Random number generator cannot be null");
    }

    @Override
    public void layout(Graph<V, E> graph, LayoutModel2D<V> model)
    {
        super.init(graph, model);

        Box2D drawableArea = model.getDrawableArea();

        double minX = drawableArea.getMinX();
        double minY = drawableArea.getMinX();
        double width = drawableArea.getWidth();
        double height = drawableArea.getHeight();

        for (V v : graph.vertexSet()) {
            double x = rng.nextDouble() * width;
            double y = rng.nextDouble() * height;
            model.put(v, Point2D.of(minX + x, minY + y));
        }
    }

}
