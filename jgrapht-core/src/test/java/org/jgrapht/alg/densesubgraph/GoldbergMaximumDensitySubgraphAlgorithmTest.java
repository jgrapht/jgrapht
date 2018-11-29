package org.jgrapht.alg.densesubgraph;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.*;
import org.junit.*;
import java.util.*;

/**
 * Tests for {@link GoldbergMaximumDensitySubgraphAlgorithm}
 *
 * @author Andre Immig
 */


public class GoldbergMaximumDensitySubgraphAlgorithmTest extends GoldbergMaximumDensitySubgraphTestBase<Integer, DefaultEdge>{

    @Override
    protected MaximumDensitySubgraphAlgorithm<Integer, DefaultEdge> constructDefaultSolver(Graph<Integer, DefaultEdge> graph){
        return new GoldbergMaximumDensitySubgraphAlgorithm<>(graph, s,t, DEFAULT_EPS);
    }

    @Override
    protected Integer getAdditionalSource(){
        return -1;
    }

    @Override
    protected Integer getAdditionalSink(){
        return -2;
    }

    @Test
    public void testEmpty1(){
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        test(g, constructDefaultSolver(g),0, new ArrayList<>());
    }

    @Test
    public void testEmpty2(){
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        addVertices(g, Arrays.asList(0,1));
        test(g, constructDefaultSolver(g),0, new ArrayList<>());
    }

    @Test
    public void testMinimal(){
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        addVertices(g, Arrays.asList(0,1));
        addEdgesAndWeights(g,Arrays.asList(new Pair<>(0,1)),Arrays.asList(10.0));
        test(g, constructDefaultSolver(g), 5, Arrays.asList(0,1));
    }

    @Test
    public void testSmall1(){
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        addVertices(g, Arrays.asList(0,1,2,3, 4));
        List<Pair<Integer,Integer>> edges = Arrays.asList(
                new Pair<>(0,3), new Pair<>(0,1),
                new Pair<>(0,2), new Pair<>(4,2),
                new Pair<>(0,4), new Pair<>(2,3));
        List<Double> weights = Arrays.asList(
                            2.0, 1.0,
                            1.0, 1.0,
                            3.0, 1.0);
        addEdgesAndWeights(g,edges,weights);
        test(g, constructDefaultSolver(g),2, Arrays.asList(0,2,3,4));
    }

    @Test
    public void testSmall2(){
        SimpleWeightedGraph<Integer, DefaultEdge> g = new SimpleWeightedGraph<>(DefaultEdge.class);
        addVertices(g, Arrays.asList(0,1,2,3,4,5,6,7));
        List<Pair<Integer,Integer>> edges = Arrays.asList(
                new Pair<>(0,1), new Pair<>(1,2), new Pair<>(2,3),
                new Pair<>(3,4), new Pair<>(4,5), new Pair<>(5,6),
                new Pair<>(6,7), new Pair<>(1,7), new Pair<>(2,7),
                new Pair<>(3,7), new Pair<>(4,2));
        List<Double> weights = Arrays.asList(
                            3.0, 2.0, 1.0,
                            2.0, 1.0, 3.0,
                            1.0, 2.0, 1.0,
                            4.0, 1.0);
        addEdgesAndWeights(g,edges,weights);
        test(g, constructDefaultSolver(g),
            2.66666666, Arrays.asList(0, 1, 2, 3, 4, 7));
    }

    @Test
    public void testSmallWeights(){
        SimpleDirectedWeightedGraph<Integer, DefaultEdge> g = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
        addVertices(g, Arrays.asList(0,1,2,3, 4));
        List<Pair<Integer,Integer>> edges = Arrays.asList(
                new Pair<>(0,3), new Pair<>(0,1), new Pair<>(0,2),
                new Pair<>(4,2), new Pair<>(0,4), new Pair<>(2,3));
        List<Double> weights = Arrays.asList(
                0.0002, 0.00000001, 0.001,
                0.0009, 0.003,      0.001);
        addEdgesAndWeights(g,edges,weights);
        test(g, constructDefaultSolver(g),
            0.001633333, Arrays.asList(0, 2, 4));
    }
}
