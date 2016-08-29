/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* -------------------
 * DIMACSImporter.java
 * -------------------
 * (C) Copyright 2010-2010, by Michael Behrisch and Contributors.
 *
 * Original Author:  Michael Behrisch
 * Contributor(s): Joris Kinable, Dimitrios Michail
 *
 * Changes
 * -------
 * 24-Dec-2008 : Initial revision (AN);
 *
 */
package org.jgrapht.ext;

import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Imports a graph specified in DIMACS format
 * (http://mat.gsia.cmu.edu/COLOR/general/ccformat.ps). In summary, graphs
 * specified in DIMACS format adhere to the following structure:
 * 
 * <pre>
 * {@code
 * DIMACS G {
 *    c <comments; ignored during parsing of the graph
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
 * Although not specified directly in the DIMACS format documentation, this
 * implementation also allows for the a weighted variant:
 * 
 * <pre>
 * {@code 
 * e <edge source 1> <edge target 1> <edge_weight> 
 * }
 * </pre>
 * 
 * Note: the current implementation does not fully implement the DIMACS
 * specifications! Special (rarely used) fields specified as 'Optional
 * Descriptors' are currently not supported.
 *
 * @author Michael Behrisch (adaptation of GraphReader class)
 * @author Joris Kinable
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class DIMACSImporter<V, E>
    implements GraphImporter<V, E>
{
    private VertexProvider<V> vertexProvider;
    private EdgeProvider<V, E> edgeProvider;
    private final double defaultWeight;

    // ~ Constructors ----------------------------------------------------------

    /**
     * Construct a new DIMACSImporter
     * 
     * @param vertexProvider provider for the generation of vertices. Must not
     *        be null.
     * @param edgeProvider provider for the generation of edges. Must not be
     *        null.
     * @param defaultWeight default edge weight
     */
    public DIMACSImporter(
        VertexProvider<V> vertexProvider,
        EdgeProvider<V, E> edgeProvider,
        double defaultWeight)
    {
        if (vertexProvider == null) {
            throw new IllegalArgumentException(
                "Vertex provider cannot be null");
        }
        this.vertexProvider = vertexProvider;
        if (edgeProvider == null) {
            throw new IllegalArgumentException("Edge provider cannot be null");
        }
        this.edgeProvider = edgeProvider;
        this.defaultWeight = defaultWeight;
    }

    /**
     * Construct a new DIMACSImporter
     * 
     * @param vertexProvider provider for the generation of vertices. Must not
     *        be null.
     * @param edgeProvider provider for the generation of edges. Must not be
     *        null.
     */
    public DIMACSImporter(
        VertexProvider<V> vertexProvider,
        EdgeProvider<V, E> edgeProvider)
    {
        this(vertexProvider, edgeProvider, WeightedGraph.DEFAULT_EDGE_WEIGHT);
    }

    // ~ Methods ---------------------------------------------------------------

    /**
     * Get the vertex provider
     * 
     * @return the vertex provider
     */
    public VertexProvider<V> getVertexProvider()
    {
        return vertexProvider;
    }

    /**
     * Set the vertex provider
     * 
     * @param vertexProvider the new vertex provider. Must not be null.
     */
    public void setVertexProvider(VertexProvider<V> vertexProvider)
    {
        if (vertexProvider == null) {
            throw new IllegalArgumentException(
                "Vertex provider cannot be null");
        }
        this.vertexProvider = vertexProvider;
    }

    /**
     * Get the edge provider
     * 
     * @return The edge provider
     */
    public EdgeProvider<V, E> getEdgeProvider()
    {
        return edgeProvider;
    }

    /**
     * Set the edge provider.
     * 
     * @param edgeProvider the new edge provider. Must not be null.
     */
    public void setEdgeProvider(EdgeProvider<V, E> edgeProvider)
    {
        if (edgeProvider == null) {
            throw new IllegalArgumentException("Edge provider cannot be null");
        }
        this.edgeProvider = edgeProvider;
    }
    
    /**
     * Import a graph.
     * 
     * <p>
     * The provided graph must be able to support the features of the graph that
     * is read. For example if the file contains self-loops then the graph
     * provided must also support self-loops. The same for multiple edges.
     * 
     * <p>
     * If the provided graph is a weighted graph, the importer also reads edge
     * weights. Otherwise edge weights are ignored.
     * 
     * @param graph the output graph
     * @param input the input reader
     * @throws ImportException in case an error occurs, such as I/O or parse
     *         error
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
            V vertex = vertexProvider
                .buildVertex(id.toString(), new HashMap<String, String>());
            map.put(id, vertex);
            graph.addVertex(vertex);
        }

        // add edges
        String[] cols = skipComments(in);
        while (cols != null) {
            if (cols[0].equals("e")) {
                if (cols.length < 3) {
                    throw new ImportException(
                        "Failed to parse edge:" + Arrays.toString(cols));
                }
                Integer source;
                try {
                    source = Integer.parseInt(cols[1]);
                } catch (NumberFormatException e) {
                    throw new ImportException(
                        "Failed to parse edge source node:" + e.getMessage(),
                        e);
                }
                Integer target;
                try {
                    target = Integer.parseInt(cols[2]);
                } catch (NumberFormatException e) {
                    throw new ImportException(
                        "Failed to parse edge target node:" + e.getMessage(),
                        e);
                }

                String label = "e_" + source + "_" + target;
                V from = map.get(source);
                if (from == null) {
                    throw new ImportException(
                        "Node " + source + " does not exist");
                }
                V to = map.get(target);
                if (to == null) {
                    throw new ImportException(
                        "Node " + target + " does not exist");
                }

                try {

                    E e = edgeProvider.buildEdge(
                        from,
                        to,
                        label,
                        new HashMap<String, String>());
                    graph.addEdge(from, to, e);

                    if (graph instanceof WeightedGraph<?, ?>) {
                        double weight = defaultWeight;
                        if (cols.length > 3) {
                            weight = Double.parseDouble(cols[3]);
                        }
                        ((WeightedGraph<V, E>) graph).setEdgeWeight(e, weight);
                    }
                } catch (IllegalArgumentException e) {
                    throw new ImportException(
                        "Failed to import DIMACS graph:" + e.getMessage(),
                        e);
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
            while ((cols != null) && ((cols.length == 0) || cols[0].equals("c")
                || cols[0].startsWith("%")))
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
