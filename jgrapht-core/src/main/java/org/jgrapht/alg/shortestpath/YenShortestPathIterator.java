/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
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
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jheaps.*;
import org.jheaps.tree.*;

import java.util.*;
import java.util.function.*;

/**
 * Iterator over the shortest loopless paths between two vertices in a graph sorted by weight.
 *
 * <p>
 * For this iterator to work correctly the graph must not be modified during iteration. Currently
 * there are no means to ensure that, nor to fail-fast. The results of such modifications are
 * undefined.
 *
 * <p>
 * The main idea of this algorithm is to divide each path between the {@code source} and the
 * {@code sink} into the root part - the part that coincides within some of the paths computed so
 * far, and the spur part, the part that deviates from all other paths computed so far. Therefore,
 * for each path the algorithm maintains a vertex, at which the path deviates from its "parent" path
 * (the candidate path using which it was computed).
 *
 * <p>
 * First the algorithm finds the shortest path between the {@code source} and the {@code sink},
 * which is put into the candidates heap. The {@code source} is assigned to be its deviation vertex.
 * Then on each iteration the algorithm takes a candidate from the heap with minimum weight, puts it
 * into the result list and builds all possible deviations from it wrt. other paths, that are in the
 * result list. By generating spur paths starting only from the vertices that are after the
 * deviation vertex of current path (including the deviation vertex) it is possible to avoid
 * building duplicated candidates.
 *
 * <p>
 * Additionally, the algorithm supports path validation by means of {@link PathValidator}.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 * @see PathValidator
 */
