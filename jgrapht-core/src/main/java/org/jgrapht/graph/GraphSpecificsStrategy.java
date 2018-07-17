package org.jgrapht.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.specifics.ArrayUnenforcedSetEdgeSetFactory;
import org.jgrapht.graph.specifics.Specifics;

/**
 * A graph specifics construction factory.
 * 
 * <p>Such a strategy can be used to adjust the internals of the default graph implementations. 
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see FastLookupGraphSpecificsStrategy
 * @see DefaultGraphSpecificsStrategy
 */
public interface GraphSpecificsStrategy<V, E> extends Serializable
{
    /**
     * Get a function which creates the intrusive edges specifics. The factory will accept the graph
     * type as a parameter.
     * 
     * @return a function which creates intrusive edges specifics.
     */
    default Function<GraphType, IntrusiveEdgesSpecifics<V, E>> getIntrusiveEdgesSpecificsFactory() { 
        return (Function<GraphType, IntrusiveEdgesSpecifics<V, E>> & Serializable) (type) -> {
            if (type.isWeighted()) {
                return new WeightedIntrusiveEdgesSpecifics<V, E>(
                    this.<E, IntrusiveWeightedEdge> getPredictableOrderMapFactory().get());
            } else {
                return new UniformIntrusiveEdgesSpecifics<>(
                    this.<E, IntrusiveEdge> getPredictableOrderMapFactory().get());
            }
        };
    }
    
    /**
     * Get a function which creates the specifics. The factory will accept the graph type as a
     * parameter.
     * 
     * @return a function which creates intrusive edges specifics.
     */
    BiFunction<Graph<V, E>, GraphType, Specifics<V, E>> getSpecificsFactory();

    /**
     * Get a supplier of maps with a predictable iteration order.
     * 
     * @return a supplier of maps with a predictable iteration order.
     * @param <K1> the key type
     * @param <V1> the value type 
     */
    default <K1, V1> Supplier<Map<K1, V1>> getPredictableOrderMapFactory()
    {
        return (Supplier<Map<K1, V1>> & Serializable)() -> new LinkedHashMap<>();
    }

    /**
     * Get a supplier of maps.
     * 
     * @return a supplier of maps.
     * @param <K1> the key type
     * @param <V1> the value type 
     */
    default <K1, V1> Supplier<Map<K1, V1>> getMapFactory()
    {
        return (Supplier<Map<K1, V1>> & Serializable)() -> new HashMap<>();
    }

    /**
     * Get an edge set factory.
     * 
     * @return an edge set factory
     */
    default EdgeSetFactory<V, E> getEdgeSetFactory()
    {
        return new ArrayUnenforcedSetEdgeSetFactory<>();
    }

}
