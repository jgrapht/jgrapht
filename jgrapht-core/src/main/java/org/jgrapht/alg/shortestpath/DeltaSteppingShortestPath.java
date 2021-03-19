/*
 * (C) Copyright 2018-2021, by Semen Chudakov and Contributors.
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
import org.jgrapht.util.*;
import org.jgrapht.alg.util.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Parallel implementation of a single-source shortest path algorithm: the delta-stepping algorithm.
 * The algorithm computes single source shortest paths in a graphs with non-negative edge weights.
 * When using multiple threads, this implementation typically outperforms
 * {@link DijkstraShortestPath} and {@link BellmanFordShortestPath}.
 * <p>
 * The delta-stepping algorithm is described in the paper: U. Meyer, P. Sanders, $\Delta$-stepping:
 * a parallelizable shortest path algorithm, Journal of Algorithms, Volume 49, Issue 1, 2003, Pages
 * 114-152, ISSN 0196-6774.
 * <p>
 * The $\Delta$-stepping algorithm takes as input a weighted graph $G(V,E)$, a source node $s$ and a
 * parameter $\Delta &#62; 0$. Let $tent[v]$ be the best known shortest distance from $s$ to vertex
 * $v\in V$. At the start of the algorithm, $tent[s]=0$, $tent[v]=\infty$ for $v\in V\setminus
 * \{s\}$. The algorithm partitions vertices in a series of buckets $B=(B_0, B_1, B_2, \dots)$,
 * where a vertex $v\in V$ is placed in bucket $B_{\lfloor\frac{tent[v]}{\Delta}\rfloor}$. During
 * the execution of the algorithm, vertices in bucket $B_i$, for $i=0,1,2,\dots$, are removed
 * one-by-one. For each removed vertex $v$, and for all its outgoing edges $(v,w)$, the algorithm
 * checks whether $tent[v]+c(v,w) &#60; tent[w]$. If so, $w$ is removed from its current bucket,
 * $tent[w]$ is updated ($tent[w]=tent[v]+c(v,w)$), and $w$ is placed into bucket
 * $B_{\lfloor\frac{tent[w]}{\Delta}\rfloor}$. Parallelism is achieved by processing all vertices
 * belonging to the same bucket concurrently. The algorithm terminates when all buckets are empty.
 * At this stage the array $tent$ contains the minimal cost from $s$ to every vertex $v \in V$. For
 * a more detailed description of the algorithm, refer to the aforementioned paper.
 *
 * <p>
 * For a given graph $G(V,E)$ and parameter $\Delta$, let a $\Delta$-path be a path of total weight
 * at most $\Delta$ with no repeated edges. The time complexity of the algorithm is $O(\frac{(|V| +
 * |E| + n_{\Delta} + m_{\Delta})}{p} + \frac{L}{\Delta}\cdot d\cdot l_{\Delta}\cdot \log n)$, where
 * <ul>
 * <li>$n_{\Delta}$ - number of vertex pairs $(u,v)$, where $u$ and $v$ are connected by some
 * $\Delta$-path.</li>
 * <li>$m_{\Delta}$ - number of vertex triples $(u,v^{\prime},v)$, where $u$ and $v^{\prime}$ are
 * connected by some $\Delta$-path and edge $(v^{\prime},v)$ has weight at most $\Delta$.</li>
 * <li>$L$ - maximum weight of a shortest path from selected source to any sink.</li>
 * <li>$d$ - maximum vertex degree.</li>
 * <li>$l_{\Delta}$ - maximum number of edges in a $\Delta$-path $+1$.</li>
 * </ul>
 *
 * <p>
 * For parallelization, this implementation relies on the {@link ThreadPoolExecutor} which is
 * supplied to this algorithm from outside.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @since January 2018
 */
