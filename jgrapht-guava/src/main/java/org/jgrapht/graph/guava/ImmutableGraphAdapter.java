/*
 * (C) Copyright 2018-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.graph.guava;

import com.google.common.graph.*;
import com.google.common.graph.Graphs;
import org.jgrapht.Graph;
import org.jgrapht.*;
import org.jgrapht.util.*;

import java.io.*;

/**
 * A graph adapter class using Guava's {@link ImmutableGraph}.
 * 
 * <p>
 * The adapter uses class {@link EndpointPair} to represent edges. Since the underlying graph is
 * immutable, the resulting graph is unmodifiable.
 *
 * <p>
 * See the example below on how to create such an adapter: <blockquote>
 * 
 * <pre>
 * MutableGraph&lt;String&gt; mutableGraph = GraphBuilder.directed().allowsSelfLoops(true).build();
 * 
 * mutableGraph.addNode("v1");
 * mutableGraph.addNode("v2");
 * mutableGraph.addEdge("v1", "v2");
 * 
 * ImmutableGraph&lt;String&gt; immutableGraph = ImmutableGraph.copyOf(mutableGraph);
 * 
 * Graph&lt;String, EndpointPair&lt;String&gt;&gt; graph = new ImmutableGraphAdapter&lt;&gt;(immutableGraph);
 * </pre>
 * 
 * </blockquote>
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 */
public class ImmutableGraphAdapter<V>
    extends
    BaseGraphAdapter<V, ImmutableGraph<V>>
    implements
    Graph<V, EndpointPair<V>>,
    Cloneable,
    Serializable
{
    private static final long serialVersionUID = -6619929013881511474L;

    protected static final String GRAPH_IS_IMMUTABLE = "Graph is immutable";

    /**
     * Create a new adapter.
     * 
     * @param graph the graph
     */
    public ImmutableGraphAdapter(ImmutableGraph<V> graph)
    {
        super(graph);
    }

    @Override
    public EndpointPair<V> addEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(GRAPH_IS_IMMUTABLE);
    }

    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, EndpointPair<V> e)
    {
        throw new UnsupportedOperationException(GRAPH_IS_IMMUTABLE);
    }

    @Override
    public V addVertex()
    {
        throw new UnsupportedOperationException(GRAPH_IS_IMMUTABLE);
    }

    @Override
    public boolean addVertex(V v)
    {
        throw new UnsupportedOperationException(GRAPH_IS_IMMUTABLE);
    }

    @Override
    public EndpointPair<V> removeEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(GRAPH_IS_IMMUTABLE);
    }

    @Override
    public boolean removeEdge(EndpointPair<V> e)
    {
        throw new UnsupportedOperationException(GRAPH_IS_IMMUTABLE);
    }

    @Override
    public boolean removeVertex(V v)
    {
        throw new UnsupportedOperationException(GRAPH_IS_IMMUTABLE);
    }

    @Override
    public void setEdgeWeight(EndpointPair<V> e, double weight)
    {
        throw new UnsupportedOperationException(GRAPH_IS_IMMUTABLE);
    }

    @Override
    public GraphType getType()
    {
        return super.getType().asUnmodifiable();
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
            ImmutableGraphAdapter<V> newGraph = TypeUtil.uncheckedCast(super.clone());

            newGraph.unmodifiableVertexSet = null;
            newGraph.unmodifiableEdgeSet = null;
            newGraph.graph = ImmutableGraph.copyOf(Graphs.copyOf(this.graph));

            return newGraph;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private void writeObject(ObjectOutputStream oos)
        throws IOException
    {
        oos.defaultWriteObject();

        // write type
        oos.writeObject(getType());

        // write vertices
        int n = vertexSet().size();
        oos.writeInt(n);
        for (V v : vertexSet()) {
            oos.writeObject(v);
        }

        // write edges
        int m = edgeSet().size();
        oos.writeInt(m);
        for (EndpointPair<V> e : edgeSet()) {
            V u = e.nodeU();
            V v = e.nodeV();
            oos.writeObject(u);
            oos.writeObject(v);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException,
        IOException
    {
        ois.defaultReadObject();

        GraphType type = (GraphType) ois.readObject();
        if (type.isMixed() || type.isAllowingMultipleEdges()) {
            throw new IOException("Graph type not supported");
        }

        MutableGraph<V> mutableGraph =
            (type.isDirected() ? GraphBuilder.directed() : GraphBuilder.undirected())
                .allowsSelfLoops(type.isAllowingSelfLoops()).build();

        // read vertices
        int n = ois.readInt();
        for (int i = 0; i < n; i++) {
            V v = (V) ois.readObject();
            mutableGraph.addNode(v);
        }

        // read edges
        int m = ois.readInt();
        for (int i = 0; i < m; i++) {
            V s = (V) ois.readObject();
            V t = (V) ois.readObject();
            mutableGraph.putEdge(s, t);
        }

        // setup the immutable copy
        this.graph = ImmutableGraph.copyOf(mutableGraph);
    }

}
