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
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;

/**
 * Test fixture helper that loads the Andorra OSM road graph from the gzipped CSVs
 * expected under {@code jgrapht-osm/src/test/resources/perf/osm/}, and serves as a
 * worked example of how to compose {@link OsmCsvGraphLoader},
 * {@link OsmCoordinatesReader}, and {@link HaversineHeuristic}. Used by the bench
 * classes in {@code org.jgrapht.osm.perf} and by the smoke test.
 *
 * <p>
 * The CSVs are <em>not</em> committed; contributors generate them locally from a
 * Geofabrik free-tier GPKG using {@link GpkgRoadGraphPreprocessor}. See the README
 * beside the {@code src/test/resources/perf/osm/} directory for download and
 * preprocessing instructions.
 *
 * @author Shai Eilat
 */
public final class AndorraGraphLoader
{
    /** Edges CSV resource path (relative to test resources). */
    public static final String EDGES_RESOURCE = "/perf/osm/andorra-edges.csv.gz";
    /** Node coordinates CSV resource path (relative to test resources). */
    public static final String NODES_RESOURCE = "/perf/osm/andorra-edges.nodes.csv.gz";

    private AndorraGraphLoader()
    {
    }

    /**
     * Returns {@code true} when both Andorra fixtures are present on the classpath, so
     * tests and benches that depend on them can skip cleanly when a contributor has not
     * yet produced the CSVs.
     *
     * @return whether the Andorra fixtures are available
     */
    public static boolean isFixtureAvailable()
    {
        return AndorraGraphLoader.class.getResource(EDGES_RESOURCE) != null
            && AndorraGraphLoader.class.getResource(NODES_RESOURCE) != null;
    }

    /**
     * Loads the Andorra road graph plus the per-node coordinate map. Vertices are
     * pre-added in node-id order so the graph's vertex set is the dense range
     * {@code 0 .. N-1}, which the bench classes rely on for {@code rng.nextInt(N)}
     * sampling.
     *
     * @return loaded data bundle
     * @throws IllegalStateException if the Andorra CSV fixtures are not on the
     *         classpath; the message points at the README
     */
    public static AndorraData load()
    {
        if (!isFixtureAvailable()) {
            throw new IllegalStateException(
                "Andorra CSV fixtures not on the classpath ("
                    + EDGES_RESOURCE + ", " + NODES_RESOURCE + "). "
                    + "See jgrapht-osm/src/test/resources/perf/osm/README.md for "
                    + "download and preprocessing instructions.");
        }
        try {
            Map<Integer, double[]> coords =
                OsmCoordinatesReader.readGzippedResource(
                    AndorraGraphLoader.class, NODES_RESOURCE);

            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
                new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            List<Integer> ordered = new ArrayList<>(coords.keySet());
            Collections.sort(ordered);
            for (Integer id : ordered) {
                graph.addVertex(id);
            }

            try (java.io.InputStream in =
                AndorraGraphLoader.class.getResourceAsStream(EDGES_RESOURCE))
            {
                if (in == null) {
                    throw new IOException("missing resource: " + EDGES_RESOURCE);
                }
                OsmCsvGraphLoader.loadGzippedInto(in, graph);
            }

            return new AndorraData(graph, coords);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Loaded graph + node coordinates. */
    public static final class AndorraData
    {
        /** The loaded road graph. */
        public final Graph<Integer, DefaultWeightedEdge> graph;
        /** Node coordinates keyed by node id; values are {@code {lat, lon}}. */
        public final Map<Integer, double[]> coords;

        AndorraData(Graph<Integer, DefaultWeightedEdge> graph, Map<Integer, double[]> coords)
        {
            this.graph = graph;
            this.coords = coords;
        }
    }
}
