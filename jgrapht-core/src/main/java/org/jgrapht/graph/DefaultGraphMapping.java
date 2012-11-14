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
 * DefaultGraphMapping.java
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
package org.jgrapht.graph;

import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;

import java.util.Map;


/**
 * Implementation of the GraphMapping interface. The performance of <code>
 * getVertex/EdgeCorrespondence</code> is based on the performance of the
 * concrete Map class which is passed in the constructor. For example, using
 * hashmaps will provide O(1) performence.
 *
 * @author Assaf
 * @since Jul 30, 2005
 */
public class DefaultGraphMapping<V, E>
    implements GraphMapping<V, E>
{
    //~ Instance fields --------------------------------------------------------

    private final Map<V, V> graphMappingForward;
    private final Map<V, V> graphMappingReverse;

    private final Graph<V, E> graph1;
    private final Graph<V, E> graph2;

    //~ Constructors -----------------------------------------------------------

    /**
     * The maps themselves are used. There is no defensive-copy. Assumption: The
     * key and value in the mappings are of valid graph objects. It is not
     * checked.
     *
     * @param g1ToG2
     * @param g2ToG1
     * @param g1
     * @param g2
     */
    public DefaultGraphMapping(
        final Map<V, V> g1ToG2,
        final Map<V, V> g2ToG1,
        final Graph<V, E> g1,
        final Graph<V, E> g2)
    {
        graph1 = g1;
        graph2 = g2;
        graphMappingForward = g1ToG2;
        graphMappingReverse = g2ToG1;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public E getEdgeCorrespondence(final E currEdge, final boolean forward)
    {
        final Graph<V, E> sourceGraph;
        final Graph<V, E> targetGraph;

        if (forward) {
            sourceGraph = graph1;
            targetGraph = graph2;
        } else {
            sourceGraph = graph2;
            targetGraph = graph1;
        }

        final V mappedSourceVertex =
            getVertexCorrespondence(
                sourceGraph.getEdgeSource(currEdge),
                forward);
        final V mappedTargetVertex =
            getVertexCorrespondence(
                sourceGraph.getEdgeTarget(currEdge),
                forward);
        if (mappedSourceVertex == null || mappedTargetVertex == null) {
            return null;
        } else {
            return targetGraph.getEdge(
                mappedSourceVertex,
                mappedTargetVertex);
        }
    }

    @Override
    public V getVertexCorrespondence(
        final V keyVertex,
        final boolean forward)
    {
        final Map<V, V> graphMapping;
        if (forward) {
            graphMapping = graphMappingForward;
        } else {
            graphMapping = graphMappingReverse;
        }

        return graphMapping.get(keyVertex);
    }
}

// End DefaultGraphMapping.java
