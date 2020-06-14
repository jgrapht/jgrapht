package org.jgrapht.alg.densesubgraph;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.junit.Assert.assertEquals;

/**
 * base class for {@link GoldbergMaximumDensitySubgraphAlgorithm} testing
 *
 * @author Andre Immig
 */

public abstract class GoldbergMaximumDensitySubgraphTestBase<V, E>
{

    protected final double DEFAULT_EPS = Math.pow(10, -5);
    protected V s, t;

    public GoldbergMaximumDensitySubgraphTestBase()
    {
        s = this.getAdditionalSource();
        t = this.getAdditionalSink();
    }

    protected abstract MaximumDensitySubgraphAlgorithm<V, E> constructSolver(
        Graph<V, E> g,
        Function<Graph<V, DefaultWeightedEdge>, MinimumSTCutAlgorithm<V, DefaultWeightedEdge>> alg);

    protected abstract V getAdditionalSource();

    protected abstract V getAdditionalSink();

    protected void addVertices(Graph<V, E> g, List<V> vertices)
    {
        for (V v : vertices) {
            g.addVertex(v);
        }
    }

    protected <T> List<T> getByIndices(List<T> list, List<Integer> indexes)
    {
        return indexes.stream().map(list::get).collect(Collectors.toList());
    }

    protected void addEdgesAndWeights(Graph<V, E> g, List<Pair<V, V>> edges, List<Double> weights)
    {
        for (int i = 0; i < edges.size(); i++) {
            Pair<V, V> e = edges.get(i);
            g.setEdgeWeight(g.addEdge(e.getFirst(), e.getSecond()), weights.get(i));
        }
    }

    public void test(
        Graph<V, E> g, MaximumDensitySubgraphAlgorithm<V, E> solver, double expectedDensity,
        List<V> expectedVertices)
    {
        Graph<V, E> computed = solver.calculateDensest();
        assertEquals(expectedDensity, solver.getDensity(), DEFAULT_EPS);
        Graph<V, E> expected = new AsSubgraph<>(g, new LinkedHashSet<>(expectedVertices));
        assertEquals(expected, computed);
    }
}
