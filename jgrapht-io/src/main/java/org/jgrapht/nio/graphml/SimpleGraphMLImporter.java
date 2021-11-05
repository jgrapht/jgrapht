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
package org.jgrapht.nio.graphml;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Imports a graph from a GraphML data source.
 * 
 * <p>
 * This is a simple implementation with supports only a limited set of features of the GraphML
 * specification. For a more rigorous parser use {@link GraphMLImporter}. This version is oriented
 * towards parsing speed.
 * 
 * <p>
 * The importer uses the graph suppliers ({@link Graph#getVertexSupplier()} and
 * {@link Graph#getEdgeSupplier()}) in order to create new vertices and edges. Moreover, it notifies
 * lazily and completely out-of-order for any additional vertex, edge or graph attributes in the
 * input file. Users can register consumers for vertex, edge and graph attributes after construction
 * of the importer. Finally, default attribute values are completely ignored.
 * 
 * <p>
 * For a description of the format see <a href="http://en.wikipedia.org/wiki/GraphML">
 * http://en.wikipedia.org/wiki/ GraphML</a> or the
 * <a href="http://graphml.graphdrawing.org/primer/graphml-primer.html">GraphML Primer</a>.
 * </p>
 * 
 * <p>
 * Below is small example of a graph in GraphML format.
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <graphml xmlns="http://graphml.graphdrawing.org/xmlns"  
 *     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *     xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns 
 *     http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
 *   <key id="d0" for="node" attr.name="color" attr.type="string" />
 *   <key id="d1" for="edge" attr.name="weight" attr.type="double"/>
 *   <graph id="G" edgedefault="undirected">
 *     <node id="n0">
 *       <data key="d0">green</data>
 *     </node>
 *     <node id="n1">
 *       <data key="d0">black</data>
 *     </node>     
 *     <node id="n2">
 *       <data key="d0">blue</data>
 *     </node>
 *     <node id="n3">
 *       <data key="d0">red</data>
 *     </node>
 *     <node id="n4">
 *       <data key="d0">white</data>
 *     </node>
 *     <node id="n5">
 *       <data key="d0">turquoise</data>
 *     </node>
 *     <edge id="e0" source="n0" target="n2">
 *       <data key="d1">1.0</data>
 *     </edge>
 *     <edge id="e1" source="n0" target="n1">
 *       <data key="d1">1.0</data>
 *     </edge>
 *     <edge id="e2" source="n1" target="n3">
 *       <data key="d1">2.0</data>
 *     </edge>
 *     <edge id="e3" source="n3" target="n2"/>
 *     <edge id="e4" source="n2" target="n4"/>
 *     <edge id="e5" source="n3" target="n5"/>
 *     <edge id="e6" source="n5" target="n4">
 *       <data key="d1">1.1</data>
 *     </edge>
 *   </graph>
 * </graphml>
 * }
 * </pre>
 * 
 * <p>
 * The importer reads the input into a graph which is provided by the user. In case the graph is
 * weighted and the corresponding edge key with attr.name="weight" is defined, the importer also
 * reads edge weights. Otherwise edge weights are ignored. To test whether the graph is weighted,
 * method {@link Graph#getType()} can be used.
 * 
 * <p>
 * The provided graph object, where the imported graph will be stored, must be able to support the
 * features of the graph that is read. For example if the GraphML file contains self-loops then the
 * graph provided must also support self-loops. The same for multiple edges. Moreover, the parser
 * completely ignores the attribute "edgedefault" which denotes whether an edge is directed or not.
 * Whether edges are directed or not depends on the underlying implementation of the user provided
 * graph object.
 * 
 * <p>
 * The importer by default validates the input using the 1.0
 * <a href="http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">GraphML Schema</a>. The user can
 * (not recommended) disable the validation by calling {@link #setSchemaValidation(boolean)}.
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
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class SimpleGraphMLImporter<V, E>
    extends
    BaseEventDrivenImporter<V, E>
    implements
    GraphImporter<V, E>
{
    /**
     * Default key used for vertex ID.
     */
    public static final String DEFAULT_VERTEX_ID_KEY = "ID";

    private static final String EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME = "weight";
    private boolean schemaValidation;
    private String edgeWeightAttributeName = EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME;
    private Function<String, V> vertexFactory;

    /**
     * Constructs a new importer.
     */
    public SimpleGraphMLImporter()
    {
        super();
        this.schemaValidation = true;
    }

    /**
     * Get the attribute name for edge weights
     * 
     * @return the attribute name
     */
    public String getEdgeWeightAttributeName()
    {
        return edgeWeightAttributeName;
    }

    /**
     * Set the attribute name to use for edge weights.
     * 
     * @param edgeWeightAttributeName the attribute name
     */
    public void setEdgeWeightAttributeName(String edgeWeightAttributeName)
    {
        this.edgeWeightAttributeName = Objects
            .requireNonNull(edgeWeightAttributeName, "Edge weight attribute name cannot be null");
    }

    /**
     * Whether the importer validates the input
     * 
     * @return true if the importer validates the input
     */
    public boolean isSchemaValidation()
    {
        return schemaValidation;
    }

    /**
     * Set whether the importer should validate the input
     * 
     * @param schemaValidation value for schema validation
     */
    public void setSchemaValidation(boolean schemaValidation)
    {
        this.schemaValidation = schemaValidation;
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

    /**
     * Import a graph.
     * 
     * <p>
     * The provided graph must be able to support the features of the graph that is read. For
     * example if the GraphML file contains self-loops then the graph provided must also support
     * self-loops. The same for multiple edges.
     * 
     * @param graph the output graph
     * @param input the input reader
     * @throws ImportException in case an error occurs, such as I/O or parse error
     */
    @Override
    public void importGraph(Graph<V, E> graph, Reader input)
    {
        SimpleGraphMLEventDrivenImporter genericImporter = new SimpleGraphMLEventDrivenImporter();
        genericImporter.setEdgeWeightAttributeName(edgeWeightAttributeName);
        genericImporter.setSchemaValidation(schemaValidation);

        Consumers globalConsumer = new Consumers(graph);
        genericImporter.addGraphAttributeConsumer(globalConsumer.graphAttributeConsumer);
        genericImporter.addVertexAttributeConsumer(globalConsumer.vertexAttributeConsumer);
        genericImporter.addEdgeAttributeConsumer(globalConsumer.edgeAttributeConsumer);
        genericImporter.addVertexConsumer(globalConsumer.vertexConsumer);
        genericImporter.addEdgeConsumer(globalConsumer.edgeConsumer);
        genericImporter.importInput(input);
    }

    private class Consumers
    {
        private Graph<V, E> graph;
        private Map<String, V> nodesMap;
        private E lastEdge;
        private Triple<String, String, Double> lastTriple;

        public Consumers(Graph<V, E> graph)
        {
            this.graph = graph;
            this.nodesMap = new HashMap<>();
            this.lastEdge = null;
            this.lastTriple = null;
        }

        public final BiConsumer<String, Attribute> graphAttributeConsumer = (key, a) -> {
            notifyGraphAttribute(key, a);
        };

        public final BiConsumer<Pair<String, String>, Attribute> vertexAttributeConsumer =
            (vertexAndKey, a) -> {
                notifyVertexAttribute(
                    mapNode(vertexAndKey.getFirst()), vertexAndKey.getSecond(), a);
            };

        public final BiConsumer<Pair<Triple<String, String, Double>, String>,
            Attribute> edgeAttributeConsumer = (edgeAndKey, a) -> {
                Triple<String, String, Double> qe = edgeAndKey.getFirst();

                if (qe == lastTriple) {
                    if (qe.getThird() != null
                        && edgeWeightAttributeName.equals(edgeAndKey.getSecond())
                        && graph.getType().isWeighted())
                {
                        graph.setEdgeWeight(lastEdge, qe.getThird());
                    }

                    notifyEdgeAttribute(lastEdge, edgeAndKey.getSecond(), a);
                }
            };

        public final Consumer<String> vertexConsumer = (vId) -> {
            V v = mapNode(vId);
            notifyVertex(v);
            notifyVertexAttribute(v, DEFAULT_VERTEX_ID_KEY, DefaultAttribute.createAttribute(vId));
        };

        public final Consumer<Triple<String, String, Double>> edgeConsumer = (qe) -> {
            if (lastTriple != qe) {
                String source = qe.getFirst();
                String target = qe.getSecond();
                Double weight = qe.getThird();

                E e = graph.addEdge(mapNode(source), mapNode(target));
                if (weight != null && graph.getType().isWeighted()) {
                    graph.setEdgeWeight(e, weight);
                }

                lastEdge = e;
                lastTriple = qe;

                notifyEdge(lastEdge);
            }
        };

        private V mapNode(String vId)
        {
            V vertex = nodesMap.get(vId);
            if (vertex == null) {
                if (vertexFactory != null) {
                    vertex = vertexFactory.apply(vId);
                    graph.addVertex(vertex);
                } else {
                    vertex = graph.addVertex();
                }
                nodesMap.put(vId, vertex);
            }
            return vertex;
        }

    }

}
