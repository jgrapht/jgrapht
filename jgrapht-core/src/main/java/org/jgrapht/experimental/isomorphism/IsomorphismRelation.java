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
/* -----------------
 * IsomorphismRelation.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.graph.DefaultGraphMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Holds an isomorphism relation for two graphs. It contains a mapping between
 * the two graphs.
 *
 * <p>Usage:
 *
 * <ol>
 * <li>use <code>getVertexCorrespondence()</code> or <code>
 * getEdgeCorrespondence()</code> to get the mapped object in the other graph.
 * </ol>
 *
 * <p>
 * <p>It consists of two vertexes array , the i-th vertex in the 1st array is
 * the isomorphic eqv. of the i-th in 2nd array. Note that the getters are
 * unsafe (they return the array and not a copy of it).
 *
 * @author Assaf
 * @since May 27, 2005
 */
public class IsomorphismRelation<V, E>
    implements GraphMapping<V, E>
{
    //~ Instance fields --------------------------------------------------------

    private final List<V> vertexList1;
    private final List<V> vertexList2;

    private GraphMapping<V, E> graphMapping = null;

    private final Graph<V, E> graph1;
    private final Graph<V, E> graph2;

    //~ Constructors -----------------------------------------------------------

    /**
     */
    public IsomorphismRelation(
        final List<V> aGraph1vertexArray,
        final List<V> aGraph2vertexArray,
        final Graph<V, E> g1,
        final Graph<V, E> g2)
    {
        vertexList1 = aGraph1vertexArray;
        vertexList2 = aGraph2vertexArray;
        graph1 = g1;
        graph2 = g2;
    }

    //~ Methods ----------------------------------------------------------------

    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append("vertexList1: ").append(vertexList1.toString());
        sb.append("\tvertexList2: ").append(vertexList2.toString());
        return sb.toString();
    }

    @Override
    public V getVertexCorrespondence(final V vertex, final boolean forward)
    {
        // lazy initializer for graphMapping
        if (graphMapping == null) {
            initGraphMapping();
        }

        return graphMapping.getVertexCorrespondence(vertex, forward);
    }

    @Override
    public E getEdgeCorrespondence(final E edge, final boolean forward)
    {
        // lazy initializer for graphMapping
        if (graphMapping == null) {
            initGraphMapping();
        }

        return graphMapping.getEdgeCorrespondence(edge, forward);
    }

    /**
     * We currently have the vertexes array. From them we will construct two
     * maps: g1ToG2 and g2ToG1, using the array elements with the same index.
     */
    private void initGraphMapping()
    {
        final int mapSize = vertexList1.size();
        final Map<V, V> g1ToG2 = new HashMap<V, V>(mapSize);
        final Map<V, V> g2ToG1 = new HashMap<V, V>(mapSize);

        for (int i = 0; i < mapSize; i++) {
            final V source = vertexList1.get(i);
            final V target = vertexList2.get(i);
            g1ToG2.put(source, target);
            g2ToG1.put(target, source);
        }
        graphMapping =
            new DefaultGraphMapping<V, E>(
                g1ToG2,
                g2ToG1, graph1, graph2);
    }
}

// End IsomorphismRelation.java
