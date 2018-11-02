/*
 * (C) Copyright 2018-2018, by Semen Chudakov and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the parallel version of the delta-stepping algorithm.
 *
 * <p>
 * The time complexity of the algorithm is
 * $O(\frac{(|V| + |E| + n_{\Delta} + m_{\Delta})}{p} + \frac{L}{\Delta}\cdot d\cdot l_{\Delta}\cdot \log n)$, where,
 * denoting $\Delta$-path as a path of total weight at most $\Delta$ with no edge repetition,
 * <ul>
 * <li>$n_{\Delta}$ - number of vertices pairs (u,v), where u and v are connected by some $\Delta$-path.</li>
 * <li>$m_{\Delta}$ - number of vertices triples (u,$v^{\prime}$,v), where u and $v^{\prime}$ are connected
 * by some $\Delta$-path and edge ($v^{\prime}$,v) has weight at most $\Delta$.</li>
 * <li>$L$ - maximal weight of a shortest path from selected source to any sink.</li>
 * <li>$d$ - maximal edge degree.</li>
 * <li>$l_{\Delta}$ - maximal number of edges in a $\Delta$-path $+1$.</li>
 * </ul>
 *
 * <p>
 * The algorithm is described in the paper: U. Meyer, P. Sanders,
 * $\Delta$-stepping: a parallelizable shortest path algorithm, Journal of Algorithms,
 * Volume 49, Issue 1, 2003, Pages 114-152, ISSN 0196-6774.
 *
 * <p>
 * The algorithm solves the single source shortest path problem in a graph with no
 * negative weight edges. Its advantage of the {@link DijkstraShortestPath}
 * algorithm is that it can benefit from multiple threads. While the Dijkstra`s
 * algorithm is fully sequential and the {@link BellmanFordShortestPath} algorithm
 * has high parallelism since all edges can be relaxed in parallel, the delta-stepping
 * introduces parameter delta, which, when chooses optimally, yields still good parallelism
 * and at the same time enables avoiding too many re-relaxations of the edges.
 *
 * <p>
 * In this algorithm the vertices of the graph are maintained in the bucket structure according to
 * the their tentative distance in the shortest path tree computed so far. On each iteration the
 * first non-empty bucket is emptied and all light edges emanating from its vertices are relaxed.
 * This may take more than $1$ iteration due to the reinsertion to the same bucket occurring during
 * the relaxations process. All heavy edges of the vertices that were removed from the bucket are than
 * relaxed one and for all.
 *
 * <p>
 * This implementation delegates paralleling to the {@link ExecutorService}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @since October 2018
 */
public class DeltaSteppingShortestPath<V, E> extends BaseShortestPathAlgorithm<V, E> {
    /**
     * Error message for reporting the existence of an edge with negative weight.
     */
    private static final String NEGATIVE_EDGE_WEIGHT_NOT_ALLOWED = "Negative edge weight not allowed";
    /**
     * Error message for reporting that delta must be positive.
     */
    private static final String DELTA_MUST_BE_NON_NEGATIVE = "Delta must be non-negative";
    /**
     * Default value for {@link #parallelism}.
     */
    private static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();

    /**
     * The bucket width. A bucket with index $i$ therefore stores
     * a vertex v if and only if v is queued and tentative distance
     * to v $\in[i\cdot\Delta,(i+1)\cdot\Delta]$
     */
    private double delta;
    /**
     * Maximum number of threads the {@link #executor} can run at the same time.
     */
    private int parallelism;
    /**
     * Indicates whether load balancing should be used while computations or not.
     * Should be true if the graph large vertices out degrees deviations as well
     * log simple paths of small weight.
     */
    private boolean loadBalancing;

    /**
     * Num of buckets in the bucket structure.
     */
    private int numOfBuckets;
    /**
     * Maximum edge weight in the {@link #graph}.
     */
    private double maxEdgeWeight;

    /**
     * Map with light edges for each vertex. An edge is considered
     * light if its weight is less than or equal to {@link #delta}.
     */
    private Map<V, Set<E>> light;
    /**
     * Map with heavy edges for each vertex. An edge is
     * considered heavy if its weight is greater than {@link #delta}.
     */
    private Map<V, Set<E>> heavy;

    /**
     * Map to store predecessor for each vertex in the shortest path tree.
     */
    private Map<V, Pair<Double, E>> distanceAndPredecessorMap;
    /**
     * Buckets structure.
     */
    private BucketStructure bucketStructure;

