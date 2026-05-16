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

import org.jgrapht.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.zip.*;

/**
 * Loads a weighted directed/undirected edge list from a CSV stream into a target
 * {@link Graph}. The input is one edge per line: {@code source,target,weight}. Useful for
 * spinning up JMH benchmarks against real road graphs (Andorra OSM, etc.) or any other
 * dataset shipped as a CSV.
 *
 * <p>
 * Configurable via a fluent builder:
 * <ul>
 *   <li>{@link #vertexParser(Function)} — how to parse a vertex token (default
 *       {@code Integer.valueOf}).</li>
 *   <li>{@link #gzipped(boolean)} — wrap the stream in {@link GZIPInputStream}.</li>
 *   <li>{@link #hasHeader(boolean)} — skip the first non-empty line as a header.</li>
 *   <li>{@link #delimiter(char)} — field delimiter (default {@code ','}).</li>
 *   <li>{@link #addMissingVertices(boolean)} — auto-add vertices encountered in the edge
 *       list (default {@code true}). Disable when the caller has pre-added vertices in a
 *       specific order (for example to keep node IDs dense and contiguous).</li>
 * </ul>
 *
 * <p>
 * Lines that fail to parse cause an {@link IOException}; the reader is intended for
 * trusted, machine-generated input. Edge weights are parsed with
 * {@link Double#parseDouble(String)} so {@code POSITIVE_INFINITY} and scientific notation
 * are accepted.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Shai Eilat
 */
public final class WeightedEdgeListCsvReader<V, E>
{
    private final Graph<V, E> target;
    private Function<String, V> vertexParser = WeightedEdgeListCsvReader::defaultParseInteger;
    private boolean gzipped = false;
    private boolean hasHeader = true;
    private char delimiter = ',';
    private boolean addMissingVertices = true;

    /**
     * Constructs a reader that loads edges into the given target graph.
     *
     * @param target the destination graph
     */
    public WeightedEdgeListCsvReader(Graph<V, E> target)
    {
        this.target = Objects.requireNonNull(target, "target graph");
    }

    /**
     * Sets the vertex token parser. The default parses tokens as {@link Integer}; this
     * works only when {@code V == Integer}.
     *
     * @param parser maps a token (already trimmed of the trailing delimiter) to a vertex
     * @return this reader
     */
    public WeightedEdgeListCsvReader<V, E> vertexParser(Function<String, V> parser)
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
    public WeightedEdgeListCsvReader<V, E> gzipped(boolean gzipped)
    {
        this.gzipped = gzipped;
        return this;
    }

    /**
     * Configures whether the first non-empty line is a header to skip. Default {@code
     * true}.
     *
     * @param hasHeader {@code true} to skip the first non-empty line
     * @return this reader
     */
    public WeightedEdgeListCsvReader<V, E> hasHeader(boolean hasHeader)
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
    public WeightedEdgeListCsvReader<V, E> delimiter(char delimiter)
    {
        this.delimiter = delimiter;
        return this;
    }

    /**
     * Configures whether to auto-add vertices encountered in the edge list. Default
     * {@code true}. Disable when the caller has pre-added the vertex set in a specific
     * order (for example, to keep node IDs dense and contiguous for downstream
     * algorithms).
     *
     * @param add {@code true} to auto-add missing vertices
     * @return this reader
     */
    public WeightedEdgeListCsvReader<V, E> addMissingVertices(boolean add)
    {
        this.addMissingVertices = add;
        return this;
    }

    /**
     * Reads the edge list from the given input stream. The caller is responsible for
     * closing the stream.
     *
     * @param in the input stream
     * @throws IOException on I/O or parse error
     */
    public void read(InputStream in) throws IOException
    {
        Objects.requireNonNull(in, "input stream");
        InputStream raw = gzipped ? new GZIPInputStream(in) : in;
        try (BufferedReader r = new BufferedReader(
            new InputStreamReader(raw, StandardCharsets.UTF_8)))
        {
            readLines(r);
        }
    }

    /**
     * Reads the edge list from a classpath resource looked up relative to the given
     * anchor class.
     *
     * @param anchor the anchor class for resource lookup
     * @param resource the resource path passed to {@link Class#getResourceAsStream}
     * @throws IOException if the resource is missing or unreadable
     */
    public void readResource(Class<?> anchor, String resource) throws IOException
    {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(resource, "resource");
        InputStream in = anchor.getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("missing resource: " + resource);
        }
        try (InputStream owned = in) {
            read(owned);
        }
    }

    /**
     * Reads the edge list from a filesystem path.
     *
     * @param path the file path
     * @throws IOException on I/O or parse error
     */
    public void readFile(Path path) throws IOException
    {
        Objects.requireNonNull(path, "path");
        try (InputStream in = Files.newInputStream(path)) {
            read(in);
        }
    }

    private void readLines(BufferedReader r) throws IOException
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
                parseLine(line);
            } catch (RuntimeException ex) {
                throw new IOException(
                    "failed to parse edge list at line " + lineNo + ": " + line, ex);
            }
        }
    }

    private void parseLine(String line)
    {
        int d1 = line.indexOf(delimiter);
        if (d1 < 0) {
            throw new IllegalArgumentException("missing first delimiter");
        }
        int d2 = line.indexOf(delimiter, d1 + 1);
        if (d2 < 0) {
            throw new IllegalArgumentException("missing second delimiter");
        }
        V src = vertexParser.apply(line.substring(0, d1));
        V dst = vertexParser.apply(line.substring(d1 + 1, d2));
        double weight = Double.parseDouble(line.substring(d2 + 1));
        if (addMissingVertices) {
            if (!target.containsVertex(src)) {
                target.addVertex(src);
            }
            if (!target.containsVertex(dst)) {
                target.addVertex(dst);
            }
        }
        E edge = target.addEdge(src, dst);
        if (edge != null) {
            target.setEdgeWeight(edge, weight);
        }
    }

    @SuppressWarnings("unchecked")
    private static <V> V defaultParseInteger(String token)
    {
        return (V) Integer.valueOf(token.trim());
    }
}
