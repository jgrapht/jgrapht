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

import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

/** Regression tests for relaxation of equal vertex objects. */
public class DeltaSteppingConcurrencyTest
{
    @Test
    public void equalDistinctVerticesShareRelaxationMonitor() throws Exception
    {
        var graph = new DirectedWeightedMultigraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        graph.addVertex("source");
        graph.addVertex("target");
        var executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        try {
            var algorithm = new DeltaSteppingShortestPath<>(graph, 10.0, executor);
            algorithm.getPaths("source");
            var firstRead = new CountDownLatch(1);
            var secondRead = new CountDownLatch(1);
            var secondPublished = new CountDownLatch(1);
            var map = new ConcurrentHashMap<String, Pair<Double, DefaultWeightedEdge>>() {
                @Override public Pair<Double, DefaultWeightedEdge> get(Object key)
                {
                    var snapshot = super.get(key);
                    try {
                        if (Thread.currentThread().getName().equals("first-relax")) {
                            firstRead.countDown();
                            // A shared monitor prevents the second read until this one completes.
                            if (secondRead.await(1, TimeUnit.SECONDS)) {
                                assertTrue(secondPublished.await(5, TimeUnit.SECONDS));
                            }
                        } else if (Thread.currentThread().getName().equals("second-relax")) {
                            secondRead.countDown();
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(exception);
                    }
                    return snapshot;
                }
                @Override public Pair<Double, DefaultWeightedEdge> put(
                    String key, Pair<Double, DefaultWeightedEdge> value)
                {
                    var previous = super.put(key, value);
                    if (Thread.currentThread().getName().equals("second-relax")) {
                        secondPublished.countDown();
                    }
                    return previous;
                }
            };
            map.put("target", Pair.of(100.0, null));
            Field field = DeltaSteppingShortestPath.class.getDeclaredField("distanceAndPredecessorMap");
            field.setAccessible(true);
            field.set(algorithm, map);
            Method relax = DeltaSteppingShortestPath.class.getDeclaredMethod("relax", Object.class, Object.class, double.class);
            relax.setAccessible(true);
            var failure = new AtomicReference<Throwable>();
            Thread first = new Thread(() -> {
                try { relax.invoke(algorithm, new String("target"), null, 10.0); }
                catch (Throwable exception) { failure.compareAndSet(null, exception); }
            }, "first-relax");
            Thread second = new Thread(() -> {
                try { relax.invoke(algorithm, new String("target"), null, 5.0); }
                catch (Throwable exception) { failure.compareAndSet(null, exception); }
            }, "second-relax");
            first.start();
            assertTrue(firstRead.await(5, TimeUnit.SECONDS));
            second.start();
            first.join(10000);
            second.join(10000);
            assertFalse(first.isAlive());
            assertFalse(second.isAlive());
            assertNull(failure.get());
            assertEquals(5.0, map.get("target").getFirst());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }
}
