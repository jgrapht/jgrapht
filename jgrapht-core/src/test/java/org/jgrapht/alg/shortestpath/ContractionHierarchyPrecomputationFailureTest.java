/*
 * (C) Copyright 2026-2026, by simpleqt and Contributors.
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

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests which verify that worker failures of the
 * {@link ContractionHierarchyPrecomputation} algorithm are propagated to the caller instead of
 * being swallowed.
 */
public class ContractionHierarchyPrecomputationFailureTest
{
    /**
     * Builds a directed grid graph which is large enough that the precomputation is still running
     * when the calling thread gets interrupted.
     */
    private static DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> gridGraph(int n)
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> graph =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
        for (int i = 0; i < n * n; i++) {
            graph.addVertex(i);
        }
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                int v = row * n + col;
                if (col + 1 < n) {
                    graph.addEdge(v, v + 1);
                }
                if (row + 1 < n) {
                    graph.addEdge(v, v + n);
                }
                if (col > 0) {
                    graph.addEdge(v, v - 1);
                }
                if (row > 0) {
                    graph.addEdge(v, v - n);
                }
            }
        }
        return graph;
    }

    @Test
    public void interruptedPrecomputationThrowsAndPreservesInterrupt()
        throws Exception
    {
        DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> graph = gridGraph(70);

        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicBoolean interruptPreserved = new AtomicBoolean();
        ExecutorService callerService = Executors.newSingleThreadExecutor();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        try {
            ContractionHierarchyPrecomputation<Integer, DefaultWeightedEdge> precomputation =
                new ContractionHierarchyPrecomputation<>(graph, executor);
            callerService.submit(() -> {
                try {
                    precomputation.computeContractionHierarchy();
                } catch (Throwable t) {
                    thrown.set(t);
                    interruptPreserved.set(Thread.currentThread().isInterrupted());
                }
            });
            Thread.sleep(500); // the precomputation needs several seconds for this graph
            callerService.shutdownNow(); // interrupts the thread waiting for the workers
            assertTrue(callerService.awaitTermination(120, TimeUnit.SECONDS));
            assertInstanceOf(IllegalStateException.class, thrown.get(), () ->
                "expected IllegalStateException but got " + thrown.get());
            assertInstanceOf(
                InterruptedException.class, ((IllegalStateException) thrown.get()).getCause());
            assertTrue(interruptPreserved.get());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
