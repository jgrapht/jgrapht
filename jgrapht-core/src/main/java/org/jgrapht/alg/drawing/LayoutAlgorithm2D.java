/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.drawing;

import org.jgrapht.*;
import org.jgrapht.alg.drawing.model.*;

/**
 * A general interface for a layout 2D algorithm.
 * 
 * A layout algorithm takes as input a graph and computes point coordinates for each of the graph
 * vertices. Details such as the dimensions of the drawable area, the storage of the vertices'
 * coordinates, etc. are provided using a {@link LayoutModel2D}.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public interface LayoutAlgorithm2D<V, E>
{

    /**
     * Layout a graph.
     * 
     * @param graph the graph
     * @param model the layout model to use
     */
    void layout(Graph<V, E> graph, LayoutModel2D<V> model);

}
