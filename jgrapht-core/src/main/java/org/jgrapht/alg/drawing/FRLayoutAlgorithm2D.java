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
package org.jgrapht.alg.drawing;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;

import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.model.DoublePoint2D;
import org.jgrapht.alg.drawing.model.LayoutModel;
import org.jgrapht.alg.drawing.model.MapLayoutModel;
import org.jgrapht.alg.drawing.model.Point2D;
import org.jgrapht.alg.drawing.model.Points;
import org.jgrapht.alg.drawing.model.Rectangle2D;

/**
 * Fruchterman and Reingold Force-Directed Placement Algorithm.
 * 
 * @author Dimitrios Michail
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 */
public class FRLayoutAlgorithm2D<V, E>
    implements
    LayoutAlgorithm2D<V, E, Double>
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
    protected BiFunction<LayoutModel<V, Double, Point2D<Double>, Rectangle2D<Double>>, Integer,
        TemperatureModel> temperatureModelSupplier;

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
        BiFunction<LayoutModel<V, Double, Point2D<Double>, Rectangle2D<Double>>, Integer,
            TemperatureModel> temperatureModelSupplier,
        Random rng)
    {
        this.rng = Objects.requireNonNull(rng);
        this.iterations = iterations;
        this.normalizationFactor = normalizationFactor;
        this.temperatureModelSupplier = Objects.requireNonNull(temperatureModelSupplier);
    }

    @Override
    public void layout(
        Graph<V, E> graph, LayoutModel<V, Double, Point2D<Double>, Rectangle2D<Double>> model)
    {
        // read area
        Rectangle2D<Double> drawableArea = model.getDrawableArea();
        double minX = drawableArea.getMinX();
        double minY = drawableArea.getMinY();

        if (model.getInitializer() != null) {
            // respect user initializer
            model.init(graph);

            // make sure all vertices have coordinates
            for (V v : graph.vertexSet()) {
                Point2D<Double> vPos = model.get(v);
                if (vPos == null) {
                    model.put(v, DoublePoint2D.of(minX, minY));
                }
            }
        } else {
            // assign random initial positions
            MapLayoutModel<V, Double, Point2D<Double>, Rectangle2D<Double>> randomModel =
                new MapLayoutModel<>(drawableArea);
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
            Map<V, Point2D<Double>> repulsiveDisp = calculateRepulsiveForces(graph, model);

            // attractive forces
            Map<V, Point2D<Double>> attractiveDisp = calculateAttractiveForces(graph, model);

            // calculate current temperature
            double temp = temperatureModel.temperature(i, iterations);

            // limit maximum displacement by the temperature
            // and prevent from being displaced outside frame
            for (V v : graph.vertexSet()) {
                // limit by temperature
                Point2D<Double> vDisp = Points.add(repulsiveDisp.get(v), attractiveDisp.get(v));
                double vDispLen = Points.length(vDisp);
                Point2D<Double> vPos = Points
                    .add(
                        model.get(v),
                        Points.scalarMultiply(vDisp, Math.min(vDispLen, temp) / vDispLen));

                // limit by frame
                vPos = DoublePoint2D
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
    protected Map<V, Point2D<Double>> calculateRepulsiveForces(
        Graph<V, E> graph, LayoutModel<V, Double, Point2D<Double>, Rectangle2D<Double>> model)
    {
        Point2D<Double> origin =
            DoublePoint2D.of(model.getDrawableArea().getMinX(), model.getDrawableArea().getMinY());
        Map<V, Point2D<Double>> disp = new HashMap<>();
        for (V v : graph.vertexSet()) {
            Point2D<Double> vPos = Points.sub(model.get(v), origin);
            Point2D<Double> vDisp = DoublePoint2D.of(0d, 0d);

            for (V u : graph.vertexSet()) {
                if (v == u) {
                    continue;
                }
                Point2D<Double> uPos = Points.sub(model.get(u), origin);
                Point2D<Double> delta = Points.sub(vPos, uPos);
                double deltaLen = Points.length(delta);
                Point2D<Double> dispContribution =
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
    protected Map<V, Point2D<Double>> calculateAttractiveForces(
        Graph<V, E> graph, LayoutModel<V, Double, Point2D<Double>, Rectangle2D<Double>> model)
    {
        Point2D<Double> origin =
            DoublePoint2D.of(model.getDrawableArea().getMinX(), model.getDrawableArea().getMinY());
        Map<V, Point2D<Double>> disp = new HashMap<>();
        for (E e : graph.edgeSet()) {
            V v = graph.getEdgeSource(e);
            V u = graph.getEdgeTarget(e);
            Point2D<Double> vPos = Points.sub(model.get(v), origin);
            Point2D<Double> uPos = Points.sub(model.get(u), origin);

            Point2D<Double> delta = Points.sub(vPos, uPos);
            double deltaLen = Points.length(delta);
            Point2D<Double> dispContribution =
                Points.scalarMultiply(delta, attractiveForce(deltaLen) / deltaLen);
            disp.put(v, Points.add(disp.getOrDefault(v, DoublePoint2D.of(0d, 0d)), Points.minus(dispContribution)));
            disp.put(u, Points.add(disp.getOrDefault(u, DoublePoint2D.of(0d, 0d)), dispContribution));
        }
        return disp;
    }

    /**
     * An inverse linear temperature model.
     * 
     * @author Dimitrios Michail
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
