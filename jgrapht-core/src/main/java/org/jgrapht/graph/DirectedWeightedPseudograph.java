/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2013, by Barak Naveh and Contributors.
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
/* -------------------------------
 * DirectedWeightedPseudograph.java
 * -------------------------------
 * (C) Copyright 2003-2013, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer, Adam Gouge
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Aug-2003 : Initial revision (BN);
 * 06-Jun-2005 : Made generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 * 08-Apr-2013 : Added DirectedWeightedPseudograph (AG)
 */
package org.jgrapht.graph;

import org.jgrapht.*;

/**
 * A directed weighted pseudograph. A directed weighted pseudograph is a
 * non-simple directed graph in which both graph loops and multiple edges are
 * permitted, and edges have weights.
 */
public class DirectedWeightedPseudograph<V, E>
        extends DirectedPseudograph<V, E>
        implements WeightedGraph<V, E> {

    private static final long serialVersionUID = 8762514879586423517L;

    /**
     * Creates a new directed weighted pseudograph.
     *
     * @param edgeClass class on which to base factory for edges
     */
    public DirectedWeightedPseudograph(Class<? extends E> edgeClass) {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }

    /**
     * Creates a new directed weighted pseudograph with the specified edge
     * factory.
     *
     * @param ef the edge factory of the new graph.
     */
    public DirectedWeightedPseudograph(EdgeFactory<V, E> ef) {
        super(ef);
    }
}
