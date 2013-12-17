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

import org.jgrapht.*;


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
    

    private static final long serialVersionUID = 229708706467350994L;

    

    double weight = WeightedGraph.DEFAULT_EDGE_WEIGHT;

    

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
