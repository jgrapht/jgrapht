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
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;

/**
 * An abstract builder class for nice tree decompositions, which builds the tree decomposition in a
 * top-down manner.
 * <p>
 * A tree decomposition of a graph $G$ is a tree $T$ and a map $b:V(T) \rightarrow Set&lt;V(G)&gt;$,
 * which satisfies the properties:
 * <ul>
 * <li>for every edge $e \in E(G)$, there is a node $t \in V(T)$ with $e \subseteq b(v)$</li>
 * <li>for all vertices $v \in V(G)$ the set $\{t \in V(T) | v \in b(t)\}$ is non-empty and
 * connected in $T$</li>
 * </ul>
 * <br>
 * A nice tree decomposition is a special tree decomposition, which satisfies the properties:
 * <ul>
 * <li>for root $r \in V(T)$ and leaf $l \in V(T): b(r)=b(t)=\emptyset$</li>
 * <li>every non-leaf node $t \in V(T)$ is of one of the following three types:
 * <ul>
 * <li>forget node: $t$ has exactly one child $d$ and $b(t) = b(d) \cup w$ for some $w \in
 * V(G)\setminus b(d)$</li>
 * <li>introduce node: $t$ has exactly one child $d$ and $b(t) \cup w = b(d)$ for some $w \in
 * V(G)\setminus b(t)$</li>
 * <li>join node: $t$ has exactly two children $d_1$, $d_2$ and $b(t)=b(d_1)=b(d_2)$</li>
 * </ul>
 * </ul>
 * <p>
 * See:<br>
 * Bodlaender, Hans &amp; Kloks, Ton. (1991). Better Algorithms for the Pathwidth and Treewidth of
 * Graphs. 544-555. 10.1007/3-540-54233-7_162.
 * 
 * @author Ira Justus Fesefeldt (PhoenixIra)
 *
 * @param <V> the vertices of the graph
 */
abstract public class NiceDecompositionBuilder<V>
{

    // resulting decomposition
    private Graph<Integer, DefaultEdge> decomposition;

    // map from decomposition nodes to the interval sets
    private Map<Integer, Set<V>> decompositionMap;

    // the root of the tree
    private Integer root;

    // next integer for vertex generation
    private Integer nextInteger;

    /**
     * Constructor for all methods used in the abstract method This constructor instantiates the
     * tree of the decomposition, the map from tree vertices to vertex sets and adds the root to the
     * tree.
     */
    protected NiceDecompositionBuilder()
    {
        // creating objects
        decomposition = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
        decompositionMap = new HashMap<Integer, Set<V>>();

        // create root
        root = 0;
        nextInteger = 1;
        decompositionMap.put(root, new HashSet<V>());
        decomposition.addVertex(root);
    }

    /**
     * Getter for the next free Integer Supplies the add vertex methods with new vertices
     * 
     * @return unused integer
     */
    private Integer getNextInteger()
    {
        return nextInteger++;
    }

    /**
     * Method for adding a new join node.<br>
     * {@code node} is copied two times j1 and j2. {@code node} afterwards becomes the root of the
     * subtree. j2 becomes the child of {@code node} and has the children of {@code node} as
     * children. j1 becomes a leaf and the child of {@code node}. This can be used to make a join
     * node retrospectively to branch of this node. j2 continues the path while j1 adds another
     * path.<br>
     * The time complexity of this method is in $\mathcal{O}(|b(node)|)$.
     * 
     * @param node which nodes should become a join node
     * @return the new children of the join node, first element has no children, second element has
     *         the children of toJoin
     */
    protected Pair<Integer, Integer> addJoin(Integer node)
    {
        Set<V> currentVertexBag = null;

        // new
        Integer vertexChildLeft = getNextInteger();
        decomposition.addVertex(vertexChildLeft);
        currentVertexBag = new HashSet<V>(decompositionMap.get(node));
        decompositionMap.put(vertexChildLeft, currentVertexBag);

        // new current root
        Integer vertexChildRight = getNextInteger();
        decomposition.addVertex(vertexChildRight);
        currentVertexBag = new HashSet<V>(decompositionMap.get(node));
        decompositionMap.put(vertexChildRight, currentVertexBag);

        // redirect all edges to new parent (should be just one!)
        for (Integer successor : Graphs.successorListOf(decomposition, node)) {
            decomposition.removeEdge(node, successor);
            decomposition.addEdge(vertexChildRight, successor);
        }
        // make children of parent vertex
        decomposition.addEdge(node, vertexChildLeft);
        decomposition.addEdge(node, vertexChildRight);

        return new Pair<Integer, Integer>(vertexChildLeft, vertexChildRight);
    }

