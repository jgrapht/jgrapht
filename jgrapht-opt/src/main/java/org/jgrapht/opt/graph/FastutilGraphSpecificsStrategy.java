package org.jgrapht.opt.graph;

import java.io.Serializable;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.GraphSpecificsStrategy;
import org.jgrapht.graph.specifics.DirectedEdgeContainer;
import org.jgrapht.graph.specifics.DirectedSpecifics;
import org.jgrapht.graph.specifics.Specifics;
import org.jgrapht.graph.specifics.UndirectedEdgeContainer;
import org.jgrapht.graph.specifics.UndirectedSpecifics;

import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

/**
 * A specifics strategy implementation using fastutil maps for storage.
 * 
 * <p>
 * Graphs constructed using this strategy require the least amount of memory, at the expense of slow
 * edge retrievals. Methods which depend on edge retrievals, e.g. getEdge(V u, V v), containsEdge(V
 * u, V v), addEdge(V u, V v), etc may be relatively slow when the average degree of a vertex is
 * high (dense graphs). For a fast implementation, use
 * {@link FastutilFastLookupGraphSpecificsStrategy}.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class FastutilGraphSpecificsStrategy<V, E>
    implements
    GraphSpecificsStrategy<V, E>
{
    private static final long serialVersionUID = -4319431062943632549L;

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
                    return new DirectedSpecifics<>(
                        graph,
                        this.<V, DirectedEdgeContainer<V, E>> getPredictableOrderMapFactory().get(),
                        getEdgeSetFactory());
                } else {
                    return new UndirectedSpecifics<>(
                        graph,
                        this
                            .<V, UndirectedEdgeContainer<V, E>> getPredictableOrderMapFactory()
                            .get(),
                        getEdgeSetFactory());
                }
            };
    }

    @Override
    public <K1, V1> Supplier<Map<K1, V1>> getPredictableOrderMapFactory()
    {
        return (Supplier<
            Map<K1, V1>> & Serializable) () -> new Object2ReferenceLinkedOpenHashMap<>();
    }

    @Override
    public <K1, V1> Supplier<Map<K1, V1>> getMapFactory()
    {
        return (Supplier<Map<K1, V1>> & Serializable) () -> new Object2ReferenceOpenHashMap<>();
    }

}
