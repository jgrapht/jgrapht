/*
 * (C) Copyright 2017-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.nio.graph6;

import org.jgrapht.alg.util.*;
import org.jgrapht.nio.*;

import java.io.*;

/**
 * Importer which reads graphs in graph6 or sparse6 format.
 * 
 * <p>
 * A description of the format can be found
 * <a href="https://users.cecs.anu.edu.au/~bdm/data/formats.txt">here</a>. graph6 and sparse6 are
 * formats for storing undirected graphs in a compact manner, using only printable ASCII characters.
 * Files in these formats have text format and contain one line per graph. graph6 is suitable for
 * small graphs, or large dense graphs. sparse6 is more space-efficient for large sparse graphs.
 * Typically, files storing graph6 graphs have the 'g6' extension. Similarly, files storing sparse6
 * graphs have a 's6' file extension. sparse6 graphs support loops and multiple edges, graph6 graphs
 * do not.
 * 
 * <p>
 * Note that a g6/s6 string may contain backslashes '\'. Thus, escaping is required. E.g.
 * 
 * <pre>
 * <code>":?@MnDA\oi"</code>
 * </pre>
 * 
 * may result in undefined behavior. This should have been:
 * 
 * <pre>
 * <code>":?@MnDA\\oi"</code>
 * </pre>
 *
 * @author Joris Kinable
 */
public class Graph6Sparse6EventDrivenImporter
    extends
    BaseEventDrivenImporter<Integer, Pair<Integer, Integer>>
    implements
    EventDrivenImporter<Integer, Pair<Integer, Integer>>
{
    private static final String GRAPH_STRING_SEEMS_TO_BE_CORRUPT_INVALID_NUMBER_OF_VERTICES =
        "Graph string seems to be corrupt. Invalid number of vertices.";

    enum Format
    {
        GRAPH6,
        SPARSE6
    }

    // ~ Constructors ----------------------------------------------------------

    /**
     * Construct a new importer
     */
    public Graph6Sparse6EventDrivenImporter()
    {
        super();
    }

    @Override
    public void importInput(Reader input)
        throws ImportException
    {
        // convert to buffered
        BufferedReader in;
        if (input instanceof BufferedReader) {
            in = (BufferedReader) input;
        } else {
            in = new BufferedReader(input);
        }

        notifyImportEvent(ImportEvent.START);

        // read line
        String g6 = null;
        try {
            g6 = in.readLine();
        } catch (IOException e) {
            throw new ImportException("Failed to read graph: " + e.getMessage());
        }
        if (g6.isEmpty()) {
            throw new ImportException("Failed to read graph: empty line");
        }

        // remove any new line characters
        g6 = g6.replace("\n", "").replace("\r", "");

        // do the actual parsing
        new Parser(g6).parse();

        notifyImportEvent(ImportEvent.END);
    }

    /**
     * The actual parser. The parser assumes the input is a single line.
     */
    private class Parser
    {
        private Format format;
        private byte[] bytes;
        private int byteIndex;
        private int bitIndex;
        private int n;

        /**
         * Create a new parser.
         * 
         * @param inputLine an input line
         */
        public Parser(String inputLine)
        {
            this.format = Format.GRAPH6;
            if (inputLine.startsWith(":")) {
                inputLine = inputLine.substring(1, inputLine.length());
                this.format = Format.SPARSE6;
            } else if (inputLine.startsWith(">>sparse6<<:")) {
                inputLine = inputLine.substring(12, inputLine.length());
                this.format = Format.SPARSE6;
            } else if (inputLine.startsWith(">>graph6<<")) {
                inputLine = inputLine.substring(10, inputLine.length());
            }

            this.bytes = inputLine.getBytes();
            this.byteIndex = 0;
            this.bitIndex = 0;
            this.n = 0;
        }

        public void parse()
        {
            validateInput();
            readNumberOfVertices();
            notifyVertexCount(n);
            for (int i = 0; i < n; i++) {
                notifyVertex(i);
            }
            if (format == Format.GRAPH6)
                readGraph6();
            else
                readSparse6();
        }

        private void readGraph6()
            throws ImportException
        {
            // check whether there's enough data
            int requiredBytes = (int) Math.ceil(n * (n - 1) / 12.0) + byteIndex;
            if (bytes.length < requiredBytes)
                throw new ImportException(
                    "Graph string seems to be corrupt. Not enough data to read graph6 graph");

            // Read the lower triangle of the adjacency matrix of G
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < i; j++) {
                    int bit = getBits(1);
                    if (bit == 1) {
                        notifyEdge(Pair.of(i, j));
                    }
                }
            }
        }

        private void readSparse6()
            throws ImportException
        {
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
                else
                    notifyEdge(Pair.of(x, v));
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
         * @throws ImportException in case any error occurs, such as I/O or parse error
         */
        private void readNumberOfVertices()
            throws ImportException
        {
            // Determine whether the number of vertices is encoded in 1, 4 or 8 bytes.
            int n;
            if (bytes.length > 8 && bytes[0] == 126 && bytes[1] == 126) {
                byteIndex += 2; // Strip the first 2 garbage bytes
                n = getBits(36);
                if (n < 258048)
                    throw new ImportException(
                        GRAPH_STRING_SEEMS_TO_BE_CORRUPT_INVALID_NUMBER_OF_VERTICES);
            } else if (bytes.length > 4 && bytes[0] == 126) {
                byteIndex++; // Strip the first garbage byte
                n = getBits(18);
                if (n < 63 || n > 258047)
                    throw new ImportException(
                        GRAPH_STRING_SEEMS_TO_BE_CORRUPT_INVALID_NUMBER_OF_VERTICES);
            } else {
                n = getBits(6);
                if (n < 0 || n > 62)
                    throw new ImportException(
                        GRAPH_STRING_SEEMS_TO_BE_CORRUPT_INVALID_NUMBER_OF_VERTICES);
            }
            this.n = n;
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
                bitIndex += x;
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

}
