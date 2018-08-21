/*
 * (C) Copyright 2018-2018, by Christoph Grüne and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.Graph;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.util.TypeUtil;

import java.util.*;

/**
 * Implementation of an algorithm for the local augmentation problem for the cyclic exchange neighborhood,
 * i.e. it finds subset-disjoint negative cycles in a graph, based on
 * Ravindra K. Ahuja, James B. Orlin, Dushyant Sharma,
 * A composite very large-scale neighborhood structure for the capacitated minimum spanning tree problem,
 * Operations Research Letters, Volume 31, Issue 3, 2003, Pages 185-194, ISSN 0167-6377,
 * https://doi.org/10.1016/S0167-6377(02)00236-5. (http://www.sciencedirect.com/science/article/pii/S0167637702002365)
 *
 * A subset-disjoint cycle is a cycle such that no two vertices in the cycle are in the same subset of a given partition of the whole vertex set.
 *
 * This algorithm returns the first found negative subset-disjoint cycle, that is, the cycle has minimum number of vertices. It may enumerate all paths up
 * to the length given by the parameter <code>lengthBound</code>, i.e the algorithm runs in exponential time.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author Christoph Grüne
 * @since June 7, 2018
 */
public class AhujaOrlinSharmaCyclicExchangeLocalAugmentation<V, E> {

    /**
     * Implementation of a labeled path.
     * It is used in AhujaOrlinSharmaCyclicExchangeLocalAugmentation to efficiently maintain the paths in the calculation.
     *
     * @param <V> the vertex type
     *
     * @author Christoph Grüne
     * @since June 7, 2018
     */
    private static class LabeledPath<V> implements Cloneable {

        /**
         * the vertices in the path
         */
        private ArrayList<V> vertices;
        /**
         * the labels the path contains
         */
        private HashSet<Integer> labels;
        /**
         * the cost of the path
         */
        private double cost;

        /**
         * Constructs a LabeledPath with the given inputs
         *
         * @param vertices the vertices of the path in order of the path
         * @param cost the cost of the edges connecting the vertices
         * @param labels the mapping of the vertices to labels (subsets)
         */
        protected LabeledPath(ArrayList<V> vertices, double cost, HashSet<Integer> labels) {
            this.vertices = vertices;
            this.cost = cost;
            this.labels = labels;
        }

        /**
         * Adds a vertex to the path
         *
         * @param v the vertex
         * @param edgeCost the cost of the edge connecting the last vertex of the path and the new vertex
         * @param label the label of the new vertex
         */
        protected void addVertex(V v, double edgeCost, int label) {
            this.vertices.add(v);
            this.cost += edgeCost;
            this.labels.add(label);
        }

        /**
         * Returns the start vertex of the path
         *
         * @return the start vertex of the path
         */
        protected V getHead() {
            return vertices.get(0);
        }

        /**
         * Returns the end vertex of the path
         *
         * @return the end vertex of the path
         */
        protected V getTail() {
            return vertices.get(vertices.size() - 1);
        }

        /**
         * Returns whether the path is empty, i.e. has no vertices
         *
         * @return whether the path is empty
         */
        protected boolean isEmpty() {
            return vertices.isEmpty();
        }

        /**
         * Returns an ordered list of the vertices of the path
         *
         * @return an ordered list of the vertices of the path
         */
        protected List<V> getVertices() {
            return vertices;
        }

        /**
         * Returns the labels of all vertices of the path
         *
         * @return the labels of all vertices of the path
         */
        protected Set<Integer> getLabels() {
            return labels;
        }

        /**
         * Returns the cost of the path
         *
         * @return the cost of the path
         */
        protected double getCost() {
            return cost;
        }

