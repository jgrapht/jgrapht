/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.scoring;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * Closeness centrality.
 * 
 * <p>
 * Computes the closeness centrality of each vertex of a graph. The closeness of a vertex $x$ is
 * defined as the reciprocal of the farness, that is $H(x)= 1 / \sum_{y \neq x} d(x,y)$, where
 * $d(x,y)$ is the shortest path distance from $x$ to $y$. When normalization is used, the score is
 * multiplied by $n-1$ where $n$ is the total number of vertices in the graph. For more details see
 * <a href="https://en.wikipedia.org/wiki/Closeness_centrality">wikipedia</a> and
 * <ul>
 * <li>Alex Bavelas. Communication patterns in task-oriented groups. J. Acoust. Soc. Am,
 * 22(6):725–730, 1950.</li>
 * </ul>
 *
 * <p>
 * This implementation computes by default the closeness centrality using outgoing paths and
 * normalizes the scores. This behavior can be adjusted by the constructor arguments.
 *
 * <p>
 * When the graph is disconnected, the closeness centrality score equals $0$ for all vertices. In
 * the case of weakly connected digraphs, the closeness centrality of several vertices might be 0.
 * See {@link HarmonicCentrality} for a different approach in case of disconnected graphs.
 * 
 * <p>
 * Shortest paths are computed either by using Dijkstra's algorithm or Floyd-Warshall depending on
 * whether the graph has edges with negative edge weights. Thus, the running time is either $O(n (m
 * +n \log n))$ or $O(n^3)$ respectively, where $n$ is the number of vertices and $m$ the number of
 * edges of the graph.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class ClosenessCentrality<V, E>
    implements
    VertexScoringAlgorithm<V, Double>
{
    /**
     * Underlying graph
     */
    protected final Graph<V, E> graph;
    /**
     * Whether to use incoming or outgoing paths
     */
    protected final boolean incoming;
    /**
     * Whether to normalize scores
     */
    protected final boolean normalize;
    /**
     * The actual scores
     */
    protected Map<V, Double> scores;

    /**
     * Construct a new instance. By default the centrality is normalized and computed using outgoing
     * paths.
     * 
     * @param graph the input graph
     */
    public ClosenessCentrality(Graph<V, E> graph)
    {
        this(graph, false, true);
    }

    /**
     * Construct a new instance.
     * 
     * @param graph the input graph
     * @param incoming if true incoming paths are used, otherwise outgoing paths
     * @param normalize whether to normalize by multiplying the closeness by $n-1$, where $n$ is the
     *        number of vertices of the graph
     */
    public ClosenessCentrality(Graph<V, E> graph, boolean incoming, boolean normalize)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.incoming = incoming;
        this.normalize = normalize;
        this.scores = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<V, Double> getScores()
    {
        if (scores == null) {
            compute();
        }
        return Collections.unmodifiableMap(scores);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getVertexScore(V v)
    {
        if (!graph.containsVertex(v)) {
            throw new IllegalArgumentException("Cannot return score of unknown vertex");
        }
        if (scores == null) {
            compute();
        }
        return scores.get(v);
    }

    /**
     * Get the shortest path algorithm for the paths computation.
     * 
     * @return the shortest path algorithm
     */
    protected ShortestPathAlgorithm<V, E> getShortestPathAlgorithm()
    {
        // setup graph
        Graph<V, E> g;
        if (incoming && graph.getType().isDirected()) {
            g = new EdgeReversedGraph<>(graph);
        } else {
            g = graph;
        }

        // test if we can use Dijkstra
        boolean noNegativeWeights = true;
        for (E e : g.edgeSet()) {
            double w = g.getEdgeWeight(e);
            if (w < 0.0) {
                noNegativeWeights = false;
                break;
            }
        }

        // initialize shortest path algorithm
        ShortestPathAlgorithm<V, E> alg;
        if (noNegativeWeights) {
            alg = new DijkstraShortestPath<>(g);
        } else {
            alg = new FloydWarshallShortestPaths<>(g);
        }
        return alg;
    }

    /**
     * Compute the centrality index
     */
    protected void compute()
    {
        // create result container
        this.scores = new HashMap<>();

        // initialize shortest path algorithm
        ShortestPathAlgorithm<V, E> alg = getShortestPathAlgorithm();

        // compute shortest paths
        int n = graph.vertexSet().size();
        for (V v : graph.vertexSet()) {
            double sum = 0d;

            SingleSourcePaths<V, E> paths = alg.getPaths(v);
            for (V u : graph.vertexSet()) {
                if (!u.equals(v)) {
                    sum += paths.getWeight(u);
                }
            }

            if (normalize) {
                this.scores.put(v, (n - 1) / sum);
            } else {
                this.scores.put(v, 1 / sum);
            }
        }
    }

}
