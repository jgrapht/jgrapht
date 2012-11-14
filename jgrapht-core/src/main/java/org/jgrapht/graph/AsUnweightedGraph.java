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
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/*  ----------------------
 * AsUnweightedGraph.java
 * ----------------------
 * (C) Copyright 2007-2008, by Lucas J. Scharenbroich and Contributors.
 *
 * Original Author:  Lucas J. Scharenbroich
 * Contributor(s):   John V. Sichi
*
 * $Id$
 *
 * Changes
 * -------
 * 7-Sep-2007 : Initial revision (LJS);
 *
 */
package org.jgrapht.graph;

import java.io.*;

import org.jgrapht.*;


/**
 * An unweighted view of the backing weighted graph specified in the
 * constructor. This graph allows modules to apply algorithms designed for
 * unweighted graphs to a weighted graph by simply ignoring edge weights. Query
 * operations on this graph "read through" to the backing graph. Vertex
 * addition/removal and edge addition/removal are all supported (and immediately
 * reflected in the backing graph).
 *
 * <p>Note that edges returned by this graph's accessors are really just the
 * edges of the underlying directed graph.</p>
 *
 * <p>This graph does <i>not</i> pass the hashCode and equals operations through
 * to the backing graph, but relies on <tt>Object</tt>'s <tt>equals</tt> and
 * <tt>hashCode</tt> methods. This graph will be serializable if the backing
 * graph is serializable.</p>
 *
 * @author Lucas J. Scharenbroich
 * @since Sep 7, 2007
 */

public class AsUnweightedGraph<V, E>
    extends GraphDelegator<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = 7175505077601824663L;

    //~ Constructors -----------------------------------------------------------

    /**
     * Constructor for AsUnweightedGraph.
     *
     * @param g the backing graph over which an unweighted view is to be
     * created.
     */
    public AsUnweightedGraph(final Graph<V, E> g)
    {
        super(g);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see Graph#getEdgeWeight
     */
    @Override
    public double getEdgeWeight(final E e)
    {
        return WeightedGraph.DEFAULT_EDGE_WEIGHT;
    }
}

// End AsUnweightedGraph.java
