package org.jgrapht.alg.interfaces;

import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DirectedSubgraph;



/**
 * Allows to create a StrongConnectivityAlgorithm to find cycles in a directed graph
 *
 * @param <V>   vertex concept type
 * @param <E>   edge concept type
 */
public interface StrongConnectivityAlgorithm<V,E> {
	
	/**
     * Returns the graph inspected by the StrongConnectivityAlgorithm.
     *
     * @return the graph inspected by this StrongConnectivityAlgorithm
     */
    public DirectedGraph<V, E> getGraph();

    /**
     * Returns true if the graph of this <code>
     * StronglyConnectivityAlgorithmr</code> instance is strongly connected.
     *
     * @return true if the graph is strongly connected, false otherwise
     */
    public boolean isStronglyConnected();

    /**
     * Computes a {@link List} of {@link Set}s, where each set contains vertices
     * which together form a strongly connected component within the given
     * graph.
     *
     * @return <code>List</code> of <code>Set</code> s containing the strongly
     * connected components
     */
    public List<Set<V>> stronglyConnectedSets();

    /**
     * <p>Computes a list of {@link DirectedSubgraph}s of the given graph. Each
     * subgraph will represent a strongly connected component and will contain
     * all vertices of that component. The subgraph will have an edge (u,v) iff
     * u and v are contained in the strongly connected component.</p>
     *
     *
     * @return a list of subgraphs representing the strongly connected
     * components
     */
    public List<DirectedSubgraph<V, E>> stronglyConnectedSubgraphs();

   

}
