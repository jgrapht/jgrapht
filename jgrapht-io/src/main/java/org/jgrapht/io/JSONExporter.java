/*
 * (C) Copyright 2019-2019, Dimitrios Michail and Contributors.
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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.jgrapht.Graph;

/**
 * Exports a graph using <a href="https://tools.ietf.org/html/rfc8259">JSON</a>.
 * 
 * <p>
 * The output is one object which contains:
 * <ul>
 * <li>A member named <code>nodes</code> whose value is an array of nodes.
 * <li>A member named <code>edges</code> whose value is an array of edges.
 * <li>Two members named <code>creator</code> and <code>version</code> for metadata.
 * </ul>
 * 
 * <p>
 * Each node contains an identifier and possibly other attributes provided using a
 * {@link ComponentAttributeProvider}. Similarly each edge contains the source and target vertices,
 * a possible identifier using {@link ComponentNameProvider} and possible other attributes using a
 * {@link ComponentAttributeProvider}. All these can be adjusted using the appropriate constructor
 * call, see
 * {@link #JSONExporter(ComponentNameProvider, ComponentAttributeProvider, ComponentNameProvider, ComponentAttributeProvider)}.
 * The default constructor constructs integer identifiers using an
 * {@link IntegerComponentNameProvider} for both vertices and edges and does not output any custom
 * attributes using {@link EmptyComponentAttributeProvider}.
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class JSONExporter<V, E>
    extends
    AbstractBaseExporter<V, E>
    implements
    GraphExporter<V, E>
{
    private static final String CREATOR = "JGraphT JSON Exporter";
    private static final String VERSION = "1";

    private ComponentAttributeProvider<V> vertexAttributeProvider;
    private ComponentAttributeProvider<E> edgeAttributeProvider;

    /**
     * Creates a new exporter with integer name provider for the vertex identifiers.
     */
    public JSONExporter()
    {
        this(new IntegerComponentNameProvider<>());
    }

    /**
     * Creates a new exporter.
     * 
     * @param vertexIDProvider for generating vertex identifiers. Must not be null.
     */
    public JSONExporter(ComponentNameProvider<V> vertexIDProvider)
    {
        this(
            vertexIDProvider, new EmptyComponentAttributeProvider<>(),
            new IntegerComponentNameProvider<>(), new EmptyComponentAttributeProvider<>());
    }

    /**
     * Constructs a new exporter
     *
     * @param vertexIDProvider for generating vertex identifiers. Must not be null.
     * @param vertexAttributeProvider for generating vertex attributes. If null, no additional
     *        attributes will be exported.
     * @param edgeIDProvider for generating edge identifiers. Must not be null.
     * @param edgeAttributeProvider for generating edge attributes. If null, no additional
     *        attributes will be exported.
     */
    public JSONExporter(
        ComponentNameProvider<V> vertexIDProvider,
        ComponentAttributeProvider<V> vertexAttributeProvider,
        ComponentNameProvider<E> edgeIDProvider,
        ComponentAttributeProvider<E> edgeAttributeProvider)
    {
        super(vertexIDProvider, edgeIDProvider);
        this.vertexAttributeProvider = Objects.requireNonNull(vertexAttributeProvider);
        this.edgeAttributeProvider = Objects.requireNonNull(edgeAttributeProvider);
    }

    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
        throws ExportException
    {
        export(g, writer);
    }

    private void export(Graph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);

        out.print('{');

        /*
         * Version
         */
        out.print(quoted("creator"));
        out.print(':');
        out.print(quoted(CREATOR));

        out.print(',');
        out.print(quoted("version"));
        out.print(':');
        out.print(quoted(VERSION));

        /*
         * Vertices
         */
        out.print(',');
        out.print(quoted("nodes"));
        out.print(':');
        out.print('[');
        boolean printComma = false;
        for (V v : g.vertexSet()) {
            if (!printComma) {
                printComma = true;
            } else {
                out.print(',');
            }
            exportVertex(out, g, v);
        }
        out.print("]");

        /*
         * Edges
         */
        out.print(',');
        out.print(quoted("edges"));
        out.print(':');
        out.print('[');
        printComma = false;
        for (E e : g.edgeSet()) {
            if (!printComma) {
                printComma = true;
            } else {
                out.print(',');
            }
            exportEdge(out, g, e);
        }
        out.print("]");

        out.print('}');

        out.flush();
    }

    private void exportVertex(PrintWriter out, Graph<V, E> g, V v)
    {
        String vertexId = vertexIDProvider.getName(v);

        out.print('{');
        out.print(quoted("id"));
        out.print(':');
        out.print(quoted(vertexId));
        exportVertexAttributes(out, g, v);
        out.print('}');
    }

    private void exportEdge(PrintWriter out, Graph<V, E> g, E e)
    {
        V source = g.getEdgeSource(e);
        String sourceId = vertexIDProvider.getName(source);
        V target = g.getEdgeTarget(e);
        String targetId = vertexIDProvider.getName(target);

        out.print('{');

        boolean hasId = false;
        if (edgeIDProvider != null) {
            String edgeId = edgeIDProvider.getName(e);
            if (edgeId != null) {
                out.print(quoted("id"));
                out.print(':');
                out.print(quoted(edgeId));
                hasId = true;
            }
        }
        if (hasId) {
            out.print(',');
        }
        out.print(quoted("source"));
        out.print(':');
        out.print(quoted(sourceId));
        out.print(',');
        out.print(quoted("target"));
        out.print(':');
        out.print(quoted(targetId));

        exportEdgeAttributes(out, g, e);

        out.print('}');
    }

    private void exportVertexAttributes(PrintWriter out, Graph<V, E> g, V v)
    {
        vertexAttributeProvider
            .getComponentAttributes(v).entrySet().stream().filter(e -> !e.getKey().equals("id"))
            .forEach(entry -> {
                out.print(",");
                out.print(quoted(entry.getKey()));
                out.print(":");
                outputValue(out, entry.getValue());
            });
    }

    private void exportEdgeAttributes(PrintWriter out, Graph<V, E> g, E e)
    {
        Set<String> forbidden = new HashSet<>(Arrays.asList("id", "source", "target"));
        edgeAttributeProvider
            .getComponentAttributes(e).entrySet().stream()
            .filter(entry -> !forbidden.contains(entry.getKey())).forEach(entry -> {
                out.print(",");
                out.print(quoted(entry.getKey()));
                out.print(":");
                outputValue(out, entry.getValue());
            });
    }
    
    private void outputValue(PrintWriter out, Attribute value) {
        AttributeType type = value.getType();
        if (type.equals(AttributeType.BOOLEAN)) {
            boolean booleanValue = Boolean.parseBoolean(value.getValue());
            out.print(booleanValue?"true":"false");
        } else if (type.equals(AttributeType.INT)) { 
            out.print(Integer.parseInt(value.getValue()));
        } else if (type.equals(AttributeType.LONG)) { 
            out.print(Long.parseLong(value.getValue()));
        } else if (type.equals(AttributeType.FLOAT)) { 
            float floatValue = Float.parseFloat(value.getValue());
            if (!Float.isFinite(floatValue)) {
                throw new IllegalArgumentException("Infinity and NaN not allowed in JSON");
            }
            out.print(floatValue);
        } else if (type.equals(AttributeType.DOUBLE)) {
            double doubleValue = Double.parseDouble(value.getValue());
            if (!Double.isFinite(doubleValue)) {
                throw new IllegalArgumentException("Infinity and NaN not allowed in JSON");
            }
            out.print(doubleValue);
        } else { 
            out.print(quoted(value.toString()));
        }
    }

    private String quoted(final String s)
    {
        return "\"" + StringEscapeUtils.escapeJson(s) + "\"";
    }

}
