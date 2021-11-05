/*
 * (C) Copyright 2020-2021, by Semen Chudakov and Contributors.
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
import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.util.CollectionUtil;
import org.jgrapht.util.ConcurrencyUtil;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.PairingHeap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths;
import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.ContractionEdge;
import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.ContractionHierarchy;
import static org.jgrapht.alg.shortestpath.ContractionHierarchyPrecomputation.ContractionVertex;
import static org.jgrapht.alg.shortestpath.DefaultManyToManyShortestPaths.DefaultManyToManyShortestPathsImpl;

/**
 * Parallel implementation of the <a href="https://en.wikipedia.org/wiki/Transit_node_routing">
 * transit node routing route planning precomputation technique</a>.
 *
 * <p>
 * This implementation is based on the {@link ContractionHierarchyPrecomputation}.
 *
 * <p>
 * The precomputation algorithm is described in the article: Arz, Julian &amp; Luxen, Dennis &amp;
 * Sanders, Peter. (2013). Transit Node Routing Reconsidered. 7933. 10.1007/978-3-642-38527-8_7.
 *
 * <p>
 * As mentioned is the original paper, TNR in itself is not a complete algorithm, but a framework
 * which is used to speed up shortest paths computations. Formally the framework consists of the
 * following parts:
 * <p>
 * <ul style="list-style-type:circle;">
 * <li>set $T ⊆ V$ of transit vertices;</li>
 * <li>distance table $D_{T} : T × T → {\rm I\!R}^{+}_{0}$ of shortest path distances between the
 * transit vertices;</li>
 * <li>forward (backward) access vertex mapping $A^{↑} : V → 2^{T}$ ($A^{↓} : V → 2^{T}$). For any
 * shortest $s$–$t$-path $P$ containing transit vertices, $A^{↑}(s)$ ($A^{↓}(t)$) must contain the
 * first (last) transit vertex on $P$;</li>
 * <li>locality filter $L : V × V → \{true, false\}$. $L(s, t)$ must be $true$ when no shortest path
 * between $s$ and $t$ contains a transit vertex. False positives are allowed, i.e., $L(s, t)$ may
 * sometimes be $true$ even when a shortest path contains a transit vertex.</li>
 * </ul>
 *
 * <p>
 * To implement the TNR framework means to define how to select transit vertices and how to compute
 * distance table $D_{T}$, access vertices and locality filter. This implementation selects transit
 * vertices to be to $k$ vertices form the contraction hierarchy. For the details of how other parts
 * of this TNR work please refer to the original paper.
 *
 * <p>
 * For parallelization, this implementation relies on the {@link ThreadPoolExecutor} which is
 * supplied to this algorithm from outside.
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 * @author Semen Chudakov
 * @see ContractionHierarchyPrecomputation
 */
class TransitNodeRoutingPrecomputation<V, E>
{
    /**
     * Special Voronoi diagram cell id to indicate, that a vertex does not belong to any cells. For
     * usual Voronoi cell the ids of contracted vertices are used. Once those ids are non-negative,
     * this value is guaranteed to be unique.
     */
    private static final int NO_VORONOI_CELL = -1;

    /**
     * Contraction hierarchy which is used to compute transit node routing.
     */
    private ContractionHierarchy<V, E> contractionHierarchy;
    /**
     * Contracted graph.
     */
    private Graph<ContractionVertex<V>, ContractionEdge<E>> contractionGraph;
    /**
     * Mapping of vertices in the initial graph to contracted vertices.
     */
    private Map<V, ContractionVertex<V>> contractionMapping;
    /**
     * Number of transit vertices in the graph.
     */
    private int numberOfTransitVertices;
    /**
     * Maximum number of threads used in the computations.
     */
    private int parallelism;
    /**
     * Supplier for the preferable heap implementation. Provided heap is used to build Voronoi
     * diagram.
     */
    private Supplier<AddressableHeap<Double, ContractionVertex<V>>> heapSupplier;

    /**
     * List of contracted vertices. It is used to evenly distribute work between threads in the
     * parallel computations.
     */
    private List<ContractionVertex<V>> contractionVertices;
    /**
     * Algorithm which is used to compute many-to-many shortest paths between transit vertices.
     */
    private ManyToManyShortestPathsAlgorithm<V, E> manyToManyShortestPathsAlgorithm;

    /**
     * Set of contracted transit vertices.
     */
    private Set<ContractionVertex<V>> contractedTransitVerticesSet;
    /**
     * Set of transit vertices.
     */
    private Set<V> transitVerticesSet;
    /**
     * List of transit vertices.
     */
    private List<V> transitVerticesList;

    /**
     * Voronoi diagram for the contraction graph. Here the transit vertices are used as cells
     * centers.
     */
    private VoronoiDiagram<V> voronoiDiagram;
    /**
     * Many-to-many shortest paths between transit vertices.
     */
    private ManyToManyShortestPaths<V, E> transitVerticesPaths;

    /**
     * Executor to which parallel computation tasks are submitted.
     */
    private ExecutorService executor;
    /**
     * Decorator for {@code executor} that allows to keep track of when all submitted tasks are
     * finished.
     */
    private ExecutorCompletionService<Void> completionService;

    /**
     * Constructs an instance of the algorithm for a given {@code graph} and {@code executor}. It is
     * up to a user of this algorithm to handle the creation and termination of the provided
     * {@code executor}. Utility methods to manage a {@code ThreadPoolExecutor} see
     * {@link org.jgrapht.util.ConcurrencyUtil}.
     *
     * @param graph graph
     * @param executor executor which will be used for parallelization
     */
    public TransitNodeRoutingPrecomputation(Graph<V, E> graph, ThreadPoolExecutor executor)
    {
        this(
            new ContractionHierarchyPrecomputation<>(graph, executor).computeContractionHierarchy(),
            executor);
    }

