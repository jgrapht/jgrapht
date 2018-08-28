package org.jgrapht.opt.graph.fastutil;

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

import it.unimi.dsi.fastutil.ints.Int2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * A specifics strategy implementation using fastutil maps for storage specialized for 
 * integer vertices.
 * 
 * @author Dimitrios Michail
 *
 * @param <E> the graph edge type
 */
public class FastutilIntegerAnyGraphSpecificsStrategy<E>
    implements
    GraphSpecificsStrategy<Integer, E>
{
    private static final long serialVersionUID = 803286406699705306L;

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
                    return new DirectedSpecifics<>(
                        graph,
                        new Int2ReferenceLinkedOpenHashMap<DirectedEdgeContainer<Integer, E>>(),
                        getEdgeSetFactory());
                } else {
                    return new UndirectedSpecifics<>(
                        graph,
                        new Int2ReferenceLinkedOpenHashMap<UndirectedEdgeContainer<Integer, E>>(),
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
