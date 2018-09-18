/*
 * (C) Copyright 2017-2017, by Joris Kinable and Contributors.
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
package org.jgrapht.io;

import org.jgrapht.*;

import java.io.*;
import java.util.*;

/**
 * Exporter which exports graphs in graph6 or sparse6 format. A description of the format can be
 * found <a href="https://users.cecs.anu.edu.au/~bdm/data/formats.txt">here</a>. graph6 and sparse6
 * are formats for storing undirected graphs in a compact manner, using only printable ASCII
 * characters. Files in these formats have text format and contain one line per graph. graph6 is
 * suitable for small graphs, or large dense graphs. sparse6 is more space-efficient for large
 * sparse graphs. Typically, files storing graph6 graphs have the 'g6' extension. Similarly, files
 * storing sparse6 graphs have a 's6' file extension. sparse6 graphs support loops and multiple
 * edges, graph6 graphs do not.
 * <p>
 * In particular, the length of a Graph6 string representation of a graph depends only on the number
 * of vertices. However, this also means that graphs with few edges take as much space as graphs
 * with many edges. On the other hand, Sparse6 is a variable length format which can use
 * dramatically less space for sparse graphs but can have a much larger storage size for dense
 * graphs.
 *
 * @author Joris Kinable
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 */
public class Graph6Sparse6Exporter<V, E>
    implements
    GraphExporter<V, E>
{

    /**
     * Format type: graph6 (g6) or sparse6(s6)
     */
    public enum Format
    {
        GRAPH6,
        SPARSE6
    }

    private Format format;

    private ByteArrayOutputStream byteArrayOutputStream;

    /**
     * The default format used by the exporter.
     */
    public static final Format DEFAULT_GRAPH6SPARSE6_FORMAT = Format.GRAPH6;

    /**
     * Constructs a new exporter with a given vertex ID provider.
     *
     */
    public Graph6Sparse6Exporter()
    {
        this(DEFAULT_GRAPH6SPARSE6_FORMAT);
    }

    /**
     * Constructs a new exporter with a given vertex ID provider.
     *
     * @param format the format to use
     */
    public Graph6Sparse6Exporter(Format format)
    {
        this.format = Objects.requireNonNull(format, "Format cannot be null");
    }

    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
        throws ExportException
    {
        GraphTests.requireUndirected(g);
        if (format == Format.GRAPH6 && !GraphTests.isSimple(g))
            throw new ExportException(
                "Graphs exported in graph6 format cannot contain loops or multiple edges.");

        // Map all vertices to a unique integer
        List<V> vertices = new ArrayList<>(g.vertexSet());

        byteArrayOutputStream = new ByteArrayOutputStream();
        currentByte = 0;
        bitIndex = 0;

        try {
            if (format == Format.SPARSE6)
                writeSparse6(g, vertices);
            else
                writeGraph6(g, vertices);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String g6 = "";
        try {
            g6 = byteArrayOutputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        PrintWriter out = new PrintWriter(writer);
        out.print(g6);
        out.flush();
    }

    private void writeSparse6(Graph<V, E> g, List<V> vertices)
        throws IOException
    {
        int[][] edges = new int[g.edgeSet().size()][2];
        int index = 0;
        for (int j = 0; j < vertices.size(); j++) {
            for (int i = 0; i <= j; i++) {
                if (g.containsEdge(vertices.get(i), vertices.get(j))) {
                    for (int p = 0; p < g.getAllEdges(vertices.get(i), vertices.get(j)).size();
                        p++)
                    {
                        edges[index][0] = i;
                        edges[index][1] = j;
                        index++;
                    }
                }
            }
        }

        // sparse6 format always starts with ":"
        byteArrayOutputStream.write(":".getBytes());
        writeNumberOfVertices(vertices.size());
        // number of bits needed to represent n-1 in binary
        int k = (int) Math.ceil(Math.log(vertices.size()) / Math.log(2));

        int m = 0;
        int v = 0;
        while (m < edges.length) {
            if (edges[m][1] > v + 1) {
                writeBit(true);
                writeIntInKBits(edges[m][1], k);
                v = edges[m][1];
            } else if (edges[m][1] == v + 1) {
                writeBit(true);
                writeIntInKBits(edges[m][0], k);
                v++;
                m++;
            } else {
                writeBit(false);
                writeIntInKBits(edges[m][0], k);
                m++;
            }
        }
        // Pad right hand side with '1's to fill the last byte. This may not be the 'correct' way of
        // padding as
        // described in the sparse6 format descr, but it's hard to make sense of the sparse6
        // description. This seems to work fine.
        if (bitIndex != 0) {
            int padding = 6 - bitIndex;
            for (int i = 0; i < padding; i++)
                writeBit(true);
            writeByte(); // push the last byte
        }

    }

    private void writeGraph6(Graph<V, E> g, List<V> vertices)
        throws IOException
    {
        writeNumberOfVertices(vertices.size());
        // Write the lower triangle of the adjacency matrix of G as a bit vector x of length
        // n(n-1)/2,
        // using the ordering (0,1),(0,2),(1,2),(0,3),(1,3),(2,3),...,(n-1,n).
        for (int i = 0; i < vertices.size(); i++)
            for (int j = 0; j < i; j++)
                writeBit(g.containsEdge(vertices.get(i), vertices.get(j)));
        writeByte(); // Finish writing the last byte
    }

    private void writeNumberOfVertices(int n)
        throws IOException
    {
        assert n >= 0;
        if (n <= 62)
            byteArrayOutputStream.write(n + 63);
        else if (n <= 258047) {
            // write number in 4 bytes
            writeIntInKBits(63, 6);
            writeIntInKBits(n, 18);
        } else {
            // write number in 8 bytes
            writeIntInKBits(63, 6);
            writeIntInKBits(63, 6);
            writeIntInKBits(n, 36);
        }
    }

    private byte currentByte;
    private int bitIndex;

    private void writeIntInKBits(int number, int k)
    {
        for (int i = k - 1; i >= 0; i--)
            writeBit((number & (1 << i)) != 0);
    }

    private void writeBit(boolean bit)
    {
        if (bitIndex == 6)
            writeByte();
        if (bit)
            currentByte |= 1 << (5 - bitIndex);
        bitIndex++;
    }

    private void writeByte()
    {
        byteArrayOutputStream.write(currentByte + 63);
        currentByte = 0;
        bitIndex = 0;
    }
}
