/*
 * (C) Copyright 2019-2021, by Peter Harman and Contributors.
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
package org.jgrapht.alg.tour;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.util.ArrayUtil;

import java.util.*;

import static org.jgrapht.util.ArrayUtil.swap;

/**
 * The nearest neighbour heuristic algorithm for the TSP problem.
 *
 * <p>
 * The travelling salesman problem (TSP) asks the following question: "Given a list of cities and
 * the distances between each pair of cities, what is the shortest possible route that visits each
 * city exactly once and returns to the origin city?".
 * </p>
 *
 * <p>
 * This is perhaps the simplest and most straightforward TSP heuristic. The key to this algorithm is
 * to always visit the nearest city.
 * </p>
 *
 * <p>
 * The tour computed with a {@code Nearest-Neighbor-Heuristic} can vary depending on the first
 * vertex visited. The first vertex for the next or for multiple subsequent tour computations (calls
 * of {@link #getTour(Graph)}) can be specified in the constructors
 * {@link #FarthestInsertionHeuristicTSP(Object)} or {@link #FarthestInsertionHeuristicTSP(Iterable)}.
 * This can be used for example to ensure that the first vertices visited are different for
 * subsequent calls of {@code  getTour(Graph)}. Once each specified first vertex is used, the first
 * vertex in subsequent tour computations is selected randomly from the graph. Alternatively
 * {@link #FarthestInsertionHeuristicTSP(Random)} or {@link #FarthestInsertionHeuristicTSP(long)} can be
 * used to specify a {@code Random} used to randomly select the vertex visited first.
 * </p>
 *
 * <p>
 * The implementation of this class is based on: <br>
 * Nilsson, Christian. "Heuristics for the traveling salesman problem." Linkoping University 38
 * (2003)
 * </p>
 *
 * <p>
 * The runtime complexity of this algorithm is $O(V^2)$.
 * </p>
 *
 * <p>
 * This algorithm requires that the graph is complete.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Jose Alejandro Cornejo Acosta
 */
