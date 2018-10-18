/*
 * (C) Copyright 2018-2018, by Semen Chudakov and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
 * To prevent the necessity to synchronize threads the bucket structure is implemented here
 * as a map of vertices to their bucket indices. Furthermore, every time a vertex is inserted
 * in a bucket the tentative distance to the vertex and its predecessor should be updated in
 * order to reconstruct the shortest paths tree at the end of the computation. Therefore to be
 * able to update all the information safely it is kept in a single object which is wrapped
 * in {@link AtomicReference}.
 *
 * <p>
 * This implementation delegates paralleling to the {@link ExecutorService}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @since September 2018
 */
public class DeltaSteppingShortestPath<V, E> extends BaseShortestPathAlgorithm<V, E> {
    /**
     * Error message for reporting the existence of an edge with negative weight.
     */
    private static final String NEGATIVE_EDGE_WEIGHT_NOT_ALLOWED = "Negative edge weight not allowed";
    /**
     * Error message for reporting that delta must be positive.
     */
    private static final String DELTA_MUST_BE_POSITIVE = "Delta must be positive";
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
     * Num of buckets in the bucket structure.
     */
    private int numOfBuckets;
    /**
     * Maximum edge weight in the {@link #graph}.
     */
    private double maxEdgeWeight;
    /**
     * Maximum number of threads the {@link #executor} can run at the same time.
     */
    private int parallelism;
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

    private Map<V, Pair<Double, E>> distanceAndPredecessorMap;
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
     * Constructs a new instance of the algorithm for a given graph and parallelism.
     * Initializes {@link #delta} to $0.0$ to preserve lazy computation style.
     *
     * @param graph       the graph
     * @param parallelism num of threads
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, int parallelism) {
        super(graph);
        delta = 0.0;
        this.parallelism = parallelism;
        init();
    }

