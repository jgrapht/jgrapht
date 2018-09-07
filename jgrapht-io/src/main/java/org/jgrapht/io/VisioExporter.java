/*
 * (C) Copyright 2003-2017, by Avner Linder and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.io;

import org.jgrapht.*;

import java.io.*;

/**
 * Exports a graph to a CSV format that can be imported into MS Visio.
 *
 * <p>
 * <b>Tip:</b> By default, the exported graph doesn't show link directions. To show link
 * directions:<br>
 *
 * <ol>
 * <li>Select All (Ctrl-A)</li>
 * <li>Right Click the selected items</li>
 * <li>Format/Line...</li>
 * <li>Line ends: End: (choose an arrow)</li>
 * </ol>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Avner Linder
 */
public class VisioExporter<V, E>
    extends
    AbstractBaseExporter<V, E>
    implements
    GraphExporter<V, E>
{
    /**
     * Creates a new VisioExporter with the specified naming policy.
     *
     * @param vertexIDProvider the vertex id provider to be used for naming the Visio shapes
     */
    public VisioExporter(ComponentNameProvider<V> vertexIDProvider)
    {
        super(vertexIDProvider);
    }

    /**
     * Creates a new VisioExporter.
     */
    public VisioExporter()
    {
        this(new StringComponentNameProvider<>());
    }

    /**
     * Exports the specified graph into a Visio CSV file format.
     *
     * @param g the graph to be exported.
     * @param writer the writer to which the graph to be exported.
     */
    @Override
    public void exportGraph(Graph<V, E> g, Writer writer)
    {
        PrintWriter out = new PrintWriter(writer);

        for (V v : g.vertexSet()) {
            exportVertex(out, v);
        }

        for (E e : g.edgeSet()) {
            exportEdge(out, e, g);
        }

        out.flush();
    }

    private void exportEdge(PrintWriter out, E edge, Graph<V, E> g)
    {
        String sourceName = vertexIDProvider.getName(g.getEdgeSource(edge));
        String targetName = vertexIDProvider.getName(g.getEdgeTarget(edge));

        out.print("Link,");

        // create unique ShapeId for link
        out.print(sourceName);
        out.print("-->");
        out.print(targetName);

        // MasterName and Text fields left blank
        out.print(",,,");
        out.print(sourceName);
        out.print(",");
        out.print(targetName);
        out.print("\n");
    }

    private void exportVertex(PrintWriter out, V vertex)
    {
        String name = vertexIDProvider.getName(vertex);

        out.print("Shape,");
        out.print(name);
        out.print(",,"); // MasterName field left empty
        out.print(name);
        out.print("\n");
    }
}

// End VisioExporter.java
