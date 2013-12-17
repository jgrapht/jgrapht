/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -------------------
 * DirectedAcyclicGraph.java
 * -------------------
 * (C) Copyright 2008-2008, by Peter Giles and Contributors.
 *
 * Original Author:  Peter Giles
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 17-Mar-2008 : Initial revision (PG);
 * 23-Aug-2008 : Added VisitedBitSetImpl and made it the default (JVS);
 *
 */
package org.jgrapht.experimental.dag;

import java.io.*;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * <p>DirectedAcyclicGraph implements a DAG that can be modified (vertices &amp;
 * edges added and removed), is guaranteed to remain acyclic, and provides fast
 * topological order iteration.</p>
 *
 * <p>This is done using a dynamic topological sort which is based on the
 * algorithm PK described in "D. Pearce &amp; P. Kelly, 2007: A Dynamic
 * Topological Sort Algorithm for Directed Acyclic Graphs", (see <a
 * href="http://www.mcs.vuw.ac.nz/~djp/files/PK-JEA07.pdf">Paper</a> or <a
 * href="http://doi.acm.org/10.1145/1187436.1210590">ACM link</a> for details).
 * </p>
 *
 * <p>The implementation differs from the algorithm specified in the above paper
 * in some ways, perhaps most notably in that the topological ordering is stored
 * by default using two HashMaps, which will have some effects on runtime, but
 * also allows for vertex addition and removal, and other operations which are
 * helpful for manipulating or combining DAGs. This storage mechanism is
 * pluggable for subclassers.</p>
 *
 * <p>This class makes no claims to thread safety, and concurrent usage from
 * multiple threads will produce undefined results.</p>
 *
 * @author Peter Giles, gilesp@u.washington.edu
 */
