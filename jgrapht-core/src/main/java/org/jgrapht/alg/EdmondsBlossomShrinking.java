/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2012, by Barak Naveh and Contributors.
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
 * EdmondsBlossomShrinking.java
 * -------------------------
 * (C) Copyright 2012-2012, by Alejandro Ramon Lopez del Huerto and Contributors.
 *
 * Original Author:  Alejandro Ramon Lopez del Huerto
 * Contributor(s):
 *
 * Changes
 * -------
 * 24-Jan-2012 : Initial revision (ARLH);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.util.*;

/**
 * An implementation of Edmonds Blossom Shrinking algorithm for constructing
 * maximum matchings on graphs. The algorithm runs in time O(V^4).
 * 
 * @author Alejandro R. Lopez del Huerto
 * @since Jan 24, 2012
 */
public class EdmondsBlossomShrinking<V, E>
{
    // ~ Instance fields
    // --------------------------------------------------------

    private Map<V, V> match;
    private Map<V, V> p;
    private Map<V, V> base;
    private Queue<V> q;
    private Set<V> used;
    private Set<V> blossom;

    // ~ Methods
    // ----------------------------------------------------------------

    /**
     * Runs the algorithm on the input graph and returns the match edge set.
     * 
     * @param g
     *            The graph to be matched
     * @return set of Edges
     */
    public Set<E> findMatch(final UndirectedGraph<V, E> g)
    {
        Set<E> result = new ArrayUnenforcedSet<E>();
        match = new HashMap<V, V>();
        p = new HashMap<V, V>();
        q = new ArrayDeque<V>();
        base = new HashMap<V, V>();
        used = new HashSet<V>();
        blossom = new HashSet<V>();

        for (V i : g.vertexSet()) {
            if (!match.containsKey(i)) {
                V v = findPath(g, i);
                while (v != null) {
                    V pv = p.get(v);
                    V ppv = match.get(pv);
                    match.put(v, pv);
                    match.put(pv, v);
                    v = ppv;
                }
            }
        }

        Set<V> seen = new HashSet<V>();
        for (V v : g.vertexSet()) {
            if (!seen.contains(v) && match.containsKey(v)) {
                seen.add(v);
                seen.add(match.get(v));
                result.add(g.getEdge(v, match.get(v)));
            }
        }

        return result;
    }

    private V findPath(UndirectedGraph<V, E> g, V root)
    {
        used.clear();
        p.clear();
        base.clear();

        for (V i : g.vertexSet()) {
            base.put(i, i);
        }

        used.add(root);
        q.add(root);
        while (!q.isEmpty()) {
            V v = q.remove();
            for (V to : g.vertexSet()) {
                if (!g.containsEdge(v, to)) {
                    continue;
                }

                if ((base.get(v) == base.get(to)) || (match.get(v) == to)) {
                    continue;
                }
                if (to == root || (match.containsKey(to))
                        && (p.containsKey(match.get(to)))) {
                    V curbase = lca(g, v, to);
                    blossom.clear();
                    markPath(g, v, curbase, to);
                    markPath(g, to, curbase, v);

                    for (V i : g.vertexSet()) {
                        if (base.containsKey(i)
                                && blossom.contains(base.get(i)))
                        {
                            base.put(i, curbase);
                            if (!used.contains(i)) {
                                used.add(i);
                                q.add(i);
                            }
                        }
                    }
                } else if (!p.containsKey(to)) {
                    p.put(to, v);
                    if (!match.containsKey(to)) {
                        return to;
                    }
                    to = match.get(to);
                    used.add(to);
                    q.add(to);
                }
            }
        }
        return null;
    }

    private void markPath(UndirectedGraph<V, E> g, V v, V b, V children)
    {
        while (base.get(v) != b) {
            blossom.add(base.get(v));
            blossom.add(base.get(match.get(v)));
            p.put(v, children);
            children = match.get(v);
            v = p.get(match.get(v));
        }
    }

    private V lca(UndirectedGraph<V, E> g, V a, V b)
    {
        Set<V> seen = new HashSet<V>();
        for (;;) {
            a = base.get(a);
            seen.add(a);
            if (!match.containsKey(a)) {
                break;
            }
            a = p.get(match.get(a));
        }
        for (;;) {
            b = base.get(b);
            if (seen.contains(b)) {
                return b;
            }
            b = p.get(match.get(b));
        }
    }

}
