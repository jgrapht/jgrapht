/*
 * (C) Copyright 2008-2021, by Peter Giles and Contributors.
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
package org.jgrapht.graph;

import org.jgrapht.graph.builder.*;
import org.jgrapht.traverse.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * A directed acyclic graph (DAG).
 *
 * <p>
 * Implements a DAG that can be modified (vertices &amp; edges added and removed), is guaranteed to
 * remain acyclic, and provides fast topological order iteration. An attempt to add an edge which
 * would induce a cycle throws an {@link IllegalArgumentException}.
 *
 * <p>
 * This is done using a dynamic topological sort which is based on the algorithm described in "David
 * J. Pearce &amp; Paul H. J. Kelly. A dynamic topological sort algorithm for directed acyclic
 * graphs. Journal of Experimental Algorithmics, 11, 2007." (see
 * <a href="http://www.mcs.vuw.ac.nz/~djp/files/PK-JEA07.pdf">paper</a> or
 * <a href="http://doi.acm.org/10.1145/1187436.1210590">ACM link</a> for details). The
 * implementation differs from the algorithm specified in the above paper in some ways, perhaps most
 * notably in that the topological ordering is stored by default using two hash maps, which will
 * have some effects on the runtime, but also allow for vertex addition and removal. This storage
 * mechanism can be adjusted by subclasses.
 *
 * <p>
 * The complexity of adding a new edge in the graph depends on the number of edges incident to the
 * "affected region", and should in general be faster than recomputing the whole topological
 * ordering from scratch. For details about the complexity parameters and running times, see the
 * previously mentioned paper.
 *
 * <p>
 * This class makes no claims to thread safety, and concurrent usage from multiple threads will
 * produce undefined results.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Peter Giles
 */
