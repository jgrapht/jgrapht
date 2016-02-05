/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2016, by Barak Naveh and Contributors.
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
/* -------------------
 * GomoryHuCutTree.java
 * -------------------
 * (C) Copyright 2016, by Mads Jensen
 *
 * Original Author:  Mads Jensen
 *
 * Changes
 * -------
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

/**
 * Implementation of the Gomory Hu minimum cut tree algorithm defined
 * originally by Gomory and Hu in 1961, and revised for simplicity by
 * Gusfield.  The article builds on computing O(n) max flows.
 *
 * Gomory, Ralph E., and Tien Chung Hu. "Multi-terminal network flows."
 * Journal of the Society for Industrial and Applied Mathematics 9.4 (1961):
 * 551-570.
 *
 * Gusfield, Dan. "Very simple methods for all pairs network flow analysis."
 * SIAM Journal on Computing 19.1 (1990): 143-155.
 *
 * @author Mads Jensen
 * @since January 2016
 */
public class GomoryHuCutTree<V, E> {

    private final SimpleWeightedGraph<V, E> graph;
    private SimpleWeightedGraph<V, DefaultWeightedEdge> gomoryHuCutTree;

    /**
     * Constructor for the algorithm which also invokes a private method that
     * computes the minimum cut tree.
     *
     * @param graph Undirected graph for which to compute the Gomory Hu cut tree
     */
    public GomoryHuCutTree(SimpleWeightedGraph<V, E> graph){
        this.graph = graph;
        this.gomoryHuCutTree = new SimpleWeightedGraph<V, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        this.run();
    }

    /**
     * Generates a new bidirectional weighted graph
     * @param graph Undirected graph to be converted into a bidirectional graph
     * @return A bidirectional graph
     */
    private DefaultDirectedWeightedGraph<V, DefaultWeightedEdge>
        makeDirectedCopy(UndirectedGraph<V, E> graph)
        {
            // code is based on org.jgrapht.alg.CliqueMinimalSeparatorDecomposition:copyAsSimpleGraph
            DefaultDirectedWeightedGraph<V, DefaultWeightedEdge> copy =
                new DefaultDirectedWeightedGraph<V, DefaultWeightedEdge>(DefaultWeightedEdge.class);

            Graphs.addAllVertices(copy, graph.vertexSet());
            for (E e : this.graph.edgeSet()) {
                V v1 = this.graph.getEdgeSource(e);
                V v2 = this.graph.getEdgeTarget(e);
                Graphs.addEdge(copy, v1, v2, graph.getEdgeWeight(e));
                Graphs.addEdge(copy, v2, v1, graph.getEdgeWeight(e));
            }
            return copy;
        }

    private void run(){
        HashMap<V, V> predecessors = new HashMap<V, V>();
        DefaultDirectedWeightedGraph<V, DefaultWeightedEdge> directedGraph =
            new DefaultDirectedWeightedGraph<V, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        directedGraph = this.makeDirectedCopy(this.graph);
        MinSourceSinkCut minSourceSinkCut = new MinSourceSinkCut(directedGraph);

        Graphs.addAllVertices(this.gomoryHuCutTree, this.graph.vertexSet());

        Set<V> vertexSet = directedGraph.vertexSet();
        Iterator<V> it = vertexSet.iterator();
        V start = it.next();
        predecessors.put(start, start);
        while(it.hasNext()){
            V vertex = it.next();
            predecessors.put(vertex, start);
        }

        Iterator<V> itVertices = directedGraph.vertexSet().iterator();
        itVertices.next();
        while(itVertices.hasNext()){
            V vertex = itVertices.next();
            V predecessor = predecessors.get(vertex);
            minSourceSinkCut.computeMinCut(vertex, predecessor);
            Set<V> sourcePartition = minSourceSinkCut.getSourcePartition();
            double flowValue = minSourceSinkCut.getCutWeight();
            for(V sourceVertex : this.graph.vertexSet()){
                if(!sourceVertex.equals(vertex)
                   && predecessors.get(sourceVertex).equals(predecessor)
                   && sourcePartition.contains(sourceVertex)){
                    predecessors.put(sourceVertex, vertex);
                }
            }
            if(sourcePartition.contains(predecessors.get(predecessor))){
                V predecessorParent = predecessors.get(predecessor);
                // p[s] = p[t]
                predecessors.put(vertex, predecessorParent);
                predecessors.put(predecessorParent, vertex);
            }
            DefaultWeightedEdge cutEdge =
                Graphs.addEdge(this.gomoryHuCutTree, predecessor, vertex, flowValue);
        }
    }

    public SimpleWeightedGraph<V, DefaultWeightedEdge> getGomoryHuCutTreeGraph(){
        return this.gomoryHuCutTree;
    }
}
