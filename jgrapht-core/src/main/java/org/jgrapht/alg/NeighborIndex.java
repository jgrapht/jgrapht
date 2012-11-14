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
/* --------------------------
 * NeighborIndex.java
 * --------------------------
 * (C) Copyright 2005-2008, by Charles Fry and Contributors.
 *
 * Original Author:  Charles Fry
 *
 * $Id$
 *
 * Changes
 * -------
 * 13-Dec-2005 : Initial revision (CF);
 *
 */
package org.jgrapht.alg;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.event.VertexSetListener;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Maintains a cache of each vertex's neighbors. While lists of neighbors can be
 * obtained from {@link Graphs}, they are re-calculated at each invocation by
 * walking a vertex's incident edges, which becomes inordinately expensive when
 * performed often.
 *
 * <p>Edge direction is ignored when evaluating neighbors; to take edge
 * direction into account when indexing neighbors, use {@link
 * DirectedNeighborIndex}.
 *
 * <p>A vertex's neighbors are cached the first time they are asked for (i.e.
 * the index is built on demand). The index will only be updated automatically
 * if it is added to the associated graph as a listener. If it is added as a
 * listener to a graph other than the one it indexes, results are undefined.</p>
 *
 * @author Charles Fry
 * @since Dec 13, 2005
 */
public class NeighborIndex<V, E>
    implements GraphListener<V, E>
{
    //~ Instance fields --------------------------------------------------------

    Map<V, Neighbors<V>> neighborMap = new HashMap<V, Neighbors<V>>();
    private Graph<V, E> graph;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a neighbor index for the specified undirected graph.
     *
     * @param g the graph for which a neighbor index is to be created.
     */
    public NeighborIndex(Graph<V, E> g)
    {
        // no need to distinguish directedgraphs as we don't do traversals
        graph = g;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the set of vertices which are adjacent to a specified vertex. The
     * returned set is backed by the index, and will be updated when the graph
     * changes as long as the index has been added as a listener to the graph.
     *
     * @param v the vertex whose neighbors are desired
     *
     * @return all unique neighbors of the specified vertex
     */
    public Set<V> neighborsOf(V v)
    {
        return getNeighbors(v).getNeighbors();
    }

    /**
     * Returns a list of vertices which are adjacent to a specified vertex. If
     * the graph is a multigraph, vertices may appear more than once in the
     * returned list. Because a list of neighbors can not be efficiently
     * maintained, it is reconstructed on every invocation, by duplicating
     * entries in the neighbor set. It is thus more efficient to use {@link
     * #neighborsOf(Object)} unless duplicate neighbors are important.
     *
     * @param v the vertex whose neighbors are desired
     *
     * @return all neighbors of the specified vertex
     */
    public List<V> neighborListOf(V v)
    {
        return getNeighbors(v).getNeighborList();
    }

    /**
     * @see GraphListener#edgeAdded(GraphEdgeChangeEvent)
     */
    @Override
    public void edgeAdded(GraphEdgeChangeEvent<V, E> e)
    {
        E edge = e.getEdge();
        V source = graph.getEdgeSource(edge);
        V target = graph.getEdgeTarget(edge);

        // if a map does not already contain an entry,
        // then skip addNeighbor, since instantiating the map
        // will take care of processing the edge (which has already
        // been added)

        if (neighborMap.containsKey(source)) {
            getNeighbors(source).addNeighbor(target);
        } else {
            getNeighbors(source);
        }
        if (neighborMap.containsKey(target)) {
            getNeighbors(target).addNeighbor(source);
        } else {
            getNeighbors(target);
        }
    }

    /**
     * @see GraphListener#edgeRemoved(GraphEdgeChangeEvent)
     */
    @Override
    public void edgeRemoved(GraphEdgeChangeEvent<V, E> e)
    {
        E edge = e.getEdge();
        V source = e.getEdgeSource();
        V target = e.getEdgeTarget();
        if (neighborMap.containsKey(source)) {
            neighborMap.get(source).removeNeighbor(target);
        }
        if (neighborMap.containsKey(target)) {
            neighborMap.get(target).removeNeighbor(source);
        }
    }

    /**
     * @see VertexSetListener#vertexAdded(GraphVertexChangeEvent)
     */
    @Override
    public void vertexAdded(GraphVertexChangeEvent<V> e)
    {
        // nothing to cache until there are edges
    }

    /**
     * @see VertexSetListener#vertexRemoved(GraphVertexChangeEvent)
     */
    @Override
    public void vertexRemoved(GraphVertexChangeEvent<V> e)
    {
        neighborMap.remove(e.getVertex());
    }

    private Neighbors<V> getNeighbors(V v)
    {
        Neighbors<V> neighbors = neighborMap.get(v);
        if (neighbors == null) {
            neighbors = new Neighbors<V>(Graphs.neighborListOf(graph, v));
            neighborMap.put(v, neighbors);
        }
        return neighbors;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Stores cached neighbors for a single vertex. Includes support for live
     * neighbor sets and duplicate neighbors.
     */
    static final class Neighbors<V>
    {
        private final Map<V, AtomicInteger> neighborCounts = Maps.newHashMap();

        private final Set<V> neighborSet
            = Collections.unmodifiableSet(neighborCounts.keySet());

        Neighbors(final Collection<V> neighbors)
        {
            for (final V neighbor : neighbors)
                addNeighbor(neighbor);
        }

        public void addNeighbor(V v)
        {
            if (!neighborCounts.containsKey(v)) {
                neighborCounts.put(v, new AtomicInteger(1));
                return;
            }

            neighborCounts.get(v).incrementAndGet();
        }

        public void removeNeighbor(V v)
        {
            Preconditions.checkArgument(neighborCounts.containsKey(v),
                "Attempting to remove a neighbor that wasn't present");

            final int count = neighborCounts.get(v).decrementAndGet();

            if (count == 0)
                neighborCounts.remove(v);
        }

        public Set<V> getNeighbors()
        {
            return neighborSet;
        }

        public List<V> getNeighborList()
        {
            final List<V> neighbors = Lists.newArrayList();

            V v;
            int count;

            for (final Map.Entry<V, AtomicInteger> entry:
                neighborCounts.entrySet()) {
                v = entry.getKey();
                count = entry.getValue().get();
                for (int i = 0; i < count; i++)
                    neighbors.add(v);
            }
            return neighbors;
        }
    }
}

// End NeighborIndex.java
