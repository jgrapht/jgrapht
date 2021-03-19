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
package org.jgrapht.alg.clustering;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;

import java.util.*;
import java.util.stream.*;

/**
 * A label propagation clustering algorithm.
 * 
 * <p>
 * The algorithm is a near linear time algorithm capable of discovering communities in large graphs.
 * It is described in detail in the following
 * <a href="http://dx.doi.org/10.1103/PhysRevE.76.036106">paper</a>:
 * <ul>
 * <li>Raghavan, U. N., Albert, R., and Kumara, S. (2007). Near linear time algorithm to detect
 * community structures in large-scale networks. Physical review E, 76(3), 036106.</li>
 * </ul>
 * 
 * <p>
 * As the paper title suggests the running time is close to linear. The algorithm runs in
 * iterations, each of which runs in $O(n + m)$ where $n$ is the number of vertices and $m$ is the
 * number of edges. The authors found experimentally that in most cases, 95% of the nodes or more
 * are classified correctly by the end of iteration 5. See the paper for more details.
 * 
 * <p>
 * The algorithm is randomized, meaning that two runs on the same graph may return different
 * results. If the user requires deterministic behavior, the random number generator can be provided
 * by the constructor.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class LabelPropagationClustering<V, E>
    implements
    ClusteringAlgorithm<V>
{
    private Graph<V, E> graph;
    private int maxIterations;
    private Random rng;
    private Clustering<V> result;

    /**
     * Create a new clustering algorithm.
     * 
     * @param graph the graph (needs to be undirected)
     */
    public LabelPropagationClustering(Graph<V, E> graph)
    {
        this(graph, 0, new Random());
    }

    /**
     * Create a new clustering algorithm.
     * 
     * @param graph the graph (needs to be undirected)
     * @param rng random number generator
     */
    public LabelPropagationClustering(Graph<V, E> graph, Random rng)
    {
        this(graph, 0, rng);
    }

    /**
     * Create a new clustering algorithm.
     * 
     * @param graph the graph (needs to be undirected)
     * @param maxIterations maximum number of iterations (zero means no limit)
     */
    public LabelPropagationClustering(Graph<V, E> graph, int maxIterations)
    {
        this(graph, maxIterations, new Random());
    }

    /**
     * Create a new clustering algorithm.
     * 
     * @param graph the graph (needs to be undirected)
     * @param maxIterations maximum number of iterations (zero means no limit)
     * @param rng random number generator
     */
    public LabelPropagationClustering(Graph<V, E> graph, int maxIterations, Random rng)
    {
        this.graph = GraphTests.requireUndirected(graph);
        this.maxIterations = maxIterations;
        this.rng = Objects.requireNonNull(rng);
        if (maxIterations < 0) {
            throw new IllegalArgumentException("Max iterations cannot be negative");
        }
    }

    @Override
    public Clustering<V> getClustering()
    {
        if (result == null) {
            result =
                new ClusteringImpl<>(new Implementation<>(graph, rng, maxIterations).compute());
        }
        return result;
    }

    /**
     * The actual implementation
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    private static class Implementation<V, E>
    {
        private Graph<V, E> graph;
        private Random rng;
        private int maxIterations;
        private Map<V, String> labels;

        /**
         * Initialize the computation
         * 
         * @param graph the graph
         * @param rng the random number generator
         * @param maxIterations maximum iterations
         */
        public Implementation(Graph<V, E> graph, Random rng, int maxIterations)
        {
            this.graph = graph;
            this.rng = rng;
            this.maxIterations = maxIterations;
            this.labels = new HashMap<>();

            int i = 0;
            for (V v : graph.vertexSet()) {
                labels.put(v, String.valueOf(i++));
            }
        }

        /**
         * Main loop of the algorithm
         * 
         * @return the clusters
         */
        public List<Set<V>> compute()
        {
            int currentIteration = 0;
            while (true) {
                // is there a limit on the number of iterations?
                if (maxIterations > 0 && currentIteration > maxIterations) {
                    break;
                }

                // perform synchronous label update (to avoid oscillations)
                boolean anyChange = false;
                List<V> allVertices = new ArrayList<>(graph.vertexSet());
                Collections.shuffle(allVertices, rng);
                for (V v : allVertices) {
                    if (updateLabel(v)) {
                        anyChange = true;
                    }
                }

                // stopping criterion
                if (anyChange == false || shouldStop()) {
                    break;
                }

                currentIteration++;
            }

            return computeCommunities();
        }

        /**
         * Stopping criterion. Perform the iterative process until every node in the network has a
         * label equal to a label that the maximum number of its neighbors belong to.
         * 
         * @return true whether we should stop, false otherwise
         */
        private boolean shouldStop()
        {
            for (V v : graph.vertexSet()) {
                Pair<Map<String, Integer>, Integer> labelCountsAndMaximum =
                    getNeighborLabelCountsAndMaximum(v);
                Map<String, Integer> counts = labelCountsAndMaximum.getFirst();

                String vLabel = labels.get(v);
                int vLabelCount = counts.getOrDefault(vLabel, 0);
                int maxCount = labelCountsAndMaximum.getSecond();
                if (maxCount > vLabelCount) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Compute the frequency of the labels of all neighbors of a vertex and the maximum
         * frequency of the vertices, which have a label not equal to the input vertex label.
         * 
         * @param v the input vertex
         * @return the frequency of the labels of all neighbors of a vertex and the maximum label
         *         frequency of the vertices with a label not equal to the input vertex label
         */
        private Pair<Map<String, Integer>, Integer> getNeighborLabelCountsAndMaximum(V v)
        {
            Map<String, Integer> counts = new HashMap<>();

            String vLabel = labels.get(v);
            int maxCount = 0;
            for (E e : graph.edgesOf(v)) {
                V u = Graphs.getOppositeVertex(graph, e, v);
                String uLabel = labels.get(u);
                int newCount = counts.getOrDefault(uLabel, 0) + 1;
                counts.put(uLabel, newCount);
                if (newCount > maxCount && !uLabel.equals(vLabel)) {
                    maxCount = newCount;
                }
            }

            return Pair.of(counts, maxCount);
        }

        /**
         * Update the label of a vertex.
         * 
         * @param v the vertex
         * @return true if a label change occurred
         */
        private boolean updateLabel(V v)
        {
            if (graph.degreeOf(v) == 0) {
                return false;
            }

            Pair<Map<String, Integer>, Integer> labelCountsAndMaximum =
                getNeighborLabelCountsAndMaximum(v);
            Map<String, Integer> counts = labelCountsAndMaximum.getFirst();

            String oldLabel = labels.get(v);
            int vLabelCount = counts.getOrDefault(oldLabel, 0);
            final int maxCount = Math.max(labelCountsAndMaximum.getSecond(), vLabelCount);

            ArrayList<String> maxLabels = counts
                .entrySet().stream().filter(e -> e.getValue() == maxCount).map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));
            String newLabel = maxLabels.get(rng.nextInt(maxLabels.size()));

            if (oldLabel.equals(newLabel)) {
                return false;
            } else {
                labels.put(v, newLabel);
                return true;
            }
        }

        /**
         * Compute the final communities from the labels. We need to do some extra work due to the
         * way the algorithm works, as described in the following paragraph from the original paper.
         * 
         * "When the algorithm terminates it is possible that two or more disconnected groups of
         * nodes have the same label (the groups are connected in the network via other nodes of
         * different labels). This happens when two or more neighbors of a node receive its label
         * and pass the labels in different directions, which ultimately leads to different
         * communities adopting the same label. In such cases, after the algorithm terminates one
         * can run a simple breadth-first search on the sub-networks of each individual groups to
         * separate the disconnected communities."
         * 
         * @return the clustering
         */
        private List<Set<V>> computeCommunities()
        {
            Map<V, String> finalLabels = new HashMap<>();
            int nextLabel = 0;

            for (V v : graph.vertexSet()) {
                if (finalLabels.containsKey(v)) {
                    continue;
                }

                // start a BFS
                Deque<V> frontier = new ArrayDeque<>();
                String currentLabel = String.valueOf(nextLabel++);
                finalLabels.put(v, currentLabel);
                frontier.addLast(v);

                while (!frontier.isEmpty()) {
                    V u = frontier.removeFirst();
                    String uLabel = labels.get(u);

                    for (E e : graph.edgesOf(u)) {
                        V w = Graphs.getOppositeVertex(graph, e, u);
                        String wLabel = labels.get(w);
                        if (!wLabel.equals(uLabel) || finalLabels.containsKey(w)) {
                            continue;
                        }
                        finalLabels.put(w, currentLabel);
                        frontier.addLast(w);
                    }
                }
            }

            return convert(graph, finalLabels);
        }

        /**
         * Convert from a map representation to a list of sets.
         * 
         * @param graph the graph
         * @param labels the map representation
         * @return the list of sets
         */
        private List<Set<V>> convert(Graph<V, E> graph, Map<V, String> labels)
        {
            Map<String, Set<V>> clusterMap = new LinkedHashMap<>();
            for (V v : graph.vertexSet()) {
                String rv = labels.get(v);
                if (rv == null) {
                    throw new IllegalArgumentException("Not all vertices have labels.");
                }
                Set<V> cluster = clusterMap.get(rv);
                if (cluster == null) {
                    cluster = new LinkedHashSet<>();
                    clusterMap.put(rv, cluster);
                }
                cluster.add(v);
            }
            return new ArrayList<>(clusterMap.values());
        }

    }

}