        /**
         * Returns a shallow copy of this labeled path instance. Vertices are not cloned.
         *
         * @return a shallow copy of this path.
         *
         * @throws RuntimeException in case the clone is not supported
         *
         * @see java.lang.Object#clone()
         */
        public LabeledPath<V> clone() {
            try {
                LabeledPath<V> newLabeledPath = TypeUtil.uncheckedCast(super.clone());
                newLabeledPath.vertices = (ArrayList<V>) this.vertices.clone();
                newLabeledPath.labels = (HashSet<Integer>) this.labels.clone();
                newLabeledPath.cost = this.cost;

                return newLabeledPath;
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    /**
     * Implementation of a key for the path maps.
     * It is used in AhujaOrlinSharmaCyclicExchangeLocalAugmentation to efficiently maintain the path sets in the calculation.
     *
     * @param <V> the vertex type
     *
     * @author Christoph Grüne
     * @since June 7, 2018
     */
    private static class PathSetKey<V> {
        /**
         * the head of the paths indexed by this key
         */
        private V head;
        /**
         * the tail of the paths indexed by this key
         */
        private V tail;
        /**
         * the label set of the paths indexed by this key
         */
        private Set<Integer> labels;

        /**
         * Constructs a new PathSetKey object
         *
         * @param head the head of the paths indexed by this key
         * @param tail the tail of the paths indexed by this key
         * @param labels the label set of the paths indexed by this key
         */
        private PathSetKey(V head, V tail, Set<Integer> labels) {
            this.head = head;
            this.tail = tail;
            this.labels = labels;
        }

        /**
         * Constructs a new PathSetKey object
         *
         * @param head the head of the path indexed by this key
         * @param tail the tail of the path indexed by this key
         * @param labels the label set of the path indexed by this key
         * @param <V> the vertex type
         * @return a new PathSetKey object
         */
        protected static <V> PathSetKey<V> of(V head, V tail, Set<Integer> labels) {
            return new PathSetKey<>(head, tail, labels);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.head, this.tail, this.labels);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            else if (!(o instanceof PathSetKey))
                return false;

            @SuppressWarnings("unchecked") PathSetKey<V> other = (PathSetKey<V>) o;
            return Objects.equals(head, other.head) && Objects.equals(tail, other.tail) && Objects.equals(labels, other.labels);
        }
    }

    /**
     * the input graph
     */
    private Graph<V, E> graph;
    /**
     * the map that maps each vertex to a subset (identified by labels) of the partition
     */
    private Map<V, Integer> labelMap;
    /**
     * bound on how long the cycle can get
     */
    private int lengthBound;

    /**
     * Constructs an algorithm with given inputs
     *
     * @param graph the (improvement) graph on which to calculate the local augmentation
     * @param lengthBound the (inclusive) upper bound for the length of cycles to detect
     * @param labelMap the labelMap of the vertices encoding the subsets of vertices
     */
    public AhujaOrlinSharmaCyclicExchangeLocalAugmentation(Graph<V, E> graph, int lengthBound, Map<V, Integer> labelMap) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        if (!graph.getType().isWeighted()) {
            throw new IllegalArgumentException("Graph is not weighted");
        }
        this.lengthBound = lengthBound;
        this.labelMap = Objects.requireNonNull(labelMap, "Labels cannot be null");
    }

    /**
     * Calculates a valid subset-disjoint negative cycle.
     * If there is no such cycle, it returns an empty GraphWalk instance
     *
     * @return a valid subset-disjoint negative cycle encoded as GraphWalk
     */
    public GraphWalk<V, E> getLocalAugmentationCycle() {

        int k = 1;

        /*
         * Store the path in map with key PathSetKey<V, V, Set<Integer>>, since only paths with the same head,
         * same tail, and the subset of labels may be in domination relation. Thus the algorithm runs faster.
         */
        Map<PathSetKey<V>, LabeledPath<V>> pathsLengthK = new LinkedHashMap<>();
        Map<PathSetKey<V>, LabeledPath<V>> pathsLengthKplus1 = new LinkedHashMap<>();

        // initialize pathsLengthK for k = 1
        for(E e : graph.edgeSet()) {
            if(graph.getEdgeWeight(e) < 0) {
                // initialize all paths of cost < 0
                V sourceVertex = graph.getEdgeSource(e);
                V targetVertex = graph.getEdgeTarget(e);
                // catch self-loops directly
                if(sourceVertex == targetVertex) {
                    List<V> vertices = new ArrayList<>();
                    vertices.add(sourceVertex);
                    vertices.add(targetVertex);
                    return new GraphWalk<>(graph, vertices, graph.getEdgeWeight(e));
                }
                if (!labelMap.get(sourceVertex).equals(labelMap.get(targetVertex))) {
                    ArrayList<V> pathVertices = new ArrayList<>(lengthBound);
                    HashSet<Integer> pathLabels = new HashSet<>();
                    pathVertices.add(sourceVertex);
                    pathVertices.add(targetVertex);
                    pathLabels.add(labelMap.get(sourceVertex));
                    pathLabels.add(labelMap.get(targetVertex));
                    LabeledPath<V> path = new LabeledPath<>(pathVertices, graph.getEdgeWeight(e), pathLabels);

                    // add path to set of paths of length 1
                    updatePathIndex(pathsLengthK, path);
                }
            }
        }

        while (k < lengthBound) {

            // go through all valid paths of length k
            for (LabeledPath<V> path : pathsLengthK.values()) {

                    V head = path.getHead();
                    V tail = path.getTail();

                    // the path builds a valid negative cycle
                    if (graph.containsEdge(tail, head)
                            && path.getCost() + graph.getEdgeWeight(graph.getEdge(tail, head)) < 0) {
                        LabeledPath<V> cycleResult = path.clone();
                        cycleResult.addVertex(head, graph.getEdgeWeight(graph.getEdge(tail, head)), labelMap.get(head));

                        return new GraphWalk<>(graph, cycleResult.getVertices(), cycleResult.getCost());
                    }

                    for (E e : graph.outgoingEdgesOf(tail)) {
                        V currentVertex = graph.getEdgeTarget(e);
                        // extend the path if the extension is still negative and correctly labeled
                        double edgeWeight = graph.getEdgeWeight(e);
                        int currentLabel = labelMap.get(currentVertex);
                        if (!path.getLabels().contains(currentLabel) && path.getCost() + edgeWeight < 0) {
                            LabeledPath<V> newPath = path.clone();
                            newPath.addVertex(currentVertex, edgeWeight, currentLabel);

                            /*
                             * check if paths are dominated, i.e. if the path is definitely worse than other paths
                             * and does not have to be considered in the future
                             */
                            if (!checkDominatedPathsOfLengthKplus1(newPath, pathsLengthKplus1)) {
                                if(!checkDominatedPathsOfLengthK(newPath, pathsLengthK)) {
                                    updatePathIndex(pathsLengthKplus1, newPath);
                                }
                            }
                        }
                    }

            }
            // update k and the corresponding sets
            k += 1;
            pathsLengthK = pathsLengthKplus1;
            pathsLengthKplus1 = new LinkedHashMap<>();
        }

        return new GraphWalk<>(graph, new ArrayList<>(lengthBound), Double.MAX_VALUE);
    }

