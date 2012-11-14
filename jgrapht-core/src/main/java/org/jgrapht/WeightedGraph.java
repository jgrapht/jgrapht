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
/* ------------------
 * WeightedGraph.java
 * ------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 13-Aug-2003 : Included weight methods in Edge interface (BN);
 * 11-Mar-2004 : Made generic (CH);
 *
 */
package org.jgrapht;

/**
 * An interface for a graph whose edges have non-uniform weights.
 *
 * @author Barak Naveh
 * @since Jul 23, 2003
 */
public interface WeightedGraph<V, E>
    extends Graph<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     * The default weight for an edge.
     */
    double DEFAULT_EDGE_WEIGHT = 1.0;

    //~ Methods ----------------------------------------------------------------

    /**
     * Assigns a weight to an edge.
     *
     * @param e edge on which to set weight
     * @param weight new weight for edge
     */
    void setEdgeWeight(E e, double weight);
}

// End WeightedGraph.java
