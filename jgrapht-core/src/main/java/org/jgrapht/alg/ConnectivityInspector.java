/*
 * (C) Copyright 2003-2016, by Barak Naveh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.event.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

/**
 * Allows obtaining various connectivity aspects of a graph. The <i>inspected graph</i> is specified
 * at construction time and cannot be modified. Currently, the inspector supports connected
 * components for an undirected graph and weakly connected components for a directed graph. To find
 * strongly connected components, use {@link KosarajuStrongConnectivityInspector} instead.
 *
 * <p>
 * The inspector methods work in a lazy fashion: no computation is performed unless immediately
 * necessary. Computation are done once and results and cached within this class for future need.
 * </p>
 *
 * <p>
 * The inspector is also a {@link org.jgrapht.event.GraphListener}. If added as a listener to the
 * inspected graph, the inspector will amend internal cached results instead of recomputing them. It
 * is efficient when a few modifications are applied to a large graph. If many modifications are
 * expected it will not be efficient due to added overhead on graph update operations. If inspector
 * is added as listener to a graph other than the one it inspects, results are undefined.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Barak Naveh
 * @author John V. Sichi
 * @since Aug 6, 2003
 */
public class ConnectivityInspector<V, E>
    implements GraphListener<V, E>
{
    List<Set<V>> connectedSets;
    Map<V, Set<V>> vertexToConnectedSet;
    private Graph<V, E> graph;
    private Graph<V, E> originalgraph;

    /**
     * Creates a connectivity inspector for the specified undirected graph.
     *
     * @param g the graph for which a connectivity inspector to be created.
     */
    public ConnectivityInspector(UndirectedGraph<V, E> g)
    {
        init();
        this.graph = g;
        this.originalgraph = g;
    }

    /**
     * Creates a connectivity inspector for the specified directed graph.
     *
     * @param g the graph for which a connectivity inspector to be created.
     */
    public ConnectivityInspector(DirectedGraph<V, E> g)
    {
        init();
        this.graph = new AsUndirectedGraph<>(g);
    }

    /**
     * Test if the inspected graph is connected. An empty graph is <i>not</i> considered connected.
     *
     * @return <code>true</code> if and only if inspected graph is connected.
     */
    public boolean isGraphConnected()
    {
        return lazyFindConnectedSets().size() == 1;
    }

    /**
     * Returns a set of all vertices that are in the maximally connected component together with the
     * specified vertex. For more on maximally connected component, see
     * <a href="http://www.nist.gov/dads/HTML/maximallyConnectedComponent.html">
     * http://www.nist.gov/dads/HTML/maximallyConnectedComponent.html</a>.
     *
     * @param vertex the vertex for which the connected set to be returned.
     *
     * @return a set of all vertices that are in the maximally connected component together with the
     *         specified vertex.
     */
    public Set<V> connectedSetOf(V vertex)
    {
        Set<V> connectedSet = vertexToConnectedSet.get(vertex);

        if (connectedSet == null) {
            connectedSet = new HashSet<>();

            BreadthFirstIterator<V, E> i = new BreadthFirstIterator<>(graph, vertex);

            while (i.hasNext()) {
                connectedSet.add(i.next());
            }

            vertexToConnectedSet.put(vertex, connectedSet);
        }

        return connectedSet;
    }

    /**
     * Returns a list of <code>Set</code> s, where each set contains all vertices that are in the
     * same maximally connected component. All graph vertices occur in exactly one set. For more on
     * maximally connected component, see
     * <a href="http://www.nist.gov/dads/HTML/maximallyConnectedComponent.html">
     * http://www.nist.gov/dads/HTML/maximallyConnectedComponent.html</a>.
     *
     * @return Returns a list of <code>Set</code> s, where each set contains all vertices that are
     *         in the same maximally connected component.
     */
    public List<Set<V>> connectedSets()
    {
        return lazyFindConnectedSets();
    }

    /**
     * @see GraphListener#edgeAdded(GraphEdgeChangeEvent)
     */
    @Override
    public void edgeAdded(GraphEdgeChangeEvent<V, E> e)
    {
        init(); // for now invalidate cached results, in the future need to
                // amend them.
    }

    /**
     * @see GraphListener#edgeRemoved(GraphEdgeChangeEvent)
     */
    @Override
    public void edgeRemoved(GraphEdgeChangeEvent<V, E> e)
    {
        init(); // for now invalidate cached results, in the future need to
                // amend them.
    }

    /**
     * Tests if there is a path from the specified source vertex to the specified target vertices.
     * For a directed graph, direction is ignored for this interpretation of path.
     *
     * <p>
     * Note: Future versions of this method might not ignore edge directions for directed graphs.
     * </p>
     *
     * @param sourceVertex one end of the path.
     * @param targetVertex another end of the path.
     *
     * @return <code>true</code> if and only if there is a path from the source vertex to the target
     *         vertex.
     */
    public boolean pathExists(V sourceVertex, V targetVertex)
    {
        /*
         * TODO: Ignoring edge direction for directed graph may be confusing. For directed graphs,
         * consider Dijkstra's algorithm.
         */
        Set<V> sourceSet = connectedSetOf(sourceVertex);

        return sourceSet.contains(targetVertex);
    }

    /**
     * @see VertexSetListener#vertexAdded(GraphVertexChangeEvent)
     */
    @Override
    public void vertexAdded(GraphVertexChangeEvent<V> e)
    {
        init(); // for now invalidate cached results, in the future need to
                // amend them.
    }

    /**
     * @see VertexSetListener#vertexRemoved(GraphVertexChangeEvent)
     */
    @Override
    public void vertexRemoved(GraphVertexChangeEvent<V> e)
    {
        init(); // for now invalidate cached results, in the future need to
                // amend them.
    }

    private void init()
    {
        connectedSets = null;
        vertexToConnectedSet = new HashMap<>();
    }

    private List<Set<V>> lazyFindConnectedSets()
    {
        if (connectedSets == null) {
            connectedSets = new ArrayList<>();

            Set<V> vertexSet = graph.vertexSet();

            if (vertexSet.size() > 0) {
                BreadthFirstIterator<V, E> i = new BreadthFirstIterator<>(graph, null);
                i.addTraversalListener(new MyTraversalListener());

                while (i.hasNext()) {
                    i.next();
                }
            }
        }

        return connectedSets;
    }

    /**
     * A traversal listener that groups all vertices according to to their containing connected set.
     *
     * @author Barak Naveh
     * @since Aug 6, 2003
     */
    private class MyTraversalListener
        extends TraversalListenerAdapter<V, E>
    {
        private Set<V> currentConnectedSet;

        /**
         * @see TraversalListenerAdapter#connectedComponentFinished(ConnectedComponentTraversalEvent)
         */
        @Override
        public void connectedComponentFinished(ConnectedComponentTraversalEvent e)
        {
            connectedSets.add(currentConnectedSet);
        }

        /**
         * @see TraversalListenerAdapter#connectedComponentStarted(ConnectedComponentTraversalEvent)
         */
        @Override
        public void connectedComponentStarted(ConnectedComponentTraversalEvent e)
        {
            currentConnectedSet = new HashSet<>();
        }

        /**
         * @see TraversalListenerAdapter#vertexTraversed(VertexTraversalEvent)
         */
        @Override
        public void vertexTraversed(VertexTraversalEvent<V> e)
        {
            V v = e.getVertex();
            currentConnectedSet.add(v);
            vertexToConnectedSet.put(v, currentConnectedSet);
        }
    }


    /**
     * A complete graph is a graph where
     * every vertex shares an edge with every other vertex. If it is a directed
     * graph, then edges must always exist in both directions.
     * 
     * This method is equivalent to incompleteVertices().isEmpty() 
     * 
     * @return true if the graph is complete. 
     */
	public boolean isComplete() {
		return this.incompleteVertices().isEmpty();
	}

	/**
	 *  Calculates the set of vertices that makes a graph incomplete.
	 *  A complete graph is a graph where
     * every vertex shares an edge with every other vertex. If it is a directed
     * graph, then edges must always exist in both directions.
     * 
	 * @return A set with vertices that have less edges than the necessary to 
	 * be a complete graph.
	 */
	public Set<V> incompleteVertices() {
		int grade = (originalgraph.vertexSet().size() - 1);

		if (isDirectedGraph()) {
			return this.incompleteVertices_DirectedGraph(grade);
		}

		Set<V> set = new HashSet<V>();
		Set<V> temp;

		for (V v : originalgraph.vertexSet()) {
			temp = new HashSet<V>(Graphs.neighborListOf(originalgraph, v));
			if (temp.size() < grade) {
				set.add(v);
			}
		}

		return set;
	}
	

	boolean isDirectedGraph() {
		return (originalgraph instanceof DirectedGraph);
	}

	Set<V> incompleteVertices_DirectedGraph(int grade) {
		Set<V> set = new HashSet<V>();
		DirectedGraph<V, E> dg = (DirectedGraph<V, E>)originalgraph;
		List<V> sucessors;
		int loopCount;
		for (V v : originalgraph.vertexSet()) {
			loopCount = 0;
			sucessors = Graphs.successorListOf(dg, v);
			if (sucessors.contains(v)) {
				loopCount = 1;
			}
			if (sucessors.size() < (grade + loopCount)) {
				set.add(v);
			}
		}
		return set;
	}
	
}

// End ConnectivityInspector.java
