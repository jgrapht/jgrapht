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
/* ----------------
 * DefaultWeightedEdge.java
 * ----------------
 * (C) Copyright 2006-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 29-May-2006 : Initial revision (JVS);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.WeightedGraph;


/**
 * A default implementation for edges in a {@link WeightedGraph}. All access to
 * the weight of an edge must go through the graph interface, which is why this
 * class doesn't expose any public methods.
 *
 * @author John V. Sichi
 */
public class DefaultWeightedEdge
    extends DefaultEdge
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = 229708706467350994L;

    //~ Instance fields --------------------------------------------------------

    double weight = WeightedGraph.DEFAULT_EDGE_WEIGHT;

    //~ Methods ----------------------------------------------------------------

    /**
     * Retrieves the weight of this edge. This is protected, for use by
     * subclasses only (e.g. for implementing toString).
     *
     * @return weight of this edge
     */
    protected double getWeight()
    {
        return weight;
    }
}

// End DefaultWeightedEdge.java
