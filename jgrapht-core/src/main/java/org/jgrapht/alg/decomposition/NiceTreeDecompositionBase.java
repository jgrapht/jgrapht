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
 * An abstract class for building a nice tree decomposition in a top-down manner.
 * <p>
 * A tree decomposition of a graph $G = (V, E)$ is a pair $(X, T) = (\{X_i\ |\ i\in I\}, (I, F))$ where
 * $X = \{X_i\ |\ i \in I \}$ is a family of subsets of $V$, and $T = (I, F)$ is a tree, such that
 * <ul>
 * <li>union of the sets $X_i$ equals to $V$</li>
 * <li>for every edge $e = (u,v)$ there exists a set $X_i$ such that $u \in X_i$ and $v \in X_i$</li>
 * <li>if both $X_i$ and $X_j$ contain a vertex $v$ then every set $X_k$ on the simple path from $X_i$ to $X_j$
 * contains vertex $v$.</li>
 * </ul>
 * In the following we will use the map $b:I \rightarrow X$ with $b(i)=X_i$ to denote the set $X_i \in X$ of a 
 * tree decomposition node $i \in I$.
 * <br>
 * A nice tree decomposition is a special tree decomposition, which satisfies the properties:
 * <ul>
 * <li>for root $r \in V(T)$ and leaf $l \in V(T): |b(r)|=|b(t)|=1$</li>
 * <li>every non-leaf node $t \in V(T)$ is of one of the following three types:
 * <ul>
 * <li>forget node: $t$ has exactly one child $d$ and $b(t) \cup \{ w\} = b(d)$ for some $w \in
 * V(G)\setminus b(t)$</li>
 * <li>introduce node: $t$ has exactly one child $d$ and $b(t) = b(d) \cup \{ w\}$ for some $w \in
 * V(G)\setminus b(d)$</li>
 * <li>join node: $t$ has exactly two children $d_1$, $d_2$ and $b(t)=b(d_1)=b(d_2)$</li>
 * </ul>
 * </ul>
 * <p>
 * See:<br>
 * Bodlaender, Hans &amp; Kloks, Ton. (1991). Better Algorithms for the Pathwidth and Treewidth of
 * Graphs. 544-555. 10.1007/3-540-54233-7_162.<br>
 * for a more complete description of tree decomposition and nice tree decomposition.
 * 
 * @author Ira Justus Fesefeldt (PhoenixIra)
 *
 * @param <V> the vertices of the graph
 */
abstract public class NiceTreeDecompositionBase<V>
{

    // resulting decomposition
    protected Graph<Integer, DefaultEdge> decomposition;

    // map from decomposition nodes to the vertex sets
    protected Map<Integer, Set<V>> decompositionMap;

    // the root of the tree
    protected int root;

    // next integer for vertex generation
    private int nextInteger;
    
    // flag that checks whether computeNiceTreeDecomposition was already called
    private boolean hasLazyComputed;
    
    /**
     * Constructor for all methods used in the abstract method. This constructor instantiates the
     * tree of the decomposition, the map from tree vertices to vertex sets and adds the root to the
     * tree.
     */
    protected NiceTreeDecompositionBase()
    {
        // creating objects
        decomposition = new DefaultDirectedGraph<>(DefaultEdge.class);
        decompositionMap = new HashMap<>();
        nextInteger = 0;
        hasLazyComputed = false;
    }
    
    /**
     * Method for computing the nice tree decomposition for the given graph lazy. This method is called whenever 
     * a getter is called and should compute {@code root}, {@code decomposition} and {@code decompositionMap}. 
     * The implementation must be done in the concrete class.
     */
    protected abstract void computeLazyNiceTreeDecomposition();

    /**
     * Getter for the next integer, which is not yet used as a node in the decomposition. 
     * Supplies the add vertex methods with new vertices.
     * 
     * @return unused integer node for the decomposition
     */
    private int getNextInteger()
    {
        return nextInteger++;
    }
    
    /**
     * Method for adding a root node. This method should be called at the start of every computation.
     * It creates a single node with the bag containing {@code rootElement}.
     * 
     * @throws UnsupportedOperationException when calling this method twice
     * @param rootElement the content of the bag of the root
     * @return the root node
     */
    protected Integer addRootNode(V rootElement)
    {
        if(!decomposition.vertexSet().isEmpty())
            throw new UnsupportedOperationException("A root can not be created twice");
        
        root = getNextInteger();
        Set<V> bag = new HashSet<>();
        bag.add(rootElement);
        decompositionMap.put(root, bag);
        decomposition.addVertex(root);
        return root;
    }

    /**
     * Method for adding a new join node.<br>
     * {@code node} is copied two times jLeft and jRight. {@code node} afterwards becomes the root
     * of the subtree. jRight becomes the child of {@code node} and has the children of {@code node}
     * as children. jLeft becomes a leaf and the child of {@code node}. This can be used to make a
     * join node retrospectively to branch of this node. jRight continues the path while jLeft adds
     * another path.<br>
     * The time complexity of this method is in $\mathcal{O}(|b(node)|)$.
     * 
     * @param node which nodes should become a join node
     * @return the new children of the join node; first/left element has no children, second/right
     *         element has the children of {@code node}
     */
    protected Pair<Integer, Integer> addJoinNode(Integer node)
    {
        Set<V> currentVertexBag = null;

        // create first/left new child of node
        int vertexChildLeft = getNextInteger();
        decomposition.addVertex(vertexChildLeft);
        currentVertexBag = new HashSet<>(decompositionMap.get(node));
        decompositionMap.put(vertexChildLeft, currentVertexBag);

        // create second/right new child of node
        int vertexChildRight = getNextInteger();
        decomposition.addVertex(vertexChildRight);
        currentVertexBag = new HashSet<>(decompositionMap.get(node));
        decompositionMap.put(vertexChildRight, currentVertexBag);

        // redirect all edges to new parent, the second/right element
        for (int successor : Graphs.successorListOf(decomposition, node)) {
            decomposition.removeEdge(node, successor);
            decomposition.addEdge(vertexChildRight, successor);
        }
        // make edge from node to left and right vertex
        decomposition.addEdge(node, vertexChildLeft);
        decomposition.addEdge(node, vertexChildRight);

        return new Pair<>(vertexChildLeft, vertexChildRight);
    }

