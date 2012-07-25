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
    //~ Instance fields --------------------------------------------------------

    private BlockCutpointGraph<V, E> blockCutpointGraph;

    //~ Constructors -----------------------------------------------------------

    /**
     * Running time = O(m) where m is the number of edges.
     */
    public BiconnectivityInspector(UndirectedGraph<V, E> graph)
    {
        super();
        this.blockCutpointGraph = new BlockCutpointGraph<V, E>(graph);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the biconnected vertex-components of the graph.
     */
    public Set<Set<V>> getBiconnectedVertexComponents()
    {
        Set<Set<V>> biconnectedVertexComponents = new HashSet<Set<V>>();
        for (UndirectedGraph<V, E> subgraph : this.blockCutpointGraph
            .vertexSet()) {
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
        for (Set<V> vertexComponent : getBiconnectedVertexComponents()) {
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
