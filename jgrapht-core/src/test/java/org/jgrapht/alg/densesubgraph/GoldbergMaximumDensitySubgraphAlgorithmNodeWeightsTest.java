package org.jgrapht.alg.densesubgraph;

import org.jgrapht.*;
import org.jgrapht.alg.flow.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests for {@link GoldbergMaximumDensitySubgraphAlgorithm}
 *
 * @author Andre Immig
 */


public class GoldbergMaximumDensitySubgraphAlgorithmNodeWeightsTest
{

    private final double DEFAULT_EPS = Math.pow(10,-5);
    @Test
    public void testMinimal(){
        SimpleDirectedWeightedGraph<Pair<Integer,Double>, DefaultEdge> g = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
        Pair<Integer, Double> v1 = new Pair<>(1,1.5);
        Pair<Integer, Double> v2 = new Pair<>(0,2.5);
        g.addVertex(v1);
        g.addVertex(v2);
        g.setEdgeWeight(g.addEdge(v1,v2),10);
        test(g, constructDefaultSolver(g), 7, new LinkedHashSet<>(Arrays.asList(v1,v2)));
    }

    private MaximumDensitySubgraphAlgorithm<Pair<Integer,Double>, DefaultEdge> constructDefaultSolver(Graph<Pair<Integer,Double>, DefaultEdge> graph)
    {
            Pair<Integer, Double> s = new Pair<>(-1,0.0);
            Pair<Integer, Double> t = new Pair<>(-2,0.0);
            return new GoldbergMaximumDensitySubgraphAlgorithmNodeWeights<>(graph, s,t, DEFAULT_EPS, DinicMFImpl::new);
    }

    @Test
    public void testEmpty1(){
        WeightedMultigraph<Pair<Integer,Double>, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        test(g, constructDefaultSolver(g),0, new LinkedHashSet<>());
    }

    @Test
    public void testEmpty2(){
        WeightedMultigraph<Pair<Integer,Double>, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        Pair<Integer,Double> p1 = new Pair<>(0,1.3);
        Pair<Integer,Double> p2 = new Pair<>(1, 2.1);
        g.addVertex(p1);
        g.addVertex(p2);
        test(g, constructDefaultSolver(g),2.1, new LinkedHashSet<>(Collections.singletonList(p2)));
    }

    @Test
    public void testSmall1(){
        SimpleWeightedGraph<Pair<Integer,Double>, DefaultEdge> g = new SimpleWeightedGraph<>(DefaultEdge.class);
        ArrayList<Pair<Integer,Double>> vertices = new ArrayList<>();
        vertices.add(new Pair<>(0,1.51));
        vertices.add(new Pair<>(1,1.0));
        vertices.add(new Pair<>(2,1.0));
        for (int i=0; i<=2 ;i++) {
            g.addVertex(vertices.get(i));
        }
        g.setEdgeWeight(g.addEdge(vertices.get(0),vertices.get(1)),4);
        g.setEdgeWeight(g.addEdge(vertices.get(0),vertices.get(2)),2);
        test(g, constructDefaultSolver(g),
            3.255, new LinkedHashSet<>(Arrays.asList(vertices.get(0),vertices.get(1))));
    }


    @Test
    public void testSmall2(){
        SimpleWeightedGraph<Pair<Integer,Double>, DefaultEdge> g = new SimpleWeightedGraph<>(DefaultEdge.class);
        ArrayList<Pair<Integer,Double>> vertices = new ArrayList<>();
        for (int i=0; i<=7 ;i++){
            vertices.add(new Pair<>(i,1.1));
            g.addVertex(vertices.get(i));
        }
        g.setEdgeWeight(g.addEdge(vertices.get(0),vertices.get(1)),3);
        g.setEdgeWeight(g.addEdge(vertices.get(1),vertices.get(2)),2);
        g.setEdgeWeight(g.addEdge(vertices.get(2),vertices.get(3)),1);
        g.setEdgeWeight(g.addEdge(vertices.get(3),vertices.get(4)),2);
        g.setEdgeWeight(g.addEdge(vertices.get(4),vertices.get(5)),1);
        g.setEdgeWeight(g.addEdge(vertices.get(5),vertices.get(6)),3);
        g.setEdgeWeight(g.addEdge(vertices.get(6),vertices.get(7)),1);
        g.setEdgeWeight(g.addEdge(vertices.get(1),vertices.get(7)),2);
        g.setEdgeWeight(g.addEdge(vertices.get(2),vertices.get(7)),1);
        g.setEdgeWeight(g.addEdge(vertices.get(3),vertices.get(7)),4);
        g.setEdgeWeight(g.addEdge(vertices.get(4),vertices.get(2)),1);
        test(g, constructDefaultSolver(g),
            3.76666666, new LinkedHashSet<>(Arrays.asList(vertices.get(0),vertices.get(1),vertices.get(2),vertices.get(3),
            vertices.get(4),vertices.get(7))));
    }

    public void test(Graph<Pair<Integer,Double>,DefaultEdge> g, MaximumDensitySubgraphAlgorithm<Pair<Integer,Double>,DefaultEdge> solver,
        double expectedDensity, Set<Pair<Integer,Double>> expectedVertices){
        Graph<Pair<Integer,Double>,DefaultEdge> computed = solver.calculateDensest();
        assertEquals(expectedDensity, solver.getDensity(), DEFAULT_EPS);
        Graph<Pair<Integer,Double>,DefaultEdge> expected = new AsSubgraph<>(g, expectedVertices);
        assertEquals(expected, computed);
    }
}