public class DirectedAcyclicGraph<V, E>
    extends SimpleDirectedGraph<V, E>
{
    

    private static final long serialVersionUID = 4522128427004938150L;

    

    private TopoComparator<V> topoComparator;

    private TopoOrderMapping<V> topoOrderMap;

    private int maxTopoIndex = 0;
    private int minTopoIndex = 0;

    // this update count is used to keep internal topological iterators honest
    private long topologyUpdateCount = 0;

    /**
     * Pluggable VisitedFactory implementation
     */
    private VisitedFactory visitedFactory = new VisitedBitSetImpl();

    /**
     * Pluggable TopoOrderMappingFactory implementation
     */
    private TopoOrderMappingFactory<V> topoOrderFactory = new TopoVertexBiMap();

    

    public DirectedAcyclicGraph(Class<? extends E> arg0)
    {
        super(arg0);
        initialize();
    }

    DirectedAcyclicGraph(
        Class<? extends E> arg0,
        VisitedFactory visitedFactory,
        TopoOrderMappingFactory<V> topoOrderFactory)
    {
        super(arg0);
        if (visitedFactory != null) {
            this.visitedFactory = visitedFactory;
        }
        if (topoOrderFactory != null) {
            this.topoOrderFactory = topoOrderFactory;
        }
        initialize();
    }

    

    /**
     * set the topoOrderMap based on the current factory, and create the
     * comparator;
     */
    private void initialize()
    {
        topoOrderMap = topoOrderFactory.getTopoOrderMapping();
        topoComparator = new TopoComparator<V>(topoOrderMap);
    }

    /**
     * iterator will traverse the vertices in topological order, meaning that
     * for a directed graph G = (V,E), if there exists a path from vertex va to
     * vertex vb then va is guaranteed to come before vertex vb in the iteration
     * order.
     *
     * @return an iterator that will traverse the graph in topological order
     */
    public Iterator<V> iterator()
    {
        return new TopoIterator();
    }

    /**
     * adds the vertex if it wasn't already in the graph, and puts it at the top
     * of the internal topological vertex ordering
     */
    @Override public boolean addVertex(V v)
    {
        boolean added = super.addVertex(v);

        if (added) {
            // add to the top
            ++maxTopoIndex;
            topoOrderMap.putVertex(maxTopoIndex, v);

            ++topologyUpdateCount;
        }

        return added;
    }

    /**
     * adds the vertex if it wasn't already in the graph, and puts it either at
     * the top or the bottom of the topological ordering, depending on the value
     * of addToTop. This may provide useful optimizations for merging
     * DirectedAcyclicGraphs that become connected.
     *
     * @param v
     * @param addToTop
     *
     * @return whether new vertex was added
     */
    public boolean addVertex(V v, boolean addToTop)
    {
        boolean added = super.addVertex(v);

        if (added) {
            int insertIndex;

            // add to the top
            if (addToTop) {
                insertIndex = ++maxTopoIndex;
            } else {
                insertIndex = --minTopoIndex;
            }
            topoOrderMap.putVertex(insertIndex, v);

            ++topologyUpdateCount;
        }
        return added;
    }

    /**
     * <p>Adds the given edge and updates the internal topological order for
     * consistency IFF
     *
     * <UL>
     * <li>there is not already an edge (fromVertex, toVertex) in the graph
     * <li>the edge does not induce a cycle in the graph
     * </ul>
     * </p>
     *
     * @return null if the edge is already in the graph, else the created edge
     * is returned
     *
     * @throws IllegalArgumentException If either fromVertex or toVertex is not
     * a member of the graph
     * @throws CycleFoundException if the edge would induce a cycle in the graph
     *
     * @see Graph#addEdge(Object, Object, Object)
     */
    public E addDagEdge(V fromVertex, V toVertex)
        throws CycleFoundException
    {
        updateDag(fromVertex, toVertex);

        return super.addEdge(fromVertex, toVertex);
    }

    /**
     * identical to {@link #addDagEdge(Object, Object)}, except an unchecked
     * {@link IllegalArgumentException} is thrown if a cycle would have been
     * induced by this edge
     */
    @Override public E addEdge(V sourceVertex, V targetVertex)
    {
        E result = null;
        try {
            result = addDagEdge(sourceVertex, targetVertex);
        } catch (CycleFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return result;
    }

    /**
     * <p>Adds the given edge and updates the internal topological order for
     * consistency IFF
     *
     * <UL>
     * <li>the given edge is not already a member of the graph
     * <li>there is not already an edge (fromVertex, toVertex) in the graph
     * <li>the edge does not induce a cycle in the graph
     * </ul>
     * </p>
     *
     * @return true if the edge was added to the graph
     *
     * @throws CycleFoundException if adding an edge (fromVertex, toVertex) to
     * the graph would induce a cycle.
     *
     * @see Graph#addEdge(Object, Object, Object)
     */
    public boolean addDagEdge(V fromVertex, V toVertex, E e)
        throws CycleFoundException
    {
        if (e == null) {
            throw new NullPointerException();
        } else if (containsEdge(e)) {
            return false;
        }

        updateDag(fromVertex, toVertex);

        return super.addEdge(fromVertex, toVertex, e);
    }

    private void updateDag(V fromVertex, V toVertex)
        throws CycleFoundException
    {
        Integer lb = topoOrderMap.getTopologicalIndex(toVertex);
        Integer ub = topoOrderMap.getTopologicalIndex(fromVertex);

        if ((lb == null) || (ub == null)) {
            throw new IllegalArgumentException(
                "vertices must be in the graph already!");
        }

        if (lb < ub) {
            Set<V> df = new HashSet<V>();
            Set<V> db = new HashSet<V>();

            // Discovery
            Region affectedRegion = new Region(lb, ub);
            Visited visited = visitedFactory.getInstance(affectedRegion);

            // throws CycleFoundException if there is a cycle
            dfsF(toVertex, df, visited, affectedRegion);

            dfsB(fromVertex, db, visited, affectedRegion);
            reorder(df, db, visited);
            ++topologyUpdateCount; // if we do a reorder, than the topology has
                                   // been updated
        }
    }

    /**
     * identical to {@link #addDagEdge(Object, Object, Object)}, except an
     * unchecked {@link IllegalArgumentException} is thrown if a cycle would
     * have been induced by this edge
     */
    @Override public boolean addEdge(V sourceVertex, V targetVertex, E edge)
    {
        boolean result;
        try {
            result = addDagEdge(sourceVertex, targetVertex, edge);
        } catch (CycleFoundException e) {
            throw new IllegalArgumentException(e);
        }
        return result;
    }

    // note that this can leave holes in the topological ordering, which
    // (depending on the TopoOrderMap implementation) can degrade performance
    // for certain operations over time
    @Override public boolean removeVertex(V v)
    {
        boolean removed = super.removeVertex(v);

        if (removed) {
            Integer topoIndex = topoOrderMap.removeVertex(v);

            // contract minTopoIndex as we are able
            if (topoIndex == minTopoIndex) {
                while (
                    (minTopoIndex < 0)
                    && (null == topoOrderMap.getVertex(minTopoIndex)))
                {
                    ++minTopoIndex;
                }
            }

            // contract maxTopoIndex as we are able
            if (topoIndex == maxTopoIndex) {
                while (
                    (maxTopoIndex > 0)
                    && (null == topoOrderMap.getVertex(maxTopoIndex)))
                {
                    --maxTopoIndex;
                }
            }

            ++topologyUpdateCount;
        }

        return removed;
    }

    @Override public boolean removeAllVertices(Collection<? extends V> arg0)
    {
        boolean removed = super.removeAllVertices(arg0);

        topoOrderMap.removeAllVertices();

        maxTopoIndex = 0;
        minTopoIndex = 0;

        ++topologyUpdateCount;

        return removed;
    }

    /**
     * Depth first search forward, building up the set (df) of forward-connected
     * vertices in the Affected Region
     *
     * @param vertex the vertex being visited
     * @param df the set we are populating with forward connected vertices in
     * the Affected Region
     * @param visited a simple data structure that lets us know if we already
     * visited a node with a given topo index
     * @param topoIndexMap for quick lookups, a map from vertex to topo index in
     * the AR
     * @param ub the topo index of the original fromVertex -- used for cycle
     * detection
     *
     * @throws CycleFoundException if a cycle is discovered
     */
    private void dfsF(
        V vertex,
        Set<V> df,
        Visited visited,
        Region affectedRegion)
        throws CycleFoundException
    {
        int topoIndex = topoOrderMap.getTopologicalIndex(vertex);

        // Assumption: vertex is in the AR and so it will be in visited
        visited.setVisited(topoIndex);

        df.add(vertex);

        for (E outEdge : outgoingEdgesOf(vertex)) {
            V nextVertex = getEdgeTarget(outEdge);
            Integer nextVertexTopoIndex =
                topoOrderMap.getTopologicalIndex(nextVertex);

            if (nextVertexTopoIndex.intValue() == affectedRegion.finish) {
                // reset visited
                try {
                    for (V visitedVertex : df) {
                        visited.clearVisited(
                            topoOrderMap.getTopologicalIndex(visitedVertex));
                    }
                } catch (UnsupportedOperationException e) {
                    // okay, fine, some implementations (ones that automatically
                    // clear themselves out) don't work this way
                }
                throw new CycleFoundException();
            }

            // note, order of checks is important as we need to make sure the
            // vertex is in the affected region before we check its visited
            // status (otherwise we will be causing an
            // ArrayIndexOutOfBoundsException).
            if (affectedRegion.isIn(nextVertexTopoIndex)
                && !visited.getVisited(nextVertexTopoIndex))
            {
                dfsF(nextVertex, df, visited, affectedRegion); // recurse
            }
        }
    }

    /**
     * Depth first search backward, building up the set (db) of back-connected
     * vertices in the Affected Region
     *
     * @param vertex the vertex being visited
     * @param db the set we are populating with back-connected vertices in the
     * AR
     * @param visited
     * @param topoIndexMap
     */
    private void dfsB(
        V vertex,
        Set<V> db,
        Visited visited,
        Region affectedRegion)
    {
        // Assumption: vertex is in the AR and so we will get a topoIndex from
        // the map
        int topoIndex = topoOrderMap.getTopologicalIndex(vertex);
        visited.setVisited(topoIndex);

        db.add(vertex);

        for (E inEdge : incomingEdgesOf(vertex)) {
            V previousVertex = getEdgeSource(inEdge);
            Integer previousVertexTopoIndex =
                topoOrderMap.getTopologicalIndex(previousVertex);

            // note, order of checks is important as we need to make sure the
            // vertex is in the affected region before we check its visited
            // status (otherwise we will be causing an
            // ArrayIndexOutOfBoundsException).
            if (affectedRegion.isIn(previousVertexTopoIndex)
                && !visited.getVisited(previousVertexTopoIndex))
            {
                // if prevousVertexTopoIndex != null, the vertex is in the
                // Affected Region according to our topoIndexMap

                dfsB(previousVertex, db, visited, affectedRegion);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void reorder(Set<V> df, Set<V> db, Visited visited)
    {
        List<V> topoDf = new ArrayList<V>(df);
        List<V> topoDb = new ArrayList<V>(db);

        Collections.sort(topoDf, topoComparator);
        Collections.sort(topoDb, topoComparator);

        // merge these suckers together in topo order

        SortedSet<Integer> availableTopoIndices = new TreeSet<Integer>();

        // we have to cast to the generic type, can't do "new V[size]" in java
        // 5;
        V [] bigL = (V []) new Object[df.size() + db.size()];
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
     * For performance tuning, an interface for storing the topological ordering
     *
     * @author gilesp
     */
    public interface TopoOrderMapping<V>
        extends Serializable
    {
        /**
         * add a vertex at the given topological index.
         *
         * @param index
         * @param vertex
         */
        public void putVertex(Integer index, V vertex);

        /**
         * get the vertex at the given topological index.
         *
         * @param index
         *
         * @return vertex
         */
        public V getVertex(Integer index);

        /**
         * get the topological index of the given vertex.
         *
         * @param vertex
         *
         * @return the index that the vertex is at, or null if the vertex isn't
         * in the topological ordering
         */
        public Integer getTopologicalIndex(V vertex);

        /**
         * remove the given vertex from the topological ordering
         *
         * @param vertex
         *
         * @return the index that the vertex was at, or null if the vertex
         * wasn't in the topological ordering
         */
        public Integer removeVertex(V vertex);

        /**
         * remove all vertices from the topological ordering
         */
        public void removeAllVertices();
    }

    public interface TopoOrderMappingFactory<V>
    {
        public TopoOrderMapping<V> getTopoOrderMapping();
    }

    /**
     * this interface allows specification of a strategy for marking vertices as
     * visited (based on their topological index, so the vertex type isn't part
     * of the interface).
     */
    public interface Visited
    {
        /**
         * mark the given topological index as visited
         *
         * @param index the topological index
         */
        public void setVisited(int index);

        /**
         * has the given topological index been visited?
         *
         * @param index the topological index
         */
        public boolean getVisited(int index);

        /**
         * Clear the visited state of the given topological index
         *
         * @param index
         *
         * @throws UnsupportedOperationException if the implementation doesn't
         * support (or doesn't need) clearance. For example, if the factory
         * vends a new instance every time, it is a waste of cycles to clear the
         * state after the search of the Affected Region is done, so an
         * UnsupportedOperationException *should* be thrown.
         */
        public void clearVisited(int index)
            throws UnsupportedOperationException;
    }

    /**
     * interface for a factory that vends Visited implementations
     *
     * @author gilesp
     */
    public interface VisitedFactory
        extends Serializable
    {
        public Visited getInstance(Region affectedRegion);
    }

    

    /**
     * Note, this is a lazy and incomplete implementation, with assumptions that
     * inputs are in the given topoIndexMap
     *
     * @param <V>
     *
     * @author gilesp
     */
    private static class TopoComparator<V>
        implements Comparator<V>,
            Serializable
    {
        /**
         */
        private static final long serialVersionUID = 1L;

        private TopoOrderMapping<V> topoOrderMap;

        public TopoComparator(TopoOrderMapping<V> topoOrderMap)
        {
            this.topoOrderMap = topoOrderMap;
        }

        public int compare(V o1, V o2)
        {
            return topoOrderMap.getTopologicalIndex(o1).compareTo(
                topoOrderMap.getTopologicalIndex(o2));
        }
    }

    /**
     * a dual HashMap implementation
     *
     * @author gilesp
     */
    private class TopoVertexBiMap
        implements TopoOrderMapping<V>,
            TopoOrderMappingFactory<V>
    {
        /**
         */
        private static final long serialVersionUID = 1L;

        private final Map<Integer, V> topoToVertex = new HashMap<Integer, V>();
        private final Map<V, Integer> vertexToTopo = new HashMap<V, Integer>();

        public void putVertex(Integer index, V vertex)
        {
            topoToVertex.put(index, vertex);
            vertexToTopo.put(vertex, index);
        }

        public V getVertex(Integer index)
        {
            return topoToVertex.get(index);
        }

        public Integer getTopologicalIndex(V vertex)
        {
            Integer topoIndex = vertexToTopo.get(vertex);
            return topoIndex;
        }

        public Integer removeVertex(V vertex)
        {
            Integer topoIndex = vertexToTopo.remove(vertex);
            if (topoIndex != null) {
                topoToVertex.remove(topoIndex);
            }
            return topoIndex;
        }

        public void removeAllVertices()
        {
            vertexToTopo.clear();
            topoToVertex.clear();
        }

        public TopoOrderMapping<V> getTopoOrderMapping()
        {
            return this;
        }
    }

    /**
     * For performance and flexibility uses an ArrayList for topological index
     * to vertex mapping, and a HashMap for vertex to topological index mapping.
     *
     * @author gilesp
     */
    public class TopoVertexMap
        implements TopoOrderMapping<V>,
            TopoOrderMappingFactory<V>
    {
        /**
         */
        private static final long serialVersionUID = 1L;

        private final List<V> topoToVertex = new ArrayList<V>();
        private final Map<V, Integer> vertexToTopo = new HashMap<V, Integer>();

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

        public V getVertex(Integer index)
        {
            return topoToVertex.get(translateIndex(index));
        }

        public Integer getTopologicalIndex(V vertex)
        {
            return vertexToTopo.get(vertex);
        }

        public Integer removeVertex(V vertex)
        {
            Integer topoIndex = vertexToTopo.remove(vertex);
            if (topoIndex != null) {
                topoToVertex.set(translateIndex(topoIndex), null);
            }
            return topoIndex;
        }

        public void removeAllVertices()
        {
            vertexToTopo.clear();
            topoToVertex.clear();
        }

        public TopoOrderMapping<V> getTopoOrderMapping()
        {
            return this;
        }

        /**
         * We translate the topological index to an ArrayList index. We have to
         * do this because topological indices can be negative, and we want to
         * do it because we can make better use of space by only needing an
         * ArrayList of size |AR|.
         *
         * @param unscaledIndex
         *
         * @return the ArrayList index
         */
        private final int translateIndex(int index)
        {
            if (index >= 0) {
                return 2 * index;
            }
            return -1 * ((index * 2) - 1);
        }
    }

    /**
     * Region is an *inclusive* range of indices. Esthetically displeasing, but
     * convenient for our purposes.
     *
     * @author gilesp
     */
    public static class Region
        implements Serializable
    {
        /**
         */
        private static final long serialVersionUID = 1L;

        public final int start;
        public final int finish;

        public Region(int start, int finish)
        {
            if (start > finish) {
                throw new IllegalArgumentException(
                    "(start > finish): invariant broken");
            }
            this.start = start;
            this.finish = finish;
        }

        public int getSize()
        {
            return (finish - start) + 1;
        }

        public boolean isIn(int index)
        {
            return (index >= start) && (index <= finish);
        }
    }

    /**
     * This implementation is close to the performance of VisitedArrayListImpl,
     * with 1/8 the memory usage.
     *
     * @author perfecthash
     */
    public static class VisitedBitSetImpl
        implements Visited,
            VisitedFactory
    {
        /**
         */
        private static final long serialVersionUID = 1L;

        private final BitSet visited = new BitSet();

        private Region affectedRegion;

        public Visited getInstance(Region affectedRegion)
        {
            this.affectedRegion = affectedRegion;

            return this;
        }

        public void setVisited(int index)
        {
            visited.set(translateIndex(index), true);
        }

        public boolean getVisited(int index)
        {
            return visited.get(translateIndex(index));
        }

        public void clearVisited(int index)
            throws UnsupportedOperationException
        {
            visited.clear(translateIndex(index));
        }

        /**
         * We translate the topological index to an ArrayList index. We have to
         * do this because topological indices can be negative, and we want to
         * do it because we can make better use of space by only needing an
         * ArrayList of size |AR|.
         *
         * @param unscaledIndex
         *
         * @return the ArrayList index
         */
        private int translateIndex(int index)
        {
            return index - affectedRegion.start;
        }
    }

    /**
     * This implementation seems to offer the best performance in most cases. It
     * grows the internal ArrayList as needed to be as large as |AR|, so it will
     * be more memory intensive than the HashSet implementation, and unlike the
     * Array implementation, it will hold on to that memory (it expands, but
     * never contracts).
     *
     * @author gilesp
     */
    public static class VisitedArrayListImpl
        implements Visited,
            VisitedFactory
    {
        /**
         */
        private static final long serialVersionUID = 1L;

        private final List<Boolean> visited = new ArrayList<Boolean>();

        private Region affectedRegion;

        public Visited getInstance(Region affectedRegion)
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

        public void setVisited(int index)
        {
            visited.set(translateIndex(index), Boolean.TRUE);
        }

        public boolean getVisited(int index)
        {
            Boolean result = null;

            result = visited.get(translateIndex(index));

            return result;
        }

        public void clearVisited(int index)
            throws UnsupportedOperationException
        {
            visited.set(translateIndex(index), Boolean.FALSE);
        }

        /**
         * We translate the topological index to an ArrayList index. We have to
         * do this because topological indices can be negative, and we want to
         * do it because we can make better use of space by only needing an
         * ArrayList of size |AR|.
         *
         * @param unscaledIndex
         *
         * @return the ArrayList index
         */
        private int translateIndex(int index)
        {
            return index - affectedRegion.start;
        }
    }

    /**
     * This implementation doesn't seem to perform as well, though I can imagine
     * circumstances where it should shine (lots and lots of vertices). It also
     * should have the lowest memory footprint as it only uses storage for
     * indices that have been visited.
     *
     * @author gilesp
     */
    public static class VisitedHashSetImpl
        implements Visited,
            VisitedFactory
    {
        /**
         */
        private static final long serialVersionUID = 1L;

        private final Set<Integer> visited = new HashSet<Integer>();

        public Visited getInstance(Region affectedRegion)
        {
            visited.clear();
            return this;
        }

        public void setVisited(int index)
        {
            visited.add(index);
        }

        public boolean getVisited(int index)
        {
            return visited.contains(index);
        }

        public void clearVisited(int index)
            throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * This implementation, somewhat to my surprise, is slower than the
     * ArrayList version, probably due to its reallocation of the underlying
     * array for every topology reorder that is required.
     *
     * @author gilesp
     */
    public static class VisitedArrayImpl
        implements Visited,
            VisitedFactory
    {
        /**
         */
        private static final long serialVersionUID = 1L;

        private final boolean [] visited;

        private final Region region;

        /**
         * Constructs empty factory instance
         */
        public VisitedArrayImpl()
        {
            this(null);
        }

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

        public Visited getInstance(Region affectedRegion)
        {
            return new VisitedArrayImpl(affectedRegion);
        }

        public void setVisited(int index)
        {
            try {
                visited[index - region.start] = true;
            } catch (ArrayIndexOutOfBoundsException e) {
                /*
                log.error("Visited set operation out of region boundaries", e);
                */
                throw e;
            }
        }

        public boolean getVisited(int index)
        {
            try {
                return visited[index - region.start];
            } catch (ArrayIndexOutOfBoundsException e) {
                /*
                log.error("Visited set operation out of region boundaries", e);
                */
                throw e;
            }
        }

        public void clearVisited(int index)
            throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Exception used in dfsF when a cycle is found
     *
     * @author gilesp
     */
    public static class CycleFoundException
        extends Exception
    {
        private static final long serialVersionUID = 5583471522212552754L;
    }

    /**
     * iterator which follows topological order
     *
     * @author gilesp
     */
    private class TopoIterator
        implements Iterator<V>
    {
        private int currentTopoIndex;
        private final long updateCountAtCreation;
        private Integer nextIndex = null;

        public TopoIterator()
        {
            updateCountAtCreation = topologyUpdateCount;
            currentTopoIndex = minTopoIndex - 1;
        }

        public boolean hasNext()
        {
            if (updateCountAtCreation != topologyUpdateCount) {
                throw new ConcurrentModificationException();
            }

            nextIndex = getNextIndex();
            return nextIndex != null;
        }

        public V next()
        {
            if (updateCountAtCreation != topologyUpdateCount) {
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
            return topoOrderMap.getVertex(currentTopoIndex); //topoToVertex.get(currentTopoIndex);
        }

        public void remove()
        {
            if (updateCountAtCreation != topologyUpdateCount) {
                throw new ConcurrentModificationException();
            }

            V vertexToRemove = null;
            if (null
                != (vertexToRemove =
                        topoOrderMap.getVertex(
                            currentTopoIndex)))
            {
                topoOrderMap.removeVertex(vertexToRemove);
            } else {
                // should only happen if next() hasn't been called
                throw new IllegalStateException();
            }
        }

        private Integer getNextIndex()
        {
            for (int i = currentTopoIndex + 1; i <= maxTopoIndex; i++) {
                if (null != topoOrderMap.getVertex(i)) {
                    return i;
                }
            }
            return null;
        }
    }
}

// End DirectedAcyclicGraph.java
