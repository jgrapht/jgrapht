/*
 * (C) Copyright 2018-2019, by Dimitrios Michail and Contributors.
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

import org.jgrapht.Graph;
import org.jgrapht.alg.drawing.model.LayoutModel;
import org.jgrapht.alg.drawing.model.Point;
import org.jgrapht.alg.drawing.model.Box;

/**
 * A general interface for a layout algorithm.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @param <N> the number type
 * @param <P> the point type
 * @param <R> the box type
 */
public interface LayoutAlgorithm<V, E, N extends Number, P extends Point<N>, R extends Box<N>>
{

    /**
     * Layout a graph.
     * 
     * @param graph the graph
     * @param model the layout model to use
     */
    void layout(Graph<V, E> graph, LayoutModel<V, N, P, R> model);

}
