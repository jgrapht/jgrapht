/*
 * (C) Copyright 2018-2018, by Ira Justus Fesefeldt and Contributors.
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
package org.jgrapht.alg.decomposition;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.cycle.*;

/**
 * A builder for a nice decomposition for chordal graphs. See {@link NiceDecompositionBuilder} for
 * an explanation of nice decomposition.
 * <p>
 * This builder uses the perfect elimination order from {@link ChordalityInspector} to iterate over
 * the graph. For every node it generates a node for the predecessors of the current node according
 * to the perfect elimination order and builds a path to such a node from the node where the
 * greatest predecessor was introduced.
 * <p>
 * The complexity of this algorithm is in $\mathcal{O}(|V|(|V|+|E|))$.<br>
 * Consider the every node in the nice tree decomposition: There are exactly $|V|$ many forget
 * nodes. There are at most $2|V|$ additionally nodes because of a join nodes. Every join node
 * creates one additional path from root to a leaf, every such path can contain for every vertex at
 * most one introduce node, which yields $|V|^2$ introduce nodes. Now considering the bags of the
 * introduce nodes. On a path from root to a leaf we have at most one introduce node for every
 * introduced vertex. Since this introduced vertex is part of the clique of the least ancestor
 * forget node, the corresponding bag of the introduce node is smaller than the set of neighbors of
 * this vertex. Thus the time complexity is in $\mathcal{O}(|V|(|V|+|E|))$.
 * <p>
 * This is a non-recursive adaption for nice tree decomposition of algorithm 2 from here: <br>
 * Hans L. Bodlaender, Arie M.C.A. Koster, Treewidth computations I. Upper bounds, Information and
 * Computation, Volume 208, Issue 3, 2010, Pages 259-275,
 * 
 * @author Ira Justus Fesefeldt (PhoenixIra)
 * @author Timofey Chudakov
 *
 * @since June 2018
 *
 * @param <V> the vertex type of the graph
 * @param <E> the edge type of the graph
 */
