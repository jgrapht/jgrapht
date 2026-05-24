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
package org.jgrapht.osm;

import org.jgrapht.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end smoke test against the tiny committed sample fixture
 * ({@code sample-edges.csv.gz} + {@code sample-nodes.csv.gz}), which always runs &mdash;
 * no contributor setup required. Exercises {@link OsmCsvGraphLoader},
 * {@link OsmCoordinatesReader}, {@link HaversineHeuristic}, and a Dijkstra over the
 * loaded graph in a single round-trip.
 *
 * <p>
 * The fixture is a five-vertex graph at the equator, with edges placed so every
 * direction step is exactly {@code 0.001} degrees (Haversine distance
 * {@code 111.1949} m to four decimals):
 *
 * <pre>
 *           2 (0.001, 0.001)
 *           |
 *  4 ---- 0 ---- 1 ---- 2
 *  (-0.001,0)  (0,0)  (0,0.001)
 *           |
 *           3 (0.001, 0)
 * </pre>
 *
 * Edges (directed pairs, all weight {@code 111.1949}):
 * {@code 0<->1, 1<->2, 2<->3, 3<->0, 0<->4}.
 *
 * @author Shai Eilat
 */
class SampleGraphSmokeTest
{
    private static final double EXPECTED_EDGE_WEIGHT_M = 111.1949;
    private static final double WEIGHT_TOLERANCE = 1e-4;

    @Test
    void loadsSampleFixtureEndToEnd() throws Exception
    {
        Graph<Integer, DefaultWeightedEdge> graph = OsmCsvGraphLoader.loadGzippedResource(
            getClass(), "/perf/osm/sample-edges.csv.gz");
        Map<Integer, double[]> coords = OsmCoordinatesReader.readGzippedResource(
            getClass(), "/perf/osm/sample-nodes.csv.gz");

        assertEquals(5, graph.vertexSet().size(), "sample vertex count");
        assertEquals(10, graph.edgeSet().size(), "sample directed edge count");
        assertEquals(5, coords.size(), "sample coordinate count");

        for (DefaultWeightedEdge e : graph.edgeSet()) {
            assertEquals(
                EXPECTED_EDGE_WEIGHT_M, graph.getEdgeWeight(e), WEIGHT_TOLERANCE,
                "every sample edge is one 0.001-degree step");
        }

        // Dijkstra 0 -> 2: shortest path is two hops (0->1->2 or 0->3->2),
        // total weight = 2 * 111.1949.
        GraphPath<Integer, DefaultWeightedEdge> path =
            new DijkstraShortestPath<>(graph).getPath(0, 2);
        assertNotNull(path, "path 0 -> 2 must exist in the connected sample");
        assertEquals(2, path.getLength(), "shortest path 0 -> 2 has exactly two edges");
        assertEquals(
            2 * EXPECTED_EDGE_WEIGHT_M, path.getWeight(), 2 * WEIGHT_TOLERANCE);

        // Haversine straight-line from 0 to 2 is the diagonal of the unit square,
        // i.e. ~111.1949 * sqrt(2) ~= 157.25 m; admissible against the 222.39 m path.
        HaversineHeuristic<Integer> heuristic = HaversineHeuristic.ofMap(coords);
        double estimate = heuristic.getCostEstimate(0, 2);
        assertTrue(
            estimate <= path.getWeight() + WEIGHT_TOLERANCE,
            "Haversine admissible: h=" + estimate + " <= path=" + path.getWeight());
        assertTrue(estimate > 150.0, "diagonal ~> 150 m, got " + estimate);
        assertTrue(estimate < 160.0, "diagonal ~< 160 m, got " + estimate);
    }
}
