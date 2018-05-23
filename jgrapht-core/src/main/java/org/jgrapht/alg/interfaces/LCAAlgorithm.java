package org.jgrapht.alg.interfaces;

import org.jgrapht.alg.util.Pair;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface LCAAlgorithm<V> {

    V getLCA(V a, V b);

    default List<V> getLCAs(List<Pair<V, V>> queries){
        List<V> lcas = new ArrayList<>(queries.size());

        for (int i = 0; i < queries.size(); i++) {
            lcas.set(i, getLCA(queries.get(i).getFirst(), queries.get(i).getSecond()));
        }

        return lcas;
    }
}
