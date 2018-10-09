/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
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
import org.jgrapht.graph.specifics.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * The most general implementation of the {@link org.jgrapht.Graph} interface.
 * 
 * <p>
 * Its subclasses add various restrictions to get more specific graphs. The decision whether it is
 * directed or undirected is decided at construction time and cannot be later modified (see
 * constructor for details).
 * 
 * <p>
 * The behavior of this class can be adjusted by changing the {@link GraphSpecificsStrategy} that is
 * provided from the constructor. All implemented strategies guarantee deterministic vertex and edge
 * set ordering (via {@link LinkedHashMap} and {@link LinkedHashSet}). The defaults are reasonable
 * for most use-cases, only change if you know what you are doing.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @author Dimitrios Michail
 */
public abstract class AbstractBaseGraph<V, E>
    extends AbstractGraph<V, E>
    implements Graph<V, E>, Cloneable, Serializable
{
    private static final long serialVersionUID = -3582386521833998627L;

    private static final String LOOPS_NOT_ALLOWED = "loops not allowed";
    private static final String GRAPH_SPECIFICS_MUST_NOT_BE_NULL =
        "Graph specifics must not be null";
    private static final String INVALID_VERTEX_SUPPLIER_DOES_NOT_RETURN_UNIQUE_VERTICES_ON_EACH_CALL =
        "Invalid vertex supplier (does not return unique vertices on each call).";
    private static final String MIXED_GRAPH_NOT_SUPPORTED = "Mixed graph not supported";
    private static final String GRAPH_SPECIFICS_STRATEGY_REQUIRED = "Graph specifics strategy required";
    private static final String THE_GRAPH_CONTAINS_NO_VERTEX_SUPPLIER = "The graph contains no vertex supplier";
    private static final String THE_GRAPH_CONTAINS_NO_EDGE_SUPPLIER = "The graph contains no edge supplier";

    
    private transient Set<V> unmodifiableVertexSet = null;

    private Supplier<V> vertexSupplier;
    private Supplier<E> edgeSupplier;
    private GraphType type;

    private Specifics<V, E> specifics;
    private IntrusiveEdgesSpecifics<V, E> intrusiveEdgesSpecifics;
    private GraphSpecificsStrategy<V, E> graphSpecificsStrategy;

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
        /*
         * Replace with the following code after the next release
         * 
         * this(vertexSupplier, edgeSupplier, type, new FastLookupGraphSpecificsStrategy());
         */
        this.vertexSupplier = vertexSupplier;
        this.edgeSupplier = edgeSupplier;
        this.type = Objects.requireNonNull(type);
        if (type.isMixed()) {
            throw new IllegalArgumentException(MIXED_GRAPH_NOT_SUPPORTED);
        }
        this.graphSpecificsStrategy = new BackwardsCompatibleGraphSpecificsStrategy();
        this.specifics = Objects.requireNonNull(
            graphSpecificsStrategy.getSpecificsFactory().apply(this, type),
            GRAPH_SPECIFICS_MUST_NOT_BE_NULL);
        this.intrusiveEdgesSpecifics = Objects.requireNonNull(
            graphSpecificsStrategy.getIntrusiveEdgesSpecificsFactory().apply(type),
            GRAPH_SPECIFICS_MUST_NOT_BE_NULL);
    }

    /**
     * Construct a new graph.
     *
     * @param vertexSupplier the vertex supplier, can be null
     * @param edgeSupplier the edge supplier, can be null
     * @param type the graph type
     * @param graphSpecificsStrategy strategy for constructing low-level graph specifics
     *
     * @throws IllegalArgumentException if the graph type is mixed
     */
    protected AbstractBaseGraph(
        Supplier<V> vertexSupplier, Supplier<E> edgeSupplier, GraphType type,
        GraphSpecificsStrategy<V, E> graphSpecificsStrategy)
    {
        this.vertexSupplier = vertexSupplier;
        this.edgeSupplier = edgeSupplier;
        this.type = Objects.requireNonNull(type);
        if (type.isMixed()) {
            throw new IllegalArgumentException(MIXED_GRAPH_NOT_SUPPORTED);
        }

        this.graphSpecificsStrategy =
            Objects.requireNonNull(graphSpecificsStrategy, GRAPH_SPECIFICS_STRATEGY_REQUIRED);
        this.specifics = Objects.requireNonNull(
            graphSpecificsStrategy.getSpecificsFactory().apply(this, type),
            GRAPH_SPECIFICS_MUST_NOT_BE_NULL);
        this.intrusiveEdgesSpecifics = Objects.requireNonNull(
            graphSpecificsStrategy.getIntrusiveEdgesSpecificsFactory().apply(type),
            GRAPH_SPECIFICS_MUST_NOT_BE_NULL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        return specifics.getAllEdges(sourceVertex, targetVertex);
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
     * <p>
     * Care must also be taken when interchanging calls to methods {@link Graph#addVertex(Object)}
     * and {@link Graph#addVertex()}. In such a case the user must make sure never to add vertices
     * in the graph using method {@link Graph#addVertex(Object)}, which are going to be returned in
     * the future by the supplied vertex supplier. Such a sequence will result into an
     * {@link IllegalArgumentException} when calling method {@link Graph#addVertex()}.
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

        if (edgeSupplier == null) {
            throw new UnsupportedOperationException(THE_GRAPH_CONTAINS_NO_EDGE_SUPPLIER);
        }

        E e = edgeSupplier.get();
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
            throw new UnsupportedOperationException(THE_GRAPH_CONTAINS_NO_VERTEX_SUPPLIER);
        }

        V v = vertexSupplier.get();

        if (!specifics.addVertex(v)) { 
            throw new IllegalArgumentException(INVALID_VERTEX_SUPPLIER_DOES_NOT_RETURN_UNIQUE_VERTICES_ON_EACH_CALL);
        }
        return v;
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
            newGraph.type = type;
            newGraph.unmodifiableVertexSet = null;

            newGraph.graphSpecificsStrategy = this.graphSpecificsStrategy;

            // NOTE: it's important for this to happen in an object
            // method so that the new inner class instance gets associated with
            // the right outer class instance
            newGraph.specifics = newGraph.graphSpecificsStrategy
                .getSpecificsFactory().apply(newGraph, newGraph.type);
            newGraph.intrusiveEdgesSpecifics = newGraph.graphSpecificsStrategy
                .getIntrusiveEdgesSpecificsFactory().apply(newGraph.type);

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
     * 
     * @deprecated In favor of using factories
     */
    @Deprecated
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
     * 
     * @deprecated In favor of using factories
     */
    @Deprecated
    protected IntrusiveEdgesSpecifics<V, E> createIntrusiveEdgesSpecifics(boolean weighted)
    {
        if (weighted) {
            return new WeightedIntrusiveEdgesSpecifics<>();
        } else {
            return new UniformIntrusiveEdgesSpecifics<>();
        }
    }

    /*
     * Added for backwards compatibility, remove after next release.
     */
    @Deprecated
    class BackwardsCompatibleGraphSpecificsStrategy
        implements GraphSpecificsStrategy<V, E>
    {
        @Override
        public Function<GraphType,
            IntrusiveEdgesSpecifics<V, E>> getIntrusiveEdgesSpecificsFactory()
        {
            return (type) -> createIntrusiveEdgesSpecifics(type.isWeighted());
        }

        @Override
        public BiFunction<Graph<V, E>, GraphType, Specifics<V, E>> getSpecificsFactory()
        {
            return (graph, type) -> {
                return createSpecifics(type.isDirected());
            };
        }
    }

}

