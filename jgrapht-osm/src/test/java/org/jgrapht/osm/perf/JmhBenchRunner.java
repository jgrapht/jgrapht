/*
 * (C) Copyright 2026-2026, by Shai Eilat and Contributors.
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
package org.jgrapht.osm.perf;

import org.openjdk.jmh.results.format.*;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

/**
 * Small helper to run a JMH benchmark class programmatically and write its summary to a
 * dedicated output file. Used by {@link AndorraBenchSuite} so the OSM benches can be
 * dispatched through surefire ({@code -Dtest=AndorraBenchSuite#runM2M}) and inherit the
 * surefire JVM's {@code argLine}.
 *
 * <p>
 * The runner uses {@code forks=0} so the bench runs in-process. For publication-quality
 * numbers, prefer launching via {@code org.openjdk.jmh.Main} from the shell with
 * {@code forks=1}.
 *
 * @author Shai Eilat
 */
public final class JmhBenchRunner
{
    private JmhBenchRunner()
    {
    }

    /**
     * Runs the given bench class in {@link org.openjdk.jmh.annotations.Mode#AverageTime}
     * with three warmup iterations of 5 s and five measurement iterations of 10 s.
     *
     * @param benchClass the JMH bench class
     * @param resultsFile destination for the JMH text summary
     * @throws RunnerException if JMH fails
     * @throws IOException if the output directory cannot be created
     */
    public static void runAverageTime(Class<?> benchClass, Path resultsFile)
        throws RunnerException, IOException
    {
        run(
            benchClass, resultsFile, 3, 5, TimeValue.seconds(5), TimeValue.seconds(10));
    }

    /**
     * Runs the given bench class in single-shot mode (zero warmup, one measurement).
     * Suitable for benchmarks whose per-iteration cost is large (tens of seconds and up).
     *
     * @param benchClass the JMH bench class
     * @param resultsFile destination for the JMH text summary
     * @throws RunnerException if JMH fails
     * @throws IOException if the output directory cannot be created
     */
    public static void runSingleShot(Class<?> benchClass, Path resultsFile)
        throws RunnerException, IOException
    {
        run(benchClass, resultsFile, 0, 1, null, null);
    }

    /**
     * Runs the given bench class with caller-supplied warmup / measurement iteration
     * counts and times. Pass {@code null} for either {@link TimeValue} to leave the
     * bench class's annotated defaults in effect.
     *
     * @param benchClass the JMH bench class
     * @param resultsFile destination for the JMH text summary
     * @param warmupIters warmup iteration count (0 for none)
     * @param measureIters measurement iteration count (must be at least 1)
     * @param warmupTime per-iteration warmup duration, or {@code null} to use annotated
     *        default
     * @param measureTime per-iteration measurement duration, or {@code null} to use
     *        annotated default
     * @throws RunnerException if JMH fails
     * @throws IOException if the output directory cannot be created
     */
    public static void run(
        Class<?> benchClass,
        Path resultsFile,
        int warmupIters,
        int measureIters,
        TimeValue warmupTime,
        TimeValue measureTime)
        throws RunnerException, IOException
    {
        Path parent = resultsFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        OptionsBuilder builder = new OptionsBuilder();
        builder
            .include(".*" + benchClass.getSimpleName() + ".*")
            .forks(0)
            .warmupIterations(warmupIters)
            .measurementIterations(measureIters)
            .shouldFailOnError(true)
            .shouldDoGC(true)
            .timeUnit(TimeUnit.MILLISECONDS)
            .resultFormat(ResultFormatType.TEXT)
            .result(resultsFile.toString());
        if (warmupTime != null && warmupIters > 0) {
            builder.warmupTime(warmupTime);
        }
        if (measureTime != null && measureIters > 0) {
            builder.measurementTime(measureTime);
        }
        new Runner(builder.build()).run();
    }
}
