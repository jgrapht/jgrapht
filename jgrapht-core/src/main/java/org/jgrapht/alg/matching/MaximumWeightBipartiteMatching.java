/*
 * (C) Copyright 2017-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jheaps.*;
import org.jheaps.tree.*;

import java.math.*;
import java.util.*;
import java.util.function.*;

/**
 * Maximum weight matching in bipartite graphs.
 * 
 * <p>
 * Running time is $O(n(m+n \log n))$ where n is the number of vertices and m the number of edges of
 * the input graph. Uses exact arithmetic and produces a certificate of optimality in the form of a
 * tight vertex potential function.
 * 
 * <p>
 * This is the algorithm and implementation described in the
 * <a href="https://people.mpi-inf.mpg.de/~mehlhorn/LEDAbook.html">LEDA book</a>. See the LEDA
 * Platform of Combinatorial and Geometric Computing, Cambridge University Press, 1999.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 */
public class MaximumWeightBipartiteMatching<V, E>
    implements
    MatchingAlgorithm<V, E>
{
    private final Graph<V, E> graph;
    private final Set<V> partition1;
    private final Set<V> partition2;

    private final Comparator<BigDecimal> comparator;
    private final Function<Comparator<BigDecimal>, AddressableHeap<BigDecimal, V>> heapSupplier;

    // vertex potentials
    private Map<V, BigDecimal> pot;
    // the matched edge of a vertex, also used to check if a vertex is free
    private Map<V, E> matchedEdge;

    // shortest path related data structures
    private AddressableHeap<BigDecimal, V> heap;
    private Map<V, AddressableHeap.Handle<BigDecimal, V>> nodeInHeap;
    private Map<V, E> pred;
    private Map<V, BigDecimal> dist;

    // the actual result
    private Set<E> matching;
    private BigDecimal matchingWeight;

    /**
     * Constructor.
     * 
     * @param graph the input graph
     * @param partition1 the first partition of the vertex set
     * @param partition2 the second partition of the vertex set
     * @throws IllegalArgumentException if the graph is not undirected
     */
    public MaximumWeightBipartiteMatching(Graph<V, E> graph, Set<V> partition1, Set<V> partition2)
    {
        this(graph, partition1, partition2, (comparator) -> new FibonacciHeap<>(comparator));
    }

    /**
     * Constructor.
     * 
     * @param graph the input graph
     * @param partition1 the first partition of the vertex set
     * @param partition2 the second partition of the vertex set
     * @param heapSupplier a supplier for the addressable heap to use in the algorithm.
     * @throws IllegalArgumentException if the graph is not undirected
     */
    public MaximumWeightBipartiteMatching(
        Graph<V, E> graph, Set<V> partition1, Set<V> partition2,
        Function<Comparator<BigDecimal>, AddressableHeap<BigDecimal, V>> heapSupplier)
    {
        this.graph = GraphTests.requireUndirected(graph);
        this.partition1 = Objects.requireNonNull(partition1, "Partition 1 cannot be null");
        this.partition2 = Objects.requireNonNull(partition2, "Partition 2 cannot be null");
        this.comparator = Comparator.<BigDecimal> naturalOrder();
        this.heapSupplier = Objects.requireNonNull(heapSupplier, "Heap supplier cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matching<V, E> getMatching()
    {
        /*
         * Test input instance
         */
        if (!GraphTests.isSimple(graph)) {
            throw new IllegalArgumentException("Only simple graphs supported");
        }
        if (!GraphTests.isBipartitePartition(graph, partition1, partition2)) {
            throw new IllegalArgumentException("Graph partition is not bipartite");
        }

        // initialize result
        matching = new LinkedHashSet<>();
        matchingWeight = BigDecimal.ZERO;

        // empty graph
        if (graph.edgeSet().isEmpty()) {
            return new MatchingImpl<>(graph, matching, matchingWeight.doubleValue());
        }

        // initialize
        pot = new HashMap<>();
        dist = new HashMap<>();
        matchedEdge = new HashMap<>();
        heap = heapSupplier.apply(comparator);
        nodeInHeap = new HashMap<>();
        pred = new HashMap<>();
        graph.vertexSet().forEach(v -> {
            pot.put(v, BigDecimal.ZERO);
            pred.put(v, null);
            dist.put(v, BigDecimal.ZERO);
        });

        // run simple heuristic
        simpleHeuristic();

        // augment to optimality
        for (V v : partition1) {
            if (!matchedEdge.containsKey(v)) {
                augment(v);
            }
        }

        return new MatchingImpl<>(graph, matching, matchingWeight.doubleValue());
    }

    /**
     * Get the vertex potentials.
     * 
     * <p>
     * This is a tight non-negative potential function which proves the optimality of the maximum
     * weight matching. See any standard textbook about linear programming duality.
     * 
     * @return the vertex potentials
     */
    public Map<V, BigDecimal> getPotentials()
    {
        if (pot == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(pot);
        }
    }

    /**
     * Get the weight of the matching.
     * 
     * @return the weight of the matching
     */
    public BigDecimal getMatchingWeight()
    {
        return matchingWeight;
    }

    /**
     * Augment from a particular node. The algorithm always looks for augmenting paths from nodes in
     * partition1. In the following code partition1 is $A$ and partition2 is $B$.
     * 
     * @param a the node
     */
    private void augment(V a)
    {
        dist.put(a, BigDecimal.ZERO);
        V bestInA = a;
        BigDecimal minA = pot.get(a);
        BigDecimal delta;

        Deque<V> reachedA = new ArrayDeque<>();
        reachedA.push(a);
        Deque<V> reachedB = new ArrayDeque<>();

        // relax all edges out of a1
        V a1 = a;
        for (E e1 : graph.edgesOf(a1)) {
            if (!matching.contains(e1)) {
                V b1 = Graphs.getOppositeVertex(graph, e1, a1);
                BigDecimal db1 = dist
                    .get(a1).add(pot.get(a1)).add(pot.get(b1))
                    .subtract(BigDecimal.valueOf(graph.getEdgeWeight(e1)));

                if (pred.get(b1) == null) {
                    dist.put(b1, db1);
                    pred.put(b1, e1);
                    reachedB.push(b1);

                    AddressableHeap.Handle<BigDecimal, V> node = heap.insert(db1, b1);
                    nodeInHeap.put(b1, node);
                } else {
                    if (comparator.compare(db1, dist.get(b1)) < 0) {
                        dist.put(b1, db1);
                        pred.put(b1, e1);
                        nodeInHeap.get(b1).decreaseKey(db1);
                    }
                }
            }
        }

        while (true) {
            /*
             * select from priority queue the node b with minimal distance db
             */
            V b = null;
            BigDecimal db = BigDecimal.ZERO;
            if (!heap.isEmpty()) {
                b = heap.deleteMin().getValue();
                nodeInHeap.remove(b);
                db = dist.get(b);
            }

            /*
             * three cases
             */
            if (b == null || comparator.compare(db, minA) >= 0) {
                delta = minA;
                augmentPathTo(bestInA);
                break;
            } else {
                E e = matchedEdge.get(b);
                if (e == null) {
                    delta = db;
                    augmentPathTo(b);
                    break;
                } else {
                    a1 = Graphs.getOppositeVertex(graph, e, b);
                    pred.put(a1, e);
                    reachedA.push(a1);
                    dist.put(a1, db);

                    if (comparator.compare(db.add(pot.get(a1)), minA) < 0) {
                        bestInA = a1;
                        minA = db.add(pot.get(a1));
                    }

                    // relax all edges out of a1
                    for (E e1 : graph.edgesOf(a1)) {
                        if (!matching.contains(e1)) {
                            V b1 = Graphs.getOppositeVertex(graph, e1, a1);
                            BigDecimal db1 = dist
                                .get(a1).add(pot.get(a1)).add(pot.get(b1))
                                .subtract(BigDecimal.valueOf(graph.getEdgeWeight(e1)));
                            if (pred.get(b1) == null) {
                                dist.put(b1, db1);
                                pred.put(b1, e1);
                                reachedB.push(b1);

                                AddressableHeap.Handle<BigDecimal, V> node = heap.insert(db1, b1);
                                nodeInHeap.put(b1, node);
                            } else {
                                if (comparator.compare(db1, dist.get(b1)) < 0) {
                                    dist.put(b1, db1);
                                    pred.put(b1, e1);
                                    nodeInHeap.get(b1).decreaseKey(db1);
                                }
                            }
                        }
                    }
                }
            }
        }

        // augment: potential update and re-initialization
        while (!reachedA.isEmpty()) {
            V v = reachedA.pop();
            pred.put(v, null);
            BigDecimal potChange = delta.subtract(dist.get(v));
            if (comparator.compare(potChange, BigDecimal.ZERO) <= 0) {
                continue;
            }
            pot.put(v, pot.get(v).subtract(potChange));
        }

        while (!reachedB.isEmpty()) {
            V v = reachedB.pop();
            pred.put(v, null);
            if (nodeInHeap.containsKey(v)) {
                nodeInHeap.remove(v).delete();
            }
            BigDecimal potChange = delta.subtract(dist.get(v));
            if (comparator.compare(potChange, BigDecimal.ZERO) <= 0) {
                continue;
            }
            pot.put(v, pot.get(v).add(potChange));
        }
    }

    private void augmentPathTo(V v)
    {
        List<E> matched = new ArrayList<>();
        List<E> free = new ArrayList<>();

        E e1 = pred.get(v);
        while (e1 != null) {
            if (matching.contains(e1)) {
                matched.add(e1);
            } else {
                free.add(e1);
            }
            v = Graphs.getOppositeVertex(graph, e1, v);
            e1 = pred.get(v);
        }

        for (E e : matched) {
            BigDecimal w = BigDecimal.valueOf(graph.getEdgeWeight(e));
            V s = graph.getEdgeSource(e);
            V t = graph.getEdgeTarget(e);
            matchedEdge.remove(s);
            matchedEdge.remove(t);
            matchingWeight = matchingWeight.subtract(w);
            matching.remove(e);
        }

        for (E e : free) {
            BigDecimal w = BigDecimal.valueOf(graph.getEdgeWeight(e));
            V s = graph.getEdgeSource(e);
            V t = graph.getEdgeTarget(e);
            matchedEdge.put(s, e);
            matchedEdge.put(t, e);
            matchingWeight = matchingWeight.add(w);
            matching.add(e);
        }
    }

    private void simpleHeuristic()
    {
        for (V v : partition1) {
            E maxEdge = null;
            BigDecimal maxWeight = BigDecimal.ZERO;

            for (E e : graph.edgesOf(v)) {
                BigDecimal w = BigDecimal.valueOf(graph.getEdgeWeight(e));
                if (comparator.compare(w, maxWeight) > 0) {
                    maxWeight = w;
                    maxEdge = e;
                }
            }

            pot.put(v, maxWeight);
            if (maxEdge != null) {
                V u = Graphs.getOppositeVertex(graph, maxEdge, v);
                if (!matchedEdge.containsKey(u)) {
                    matching.add(maxEdge);
                    matchingWeight = matchingWeight.add(maxWeight);
                    matchedEdge.put(v, maxEdge);
                    matchedEdge.put(u, maxEdge);
                }
            }
        }
    }
}
