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
package org.jgrapht.alg.clustering;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm.Clustering;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.GraphDelegator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests which verify that worker failures of the {@link OverlappingClustering}
 * algorithm are propagated to the caller instead of being swallowed.
 */
public class OverlappingClusteringFailureTest
{
    @Test
    public void workerFailureReachesCaller()
        throws Exception
    {
        DefaultUndirectedGraph<String, DefaultEdge> base =
            new DefaultUndirectedGraph<>(DefaultEdge.class);
        base.addVertex("s");
        base.addVertex("a");
        base.addVertex("b");
        base.addEdge("s", "a");
        base.addEdge("s", "b");

        Thread caller = Thread.currentThread();
        Graph<String, DefaultEdge> graph = new GraphDelegator<>(base)
        {
            @Override
            public Set<DefaultEdge> outgoingEdgesOf(String vertex)
            {
                if (Thread.currentThread() != caller) {
                    throw new IllegalArgumentException("worker failure");
                }
                return super.outgoingEdgesOf(vertex);
            }
        };

        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        try {
            OverlappingClustering<String, DefaultEdge> algorithm =
                new OverlappingClustering<>(graph, executor, 1);
            IllegalStateException exception =
                assertThrows(IllegalStateException.class, algorithm::getClustering);
            assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    public void interruptedClusteringThrowsAndPreservesInterrupt()
        throws Exception
    {
        DefaultUndirectedGraph<String, DefaultEdge> base =
            new DefaultUndirectedGraph<>(DefaultEdge.class);
        base.addVertex("s");
        base.addVertex("a");
        base.addVertex("b");
        base.addEdge("s", "a");
        base.addEdge("s", "b");
        base.addEdge("a", "b");

        Thread caller = Thread.currentThread();
        CountDownLatch workerStarted = new CountDownLatch(1);
        Graph<String, DefaultEdge> graph = new GraphDelegator<>(base)
        {
            @Override
            public Set<DefaultEdge> outgoingEdgesOf(String vertex)
            {
                if (Thread.currentThread() != caller) {
                    workerStarted.countDown();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return super.outgoingEdgesOf(vertex);
            }
        };

        AtomicReference<Throwable> thrown = new AtomicReference<>();
        AtomicReference<Clustering<String>> result = new AtomicReference<>();
        AtomicBoolean interruptPreserved = new AtomicBoolean();
        ExecutorService callerService = Executors.newSingleThreadExecutor();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        try {
            OverlappingClustering<String, DefaultEdge> algorithm =
                new OverlappingClustering<>(graph, executor, 1);
            callerService.submit(() -> {
                try {
                    result.set(algorithm.getClustering());
                } catch (Throwable t) {
                    thrown.set(t);
                    interruptPreserved.set(Thread.currentThread().isInterrupted());
                }
            });
            assertTrue(workerStarted.await(10, TimeUnit.SECONDS));
            callerService.shutdownNow(); // interrupts the thread waiting for the workers
            assertTrue(callerService.awaitTermination(30, TimeUnit.SECONDS));
            // the interruption must surface as an exception and the interrupt flag must be restored
            assertInstanceOf(IllegalStateException.class, thrown.get(), () ->
                "expected IllegalStateException but got " + thrown.get());
            assertInstanceOf(
                InterruptedException.class, ((IllegalStateException) thrown.get()).getCause());
            assertTrue(interruptPreserved.get());
            assertNull(result.get());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
