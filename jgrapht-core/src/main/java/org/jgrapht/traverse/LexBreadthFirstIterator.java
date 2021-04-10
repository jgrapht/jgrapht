/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.util.Ordering;

import java.util.*;

/**
 * A lexicographical breadth-first iterator for an undirected graph.
 * <p>
 * Every vertex has an implicit label (they aren't used explicitly in order to reduce time and
 * memory complexity). When some vertex is returned by this iterator, its index is the number of
 * vertices in this graph minus number of already returned vertices. For a given vertex v its label
 * is a concatenation of indices of already returned vertices, that were also its neighbours, with
 * some separator between them. For example, 7#4#3 is a valid vertex label.
 * <p>
 * Iterator chooses vertex with lexicographically largest label and returns it. It breaks ties
 * arbitrarily. For more information on lexicographical BFS see the following article: Corneil D.G.
 * (2004) <a href="https://pdfs.semanticscholar.org/d4b5/a492f781f23a30773841ec79c46d2ec2eb9c.pdf">
 * <i>Lexicographic Breadth First Search – A Survey</i></a>. In: Hromkovic J., Nagl M., Westfechtel
 * B. (eds) Graph-Theoretic Concepts in Computer Science. WG 2004. Lecture Notes in Computer
 * Science, vol 3353. Springer, Berlin, Heidelberg; and the following
 * paper:<a href="http://www.cse.iitd.ac.in/~naveen/courses/CSL851/uwaterloo.pdf"><i>CS 762:
 * Graph-theoretic algorithms. Lecture notes of a graduate course. University of Waterloo</i></a>.
 * <p>
 * For this iterator to work correctly the graph must not be modified during iteration. Currently
 * there are no means to ensure that, nor to fail-fast. The results of such modifications are
 * undefined.
 * <p>
 * Note: only vertex events are fired by this iterator.
 *
 * @param <V> the graph vertex type.
 * @param <E> the graph edge type.
 * @author Timofey Chudakov
 * @author Oliver Feith
 * @author Dennis Fischer
 * @author Daniel Mock
 * @since March 2018
 */