public class DirectedAcyclicGraph<V, E>
    extends
    AbstractBaseGraph<V, E>
    implements
    Iterable<V>
{
    private static final long serialVersionUID = 4522128427004938150L;

    private static final String EDGE_WOULD_INDUCE_A_CYCLE = "Edge would induce a cycle";

    private final Comparator<V> topoComparator;
    private final TopoOrderMap<V> topoOrderMap;
    private int maxTopoIndex = 0;
    private int minTopoIndex = 0;

    // this update count is used to keep internal topological iterators honest
    private transient long topoModCount = 0;

    /**
     * The visited strategy factory to use. Subclasses can change this.
     */
    private final VisitedStrategyFactory visitedStrategyFactory;

    /**
     * Construct a directed acyclic graph.
     *
     * @param edgeClass the edge class
     */
    public DirectedAcyclicGraph(Class<? extends E> edgeClass)
    {
        this(null, SupplierUtil.createSupplier(edgeClass), false, false);
    }

    /**
     * Construct a directed acyclic graph.
     *
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     * @param weighted if true the graph will be weighted, otherwise not
     */
    public DirectedAcyclicGraph(
        Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, boolean weighted)
    {
        this(
            vertexSupplier, edgeSupplier, new VisitedBitSetImpl(), new TopoVertexBiMap<>(),
            weighted, false);
    }

    /**
     * Construct a directed acyclic graph.
     *
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     * @param weighted if true the graph will be weighted, otherwise not
     * @param allowMultipleEdges if true the graph will allow multiple edges, otherwise not
     */
    public DirectedAcyclicGraph(
        Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, boolean weighted,
        boolean allowMultipleEdges)
    {
        this(
            vertexSupplier, edgeSupplier, new VisitedBitSetImpl(), new TopoVertexBiMap<>(),
            weighted, allowMultipleEdges);
    }

    /**
     * Construct a directed acyclic graph.
     * 
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     * @param visitedStrategyFactory the visited strategy factory. Subclasses can change this
     *        implementation to adjust the performance tradeoffs.
     * @param topoOrderMap the topological order map. For performance reasons, subclasses can change
     *        the way this class stores the topological order.
     * @param weighted if true the graph will be weighted, otherwise not
     */
    protected DirectedAcyclicGraph(
        Supplier<V> vertexSupplier, Supplier<E> edgeSupplier,
        VisitedStrategyFactory visitedStrategyFactory, TopoOrderMap<V> topoOrderMap,
        boolean weighted)
    {
        this(vertexSupplier, edgeSupplier, visitedStrategyFactory, topoOrderMap, weighted, false);
    }

    /**
     * Construct a directed acyclic graph.
     * 
     * @param vertexSupplier the vertex supplier
     * @param edgeSupplier the edge supplier
     * @param visitedStrategyFactory the visited strategy factory. Subclasses can change this
     *        implementation to adjust the performance tradeoffs.
     * @param topoOrderMap the topological order map. For performance reasons, subclasses can change
     *        the way this class stores the topological order.
     * @param weighted if true the graph will be weighted, otherwise not
     * @param allowMultipleEdges if true the graph will allow multiple edges, otherwise not
     */
    protected DirectedAcyclicGraph(
        Supplier<V> vertexSupplier, Supplier<E> edgeSupplier,
        VisitedStrategyFactory visitedStrategyFactory, TopoOrderMap<V> topoOrderMap,
        boolean weighted, boolean allowMultipleEdges)
    {
        super(
            vertexSupplier, edgeSupplier,
            new DefaultGraphType.Builder()
                .directed().allowMultipleEdges(allowMultipleEdges).allowSelfLoops(false)
                .weighted(weighted).allowCycles(false).build());
        this.visitedStrategyFactory =
            Objects.requireNonNull(visitedStrategyFactory, "Visited factory cannot be null");
        this.topoOrderMap =
            Objects.requireNonNull(topoOrderMap, "Topological order map cannot be null");
        this.topoComparator = new TopoComparator();
    }

    /**
     * Create a builder for this kind of graph.
     *
     * @param edgeClass class on which to base factory for edges
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a builder for this kind of graph
     */
    public static <V, E> GraphBuilder<V, E, ? extends DirectedAcyclicGraph<V, E>> createBuilder(
        Class<? extends E> edgeClass)
    {
        return new GraphBuilder<>(new DirectedAcyclicGraph<>(edgeClass));
    }

    /**
     * Create a builder for this kind of graph.
     * 
     * @param edgeSupplier edge supplier for the edges
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     * @return a builder for this kind of graph
     */
    public static <V, E> GraphBuilder<V, E, ? extends DirectedAcyclicGraph<V, E>> createBuilder(
        Supplier<E> edgeSupplier)
    {
        return new GraphBuilder<>(new DirectedAcyclicGraph<>(null, edgeSupplier, false));
    }

    @Override
    public V addVertex()
    {
        V v = super.addVertex();

        if (v != null) {
            // add to the topological map
            ++maxTopoIndex;
            topoOrderMap.putVertex(maxTopoIndex, v);
            ++topoModCount;
        }

        return v;
    }

    @Override
    public boolean addVertex(V v)
    {
        boolean added = super.addVertex(v);

        if (added) {
            // add to the topological map
            ++maxTopoIndex;
            topoOrderMap.putVertex(maxTopoIndex, v);
            ++topoModCount;
        }

        return added;
    }

    @Override
    public boolean removeVertex(V v)
    {
        boolean removed = super.removeVertex(v);

        if (removed) {
            /*
             * Depending on the topoOrderMap implementation, this can leave holes in the topological
             * ordering, which can degrade performance for certain operations over time.
             */
            Integer topoIndex = topoOrderMap.removeVertex(v);

            // if possible contract minTopoIndex
            if (topoIndex == minTopoIndex) {
                while ((minTopoIndex < 0) && (topoOrderMap.getVertex(minTopoIndex) == null)) {
                    ++minTopoIndex;
                }
            }

            // if possible contract maxTopoIndex
            if (topoIndex == maxTopoIndex) {
                while ((maxTopoIndex > 0) && (topoOrderMap.getVertex(maxTopoIndex) == null)) {
                    --maxTopoIndex;
                }
            }

            ++topoModCount;
        }

        return removed;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The complexity of adding a new edge in the graph depends on the number of edges incident to
     * the "affected region", and should in general be faster than recomputing the whole topological
     * ordering from scratch.
     *
     * @throws IllegalArgumentException if the edge would induce a cycle in the graph
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        E result;
        try {
            updateDag(sourceVertex, targetVertex);
            result = super.addEdge(sourceVertex, targetVertex);
        } catch (CycleFoundException e) {
            throw new IllegalArgumentException(EDGE_WOULD_INDUCE_A_CYCLE);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The complexity of adding a new edge in the graph depends on the number of edges incident to
     * the "affected region", and should in general be faster than recomputing the whole topological
     * ordering from scratch.
     *
     * @throws IllegalArgumentException if the edge would induce a cycle in the graph
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        if (e == null) {
            throw new NullPointerException();
        } else if (containsEdge(e)) {
            return false;
        }

        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        boolean result;
        try {
            updateDag(sourceVertex, targetVertex);
            result = super.addEdge(sourceVertex, targetVertex, e);
        } catch (CycleFoundException ex) {
            throw new IllegalArgumentException(EDGE_WOULD_INDUCE_A_CYCLE);
        }
        return result;
    }

    /**
     * Get the ancestors of a vertex.
     *
     * @param vertex the vertex to get the ancestors of
     * @return {@link Set} of ancestors of a vertex
     */
    public Set<V> getAncestors(V vertex)
    {
        EdgeReversedGraph<V, E> reversedGraph = new EdgeReversedGraph<>(this);
        Iterator<V> iterator = new DepthFirstIterator<>(reversedGraph, vertex);
        Set<V> ancestors = new HashSet<>();

        // Do not add start vertex to result.
        if (iterator.hasNext()) {
            iterator.next();
        }

        iterator.forEachRemaining(ancestors::add);

        return ancestors;
    }

    /**
     * Get the descendants of a vertex.
     *
     * @param vertex the vertex to get the descendants of
     * @return {@link Set} of descendants of a vertex
     */
    public Set<V> getDescendants(V vertex)
    {
        Iterator<V> iterator = new DepthFirstIterator<>(this, vertex);
        Set<V> descendants = new HashSet<>();

        // Do not add start vertex to result.
        if (iterator.hasNext()) {
            iterator.next();
        }

        iterator.forEachRemaining(descendants::add);

        return descendants;
    }

    /**
     * Returns a topological order iterator.
     *
     * @return a topological order iterator
     */
    public Iterator<V> iterator()
    {
        return new TopoIterator();
    }

    /**
     * Update as if a new edge is added.
     *
     * @param sourceVertex the source vertex
     * @param targetVertex the target vertex
     */
    private void updateDag(V sourceVertex, V targetVertex)
        throws CycleFoundException
    {
        Integer lb = topoOrderMap.getTopologicalIndex(targetVertex);
        Integer ub = topoOrderMap.getTopologicalIndex(sourceVertex);

        if (lb < ub) {
            Set<V> df = new HashSet<>();
            Set<V> db = new HashSet<>();

            // discovery
            Region affectedRegion = new Region(lb, ub);
            VisitedStrategy visited = visitedStrategyFactory.getVisitedStrategy(affectedRegion);

            // throws CycleFoundException if there is a cycle
            dfsF(targetVertex, df, visited, affectedRegion);
            dfsB(sourceVertex, db, visited, affectedRegion);
            reorder(df, db, visited);

            /*
             * if we do a reorder, then the topology has been updated
             */
            ++topoModCount;
        }
    }

    /**
     * Depth first search forward, building up the set (df) of forward-connected vertices in the
     * Affected Region
     *
     * @param initialVertex the vertex being visited
     * @param df the set we are populating with forward connected vertices in the Affected Region
     * @param visited a simple data structure that lets us know if we already visited a node with a
     *        given topo index
     *
     * @throws CycleFoundException if a cycle is discovered
     */
    private void dfsF(V initialVertex, Set<V> df, VisitedStrategy visited, Region affectedRegion)
        throws CycleFoundException
    {
        Deque<V> vertices = new ArrayDeque<>();
        vertices.push(initialVertex);

        while (!vertices.isEmpty()) {
            V vertex = vertices.pop();
            int topoIndex = topoOrderMap.getTopologicalIndex(vertex);

            if (visited.getVisited(topoIndex)) {
                continue;
            }

            // Assumption: vertex is in the AR and so it will be in visited
            visited.setVisited(topoIndex);

            df.add(vertex);

            for (E outEdge : outgoingEdgesOf(vertex)) {
                V nextVertex = getEdgeTarget(outEdge);
                Integer nextVertexTopoIndex = topoOrderMap.getTopologicalIndex(nextVertex);

                if (nextVertexTopoIndex == affectedRegion.finish) {
                    // reset visited
                    try {
                        for (V visitedVertex : df) {
                            visited.clearVisited(topoOrderMap.getTopologicalIndex(visitedVertex));
                        }
                    } catch (UnsupportedOperationException e) {
                        // okay, fine, some implementations (ones that automatically
                        // reset themselves out) don't work this way
                    }
                    throw new CycleFoundException();
                }

                /*
                 * Note, order of checks is important as we need to make sure the vertex is in the
                 * affected region before we check its visited status (otherwise we will be causing
                 * an ArrayIndexOutOfBoundsException).
                 */
                if (affectedRegion.isIn(nextVertexTopoIndex)
                    && !visited.getVisited(nextVertexTopoIndex))
                {
                    vertices.push(nextVertex); // recurse
                }
            }
        }
    }

    /**
     * Depth first search backward, building up the set (db) of back-connected vertices in the
     * Affected Region
     *
     * @param initialVertex the vertex being visited
     * @param db the set we are populating with back-connected vertices in the AR
     * @param visited
     */
    private void dfsB(V initialVertex, Set<V> db, VisitedStrategy visited, Region affectedRegion)
    {
        Deque<V> vertices = new ArrayDeque<>();
        vertices.push(initialVertex);

        while (!vertices.isEmpty()) {
            V vertex = vertices.pop();
            // Assumption: vertex is in the AR and so we will get a topoIndex from
            // the map
            int topoIndex = topoOrderMap.getTopologicalIndex(vertex);

            if (visited.getVisited(topoIndex)) {
                continue;
            }

            visited.setVisited(topoIndex);

            db.add(vertex);

            for (E inEdge : incomingEdgesOf(vertex)) {
                V previousVertex = getEdgeSource(inEdge);
                Integer previousVertexTopoIndex = topoOrderMap.getTopologicalIndex(previousVertex);

                /*
                 * Note, order of checks is important as we need to make sure the vertex is in the
                 * affected region before we check its visited status (otherwise we will be causing
                 * an ArrayIndexOutOfBoundsException).
                 */
                if (affectedRegion.isIn(previousVertexTopoIndex)
                    && !visited.getVisited(previousVertexTopoIndex))
                {
                    // if previousVertexTopoIndex != null, the vertex is in the
                    // Affected Region according to our topoIndexMap
                    vertices.push(previousVertex);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void reorder(Set<V> df, Set<V> db, VisitedStrategy visited)
    {
        List<V> topoDf = new ArrayList<>(df);
        List<V> topoDb = new ArrayList<>(db);

        topoDf.sort(topoComparator);
        topoDb.sort(topoComparator);

        // merge these suckers together in topological order
        SortedSet<Integer> availableTopoIndices = new TreeSet<>();

        // we have to cast to the generic type, can't do "new V[size]" in java
        // 5;
        V[] bigL = (V[]) new Object[df.size() + db.size()];
        int lIndex = 0; // this index is used for the sole purpose of pushing
                        // into

        // the correct index of bigL
        // assume (for now) that we are resetting visited
        boolean clearVisited = true;

        for (V vertex : topoDb) {
            Integer topoIndex = topoOrderMap.getTopologicalIndex(vertex);

            // add the available indices to the set
            availableTopoIndices.add(topoIndex);

            bigL[lIndex++] = vertex;

            if (clearVisited) { // reset visited status if supported
                try {
                    visited.clearVisited(topoIndex);
                } catch (UnsupportedOperationException e) {
                    clearVisited = false;
                }
            }
        }

        for (V vertex : topoDf) {
            Integer topoIndex = topoOrderMap.getTopologicalIndex(vertex);

            // add the available indices to the set
            availableTopoIndices.add(topoIndex);
            bigL[lIndex++] = vertex;

            if (clearVisited) { // reset visited status if supported
                try {
                    visited.clearVisited(topoIndex);
                } catch (UnsupportedOperationException e) {
                    clearVisited = false;
                }
            }
        }

        lIndex = 0; // reusing lIndex
        for (Integer topoIndex : availableTopoIndices) {
            // assign the indexes to the elements of bigL in order
            V vertex = bigL[lIndex++]; // note the post-increment
            topoOrderMap.putVertex(topoIndex, vertex);
        }
    }

    /**
     * An interface for storing the topological ordering.
     *
     * @param <V> the graph vertex type
     *
     * @author Peter Giles
     */
    protected interface TopoOrderMap<V>
        extends
        Serializable
    {
        /**
         * Add a vertex at the given topological index.
         *
         * @param index the topological index
         * @param vertex the vertex
         */
        void putVertex(Integer index, V vertex);

        /**
         * Get the vertex at the given topological index.
         *
         * @param index the topological index
         * @return vertex the vertex
         */
        V getVertex(Integer index);

        /**
         * Get the topological index of the given vertex.
         *
         * @param vertex the vertex
         * @return the index that the vertex is at, or null if the vertex isn't in the topological
         *         ordering
         */
        Integer getTopologicalIndex(V vertex);

        /**
         * Remove the given vertex from the topological ordering.
         *
         * @param vertex the vertex
         * @return the index that the vertex was at, or null if the vertex wasn't in the topological
         *         ordering
         */
        Integer removeVertex(V vertex);

        /**
         * Remove all vertices from the topological ordering.
         */
        void removeAllVertices();
    }

    /**
     * A strategy for marking vertices as visited.
     *
     * <p>
     * Vertices are indexed by their topological index, to avoid using the vertex type in the
     * interface.
     *
     * @author Peter Giles
     */
    protected interface VisitedStrategy
    {
        /**
         * Mark the given topological index as visited.
         *
         * @param index the topological index
         */
        void setVisited(int index);

        /**
         * Get if the given topological index has been visited.
         *
         * @param index the topological index
         * @return true if the given topological index has been visited, false otherwise
         */
        boolean getVisited(int index);

        /**
         * Clear the visited state of the given topological index.
         *
         * @param index the index
         * @throws UnsupportedOperationException if the implementation doesn't support (or doesn't
         *         need) clearance. For example, if the factory creates a new instance every time,
         *         it is a waste of cycles to reset the state after the search of the Affected
         *         Region is done, so an UnsupportedOperationException *should* be thrown.
         */
        void clearVisited(int index)
            throws UnsupportedOperationException;
    }

    /**
     * A visited strategy factory.
     *
     * @author Peter Giles
     */
    protected interface VisitedStrategyFactory
        extends
        Serializable
    {
        /**
         * Create a new instance of {@link VisitedStrategy}.
         *
         * @param affectedRegion the affected region
         * @return a new instance of {@link VisitedStrategy} for the affected region
         */
        VisitedStrategy getVisitedStrategy(Region affectedRegion);
    }

    /**
     * A dual map implementation of the topological order map.
     *
     * @author Peter Giles
     */
    protected static class TopoVertexBiMap<V>
        implements
        TopoOrderMap<V>
    {
        private static final long serialVersionUID = 1L;

        private final Map<Integer, V> topoToVertex = new HashMap<>();
        private final Map<V, Integer> vertexToTopo = new HashMap<>();

        /**
         * Constructor
         */
        public TopoVertexBiMap()
        {
        }

        @Override
        public void putVertex(Integer index, V vertex)
        {
            topoToVertex.put(index, vertex);
            vertexToTopo.put(vertex, index);
        }

        @Override
        public V getVertex(Integer index)
        {
            return topoToVertex.get(index);
        }

        @Override
        public Integer getTopologicalIndex(V vertex)
        {
            return vertexToTopo.get(vertex);
        }

        @Override
        public Integer removeVertex(V vertex)
        {
            Integer topoIndex = vertexToTopo.remove(vertex);
            if (topoIndex != null) {
                topoToVertex.remove(topoIndex);
            }
            return topoIndex;
        }

        @Override
        public void removeAllVertices()
        {
            vertexToTopo.clear();
            topoToVertex.clear();
        }
    }

    /**
     * An implementation of the topological order map which for performance and flexibility uses an
     * ArrayList for topological index to vertex mapping, and a HashMap for vertex to topological
     * index mapping.
     *
     * @author Peter Giles
     */
    protected class TopoVertexMap
        implements
        TopoOrderMap<V>
    {
        private static final long serialVersionUID = 1L;

        private final List<V> topoToVertex = new ArrayList<>();
        private final Map<V, Integer> vertexToTopo = new HashMap<>();

        /**
         * Constructor
         */
        public TopoVertexMap()
        {
        }

        @Override
        public void putVertex(Integer index, V vertex)
        {
            int translatedIndex = translateIndex(index);

            // grow topoToVertex as needed to accommodate elements
            while ((translatedIndex + 1) > topoToVertex.size()) {
                topoToVertex.add(null);
            }

            topoToVertex.set(translatedIndex, vertex);
            vertexToTopo.put(vertex, index);
        }

        @Override
        public V getVertex(Integer index)
        {
            return topoToVertex.get(translateIndex(index));
        }

        @Override
        public Integer getTopologicalIndex(V vertex)
        {
            return vertexToTopo.get(vertex);
        }

        @Override
        public Integer removeVertex(V vertex)
        {
            Integer topoIndex = vertexToTopo.remove(vertex);
            if (topoIndex != null) {
                topoToVertex.set(translateIndex(topoIndex), null);
            }
            return topoIndex;
        }

        @Override
        public void removeAllVertices()
        {
            vertexToTopo.clear();
            topoToVertex.clear();
        }

        /**
         * We translate the topological index to an ArrayList index. We have to do this because
         * topological indices can be negative, and we want to do it because we can make better use
         * of space by only needing an ArrayList of size |AR|.
         *
         * @return the ArrayList index
         */
        private int translateIndex(int index)
        {
            if (index >= 0) {
                return 2 * index;
            }
            return -1 * ((index * 2) - 1);
        }
    }

    /**
     * An inclusive range of indices: [start, finish].
     *
     * @author Peter Giles
     */
    protected static class Region
        implements
        Serializable
    {
        private static final long serialVersionUID = 1L;

        private final int start;
        private final int finish;

        /**
         * Construct a new region.
         *
         * @param start the start of the region
         * @param finish the end of the region (inclusive)
         */
        public Region(int start, int finish)
        {
            if (start > finish) {
                throw new IllegalArgumentException("(start > finish): invariant broken");
            }
            this.start = start;
            this.finish = finish;
        }

        /**
         * Get the size of the region.
         *
         * @return the size of the region
         */
        public int getSize()
        {
            return (finish - start) + 1;
        }

        /**
         * Check if index is in the region.
         *
         * @param index the index to check
         * @return true if the index is in the region, false otherwise
         */
        public boolean isIn(int index)
        {
            return (index >= start) && (index <= finish);
        }

        /**
         * Get the start of the region.
         *
         * @return the start of the region
         */
        public int getStart()
        {
            return start;
        }

        /**
         * Get the end of the region (inclusive).
         *
         * @return the end of the region (inclusive)
         */
        public int getFinish()
        {
            return finish;
        }

    }

    /**
     * A visited strategy which uses a {@link BitSet}.
     *
     * <p>
     * This implementation is close to the performance of {@link VisitedArrayListImpl}, with 1/8 the
     * memory usage.
     *
     * @author John V. Sichi
     */
    protected static class VisitedBitSetImpl
        implements
        VisitedStrategy,
        VisitedStrategyFactory
    {
        private static final long serialVersionUID = 1L;

        private final BitSet visited = new BitSet();
        private Region affectedRegion;

        /**
         * Constructor
         */
        public VisitedBitSetImpl()
        {
        }

        @Override
        public VisitedStrategy getVisitedStrategy(Region affectedRegion)
        {
            this.affectedRegion = affectedRegion;
            return this;
        }

        @Override
        public void setVisited(int index)
        {
            visited.set(translateIndex(index), true);
        }

        @Override
        public boolean getVisited(int index)
        {
            return visited.get(translateIndex(index));
        }

        @Override
        public void clearVisited(int index)
            throws UnsupportedOperationException
        {
            visited.clear(translateIndex(index));
        }

        /**
         * We translate the topological index to an ArrayList index. We have to do this because
         * topological indices can be negative, and we want to do it because we can make better use
         * of space by only needing an ArrayList of size |AR|.
         *
         * @return the ArrayList index
         */
        private int translateIndex(int index)
        {
            return index - affectedRegion.start;
        }
    }

    /**
     * A visited strategy using an {@link ArrayList}.
     *
     * <p>
     * This implementation seems to offer the best performance in most cases. It grows the internal
     * ArrayList as needed to be as large as |AR|, so it will be more memory intensive than the
     * HashSet implementation, and unlike the Array implementation, it will hold on to that memory
     * (it expands, but never contracts).
     *
     * @author Peter Giles
     */
    protected static class VisitedArrayListImpl
        implements
        VisitedStrategy,
        VisitedStrategyFactory
    {
        private static final long serialVersionUID = 1L;

        private final List<Boolean> visited = new ArrayList<>();
        private Region affectedRegion;

        /**
         * Constructor
         */
        public VisitedArrayListImpl()
        {
        }

        @Override
        public VisitedStrategy getVisitedStrategy(Region affectedRegion)
        {
            // Make sure visited is big enough
            int minSize = (affectedRegion.finish - affectedRegion.start) + 1;
            /* plus one because the region range is inclusive of both indices */

            while (visited.size() < minSize) {
                visited.add(Boolean.FALSE);
            }

            this.affectedRegion = affectedRegion;
            return this;
        }

        @Override
        public void setVisited(int index)
        {
            visited.set(translateIndex(index), Boolean.TRUE);
        }

        @Override
        public boolean getVisited(int index)
        {
            return visited.get(translateIndex(index));
        }

        @Override
        public void clearVisited(int index)
            throws UnsupportedOperationException
        {
            visited.set(translateIndex(index), Boolean.FALSE);
        }

        /**
         * We translate the topological index to an ArrayList index. We have to do this because
         * topological indices can be negative, and we want to do it because we can make better use
         * of space by only needing an ArrayList of size |AR|.
         *
         * @return the ArrayList index
         */
        private int translateIndex(int index)
        {
            return index - affectedRegion.start;
        }
    }

    /**
     * A visited strategy using a {@link HashSet}.
     *
     * <p>
     * This implementation doesn't seem to perform as well, though I can imagine circumstances where
     * it should shine (lots and lots of vertices). It also should have the lowest memory footprint
     * as it only uses storage for indices that have been visited.
     *
     * @author Peter Giles
     */
    protected static class VisitedHashSetImpl
        implements
        VisitedStrategy,
        VisitedStrategyFactory
    {
        private static final long serialVersionUID = 1L;

        private final Set<Integer> visited = new HashSet<>();

        /**
         * Constructor
         */
        public VisitedHashSetImpl()
        {
        }

        @Override
        public VisitedStrategy getVisitedStrategy(Region affectedRegion)
        {
            visited.clear();
            return this;
        }

        @Override
        public void setVisited(int index)
        {
            visited.add(index);
        }

        @Override
        public boolean getVisited(int index)
        {
            return visited.contains(index);
        }

        @Override
        public void clearVisited(int index)
            throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A visited strategy using an array.
     *
     * <p>
     * This implementation, somewhat to my surprise, is slower than the ArrayList version, probably
     * due to its reallocation of the underlying array for every topology reorder that is required.
     *
     * @author Peter Giles
     */
    protected static class VisitedArrayImpl
        implements
        VisitedStrategy,
        VisitedStrategyFactory
    {
        private static final long serialVersionUID = 1L;

        private final boolean[] visited;
        private final Region region;

        /**
         * Constructs empty instance
         */
        public VisitedArrayImpl()
        {
            this(null);
        }

        /**
         * Construct an empty instance for a region.
         *
         * @param region the region
         */
        public VisitedArrayImpl(Region region)
        {
            if (region == null) { // make empty instance
                this.visited = null;
                this.region = null;
            } else { // fill in the needed pieces
                this.region = region;

                // initialized to all false by default
                visited = new boolean[region.getSize()];
            }
        }

        @Override
        public VisitedStrategy getVisitedStrategy(Region affectedRegion)
        {
            return new VisitedArrayImpl(affectedRegion);
        }

        @Override
        public void setVisited(int index)
        {
            visited[index - region.start] = true;
        }

        @Override
        public boolean getVisited(int index)
        {
            return visited[index - region.start];
        }

        @Override
        public void clearVisited(int index)
            throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Exception used in dfsF when a cycle is found
     *
     * @author Peter Giles
     */
    private static class CycleFoundException
        extends
        Exception
    {
        private static final long serialVersionUID = 5583471522212552754L;
    }

    /**
     * Comparator for vertices based on their topological ordering
     *
     * @author Peter Giles
     */
    private class TopoComparator
        implements
        Comparator<V>,
        Serializable
    {
        private static final long serialVersionUID = 8144905376266340066L;

        @Override
        public int compare(V o1, V o2)
        {
            return topoOrderMap
                .getTopologicalIndex(o1).compareTo(topoOrderMap.getTopologicalIndex(o2));
        }

    }

    /**
     * An iterator which follows topological order
     *
     * @author Peter Giles
     */
    private class TopoIterator
        implements
        Iterator<V>
    {
        private int currentTopoIndex;
        private final long expectedTopoModCount = topoModCount;
        private Integer nextIndex = null;

        public TopoIterator()
        {
            currentTopoIndex = minTopoIndex - 1;
        }

        @Override
        public boolean hasNext()
        {
            if (expectedTopoModCount != topoModCount) {
                throw new ConcurrentModificationException();
            }

            nextIndex = getNextIndex();
            return nextIndex != null;
        }

        @Override
        public V next()
        {
            if (expectedTopoModCount != topoModCount) {
                throw new ConcurrentModificationException();
            }

            if (nextIndex == null) {
                // find nextIndex
                nextIndex = getNextIndex();
            }
            if (nextIndex == null) {
                throw new NoSuchElementException();
            }
            currentTopoIndex = nextIndex;
            nextIndex = null;
            return topoOrderMap.getVertex(currentTopoIndex);
        }

        @Override
        public void remove()
        {
            if (expectedTopoModCount != topoModCount) {
                throw new ConcurrentModificationException();
            }

            V vertexToRemove;
            if ((vertexToRemove = topoOrderMap.getVertex(currentTopoIndex)) != null) {
                topoOrderMap.removeVertex(vertexToRemove);
            } else {
                // should only happen if next() hasn't been called
                throw new IllegalStateException();
            }
        }

        private Integer getNextIndex()
        {
            for (int i = currentTopoIndex + 1; i <= maxTopoIndex; i++) {
                if (topoOrderMap.getVertex(i) != null) {
                    return i;
                }
            }
            return null;
        }
    }
}
