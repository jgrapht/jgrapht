/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2010, by Barak Naveh and Contributors.
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

import com.google.common.collect.Maps;
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
    //~ Instance fields --------------------------------------------------------

    /**
     * Vertex that paths of the list must not disconnect.
     */
    private V guardVertexToNotDisconnect = null;

    private final Map<RankingPathElement<V, E>, Boolean> path2disconnect
        = Maps.newHashMap();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a list with an empty path. The list size is 1.
     *
     * @param maxSize max number of paths the list is able to store.
     */
    RankingPathElementList(
        final Graph<V, E> graph,
        final int maxSize,
        final RankingPathElement<V, E> pathElement)
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
        final Graph<V, E> graph,
        final int maxSize,
        final RankingPathElementList<V, E> elementList,
        final E edge)
    {
        this(graph, maxSize, elementList, edge, null);

        assert !pathElements.isEmpty();
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
        final Graph<V, E> graph,
        final int maxSize,
        final RankingPathElementList<V, E> elementList,
        final E edge,
        final V guardVertexToNotDisconnect)
    {
        super(graph, maxSize, elementList, edge);
        this.guardVertexToNotDisconnect = guardVertexToNotDisconnect;

        // loop over the path elements in increasing order of weight.
        for (final RankingPathElement<V, E> anElementList : elementList) {

            if (isNotValidPath(anElementList, edge)) {
                continue;
            }

            if (size() < this.maxSize) {
                final double weight = calculatePathWeight(anElementList, edge);
                final RankingPathElement<V, E> newPathElement =
                    new RankingPathElement<V, E>(
                        this.graph, anElementList,
                        edge,
                        weight);

                // the new path is inserted at the end of the list.
                pathElements.add(newPathElement);
            }
        }
    }

    /**
     * Creates an empty list. The list size is 0.
     *
     * @param maxSize max number of paths the list is able to store.
     */
    RankingPathElementList(final Graph<V, E> graph, final int maxSize, final V vertex)
    {
        super(graph, maxSize, vertex);
    }

    //~ Methods ----------------------------------------------------------------

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
        final RankingPathElementList<V, E> elementList,
        final E edge)
    {
        assert vertex.equals(
            Graphs.getOppositeVertex(graph, edge, elementList.getVertex()));

        boolean pathAdded = false;

        // loop over the paths elements of the list at vertex v.
        for (
            int vIndex = 0, yIndex = 0;
            vIndex < elementList.size();
            vIndex++)
        {
            final RankingPathElement<V, E> prevPathElement = elementList.get(vIndex);

            if (isNotValidPath(prevPathElement, edge)) {
                // checks if path is simple and if guard-vertex is not
                // disconnected.
                continue;
            }
            final double newPathWeight = calculatePathWeight(prevPathElement, edge);
            final RankingPathElement<V, E> newPathElement =
                new RankingPathElement<V, E>(graph,
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
                    pathElements.add(yIndex, newPathElement);
                    pathAdded = true;

                    // ensures max size limit is not exceeded.
                    if (size() > maxSize) {
                        pathElements.remove(maxSize);
                    }
                    break;
                }

                // case when the new path is of the same length as the path Py
                // stored at index y
                if (newPathWeight == yPathElement.getWeight()) {
                    pathElements.add(yIndex + 1, newPathElement);
                    pathAdded = true;

                    // ensures max size limit is not exceeded.
                    if (size() > maxSize) {
                        pathElements.remove(maxSize);
                    }
                    break;
                }
            }

            // case when the new path is longer than the longest path in the
            // list (Py stored at the last index y)
            if (newPathWeight > yPathElement.getWeight()) {
                // ensures max size limit is not exceeded.
                if (size() < maxSize) {
                    // the new path is inserted at the end of the list.
                    pathElements.add(newPathElement);
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
        return pathElements;
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
        final RankingPathElement<V, E> pathElement,
        final E edge)
    {
        double pathWeight = graph.getEdgeWeight(edge);

        // otherwise it's the start vertex.
        if (pathElement.getPrevEdge() != null) {
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
        final RankingPathElement<V, E> prevPathElement)
    {
        if (guardVertexToNotDisconnect == null) {
            return false;
        }

        if (path2disconnect.containsKey(prevPathElement)) {
            return path2disconnect.get(prevPathElement);
        }

        final ConnectivityInspector<V, E> connectivityInspector;
        final MaskFunctor<V, E> connectivityMask;

        if (graph instanceof DirectedGraph<?, ?>) {
            connectivityMask = new PathMask<V, E>(prevPathElement);
            final DirectedMaskSubgraph<V, E> connectivityGraph =
                new DirectedMaskSubgraph<V, E>(
                    (DirectedGraph<V, E>) graph,
                    connectivityMask);
            connectivityInspector =
                new ConnectivityInspector<V, E>(
                    connectivityGraph);
        } else {
            connectivityMask = new PathMask<V, E>(prevPathElement);
            final UndirectedMaskSubgraph<V, E> connectivityGraph =
                new UndirectedMaskSubgraph<V, E>(
                    (UndirectedGraph<V, E>) graph,
                    connectivityMask);
            connectivityInspector =
                new ConnectivityInspector<V, E>(
                    connectivityGraph);
        }

        if (connectivityMask.isVertexMasked(guardVertexToNotDisconnect)) {
            // the guard-vertex was already in the path element -> invalid path
            path2disconnect.put(prevPathElement, true);
            return true;
        }

        if (!connectivityInspector.pathExists(vertex,
            guardVertexToNotDisconnect))
        {
            path2disconnect.put(prevPathElement, true);
            return true;
        }

        path2disconnect.put(prevPathElement, false);
        return false;
    }

    private boolean isNotValidPath(
        final RankingPathElement<V, E> prevPathElement,
        final E edge)
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
        final RankingPathElement<V, E> prevPathElement,
        final E edge)
    {
        RankingPathElement<V, E> pathElementToTest = prevPathElement;
        while (pathElementToTest.getPrevEdge() != null) {
            if (pathElementToTest.getVertex() == vertex) {
                return false;
            } else {
                pathElementToTest = pathElementToTest.getPrevPathElement();
            }
        }

        return true;
    }

    //~ Inner Classes ----------------------------------------------------------

    private static class PathMask<V, E>
        implements MaskFunctor<V, E>
    {
        private final Set<E> maskedEdges;

        private final Set<V> maskedVertices;

        /**
         * Creates a mask for all the edges and the vertices of the path
         * (including the 2 extremity vertices).
         *
         * @param pathElement
         */
        PathMask(RankingPathElement<V, E> pathElement)
        {
            maskedEdges = new HashSet<E>();
            maskedVertices = new HashSet<V>();

            while (pathElement.getPrevEdge() != null) {
                maskedEdges.add(pathElement.getPrevEdge());
                maskedVertices.add(pathElement.getVertex());
                pathElement = pathElement.getPrevPathElement();
            }
            maskedVertices.add(pathElement.getVertex());
        }

        // implement MaskFunctor
        @Override
        public boolean isEdgeMasked(final E edge)
        {
            return maskedEdges.contains(edge);
        }

        // implement MaskFunctor
        @Override
        public boolean isVertexMasked(final V vertex)
        {
            return maskedVertices.contains(vertex);
        }
    }
}

// End RankingPathElementList.java
