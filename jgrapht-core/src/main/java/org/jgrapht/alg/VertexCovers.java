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
/* -----------------
 * VertexCovers.java
 * -----------------
 * (C) Copyright 2003-2008, by Linda Buisman and Contributors.
 *
 * Original Author:  Linda Buisman
 * Contributor(s):   Barak Naveh
 *                   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Nov-2003 : Initial revision (LB);
 * 07-Jun-2005 : Made generic (CH);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;


/**
 * Algorithms to find a vertex cover for a graph. A vertex cover is a set of
 * vertices that touches all the edges in the graph. The graph's vertex set is a
 * trivial cover. However, a <i>minimal</i> vertex set (or at least an
 * approximation for it) is usually desired. Finding a true minimal vertex cover
 * is an NP-Complete problem. For more on the vertex cover problem, see <a
 * href="http://mathworld.wolfram.com/VertexCover.html">
 * http://mathworld.wolfram.com/VertexCover.html</a>
 *
 * @author Linda Buisman
 * @since Nov 6, 2003
 */
public abstract class VertexCovers
{
    

    /**
     * Finds a 2-approximation for a minimal vertex cover of the specified
     * graph. The algorithm promises a cover that is at most double the size of
     * a minimal cover. The algorithm takes O(|E|) time.
     *
     * <p>For more details see Jenny Walter, CMPU-240: Lecture notes for
     * Language Theory and Computation, Fall 2002, Vassar College, <a
     * href="http://www.cs.vassar.edu/~walter/cs241index/lectures/PDF/approx.pdf">
     * http://www.cs.vassar.edu/~walter/cs241index/lectures/PDF/approx.pdf</a>.
     * </p>
     *
     * @param g the graph for which vertex cover approximation is to be found.
     *
     * @return a set of vertices which is a vertex cover for the specified
     * graph.
     */
    public static <V, E> Set<V> find2ApproximationCover(Graph<V, E> g)
    {
        // C <-- {}
        Set<V> cover = new HashSet<V>();

        // G'=(V',E') <-- G(V,E)
        Subgraph<V, E, Graph<V, E>> sg =
            new Subgraph<V, E, Graph<V, E>>(
                g,
                null,
                null);

        // while E' is non-empty
        while (sg.edgeSet().size() > 0) {
            // let (u,v) be an arbitrary edge of E'
            E e = sg.edgeSet().iterator().next();

            // C <-- C U {u,v}
            V u = g.getEdgeSource(e);
            V v = g.getEdgeTarget(e);
            cover.add(u);
            cover.add(v);

            // remove from E' every edge incident on either u or v
            sg.removeVertex(u);
            sg.removeVertex(v);
        }

        return cover; // return C
    }

    /**
     * Finds a greedy approximation for a minimal vertex cover of a specified
     * graph. At each iteration, the algorithm picks the vertex with the highest
     * degree and adds it to the cover, until all edges are covered.
     *
     * <p>The algorithm works on undirected graphs, but can also work on
     * directed graphs when their edge-directions are ignored. To ignore edge
     * directions you can use {@link org.jgrapht.Graphs#undirectedGraph(Graph)}
     * or {@link org.jgrapht.graph.AsUndirectedGraph}.</p>
     *
     * @param g the graph for which vertex cover approximation is to be found.
     *
     * @return a set of vertices which is a vertex cover for the specified
     * graph.
     */
    public static <V, E> Set<V> findGreedyCover(UndirectedGraph<V, E> g)
    {
        // C <-- {}
        Set<V> cover = new HashSet<V>();

        // G' <-- G
        UndirectedGraph<V, E> sg = new UndirectedSubgraph<V, E>(g, null, null);

        // compare vertices in descending order of degree
        VertexDegreeComparator<V, E> comp =
            new VertexDegreeComparator<V, E>(sg);

        // while G' != {}
        while (sg.edgeSet().size() > 0) {
            // v <-- vertex with maximum degree in G'
            V v = Collections.max(sg.vertexSet(), comp);

            // C <-- C U {v}
            cover.add(v);

            // remove from G' every edge incident on v, and v itself
            sg.removeVertex(v);
        }

        return cover;
    }
}

// End VertexCovers.java
