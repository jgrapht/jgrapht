package org.jgrapht.alg.densesubgraph;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MaximumDensitySubgraphAlgorithm;
import org.jgrapht.graph.AsSubgraph;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public abstract class GoldbergMaximumDensitySubgraphTestBase<V,E> {

    public GoldbergMaximumDensitySubgraphTestBase(){
        s = this.getAdditionalSource();
        t = this.getAdditionalSink();
    }

    protected final double DEFAULT_EPS = Math.pow(10,-5);
    protected V s,t;

    protected abstract MaximumDensitySubgraphAlgorithm<V,E> constructDefaultSolver(Graph<V,E> graph);

    protected abstract V getAdditionalSource();

    protected abstract V getAdditionalSink();

    public void test(Graph<V,E> g, MaximumDensitySubgraphAlgorithm<V,E> solver, double expectedDensity, Set<V> expectedVertices){
        Graph<V,E> computed = solver.calculateDensest();
        assertEquals(expectedDensity, solver.getDensity(), DEFAULT_EPS);
        Graph<V,E> expected = new AsSubgraph<>(g, expectedVertices);
        assertEquals(expected, computed);
    }
}
