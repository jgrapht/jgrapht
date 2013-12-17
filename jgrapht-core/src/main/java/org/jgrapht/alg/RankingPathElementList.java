/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
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
 * RankingPathElementList.java
 * -------------------------
 * (C) Copyright 2007-2010, by France Telecom
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
 * 06-Dec-2010 : Bugfixes (GB);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * List of simple paths in increasing order of weight.
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
final class RankingPathElementList<V, E>
    extends AbstractPathElementList<V, E, RankingPathElement<V, E>>
{
    

    /**
     * Vertex that paths of the list must not disconnect.
     */
    private V guardVertexToNotDisconnect = null;

    private Map<RankingPathElement<V, E>, Boolean> path2disconnect =
        new HashMap<RankingPathElement<V, E>, Boolean>();

    

    /**
     * Creates a list with an empty path. The list size is 1.
     *
     * @param maxSize max number of paths the list is able to store.
     */
    RankingPathElementList(
        Graph<V, E> graph,
        int maxSize,
        RankingPathElement<V, E> pathElement)
    {
        super(graph, maxSize, pathElement);
    }

    /**
     * Creates paths obtained by concatenating the specified edge to the
     * specified paths.
     *
     * @param prevPathElementList paths, list of <code>
     * RankingPathElement</code>.
     * @param edge edge reaching the end vertex of the created paths.
     * @param maxSize maximum number of paths the list is able to store.
     */
    RankingPathElementList(
        Graph<V, E> graph,
        int maxSize,
        RankingPathElementList<V, E> elementList,
        E edge)
    {
        this(graph, maxSize, elementList, edge, null);

        assert (!this.pathElements.isEmpty());
    }

    /**
     * Creates paths obtained by concatenating the specified edge to the
     * specified paths.
     *
     * @param prevPathElementList paths, list of <code>
     * RankingPathElement</code>.
     * @param edge edge reaching the end vertex of the created paths.
     * @param maxSize maximum number of paths the list is able to store.
     */
    RankingPathElementList(
        Graph<V, E> graph,
        int maxSize,
        RankingPathElementList<V, E> elementList,
        E edge,
        V guardVertexToNotDisconnect)
    {
        super(graph, maxSize, elementList, edge);
        this.guardVertexToNotDisconnect = guardVertexToNotDisconnect;

        // loop over the path elements in increasing order of weight.
        for (int i = 0; (i < elementList.size()) && (size() < maxSize); i++) {
            RankingPathElement<V, E> prevPathElement = elementList.get(i);

            if (isNotValidPath(prevPathElement, edge)) {
                // go to the next path element in the loop
                continue;
            }

            double weight = calculatePathWeight(prevPathElement, edge);
            RankingPathElement<V, E> newPathElement =
                new RankingPathElement<V, E>(
                    this.graph,
                    prevPathElement,
                    edge,
                    weight);

            // the new path is inserted at the end of the list.
            this.pathElements.add(newPathElement);
        }
    }

    /**
     * Creates an empty list. The list size is 0.
     *
     * @param maxSize max number of paths the list is able to store.
     */
    RankingPathElementList(Graph<V, E> graph, int maxSize, V vertex)
    {
        super(graph, maxSize, vertex);
    }

    

    /**
     * <p>Adds paths in the list at vertex y. Candidate paths are obtained by
     * concatenating the specified edge (v->y) to the paths <code>
     * elementList</code> at vertex v.</p>
     *
     * Complexity =
     *
     * <ul>
     * <li>w/o guard-vertex: O(<code>k*np</code>) where <code>k</code> is the
     * max size limit of the list and <code>np</code> is the maximum number of
     * vertices in the paths stored in the list</li>
     * <li>with guard-vertex: O(<code>k*(m+n)</code>) where <code>k</code> is
     * the max size limit of the list, <code>m</code> is the number of edges of
     * the graph and <code>n</code> is the number of vertices of the graph,
     * O(<code>m+n</code>) being the complexity of the <code>
     * ConnectivityInspector</code> to check whether a path exists towards the
     * guard-vertex</li>
     * </ul>
     *
     * @param elementList list of paths at vertex v.
     * @param edge edge (v->y).
     *
     * @return <code>true</code> if at least one path has been added in the
     * list, <code>false</code> otherwise.
     */
    public boolean addPathElements(
        RankingPathElementList<V, E> elementList,
        E edge)
    {
        assert (this.vertex.equals(
            Graphs.getOppositeVertex(
                this.graph,
                edge,
                elementList.getVertex())));

        boolean pathAdded = false;

        // loop over the paths elements of the list at vertex v.
        for (
            int vIndex = 0, yIndex = 0;
            vIndex < elementList.size();
            vIndex++)
        {
            RankingPathElement<V, E> prevPathElement = elementList.get(vIndex);

            if (isNotValidPath(prevPathElement, edge)) {
                // checks if path is simple and if guard-vertex is not
                // disconnected.
                continue;
            }
            double newPathWeight = calculatePathWeight(prevPathElement, edge);
            RankingPathElement<V, E> newPathElement =
                new RankingPathElement<V, E>(
                    this.graph,
                    prevPathElement,
                    edge,
                    newPathWeight);

            // loop over the paths of the list at vertex y from yIndex to the
            // end.
            RankingPathElement<V, E> yPathElement = null;
            for (; yIndex < size(); yIndex++) {
                yPathElement = get(yIndex);

                // case when the new path is shorter than the path Py stored at
                // index y
                if (newPathWeight < yPathElement.getWeight()) {
                    this.pathElements.add(yIndex, newPathElement);
                    pathAdded = true;

                    // ensures max size limit is not exceeded.
                    if (size() > this.maxSize) {
                        this.pathElements.remove(this.maxSize);
                    }
                    break;
                }

                // case when the new path is of the same length as the path Py
                // stored at index y
                if (newPathWeight == yPathElement.getWeight()) {
                    this.pathElements.add(yIndex + 1, newPathElement);
                    pathAdded = true;

                    // ensures max size limit is not exceeded.
                    if (size() > this.maxSize) {
                        this.pathElements.remove(this.maxSize);
                    }
                    break;
                }
            }

            // case when the new path is longer than the longest path in the
            // list (Py stored at the last index y)
            if (newPathWeight > yPathElement.getWeight()) {
                // ensures max size limit is not exceeded.
                if (size() < this.maxSize) {
                    // the new path is inserted at the end of the list.
                    this.pathElements.add(newPathElement);
                    pathAdded = true;
                } else {
                    // max size limit is reached -> end of the loop over the
                    // paths elements of the list at vertex v.
                    break;
                }
            }
        }

        return pathAdded;
    }

    /**
     * @return list of <code>RankingPathElement</code>.
     */
    List<RankingPathElement<V, E>> getPathElements()
    {
        return this.pathElements;
    }

    /**
     * Costs taken into account are the weights stored in <code>Edge</code>
     * objects.
     *
     * @param pathElement
     * @param edge the edge via which the vertex was encountered.
     *
     * @return the cost obtained by concatenation.
     *
     * @see Graph#getEdgeWeight(E)
     */
    private double calculatePathWeight(
        RankingPathElement<V, E> pathElement,
        E edge)
    {
        double pathWeight = this.graph.getEdgeWeight(edge);

        // otherwise it's the start vertex.
        if ((pathElement.getPrevEdge() != null)) {
            pathWeight += pathElement.getWeight();
        }

        return pathWeight;
    }

    /**
     * Ensures that paths of the list do not disconnect the guard-vertex.
     *
     * @return <code>true</code> if the specified path element disconnects the
     * guard-vertex, <code>false</code> otherwise.
     */
    private boolean isGuardVertexDisconnected(
        RankingPathElement<V, E> prevPathElement)
    {
        if (this.guardVertexToNotDisconnect == null) {
            return false;
        }

        if (this.path2disconnect.containsKey(prevPathElement)) {
            return this.path2disconnect.get(prevPathElement);
        }

        ConnectivityInspector<V, E> connectivityInspector;
        MaskFunctor<V, E> connectivityMask;

        if (this.graph instanceof DirectedGraph<?, ?>) {
            connectivityMask = new PathMask<V, E>(prevPathElement);
            DirectedMaskSubgraph<V, E> connectivityGraph =
                new DirectedMaskSubgraph<V, E>(
                    (DirectedGraph<V, E>) this.graph,
                    connectivityMask);
            connectivityInspector =
                new ConnectivityInspector<V, E>(
                    connectivityGraph);
        } else {
            connectivityMask = new PathMask<V, E>(prevPathElement);
            UndirectedMaskSubgraph<V, E> connectivityGraph =
                new UndirectedMaskSubgraph<V, E>(
                    (UndirectedGraph<V, E>) this.graph,
                    connectivityMask);
            connectivityInspector =
                new ConnectivityInspector<V, E>(
                    connectivityGraph);
        }

        if (connectivityMask.isVertexMasked(this.guardVertexToNotDisconnect)) {
            // the guard-vertex was already in the path element -> invalid path
            this.path2disconnect.put(prevPathElement, true);
            return true;
        }

        if (!connectivityInspector.pathExists(
                this.vertex,
                this.guardVertexToNotDisconnect))
        {
            this.path2disconnect.put(prevPathElement, true);
            return true;
        }

        this.path2disconnect.put(prevPathElement, false);
        return false;
    }

    private boolean isNotValidPath(
        RankingPathElement<V, E> prevPathElement,
        E edge)
    {
        return !isSimplePath(prevPathElement, edge)
            || isGuardVertexDisconnected(prevPathElement);
    }

    /**
     * Ensures that paths of the list are simple (check that the vertex was not
     * already in the path element).
     *
     * @param prevPathElement
     * @param edge
     *
     * @return <code>true</code> if the resulting path (obtained by
     * concatenating the specified edge to the specified path) is simple, <code>
     * false</code> otherwise.
     */
    private boolean isSimplePath(
        RankingPathElement<V, E> prevPathElement,
        E edge)
    {
        V endVertex =
            Graphs.getOppositeVertex(
                this.graph,
                edge,
                prevPathElement.getVertex());
        assert (endVertex.equals(this.vertex));

        RankingPathElement<V, E> pathElementToTest = prevPathElement;
        do {
            if (pathElementToTest.getVertex().equals(endVertex)) {
                return false;
            } else {
                pathElementToTest = pathElementToTest.getPrevPathElement();
            }
        } while (pathElementToTest != null);

        return true;
    }

    

    private static class PathMask<V, E>
        implements MaskFunctor<V, E>
    {
        private Set<E> maskedEdges;

        private Set<V> maskedVertices;

        /**
         * Creates a mask for all the edges and the vertices of the path
         * (including the 2 extremity vertices).
         *
         * @param pathElement
         */
        PathMask(RankingPathElement<V, E> pathElement)
        {
            this.maskedEdges = new HashSet<E>();
            this.maskedVertices = new HashSet<V>();

            while (pathElement.getPrevEdge() != null) {
                this.maskedEdges.add(pathElement.getPrevEdge());
                this.maskedVertices.add(pathElement.getVertex());
                pathElement = pathElement.getPrevPathElement();
            }
            this.maskedVertices.add(pathElement.getVertex());
        }

        // implement MaskFunctor
        public boolean isEdgeMasked(E edge)
        {
            return this.maskedEdges.contains(edge);
        }

        // implement MaskFunctor
        public boolean isVertexMasked(V vertex)
        {
            return this.maskedVertices.contains(vertex);
        }
    }
}

// End RankingPathElementList.java
