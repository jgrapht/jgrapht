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

import java.util.ArrayList;
import java.util.List;
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
    private static final boolean DEFAULT_ALLOW_SELFLOOPS = false;

    private final int l;
    private final int k;
    private final double p;
    private final double q;
    private final Random rng;
    private final boolean selfLoops;

    /**
     * Construct a new PlantedPartitionGraphGenerator.
     *
     * @param l number of groups
     * @param k number of nodes in each group
     * @param p probability of connecting vertices within a group
     * @param q probability of connecting vertices between groups
     * @throws IllegalArgumentException if number of groups is negative
     * @throws IllegalArgumentException if number of nodes in each group is negative
     * @throws IllegalArgumentException if p is not in [0,1]
     * @throws IllegalArgumentException if q is not in [0,1]
     */
    public PlantedPartitionGraphGenerator(int l, int k, double p, double q)
    {
        this(l, k, p, q, new Random(), DEFAULT_ALLOW_SELFLOOPS);
    }

    /**
     * Construct a new PlantedPartitionGraphGenerator.
     *
     * @param l number of groups
     * @param k number of nodes in each group
     * @param p probability of connecting vertices within a group
     * @param q probability of connecting vertices between groups
     * @param selfLoops true if the graph allows self loops
     * @throws IllegalArgumentException if number of groups is negative
     * @throws IllegalArgumentException if number of nodes in each group is negative
     * @throws IllegalArgumentException if p is not in [0,1]
     * @throws IllegalArgumentException if q is not in [0,1]
     */
    public PlantedPartitionGraphGenerator(int l, int k, double p, double q, boolean selfLoops)
    {
        this(l, k, p, q, new Random(), selfLoops);
    }

    /**
     * Construct a new PlantedPartitionGraphGenerator.
     *
     * @param l number of groups
     * @param k number of nodes in each group
     * @param p probability of connecting vertices within a group
     * @param q probability of connecting vertices between groups
     * @param seed seed for the random number generator
     * @throws IllegalArgumentException if number of groups is negative
     * @throws IllegalArgumentException if number of nodes in each group is negative
     * @throws IllegalArgumentException if p is not in [0,1]
     * @throws IllegalArgumentException if q is not in [0,1]
     */
    public PlantedPartitionGraphGenerator(int l, int k, double p, double q, long seed)
    {
        this(l, k, p, q, new Random(seed), DEFAULT_ALLOW_SELFLOOPS);
    }

    /**
     * Construct a new PlantedPartitionGraphGenerator.
     *
     * @param l number of groups
     * @param k number of nodes in each group
     * @param p probability of connecting vertices within a group
     * @param q probability of connecting vertices between groups
     * @param seed seed for the random number generator
     * @param selfLoops true if the graph allows self loops
     * @throws IllegalArgumentException if number of groups is negative
     * @throws IllegalArgumentException if number of nodes in each group is negative
     * @throws IllegalArgumentException if p is not in [0,1]
     * @throws IllegalArgumentException if q is not in [0,1]
     */
    public PlantedPartitionGraphGenerator(int l, int k, double p, double q, long seed, boolean selfLoops)
    {
        this(l, k, p, q, new Random(seed), DEFAULT_ALLOW_SELFLOOPS);
    }

    /**
     * Construct a new PlantedPartitionGraphGenerator.
     *
     * @param l number of groups
     * @param k number of nodes in each group
     * @param p probability of connecting vertices within a group
     * @param q probability of connecting vertices between groups
     * @param rng random number generator
     * @param selfLoops true if the graph allows self loops
     * @throws IllegalArgumentException if number of groups is negative
     * @throws IllegalArgumentException if number of nodes in each group is negative
     * @throws IllegalArgumentException if p is not in [0,1]
     * @throws IllegalArgumentException if q is not in [0,1]
     */
    public PlantedPartitionGraphGenerator(int l, int k, double p, double q, Random rng, boolean selfLoops)
    {
        if (l < 0) {
            throw new IllegalArgumentException("number of groups must be non-negative");
        }
        if (k < 0) {
            throw new IllegalArgumentException("number of nodes in each group must be non-negative");
        }
        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("invalid probability p");
        }
        if (q < 0 || q > 1) {
            throw new IllegalArgumentException("invalid probability q");
        }
        this.l = l;
        this.k = k;
        this.p = p;
        this.q = q;
        this.rng = rng;
        this.selfLoops = selfLoops;
    }

    /**
     * Generate an $l$-planted partition graph.
     *
     * @param target target graph
     * @param vertexFactory vertex factory
     * @param resultMap result map
     * @throws IllegalArgumentException if target is directed
     * @throws IllegalArgumentException if self loops are requested but target does not allow them
     */
    @Override
    public void generateGraph(Graph<V, E> target, VertexFactory<V> vertexFactory, Map<String, V> resultMap)
    {
        // empty graph case
        if (this.l == 0 || this.k == 0) {
            return;
        }

        // number of nodes
        int n = this.k * this.l;
        // integer to vertices
        List<V> vertices = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            V vertex = vertexFactory.createVertex();
            vertices.add(vertex);
            target.addVertex(vertex);
        }

        // add self loops
        if (this.selfLoops) {
            if (target.getType().isAllowingSelfLoops()) {
                for (V v : vertices) {
                    if (this.rng.nextDouble() < this.p) {
                        target.addEdge(v, v);
                    }
                }
            }
            else {
                throw new IllegalArgumentException("target graph must allow self-loops");
            }
        }

        // undirected edges
        if (target.getType().isUndirected()) {
            for (int i = 0; i < n; i++) {
                int li = i / this.k;  // group of node i
                for (int j = i + 1; j < n; j++) {
                    int lj = j / this.k;  // group of node j

                    // edge within partition
                    if (li == lj) {
                        if (this.rng.nextDouble() < this.p) {
                            target.addEdge(vertices.get(i), vertices.get(j));
                        }
                    }
                    // edge between partitions
                    else {
                        if (this.rng.nextDouble() < this.q) {
                            target.addEdge(vertices.get(i), vertices.get(j));
                        }
                    }
                }
            }
        }
        // directed edges
        else {
            for (int i = 0; i < n; i++) {
                int li = i / this.k;  // group of node i
                for (int j = i + 1; j < n; j++) {
                    int lj = j / this.k;  // group of node j

                    // edge within partition
                    if (li == lj) {
                        if (this.rng.nextDouble() < this.p) {
                            target.addEdge(vertices.get(i), vertices.get(j));
                        }
                        if (this.rng.nextDouble() < this.p) {
                            target.addEdge(vertices.get(j), vertices.get(i));
                        }
                    }
                    // edge between partitions
                    else {
                        if (this.rng.nextDouble() < this.q) {
                            target.addEdge(vertices.get(i), vertices.get(j));
                        }
                        if (this.rng.nextDouble() < this.q) {
                            target.addEdge(vertices.get(j), vertices.get(i));
                        }
                    }
                }
            }
        }
    }

}
