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

import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.junit.jupiter.api.Assertions.*;

/** Regression tests for relaxation of equal vertex objects. */
public class DeltaSteppingConcurrencyTest
{
    @Test
    @Timeout(20)
    public void equalDistinctVerticesShareRelaxationMonitor() throws Exception
    {
        var graph = new DirectedWeightedMultigraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        for (String vertex : List.of("source", "long", "short", "target", "sentinel")) {
            graph.addVertex(vertex);
        }
        String longTarget = new String("target");
        String shortTarget = new String("target");
        assertEquals(longTarget, shortTarget);
        assertNotSame(longTarget, shortTarget);
        graph.setEdgeWeight(graph.addEdge("source", "long"), 1.0);
        graph.setEdgeWeight(graph.addEdge("source", "short"), 1.0);
        graph.setEdgeWeight(graph.addEdge("long", longTarget), 9.0);
        var shortEdge = graph.addEdge("short", shortTarget);
        graph.setEdgeWeight(shortEdge, 4.0);
        var sentinelEdge = graph.addEdge("short", "sentinel");
        graph.setEdgeWeight(sentinelEdge, 1.0);

        var longInsertion = new CountDownLatch(1);
        var shortPublished = new CountDownLatch(1);
        var paused = new AtomicBoolean();
        var failure = new AtomicReference<Throwable>();
        var shortReady = new CountDownLatch(1);
        var shortWorker = ThreadLocal.withInitial(() -> false);
        var observedGraph = new GraphDelegator<String, DefaultWeightedEdge>(graph) {
            @Override
            public Set<DefaultWeightedEdge> outgoingEdgesOf(String vertex)
            {
                if (vertex.equals("short")) {
                    shortWorker.set(true);
                    try {
                        if (!longInsertion.await(5, TimeUnit.SECONDS)) {
                            failure.compareAndSet(null, new AssertionError("Long path was not scheduled"));
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        failure.compareAndSet(null, exception);
                    }
                    return new LinkedHashSet<>(List.of(shortEdge, sentinelEdge));
                }
                return super.outgoingEdgesOf(vertex);
            }

            @Override
            public double getEdgeWeight(DefaultWeightedEdge edge)
            {
                // Visiting the next edge proves that the preceding relaxation has returned.
                // The thread-local guard excludes the initial maximum-edge-weight scan.
                if (edge == shortEdge && shortWorker.get()) {
                    shortReady.countDown();
                } else if (edge == sentinelEdge && shortWorker.get()) {
                    shortPublished.countDown();
                }
                return super.getEdgeWeight(edge);
            }
        };
        Comparator<String> comparator = (left, right) -> {
            if (left == longTarget && paused.compareAndSet(false, true)) {
                // Bucket insertion occurs after reading the old distance. With separate
                // monitors, let the shorter path publish before the longer path resumes.
                // With a shared monitor, the shorter relaxation must wait for this one.
                longInsertion.countDown();
                try {
                    if (!shortReady.await(5, TimeUnit.SECONDS)) {
                        failure.compareAndSet(null, new AssertionError("Short path was not scheduled"));
                    } else {
                        // A shared monitor prevents publication until this callback returns.
                        // Give the competing relaxation a chance to finish on the old implementation.
                        shortPublished.await(500, TimeUnit.MILLISECONDS);
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    failure.compareAndSet(null, exception);
                }
            }
            return left.compareTo(right);
        };
        var executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        try {
            var algorithm = new DeltaSteppingShortestPath<>(observedGraph, 10.0, executor, comparator);
            var paths = algorithm.getPaths("source");
            assertNull(failure.get());
            assertTrue(paused.get(), "The controlled interleaving must be exercised");
            assertEquals(0, shortPublished.getCount());
            assertEquals(5.0, paths.getWeight("target"));
            assertEquals(List.of("source", "short", "target"), paths.getPath("target").getVertexList());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
