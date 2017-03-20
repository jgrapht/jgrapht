package org.jgrapht.alg.cycle;

import org.jgrapht.Graph;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.GraphWalk;

/**
 * A common interface for classes implementing algorithms for finding a cycle base of an undirected
 * graph.
 *
 * @param <V> the vertex type.
 * @param <E> the edge type.
 *
 * @author Nikita Zhuchkov
 * @author Stanislav Nikitin
 * @author Svetlana Blinova
 */

public abstract class Cycle<V, E> {

    protected Graph<V, E> graph;

    /**
     * Returns the graph on which the cycle base search algorithm is executed by this object.
     *
     * @return The graph.
     */
    abstract Graph<V, E> getGraph();

    /**
     * Sets the graph on which the cycle base search algorithm is executed by this object.
     *
     * @param graph the graph.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    abstract void setGraph(Graph<V, E> graph);


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
    abstract List<List<V>> findCycle();

    /**
     * * Finds a cycle base of the graph.<br>
     * Note that the full algorithm is executed on every call since the graph may have changed
     * between calls.
     *
     * @return A list of paths which are cycles. Based on <code> List<List<V>> findCycle() </code>.
     * @throws IllegalArgumentException
     */
    List<GraphPath<V, E>> findCyclePath() throws IllegalArgumentException {
        if (graph == null) {
            throw new IllegalArgumentException("Null graph.");
        }
        List<GraphPath<V, E>> gpl = new ArrayList<>();
        List<List<V>> list = this.findCycle();
        boolean isWeighted = this.graph instanceof WeightedGraph;

        for(List<V> lv: list){
            if(isWeighted){
                int weight = 0;
                for(int i=1; i<lv.size(); i++){
                    V v1 = lv.get(i-1);
                    V v2 = lv.get(i);
                    weight += this.graph.getEdgeWeight( this.graph.getEdge(v1,v2) );
                }
                weight += this.graph.getEdgeWeight( this.graph.getEdge(lv.get(lv.size()-1), lv.get(0)) );
                gpl.add(new GraphWalk<V, E>(this.graph, lv, weight));
            }
            else{
                gpl.add(new GraphWalk<V, E>(this.graph, lv, lv.size()+1));
            }
        }

        return gpl;
    }
}
