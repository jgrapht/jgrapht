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
import org.jgrapht.nio.*;
import org.jgrapht.nio.csv.*;
import org.jgrapht.util.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.zip.*;

/**
 * Loads a headerless weighted edge-list CSV (the format produced by
 * {@link GpkgRoadGraphPreprocessor}) into a target {@link Graph}. The actual parsing is
 * delegated to {@link CSVImporter} from {@code jgrapht-io}; this class only adds gzip
 * support and the conventional {@code Integer}-vertex factory.
 *
 * <p>
 * CSV schema (one directed edge per line, no header):
 *
 * <pre>{@code
 * src,dst,weight_m
 * }</pre>
 *
 * <p>
 * Vertices are created on demand from the CSV tokens via {@code Integer::valueOf}.
 * Callers that need a different vertex type can build their own {@link CSVImporter}
 * directly; this class is a convenience for the OSM road-graph case.
 *
 * @author Shai Eilat
 */
public final class OsmCsvGraphLoader
{
    private OsmCsvGraphLoader()
    {
    }

    /**
     * Loads the gzipped edges resource into a freshly created
     * {@link SimpleDirectedWeightedGraph}{@code <Integer, DefaultWeightedEdge>}. Reads
     * directly from a classpath resource looked up relative to the given anchor class.
     *
     * @param anchor anchor class for {@link Class#getResourceAsStream}
     * @param resource resource path (must point at a gzipped CSV)
     * @return the loaded graph
     * @throws IOException on missing resource or parse error
     */
    public static Graph<Integer, DefaultWeightedEdge> loadGzippedResource(
        Class<?> anchor, String resource) throws IOException
    {
        InputStream in = anchor.getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("missing resource: " + resource);
        }
        try (InputStream owned = in) {
            return loadGzipped(owned);
        }
    }

    /**
     * Loads the gzipped edges file into a freshly created
     * {@link SimpleDirectedWeightedGraph}{@code <Integer, DefaultWeightedEdge>}.
     *
     * @param path file path (must point at a gzipped CSV)
     * @return the loaded graph
     * @throws IOException on parse error
     */
    public static Graph<Integer, DefaultWeightedEdge> loadGzippedFile(Path path)
        throws IOException
    {
        try (InputStream in = Files.newInputStream(path)) {
            return loadGzipped(in);
        }
    }

    /**
     * Loads the gzipped edges stream into a freshly created
     * {@link SimpleDirectedWeightedGraph}{@code <Integer, DefaultWeightedEdge>}. The
     * caller closes the stream.
     *
     * @param gzippedCsv gzipped CSV input stream
     * @return the loaded graph
     * @throws IOException on parse error
     */
    public static Graph<Integer, DefaultWeightedEdge> loadGzipped(InputStream gzippedCsv)
        throws IOException
    {
        SimpleDirectedWeightedGraph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
        loadGzippedInto(gzippedCsv, graph);
        return graph;
    }

    /**
     * Loads the gzipped edges stream into the caller-supplied graph. Useful when the
     * caller has pre-added vertices in a specific order, or wants a different graph
     * concrete type (e.g. a directed multigraph).
     *
     * @param gzippedCsv gzipped CSV input stream (caller closes)
     * @param graph the destination graph
     * @throws IOException on parse error
     */
    public static void loadGzippedInto(
        InputStream gzippedCsv, Graph<Integer, DefaultWeightedEdge> graph) throws IOException
    {
        try (Reader r = new InputStreamReader(
            new GZIPInputStream(gzippedCsv), StandardCharsets.UTF_8))
        {
            CSVImporter<Integer, DefaultWeightedEdge> importer =
                new CSVImporter<>(CSVFormat.EDGE_LIST);
            importer.setParameter(CSVFormat.Parameter.EDGE_WEIGHTS, true);
            importer.setVertexFactory(Integer::valueOf);
            try {
                importer.importGraph(graph, r);
            } catch (ImportException ex) {
                throw new IOException("failed to import OSM edge list", ex);
            }
        }
    }
}
