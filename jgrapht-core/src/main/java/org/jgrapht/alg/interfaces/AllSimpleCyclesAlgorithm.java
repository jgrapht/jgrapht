package org.jgrapht.alg.interfaces;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

import java.util.List;

/**
 * Created by svetl on 26.03.2017.
 */
public interface AllSimpleCyclesAlgorithm<V, E> {
    /**
     * Returns the graph on which the cycle base search algorithm is executed by this object.
     *
     * @return The graph.
     */
    Graph<V, E> getGraph();

    /**
     * Sets the graph on which the cycle base search algorithm is executed by this object.
     *
     * @param graph the graph.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    void setGraph(Graph<V, E> graph);

    /**
     * Finds a cycle base of the graph.<br>
     * Note that the full algorithm is executed on every call since the graph may have changed
     * between calls.
     *
     * @return A list of cycles constituting a cycle base for the graph. Possibly empty but never
     *         <code>null</code>.
     *
     * @throws IllegalArgumentException if the current graph is null.
     */
    List<List<V>> findSimpleCycles();

    /**
     * * Finds a cycle base of the graph.<br>
     * Note that the full algorithm is executed on every call since the graph may have changed
     * between calls.
     *
     * @return A list of paths which are cycles. Based on <code> List<List<V>> findCycles() </code>.
     * @throws IllegalArgumentException
     */
    List<GraphPath<V, E>> findCyclePath();
}