    /**
     * Constructs a new instance of the algorithm for a given graph and delta.
     *
     * @param graph the graph
     * @param delta bucket width
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, double delta) {
        this(graph, delta, DEFAULT_PARALLELISM);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, delta and parallelism.
     *
     * @param graph       the graph
     * @param delta       bucket width
     * @param parallelism num of threads
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, double delta, int parallelism) {
        super(graph);
        if (delta <= 0) {
            throw new IllegalArgumentException(DELTA_MUST_BE_POSITIVE);
        }
        this.delta = delta;
        this.parallelism = parallelism;
        init();
    }

    /**
     * Initializes {@link #light}, {@link #heavy},
     * {@link #executor} and {@link #completionService} fields.
     */
    private void init() {
        light = new HashMap<>(graph.vertexSet().size());
        heavy = new HashMap<>(graph.vertexSet().size());
        executor = Executors.newFixedThreadPool(parallelism);
        completionService = new ExecutorCompletionService<>(executor);
        distanceAndPredecessorMap = new ConcurrentHashMap<>(graph.vertexSet().size());
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
        graph.vertexSet().forEach(v -> {
            light.put(v, new HashSet<>());
            heavy.put(v, new HashSet<>());
            distanceAndPredecessorMap.put(v, Pair.of(Double.POSITIVE_INFINITY, null));
        });
        graph.vertexSet().parallelStream().forEach(v -> {
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

        int firstNonEmptyBucket;
        while (!bucketStructure.isEmpty()) {
            firstNonEmptyBucket = bucketStructure.firstNonEmptyBucket();
            NavigableSet<V> removed = new TreeSet<>();
            NavigableSet<V> bucketElements = bucketStructure.bucketElements(firstNonEmptyBucket);

            while (!bucketElements.isEmpty()) {
                removed.addAll(bucketElements);
//                bucketStructure.clearBucket(firstNonEmptyBucket);

                findAndRelaxRequests(bucketElements, light);
                bucketElements = bucketStructure.bucketElements(firstNonEmptyBucket);
            }

            findAndRelaxRequests(removed, heavy);
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
     * Receives tasks by calling {@link #getRelaxTasks(NavigableSet, Map)},
     * submits them to the {@link #completionService} and waits till
     * they are finished.
     *
     * @param vertices  vertices
     * @param edgesKind vertex to edges map
     */
    private void findAndRelaxRequests(NavigableSet<V> vertices, Map<V, Set<E>> edgesKind) {
        List<Runnable> tasks = getRelaxTasks(vertices, edgesKind);
//        System.out.println(vertices.size() + " " + tasks.size());
        for (Runnable task : tasks) {
            completionService.submit(task, null);
        }
        for (int i = 0; i < tasks.size(); i++) {
            try {
                completionService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates relaxation tasks using prefix sums load balancing.
     *
     * <p>
     * Iterates over {@code vertices} and creates a new {@link RelaxTask}
     * whenever sum of relax requests for passed vertices reaches.
     *
     * @param vertices  vertex list
     * @param edgesKind light or heavy edges
     * @return relax tasks
     */
    private List<Runnable> getRelaxTasks(NavigableSet<V> vertices, Map<V, Set<E>> edgesKind) {
        int totalRequests = vertices.stream().mapToInt(v -> edgesKind.get(v).size()).sum();
        int requestsPerThread = (int) Math.ceil((double) totalRequests / parallelism);

        List<Runnable> tasks = new ArrayList<>();
        if (totalRequests == 0) {
            return tasks;
        }

        V begin = null;
        int numOfRequests = 0;
        for (V end : vertices) {
            if (begin == null) {
                begin = end;
            }
            numOfRequests += edgesKind.get(end).size();
            if (numOfRequests >= requestsPerThread) {
                if (numOfRequests != 0) {
                    tasks.add(new RelaxTask(vertices.subSet(begin, true, end, true), edgesKind));
                }
                numOfRequests = 0;
                begin = null;
            }
        }
        if (numOfRequests != 0) {
            tasks.add(new RelaxTask(vertices.tailSet(begin), edgesKind));
        }
        return tasks;
    }

    /**
     * Performs relaxation in parallel-safe fashion. Vertex data {@code v} is
     * considered updated once either {@code distance} is greater than or equal
     * to tentative distance in ??? or
     * {@link AtomicReference#compareAndSet(Object, Object)} returns {@code true}.
     *
     * @param v        vertex
     * @param e        edge to predecessor
     * @param distance distance
     */
    private void relax(V v, E e, double distance) {
        int updatedBucket = bucketIndex(distance);
        synchronized (v) {
            Pair<Double, E> oldData = distanceAndPredecessorMap.get(v);
            int oldBucket = bucketIndex(oldData.getFirst());
            if (distance < oldData.getFirst()) {
                if (!oldData.getFirst().equals(Double.POSITIVE_INFINITY)) {
                    bucketStructure.remove(oldBucket, v);
                }
                bucketStructure.add(updatedBucket, v);
                distanceAndPredecessorMap.put(v, Pair.of(distance, e));
            }
        }
    }

    /**
     * Calculates number of buckets in the bucket structure.
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
        private Set<V> vertices;
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
        RelaxTask(Set<V> vertices, Map<V, Set<E>> edgesKind) {
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

    class BucketStructure {
        private final Set[] buckets;
        private int numOfBuckets;

        BucketStructure(int numOfBuckets) {
            buckets = new Set[numOfBuckets];
            for (int i = 0; i < buckets.length; i++) {
                buckets[i] = new ConcurrentSkipListSet<V>();
            }
            this.numOfBuckets = numOfBuckets;
        }

        void add(int bucket, V element) {
            buckets[bucket].add(element);
        }

        void remove(int bucket, V element) {
            buckets[bucket].remove(element);
        }

        NavigableSet<V> bucketElements(int bucket) {
            NavigableSet result = (ConcurrentSkipListSet<V>) buckets[bucket];
            buckets[bucket] = new ConcurrentSkipListSet<V>();
            return result;
        }

        void clearBucket(int bucket) {
            if (!buckets[bucket].isEmpty()) {
                buckets[bucket].clear();
            }
        }

        int firstNonEmptyBucket() {
            for (int i = 0; i < numOfBuckets; i++) {
                if (!buckets[i].isEmpty()) {
                    return i;
                }
            }
            return -1;
        }

        boolean isEmpty() {
            return firstNonEmptyBucket() == -1;
        }

        int size() {
            return numOfBuckets;
        }
    }
}
