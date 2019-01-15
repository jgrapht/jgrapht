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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * Concurrent implementation of the parallel version of the delta-stepping algorithm.
 *
 * <p>
 * The time complexity of the algorithm is
 * $O(\frac{(|V| + |E| + n_{\Delta} + m_{\Delta})}{p} + \frac{L}{\Delta}\cdot d\cdot l_{\Delta}\cdot \log n)$, where,
 * denoting $\Delta$-path as a path of total weight at most $\Delta$ with no edge repetition,
 * <ul>
 * <li>$n_{\Delta}$ - number of vertices pairs (u,v), where u and v are connected by some $\Delta$-path (a path with total weight at most $\Delta$).</li>
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
 * negative weight edges. Its advantage of the {@link DijkstraShortestPath} and
 * {@link BellmanFordShortestPath} algorithms is that it can benefit from multiple
 * threads. While the Dijkstra`s algorithm is fully sequential and the Bellman-Ford`s algorithm
 * has high parallelism since all edges can be relaxed simultaneously, the delta-stepping
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
 * @since January 2018
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
     * Empirically computed amount of tasks per worker thread in
     * the {@link ForkJoinPool} that yields good performance.
     */
    private static final int TASKS_TO_THREADS_RATIO = 20;

    /**
     * The bucket width. A bucket with index $i$ therefore stores
     * a vertex v if and only if v is queued and tentative distance
     * to v $\in[i\cdot\Delta,(i+1)\cdot\Delta]$
     */
    private double delta;
    /**
     * Maximal number of threads used in the computations.
     */
    private int parallelism;

    /**
     * Num of buckets in the bucket structure.
     */
    private int numOfBuckets;
    /**
     * Maximum edge weight in the {@link #graph}.
     */
    private double maxEdgeWeight;
    /**
     * Map to store predecessor for each vertex in the shortest path tree.
     */
    private Map<V, Pair<Double, E>> distanceAndPredecessorMap;
    /**
     * Buckets structure.
     */
    private Set[] bucketStructure;

    /**
     * Executor to which relax tasks will be submitted.
     */
    private ExecutorService executor;
    /**
     * Decorator for {@link #executor} that enables to keep track of
     * when all submitted tasks are finished.
     */
    private ExecutorCompletionService<Void> completionService;
    /**
     * Queue of vertices which edges should be relaxed on current iteration.
     */
    private Queue<V> verticesQueue;
    /**
     * Task for light edges relaxation.
     */
    private Runnable lightRelaxTask;
    /**
     * Task for light edges relaxation.
     */
    private Runnable heavyRelaxTask;
    /**
     * Indicates when all the vertices are been added to the
     * {@link #verticesQueue} on each iteration.
     */
    private volatile boolean allVerticesAdded;

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
        this(graph, delta, DEFAULT_PARALLELISM);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph and parallelism.
     *
     * @param graph       the graph
     * @param parallelism maximum number of threads used in the computations
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, int parallelism) {
        this(graph, 0.0, parallelism);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, delta, parallelism.
     * If delta is $0.0$ it will be computed during the algorithm execution. In general
     * if the value of $\frac{maximum edge weight}{maximum edge outdegree}$ is know beforehand,
     * it is preferable to specify it via this constructor, because processing the whole graph
     * to compute may significantly slow down the algorithm.
     *
     * @param graph       the graph
     * @param delta       bucket width
     * @param parallelism maximum number of threads used in the computations
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, double delta, int parallelism) {
        super(graph);
        if (delta < 0) {
            throw new IllegalArgumentException(DELTA_MUST_BE_NON_NEGATIVE);
        }
        this.delta = delta;
        this.parallelism = parallelism;
        distanceAndPredecessorMap = new ConcurrentHashMap<>(graph.vertexSet().size());
        executor = Executors.newFixedThreadPool(parallelism);
        completionService = new ExecutorCompletionService<>(executor);
        verticesQueue = new ConcurrentLinkedQueue<>();
        lightRelaxTask = new LightRelaxTask(verticesQueue);
        heavyRelaxTask = new HeavyRelaxTask(verticesQueue);
    }

    /**
     * Calculates max edge weight in the {@link #graph}.
     *
     * @return max edge weight
     */
    private double getMaxEdgeWeight() {
        ForkJoinTask<Double> task = ForkJoinPool.commonPool().submit(
                new MaxEdgeWeightTask(
                        graph.edgeSet().spliterator(),
                        graph.edgeSet().size() / (TASKS_TO_THREADS_RATIO * parallelism) + 1));
        return task.join();
    }

    /**
     * Is used during the algorithm to compute maximum edge weight of the {@link #graph}.
     * Apart from computing the maximal edge weight in the graph the task also checks if
     * there is emy edges with negative weights. It is done to speedup the algorithm.
     */
    class MaxEdgeWeightTask extends RecursiveTask<Double> {
        /**
         * Is used to split a collection and create new recursive tasks during the computation.
         */
        Spliterator<E> spliterator;
        /**
         * Amount of vertices at which he computation of performed sequentially.
         */
        long loadBalancing;

        /**
         * Constructs a new instance for the given spliterator and loadBalancing
         *
         * @param spliterator   spliterator
         * @param loadBalancing loadBalancing
         */
        MaxEdgeWeightTask(Spliterator<E> spliterator, long loadBalancing) {
            this.spliterator = spliterator;
            this.loadBalancing = loadBalancing;
        }

        /**
         * Computes maximum edge weight. If amount of vertices in
         * {@link #spliterator} is less than {@link #loadBalancing},
         * then computation is performed sequentially. If not, the
         * {@link #spliterator} is used to split the collection and
         * then two new child tasks are created.
         *
         * @return max edge weight
         */
        @Override
        protected Double compute() {
            if (spliterator.estimateSize() <= loadBalancing) {
                double[] max = {0};
                spliterator.forEachRemaining(e -> {
                    double weight = graph.getEdgeWeight(e);
                    if (weight < 0) {
                        throw new IllegalArgumentException(NEGATIVE_EDGE_WEIGHT_NOT_ALLOWED);
                    }
                    max[0] = Math.max(weight, max[0]);
                });
                return max[0];
            } else {
                MaxEdgeWeightTask t1 = new MaxEdgeWeightTask(spliterator.trySplit(), loadBalancing);
                t1.fork();
                MaxEdgeWeightTask t2 = new MaxEdgeWeightTask(spliterator, loadBalancing);
                return Math.max(t2.compute(), t1.join());
            }
        }
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
        if (delta == 0.0) { // the value should be computed
            delta = findDelta();
        }
        numOfBuckets = (int) (Math.ceil(maxEdgeWeight / delta) + 1);
        bucketStructure = new Set[numOfBuckets];
        for (int i = 0; i < numOfBuckets; i++) {
            bucketStructure[i] = new ConcurrentSkipListSet();
        }
        fillDistanceAndPredecessorMap();

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
     * Fills {@link #distanceAndPredecessorMap} concurrently.
     */
    private void fillDistanceAndPredecessorMap() {
        graph.vertexSet().parallelStream().forEach(v -> distanceAndPredecessorMap.put(v, Pair.of(Double.POSITIVE_INFINITY, null)));
    }

    /**
     * Performs computation of the shortest paths .
     *
     * @param source the source vertex
     */
    private void computeShortestPaths(V source) {
        relax(source, null, 0.0);

        int firstNonEmptyBucket = 0;
        List<Set<V>> removed = new ArrayList<>();
        while (firstNonEmptyBucket < numOfBuckets) {
            // the content of a bucket is replaced
            // in order not to handle the same vertices
            // several times
            Set<V> bucketElements = getContentAndReplace(firstNonEmptyBucket);

            while (!bucketElements.isEmpty()) {  // reinsertions may occur
                removed.add(bucketElements);
                findAndRelaxLightRequests(bucketElements);
                bucketElements = getContentAndReplace(firstNonEmptyBucket);
            }

            findAndRelaxHeavyRequests(removed);
            removed.clear();
            ++firstNonEmptyBucket;
            while (firstNonEmptyBucket < numOfBuckets
                    && bucketStructure[firstNonEmptyBucket].isEmpty()) { // skip empty buckets
                ++firstNonEmptyBucket;
            }
        }
        shutDownExecutor();
    }

    /**
     * Performs shutting down the {@link #executor}.
     */
    private void shutDownExecutor() {
        executor.shutdown();
        try { // wait till the executor is shut down
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Manages edges relaxation. Adds all elements from
     * {@code vertices} to the {@link #verticesQueue}
     * and submits as many {@link #lightRelaxTask} to the
     * {@link #completionService} as needed.
     *
     * @param vertices vertices
     */
    private void findAndRelaxLightRequests(Set<V> vertices) {
        allVerticesAdded = false;
        int numOfVertices = vertices.size();
        int numOfTasks;
        if (numOfVertices >= parallelism) {
            // use as available tasks
            numOfTasks = parallelism;
            Iterator<V> iterator = vertices.iterator();
            // provide some work to the workers
            addSetVertices(iterator, parallelism);
            submitTasks(lightRelaxTask, parallelism - 1); // one thread should
            // submit rest of vertices
            addSetRemaining(iterator);
            submitTasks(lightRelaxTask, 1); // use remaining thread for relaxation
        } else {
            // only several relaxation tasks are needed
            numOfTasks = numOfVertices;
            addSetRemaining(vertices.iterator());
            submitTasks(lightRelaxTask, numOfVertices);
        }

        allVerticesAdded = true;
        waitForTasksCompletion(numOfTasks);
    }

    /**
     * Manages execution of edges relaxation. Adds all
     * elements from {@code vertices} to the {@link #verticesQueue}
     * and submits as many {@link #heavyRelaxTask} to the
     * {@link #completionService} as needed.
     *
     * @param verticesSets set of sets of vertices
     */
    private void findAndRelaxHeavyRequests(List<Set<V>> verticesSets) {
        allVerticesAdded = false;
        int numOfVertices = verticesSets.stream().mapToInt(Set::size).sum();
        int numOfTasks;
        if (numOfVertices >= parallelism) {
            // use as available tasks
            numOfTasks = parallelism;
            Iterator<Set<V>> setIterator = verticesSets.iterator();
            // provide some work to the workers
            Iterator<V> iterator = addSetsVertices(setIterator, parallelism);
            submitTasks(heavyRelaxTask, parallelism - 1);// one thread should
            // submit rest of vertices
            addSetRemaining(iterator);
            addSetsRemaining(setIterator);
            submitTasks(heavyRelaxTask, 1); // use remaining thread for relaxation
        } else {
            // only several relaxation tasks are needed
            numOfTasks = numOfVertices;
            addSetsRemaining(verticesSets.iterator());
            submitTasks(heavyRelaxTask, numOfVertices);
        }

        allVerticesAdded = true;
        waitForTasksCompletion(numOfTasks);
    }

    /**
     * Adds {@code numOfVertices} vertices to the {@link #verticesQueue}
     * provided by the {@code iterator}.
     *
     * @param iterator      vertices iterator
     * @param numOfVertices vertices amount
     */
    private void addSetVertices(Iterator<V> iterator, int numOfVertices) {
        for (int i = 0; i < numOfVertices && iterator.hasNext(); i++) {
            verticesQueue.add(iterator.next());
        }
    }

    /**
     * Adds all remaining vertices to the {@link #verticesQueue}
     * provided by the {@code iterator}.
     *
     * @param iterator vertices iterator
     */
    private void addSetRemaining(Iterator<V> iterator) {
        while (iterator.hasNext()) {
            verticesQueue.add(iterator.next());
        }
    }

    /**
     * Adds {@code numOfVertices} vertices to the {@link #verticesQueue}
     * that are contained in the sets provided by the {@code setIterator}.
     * Returns iterator of the set which vertex was added last.
     *
     * @param setIterator   sets of vertices iterator
     * @param numOfVertices vertices amount
     * @return iterator of the last set
     */
    private Iterator<V> addSetsVertices(Iterator<Set<V>> setIterator, int numOfVertices) {
        int i = 0;
        Iterator<V> iterator = null;
        while (setIterator.hasNext() && i < numOfVertices) {
            iterator = setIterator.next().iterator();
            while (iterator.hasNext() && i < numOfVertices) {
                verticesQueue.add(iterator.next());
                i++;
            }
        }
        return iterator;
    }

    /**
     * Adds all remaining vertices to the {@link #verticesQueue}
     * that are contained in the sets provided by the {@code setIterator}.
     *
     * @param setIterator sets of vertices iterator
     */
    private void addSetsRemaining(Iterator<Set<V>> setIterator) {
        while (setIterator.hasNext()) {
            verticesQueue.addAll(setIterator.next());
        }
    }


    /**
     * Submits the {@code task} {@code numOfTasks} times to the {@link #completionService}.
     *
     * @param task       task to be submitted
     * @param numOfTasks amount of times task should be submitted
     */
    private void submitTasks(Runnable task, int numOfTasks) {
        for (int i = 0; i < numOfTasks; i++) {
            completionService.submit(task, null);
        }
    }

    /**
     * Takes {@code numOfTasks} tasks from the {@link #completionService}.
     *
     * @param numOfTasks amount of tasks
     */
    private void waitForTasksCompletion(int numOfTasks) {
        for (int i = 0; i < numOfTasks; i++) {
            try {
                completionService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Performs relaxation in parallel-safe fashion. Synchronises by {@code vertex},
     * then if new tentative distance is less then removes {@code v} from the old bucket,
     * adds is to the new bucket and updates {@link #distanceAndPredecessorMap} value for {@code v}.
     *
     * @param v        vertex
     * @param e        edge to predecessor
     * @param distance distance
     */
    private void relax(V v, E e, double distance) {
        int updatedBucket = bucketIndex(distance);
        synchronized (v) { // to make relaxation updates thread-safe
            Pair<Double, E> oldData = distanceAndPredecessorMap.get(v);
            if (distance < oldData.getFirst()) {
                if (!oldData.getFirst().equals(Double.POSITIVE_INFINITY)) {
                    bucketStructure[bucketIndex(oldData.getFirst())].remove(v);
                }
                bucketStructure[updatedBucket].add(v);
                distanceAndPredecessorMap.put(v, Pair.of(distance, e));
            }
        }
    }

    /**
     * Calculates bucket index for a given {@code distance}.
     *
     * @param distance distance
     * @return bucket index
     */
    private int bucketIndex(double distance) {
        return (int) Math.round(distance / delta) % numOfBuckets;
    }

    /**
     * Replaces the bucket at the {@code bucketIndex} index with a new instance of the {@link ConcurrentSkipListSet}.
     * Return the reference to the set that was previously in the bucket.
     *
     * @param bucketIndex bucket index
     * @return content of the bucket
     */
    private Set getContentAndReplace(int bucketIndex) {
        Set result = bucketStructure[bucketIndex];
        bucketStructure[bucketIndex] = new ConcurrentSkipListSet<V>();
        return result;
    }

    /**
     * Task that is submitted to the {@link #completionService}
     * during shortest path computation for light relax requests relaxation.
     */
    class LightRelaxTask implements Runnable {
        /**
         * Vertices which edges will be relaxed.
         */
        private Queue<V> vertices;

        /**
         * Constructs instance of a new task.
         *
         * @param vertices vertices
         */
        LightRelaxTask(Queue<V> vertices) {
            this.vertices = vertices;
        }

        /**
         * Performs relaxation of edges emanating from {@link #vertices}.
         */
        @Override
        public void run() {

            while (true) {
                V v = vertices.poll();
                if (v == null) { // we might have a termination situation
                    if (allVerticesAdded && vertices.isEmpty()) { // need to check
                        // is the queue is empty, because some vertices might have been added
                        // while passing from first if condition to the second
                        break;
                    }
                } else {
                    for (E e : graph.outgoingEdgesOf(v)) {
                        if (graph.getEdgeWeight(e) <= delta) {
                            relax(Graphs.getOppositeVertex(graph, e, v), e, distanceAndPredecessorMap.get(v).getFirst() + graph.getEdgeWeight(e));
                        }
                    }
                }
            }
        }
    }

    /**
     * Task that is submitted to the {@link #completionService}
     * during shortest path computation for heavy relax requests relaxation.
     */
    class HeavyRelaxTask implements Runnable {
        /**
         * Vertices which edges will be relaxed.
         */
        private Queue<V> vertices;

        /**
         * Constructs instance of a new task.
         *
         * @param vertices vertices
         */
        HeavyRelaxTask(Queue<V> vertices) {
            this.vertices = vertices;
        }

        /**
         * Performs relaxation of edges emanating from {@link #vertices}.
         */
        @Override
        public void run() {

            while (true) {
                V v = vertices.poll();
                if (v == null) {
                    if (allVerticesAdded && vertices.isEmpty()) {
                        break;
                    }
                } else {
                    for (E e : graph.outgoingEdgesOf(v)) {
                        if (graph.getEdgeWeight(e) > delta) {
                            relax(Graphs.getOppositeVertex(graph, e, v), e, distanceAndPredecessorMap.get(v).getFirst() + graph.getEdgeWeight(e));
                        }
                    }
                }
            }
        }
    }
}
