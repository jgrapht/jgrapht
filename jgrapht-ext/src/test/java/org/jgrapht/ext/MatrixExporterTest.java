/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* ------------------------------
 * MatrixExporterTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Charles Fry
 *
 * $Id$
 *
 * Changes
 * -------
 * 12-Dec-2005 : Initial revision (CF);
 *
 */
package org.jgrapht.ext;

import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.SimpleGraph;

import java.io.StringWriter;
import java.io.Writer;


/**
 * .
 *
 * @author Charles Fry
 */
public class MatrixExporterTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    // TODO jvs 23-Dec-2006:  externalized diff-based testing framework

    private static final String LAPLACIAN =
        "1 1 2" + NL
        + "1 2 -1" + NL
        + "1 3 -1" + NL
        + "2 2 1" + NL
        + "2 1 -1" + NL
        + "3 3 1" + NL
        + "3 1 -1" + NL;

    private static final String NORMALIZED_LAPLACIAN =
        "1 1 1" + NL
        + "1 2 -0.7071067811865475" + NL
        + "1 3 -0.7071067811865475" + NL
        + "2 2 1" + NL
        + "2 1 -0.7071067811865475" + NL
        + "3 3 1" + NL
        + "3 1 -0.7071067811865475" + NL;

    private static final String UNDIRECTED_ADJACENCY =
        "1 2 1" + NL
        + "1 3 1" + NL
        + "1 1 2" + NL
        + "2 1 1" + NL
        + "3 1 1" + NL;

    private static final String DIRECTED_ADJACENCY =
        "1 2 1" + NL
        + "3 1 2" + NL;

    private static final MatrixExporter<String, DefaultEdge> exporter =
        new MatrixExporter<String, DefaultEdge>();

    //~ Methods ----------------------------------------------------------------

    public void testLaplacian()
    {
        final UndirectedGraph<String, DefaultEdge> g =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        StringWriter w = new StringWriter();
        exporter.exportLaplacianMatrix(w, g);
        assertEquals(LAPLACIAN, w.toString());

        w = new StringWriter();
        exporter.exportNormalizedLaplacianMatrix(w, g);
        assertEquals(NORMALIZED_LAPLACIAN, w.toString());
    }

    public void testAdjacencyUndirected()
    {
        final UndirectedGraph<String, DefaultEdge> g =
            new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.addEdge(V1, V1);

        final StringWriter w = new StringWriter();
        exporter.exportAdjacencyMatrix(w, g);
        assertEquals(UNDIRECTED_ADJACENCY, w.toString());
    }

    public void testAdjacencyDirected()
    {
        final DirectedGraph<String, DefaultEdge> g =
            new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);
        g.addEdge(V3, V1);

        final Writer w = new StringWriter();
        exporter.exportAdjacencyMatrix(w, g);
        assertEquals(DIRECTED_ADJACENCY, w.toString());
    }
}

// End MatrixExporterTest.java
