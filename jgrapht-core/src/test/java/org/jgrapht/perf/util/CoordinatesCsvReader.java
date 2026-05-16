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
package org.jgrapht.perf.util;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.zip.*;

/**
 * Loads a {@code vertex,lat,lon} coordinate table from a CSV stream into a
 * {@link Map}{@code <V, double[]>}. The returned array is {@code {lat, lon}}; pair this
 * with {@link HaversineHeuristic} for geographic-graph A* heuristics or any other
 * coordinate-driven analysis.
 *
 * <p>
 * Configurable via a fluent builder:
 * <ul>
 *   <li>{@link #vertexParser(Function)} — how to parse a vertex token (default
 *       {@code Integer.valueOf}).</li>
 *   <li>{@link #gzipped(boolean)} — wrap the stream in {@link GZIPInputStream}.</li>
 *   <li>{@link #hasHeader(boolean)} — skip the first non-empty line as a header.</li>
 *   <li>{@link #delimiter(char)} — field delimiter (default {@code ','}).</li>
 * </ul>
 *
 * <p>
 * The reader is intended for trusted, machine-generated input; parse failures raise
 * {@link IOException} with the offending line number.
 *
 * @param <V> the vertex type
 *
 * @author Shai Eilat
 */
public final class CoordinatesCsvReader<V>
{
    private Function<String, V> vertexParser = CoordinatesCsvReader::defaultParseInteger;
    private boolean gzipped = false;
    private boolean hasHeader = true;
    private char delimiter = ',';

    /**
     * Sets the vertex token parser. The default parses tokens as {@link Integer}; this
     * works only when {@code V == Integer}.
     *
     * @param parser maps a token (already trimmed of the trailing delimiter) to a vertex
     * @return this reader
     */
    public CoordinatesCsvReader<V> vertexParser(Function<String, V> parser)
    {
        this.vertexParser = Objects.requireNonNull(parser, "vertexParser");
        return this;
    }

    /**
     * Configures whether the input stream is gzip-compressed.
     *
     * @param gzipped {@code true} to wrap the input in {@link GZIPInputStream}
     * @return this reader
     */
    public CoordinatesCsvReader<V> gzipped(boolean gzipped)
    {
        this.gzipped = gzipped;
        return this;
    }

    /**
     * Configures whether the first non-empty line is a header to skip. Default
     * {@code true}.
     *
     * @param hasHeader {@code true} to skip the first non-empty line
     * @return this reader
     */
    public CoordinatesCsvReader<V> hasHeader(boolean hasHeader)
    {
        this.hasHeader = hasHeader;
        return this;
    }

    /**
     * Sets the field delimiter. Default {@code ','}.
     *
     * @param delimiter the delimiter character
     * @return this reader
     */
    public CoordinatesCsvReader<V> delimiter(char delimiter)
    {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * Reads the coordinate table from the given input stream. Caller closes the stream.
     *
     * @param in the input stream
     * @return a map from vertex to {@code {lat, lon}}
     * @throws IOException on I/O or parse error
     */
    public Map<V, double[]> read(InputStream in) throws IOException
    {
        Objects.requireNonNull(in, "input stream");
        InputStream raw = gzipped ? new GZIPInputStream(in) : in;
        Map<V, double[]> out = new LinkedHashMap<>();
        try (BufferedReader r = new BufferedReader(
            new InputStreamReader(raw, StandardCharsets.UTF_8)))
        {
            readLines(r, out);
        }
        return out;
    }

    /**
     * Reads the coordinate table from a classpath resource looked up relative to the
     * given anchor class.
     *
     * @param anchor the anchor class for resource lookup
     * @param resource the resource path passed to {@link Class#getResourceAsStream}
     * @return a map from vertex to {@code {lat, lon}}
     * @throws IOException if the resource is missing or unreadable
     */
    public Map<V, double[]> readResource(Class<?> anchor, String resource) throws IOException
    {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(resource, "resource");
        InputStream in = anchor.getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("missing resource: " + resource);
        }
        try (InputStream owned = in) {
            return read(owned);
        }
    }

    /**
     * Reads the coordinate table from a filesystem path.
     *
     * @param path the file path
     * @return a map from vertex to {@code {lat, lon}}
     * @throws IOException on I/O or parse error
     */
    public Map<V, double[]> readFile(Path path) throws IOException
    {
        Objects.requireNonNull(path, "path");
        try (InputStream in = Files.newInputStream(path)) {
            return read(in);
        }
    }

    private void readLines(BufferedReader r, Map<V, double[]> out) throws IOException
    {
        String line = r.readLine();
        if (hasHeader && line != null) {
            line = r.readLine();
        }
        long lineNo = hasHeader ? 1 : 0;
        for (; line != null; line = r.readLine()) {
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

    private void parseLine(String line, Map<V, double[]> out)
    {
        int d1 = line.indexOf(delimiter);
        if (d1 < 0) {
            throw new IllegalArgumentException("missing first delimiter");
        }
        int d2 = line.indexOf(delimiter, d1 + 1);
        if (d2 < 0) {
            throw new IllegalArgumentException("missing second delimiter");
        }
        V vertex = vertexParser.apply(line.substring(0, d1));
        double lat = Double.parseDouble(line.substring(d1 + 1, d2));
        double lon = Double.parseDouble(line.substring(d2 + 1));
        out.put(vertex, new double[] { lat, lon });
    }

    @SuppressWarnings("unchecked")
    private static <V> V defaultParseInteger(String token)
    {
        return (V) Integer.valueOf(token.trim());
    }
}
