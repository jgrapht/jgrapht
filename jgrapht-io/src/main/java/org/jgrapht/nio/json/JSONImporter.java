/*
 * (C) Copyright 2019-2020, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.json;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Imports a graph from a <a href="https://tools.ietf.org/html/rfc8259">JSON</a> file.
 * 
 * Below is a small example of a graph in JSON format.
 * 
 * <pre>
 * {
 *   "nodes": [
 *     { "id": "1" },
 *     { "id": "2", "label": "Node 2 label" },
 *     { "id": "3" }
 *   ],
 *   "edges": [
 *     { "source": "1", "target": "2", "weight": 2.0, "label": "Edge between 1 and 2" },
 *     { "source": "2", "target": "3", "weight": 3.0, "label": "Edge between 2 and 3" }
 *   ]
 * }
 * </pre>
 * 
 * <p>
 * In case the graph is weighted then the importer also reads edge weights. Otherwise edge weights
 * are ignored. The importer also supports reading additional string attributes such as label or
 * custom user attributes.
 * 
 * <p>
 * The parser completely ignores elements from the input that are not related to vertices or edges
 * of the graph. Moreover, complicated nested structures which are inside vertices or edges are
 * simply returned as a whole. For example, in the following graph
 * 
 * <pre>
 * {
 *   "nodes": [
 *     { "id": "1" },
 *     { "id": "2" }
 *   ],
 *   "edges": [
 *     { "source": "1", "target": "2", "points": { "x": 1.0, "y": 2.0 } }
 *   ]
 * }
 * </pre>
 * 
 * the points attribute of the edge is returned as a string containing {"x":1.0,"y":2.0}. The same
 * is done for arrays or any other arbitrary nested structure.
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
 * @param <V> the vertex type
 * @param <E> the edge type
 * 
 * @author Dimitrios Michail
 */
public class JSONImporter<V, E>
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
     * Construct a new importer
     */
    public JSONImporter()
    {
        super();
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
    {
        JSONEventDrivenImporter genericImporter = new JSONEventDrivenImporter();
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
        private GraphType graphType;
        private Map<String, V> map;
        private Triple<String, String, Double> lastTriple;
        private E lastEdge;

        public Consumers(Graph<V, E> graph)
        {
            this.graph = graph;
            this.graphType = graph.getType();
            this.map = new HashMap<>();
        }

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

        public final Consumer<Triple<String, String, Double>> edgeConsumer = (t) -> {
            String source = t.getFirst();
            V from = map.get(t.getFirst());
            if (from == null) {
                throw new ImportException("Node " + source + " does not exist");
            }

            String target = t.getSecond();
            V to = map.get(target);
            if (to == null) {
                throw new ImportException("Node " + target + " does not exist");
            }

            E e = graph.addEdge(from, to);
            if (graphType.isWeighted() && t.getThird() != null) {
                graph.setEdgeWeight(e, t.getThird());
            }
            notifyEdge(e);

            lastTriple = t;
            lastEdge = e;
        };

        public final BiConsumer<Pair<Triple<String, String, Double>, String>,
            Attribute> edgeAttributeConsumer = (p, a) -> {
                Triple<String, String, Double> t = p.getFirst();
                if (t == lastTriple) {
                    notifyEdgeAttribute(lastEdge, p.getSecond(), a);
                }
            };

    }

}
