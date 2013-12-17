/* This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.generate;

import java.util.*;

import org.jgrapht.*;


/**
 * WeightedGraphGenerator defines an interface for generating graph structures
 * having edges weighted with real values.
 *
 * @author Alexey Kudinkin
 * @since Aug 1, 2013
 */
public abstract class WeightedGraphGeneratorAdapter<V, E, T>
    implements GraphGenerator<V, E, T>
{
    

    protected double [][] weights;

    

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void generateGraph(
        WeightedGraph<V, E> target,
        VertexFactory<V> vertexFactory,
        Map<String, T> resultMap);

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public WeightedGraphGeneratorAdapter<V, E, T> weights(double [][] weights)
    {
        this.weights = weights;
        return this;
    }

    @Override public void generateGraph(
        Graph<V, E> target,
        VertexFactory<V> vertexFactory,
        Map<String, T> resultMap)
    {
        generateGraph((WeightedGraph<V, E>) target, vertexFactory, resultMap);
    }
}

// End WeightedGraphGeneratorAdapter.java
