package org.jgrapht.graph;

import java.io.Serializable;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.graph.specifics.ArrayUnenforcedSetEdgeSetFactory;
import org.jgrapht.graph.specifics.Specifics;

/**
 * A graph specifics construction factory.
 * 
 * <p>
 * Such a strategy can be used to adjust the internals of the default graph implementations.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @see FastLookupGraphSpecificsStrategy
 * @see DefaultGraphSpecificsStrategy
 */
public interface GraphSpecificsStrategy<V, E>
    extends
    Serializable
{
    /**
     * Get a function which creates the intrusive edges specifics. The factory will accept the graph
     * type as a parameter.
     * 
     * <p>
     * Note that it is very important to use a map implementation which respects iteration order.
     * 
     * @return a function which creates intrusive edges specifics.
     */
    Function<GraphType, IntrusiveEdgesSpecifics<V, E>> getIntrusiveEdgesSpecificsFactory();

    /**
     * Get a function which creates the specifics. The factory will accept the graph type as a
     * parameter.
     * 
     * @return a function which creates intrusive edges specifics.
     */
    BiFunction<Graph<V, E>, GraphType, Specifics<V, E>> getSpecificsFactory();

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
