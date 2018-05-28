package org.jgrapht.alg.lca;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.LCAAlgorithm;

public class TarjanLCAFinderTest extends LCATestBase {
    @Override
    <V, E> LCAAlgorithm<V> createSolver(Graph<V, E> graph, V root) {
        return new TarjanLCAFinder<>(graph, root);
    }
}