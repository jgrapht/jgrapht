/*
 * (C) Copyright 2020-2021, by Semen Chudakov and Contributors.
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
package org.jgrapht.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to manage creation and shutting down instance of the {@link ThreadPoolExecutor}.
 */
public class ConcurrencyUtil
{
    /**
     * Creates a {@link ThreadPoolExecutor} with fixed number of threads which is equal to
     * {@code parallelism}.
     *
     * @param parallelism number of thread for the executor
     * @return created executor
     */
    public static ThreadPoolExecutor createThreadPoolExecutor(int parallelism)
    {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(parallelism);
    }

    /**
     * Shuts down the {@code executor}. This operation puts the {@code service} into a state where
     * every subsequent task submitted to the {@code service} will be rejected. This method calls
     * {@link #shutdownExecutionService(ExecutorService, long, TimeUnit)} with $time =
     * Long.MAX_VALUE$ and $timeUnit = TimeUnit.MILLISECONDS$.
     *
     * @param service service to be shut down
     */
    public static void shutdownExecutionService(ExecutorService service)
        throws InterruptedException
    {
        shutdownExecutionService(service, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    /**
     * Shuts down the {@code executor}. This operation puts the {@code service} into a state where
     * every subsequent task submitted to the {@code service} will be rejected.
     *
     * @param service service to be shut down
     * @param time period of time to wait for the completion of the termination
     * @param timeUnit time duration granularity for the provided {@code time}
     */
    public static void shutdownExecutionService(
        ExecutorService service, long time, TimeUnit timeUnit)
        throws InterruptedException
    {
        service.shutdown();
        service.awaitTermination(time, timeUnit);
    }
}
