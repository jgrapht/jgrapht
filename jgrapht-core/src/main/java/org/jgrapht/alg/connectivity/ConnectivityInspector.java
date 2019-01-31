/*
 * (C) Copyright 2003-2017, by Barak Naveh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.connectivity;

import org.jgrapht.*;
import org.jgrapht.event.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

import java.util.*;

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
 */
public class ConnectivityInspector<V, E>
    implements
    GraphListener<V, E>
{
    private List<Set<V>> connectedSets;
    private Map<V, Set<V>> vertexToConnectedSet;
    private Graph<V, E> graph;

    /**
     * Creates a connectivity inspector for the specified graph.
     *
     * @param g the graph for which a connectivity inspector to be created.
     */
    public ConnectivityInspector(Graph<V, E> g)
    {
        init();
        this.graph = Objects.requireNonNull(g);
        if (g.getType().isDirected())
            this.graph = new AsUndirectedGraph<>(g);
    }

    /**
     * Test if the inspected graph is connected. A graph is connected when there is a path between
     * every pair of vertices. In a connected graph, there are no unreachable vertices. When the
     * inspected graph is a <i>directed</i> graph, this method returns true if and only if the
     * inspected graph is <i>weakly</i> connected. An empty graph is <i>not</i> considered
     * connected.
     *
     * @return <code>true</code> if and only if inspected graph is connected.
     */
    public boolean isConnected()
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
        V source = e.getEdgeSource();
        V target = e.getEdgeTarget();
        Set<V> sourceSet = connectedSetOf(source);
        Set<V> targetSet = connectedSetOf(target);

        // If source and target are in the same set, do nothing, otherwise, merge sets
        if (sourceSet != targetSet) {
            Set<V> merge = new HashSet<>();
            merge.addAll(sourceSet);
            merge.addAll(targetSet);
            connectedSets.remove(sourceSet);
            connectedSets.remove(targetSet);
            connectedSets.add(merge);
            for (V v : merge)
                vertexToConnectedSet.put(v, merge);
        }
    }

    /**
     * @see GraphListener#edgeRemoved(GraphEdgeChangeEvent)
     */
    @Override
    public void edgeRemoved(GraphEdgeChangeEvent<V, E> e)
    {
        init(); // for now invalidate cached results, in the future need to
                // amend them. If the edge is a bridge, 2 components need to be split.
    }

    /**
     * Tests whether two vertices lay respectively in the same connected component (undirected
     * graph), or in the same weakly connected component (directed graph).
     *
     * @param sourceVertex one end of the path.
     * @param targetVertex another end of the path.
     *
     * @return <code>true</code> if and only if the source and target vertex are in the same
     *         connected component (undirected graph), or in the same weakly connected component
     *         (directed graph).
     */
    public boolean pathExists(V sourceVertex, V targetVertex)
    {
        return connectedSetOf(sourceVertex).contains(targetVertex);
    }

    /**
     * @see VertexSetListener#vertexAdded(GraphVertexChangeEvent)
     */
    @Override
    public void vertexAdded(GraphVertexChangeEvent<V> e)
    {
        Set<V> component = new HashSet<>();
        component.add(e.getVertex());
        connectedSets.add(component);
        vertexToConnectedSet.put(e.getVertex(), component);
    }

    /**
     * @see VertexSetListener#vertexRemoved(GraphVertexChangeEvent)
     */
    @Override
    public void vertexRemoved(GraphVertexChangeEvent<V> e)
    {
        init(); // for now invalidate cached results, in the future need to
                // amend them. If the vertex is an articulation point, two
                // components need to be split
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

            if (!vertexSet.isEmpty()) {
                BreadthFirstIterator<V, E> i = new BreadthFirstIterator<>(graph);
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
     */
    private class MyTraversalListener
        extends
        TraversalListenerAdapter<V, E>
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
}
