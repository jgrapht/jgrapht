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

import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Smoke test for {@link AndorraGraphLoader}. Skips cleanly when the CSV fixtures are
 * absent so the test suite stays green on a clean checkout. When the fixtures are
 * present, asserts internal consistency of the loaded data (coords cover the graph,
 * counts in a sensible range), that Dijkstra can route through it, and that the
 * {@link HaversineHeuristic} is admissible against the routed weight.
 *
 * <p>
 * The test deliberately does <em>not</em> pin exact vertex / edge counts. Geofabrik
 * refreshes the upstream OSM extract daily, so two contributors generating fixtures a
 * day apart can land on slightly different SCC sizes; the prior version of this test
 * hardcoded a single snapshot and failed every time the data drifted.
 *
 * @author Shai Eilat
 */
class AndorraGraphLoaderSmokeTest
{
    /**
     * Lower bound on the Andorra largest-SCC vertex count. Recent snapshots have been
     * around 36-37k; we use 10k as a generous floor that still catches a loader that
     * silently dropped most of the graph.
     */
    private static final int MIN_EXPECTED_VERTICES = 10_000;

    @Test
    void loadsAndorraRoadGraphAndRoutesThroughIt()
    {
        assumeTrue(
            AndorraGraphLoader.isFixtureAvailable(),
            "Andorra CSV fixtures not on classpath; "
                + "see jgrapht-osm/src/test/resources/perf/osm/README.md for setup");

        AndorraGraphLoader.AndorraData data = AndorraGraphLoader.load();

        int vertexCount = data.graph.vertexSet().size();
        int edgeCount = data.graph.edgeSet().size();
        assertTrue(
            vertexCount >= MIN_EXPECTED_VERTICES,
            "Andorra largest SCC vertex count below sanity floor: " + vertexCount);
        assertTrue(
            edgeCount >= vertexCount,
            "road graph should have at least as many directed edges as vertices, "
                + "got " + edgeCount + " edges for " + vertexCount + " vertices");
        assertEquals(
            vertexCount, data.coords.size(),
            "coordinate map must cover the loaded graph one-to-one");
        assertTrue(
            data.coords.keySet().containsAll(data.graph.vertexSet()),
            "every graph vertex must have a coordinate entry");

        int source = 0;
        int sink = vertexCount - 1;
        DijkstraShortestPath<Integer, DefaultWeightedEdge> dijkstra =
            new DijkstraShortestPath<>(data.graph);
        var path = dijkstra.getPath(source, sink);
        assertNotNull(
            path, "expected a route from 0 to the last vertex in the largest SCC");
        assertTrue(path.getWeight() > 0, "non-trivial route weight");

        HaversineHeuristic<Integer> heuristic = HaversineHeuristic.ofMap(data.coords);
        double estimate = heuristic.getCostEstimate(source, sink);
        assertTrue(
            estimate <= path.getWeight() + 1e-6,
            "great-circle heuristic must be admissible (h <= true distance)");
    }
}
