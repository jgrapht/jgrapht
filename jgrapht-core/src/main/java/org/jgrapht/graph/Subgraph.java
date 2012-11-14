/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* -------------
 * Subgraph.java
 * -------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 26-Jul-2003 : Accurate constructors to avoid casting problems (BN);
 * 10-Aug-2003 : Adaptation to new event model (BN);
 * 23-Oct-2003 : Allowed non-listenable graph as base (BN);
 * 07-Feb-2004 : Enabled serialization (BN);
 * 11-Mar-2004 : Made generic (CH);
 * 15-Mar-2004 : Integrity is now checked using Maps (CH);
 * 20-Mar-2004 : Cancelled verification of element identity to base graph (BN);
 * 21-Sep-2004 : Added induced subgraph (who?)
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import com.google.common.collect.Sets;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.event.VertexSetListener;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * A subgraph is a graph that has a subset of vertices and a subset of edges
 * with respect to some base graph. More formally, a subgraph G(V,E) that is
 * based on a base graph Gb(Vb,Eb) satisfies the following <b><i>subgraph
 * property</i></b>: V is a subset of Vb and E is a subset of Eb. Other than
 * this property, a subgraph is a graph with any respect and fully complies with
 * the <code>Graph</code> interface.
 *
 * <p>If the base graph is a {@link ListenableGraph}, the subgraph
 * listens on the base graph and guarantees the subgraph property. If an edge or
 * a vertex is removed from the base graph, it is automatically removed from the
 * subgraph. Subgraph listeners are informed on such removal only if it results
 * in a cascaded removal from the subgraph. If the subgraph has been created as
 * an induced subgraph it also keeps track of edges being added to its vertices.
 * If vertices are added to the base graph, the subgraph remains unaffected.</p>
 *
 * <p>If the base graph is <i>not</i> a ListenableGraph, then the subgraph
 * property cannot be guaranteed. If edges or vertices are removed from the base
 * graph, they are <i>not</i> removed from the subgraph.</p>
 *
 * <p>Modifications to Subgraph are allowed as long as the subgraph property is
 * maintained. Addition of vertices or edges are allowed as long as they also
 * exist in the base graph. Removal of vertices or edges is always allowed. The
 * base graph is <i>never</i> affected by any modification made to the
 * subgraph.</p>
 *
 * <p>A subgraph may provide a "live-window" on a base graph, so that changes
 * made to its vertices or edges are immediately reflected in the base graph,
 * and vice versa. For that to happen, vertices and edges added to the subgraph
 * must be <i>identical</i> (that is, reference-equal and not only value-equal)
 * to their respective ones in the base graph. Previous versions of this class
 * enforced such identity, at a severe performance cost. Currently it is no
 * longer enforced. If you want to achieve a "live-window"functionality, your
 * safest tactics would be to NOT override the <code>equals()</code> methods of
 * your vertices and edges. If you use a class that has already overridden the
 * <code>equals()</code> method, such as <code>String</code>, than you can use a
 * wrapper around it, or else use it directly but exercise a great care to avoid
 * having different-but-equal instances in the subgraph and the base graph.</p>
 *
 * <p>This graph implementation guarantees deterministic vertex and edge set
 * ordering (via {@link LinkedHashSet}).</p>
 *
 * @author Barak Naveh
 * @see Graph
 * @see Set
 * @since Jul 18, 2003
 */
