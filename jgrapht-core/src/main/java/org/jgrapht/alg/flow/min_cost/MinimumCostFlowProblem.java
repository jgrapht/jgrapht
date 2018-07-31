package org.jgrapht.alg.flow.min_cost;

import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;

public class MinimumCostFlowProblem<V, E> {
    Graph<V, E> graph;
    Map<V, Integer> supplyMap;
    Map<E, Integer> lowerCapacityMap;
    Map<E, Integer> upperCapacityMap;

    public MinimumCostFlowProblem(Graph<V, E> graph, Map<V, Integer> supplyMap, Map<E, Integer> upperCapacityMap, Map<E, Integer> lowerCapacityMap) {
        this.graph = graph;
        this.supplyMap = supplyMap;
        this.upperCapacityMap = upperCapacityMap;
        if (lowerCapacityMap == null) {
            this.lowerCapacityMap = new HashMap<>(0);
        } else {
            this.lowerCapacityMap = lowerCapacityMap;
        }
    }

    public MinimumCostFlowProblem(Graph<V, E> graph, Map<V, Integer> supplyMap, Map<E, Integer> upperCapacityMap) {
        this(graph, supplyMap, upperCapacityMap, null);
    }
}
