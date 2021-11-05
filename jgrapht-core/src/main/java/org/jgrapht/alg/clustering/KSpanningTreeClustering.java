/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.*;
import org.jgrapht.alg.spanning.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * The k spanning tree clustering algorithm.
 * 
 * <p>
 * The algorithm finds a minimum spanning tree $T$ using Prim's algorithm, then executes Kruskal's
 * algorithm only on the edges of $T$ until $k$ trees are formed. The resulting trees are the final
 * clusters. The total running time is $O(m + n \log n)$.
 * 
 * <p>
 * The algorithm is strongly related to single linkage cluster analysis, also known as single-link
 * clustering. For more information see: J. C. Gower and G. J. S. Ross. Minimum Spanning Trees and
 * Single Linkage Cluster Analysis. Journal of the Royal Statistical Society. Series C (Applied
 * Statistics), 18(1):54--64, 1969.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class KSpanningTreeClustering<V, E>
    implements
    ClusteringAlgorithm<V>
{
    private Graph<V, E> graph;
    private int k;

    /**
     * Create a new clustering algorithm.
     * 
     * @param graph the graph (needs to be undirected)
     * @param k the desired number of clusters
     */
    public KSpanningTreeClustering(Graph<V, E> graph, int k)
    {
        this.graph = GraphTests.requireUndirected(graph);
        if (k < 1 || k > graph.vertexSet().size()) {
            throw new IllegalArgumentException("Illegal number of clusters");
        }
        this.k = k;
    }

    @Override
    public Clustering<V> getClustering()
    {
        /*
         * Compute an MST
         */
        SpanningTree<E> mst = new PrimMinimumSpanningTree<>(graph).getSpanningTree();

        /*
         * Run Kruskal only on MST edges until we get k clusters
         */
        UnionFind<V> forest = new UnionFind<>(graph.vertexSet());
        ArrayList<E> allEdges = new ArrayList<>(mst.getEdges());
        allEdges.sort(Comparator.comparingDouble(graph::getEdgeWeight));

        for (E edge : allEdges) {
            if (forest.numberOfSets() == k) {
                break;
            }
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (forest.find(source).equals(forest.find(target))) {
                continue;
            }

            forest.union(source, target);
        }

        /*
         * Transform and return result
         */
        Map<V, Set<V>> clusterMap = new LinkedHashMap<>();
        for (V v : graph.vertexSet()) {
            V rv = forest.find(v);
            Set<V> cluster = clusterMap.get(rv);
            if (cluster == null) {
                cluster = new LinkedHashSet<>();
                clusterMap.put(rv, cluster);
            }
            cluster.add(v);
        }
        return new ClusteringImpl<>(new ArrayList<>(clusterMap.values()));
    }

}
