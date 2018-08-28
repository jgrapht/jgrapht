package org.jgrapht.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.specifics.FastLookupDirectedSpecifics;
import org.jgrapht.graph.specifics.FastLookupUndirectedSpecifics;
import org.jgrapht.graph.specifics.Specifics;

/**
 * The fast lookup specifics strategy implementation.
 * 
 * <p>
 * Graphs constructed using this strategy use additional data structures to improve the performance
 * of methods which depend on edge retrievals, e.g. getEdge(V u, V v), containsEdge(V u, V
 * v),addEdge(V u, V v). A disadvantage is an increase in memory consumption. If memory utilization
 * is an issue, use the {@link DefaultGraphSpecificsStrategy} instead.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class FastLookupGraphSpecificsStrategy<V, E>
    implements GraphSpecificsStrategy<V, E>
{
    private static final long serialVersionUID = -5490869870275054280L;

    @Override
    public Function<GraphType, IntrusiveEdgesSpecifics<V, E>> getIntrusiveEdgesSpecificsFactory() { 
        return (Function<GraphType, IntrusiveEdgesSpecifics<V, E>> & Serializable) (type) -> {
            if (type.isWeighted()) {
                return new WeightedIntrusiveEdgesSpecifics<V, E>(new LinkedHashMap<>());
            } else {
                return new UniformIntrusiveEdgesSpecifics<>(new LinkedHashMap<>());
            }
        };
    }
    
    @Override
    public BiFunction<Graph<V, E>, GraphType, Specifics<V, E>> getSpecificsFactory()
    {
        return (BiFunction<Graph<V, E>, GraphType,
            Specifics<V, E>> & Serializable) (graph, type) -> {
                if (type.isDirected()) {
                    return new FastLookupDirectedSpecifics<>(
                        graph, new LinkedHashMap<>(), new HashMap<>(), getEdgeSetFactory());
                } else {
                    return new FastLookupUndirectedSpecifics<>(
                        graph, new LinkedHashMap<>(), new HashMap<>(), getEdgeSetFactory());
                }
            };
    }

}
