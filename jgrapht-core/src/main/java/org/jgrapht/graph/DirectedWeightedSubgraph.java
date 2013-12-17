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
/* -----------------------------
 * DirectedWeightedSubgraph.java
 * -----------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Aug-2003 : Initial revision (BN);
 * 06-Aug-2005 : Made generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;


/**
 * A directed weighted graph that is a subgraph on other graph.
 *
 * @see Subgraph
 */
public class DirectedWeightedSubgraph<V, E>
    extends DirectedSubgraph<V, E>
    implements WeightedGraph<V, E>
{
    

    private static final long serialVersionUID = 3905799799168250680L;

    

    /**
     * Creates a new weighted directed subgraph.
     *
     * @param base the base (backing) graph on which the subgraph will be based.
     * @param vertexSubset vertices to include in the subgraph. If <code>
     * null</code> then all vertices are included.
     * @param edgeSubset edges to in include in the subgraph. If <code>
     * null</code> then all the edges whose vertices found in the graph
     * are included.
     */
    public DirectedWeightedSubgraph(
        WeightedGraph<V, E> base,
        Set<V> vertexSubset,
        Set<E> edgeSubset)
    {
        super((DirectedGraph<V, E>) base, vertexSubset, edgeSubset);
    }
}

// End DirectedWeightedSubgraph.java
