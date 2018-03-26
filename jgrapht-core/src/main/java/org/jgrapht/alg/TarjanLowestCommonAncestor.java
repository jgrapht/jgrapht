/*
 * (C) Copyright 2016-2018, by Leo Crawford and Contributors.
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
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;

/**
 * Used to calculate Tarjan's Lowest Common Ancestors Algorithm
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Leo Crawford
 */
public class TarjanLowestCommonAncestor<V, E>
{
    private Graph<V, E> g;

    /**
     * Create an instance with a reference to the graph that we will find LCAs for
     * 
     * @param g the input graph
     */
    public TarjanLowestCommonAncestor(Graph<V, E> g)
    {
        this.g = g;
    }

    /**
     * Calculate the LCM between <code>a</code> and <code>b</code> treating <code>start</code> as
     * the root we want to search from.
     * 
     * @param start the root of subtree
     * @param a the first vertex
     * @param b the second vertex
     * @return the least common ancestor
     */
    public V calculate(V start, V a, V b)
    {
        List<LcaRequestResponse<V>> list = new LinkedList<>();
        list.add(new LcaRequestResponse<>(a, b));
        return calculate(start, list).get(0);
    }

    /**
     * Calculate the LCM's between a set of pairs (<code>a</code> and <code>
     * b</code>) treating <code>start</code> as the root we want to search from, and setting the LCA
     * of each pair in its LCA field.
     * 
     * @param start the root of the subtree
     * @param lrr a list of requests-response objects. The answer if stored on these objects at the
     *        LCA field.
     * @return the LCMs
     */
    public List<V> calculate(V start, List<LcaRequestResponse<V>> lrr)
    {
        return new Worker(lrr).calculate(start);
    }

    /* The worker class keeps the state whilst doing calculations. */
    private class Worker
    {
        // The implementation of makeFind as referred to by <block>It uses the
        // MakeSet, Find, and Union functions of a disjoint-set forest.
        // MakeSet(u) removes u to a singleton set, Find(u) returns the standard
        // representative of the set containing u, and Union(u,v) merges the set
        // containing u with the set containing v. </block>
        // (http://en.wikipedia.org/wiki/Tarjan's_off-line_lowest_common_ancestors_algorithm)
        private UnionFind<V> uf = new UnionFind<>(Collections.<V> emptySet());

        // the ancestors. instead of <code>u.ancestor = x</code> we do
        // <code>ancestors.put(u,x)</code>
        private Map<V, V> ancestors = new HashMap<>();

        // instead of u.colour = black we do black.add(u)
        private Set<V> black = new HashSet<>();

        // the two vertex that we want to find the LCA for
        private List<LcaRequestResponse<V>> lrr;
        private MultiMap<V> lrrMap;

        private Worker(List<LcaRequestResponse<V>> lrr)
        {
            this.lrr = lrr;
            this.lrrMap = new MultiMap<>();

            // put in the reverse links from a and b entries back to the
            // LcaRequestReponse they're contained in
            for (LcaRequestResponse<V> r : lrr) {
                lrrMap.getOrCreate(r.getA()).add(r);
                lrrMap.getOrCreate(r.getB()).add(r);
            }
        }

        /**
         * Calculates the LCM as described by
         * http://en.wikipedia.org/wiki/Tarjan's_off-line_lowest_common_ancestors_algorithm
         * <code>function TarjanOLCA(u) MakeSet(u); u.ancestor := u; for each v
         * in u.children do TarjanOLCA(v); Union(u,v); Find(u).ancestor := u;
         * u.colour := black; for each v such that {u,v} in P do if v.colour ==
         * black print "Tarjan's Lowest Common Ancestor of " + u + " and " + v +
         * " is " + Find(v).ancestor + ".";</code>
         *
         * @param u the starting node (called recursively)
         *
         * @return the LCM if found, if not null
         */
        private List<V> calculate(final V u)
        {
            uf.addElement(u);
            ancestors.put(u, u);
            for (E vEdge : g.edgesOf(u)) {
                if (g.getEdgeSource(vEdge).equals(u)) {
                    V v = g.getEdgeTarget(vEdge);
                    calculate(v);
                    uf.union(u, v);
                    ancestors.put(uf.find(u), u);
                }
            }
            black.add(u);

            Set<LcaRequestResponse<V>> requestsForNodeU = lrrMap.get(u);
            if (requestsForNodeU != null) {
                for (LcaRequestResponse<V> rr : requestsForNodeU) {
                    if (black.contains(rr.getB()) && rr.getA().equals(u)) {
                        rr.setLca(ancestors.get(uf.find(rr.getB())));
                    }
                    if (black.contains(rr.getA()) && rr.getB().equals(u)) {
                        rr.setLca(ancestors.get(uf.find(rr.getA())));
                    }
                }

                // once we've dealt with it - remove it (to save memory?)
                lrrMap.remove(u);
            }

            List<V> result = new LinkedList<>();
            for (LcaRequestResponse<V> current : lrr) {
                result.add(current.getLca());
            }
            return result;
        }
    }

    /**
     * Data transfer object for LCA request and response.
     *
     * @param <V> the graph vertex type
     */
    public static class LcaRequestResponse<V>
    {
        private V a, b, lca;

        /**
         * Create a new LCA request response data transfer object.
         * 
         * @param a the first vertex of the request
         * @param b the second vertex of the request
         */
        public LcaRequestResponse(V a, V b)
        {
            this.a = a;
            this.b = b;
        }

        /**
         * Get the first vertex of the request
         * 
         * @return the first vertex of the request
         */
        public V getA()
        {
            return a;
        }

        /**
         * Get the second vertex of the request
         * 
         * @return the second vertex of the request
         */
        public V getB()
        {
            return b;
        }

        /**
         * Get the least common ancestor
         * 
         * @return the least common ancestor
         */
        public V getLca()
        {
            return lca;
        }

        void setLca(V lca)
        {
            this.lca = lca;
        }
    }

    @SuppressWarnings("serial")
    private static final class MultiMap<V>
        extends HashMap<V, Set<LcaRequestResponse<V>>>
    {
        public Set<LcaRequestResponse<V>> getOrCreate(V key)
        {
            if (!containsKey(key)) {
                put(key, new HashSet<>());
            }
            return get(key);
        }
    }
}

// End TarjanLowestCommonAncestor.java
