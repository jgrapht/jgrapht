/*
 * (C) Copyright 2011-2021, by Assaf Mizrachi and Contributors.
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
package org.jgrapht.generate;

import org.jgrapht.*;

import java.util.*;

/**
 * Generates a bidirectional <a href="http://mathworld.wolfram.com/GridGraph.html">grid graph</a> of
 * any size. A grid graph is a two dimensional graph whose vertices correspond to the points in the
 * plane with integer coordinates, x-coordinates being in the range 0,..., n, y-coordinates being in
 * the range 1,...m, and two vertices are connected by an edge whenever the corresponding points are
 * at distance 1. Vertices are created from left to right and from top to bottom.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Assaf Mizrachi
 */
public class GridGraphGenerator<V, E>
    implements
    GraphGenerator<V, E, V>
{
    /**
     * Role for the vertices at the corners.
     */
    public static final String CORNER_VERTEX = "Corner Vertex";

    private final int rows;
    private final int cols;

    /**
     * Creates a new GridGraphGenerator object with rows x cols dimension.
     *
     * @param rows the number of rows
     * @param cols the number of columns
     */
    public GridGraphGenerator(int rows, int cols)
    {
        if (rows < 2) {
            throw new IllegalArgumentException(
                "illegal number of rows (" + rows + "). there must be at least two.");
        }
        if (cols < 2) {
            throw new IllegalArgumentException(
                "illegal number of columns (" + cols + "). there must be at least two.");
        }
        this.rows = rows;
        this.cols = cols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateGraph(Graph<V, E> target, Map<String, V> resultMap)
    {
        List<V> vertices = new ArrayList<>(rows * cols);

        // Adding all vertices to the set
        int cornerCtr = 0;
        for (int i = 0; i < rows * cols; i++) {
            V vertex = target.addVertex();
            vertices.add(vertex);

            boolean isCorner = (i == 0) || (i == (cols - 1)) || (i == (cols * (rows - 1)))
                || (i == ((rows * cols) - 1));
            if (isCorner && (resultMap != null)) {
                resultMap.put(CORNER_VERTEX + ' ' + ++cornerCtr, vertex);
            }
        }

        // Iterating twice over the key set, for undirected graph edges are
        // added from upper vertices to lower, and from left to right. The
        // second addEdge call will return nothing; it will not add a the edge
        // at the opposite direction. For directed graph, edges in opposite
        // direction are also added.
        for (int i = 1; i <= vertices.size(); i++) {
            for (int j = 1; j <= vertices.size(); j++) {
                if ((((i % cols) > 0) && ((i + 1) == j)) || ((i + cols) == j)) {
                    target.addEdge(vertices.get(i - 1), vertices.get(j - 1));
                    target.addEdge(vertices.get(j - 1), vertices.get(i - 1));
                }
            }
        }
    }
}
