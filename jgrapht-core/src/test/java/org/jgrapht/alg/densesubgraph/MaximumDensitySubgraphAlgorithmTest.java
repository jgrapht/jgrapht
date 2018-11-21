package org.jgrapht.alg.densesubgraph;

import org.jgrapht.*;
import org.jgrapht.alg.flow.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link GoldbergMaximumDensitySubgraphAlgorithm}
 *
 * @author Andre Immig
 */


public class MaximumDensitySubgraphAlgorithmTest {

    private final double DEFAULT_EPS = Math.pow(10,-5);
    @Test
    public void testMinimal(){
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        g.setEdgeWeight(g.addEdge(0,1),10);
        test(g, constructDefaultSolver(g), 5, new LinkedHashSet<>(Arrays.asList(0,1)));
    }

    private MaximumDensitySubgraphAlgorithm<Integer, DefaultEdge> constructDefaultSolver(Graph<Integer, DefaultEdge> graph)
    {
        try {
            MinimumSTCutAlgorithm<Integer, DefaultEdge> alg = new PushRelabelMFImpl<>(graph);
            return new GoldbergMaximumDensitySubgraphAlgorithm<>(alg, graph, -1, -2);
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    public void testSmall1(){
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        for (int i=0; i<=4; i++){
            g.addVertex(i);
        }
        g.setEdgeWeight(g.addEdge(0,3),2);
        g.setEdgeWeight(g.addEdge(0,1),1);
        g.setEdgeWeight(g.addEdge(0,2),1);
        g.setEdgeWeight(g.addEdge(4,2),1);
        g.setEdgeWeight(g.addEdge(0,4),3);
        g.setEdgeWeight(g.addEdge(2,3),1);
        test(g, constructDefaultSolver(g),
            2, new LinkedHashSet<>(Arrays.asList(0,2,3,4)));
    }

    @Test
    public void testSmall2(){
        SimpleWeightedGraph<Integer, DefaultEdge> g = new SimpleWeightedGraph<>(DefaultEdge.class);
        for (int i=0; i<=7 ;i++){
            g.addVertex(i);
        }
        g.setEdgeWeight(g.addEdge(0,1),3);
        g.setEdgeWeight(g.addEdge(1,2),2);
        g.setEdgeWeight(g.addEdge(2,3),1);
        g.setEdgeWeight(g.addEdge(3,4),2);
        g.setEdgeWeight(g.addEdge(4,5),1);
        g.setEdgeWeight(g.addEdge(5,6),3);
        g.setEdgeWeight(g.addEdge(6,7),1);
        g.setEdgeWeight(g.addEdge(1,7),2);
        g.setEdgeWeight(g.addEdge(2,7),1);
        g.setEdgeWeight(g.addEdge(3,7),4);
        g.setEdgeWeight(g.addEdge(4,2),1);
        test(g, constructDefaultSolver(g),
            2.66666666, new LinkedHashSet<>(Arrays.asList(0, 1, 2, 3, 4, 7)));
    }

    @Test
    public void testSmallWeights(){
        SimpleDirectedWeightedGraph<Integer, DefaultEdge> g = new SimpleDirectedWeightedGraph<>(DefaultEdge.class);
        for (int i=0;i<=4; i++){
            g.addVertex(i);
        }
        g.setEdgeWeight(g.addEdge(0,3),0.0002);
        g.setEdgeWeight(g.addEdge(0,1),0.00000001);
        g.setEdgeWeight(g.addEdge(0,2),0.001);
        g.setEdgeWeight(g.addEdge(4,2),0.0009);
        g.setEdgeWeight(g.addEdge(0,4),0.003);
        g.setEdgeWeight(g.addEdge(2,3),0.001);
        test(g, constructDefaultSolver(g),
            0.001633333, new LinkedHashSet<>(Arrays.asList(0, 2, 4)));
    }

    public void test(Graph<Integer,DefaultEdge> g, MaximumDensitySubgraphAlgorithm<Integer,
                         DefaultEdge> solver, double expectedDensity, Set<Integer> expectedVertices){
        Graph<Integer,DefaultEdge> computed = solver.calculateDensest(DEFAULT_EPS);
        assertEquals(expectedDensity, solver.getDensity(), DEFAULT_EPS);
        Graph<Integer, DefaultEdge> expected = new AsSubgraph<>(g, expectedVertices);
        assertEquals(expected, computed);
    }
}
