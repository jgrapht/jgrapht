/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2009, by Barak Naveh and Contributors.
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
/* ----------------
 * GraphPathImpl.java
 * ----------------
 * (C) Copyright 2009-2009, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 03-Jul-2009 : Initial revision (JVS);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;


/**
 * GraphPathImpl is a default implementation of {@link GraphPath}.
 *
 * @author John Sichi
 * @version $Id$
 */
public class GraphPathImpl<V, E>
    implements GraphPath<V, E>
{
    

    private Graph<V, E> graph;

    private List<E> edgeList;

    private V startVertex;

    private V endVertex;

    private double weight;

    

    public GraphPathImpl(
        Graph<V, E> graph,
        V startVertex,
        V endVertex,
        List<E> edgeList,
        double weight)
    {
        this.graph = graph;
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.edgeList = edgeList;
        this.weight = weight;
    }

    

    // implement GraphPath
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    // implement GraphPath
    public V getStartVertex()
    {
        return startVertex;
    }

    // implement GraphPath
    public V getEndVertex()
    {
        return endVertex;
    }

    // implement GraphPath
    public List<E> getEdgeList()
    {
        return edgeList;
    }

    // implement GraphPath
    public double getWeight()
    {
        return weight;
    }

    // override Object
    public String toString()
    {
        return edgeList.toString();
    }
}

// End GraphPathImpl.java
