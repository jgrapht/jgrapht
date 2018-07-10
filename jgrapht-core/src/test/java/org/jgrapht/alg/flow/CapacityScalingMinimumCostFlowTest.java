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

    @Test
    public void testGetMinimumCostFlow3(){
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        CapacityScalingMinimumCostFlow<Integer, DefaultWeightedEdge> flow = new CapacityScalingMinimumCostFlow<>(graph);
        DefaultWeightedEdge edge15 = Graphs.addEdgeWithVertices(graph, 1,5,6);
        DefaultWeightedEdge edge36 = Graphs.addEdgeWithVertices(graph, 3,6,9);
        DefaultWeightedEdge edge31 = Graphs.addEdgeWithVertices(graph, 3,1,6);
        DefaultWeightedEdge edge53 = Graphs.addEdgeWithVertices(graph, 5,3,4);
        DefaultWeightedEdge edge56 = Graphs.addEdgeWithVertices(graph, 5,6,4);
        DefaultWeightedEdge edge24 = Graphs.addEdgeWithVertices(graph, 2,4,10);
        DefaultWeightedEdge edge23 = Graphs.addEdgeWithVertices(graph, 2,3,3);
        DefaultWeightedEdge edge46 = Graphs.addEdgeWithVertices(graph, 4,6,10);
        DefaultWeightedEdge edge41 = Graphs.addEdgeWithVertices(graph, 4,1,3);
        DefaultWeightedEdge edge43 = Graphs.addEdgeWithVertices(graph, 4,3,8);
        Map<Integer, Double> supplyMap = new HashMap<>();
        supplyMap.put(1, 2d);
        supplyMap.put(2, 5d);
        supplyMap.put(6, -7d);
        Map<DefaultWeightedEdge, Double> upperCapacityMap = new HashMap<>();
        upperCapacityMap.put(edge15, 3d);
        upperCapacityMap.put(edge36, 3d);
        upperCapacityMap.put(edge31, 3d);
        upperCapacityMap.put(edge53, 3d);
        upperCapacityMap.put(edge56, 7d);
        upperCapacityMap.put(edge24, 5d);
        upperCapacityMap.put(edge23, 1d);
        upperCapacityMap.put(edge46, 5d);
        upperCapacityMap.put(edge41, 5d);
        upperCapacityMap.put(edge43, 1d);
        double cost = flow.calculateMinimumCostFlow(supplyMap, upperCapacityMap);
        MinimumCostFlowAlgorithm.MinimumCostFLow<Integer, DefaultWeightedEdge> minimumCostFLow = flow.getMinimumCostFlow();
        assertEquals(112, cost, EPS);
        assertEquals(cost, minimumCostFLow.getCost(), EPS);
    }
}
