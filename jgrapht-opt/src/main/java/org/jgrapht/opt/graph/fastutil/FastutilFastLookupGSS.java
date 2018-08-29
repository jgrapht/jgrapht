package org.jgrapht.opt.graph.fastutil;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.GraphSpecificsStrategy;
import org.jgrapht.graph.IntrusiveEdgesSpecifics;
import org.jgrapht.graph.UniformIntrusiveEdgesSpecifics;
import org.jgrapht.graph.WeightedIntrusiveEdgesSpecifics;
import org.jgrapht.graph.specifics.FastLookupDirectedSpecifics;
import org.jgrapht.graph.specifics.FastLookupUndirectedSpecifics;
import org.jgrapht.graph.specifics.Specifics;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * The fast lookup specifics strategy implementation using fastutil maps for storage..
 * 
 * <p>
 * Graphs constructed using this strategy use additional data structures to improve the performance
 * of methods which depend on edge retrievals, e.g. getEdge(V u, V v), containsEdge(V u, V
 * v),addEdge(V u, V v). A disadvantage is an increase in memory consumption. If memory utilization
 * is an issue, use the {@link FastutilGSS} instead.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class FastutilFastLookupGSS<V, E>
    implements
    GraphSpecificsStrategy<V, E>
{
    private static final long serialVersionUID = -1335362823522091418L;

    @Override
    public BiFunction<Graph<V, E>, GraphType, Specifics<V, E>> getSpecificsFactory()
    {
        return (BiFunction<Graph<V, E>, GraphType,
            Specifics<V, E>> & Serializable) (graph, type) -> {
                if (type.isDirected()) {
                    return new FastLookupDirectedSpecifics<>(
                        graph, new Object2ObjectLinkedOpenHashMap<>(),
                        new Object2ObjectOpenHashMap<>(), getEdgeSetFactory());
                } else {
                    return new FastLookupUndirectedSpecifics<>(
                        graph, new Object2ObjectLinkedOpenHashMap<>(),
                        new Object2ObjectOpenHashMap<>(), getEdgeSetFactory());
                }
            };
    }

    @Override
    public Function<GraphType, IntrusiveEdgesSpecifics<V, E>> getIntrusiveEdgesSpecificsFactory()
    {
        return (Function<GraphType, IntrusiveEdgesSpecifics<V, E>> & Serializable) (type) -> {
            if (type.isWeighted()) {
                return new WeightedIntrusiveEdgesSpecifics<V, E>(
                    new Object2ObjectLinkedOpenHashMap<>());
            } else {
                return new UniformIntrusiveEdgesSpecifics<>(new Object2ObjectLinkedOpenHashMap<>());
            }
        };
    }

}