public class LexBreadthFirstIterator<V, E>
        extends
        AbstractGraphIterator<V, E> {

    private final Mode mode;
    /**
     * Reference to the {@code BucketList} that contains unvisited vertices.
     */
    private final BucketList bucketList;
    /**
     * Contains current vertex of the {@code graph}.
     */
    private V current;
    /**
     * Lookup tables for the sorted neighborhoods according to the imposed tiebreaking orders.
     */
    private Map<V, List<V>> sortedNeighbors;


    /*
     *  Attributed needed for LBFS+, LBFS*, not LBFS
     */
    private Map<V, List<V>> sortedNeighborsB = null; // only LBFS*
    private final Ordering<V> priorityA;
    private final Ordering<V> priorityB; // only LBFS*
    /**
     * LBFS(*) static parameters
     */
    private Ordering<V> neighborIndexA = null;
    private Ordering<V> neighborIndexB = null;
    private HashMap<V, Set<V>> aSets = null;
    private HashMap<V, Set<V>> bSets = null;
    /**
     * Creates new lexicographical breadth-first (LBFS*) iterator with a static priority list for
     * {@code graph}.
     * <p>
     * This is a variant for the LBFS* algorithm used for interval graph detection, the terminology
     * used follows the original paper
     * (<a href= "https://webdocs.cs.ualberta.ca/~stewart/Pubs/IntervalSIAM.pdf">
     * https://webdocs.cs.ualberta.ca/~stewart/Pubs/IntervalSIAM.pdf</a>, <i>The LBFS Structure and
     * Recognition of Interval Graphs. SIAM J. Discrete Math.. 23. 1905-1953.
     * 10.1137/S0895480100373455.</i>) by Derek Corneil, Stephan Olariu and Lorna Stewart.
     *
     * @param graph          the graph to be iterated.
     * @param priorityA      An ordering of the vertices resulting from a previous LBFS run.
     * @param priorityB      An ordering of the vertices resulting from a previous LBFS run.
     * @param neighborIndexA The A neighboring list
     * @param neighborIndexB The B neighboring list
     * @param aSets          The A sets
     * @param bSets          The B sets
     */
    public LexBreadthFirstIterator(
            Graph<V, E> graph, Ordering<V> priorityA, Ordering<V> priorityB,
            Ordering<V> neighborIndexA, Ordering<V> neighborIndexB,
            HashMap<V, Set<V>> aSets, HashMap<V, Set<V>> bSets) {
        this(graph, priorityA, priorityB, neighborIndexA, neighborIndexB, aSets, bSets, Mode.LBFS_STAR);
    }


    private LexBreadthFirstIterator(
            Graph<V, E> graph, Ordering<V> priorityA, Ordering<V> priorityB,
            Ordering<V> neighborIndexA, Ordering<V> neighborIndexB,
            HashMap<V, Set<V>> aSets, HashMap<V, Set<V>> bSets, Mode mode) {
        super(graph);
        GraphTests.requireUndirected(graph);

        // check that orderings and vertex set are compatible
        if (mode != Mode.LBFS) {
            boolean k = priorityA.size() == graph.vertexSet().size();
            for (V vertex : graph.vertexSet()) {
                k &= priorityA.contains(vertex);
                if (!k) {
                    throw new IllegalArgumentException();
                }
            }

            if (mode == Mode.LBFS_STAR) {
                k &= priorityB.size() == priorityA.size();
                for (V vertex : graph.vertexSet()) {
                    k &= priorityA.contains(vertex);
                    if (!k) {
                        throw new IllegalArgumentException();
                    }
                }
            }
        }

        this.mode = mode;
        this.priorityA = priorityA;
        this.priorityB = priorityB;
        this.neighborIndexA = neighborIndexA;
        this.neighborIndexB = neighborIndexB;
        this.aSets = aSets;
        this.bSets = bSets;

        LinkedHashSet<V> verticesA;
        LinkedHashSet<V> verticesB = null;
        if (mode == Mode.LBFS) {
            verticesA = new LinkedHashSet<>(graph.vertexSet());
        } else {
            verticesA = new LinkedHashSet<>(graph.vertexSet().size());
            priorityA.forEach(verticesA::add);

            if (mode == Mode.LBFS_STAR) {
                verticesB = new LinkedHashSet<>(graph.vertexSet().size());
                priorityB.forEach(verticesB::add);
            }
        }

        bucketList = new BucketList(verticesA, verticesB);

        // Precompute sorted neighborhoods
        if (mode != Mode.LBFS) {
            this.sortedNeighbors = computeSortedNeighborhoods(priorityA);
            if (mode == Mode.LBFS_STAR) {
                this.sortedNeighborsB = computeSortedNeighborhoods(priorityB);
            }
        }
    }

    /**
     * Creates a new lexical breadth-first search (LBFS) iterator from {@code graph}.
     *
     * @param graph the graph to be iterated.
     */
    public LexBreadthFirstIterator(Graph<V, E> graph) {
        this(graph, null, null, null, null, null, null, Mode.LBFS);
    }


    /**
     * The LBFS+ Iterator. A previous LBFS ordering is needed. Instead of ties arbitrarily when choosing a vertex with
     * lexicographically the largest label, LBFS+ chooses the last vertex in the ordering.
     *
     * @param graph    the graph to be iterated.
     * @param priority An ordering of the vertices resulting from a previous LBFS run.
     */
    public LexBreadthFirstIterator(Graph<V, E> graph, Ordering<V> priority) {
        this(graph, priority, null, null, null, null, null, Mode.LBFS_PLUS);
    }

    /**
     * Helper function to compute the neighborhoods sorted by the vertex ordering for efficiency.
     * Must be called before using the iterator. Uses linear, i.e. O(|V| + |E|) time.
     *
     * @param ordering The ordering.
     * @return A map from the vertices to lists containing their neighborhoods (sorted by the vertex
     * ordering)
     */
    private HashMap<V, List<V>> computeSortedNeighborhoods(Ordering<V> ordering) {
        HashMap<V, List<V>> neighborhoodMap = new HashMap<>();

        for (int priority = graph.vertexSet().size() - 1; priority >= 0; priority--) {
            // get vertex with priority
            V vertex = ordering.getElementAt(priority);

            // if needed, initialize the neighbor list of this vertex
            if (!neighborhoodMap.containsKey(vertex)) {
                neighborhoodMap.put(vertex, new ArrayList<>(graph.inDegreeOf(vertex)));
            }

            // add vertex to the neighbor lists of its neighbors
            for (V neighbor : Graphs.neighborSetOf(graph, vertex)) {
                if (neighborhoodMap.containsKey(neighbor)) {
                    neighborhoodMap.get(neighbor).add(vertex);
                } else {
                    List<V> neighborList = new ArrayList<>(graph.inDegreeOf(neighbor));
                    neighborList.add(vertex);

                    neighborhoodMap.put(neighbor, neighborList);
                }
            }
        }

        return neighborhoodMap;
    }

    /**
     * Checks whether there exist unvisited vertices.
     *
     * @return true if there exist unvisited vertices.
     */
    @Override
    public boolean hasNext() {
        if (current != null) {
            return true;
        }
        current = advance();
        if (current != null && nListeners != 0) {
            fireVertexTraversed(createVertexTraversalEvent(current));
        }
        return current != null;
    }

    /**
     * Returns the next vertex in the ordering.
     *
     * @return the next vertex in the ordering.
     */
    @Override
    public V next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        V result = current;
        current = null;
        if (nListeners != 0) {
            fireVertexFinished(createVertexTraversalEvent(result));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always returns true since this iterator doesn't care about connected components.
     */
    @Override
    public boolean isCrossComponentTraversal() {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Trying to disable the cross components nature of this iterator will result into throwing a
     * {@link IllegalArgumentException}.
     */
    @Override
    public void setCrossComponentTraversal(boolean crossComponentTraversal) {
        if (!crossComponentTraversal) {
            throw new IllegalArgumentException("Iterator is always cross-component");
        }
    }

    /**
     * Retrieves vertex from the {@code bucketList} and returns it.
     *
     * @return the vertex retrieved from the {@code bucketList}.
     */
    private V advance() {
        V vertex = bucketList.poll();
        if (vertex != null) {
            bucketList.updateBuckets(vertex);
        }
        return vertex;
    }

    private enum Mode {
        LBFS,
        LBFS_PLUS,
        LBFS_STAR
    }

    /**
     * Data structure for performing lexicographical breadth-first search. Allows to add and
     * retrieve vertices from buckets, update their buckets after a new vertex has been added to the
     * LexBFS order. Labels aren't used explicitly, which results in time and space optimization.
     *
     * @author Timofey Chudakov
     */
    class BucketList {
        /**
         * Bucket with the vertices that have lexicographically largest label.
         */
        private Bucket head;
        /**
         * Map for mapping vertices to buckets they are currently in. Is used for finding the bucket
         * of the vertex in constant time.
         */
        private final Map<V, Bucket> bucketMap;

        /**
         * Creates a {@code BucketList} with a single bucket and all specified {@code vertices} in
         * it.
         * The Sets have to contain the same elements.
         *
         * @param verticesA The vertices sorted by the first ordering
         * @param verticesB The vertices sorted by the second ordering
         */
        BucketList(LinkedHashSet<V> verticesA, LinkedHashSet<V> verticesB) {
            head = new Bucket(verticesA, verticesB);

            bucketMap = new HashMap<>(verticesA.size());
            for (V vertex : verticesA) {
                bucketMap.put(vertex, head);
            }
        }


        /**
         * Checks whether there exists a bucket with the specified {@code vertex}.
         *
         * @param vertex The vertex whose presence in some {@code Bucket} in this {@code BucketList}
         *               is checked.
         * @return <tt>true</tt> if there exists a bucket with {@code vertex} in it, otherwise
         * <tt>false</tt>.
         */
        boolean containsBucketWith(V vertex) {
            return bucketMap.containsKey(vertex);
        }

        /**
         * Retrieves element from the head bucket by invoking {@link Bucket#poll()} or null if this
         * {@code BucketList} is empty.
         * <p>
         * Removes the head bucket if it becomes empty after the operation.
         *
         * @return Vertex returned by {@link Bucket#poll()} invoked on head bucket or null if this
         * {@code BucketList} is empty.
         */
        V poll() {
            if (bucketMap.size() <= 0) {
                return null;
            }
            // Poll the vertex according to the tiebreaking rules
            V res = head.poll();

            bucketMap.remove(res);
            if (head.isEmpty()) {
                head = head.next;
                if (head != null) {
                    head.prev = null;
                }
            }
            return res;
        }

        /**
         * Split every bucket $B$ into buckets $B \cap N(vertex)$ and $B \setminus N(vertex)$. For
         * every such {@code Bucket} B only one new {@code Bucket} B' is created. If some bucket B
         * becomes empty after this operation, it is removed from the data structure.
         *
         * @param vertex The vertex whose neighborhood is used for splitting.
         */
        void updateBuckets(V vertex) {
            // Initialize the neighbors
            List<V> neighbors;
            if (mode == Mode.LBFS) {
                neighbors = getUnvisitedNeighbours(vertex);
            } else { // if LBFS+ or LBFS*
                neighbors = sortedNeighbors.get(vertex);
                neighbors.removeIf(n -> !containsBucketWith(n)); // remove visited neighbors
                // a vertex which was visited is not in a bucket
            }

            Set<Bucket> visitedBuckets = new HashSet<>(neighbors.size());

            for (V v : neighbors) {
                Bucket bucket = bucketMap.get(v);
                if (visitedBuckets.contains(bucket)) {
                    bucket.prev.vertices.add(v);
                    bucketMap.put(v, bucket.prev);
                } else {
                    visitedBuckets.add(bucket);
                    Bucket newBucket = new Bucket(new LinkedHashSet<>(), mode == Mode.LBFS_STAR ? new LinkedHashSet<>() : null);
                    newBucket.vertices.add(v);
                    newBucket.insertBefore(bucket);
                    bucketMap.put(v, newBucket);
                    if (head == bucket) {
                        head = newBucket;
                    }
                }
                bucket.vertices.remove(v);
                if (mode == Mode.LBFS_STAR) {
                    bucket.verticesB.remove(v);
                }
                if (bucket.isEmpty()) {
                    visitedBuckets.remove(bucket);
                    bucket.removeSelf();
                }
            }

            if (mode == Mode.LBFS_STAR) {
                assert sortedNeighborsB != null;
                List<V> neighborsB = sortedNeighborsB.get(vertex);
                neighborsB.removeIf(n -> !containsBucketWith(n)); // remove visited neighbors
                for (V neighbor : neighborsB) {
                    Bucket bucket = bucketMap.get(neighbor);
                    bucket.verticesB.add(neighbor);
                }
            }

        }

        /**
         * Computes and returns neighbours of {@code vertex} which haven't been visited by this
         * iterator.
         *
         * @param vertex the vertex, whose neighbours are being explored.
         * @return neighbours of {@code vertex} which have yet to be visited by this iterator.
         */
        private List<V> getUnvisitedNeighbours(V vertex) {
            List<V> unmapped = new ArrayList<>();
            Set<E> edges = graph.edgesOf(vertex);
            for (E edge : edges) {
                V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);
                if (bucketList.containsBucketWith(oppositeVertex)) {
                    unmapped.add(oppositeVertex);
                }
            }
            return unmapped;
        }


        /**
         * Plays the role of the container of vertices. All vertices stored in a bucket have
         * identical label. Labels aren't used explicitly.
         * <p>
         * Encapsulates operations of addition and removal of vertices from the bucket and removal
         * of a bucket from the data structure.
         */
        private class Bucket {
            /**
             * Reference of the bucket with lexicographically smaller label.
             */
            private Bucket next;
            /**
             * Reference of the bucket with lexicographically larger label.
             */
            private Bucket prev;
            /**
             * Set of vertices currently stored in this bucket.
             */
            private final LinkedHashSet<V> vertices;
            /**
             * Set of vertices currently stored in this bucket (sorted by other order).
             */
            private final LinkedHashSet<V> verticesB;

            /**
             * Creates a new bucket with all {@code vertices} stored in it.
             *
             * @param vertices  A SplitQueue (sorted to priority A) holding the vertices to store in
             *                  this bucket.
             * @param verticesB A SplitQueue (sorted to priority B) holding the vertices to store in
             *                  this bucket.
             */
            Bucket(LinkedHashSet<V> vertices, LinkedHashSet<V> verticesB) {
                this.vertices = vertices;
                this.verticesB = verticesB;
            }

            /**
             * Removes this bucket from the data structure.
             */
            void removeSelf() {
                if (next != null) {
                    next.prev = prev;
                }
                if (prev != null) {
                    prev.next = next;
                }
            }

            /**
             * Inserts this bucket in the data structure before the {@code bucket}.
             *
             * @param bucket The bucket that will be the in front of this bucket.
             */
            void insertBefore(Bucket bucket) {
                this.next = bucket;
                if (bucket != null) {
                    this.prev = bucket.prev;
                    if (bucket.prev != null) {
                        bucket.prev.next = this;
                    }
                    bucket.prev = this;
                } else {
                    this.prev = null;
                }
            }

            /**
             * Retrieves one vertex from this bucket (without special priorities).
             *
             * @return The vertex that was removed from this bucket, null if the bucket was empty.
             */
            V poll() {
                if (vertices.isEmpty()) {
                    return null;
                }

                // When LBFS or LBFS+ is used
                if (mode != Mode.LBFS_STAR) {
                    Iterator<V> it = vertices.iterator();
                    V alpha = it.next();
                    it.remove();

                    return alpha;
                }
                // For LBFS*

                // The first element of vertices and verticesB are peeked
                Iterator<V> it = vertices.iterator();
                Iterator<V> itB = verticesB.iterator();

                V alpha = it.next();
                V beta = itB.next();

                V result;

                // Choose the vertex according to Section 7
                if (neighborIndexA.getPositionOf(alpha) > priorityA.getPositionOf(alpha)) {
                    result = beta; // return Beta
                } else if (neighborIndexB.getPositionOf(beta) > priorityB.getPositionOf(beta)) {
                    result = alpha; // return Alpha
                } else if (bSets.get(beta).isEmpty() || !aSets.get(alpha).isEmpty()) {
                    result = beta; // return Beta
                } else if (Objects.equals(
                        neighborIndexA.getPositionOf(bSets.get(beta).iterator().next()),
                        priorityA.getPositionOf(alpha))) {
                    result = beta; // return Beta
                } else {
                    result = alpha; // return Alpha
                }
                // remove the chosen vertex
                vertices.remove(result);
                verticesB.remove(result);
                return result;
            }

            /**
             * Checks whether this bucket is empty.
             *
             * @return <tt>true</tt> if this bucket doesn't contain any elements, otherwise false.
             */
            boolean isEmpty() {
                return vertices.size() == 0;
            }
        }
    }
}
