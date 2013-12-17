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

import java.util.*;

import org.jgrapht.*;


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
    

    private Map<V, V> graphMappingForward;
    private Map<V, V> graphMappingReverse;

    private Graph<V, E> graph1;
    private Graph<V, E> graph2;

    

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
        Map<V, V> g1ToG2,
        Map<V, V> g2ToG1,
        Graph<V, E> g1,
        Graph<V, E> g2)
    {
        this.graph1 = g1;
        this.graph2 = g2;
        this.graphMappingForward = g1ToG2;
        this.graphMappingReverse = g2ToG1;
    }

    

    public E getEdgeCorrespondence(E currEdge, boolean forward)
    {
        Graph<V, E> sourceGraph, targetGraph;

        if (forward) {
            sourceGraph = this.graph1;
            targetGraph = this.graph2;
        } else {
            sourceGraph = this.graph2;
            targetGraph = this.graph1;
        }

        V mappedSourceVertex =
            getVertexCorrespondence(
                sourceGraph.getEdgeSource(currEdge),
                forward);
        V mappedTargetVertex =
            getVertexCorrespondence(
                sourceGraph.getEdgeTarget(currEdge),
                forward);
        if ((mappedSourceVertex == null) || (mappedTargetVertex == null)) {
            return null;
        } else {
            return targetGraph.getEdge(
                mappedSourceVertex,
                mappedTargetVertex);
        }
    }

    public V getVertexCorrespondence(
        V keyVertex,
        boolean forward)
    {
        Map<V, V> graphMapping;
        if (forward) {
            graphMapping = graphMappingForward;
        } else {
            graphMapping = graphMappingReverse;
        }

        return graphMapping.get(keyVertex);
    }
}

// End DefaultGraphMapping.java
