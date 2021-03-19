/*
 * (C) Copyright 2020-2021, by Dimitrios Michail and Contributors.
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

import java.util.OptionalDouble;
import java.util.stream.StreamSupport;

import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.model.Box2D;
import org.jgrapht.alg.drawing.model.LayoutModel2D;
import org.jgrapht.alg.drawing.model.Point2D;

/**
 * A layout algorithm which re-scales vertex positions to (center-scale,center+scale) in all
 * dimensions.
 * 
 * The algorithm first subtracts the mean on each axis separately, then all values are adjusted so
 * that the maximum magnitude becomes scale. The result is finally translated back to the old
 * center. This procedure preserves the aspect ratio.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class RescaleLayoutAlgorithm2D<V, E>
    extends
    BaseLayoutAlgorithm2D<V, E>
{
    private double scale;

    /**
     * Create a new layout algorithm
     * 
     * @param scale the scale parameter
     */
    public RescaleLayoutAlgorithm2D(double scale)
    {
        if (scale <= 0d) {
            throw new IllegalArgumentException("Scale must be positive");
        }
        this.scale = scale;
    }

    @Override
    public void layout(Graph<V, E> graph, LayoutModel2D<V> model)
    {
        Box2D oldArea = model.getDrawableArea();
        double oldCenterX = oldArea.getMinX() + oldArea.getWidth() / 2.0;
        double oldCenterY = oldArea.getMinY() + oldArea.getHeight() / 2.0;

        double maxX = 0d, maxY = 0d;

        OptionalDouble optMeanX = StreamSupport
            .stream(model.spliterator(), false).mapToDouble(e -> e.getValue().getX()).average();
        if (optMeanX.isPresent()) {
            double meanX = optMeanX.getAsDouble();
            for (V v : graph.vertexSet()) {
                Point2D p = model.get(v);
                double newX = p.getX() - meanX;
                Point2D newP = Point2D.of(newX, p.getY());
                model.put(v, newP);
                maxX = Math.max(Math.abs(newX), maxX);
            }
        }

        OptionalDouble optMeanY = StreamSupport
            .stream(model.spliterator(), false).mapToDouble(e -> e.getValue().getY()).average();
        if (optMeanY.isPresent()) {
            double meanY = optMeanY.getAsDouble();
            for (V v : graph.vertexSet()) {
                Point2D p = model.get(v);
                double newY = p.getY() - meanY;
                Point2D newP = Point2D.of(p.getX(), newY);
                model.put(v, newP);
                maxY = Math.max(Math.abs(newY), maxY);
            }
        }

        double allMax = Math.max(maxX, maxY);

        if (allMax > 0d) {
            for (V v : graph.vertexSet()) {
                Point2D p = model.get(v);
                double newX = oldCenterX + p.getX() * scale / allMax;
                Point2D newP = Point2D.of(newX, p.getY());
                model.put(v, newP);
            }
        }

        if (allMax > 0d) {
            for (V v : graph.vertexSet()) {
                Point2D p = model.get(v);
                double newY = oldCenterY + p.getY() * scale / allMax;
                Point2D newP = Point2D.of(p.getX(), newY);
                model.put(v, newP);
            }
        }

        model
            .setDrawableArea(
                Box2D.of(oldCenterX - scale, oldCenterY - scale, 2.0 * scale, 2.0 * scale));
    }

}
