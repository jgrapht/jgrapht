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
import org.jgrapht.graph.specifics.DirectedSpecifics;
import org.jgrapht.graph.specifics.Specifics;
import org.jgrapht.graph.specifics.UndirectedSpecifics;

import it.unimi.dsi.fastutil.ints.Int2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

/**
 * A specifics strategy implementation using fastutil maps for storage specialized for integer
 * vertices.
 * 
 * @author Dimitrios Michail
 *
 * @param <E> the graph edge type
 */
public class FastutilIntVertexGSS<E>
    implements
    GraphSpecificsStrategy<Integer, E>
{
    private static final long serialVersionUID = 803286406699705306L;

    @Override
    public BiFunction<Graph<Integer, E>, GraphType, Specifics<Integer, E>> getSpecificsFactory()
    {
        return (BiFunction<Graph<Integer, E>, GraphType,
            Specifics<Integer, E>> & Serializable) (graph, type) -> {
                if (type.isDirected()) {
                    return new DirectedSpecifics<>(
                        graph, new Int2ReferenceLinkedOpenHashMap<>(), getEdgeSetFactory());
                } else {
                    return new UndirectedSpecifics<>(
                        graph, new Int2ReferenceLinkedOpenHashMap<>(), getEdgeSetFactory());
                }
            };
    }

    @Override
    public Function<GraphType,
        IntrusiveEdgesSpecifics<Integer, E>> getIntrusiveEdgesSpecificsFactory()
    {
        return (Function<GraphType, IntrusiveEdgesSpecifics<Integer, E>> & Serializable) (type) -> {
            if (type.isWeighted()) {
                return new WeightedIntrusiveEdgesSpecifics<Integer, E>(
                    new Object2ObjectLinkedOpenHashMap<>());
            } else {
                return new UniformIntrusiveEdgesSpecifics<>(new Object2ObjectLinkedOpenHashMap<>());
            }
        };
    }

}