public class DeltaSteppingShortestPath<V, E>
    extends
    BaseShortestPathAlgorithm<V, E>
{
    /**
     * Error message for reporting the existence of an edge with negative weight.
     */
    private static final String NEGATIVE_EDGE_WEIGHT_NOT_ALLOWED =
        "Negative edge weight not allowed";
    /**
     * Error message for reporting that delta must be positive.
     */
    private static final String DELTA_MUST_BE_NON_NEGATIVE = "Delta must be non-negative";
    /**
     * Default value for {@link #parallelism}.
     */
    private static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();
    /**
     * Empirically computed amount of tasks per worker thread in the {@link ForkJoinPool} that
     * yields good performance.
     */
    private static final int TASKS_TO_THREADS_RATIO = 20;

    /**
     * The bucket width. A bucket with index $i$ therefore stores a vertex v if and only if v is
     * queued and tentative distance to v $\in[i\cdot\Delta,(i+1)\cdot\Delta]$
     */
    private double delta;
    /**
     * Maximum number of threads used in the computations.
     */
    private int parallelism;

    /**
     * Number of buckets in the bucket structure.
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
    private List<Set<V>> bucketStructure;

    /**
     * Decorator for {@link ThreadPoolExecutor} supplied to this algorithm that enables to keep
     * track of when all submitted tasks are finished.
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
     * Indicates when all the vertices are been added to the {@link #verticesQueue} on each
     * iteration.
     */
    private volatile boolean allVerticesAdded;

    /**
     * Constructs a new instance of the algorithm for a given graph.
     *
     * @param graph graph
     * @deprecated replaced with {@link #DeltaSteppingShortestPath(Graph, ThreadPoolExecutor)}
     */
    @Deprecated
    public DeltaSteppingShortestPath(Graph<V, E> graph)
    {
        this(graph, DEFAULT_PARALLELISM);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph and {@code executor}. It is up
     * to a user of this algorithm to handle the creation and termination of the provided
     * {@code executor}. For utility methods to manage a {@code ThreadPoolExecutor} see
     * {@link ConcurrencyUtil}.
     *
     * @param graph graph
     * @param executor executor which will be used for parallelization
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, ThreadPoolExecutor executor)
    {
        this(graph, 0.0, executor);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, delta.
     *
     * @param graph the graph
     * @param delta bucket width
     * @deprecated replaced with
     *             {@link #DeltaSteppingShortestPath(Graph, double, ThreadPoolExecutor)}
     */
    @Deprecated
    public DeltaSteppingShortestPath(Graph<V, E> graph, double delta)
    {
        this(graph, delta, DEFAULT_PARALLELISM);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, delta and {@code executor}. It
     * is up to a user of this algorithm to handle the creation and termination of the provided
     * {@code executor}. For utility methods to manage a {@code ThreadPoolExecutor} see
     * {@link ConcurrencyUtil}.
     *
     * @param graph the graph
     * @param delta bucket width
     * @param executor executor which will be used for parallelization
     */
    public DeltaSteppingShortestPath(Graph<V, E> graph, double delta, ThreadPoolExecutor executor)
    {
        super(graph);
        init(graph, delta, executor);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, parallelism.
     *
     * @param graph the graph
     * @param parallelism maximum number of threads used in the computations
     * @deprecated replaced with {@link #DeltaSteppingShortestPath(Graph, ThreadPoolExecutor)}
     */
    @Deprecated
    public DeltaSteppingShortestPath(Graph<V, E> graph, int parallelism)
    {
        this(graph, 0.0, parallelism);
    }

    /**
     * Constructs a new instance of the algorithm for a given graph, delta, parallelism. If delta is
     * $0.0$ it will be computed during the algorithm execution. In general if the value of
     * $\frac{maximum edge weight}{maximum outdegree}$ is known beforehand, it is preferable to
     * specify it via this constructor, because processing the whole graph to compute this value may
     * significantly slow down the algorithm.
     *
     * @param graph the graph
     * @param delta bucket width
     * @param parallelism maximum number of threads used in the computations
     * @deprecated replaced with
     *             {@link #DeltaSteppingShortestPath(Graph, double, ThreadPoolExecutor)}
     */
    @Deprecated
    public DeltaSteppingShortestPath(Graph<V, E> graph, double delta, int parallelism)
    {
        super(graph);
        init(graph, delta, ConcurrencyUtil.createThreadPoolExecutor(parallelism));
    }

    /**
     * Initializes {@code delta}, {@code parallelism}, {@code distanceAndPredecessorMap},
     * {@code completionService}, {@code verticesQueue}, {@code lightRelaxTask} and
     * {@code heavyRelaxTask} fields.
     *
     * @param graph a graph
     * @param delta bucket width
     * @param executor executor which will be used for parallelization
     */
    private void init(Graph<V, E> graph, double delta, ThreadPoolExecutor executor)
    {
        if (delta < 0) {
            throw new IllegalArgumentException(DELTA_MUST_BE_NON_NEGATIVE);
        }
        this.delta = delta;
        this.parallelism = executor.getMaximumPoolSize();
        distanceAndPredecessorMap = new ConcurrentHashMap<>(graph.vertexSet().size());
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
    private double getMaxEdgeWeight()
    {
        ForkJoinTask<Double> task = ForkJoinPool
            .commonPool().submit(
                new MaxEdgeWeightTask(
                    graph.edgeSet().spliterator(),
                    graph.edgeSet().size() / (TASKS_TO_THREADS_RATIO * parallelism) + 1));
        return task.join();
    }

    /**
     * Is used during the algorithm to compute maximum edge weight of the {@link #graph}. Apart from
     * computing the maximal edge weight in the graph the task also checks if there exist edges with
     * negative weights.
     */
    class MaxEdgeWeightTask
        extends
        RecursiveTask<Double>
    {
        /**
         * Is used to split a collection and create new recursive tasks during the computation.
         */
        Spliterator<E> spliterator;
        /**
         * Amount of edges which are processed in parallel.
         */
        long loadBalancing;

        /**
         * Constructs a new instance for the given spliterator and loadBalancing
         *
         * @param spliterator spliterator
         * @param loadBalancing loadBalancing
         */
        MaxEdgeWeightTask(Spliterator<E> spliterator, long loadBalancing)
        {
            this.spliterator = spliterator;
            this.loadBalancing = loadBalancing;
        }

        /**
         * Computes maximum edge weight. If amount of edges in {@link #spliterator} is less than
         * {@link #loadBalancing}, then computation is performed sequentially. If not, the
         * {@link #spliterator} is used to split the collection and then two new child tasks are
         * created.
         *
         * @return max edge weight
         */
        @Override
        protected Double compute()
        {
            if (spliterator.estimateSize() <= loadBalancing) {
                double[] max = { 0 };
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
    public GraphPath<V, E> getPath(V source, V sink)
    {
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
    public SingleSourcePaths<V, E> getPaths(V source)
    {
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException(GRAPH_MUST_CONTAIN_THE_SOURCE_VERTEX);
        }
        maxEdgeWeight = getMaxEdgeWeight();
        if (delta == 0.0) { // the value should be computed
            delta = findDelta();
        }
        numOfBuckets = (int) (Math.ceil(maxEdgeWeight / delta) + 1);
        bucketStructure = new ArrayList<>(numOfBuckets);
        for (int i = 0; i < numOfBuckets; i++) {
            bucketStructure.add(new ConcurrentSkipListSet<>());
        }
        fillDistanceAndPredecessorMap();

        computeShortestPaths(source);

        return new TreeSingleSourcePathsImpl<>(graph, source, distanceAndPredecessorMap);
    }

    /**
     * Calculates value of {@link #delta}. The value is calculated as the maximal edge weight
     * divided by maximal out-degree in the {@link #graph} or $1.0$ if edge set of the
     * {@link #graph} is empty.
     *
     * @return bucket width
     */
    private double findDelta()
    {
        if (maxEdgeWeight == 0) {
            return 1.0;
        } else {
            int maxOutDegree =
                graph.vertexSet().parallelStream().mapToInt(graph::outDegreeOf).max().orElse(0);
            return maxEdgeWeight / maxOutDegree;
        }
    }

    /**
     * Fills {@link #distanceAndPredecessorMap} concurrently.
     */
    private void fillDistanceAndPredecessorMap()
    {
        graph
            .vertexSet().parallelStream().forEach(
                v -> distanceAndPredecessorMap.put(v, Pair.of(Double.POSITIVE_INFINITY, null)));
    }

    /**
     * Performs shortest path computations.
     *
     * @param source the source vertex
     */
    private void computeShortestPaths(V source)
    {
        relax(source, null, 0.0);

        List<Set<V>> removed = new ArrayList<>();
        while (true) {
            int firstNonEmptyBucket = 0;
            while (firstNonEmptyBucket < numOfBuckets
                && bucketStructure.get(firstNonEmptyBucket).isEmpty())
            { // skip empty buckets
                ++firstNonEmptyBucket;
            }
            if (firstNonEmptyBucket == numOfBuckets) { // terminate if all buckets are empty
                break;
            }
            // the content of a bucket is replaced
            // in order not to handle the same vertices
            // multiple times
            Set<V> bucketElements = getContentAndReplace(firstNonEmptyBucket);

            while (!bucketElements.isEmpty()) { // reinsertions may occur
                removed.add(bucketElements);
                findAndRelaxLightRequests(bucketElements);
                bucketElements = getContentAndReplace(firstNonEmptyBucket);
            }

            findAndRelaxHeavyRequests(removed);
            removed.clear();
        }
    }

    /**
     * Manages edge relaxations. Adds all elements from {@code vertices} to the
     * {@link #verticesQueue} and submits as many {@link #lightRelaxTask} to the
     * {@link #completionService} as needed.
     *
     * @param vertices vertices
     */
    private void findAndRelaxLightRequests(Set<V> vertices)
    {
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
     * Manages execution of edges relaxation. Adds all elements from {@code vertices} to the
     * {@link #verticesQueue} and submits as many {@link #heavyRelaxTask} to the
     * {@link #completionService} as needed.
     *
     * @param verticesSets set of sets of vertices
     */
    private void findAndRelaxHeavyRequests(List<Set<V>> verticesSets)
    {
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
     * Adds {@code numOfVertices} vertices to the {@link #verticesQueue} provided by the
     * {@code iterator}.
     *
     * @param iterator vertices iterator
     * @param numOfVertices vertices amount
     */
    private void addSetVertices(Iterator<V> iterator, int numOfVertices)
    {
        for (int i = 0; i < numOfVertices && iterator.hasNext(); i++) {
            verticesQueue.add(iterator.next());
        }
    }

    /**
     * Adds all remaining vertices to the {@link #verticesQueue} provided by the {@code iterator}.
     *
     * @param iterator vertices iterator
     */
    private void addSetRemaining(Iterator<V> iterator)
    {
        while (iterator.hasNext()) {
            verticesQueue.add(iterator.next());
        }
    }

    /**
     * Adds {@code numOfVertices} vertices to the {@link #verticesQueue} that are contained in the
     * sets provided by the {@code setIterator}. Returns iterator of the set which vertex was added
     * last.
     *
     * @param setIterator sets of vertices iterator
     * @param numOfVertices vertices amount
     * @return iterator of the last set
     */
    private Iterator<V> addSetsVertices(Iterator<Set<V>> setIterator, int numOfVertices)
    {
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
     * Adds all remaining vertices to the {@link #verticesQueue} that are contained in the sets
     * provided by the {@code setIterator}.
     *
     * @param setIterator sets of vertices iterator
     */
    private void addSetsRemaining(Iterator<Set<V>> setIterator)
    {
        while (setIterator.hasNext()) {
            verticesQueue.addAll(setIterator.next());
        }
    }

    /**
     * Submits the {@code task} {@code numOfTasks} times to the {@link #completionService}.
     *
     * @param task task to be submitted
     * @param numOfTasks amount of times task should be submitted
     */
    private void submitTasks(Runnable task, int numOfTasks)
    {
        for (int i = 0; i < numOfTasks; i++) {
            completionService.submit(task, null);
        }
    }

    /**
     * Takes {@code numOfTasks} tasks from the {@link #completionService}.
     *
     * @param numOfTasks amount of tasks
     */
    private void waitForTasksCompletion(int numOfTasks)
    {
        for (int i = 0; i < numOfTasks; i++) {
            try {
                completionService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Performs relaxation in parallel-safe fashion. Synchronises by {@code vertex}, then if new
     * tentative distance is less then removes {@code v} from the old bucket, adds is to the new
     * bucket and updates {@link #distanceAndPredecessorMap} value for {@code v}.
     *
     * @param v vertex
     * @param e edge to predecessor
     * @param distance distance
     */
    private void relax(V v, E e, double distance)
    {
        int updatedBucket = bucketIndex(distance);
        synchronized (v) { // to make relaxation updates thread-safe
            Pair<Double, E> oldData = distanceAndPredecessorMap.get(v);
            if (distance < oldData.getFirst()) {
                if (!oldData.getFirst().equals(Double.POSITIVE_INFINITY)) {
                    bucketStructure.get(bucketIndex(oldData.getFirst())).remove(v);
                }
                bucketStructure.get(updatedBucket).add(v);
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
    private int bucketIndex(double distance)
    {
        return (int) Math.round(distance / delta) % numOfBuckets;
    }

    /**
     * Replaces the bucket at the {@code bucketIndex} index with a new instance of the
     * {@link ConcurrentSkipListSet}. Return the reference to the set that was previously in the
     * bucket.
     *
     * @param bucketIndex bucket index
     * @return content of the bucket
     */
    private Set<V> getContentAndReplace(int bucketIndex)
    {
        Set<V> result = bucketStructure.get(bucketIndex);
        bucketStructure.set(bucketIndex, new ConcurrentSkipListSet<V>());
        return result;
    }

    /**
     * Task that is submitted to the {@link #completionService} during shortest path computation for
     * light relax requests relaxation.
     */
    class LightRelaxTask
        implements
        Runnable
    {
        /**
         * Vertices which edges will be relaxed.
         */
        private Queue<V> vertices;

        /**
         * Constructs instance of a new task.
         *
         * @param vertices vertices
         */
        LightRelaxTask(Queue<V> vertices)
        {
            this.vertices = vertices;
        }

        /**
         * Performs relaxation of edges emanating from {@link #vertices}.
         */
        @Override
        public void run()
        {

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
                            relax(
                                Graphs.getOppositeVertex(graph, e, v), e,
                                distanceAndPredecessorMap.get(v).getFirst()
                                    + graph.getEdgeWeight(e));
                        }
                    }
                }
            }
        }
    }

    /**
     * Task that is submitted to the {@link #completionService} during shortest path computation for
     * heavy relax requests relaxation.
     */
    class HeavyRelaxTask
        implements
        Runnable
    {
        /**
         * Vertices which edges will be relaxed.
         */
        private Queue<V> vertices;

        /**
         * Constructs instance of a new task.
         *
         * @param vertices vertices
         */
        HeavyRelaxTask(Queue<V> vertices)
        {
            this.vertices = vertices;
        }

        /**
         * Performs relaxation of edges emanating from {@link #vertices}.
         */
        @Override
        public void run()
        {

            while (true) {
                V v = vertices.poll();
                if (v == null) {
                    if (allVerticesAdded && vertices.isEmpty()) {
                        break;
                    }
                } else {
                    for (E e : graph.outgoingEdgesOf(v)) {
                        if (graph.getEdgeWeight(e) > delta) {
                            relax(
                                Graphs.getOppositeVertex(graph, e, v), e,
                                distanceAndPredecessorMap.get(v).getFirst()
                                    + graph.getEdgeWeight(e));
                        }
                    }
                }
            }
        }
    }
}
