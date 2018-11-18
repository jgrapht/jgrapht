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

import org.jgrapht.alg.drawing.model.Point2D;
import org.jgrapht.alg.drawing.model.Rectangle2D;

/**
 * A general interface for a 2d layout algorithm.
 * 
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * @param <N> the number type
 */
public interface LayoutAlgorithm2D<V, E, N extends Number>
    extends
    LayoutAlgorithm<V, E, N, Point2D<N>, Rectangle2D<N>>
{
}
