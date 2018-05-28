package org.jgrapht.alg.lca;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.LCAAlgorithm;

public class EulerTourRMQLCAFinderTest extends LCATestBase {

    @Override
    <V, E> LCAAlgorithm<V> createSolver(Graph<V, E> graph, V root) {
        return new EulerTourRMQLCAFinder<>(graph, root);
    }
}