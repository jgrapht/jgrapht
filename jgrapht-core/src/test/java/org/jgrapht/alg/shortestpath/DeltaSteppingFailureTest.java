/*
 * (C) Copyright 2026-2026, by lolarun and Contributors.
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

package org.jgrapht.alg.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.Test;
import java.util.Set;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Regression tests for bucket range and worker-failure handling. */
public class DeltaSteppingFailureTest
{
    @Test
    public void quotientBeyondIntegerRangeKeepsNonnegativeBucket() throws Exception
    {
        var graph = new DirectedWeightedMultigraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        for (int vertex = 0; vertex <= 2200; vertex++) graph.addVertex(vertex);
        for (int vertex = 0; vertex < 2200; vertex++) graph.setEdgeWeight(graph.addEdge(vertex, vertex + 1), 1.0);
        var executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        try {
            var paths = new DeltaSteppingShortestPath<>(graph, 1e-6, executor).getPaths(0);
            assertEquals(2148.0, paths.getWeight(2148));
            assertEquals(2200.0, paths.getWeight(2200));
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    public void workerFailureReachesCaller() throws Exception
    {
        var base = new DirectedWeightedMultigraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        for (int vertex = 0; vertex < 4; vertex++) base.addVertex(vertex);
        for (int vertex = 0; vertex < 3; vertex++) base.setEdgeWeight(base.addEdge(vertex, vertex + 1), 1.0);
        Thread caller = Thread.currentThread();
        Graph<Integer, DefaultWeightedEdge> graph = new GraphDelegator<>(base) {
            @Override public Set<DefaultWeightedEdge> outgoingEdgesOf(Integer vertex)
            {
                if (vertex == 1 && Thread.currentThread() != caller) {
                    throw new IllegalArgumentException("worker failure");
                }
                return super.outgoingEdgesOf(vertex);
            }
        };
        var executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        try {
            var algorithm = new DeltaSteppingShortestPath<>(graph, 1.0, executor);
            var exception = assertThrows(IllegalStateException.class, () -> algorithm.getPaths(0));
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
