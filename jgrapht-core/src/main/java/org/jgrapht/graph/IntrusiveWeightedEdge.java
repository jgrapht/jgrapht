/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.graph;

import org.jgrapht.*;

/**
 * IntrusiveEdge extension for weighted edges. IntrusiveWeightedEdge encapsulates the internals for
 * the default weighted edge implementation. It is not intended to be referenced directly (which is
 * why it's not public); use DefaultWeightedEdge for that.
 *
 * @author Dimitrios Michail
 */
class IntrusiveWeightedEdge
    extends
    IntrusiveEdge
{
    private static final long serialVersionUID = 2890534758523920741L;

    double weight = Graph.DEFAULT_EDGE_WEIGHT;

}
