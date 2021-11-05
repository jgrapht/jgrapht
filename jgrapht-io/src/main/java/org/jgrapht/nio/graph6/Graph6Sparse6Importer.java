/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors. 
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

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

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
 * <p>
 * The graph vertices and edges are build using the corresponding graph suppliers. The id of the
 * vertices in the original file are reported as a vertex attribute named "ID". Note, however, that
 * in this file format the identifiers from the input files are always going to be integers starting
 * from zero, as the format does not retain such information in order to achieve compactness.
 * 
 * <p>
 * The default behavior of the importer is to use the graph vertex supplier in order to create
 * vertices. The user can also bypass vertex creation by providing a custom vertex factory method
 * using {@link #setVertexFactory(Function)}. The factory method is responsible to create a new
 * graph vertex given the vertex identifier read from file.
 *
 * @author Dimitrios Michail
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 */
public class Graph6Sparse6Importer<V, E>
    extends
    BaseEventDrivenImporter<V, E>
    implements
    GraphImporter<V, E>
{
    /**
     * Default key used for vertex ID.
     */
    public static final String DEFAULT_VERTEX_ID_KEY = "ID";

    private Function<Integer, V> vertexFactory;

    /**
     * Construct a new importer
     */
    public Graph6Sparse6Importer()
    {
        super();
    }

    /**
     * Get the user custom vertex factory. This is null by default and the graph supplier is used
     * instead.
     * 
     * @return the user custom vertex factory
     */
    public Function<Integer, V> getVertexFactory()
    {
        return vertexFactory;
    }

    /**
     * Set the user custom vertex factory. The default behavior is being null in which case the
     * graph vertex supplier is used.
     * 
     * If supplied the vertex factory is called every time a new vertex is encountered in the file.
     * The method is called with parameter the vertex identifier from the file and should return the
     * actual graph vertex to add to the graph.
     * 
     * @param vertexFactory a vertex factory
     */
    public void setVertexFactory(Function<Integer, V> vertexFactory)
    {
        this.vertexFactory = vertexFactory;
    }

    /**
     * Import a graph.
     * 
     * <p>
     * The provided graph must be able to support the features of the graph that is read. For
     * example if the file contains self-loops then the graph provided must also support self-loops.
     * The same for multiple edges.
     * 
     * @param graph the output graph
     * @param input the input reader
     * @throws ImportException in case an error occurs, such as I/O or parse error
     */
    @Override
    public void importGraph(Graph<V, E> graph, Reader input)
    {
        Graph6Sparse6EventDrivenImporter genericImporter = new Graph6Sparse6EventDrivenImporter();
        Consumers consumers = new Consumers(graph);
        genericImporter.addVertexConsumer(consumers.vertexConsumer);
        genericImporter.addEdgeConsumer(consumers.edgeConsumer);
        genericImporter.importInput(input);
    }

    private class Consumers
    {
        private Graph<V, E> graph;
        private Map<Integer, V> map;

        public Consumers(Graph<V, E> graph)
        {
            this.graph = graph;
            this.map = new HashMap<Integer, V>();
        }

        public final Consumer<Integer> vertexConsumer = (t) -> {
            if (map.containsKey(t)) {
                throw new ImportException("Node " + t + " reported twice");
            }
            V v;
            if (vertexFactory != null) {
                v = vertexFactory.apply(t);
                graph.addVertex(v);
            } else {
                v = graph.addVertex();
            }
            map.put(t, v);

            /*
             * Notify the first time we create the node.
             */
            notifyVertex(v);
            notifyVertexAttribute(v, DEFAULT_VERTEX_ID_KEY, DefaultAttribute.createAttribute(t));
        };

        public final Consumer<Pair<Integer, Integer>> edgeConsumer = (p) -> {
            int source = p.getFirst();
            V from = map.get(p.getFirst());
            if (from == null) {
                throw new ImportException("Node " + source + " does not exist");
            }

            int target = p.getSecond();
            V to = map.get(target);
            if (to == null) {
                throw new ImportException("Node " + target + " does not exist");
            }

            E e = graph.addEdge(from, to);
            notifyEdge(e);
        };

    }

}