    /**
     * Method for adding forget nodes. It is only usable if {@code node} is a leaf. It then adds the
     * new node as the child of {@code node} with the set of {@code node} plus
     * {@code forgottenElement}.<br>
     * The time complexity of this method is in $\mathcal{O}(|b(node)|)$.
     * 
     * @param forgottenElement the element, which gets forgotten
     * @param node the node of the tree decomposition, which becomes a forget node
     * @return the newly created vertex, null and no change if either introducedElement is in the
     *         bag of currentVertex or currentVertex is not a leaf.
     */
    protected Integer addForget(V forgottenElement, Integer node)
    {
        if (!Graphs.successorListOf(decomposition, node).isEmpty())
            return null;
        if (decompositionMap.get(node).contains(forgottenElement))
            return null;

        Set<V> nextVertexBag = new HashSet<>(decompositionMap.get(node));
        nextVertexBag.add(forgottenElement);
        Integer nextVertex = getNextInteger();
        decomposition.addVertex(nextVertex);
        decomposition.addEdge(node, nextVertex);
        decompositionMap.put(nextVertex, nextVertexBag);

        return nextVertex;
    }

    /**
     * Method for adding introduce nodes. It is only usable if {@code node} is a leaf. It then adds
     * the new node as the child of {@code node} with the set of {@code node} minus
     * {@code introducedElement}.<br>
     * The time complexity of this method is in $\mathcal{O}(|b(node)|)$.
     * 
     * @param introducedElement the element, which is introduced
     * @param node the node, which becomes an introduce node
     * @return the next vertex, null and no change if either introducedElement is in the bag of
     *         currentVertex or currentVertex is not a leaf.
     */
    protected Integer addIntroduce(V introducedElement, Integer node)
    {
        if (!Graphs.successorListOf(decomposition, node).isEmpty())
            return null;
        if (!decompositionMap.get(node).contains(introducedElement))
            return null;

        Set<V> nextVertexBag = new HashSet<>(decompositionMap.get(node));
        nextVertexBag.remove(introducedElement);
        Integer nextVertex = getNextInteger();
        decomposition.addVertex(nextVertex);
        decomposition.addEdge(node, nextVertex);
        decompositionMap.put(nextVertex, nextVertexBag);

        return nextVertex;
    }

    /**
     * Adds to all current leaves in the decomposition forget/introduce nodes until only empty sets
     * are leaves.<br>
     */
    protected void leafClosure()
    {
        Set<Integer> vertices = new HashSet<Integer>(decomposition.vertexSet());
        // make leave nodes
        for (Integer leaf : vertices) {
            // leaf is not a leaf
            if (Graphs.vertexHasSuccessors(decomposition, leaf))
                continue;

            // otherwise add nodes until empty set
            Set<V> vertexSet = decompositionMap.get(leaf);
            Integer current = leaf;
            for (V forget : vertexSet) {
                current = addIntroduce(forget, current);
            }
        }
    }

    /**
     * Returns the tree of the decomposition as an unmodifiable, directed graph
     * 
     * @return the computed decomposition
     */
    public Graph<Integer, DefaultEdge> getDecomposition()
    {
        return new AsUnmodifiableGraph<>(decomposition);
    }

    /**
     * Returns the map from integer nodes of the tree decomposition {@code getDecomposition()} to
     * the intervals of the interval graph as an unmodifiable map
     * 
     * @return a nodes to interval map
     */
    public Map<Integer, Set<V>> getMap()
    {
        return Collections.unmodifiableMap(decompositionMap);
    }

    /**
     * Returns the root of the decomposition {@code getDecomposition()}
     * 
     * @return a set of roots
     */
    public Integer getRoot()
    {
        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return getDecomposition() + "\n " + getMap();
    }
}