    /**
     * Checks whether <code>path</code> dominates the current minimal cost path with the same head, tail and label set
     * in the set of all paths of length k + 1. Thus, dominated paths are eliminated.
     * This is important out of efficiency reasons, otherwise many unnecessary paths may
     * be considered in further calculations.
     *
     * @param path the currently calculated path
     * @param pathsLengthKplus1 all before calculated paths of length k + 1
     *
     * @return whether <code>path</code> dominates the current minimal cost path with the same head, tail and label set.
     */
    private boolean checkDominatedPathsOfLengthKplus1(LabeledPath<V> path, Map<PathSetKey<V>, LabeledPath<V>> pathsLengthKplus1) {
        // simulates domination test by using the index structure
        LabeledPath<V> pathToCheck = pathsLengthKplus1.get(PathSetKey.of(path.getHead(), path.getTail(), path.getLabels()));
        if (pathToCheck != null) {
            return pathToCheck.getCost() < path.getCost();
        }
        return false;
    }

    /**
     * Checks whether <code>path</code> is dominated by some path in the previously calculated set of paths of length k.
     * This is important out of efficiency reasons, otherwise many unnecessary paths may
     * be considered in further calculations.
     *
     * @param path the currently calculated path
     * @param pathsLengthK all previously calculated paths of length k
     *
     * @return whether <code>path</code> is dominated by some path in <code>pathsLengthK</code>
     */
    private boolean checkDominatedPathsOfLengthK(LabeledPath<V> path, Map<PathSetKey<V>, LabeledPath<V>> pathsLengthK) {
        Set<Integer> modifiableLabelSet = new HashSet<>(path.getLabels());
        for(Integer label : path.getLabels()) {
            modifiableLabelSet.remove(label);
            // simulates domination test by using the index structure
            LabeledPath<V> pathToCheck = pathsLengthK.get(PathSetKey.of(path.getHead(), path.getTail(), modifiableLabelSet));
            if (pathToCheck != null) {
                if(pathToCheck.getCost() < path.getCost()) {
                    return true;
                }
            }
            modifiableLabelSet.add(label);
        }
        return false;
    }

    /**
     * Adds a path and removes the path, which has the same tail, head and label set,
     * to the data structure <code>paths</code>, which contains all paths indexed by their head, tail and label set.
     *
     * @param paths the map of paths, which are indexed by head, tail and label set, to add the path to
     * @param path the path to add
     */
    private void updatePathIndex(Map<PathSetKey<V>, LabeledPath<V>> paths, LabeledPath<V> path) {
        PathSetKey<V> currentKey = PathSetKey.of(path.getHead(), path.getTail(), path.getLabels());
        paths.put(currentKey, path);
    }
}
