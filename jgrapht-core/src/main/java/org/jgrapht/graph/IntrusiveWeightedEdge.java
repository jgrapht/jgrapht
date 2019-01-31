/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
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
