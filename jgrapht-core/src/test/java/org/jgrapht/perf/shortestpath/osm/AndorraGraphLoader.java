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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.perf.util.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;

/**
 * Builds the Andorra OSM road graph used by the {@code osm/Andorra*Bench} JMH harnesses,
 * and serves as a worked example of how to combine
 * {@link WeightedEdgeListCsvReader}, {@link CoordinatesCsvReader}, and
 * {@link HaversineHeuristic} to load any geographic road graph for benchmarking.
 *
 * <p>
 * The source dataset is a Geofabrik "free" GPKG snapshot of OpenStreetMap (Andorra)
 * preprocessed into two gzipped CSV resources expected under
 * {@code src/test/resources/perf/osm/} (the binaries are <em>not</em> committed;
 * contributors generate them locally as described in the README beside that directory):
 *
 * <ul>
 * <li>{@code andorra-edges.csv.gz} &mdash; {@code src,dst,weight_m} per directed edge
 * inside the largest strongly-connected component.</li>
 * <li>{@code andorra-edges.nodes.csv.gz} &mdash; {@code node_id,lat,lon} per vertex.</li>
 * </ul>
 *
 * <p>
 * Both files are written by {@link org.jgrapht.perf.util.GpkgRoadGraphPreprocessor};
 * refer to that class for the GPKG schema and the preprocessing rules (routable
 * {@code fclass} filter, coordinate snapping, oneway handling, parallel-edge dedupe).
 * The preprocessor accepts any free-tier Geofabrik GPKG, so other regions can be loaded
 * the same way.
 *
 * <p>
 * Edge weights are great-circle distances in metres (Haversine, R = 6,371,008.8 m), so
 * a {@link HaversineHeuristic} built from {@link AndorraData#coords} is an admissible A*
 * heuristic for any path query on the loaded graph.
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
     * yet produced the CSVs. See the README under
     * {@code src/test/resources/perf/osm/} for instructions.
     *
     * @return whether the Andorra fixtures are available
     */
    public static boolean isFixtureAvailable()
    {
        return AndorraGraphLoader.class.getResource(EDGES_RESOURCE) != null
            && AndorraGraphLoader.class.getResource(NODES_RESOURCE) != null;
    }

    /**
     * Load the Andorra road graph plus the per-node coordinate map.
     *
     * @return loaded data bundle
     * @throws IllegalStateException if the Andorra CSV fixtures are not on the classpath;
     *         the message points at the README with download and preprocessing
     *         instructions
     */
    public static AndorraData load()
    {
        if (!isFixtureAvailable()) {
            throw new IllegalStateException(
                "Andorra CSV fixtures not on the classpath ("
                    + EDGES_RESOURCE + ", " + NODES_RESOURCE + "). "
                    + "See jgrapht-core/src/test/resources/perf/osm/README.md for "
                    + "download and preprocessing instructions.");
        }
        try {
            Map<Integer, double[]> coords = new CoordinatesCsvReader<Integer>()
                .gzipped(true)
                .readResource(AndorraGraphLoader.class, NODES_RESOURCE);

            // SimpleDirectedWeightedGraph so Eppstein-style algorithms can consume the
            // result; the preprocessor already deduplicates parallel edges so loading
            // does not violate the simple-graph contract. Pre-add vertices in node-id
            // order to keep the vertex set dense and contiguous.
            SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
                new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            List<Integer> ordered = new ArrayList<>(coords.keySet());
            Collections.sort(ordered);
            for (Integer id : ordered) {
                graph.addVertex(id);
            }

            new WeightedEdgeListCsvReader<Integer, DefaultWeightedEdge>(graph)
                .gzipped(true)
                .addMissingVertices(false)
                .readResource(AndorraGraphLoader.class, EDGES_RESOURCE);

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
