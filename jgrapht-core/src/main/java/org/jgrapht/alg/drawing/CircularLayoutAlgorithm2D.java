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
import org.jgrapht.alg.util.*;

import java.util.*;
import java.util.stream.*;

/**
 * Circular layout.
 * 
 * <p>
 * The algorithm places the graph vertices on a circle evenly spaced. The vertices are iterated
 * based on the iteration order of the vertex set of the graph. The order can be adjusted by
 * providing an external comparator.
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class CircularLayoutAlgorithm2D<V, E>
    extends
    BaseLayoutAlgorithm2D<V, E>
{
    protected double radius;
    protected Comparator<Double> comparator;
    protected Comparator<V> vertexComparator;

    /**
     * Create a new layout algorithm
     */
    public CircularLayoutAlgorithm2D()
    {
        this(0.5d);
    }

    /**
     * Create a new layout algorithm
     * 
     * @param radius the circle radius
     */
    public CircularLayoutAlgorithm2D(double radius)
    {
        this(radius, null);
    }

    /**
     * Create a new layout algorithm. The algorithm will iterate over the vertices of the graph
     * using the provided ordering.
     * 
     * @param radius the circle radius
     * @param vertexComparator the vertex comparator. Can be null.
     */
    public CircularLayoutAlgorithm2D(double radius, Comparator<V> vertexComparator)
    {
        this.comparator = new ToleranceDoubleComparator();
        this.radius = radius;
        if (comparator.compare(radius, 0d) <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        this.vertexComparator = vertexComparator;
    }

    @Override
    public void layout(Graph<V, E> graph, LayoutModel2D<V> model)
    {
        super.init(graph, model);

        Box2D drawableArea = model.getDrawableArea();

        double width = drawableArea.getWidth();
        if (comparator.compare(2d * radius, width) > 0) {
            throw new IllegalArgumentException("Circle does not fit into drawable area width");
        }
        double height = drawableArea.getHeight();
        if (comparator.compare(2d * radius, height) > 0) {
            throw new IllegalArgumentException("Circle does not fit into drawable area height");
        }
        double minX = drawableArea.getMinX();
        double minY = drawableArea.getMinY();

        int n = graph.vertexSet().size();
        double angleStep = 2 * Math.PI / n;

        Stream<V> vertexStream;
        if (vertexComparator != null) {
            vertexStream = graph.vertexSet().stream().sorted(vertexComparator);
        } else {
            vertexStream = graph.vertexSet().stream();
        }

        Iterator<V> it = vertexStream.iterator();
        int i = 0;
        while (it.hasNext()) {
            double x = radius * Math.cos(angleStep * i) + width / 2;
            double y = radius * Math.sin(angleStep * i) + height / 2;
            V v = it.next();
            model.put(v, Point2D.of(minX + x, minY + y));
            i++;
        }
    }

}
