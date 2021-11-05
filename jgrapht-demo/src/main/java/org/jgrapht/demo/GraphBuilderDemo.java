/*
 * (C) Copyright 2018-2021, by John Sichi and Contributors.
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
package org.jgrapht.demo;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;

/**
 * Demonstrates how to use {@link GraphTypeBuilder} and {@link GraphBuilder}.
 *
 * @author John Sichi
 */
public final class GraphBuilderDemo
{
    /**
     * Main demo entry point.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        Graph<Integer, DefaultEdge> kite = buildKiteGraph();
    }

    /**
     * Builds a simple graph with no vertices.
     *
     * @return a modifiable empty graph instance
     */
    // @example:buildType:begin
    private static Graph<Integer, DefaultEdge> buildEmptySimpleGraph()
    {
        return GraphTypeBuilder
            .<Integer, DefaultEdge> undirected().allowingMultipleEdges(false)
            .allowingSelfLoops(false).edgeClass(DefaultEdge.class).weighted(false).buildGraph();
    }
    // @example:buildType:end

    /**
     * Builds the <a href="http://mathworld.wolfram.com/KiteGraph.html">kite graph</a>.
     *
     * @return an unmodifiable instance of the kite graph
     */
    // @example:buildEdges:begin
    private static Graph<Integer, DefaultEdge> buildKiteGraph()
    {
        return new GraphBuilder<>(buildEmptySimpleGraph())
            .addEdgeChain(1, 2, 3, 4, 1).addEdge(2, 4).addEdge(3, 5).buildAsUnmodifiable();
    }
    // @example:buildEdges:end

}
