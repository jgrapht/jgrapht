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
/* ------------------
 * AbstractGraph.java
 * ------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *                   Vladimir Kostyukov
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 11-Mar-2004 : Made generic (CH);
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 * 14-Jun-2012 : Added hashCode() and equals() methods implementation (VK);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.util.*;


/**
 * A skeletal implementation of the <tt>Graph</tt> interface, to minimize the
 * effort required to implement graph interfaces. This implementation is
 * applicable to both: directed graphs and undirected graphs.
 *
 * @author Barak Naveh
 * @see Graph
 * @see DirectedGraph
 * @see UndirectedGraph
 */
public abstract class AbstractGraph<V, E>
    implements Graph<V, E>
{
    

    /**
     * Construct a new empty graph object.
     */
    public AbstractGraph()
    {
    }

    

    /**
     * @see Graph#containsEdge(Object, Object)
     */
    public boolean containsEdge(V sourceVertex, V targetVertex)
    {
        return getEdge(sourceVertex, targetVertex) != null;
    }

    /**
     * @see Graph#removeAllEdges(Collection)
     */
    public boolean removeAllEdges(Collection<? extends E> edges)
    {
        boolean modified = false;

        for (E e : edges) {
            modified |= removeEdge(e);
        }

        return modified;
    }

    /**
     * @see Graph#removeAllEdges(Object, Object)
     */
    public Set<E> removeAllEdges(V sourceVertex, V targetVertex)
    {
        Set<E> removed = getAllEdges(sourceVertex, targetVertex);
        if (removed == null) {
            return null;
        }
        removeAllEdges(removed);

        return removed;
    }

    /**
     * @see Graph#removeAllVertices(Collection)
     */
    public boolean removeAllVertices(Collection<? extends V> vertices)
    {
        boolean modified = false;

        for (V v : vertices) {
            modified |= removeVertex(v);
        }

        return modified;
    }

    /**
     * Returns a string of the parenthesized pair (V, E) representing this
     * G=(V,E) graph. 'V' is the string representation of the vertex set, and
     * 'E' is the string representation of the edge set.
     *
     * @return a string representation of this graph.
     */
    public String toString()
    {
        return toStringFromSets(
            vertexSet(),
            edgeSet(),
            (this instanceof DirectedGraph<?, ?>));
    }

    /**
     * Ensures that the specified vertex exists in this graph, or else throws
     * exception.
     *
     * @param v vertex
     *
     * @return <code>true</code> if this assertion holds.
     *
     * @throws NullPointerException if specified vertex is <code>null</code>.
     * @throws IllegalArgumentException if specified vertex does not exist in
     * this graph.
     */
    protected boolean assertVertexExist(V v)
    {
        if (containsVertex(v)) {
            return true;
        } else if (v == null) {
            throw new NullPointerException();
        } else {
            throw new IllegalArgumentException("no such vertex in graph");
        }
    }

    /**
     * Removes all the edges in this graph that are also contained in the
     * specified edge array. After this call returns, this graph will contain no
     * edges in common with the specified edges. This method will invoke the
     * {@link Graph#removeEdge(Object)} method.
     *
     * @param edges edges to be removed from this graph.
     *
     * @return <tt>true</tt> if this graph changed as a result of the call.
     *
     * @see Graph#removeEdge(Object)
     * @see Graph#containsEdge(Object)
     */
    protected boolean removeAllEdges(E [] edges)
    {
        boolean modified = false;

        for (int i = 0; i < edges.length; i++) {
            modified |= removeEdge(edges[i]);
        }

        return modified;
    }

    /**
     * Helper for subclass implementations of toString( ).
     *
     * @param vertexSet the vertex set V to be printed
     * @param edgeSet the edge set E to be printed
     * @param directed true to use parens for each edge (representing directed);
     * false to use curly braces (representing undirected)
     *
     * @return a string representation of (V,E)
     */
    protected String toStringFromSets(
        Collection<? extends V> vertexSet,
        Collection<? extends E> edgeSet,
        boolean directed)
    {
        List<String> renderedEdges = new ArrayList<String>();

        StringBuffer sb = new StringBuffer();
        for (E e : edgeSet) {
            if ((e.getClass() != DefaultEdge.class)
                && (e.getClass() != DefaultWeightedEdge.class))
            {
                sb.append(e.toString());
                sb.append("=");
            }
            if (directed) {
                sb.append("(");
            } else {
                sb.append("{");
            }
            sb.append(getEdgeSource(e));
            sb.append(",");
            sb.append(getEdgeTarget(e));
            if (directed) {
                sb.append(")");
            } else {
                sb.append("}");
            }

            // REVIEW jvs 29-May-2006:  dump weight somewhere?
            renderedEdges.add(sb.toString());
            sb.setLength(0);
        }

        return "(" + vertexSet + ", " + renderedEdges + ")";
    }

    /**
     * Returns a hash code value for this graph. The hash code of a graph is
     * defined to be the sum of the hash codes of vertices and edges in the
     * graph. It is also based on graph topology and edges weights.
     *
     * @return the hash code value this graph
     *
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        int hash = vertexSet().hashCode();

        for (E e : edgeSet()) {
            int part = e.hashCode();

            int source = getEdgeSource(e).hashCode();
            int target = getEdgeTarget(e).hashCode();

            // see http://en.wikipedia.org/wiki/Pairing_function (VK);
            int pairing =
                ((source + target)
                    * (source + target + 1) / 2) + target;
            part = (27 * part) + pairing;

            long weight = (long) getEdgeWeight(e);
            part = (27 * part) + (int) (weight ^ (weight >>> 32));

            hash += part;
        }

        return hash;
    }

    /**
     * Indicates whether some other object is "equal to" this graph. Returns
     * <code>true</code> if the given object is also a graph, the two graphs are
     * instances of the same graph class, have identical vertices and edges sets
     * with the same weights.
     *
     * @param obj object to be compared for equality with this graph
     *
     * @return <code>true</code> if the specified object is equal to this graph
     *
     * @see Object#equals(Object)
     */
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        TypeUtil<Graph<V, E>> typeDecl = null;
        Graph<V, E> g = TypeUtil.uncheckedCast(obj, typeDecl);

        if (!vertexSet().equals(g.vertexSet())) {
            return false;
        }
        if (edgeSet().size() != g.edgeSet().size()) {
            return false;
        }

        for (E e : edgeSet()) {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            if (!g.containsEdge(e)) {
                return false;
            }

            if (!g.getEdgeSource(e).equals(source)
                || !g.getEdgeTarget(e).equals(target))
            {
                return false;
            }

            if (Math.abs(getEdgeWeight(e) - g.getEdgeWeight(e)) > 10e-7) {
                return false;
            }
        }

        return true;
    }
}

// End AbstractGraph.java
