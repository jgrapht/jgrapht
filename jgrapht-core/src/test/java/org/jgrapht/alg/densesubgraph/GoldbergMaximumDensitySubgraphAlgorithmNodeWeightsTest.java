package org.jgrapht.alg.densesubgraph;

import org.jgrapht.*;
import org.jgrapht.alg.flow.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.junit.*;
import java.util.*;

/**
 * Tests for {@link GoldbergMaximumDensitySubgraphAlgorithm}
 *
 * @author Andre Immig
 */


public class GoldbergMaximumDensitySubgraphAlgorithmNodeWeightsTest extends GoldbergMaximumDensitySubgraphTestBase<Pair<Integer,Double>, DefaultEdge>{

    protected MaximumDensitySubgraphAlgorithm<Pair<Integer,Double>, DefaultEdge> constructDefaultSolver(Graph<Pair<Integer,Double>, DefaultEdge> graph){
        return new GoldbergMaximumDensitySubgraphAlgorithmNodeWeights<>(graph, s,t, DEFAULT_EPS, DinicMFImpl::new);
    }

    @Override
    protected Pair<Integer, Double> getAdditionalSink(){
        return new Pair<>(-1,0.0);
    }

    @Override
    protected Pair<Integer, Double> getAdditionalSource(){
        return new Pair<>(-2,0.0);
    }

    @Test
    public void testEmpty1(){
        WeightedMultigraph<Pair<Integer,Double>, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        test(g, constructDefaultSolver(g),0, new ArrayList<>());
    }

    @Test
    public void testEmpty2(){
        WeightedMultigraph<Pair<Integer,Double>, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        Pair<Integer,Double> p1 = new Pair<>(0,1.3);
        Pair<Integer,Double> p2 = new Pair<>(1, 2.1);
        addVertices(g, Arrays.asList(p1,p2));
        test(g, constructDefaultSolver(g),2.1, Arrays.asList(p2));
    }

    @Test
    public void testMinimal(){
        SimpleDirectedWeightedGraph<Pair<Integer,Double>, DefaultEdge> g = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
        Pair<Integer, Double> v1 = new Pair<>(1,1.5);
        Pair<Integer, Double> v2 = new Pair<>(0,2.5);
        addVertices(g, Arrays.asList(v1,v2));
        addEdgesAndWeights(g, Arrays.asList(new Pair<>(v1,v2)),Arrays.asList(10.0));
        test(g, constructDefaultSolver(g), 7, Arrays.asList(v1,v2));
    }

    @Test
    public void testSmall1() {
        SimpleWeightedGraph<Pair<Integer, Double>, DefaultEdge> g = new SimpleWeightedGraph<>(DefaultEdge.class);
        ArrayList<Pair<Integer, Double>> vertices = new ArrayList<>();
        vertices.add(new Pair<>(0, 1.51));
        vertices.add(new Pair<>(1, 1.0));
        vertices.add(new Pair<>(2, 1.0));
        addVertices(g, vertices);
        addEdgesAndWeights(g,  Arrays.asList(new Pair<>(vertices.get(0), vertices.get(1)),
                                             new Pair<>(vertices.get(0), vertices.get(2))),
                                Arrays.asList(4.0,2.0));
        test(g, constructDefaultSolver(g),
                3.255, getByIndices(vertices, Arrays.asList(0, 1)));
    }


    @Test
    public void testSmall2(){
        SimpleWeightedGraph<Pair<Integer,Double>, DefaultEdge> g = new SimpleWeightedGraph<>(DefaultEdge.class);
        ArrayList<Pair<Integer,Double>> vertices = new ArrayList<>();
        for (int i=0; i<=7 ;i++){
            vertices.add(new Pair<>(i,1.1));
        }
        addVertices(g, vertices);
        List<Pair<Pair<Integer,Double>,Pair<Integer,Double>>> edges = Arrays.asList(
                new Pair<>(vertices.get(0),vertices.get(1)), new Pair<>(vertices.get(1),vertices.get(2)),
                new Pair<>(vertices.get(2),vertices.get(3)), new Pair<>(vertices.get(3),vertices.get(4)),
                new Pair<>(vertices.get(4),vertices.get(5)), new Pair<>(vertices.get(5),vertices.get(6)),
                new Pair<>(vertices.get(6),vertices.get(7)), new Pair<>(vertices.get(1),vertices.get(7)),
                new Pair<>(vertices.get(2),vertices.get(7)), new Pair<>(vertices.get(3),vertices.get(7)),
                new Pair<>(vertices.get(4),vertices.get(2)));
        List<Double> weights = Arrays.asList(
                3.0, 2.0, 1.0,
                2.0, 1.0, 3.0,
                1.0, 2.0, 1.0,
                4.0, 1.0);
        addEdgesAndWeights(g,edges,weights);
        test(g, constructDefaultSolver(g),
            3.76666666, getByIndices(vertices,Arrays.asList(0,1,2,3,4,7)));
    }
}
