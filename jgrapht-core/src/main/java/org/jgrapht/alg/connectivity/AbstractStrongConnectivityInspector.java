/*
 * (C) Copyright 2005-2021, by Christian Soltenborn and Contributors.
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
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Base implementation of the strongly connected components algorithm.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Christian Soltenborn
 * @author Christian Hammer
 * @author Dimitrios Michail
 */
abstract class AbstractStrongConnectivityInspector<V, E>
    implements
    StrongConnectivityAlgorithm<V, E>
{
    protected final Graph<V, E> graph;
    protected List<Set<V>> stronglyConnectedSets;
    protected List<Graph<V, E>> stronglyConnectedSubgraphs;

    protected AbstractStrongConnectivityInspector(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireDirected(graph);
    }

    @Override
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    @Override
    public boolean isStronglyConnected()
    {
        return stronglyConnectedSets().size() == 1;
    }

    @Override
    public List<Graph<V, E>> getStronglyConnectedComponents()
    {
        if (stronglyConnectedSubgraphs == null) {
            List<Set<V>> sets = stronglyConnectedSets();
            stronglyConnectedSubgraphs = new ArrayList<>(sets.size());

            for (Set<V> set : sets) {
                stronglyConnectedSubgraphs.add(new AsSubgraph<>(graph, set, null));
            }
        }
        return stronglyConnectedSubgraphs;
    }

    @Override
    public Graph<Graph<V, E>, DefaultEdge> getCondensation()
    {
        List<Set<V>> sets = stronglyConnectedSets();

        Graph<Graph<V, E>, DefaultEdge> condensation = new SimpleDirectedGraph<>(DefaultEdge.class);
        Map<V, Graph<V, E>> vertexToComponent =
            CollectionUtil.newHashMapWithExpectedSize(graph.vertexSet().size());

        for (Set<V> set : sets) {
            Graph<V, E> component = new AsSubgraph<>(graph, set, null);
            condensation.addVertex(component);
            for (V v : set) {
                vertexToComponent.put(v, component);
            }
        }

        for (E e : graph.edgeSet()) {
            V s = graph.getEdgeSource(e);
            Graph<V, E> sComponent = vertexToComponent.get(s);

            V t = graph.getEdgeTarget(e);
            Graph<V, E> tComponent = vertexToComponent.get(t);

            if (sComponent != tComponent) { // reference equal on purpose
                condensation.addEdge(sComponent, tComponent);
            }
        }

        return condensation;
    }

}