public class FarthestInsertionHeuristicTSP<V, E>
        extends
        HamiltonianCycleAlgorithmBase<V, E> {

    private Random rng;
    /**
     * Nulled, if it has no next
     */
    private Iterator<V> initiaVertex;

    /**
     * Distances from V the tour
     */
    double[] distances = null;


    /**
     * Constructor. By default a random vertex is chosen to start.
     */
    public FarthestInsertionHeuristicTSP() {
        this(null, new Random());
    }

    /**
     * Constructor
     *
     * @param first First vertex to visit
     * @throws NullPointerException if first is null
     */
    public FarthestInsertionHeuristicTSP(V first) {
        this(
                Collections
                        .singletonList(
                                Objects.requireNonNull(first, "Specified initial vertex cannot be null")),
                new Random());
    }

    /**
     * Constructor
     *
     * @param initialVertices The Iterable of vertices visited first in subsequent tour computations
     *                        (per call of {@link #getTour(Graph)} another vertex of the Iterable is used as first)
     * @throws NullPointerException if first is null
     */
    public FarthestInsertionHeuristicTSP(Iterable<V> initialVertices) {
        this(
                Objects.requireNonNull(initialVertices, "Specified initial vertices cannot be null"),
                new Random());
    }

    /**
     * Constructor
     *
     * @param seed seed for the random number generator
     */
    public FarthestInsertionHeuristicTSP(long seed) {
        this(null, new Random(seed));
    }

    /**
     * Constructor
     *
     * @param rng Random number generator
     * @throws NullPointerException if rng is null
     */
    public FarthestInsertionHeuristicTSP(Random rng) {
        this(null, Objects.requireNonNull(rng, "Random number generator cannot be null"));
    }

    /**
     * Constructor
     *
     * @param initialVertices The Iterable of vertices visited first in subsequent tour
     *                        computations, or null to choose at random
     * @param rng             Random number generator
     */
    private FarthestInsertionHeuristicTSP(Iterable<V> initialVertices, Random rng) {
        if (initialVertices != null) {
            Iterator<V> iterator = initialVertices.iterator();
            this.initiaVertex = iterator.hasNext() ? iterator : null;
        }
        this.rng = rng;
    }

    // algorithm

    /**
     * Computes a tour using the farthest insertion heuristic.
     *
     * @param graph the input graph
     * @return a tour
     * @throws IllegalArgumentException if the graph is not undirected
     * @throws IllegalArgumentException if the graph is not complete
     * @throws IllegalArgumentException if the graph contains no vertices
     * @throws IllegalArgumentException if the specified initial vertex is not in the graph
     */
    @Override
    public GraphPath<V, E> getTour(Graph<V, E> graph) {
        checkGraph(graph);
        if (graph.vertexSet().size() == 1) {
            return getSingletonTour(graph);
        }

        Set<V> vertexSet = graph.vertexSet();
        int n = vertexSet.size();
        distances = new double[n];
        Arrays.fill(distances, Double.POSITIVE_INFINITY);

        double[] edgesTour = new double[n-1];

        @SuppressWarnings("unchecked") V[] path = (V[]) vertexSet.toArray(new Object[n + 1]);
        List<V> pathList = Arrays.asList(path); // List backed by path-array

        // move initial vertex to the beginning
        int initalIndex = getFirstVertexIndex(pathList);
        ArrayUtil.swap(path, 0, initalIndex);
        V k = path[0];

        // construct tour
        for (int i = 1; i < n; i++) {

            // Update distances from vertices to the partial path
            updateDistances(k, path, i, graph);

            // Find the farthest unvisited vertex.
            int farthest = getFarthest(i, n);
            k = path[farthest];

            // Search for the best position of vertex k in the path
            double saving = Double.POSITIVE_INFINITY;
            int bestIndex = -1;
            for (int j = 0; j < i; j++) {
                V x = path[j];
                V y = (j == i - 1 ? path[0] : path[j + 1]);
                double dxk = graph.getEdgeWeight(graph.getEdge(x, k));
                double dky = graph.getEdgeWeight(graph.getEdge(k, y));
                double dxy = (x == y ? 0 : graph.getEdgeWeight(graph.getEdge(x, y)));
                double savingTmp = dxk + dky - dxy;
                if (savingTmp < saving) {
                    saving = savingTmp;
                    bestIndex = j + 1;
                }
            }
            ArrayUtil.swap(path, i, farthest);
            swap(distances, i, farthest);

            // perform insertion
            performInsertion(i, bestIndex, path);
//            for (int j = i; j > bestIndex; j--) {
//                path[j] = path[j - 1];
//            }
//                         if (i - bestIndex >= 0) System.arraycopy(path, bestIndex, path, bestIndex + 1, i - bestIndex);
            path[bestIndex] = k;
        }

        path[n] = path[0]; // close tour manually. Arrays.asList does not support add
        return closedVertexListToTour(pathList, graph);
    }

    public void performInsertion(int i, int bestIndex, V[] path){
        for (int j = i; j > bestIndex; j--) {
            path[j] = path[j - 1];
        }
    }


//    @Override
//    public GraphPath<V, E> getTour(Graph<V, E> graph) {
//        checkGraph(graph);
//        if (graph.vertexSet().size() == 1) {
//            return getSingletonTour(graph);
//        }
//
//        Set<V> vertexSet = graph.vertexSet();
//        int n = vertexSet.size();
//        distances = new double[n];
//        Arrays.fill(distances, Double.POSITIVE_INFINITY);
//
//        @SuppressWarnings("unchecked") V[] path = (V[]) vertexSet.toArray(new Object[n + 1]);
//        List<V> pathList = Arrays.asList(path); // List backed by path-array
//        LinkedList<V> T = new LinkedList<>();
//
//        // move initial vertex to the beginning
//        int initalIndex = getFirstVertexIndex(pathList);
//        ArrayUtil.swap(path, 0, initalIndex);
//        V k = path[0];
//        T.add(k);
//
//        // construct tour
//        for (int i = 1; i < n; i++) {
//
//            // Update distances from vertices to the partial path
//            updateDistances(k, path, i, graph);
//
//            // Find the farthest unvisited vertex.
//            int farthest = getFarthest(i, n);
//            k = path[farthest];
//
//            // Search for the best position of vertex k in the path
//            double saving = Double.POSITIVE_INFINITY;
//            int bestIndex = -1;
//            for (int j = 0; j < i; j++) {
//                V x = path[j];
//                V y = (j == i - 1 ? path[0] : path[j + 1]);
//                double dxk = graph.getEdgeWeight(graph.getEdge(x, k));
//                double dky = graph.getEdgeWeight(graph.getEdge(k, y));
//                double dxy = (x == y ? 0 : graph.getEdgeWeight(graph.getEdge(x, y)));
//                double savingTmp = dxk + dky - dxy;
//                if (savingTmp < saving) {
//                    saving = savingTmp;
//                    bestIndex = j + 1;
//                }
//            }
//            if(T.size()==1){
//                T.add(k);
//            }
//            else{
//                ListIterator<V> xListItr = null;
//                var itr = T.listIterator();
//                while(itr.hasNext()){
////                    xListItr = itr.next();
//                }
//            }
//            ArrayUtil.swap(path, i, farthest);
//            swap(distances, i, farthest);
//
//            // perform insertion
//            for (int j = i; j > bestIndex; j--) {
//                path[j] = path[j - 1];
//            }
//            path[bestIndex] = k;
//        }
//
//        T.add(T.getFirst());
//        path[n] = path[0]; // close tour manually. Arrays.asList does not support add
//        return closedVertexListToTour(T, graph);
//    }

    /**
     * Returns the start vertex of the tour about to compute.
     *
     * @param path the initial path, containing all vertices in unspecified order
     * @return the vertex to start with
     * @throws IllegalArgumentException if the specified initial vertex is not in the graph
     */
    private int getFirstVertexIndex(List<V> path) {
        if (initiaVertex != null) {
            V first = initiaVertex.next();
            if (!initiaVertex.hasNext()) {
                initiaVertex = null; // release the resource backing the iterator immediately
            }
            int initialIndex = path.indexOf(first);
            if (initialIndex < 0) {
                throw new IllegalArgumentException("Specified initial vertex is not in graph");
            }
            return initialIndex;
        } else { // first not specified
            return rng.nextInt(path.size() - 1); // path has size n+1
        }
    }

    /**
     * Find the vertex in the range staring at {@code from} that is closest to the element at index
     * from-1.
     *
     * @param current  the vertex for which the nearest neighbor is searched
     * @param vertices the vertices of the graph. The unvisited neighbors start at index
     *                 {@code start}
     * @param start    the index of the first vertex to consider
     * @param g        the graph containing the vertices
     * @return the index of the unvisited vertex closest to the vertex at firstNeighbor-1.
     */
    private int getFarthest(int start, int n) {
        int farthest = -1;
        double maxDist = -1;

        for (int i = start; i < n; i++) {
            double dist = distances[i];
            if (dist > maxDist) {
                farthest = i;
                maxDist = dist;
            }
        }
        return farthest;
    }

    private void updateDistances(V current, V[] vertices, int start, Graph<V, E> g) {


        for (int i = start; i < distances.length; i++) {
            V v = vertices[i];

            double vDist = g.getEdgeWeight(g.getEdge(current, v));
            if (vDist < distances[i]) {
                distances[i] = vDist;
            }
        }
    }

    /**
     * Swaps the two elements at the specified indices in the given double array.
     *
     * @param arr the array
     * @param i   the index of the first element
     * @param j   the index of the second element
     */
    public static void swap(double[] arr, int i, int j) {
        double tmp = arr[j];
        arr[j] = arr[i];
        arr[i] = tmp;
    }
}
