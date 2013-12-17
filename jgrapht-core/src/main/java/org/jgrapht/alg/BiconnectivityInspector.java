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
/* -------------------------
 * BiconnectivityInspector.java
 * -------------------------
 * (C) Copyright 2007-2008, by France Telecom
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
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;


/**
 * Inspects a graph for the biconnectivity property. See {@link
 * BlockCutpointGraph} for more information. A biconnected graph has only one
 * block (i.e. no cutpoints).
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
public class BiconnectivityInspector<V, E>
{
    

    private BlockCutpointGraph<V, E> blockCutpointGraph;

    

    /**
     * Running time = O(m) where m is the number of edges.
     */
    public BiconnectivityInspector(UndirectedGraph<V, E> graph)
    {
        super();
        this.blockCutpointGraph = new BlockCutpointGraph<V, E>(graph);
    }

    

    /**
     * Returns the biconnected vertex-components of the graph.
     */
    public Set<Set<V>> getBiconnectedVertexComponents()
    {
        Set<Set<V>> biconnectedVertexComponents = new HashSet<Set<V>>();
        for (
            Iterator<UndirectedGraph<V, E>> iter =
                this.blockCutpointGraph.vertexSet().iterator();
            iter.hasNext();)
        {
            UndirectedGraph<V, E> subgraph = iter.next();
            if (!subgraph.edgeSet().isEmpty()) {
                biconnectedVertexComponents.add(subgraph.vertexSet());
            }
        }

        return biconnectedVertexComponents;
    }

    /**
     * Returns the biconnected vertex-components containing the vertex. A
     * biconnected vertex-component contains all the vertices in the component.
     * A vertex which is not a cutpoint is contained in exactly one component. A
     * cutpoint is contained is at least 2 components.
     *
     * @param vertex
     *
     * @return set of all biconnected vertex-components containing the vertex.
     */
    public Set<Set<V>> getBiconnectedVertexComponents(V vertex)
    {
        Set<Set<V>> vertexComponents = new HashSet<Set<V>>();
        for (
            Iterator<Set<V>> iter = getBiconnectedVertexComponents().iterator();
            iter.hasNext();)
        {
            Set<V> vertexComponent = iter.next();
            if (vertexComponent.contains(vertex)) {
                vertexComponents.add(vertexComponent);
            }
        }
        return vertexComponents;
    }

    /**
     * Returns the cutpoints of the graph.
     */
    public Set<V> getCutpoints()
    {
        return this.blockCutpointGraph.getCutpoints();
    }

    /**
     * Returns <code>true</code> if the graph is biconnected (no cutpoint),
     * <code>false</code> otherwise.
     */
    public boolean isBiconnected()
    {
        return this.blockCutpointGraph.vertexSet().size() == 1;
    }
}

// End BiconnectivityInspector.java
