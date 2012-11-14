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
 * TransitiveClosure.java
 * ----------------------
 * (C) Copyright 2007, by Vinayak R. Borkar.
 *
 * Original Author:   Vinayak R. Borkar
 * Contributor(s):
 *
 * Changes
 * -------
 * 5-May-2007: Initial revision (VRB);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.graph.*;


/**
 * Constructs the transitive closure of the input graph.
 *
 * @author Vinayak R. Borkar
 * @since May 5, 2007
 */
public class TransitiveClosure
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     * Singleton instance.
     */
    public static final TransitiveClosure INSTANCE = new TransitiveClosure();

    //~ Constructors -----------------------------------------------------------

    /**
     * Private Constructor.
     */
    private TransitiveClosure()
    {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Computes the transitive closure of the given graph.
     *
     * @param graph - Graph to compute transitive closure for.
     */
    public <V, E> void closeSimpleDirectedGraph(final SimpleDirectedGraph<V, E> graph)
    {
        final Set<V> vertexSet = graph.vertexSet();

        final Set<V> newEdgeTargets = new HashSet<V>();

        // At every iteration of the outer loop, we add a path of length 1
        // between nodes that originally had a path of length 2. In the worst
        // case, we need to make floor(log |V|) + 1 iterations. We stop earlier
        // if there is no change to the output graph.

        final int bound = computeBinaryLog(vertexSet.size());
        boolean done = false;
        for (int i = 0; !done && i < bound; ++i) {
            done = true;
            for (final V v1 : vertexSet) {
                newEdgeTargets.clear();

                for (final E v1OutEdge : graph.outgoingEdgesOf(v1)) {
                    final V v2 = graph.getEdgeTarget(v1OutEdge);
                    for (final E v2OutEdge : graph.outgoingEdgesOf(v2)) {
                        final V v3 = graph.getEdgeTarget(v2OutEdge);

                        if (v1.equals(v3)) {
                            // Its a simple graph, so no self loops.
                            continue;
                        }

                        if (graph.getEdge(v1, v3) != null) {
                            // There is already an edge from v1 ---> v3, skip;
                            continue;
                        }

                        newEdgeTargets.add(v3);
                        done = false;
                    }
                }

                for (final V v3 : newEdgeTargets) {
                    graph.addEdge(v1, v3);
                }
            }
        }
    }

    /**
     * Computes floor(log_2(n)) + 1
     */
    private int computeBinaryLog(int n)
    {
        assert n >= 0;

        int result = 0;
        while (n > 0) {
            n >>= 1;
            ++result;
        }

        return result;
    }
}

// End TransitiveClosure.java
