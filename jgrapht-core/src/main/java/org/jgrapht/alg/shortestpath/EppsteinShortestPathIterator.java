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
import org.jgrapht.traverse.*;

import java.util.*;

/**
 * Iterator over the shortest paths (not required to be simple) between two vertices in a graph
 * sorted by weight.
 *
 * <p>
 * This implementation can only be used for directed simple graphs. Also for this iterator to work
 * correctly the graph must not be modified during iteration. Currently there are no means to ensure
 * that, nor to fail-fast. The results of such modifications are undefined.
 *
 * <p>
 * First the shortest paths tree in the edge reversed graph starting at {@code sink} is built. Thus
 * we get distances $d(v)$ from every vertex $v$ to {@code sink}. We then define a sidetrack edge to
 * be an edge, which is not in the shortest paths tree. The key observation is that every path
 * between the {@code source} and the {@code sink} can be solely determined by a sub-sequence of its
 * edges which are sidetracks.
 *
 * <p>
 * Let $d(v)$ be the distance from $v$ to {@code sink} and $w()$ be the weight function for edges in
 * {@code graph}. If $e$ connects a pair of vertices $(u, w)$, the $\delta(e)$ is defined as
 * $w(e)+d(w)-d(u)$. Intuitively, $\delta(e)$ measures how much distance is lost by being
 * “sidetracked” along $e$ instead of taking a shortest path to {@code sink}.
 *
 * <p>
 * The idea of the algorithm is to build a heap of sidetracks. This heap can be then traversed with
 * breadth-first search in order to retrieve the implicit representations of the paths between
 * {@code source} and {@code sink}.
 *
 * <p>
 * This implementation has several improvements in comparison to the original description in the
 * article:
 *
 * <ol>
 * <li>An outgoing edge of vertex $v$ is inserted in the paths graph iff it is reachable from the
 * {@code source}.</li>
 * <li>The cross edges in the paths graph are added only for those vertices which are reachable from
 * the root vertex.</li>
 * <li>Weights of the edges in the paths graph are mot maintained explicitly, because they are
 * computed during its traversal.</li>
 * </ol>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @author Semen Chudakov
 */
