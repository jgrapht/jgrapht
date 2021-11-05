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
import org.jgrapht.alg.drawing.FRQuadTree.*;
import org.jgrapht.alg.drawing.model.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * Fruchterman and Reingold Force-Directed Placement Algorithm using the
 * <a href="https://en.wikipedia.org/wiki/Barnes%E2%80%93Hut_simulation">Barnes-Hut</a> indexing
 * technique with a <a href="https://en.wikipedia.org/wiki/Quadtree">QuadTree</a>.
 * 
 * The Barnes-Hut indexing technique is described in the following paper:
 * <ul>
 * <li>J. Barnes and P. Hut. A hierarchical O(N log N) force-calculation algorithm. Nature.
 * 324(4):446--449, 1986.</li>
 * </ul>
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class IndexedFRLayoutAlgorithm2D<V, E>
    extends
    FRLayoutAlgorithm2D<V, E>
{
    /**
     * Default $\theta$ value for approximation using the Barnes-Hut technique
     */
    public static final double DEFAULT_THETA_FACTOR = 0.5;

    protected double theta;
    protected long savedComparisons;

    /**
     * Create a new layout algorithm
     */
    public IndexedFRLayoutAlgorithm2D()
    {
        this(DEFAULT_ITERATIONS, DEFAULT_THETA_FACTOR, DEFAULT_NORMALIZATION_FACTOR);
    }

    /**
     * Create a new layout algorithm
     * 
     * @param iterations number of iterations
     * @param theta parameter for approximation using the Barnes-Hut technique
     */
    public IndexedFRLayoutAlgorithm2D(int iterations, double theta)
    {
        this(iterations, theta, DEFAULT_NORMALIZATION_FACTOR);
    }

    /**
     * Create a new layout algorithm
     * 
     * @param iterations number of iterations
     * @param theta parameter for approximation using the Barnes-Hut technique
     * @param normalizationFactor normalization factor for the optimal distance
     */
    public IndexedFRLayoutAlgorithm2D(int iterations, double theta, double normalizationFactor)
    {
        this(iterations, theta, normalizationFactor, new Random());
    }

    /**
     * Create a new layout algorithm
     * 
     * @param iterations number of iterations
     * @param theta theta parameter for the Barnes-Hut approximation
     * @param normalizationFactor normalization factor for the optimal distance
     * @param rng the random number generator
     */
    public IndexedFRLayoutAlgorithm2D(
        int iterations, double theta, double normalizationFactor, Random rng)
    {
        this(
            iterations, theta, normalizationFactor, rng, ToleranceDoubleComparator.DEFAULT_EPSILON);
    }

    /**
     * Create a new layout algorithm
     * 
     * @param iterations number of iterations
     * @param theta theta parameter for the Barnes-Hut approximation
     * @param normalizationFactor normalization factor for the optimal distance
     * @param rng the random number generator
     * @param tolerance tolerance used when comparing floating point values
     */
    public IndexedFRLayoutAlgorithm2D(
        int iterations, double theta, double normalizationFactor, Random rng, double tolerance)
    {
        super(iterations, normalizationFactor, rng, tolerance);
        this.theta = theta;
        if (theta < 0d || theta > 1d) {
            throw new IllegalArgumentException("Illegal theta value");
        }
        this.savedComparisons = 0;
    }

    @Override
    public void layout(Graph<V, E> graph, LayoutModel2D<V> model)
    {
        this.savedComparisons = 0;
        super.layout(graph, model);
    }

    @Override
    protected Map<V, Point2D> calculateRepulsiveForces(Graph<V, E> graph, LayoutModel2D<V> model)
    {
        // index all points
        FRQuadTree quadTree = new FRQuadTree(model.getDrawableArea());
        for (V v : graph.vertexSet()) {
            quadTree.insert(model.get(v));
        }

        Point2D origin =
            Point2D.of(model.getDrawableArea().getMinX(), model.getDrawableArea().getMinY());

        // compute displacement with index
        Map<V, Point2D> disp = new HashMap<>();
        for (V v : graph.vertexSet()) {
            Point2D vPos = Points.subtract(model.get(v), origin);
            Point2D vDisp = Point2D.of(0d, 0d);

            Deque<Node> queue = new ArrayDeque<>();
            queue.add(quadTree.getRoot());

            while (!queue.isEmpty()) {
                Node node = queue.removeFirst();
                Box2D box = node.getBox();
                double boxWidth = box.getWidth();

                Point2D uPos = null;
                if (node.isLeaf()) {
                    if (!node.hasPoints()) {
                        continue;
                    }
                    uPos = Points.subtract(node.getPoints().iterator().next(), origin);
                } else {
                    double distanceToCentroid =
                        Points.length(Points.subtract(vPos, node.getCentroid()));
                    if (comparator.compare(distanceToCentroid, 0d) == 0) {
                        savedComparisons += node.getNumberOfPoints() - 1;
                        continue;
                    } else if (comparator.compare(boxWidth / distanceToCentroid, theta) < 0) {
                        uPos = Points.subtract(node.getCentroid(), origin);
                        savedComparisons += node.getNumberOfPoints() - 1;
                    } else {
                        for (Node child : node.getChildren()) {
                            queue.add(child);
                        }
                        continue;
                    }
                }

                if (comparator.compare(vPos.getX(), uPos.getX()) != 0
                    || comparator.compare(vPos.getY(), uPos.getY()) != 0)
                {
                    Point2D delta = Points.subtract(vPos, uPos);
                    double deltaLen = Points.length(delta);
                    Point2D dispContribution =
                        Points.scalarMultiply(delta, repulsiveForce(deltaLen) / deltaLen);
                    vDisp = Points.add(vDisp, dispContribution);
                }
            }

            disp.put(v, vDisp);
        }
        return disp;
    }

    /**
     * Get the total number of saved comparisons due to the Barnes-Hut technique.
     * 
     * @return the total number of saved comparisons
     */
    public long getSavedComparisons()
    {
        return savedComparisons;
    }

}
