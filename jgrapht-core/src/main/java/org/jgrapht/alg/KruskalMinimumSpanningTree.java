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
 * KruskalMinimumSpanningTree.java
 * -------------------------
 * (C) Copyright 2010-2010, by Tom Conerly and Contributors.
 *
 * Original Author:  Tom Conerly
 * Contributor(s):
 *
 * Changes
 * -------
 * 02-Feb-2010 : Initial revision (TC);
 *
 */
package org.jgrapht.alg;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MinimumSpanningTree;
import org.jgrapht.alg.util.UnionFind;

import java.util.*;


/**
 * An implementation of <a
 * href="http://en.wikipedia.org/wiki/Kruskal's_algorithm">Kruskal's minimum
 * spanning tree algorithm</a>. If the given graph is connected it computes the
 * minimum spanning tree, otherwise it computes the minimum spanning forest. The
 * algorithm runs in time O(E log E). This implementation uses the hashCode and
 * equals method of the vertices.
 *
 * @author Tom Conerly
 * @since Feb 10, 2010
 */
public class KruskalMinimumSpanningTree<V, E> implements MinimumSpanningTree<V, E>
{
    //~ Instance fields --------------------------------------------------------

    private double spanningTreeCost;
    private Set<E> edgeList;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates and executes a new KruskalMinimumSpanningTree algorithm instance.
     * An instance is only good for a single spanning tree; after construction,
     * it can be accessed to retrieve information about the spanning tree found.
     *
     * @param graph the graph to be searched
     */
    public KruskalMinimumSpanningTree(final Graph<V, E> graph)
    {
        UnionFind<V> forest = new UnionFind<V>(graph.vertexSet());
        ArrayList<E> allEdges = new ArrayList<E>(graph.edgeSet());
        Collections.sort(
            allEdges,
            new Comparator<E>() {
                public int compare(E edge1, E edge2)
                {
                    return Double.valueOf(graph.getEdgeWeight(edge1)).compareTo(
                        graph.getEdgeWeight(edge2));
                }
            });

        spanningTreeCost = 0;
        edgeList = new HashSet<E>();

        for (E edge : allEdges) {
            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            if (forest.find(source).equals(forest.find(target))) {
                continue;
            }

            forest.union(source, target);
            edgeList.add(edge);
            spanningTreeCost += graph.getEdgeWeight(edge);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Set<E> getMinimumSpanningTreeEdgeSet()
    {
        return edgeList;
    }

    @Override
    public double getMinimumSpanningTreeTotalWeight()
    {
        return spanningTreeCost;
    }


    /**
     * Returns edges set constituting the minimum spanning tree/forest
     *
     * @return minimum spanning-tree edges set
     */
    @Deprecated
    public Set<E> getEdgeSet() {
        return getMinimumSpanningTreeEdgeSet();
    }

    /**
     * Returns total weight of the minimum spanning tree/forest.
     *
     * @return minimum spanning-tree total weight
     */
    @Deprecated
    public double getSpanningTreeCost() {
        return getMinimumSpanningTreeTotalWeight();
    }

}

// End KruskalMinimumSpanningTree.java