public class YenShortestPathIterator<V, E>
    implements
    Iterator<GraphPath<V, E>>
{
    /**
     * Underlying graph.
     */
    private final Graph<V, E> graph;
    /**
     * Source vertex.
     */
    private final V source;
    /**
     * Sink vertex.
     */
    private final V sink;

    /**
     * Provides possibility to validate computed paths and exclude invalid ones. Whenever a
     * candidate path $P$ first deviation vertex $u$ is produces by this algorithm, it is passed to
     * {@code getLastValidDeviation()} to find the last valid deviation vertex $v$ for it. The
     * algorithm puts obtained vertex in {@code lastDeviations} map. If vertex $v$ is {@code null},
     * the candidate is considered correct. Otherwise for the path $P$ deviation are built only from
     * vertices between $u$ and $v$ inclusive.
     */
    private PathValidator<V, E> pathValidator;

    /**
     * List of the paths returned so far via the {@link #next()} method.
     */
    private List<GraphPath<V, E>> resultList;

    /**
     * Heap of the candidate path generated so far and sorted my their weights. There is a boolean
     * flag for every candidate in the queue, which indicates, if the path is valid ot not. An
     * invalid path is a path which contains an edge which fails the {@code pathValidator} check.
     * Invalid paths are kept in the queue, because it is possible to build a valid path by
     * deviating from an invalid one.
     */
    private AddressableHeap<Double, Pair<GraphPath<V, E>, Boolean>> candidatePaths;

    /**
     * For each path $P$, stores its deviation point.
     * <p>
     * A path deviation point is a first node in the path that doesn't belong to the parent path. If
     * the path doesn't have a parent (which is only possible for one shortest path between the
     * {@code source} and the {@code sink}), this map stores its first node.
     */
    private Map<GraphPath<V, E>, V> firstDeviations;

    /**
     * For each path $P$ stores the vertex $u$ such that $pathValidator#isValidPath([start_vertex,
     * u], (u,v)) = false$, where $[start_vertex, u]$ denotes the subpath of $P$ from its start to
     * vertex $u$ and $v$ is the next vertex in $P$ after $u$. Stores {@code null}, if there is no
     * such vertex.
     */
    private Map<GraphPath<V, E>, V> lastDeviations;

    /**
     * Stores number of valid candidates in {@code candidatePaths}.
     */
    private int numberOfValidPathInQueue;

    /**
     * Indicates if the {@code lazyInitializePathHeap} procedure has already been executed.
     */
    private boolean shortestPathComputed;

    /**
     * Constructs an instance of the algorithm for given {@code graph}, {@code source} and
     * {@code sink}.
     *
     * @param graph graph
     * @param source source vertex
     * @param sink sink vertex
     */
    public YenShortestPathIterator(Graph<V, E> graph, V source, V sink)
    {
        this(graph, source, sink, PairingHeap::new);
    }

    /**
     * Constructs an instance of the algorithm for given {@code graph}, {@code source}, {@code sink}
     * and {@code pathValidator}. The {@code pathValidator} can be {@code null}, which will indicate
     * that all paths are valid.
     *
     * @param graph graph
     * @param source source vertex
     * @param sink sink vertex
     * @param pathValidator validator to computed paths
     */
    public YenShortestPathIterator(
        Graph<V, E> graph, V source, V sink, PathValidator<V, E> pathValidator)
    {
        this(graph, source, sink, PairingHeap::new, pathValidator);
    }

    /**
     * Constructs an instance of the algorithm for given {@code graph}, {@code source}, {@code sink}
     * and {@code heapSupplier}.
     *
     * @param graph graph
     * @param source source vertex
     * @param sink sink vertex
     * @param heapSupplier supplier of the preferable heap implementation
     */
    public YenShortestPathIterator(
        Graph<V, E> graph, V source, V sink,
        Supplier<AddressableHeap<Double, Pair<GraphPath<V, E>, Boolean>>> heapSupplier)
    {
        this(graph, source, sink, heapSupplier, null);
    }

    /**
     * Constructs an instance of the algorithm for given {@code graph}, {@code source},
     * {@code sink}, {@code heapSupplier} and {@code pathValidator}. The {@code pathValidator} can
     * be {@code null}, which will indicate that all paths are valid.
     *
     * @param graph graph
     * @param source source vertex
     * @param sink sink vertex
     * @param heapSupplier supplier of the preferable heap implementation
     * @param pathValidator validator for computed paths
     */
    public YenShortestPathIterator(
        Graph<V, E> graph, V source, V sink,
        Supplier<AddressableHeap<Double, Pair<GraphPath<V, E>, Boolean>>> heapSupplier,
        PathValidator<V, E> pathValidator)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null!");
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph should contain source vertex!");
        }
        this.source = source;
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException("Graph should contain sink vertex!");
        }
        this.sink = sink;
        this.pathValidator = pathValidator;
        Objects.requireNonNull(heapSupplier, "Heap supplier cannot be null");
        this.resultList = new ArrayList<>();
        this.candidatePaths = heapSupplier.get();
        this.firstDeviations = new HashMap<>();
        this.lastDeviations = new HashMap<>();
    }

    /**
     * Lazily initializes the path heap by computing the shortest path between the {@code source}
     * and the {@code sink} and building a necessary amount of paths until at least one valid path
     * is found.
     *
     * Note: this computation is done only once during the first call to either {@code hasNext()} or
     * {@code next()}.
     */
    private void lazyInitializePathHeap()
    {
        if (!shortestPathComputed) {
            GraphPath<V, E> shortestPath =
                DijkstraShortestPath.findPathBetween(graph, source, sink);

            if (shortestPath != null) {
                V lastValidDeviation = getLastValidDeviation(shortestPath, source);
                boolean shortestPathIsValid = lastValidDeviation == null;

                candidatePaths
                    .insert(shortestPath.getWeight(), Pair.of(shortestPath, shortestPathIsValid));
                firstDeviations.put(shortestPath, source);
                lastDeviations.put(shortestPath, lastValidDeviation);

                if (shortestPathIsValid) {
                    ++numberOfValidPathInQueue;
                }

                ensureAtLeastOneValidPathInQueue();
            }
        }
        shortestPathComputed = true;
    }

    /**
     * This method is used to make sure that there exist at least one valid path on the queue.
     * During the iteration if the candidates queue is not empty then the iterator has next value.
     * Otherwise is does not.
     */
    private void ensureAtLeastOneValidPathInQueue()
    {
        while (numberOfValidPathInQueue == 0 && !candidatePaths.isEmpty()) {
            Pair<GraphPath<V, E>, Boolean> p = candidatePaths.deleteMin().getValue();
            GraphPath<V, E> currentPath = p.getFirst();
            resultList.add(currentPath);
            int numberOfValidDeviations = addDeviations(currentPath);
            numberOfValidPathInQueue += numberOfValidDeviations;
        }
    }

    /**
     * Computes vertex $u$ such that $pathValidator#isValidPath([start_vertex, u], (u,v)) = false$,
     * where $[start_vertex, u]$ denotes the subpath of $P$ from its start to vertex $u$ and $v$ is
     * the next vertex in $P$ after $u$. Returns null if there is no such vertex.
     *
     * @param path graph path
     * @param firstDeviation vertex at which {@code path} deviates from its parent path
     * @return vertex which is last valid deviation for {@code path}
     */
    private V getLastValidDeviation(GraphPath<V, E> path, V firstDeviation)
    {
        if (pathValidator == null) {
            return null;
        }
        List<V> vertices = path.getVertexList();
        List<E> edges = path.getEdgeList();

        V result = null;
        double partialPathWeight = 0.0;
        int firstDeviationIndex = vertices.indexOf(firstDeviation);
        for (int i = firstDeviationIndex; i < edges.size(); ++i) {
            GraphPath<V,
                E> partialPath = new GraphWalk<>(
                    path.getGraph(), path.getStartVertex(), vertices.get(i),
                    vertices.subList(0, i + 1), edges.subList(0, i), partialPathWeight);
            E edge = edges.get(i);
            boolean isValid = pathValidator.isValidPath(partialPath, edge);
            if (!isValid) {
                result = vertices.get(i);
                break;
            }
            partialPathWeight += graph.getEdgeWeight(edge);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext()
    {
        lazyInitializePathHeap();
        return !candidatePaths.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> next()
    {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        GraphPath<V, E> result = null;
        while (result == null) {
            Pair<GraphPath<V, E>, Boolean> p = candidatePaths.deleteMin().getValue();
            GraphPath<V, E> path = p.getFirst();
            boolean isValid = p.getSecond();

            if (isValid) {
                result = path;
                --numberOfValidPathInQueue;
            }

            resultList.add(path);

            int numberOfValidDeviations = addDeviations(path);
            numberOfValidPathInQueue += numberOfValidDeviations;
        }

        ensureAtLeastOneValidPathInQueue();
        return result;
    }

    /**
     * Builds unique loopless deviations from the given path in the {@code graph}. First receives
     * the deviation vertex of the current path as well as sets of vertices and edges to be masked
     * during the computations. Then creates an instance of the {@link MaskSubgraph} and builds a
     * reversed shortest paths tree starting at {@code sink} in it. Finally builds new candidate
     * paths by deviating from the vertices of the provided {@code path}. Puts only those candidates
     * in the {@code candidatesList}, which deviate from {@code path} between $firstDeviation$ and
     * $lastDeviation$. $firstDeviation$ and $lastDeviation$ are obtainer from
     * {@code firstDeviations} and {@code lastDeviations} correspondingly.
     *
     * <p>
     * For more information on this step refer to the article with the original description of the
     * algorithm.
     *
     * @param path path to build deviations of
     *
     * @return number of computed valid deviations
     */
    private int addDeviations(GraphPath<V, E> path)
    {
        int result = 0;

        // initializations
        V pathDeviation = firstDeviations.get(path);
        List<V> pathVertices = path.getVertexList();
        List<E> pathEdges = path.getEdgeList();
        int pathVerticesSize = pathVertices.size();
        int pathDeviationIndex = pathVertices.indexOf(pathDeviation);

        // receive masked vertices and edges
        Pair<Set<V>, Set<E>> p = getMaskedVerticesAndEdges(path, pathDeviation, pathDeviationIndex);
        Set<V> maskedVertices = p.getFirst();
        Set<E> maskedEdges = p.getSecond();

        // build reversed shortest paths tree
        Graph<V, E> maskSubgraph =
            new MaskSubgraph<>(graph, maskedVertices::contains, maskedEdges::contains);
        Graph<V, E> reversedMaskedGraph = new EdgeReversedGraph<>(maskSubgraph);
        DijkstraShortestPath<V, E> shortestPath = new DijkstraShortestPath<>(reversedMaskedGraph);
        TreeSingleSourcePathsImpl<V, E> singleSourcePaths =
            (TreeSingleSourcePathsImpl<V, E>) shortestPath.getPaths(sink);
        Map<V, Pair<Double, E>> distanceAndPredecessorMap =
            new HashMap<>(singleSourcePaths.getDistanceAndPredecessorMap());
        YenShortestPathsTree customTree = new YenShortestPathsTree(
            maskSubgraph, maskedVertices, maskedEdges, distanceAndPredecessorMap, sink);

        // get index of last deviation
        V lastDeviation = lastDeviations.get(path);
        int lastDeviationIndex;
        if (lastDeviation == null) { // path is valid
            lastDeviationIndex = pathVerticesSize - 2;
        } else {
            lastDeviationIndex = pathVertices.indexOf(lastDeviation);
        }

        // build spur paths by iteratively recovering vertices of the current path
        boolean proceed = true;
        for (int i = pathVerticesSize - 2; i >= 0 && proceed; i--) {
            V recoverVertex = pathVertices.get(i);
            if (recoverVertex.equals(pathDeviation)) {
                proceed = false;
            }

            // recover vertex
            customTree.recoverVertex(recoverVertex);
            customTree.correctDistanceForward(recoverVertex);
            GraphPath<V, E> spurPath = customTree.getPath(recoverVertex);

            // construct a new path if possible
            if (spurPath != null) {
                customTree.correctDistanceBackward(recoverVertex);

                if (i <= lastDeviationIndex) { // candidate path can be valid
                    GraphPath<V, E> candidate = getCandidatePath(path, i, spurPath);
                    double candidateWeight = candidate.getWeight();
                    V candidateLastDeviation = getLastValidDeviation(candidate, recoverVertex);
                    boolean candidateIsValid = candidateLastDeviation == null;

                    candidatePaths.insert(candidateWeight, Pair.of(candidate, candidateIsValid));
                    firstDeviations.put(candidate, recoverVertex);
                    lastDeviations.put(candidate, candidateLastDeviation);

                    if (candidateIsValid) {
                        ++result;
                    }
                }
            }
            // recover edge
            V recoverVertexSuccessor = pathVertices.get(i + 1);
            E edge = pathEdges.get(i);
            customTree.recoverEdge(edge);

            double recoverVertexUpdatedDistance = maskSubgraph.getEdgeWeight(edge)
                + customTree.map.get(recoverVertexSuccessor).getFirst();

            if (customTree.map.get(recoverVertex).getFirst() > recoverVertexUpdatedDistance) {
                customTree.map.put(recoverVertex, Pair.of(recoverVertexUpdatedDistance, edge));
                customTree.correctDistanceBackward(recoverVertex);
            }
        }
        return result;
    }

    /**
     * For the given {@code path} builds sets of vertices and edges to be masked. First masks all
     * edges and vertices of the provided {@code path} except for the {@code sink}. Then for each
     * path in the {@code resultList} that coincides in the {@code path} until the
     * {@code pathDeviation} masks the edge between the {@code pathDeviation} and its successor in
     * this path.
     *
     * @param path path to mask vertices and edges of
     * @param pathDeviation deviation vertex of the path
     * @param pathDeviationIndex index of the deviation vertex in the vertices list of the path
     * @return pair of sets of masked vertices and edges
     */
    private Pair<Set<V>, Set<E>> getMaskedVerticesAndEdges(
        GraphPath<V, E> path, V pathDeviation, int pathDeviationIndex)
    {
        List<V> pathVertices = path.getVertexList();
        List<E> pathEdges = path.getEdgeList();

        Set<V> maskedVertices = new HashSet<>();
        Set<E> maskedEdges = new HashSet<>();

        int pathVerticesSize = pathVertices.size();

        // mask vertices and edges of the current path
        for (int i = 0; i < pathVerticesSize - 1; i++) {
            maskedVertices.add(pathVertices.get(i));
            maskedEdges.add(pathEdges.get(i));
        }

        // mask corresponding edges of coinciding paths
        int resultListSize = resultList.size();
        for (int i = 0; i < resultListSize - 1; i++) { // the vertex of the current paths has been
                                                       // masked already
            GraphPath<V, E> resultPath = resultList.get(i);
            List<V> resultPathVertices = resultPath.getVertexList();
            int deviationIndex = resultPathVertices.indexOf(pathDeviation);

            if (deviationIndex < 0 || deviationIndex != pathDeviationIndex
                || !equalLists(pathVertices, resultPathVertices, deviationIndex))
            {
                continue;
            }

            maskedEdges.add(resultPath.getEdgeList().get(deviationIndex));
        }
        return Pair.of(maskedVertices, maskedEdges);
    }

    /**
     * Builds a candidate path based on the information provided in the methods parameters. First
     * adds the root part of the candidate by traversing the vertices and edges of the {@code path}
     * until the {@code recoverVertexIndex}. Then adds vertices and edges of the {@code spurPath}.
     *
     * @param path path the candidate path deviates from
     * @param recoverVertexIndex vertex that is being recovered
     * @param spurPath spur path of the candidate
     * @return candidate path
     */
    private GraphPath<V, E> getCandidatePath(
        GraphPath<V, E> path, int recoverVertexIndex, GraphPath<V, E> spurPath)
    {
        List<V> pathVertices = path.getVertexList();
        List<E> pathEdges = path.getEdgeList();

        List<V> candidatePathVertices = new LinkedList<>();
        List<E> candidatePathEdges = new LinkedList<>();

        double rootPathWeight = 0.0;
        for (int i = 0; i < recoverVertexIndex; i++) {
            E edge = pathEdges.get(i);
            rootPathWeight += graph.getEdgeWeight(edge);
            candidatePathEdges.add(edge);
            candidatePathVertices.add(pathVertices.get(i));
        }

        ListIterator<V> spurPathVerticesIterator =
            spurPath.getVertexList().listIterator(spurPath.getVertexList().size());
        while (spurPathVerticesIterator.hasPrevious()) {
            candidatePathVertices.add(spurPathVerticesIterator.previous());
        }
        ListIterator<E> spurPathEdgesIterator =
            spurPath.getEdgeList().listIterator(spurPath.getEdgeList().size());
        while (spurPathEdgesIterator.hasPrevious()) {
            candidatePathEdges.add(spurPathEdgesIterator.previous());
        }

        double candidateWeight = rootPathWeight + spurPath.getWeight();
        return new GraphWalk<>(
            graph, source, sink, candidatePathVertices, candidatePathEdges, candidateWeight);
    }

    /**
     * Checks if the lists have the same content until the {@code index} (inclusive).
     *
     * @param first first list
     * @param second second list
     * @param index position in the lists
     * @return true iff the contents of the list are equal until the index
     */
    private boolean equalLists(List<V> first, List<V> second, int index)
    {
        for (int i = 0; i <= index; i++) {
            if (!first.get(i).equals(second.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper class which represents the shortest paths tree using which the spur parts are computed
     * and appended to the candidate paths
     */
    class YenShortestPathsTree
        extends
        TreeSingleSourcePathsImpl<V, E>
    {
        /**
         * Vertices which are masked in the {@code g}.
         */
        Set<V> maskedVertices;
        /**
         * Edges which are masked in the {@code g}.
         */
        Set<E> maskedEdges;

        /**
         * Constructs an instance of the shortest paths tree for the given {@code maskSubgraph},
         * {@code maskedVertices}, {@code maskedEdges}, {@code reversedTree}, {@code treeSource}.
         *
         * @param maskSubgraph graph which has removed vertices and edges
         * @param maskedVertices vertices removed form the graph
         * @param maskedEdges edges removed from the graph
         * @param reversedTree shortest path tree in the edge reversed {@code maskSubgraph} starting
         *        at {@code treeSource}.
         * @param treeSource source vertex of the {@code reversedTree}
         */
        YenShortestPathsTree(
            Graph<V, E> maskSubgraph, Set<V> maskedVertices, Set<E> maskedEdges,
            Map<V, Pair<Double, E>> reversedTree, V treeSource)
        {
            super(maskSubgraph, treeSource, reversedTree);
            this.maskedVertices = maskedVertices;
            this.maskedEdges = maskedEdges;
        }

        /**
         * Restores vertex {@code v} in the {@code g}.
         *
         * @param v vertex to be recovered
         */
        void recoverVertex(V v)
        {
            maskedVertices.remove(v);
        }

        /**
         * Restores edge {@code e} in the {@code g}.
         *
         * @param e edge to be recovered
         */
        void recoverEdge(E e)
        {
            maskedEdges.remove(e);
        }

        /**
         * Updates the distance of provided vertex {@code v} in the shortest paths tree based on the
         * current distances of its successors in the {@code g}.
         *
         * @param v vertex which should be updated
         */
        void correctDistanceForward(V v)
        {
            super.map.putIfAbsent(v, new Pair<>(Double.POSITIVE_INFINITY, null));

            for (E e : super.g.outgoingEdgesOf(v)) {
                V successor = Graphs.getOppositeVertex(super.g, e, v);
                if (successor.equals(v)) {
                    continue;
                }
                double updatedDistance = Double.POSITIVE_INFINITY;
                if (super.map.containsKey(successor)) {
                    updatedDistance = super.map.get(successor).getFirst();
                }
                updatedDistance += super.g.getEdgeWeight(e);

                double currentDistance = super.map.get(v).getFirst();
                if (currentDistance > updatedDistance) {
                    super.map.put(v, Pair.of(updatedDistance, e));
                }
            }
        }

        /**
         * Updates the distance of relevant predecessors of the input vertex.
         *
         * @param v vertex which distance should be updated
         */
        void correctDistanceBackward(V v)
        {
            List<V> vertices = new LinkedList<>();
            vertices.add(v);

            while (!vertices.isEmpty()) {
                V vertex = vertices.remove(0);
                double vertexDistance = super.map.get(vertex).getFirst();

                for (E e : super.g.incomingEdgesOf(vertex)) {
                    V predecessor = Graphs.getOppositeVertex(super.g, e, vertex);
                    if (predecessor.equals(vertex)) {
                        continue;
                    }
                    double predecessorDistance = Double.POSITIVE_INFINITY;
                    if (super.map.containsKey(predecessor)) {
                        predecessorDistance = super.map.get(predecessor).getFirst();
                    }

                    double updatedDistance = vertexDistance + super.g.getEdgeWeight(e);
                    if (predecessorDistance > updatedDistance) {
                        super.map.put(predecessor, Pair.of(updatedDistance, e));
                        vertices.add(predecessor);
                    }
                }
            }
        }
    }
}
