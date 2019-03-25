/*
 * (C) Copyright 2019-2019, by Semen Chudakov and Contributors.
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
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.MaskSubgraph;
import org.jheaps.AddressableHeap;
import org.jheaps.tree.PairingHeap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Iterator over the shortest loopless paths between two vertices in a graph sorted by weight.
 *
 * <p>
 * For this iterator to work correctly the graph must not be modified during iteration.
 * Currently there are no means to ensure that, nor to fail-fast. The results of such
 * modifications are undefined.
 *
 * <p>
 * The main idea of this algorithm is to divide each path between the {@code source} and the
 * {@code sink} into the root part - the part that coincides within some of the paths computed so far, and
 * the spur part, the part that deviates from all other paths computed so far. Therefore, for each path the
 * algorithm maintains a vertex, at which the path deviates from its "parent" path (the candidate path
 * using which it was computed).
 *
 * <p>
 * First the algorithm finds the shortest path between the {@code source} and the {@code sink},
 * which is put into the candidates heap. The {@code source} is assigned to be its deviation vertex.
 * Then on each iteration the algorithm takes a candidate from the heap with minimum weight, puts it into the
 * result list and builds all possible deviations from it wrt. other paths, that are in the result list.
 * By generating spur paths starting only from the vertices that are after the deviation vertex of current
 * path (including the deviation vertex) it is possible to avoid building duplicated candidates.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 */
