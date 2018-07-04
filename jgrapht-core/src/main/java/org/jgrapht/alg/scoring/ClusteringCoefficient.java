/*
 * (C) Copyright 2017-2017, by Assaf Mizrachi and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.scoring;

import java.util.*;
import java.util.stream.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

/**
 * Clusteing coefficient.
 * 
 * <p>
 * Computes the clustering coefficient of each vertex of a graph. The clustering coefficient of a
 * node $v$ in a directed graph is given by the expression: $C_i = \frac{|\{e_{jk}: v_j,v_k \in N_i, e_{jk} \in E\}|}{k_i(k_i-1)}$
 * where $N_i$ is the vertex neighbourhood: $N_i = \{v_j : e_{ij} \in E \or e_{ji} \in E\}$. For undirected graphs,
 * $e_{ij}$ and $e_{ji}$ are considered identical hence the clustering coefficient is given by:
 * $C_i = \frac{2|\{e_{jk}: v_j,v_k \in N_i, e_{jk} \in E\}|}{k_i(k_i-1)}$. 

 * For more details see
 * <a href="https://en.wikipedia.org/wiki/Clustering_coefficient">wikipedia</a>.
 * 
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Assaf Mizrachi
 * @since June 2018
 */
public class ClusteringCoefficient<V, E>
    implements VertexScoringAlgorithm<V, Double>
{

    /**
     * Underlying graph
     */
    private final Graph<V, E> graph;
    /**
     * The actual scores
     */
    private Map<V, Double> scores;

    /**
     * Construct a new instance.
     * 
     * @param graph the input graph
     */
    public ClusteringCoefficient(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireDirectedOrUndirected(graph);

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
     * Compute the centrality index
     */
    private void compute()
    {
        // initialize result container
        this.scores = new HashMap<>();

        // compute for each source
        this.graph.vertexSet().forEach(this::compute);
    }

    private void compute(V v)
    {
        // we count each neighbor only once
        Set<V> neighboursSet = Graphs.successorListOf(graph, v).stream().collect(Collectors.toSet());

        double actualEdgesCount = 0.0;
        List<V> neighbourhood = new ArrayList<>(neighboursSet);
        for (V u : neighbourhood) {
            for (V w : neighbourhood) {       
                //self edges are not counted.
                if (v == w) {
                    continue;
                }
                //multiple edges are not counted
                if (graph.containsEdge(u, w)) {
                    actualEdgesCount++;
                }
                
            }
        }
        
        long neighbourhoodSize = neighbourhood.size();
        double potentialEdgesCount = neighbourhoodSize * (neighbourhoodSize - 1);
        double clusteringCoefficient = potentialEdgesCount == 0 ? 0 : actualEdgesCount / potentialEdgesCount;

        this.scores.put(v, clusteringCoefficient);
    }

}
