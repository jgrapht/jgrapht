/*
 * (C) Copyright 2016-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.csv;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.jgrapht.Graph;
import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.nio.BaseEventDrivenImporter;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.ImportException;

/**
 * Imports a graph from a CSV Format or any other Delimiter-separated value format.
 * 
 * <p>
 * The importer supports various different formats which can be adjusted using the
 * {@link #setFormat(CSVFormat) setFormat} method. The supported formats are the same CSV formats
 * used by <a href="https://gephi.org/users/supported-graph-formats/csv-format">Gephi </a>. For some
 * of the formats, the behavior of the importer can be adjusted using the
 * {@link #setParameter(org.jgrapht.nio.csv.CSVFormat.Parameter, boolean) setParameter} method. See
 * {@link CSVFormat} for a description of the formats.
 * </p>
 * 
 * <p>
 * The importer respects <a href="http://www.ietf.org/rfc/rfc4180.txt">rfc4180</a>. The caller can
 * also adjust the separator to something like semicolon or pipe instead of comma. In such a case,
 * all fields are unescaped using the new separator. See
 * <a href="https://en.wikipedia.org/wiki/Delimiter-separated_values">Delimiter- separated
 * values</a> for more information.
 * </p>
 * 
 * <p>
 * This importer does not distinguish between {@link CSVFormat#EDGE_LIST} and
 * {@link CSVFormat#ADJACENCY_LIST}. In both cases it assumes the format is
 * {@link CSVFormat#ADJACENCY_LIST}.
 * </p>
 * 
 * @see CSVFormat
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class CSVImporter<V, E>
    extends
    BaseEventDrivenImporter<V, E>
    implements
    GraphImporter<V, E>
{
    private static final char DEFAULT_DELIMITER = ',';
    private static final String DEFAULT_VERTEX_ID_KEY = "ID";
    private static final String DEFAULT_WEIGHT_KEY = "weight";

    private CSVFormat format;
    private char delimiter;
    private final Set<CSVFormat.Parameter> parameters;

    /**
     * Constructs a new importer using the {@link CSVFormat#ADJACENCY_LIST} format as default.
     */
    public CSVImporter()
    {
        this(CSVFormat.ADJACENCY_LIST, DEFAULT_DELIMITER);
    }

    /**
     * Constructs a new importer.
     * 
     * @param format format to use out of the supported ones
     */
    public CSVImporter(CSVFormat format)
    {
        this(format, DEFAULT_DELIMITER);
    }

    /**
     * Constructs a new importer.
     * 
     * @param format format to use out of the supported ones
     * @param delimiter delimiter to use (comma, semicolon, pipe, etc.)
     */
    public CSVImporter(CSVFormat format, char delimiter)
    {
        super();
        this.format = format;
        if (!DSVUtils.isValidDelimiter(delimiter)) {
            throw new IllegalArgumentException("Character cannot be used as a delimiter");
        }
        this.delimiter = delimiter;
        this.parameters = new HashSet<>();
    }

    /**
     * Get the format that the importer is using.
     * 
     * @return the input format
     */
    public CSVFormat getFormat()
    {
        return format;
    }

    /**
     * Set the format of the importer
     * 
     * @param format the format to use
     */
    public void setFormat(CSVFormat format)
    {
        this.format = format;
    }

    /**
     * Get the delimiter (comma, semicolon, pipe, etc).
     * 
     * @return the delimiter
     */
    public char getDelimiter()
    {
        return delimiter;
    }

    /**
     * Set the delimiter (comma, semicolon, pipe, etc).
     * 
     * @param delimiter the delimiter to use
     */
    public void setDelimiter(char delimiter)
    {
        if (!DSVUtils.isValidDelimiter(delimiter)) {
            throw new IllegalArgumentException("Character cannot be used as a delimiter");
        }
        this.delimiter = delimiter;
    }

    /**
     * Return if a particular parameter of the exporter is enabled
     * 
     * @param p the parameter
     * @return {@code true} if the parameter is set, {@code false} otherwise
     */
    public boolean isParameter(CSVFormat.Parameter p)
    {
        return parameters.contains(p);
    }

    /**
     * Set the value of a parameter of the exporter
     * 
     * @param p the parameter
     * @param value the value to set
     */
    public void setParameter(CSVFormat.Parameter p, boolean value)
    {
        if (value) {
            parameters.add(p);
        } else {
            parameters.remove(p);
        }
    }

    /**
     * Import a graph.
     * 
     * <p>
     * The provided graph must be able to support the features of the graph that is read. For
     * example if the input contains self-loops then the graph provided must also support
     * self-loops. The same for multiple edges.
     * 
     * <p>
     * If the provided graph is a weighted graph, the importer also reads edge weights.
     * 
     * @param graph the graph
     * @param input the input reader
     * @throws ImportException in case an error occurs, such as I/O or parse error
     */
    @Override
    public void importGraph(Graph<V, E> graph, Reader input)
        throws ImportException
    {
        CSVEventDrivenImporter genericImporter = new CSVEventDrivenImporter();
        genericImporter.setDelimiter(delimiter);
        genericImporter.setFormat(format);
        genericImporter
            .setParameter(
                CSVFormat.Parameter.EDGE_WEIGHTS, isParameter(CSVFormat.Parameter.EDGE_WEIGHTS));
        genericImporter
            .setParameter(
                CSVFormat.Parameter.MATRIX_FORMAT_NODEID,
                isParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID));
        genericImporter
            .setParameter(
                CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE,
                isParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE));

        Consumers consumers = new Consumers(graph);
        genericImporter.addVertexConsumer(consumers.vertexConsumer);
        genericImporter.addEdgeConsumer(consumers.edgeConsumer);
        genericImporter.importInput(input);
    }

    private class Consumers
    {
        private Graph<V, E> graph;
        private GraphType graphType;
        private Map<String, V> map;

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
            V v = graph.addVertex();
            map.put(t, v);
            notifyVertex(v);
            notifyVertexAttribute(v, DEFAULT_VERTEX_ID_KEY, DefaultAttribute.createAttribute(t));
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
            if (graphType.isWeighted() && t.getThird() != null) {
                notifyEdgeAttribute(
                    e, DEFAULT_WEIGHT_KEY, DefaultAttribute.createAttribute(t.getThird()));
            }
        };

    }

}
