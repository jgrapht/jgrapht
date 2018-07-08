package org.jgrapht.alg.interfaces;

import java.util.Map;

public interface MinimumCostFlowAlgorithm<V, E> {

    double calculateMinimumCostFlow(Map<V, Double> supplyMap, Map<E, Double> upperCapacityMap);

    double calculateMinimumCostFlow(Map<V, Double> supplyMap, Map<E, Double> upperCapacityMap, Map<E, Double> lowerCapacityMap);

    default double getCost() {
        return getMinimumCostFlow().getCost();
    }

    default V getFlowDirection(E edge) {
        throw new UnsupportedOperationException("Function not implemented");
    }

    MinimumCostFLow<V, E> getMinimumCostFlow();

    interface MinimumCostFLow<V, E> {
        double getCost();

        double getFlow(E edge);

        Map<E, Double> getFlow();
    }

    class MinimumCostFlowImpl<V, E> implements MinimumCostFLow<V, E> {
        double cost;
        private Map<E, Double> flowMap;

        public MinimumCostFlowImpl(double cost, Map<E, Double> flowMap) {
            this.cost = cost;
            this.flowMap = flowMap;
        }

        @Override
        public Map<E, Double> getFlow() {
            return flowMap;
        }

        @Override
        public double getCost() {
            return cost;
        }

        @Override
        public double getFlow(E edge) {
            return flowMap.get(edge);
        }


    }
}
