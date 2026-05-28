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
import org.jgrapht.graph.*;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.jgrapht.osm.TestStreams.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link OsmCsvGraphLoader}.
 *
 * @author Shai Eilat
 */
class OsmCsvGraphLoaderTest
{

    @Test
    void loadsHeaderlessWeightedEdgeList() throws IOException
    {
        Graph<Integer, DefaultWeightedEdge> g =
            OsmCsvGraphLoader.loadGzipped(gzipOf("0,1,2.5\n1,2,7.0\n"));

        assertEquals(3, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertEquals(2.5, g.getEdgeWeight(g.getEdge(0, 1)), 0.0);
        assertEquals(7.0, g.getEdgeWeight(g.getEdge(1, 2)), 0.0);
    }

    @Test
    void loadsBidirectionalRoadEdges() throws IOException
    {
        // Mirrors the GpkgRoadGraphPreprocessor output: each undirected road becomes a
        // pair of directed edges.
        Graph<Integer, DefaultWeightedEdge> g =
            OsmCsvGraphLoader.loadGzipped(gzipOf("0,1,7.14\n1,0,7.14\n"));

        assertEquals(2, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertNotNull(g.getEdge(0, 1));
        assertNotNull(g.getEdge(1, 0));
        assertEquals(7.14, g.getEdgeWeight(g.getEdge(0, 1)), 0.0);
    }

    @Test
    void loadsIntoCallerSuppliedGraph() throws IOException
    {
        SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> g =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        // Pre-add the vertex set to keep ids dense and ordered.
        for (int i = 0; i <= 2; i++) {
            g.addVertex(i);
        }

        OsmCsvGraphLoader.loadGzippedInto(gzipOf("0,1,1.0\n1,2,2.0\n"), g);

        assertEquals(3, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
    }

    @Test
    void throwsOnMissingResource()
    {
        assertThrows(
            IOException.class,
            () -> OsmCsvGraphLoader.loadGzippedResource(getClass(), "/does-not-exist.csv.gz"));
    }

}