    /**
     * Constructs an instance of the algorithm for the given {@code contractionHierarchy} and
     * {@code executor}. It is up to a user of this algorithm to handle the creation and termination
     * of the provided {@code executor}. Utility methods to manage a {@code ThreadPoolExecutor} see
     * {@link org.jgrapht.util.ConcurrencyUtil}.
     *
     * @param hierarchy contraction hierarchy
     * @param executor executor which will be used for parallelization
     */
    public TransitNodeRoutingPrecomputation(
        ContractionHierarchy<V, E> hierarchy, ThreadPoolExecutor executor)
    {
        this(hierarchy, (int) Math.sqrt(hierarchy.getGraph().vertexSet().size()), executor);
    }

    /**
     * Constructs an instance of the algorithm for a given {@code contractionHierarchy},
     * {@code numberOfTransitVertices} and {@code executor}. It is up to a user of this algorithm to
     * handle the creation and termination of the provided {@code executor}. Utility methods to
     * manage a {@code ThreadPoolExecutor} see {@link org.jgrapht.util.ConcurrencyUtil}.
     *
     * @param hierarchy contraction hierarchy
     * @param numberOfTransitVertices number of transit vertices
     * @param executor executor which will be used for parallelization
     */
    public TransitNodeRoutingPrecomputation(
        ContractionHierarchy<V, E> hierarchy, int numberOfTransitVertices,
        ThreadPoolExecutor executor)
    {
        this(hierarchy, numberOfTransitVertices, PairingHeap::new, executor);
    }

    /**
     * Constructs an instance of the algorithm for a given {@code contractionHierarchy},
     * {@code parallelism}, {@code numberOfTransitVertices}, {@code heapSupplier} and
     * {@code executor}. Heap provided by the {@code heapSupplier} is used which computing the
     * Voronoi diagram. It is up to a user of this algorithm to handle the creation and termination
     * of the provided {@code executor}. Utility methods to manage a {@code ThreadPoolExecutor} see
     * {@link org.jgrapht.util.ConcurrencyUtil}.
     *
     * @param hierarchy contraction hierarchy
     * @param numberOfTransitVertices number of transit vertices
     * @param heapSupplier supplier for preferable heap implementation
     * @param executor executor which will be used for parallelization
     */
    public TransitNodeRoutingPrecomputation(
        ContractionHierarchy<V, E> hierarchy, int numberOfTransitVertices,
        Supplier<AddressableHeap<Double, ContractionVertex<V>>> heapSupplier,
        ThreadPoolExecutor executor)
    {
        if (numberOfTransitVertices > hierarchy.getGraph().vertexSet().size()) {
            throw new IllegalArgumentException(
                "number of transit vertices is larger than the number of vertices in the graph");
        }
        this.contractionHierarchy = hierarchy;
        this.contractionGraph = hierarchy.getContractionGraph();
        this.contractionMapping = hierarchy.getContractionMapping();
        this.numberOfTransitVertices = numberOfTransitVertices;
        this.parallelism = executor.getMaximumPoolSize();
        this.heapSupplier = heapSupplier;

        this.contractionVertices =
            new ArrayList<>(Collections.nCopies(contractionGraph.vertexSet().size(), null));
        this.manyToManyShortestPathsAlgorithm = new CHManyToManyShortestPaths<>(hierarchy);

        this.executor = executor;
        this.completionService = new ExecutorCompletionService<>(this.executor);
    }

    /**
     * Computes transit node routing based on {@code contractionHierarchy}.
     *
     * @return transit node routing
     */
    public TransitNodeRouting<V, E> computeTransitNodeRouting()
    {
        fillContractionVerticesList();

        contractedTransitVerticesSet = selectTopKTransitVertices(numberOfTransitVertices);
        transitVerticesSet = contractedTransitVerticesSet
            .stream().map(v -> v.vertex).collect(Collectors.toCollection(HashSet::new));
        transitVerticesList = new ArrayList<>(transitVerticesSet);

        VoronoiDiagramComputation voronoiDiagramComputation = new VoronoiDiagramComputation();
        voronoiDiagram = voronoiDiagramComputation.computeVoronoiDiagram();

        ManyToManyShortestPaths<V, E> contractedPaths = manyToManyShortestPathsAlgorithm
            .getManyToManyPaths(transitVerticesSet, transitVerticesSet);
        transitVerticesPaths = unpackPaths(contractedPaths);

        Pair<AccessVertices<V, E>, LocalityFilter<V>> avAndLf = computeAVAndLF();

        return new TransitNodeRouting<>(
            contractionHierarchy, contractedTransitVerticesSet, transitVerticesPaths,
            voronoiDiagram, avAndLf.getFirst(), avAndLf.getSecond());
    }

    /**
     * Fills {@code contractionVertices} with vertices from {@code contractionGraph}. For each
     * vertex its position in the list is equal to its {@code id}.
     */
    private void fillContractionVerticesList()
    {
        for (ContractionVertex<V> v : contractionGraph.vertexSet()) {
            contractionVertices.set(v.vertexId, v);
        }
    }

    /**
     * Unpacks in parallel contracted paths stored in {@code shortestPaths}. Unpacked path are
     * returned as {@link DefaultManyToManyShortestPathsImpl}.
     *
     * @param shortestPaths contracted many-to-many shortest paths
     * @return unpacked paths
     */
    private ManyToManyShortestPaths<V, E> unpackPaths(ManyToManyShortestPaths<V, E> shortestPaths)
    {
        Map<V, Map<V, GraphPath<V, E>>> pathsMap =
            CollectionUtil.newHashMapWithExpectedSize(numberOfTransitVertices);
        for (V v : transitVerticesList) {
            pathsMap.put(v, CollectionUtil.newHashMapWithExpectedSize(numberOfTransitVertices));
        }

        for (int taskId = 0; taskId < parallelism; ++taskId) {
            PathsUnpackingTask task =
                new PathsUnpackingTask(taskId, transitVerticesList, pathsMap, shortestPaths);
            completionService.submit(task, null);
        }
        waitForTasksCompletion(parallelism);

        return new DefaultManyToManyShortestPathsImpl<>(
            transitVerticesSet, transitVerticesSet, pathsMap);
    }

