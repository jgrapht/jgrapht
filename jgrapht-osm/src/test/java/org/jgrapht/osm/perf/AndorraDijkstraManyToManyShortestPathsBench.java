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

import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;
import org.jgrapht.osm.*;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Andorra-OSM benchmark template for {@link DijkstraManyToManyShortestPaths}.
 *
 * <p>
 * Exercises the full many-to-many computation followed by a single-source view, on the
 * Andorra strongly-connected component (~36-37k vertices / ~67k edges depending on the
 * upstream Geofabrik snapshot). Serves as a worked example of how to plug
 * {@link AndorraGraphLoader} into a JMH harness: load the graph once per trial, refresh
 * the (source, target) sets each iteration, and time the algorithm. Contributors can
 * copy this class as a starting point for their own many-to-many or all-pairs
 * shortest-path benchmarks on real road graphs.
 *
 * @author Shai Eilat
 */
@BenchmarkMode(Mode.AverageTime)
@Fork(value = 1, warmups = 0, jvmArgs = {
    "--add-opens=org.jgrapht.osm/org.jgrapht.osm.perf=ALL-UNNAMED",
    "--add-opens=org.jgrapht.osm/org.jgrapht.osm.perf.jmh_generated=ALL-UNNAMED",
    "--add-exports=org.jgrapht.osm/org.jgrapht.osm.perf=ALL-UNNAMED",
    "--add-exports=org.jgrapht.osm/org.jgrapht.osm.perf.jmh_generated=ALL-UNNAMED"
})
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 3, time = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class AndorraDijkstraManyToManyShortestPathsBench
{

    @Benchmark
    public SingleSourcePaths<Integer, DefaultWeightedEdge> manyToManyThenGetPaths(
        AndorraM2MState s)
    {
        DijkstraManyToManyShortestPaths<Integer, DefaultWeightedEdge> alg =
            new DijkstraManyToManyShortestPaths<>(s.data.graph);
        alg.getManyToManyPaths(s.sources, s.targets);
        return alg.getPaths(s.sources.iterator().next());
    }

    @State(Scope.Benchmark)
    public static class AndorraM2MState
    {
        @Param({ "2", "8" })
        int sourcesCount;
        @Param({ "8" })
        int targetsCount;

        AndorraGraphLoader.AndorraData data;
        Set<Integer> sources;
        Set<Integer> targets;

        @Setup(Level.Trial)
        public void load()
        {
            data = AndorraGraphLoader.load();
        }

        @Setup(Level.Iteration)
        public void buildEndpoints()
        {
            int v = data.graph.vertexSet().size();
            Random rng = new Random(11L);
            sources = new HashSet<>();
            while (sources.size() < sourcesCount) {
                sources.add(rng.nextInt(v));
            }
            targets = new HashSet<>();
            while (targets.size() < targetsCount) {
                targets.add(rng.nextInt(v));
            }
        }
    }
}
