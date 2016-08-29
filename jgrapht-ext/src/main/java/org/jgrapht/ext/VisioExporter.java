/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* ------------------
 * VisioExporter.java
 * ------------------
 * (C) Copyright 2003-2016, by Avner Linder and Contributors.
 *
 * Original Author:  Avner Linder
 * Contributor(s):   Barak Naveh, Dimitrios Michail
 *
 * Changes
 * -------
 * 27-May-2004 : Initial Version (AL);
 *
 */
package org.jgrapht.ext;

import org.jgrapht.Graph;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;


/**
 * Exports a graph to a CSV format that can be imported into MS Visio.
 *
 * <p><b>Tip:</b> By default, the exported graph doesn't show link directions.
 * To show link directions:<br>
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
    implements GraphExporter<V, E>
{
    private VertexNameProvider<V> vertexNameProvider;

    /**
     * Creates a new VisioExporter with the specified naming policy.
     *
     * @param vertexNameProvider the vertex name provider to be used for naming
     * the Visio shapes.
     */
    public VisioExporter(VertexNameProvider<V> vertexNameProvider)
    {
        this.vertexNameProvider = vertexNameProvider;
    }

    /**
     * Creates a new VisioExporter.
     */
    public VisioExporter()
    {
        this(new StringNameProvider<>());
    }

    /**
     * Exports the specified graph into a Visio CSV file format.
     *
     * @param output the print stream to which the graph to be exported.
     * @param g the graph to be exported.
     */
    @Deprecated
    public void export(OutputStream output, Graph<V, E> g)
    {
        try {
            exportGraph(g, output);
        } catch (ExportException e) {
            // ignore
        }
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
        String sourceName =
            vertexNameProvider.getVertexName(g.getEdgeSource(edge));
        String targetName =
            vertexNameProvider.getVertexName(g.getEdgeTarget(edge));

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
        String name = vertexNameProvider.getVertexName(vertex);

        out.print("Shape,");
        out.print(name);
        out.print(",,"); // MasterName field left empty
        out.print(name);
        out.print("\n");
    }
}

// End VisioExporter.java
