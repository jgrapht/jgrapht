/*
 * (C) Copyright 2017-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * Implementation of the well-known Hopcroft Karp algorithm to compute a matching of maximum
 * cardinality in a bipartite graph. The algorithm runs in $O(|E| \cdot \sqrt{|V|})$ time. This
 * implementation accepts undirected graphs which may contain self-loops and multiple edges. To
 * compute a maximum cardinality matching in general (non-bipartite) graphs, use
 * {@link EdmondsMaximumCardinalityMatching} instead.
 *
 * <p>
 * The Hopcroft Karp matching algorithm computes augmenting paths of increasing length, until no
 * augmenting path exists in the graph. At each iteration, the algorithm runs a Breadth First Search
 * from the exposed (free) vertices, until an augmenting path is found. Next, a Depth First Search
 * is performed to find all (vertex disjoint) augmenting paths of the same length. The matching is
 * augmented along all discovered augmenting paths simultaneously.
 *
 * <p>
 * The original algorithm is described in: Hopcroft, John E.; Karp, Richard M. (1973), "An n5/2
 * algorithm for maximum matchings in bipartite graphs", SIAM Journal on Computing 2 (4): 225–231,
 * doi:10.1137/0202019 A coarse overview of the algorithm is given in: <a href=
 * "http://en.wikipedia.org/wiki/Hopcroft-Karp_algorithm">http://en.wikipedia.org/wiki/Hopcroft-Karp_algorithm</a>
 *
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class HopcroftKarpMaximumCardinalityBipartiteMatching<V, E>
    implements
    MatchingAlgorithm<V, E>
{

    private final Graph<V, E> graph;
    private final Set<V> partition1;
    private final Set<V> partition2;

    /* Ordered list of vertices */
    private List<V> vertices;
    /* Mapping of a vertex to their unique position in the ordered list of vertices */
    private Map<V, Integer> vertexIndexMap;

    /* Number of matched vertices i partition 1. */
    private int matchedVertices;

    /* Dummy vertex. All vertices are initially matched against this dummy vertex */
    private final int DUMMY = 0;
    /* Infinity */
    private final int INF = Integer.MAX_VALUE;

    /* Array keeping track of the matching. */
    private int[] matching;
    /* Distance array. Used to compute shoretest augmenting paths */
    private int[] dist;

    /* queue used for breadth first search */
    private FixedSizeIntegerQueue queue;

    /**
     * Constructs a new instance of the Hopcroft Karp bipartite matching algorithm. The input graph
     * must be bipartite. For efficiency reasons, this class does not check whether the input graph
     * is bipartite. Invoking this class on a non-bipartite graph results in undefined behavior. To
     * test whether a graph is bipartite, use {@link GraphTests#isBipartite(Graph)}.
     * 
     * @param graph bipartite graph
     * @param partition1 the first partition of vertices in the bipartite graph
     * @param partition2 the second partition of vertices in the bipartite graph
     */
    public HopcroftKarpMaximumCardinalityBipartiteMatching(
        Graph<V, E> graph, Set<V> partition1, Set<V> partition2)
    {
        this.graph = GraphTests.requireUndirected(graph);

        // Ensure that partition1 is smaller or equal in size compared to partition 2
        if (partition1.size() <= partition2.size()) {
            this.partition1 = partition1;
            this.partition2 = partition2;
        } else { // else, swap
            this.partition1 = partition2;
            this.partition2 = partition1;
        }
    }

    /**
     * Initialize data structures
     */
    private void init()
    {
        vertices = new ArrayList<>();
        vertices.add(null);
        vertices.addAll(partition1);
        vertices.addAll(partition2);
        vertexIndexMap = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++)
            vertexIndexMap.put(vertices.get(i), i);

        matching = new int[vertices.size() + 1];
        dist = new int[partition1.size() + 1];
        queue = new FixedSizeIntegerQueue(vertices.size());
    }

    /**
     * Greedily compute an initial feasible matching
     */
    private void warmStart()
    {
        for (V uOrig : partition1) {
            int u = vertexIndexMap.get(uOrig);

            for (V vOrig : Graphs.neighborListOf(graph, uOrig)) {
                int v = vertexIndexMap.get(vOrig);
                if (matching[v] == DUMMY) {
                    matching[v] = u;
                    matching[u] = v;
                    matchedVertices++;
                    break;
                }
            }
        }
    }

    /**
     * BFS function which finds the shortest augmenting path. The length of the shortest augmenting
     * path is stored in dist[DUMMY].
     * 
     * @return true if an augmenting path was found, false otherwise
     */
    private boolean bfs()
    {
        queue.clear();

        for (int u = 1; u <= partition1.size(); u++)
            if (matching[u] == DUMMY) { // Add all unmatched vertices to the queue and set their
                                        // distance to 0
                dist[u] = 0;
                queue.enqueue(u);
            } else // Set distance of all matched vertices to INF
                dist[u] = INF;
        dist[DUMMY] = INF;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            if (dist[u] < dist[DUMMY])
                for (V vOrig : Graphs.neighborListOf(graph, vertices.get(u))) {
                    int v = vertexIndexMap.get(vOrig);
                    if (dist[matching[v]] == INF) {
                        dist[matching[v]] = dist[u] + 1;
                        queue.enqueue(matching[v]);
                    }
                }
        }
        return dist[DUMMY] != INF; // Return true if an augmenting path is found
    }

    /**
     * Find all vertex disjoint augmenting paths of length dist[DUMMY]. To find paths of dist[DUMMY]
     * length, we simply follow nodes that are 1 distance increments away from each other.
     * 
     * @param u vertex from which the DFS is started
     * @return true if an augmenting path from vertex u was found, false otherwise
     */
    private boolean dfs(int u)
    {
        if (u != DUMMY) {
            for (V vOrig : Graphs.neighborListOf(graph, vertices.get(u))) {
                int v = vertexIndexMap.get(vOrig);
                if (dist[matching[v]] == dist[u] + 1)
                    if (dfs(matching[v])) {
                        matching[v] = u;
                        matching[u] = v;
                        return true;
                    }
            }
            // No augmenting path has been found. Set distance of u to INF to ensure that u isn't
            // visited again.
            dist[u] = INF;
            return false;
        }
        return true;
    }

    @Override
    public Matching<V, E> getMatching()
    {
        this.init();
        this.warmStart();

        while (matchedVertices < partition1.size() && bfs()) {
            // Greedily search for vertex disjoint augmenting paths
            for (int v = 1; v <= partition1.size() && matchedVertices < partition1.size(); v++)
                if (matching[v] == DUMMY) // v is unmatched
                    if (dfs(v))
                        matchedVertices++;
        }
        assert matchedVertices <= partition1.size();

        Set<E> edges = new HashSet<>();
        for (int i = 0; i < vertices.size(); i++) {
            if (matching[i] != DUMMY) {
                edges.add(graph.getEdge(vertices.get(i), vertices.get(matching[i])));
            }
        }
        return new MatchingImpl<>(graph, edges, edges.size());
    }
}
