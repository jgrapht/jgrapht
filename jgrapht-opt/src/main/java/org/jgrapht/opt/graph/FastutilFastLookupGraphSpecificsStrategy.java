package org.jgrapht.opt.graph;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.GraphSpecificsStrategy;
import org.jgrapht.graph.specifics.DirectedEdgeContainer;
import org.jgrapht.graph.specifics.FastLookupDirectedSpecifics;
import org.jgrapht.graph.specifics.FastLookupUndirectedSpecifics;
import org.jgrapht.graph.specifics.Specifics;
import org.jgrapht.graph.specifics.UndirectedEdgeContainer;

import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

/**
 * The fast lookup specifics strategy implementation using fastutil maps for storage..
 * 
 * <p>
 * Graphs constructed using this strategy use additional data structures to improve the performance
 * of methods which depend on edge retrievals, e.g. getEdge(V u, V v), containsEdge(V u, V
 * v),addEdge(V u, V v). A disadvantage is an increase in memory consumption. If memory utilization
 * is an issue, use the {@link FastutilGraphSpecificsStrategy} instead.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class FastutilFastLookupGraphSpecificsStrategy<V, E>
    implements GraphSpecificsStrategy<V, E>
{
    private static final long serialVersionUID = -1335362823522091418L;

    /**
     * Get a function which creates the specifics. The factory will accept the graph type as a
     * parameter.
     * 
     * @return a function which creates intrusive edges specifics.
     */
    @Override
    public BiFunction<Graph<V, E>, GraphType, Specifics<V, E>> getSpecificsFactory()
    {
        return (BiFunction<Graph<V, E>, GraphType,
            Specifics<V, E>> & Serializable) (graph, type) -> {
                if (type.isDirected()) {
                    return new FastLookupDirectedSpecifics<>(graph, this
                        .<V, DirectedEdgeContainer<V, E>> getPredictableOrderMapFactory()
                        .get(), this.<Pair<V, V>, Set<E>> getMapFactory().get(),
                        getEdgeSetFactory());
                } else {
                    return new FastLookupUndirectedSpecifics<>(graph, this
                        .<V, UndirectedEdgeContainer<V, E>> getPredictableOrderMapFactory()
                        .get(), this.<Pair<V, V>, Set<E>> getMapFactory().get(),
                        getEdgeSetFactory());
                }
            };
    }

    @Override
    public <K1, V1> Supplier<Map<K1, V1>> getPredictableOrderMapFactory()
    {
        return (Supplier<Map<K1, V1>> & Serializable)() -> new Object2ReferenceLinkedOpenHashMap<>();
    }

    @Override
    public <K1, V1> Supplier<Map<K1, V1>> getMapFactory()
    {
        return (Supplier<Map<K1, V1>> & Serializable)() -> new Object2ReferenceOpenHashMap<>();
    }

}

