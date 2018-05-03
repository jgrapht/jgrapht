/*
 * (C) Copyright 2013-2018, by Alexey Kudinkin and Contributors.
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
package org.jgrapht.alg.interfaces;

import java.io.*;
import java.util.*;

import org.jgrapht.*;

/**
 * Allows to derive a <a href="http://en.wikipedia.org/wiki/Matching_(graph_theory)">matching</a> of
 * a given graph.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface MatchingAlgorithm<V, E>
{
    /**
     * Default tolerance used by algorithms comparing floating point values.
     */
    double DEFAULT_EPSILON = 1e-9;

    /**
     * Compute a matching for a given graph.
     *
     * @return a matching
     */
    Matching<V, E> getMatching();

    /**
     * A graph matching.
     *
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    interface Matching<V, E>
        extends Iterable<E>
    {
        /**
         * Returns the graph over which this matching is defined.
         *
         * @return the graph
         */
        Graph<V, E> getGraph();

        /**
         * Returns the weight of the matching.
         *
         * @return the weight of the matching
         */
        double getWeight();

        /**
         * Get the edges of the matching.
         *
         * @return the edges of the matching
         */
        Set<E> getEdges();

        /**
         * Returns true if vertex v is incident to an edge in this matching.
         * 
         * @param v vertex
         * @return true if vertex v is incident to an edge in this matching.
         */
        default boolean isMatched(V v)
        {
            Set<E> edges = getEdges();
            return getGraph().edgesOf(v).stream().anyMatch(edges::contains);
        }

        /**
         * Returns true if the matching is a perfect matching. A matching is perfect if every vertex
         * in the graph is incident to an edge in the matching.
         * 
         * @return true if the matching is perfect. By definition, a perfect matching consists of
         *         exactly $\frac{1}{2|V|}$ edges, and the number of vertices in the graph must be even.
         */
        default boolean isPerfect()
        {
            return getEdges().size() == getGraph().vertexSet().size() / 2.0;
        }

        /**
         * Returns an iterator over the edges in the matching.
         * 
         * @return iterator over the edges in the matching.
         */
        @Override
        default Iterator<E> iterator()
        {
            return getEdges().iterator();
        }
    }

    /**
     * A default implementation of the matching interface.
     * 
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    class MatchingImpl<V, E>
        implements Matching<V, E>, Serializable
    {
        private static final long serialVersionUID = 4767675421846527768L;

        private Graph<V, E> graph;
        private Set<E> edges;
        private double weight;
        private Set<V> matchedVertices = null;

        /**
         * Construct a new instance
         *
         * @param graph graph on which the matching is defined
         * @param edges the edges of the matching
         * @param weight the weight of the matching
         */
        public MatchingImpl(Graph<V, E> graph, Set<E> edges, double weight)
        {
            this.graph = graph;
            this.edges = edges;
            this.weight = weight;
        }

        @Override
        public Graph<V, E> getGraph()
        {
            return graph;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getWeight()
        {
            return weight;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Set<E> getEdges()
        {
            return edges;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isMatched(V v)
        {
            if (matchedVertices == null) { // lazily index the vertices that have been matched
                matchedVertices = new HashSet<>();
                for (E e : edges) {
                    matchedVertices.add(graph.getEdgeSource(e));
                    matchedVertices.add(graph.getEdgeTarget(e));
                }
            }
            return matchedVertices.contains(v);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return "Matching [edges=" + edges + ", weight=" + weight + "]";
        }
    }

}

// End MatchingAlgorithm.java
