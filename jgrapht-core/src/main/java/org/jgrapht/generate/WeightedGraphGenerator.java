package org.jgrapht.generate;

import org.jgrapht.EdgeFactory;

public abstract class WeightedGraphGenerator<V, E> implements GraphGenerator<V, E, V> {

    protected Class<? extends E> edgeClass;

    protected EdgeFactory<V, E> edgeFactory;

    protected double[][] weights;

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public WeightedGraphGenerator<V, E> edgeFactory(EdgeFactory<V, E> edgeFactory) {
        this.edgeFactory = edgeFactory;
        return this;
    }

    public WeightedGraphGenerator<V, E> edgeClass(Class<? extends E> edgeClass) {
        this.edgeClass = edgeClass;
        return this;
    }

    public WeightedGraphGenerator<V, E> weights(double[][] weights) {
        this.weights = weights;
        return this;
    }

}
