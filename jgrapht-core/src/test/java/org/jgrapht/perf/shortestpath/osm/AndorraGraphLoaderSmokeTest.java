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
package org.jgrapht.perf.shortestpath.osm;

import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.graph.*;
import org.jgrapht.perf.util.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Smoke test for {@link AndorraGraphLoader}. Verifies the committed test resource
 * decodes to the expected vertex/edge counts, that Dijkstra can route through the
 * resulting graph, and that the {@link HaversineHeuristic} is admissible against the
 * routed weight.
 *
 * @author Shai Eilat
 */
class AndorraGraphLoaderSmokeTest
{
    @Test
    void loadsAndorraRoadGraphAndRoutesThroughIt()
    {
        assumeTrue(
            AndorraGraphLoader.isFixtureAvailable(),
            "Andorra CSV fixtures not on classpath; "
                + "see jgrapht-core/src/test/resources/perf/osm/README.md for setup");

        AndorraGraphLoader.AndorraData data = AndorraGraphLoader.load();

        assertEquals(36618, data.graph.vertexSet().size(), "andorra largest SCC vertex count");
        assertEquals(67354, data.graph.edgeSet().size(), "andorra largest SCC edge count");
        assertEquals(36618, data.coords.size(), "node coordinate map size");

        // pick the two vertices furthest apart by ID — works because the SCC is strongly
        // connected, so a shortest path must exist.
        int source = 0;
        int sink = data.graph.vertexSet().size() - 1;
        DijkstraShortestPath<Integer, DefaultWeightedEdge> dijkstra =
            new DijkstraShortestPath<>(data.graph);
        var path = dijkstra.getPath(source, sink);
        assertNotNull(path, "expected a route from 0 to last node in the largest SCC");
        assertTrue(path.getWeight() > 0, "non-trivial route weight");

        HaversineHeuristic<Integer> heuristic = HaversineHeuristic.ofMap(data.coords);
        double estimate = heuristic.getCostEstimate(source, sink);
        assertTrue(
            estimate <= path.getWeight() + 1e-6,
            "great-circle heuristic must be admissible (h <= true distance)");
    }
}
