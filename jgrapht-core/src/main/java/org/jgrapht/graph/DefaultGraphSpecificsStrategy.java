package org.jgrapht.graph;

import java.io.Serializable;
import java.util.function.BiFunction;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.specifics.DirectedEdgeContainer;
import org.jgrapht.graph.specifics.DirectedSpecifics;
import org.jgrapht.graph.specifics.Specifics;
import org.jgrapht.graph.specifics.UndirectedEdgeContainer;
import org.jgrapht.graph.specifics.UndirectedSpecifics;

/**
 * A default lookup specifics strategy implementation.
 * 
 * <p>
 * Graphs constructed using this strategy require the least amount of memory, at the expense of slow
 * edge retrievals. Methods which depend on edge retrievals, e.g. getEdge(V u, V v), containsEdge(V
 * u, V v), addEdge(V u, V v), etc may be relatively slow when the average degree of a vertex is
 * high (dense graphs). For a fast implementation, use {@link FastLookupGraphSpecificsStrategy}.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class DefaultGraphSpecificsStrategy<V, E>
    implements GraphSpecificsStrategy<V, E>
{
    private static final long serialVersionUID = 7615319421753562075L;

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
                    return new DirectedSpecifics<>(graph, this
                        .<V, DirectedEdgeContainer<V, E>> getPredictableOrderMapFactory()
                        .get(), getEdgeSetFactory());
                } else {
                    return new UndirectedSpecifics<>(graph, this
                        .<V, UndirectedEdgeContainer<V, E>> getPredictableOrderMapFactory()
                        .get(), getEdgeSetFactory());
                }
            };
    }

}
