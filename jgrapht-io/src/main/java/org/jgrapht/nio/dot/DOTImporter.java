/*
 * (C) Copyright 2016-2020, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.dot;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Import a graph from a DOT file.
 *
 * <p>
 * For a description of the format see <a href="http://en.wikipedia.org/wiki/DOT_language">
 * http://en.wikipedia.org/wiki/DOT_language</a> and
 * <a href="http://www.graphviz.org/doc/info/lang.html">
 * http://www.graphviz.org/doc/info/lang.html</a>
 * 
 * <p>
 * The provided graph object, where the imported graph will be stored, must be able to support the
 * features of the graph that is read. For example if the file contains self-loops then the graph
 * provided must also support self-loops. The same for multiple edges. Whether edges are directed or
 * not depends on the underlying implementation of the user provided graph object.
 * 
 * <p>
 * The graph vertices and edges are build using the corresponding graph suppliers. The id of the
 * vertices in the original dot file are reported as a vertex attribute named "ID". Thus, in case
 * vertices in the dot file also contain an "ID" attribute, such an attribute will be reported
 * multiple times.
 * 
 * <p>
 * The default behavior of the importer is to use the graph vertex supplier in order to create
 * vertices. The user can also bypass vertex creation by providing a custom vertex factory method
 * using {@link #setVertexFactory(Function)}. The factory method is responsible to create a new
 * graph vertex given the vertex identifier read from file.
 *
 * @author Dimitrios Michail
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class DOTImporter<V, E>
    extends
    BaseEventDrivenImporter<V, E>
    implements
    GraphImporter<V, E>
{
    /**
     * Default key used for vertex ID.
     */
    public static final String DEFAULT_VERTEX_ID_KEY = "ID";

    private Function<String, V> vertexFactory;

    /**
     * Constructs a new importer.
     */
    public DOTImporter()
    {
        super();
    }

    @Override
    public void importGraph(Graph<V, E> graph, Reader input)
    {
        DOTEventDrivenImporter genericImporter = new DOTEventDrivenImporter();
        Consumers consumers = new Consumers(graph);
        genericImporter.addVertexConsumer(consumers.vertexConsumer);
        genericImporter.addVertexAttributeConsumer(consumers.vertexAttributeConsumer);
        genericImporter.addEdgeConsumer(consumers.edgeConsumer);
        genericImporter.addEdgeAttributeConsumer(consumers.edgeAttributeConsumer);
        genericImporter.addGraphAttributeConsumer(consumers.graphAttributeConsumer);
        genericImporter.importInput(input);
    }

    /**
     * Get the user custom vertex factory. This is null by default and the graph supplier is used
     * instead.
     * 
     * @return the user custom vertex factory
     */
    public Function<String, V> getVertexFactory()
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
    public void setVertexFactory(Function<String, V> vertexFactory)
    {
        this.vertexFactory = vertexFactory;
    }

    private class Consumers
    {
        private Graph<V, E> graph;
        private Map<String, V> map;
        private Pair<String, String> lastPair;
        private E lastEdge;

        public Consumers(Graph<V, E> graph)
        {
            this.graph = graph;
            this.map = new HashMap<>();
        }

        public final BiConsumer<String, Attribute> graphAttributeConsumer = (k, a) -> {
            notifyGraphAttribute(k, a);
        };

        public final Consumer<String> vertexConsumer = (t) -> {
            if (map.containsKey(t)) {
                throw new ImportException("Node " + t + " already exists");
            }
            V v;
            if (vertexFactory != null) {
                v = vertexFactory.apply(t);
                graph.addVertex(v);
            } else {
                v = graph.addVertex();
            }
            map.put(t, v);
            notifyVertex(v);
            notifyVertexAttribute(v, DEFAULT_VERTEX_ID_KEY, DefaultAttribute.createAttribute(t));
        };

        public final BiConsumer<Pair<String, String>, Attribute> vertexAttributeConsumer =
            (p, a) -> {
                String vertex = p.getFirst();
                if (!map.containsKey(vertex)) {
                    throw new ImportException("Node " + vertex + " does not exist");
                }
                notifyVertexAttribute(map.get(vertex), p.getSecond(), a);
            };

        public final Consumer<Pair<String, String>> edgeConsumer = (p) -> {
            String source = p.getFirst();
            V from = map.get(p.getFirst());
            if (from == null) {
                throw new ImportException("Node " + source + " does not exist");
            }

            String target = p.getSecond();
            V to = map.get(target);
            if (to == null) {
                throw new ImportException("Node " + target + " does not exist");
            }

            E e = graph.addEdge(from, to);
            notifyEdge(e);

            lastPair = p;
            lastEdge = e;
        };

        public final BiConsumer<Pair<Pair<String, String>, String>,
            Attribute> edgeAttributeConsumer = (p, a) -> {
                if (p.getFirst() == lastPair) {
                    notifyEdgeAttribute(lastEdge, p.getSecond(), a);
                }
            };

    }

}
