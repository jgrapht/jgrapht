package org.jgrapht.alg.interfaces;

import org.jgrapht.alg.util.Pair;

import java.util.List;
import java.util.stream.Collectors;

@FunctionalInterface
public interface LCAAlgorithm<V> {

    /**
     * Return the LCA of a and b
     *
     * @param a the first element to find LCA for
     * @param b the other element to find the LCA for
     *
     * @return the LCA of a and b, or null if there is no LCA.
     */
    V getLCA(V a, V b);

    /**
     * Return a list of LCA for a batch of queries
     *
     * @param queries a list of pairs of vertices
     * @return a list L of LCAs where L(i) is the LCA of pair queries(i)
     */
    default List<V> getLCAs(List<Pair<V, V>> queries){
        return queries.stream().map(p -> getLCA(p.getFirst(), p.getSecond())).collect(Collectors.toList());
    }
}