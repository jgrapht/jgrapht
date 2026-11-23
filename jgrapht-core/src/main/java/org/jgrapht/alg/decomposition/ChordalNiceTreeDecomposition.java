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
 * Builds a nice tree decomposition of chordal graphs. See {@link NiceTreeDecompositionBase} for an
 * explanation of the nice tree decomposition.
 * <p>
 * For a given perfect elimination order of the graph $G = (V, E)$ every vertex in $V(G)$ has an
 * associated index in this order. The predecessors of a vertex $x \in V(G)$ are those vertices
 * incident to $x$ that have smaller index than $x$ (a vertex can have no predecessors). This class
 * uses a perfect elimination order produced by {@link ChordalityInspector} to iteratively add
 * vertices of $G$ to some nodes in the tree $T$. Initially, it adds a bag to the decomposition with
 * the first vertex from the perfect elimination order as a root of the tree. Then for all remaining
 * vertices $v \in V(G)$ this class generates a node (bag) $s \in I(T)$ in the tree $T$ which
 * contains vertex $v$ and all its predecessors. Then it builds a path from $s \in I(T)$ to $t \in
 * I(T)$, where $s$ is the node where the predecessor of $v$ with the highest index was added to the
 * decomposition for the first time. If the vertex $v$ has no predecessors, a path is built to an
 * arbitrary node from the tree $T$. The bags on the resulting path between $s$ and $t$ contain only
 * cliques.
 * <p>
 * The time complexity of this algorithm is $\mathcal{O}(|V|(|V|+|E|))$. In the decomposition
 * produced by this class there are at most $|V|-1$ additionally added join nodes which are
 * generated during the connection of forget nodes to the tree. The algorithm adds exactly $|V|-1$
 * many forget nodes since they are generated for every vertex in the $V(G)$ except for the vertex
 * added as a root. They may be doubled by join nodes, which adds up to at most $2(|V|-1)$ forget
 * nodes. Every join node creates one additional root-to-leaf path. Therefore, there are
 * $\mathcal{O}(|V|)$ root-to-leaf paths in the resulting decomposition. Every root-to-left path can
 * contain $\mathcal{O}(|V|)$ introduce nodes, which yields $\mathcal{O}(|V|^2)$ upper bound on
 * introduce nodes. Now considering the bags of the introduce nodes. Bags of every root-to-leaf path
 * can contain at most $|V|$ distinct vertices of the initial graph. Every vertex $v$ can be
 * introduced and forgotten on a path at most once. The sizes of these introduce and forget nodes
 * are $\mathcal{O}(deg(v) + 1)$. Consequently, every root-to-leaf path is constructed in
 * $\mathcal{O}(|V| + |E|)$ time. Since there are $\mathcal{O}(|V|)$ root-to-leaf paths, the total
 * running time is $\mathcal{O}(|V|(|V| + |E|))$.
 * <p>
 * The algorithm used to construct a nice tree decomposition is a non-recursive adaption of
 * algorithm 2 from here: <br>
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
public class ChordalNiceTreeDecomposition<V, E>
    extends
    NiceTreeDecompositionBase<V>
{
    // the chordal graph
    private Graph<V, E> graph;

    // the perfect elimination order of graph
    private List<V> perfectOrder;

    // another representation of the perfect elimination order of graph
    private Map<V, Integer> vertexInOrder;

    /**
     * Constructor for the nice tree decomposition of chordal graphs.
     * The tree decomposition is calculated lazy when its needed for the first time.
     * 
     * @throws IllegalArgumentException if the graph is not chordal.
     * @param graph the chordal graph for which a nice tree decomposition should be created
     */
    public ChordalNiceTreeDecomposition(Graph<V, E> graph)
    {
        super();
        ChordalityInspector<V, E> inspec = new ChordalityInspector<>(graph);
        if (!inspec.isChordal())
            throw new IllegalArgumentException("The given graph is not chordal.");
        this.graph = graph;
        this.perfectOrder = inspec.getPerfectEliminationOrder();
        vertexInOrder = getVertexInOrder();
    }

    /**
     * Constructor for the nice tree decomposition of chordal graphs. 
     * This method needs the perfect elimination order. 
     * The tree decomposition is calculated lazy when its needed for the first time.<p>
     * Note: This method does NOT check whether the order is correct.
     * 
     * @param graph the chordal graph for which a nice tree decomposition should be created
     * @param perfectEliminationOrder the perfect elimination order of the graph
     */
    public ChordalNiceTreeDecomposition(Graph<V, E> graph, List<V> perfectEliminationOrder)
    {
        super();
        this.graph = graph;
        this.perfectOrder = perfectEliminationOrder;
        vertexInOrder = getVertexInOrder();
    }

    /**
     * Computes the nice tree decomposition of the graph. We iterate over the perfect elimination order
     * of the chordal graph and try to add a node containing the predecessors regarding the
     * perfect elimination as a bag to the tree
     */
    protected void computeLazyNiceTreeDecomposition()
    {
        /*
         * Map from the vertices of the graph to the forget nodes from the decomposition tree. The mapping 
         * is one-to-one and each decomposition node contains corresponding vertex and all its predecessors.
         */
        Map<V, Integer> forgetNodeMap = new HashMap<>(graph.vertexSet().size());

        //iterator over the perfect elimination order
        Iterator<V> iterator = perfectOrder.iterator();
        
        //empty graph
        if(!iterator.hasNext())
            return;
        
        // set current node to the root
        V element = iterator.next();
        int decompNode = addRootNode(element);
        forgetNodeMap.put(element, decompNode);

        // iterate over the perfect elimination order
        while (iterator.hasNext()) {
            V vertex = iterator.next();
            // get the predecessors regarding the perfect elimination order
            List<V> predecessors = getOrderPredecessors(vertexInOrder, vertex);

            /*
             * Calculate the nearest predecessor according to the perfect elimination order.
             * The nearest predecessor means here the predecessor of the vertex, for which the forget node
             * was created last and thus is the highest predecessor (and nearest to the vertex).
             */
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
                decompNode = addJoinNode(decompNode).getFirst();
            }

            // calculate vertices of nearest successor, which needs to be handled
            Set<V> toIntroduce = new HashSet<>(decompositionMap.get(decompNode));
            toIntroduce.removeAll(predecessors);
            toIntroduce.remove(vertex);

            // first remove unnecessary nodes
            for (V introduce : toIntroduce) {
                decompNode = addIntroduceNode(introduce, decompNode);
            }
            // now add new node!
            decompNode = addForgetNode(vertex, decompNode);
            forgetNodeMap.put(vertex, decompNode);

        }
        //finish all unfinished paths
        leafClosure();
    }

    /**
     * Returns a map containing vertices from the {@code vertexOrder} mapped to their indices in
     * {@code vertexOrder}.
     *
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
    private List<V> getOrderPredecessors(Map<V, Integer> map, V vertex)
    {
        Set<E> edges = graph.edgesOf(vertex);
        List<V> predecessors = new ArrayList<>(edges.size());
        int vertexPosition = map.get(vertex);
        for (E edge : edges) {
            V oppositeVertex = Graphs.getOppositeVertex(graph, edge, vertex);
            int destPosition = map.get(oppositeVertex);
            if (destPosition < vertexPosition) {
                predecessors.add(oppositeVertex);
            }
        }
        return predecessors;
    }
}
