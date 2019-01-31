/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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

import org.jgrapht.*;

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
 * <i>Lexicographic Breadth First Search – A Survey</i></a>. In: Hromkovič J., Nagl M., Westfechtel
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
 */
public class LexBreadthFirstIterator<V, E>
    extends
    AbstractGraphIterator<V, E>
{

    /**
     * Reference to the {@code BucketList} that contains unvisited vertices.
     */
    private BucketList bucketList;

    /**
     * Contains current vertex of the {@code graph}.
     */
    private V current;

    /**
     * Creates new lexicographical breadth-first iterator for {@code graph}.
     *
     * @param graph the graph to be iterated.
     */
    public LexBreadthFirstIterator(Graph<V, E> graph)
    {
        super(graph);
        GraphTests.requireUndirected(graph);
        bucketList = new BucketList(graph.vertexSet());
    }

    /**
     * Checks whether there exist unvisited vertices.
     *
     * @return true if there exist unvisited vertices.
     */
    @Override
    public boolean hasNext()
    {
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
    public V next()
    {
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
    public boolean isCrossComponentTraversal()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Trying to disable the cross components nature of this iterator will result into throwing a
     * {@link IllegalArgumentException}.
     */
    @Override
    public void setCrossComponentTraversal(boolean crossComponentTraversal)
    {
        if (!crossComponentTraversal) {
            throw new IllegalArgumentException("Iterator is always cross-component");
        }
    }

    /**
     * Retrieves vertex from the {@code bucketList} and returns it.
     *
     * @return the vertex retrieved from the {@code bucketList}.
     */
    private V advance()
    {
        V vertex = bucketList.poll();
        if (vertex != null) {
            bucketList.updateBuckets(getUnvisitedNeighbours(vertex));
        }
        return vertex;
    }

    /**
     * Computes and returns neighbours of {@code vertex} which haven't been visited by this
     * iterator.
     *
     * @param vertex the vertex, whose neighbours are being explored.
     * @return neighbours of {@code vertex} which have yet to be visited by this iterator.
     */
    private Set<V> getUnvisitedNeighbours(V vertex)
    {
        Set<V> unmapped = new HashSet<>();
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
     * Data structure for performing lexicographical breadth-first search. Allows to add and
     * retrieve vertices from buckets, update their buckets after a new vertex has been added to the
     * LexBFS order. Labels aren't used explicitly, which results in time and space optimization.
     *
     * @author Timofey Chudakov
     */
    class BucketList
    {
        /**
         * Bucket with the vertices that have lexicographically largest label.
         */
        private Bucket head;
        /**
         * Map for mapping vertices to buckets they are currently in. Is used for finding the bucket
         * of the vertex in constant time.
         */
        private Map<V, Bucket> bucketMap;

        /**
         * Creates a {@code BucketList} with a single bucket and all specified {@code vertices} in
         * it.
         *
         * @param vertices the vertices of the graph, that should be stored in the {@code head}
         *        bucket.
         */
        BucketList(Collection<V> vertices)
        {
            head = new Bucket(vertices);
            bucketMap = new HashMap<>(vertices.size());
            for (V vertex : vertices) {
                bucketMap.put(vertex, head);
            }
        }

        /**
         * Checks whether there exists a bucket with the specified {@code vertex}.
         *
         * @param vertex the vertex whose presence in some {@code Bucket} in this {@code BucketList}
         *        is checked.
         * @return <tt>true</tt> if there exists a bucket with {@code vertex} in it, otherwise
         *         <tt>false</tt>.
         */
        boolean containsBucketWith(V vertex)
        {
            return bucketMap.containsKey(vertex);
        }

        /**
         * Retrieves element from the head bucket by invoking {@link Bucket#poll()} or null if this
         * {@code BucketList} is empty.
         * <p>
         * Removes the head bucket if it becomes empty after the operation.
         *
         * @return vertex returned by {@link Bucket#poll()} invoked on head bucket or null if this
         *         {@code BucketList} is empty.
         */
        V poll()
        {
            if (bucketMap.size() > 0) {
                V res = head.poll();
                bucketMap.remove(res);
                if (head.isEmpty()) {
                    head = head.next;
                    if (head != null) {
                        head.prev = null;
                    }
                }
                return res;
            } else {
                return null;
            }
        }

        /**
         * For every bucket B in this {@code BucketList}, which contains vertices from the set
         * {@code
         * vertices}, creates a new {@code Bucket} B' and moves vertices from B to B' according to
         * the following rule: $B' = B\cap vertices$ and $B = B\backslash B'$. For every such
         * {@code Bucket} B only one {@code Bucket} B' is created. If some bucket B becomes empty
         * after this operation, it is removed from the data structure.
         *
         * @param vertices the vertices, that should be moved to new buckets.
         */
        void updateBuckets(Set<V> vertices)
        {
            Set<Bucket> visitedBuckets = new HashSet<>();
            for (V vertex : vertices) {
                Bucket bucket = bucketMap.get(vertex);
                if (visitedBuckets.contains(bucket)) {
                    bucket.prev.addVertex(vertex);
                    bucketMap.put(vertex, bucket.prev);
                } else {
                    visitedBuckets.add(bucket);
                    Bucket newBucket = new Bucket(vertex);
                    newBucket.insertBefore(bucket);
                    bucketMap.put(vertex, newBucket);
                    if (head == bucket) {
                        head = newBucket;
                    }
                }
                bucket.removeVertex(vertex);
                if (bucket.isEmpty()) {
                    visitedBuckets.remove(bucket);
                    bucket.removeSelf();
                }
            }
        }

        /**
         * Plays the role of the container of vertices. All vertices stored in a bucket have
         * identical label. Labels aren't used explicitly.
         * <p>
         * Encapsulates operations of addition and removal of vertices from the bucket and removal
         * of a bucket from the data structure.
         */
        private class Bucket
        {
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
            private Set<V> vertices;

            /**
             * Creates a new bucket with all {@code vertices} stored in it.
             *
             * @param vertices vertices to store in this bucket.
             */
            Bucket(Collection<V> vertices)
            {
                this.vertices = new HashSet<>(vertices);
            }

            /**
             * Creates a new Bucket with a single {@code vertex} in it.
             *
             * @param vertex the vertex to store in this bucket.
             */
            Bucket(V vertex)
            {
                this.vertices = new HashSet<>();
                vertices.add(vertex);
            }

            /**
             * Removes the {@code vertex} from this bucket.
             *
             * @param vertex the vertex to remove.
             */
            void removeVertex(V vertex)
            {
                vertices.remove(vertex);
            }

            /**
             * Removes this bucket from the data structure.
             */
            void removeSelf()
            {
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
             * @param bucket the bucket, that will be the next to this bucket.
             */
            void insertBefore(Bucket bucket)
            {
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
             * Adds the {@code vertex} to this bucket.
             *
             * @param vertex the vertex to add.
             */
            void addVertex(V vertex)
            {
                vertices.add(vertex);
            }

            /**
             * Retrieves one vertex from this bucket.
             *
             * @return vertex, that was removed from this bucket, null if the bucket was empty.
             */
            V poll()
            {
                if (vertices.isEmpty()) {
                    return null;
                } else {
                    V vertex = vertices.iterator().next();
                    vertices.remove(vertex);
                    return vertex;
                }
            }

            /**
             * Checks whether this bucket is empty.
             *
             * @return <tt>true</tt> if this bucket doesn't contain any elements, otherwise false.
             */
            boolean isEmpty()
            {
                return vertices.size() == 0;
            }
        }
    }
}
