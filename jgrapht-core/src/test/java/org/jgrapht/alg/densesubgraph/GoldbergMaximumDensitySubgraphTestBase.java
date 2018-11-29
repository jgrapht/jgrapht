package org.jgrapht.alg.densesubgraph;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MaximumDensitySubgraphAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.AsSubgraph;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    protected void addVertices(Graph<V,E> g, List<V> vertices){
        for (V v: vertices){
            g.addVertex(v);
        }
    }

    protected <T> List<T> getByIndices(List<T> list, List<Integer> indexes) {
        return indexes.stream().map(list::get).collect(Collectors.toList());
    }

    protected void addEdgesAndWeights(Graph<V,E> g, List<Pair<V,V>> edges, List<Double> weights){
        for (int i=0; i<edges.size();i++){
            Pair<V,V> e = edges.get(i);
            g.setEdgeWeight(g.addEdge(e.getFirst(),e.getSecond()),weights.get(i));
        }
    }

    public void test(Graph<V,E> g, MaximumDensitySubgraphAlgorithm<V,E> solver, double expectedDensity, List<V> expectedVertices){
        Graph<V,E> computed = solver.calculateDensest();
        assertEquals(expectedDensity, solver.getDensity(), DEFAULT_EPS);
        Graph<V,E> expected = new AsSubgraph<>(g, new LinkedHashSet<>(expectedVertices));
        assertEquals(expected, computed);
    }
}
