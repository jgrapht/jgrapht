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

import org.jgrapht.alg.util.*;
import org.jgrapht.nio.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

/**
 * Imports a GraphML file as an edge list. Vertices are numbered from $0$ to $n-1$ in the order they
 * are first encountered in the input file.
 * 
 * <p>
 * This is a simple implementation with supports only a limited set of features of the GraphML
 * specification. For a more rigorous parser use {@link GraphMLImporter}. This version is oriented
 * towards parsing speed. Default attribute values are completely ignored.
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
 * The importer by default validates the input using the 1.0
 * <a href="http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">GraphML Schema</a>. The user can
 * (not recommended) disable the validation by calling {@link #setSchemaValidation(boolean)}.
 * 
 * @author Dimitrios Michail
 */
public class SimpleGraphMLEdgeListImporter
    extends
    BaseEventDrivenImporter<Integer, Triple<Integer, Integer, Double>>
    implements
    EventDrivenImporter<Integer, Triple<Integer, Integer, Double>>
{
    private static final String EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME = "weight";

    private boolean schemaValidation;
    private String edgeWeightAttributeName = EDGE_WEIGHT_DEFAULT_ATTRIBUTE_NAME;

    /**
     * Constructs a new importer.
     */
    public SimpleGraphMLEdgeListImporter()
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

    @Override
    public void importInput(Reader input)
        throws ImportException
    {
        SimpleGraphMLEventDrivenImporter genericImporter = new SimpleGraphMLEventDrivenImporter();
        genericImporter.setEdgeWeightAttributeName(edgeWeightAttributeName);
        genericImporter.setSchemaValidation(schemaValidation);
        Consumers consumers = new Consumers();
        genericImporter.addImportEventConsumer(consumers.eventConsumer);
        genericImporter.addVertexConsumer(consumers.vertexConsumer);
        genericImporter.addEdgeConsumer(consumers.edgeConsumer);
        genericImporter.addEdgeAttributeConsumer(consumers.edgeAttributeConsumer);
        genericImporter.importInput(input);
    }

    private class Consumers
    {
        private int nodeCount;
        private Map<String, Integer> vertexMap;
        private Triple<Integer, Integer, Double> lastIntegerTriple;
        private Triple<String, String, Double> lastTriple;

        public Consumers()
        {
            this.nodeCount = 0;
            this.vertexMap = new HashMap<>();
        }

        public final Consumer<ImportEvent> eventConsumer = (e) -> {
            if (ImportEvent.END.equals(e)) {
                if (lastTriple != null) {
                    notifyEdge(lastIntegerTriple);
                    lastTriple = null;
                    lastIntegerTriple = null;
                }
            }
        };

        public final Consumer<String> vertexConsumer = (v) -> {
            vertexMap.computeIfAbsent(v, k -> nodeCount++);
        };

        public final BiConsumer<Pair<Triple<String, String, Double>, String>,
            Attribute> edgeAttributeConsumer = (edgeAndKey, a) -> {
                Triple<String, String, Double> q = edgeAndKey.getFirst();
                String keyName = edgeAndKey.getSecond();
                if (lastTriple == q && edgeWeightAttributeName.equals(keyName)) {
                    lastTriple.setThird(q.getThird());
                    lastIntegerTriple.setThird(q.getThird());
                }
            };

        public final Consumer<Triple<String, String, Double>> edgeConsumer = (q) -> {
            if (q != lastTriple) {
                if (lastTriple != null) {
                    notifyEdge(lastIntegerTriple);
                }
                lastTriple = q;
                lastIntegerTriple = createIntegerTriple(q);
            }
        };

        private Triple<Integer, Integer, Double> createIntegerTriple(
            Triple<String, String, Double> e)
        {
            int source = vertexMap.computeIfAbsent(e.getFirst(), k -> {
                return Integer.valueOf(nodeCount++);
            });
            int target = vertexMap.computeIfAbsent(e.getSecond(), k -> {
                return Integer.valueOf(nodeCount++);
            });
            Double weight = e.getThird();

            return Triple.of(source, target, weight);
        }

    }

}