public class ChordalNiceDecompositionBuilder<V, E>
    extends
    NiceDecompositionBuilder<V>
{
    // the chordal graph
    Graph<V, E> graph;

    // the perfect elimination order of graph
    List<V> perfectOrder;

    // another representation of the perfect elimination order of graph
    Map<V, Integer> vertexInOrder;

    /**
     * Factory method for the nice decomposition builder of chordal graphs. Returns null, if the
     * graph is not chordal.
     * 
     * @param <V> the vertex type of graph
     * @param <E> the edge type of graph
     * @param graph the chordal graph for which a decomposition should be created
     * @return a nice decomposition builder for the graph if the graph was chordal, else null
     */
    public static <V, E> ChordalNiceDecompositionBuilder<V, E> create(Graph<V, E> graph)
    {
        ChordalityInspector<V, E> inspec = new ChordalityInspector<V, E>(graph);
        if (!inspec.isChordal())
            return null;
        ChordalNiceDecompositionBuilder<V, E> builder =
            new ChordalNiceDecompositionBuilder<>(graph, inspec.getPerfectEliminationOrder());
        builder.computeNiceDecomposition();
        return builder;

    }

    /**
     * Factory method for the nice decomposition builder of chordal graphs. This method needs the
     * perfect elimination order. It does not check whether the order is correct. This method may
     * behave arbitrary if the perfect elimination order is incorrect.
     * 
     * @param <V> the vertex type of graph
     * @param <E> the edge type of graph
     * @param graph the chordal graph for which a decomposition should be created
     * @param perfectEliminationOrder the perfect elimination order of the graph
     * @return a nice decomposition builder for the graph if the graph was chordal, else null
     */
    public static <V, E> ChordalNiceDecompositionBuilder<V, E> create(
        Graph<V, E> graph, List<V> perfectEliminationOrder)
    {
        ChordalNiceDecompositionBuilder<V, E> builder =
            new ChordalNiceDecompositionBuilder<>(graph, perfectEliminationOrder);
        builder.computeNiceDecomposition();
        return builder;

    }

    /**
     * Creates a nice decomposition builder for chordal graphs.
     * 
     * @param graph the chordal graph
     * @param perfectOrder the perfect elimination order of graph
     */
    private ChordalNiceDecompositionBuilder(Graph<V, E> graph, List<V> perfectOrder)
    {
        super();
        this.graph = graph;
        this.perfectOrder = perfectOrder;
        vertexInOrder = getVertexInOrder();
    }

    /**
     * Computes the nice decomposition of the graph. We iterate over the perfect elimination order
     * of the chordal graph and try to add a node containing the predecessors regarding the
     * perfect elimination as a bag to the tree
     */
    private void computeNiceDecomposition()
    {

        // map from vertices to decomposition-nodes where the decomposition-node has all its
        // predecessors
        Map<V, Integer> forgetNodeMap = new HashMap<V, Integer>(graph.vertexSet().size());

        // set current node to the root
        Integer decompNode = getRoot();

        // iterate over the perfect elimination order
        for (V vertex : perfectOrder) {

            // get the predecessors regarding the perfect elimination order
            Set<V> predecessors = getOrderPredecessors(vertexInOrder, vertex);

            // calculate nearest successors according to perfect elimination order
            V lastVertex = null;
            for (V predecessor : predecessors) {
                if (lastVertex == null)
                    lastVertex = predecessor;
                if (vertexInOrder.get(predecessor) > vertexInOrder.get(lastVertex))
                    lastVertex = predecessor;
            }

            // get node with clique of last vertex, else we use the last node
            if (lastVertex != null)
                decompNode = forgetNodeMap.get(lastVertex);

            // if this node is not a leaf node, create a join node
            if (Graphs.vertexHasSuccessors(decomposition, decompNode)) {
                decompNode = addJoin(decompNode).getFirst();
            }

            // calculate vertices of nearest successor, which needs to be handled
            Set<V> clique = new HashSet<V>(predecessors);
            clique.add(vertex);
            Set<V> toIntroduce = new HashSet<V>(decompositionMap.get(decompNode));
            toIntroduce.removeAll(clique);

            // first remove unnecessary nodes
            for (V introduce : toIntroduce) {
                decompNode = addIntroduce(introduce, decompNode);
            }
            // now add new node!
            decompNode = addForget(vertex, decompNode);
            forgetNodeMap.put(vertex, decompNode);

        }
        //finish all unfinished paths
        leafClosure();

    }

    /**
     * Returns a map containing vertices from the {@code vertexOrder} mapped to their indices in
     * {@code vertexOrder}.
     *
     * @param vertexOrder a list with vertices.
     * @return a mapping of vertices from {@code vertexOrder} to their indices in
     *         {@code vertexOrder}.
     */
    private Map<V, Integer> getVertexInOrder()
    {
        Map<V, Integer> vertexInOrder = new HashMap<>(perfectOrder.size());
        int i = 0;
        for (V vertex : perfectOrder) {
            vertexInOrder.put(vertex, i++);
        }
        return vertexInOrder;
    }

    /**
     * Returns the predecessors of {@code vertex} in the perfect elimination order defined by
     * {@code map}. More precisely, returns those of {@code vertex}, whose mapped index in
     * {@code map} is less then the index of {@code vertex}.
     *
     * @param map defines the mapping of vertices in {@code graph} to their indices in order.
     * @param vertex the vertex whose predecessors in order are to be returned.
     * @return the predecessors of {@code vertex} in order defines by {@code map}.
     */
    private Set<V> getOrderPredecessors(Map<V, Integer> map, V vertex)
    {
        Set<V> predecessors = new HashSet<>();
        Integer vertexPosition = map.get(vertex);
        Set<E> edges = graph.edgesOf(vertex);
        for (E edge : edges) {
            V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);
            Integer destPosition = map.get(oppositeVertex);
            if (destPosition < vertexPosition) {
                predecessors.add(oppositeVertex);
            }
        }
        return predecessors;
    }
}
