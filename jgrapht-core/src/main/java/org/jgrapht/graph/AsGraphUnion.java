/*
 * (C) Copyright 2009-2018, by Ilya Razenshteyn and Contributors.
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

import java.io.*;
import java.util.*;
import java.util.function.Supplier;

import org.jgrapht.*;
import org.jgrapht.util.*;

/**
 * Read-only union of two graphs.
 * 
 * <p>
 * Read-only union of two graphs: G<sub>1</sub> and G<sub>2</sub>. If G<sub>1</sub> =
 * (V<sub>1</sub>, E<sub>1</sub>) and G<sub>2</sub> = (V<sub>2</sub>, E<sub>2</sub>) then their
 * union G = (V, E), where V is the union of V<sub>1</sub> and V<sub>2</sub>, and E is the union of
 * E<sub>1</sub> and E<sub>2</sub>. A {@link WeightCombiner} in order to calculate edge weights.
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 * 
 * @author Ilya Razenshteyn
 */
public class AsGraphUnion<V, E>
    extends AbstractGraph<V, E>
    implements Serializable
{
    private static final long serialVersionUID = -3848082143382987713L;

    private static final String READ_ONLY = "union of graphs is read-only";

    private final Graph<V, E> g1;
    private final GraphType type1;
    private final Graph<V, E> g2;
    private final GraphType type2;
    private final GraphType type;
    private final WeightCombiner operator;

    /**
     * Construct a new graph union.
     * 
     * @param g1 the first graph
     * @param g2 the second graph
     * @param operator the weight combiner (policy for edge weight calculation)
     */
    public AsGraphUnion(Graph<V, E> g1, Graph<V, E> g2, WeightCombiner operator)
    {
        this.g1 = GraphTests.requireDirectedOrUndirected(g1);
        this.type1 = g1.getType();

        this.g2 = GraphTests.requireDirectedOrUndirected(g2);
        this.type2 = g2.getType();

        if (g1 == g2) {
            throw new IllegalArgumentException("g1 is equal to g2");
        }
        this.operator = Objects.requireNonNull(operator, "Weight combiner cannot be null");

        // compute result type
        DefaultGraphType.Builder builder = new DefaultGraphType.Builder();
        if (type1.isDirected() && type2.isDirected()) {
            builder = builder.directed();
        } else if (type1.isUndirected() && type2.isUndirected()) {
            builder = builder.undirected();
        } else {
            builder = builder.mixed();
        }
        this.type = builder
            .allowSelfLoops(type1.isAllowingSelfLoops() || type2.isAllowingSelfLoops())
            .allowMultipleEdges(true).weighted(true).modifiable(false).build();
    }

    /**
     * Construct a new graph union. The union will use the {@link WeightCombiner#SUM} weight
     * combiner.
     * 
     * @param g1 the first graph
     * @param g2 the second graph
     */
    public AsGraphUnion(Graph<V, E> g1, Graph<V, E> g2)
    {
        this(g1, g2, WeightCombiner.SUM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        Set<E> res = new LinkedHashSet<>();
        if (g1.containsVertex(sourceVertex) && g1.containsVertex(targetVertex)) {
            res.addAll(g1.getAllEdges(sourceVertex, targetVertex));
        }
        if (g2.containsVertex(sourceVertex) && g2.containsVertex(targetVertex)) {
            res.addAll(g2.getAllEdges(sourceVertex, targetVertex));
        }
        return Collections.unmodifiableSet(res);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        E res = null;
        if (g1.containsVertex(sourceVertex) && g1.containsVertex(targetVertex)) {
            res = g1.getEdge(sourceVertex, targetVertex);
        }
        if ((res == null) && g2.containsVertex(sourceVertex) && g2.containsVertex(targetVertex)) {
            res = g2.getEdge(sourceVertex, targetVertex);
        }
        return res;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     * @deprecated Use suppliers instead 
     */
    @Override
    @Deprecated
    public EdgeFactory<V, E> getEdgeFactory()
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public Supplier<V> getVertexSupplier()
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public Supplier<E> getEdgeSupplier()
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public V addVertex()
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public boolean addVertex(V v)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsEdge(E e)
    {
        return g1.containsEdge(e) || g2.containsEdge(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsVertex(V v)
    {
        return g1.containsVertex(v) || g2.containsVertex(v);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Note that this operation might by expensive as it involves copying the edge sets of the two
     * underlying graphs.
     */
    @Override
    public Set<E> edgeSet()
    {
        Set<E> res = new LinkedHashSet<>();
        res.addAll(g1.edgeSet());
        res.addAll(g2.edgeSet());
        return Collections.unmodifiableSet(res);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Note that this operation might by expensive as it involves copying the edge sets of the two
     * underlying graphs.
     */
    @Override
    public Set<E> edgesOf(V vertex)
    {
        Set<E> res = new LinkedHashSet<>();
        if (g1.containsVertex(vertex)) {
            res.addAll(g1.edgesOf(vertex));
        }
        if (g2.containsVertex(vertex)) {
            res.addAll(g2.edgesOf(vertex));
        }
        return Collections.unmodifiableSet(res);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Note that this operation might by expensive as it involves copying the edge sets of the two
     * underlying graphs.
     */
    @Override
    public Set<E> incomingEdgesOf(V vertex)
    {
        Set<E> res = new LinkedHashSet<>();
        if (g1.containsVertex(vertex)) {
            res.addAll(g1.incomingEdgesOf(vertex));
        }
        if (g2.containsVertex(vertex)) {
            res.addAll(g2.incomingEdgesOf(vertex));
        }
        return Collections.unmodifiableSet(res);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Note that this operation might by expensive as it involves copying the edge sets of the two
     * underlying graphs.
     */
    @Override
    public Set<E> outgoingEdgesOf(V vertex)
    {
        Set<E> res = new LinkedHashSet<>();
        if (g1.containsVertex(vertex)) {
            res.addAll(g1.outgoingEdgesOf(vertex));
        }
        if (g2.containsVertex(vertex)) {
            res.addAll(g2.outgoingEdgesOf(vertex));
        }
        return Collections.unmodifiableSet(res);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int degreeOf(V vertex)
    {
        if (type.isMixed()) {
            int d = 0;
            if (g1.containsVertex(vertex)) {
                d += g1.degreeOf(vertex);
            }
            if (g2.containsVertex(vertex)) {
                d += g2.degreeOf(vertex);
            }
            return d;
        } else if (type.isUndirected()) {
            int degree = 0;
            Iterator<E> it = edgesOf(vertex).iterator();
            while (it.hasNext()) {
                E e = it.next();
                degree++;
                if (getEdgeSource(e).equals(getEdgeTarget(e))) {
                    degree++;
                }
            }
            return degree;
        } else {
            return incomingEdgesOf(vertex).size() + outgoingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inDegreeOf(V vertex)
    {
        if (type.isMixed()) {
            int d = 0;
            if (g1.containsVertex(vertex)) {
                d += g1.inDegreeOf(vertex);
            }
            if (g2.containsVertex(vertex)) {
                d += g2.inDegreeOf(vertex);
            }
            return d;
        } else if (type.isUndirected()) {
            return degreeOf(vertex);
        } else {
            return incomingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int outDegreeOf(V vertex)
    {
        if (type.isMixed()) {
            int d = 0;
            if (g1.containsVertex(vertex)) {
                d += g1.outDegreeOf(vertex);
            }
            if (g2.containsVertex(vertex)) {
                d += g2.outDegreeOf(vertex);
            }
            return d;
        } else if (type.isUndirected()) {
            return degreeOf(vertex);
        } else {
            return outgoingEdgesOf(vertex).size();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public boolean removeEdge(E e)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException always, since operation is unsupported
     */
    @Override
    public boolean removeVertex(V v)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Note that this operation might by expensive as it currently involves copying the vertex sets
     * of the two underlying graphs.
     */
    @Override
    public Set<V> vertexSet()
    {
        Set<V> res = new HashSet<>();
        res.addAll(g1.vertexSet());
        res.addAll(g2.vertexSet());
        return Collections.unmodifiableSet(res);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeSource(E e)
    {
        if (g1.containsEdge(e)) {
            return g1.getEdgeSource(e);
        }
        if (g2.containsEdge(e)) {
            return g2.getEdgeSource(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getEdgeTarget(E e)
    {
        if (g1.containsEdge(e)) {
            return g1.getEdgeTarget(e);
        }
        if (g2.containsEdge(e)) {
            return g2.getEdgeTarget(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getEdgeWeight(E e)
    {
        if (g1.containsEdge(e) && g2.containsEdge(e)) {
            return operator.combine(g1.getEdgeWeight(e), g2.getEdgeWeight(e));
        }
        if (g1.containsEdge(e)) {
            return g1.getEdgeWeight(e);
        }
        if (g2.containsEdge(e)) {
            return g2.getEdgeWeight(e);
        }
        throw new IllegalArgumentException("no such edge in the union");
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
     * Throws {@link UnsupportedOperationException} since graph union is read-only.
     */
    @Override
    public void setEdgeWeight(E e, double weight)
    {
        throw new UnsupportedOperationException(READ_ONLY);
    }
}

// End GraphUnion.java