    /**
     * Method for adding forget nodes. It is only usable if {@code node} is a leaf. It then adds the
     * new node as the child of {@code node} with the set of {@code node} plus
     * {@code forgottenElement}.<br>
     * The time complexity of this method is in $\mathcal{O}(|b(node)|)$.
     * 
     * @throws IllegalArgumentException if either {@code forgottenElement} is in b({@code node}) 
     * or {@code node} is not a leaf.
     * @param forgottenElement the element, which gets forgotten
     * @param node the node of the tree decomposition, which becomes a forget node
     * @return the newly created node
     */
    protected int addForgetNode(V forgottenElement, int node)
    {
        //check precondition
        if (!Graphs.successorListOf(decomposition, node).isEmpty())
            throw new IllegalArgumentException("Node "+node+" of decomposition is not a leaf.");
        if (decompositionMap.get(node).contains(forgottenElement))
            throw new IllegalArgumentException("Node "+node+" of decomposition does already contain "
                                                      +forgottenElement+", thus can not be forgotten.");

        //add new child
        Set<V> nextVertexBag = new HashSet<>(decompositionMap.get(node));
        nextVertexBag.add(forgottenElement);
        int nextVertex = getNextInteger();
        decomposition.addVertex(nextVertex);
        decomposition.addEdge(node, nextVertex);
        decompositionMap.put(nextVertex, nextVertexBag);

        //return child
        return nextVertex;
    }

    /**
     * Method for adding introduce nodes. It is only usable if {@code node} is a leaf. It then adds
     * the new node as the child of {@code node} with the set of {@code node} minus
     * {@code introducedElement}.<br>
     * The time complexity of this method is in $\mathcal{O}(|b(node)|)$.
     * 
     * @throws IllegalArgumentException if either {@code introducedElement} is not in b({@code node}) 
     * or {@code node} is not a leaf.
     * @param introducedElement the element, which is introduced
     * @param node the node, which becomes an introduce node
     * @return the newly created node
     */
    protected int addIntroduceNode(V introducedElement, int node)
    {
        //check precondition
        if (!Graphs.successorListOf(decomposition, node).isEmpty())
            throw new IllegalArgumentException("Node "+node+" of decomposition is not a leaf.");
        if (!decompositionMap.get(node).contains(introducedElement))
            throw new IllegalArgumentException("Node "+node+" of decomposition does not contain "
                                                      +introducedElement+", thus can not be introduced.");

        //add new child
        Set<V> nextVertexBag = new HashSet<>(decompositionMap.get(node));
        nextVertexBag.remove(introducedElement);
        int nextVertex = getNextInteger();
        decomposition.addVertex(nextVertex);
        decomposition.addEdge(node, nextVertex);
        decompositionMap.put(nextVertex, nextVertexBag);

        //return child
        return nextVertex;
    }

    /**
     * Adds to all current leaves in the decomposition introduce nodes until only sets of size 1
     * are leaves.
     */
    protected void leafClosure()
    {
        Set<Integer> vertices = new HashSet<>(decomposition.vertexSet());
        // make leaf nodes
        for (Integer leaf : vertices) {
            // node is not a leaf
            if (Graphs.vertexHasSuccessors(decomposition, leaf))
                continue;

            // otherwise add nodes until one element
            Set<V> vertexSet = decompositionMap.get(leaf);
            Integer current = leaf;
            for (V forget : vertexSet) {
                if(decompositionMap.get(current).size()>1) {
                    current = addIntroduceNode(forget, current);
                }
            }
        }
    }

    /**
     * Returns the tree of the decomposition as an unmodifiable, directed graph
     * 
     * @return the computed tree decomposition graph
     */
    public Graph<Integer, DefaultEdge> getDecomposition()
    {
        if(!hasLazyComputed) {
            computeLazyNiceTreeDecomposition();
            hasLazyComputed = true;
        }
        return new AsUnmodifiableGraph<>(decomposition);
    }

    /**
     * Returns an unmodifiable map from integer nodes of the tree decomposition {@code getDecomposition()} to
     * the sets of the vertices from the graph.
     * 
     * @return the map from nodes of decomposition to sets of vertices map
     */
    public Map<Integer, Set<V>> getMap()
    {
        if(!hasLazyComputed) {
            computeLazyNiceTreeDecomposition();
            hasLazyComputed = true;
        }
        return Collections.unmodifiableMap(decompositionMap);
    }

    /**
     * Get the root of the decomposition computed by {@code getDecomposition()}
     * 
     * @return the root of the decomposition
     */
    public int getRoot()
    {
        if(!hasLazyComputed) {
            computeLazyNiceTreeDecomposition();
            hasLazyComputed = true;
        }
        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        if(!hasLazyComputed) {
            computeLazyNiceTreeDecomposition();
            hasLazyComputed = true;
        }
        return getDecomposition() + "\n " + getMap();
    }
}
