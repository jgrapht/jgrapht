/*
 * (C) Copyright 2013-2020, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.spanning;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.util.*;
import org.jheaps.*;
import org.jheaps.tree.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * An implementation of <a href="http://en.wikipedia.org/wiki/Prim's_algorithm"> Prim's
 * algorithm</a> that finds a minimum spanning tree/forest subject to connectivity of the supplied
 * weighted undirected graph. The algorithm was developed by Czech mathematician V. Jarník and later
 * independently by computer scientist Robert C. Prim and rediscovered by E. Dijkstra.
 *
 * This implementation relies on a Fibonacci heap, and runs in $O(|E| + |V|log(|V|))$.
 *
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 * @author Alexey Kudinkin
 */
public class PrimMinimumSpanningTree<V, E>
    implements
    SpanningTreeAlgorithm<E>
{
    private final Graph<V, E> g;

    /**
     * Construct a new instance of the algorithm.
     * 
     * @param graph the input graph
     */
    public PrimMinimumSpanningTree(Graph<V, E> graph)
    {
        this.g = Objects.requireNonNull(graph, "Graph cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public SpanningTree<E> getSpanningTree()
    {
        Set<E> minimumSpanningTreeEdgeSet =
            CollectionUtil.newHashSetWithExpectedSize(g.vertexSet().size());
        double spanningTreeWeight = 0d;

        final int N = g.vertexSet().size();

        /*
         * Normalize the graph by mapping each vertex to an integer.
         */
        VertexToIntegerMapping<V> vertexToIntegerMapping = Graphs.getVertexToIntegerMapping(g);
        Map<V, Integer> vertexMap = vertexToIntegerMapping.getVertexMap();
        List<V> indexList = vertexToIntegerMapping.getIndexList();

        VertexInfo[] vertices = (VertexInfo[]) Array.newInstance(VertexInfo.class, N);
        AddressableHeap.Handle<Double, VertexInfo>[] fibNodes =
            (AddressableHeap.Handle<Double, VertexInfo>[]) Array
                .newInstance(AddressableHeap.Handle.class, N);
        AddressableHeap<Double, VertexInfo> fibonacciHeap = new FibonacciHeap<>();

        for (int i = 0; i < N; i++) {
            vertices[i] = new VertexInfo();
            vertices[i].id = i;
            vertices[i].distance = Double.MAX_VALUE;
            fibNodes[i] = fibonacciHeap.insert(vertices[i].distance, vertices[i]);
        }

        while (!fibonacciHeap.isEmpty()) {
            AddressableHeap.Handle<Double, VertexInfo> fibNode = fibonacciHeap.deleteMin();
            VertexInfo vertexInfo = fibNode.getValue();

            V p = indexList.get(vertexInfo.id);
            vertexInfo.spanned = true;

            // Add the edge from its parent to the spanning tree (if it exists)
            if (vertexInfo.edgeFromParent != null) {
                minimumSpanningTreeEdgeSet.add(vertexInfo.edgeFromParent);
                spanningTreeWeight += g.getEdgeWeight(vertexInfo.edgeFromParent);
            }

            // update all (unspanned) neighbors of p
            for (E e : g.edgesOf(p)) {
                V q = Graphs.getOppositeVertex(g, e, p);
                int id = vertexMap.get(q);

                // if the vertex is not explored and we found a better edge, then update the info
                if (!vertices[id].spanned) {
                    double cost = g.getEdgeWeight(e);

                    if (cost < vertices[id].distance) {
                        vertices[id].distance = cost;
                        vertices[id].edgeFromParent = e;
                        fibNodes[id].decreaseKey(cost);
                    }
                }
            }
        }

        return new SpanningTreeImpl<>(minimumSpanningTreeEdgeSet, spanningTreeWeight);
    }

    private class VertexInfo
    {
        public int id;
        public boolean spanned;
        public double distance;
        public E edgeFromParent;
    }
}
