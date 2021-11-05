/*
 * (C) Copyright 2003-2021, by Barak Naveh and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.event.*;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * A subgraph is a graph that has a subset of vertices and a subset of edges with respect to some
 * base graph. More formally, a subgraph G(V,E) that is based on a base graph Gb(Vb,Eb) satisfies
 * the following <b><i>subgraph property</i></b>: V is a subset of Vb and E is a subset of Eb. Other
 * than this property, a subgraph is a graph with any respect and fully complies with the
 * <code>Graph</code> interface.
 *
 * <p>
 * If the base graph is a {@link org.jgrapht.ListenableGraph}, the subgraph listens on the base
 * graph and guarantees the subgraph property. If an edge or a vertex is removed from the base
 * graph, it is automatically removed from the subgraph. Subgraph listeners are informed on such
 * removal only if it results in a cascaded removal from the subgraph. If the subgraph has been
 * created as an induced subgraph it also keeps track of edges being added to its vertices. If
 * vertices are added to the base graph, the subgraph remains unaffected.
 * </p>
 *
 * <p>
 * If the base graph is <i>not</i> a ListenableGraph, then the subgraph property cannot be
 * guaranteed. If edges or vertices are removed from the base graph, they are <i>not</i> removed
 * from the subgraph.
 * </p>
 *
 * <p>
 * Modifications to Subgraph are allowed as long as the subgraph property is maintained. Addition of
 * vertices or edges are allowed as long as they also exist in the base graph. Removal of vertices
 * or edges is always allowed. The base graph is <i>never</i> affected by any modification made to
 * the subgraph.
 * </p>
 *
 * <p>
 * A subgraph may provide a "live-window" on a base graph, so that changes made to its vertices or
 * edges are immediately reflected in the base graph, and vice versa. For that to happen, vertices
 * and edges added to the subgraph must be <i>identical</i> (that is, reference-equal and not only
 * value-equal) to their respective ones in the base graph. Previous versions of this class enforced
 * such identity, at a severe performance cost. Currently it is no longer enforced. If you want to
 * achieve a "live-window" functionality, your safest tactics would be to NOT override the
 * <code>equals()</code> methods of your vertices and edges. If you use a class that has already
 * overridden the <code>equals()</code> method, such as <code>String</code>, then you can use a
 * wrapper around it, or else use it directly but exercise a great care to avoid having
 * different-but-equal instances in the subgraph and the base graph.
 * </p>
 *
 * <p>
 * This graph implementation guarantees deterministic vertex and edge set ordering (via
 * {@link LinkedHashSet}).
 * </p>
 *
 * <p>
 * Note that this implementation tries to maintain a "live-window" on the base graph, which has
 * implications in the performance of the various operations. For example iterating over the
 * adjacent edges of a vertex takes time proportional to the number of adjacent edges of the vertex
 * in the base graph even if the subgraph contains only a small subset of those edges. Therefore,
 * the user must be aware that using this implementation for certain algorithms might come with
 * computational overhead. For certain algorithms it is better to maintain a subgraph by hand
 * instead of using this implementation as a black box.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author Barak Naveh
 * @see Graph
 * @see Set
 */