    /**
     * Executor to which relax tasks will be submitted.
     */
    private ExecutorService executor;
    /**
     * Enables keep track of when all submitted to the
     * {@link #executor} tasks are finished.
     */
    private ExecutorCompletionService<Void> completionService;

    /**
     * Constructs a new instance of the algorithm for a given graph.
     *
     * @param graph graph
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph) {
        this(graph, DEFAULT_PARALLELISM);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph and delta.
     *
     * @param graph the graph
     * @param delta bucket width
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, double delta) {
        this(graph, delta, DEFAULT_PARALLELISM, false);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph and parallelism.
     *
     * @param graph       the graph
     * @param parallelism parallelism
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, int parallelism) {
        this(graph, 0.0, parallelism, false);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph and load balancing.
     *
     * @param graph         the graph
     * @param loadBalancing loadBalancing
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, boolean loadBalancing) {
        this(graph, 0.0, DEFAULT_PARALLELISM, loadBalancing);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, delta and loadBalancing.
     *
     * @param graph         the graph
     * @param delta         bucket width
     * @param loadBalancing loadBalancing
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, double delta, boolean loadBalancing) {
        this(graph, delta, DEFAULT_PARALLELISM, loadBalancing);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, delta, parallelism and loadBalancing.
     * If delta is $0.0$ it will be computed when the {@link #getPath(Object, Object)} method will be invoked.
     *
     * @param graph         the graph
     * @param delta         bucket width
     * @param parallelism   num of threads
     * @param loadBalancing loadBalancing
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, double delta, int parallelism, boolean loadBalancing) {
        super(graph);
        if (delta < 0) {
            throw new IllegalArgumentException(DELTA_MUST_BE_NON_NEGATIVE);
        }
        this.delta = delta;
        this.parallelism = parallelism;
        this.loadBalancing = loadBalancing;
        light = new ConcurrentHashMap<>(graph.vertexSet().size());
        heavy = new ConcurrentHashMap<>(graph.vertexSet().size());
        distanceAndPredecessorMap = new ConcurrentHashMap<>(graph.vertexSet().size());
        executor = Executors.newFixedThreadPool(parallelism);
        completionService = new ExecutorCompletionService<>(executor);
    }

    /**
     * Calculates max edge weight in the {@link #graph}.
     *
     * @return max edge weight
     */
    private double getMaxEdgeWeight() {
        double result = 0.0;
        double weight;
        for (E defaultWeightedEdge : graph.edgeSet()) {
            weight = graph.getEdgeWeight(defaultWeightedEdge);
            if (weight < 0) {
                throw new IllegalArgumentException(NEGATIVE_EDGE_WEIGHT_NOT_ALLOWED);
            }
            if (weight > result) {
                result = weight;
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> getPath(V source, V sink) {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SINK_VERTEX);
        }
        return getPaths(source).getPath(sink);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SingleSourcePaths<V, E> getPaths(V source) {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        maxEdgeWeight = getMaxEdgeWeight();
        if (delta == 0.0) {
            delta = findDelta();
        }
        numOfBuckets = numOfBuckets();
        bucketStructure = new BucketStructure(numOfBuckets);
        fillMaps();

        computeShortestPaths(source);

        return new TreeSingleSourcePathsImpl<>(graph, source, distanceAndPredecessorMap);
    }

    /**
     * Calculates value of {@link #delta}. The value is calculated to
     * maximal edge weight divided by maximal out-degree in the {@link #graph}
     * or $1.0$ if edge set of the {@link #graph} is empty.
     *
     * @return bucket width
     */
    private double findDelta() {
        if (maxEdgeWeight == 0) {
            return 1.0;
        } else {
            int maxOutDegree = graph.vertexSet().parallelStream().mapToInt(graph::outDegreeOf).max().orElse(0);
            return maxEdgeWeight / maxOutDegree;
        }
    }

    /**
     * Fills {@link #light}, {@link #heavy}
     */
    private void fillMaps() {
        graph.vertexSet().parallelStream().forEach(v -> {
            light.put(v, new HashSet<>());
            heavy.put(v, new HashSet<>());
            distanceAndPredecessorMap.put(v, Pair.of(Double.POSITIVE_INFINITY, null));
            for (E e : graph.outgoingEdgesOf(v)) {
                if (graph.getEdgeWeight(e) > delta) {
                    heavy.get(v).add(e);
                } else {
                    light.get(v).add(e);
                }
            }
        });
    }

    /**
     * Performs shortest paths computation.
     *
     * @param source the source vertex
     */
    private void computeShortestPaths(V source) {
        relax(source, null, 0.0);

        int firstNonEmptyBucket = 0;
        while (firstNonEmptyBucket != -1) {
            List<Set<V>> removed = new ArrayList<>();
            Set<V> bucketElements = bucketStructure.getContentAndReplace(firstNonEmptyBucket);

            while (!bucketElements.isEmpty()) {
                removed.add(bucketElements);

                findAndRelaxRequests(bucketElements, light);
                bucketElements = bucketStructure.getContentAndReplace(firstNonEmptyBucket);
            }

            findAndRelaxRequests(removed, heavy);
            firstNonEmptyBucket = bucketStructure.firstNonEmptyBucket();
        }
        shutDownExecutor();
    }

    /**
     * Performs shutting down the {@link #executor}.
     */
    private void shutDownExecutor() {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Manages execution of edges relaxation.
     * Starts relaxations tasks by calling !!!!!!,
     * receives num of tasks as the result {@link #completionService} and takes them
     * from the {@link #executor} when they are finished.
     *
     * @param vertices  vertices
     * @param edgesKind vertex to edges map
     */
    private void findAndRelaxRequests(Set<V> vertices, Map<V, Set<E>> edgesKind) {
        int numOfTasks;
        if (loadBalancing) {
            long totalRequests = getTotalRequests(vertices, edgesKind);
            numOfTasks = startRelaxTasksWithLoadBalancing(vertices, edgesKind, totalRequests);
        } else {
            long numOVertices = getNumOfVertices(vertices);
            numOfTasks = startRelaxTasks(vertices, edgesKind, numOVertices);
        }
        waitForTasksCompletion(numOfTasks);
    }

    private void findAndRelaxRequests(List<Set<V>> vertices, Map<V, Set<E>> edgesKind) {
        int numOfTasks;
        if (loadBalancing) {
            long totalRequests = getTotalRequests(vertices, edgesKind);
            numOfTasks = startRelaxTasksWithLoadBalancing(vertices, edgesKind, totalRequests);
        } else {
            long numOVertices = getNumOfVertices(vertices);
            numOfTasks = startRelaxTasks(vertices, edgesKind, numOVertices);
        }
        waitForTasksCompletion(numOfTasks);
    }

    private void waitForTasksCompletion(int numOfTasks) {
        for (int i = 0; i < numOfTasks; i++) {
            try {
                completionService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long getNumOfVertices(Set<V> vertices) {
        return vertices.size();
    }

    private long getNumOfVertices(List<Set<V>> vertices) {
        return vertices.stream().mapToInt(Set::size).sum();
    }

    private long getTotalRequests(Set<V> vertices, Map<V, Set<E>> edgeKind) {
        return vertices.stream().mapToInt(v -> edgeKind.get(v).size()).sum();
    }

    private long getTotalRequests(List<Set<V>> vertices, Map<V, Set<E>> edgeKind) {
        return vertices.stream().mapToInt(set -> set.stream().mapToInt(v -> edgeKind.get(v).size()).sum()).sum();
    }


    /**
     * Generates relaxation tasks and submits them to the {@link #executor}.
     *
     * <p>
     * Calculates total amount of requests for {@code vertices}. Uses prefix sums by
     * num of requests for load balancing. Iterates over {@code vertices} and creates
     * a new {@link RelaxTask} whenever sum of relax requests for passed vertices reaches
     * $\frac{total relax requests}{parallelism}$ value.
     *
     * @param vertices  vertex list
     * @param edgesKind light or heavy edges
     * @return relax tasks
     */
    private int startRelaxTasks(Set<V> vertices, Map<V, Set<E>> edgesKind, long numOfVertices) {
        if (numOfVertices == 0) {
            return 0;
        }
        long verticesPerTask = getVerticesPerTask(numOfVertices);

        int numOfTasks = 0;
        List<V> taskVertices = new ArrayList<>();
        int currentAmount = 0;
        for (V v : vertices) {
            taskVertices.add(v);
            currentAmount++;
            if (currentAmount == verticesPerTask) {
                numOfTasks++;
                completionService.submit(new RelaxTask(taskVertices, edgesKind), null);
                currentAmount = 0;
                taskVertices = new ArrayList<>();
            }
        }
        if (currentAmount != 0) {
            numOfTasks++;
            completionService.submit(new RelaxTask(taskVertices, edgesKind), null);
        }
        return numOfTasks;
    }

    /**
     * Generates relaxation tasks and submits them to the {@link #executor}.
     *
     * <p>
     * Calculates total amount of requests for {@code vertices}. Uses prefix sums by
     * num of requests for load balancing. Iterates over {@code vertices} and creates
     * a new {@link RelaxTask} whenever sum of relax requests for passed vertices reaches
     * $\frac{total relax requests}{parallelism}$ value.
     *
     * @param vertices  vertex list
     * @param edgesKind light or heavy edges
     * @return relax tasks
     */
    private int startRelaxTasks(List<Set<V>> vertices, Map<V, Set<E>> edgesKind, long numOfVertices) {
        if (numOfVertices == 0) {
            return 0;
        }
        long verticesPerTask = getVerticesPerTask(numOfVertices);

        int numOfTasks = 0;
        List<V> taskVertices = new ArrayList<>();
        int currentAmount = 0;
        for (Set<V> set : vertices) {
            for (V v : set) {
                taskVertices.add(v);
                currentAmount++;
                if (currentAmount == verticesPerTask) {
                    numOfTasks++;
                    completionService.submit(new RelaxTask(taskVertices, edgesKind), null);
                    currentAmount = 0;
                    taskVertices = new ArrayList<>();
                }
            }
        }
        if (currentAmount != 0) {
            numOfTasks++;
            completionService.submit(new RelaxTask(taskVertices, edgesKind), null);
        }
        return numOfTasks;
    }

    /**
     * Generates relaxation tasks and submits them to the {@link #executor}.
     *
     * <p>
     * Calculates total amount of requests for {@code vertices}. Uses prefix sums by
     * num of requests for load balancing. Iterates over {@code vertices} and creates
     * a new {@link RelaxTask} whenever sum of relax requests for passed vertices reaches
     * $\frac{total relax requests}{parallelism}$ value.
     *
     * @param vertices  vertex list
     * @param edgesKind light or heavy edges
     * @return relax tasks
     */
    private int startRelaxTasksWithLoadBalancing(Set<V> vertices, Map<V, Set<E>> edgesKind, long totalRequests) {
        if (totalRequests == 0) {
            return 0;
        }
        long requestsPerTask = getRequestsPerTask(totalRequests);


        int numOfTasks = 0;
        List<V> taskVertices = new ArrayList<>();
        int numOfRequests = 0;
        for (V v : vertices) {
            taskVertices.add(v);
            numOfRequests += edgesKind.get(v).size();
            if (numOfRequests >= requestsPerTask) {
                if (numOfRequests != 0) {
                    numOfTasks++;
                    completionService.submit(new RelaxTask(taskVertices, edgesKind), null);
                }
                numOfRequests = 0;
                taskVertices = new ArrayList<>();
            }
        }
        if (numOfRequests != 0) {
            numOfTasks++;
            completionService.submit(new RelaxTask(taskVertices, edgesKind), null);
        }
        return numOfTasks;
    }

    /**
     * Generates relaxation tasks and submits them to the {@link #executor}.
     *
     * <p>
     * Calculates total amount of requests for {@code vertices}. Uses prefix sums by
     * num of requests for load balancing. Iterates over {@code vertices} and creates
     * a new {@link RelaxTask} whenever sum of relax requests for passed vertices reaches
     * $\frac{total relax requests}{parallelism}$ value.
     *
     * @param vertices  vertex list
     * @param edgesKind light or heavy edges
     * @return relax tasks
     */
    private int startRelaxTasksWithLoadBalancing(List<Set<V>> vertices, Map<V, Set<E>> edgesKind, long totalRequests) {
        if (totalRequests == 0) {
            return 0;
        }
        long requestsPerTask = getRequestsPerTask(totalRequests);


        int numOfTasks = 0;
        List<V> taskVertices = new ArrayList<>();
        int numOfRequests = 0;
        for (Set<V> set : vertices) {
            for (V v : set) {
                taskVertices.add(v);
                numOfRequests += edgesKind.get(v).size();
                if (numOfRequests >= requestsPerTask) {
                    if (numOfRequests != 0) {
                        numOfTasks++;
                        completionService.submit(new RelaxTask(taskVertices, edgesKind), null);
                    }
                    numOfRequests = 0;
                    taskVertices = new ArrayList<>();
                }
            }
        }
        if (numOfRequests != 0) {
            numOfTasks++;
            completionService.submit(new RelaxTask(taskVertices, edgesKind), null);
        }
        return numOfTasks;
    }

    private long getVerticesPerTask(long numOfVertices) {
        return (long) Math.ceil((double) numOfVertices / parallelism);
    }

    private long getRequestsPerTask(long totalRequests) {
        return (int) Math.ceil((double) totalRequests / parallelism);
    }


    /**
     * Performs relaxation in parallel-safe fashion. Synchronises by {@code vertex}
     * then if new tentative distance is less then removes {@code v} from the old bucket,
     * adds is to the new bucket and updates {@link #distanceAndPredecessorMap} value for {@code v}.
     *
     * @param v        vertex
     * @param e        edge to predecessor
     * @param distance distance
     */
    private void relax(V v, E e, double distance) {
        int updatedBucket = bucketIndex(distance);
        synchronized (v) {
            Pair<Double, E> oldData = distanceAndPredecessorMap.get(v);
            if (distance < oldData.getFirst()) {
                if (!oldData.getFirst().equals(Double.POSITIVE_INFINITY)) {
                    bucketStructure.remove(bucketIndex(oldData.getFirst()), v);
                }
                bucketStructure.add(updatedBucket, v);
                distanceAndPredecessorMap.put(v, Pair.of(distance, e));
            }
        }
    }

    /**
     * Calculates the number of buckets in the bucket structure.
     *
     * @return num of buckets
     */
    private int numOfBuckets() {
        return (int) (Math.ceil(maxEdgeWeight / delta) + 1);
    }

    /**
     * Calculates bucket index for a given {@code distance}.
     *
     * @param distance distance
     * @return bucket index
     */
    private int bucketIndex(double distance) {
        return ((int) Math.round(distance / delta)) % numOfBuckets;
    }

    /**
     * Represents task that is submitted to the {@link #completionService}
     * during shortest path computation.
     */
    class RelaxTask implements Runnable {
        /**
         * Vertices which edges will be relaxed.
         */
        private List<V> vertices;
        /**
         * Maps vertices to their edges.
         */
        private Map<V, Set<E>> edgesKind;

        /**
         * Constructs instance of a new task.
         *
         * @param vertices  vertices
         * @param edgesKind edges
         */
        RelaxTask(List<V> vertices, Map<V, Set<E>> edgesKind) {
            this.vertices = vertices;
            this.edgesKind = edgesKind;
        }

        /**
         * Performs relaxation of edges emanating from {@link #vertices}.
         */
        @Override
        public void run() {
            for (V v : vertices) {
                for (E e : edgesKind.get(v)) {
                    relax(Graphs.getOppositeVertex(graph, e, v), e, distanceAndPredecessorMap.get(v).getFirst() + graph.getEdgeWeight(e));
                }
            }
        }
    }

    /**
     * Implements bucket structure.
     */
    class BucketStructure {
        /**
         * Buckets.
         */
        private final Set[] buckets;
        /**
         * Size of {@link #buckets}.
         */
        private int numOfBuckets;

        /**
         * Constructs a new instance of the buckets structure.
         * Initializes every bucket in the {@link #buckets} with
         * an instance of the {@link ConcurrentSkipListSet} object.
         *
         * @param numOfBuckets num of buckets
         */
        BucketStructure(int numOfBuckets) {
            buckets = new Set[numOfBuckets];
            for (int i = 0; i < buckets.length; i++) {
                buckets[i] = new ConcurrentSkipListSet<V>();
            }
            this.numOfBuckets = numOfBuckets;
        }

        /**
         * Adds vertex {@code v} to the bucket indicated by {@code bucket} index.
         *
         * @param bucket bucket index
         * @param v      vertex
         */
        void add(int bucket, V v) {
            buckets[bucket].add(v);
        }

        /**
         * Removes vertex {@code v} from the bucket indicated by {@code bucket} index.
         *
         * @param bucket bucket index
         * @param v      vertex
         */
        void remove(int bucket, V v) {
            buckets[bucket].remove(v);
        }

        /**
         * Return vertex set of the bucket indicated by the index {@code bucket}
         * and replace if with a new instance of the {@link ConcurrentSkipListSet} object.
         *
         * @param bucket bucket index
         * @return set of vertices
         */
        Set<V> getContentAndReplace(int bucket) {
            Set<V> result = (Set<V>) buckets[bucket];
            buckets[bucket] = new ConcurrentSkipListSet<V>();
            return result;
        }

        /**
         * Calculates the index of the first non-empty bucket
         * or $-1$ if the buckets structure is empty.
         *
         * @return index of the first non-empty bucket
         */
        int firstNonEmptyBucket() {
            for (int i = 0; i < numOfBuckets; i++) {
                if (!buckets[i].isEmpty()) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * Returns <tt>true</tt> if this structure contains at least one non-empty bucket.
         *
         * @return <tt>true</tt> if buckets structure contains at least one non-empty bucket
         */
        boolean isEmpty() {
            return firstNonEmptyBucket() == -1;
        }
    }
}
