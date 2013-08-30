package org.jgrapht.generate;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.WeightedGraph;

import java.util.Map;

/**
 * WeightedGraphGenerator defines an interface for generating graph structures having edges weighted with
 * real values.
 *
 * @author Alexey Kudinkin
 * @since Aug 1, 2013
 */
public abstract class WeightedGraphGeneratorAdapter<V, E, T> implements GraphGenerator<V, E, T> {

    protected double[][] weights;

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void generateGraph(WeightedGraph<V, E> target, VertexFactory<V> vertexFactory, Map<String, T> resultMap);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public WeightedGraphGeneratorAdapter<V, E, T> weights(double[][] weights) {
        this.weights = weights;
        return this;
    }

    @Override
    public void generateGraph(Graph<V, E> target, VertexFactory<V> vertexFactory, Map<String, T> resultMap) {
      generateGraph((WeightedGraph<V, E>) target, vertexFactory, resultMap);
    }

}
