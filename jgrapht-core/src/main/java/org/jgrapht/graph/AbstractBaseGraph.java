/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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
package org.jgrapht.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.Graphs;
import org.jgrapht.graph.specifics.FastLookupDirectedSpecifics;
import org.jgrapht.graph.specifics.FastLookupUndirectedSpecifics;
import org.jgrapht.graph.specifics.Specifics;
import org.jgrapht.util.TypeUtil;

/**
 * The most general implementation of the {@link org.jgrapht.Graph} interface. Its subclasses add
 * various restrictions to get more specific graphs. The decision whether it is directed or
 * undirected is decided at construction time and cannot be later modified (see constructor for
 * details).
 *
 * <p>
 * This graph implementation guarantees deterministic vertex and edge set ordering (via
 * {@link LinkedHashMap} and {@link LinkedHashSet}).
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @author Dimitrios Michail
 * @since Jul 24, 2003
 */
public abstract class AbstractBaseGraph<V, E>
    extends AbstractGraph<V, E>
    implements Graph<V, E>, Cloneable, Serializable
{
    private static final long serialVersionUID = -3582386521833998627L;
    
    private static final String LOOPS_NOT_ALLOWED = "loops not allowed";
    private static final String GRAPH_SPECIFICS_MUST_NOT_BE_NULL =
        "Graph specifics must not be null";

    private transient Set<V> unmodifiableVertexSet = null;

    private Supplier<V> vertexSupplier;
    private Supplier<E> edgeSupplier;
    
    @Deprecated
    private EdgeFactory<V, E> edgeFactory;

    private GraphType type;
    private Specifics<V, E> specifics;
    private IntrusiveEdgesSpecifics<V, E> intrusiveEdgesSpecifics;

    /**
     * Construct a new graph.
     *
     * @param vertexSupplier the vertex supplier, can be null
     * @param edgeSupplier the edge supplier, can be null
     * @param type the graph type
     *
     * @throws IllegalArgumentException if the graph type is mixed
     */
    protected AbstractBaseGraph(
        Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, GraphType type)
    {
        this.vertexSupplier = vertexSupplier;
        this.edgeSupplier = edgeSupplier;
        this.type = Objects.requireNonNull(type);
        if (type.isMixed()) {
            throw new IllegalArgumentException("Mixed graph not supported");
        }
        this.specifics = Objects
            .requireNonNull(createSpecifics(type.isDirected()), GRAPH_SPECIFICS_MUST_NOT_BE_NULL);
        this.intrusiveEdgesSpecifics = Objects.requireNonNull(
            createIntrusiveEdgesSpecifics(type.isWeighted()), GRAPH_SPECIFICS_MUST_NOT_BE_NULL);
        this.edgeFactory = new BackwardsCompatibleEdgeFactory(null);
    }

    /**
     * Construct a new graph. The graph can either be directed or undirected, depending on the
     * specified edge factory.
     *
     * @param ef the edge factory of the new graph.
     * @param directed if true the graph will be directed, otherwise undirected
     * @param allowMultipleEdges whether to allow multiple (parallel) edges or not.
     * @param allowLoops whether to allow edges that are self-loops or not.
     * @param weighted whether the graph is weighted, i.e. the edges support a weight attribute
     *
     * @throws NullPointerException if the specified edge factory is <code>
     * null</code>.
     * @deprecated Use suppliers instead 
     */
    @Deprecated
    protected AbstractBaseGraph(
        EdgeFactory<V, E> ef, boolean directed, boolean allowMultipleEdges, boolean allowLoops,
        boolean weighted)
    {
        this(
            null, null,
            new DefaultGraphType.Builder(directed, !directed)
                .allowMultipleEdges(allowMultipleEdges).allowSelfLoops(allowLoops)
                .weighted(weighted).build());
        this.edgeFactory = new BackwardsCompatibleEdgeFactory(ef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        return specifics.getAllEdges(sourceVertex, targetVertex);
    }

    /**
     * Returns <code>true</code> if and only if self-loops are allowed in this graph. A self loop is
     * an edge that its source and target vertices are the same.
     *
     * @return <code>true</code> if and only if graph loops are allowed.
     * @deprecated Use type instead 
     */
    @Deprecated
    public boolean isAllowingLoops()
    {
        return type.isAllowingSelfLoops();
    }

    /**
     * Returns <code>true</code> if and only if multiple (parallel) edges are allowed in this graph.
     * The meaning of multiple edges is that there can be many edges going from vertex v1 to vertex
     * v2.
     *
     * @return <code>true</code> if and only if multiple (parallel) edges are allowed.
     * @deprecated Use type instead 
     */
    @Deprecated
    public boolean isAllowingMultipleEdges()
    {
        return type.isAllowingMultipleEdges();
    }

    /**
     * Returns <code>true</code> if and only if the graph supports edge weights.
     *
     * @return <code>true</code> if the graph supports edge weights, <code>false</code> otherwise.
     * @deprecated Use type instead 
     */
    @Deprecated
    public boolean isWeighted()
    {
        return type.isWeighted();
    }

    /**
     * Returns <code>true</code> if the graph is directed, false if undirected.
     *
     * @return <code>true</code> if the graph is directed, false if undirected.
     * @deprecated Use type instead 
     */
    @Deprecated
    public boolean isDirected()
    {
        return type.isDirected();
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use supplier instead 
     */
    @Deprecated
    @Override
    public EdgeFactory<V, E> getEdgeFactory()
    {
        return edgeFactory;
    }

    @Override
    public Supplier<E> getEdgeSupplier()
    {
        return edgeSupplier;
    }

    /**
     * Set the edge supplier that the graph uses whenever it needs to create new edges.
     * 
     * <p>
     * A graph uses the edge supplier to create new edge objects whenever a user calls method
     * {@link Graph#addEdge(Object, Object)}. Users can also create the edge in user code and then
     * use method {@link Graph#addEdge(Object, Object, Object)} to add the edge.
     * 
     * <p>
     * In contrast with the {@link Supplier} interface, the edge supplier has the additional
     * requirement that a new and distinct result is returned every time it is invoked. More
     * specifically for a new edge to be added in a graph <code>e</code> must <i>not</i> be equal to
     * any other edge in the graph (even if the graph allows edge-multiplicity). More formally, the
     * graph must not contain any edge <code>e2</code> such that <code>e2.equals(e)</code>.
     * 
     * @param edgeSupplier the edge supplier
     */
    public void setEdgeSupplier(Supplier<E> edgeSupplier)
    {
        this.edgeSupplier = edgeSupplier;
    }

    @Override
    public Supplier<V> getVertexSupplier()
    {
        return vertexSupplier;
    }

    /**
     * Set the vertex supplier that the graph uses whenever it needs to create new vertices.
     * 
     * <p>
     * A graph uses the vertex supplier to create new vertex objects whenever a user calls method
     * {@link Graph#addVertex()}. Users can also create the vertex in user code and then use method
     * {@link Graph#addVertex(Object)} to add the vertex.
     * 
     * <p>
     * In contrast with the {@link Supplier} interface, the vertex supplier has the additional
     * requirement that a new and distinct result is returned every time it is invoked. More
     * specifically for a new vertex to be added in a graph <code>v</code> must <i>not</i> be equal
     * to any other vertex in the graph. More formally, the graph must not contain any vertex
     * <code>v2</code> such that <code>v2.equals(v)</code>.
     * 
     * @param vertexSupplier the vertex supplier
     */
    public void setVertexSupplier(Supplier<V> vertexSupplier)
    {
        this.vertexSupplier = vertexSupplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        return specifics.getEdge(sourceVertex, targetVertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (!type.isAllowingMultipleEdges() && containsEdge(sourceVertex, targetVertex)) {
            return null;
        }

        if (!type.isAllowingSelfLoops() && sourceVertex.equals(targetVertex)) {
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
        }

        E e = edgeFactory.createEdge(sourceVertex, targetVertex);

        //@formatter:off
        /*
         * After next release, replace above call with code below:
         * 
         * if (edgeSupplier == null) { 
         *     throw new UnsupportedOperationException("The graph contains no edge supplier"); 
         * } 
         * 
         * E e = edgeSupplier.get();
         */
        //@formatter:on

        if (intrusiveEdgesSpecifics.add(e, sourceVertex, targetVertex)) {
            specifics.addEdgeToTouchingVertices(e);
            return e;
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

        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (!type.isAllowingMultipleEdges() && containsEdge(sourceVertex, targetVertex)) {
            return false;
        }

        if (!type.isAllowingSelfLoops() && sourceVertex.equals(targetVertex)) {
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
        }

        if (intrusiveEdgesSpecifics.add(e, sourceVertex, targetVertex)) {
            specifics.addEdgeToTouchingVertices(e);
            return true;
        }

        return false;
    }

    @Override
    public V addVertex()
    {
        if (vertexSupplier == null) {
            throw new UnsupportedOperationException("The graph contains no vertex supplier");
        }

        V v = vertexSupplier.get();

        if (specifics.addVertex(v)) {
            return v;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addVertex(V v)
    {
        if (v == null) {
            throw new NullPointerException();
        } else if (containsVertex(v)) {
            return false;
        } else {
            specifics.addVertex(v);
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeSource(E e)
    {
        return intrusiveEdgesSpecifics.getEdgeSource(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeTarget(E e)
    {
        return intrusiveEdgesSpecifics.getEdgeTarget(e);
    }

    /**
     * Returns a shallow copy of this graph instance. Neither edges nor vertices are cloned.
     *
     * @return a shallow copy of this graph.
     *
     * @throws RuntimeException in case the clone is not supported
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone()
    {
        try {
            AbstractBaseGraph<V, E> newGraph = TypeUtil.uncheckedCast(super.clone());

            newGraph.vertexSupplier = this.vertexSupplier;
            newGraph.edgeSupplier = this.edgeSupplier;
            newGraph.edgeFactory = this.edgeFactory;

            newGraph.unmodifiableVertexSet = null;

            // NOTE: it's important for this to happen in an object
            // method so that the new inner class instance gets associated with
            // the right outer class instance
            newGraph.specifics = newGraph.createSpecifics(this.getType().isDirected());
            newGraph.intrusiveEdgesSpecifics =
                newGraph.createIntrusiveEdgesSpecifics(this.getType().isWeighted());

            Graphs.addGraph(newGraph, this);

            return newGraph;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsEdge(E e)
    {
        return intrusiveEdgesSpecifics.containsEdge(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsVertex(V v)
    {
        return specifics.getVertexSet().contains(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int degreeOf(V vertex)
    {
        assertVertexExist(vertex);
        return specifics.degreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgeSet()
    {
        return intrusiveEdgesSpecifics.getEdgeSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> edgesOf(V vertex)
    {
        assertVertexExist(vertex);
        return specifics.edgesOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        assertVertexExist(vertex);
        return specifics.inDegreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        assertVertexExist(vertex);
        return specifics.incomingEdgesOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        assertVertexExist(vertex);
        return specifics.outDegreeOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        assertVertexExist(vertex);
        return specifics.outgoingEdgesOf(vertex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        E e = getEdge(sourceVertex, targetVertex);

        if (e != null) {
            specifics.removeEdgeFromTouchingVertices(e);
            intrusiveEdgesSpecifics.remove(e);
        }

        return e;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeEdge(E e)
    {
        if (containsEdge(e)) {
            specifics.removeEdgeFromTouchingVertices(e);
            intrusiveEdgesSpecifics.remove(e);
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeVertex(V v)
    {
        if (containsVertex(v)) {
            Set<E> touchingEdgesList = edgesOf(v);

            // cannot iterate over list - will cause
            // ConcurrentModificationException
            removeAllEdges(new ArrayList<>(touchingEdgesList));

            specifics.getVertexSet().remove(v); // remove the vertex itself

            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<V> vertexSet()
    {
        if (unmodifiableVertexSet == null) {
            unmodifiableVertexSet = Collections.unmodifiableSet(specifics.getVertexSet());
        }

        return unmodifiableVertexSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getEdgeWeight(E e)
    {
        if (e == null) {
            throw new NullPointerException();
        }
        return intrusiveEdgesSpecifics.getEdgeWeight(e);
    }

    /**
     * Set an edge weight.
     * 
     * @param e the edge
     * @param weight the weight
     * @throws UnsupportedOperationException if the graph is not weighted
     */
    @Override
    public void setEdgeWeight(E e, double weight)
    {
        if (e == null) {
            throw new NullPointerException();
        }
        intrusiveEdgesSpecifics.setEdgeWeight(e, weight);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphType getType()
    {
        return type;
    }

    /**
     * Create the specifics for this graph. Subclasses can override this method in order to adjust
     * the specifics and thus the space-time tradeoffs of the graph implementation.
     * 
     * @param directed if true the specifics should adjust the behavior to a directed graph
     *        otherwise undirected
     * @return the specifics used by this graph
     */
    protected Specifics<V, E> createSpecifics(boolean directed)
    {
        if (directed) {
            return new FastLookupDirectedSpecifics<>(this);
        } else {
            return new FastLookupUndirectedSpecifics<>(this);
        }
    }

    /**
     * Create the specifics for the edges set of the graph.
     * 
     * @param weighted if true the specifics should support weighted edges
     * @return the specifics used for the edge set of this graph
     */
    protected IntrusiveEdgesSpecifics<V, E> createIntrusiveEdgesSpecifics(boolean weighted)
    {
        if (weighted) {
            return new WeightedIntrusiveEdgesSpecifics<>();
        } else {
            return new UniformIntrusiveEdgesSpecifics<>();
        }
    }

    @Deprecated
    private class BackwardsCompatibleEdgeFactory
        implements EdgeFactory<V, E>, Serializable
    {
        private EdgeFactory<V, E> ef;

        public BackwardsCompatibleEdgeFactory(EdgeFactory<V, E> ef)
        {
            this.ef = ef;
        }

        @Override
        public E createEdge(V sourceVertex, V targetVertex)
        {
            if (edgeSupplier == null && this.ef == null) {
                throw new UnsupportedOperationException("The graph contains no edge supplier");
            }
            if (edgeSupplier != null) { 
                return edgeSupplier.get();
            } else { 
                return ef.createEdge(sourceVertex, targetVertex);
            }
        }

    }

}

// End AbstractBaseGraph.java
