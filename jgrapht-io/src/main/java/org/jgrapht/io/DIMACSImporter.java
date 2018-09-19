/*
 * (C) Copyright 2010-2017, by Michael Behrisch, Joris Kinable, Dimitrios 
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
 * Imports a graph specified in DIMACS format.
 *
 * <p>
 * See {@link DIMACSFormat} for a description of all the supported DIMACS formats.
 *
 * <p>
 * In summary, one of the most common DIMACS formats was used in the
 * <a href="http://mat.gsia.cmu.edu/COLOR/general/ccformat.ps">2nd DIMACS challenge</a> and follows
 * the following structure:
 * 
 * <pre>
 * {@code
 * DIMACS G {
 *    c <comments> ignored during parsing of the graph
 *    p edge <number of nodes> <number of edges>
 *    e <edge source 1> <edge target 1>
 *    e <edge source 2> <edge target 2>
 *    e <edge source 3> <edge target 3>
 *    e <edge source 4> <edge target 4>
 *    ...
 * }
 * }
 * </pre>
 * 
 * Although not specified directly in the DIMACS format documentation, this implementation also
 * allows for the a weighted variant:
 * 
 * <pre>
 * {@code 
 * e <edge source 1> <edge target 1> <edge_weight> 
 * }
 * </pre>
 * 
 * Note: the current implementation does not fully implement the DIMACS specifications! Special
 * (rarely used) fields specified as 'Optional Descriptors' are currently not supported (ignored).
 *
 * @author Michael Behrisch (adaptation of GraphReader class)
 * @author Joris Kinable
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class DIMACSImporter<V, E>
    extends
    AbstractBaseImporter<V, E>
    implements
    GraphImporter<V, E>
{
    private final double defaultWeight;

    // ~ Constructors ----------------------------------------------------------

    /**
     * Construct a new DIMACSImporter
     * 
     * @param vertexProvider provider for the generation of vertices. Must not be null.
     * @param edgeProvider provider for the generation of edges. Must not be null.
     * @param defaultWeight default edge weight
     */
    public DIMACSImporter(
        VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider, double defaultWeight)
    {
        super(vertexProvider, edgeProvider);
        this.defaultWeight = defaultWeight;
    }

    /**
     * Construct a new DIMACSImporter
     * 
     * @param vertexProvider provider for the generation of vertices. Must not be null.
     * @param edgeProvider provider for the generation of edges. Must not be null.
     */
    public DIMACSImporter(VertexProvider<V> vertexProvider, EdgeProvider<V, E> edgeProvider)
    {
        this(vertexProvider, edgeProvider, Graph.DEFAULT_EDGE_WEIGHT);
    }

    // ~ Methods ---------------------------------------------------------------

    /**
     * Import a graph.
     * 
     * <p>
     * The provided graph must be able to support the features of the graph that is read. For
     * example if the file contains self-loops then the graph provided must also support self-loops.
     * The same for multiple edges.
     * 
     * <p>
     * If the provided graph is a weighted graph, the importer also reads edge weights. Otherwise
     * edge weights are ignored.
     * 
     * @param graph the output graph
     * @param input the input reader
     * @throws ImportException in case an error occurs, such as I/O or parse error
     */
    @Override
    public void importGraph(Graph<V, E> graph, Reader input)
        throws ImportException
    {
        // convert to buffered
        BufferedReader in;
        if (input instanceof BufferedReader) {
            in = (BufferedReader) input;
        } else {
            in = new BufferedReader(input);
        }

        // add nodes
        final int size = readNodeCount(in);
        Map<Integer, V> map = new HashMap<Integer, V>();
        for (int i = 0; i < size; i++) {
            Integer id = Integer.valueOf(i + 1);
            V vertex = vertexProvider.buildVertex(id.toString(), new HashMap<String, Attribute>());
            map.put(id, vertex);
            graph.addVertex(vertex);
        }

        // add edges
        String[] cols = skipComments(in);
        while (cols != null) {
            if (cols[0].equals("e") || cols[0].equals("a")) {
                if (cols.length < 3) {
                    throw new ImportException("Failed to parse edge:" + Arrays.toString(cols));
                }
                Integer source;
                try {
                    source = Integer.parseInt(cols[1]);
                } catch (NumberFormatException e) {
                    throw new ImportException(
                        "Failed to parse edge source node:" + e.getMessage(), e);
                }
                Integer target;
                try {
                    target = Integer.parseInt(cols[2]);
                } catch (NumberFormatException e) {
                    throw new ImportException(
                        "Failed to parse edge target node:" + e.getMessage(), e);
                }

                String label = "e_" + source + "_" + target;
                V from = map.get(source);
                if (from == null) {
                    throw new ImportException("Node " + source + " does not exist");
                }
                V to = map.get(target);
                if (to == null) {
                    throw new ImportException("Node " + target + " does not exist");
                }

                try {
                    E e = edgeProvider.buildEdge(from, to, label, new HashMap<String, Attribute>());
                    graph.addEdge(from, to, e);

                    if (graph.getType().isWeighted()) {
                        double weight = defaultWeight;
                        if (cols.length > 3) {
                            weight = Double.parseDouble(cols[3]);
                        }
                        graph.setEdgeWeight(e, weight);
                    }
                } catch (IllegalArgumentException e) {
                    throw new ImportException("Failed to import DIMACS graph:" + e.getMessage(), e);
                }
            }
            cols = skipComments(in);
        }

    }

    private String[] split(final String src)
    {
        if (src == null) {
            return null;
        }
        return src.split("\\s+");
    }

    private String[] skipComments(BufferedReader input)
    {
        String[] cols = null;
        try {
            cols = split(input.readLine());
            while ((cols != null)
                && ((cols.length == 0) || cols[0].equals("c") || cols[0].startsWith("%")))
            {
                cols = split(input.readLine());
            }
        } catch (IOException e) {
            // ignore
        }
        return cols;
    }

    private int readNodeCount(BufferedReader input)
        throws ImportException
    {
        final String[] cols = skipComments(input);
        if (cols[0].equals("p")) {
            if (cols.length < 3) {
                throw new ImportException("Failed to read number of vertices.");
            }
            Integer nodes;
            try {
                nodes = Integer.parseInt(cols[2]);
            } catch (NumberFormatException e) {
                throw new ImportException("Failed to read number of vertices.");
            }
            if (nodes < 0) {
                throw new ImportException("Negative number of vertices.");
            }
            return nodes;
        }
        throw new ImportException("Failed to read number of vertices.");
    }

}