public class YenShortestPathIterator<V, E> implements Iterator<GraphPath<V, E>> {
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
     * List of the paths returned so far via the {@link #next()} method.
     */
    private List<GraphPath<V, E>> resultList;

    /**
     * Heap of the candidate path generated so far and sorted my their weights.
     */
    private AddressableHeap<Double, GraphPath<V, E>> candidatePaths;

    /**
     * Keeps track of the vertex at which each path
     * deviates from its "parent" path.
     */
    private Map<GraphPath<V, E>, V> deviations;

    /**
     * Keeps track of the number of paths in the candidates
     * heap which have a particular weight. The algorithm
     * uses this map to maintain the number of paths with
     * minimum weight in the heap.
     */
    private Map<Double, Integer> weightsFrequencies;

    /**
     * Stores the number of paths in {@code candidatePaths}
     * with minimum weight.
     */
    private int numberOfCandidatesWithMinimumWeight;

    /**
     * Returns current number of candidate paths with minimum weight.
     *
     * @return current number of candidate paths with minimum weight
     */
    int getNumberOfCandidatesWithMinimumWeight() {
        return numberOfCandidatesWithMinimumWeight;
    }

    /**
     * Returns heap with candidate paths.
     *
     * @return heap with candidate paths
     */
    AddressableHeap<Double, GraphPath<V, E>> getCandidatePaths() {
        return candidatePaths;
    }

    /**
     * Constructs an instance of the algorithm for given {@code graph},
     * {@code source} and {@code sink}.
     *
     * @param graph  graph
     * @param source source vertex
     * @param sink   sink vertex
     */
    public YenShortestPathIterator(Graph<V, E> graph, V source, V sink) {
        this(graph, source, sink, PairingHeap::new);
    }

    /**
     * Constructs an instance of the algorithm for given {@code graph},
     * {@code source}, {@code sink} and {@code heapSupplier}.
     *
     * @param graph        graph
     * @param source       source vertex
     * @param sink         sink vertex
     * @param heapSupplier supplier of the preferable heap implementation
     */
    public YenShortestPathIterator(Graph<V, E> graph, V source, V sink,
                                   Supplier<AddressableHeap<Double, GraphPath<V, E>>> heapSupplier) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null!");
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph should contain source vertex!");
        }
        this.source = source;
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph should contain sink vertex!");
        }
        this.sink = sink;
        Objects.requireNonNull(heapSupplier, "Heap supplier cannot be null");
        this.resultList = new ArrayList<>();
        this.candidatePaths = heapSupplier.get();
        this.deviations = new HashMap<>();
        this.weightsFrequencies = new HashMap<>();

        GraphPath<V, E> shortestPath = DijkstraShortestPath.findPathBetween(graph, source, sink);
        if (shortestPath != null) {
            candidatePaths.insert(shortestPath.getWeight(), shortestPath);
            deviations.put(shortestPath, source);
            weightsFrequencies.put(shortestPath.getWeight(), 1);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return !candidatePaths.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> next() {
        if (candidatePaths.isEmpty()) {
            throw new NoSuchElementException();
        }

        GraphPath<V, E> path = candidatePaths.deleteMin().getValue();
        resultList.add(path);
        double pathWeight = path.getWeight();
        int minWeightFrequency = weightsFrequencies.get(pathWeight);
        if (minWeightFrequency == 1) {
            weightsFrequencies.remove(pathWeight);
            if (candidatePaths.isEmpty()) {
                numberOfCandidatesWithMinimumWeight = 0;
            } else {
                numberOfCandidatesWithMinimumWeight = weightsFrequencies.get(candidatePaths.findMin().getKey());
            }
        } else {
            weightsFrequencies.put(pathWeight, minWeightFrequency - 1);
        }

        addDeviations(path);

        return path;
    }

    /**
     * Builds unique loopless deviations from the given path
     * in the {@code graph}. First receives the deviation
     * vertex of the current path as well as sets of vertices
     * and edges to be masked during the computations. Then
     * creates an instance of the {@link MaskSubgraph}
     * and builds a reversed shortest paths tree starting at
     * {@code sink} in it. Finally builds new paths by deviating
     * from the vertices of the provided {@code path}.
     * <p>
     * For more information on this step refer to the article
     * with the original description of the algorithm.
     *
     * @param path path to build deviations of
     */
    private void addDeviations(GraphPath<V, E> path) {
        // initializations
        V pathDeviation = deviations.get(path);
        List<V> pathVertices = path.getVertexList();
        int pathVerticesSize = pathVertices.size();
        int pathDeviationIndex = pathVertices.indexOf(pathDeviation);

        // receive masked vertices and edges
        Pair<Set<V>, Set<E>> p = getMaskedVerticesAndEdges(path, pathDeviation, pathDeviationIndex);
        Set<V> maskedVertices = p.getFirst();
        Set<E> maskedEdges = p.getSecond();


        // build reversed shortest paths tree
        Graph<V, E> maskSubgraph = new MaskSubgraph<>(graph, maskedVertices::contains, maskedEdges::contains);
        Graph<V, E> reversedMaskedGraph = new EdgeReversedGraph<>(maskSubgraph);
        DijkstraShortestPath<V, E> shortestPath = new DijkstraShortestPath<>(reversedMaskedGraph);
        TreeSingleSourcePathsImpl<V, E> singleSourcePaths = (TreeSingleSourcePathsImpl<V, E>) shortestPath.getPaths(sink);
        Map<V, Pair<Double, E>> distanceAndPredecessorMap = new HashMap<>(singleSourcePaths.getDistanceAndPredecessorMap());
        YenShortestPathsTree customTree = new YenShortestPathsTree(maskSubgraph, maskedVertices,
                maskedEdges, distanceAndPredecessorMap, sink);


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

                GraphPath<V, E> candidate = getCandidatePath(path, i, spurPath);
                double candidateWeight = candidate.getWeight();

                candidatePaths.insert(candidateWeight, candidate);
                deviations.put(candidate, recoverVertex);

                if (weightsFrequencies.containsKey(candidateWeight)) {
                    weightsFrequencies.computeIfPresent(candidateWeight, (weight, frequency) -> frequency + 1);
                } else {
                    weightsFrequencies.put(candidateWeight, 1);
                }
            }
            // recover edge
            V recoverVertexSuccessor = pathVertices.get(i + 1);
            E edge = graph.getEdge(recoverVertex, recoverVertexSuccessor);
            customTree.recoverEdge(edge);

            double recoverVertexUpdatedDistance = maskSubgraph.getEdgeWeight(edge) +
                    customTree.map.get(recoverVertexSuccessor).getFirst();

            if (customTree.map.get(recoverVertex).getFirst() > recoverVertexUpdatedDistance) {
                customTree.map.put(recoverVertex, Pair.of(recoverVertexUpdatedDistance, edge));
                customTree.correctDistanceBackward(recoverVertex);
            }
        }
    }

    /**
     * For the given {@code path} builds sets of vertices and edges to be masked.
     * First masks all edges and vertices of the provided {@code path} except for the {@code sink}.
     * Then for each path in the {@code resultList} that coincides in the {@code path}
     * until the {@code pathDeviation} masks the edge between the {@code pathDeviation} and its
     * successor in this path.
     *
     * @param path               path to mask vertices and edges of
     * @param pathDeviation      deviation vertex of the path
     * @param pathDeviationIndex index of the deviation vertex in the
     *                           vertices list of the path
     * @return pair of sets of masked vertices and edges
     */
    private Pair<Set<V>, Set<E>> getMaskedVerticesAndEdges(GraphPath<V, E> path,
                                                           V pathDeviation, int pathDeviationIndex) {
        List<V> pathVertices = path.getVertexList();
        Set<V> maskedVertices = new HashSet<>();
        Set<E> maskedEdges = new HashSet<>();

        int pathVerticesSize = pathVertices.size();

        // mask vertices and edges of the current path
        for (int i = 0; i < pathVerticesSize - 1; i++) {
            V v = pathVertices.get(i);
            maskedVertices.add(v);
            maskedEdges.add(graph.getEdge(v, pathVertices.get(i + 1)));
        }

        // mask corresponding edges of coinciding paths
        int resultListSize = resultList.size();
        for (int i = 0; i < resultListSize - 1; i++) { // the vertex of the current paths has been masked already
            GraphPath<V, E> resultPath = resultList.get(i);
            List<V> resultPathVertices = resultPath.getVertexList();
            int deviationIndex = resultPathVertices.indexOf(pathDeviation);

            if (deviationIndex < 0 || deviationIndex != pathDeviationIndex ||
                    !equalLists(pathVertices, resultPathVertices, deviationIndex)) {
                continue;
            }

            V successor = resultPathVertices.get(deviationIndex + 1);
            maskedEdges.add(graph.getEdge(pathDeviation, successor));
        }
        return Pair.of(maskedVertices, maskedEdges);
    }

    /**
     * Builds a candidate path based on the information provided in the
     * methods parameters. First adds the root part of the candidate by
     * traversing the vertices and edges of the {@code path} until the
     * {@code recoverVertexIndex}. Then adds vertices and edges of the
     * {@code spurPath}.
     *
     * @param path               path the candidate path deviates from
     * @param recoverVertexIndex vertex that is being recovered
     * @param spurPath           spur path of the candidate
     * @return candidate path
     */
    private GraphPath<V, E> getCandidatePath(GraphPath<V, E> path, int recoverVertexIndex, GraphPath<V, E> spurPath) {
        List<V> pathVertices = path.getVertexList();

        List<V> candidatePathVertices = new LinkedList<>();
        List<E> candidatePathEdges = new LinkedList<>();

        double rootPathWeight = 0.0;
        for (int j = 0; j < recoverVertexIndex; j++) {
            E edge = graph.getEdge(pathVertices.get(j), pathVertices.get(j + 1));
            rootPathWeight += graph.getEdgeWeight(edge);
            candidatePathEdges.add(edge);
            candidatePathVertices.add(pathVertices.get(j));
        }

        ListIterator<V> spurPathVerticesIterator = spurPath.getVertexList().listIterator(spurPath.getVertexList().size());
        while (spurPathVerticesIterator.hasPrevious()) {
            candidatePathVertices.add(spurPathVerticesIterator.previous());
        }
        ListIterator<E> spurPathEdgesIterator = spurPath.getEdgeList().listIterator(spurPath.getEdgeList().size());
        while (spurPathEdgesIterator.hasPrevious()) {
            candidatePathEdges.add(spurPathEdgesIterator.previous());
        }

        double candidateWeight = rootPathWeight + spurPath.getWeight();
        return new GraphWalk<>(graph, source, sink,
                candidatePathVertices, candidatePathEdges, candidateWeight);
    }

    /**
     * Checks if the lists have the same content until the {@code index} (inclusive).
     *
     * @param first  first list
     * @param second second list
     * @param index  position in the lists
     * @return true iff the contents of the list are equal until the index
     */
    private boolean equalLists(List<V> first, List<V> second, int index) {
        for (int i = 0; i <= index; i++) {
            if (!first.get(i).equals(second.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper class which represents the shortest paths tree
     * using which the spur parts are computed and appended
     * to the candidate paths
     */
    class YenShortestPathsTree extends TreeSingleSourcePathsImpl<V, E> {
        /**
         * Vertices which are masked in the {@code g}.
         */
        Set<V> maskedVertices;
        /**
         * Edges which are masked in the {@code g}.
         */
        Set<E> maskedEdges;

        /**
         * Constructs an instance of the shortest paths tree for the given
         * {@code maskSubgraph}, {@code maskedVertices}, {@code maskedEdges}, {@code reversedTree}, {@code treeSource}.
         *
         * @param maskSubgraph   graph which has removed vertices and edges
         * @param maskedVertices vertices removed form the graph
         * @param maskedEdges    edges removed from the graph
         * @param reversedTree   shortest path tree in the edge reversed
         *                       {@code maskSubgraph} starting at {@code treeSource}.
         * @param treeSource     source vertex of the  {@code reversedTree}
         */
        YenShortestPathsTree(Graph<V, E> maskSubgraph, Set<V> maskedVertices, Set<E> maskedEdges,
                             Map<V, Pair<Double, E>> reversedTree, V treeSource) {
            super(maskSubgraph, treeSource, reversedTree);
            this.maskedVertices = maskedVertices;
            this.maskedEdges = maskedEdges;
        }

        /**
         * Restores vertex {@code v} in the {@code g}.
         *
         * @param v vertex to be recovered
         */
        void recoverVertex(V v) {
            maskedVertices.remove(v);
        }

        /**
         * Restores edge {@code e} in the {@code g}.
         *
         * @param e edge to be recovered
         */
        void recoverEdge(E e) {
            maskedEdges.remove(e);
        }

        /**
         * Updates the distance of provided vertex {@code v} in the shortest paths tree
         * based on the current distances of its successors in the {@code g}.
         *
         * @param v vertex which should be updated
         */
        void correctDistanceForward(V v) {
            super.map.putIfAbsent(v, new Pair<>(Double.POSITIVE_INFINITY, null));

            for (E e : super.g.outgoingEdgesOf(v)) {
                V successor = Graphs.getOppositeVertex(super.g, e, v);

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
        void correctDistanceBackward(V v) {
            List<V> vertices = new LinkedList<>();
            vertices.add(v);

            while (!vertices.isEmpty()) {
                V vertex = vertices.remove(0);
                double vertexDistance = super.map.get(vertex).getFirst();

                for (E e : super.g.incomingEdgesOf(vertex)) {
                    V predecessor = Graphs.getOppositeVertex(super.g, e, vertex);

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
