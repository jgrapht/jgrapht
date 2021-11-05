/*
 * (C) Copyright 2010-2021, by Michael Behrisch and Contributors. 
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
package org.jgrapht.nio.dimacs;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

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
    BaseEventDrivenImporter<V, E>
    implements
    GraphImporter<V, E>
{
    /**
     * Default key used for vertex ID.
     */
    public static final String DEFAULT_VERTEX_ID_KEY = "ID";

    private Function<Integer, V> vertexFactory;
    private final double defaultWeight;

    /**
     * Construct a new DIMACSImporter
     * 
     * @param defaultWeight default edge weight
     */
    public DIMACSImporter(double defaultWeight)
    {
        super();
        this.defaultWeight = defaultWeight;
    }

    /**
     * Construct a new DIMACSImporter
     */
    public DIMACSImporter()
    {
        this(Graph.DEFAULT_EDGE_WEIGHT);
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
        DIMACSEventDrivenImporter genericImporter =
            new DIMACSEventDrivenImporter().renumberVertices(false).zeroBasedNumbering(false);
        Consumers consumers = new Consumers(graph);
        genericImporter.addVertexCountConsumer(consumers.nodeCountConsumer);
        genericImporter.addEdgeConsumer(consumers.edgeConsumer);
        genericImporter.importInput(input);
    }

    private class Consumers
    {
        private Graph<V, E> graph;
        private List<V> list;

        public Consumers(Graph<V, E> graph)
        {
            this.graph = graph;
            this.list = new ArrayList<>();
        }

        public final Consumer<Integer> nodeCountConsumer = n -> {
            for (int i = 1; i <= n; i++) {
                V v;
                if (vertexFactory != null) {
                    v = vertexFactory.apply(i);
                    graph.addVertex(v);
                } else {
                    v = graph.addVertex();
                }

                list.add(v);

                /*
                 * Notify the first time we create the node.
                 */
                notifyVertex(v);
                notifyVertexAttribute(
                    v, DEFAULT_VERTEX_ID_KEY, DefaultAttribute.createAttribute(i));
            }
        };

        public final Consumer<Triple<Integer, Integer, Double>> edgeConsumer = t -> {
            int source = t.getFirst();
            V from = getElement(list, source - 1);
            if (from == null) {
                throw new ImportException("Node " + source + " does not exist");
            }

            int target = t.getSecond();
            V to = getElement(list, target - 1);
            if (to == null) {
                throw new ImportException("Node " + target + " does not exist");
            }

            E e = graph.addEdge(from, to);
            if (graph.getType().isWeighted()) {
                double weight = t.getThird() == null ? defaultWeight : t.getThird();
                graph.setEdgeWeight(e, weight);
            }

            notifyEdge(e);
        };

    }

    private static <E> E getElement(List<E> list, int index)
    {
        return index < list.size() ? list.get(index) : null;
    }
}
