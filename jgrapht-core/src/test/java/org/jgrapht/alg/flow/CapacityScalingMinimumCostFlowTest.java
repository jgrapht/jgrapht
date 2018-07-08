package org.jgrapht.alg.flow;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.jgrapht.alg.flow.CapacityScalingMinimumCostFlow.EPS;
import static org.junit.Assert.assertEquals;

public class CapacityScalingMinimumCostFlowTest {

    @Test
    public void testGetMinimumCostFlow1() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge edge12 = Graphs.addEdgeWithVertices(graph, 1, 2, 5);
        CapacityScalingMinimumCostFlow<Integer, DefaultWeightedEdge> flow = new CapacityScalingMinimumCostFlow<>(graph);
        Map<Integer, Double> supplyMap = new HashMap<>();
        supplyMap.put(1, 3d);
        supplyMap.put(2, -3d);
        Map<DefaultWeightedEdge, Double> upperCapacityMap = new HashMap<>();
        upperCapacityMap.put(edge12, 4d);
        MinimumCostFlowAlgorithm.MinimumCostFLow<Integer, DefaultWeightedEdge> minimumCostFLow = flow.getMinimumCostFlow();
        double cost = flow.calculateMinimumCostFlow(supplyMap, upperCapacityMap);
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
        CapacityScalingMinimumCostFlow<Integer, DefaultWeightedEdge> flow = new CapacityScalingMinimumCostFlow<>(graph);
        Map<Integer, Double> supplyMap = new HashMap<>();
        supplyMap.put(1, 4d);
        supplyMap.put(4, -4d);
        Map<DefaultWeightedEdge, Double> upperCapacityMap = new HashMap<>();
        upperCapacityMap.put(edge12, 4d);
        upperCapacityMap.put(edge13, 1d);
        upperCapacityMap.put(edge23, 1d);
        upperCapacityMap.put(edge24, 5d);
        upperCapacityMap.put(edge34, 4d);
        double cost = flow.calculateMinimumCostFlow(supplyMap, upperCapacityMap);
        MinimumCostFlowAlgorithm.MinimumCostFLow<Integer, DefaultWeightedEdge> minimumCostFLow = flow.getMinimumCostFlow();
        assertEquals(26, cost, EPS);
        assertEquals(3, minimumCostFLow.getFlow(edge12), EPS);
        assertEquals(1, minimumCostFLow.getFlow(edge13), EPS);
        assertEquals(1, minimumCostFLow.getFlow(edge23), EPS);
        assertEquals(2, minimumCostFLow.getFlow(edge24),EPS);
        assertEquals(2, minimumCostFLow.getFlow(edge34), EPS);
        assertEquals(cost, minimumCostFLow.getCost(), EPS);
    }

}
