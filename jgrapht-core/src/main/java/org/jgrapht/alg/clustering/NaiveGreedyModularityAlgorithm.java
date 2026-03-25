/*
 * (C) Copyright 2021-2021, by Antonia Tsiftsi and Contributors.
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
package org.jgrapht.alg.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static java.util.Objects.isNull;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;

/**
 * A naive implementation of greedy modularity maximization for community detection.
 * 
 * <p>
 * The algorithm partitions the vertices of an undirected graph into communities by greedily
 * maximizing the <a href="https://en.wikipedia.org/wiki/Modularity_(networks)">modularity</a>
 * of possible communities. Greedy modularity maximization begins with each node in its own
 * community and repeatedly joins the pair of communities that lead to the largest modularity
 * until no further increase in modularity is possible (a maximum).
 * </p> 
 *
 * <p>
 * This implementation is simple but computationally expensive, with a worst-case complexity
 * of <b>O(n^4)</b>. It is intended as an easy-to-understand reference implementation rather
 * than a performance-optimized solution.
 * </p>
 * 
 * @author Antonia Tsiftsi
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class NaiveGreedyModularityAlgorithm<V, E>
    implements
    ClusteringAlgorithm<V>
{
    private final Graph<V, E> graph;

    /**
     * Create a new Naive Greedy clustering algorithm.
     * 
     * @param graph the graph
     */
    public NaiveGreedyModularityAlgorithm(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireUndirected(graph);
    }

    @Override
    public ClusteringAlgorithm.Clustering<V> getClustering()
    {
        int i, j;

        // create one community for each node
        List<Set<V>> communities = new ArrayList<>();
        for (V v : graph.iterables().vertices()) {
            Set<V> set = Set.of(v);
            communities.add(set);
        }

        Double oldModularity = null;
        UndirectedModularityMeasurer<V, E> measurer = new UndirectedModularityMeasurer<>(graph);
        double curModularity = measurer.modularity(communities);

        // greedily merge communities until no improvement is possible
        while (isNull(oldModularity) || oldModularity < curModularity) {
            oldModularity = curModularity;
            List<Set<V>> bestCommunities = null;

            for (i = 0; i < communities.size(); i++) {
                for (j = 0; j < communities.size(); j++) {
                    if (j <= i) {
                        continue;
                    }

                    // initialize trialCommunities
                    List<Set<V>> trialCommunities = new ArrayList<>();
                    for (int k = 0; k < communities.size(); k++) {
                        if (k != j && k != i) {
                            trialCommunities.add(communities.get(k));
                        }
                    }

                    // create trial partition
                    Set<V> merge = new HashSet<>();
                    merge.addAll(communities.get(i));
                    merge.addAll(communities.get(j));
                    trialCommunities.add(merge);

                    // check the modularity of the trial partition
                    double trialModularity = measurer.modularity(trialCommunities);
                    if (trialModularity >= curModularity) {
                        curModularity = trialModularity;
                        bestCommunities = trialCommunities;
                    }
                }
            }
            if (bestCommunities == null) {
                break;
            }
            communities = bestCommunities;
        }

        ClusteringAlgorithm.ClusteringImpl<V> clustering =
            new ClusteringAlgorithm.ClusteringImpl<>(communities);
        return clustering;
    }
}