public class EppsteinShortestPathIterator<V, E>
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
     * Vertex of the paths graph from which the BFS traversal is started.
     */
    private PathsGraphVertex pathsGraphRoot;

    /**
     * Shortest paths tree in the edge reversed graph {@code graph} rooted at {@code sink}.
     */
    private Map<V, Pair<Double, E>> distanceAndPredecessorMap;

    /**
     * Priority queue of the paths generated during the computation.
     */
    private Queue<EppsteinGraphPath> pathsQueue;

    /**
     * For each vertex $v$ in {@code graph} maintains the root of the balanced heap, which
     * corresponds to it.
     */
    private Map<V, PathsGraphVertex> hMapping;

    /**
     * Constructs an instance of the algorithm for the given {@code graph}, {@code source} and
     * {@code sink}.
     *
     * @param graph graph
     * @param source source vertex
     * @param sink sink vertex
     */
    public EppsteinShortestPathIterator(Graph<V, E> graph, V source, V sink)
    {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null!");
        GraphType type = graph.getType();
        if (!(type.isDirected() && type.isSimple())) {
            throw new IllegalArgumentException("graph must be simple and directed");
        }
        if (!graph.containsVertex(source)) {
            throw new IllegalArgumentException("Graph does not contain source vertex");
        }
        this.source = source;
        if (!graph.containsVertex(sink)) {
            throw new IllegalArgumentException("Graph does not contain sink vertex");
        }
        this.sink = sink;

        pathsQueue = new PriorityQueue<>();

        TreeSingleSourcePathsImpl<V, E> shortestPaths = (TreeSingleSourcePathsImpl<V,
            E>) new DijkstraShortestPath<>(new EdgeReversedGraph<>(graph)).getPaths(sink);

        GraphPath<V, E> shortestPath = shortestPaths.getPath(source);
        if (shortestPath != null) {
            distanceAndPredecessorMap = shortestPaths.getDistanceAndPredecessorMap();
            pathsQueue
                .add(
                    new EppsteinGraphPath(
                        graph, new ArrayList<>(0), distanceAndPredecessorMap,
                        shortestPath.getWeight()));
            hMapping = new HashMap<>();

            buildPathsGraph();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext()
    {
        return !pathsQueue.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphPath<V, E> next()
    {
        if (pathsQueue.isEmpty()) {
            throw new NoSuchElementException();
        }

        EppsteinGraphPath result = pathsQueue.remove();
        addOneEdgeExtension(result);

        return result;
    }

    /**
     * Adds all one-edge extension of the {@code path} wrt the paths graph.
     *
     * @param path path to put extensions of
     */
    private void addOneEdgeExtension(EppsteinGraphPath path)
    {
        PathsGraphVertex lastPathsGraphVertex;

        if (path.pathsGraphVertices.isEmpty()) { // if this is shortest path between the source and
                                                 // sink
            lastPathsGraphVertex = pathsGraphRoot;
        } else {
            lastPathsGraphVertex = path.pathsGraphVertices.get(path.pathsGraphVertices.size() - 1);
        }

        if (lastPathsGraphVertex.left != null) {
            addExtension(
                path, lastPathsGraphVertex.left,
                lastPathsGraphVertex.left.delta - lastPathsGraphVertex.delta);
        }
        if (lastPathsGraphVertex.right != null) {
            addExtension(
                path, lastPathsGraphVertex.right,
                lastPathsGraphVertex.right.delta - lastPathsGraphVertex.delta);
        }
        if (lastPathsGraphVertex.rest != null) {
            addExtension(
                path, lastPathsGraphVertex.rest,
                lastPathsGraphVertex.rest.delta - lastPathsGraphVertex.delta);
        }
        if (lastPathsGraphVertex.cross != null) {
            addExtension(path, lastPathsGraphVertex.cross, lastPathsGraphVertex.cross.delta);
        }
    }

    /**
     * Adds an extension of {@code paths} with {@code extendingVertex} being its last element.
     *
     * @param path path to put extension of
     * @param extendingVertex vertex to extend path with
     * @param weight weight of the resulting path
     */
    private void addExtension(
        EppsteinGraphPath path, PathsGraphVertex extendingVertex, double weight)
    {
        List<PathsGraphVertex> sidetracks = new ArrayList<>(path.pathsGraphVertices);
        sidetracks.add(extendingVertex);

        pathsQueue
            .add(
                new EppsteinGraphPath(
                    graph, sidetracks, distanceAndPredecessorMap, path.weight + weight));
    }

    /**
     * Guides the building process of the paths graph. The process is divided into three stages.
     * First the D(g) is constructed, then cross edges are added and finally the root vertex is
     * created.
     */
    private void buildPathsGraph()
    {
        buildDGraph();
        addCrossEdges();
        addPathGraphRoot();
    }

    /**
     * If the {@code graph} is denoted by $G$, then for every vertex $v$ reachable from
     * {@code source} in $G$ $D(G)$ contains balanced heaps of all outroots, which corresponds to
     * vertices on the path from $v$ to {@code sink}. If there are no sidetracks on the path from
     * $v$ to {@code sink}, the value $null$ is stored. An outroot is connected to its rest heap if
     * the corresponding vertex has more than one sidetrack.
     */
    private void buildDGraph()
    {
        DepthFirstIterator<V, E> it = new DepthFirstIterator<>(graph, source);
        Deque<V> stack = new ArrayDeque<>();
        while (it.hasNext()) {
            V vertex = it.next();
            if (!distanceAndPredecessorMap.containsKey(vertex)) { // sink is unreachable from vertex
                continue;
            }
            if (!hMapping.containsKey(vertex)) { // heap has not been built yet
                stack.addLast(vertex);
                while (!stack.isEmpty()) {
                    V v = stack.peekLast();

                    if (v.equals(sink)) {
                        stack.removeLast();
                        insertVertex(v, null);
                    } else {
                        V predecessor = Graphs
                            .getOppositeVertex(
                                graph, distanceAndPredecessorMap.get(v).getSecond(), v);

                        if (hMapping.containsKey(predecessor)) {
                            stack.removeLast();
                            PathsGraphVertex predecessorH = hMapping.get(predecessor);
                            insertVertex(v, predecessorH);
                        } else {
                            stack.addLast(predecessor);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds cross edges for every vertex $v$ reachable from the root of balanced heap of
     * {@code source} in the paths graph. If a sidetrack, which corresponds to $v$ connects some
     * pair of vertices $(u,w)$, a cross edge from $v$ to the root of the balanced heap of $w$ is
     * added.
     */
    private void addCrossEdges()
    {
        Queue<PathsGraphVertex> queue = new ArrayDeque<>();
        PathsGraphVertex sourceMapping = hMapping.get(source);
        Set<PathsGraphVertex> seen = new HashSet<>();
        if (sourceMapping != null) { // no sidetracks on the paths from source to sink
            queue.add(sourceMapping);
            while (!queue.isEmpty()) {
                PathsGraphVertex v = queue.remove();
                seen.add(v);
                V target = graph.getEdgeTarget(v.edge);
                v.cross = hMapping.get(target);

                if (v.left != null && !seen.contains(v.left)) {
                    queue.add(v.left);
                }
                if (v.right != null && !seen.contains(v.right)) {
                    queue.add(v.right);
                }
                if (v.rest != null && !seen.contains(v.rest)) {
                    queue.add(v.rest);
                }
                if (v.cross != null && !seen.contains(v.cross)) {
                    queue.add(v.cross);
                }
            }
        }
    }

    /**
     * Creates the root vertex $r$ of the paths graph and connects it to the root of the balanced
     * heap of {@code source}.
     */
    private void addPathGraphRoot()
    {
        PathsGraphVertex root = new PathsGraphVertex(null, 0);
        root.cross = hMapping.get(source);
        pathsGraphRoot = root;
    }

    /**
     * Guides the process of adding the sidetracks of {@code v} to the paths graph. First receives
     * the outroot and root of the rest heap of {@code v} by calling
     * {@code getOutrootAndRestHeapRoot(Object)}. If the outroot if $null$ maps $v$ to
     * {@code predecessorHeap} in {@code hMapping}. Otherwise inserts outroot of $v$ in the balanced
     * heap rooted at {@code predecessorHeap} and links it to the received rest heap root.
     *
     * @param v vertex
     * @param predecessorHeap balanced heap root
     */
    private void insertVertex(V v, PathsGraphVertex predecessorHeap)
    {
        Pair<PathsGraphVertex, PathsGraphVertex> p = getOutrootAndRestHeapRoot(v);
        PathsGraphVertex outroot = p.getFirst();
        PathsGraphVertex restHeapRoot = p.getSecond();

        if (outroot == null) {
            hMapping.put(v, predecessorHeap);
        } else {
            PathsGraphVertex mappingVertex = insertPersistently(predecessorHeap, outroot);
            hMapping.put(v, mappingVertex);
            mappingVertex.rest = restHeapRoot;
        }
    }

    /**
     * Inserts {@code vertex} into the balanced heap rooted at {@code root} in a persistent
     * (non-destructive) way. Return root of the modified heap.
     *
     * @param root root of a balanced heap
     * @param vertex vertex to be inserted
     * @return root of the modified heap
     */
    private PathsGraphVertex insertPersistently(PathsGraphVertex root, PathsGraphVertex vertex)
    {
        if (root == null) {
            vertex.left = null;
            vertex.right = null;
            vertex.size = 1;
            return vertex;
        } else {
            PathsGraphVertex rootCopy = new PathsGraphVertex(root);

            boolean leftDirection =
                root.left == null || (root.right != null && root.left.size <= root.right.size);

            PathsGraphVertex min;
            PathsGraphVertex max;
            if (vertex.delta >= rootCopy.delta) {
                min = rootCopy;
                max = vertex;
            } else {
                vertex.left = rootCopy.left;
                vertex.right = rootCopy.right;
                vertex.size = rootCopy.size;

                rootCopy.left = null;
                rootCopy.right = null;

                min = vertex;
                max = rootCopy;
            }
            if (leftDirection) {
                min.left = insertPersistently(min.left, max);
            } else {
                min.right = insertPersistently(min.right, max);
            }
            min.size++;
            return min;
        }
    }

    /**
     * Builds outroot and heapification of other sidetracks of {@code v}.
     *
     * @param v vertex
     * @return outroot and rest heap root
     */
    private Pair<PathsGraphVertex, PathsGraphVertex> getOutrootAndRestHeapRoot(V v)
    {
        List<PathsGraphVertex> restHeapElements = new ArrayList<>();

        PathsGraphVertex outroot = new PathsGraphVertex(null, Double.POSITIVE_INFINITY); // dummy
                                                                                         // vertex
        E predecessor = distanceAndPredecessorMap.get(v).getSecond();
        for (E e : graph.outgoingEdgesOf(v)) {
            if (distanceAndPredecessorMap.containsKey(graph.getEdgeTarget(e))) {
                if (!e.equals(predecessor)) {
                    double delta = delta(e);
                    if (delta < outroot.delta) {
                        if (outroot.edge != null) {
                            restHeapElements.add(outroot);
                        }
                        outroot = new PathsGraphVertex(e, delta);
                    } else {
                        restHeapElements.add(new PathsGraphVertex(e, delta));
                    }
                }
            }
        }

        PathsGraphVertex restHeapRoot = null;
        int size = restHeapElements.size();
        if (size > 0) {
            heapify(restHeapElements, size);
            restHeapRoot = getRestHeap(restHeapElements, 0, size);
        }

        if (outroot.edge == null) { // it is still dummy vertex
            return new Pair<>(null, restHeapRoot);
        } else {
            return new Pair<>(outroot, restHeapRoot);
        }
    }

    /**
     * Builds a min-heap out of the {@code vertices} list
     *
     * @param vertices vertices
     * @param size size of vertices
     */
    private void heapify(List<PathsGraphVertex> vertices, int size)
    {
        for (int i = size / 2 - 1; i >= 0; i--) {
            siftDown(vertices, i, size);
        }
    }

    private void siftDown(List<PathsGraphVertex> vertices, int i, int size)
    {
        int left;
        int right;
        int smaller;
        int current = i;

        while (true) {
            left = 2 * current + 1;
            right = 2 * current + 2;
            smaller = current;

            if (left < size && vertices.get(left).compareTo(vertices.get(smaller)) < 0) {
                smaller = left;
            }

            if (right < size && vertices.get(right).compareTo(vertices.get(smaller)) < 0) {
                smaller = right;
            }

            if (smaller == current) {
                break;
            }
            swap(vertices, current, smaller);
            current = smaller;
        }
    }

    /**
     * Constructs an explicit tree-like representation of the binary heap contained in
     * {@code vertices} starting at position {@code i}.
     *
     * @param vertices heapified vertices
     * @param i heap start position
     * @param size size of vertices
     * @return root of the built heap
     */
    private PathsGraphVertex getRestHeap(List<PathsGraphVertex> vertices, int i, int size)
    {
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        if (l < size) {
            vertices.get(i).left = getRestHeap(vertices, l, size);
        }
        if (r < size) {
            vertices.get(i).right = getRestHeap(vertices, r, size);
        }
        return vertices.get(i);
    }

    private void swap(List<PathsGraphVertex> vertices, int i, int j)
    {
        if (i != j) {
            PathsGraphVertex tmp = vertices.get(i);
            vertices.set(i, vertices.get(j));
            vertices.set(j, tmp);
        }
    }

    /**
     * Calculates the $\delta(e)$ value for a given edge {@code e}.
     *
     * @param e edge
     * @return value of $\delta(e)$
     */
    private double delta(E e)
    {
        return graph.getEdgeWeight(e)
            + distanceAndPredecessorMap.get(graph.getEdgeTarget(e)).getFirst()
            - distanceAndPredecessorMap.get(graph.getEdgeSource(e)).getFirst();
    }

    /**
     * Represents a path that is generated during the computations.
     */
    private class EppsteinGraphPath
        implements
        GraphPath<V, E>,
        Comparable<EppsteinGraphPath>
    {

        /**
         * The graph.
         */
        private Graph<V, E> graph;

        /**
         * Vertices of the paths graph this path corresponds to.
         */
        private List<PathsGraphVertex> pathsGraphVertices;

        /**
         * Shortest paths tree in the edge reversed graph {@code graph} rooted at {@code sink}.
         */
        private Map<V, Pair<Double, E>> distanceAndPredecessorMap;

        /**
         * Weight of tha path.
         */
        private double weight;

        EppsteinGraphPath(
            Graph<V, E> graph, List<PathsGraphVertex> pathsGraphVertices,
            Map<V, Pair<Double, E>> distanceAndPredecessorMap, double weight)
        {
            this.graph = graph;
            this.pathsGraphVertices = pathsGraphVertices;
            this.distanceAndPredecessorMap = distanceAndPredecessorMap;
            this.weight = weight;
        }

        @Override
        public Graph<V, E> getGraph()
        {
            return graph;
        }

        @Override
        public V getStartVertex()
        {
            return source;
        }

        @Override
        public V getEndVertex()
        {
            return sink;
        }

        @Override
        public double getWeight()
        {
            return weight;
        }

        /**
         * Given the implicit representation of the path between {@code source} and {@code sink}
         * constructs the edge list of the path.
         *
         * @return edge list of the path
         */
        @Override
        public List<E> getEdgeList()
        {
            List<PathsGraphVertex> sidetracks = getSidetracks(pathsGraphVertices);
            List<E> result = new ArrayList<>();

            Iterator<PathsGraphVertex> it = sidetracks.iterator();

            V shortestPathSource = source;
            PathsGraphVertex sidetrack = null;
            if (it.hasNext()) {
                sidetrack = it.next();
            }
            while (sidetrack != null) {
                V sidetrackSource = graph.getEdgeSource(sidetrack.edge);
                while (!shortestPathSource.equals(sidetrackSource)) {
                    E shortestPathEdge =
                        distanceAndPredecessorMap.get(shortestPathSource).getSecond();
                    result.add(shortestPathEdge);
                    shortestPathSource =
                        Graphs.getOppositeVertex(graph, shortestPathEdge, shortestPathSource);
                }

                PathsGraphVertex curr = sidetrack;
                PathsGraphVertex next = null;
                while (it.hasNext()) {
                    next = it.next();
                    if (graph.getEdgeTarget(curr.edge).equals(graph.getEdgeSource(next.edge))) {
                        result.add(curr.edge);
                        curr = next;
                        next = null;
                    } else {
                        break;
                    }
                }
                result.add(curr.edge);

                sidetrack = next;
                shortestPathSource = graph.getEdgeTarget(curr.edge);
            }

            // only shortest path edges are left
            while (!shortestPathSource.equals(sink)) {
                E edge = distanceAndPredecessorMap.get(shortestPathSource).getSecond();
                result.add(edge);
                shortestPathSource = graph.getEdgeTarget(edge);
            }

            return result;
        }

        /**
         * Builds sequence of sidetracks in the {@code graph} this path corresponds to.
         *
         * @param vertices vertices of the paths graph
         * @return list of sidetracks
         */
        private List<PathsGraphVertex> getSidetracks(List<PathsGraphVertex> vertices)
        {
            if (vertices.size() > 1) {
                List<Integer> toBeRemoved = new ArrayList<>();
                Iterator<PathsGraphVertex> it = vertices.iterator();
                PathsGraphVertex curr = it.next();
                PathsGraphVertex next;
                int currPosition = 0;
                while (it.hasNext()) {
                    next = it.next();
                    if (curr.left == next || curr.right == next || curr.rest == next) {
                        toBeRemoved.add(currPosition);
                    }
                    curr = next;
                    currPosition++;
                }

                List<PathsGraphVertex> result =
                    new ArrayList<>(vertices.size() - toBeRemoved.size());
                int size = toBeRemoved.size();
                for (int i = 0, j = 0; i < vertices.size(); i++) {
                    if (j < size && toBeRemoved.get(j).equals(i)) {
                        j++;
                    } else {
                        result.add(vertices.get(i));
                    }
                }
                return result;
            }
            return vertices;
        }

        @Override
        public int compareTo(EppsteinGraphPath o)
        {
            return Double.compare(weight, o.weight);
        }
    }

    /**
     * Vertex of the paths graph. Does not maintain the weights of the edges to {@code left},
     * {@code right}, {@code rest} and {@code cross} vertices, because they are computed during the
     * paths graph traversal.
     */
    private class PathsGraphVertex
        implements
        Comparable<PathsGraphVertex>
    {

        /**
         * Edge this vertex corresponds to.
         */
        E edge;

        /**
         * $Delta(edge)$ value.
         */
        double delta;

        /**
         * If this vertex is part of a balanced heap of outroots in the path graph, this value is
         * used to determine where a new vertex should be inserted in order for the heap to remain
         * balanced.
         */
        int size;

        // Connections to other vertices in the paths graph.
        PathsGraphVertex left;
        PathsGraphVertex right;
        PathsGraphVertex rest;
        PathsGraphVertex cross;

        PathsGraphVertex(E edge, double delta)
        {
            this.edge = edge;
            this.delta = delta;
            this.size = 1;
        }

        /**
         * Copy constructor.
         *
         * @param other other vertex
         */
        PathsGraphVertex(PathsGraphVertex other)
        {
            this.edge = other.edge;
            this.size = other.size;
            this.delta = other.delta;
            this.left = other.left;
            this.right = other.right;
            this.cross = other.cross;
            this.rest = other.rest;
        }

        @Override
        public int compareTo(PathsGraphVertex o)
        {
            return Double.compare(delta, o.delta);
        }
    }
}
