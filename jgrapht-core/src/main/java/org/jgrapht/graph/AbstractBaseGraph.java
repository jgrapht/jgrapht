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
/* ----------------------
 * AbstractBaseGraph.java
 * ----------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   John V. Sichi
 *                   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 10-Aug-2003 : General edge refactoring (BN);
 * 06-Nov-2003 : Change edge sharing semantics (JVS);
 * 07-Feb-2004 : Enabled serialization (BN);
 * 11-Mar-2004 : Made generic (CH);
 * 01-Jun-2005 : Added EdgeListFactory (JVS);
 * 07-May-2006 : Changed from List<Edge> to Set<Edge> (JVS);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import java.io.*;

import java.util.*;

import com.google.common.base.Preconditions;
import org.jgrapht.*;
import org.jgrapht.util.*;


/**
 * The most general implementation of the {@link org.jgrapht.Graph} interface.
 * Its subclasses add various restrictions to get more specific graphs. The
 * decision whether it is directed or undirected is decided at construction time
 * and cannot be later modified (see constructor for details).
 *
 * <p>This graph implementation guarantees deterministic vertex and edge set
 * ordering (via {@link LinkedHashMap} and {@link LinkedHashSet}).</p>
 *
 * @author Barak Naveh
 * @since Jul 24, 2003
 */
