/*
 * (C) Copyright 2017-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * Algorithm class which computes a number of distance related metrics. A summary of various
 * distance metrics can be found
 * <a href="https://en.wikipedia.org/wiki/Distance_(graph_theory)">here</a>.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable, Alexandru Valeanu
 */
public class GraphMeasurer<V, E>
{

    /* Input graph */
    private final Graph<V, E> graph;
    /* All-pairs shortest path algorithm */
    private final ShortestPathAlgorithm<V, E> shortestPathAlgorithm;

    /* Vertex eccentricity map */
    private Map<V, Double> eccentricityMap = null;
    /* Diameter of the graph */
    private double diameter = 0;
    /* Radius of the graph */
    private double radius = Double.POSITIVE_INFINITY;

    /**
     * Constructs a new instance of GraphMeasurer. {@link FloydWarshallShortestPaths} is used as the
     * default shortest path algorithm.
     * 
     * @param graph input graph
     */
    public GraphMeasurer(Graph<V, E> graph)
    {
        this(graph, new FloydWarshallShortestPaths<V, E>(graph));
    }

    /**
     * Constructs a new instance of GraphMeasurer.
     * 
     * @param graph input graph
     * @param shortestPathAlgorithm shortest path algorithm used to compute shortest paths between
     *        all pairs of vertices. Recommended algorithms are:
     *        {@link org.jgrapht.alg.shortestpath.JohnsonShortestPaths} (Runtime complexity:
     *        $O(|V||E| + |V|^2 log|V|)$) or
     *        {@link org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths} (Runtime complexity:
     *        $O(|V|^3)$.
     */
    public GraphMeasurer(Graph<V, E> graph, ShortestPathAlgorithm<V, E> shortestPathAlgorithm)
    {
        this.graph = graph;
        this.shortestPathAlgorithm = shortestPathAlgorithm;
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphDiameter.html">diameter</a> of the
     * graph. The diameter of a graph is defined as $\max_{v\in V}\epsilon(v)$, where $\epsilon(v)$
     * is the eccentricity of vertex $v$. In other words, this method computes the 'longest shortest
     * path'. Two special cases exist. If the graph has no vertices, the diameter is 0. If the graph
     * is disconnected, the diameter is {@link Double#POSITIVE_INFINITY}.
     *
     * @return the diameter of the graph.
     */
    public double getDiameter()
    {
        computeEccentricityMap();
        return diameter;
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphRadius.html">radius</a> of the graph.
     * The radius of a graph is defined as $\min_{v\in V}\epsilon(v)$, where $\epsilon(v)$ is the
     * eccentricity of vertex $v$. Two special cases exist. If the graph has no vertices, the radius
     * is 0. If the graph is disconnected, the diameter is {@link Double#POSITIVE_INFINITY}.
     *
     * @return the diameter of the graph.
     */
    public double getRadius()
    {
        computeEccentricityMap();
        return radius;
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphEccentricity.html">eccentricity</a> of
     * each vertex in the graph. The eccentricity of a vertex $u$ is defined as $\max_{v}d(u,v)$,
     * where $d(u,v)$ is the shortest path between vertices $u$ and $v$. If the graph is
     * disconnected, the eccentricity of each vertex is {@link Double#POSITIVE_INFINITY}. The
     * runtime complexity of this method is $O(n^2+L)$, where $L$ is the runtime complexity of the
     * shortest path algorithm provided during construction of this class.
     *
     * @return a map containing the eccentricity of each vertex.
     */
    public Map<V, Double> getVertexEccentricityMap()
    {
        computeEccentricityMap();
        return Collections.unmodifiableMap(this.eccentricityMap);
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphCenter.html">graph center</a>. The
     * center of a graph is the set of vertices of graph eccentricity equal to the graph radius.
     *
     * @return the graph center
     */
    public Set<V> getGraphCenter()
    {
        computeEccentricityMap();
        Set<V> graphCenter = new LinkedHashSet<>();
        ToleranceDoubleComparator comp = new ToleranceDoubleComparator();
        for (Map.Entry<V, Double> entry : eccentricityMap.entrySet()) {
            if (comp.compare(entry.getValue(), radius) == 0)
                graphCenter.add(entry.getKey());
        }
        return graphCenter;
    }

    /**
     * Compute the <a href="http://mathworld.wolfram.com/GraphPeriphery.html">graph periphery</a>.
     * The periphery of a graph is the set of vertices of graph eccentricity equal to the graph
     * diameter.
     * 
     * @return the graph periphery
     */
    public Set<V> getGraphPeriphery()
    {
        computeEccentricityMap();
        Set<V> graphPeriphery = new LinkedHashSet<>();
        ToleranceDoubleComparator comp = new ToleranceDoubleComparator();
        for (Map.Entry<V, Double> entry : eccentricityMap.entrySet()) {
            if (comp.compare(entry.getValue(), diameter) == 0)
                graphPeriphery.add(entry.getKey());
        }
        return graphPeriphery;
    }

    /**
     * Compute the graph pseudo-periphery. The pseudo-periphery of a graph is the set of all
     * pseudo-peripheral vertices. A pseudo-peripheral vertex $v$ has the property that for any
     * vertex $u$, if $v$ is as far away from $u$ as possible, then $u$ is as far away from $v$ as
     * possible. Formally, a vertex $u$ is pseudo-peripheral, if for each vertex $v$ with
     * $d(u,v)=\epsilon(u)$ holds $\epsilon(u)=\epsilon(v)$, where $\epsilon(u)$ is the eccentricity
     * of vertex $u$.
     *
     * @return the graph pseudo-periphery
     */
    public Set<V> getGraphPseudoPeriphery()
    {
        computeEccentricityMap();
        Set<V> graphPseudoPeriphery = new LinkedHashSet<>();
        ToleranceDoubleComparator comp = new ToleranceDoubleComparator();

        for (Map.Entry<V, Double> entry : eccentricityMap.entrySet()) {
            V u = entry.getKey();

            for (V v : graph.vertexSet())
                if (comp.compare(shortestPathAlgorithm.getPathWeight(u, v), entry.getValue()) == 0
                    && comp.compare(entry.getValue(), eccentricityMap.get(v)) == 0)
                    graphPseudoPeriphery.add(entry.getKey());
        }

        return graphPseudoPeriphery;
    }

    /**
     * Lazy method which computes the eccentricity of each vertex
     */
    private void computeEccentricityMap()
    {
        if (eccentricityMap != null)
            return;

        // Compute the eccentricity map
        eccentricityMap = new LinkedHashMap<>();
        if (graph.getType().isUndirected()) {
            List<V> vertices = new ArrayList<>(graph.vertexSet());
            double[] eccentricityVector = new double[vertices.size()];
            for (int i = 0; i < vertices.size() - 1; i++) {
                for (int j = i + 1; j < vertices.size(); j++) {
                    double dist =
                        shortestPathAlgorithm.getPathWeight(vertices.get(i), vertices.get(j));
                    eccentricityVector[i] = Math.max(eccentricityVector[i], dist);
                    eccentricityVector[j] = Math.max(eccentricityVector[j], dist);
                }
            }
            for (int i = 0; i < vertices.size(); i++)
                eccentricityMap.put(vertices.get(i), eccentricityVector[i]);
        } else {
            for (V u : graph.vertexSet()) {
                double eccentricity = 0;
                for (V v : graph.vertexSet())
                    eccentricity =
                        Double.max(eccentricity, shortestPathAlgorithm.getPathWeight(u, v));
                eccentricityMap.put(u, eccentricity);
            }
        }

        // Compute the graph diameter and radius
        if (eccentricityMap.isEmpty()) {
            diameter = 0;
            radius = 0;
        } else {
            for (V v : graph.vertexSet()) {
                diameter = Math.max(diameter, eccentricityMap.get(v));
                radius = Math.min(radius, eccentricityMap.get(v));
            }
        }
    }
}
