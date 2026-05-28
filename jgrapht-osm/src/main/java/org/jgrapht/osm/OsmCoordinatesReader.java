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

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * Reads a headerless {@code node_id,lat,lon} CSV (the companion file produced by
 * {@link GpkgRoadGraphPreprocessor}) into a {@code Map<Integer, double[]>} keyed by node
 * id, with each value an array {@code {lat, lon}} in decimal degrees. Pair with
 * {@link HaversineHeuristic#ofMap(Map)} for A* heuristics over the loaded graph.
 *
 * <p>
 * Edge / node CSVs are intentionally separate files: the edge list goes through
 * {@code CSVImporter} (which is graph-shaped), and the coordinate table is a flat
 * key-value resource that does not fit the importer interface.
 *
 * @author Shai Eilat
 */
public final class OsmCoordinatesReader
{
    private OsmCoordinatesReader()
    {
    }

    /**
     * Reads the gzipped coordinates resource into a map.
     *
     * @param anchor anchor class for {@link Class#getResourceAsStream}
     * @param resource resource path (must point at a gzipped CSV)
     * @return a map from node id to {@code {lat, lon}}
     * @throws IOException on missing resource or parse error
     */
    public static Map<Integer, double[]> readGzippedResource(
        Class<?> anchor, String resource) throws IOException
    {
        InputStream in = anchor.getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("missing resource: " + resource);
        }
        try (InputStream owned = in) {
            return readGzipped(owned);
        }
    }

    /**
     * Reads the gzipped coordinates file into a map.
     *
     * @param path file path (must point at a gzipped CSV)
     * @return a map from node id to {@code {lat, lon}}
     * @throws IOException on parse error
     */
    public static Map<Integer, double[]> readGzippedFile(Path path) throws IOException
    {
        try (InputStream in = Files.newInputStream(path)) {
            return readGzipped(in);
        }
    }

    /**
     * Reads the gzipped coordinates stream into a map. The caller closes the stream.
     *
     * @param gzippedCsv gzipped CSV input stream
     * @return a map from node id to {@code {lat, lon}}
     * @throws IOException on parse error
     */
    public static Map<Integer, double[]> readGzipped(InputStream gzippedCsv)
        throws IOException
    {
        Map<Integer, double[]> out = new LinkedHashMap<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
            new GZIPInputStream(gzippedCsv), StandardCharsets.UTF_8)))
        {
            String line;
            long lineNo = 0;
            while ((line = r.readLine()) != null) {
                lineNo++;
                if (line.isEmpty()) {
                    continue;
                }
                try {
                    parseLine(line, out);
                } catch (RuntimeException ex) {
                    throw new IOException(
                        "failed to parse coordinate at line " + lineNo + ": " + line, ex);
                }
            }
        }
        return out;
    }

    private static void parseLine(String line, Map<Integer, double[]> out)
    {
        int d1 = line.indexOf(',');
        if (d1 < 0) {
            throw new IllegalArgumentException("missing first delimiter");
        }
        int d2 = line.indexOf(',', d1 + 1);
        if (d2 < 0) {
            throw new IllegalArgumentException("missing second delimiter");
        }
        int id = Integer.parseInt(line.substring(0, d1).trim());
        double lat = Double.parseDouble(line.substring(d1 + 1, d2));
        double lon = Double.parseDouble(line.substring(d2 + 1));
        out.put(id, new double[] { lat, lon });
    }
}
