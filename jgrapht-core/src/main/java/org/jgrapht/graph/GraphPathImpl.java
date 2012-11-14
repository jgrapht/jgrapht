/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2009, by Barak Naveh and Contributors.
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
    //~ Instance fields --------------------------------------------------------

    private final Graph<V, E> graph;

    private final List<E> edgeList;

    private final V startVertex;

    private final V endVertex;

    private final double weight;

    //~ Constructors -----------------------------------------------------------

    public GraphPathImpl(
        final Graph<V, E> graph,
        final V startVertex,
        final V endVertex,
        final List<E> edgeList,
        final double weight)
    {
        this.graph = graph;
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        this.edgeList = edgeList;
        this.weight = weight;
    }

    //~ Methods ----------------------------------------------------------------

    // implement GraphPath
    @Override
    public Graph<V, E> getGraph()
    {
        return graph;
    }

    // implement GraphPath
    @Override
    public V getStartVertex()
    {
        return startVertex;
    }

    // implement GraphPath
    @Override
    public V getEndVertex()
    {
        return endVertex;
    }

    // implement GraphPath
    @Override
    public List<E> getEdgeList()
    {
        return edgeList;
    }

    // implement GraphPath
    @Override
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