public class Subgraph<V, E, G extends Graph<V, E>>
    extends AbstractGraph<V, E>
    implements Serializable
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = 3208313055169665387L;
    private static final String NO_SUCH_EDGE_IN_BASE =
        "no such edge in base graph";
    private static final String NO_SUCH_VERTEX_IN_BASE =
        "no such vertex in base graph";

    //~ Instance fields --------------------------------------------------------

    //
    Set<E> edgeSet = new LinkedHashSet<E>(); // friendly to improve performance
    Set<V> vertexSet = new LinkedHashSet<V>(); // friendly to improve

    // performance

    //
    private transient Set<E> unmodifiableEdgeSet = null;
    private transient Set<V> unmodifiableVertexSet = null;
    private final G base;
    private boolean isInduced = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Subgraph.
     *
     * @param base the base (backing) graph on which the subgraph will be based.
     * @param vertexSubset vertices to include in the subgraph. If <code>
     * null</code> then all vertices are included.
     * @param edgeSubset edges to in include in the subgraph. If <code>
     * null</code> then all the edges whose vertices found in the graph
     * are included.
     */
    public Subgraph(final G base, final Set<V> vertexSubset, final Set<E> edgeSubset)
    {

        this.base = base;

        if (edgeSubset == null) {
            isInduced = true;
        }

        if (base instanceof ListenableGraph<?, ?>) {
            ((ListenableGraph<V, E>) base).addGraphListener(
                new BaseGraphListener());
        }

        addVerticesUsingFilter(base.vertexSet(), vertexSubset);
        addEdgesUsingFilter(base.edgeSet(), edgeSubset);
    }

    /**
     * Creates a new induced Subgraph. The subgraph will keep track of edges
     * being added to its vertex subset as well as deletion of edges and
     * vertices. If base it not listenable, this is identical to the call
     * Subgraph(base, vertexSubset, null) .
     *
     * @param base the base (backing) graph on which the subgraph will be based.
     * @param vertexSubset vertices to include in the subgraph. If <code>
     * null</code> then all vertices are included.
     */
    public Subgraph(final G base, final Set<V> vertexSubset)
    {
        this(base, vertexSubset, null);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see Graph#getAllEdges(Object, Object)
     */
    @Override
    public Set<E> getAllEdges(final V sourceVertex, final V targetVertex)
    {

        if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
            return null;

        final Set<E> edges
            = Sets.newHashSet(base.getAllEdges(sourceVertex, targetVertex));
        edges.retainAll(edgeSet);

        return edges;
    }

    /**
     * @see Graph#getEdge(Object, Object)
     */
    @Override
    public E getEdge(final V sourceVertex, final V targetVertex)
    {
        final Set<E> edges = getAllEdges(sourceVertex, targetVertex);

        if (edges == null || edges.isEmpty()) {
            return null;
        } else {
            return edges.iterator().next();
        }
    }

    /**
     * @see Graph#getEdgeFactory()
     */
    @Override
    public EdgeFactory<V, E> getEdgeFactory()
    {
        return base.getEdgeFactory();
    }

    /**
     * @see Graph#addEdge(Object, Object)
     */
    @Override
    public E addEdge(final V sourceVertex, final V targetVertex)
    {
        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (!base.containsEdge(sourceVertex, targetVertex)) {
            throw new IllegalArgumentException(NO_SUCH_EDGE_IN_BASE);
        }

        final Set<E> edges = base.getAllEdges(sourceVertex, targetVertex);

        for (final E e : edges) {
            if (!containsEdge(e)) {
                edgeSet.add(e);

                return e;
            }
        }

        return null;
    }

    /**
     * @see Graph#addEdge(Object, Object, Object)
     */
    @Override
    public boolean addEdge(final V sourceVertex, final V targetVertex, final E e)
    {
        if (e == null) {
            throw new NullPointerException();
        }

        if (!base.containsEdge(e)) {
            throw new IllegalArgumentException(NO_SUCH_EDGE_IN_BASE);
        }

        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        assert base.getEdgeSource(e) == sourceVertex;
        assert base.getEdgeTarget(e) == targetVertex;

        if (containsEdge(e)) {
            return false;
        } else {
            edgeSet.add(e);

            return true;
        }
    }

    /**
     * Adds the specified vertex to this subgraph.
     *
     * @param v the vertex to be added.
     *
     * @return <code>true</code> if the vertex was added, otherwise <code>
     * false</code>.
     *
     * @throws NullPointerException
     * @throws IllegalArgumentException
     *
     * @see Subgraph
     * @see Graph#addVertex(Object)
     */
    @Override
    public boolean addVertex(final V v)
    {
        if (v == null) {
            throw new NullPointerException();
        }

        if (!base.containsVertex(v)) {
            throw new IllegalArgumentException(NO_SUCH_VERTEX_IN_BASE);
        }

        if (containsVertex(v)) {
            return false;
        } else {
            vertexSet.add(v);

            return true;
        }
    }

    /**
     * @see Graph#containsEdge(Object)
     */
    @Override
    public boolean containsEdge(final E e)
    {
        return edgeSet.contains(e);
    }

    /**
     * @see Graph#containsVertex(Object)
     */
    @Override
    public boolean containsVertex(final V v)
    {
        return vertexSet.contains(v);
    }

    /**
     * @see Graph#edgeSet()
     */
    @Override
    public Set<E> edgeSet()
    {
        if (unmodifiableEdgeSet == null) {
            unmodifiableEdgeSet = Collections.unmodifiableSet(edgeSet);
        }

        return unmodifiableEdgeSet;
    }

    /**
     * @see Graph#edgesOf(Object)
     */
    @Override
    public Set<E> edgesOf(final V vertex)
    {
        assertVertexExist(vertex);

        final Set<E> baseEdges = base.edgesOf(vertex);
        final Set<E> edges = Sets.newHashSet();

        for (final E e : baseEdges)
            if (containsEdge(e))
                edges.add(e);

        return edges;
    }

    /**
     * @see Graph#removeEdge(Object)
     */
    @Override
    public boolean removeEdge(final E e)
    {
        return edgeSet.remove(e);
    }

    /**
     * @see Graph#removeEdge(Object, Object)
     */
    @Override
    public E removeEdge(final V sourceVertex, final V targetVertex)
    {
        final E e = getEdge(sourceVertex, targetVertex);

        return edgeSet.remove(e) ? e : null;
    }

    /**
     * @see Graph#removeVertex(Object)
     */
    @Override
    public boolean removeVertex(final V v)
    {
        // If the base graph does NOT contain v it means we are here in
        // response to removal of v from the base. In such case we don't need
        // to remove all the edges of v as they were already removed.
        if (containsVertex(v) && base.containsVertex(v)) {
            removeAllEdges(edgesOf(v));
        }

        return vertexSet.remove(v);
    }

    /**
     * @see Graph#vertexSet()
     */
    @Override
    public Set<V> vertexSet()
    {
        if (unmodifiableVertexSet == null) {
            unmodifiableVertexSet = Collections.unmodifiableSet(vertexSet);
        }

        return unmodifiableVertexSet;
    }

    /**
     * @see Graph#getEdgeSource(Object)
     */
    @Override
    public V getEdgeSource(final E e)
    {
        return base.getEdgeSource(e);
    }

    /**
     * @see Graph#getEdgeTarget(Object)
     */
    @Override
    public V getEdgeTarget(final E e)
    {
        return base.getEdgeTarget(e);
    }

    private void addEdgesUsingFilter(final Set<E> edgeSet, final Set<E> filter)
    {
        E e;
        boolean containsVertices;
        boolean edgeIncluded;

        for (Iterator<E> iter = edgeSet.iterator(); iter.hasNext();) {
            e = iter.next();

            final V sourceVertex = base.getEdgeSource(e);
            final V targetVertex = base.getEdgeTarget(e);
            containsVertices =
                containsVertex(sourceVertex)
                && containsVertex(targetVertex);

            // note the use of short circuit evaluation
            edgeIncluded = filter == null || filter.contains(e);

            if (containsVertices && edgeIncluded) {
                addEdge(sourceVertex, targetVertex, e);
            }
        }
    }

    private void addVerticesUsingFilter(final Set<V> vertexSet, final Set<V> filter)
    {
        V v;

        for (final V aVertexSet : vertexSet) {
            v = aVertexSet;

            // note the use of short circuit evaluation
            if (filter == null || filter.contains(v)) {
                addVertex(v);
            }
        }
    }

    public G getBase()
    {
        return base;
    }

    /**
     * @see Graph#getEdgeWeight(Object)
     */
    @Override
    public double getEdgeWeight(final E e)
    {
        return base.getEdgeWeight(e);
    }

    /**
     * @see WeightedGraph#setEdgeWeight(Object, double)
     */
    public void setEdgeWeight(final E e, final double weight)
    {
        ((WeightedGraph<V, E>) base).setEdgeWeight(e, weight);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * An internal listener on the base graph.
     *
     * @author Barak Naveh
     * @since Jul 20, 2003
     */
    private class BaseGraphListener
        implements GraphListener<V, E>,
            Serializable
    {
        private static final long serialVersionUID = 4343535244243546391L;

        /**
         * @see GraphListener#edgeAdded(GraphEdgeChangeEvent)
         */
        @Override
        public void edgeAdded(final GraphEdgeChangeEvent<V, E> e)
        {
            if (isInduced) {
                final E edge = e.getEdge();
                final V source = e.getEdgeSource();
                final V target = e.getEdgeTarget();
                if (containsVertex(source) && containsVertex(target)) {
                    addEdge(
                        source,
                        target,
                        edge);
                }
            }
        }

        /**
         * @see GraphListener#edgeRemoved(GraphEdgeChangeEvent)
         */
        @Override
        public void edgeRemoved(final GraphEdgeChangeEvent<V, E> e)
        {
            final E edge = e.getEdge();

            removeEdge(edge);
        }

        /**
         * @see VertexSetListener#vertexAdded(GraphVertexChangeEvent)
         */
        @Override
        public void vertexAdded(final GraphVertexChangeEvent<V> e)
        {
            // we don't care
        }

        /**
         * @see VertexSetListener#vertexRemoved(GraphVertexChangeEvent)
         */
        @Override
        public void vertexRemoved(final GraphVertexChangeEvent<V> e)
        {
            final V vertex = e.getVertex();

            removeVertex(vertex);
        }
    }
}

// End Subgraph.java
