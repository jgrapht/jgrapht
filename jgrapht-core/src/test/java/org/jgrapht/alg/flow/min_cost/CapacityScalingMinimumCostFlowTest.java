package org.jgrapht.alg.flow.min_cost;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)

public class CapacityScalingMinimumCostFlowTest {

    private int scalingFactor;
    private final double EPS = 1e-9;

    @Parameterized.Parameters
    public static Object[] params(){
        return new Integer[]{1, 2, 3, 4, 5};
    }

    public CapacityScalingMinimumCostFlowTest(int scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    @Test
    public void testGetMinimumCostFlow1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge edge12 = Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        Map<Integer, Integer> supplyMap = new HashMap<>();
        supplyMap.put(1, 3);
        supplyMap.put(2, -3);
        Map<DefaultWeightedEdge, Integer> upperCapacityMap = new HashMap<>();
        upperCapacityMap.put(edge12, 4);
        CapacityScalingMinimumCostFlow<Integer, DefaultWeightedEdge> flow =
                new CapacityScalingMinimumCostFlow<>(new MinimumCostFlowProblem<>(graph, supplyMap, upperCapacityMap), scalingFactor);
        MinimumCostFlowAlgorithm.MinimumCostFLow<Integer, DefaultWeightedEdge> minimumCostFLow = flow.getMinimumCostFlow();
        double cost = flow.calculateMinimumCostFlow();
        assertEquals(15, cost, EPS);
        assertEquals(3, minimumCostFLow.getFlow(edge12), EPS);
        assertEquals(cost, minimumCostFLow.getCost(), EPS);
        assertEquals(2, (int) flow.getFlowDirection(edge12));
    }

    @Test
    public void testGetMinimumCostFlow2() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge edge12 = Graphs.addEdgeWithVertices(graph, 1, 2, 2);
        DefaultWeightedEdge edge13 = Graphs.addEdgeWithVertices(graph, 1, 3, 3);
        DefaultWeightedEdge edge23 = Graphs.addEdgeWithVertices(graph, 2, 3, 1);
        DefaultWeightedEdge edge24 = Graphs.addEdgeWithVertices(graph, 2, 4, 6);
        DefaultWeightedEdge edge34 = Graphs.addEdgeWithVertices(graph, 3, 4, 2);
        Map<Integer, Integer> supplyMap = new HashMap<>();
        supplyMap.put(1, 4);
        supplyMap.put(4, -4);
        Map<DefaultWeightedEdge, Integer> upperCapacityMap = new HashMap<>();
        upperCapacityMap.put(edge12, 4);
        upperCapacityMap.put(edge13, 1);
        upperCapacityMap.put(edge23, 1);
        upperCapacityMap.put(edge24, 5);
        upperCapacityMap.put(edge34, 4);
        CapacityScalingMinimumCostFlow<Integer, DefaultWeightedEdge> flow =
                new CapacityScalingMinimumCostFlow<>(new MinimumCostFlowProblem<>(graph, supplyMap, upperCapacityMap),scalingFactor);
        double cost = flow.calculateMinimumCostFlow();
        MinimumCostFlowAlgorithm.MinimumCostFLow<Integer, DefaultWeightedEdge> minimumCostFLow = flow.getMinimumCostFlow();
        assertEquals(26, cost, EPS);
        assertEquals(3, minimumCostFLow.getFlow(edge12), EPS);
        assertEquals(1, minimumCostFLow.getFlow(edge13), EPS);
        assertEquals(1, minimumCostFLow.getFlow(edge23), EPS);
        assertEquals(2, minimumCostFLow.getFlow(edge24), EPS);
        assertEquals(2, minimumCostFLow.getFlow(edge34), EPS);
        assertEquals(cost, minimumCostFLow.getCost(), EPS);
    }

    @Test
    public void testGetMinimumCostFlow3() {
        scalingFactor = 2;
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge edge15 = Graphs.addEdgeWithVertices(graph, 1, 5, 6);
        DefaultWeightedEdge edge36 = Graphs.addEdgeWithVertices(graph, 3, 6, 9);
        DefaultWeightedEdge edge31 = Graphs.addEdgeWithVertices(graph, 3, 1, 6);
        DefaultWeightedEdge edge53 = Graphs.addEdgeWithVertices(graph, 5, 3, 4);
        DefaultWeightedEdge edge56 = Graphs.addEdgeWithVertices(graph, 5, 6, 4);
        DefaultWeightedEdge edge24 = Graphs.addEdgeWithVertices(graph, 2, 4, 10);
        DefaultWeightedEdge edge23 = Graphs.addEdgeWithVertices(graph, 2, 3, 3);
        DefaultWeightedEdge edge46 = Graphs.addEdgeWithVertices(graph, 4, 6, 10);
        DefaultWeightedEdge edge41 = Graphs.addEdgeWithVertices(graph, 4, 1, 3);
        DefaultWeightedEdge edge43 = Graphs.addEdgeWithVertices(graph, 4, 3, 8);
        Map<Integer, Integer> supplyMap = new HashMap<>();
        supplyMap.put(1, 2);
        supplyMap.put(2, 5);
        supplyMap.put(6, -7);
        Map<DefaultWeightedEdge, Integer> upperCapacityMap = new HashMap<>();
        upperCapacityMap.put(edge15, 3);
        upperCapacityMap.put(edge36, 3);
        upperCapacityMap.put(edge31, 3);
        upperCapacityMap.put(edge53, 3);
        upperCapacityMap.put(edge56, 7);
        upperCapacityMap.put(edge24, 5);
        upperCapacityMap.put(edge23, 1);
        upperCapacityMap.put(edge46, 5);
        upperCapacityMap.put(edge41, 5);
        upperCapacityMap.put(edge43, 1);
        CapacityScalingMinimumCostFlow<Integer, DefaultWeightedEdge> flow =
                new CapacityScalingMinimumCostFlow<>(new MinimumCostFlowProblem<>(graph, supplyMap, upperCapacityMap), scalingFactor);
        double cost = flow.calculateMinimumCostFlow();
        MinimumCostFlowAlgorithm.MinimumCostFLow<Integer, DefaultWeightedEdge> minimumCostFLow = flow.getMinimumCostFlow();
        assertEquals(112, cost, EPS);
        assertEquals(cost, minimumCostFLow.getCost(), EPS);
    }
}