public abstract class AbstractBaseGraph<V, E>
    extends AbstractGraph<V, E>
    implements Cloneable, Serializable
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = -1263088497616142427L;

    private static final String LOOPS_NOT_ALLOWED = "loops not allowed";

    //~ Instance fields --------------------------------------------------------

    boolean allowingLoops;

    private EdgeFactory<V, E> edgeFactory;
    private EdgeSetFactory<V, E> edgeSetFactory;
    private Map<E, IntrusiveEdge> edgeMap;
    private transient Set<E> unmodifiableEdgeSet = null;
    private transient Set<V> unmodifiableVertexSet = null;
    private Specifics specifics;
    private final boolean allowingMultipleEdges;

    private final transient TypeUtil<V> vertexTypeDecl = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Construct a new pseudograph. The pseudograph can either be directed or
     * undirected, depending on the specified edge factory.
     *
     * @param ef the edge factory of the new graph.
     * @param allowMultipleEdges whether to allow multiple edges or not.
     * @param allowLoops whether to allow edges that are self-loops or not.
     *
     * @throws NullPointerException if the specified edge factory is <code>
     * null</code>.
     */
    protected AbstractBaseGraph(final EdgeFactory<V, E> ef,
        final boolean allowMultipleEdges, final boolean allowLoops)
    {
        edgeFactory = Preconditions.checkNotNull(ef);

        edgeMap = new LinkedHashMap<E, IntrusiveEdge>();
        allowingLoops = allowLoops;
        allowingMultipleEdges = allowMultipleEdges;

        specifics = createSpecifics();

        edgeSetFactory = new ArrayListFactory<V, E>();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see Graph#getAllEdges(Object, Object)
     */
    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex)
    {
        return specifics.getAllEdges(sourceVertex, targetVertex);
    }

    /**
     * Returns <code>true</code> if and only if self-loops are allowed in this
     * graph. A self loop is an edge that its source and target vertices are the
     * same.
     *
     * @return <code>true</code> if and only if graph loops are allowed.
     */
    public final boolean isAllowingLoops()
    {
        return allowingLoops;
    }

    /**
     * Returns <code>true</code> if and only if multiple edges are allowed in
     * this graph. The meaning of multiple edges is that there can be many edges
     * going from vertex v1 to vertex v2.
     *
     * @return <code>true</code> if and only if multiple edges are allowed.
     */
    public final boolean isAllowingMultipleEdges()
    {
        return allowingMultipleEdges;
    }

    /**
     * @see Graph#getEdge(Object, Object)
     */
    @Override
    public E getEdge(V sourceVertex, V targetVertex)
    {
        return specifics.getEdge(sourceVertex, targetVertex);
    }

    /**
     * @see Graph#getEdgeFactory()
     */
    @Override
    public EdgeFactory<V, E> getEdgeFactory()
    {
        return edgeFactory;
    }

    /**
     * Set the {@link EdgeSetFactory} to use for this graph. Initially, a graph
     * is created with a default implementation which always supplies an {@link
     * java.util.ArrayList} with capacity 1.
     *
     * @param edgeSetFactory factory to use for subsequently created edge sets
     * (this call has no effect on existing edge sets)
     */
    public void setEdgeSetFactory(EdgeSetFactory<V, E> edgeSetFactory)
    {
        this.edgeSetFactory = edgeSetFactory;
    }

    /**
     * @see Graph#addEdge(Object, Object)
     */
    @Override
    public E addEdge(V sourceVertex, V targetVertex)
    {
        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (!allowingMultipleEdges && containsEdge(sourceVertex, targetVertex))
            return null;

        if (!allowingLoops && sourceVertex.equals(targetVertex))
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);

        E e = edgeFactory.createEdge(sourceVertex, targetVertex);

        if (containsEdge(e)) // this restriction should stay!
            return null;

        IntrusiveEdge intrusiveEdge = createIntrusiveEdge(e, sourceVertex,
            targetVertex);

        edgeMap.put(e, intrusiveEdge);
        specifics.addEdgeToTouchingVertices(e);

        return e;
    }

    /**
     * @see Graph#addEdge(Object, Object, Object)
     */
    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        Preconditions.checkNotNull(e);

        if (containsEdge(e))
            return false;

        assertVertexExist(sourceVertex);
        assertVertexExist(targetVertex);

        if (!allowingMultipleEdges && containsEdge(sourceVertex, targetVertex))
            return false;

        if (!allowingLoops && sourceVertex.equals(targetVertex))
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);

        IntrusiveEdge intrusiveEdge = createIntrusiveEdge(e, sourceVertex,
            targetVertex);

        edgeMap.put(e, intrusiveEdge);
        specifics.addEdgeToTouchingVertices(e);

        return true;
    }

    private IntrusiveEdge createIntrusiveEdge(E e, V sourceVertex,
        V targetVertex)
    {
        final IntrusiveEdge intrusiveEdge = e instanceof  IntrusiveEdge
            ? (IntrusiveEdge) e
            : new IntrusiveEdge();

        intrusiveEdge.source = sourceVertex;
        intrusiveEdge.target = targetVertex;
        return intrusiveEdge;
    }

    /**
     * @see Graph#addVertex(Object)
     */
    @Override
    public boolean addVertex(V v)
    {
        Preconditions.checkNotNull(v);

        if (containsVertex(v))
            return false;

        specifics.addVertex(v);
        return true;
    }

    /**
     * @see Graph#getEdgeSource(Object)
     */
    @Override
    public V getEdgeSource(E e)
    {
        return TypeUtil.uncheckedCast(getIntrusiveEdge(e).source,
            vertexTypeDecl);
    }

    /**
     * @see Graph#getEdgeTarget(Object)
     */
    @Override
    public V getEdgeTarget(E e)
    {
        return TypeUtil.uncheckedCast(getIntrusiveEdge(e).target,
            vertexTypeDecl);
    }

    private IntrusiveEdge getIntrusiveEdge(E e)
    {
        return e instanceof IntrusiveEdge
            ? (IntrusiveEdge) e
            : edgeMap.get(e);
    }

    /**
     * Returns a shallow copy of this graph instance. Neither edges nor vertices
     * are cloned.
     *
     * @return a shallow copy of this set.
     *
     * @throws RuntimeException
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone()
    {
        try {
            TypeUtil<AbstractBaseGraph<V, E>> typeDecl = null;

            AbstractBaseGraph<V, E> newGraph =
                TypeUtil.uncheckedCast(super.clone(), typeDecl);

            newGraph.edgeMap = new LinkedHashMap<E, IntrusiveEdge>();

            newGraph.edgeFactory = edgeFactory;
            newGraph.unmodifiableEdgeSet = null;
            newGraph.unmodifiableVertexSet = null;

            // NOTE:  it's important for this to happen in an object
            // method so that the new inner class instance gets associated with
            // the right outer class instance
            newGraph.specifics = newGraph.createSpecifics();

            Graphs.addGraph(newGraph, this);

            return newGraph;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * @see Graph#containsEdge(Object)
     */
    @Override
    public boolean containsEdge(E e)
    {
        return edgeMap.containsKey(e);
    }

    /**
     * @see Graph#containsVertex(Object)
     */
    @Override
    public boolean containsVertex(V v)
    {
        return specifics.getVertexSet().contains(v);
    }

    /**
     * @see UndirectedGraph#degreeOf(Object)
     */
    public int degreeOf(V vertex)
    {
        return specifics.degreeOf(vertex);
    }

    /**
     * @see Graph#edgeSet()
     */
    @Override
    public Set<E> edgeSet()
    {
        if (unmodifiableEdgeSet == null)
            unmodifiableEdgeSet = Collections.unmodifiableSet(edgeMap.keySet());

        return unmodifiableEdgeSet;
    }

    /**
     * @see Graph#edgesOf(Object)
     */
    @Override
    public Set<E> edgesOf(V vertex)
    {
        return specifics.edgesOf(vertex);
    }

    /**
     * @see DirectedGraph#inDegreeOf(Object)
     */
    public int inDegreeOf(V vertex)
    {
        return specifics.inDegreeOf(vertex);
    }

    /**
     * @see DirectedGraph#incomingEdgesOf(Object)
     */
    public Set<E> incomingEdgesOf(V vertex)
    {
        return specifics.incomingEdgesOf(vertex);
    }

    /**
     * @see DirectedGraph#outDegreeOf(Object)
     */
    public int outDegreeOf(V vertex)
    {
        return specifics.outDegreeOf(vertex);
    }

    /**
     * @see DirectedGraph#outgoingEdgesOf(Object)
     */
    public Set<E> outgoingEdgesOf(V vertex)
    {
        return specifics.outgoingEdgesOf(vertex);
    }

    /**
     * @see Graph#removeEdge(Object, Object)
     */
    @Override
    public E removeEdge(V sourceVertex, V targetVertex)
    {
        E e = getEdge(sourceVertex, targetVertex);

        if (e != null) {
            specifics.removeEdgeFromTouchingVertices(e);
            edgeMap.remove(e);
        }

        return e;
    }

    /**
     * @see Graph#removeEdge(Object)
     */
    @Override
    public boolean removeEdge(E e)
    {
        if (!containsEdge(e))
            return false;

        specifics.removeEdgeFromTouchingVertices(e);
        edgeMap.remove(e);

        return true;
    }

    /**
     * @see Graph#removeVertex(Object)
     */
    @Override
    public boolean removeVertex(V v)
    {
        if (!containsVertex(v))
            return false;

        Set<E> touchingEdgesList = edgesOf(v);

        // cannot iterate over list - will cause
        // ConcurrentModificationException
        removeAllEdges(new ArrayList<E>(touchingEdgesList));

        specifics.getVertexSet().remove(v); // remove the vertex itself

        return true;
    }

    /**
     * @see Graph#vertexSet()
     */
    @Override
    public Set<V> vertexSet()
    {
        if (unmodifiableVertexSet == null)
            unmodifiableVertexSet =
                Collections.unmodifiableSet(specifics.getVertexSet());

        return unmodifiableVertexSet;
    }

    /**
     * @see Graph#getEdgeWeight(Object)
     */
    @Override
    public double getEdgeWeight(E e)
    {
        return e instanceof DefaultWeightedEdge
            ? ((DefaultWeightedEdge) e).getWeight()
            : WeightedGraph.DEFAULT_EDGE_WEIGHT;
    }

    /**
     * @see WeightedGraph#setEdgeWeight(Object, double)
     */
    public void setEdgeWeight(E e, double weight)
    {
        assert (e instanceof DefaultWeightedEdge) : e.getClass();
        ((DefaultWeightedEdge) e).weight = weight;
    }

    private Specifics createSpecifics()
    {
        if (this instanceof DirectedGraph<?, ?>)
            return new DirectedSpecifics();
        if (this instanceof UndirectedGraph<?, ?>)
            return new UndirectedSpecifics();
        throw new IllegalArgumentException(
            "must be instance of either DirectedGraph or UndirectedGraph");
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * .
     *
     * @author Barak Naveh
     */
    private abstract class Specifics
        implements Serializable
    {
        private static final long serialVersionUID = 785196247314761183L;

        public abstract void addVertex(V vertex);

        public abstract Set<V> getVertexSet();

        /**
         * .
         *
         * @param sourceVertex
         * @param targetVertex
         *
         * @return
         */
        public abstract Set<E> getAllEdges(V sourceVertex, V targetVertex);

        /**
         * .
         *
         * @param sourceVertex
         * @param targetVertex
         *
         * @return
         */
        public abstract E getEdge(V sourceVertex, V targetVertex);

        /**
         * Adds the specified edge to the edge containers of its source and
         * target vertices.
         *
         * @param e
         */
        public abstract void addEdgeToTouchingVertices(E e);

        /**
         * .
         *
         * @param vertex
         *
         * @return
         */
        public abstract int degreeOf(V vertex);

        /**
         * .
         *
         * @param vertex
         *
         * @return
         */
        public abstract Set<E> edgesOf(V vertex);

        /**
         * .
         *
         * @param vertex
         *
         * @return
         */
        public abstract int inDegreeOf(V vertex);

        /**
         * .
         *
         * @param vertex
         *
         * @return
         */
        public abstract Set<E> incomingEdgesOf(V vertex);

        /**
         * .
         *
         * @param vertex
         *
         * @return
         */
        public abstract int outDegreeOf(V vertex);

        /**
         * .
         *
         * @param vertex
         *
         * @return
         */
        public abstract Set<E> outgoingEdgesOf(V vertex);

        /**
         * Removes the specified edge from the edge containers of its source and
         * target vertices.
         *
         * @param e
         */
        public abstract void removeEdgeFromTouchingVertices(E e);
    }

    private static class ArrayListFactory<VV, EE>
        implements EdgeSetFactory<VV, EE>,
            Serializable
    {
        private static final long serialVersionUID = 5936902837403445985L;

        /**
         * @see EdgeSetFactory.createEdgeSet
         */
        @Override
        public Set<EE> createEdgeSet(VV vertex)
        {
            // NOTE:  use size 1 to keep memory usage under control
            // for the common case of vertices with low degree
            return new ArrayUnenforcedSet<EE>(1);
        }
    }

    /**
     * A container for vertex edges.
     *
     * <p>In this edge container we use array lists to minimize memory toll.
     * However, for high-degree vertices we replace the entire edge container
     * with a direct access subclass (to be implemented).</p>
     *
     * @author Barak Naveh
     */
    private static class DirectedEdgeContainer<VV, EE>
        implements Serializable
    {
        private static final long serialVersionUID = 7494242245729767106L;
        Set<EE> incoming;
        Set<EE> outgoing;
        private transient Set<EE> unmodifiableIncoming = null;
        private transient Set<EE> unmodifiableOutgoing = null;

        DirectedEdgeContainer(EdgeSetFactory<VV, EE> edgeSetFactory, VV vertex)
        {
            incoming = edgeSetFactory.createEdgeSet(vertex);
            outgoing = edgeSetFactory.createEdgeSet(vertex);
        }

        /**
         * A lazy build of unmodifiable incoming edge set.
         *
         * @return
         */
        public Set<EE> getUnmodifiableIncomingEdges()
        {
            if (unmodifiableIncoming == null)
                unmodifiableIncoming = Collections.unmodifiableSet(incoming);

            return unmodifiableIncoming;
        }

        /**
         * A lazy build of unmodifiable outgoing edge set.
         *
         * @return
         */
        public Set<EE> getUnmodifiableOutgoingEdges()
        {
            if (unmodifiableOutgoing == null)
                unmodifiableOutgoing = Collections.unmodifiableSet(outgoing);

            return unmodifiableOutgoing;
        }

        /**
         * .
         *
         * @param e
         */
        public void addIncomingEdge(EE e)
        {
            incoming.add(e);
        }

        /**
         * .
         *
         * @param e
         */
        public void addOutgoingEdge(EE e)
        {
            outgoing.add(e);
        }

        /**
         * .
         *
         * @param e
         */
        public void removeIncomingEdge(EE e)
        {
            incoming.remove(e);
        }

        /**
         * .
         *
         * @param e
         */
        public void removeOutgoingEdge(EE e)
        {
            outgoing.remove(e);
        }
    }

    /**
     * .
     *
     * @author Barak Naveh
     */
    private class DirectedSpecifics
        extends Specifics
    {
        private static final long serialVersionUID = 8971725103718958232L;
        private static final String NOT_IN_DIRECTED_GRAPH =
            "no such operation in a directed graph";

        private final Map<V, DirectedEdgeContainer<V, E>> vertexMapDirected =
            new LinkedHashMap<V, DirectedEdgeContainer<V, E>>();

        @Override
        public void addVertex(V v)
        {
            // add with a lazy edge container entry
            vertexMapDirected.put(v, null);
        }

        @Override
        public Set<V> getVertexSet()
        {
            return vertexMapDirected.keySet();
        }

        /**
         * @see Graph#getAllEdges(Object, Object)
         */
        @Override
        public Set<E> getAllEdges(V sourceVertex, V targetVertex)
        {

            if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
                return null;

            final Set<E> edges = new ArrayUnenforcedSet<E>();

            DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);

            for (E e : ec.outgoing)
                if (getEdgeTarget(e).equals(targetVertex))
                    edges.add(e);

            return edges;
        }

        /**
         * @see Graph#getEdge(Object, Object)
         */
        @Override
        public E getEdge(V sourceVertex, V targetVertex)
        {
            if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
                return null;

            DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);

            for (E e : ec.outgoing)
                if (getEdgeTarget(e).equals(targetVertex))
                    return e;

            return null;
        }

        /**
         * @see AbstractBaseGraph#addEdgeToTouchingVertices(Edge)
         */
        @Override
        public void addEdgeToTouchingVertices(E e)
        {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            getEdgeContainer(source).addOutgoingEdge(e);
            getEdgeContainer(target).addIncomingEdge(e);
        }

        /**
         * @see UndirectedGraph#degreeOf(Object)
         */
        @Override
        public int degreeOf(V vertex)
        {
            throw new UnsupportedOperationException(NOT_IN_DIRECTED_GRAPH);
        }

        /**
         * @see Graph#edgesOf(Object)
         */
        @Override
        public Set<E> edgesOf(V vertex)
        {
            final DirectedEdgeContainer<V, E> container
                = getEdgeContainer(vertex);
            ArrayUnenforcedSet<E> inAndOut =
                new ArrayUnenforcedSet<E>(container.incoming);
            inAndOut.addAll(container.outgoing);

            // we have two copies for each self-loop - remove one of them.
            if (allowingLoops) {
                Set<E> loops = getAllEdges(vertex, vertex);

                for (int i = 0; i < inAndOut.size();) {
                    E e = inAndOut.get(i);

                    if (loops.remove(e))
                        inAndOut.remove(i);
                    else
                        i++;
                }
            }

            return Collections.unmodifiableSet(inAndOut);
        }

        /**
         * @see DirectedGraph#inDegree(Object)
         */
        @Override
        public int inDegreeOf(V vertex)
        {
            return getEdgeContainer(vertex).incoming.size();
        }

        /**
         * @see DirectedGraph#incomingEdges(Object)
         */
        @Override
        public Set<E> incomingEdgesOf(V vertex)
        {
            return getEdgeContainer(vertex).getUnmodifiableIncomingEdges();
        }

        /**
         * @see DirectedGraph#outDegree(Object)
         */
        @Override
        public int outDegreeOf(V vertex)
        {
            return getEdgeContainer(vertex).outgoing.size();
        }

        /**
         * @see DirectedGraph#outgoingEdges(Object)
         */
        @Override
        public Set<E> outgoingEdgesOf(V vertex)
        {
            return getEdgeContainer(vertex).getUnmodifiableOutgoingEdges();
        }

        /**
         * @see AbstractBaseGraph#removeEdgeFromTouchingVertices(Edge)
         */
        @Override
        public void removeEdgeFromTouchingVertices(E e)
        {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            getEdgeContainer(source).removeOutgoingEdge(e);
            getEdgeContainer(target).removeIncomingEdge(e);
        }

        /**
         * A lazy build of edge container for specified vertex.
         *
         * @param vertex a vertex in this graph.
         *
         * @return EdgeContainer
         */
        private DirectedEdgeContainer<V, E> getEdgeContainer(V vertex)
        {
            assertVertexExist(vertex);

            DirectedEdgeContainer<V, E> ec = vertexMapDirected.get(vertex);

            if (ec == null) {
                ec = new DirectedEdgeContainer<V, E>(edgeSetFactory, vertex);
                vertexMapDirected.put(vertex, ec);
            }

            return ec;
        }
    }

    /**
     * A container of for vertex edges.
     *
     * <p>In this edge container we use array lists to minimize memory toll.
     * However, for high-degree vertices we replace the entire edge container
     * with a direct access subclass (to be implemented).</p>
     *
     * @author Barak Naveh
     */
    private static class UndirectedEdgeContainer<VV, EE>
        implements Serializable
    {
        private static final long serialVersionUID = -6623207588411170010L;
        Set<EE> vertexEdges;
        private transient Set<EE> unmodifiableVertexEdges = null;

        UndirectedEdgeContainer(
            EdgeSetFactory<VV, EE> edgeSetFactory,
            VV vertex)
        {
            vertexEdges = edgeSetFactory.createEdgeSet(vertex);
        }

        /**
         * A lazy build of unmodifiable list of vertex edges
         *
         * @return
         */
        public Set<EE> getUnmodifiableVertexEdges()
        {
            if (unmodifiableVertexEdges == null)
                unmodifiableVertexEdges =
                    Collections.unmodifiableSet(vertexEdges);

            return unmodifiableVertexEdges;
        }

        /**
         * .
         *
         * @param e
         */
        public void addEdge(EE e)
        {
            vertexEdges.add(e);
        }

        /**
         * .
         *
         * @return
         */
        public int edgeCount()
        {
            return vertexEdges.size();
        }

        /**
         * .
         *
         * @param e
         */
        public void removeEdge(EE e)
        {
            vertexEdges.remove(e);
        }
    }

    /**
     * .
     *
     * @author Barak Naveh
     */
    private class UndirectedSpecifics
        extends Specifics
        implements Serializable
    {
        private static final long serialVersionUID = 6494588405178655873L;
        private static final String NOT_IN_UNDIRECTED_GRAPH =
            "no such operation in an undirected graph";

        private Map<V, UndirectedEdgeContainer<V, E>> vertexMapUndirected =
            new LinkedHashMap<V, UndirectedEdgeContainer<V, E>>();

        @Override
        public void addVertex(V v)
        {
            // add with a lazy edge container entry
            vertexMapUndirected.put(v, null);
        }

        @Override
        public Set<V> getVertexSet()
        {
            return vertexMapUndirected.keySet();
        }

        /**
         * @see Graph#getAllEdges(Object, Object)
         */
        @Override
        public Set<E> getAllEdges(V sourceVertex, V targetVertex)
        {
            if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
                return null;

            final Set<E> edges = new ArrayUnenforcedSet<E>();

            boolean equalStraight;
            boolean equalInverted;

            for (E e : getEdgeContainer(sourceVertex).vertexEdges) {
                equalStraight = sourceVertex.equals(getEdgeSource(e))
                    && targetVertex.equals(getEdgeTarget(e));

                equalInverted = sourceVertex.equals(getEdgeTarget(e))
                    && targetVertex.equals(getEdgeSource(e));

                if (equalStraight || equalInverted)
                    edges.add(e);
            }

            return edges;
        }

        /**
         * @see Graph#getEdge(Object, Object)
         */
        @Override
        public E getEdge(V sourceVertex, V targetVertex)
        {
            if (!containsVertex(sourceVertex) || !containsVertex(targetVertex))
                return null;

            boolean equalStraight;
            boolean equalInverted;

            for (E e : getEdgeContainer(sourceVertex).vertexEdges) {
                equalStraight = sourceVertex.equals(getEdgeSource(e))
                    && targetVertex.equals(getEdgeTarget(e));

                equalInverted = sourceVertex.equals(getEdgeTarget(e))
                    && targetVertex.equals(getEdgeSource(e));

                if (equalStraight || equalInverted)
                    return e;
            }

            return null;
        }

        /**
         * @see AbstractBaseGraph#addEdgeToTouchingVertices(Edge)
         */
        @Override
        public void addEdgeToTouchingVertices(E e)
        {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            getEdgeContainer(source).addEdge(e);

            if (!source.equals(target))
                getEdgeContainer(target).addEdge(e);
        }

        /**
         * @see UndirectedGraph#degreeOf(V)
         */
        @Override
        public int degreeOf(V vertex)
        {
            if (!allowingLoops)
                return getEdgeContainer(vertex).edgeCount();

            // then we must count, and add loops twice

            final Set<E> edges = getEdgeContainer(vertex).vertexEdges;
            int size = edges.size();

            for (final E e: edges)
                if (getEdgeSource(e).equals(getEdgeTarget(e)))
                    size++;

            return size;
        }

        /**
         * @see Graph#edgesOf(V)
         */
        @Override
        public Set<E> edgesOf(V vertex)
        {
            return getEdgeContainer(vertex).getUnmodifiableVertexEdges();
        }

        /**
         * @see DirectedGraph#inDegreeOf(Object)
         */
        @Override
        public int inDegreeOf(V vertex)
        {
            throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
        }

        /**
         * @see DirectedGraph#incomingEdgesOf(Object)
         */
        @Override
        public Set<E> incomingEdgesOf(V vertex)
        {
            throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
        }

        /**
         * @see DirectedGraph#outDegreeOf(Object)
         */
        @Override
        public int outDegreeOf(V vertex)
        {
            throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
        }

        /**
         * @see DirectedGraph#outgoingEdgesOf(Object)
         */
        @Override
        public Set<E> outgoingEdgesOf(V vertex)
        {
            throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
        }

        /**
         * @see AbstractBaseGraph#removeEdgeFromTouchingVertices(Edge)
         */
        @Override
        public void removeEdgeFromTouchingVertices(E e)
        {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            getEdgeContainer(source).removeEdge(e);

            if (!source.equals(target))
                getEdgeContainer(target).removeEdge(e);
        }

        /**
         * A lazy build of edge container for specified vertex.
         *
         * @param vertex a vertex in this graph.
         *
         * @return EdgeContainer
         */
        private UndirectedEdgeContainer<V, E> getEdgeContainer(V vertex)
        {
            assertVertexExist(vertex);

            UndirectedEdgeContainer<V, E> ec = vertexMapUndirected.get(vertex);

            if (ec != null)
                return ec;

            ec = new UndirectedEdgeContainer<V, E>(edgeSetFactory, vertex);
            vertexMapUndirected.put(vertex, ec);

            return ec;
        }
    }
}

// End AbstractBaseGraph.java