public class AsSubgraph<V, E>
    extends
    AbstractGraph<V, E>
    implements
    Serializable
{

    private static final long serialVersionUID = -1471811754881775298L;

    private static final String NO_SUCH_EDGE_IN_BASE = "no such edge in base graph";
    private static final String NO_SUCH_VERTEX_IN_BASE = "no such vertex in base graph";
    private static final String CANNOT_CREATE_NEW_VERTICES_FROM_SUBGRAPH =
        "Cannot create new vertices from subgraph";

    protected final Set<E> edgeSet = new LinkedHashSet<>();
    protected final Set<V> vertexSet = new LinkedHashSet<>();
    protected final Graph<V, E> base;
    protected final GraphType baseType;
    protected final boolean isInduced;

    private transient Set<E> unmodifiableEdgeSet = null;
    private transient Set<V> unmodifiableVertexSet = null;

    /**
     * Creates a new subgraph.
     *
     * @param base the base (backing) graph on which the subgraph will be based.
     * @param vertexSubset vertices to include in the subgraph. If <code>null</code> then all
     *        vertices are included.
     * @param edgeSubset edges to in include in the subgraph. If <code>null</code> then all the
     *        edges whose vertices found in the graph are included.
     */
    public AsSubgraph(Graph<V, E> base, Set<? extends V> vertexSubset, Set<? extends E> edgeSubset)
    {
        super();

        this.base = GraphTests.requireDirectedOrUndirected(base);
        this.baseType = base.getType();
        this.isInduced = edgeSubset == null;

        if (base instanceof ListenableGraph<?, ?>) {
            ((ListenableGraph<V, E>) base).addGraphListener(new BaseGraphListener());
        }

        initialize(vertexSubset, edgeSubset);
    }

    /**
     * Creates a new induced subgraph. The subgraph will keep track of edges being added to its
     * vertex subset as well as deletion of edges and vertices. If base it not listenable, this is
     * identical to the call Subgraph(base, vertexSubset, null).
     *
     * @param base the base (backing) graph on which the subgraph will be based.
     * @param vertexSubset vertices to include in the subgraph. If <code>null</code> then all
     *        vertices are included.
     */
    public AsSubgraph(Graph<V, E> base, Set<? extends V> vertexSubset)
    {
        this(base, vertexSubset, null);
    }

    /**
     * Creates a new induced Subgraph with all vertices included. The subgraph will keep track of
     * edges being added to its vertex subset as well as deletion of edges and vertices. If base is
     * not listenable, this is identical to the call Subgraph(base, null, null).
     *
     * @param base the base (backing) graph on which the subgraph will be based.
     */
    public AsSubgraph(Graph<V, E> base)
    {
        this(base, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        if (containsVertex(sourceVertex) && containsVertex(targetVertex)) {
            return base
                .getAllEdges(sourceVertex, targetVertex).stream().filter(edgeSet::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        Set<E> edges = getAllEdges(sourceVertex, targetVertex);

        if (edges == null) {
            return null;
        } else {
            return edges.stream().findAny().orElse(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Supplier<V> getVertexSupplier()
    {
        return base.getVertexSupplier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Supplier<E> getEdgeSupplier()
    {
        return base.getEdgeSupplier();
    }

    /**
     * Add an edge to the subgraph. The end-points must exist in the subgraph and the edge must
     * exist in the base graph. In case multiple such edges exist in the base graph, one that is not
     * already in the subgraph is chosen arbitrarily and added to the subgraph. In case all such
     * edges already exist in the subgraph, the method returns null.
     * 
     * @param sourceVertex the source vertex
     * @param targetVertex the source vertex
     * @return the added edge or null if all such edges from the base graph already belong in the
     *         subgraph
     * @throws IllegalArgumentException if the source or target vertex does not belong to the
     *         subgraph
     * @throws IllegalArgumentException if the base graph does not contain any edge between the two
     *         end-points
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (!base.containsEdge(sourceVertex, targetVertex)) {
            throw new IllegalArgumentException(NO_SUCH_EDGE_IN_BASE);
        }

        Set<E> edges = base.getAllEdges(sourceVertex, targetVertex);

        for (E e : edges) {
            if (!containsEdge(e)) {
                edgeSet.add(e);
                return e;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        if (e == null) {
            throw new NullPointerException();
        }

        if (!base.containsEdge(e)) {
            throw new IllegalArgumentException(NO_SUCH_EDGE_IN_BASE);
        }

        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        assert (base.getEdgeSource(e) == sourceVertex);
        assert (base.getEdgeTarget(e) == targetVertex);

        return edgeSet.add(e);
    }

    @Override
    public V addVertex()
    {
        throw new UnsupportedOperationException(CANNOT_CREATE_NEW_VERTICES_FROM_SUBGRAPH);
    }

    /**
     * Adds the specified vertex to this subgraph.
     *
     * @param v the vertex to be added.
     *
     * @return <code>true</code> if the vertex was added, otherwise <code>
     * false</code>.
     *
     * @throws NullPointerException if v is null
     * @throws IllegalArgumentException if the base graph does not contain the vertex
     *
     * @see AsSubgraph
     * @see Graph#addVertex(Object)
     */
    @Override
    public boolean addVertex(V v)
    {
        if (v == null) {
            throw new NullPointerException();
        }
        if (!base.containsVertex(v)) {
            throw new IllegalArgumentException(NO_SUCH_VERTEX_IN_BASE);
        }
        return vertexSet.add(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsEdge(E e)
    {
        return edgeSet.contains(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsVertex(V v)
    {
        return vertexSet.contains(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgeSet()
    {
        if (unmodifiableEdgeSet == null) {
            unmodifiableEdgeSet = Collections.unmodifiableSet(edgeSet);
        }
        return unmodifiableEdgeSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgesOf(V vertex)
    {
        assertVertexExist(vertex);

        return base
            .edgesOf(vertex).stream().filter(edgeSet::contains)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * By default this method returns the sum of in-degree and out-degree. The exact value returned
     * depends on the types of the underlying graph.
     */
    @Override
    public int degreeOf(V vertex)
    {
        assertVertexExist(vertex);

        if (baseType.isUndirected()) {
            int degree = 0;
            Iterator<E> it = base.edgesOf(vertex).stream().filter(edgeSet::contains).iterator();
            while (it.hasNext()) {
                E e = it.next();
                degree++;
                if (getEdgeSource(e).equals(getEdgeTarget(e))) {
                    degree++;
                }
            }
            return degree;
        } else {
            return inDegreeOf(vertex) + outDegreeOf(vertex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        assertVertexExist(vertex);

        return base
            .incomingEdgesOf(vertex).stream().filter(edgeSet::contains)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        if (baseType.isUndirected()) {
            return degreeOf(vertex);
        } else {
            return incomingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        assertVertexExist(vertex);

        return base
            .outgoingEdgesOf(vertex).stream().filter(edgeSet::contains)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        if (baseType.isUndirected()) {
            return degreeOf(vertex);
        } else {
            return outgoingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeEdge(E e)
    {
        return edgeSet.remove(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        E e = getEdge(sourceVertex, targetVertex);

        return edgeSet.remove(e) ? e : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeVertex(V v)
    {
        // If the base graph does NOT contain v it means we are here in
        // response to removal of v from the base. In such case we don't need
        // to remove all the edges of v as they were already removed.
        if (containsVertex(v) && base.containsVertex(v)) {
            removeAllEdges(edgesOf(v));
        }

        return vertexSet.remove(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<V> vertexSet()
    {
        if (unmodifiableVertexSet == null) {
            unmodifiableVertexSet = Collections.unmodifiableSet(vertexSet);
        }

        return unmodifiableVertexSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeSource(E e)
    {
        return base.getEdgeSource(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeTarget(E e)
    {
        return base.getEdgeTarget(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphType getType()
    {
        return base.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getEdgeWeight(E e)
    {
        return base.getEdgeWeight(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEdgeWeight(E e, double weight)
    {
        base.setEdgeWeight(e, weight);
    }

    private void initialize(Set<? extends V> vertexFilter, Set<? extends E> edgeFilter)
    {
        if (vertexFilter == null && edgeFilter == null) {
            vertexSet.addAll(base.vertexSet());
            edgeSet.addAll(base.edgeSet());
            return;
        }

        // add vertices
        if (vertexFilter == null) {
            vertexSet.addAll(base.vertexSet());
        } else {
            if (vertexFilter.size() > base.vertexSet().size()) {
                base.vertexSet().stream().filter(vertexFilter::contains).forEach(vertexSet::add);
            } else {
                vertexFilter
                    .stream().filter(v -> v != null && base.containsVertex(v))
                    .forEach(vertexSet::add);
            }
        }

        // add edges
        if (edgeFilter == null) {
            base
                .edgeSet().stream()
                .filter(
                    e -> vertexSet.contains(base.getEdgeSource(e))
                        && vertexSet.contains(base.getEdgeTarget(e)))
                .forEach(edgeSet::add);
        } else {
            if (edgeFilter.size() > base.edgeSet().size()) {
                base
                    .edgeSet().stream()
                    .filter(
                        e -> edgeFilter.contains(e) && vertexSet.contains(base.getEdgeSource(e))
                            && vertexSet.contains(base.getEdgeTarget(e)))
                    .forEach(edgeSet::add);
            } else {
                edgeFilter
                    .stream()
                    .filter(
                        e -> e != null && base.containsEdge(e)
                            && vertexSet.contains(base.getEdgeSource(e))
                            && vertexSet.contains(base.getEdgeTarget(e)))
                    .forEach(edgeSet::add);
            }
        }
    }

    /**
     * An internal listener on the base graph.
     *
     * @author Barak Naveh
     */
    private class BaseGraphListener
        implements
        GraphListener<V, E>,
        Serializable
    {
        private static final long serialVersionUID = 4343535244243546391L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void edgeAdded(GraphEdgeChangeEvent<V, E> e)
        {
            if (isInduced) {
                E edge = e.getEdge();
                V source = e.getEdgeSource();
                V target = e.getEdgeTarget();
                if (containsVertex(source) && containsVertex(target)) {
                    addEdge(source, target, edge);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void edgeRemoved(GraphEdgeChangeEvent<V, E> e)
        {
            E edge = e.getEdge();

            removeEdge(edge);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void vertexAdded(GraphVertexChangeEvent<V> e)
        {
            // we don't care
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void vertexRemoved(GraphVertexChangeEvent<V> e)
        {
            V vertex = e.getVertex();

            removeVertex(vertex);
        }
    }

}
