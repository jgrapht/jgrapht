package org.jgrapht.alg.lca;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.LCAAlgorithm;

import java.util.Set;

public class HeavyPathLCAFinderTest extends LCATestBase {

    @Override
    <V, E> LCAAlgorithm<V> createSolver(Graph<V, E> graph, Set<V> roots) {
        return new HeavyPathLCAFinder<>(graph, roots);
    }
}