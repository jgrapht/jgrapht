/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.gml;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Imports a graph from a GML file (Graph Modeling Language).
 * 
 * <p>
 * For a description of the format see <a href="http://www.infosun.fmi.uni-passau.de/Graphlet/GML/">
 * http://www.infosun.fmi.uni-passau.de/Graphlet/GML/</a>.
 *
 * <p>
 * Below is small example of a graph in GML format.
 * 
 * <pre>
 * graph [
 *   node [ 
 *     id 1
 *   ]
 *   node [
 *     id 2
 *     label "Node 2 has an optional label"
 *   ]
 *   node [
 *     id 3
 *   ]
 *   edge [
 *     source 1
 *     target 2 
 *     weight 2.0
 *     label "Edge between 1 and 2"
 *   ]
 *   edge [
 *     source 2
 *     target 3
 *     weight 3.0
 *     label "Edge between 2 and 3"
 *   ]
 * ]
 * </pre>
 * 
 * <p>
 * In case the graph is weighted then the importer also reads edge weights. Otherwise edge weights
 * are ignored. The importer also supports reading additional string attributes such as label or
 * custom user attributes. String attributes are unescaped as if they are Java strings.
 * 
 * <p>
 * The parser completely ignores elements from the input that are not related to vertices or edges
 * of the graph. Moreover, complicated nested structures are simply returned as a whole. For
 * example, in the following graph
 * 
 * <pre>
 * graph [
 *   node [ 
 *     id 1
 *   ]
 *   node [ 
 *     id 2
 *   ]
 *   edge [
 *     source 1
 *     target 2 
 *     points [ x 1.0 y 2.0 ]
 *   ]
 * ]
 * </pre>
 * 
 * the points attribute of the edge is returned as a string containing "[ x 1.0 y 2.0 ]".
 * 
 * <p>
 * The graph vertices and edges are build using the corresponding graph suppliers. The id of the
 * vertices in the original file are reported as a vertex attribute named "ID".
 * 
 * <p>
 * The default behavior of the importer is to use the graph vertex supplier in order to create
 * vertices. The user can also bypass vertex creation by providing a custom vertex factory method
 * using {@link #setVertexFactory(Function)}. The factory method is responsible to create a new
 * graph vertex given the vertex identifier read from file.
 * 
 * @param <V> the vertex type
 * @param <E> the edge type
 * 
 * @author Dimitrios Michail
 */
public class GmlImporter<V, E>
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
     * Constructs a new importer.
     */
    public GmlImporter()
    {
        super();
    }

    /**
     * Import a graph.
     * 
     * <p>
     * The provided graph must be able to support the features of the graph that is read. For
     * example if the GML file contains self-loops then the graph provided must also support
     * self-loops. The same for multiple edges.
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
    {
        GmlEventDrivenImporter genericImporter = new GmlEventDrivenImporter();
        Consumers consumers = new Consumers(graph);
        genericImporter.addVertexConsumer(consumers.vertexConsumer);
        genericImporter.addVertexAttributeConsumer(consumers.vertexAttributeConsumer);
        genericImporter.addEdgeConsumer(consumers.edgeConsumer);
        genericImporter.addEdgeAttributeConsumer(consumers.edgeAttributeConsumer);
        genericImporter.importInput(input);
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

    private class Consumers
    {
        private Graph<V, E> graph;
        private GraphType graphType;
        private Map<Integer, V> map;
        private Triple<Integer, Integer, Double> lastTriple;
        private E lastEdge;

        public Consumers(Graph<V, E> graph)
        {
            this.graph = graph;
            this.graphType = graph.getType();
            this.map = new HashMap<>();
        }

        public final Consumer<Integer> vertexConsumer = (t) -> {
            getVertex(t);
        };

        public final BiConsumer<Pair<Integer, String>, Attribute> vertexAttributeConsumer =
            (p, a) -> {
                Integer vertex = p.getFirst();
                if (!map.containsKey(vertex)) {
                    throw new ImportException("Node " + vertex + " does not exist");
                }
                notifyVertexAttribute(map.get(vertex), p.getSecond(), a);
            };

        public final Consumer<Triple<Integer, Integer, Double>> edgeConsumer = (t) -> {
            V from = getVertex(t.getFirst());
            V to = getVertex(t.getSecond());

            E e = graph.addEdge(from, to);
            if (graphType.isWeighted() && t.getThird() != null) {
                graph.setEdgeWeight(e, t.getThird());
            }

            notifyEdge(e);

            lastTriple = t;
            lastEdge = e;
        };

        public final BiConsumer<Pair<Triple<Integer, Integer, Double>, String>,
            Attribute> edgeAttributeConsumer = (p, a) -> {
                if (p.getFirst() == lastTriple) {
                    notifyEdgeAttribute(lastEdge, p.getSecond(), a);
                }
            };

        private V getVertex(Integer id)
        {
            V v = map.get(id);
            if (v == null) {
                if (vertexFactory != null) {
                    v = vertexFactory.apply(id);
                    graph.addVertex(v);
                } else {
                    v = graph.addVertex();
                }
                map.put(id, v);

                /*
                 * Notify the first time we create the node.
                 */
                notifyVertex(v);
                notifyVertexAttribute(
                    v, DEFAULT_VERTEX_ID_KEY, DefaultAttribute.createAttribute(id));
            }
            return v;
        }

    }

}
