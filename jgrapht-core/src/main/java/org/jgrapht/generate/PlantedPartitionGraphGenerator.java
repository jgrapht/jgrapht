/*
 * (C) Copyright 2018-2018, by Emilio Cruciani and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;

import java.util.Map;
import java.util.Random;

/**
 * Create a random $l$-planted partition graph.
 * An $l$-planted partition graph is a random graph on $n = l \cdot k$ vertices
 * subdivided in $l$ groups with $k$ vertices each.
 * Vertices within the same group are connected by an edge with probability $p$,
 * while vertices belonging to different groups are connected by an edge with probability $q$.
 *
 * <p>
 * The $l$-planted partition model is a special case of the
 * <a href="https://en.wikipedia.org/wiki/Stochastic_block_model">Stochastic Block Model</a>.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Emilio Cruciani
 * @since April 2018
 */
public class PlantedPartitionGraphGenerator<V, E> implements GraphGenerator<V, E, V>
{
    private final int l;
    private final int k;
    private final double p;
    private final double q;
    private final Random rng;

    /**
     * Construct a new PlantedPartitionGraphGenerator.
     *
     * @param l number of groups
     * @param k number of nodes in each group
     * @param p probability of connecting vertices within a group
     * @param q probability of connecting vertices between groups
     * @throws IllegalArgumentException if number of groups is negative
     * @throws IllegalArgumentException if number of nodes in each group is negative
     * @throws IllegalArgumentException if p is negative
     * @throws IllegalArgumentException if p is greater than 1
     * @throws IllegalArgumentException if q is negative
     * @throws IllegalArgumentException if q is greater than 1
     */
    public PlantedPartitionGraphGenerator(int l, int k, double p, double q)
    {
        this(l, k, p, q, new Random());
    }

    /**
     * Construct a new PlantedPartitionGraphGenerator.
     *
     * @param l number of groups
     * @param k number of nodes in each group
     * @param p probability of connecting vertices within a group
     * @param q probability of connecting vertices between groups
     * @throws IllegalArgumentException if number of groups is negative
     * @throws IllegalArgumentException if number of nodes in each group is negative
     * @throws IllegalArgumentException if p is negative
     * @throws IllegalArgumentException if p is greater than 1
     * @throws IllegalArgumentException if q is negative
     * @throws IllegalArgumentException if q is greater than 1
     */
    public PlantedPartitionGraphGenerator(int l, int k, double p, double q, long seed)
    {
        this(l, k, p, q, new Random(seed));
    }

    /**
     * Construct a new PlantedPartitionGraphGenerator.
     *
     * @param l number of groups
     * @param k number of nodes in each group
     * @param p probability of connecting vertices within a group
     * @param q probability of connecting vertices between groups
     * @throws IllegalArgumentException if number of groups is negative
     * @throws IllegalArgumentException if number of nodes in each group is negative
     * @throws IllegalArgumentException if p is negative
     * @throws IllegalArgumentException if p is greater than 1
     * @throws IllegalArgumentException if q is negative
     * @throws IllegalArgumentException if q is greater than 1
     */
    public PlantedPartitionGraphGenerator(int l, int k, double p, double q, Random rng)
    {
        if (l < 0) {
            throw new IllegalArgumentException("number of groups must be non-negative");
        }
        if (k < 0) {
            throw new IllegalArgumentException("number of nodes in each group must be non-negative");
        }
        if (p < 0) {
            throw new IllegalArgumentException("it must hold that p > 0");
        }
        if (p > 1) {
            throw new IllegalArgumentException("it must hold that p < 1");
        }
        if (q < 0) {
            throw new IllegalArgumentException("it must hold that q > 0");
        }
        if (q > 1) {
            throw new IllegalArgumentException("it must hold that q < 1");
        }
        this.l = l;
        this.k = k;
        this.p = p;
        this.q = q;
        this.rng = rng;
    }


    @Override public void generateGraph(Graph<V, E> target, VertexFactory<V> vertexFactory, Map<String, V> resultMap)
    {
        if (this.l == 0 || this.k == 0) {
            return;
        }

        /* to be implemented */
    }
}