    /**
     * Selects top {@code numberOfTransitVertices} vertices in the contraction hierarchy as transit
     * vertices.
     *
     * @param numberOfTransitVertices number of transit vertices to select
     * @return set of transit vertices
     */
    private Set<ContractionVertex<V>> selectTopKTransitVertices(int numberOfTransitVertices)
    {
        int numberOfVertices = contractionGraph.vertexSet().size();
        Set<ContractionVertex<V>> result =
            CollectionUtil.newHashSetWithExpectedSize(numberOfTransitVertices);
        for (ContractionVertex<V> vertex : contractionGraph.vertexSet()) {
            if (vertex.contractionLevel >= numberOfVertices - numberOfTransitVertices) {
                result.add(vertex);
            }
        }
        return result;
    }

    /**
     * Computes in parallel access vertices and locality filter for the transit node routing.
     *
     * @return pair of access vertices and locality filter.
     */
    private Pair<AccessVertices<V, E>, LocalityFilter<V>> computeAVAndLF()
    {
        LocalityFilterBuilder localityFilterBuilder =
            new LocalityFilterBuilder(contractionGraph.vertexSet().size());

        AccessVerticesBuilder accessVerticesBuilder =
            new AccessVerticesBuilder(contractionGraph.vertexSet().size());

        ContractionHierarchyBFS forwardBFS = new ContractionHierarchyBFS(
            new MaskSubgraph<>(contractionGraph, v -> false, e -> !e.isUpward));
        ContractionHierarchyBFS backwardBFS = new ContractionHierarchyBFS(
            new MaskSubgraph<>(
                new EdgeReversedGraph<>(contractionGraph), v -> false, e -> e.isUpward));

        for (int taskId = 0; taskId < parallelism; ++taskId) {
            AVAndLFConstructionTask task = new AVAndLFConstructionTask(
                taskId, localityFilterBuilder, accessVerticesBuilder, forwardBFS, backwardBFS);
            completionService.submit(task, null);
        }
        waitForTasksCompletion(parallelism);

        return Pair
            .of(accessVerticesBuilder.buildVertices(), localityFilterBuilder.buildLocalityFilter());
    }

