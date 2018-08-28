package org.jgrapht.opt.graph.fastutil;

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
import org.jgrapht.graph.specifics.UndirectedEdgeContainer;
import org.jgrapht.graph.specifics.Specifics;

import it.unimi.dsi.fastutil.ints.Int2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * A specifics strategy implementation using fastutil maps for storage specialized for 
 * integer vertices.
 * 
 * <p>
 * Graphs constructed using this strategy use additional data structures to improve the performance
 * of methods which depend on edge retrievals, e.g. getEdge(V u, V v), containsEdge(V u, V
 * v),addEdge(V u, V v). A disadvantage is an increase in memory consumption. If memory utilization
 * is an issue, use the {@link FastutilIntegerAnyGraphSpecificsStrategy} instead.
 * 
 * @author Dimitrios Michail
 *
 * @param <E> the graph edge type
 */
public class FastutilFastLookupIntegerAnyGraphSpecificsStrategy<E>
    implements
    GraphSpecificsStrategy<Integer, E>
{
    private static final long serialVersionUID = 6098261533235930603L;
    
    /**
     * Get a function which creates the specifics. The factory will accept the graph type as a
     * parameter.
     * 
     * @return a function which creates intrusive edges specifics.
     */
    @Override
    public BiFunction<Graph<Integer, E>, GraphType, Specifics<Integer, E>> getSpecificsFactory()
    {
        return (BiFunction<Graph<Integer, E>, GraphType,
            Specifics<Integer, E>> & Serializable) (graph, type) -> {
                if (type.isDirected()) {
                    return new FastLookupDirectedSpecifics<>(
                        graph,
                        new Int2ReferenceLinkedOpenHashMap<DirectedEdgeContainer<Integer, E>>(),
                        this.<Pair<Integer, Integer>, Set<E>> getMapFactory().get(),
                        getEdgeSetFactory());
                } else {
                    return new FastLookupUndirectedSpecifics<>(
                        graph,
                        new Int2ReferenceLinkedOpenHashMap<UndirectedEdgeContainer<Integer, E>>(),
                        this.<Pair<Integer, Integer>, Set<E>> getMapFactory().get(),
                        getEdgeSetFactory());
                }
            };
    }

    @Override
    public <K1, V1> Supplier<Map<K1, V1>> getPredictableOrderMapFactory()
    {
        return (Supplier<
            Map<K1, V1>> & Serializable) () -> new Object2ObjectLinkedOpenHashMap<>();
    }

    @Override
    public <K1, V1> Supplier<Map<K1, V1>> getMapFactory()
    {
        return (Supplier<Map<K1, V1>> & Serializable) () -> new Object2ObjectOpenHashMap<>();
    }

}
