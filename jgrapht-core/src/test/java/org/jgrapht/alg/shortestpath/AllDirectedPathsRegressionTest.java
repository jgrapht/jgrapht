/*
 * (C) Copyright 2003-2025, by Barak Naveh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later.
 */
package org.jgrapht.alg.shortestpath;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class AllDirectedPathsRegressionTest {

    @Test
    public void simplePathsOnCycle_terminatesAndListsOnlySimplePaths() {
        // Graph: A→B→C→A (cycle) plus A→C direct edge
        var g = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex("A"); g.addVertex("B"); g.addVertex("C");
        g.addEdge("A", "B");
        g.addEdge("B", "C");
        g.addEdge("C", "A");  // cycle
        g.addEdge("A", "C");  // direct shortcut

        var alg = new AllDirectedPaths<>(g);

        List<GraphPath<String, DefaultEdge>> paths =
            alg.getAllPaths("A", "C", /* simplePaths = */ true, /* maxPathLength = */ 4);

        assertNotNull(paths);
        var verts = paths.stream()
                .map(GraphPath::getVertexList)
                .collect(Collectors.toList());

        assertTrue(verts.stream().anyMatch(l -> l.equals(List.of("A", "C"))));
        assertTrue(verts.stream().anyMatch(l -> l.equals(List.of("A", "B", "C"))));
        assertEquals(2, paths.size(), "Only the two simple paths should be returned");
    }

    @Test
    public void simplePaths_noCap_mustTerminateQuickly_evenWithCycle() {
        // Same graph: A→B→C→A (cycle) plus A→C direct edge
        var g = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex("A"); g.addVertex("B"); g.addVertex("C");
        g.addEdge("A", "B");
        g.addEdge("B", "C");
        g.addEdge("C", "A");
        g.addEdge("A", "C");

        var alg = new AllDirectedPaths<>(g);

        // If there's a bug, this may hang. Enforce a hard timeout.
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            var paths = alg.getAllPaths("A", "C", /* simplePaths= */ true, /* maxPathLength= */ null);

            var verts = paths.stream()
                    .map(GraphPath::getVertexList)
                    .collect(Collectors.toList());

            assertTrue(verts.stream().anyMatch(l -> l.equals(List.of("A","C"))));
            assertTrue(verts.stream().anyMatch(l -> l.equals(List.of("A","B","C"))));
        });
    }
}
