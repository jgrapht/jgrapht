/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * An implementation of Hierholzer's algorithm for finding an Eulerian cycle in Eulerian graphs. The
 * algorithm works with directed and undirected graphs which may contain loops and/or multiple
 * (parallel) edges. The running time is linear, i.e. $O(|E|)$ where $|E|$ is the cardinality of the
 * edge set of the graph.
 * 
 * <p>
 * See the <a href="https://en.wikipedia.org/wiki/Eulerian_path">Wikipedia article</a> for details
 * and references about Eulerian cycles and a short description of Hierholzer's algorithm for the
 * construction of an Eulerian cycle. The original presentation of the algorithm dates back to 1873
 * and the following paper: Carl Hierholzer: &Uuml;ber die M&ouml;glichkeit, einen Linienzug ohne
 * Wiederholung und ohne Unterbrechung zu umfahren. Mathematische Annalen 6(1), 30–32, 1873.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class HierholzerEulerianCycle<V, E>
    implements
    EulerianCycleAlgorithm<V, E>
{
    /*
     * The input graph.
     */
    protected Graph<V, E> g;
    /*
     * Whether the graph is directed or not.
     */
    protected boolean isDirected;
    /*
     * Non-zero degree vertices list head.
     */
    protected VertexNode verticesHead;
    /*
     * Result edge list head.
     */
    protected EdgeNode eulerianHead;
    /*
     * Result first vertex in the tour.
     */
    protected V startVertex;

    /**
     * Test whether a graph is Eulerian. An
     * <a href="http://mathworld.wolfram.com/EulerianGraph.html">Eulerian graph</a> is a graph
     * containing an <a href="http://mathworld.wolfram.com/EulerianCycle.html">Eulerian cycle</a>.
     * 
     * @param graph the input graph
     * @return true if the graph is Eulerian, false otherwise
     */
    public boolean isEulerian(Graph<V, E> graph)
    {
        GraphTests.requireDirectedOrUndirected(graph);

        if (graph.vertexSet().isEmpty()) {
            // null-graph return false
            return false;
        } else if (graph.edgeSet().isEmpty()) {
            // empty-graph with vertices
            return true;
        } else if (graph.getType().isUndirected()) {
            // check odd degrees
            for (V v : graph.vertexSet()) {
                if (graph.degreeOf(v) % 2 == 1) {
                    return false;
                }
            }
            // check that at most one connected component contains edges
            boolean foundComponentWithEdges = false;
            for (Set<V> component : new ConnectivityInspector<>(graph).connectedSets()) {
                for (V v : component) {
                    if (graph.degreeOf(v) > 0) {
                        if (foundComponentWithEdges) {
                            return false;
                        }
                        foundComponentWithEdges = true;
                        break;
                    }
                }
            }
            return true;
        } else {
            // check same in and out degrees
            for (V v : graph.vertexSet()) {
                if (graph.inDegreeOf(v) != graph.outDegreeOf(v)) {
                    return false;
                }
            }
            // check that at most one strongly connected component contains
            // edges
            boolean foundComponentWithEdges = false;
            for (Set<V> component : new KosarajuStrongConnectivityInspector<>(graph)
                .stronglyConnectedSets())
            {
                for (V v : component) {
                    if (graph.inDegreeOf(v) > 0 || graph.outDegreeOf(v) > 0) {
                        if (foundComponentWithEdges) {
                            return false;
                        }
                        foundComponentWithEdges = true;
                        break;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Compute an Eulerian cycle of a graph.
     * 
     * @param g the input graph
     * @return an Eulerian cycle
     * @throws IllegalArgumentException in case the graph is not Eulerian
     */
    public GraphPath<V, E> getEulerianCycle(Graph<V, E> g)
    {
        if (!isEulerian(g)) {
            throw new IllegalArgumentException("Graph is not Eulerian");
        } else if (g.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("Null graph not permitted");
        } else if (GraphTests.isEmpty(g)) {
            return GraphWalk.emptyWalk(g);
        }

        /*
         * Create doubly-linked lists
         */
        initialize(g);

        /*
         * Main loop
         */
        while (verticesHead != null) {
            /*
             * Record where to insert next partial cycle.
             */
            EdgeNode whereToInsert = verticesHead.insertLocation;

            /*
             * Find partial cycle, while removing used edges.
             */
            Pair<EdgeNode, EdgeNode> partialCycle = computePartialCycle();

            /*
             * Iterate over partial cycle to remove vertices with zero degrees and compute new
             * insert locations for vertices with non-zero degrees. It is important to move vertices
             * with new insert locations to the front of the vertex list, in order to make sure that
             * we always start partial cycles from already visited vertices.
             */
            updateGraphAndInsertLocations(partialCycle, verticesHead);

            /*
             * Insert partial cycle into Eulerian cycle
             */
            if (whereToInsert == null) {
                eulerianHead = partialCycle.getFirst();
            } else {
                partialCycle.getSecond().next = whereToInsert.next;
                whereToInsert.next = partialCycle.getFirst();
            }
        }

        // build final result
        GraphWalk<V, E> walk = buildWalk();

        // cleanup
        cleanup();

        return walk;
    }

    /**
     * Index the graph and create a double-linked list representation suitable for vertex and edge
     * removals in constant time. Ignore any vertices with zero degrees.
     * 
     * @param g the graph to index
     */
    protected void initialize(Graph<V, E> g)
    {
        this.g = g;
        this.isDirected = g.getType().isDirected();
        this.verticesHead = null;
        this.eulerianHead = null;
        this.startVertex = null;

        Map<V, VertexNode> vertices = new HashMap<>();
        for (V v : g.vertexSet()) {
            if (g.outDegreeOf(v) > 0) {
                VertexNode n = new VertexNode(null, v, verticesHead);
                if (verticesHead != null) {
                    verticesHead.prev = n;
                }
                verticesHead = n;
                vertices.put(v, n);
            }
        }

        for (E e : g.edgeSet()) {
            VertexNode sNode = vertices.get(g.getEdgeSource(e));
            VertexNode tNode = vertices.get(g.getEdgeTarget(e));
            addEdge(sNode, tNode, e);
        }
    }

    /**
     * Release any memory held.
     */
    protected void cleanup()
    {
        this.g = null;
        this.verticesHead = null;
        this.eulerianHead = null;
        this.startVertex = null;
    }

    /**
     * Computes a partial cycle assuming that all vertices have an even degree. The partial cycle
     * always begin from the first graph vertex in the vertex list.
     * 
     * @return the partial's cycle head and tail nodes as a pair
     */
    protected Pair<EdgeNode, EdgeNode> computePartialCycle()
    {
        if (startVertex == null) {
            // record global start vertex
            startVertex = verticesHead.v;
        }
        EdgeNode partialHead = null;
        EdgeNode partialTail = null;
        VertexNode v = verticesHead;
        do {
            EdgeNode e = v.adjEdgesHead;
            v = getOppositeVertex(v, e);
            unlink(e);

            if (partialTail == null) {
                partialTail = e;
                partialHead = partialTail;
            } else {
                partialTail.next = e;
                partialTail = partialTail.next;
            }
        } while (!v.equals(verticesHead));
        return Pair.of(partialHead, partialTail);
    }

    /**
     * Iterate over the partial cycle to remove vertices with zero degrees and compute new insert
     * locations for vertices with non-zero degrees. It is important to move vertices with new
     * insert locations to the front of the vertex list, in order to make sure that we always start
     * partial cycles from already visited vertices.
     * 
     * @param partialCycle the partial cycle
     * @param partialCycleSourceVertex the source vertex of the first edge in the partial cycle
     */
    protected void updateGraphAndInsertLocations(
        Pair<EdgeNode, EdgeNode> partialCycle, VertexNode partialCycleSourceVertex)
    {
        EdgeNode e = partialCycle.getFirst();
        assert e != null : "Graph is not Eulerian";
        VertexNode v = getOppositeVertex(partialCycleSourceVertex, e);
        while (true) {
            if (v.adjEdgesHead != null) {
                v.insertLocation = e;
                moveToFront(v);
            } else {
                unlink(v);
            }

            e = e.next;
            if (e == null) {
                break;
            }
            v = getOppositeVertex(v, e);
        }
    }

    /**
     * Build final walk
     * 
     * @return the final walk
     */
    protected GraphWalk<V, E> buildWalk()
    {
        double totalWeight = 0d;
        List<E> result = new ArrayList<>();

        EdgeNode it = eulerianHead;
        while (it != null) {
            result.add(it.e);
            totalWeight += g.getEdgeWeight(it.e);
            it = it.next;
        }
        return new GraphWalk<>(g, startVertex, startVertex, result, totalWeight);
    }

    /**
     * Add an edge to the index.
     *
     * @param sNode source vertex
     * @param tNode target vertex
     * @param e original (wrapped) edge
     */
    protected void addEdge(VertexNode sNode, VertexNode tNode, E e)
    {
        EdgeNode sHead = sNode.adjEdgesHead;
        if (sHead == null) {
            sHead = new EdgeNode(sNode, tNode, null, e, null, null);
        } else {
            EdgeNode n = new EdgeNode(sNode, tNode, null, e, null, sHead);
            sHead.prev = n;
            sHead = n;
        }
        sNode.adjEdgesHead = sHead;

        // if undirected and not a self-loop, add edge to target
        if (!isDirected && !sNode.equals(tNode)) {
            EdgeNode tHead = tNode.adjEdgesHead;
            if (tHead == null) {
                tHead = new EdgeNode(tNode, sNode, null, e, sHead, null);
            } else {
                EdgeNode n = new EdgeNode(tNode, sNode, null, e, sHead, tHead);
                tHead.prev = n;
                tHead = n;
            }
            sHead.reverse = tHead;
            tNode.adjEdgesHead = tHead;
        }
    }

    /**
     * Unlink a vertex from the vertex list.
     *
     * @param vNode vertex to unlink
     */
    protected void unlink(VertexNode vNode)
    {
        if (verticesHead == null) {
            return;
        } else if (!verticesHead.equals(vNode) && vNode.prev == null && vNode.next == null) {
            // does not belong to list
            return;
        } else if (vNode.prev != null) {
            vNode.prev.next = vNode.next;
            if (vNode.next != null) {
                vNode.next.prev = vNode.prev;
            }
        } else {
            verticesHead = vNode.next;
            if (verticesHead != null) {
                verticesHead.prev = null;
            }
        }
        vNode.next = null;
        vNode.prev = null;
    }

    /**
     * Move a vertex as first in the vertex list.
     *
     * @param vNode vertex to move to front
     */
    protected void moveToFront(VertexNode vNode)
    {
        if (vNode.prev != null) {
            vNode.prev.next = vNode.next;
            if (vNode.next != null) {
                vNode.next.prev = vNode.prev;
            }
            verticesHead.prev = vNode;
            vNode.next = verticesHead;
            vNode.prev = null;
            verticesHead = vNode;
        }
    }

    /**
     * Unlink an edge from the adjacency lists of its end-points.
     *
     * @param eNode edge to unlink
     */
    protected void unlink(EdgeNode eNode)
    {
        VertexNode vNode = eNode.sourceNode;

        if (eNode.prev != null) {
            eNode.prev.next = eNode.next;
            if (eNode.next != null) {
                eNode.next.prev = eNode.prev;
            }
        } else {
            if (eNode.next != null) {
                eNode.next.prev = null;
            }
            vNode.adjEdgesHead = eNode.next;
        }

        // remove reverse
        if (!isDirected && eNode.reverse != null) {
            EdgeNode revNode = eNode.reverse;
            VertexNode uNode = revNode.sourceNode;
            if (revNode.prev != null) {
                revNode.prev.next = revNode.next;
                if (revNode.next != null) {
                    revNode.next.prev = revNode.prev;
                }
            } else {
                if (revNode.next != null) {
                    revNode.next.prev = null;
                }
                uNode.adjEdgesHead = revNode.next;
            }
        }

        eNode.next = null;
        eNode.prev = null;
        eNode.reverse = null;
    }

    /**
     * Compute the opposite end-point of an end-point of an edge.
     *
     * @param v vertex that is part of edge
     * @param e edge used to find opposite vertex
     * @return opposite vertex in edge
     */
    protected VertexNode getOppositeVertex(VertexNode v, EdgeNode e)
    {
        return v.equals(e.sourceNode) ? e.targetNode : e.sourceNode;
    }

    /*
     * A list node for the vertices
     */
    protected class VertexNode
    {
        // actual vertex
        public V v;

        // list
        public VertexNode prev;
        public VertexNode next;

        // insert location in global Eulerian list
        public EdgeNode insertLocation;

        // adjacent edges
        public EdgeNode adjEdgesHead;

        /**
         * Create VertexNode
         *
         * @param prev previous vertex
         * @param v original (wrapped) vertex
         * @param next next vertex
         */
        public VertexNode(VertexNode prev, V v, VertexNode next)
        {
            this.prev = prev;
            this.v = v;
            this.next = next;
            this.adjEdgesHead = null;
            this.insertLocation = null;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((v == null) ? 0 : v.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            VertexNode other = TypeUtil.uncheckedCast(obj);
            return Objects.equals(this.v, other.v);
        }

        @Override
        public String toString()
        {
            return v.toString();
        }
    }

    /*
     * A list node for the edges
     */
    protected class EdgeNode
    {
        // the edge
        public E e;

        // list
        public EdgeNode next;
        public EdgeNode prev;

        // reverse if undirected and not a self-loop
        public EdgeNode reverse;

        // source and target
        public VertexNode sourceNode;
        public VertexNode targetNode;

        /**
         * Create EdgeNode
         *
         * @param sourceNode source vertex
         * @param targetNode target vertex
         * @param prev previous edge
         * @param e wrapped (original) edge
         * @param reverse reverse edge
         * @param next next edge
         */
        public EdgeNode(
            VertexNode sourceNode, VertexNode targetNode, EdgeNode prev, E e, EdgeNode reverse,
            EdgeNode next)
        {
            this.sourceNode = sourceNode;
            this.targetNode = targetNode;
            this.prev = prev;
            this.e = e;
            this.reverse = reverse;
            this.next = next;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((e == null) ? 0 : e.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EdgeNode other = TypeUtil.uncheckedCast(obj);
            return Objects.equals(this.e, other.e);
        }

        @Override
        public String toString()
        {
            return e.toString();
        }
    }

}
