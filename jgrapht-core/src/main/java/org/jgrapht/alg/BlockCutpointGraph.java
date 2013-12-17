/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -------------------------
 * BlockCutpointGraph.java
 * -------------------------
 * (C) Copyright 2007-2008, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Jun-2007 : Initial revision (GB);
 * 05-Jul-2007 : Added support for generics (JVS);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * Definition of a <a href="http://mathworld.wolfram.com/Block.html">block of a
 * graph</a> in MathWorld.<br>
 * </br> Definition and lemma taken from the article <a
 * href="http://www.albany.edu/~goel/publications/rosencrantz2005.pdf">
 * Structure-Based Resilience Metrics for Service-Oriented Networks</a>:
 *
 * <ul>
 * <li><b>Definition 4.5</b> Let G(V; E) be a connected undirected graph. The
 * block-cut point graph (BC graph) of G, denoted by GB(VB; EB), is the
 * bipartite graph defined as follows. (a) VB has one node corresponding to each
 * block and one node corresponding to each cut point of G. (b) Each edge fx; yg
 * in EB joins a block node x to a cut point y if the block corresponding to x
 * contains the cut point node corresponding to y.</li>
 * <li><b>Lemma 4.4</b> Let G(V; E) be a connected undirected graph. (a) Each
 * pair of blocks of G share at most one node, and that node is a cutpoint. (b)
 * The BC graph of G is a tree in which each leaf node corresponds to a block of
 * G.</li>
 * </ul>
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
public class BlockCutpointGraph<V, E>
    extends SimpleGraph<UndirectedGraph<V, E>, DefaultEdge>
{
    

    /**
     */
    private static final long serialVersionUID = -9101341117013163934L;

    

    private Set<V> cutpoints = new HashSet<V>();

    /**
     * DFS (Depth-First-Search) tree.
     */
    private DirectedGraph<V, DefaultEdge> dfsTree;

    private UndirectedGraph<V, E> graph;

    private int numOrder;

    private Deque<BCGEdge> stack = new ArrayDeque<BCGEdge>();

    private Map<V, Set<UndirectedGraph<V, E>>> vertex2biconnectedSubgraphs =
        new HashMap<V, Set<UndirectedGraph<V, E>>>();

    private Map<V, UndirectedGraph<V, E>> vertex2block =
        new HashMap<V, UndirectedGraph<V, E>>();

    private Map<V, Integer> vertex2numOrder = new HashMap<V, Integer>();

    

    /**
     * Running time = O(m) where m is the number of edges.
     */
    public BlockCutpointGraph(UndirectedGraph<V, E> graph)
    {
        super(DefaultEdge.class);
        this.graph = graph;

        this.dfsTree =
            new SimpleDirectedGraph<V, DefaultEdge>(
                DefaultEdge.class);
        V s = graph.vertexSet().iterator().next();
        this.dfsTree.addVertex(s);
        dfsVisit(s, s);

        if (this.dfsTree.edgesOf(s).size() > 1) {
            this.cutpoints.add(s);
        } else {
            this.cutpoints.remove(s);
        }

        for (Iterator<V> iter = this.cutpoints.iterator(); iter.hasNext();) {
            V cutpoint = iter.next();
            UndirectedGraph<V, E> subgraph =
                new SimpleGraph<V, E>(this.graph.getEdgeFactory());
            subgraph.addVertex(cutpoint);
            this.vertex2block.put(cutpoint, subgraph);
            addVertex(subgraph);
            Set<UndirectedGraph<V, E>> biconnectedSubgraphs =
                getBiconnectedSubgraphs(cutpoint);
            for (
                Iterator<UndirectedGraph<V, E>> iterator =
                    biconnectedSubgraphs.iterator();
                iterator.hasNext();)
            {
                UndirectedGraph<V, E> biconnectedSubgraph = iterator.next();
                assert (vertexSet().contains(biconnectedSubgraph));
                addEdge(subgraph, biconnectedSubgraph);
            }
        }
    }

    

    /**
     * Returns the vertex if vertex is a cutpoint, and otherwise returns the
     * block (biconnected component) containing the vertex.
     *
     * @param vertex vertex in the initial graph.
     */
    public UndirectedGraph<V, E> getBlock(V vertex)
    {
        if (!this.graph.vertexSet().contains(vertex)) {
            throw new IllegalArgumentException("No such vertex in the graph!");
        }

        return this.vertex2block.get(vertex);
    }

    /**
     * Returns the cutpoints of the initial graph.
     */
    public Set<V> getCutpoints()
    {
        return this.cutpoints;
    }

    /**
     * Returns <code>true</code> if the vertex is a cutpoint, <code>false</code>
     * otherwise.
     *
     * @param vertex vertex in the initial graph.
     */
    public boolean isCutpoint(V vertex)
    {
        if (!this.graph.vertexSet().contains(vertex)) {
            throw new IllegalArgumentException("No such vertex in the graph!");
        }

        return this.cutpoints.contains(vertex);
    }

    private void biconnectedComponentFinished(V s, V n)
    {
        this.cutpoints.add(s);

        Set<V> vertexComponent = new HashSet<V>();
        Set<BCGEdge> edgeComponent = new HashSet<BCGEdge>();
        BCGEdge edge = this.stack.removeLast();
        while (
            (getNumOrder(edge.getSource()) >= getNumOrder(n))
            && !this.stack.isEmpty())
        {
            edgeComponent.add(edge);

            vertexComponent.add(edge.getSource());
            vertexComponent.add(edge.getTarget());

            edge = this.stack.removeLast();
        }
        edgeComponent.add(edge);
        // edgeComponent is an equivalence class.

        vertexComponent.add(edge.getSource());
        vertexComponent.add(edge.getTarget());

        VertexComponentForbiddenFunction mask =
            new VertexComponentForbiddenFunction(
                vertexComponent);
        UndirectedGraph<V, E> biconnectedSubgraph =
            new UndirectedMaskSubgraph<V, E>(
                this.graph,
                mask);
        for (Iterator<V> iter = vertexComponent.iterator(); iter.hasNext();) {
            V vertex = iter.next();
            this.vertex2block.put(vertex, biconnectedSubgraph);
            getBiconnectedSubgraphs(vertex).add(biconnectedSubgraph);
        }
        addVertex(biconnectedSubgraph);
    }

    private int dfsVisit(V s, V father)
    {
        this.numOrder++;
        int minS = this.numOrder;
        setNumOrder(s, this.numOrder);

        for (
            Iterator<E> iter = this.graph.edgesOf(s).iterator();
            iter.hasNext();)
        {
            E edge = iter.next();
            V n = Graphs.getOppositeVertex(this.graph, edge, s);
            if (getNumOrder(n) == 0) {
                this.dfsTree.addVertex(n);
                BCGEdge dfsEdge = new BCGEdge(s, n);
                this.dfsTree.addEdge(s, n, dfsEdge);

                this.stack.add(dfsEdge);

                // minimum of the traverse orders of the "attach points" of
                // the vertex n.
                int minN = dfsVisit(n, s);
                minS = Math.min(minN, minS);
                if (minN >= getNumOrder(s)) {
                    // s is a cutpoint.
                    // it has a son whose "attach depth" is greater or equal.
                    biconnectedComponentFinished(s, n);
                }
            } else if ((getNumOrder(n) < getNumOrder(s)) && !n.equals(father)) {
                BCGEdge backwardEdge = new BCGEdge(s, n);
                this.stack.add(backwardEdge);

                // n is an "attach point" of s. {s->n} is a backward edge.
                minS = Math.min(getNumOrder(n), minS);
            }
        }

        // minimum of the traverse orders of the "attach points" of
        // the vertex s.
        return minS;
    }

    /**
     * Returns the biconnected components containing the vertex. A vertex which
     * is not a cutpoint is contained in exactly one component. A cutpoint is
     * contained is at least 2 components.
     *
     * @param vertex vertex in the initial graph.
     */
    private Set<UndirectedGraph<V, E>> getBiconnectedSubgraphs(V vertex)
    {
        Set<UndirectedGraph<V, E>> biconnectedSubgraphs =
            this.vertex2biconnectedSubgraphs.get(vertex);
        if (biconnectedSubgraphs == null) {
            biconnectedSubgraphs = new HashSet<UndirectedGraph<V, E>>();
            this.vertex2biconnectedSubgraphs.put(vertex, biconnectedSubgraphs);
        }
        return biconnectedSubgraphs;
    }

    /**
     * Returns the traverse order of the vertex in the DFS.
     */
    private int getNumOrder(V vertex)
    {
        assert (vertex != null);

        Integer numOrder = this.vertex2numOrder.get(vertex);
        if (numOrder == null) {
            return 0;
        } else {
            return numOrder.intValue();
        }
    }

    private void setNumOrder(V vertex, int numOrder)
    {
        this.vertex2numOrder.put(vertex, Integer.valueOf(numOrder));
    }

    

    private class BCGEdge
        extends DefaultEdge
    {
        /**
         */
        private static final long serialVersionUID = -5115006161815760059L;

        private V source;

        private V target;

        public BCGEdge(V source, V target)
        {
            super();
            this.source = source;
            this.target = target;
        }

        public V getSource()
        {
            return this.source;
        }

        public V getTarget()
        {
            return this.target;
        }
    }

    private class VertexComponentForbiddenFunction
        implements MaskFunctor<V, E>
    {
        private Set<V> vertexComponent;

        public VertexComponentForbiddenFunction(Set<V> vertexComponent)
        {
            this.vertexComponent = vertexComponent;
        }

        public boolean isEdgeMasked(E edge)
        {
            return false;
        }

        public boolean isVertexMasked(V vertex)
        {
            if (this.vertexComponent.contains(vertex)) {
                // vertex belongs to component then we do not mask it.
                return false;
            } else {
                return true;
            }
        }
    }
}

// End BlockCutpointGraph.java
