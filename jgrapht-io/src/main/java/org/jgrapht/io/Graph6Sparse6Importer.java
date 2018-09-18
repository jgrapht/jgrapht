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
 * Importer which reads graphs in graph6 or sparse6 format. A description of the format can be found
 * <a href="https://users.cecs.anu.edu.au/~bdm/data/formats.txt">here</a>. graph6 and sparse6 are
 * formats for storing undirected graphs in a compact manner, using only printable ASCII characters.
 * Files in these formats have text Format and contain one line per graph. graph6 is suitable for
 * small graphs, or large dense graphs. sparse6 is more space-efficient for large sparse graphs.
 * Typically, files storing graph6 graphs have the 'g6' extension. Similarly, files storing sparse6
 * graphs have a 's6' file extension. sparse6 graphs support loops and multiple edges, graph6 graphs
 * do not.
 *
 * @author Joris Kinable
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 */
public class Graph6Sparse6Importer<V, E>
    extends
    AbstractBaseImporter<V, E>
    implements
    GraphImporter<V, E>
{

    enum Format
    {
        GRAPH6,
        SPARSE6
    }

    private final double defaultWeight;
    /* byte representation of the input string */
    private byte[] bytes;
    /* pointers which index a specific byte/bit in the vector bytes */
    private int byteIndex, bitIndex = 0;
    private Format format = Format.GRAPH6;

    // ~ Constructors ----------------------------------------------------------

    /**
     * Construct a new Graph6Sparse6Importer
     *
     * @param vertexProvider provider for the generation of vertices. Must not be null.
     * @param edgeProvider provider for the generation of edges. Must not be null.
     * @param defaultWeight default edge weight
     */
    public Graph6Sparse6Importer(
        VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider, double defaultWeight)
    {
        super(vertexProvider, edgeProvider);
        this.defaultWeight = defaultWeight;
    }

    /**
     * Construct a new Graph6Sparse6Importer
     *
     * @param vertexProvider provider for the generation of vertices. Must not be null.
     * @param edgeProvider provider for the generation of edges. Must not be null.
     */
    public Graph6Sparse6Importer(VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider)
    {
        this(vertexProvider, edgeProvider, Graph.DEFAULT_EDGE_WEIGHT);
    }

    @Override
    public void importGraph(Graph<V, E> g, Reader input)
        throws ImportException
    {
        // convert to buffered
        BufferedReader in;
        if (input instanceof BufferedReader) {
            in = (BufferedReader) input;
        } else {
            in = new BufferedReader(input);
        }

        String g6 = "";
        try {
            g6 = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (g6.isEmpty())
            throw new ImportException("Failed to read graph");
        this.importGraph(g, g6);
    }

    /**
     * Import the graph represented by a String in graph6 or sparse6 encoding.
     * 
     * @param g the graph
     * @param g6 String representation of a graph either in graph6 or sparse6 format. WARNING: a
     *        g6/s6 string may contain backslashes '\'. Escaping is required when invoking this
     *        method directly. E.g.
     * 
     *        <pre>
     * <code>importgraph(g,":?@MnDA\oi")</code>
     *        </pre>
     * 
     *        may result in undefined behavior. This should have been:
     * 
     *        <pre>
     * <code>importgraph(g,":?@MnDA\\oi")</code>
     *        </pre>
     * 
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    public void importGraph(Graph<V, E> g, String g6)
        throws ImportException
    {

        g6 = g6.replace("\n", "").replace("\r", ""); // remove any new line characters
        // Strip header. By default we assume the input Format is GRAPH6, unless stated otherwise
        if (g6.startsWith(":")) {
            g6 = g6.substring(1, g6.length());
            this.format = Format.SPARSE6;
        } else if (g6.startsWith(">>sparse6<<:")) {
            g6 = g6.substring(12, g6.length());
            this.format = Format.SPARSE6;
        } else if (g6.startsWith(">>graph6<<"))
            g6 = g6.substring(10, g6.length());

        bytes = g6.getBytes();
        validateInput();
        byteIndex = bitIndex = 0;

        // Number of vertices n
        final int n = getNumberOfVertices();

        // Add vertices to the graph
        Map<Integer, V> map = new HashMap<>();
        for (int i = 0; i < n; i++) {
            V vertex = vertexProvider.buildVertex("" + i, new HashMap<>());
            map.put(i, vertex);
            g.addVertex(vertex);
        }

        if (format == Format.GRAPH6)
            this.readGraph6(g, map);
        else
            this.readSparse6(g, map);
    }

    private void readGraph6(Graph<V, E> g, Map<Integer, V> vertexIndexMap)
        throws ImportException
    {
        // check whether there's enough data
        int requiredBytes =
            (int) Math.ceil(vertexIndexMap.size() * (vertexIndexMap.size() - 1) / 12.0) + byteIndex;
        if (bytes.length < requiredBytes)
            throw new ImportException(
                "Graph string seems to be corrupt. Not enough data to read graph6 graph");
        // Read the lower triangle of the adjacency matrix of G
        for (int i = 0; i < vertexIndexMap.size(); i++) {
            for (int j = 0; j < i; j++) {
                int bit = getBits(1);
                if (bit == 1) {

                    V from = vertexIndexMap.get(i);
                    V to = vertexIndexMap.get(j);
                    String label = "e_" + i + "_" + j;
                    E e = edgeProvider.buildEdge(from, to, label, new HashMap<>());
                    g.addEdge(from, to, e);

                    if (g.getType().isWeighted())
                        g.setEdgeWeight(e, defaultWeight);
                }
            }
        }
    }

    private void readSparse6(Graph<V, E> g, Map<Integer, V> vertexIndexMap)
        throws ImportException
    {
        int n = vertexIndexMap.size();

        // number of bits needed to represent n-1 in binary
        int k = (int) Math.ceil(Math.log(n) / Math.log(2));

        // Current vertex
        int v = 0;

        // The remaining bytes encode a sequence b[0] x[0] b[1] x[1] b[2] x[2] ... b[m] x[m]
        // Read blocks. In decoding, an incomplete (b,x) pair at the end is discarded.
        int dataBits = bytes.length * 6 - (byteIndex * 6 + bitIndex);
        while (dataBits >= 1 + k) { // while there's data remaining

            int b = getBits(1); // Read x[i]
            int x = getBits(k); // Read b[i]

            if (b == 1)
                v++;

            if (v >= n) // Ignore the last bit, this is just padding
                break;

            if (x > v)
                v = x;
            else {
                V from = vertexIndexMap.get(x);
                V to = vertexIndexMap.get(v);
                String label = "e_" + x + "_" + v;
                E e = edgeProvider.buildEdge(from, to, label, new HashMap<>());
                g.addEdge(from, to, e);

                if (g.getType().isWeighted())
                    g.setEdgeWeight(e, defaultWeight);
            }
            dataBits -= 1 + k;
        }
    }

    /**
     * Check whether the g6 or s6 encoding contains any obvious errors
     * 
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    private void validateInput()
        throws ImportException
    {
        for (byte b : bytes)
            if (b < 63 || b > 126)
                throw new ImportException(
                    "Graph string seems to be corrupt. Illegal character detected: " + b);
    }

    /**
     * Read the number of vertices in the graph
     * 
     * @return number of vertices in the graph
     * @throws ImportException in case any error occurs, such as I/O or parse error
     */
    private int getNumberOfVertices()
        throws ImportException
    {
        // Determine whether the number of vertices is encoded in 1, 4 or 8 bytes.
        int n;
        if (bytes.length > 8 && bytes[0] == 126 && bytes[1] == 126) {
            byteIndex += 2; // Strip the first 2 garbage bytes
            n = getBits(36);
            if (n < 258048)
                throw new ImportException(
                    "Graph string seems to be corrupt. Invalid number of vertices.");
        } else if (bytes.length > 4 && bytes[0] == 126) {
            byteIndex++; // Strip the first garbage byte
            n = getBits(18);
            if (n < 63 || n > 258047)
                throw new ImportException(
                    "Graph string seems to be corrupt. Invalid number of vertices.");
        } else {
            n = getBits(6);
            if (n < 0 || n > 62)
                throw new ImportException(
                    "Graph string seems to be corrupt. Invalid number of vertices.");
        }

        return n;
    }

    /**
     * Converts the next k bits of data to an integer
     * 
     * @param k number of bits
     * @return the next k bits of data represented by an integer
     */
    private int getBits(int k)
        throws ImportException
    {
        int value = 0;
        // Read minimum{bits we need, remaining bits in current byte}
        if (bitIndex > 0 || k < 6) {
            int x = Math.min(k, 6 - bitIndex);
            int mask = (1 << x) - 1;
            int y = (bytes[byteIndex] - 63) >> (6 - bitIndex - x);
            y &= mask;
            value = (value << k) + y;
            k -= x;
            bitIndex = bitIndex + x;
            if (bitIndex == 6) {
                byteIndex++;
                bitIndex = 0;
            }
        }

        // Read blocks of 6 bits at a time
        int blocks = k / 6;
        for (int j = 0; j < blocks; j++) {
            value = (value << 6) + bytes[byteIndex] - 63;
            byteIndex++;
            k -= 6;
        }

        // Read remaining bits
        if (k > 0) {
            int y = bytes[byteIndex] - 63;
            y = y >> (6 - k);
            value = (value << k) + y;
            bitIndex = k;
        }
        return value;
    }

}
