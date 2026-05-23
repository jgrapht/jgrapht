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

import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.osm.AndorraGraphLoader;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Andorra-OSM benchmark for {@link AllDirectedPaths} non-simple mode (i.e. enumerating
 * length-bounded walks). The full Andorra SCC is too large for non-simple-mode walk
 * enumeration; we instead carve out a BFS-radius ball around a random anchor (~80&ndash;250
 * vertices) and enumerate length-bounded walks inside that subgraph.
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
@Measurement(iterations = 3, time = 10)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class AndorraAllDirectedPathsNonSimpleBench
{
    @Benchmark
    public List<GraphPath<Integer, DefaultWeightedEdge>> allDirectedPathsNonSimple(
        AndorraAdpState s)
    {
        AllDirectedPaths<Integer, DefaultWeightedEdge> adp =
            new AllDirectedPaths<>(s.subgraph);
        return adp.getAllPaths(
            Collections.singleton(s.source), Collections.singleton(s.sink), false, s.maxPathLen);
    }

    @State(Scope.Benchmark)
    public static class AndorraAdpState
    {
        /**
         * BFS radius from the urban anchor. OSM road graphs are very sparse
         * forward-wise (typical out-degree 1.3-1.5), so the ball grows roughly
         * as {@code 1.4^radius}; the committed value of 40 with the 1500-vertex
         * size cap reliably yields ~1000 vertices around Andorra la Vella.
         */
        @Param({ "40" })
        int bfsRadius;

        /** Bound on walk length (edges). */
        @Param({ "10" })
        int maxPathLen;

        // Centre of Andorra la Vella (capital, densest part of the country's road network).
        private static final double ANCHOR_LAT = 42.5063;
        private static final double ANCHOR_LON = 1.5218;

        AndorraGraphLoader.AndorraData data;
        AsSubgraph<Integer, DefaultWeightedEdge> subgraph;
        Integer source;
        Integer sink;

        @Setup(Level.Trial)
        public void load()
        {
            data = AndorraGraphLoader.load();

            int anchor = nearestNode(ANCHOR_LAT, ANCHOR_LON);
            Set<Integer> ball = boundedBfsBall(anchor, bfsRadius, /* sizeCap= */ 1500);
            if (ball.size() < 50) {
                throw new IllegalStateException(
                    "urban Andorra ball too small (" + ball.size() + " vertices); the OSM "
                        + "extract may be missing the capital, or the anchor coordinates need "
                        + "to be updated");
            }

            int chosenSink = -1;
            int bestHops = -1;
            for (Integer v : ball) {
                if (v.intValue() == anchor || data.graph.outDegreeOf(v) == 0) {
                    continue;
                }
                int h = bfsHops(anchor, v, bfsRadius);
                if (h > bestHops) {
                    bestHops = h;
                    chosenSink = v;
                }
            }
            if (chosenSink < 0) {
                throw new IllegalStateException(
                    "urban Andorra ball has no reachable non-anchor vertex with out-degree > 0");
            }

            subgraph = new AsSubgraph<>(data.graph, ball);
            source = anchor;
            sink = chosenSink;
            double[] anchorCoords = data.coords.get(anchor);
            System.out.printf(
                "[AndorraAdpState] anchor=%d (%.4f, %.4f) ball=%d sink=%d hops=%d radius=%d "
                    + "maxPathLen=%d%n",
                anchor, anchorCoords[0], anchorCoords[1], ball.size(),
                chosenSink, bestHops, bfsRadius, maxPathLen);
        }

        private int nearestNode(double targetLat, double targetLon)
        {
            int best = -1;
            double bestDist = Double.MAX_VALUE;
            for (Map.Entry<Integer, double[]> entry : data.coords.entrySet()) {
                double[] c = entry.getValue();
                double dLat = c[0] - targetLat;
                double dLon = c[1] - targetLon;
                double d = dLat * dLat + dLon * dLon;
                if (d < bestDist) {
                    bestDist = d;
                    best = entry.getKey();
                }
            }
            return best;
        }

        private Set<Integer> boundedBfsBall(Integer start, int radius, int sizeCap)
        {
            Set<Integer> visited = new HashSet<>();
            ArrayDeque<int[]> q = new ArrayDeque<>();
            q.add(new int[] { start, 0 });
            visited.add(start);
            while (!q.isEmpty() && visited.size() < sizeCap) {
                int[] head = q.poll();
                if (head[1] == radius) {
                    continue;
                }
                for (Integer nbr : Graphs.successorListOf(data.graph, head[0])) {
                    if (visited.size() >= sizeCap) {
                        break;
                    }
                    if (visited.add(nbr)) {
                        q.add(new int[] { nbr, head[1] + 1 });
                    }
                }
            }
            return visited;
        }

        private int bfsHops(Integer src, Integer dst, int radius)
        {
            ArrayDeque<int[]> q = new ArrayDeque<>();
            Set<Integer> visited = new HashSet<>();
            q.add(new int[] { src, 0 });
            visited.add(src);
            while (!q.isEmpty()) {
                int[] head = q.poll();
                if (head[0] == dst) {
                    return head[1];
                }
                if (head[1] == radius) {
                    continue;
                }
                for (Integer nbr : Graphs.successorListOf(data.graph, head[0])) {
                    if (visited.add(nbr)) {
                        q.add(new int[] { nbr, head[1] + 1 });
                    }
                }
            }
            return -1;
        }
    }
}
