/*
 * (C) Copyright 2018-2020, by Dimitrios Michail and Contributors.
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
import java.util.function.*;

/**
 * Fruchterman and Reingold Force-Directed Placement Algorithm.
 * 
 * The algorithm belongs in the broad category of
 * <a href="https://en.wikipedia.org/wiki/Force-directed_graph_drawing">force directed graph
 * drawing</a> algorithms and is described in the paper:
 * 
 * <ul>
 * <li>Thomas M. J. Fruchterman and Edward M. Reingold. Graph drawing by force-directed placement.
 * Software: Practice and experience, 21(11):1129--1164, 1991.</li>
 * </ul>
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class FRLayoutAlgorithm2D<V, E>
    extends
    BaseLayoutAlgorithm2D<V, E>
{
    /**
     * Default number of iterations
     */
    public static final int DEFAULT_ITERATIONS = 100;

    /**
     * Default normalization factor when calculating optimal distance
     */
    public static final double DEFAULT_NORMALIZATION_FACTOR = 0.5;

    protected Random rng;
    protected double optimalDistance;
    protected double normalizationFactor;
    protected int iterations;
    protected BiFunction<LayoutModel2D<V>, Integer, TemperatureModel> temperatureModelSupplier;

    /**
     * Create a new layout algorithm
     */
    public FRLayoutAlgorithm2D()
    {
        this(DEFAULT_ITERATIONS, DEFAULT_NORMALIZATION_FACTOR, new Random());
    }

    /**
     * Create a new layout algorithm
     * 
     * @param iterations number of iterations
     */
    public FRLayoutAlgorithm2D(int iterations)
    {
        this(iterations, DEFAULT_NORMALIZATION_FACTOR, new Random());
    }

    /**
     * Create a new layout algorithm
     * 
     * @param iterations number of iterations
     * @param normalizationFactor normalization factor for the optimal distance
     */
    public FRLayoutAlgorithm2D(int iterations, double normalizationFactor)
    {
        this(iterations, normalizationFactor, new Random());
    }

    /**
     * Create a new layout algorithm
     * 
     * @param iterations number of iterations
     * @param normalizationFactor normalization factor for the optimal distance
     * @param rng the random number generator
     */
    public FRLayoutAlgorithm2D(int iterations, double normalizationFactor, Random rng)
    {
        this.rng = Objects.requireNonNull(rng);
        this.iterations = iterations;
        this.normalizationFactor = normalizationFactor;
        this.temperatureModelSupplier = (model, totalIterations) -> {
            double dimension =
                Math.min(model.getDrawableArea().getWidth(), model.getDrawableArea().getHeight());
            return new InverseLinearTemperatureModel(
                -1d * dimension / (10d * totalIterations), dimension / 10d);
        };
    }

    /**
     * Create a new layout algorithm
     * 
     * @param iterations number of iterations
     * @param normalizationFactor normalization factor for the optimal distance
     * @param temperatureModelSupplier a simulated annealing temperature model supplier
     * @param rng the random number generators
     */
    public FRLayoutAlgorithm2D(
        int iterations, double normalizationFactor,
        BiFunction<LayoutModel2D<V>, Integer, TemperatureModel> temperatureModelSupplier,
        Random rng)
    {
        this.rng = Objects.requireNonNull(rng);
        this.iterations = iterations;
        this.normalizationFactor = normalizationFactor;
        this.temperatureModelSupplier = Objects.requireNonNull(temperatureModelSupplier);
    }

    @Override
    public void layout(Graph<V, E> graph, LayoutModel2D<V> model)
    {
        // read area
        Box2D drawableArea = model.getDrawableArea();
        double minX = drawableArea.getMinX();
        double minY = drawableArea.getMinY();

        if (getInitializer() != null) {
            // respect user initializer
            init(graph, model);

            // make sure all vertices have coordinates
            for (V v : graph.vertexSet()) {
                Point2D vPos = model.get(v);
                if (vPos == null) {
                    model.put(v, Point2D.of(minX, minY));
                }
            }
        } else {
            // assign random initial positions
            MapLayoutModel2D<V> randomModel = new MapLayoutModel2D<>(drawableArea);
            new RandomLayoutAlgorithm2D<V, E>(rng).layout(graph, randomModel);
            for (V v : graph.vertexSet()) {
                model.put(v, randomModel.get(v));
            }
        }

        // calculate optimal distance between vertices
        double width = drawableArea.getWidth();
        double height = drawableArea.getHeight();
        double area = width * height;
        int n = graph.vertexSet().size();
        if (n == 0) {
            return;
        }
        optimalDistance = normalizationFactor * Math.sqrt(area / n);

        // create temperature model
        TemperatureModel temperatureModel = temperatureModelSupplier.apply(model, iterations);

        // start main iterations
        for (int i = 0; i < iterations; i++) {

            // repulsive forces
            Map<V, Point2D> repulsiveDisp = calculateRepulsiveForces(graph, model);

            // attractive forces
            Map<V, Point2D> attractiveDisp = calculateAttractiveForces(graph, model);

            // calculate current temperature
            double temp = temperatureModel.temperature(i, iterations);

            // limit maximum displacement by the temperature
            // and prevent from being displaced outside frame
            for (V v : graph.vertexSet()) {
                // limit by temperature
                Point2D vDisp = Points.add(repulsiveDisp.get(v), attractiveDisp.get(v));
                double vDispLen = Points.length(vDisp);
                Point2D vPos = Points
                    .add(
                        model.get(v),
                        Points.scalarMultiply(vDisp, Math.min(vDispLen, temp) / vDispLen));

                // limit by frame
                vPos = Point2D
                    .of(
                        Math.min(minX + width, Math.max(minX, vPos.getX())),
                        Math.min(minY + height, Math.max(minY, vPos.getY())));

                // store result
                model.put(v, vPos);
            }
        }
    }

    /**
     * Calculate the attractive force.
     * 
     * @param distance the distance
     * @return the force
     */
    protected double attractiveForce(double distance)
    {
        return distance * distance / optimalDistance;
    }

    /**
     * Calculate the repulsive force.
     * 
     * @param distance the distance
     * @return the force
     */
    protected double repulsiveForce(double distance)
    {
        return optimalDistance * optimalDistance / distance;
    }

    /**
     * Calculate the repulsive forces between vertices
     * 
     * @param graph the graph
     * @param model the model
     * @return the displacement per vertex due to the repulsive forces
     */
    protected Map<V, Point2D> calculateRepulsiveForces(Graph<V, E> graph, LayoutModel2D<V> model)
    {
        Point2D origin =
            Point2D.of(model.getDrawableArea().getMinX(), model.getDrawableArea().getMinY());
        Map<V, Point2D> disp = new HashMap<>();
        for (V v : graph.vertexSet()) {
            Point2D vPos = Points.subtract(model.get(v), origin);
            Point2D vDisp = Point2D.of(0d, 0d);

            for (V u : graph.vertexSet()) {
                if (v == u) {
                    continue;
                }
                Point2D uPos = Points.subtract(model.get(u), origin);
                Point2D delta = Points.subtract(vPos, uPos);
                double deltaLen = Points.length(delta);
                Point2D dispContribution =
                    Points.scalarMultiply(delta, repulsiveForce(deltaLen) / deltaLen);
                vDisp = Points.add(vDisp, dispContribution);
            }

            disp.put(v, vDisp);
        }
        return disp;
    }

    /**
     * Calculate the repulsive forces between vertices connected with edges.
     * 
     * @param graph the graph
     * @param model the model
     * @return the displacement per vertex due to the attractive forces
     */
    protected Map<V, Point2D> calculateAttractiveForces(Graph<V, E> graph, LayoutModel2D<V> model)
    {
        Point2D origin =
            Point2D.of(model.getDrawableArea().getMinX(), model.getDrawableArea().getMinY());
        Map<V, Point2D> disp = new HashMap<>();
        for (E e : graph.edgeSet()) {
            V v = graph.getEdgeSource(e);
            V u = graph.getEdgeTarget(e);
            Point2D vPos = Points.subtract(model.get(v), origin);
            Point2D uPos = Points.subtract(model.get(u), origin);

            Point2D delta = Points.subtract(vPos, uPos);
            double deltaLen = Points.length(delta);
            Point2D dispContribution =
                Points.scalarMultiply(delta, attractiveForce(deltaLen) / deltaLen);
            disp
                .put(
                    v,
                    Points
                        .add(
                            disp.getOrDefault(v, Point2D.of(0d, 0d)),
                            Points.negate(dispContribution)));
            disp.put(u, Points.add(disp.getOrDefault(u, Point2D.of(0d, 0d)), dispContribution));
        }
        return disp;
    }

    /**
     * A general interface for a temperature model.
     * 
     * <p>
     * The temperature should start from a high enough value and gradually become zero.
     */
    public interface TemperatureModel
    {

        /**
         * Return the temperature for the new iteration
         * 
         * @param iteration the next iteration
         * @param maxIterations total number of iterations
         * @return the temperature for the next iteration
         */
        double temperature(int iteration, int maxIterations);

    }

    /**
     * An inverse linear temperature model.
     */
    protected class InverseLinearTemperatureModel
        implements
        TemperatureModel
    {
        private double a;
        private double b;

        /**
         * Create a new inverse linear temperature model.
         * 
         * @param a a
         * @param b b
         */
        public InverseLinearTemperatureModel(double a, double b)
        {
            this.a = a;
            this.b = b;
        }

        @Override
        public double temperature(int iteration, int maxIterations)
        {
            if (iteration >= maxIterations - 1) {
                return 0.0;
            }
            return a * iteration + b;
        }

    }
}