    /**
     * Takes {@code numberOfTasks} tasks from the {@code completionService}.
     *
     * @param numberOfTasks number of tasks
     */
    private void waitForTasksCompletion(int numberOfTasks)
    {
        for (int i = 0; i < numberOfTasks; ++i) {
            try {
                completionService.take().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Algorithm which computes Voronoi diagram for the {@code contractionGraph}. It uses
     * {@code transitVertices} as Voronoi cells centers. To build the diagram runs a Dijkstra`s
     * algorithm with multiple sources on a reversed {@code graph}. Uses Voronoi cells centers as
     * initial sources. During the computations for each vertex maintains distance to the closest
     * cell center as well as the id if this cell center.
     */
    private class VoronoiDiagramComputation
    {
        /**
         * Priority queue which stores vertices ordered by theirs distances to the corresponding
         * Voronoi cell center.
         */
        private AddressableHeap<Double, ContractionVertex<V>> heap;
        /**
         * For every vertex added to the {@code heap} stores a corresponding handle.
         */
        private Map<ContractionVertex<V>,
            AddressableHeap.Handle<Double, ContractionVertex<V>>> seen;

        /**
         * For every vertex stores an id of a corresponding Voronoi cell center.
         */
        private int[] voronoiCells;
        /**
         * For every vertex stores distance to the closest Voronoi cell center.
         */
        private double[] distanceToCenter;

        /**
         * Constructs a new instance of the algorithm.
         */
        VoronoiDiagramComputation()
        {
            this.heap = heapSupplier.get();
            this.seen = new HashMap<>();
        }

        /**
         * Computes Voronoi diagram for {@code graph}.
         *
         * @return Voronoi diagram
         */
        VoronoiDiagram<V> computeVoronoiDiagram()
        {
            int numberOfVertices = contractionGraph.vertexSet().size();
            voronoiCells = new int[numberOfVertices];
            distanceToCenter = new double[numberOfVertices];
            Arrays.fill(voronoiCells, NO_VORONOI_CELL);
            Arrays.fill(distanceToCenter, Double.POSITIVE_INFINITY);

            // mask all shortcuts in the contraction graph
            Graph<ContractionVertex<V>, ContractionEdge<E>> searchGraph = new EdgeReversedGraph<>(
                new MaskSubgraph<>(contractionGraph, v -> false, e -> e.edge == null));

            for (ContractionVertex<V> transitVertex : contractedTransitVerticesSet) {
                updateDistance(transitVertex, transitVertex, 0.0);
            }

            while (!heap.isEmpty()) {
                AddressableHeap.Handle<Double, ContractionVertex<V>> entry = heap.deleteMin();

                double distance = entry.getKey();
                ContractionVertex<V> v = entry.getValue();

                for (ContractionEdge<E> edge : searchGraph.outgoingEdgesOf(v)) {
                    ContractionVertex<V> successor = Graphs.getOppositeVertex(searchGraph, edge, v);

                    double updatedDistance = distance + searchGraph.getEdgeWeight(edge);
                    if (updatedDistance < distanceToCenter[successor.vertexId]) {
                        updateDistance(successor, v, updatedDistance);
                    }
                }
            }

            return new VoronoiDiagram<>(voronoiCells);
        }

        /**
         * If necessary updates distance of the {@code vertex} in the {@code heap}.
         *
         * @param vertex vertex
         * @param predecessor predecessor of {@code vertex} in the shortest paths tree
         * @param distance distance to vertex
         */
        private void updateDistance(
            ContractionVertex<V> vertex, ContractionVertex<V> predecessor, double distance)
        {
            AddressableHeap.Handle<Double, ContractionVertex<V>> handle = seen.get(vertex);
            if (handle == null) {
                handle = heap.insert(distance, vertex);
                seen.put(vertex, handle);
                visitVertex(vertex, predecessor, distance);
            } else if (distance < handle.getKey()) {
                handle.decreaseKey(distance);
                handle.setValue(handle.getValue());
                visitVertex(vertex, predecessor, distance);
            }
        }

        /**
         * If necessary updates Voronoi cell id and distance in {@code voronoiCells} and
         * {@code distanceToCenter} for vertex.
         *
         * @param vertex vertex
         * @param predecessor predecessor of {@code vertex} in the shortest paths tree
         * @param distance distance to vertex
         */
        private void visitVertex(
            ContractionVertex<V> vertex, ContractionVertex<V> predecessor, double distance)
        {
            int updatedVoronoiCell;
            if (vertex.vertexId == predecessor.vertexId) {
                updatedVoronoiCell = vertex.vertexId;
            } else {
                updatedVoronoiCell = this.voronoiCells[predecessor.vertexId];
            }
            this.voronoiCells[vertex.vertexId] = updatedVoronoiCell;
            this.distanceToCenter[vertex.vertexId] = distance;
        }
    }

    /**
     * BFS algorithm which is used to compute access vertices and locality filter. Runs a CH BFS
     * query over contractionGraph. Does not traverse edges leaving transit vertices. Reports all
     * traversed transit vertices as access vertices. For every traversed non-transit vertex reports
     * a corresponding Voronoi cell id. Those ids are then used to construct locality filter.
     */
    private class ContractionHierarchyBFS
    {
        /**
         * Search graph.
         */
        private Graph<ContractionVertex<V>, ContractionEdge<E>> contractionGraph;

        /**
         * Constructs a new instance of the algorithm for the given {@code graph}.
         *
         * @param contractionGraph contraction graph
         */
        public ContractionHierarchyBFS(
            Graph<ContractionVertex<V>, ContractionEdge<E>> contractionGraph)
        {
            this.contractionGraph = contractionGraph;
        }

        /**
         * Runs a forward CH BFS query to calculate access vertices and ids of visited Voronoi
         * cells.
         *
         * @param vertex search starting vertex
         * @return access vertices and visited Voronoi cells ids
         */
        public Pair<Set<V>, Set<Integer>> runSearch(ContractionVertex<V> vertex)
        {
            Set<V> accessVertices = new HashSet<>();
            Set<Integer> visitedVoronoiCells = new HashSet<>();

            Set<Integer> visitedVerticesIds = new HashSet<>();
            Queue<ContractionVertex<V>> queue = new LinkedList<>();
            queue.add(vertex);

            while (!queue.isEmpty()) {
                ContractionVertex<V> v = queue.remove();
                visitedVerticesIds.add(v.vertexId);

                if (contractedTransitVerticesSet.contains(v)) {
                    accessVertices.add(v.vertex);
                } else {
                    visitedVoronoiCells.add(voronoiDiagram.getVoronoiCellId(v));

                    for (ContractionEdge<E> e : contractionGraph.outgoingEdgesOf(v)) {
                        ContractionVertex<V> successor =
                            Graphs.getOppositeVertex(contractionGraph, e, v);
                        if (!visitedVerticesIds.contains(successor.vertexId)) {
                            queue.add(successor);
                        }
                    }
                }
            }

            return Pair.of(accessVertices, visitedVoronoiCells);
        }
    }

    /**
     * Provides API to build an {@code AccessVertices} object.
     */
    private class AccessVerticesBuilder
    {
        /**
         * For every vertex in {@code contractionGraph} stores a list of forward access vertices. Id
         * of a contracted vertex is equal to the index in this list, at which corresponding access
         * vertices are stored.
         */
        private List<List<AccessVertex<V, E>>> forwardAccessVertices;
        /**
         * For every vertex in {@code contractionGraph} stores a list of backward access vertices.
         * Id of a contracted vertex is equal to the index in this list, at which corresponding
         * access vertices are stored.
         */
        private List<List<AccessVertex<V, E>>> backwardAccessVertices;

        /**
         * Constructs an instance for the given {@code numberOfVertices}.
         *
         * @param numberOfVertices number of vertices in a m graph
         */
        public AccessVerticesBuilder(int numberOfVertices)
        {
            this.forwardAccessVertices = new ArrayList<>(numberOfVertices);
            this.backwardAccessVertices = new ArrayList<>(numberOfVertices);
            for (int i = 0; i < numberOfVertices; ++i) {
                forwardAccessVertices.add(new ArrayList<>());
                backwardAccessVertices.add(new ArrayList<>());
            }
        }

        /**
         * Builds a new instance of {@code AccessVertices} using {@code forwardAccessVertices} and
         * {@code backwardAccessVertices}.
         *
         * @return access vertices
         */
        public AccessVertices<V, E> buildVertices()
        {
            return new AccessVertices<>(forwardAccessVertices, backwardAccessVertices);
        }

        /**
         * Computes a list of forward access vertices for {@code v} using {@code vertices} and adds
         * them to the {@code forwardAccessVertices}.
         *
         * @param v vertex
         * @param vertices transit vertices
         */
        public void addForwardAccessVertices(ContractionVertex<V> v, Set<V> vertices)
        {
            ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<V, E> manyToManyShortestPaths =
                manyToManyShortestPathsAlgorithm
                    .getManyToManyPaths(Collections.singleton(v.vertex), vertices);

            Set<V> prunedVertices =
                getPrunedAccessVertices(v.vertex, vertices, manyToManyShortestPaths, true);
            List<AccessVertex<V, E>> accessVerticesList = forwardAccessVertices.get(v.vertexId);
            for (V unpackedVertex : vertices) {
                if (!prunedVertices.contains(unpackedVertex)) {
                    accessVerticesList
                        .add(
                            new AccessVertex<>(
                                unpackedVertex,
                                manyToManyShortestPaths.getPath(v.vertex, unpackedVertex)));
                }
            }
        }

        /**
         * Computes a list of backward access vertices for {@code v} using {@code vertices} and adds
         * them to the {@code backwardAccessVertices}.
         *
         * @param v vertex
         * @param vertices transit vertices
         */
        public void addBackwardAccessVertices(ContractionVertex<V> v, Set<V> vertices)
        {
            ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<V, E> manyToManyShortestPaths =
                manyToManyShortestPathsAlgorithm
                    .getManyToManyPaths(vertices, Collections.singleton(v.vertex));

            Set<V> prunedVertices =
                getPrunedAccessVertices(v.vertex, vertices, manyToManyShortestPaths, false);
            List<AccessVertex<V, E>> accessVerticesList = backwardAccessVertices.get(v.vertexId);
            for (V unpackedVertex : vertices) {
                if (!prunedVertices.contains(unpackedVertex)) {
                    accessVerticesList
                        .add(
                            new AccessVertex<>(
                                unpackedVertex,
                                manyToManyShortestPaths.getPath(unpackedVertex, v.vertex)));
                }
            }
        }

        /**
         * Selects redundant access vertices from {@code vertices}.
         *
         * @param v vertex
         * @param vertices transit vertices
         * @param manyToManyShortestPaths transit vertices paths
         * @param forwardAccessVertices if {@code vertices} are forward access vertices for not wrt
         *        {@code v}
         * @return redundant access vertices
         */
        private Set<V> getPrunedAccessVertices(
            V v, Set<V> vertices, ManyToManyShortestPaths<V, E> manyToManyShortestPaths,
            boolean forwardAccessVertices)
        {
            Set<V> result = new HashSet<>();
            for (V v1 : vertices) {
                if (!result.contains(v1)) {
                    for (V v2 : vertices) {
                        if (!v1.equals(v2) && !result.contains(v2)) {
                            if (forwardAccessVertices) {
                                if (manyToManyShortestPaths.getWeight(v, v1) + transitVerticesPaths
                                    .getWeight(v1, v2) <= manyToManyShortestPaths.getWeight(v, v2))
                                {
                                    result.add(v2);
                                }
                            } else {
                                if (transitVerticesPaths.getWeight(v2, v1) + manyToManyShortestPaths
                                    .getWeight(v1, v) <= manyToManyShortestPaths.getWeight(v2, v))
                                {
                                    result.add(v2);
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }
    }

    /**
     * Provides API to build a {@code LocalityFilter} object.
     */
    private class LocalityFilterBuilder
    {
        /**
         * Visited Voronoi cells by a forward {@code ContractionHierarchyBFS} search.
         */
        private List<Set<Integer>> visitedForwardVoronoiCells;
        /**
         * Visited Voronoi cells by a backward {@code ContractionHierarchyBFS} search.
         */
        private List<Set<Integer>> visitedBackwardVoronoiCells;

        /**
         * Constructs an instance for the given {@code numberOfVertices}.
         *
         * @param numberOfVertices number of vertices in graph
         */
        public LocalityFilterBuilder(int numberOfVertices)
        {
            this.visitedForwardVoronoiCells = new ArrayList<>(numberOfVertices);
            this.visitedBackwardVoronoiCells = new ArrayList<>(numberOfVertices);
            for (int i = 0; i < numberOfVertices; ++i) {
                visitedForwardVoronoiCells.add(null);
                visitedBackwardVoronoiCells.add(null);
            }
        }

        /**
         * Adds {@code visitedVoronoiCells} to this builder in the forward direction for
         * {@code vertex}.
         *
         * @param vertex vertex
         * @param visitedVoronoiCells visited Voronoi cells
         */
        public void addForwardVisitedVoronoiCells(
            ContractionVertex<V> vertex, Set<Integer> visitedVoronoiCells)
        {
            this.visitedForwardVoronoiCells.set(vertex.vertexId, visitedVoronoiCells);
        }

        /**
         * Adds {@code visitedVoronoiCells} to this builder in the backward direction for
         * {@code vertex}.
         *
         * @param vertex vertex
         * @param visitedVoronoiCells visited Voronoi cells
         */
        public void addBackwardVisitedVoronoiCells(
            ContractionVertex<V> vertex, Set<Integer> visitedVoronoiCells)
        {
            this.visitedBackwardVoronoiCells.set(vertex.vertexId, visitedVoronoiCells);
        }

        /**
         * Builds an instance of {@code LocalityFilter} using {@code visitedForwardVoronoiCells} and
         * {@code visitedBackwardVoronoiCells}.
         *
         * @return locality filter
         */
        public LocalityFilter<V> buildLocalityFilter()
        {
            return new LocalityFilter<>(
                contractionMapping, visitedForwardVoronoiCells, visitedBackwardVoronoiCells);
        }
    }

    /**
     * This class represents return type of this algorithm and contains all data computed during the
     * execution of the algorithm. Formally it consists of:
     *
     * <ul style="list-style-type:circle;">
     * <li>{@link ContractionHierarchy} which was used to compute this transit node routing;</li>
     * <li>set of selected transit vertices;</li>
     * <li>{@link ManyToManyShortestPaths} between transit vertices;</li>
     * <li>{@link VoronoiDiagram} computed using transit vertices a cell centers;</li>
     * <li>{@link AccessVertices};</li>
     * <li>{@link LocalityFilter}.</li>
     * </ul>
     *
     * @param <V> graph vertex type
     * @param <E> graph edge type
     */
    static class TransitNodeRouting<V, E>
    {
        /**
         * Contraction hierarchy based on which this transit node routing was computed.
         */
        private ContractionHierarchy<V, E> contractionHierarchy;

        /**
         * Selected transit vertices.
         */
        private Set<ContractionVertex<V>> transitVertices;
        /**
         * Paths between every pair of transit vertices.
         */
        private ManyToManyShortestPaths<V, E> transitVerticesPaths;

        /**
         * Voronoi diagram of the graph using {@code transitVertices} as cells centers.
         */
        private VoronoiDiagram<V> voronoiDiagram;
        /**
         * Forward and backward access vertices for every vertex in the contraction graph.
         */
        private AccessVertices<V, E> accessVertices;
        /**
         * Locality filter of this transit node routing.
         */
        private LocalityFilter<V> localityFilter;

        /**
         * Returns contraction hierarchy of this transit node routing.
         *
         * @return contraction hierarchy of this transit node routing
         */
        public ContractionHierarchy<V, E> getContractionHierarchy()
        {
            return contractionHierarchy;
        }

        /**
         * Returns transit vertices of this transit node routing.
         *
         * @return transit vertices of this transit node routing
         */
        public Set<ContractionVertex<V>> getTransitVertices()
        {
            return transitVertices;
        }

        /**
         * Returns paths between every pair of {@code transitVertices}.
         *
         * @return paths between every pair of {@code transitVertices}
         */
        public ManyToManyShortestPaths<V, E> getTransitVerticesPaths()
        {
            return transitVerticesPaths;
        }

        /**
         * Returns Voronoi diagram of this transit node routing.
         *
         * @return Voronoi diagram of this transit node routing
         */
        public VoronoiDiagram<V> getVoronoiDiagram()
        {
            return voronoiDiagram;
        }

        /**
         * Returns access vertices of this transit node routing.
         *
         * @return access vertices of this transit node routing
         */
        public AccessVertices<V, E> getAccessVertices()
        {
            return accessVertices;
        }

        /**
         * Returns locality filter of this transit node routing.
         *
         * @return locality filter of this transit node routing
         */
        public LocalityFilter<V> getLocalityFilter()
        {
            return localityFilter;
        }

        /**
         * Constructs a new instance for the given {@code contractionHierarchy},
         * {@code transitVertices}, {@code transitVerticesPaths}, {@code voronoiDiagram},
         * {@code accessVertices} and {@code localityFilter}.
         *
         * @param contractionHierarchy contraction hierarchy
         * @param transitVertices transit vertices
         * @param transitVerticesPaths paths between transit vertices
         * @param voronoiDiagram Voronoi diagram
         * @param accessVertices access vertices
         * @param localityFilter locality filter
         */
        public TransitNodeRouting(
            ContractionHierarchy<V, E> contractionHierarchy,
            Set<ContractionVertex<V>> transitVertices,
            ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths<V, E> transitVerticesPaths,
            VoronoiDiagram<V> voronoiDiagram, AccessVertices<V, E> accessVertices,
            LocalityFilter<V> localityFilter)
        {
            this.contractionHierarchy = contractionHierarchy;
            this.transitVertices = transitVertices;
            this.transitVerticesPaths = transitVerticesPaths;
            this.voronoiDiagram = voronoiDiagram;
            this.localityFilter = localityFilter;
            this.accessVertices = accessVertices;
        }
    }

    /**
     * Voronoi diagram for a graph. Formally each cell in the diagram is defined as $Vor(v) = \{u ∈
     * V : ∀w ∈ T$ \ $ \{v\} : \mu(u, v) ≤ \mu(u, w)\}$, where $V$ is the vertex set, $T$ is a set
     * of vertaccess verticesices representing Voronoi cells centers and $\mu(u,v)$ denotes distance
     * between vertices $u$ and $v$.
     *
     * @param <V> graph vertex type
     */
    public static class VoronoiDiagram<V>
    {
        /**
         * For each vertex in {@code contractionGraph} contains id of its Voronoi cell, or
         * {@code NO_VORONOI_CELL} if it does not belong to any cell.
         */
        private int[] voronoiCells;

        /**
         * Constructs a new instance for the given {@code voronoiCells}.
         *
         * @param voronoiCells Voronoi cells ids
         */
        public VoronoiDiagram(int[] voronoiCells)
        {
            this.voronoiCells = voronoiCells;
        }

        /**
         * Returns Voronoi cell id which corresponds to {@code vertex}.
         *
         * @param vertex vertex
         * @return Voronoi cell id
         */
        public int getVoronoiCellId(ContractionVertex<V> vertex)
        {
            return voronoiCells[vertex.vertexId];
        }
    }

    /**
     * Search space based locality filter.
     *
     * <p>
     * Formally a locality filter is defined as $L : V × V → \{true, false\}$. $L(s, t)$ must be
     * $true$ when no shortest path between $s$ and $t$ contains a transit vertex.
     *
     * <p>
     * For every vertex in the {@code contractionGraph} stores two sets of visited Voronoi cells by
     * forward and backward {@code ContractionHierarchyBFS}.
     *
     * @param <V> graph vertex type
     */
    public static class LocalityFilter<V>
    {
        /**
         * Mapping of vertices in the initial graph to the vertices in the contraction graph.
         */
        private Map<V, ContractionVertex<V>> contractionMapping;

        /**
         * For every vertex in the contraction graph stores visited Voronoi cells ids by a forward
         * {@code ContractionHierarchyBFS}.
         */
        private List<Set<Integer>> visitedForwardVoronoiCells;
        /**
         * For every vertex in the contraction graph stores visited Voronoi cells ids by a backward
         * {@code ContractionHierarchyBFS}.
         */
        private List<Set<Integer>> visitedBackwardVoronoiCells;

        /**
         * Constructs a new instance for the given {@code contractionMapping},
         * {@code visitedForwardVoronoiCells} and {@code visitedBackwardVoronoiCells}.
         *
         * @param contractionMapping contraction mapping
         * @param visitedForwardVoronoiCells visited Voronoi cells ids by a forward search
         * @param visitedBackwardVoronoiCells visited Voronoi cells ids by a backward search
         */
        public LocalityFilter(
            Map<V, ContractionVertex<V>> contractionMapping,
            List<Set<Integer>> visitedForwardVoronoiCells,
            List<Set<Integer>> visitedBackwardVoronoiCells)
        {
            this.contractionMapping = contractionMapping;
            this.visitedForwardVoronoiCells = visitedForwardVoronoiCells;
            this.visitedBackwardVoronoiCells = visitedBackwardVoronoiCells;
        }

        /**
         * Returns $true$ when no shortest paths between {@code source} and {@code sink} contains a
         * transit vertex.
         *
         * @param source source vertex
         * @param sink sink vertex
         * @return $true$ iff no shortest paths between {@code source} and {@code sink} contains a
         *         transit vertex
         */
        public boolean isLocal(V source, V sink)
        {
            ContractionVertex<V> contractedSource = contractionMapping.get(source);
            ContractionVertex<V> contractedSink = contractionMapping.get(sink);

            Set<Integer> sourceVisitedVoronoiCells =
                visitedForwardVoronoiCells.get(contractedSource.vertexId);
            Set<Integer> sinkVisitedVoronoiCells =
                visitedBackwardVoronoiCells.get(contractedSink.vertexId);

            if (sourceVisitedVoronoiCells.contains(NO_VORONOI_CELL)
                || sinkVisitedVoronoiCells.contains(NO_VORONOI_CELL))
            {
                return true;
            }

            Set<Integer> smallerSet;
            Set<Integer> largerSet;
            if (sourceVisitedVoronoiCells.size() <= sinkVisitedVoronoiCells.size()) {
                smallerSet = sourceVisitedVoronoiCells;
                largerSet = sinkVisitedVoronoiCells;
            } else {
                smallerSet = sinkVisitedVoronoiCells;
                largerSet = sourceVisitedVoronoiCells;
            }

            for (Integer visitedVoronoiCell : smallerSet) {
                if (largerSet.contains(visitedVoronoiCell)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Stores forward and backward access vertices computed for the transit node routing.
     *
     * @param <V> graph vertex type
     * @param <E> graph edge type
     */
    public static class AccessVertices<V, E>
    {
        /**
         * For each vertex in {@code contractionGraph} stores corresponding forward access vertices.
         */
        private List<List<AccessVertex<V, E>>> forwardAccessVertices;
        /**
         * For each vertex in {@code contractionGraph} stores corresponding backward access
         * vertices.
         */
        private List<List<AccessVertex<V, E>>> backwardAccessVertices;

        /**
         * Constructs a new instance for the given {@code forwardAccessVertices} and
         * {@code backwardAccessVertices}.
         *
         * @param forwardAccessVertices forward access vertices
         * @param backwardAccessVertices backward access vertices
         */
        public AccessVertices(
            List<List<AccessVertex<V, E>>> forwardAccessVertices,
            List<List<AccessVertex<V, E>>> backwardAccessVertices)
        {
            this.forwardAccessVertices = forwardAccessVertices;
            this.backwardAccessVertices = backwardAccessVertices;
        }

        /**
         * Given a contraction vertex {@code vertex} returns its forward access vertices
         *
         * @param vertex vertex
         * @return list of forward access vertices
         */
        public List<AccessVertex<V, E>> getForwardAccessVertices(ContractionVertex<V> vertex)
        {
            return forwardAccessVertices.get(vertex.vertexId);
        }

        /**
         * Given a contraction vertex {@code vertex} returns its backward access vertices
         *
         * @param vertex vertex
         * @return list of backward access vertices
         */
        public List<AccessVertex<V, E>> getBackwardAccessVertices(ContractionVertex<V> vertex)
        {
            return backwardAccessVertices.get(vertex.vertexId);
        }
    }

    /**
     * Forward or backward access vertex computed for a certain vertex $v$ in the graph.
     * <p>
     * In the transit node routing if $u$ is a forward access vertex for $v$, it means that if you
     * want go far away from $v$, it is highly likely that you would need to pass through $u$.
     * Correspondingly, if $u$ is a backward access vertex for $v$, it means that if you want to go
     * to $v$ from far away, you would highly likely go through $u$.
     *
     * <p>
     * Stores transit vertex and the shortest path between $v$ and {@code vertex}. If this is a
     * forward access vertex, then {@code vertex} is the ending vertex in the {@code path},
     * Otherwise it is a starting vertex of the {@code path}.
     *
     * @param <V> graph vertex type
     * @param <E> graph edge type
     */
    public static class AccessVertex<V, E>
    {
        /**
         * Transit vertex.
         */
        private V vertex;
        /**
         * Path between a vertex $v$ this access vertex is computed for and {@code vertex}.
         */
        private GraphPath<V, E> path;

        /**
         * Returns a transit vertex of this access vertex.
         *
         * @return transit vertex of this access vertex
         */
        public V getVertex()
        {
            return vertex;
        }

        /**
         * Returns path between a vertex in the graph and {@code vertex}.
         *
         * @return path between a vertex in the graph and {@code vertex}.
         */
        public GraphPath<V, E> getPath()
        {
            return path;
        }

        /**
         * Constructs a new instance for the given {@code vertex} and {@code path}.
         *
         * @param vertex a transit vertex
         * @param path path between a vertex in the graph and {@code vertex}
         */
        public AccessVertex(V vertex, GraphPath<V, E> path)
        {
            this.vertex = vertex;
            this.path = path;
        }
    }

    /**
     * Task which is used to perform {@code ContractionHierarchyBFS} in parallel.
     */
    private class AVAndLFConstructionTask
        implements
        Runnable
    {
        /**
         * Id of this task.
         */
        private int taskId;

        /**
         * Builder object for a {@code LocalityFilter} instance.
         */
        private LocalityFilterBuilder localityFilterBuilder;
        /**
         * Builder object for a {@code AccessVertices} instance.
         */
        private AccessVerticesBuilder accessVerticesBuilder;
        /**
         * Is used to run forward CH BFS query over the {@code contractionGraph}.
         */
        private ContractionHierarchyBFS forwardBFS;
        /**
         * Is used to run backward CH BFS query over the {@code contractionGraph}.
         */
        private ContractionHierarchyBFS backwardBFS;

        /**
         * Constructs a new instance for the give {@code taskId}, {@code localityFilterBuilder},
         * {@code accessVerticesBuilder}, {@code forwardBFS} and {@code backwardBFS}.
         *
         * @param taskId id of this task
         * @param localityFilterBuilder builder object for {@code LocalityFilter}
         * @param accessVerticesBuilder builder object for {@code AccessVertices}
         * @param forwardBFS forward {@code ContractionHierarchyBFS}
         * @param backwardBFS backward {@code ContractionHierarchyBFS}
         */
        public AVAndLFConstructionTask(
            int taskId, LocalityFilterBuilder localityFilterBuilder,
            AccessVerticesBuilder accessVerticesBuilder, ContractionHierarchyBFS forwardBFS,
            ContractionHierarchyBFS backwardBFS)
        {
            this.taskId = taskId;
            this.localityFilterBuilder = localityFilterBuilder;
            this.accessVerticesBuilder = accessVerticesBuilder;
            this.forwardBFS = forwardBFS;
            this.backwardBFS = backwardBFS;
        }

        @Override
        public void run()
        {
            int start = workerSegmentStart(0, contractionVertices.size(), taskId);
            int end = workerSegmentEnd(0, contractionVertices.size(), taskId);
            for (int i = start; i < end; ++i) {
                ContractionVertex<V> v = contractionVertices.get(i);

                Pair<Set<V>, Set<Integer>> forwardData = forwardBFS.runSearch(v);
                Pair<Set<V>, Set<Integer>> backwardData = backwardBFS.runSearch(v);

                accessVerticesBuilder.addForwardAccessVertices(v, forwardData.getFirst());
                accessVerticesBuilder.addBackwardAccessVertices(v, backwardData.getFirst());

                localityFilterBuilder.addForwardVisitedVoronoiCells(v, forwardData.getSecond());
                localityFilterBuilder.addBackwardVisitedVoronoiCells(v, backwardData.getSecond());
            }
        }

    }

    /**
     * Task which is used to unpack contracted many-to-many shortest paths between transit vertices.
     */
    private class PathsUnpackingTask
        implements
        Runnable
    {
        /**
         * Id of this task.
         */
        private int taskId;

        /**
         * Selected transit vertices.
         */
        private List<V> transitVertices;
        /**
         * Map where the unpacked paths will be stored.
         */
        private Map<V, Map<V, GraphPath<V, E>>> pathsMap;
        /**
         * Many-to-many shortest paths to be unpacked.
         */
        private ManyToManyShortestPaths<V, E> shortestPaths;

        /**
         * Constructs a new instance for the given {@code taskId}, {@code transitVertices},
         * {@code pathsMap} and {@code shortestPaths}.
         *
         * @param taskId id of this task
         * @param transitVertices transit vertices
         * @param pathsMap map for unpacked paths
         * @param shortestPaths paths to be unpacked
         */
        public PathsUnpackingTask(
            int taskId, List<V> transitVertices, Map<V, Map<V, GraphPath<V, E>>> pathsMap,
            ManyToManyShortestPaths<V, E> shortestPaths)
        {
            this.taskId = taskId;
            this.transitVertices = transitVertices;
            this.pathsMap = pathsMap;
            this.shortestPaths = shortestPaths;
        }

        @Override
        public void run()
        {
            int start = workerSegmentStart(0, transitVertices.size(), taskId);
            int end = workerSegmentEnd(0, transitVertices.size(), taskId);

            for (int i = start; i < end; ++i) {
                V v1 = transitVertices.get(i);
                Map<V, GraphPath<V, E>> targetToPathsMap = pathsMap.get(v1);
                for (V v2 : transitVertices) {
                    targetToPathsMap.put(v2, shortestPaths.getPath(v1, v2));
                }
            }
        }
    }

    /**
     * Computes start of the working chunk for this task.
     *
     * @param segmentStart working segment start
     * @param segmentEnd working segment end
     * @return working chunk start
     */
    private int workerSegmentStart(int segmentStart, int segmentEnd, int taskId)
    {
        return segmentStart + ((segmentEnd - segmentStart) * taskId) / parallelism;
    }

    /**
     * Computes end of the working chunk for this task.
     *
     * @param segmentStart working segment start
     * @param segmentEnd working segment end
     * @return working chunk end
     */
    private int workerSegmentEnd(int segmentStart, int segmentEnd, int taskId)
    {
        return segmentStart + ((segmentEnd - segmentStart) * (taskId + 1)) / parallelism;
    }
}
