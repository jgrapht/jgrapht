package org.jgrapht.alg.interfaces;

import java.util.Map;

public interface MinimumCostFlowAlgorithm<V, E> {

    double calculateMinimumCostFlow();

    default double getCost() {
        return getMinimumCostFlow().getCost();
    }

    default V getFlowDirection(E edge) {
        throw new UnsupportedOperationException("Function not implemented");
    }

    MinimumCostFLow<V, E> getMinimumCostFlow();

    interface MinimumCostFLow<V, E> {
        double getCost();

        int getFlow(E edge);

        Map<E, Integer> getFlow();
    }

    class MinimumCostFlowImpl<V, E> implements MinimumCostFLow<V, E> {
        double cost;
        private Map<E, Integer> flowMap;

        public MinimumCostFlowImpl(double cost, Map<E, Integer> flowMap) {
            this.cost = cost;
            this.flowMap = flowMap;
        }

        @Override
        public Map<E, Integer> getFlow() {
            return flowMap;
        }

        @Override
        public double getCost() {
            return cost;
        }

        @Override
        public int getFlow(E edge) {
            return flowMap.get(edge);
        }


    }
}
