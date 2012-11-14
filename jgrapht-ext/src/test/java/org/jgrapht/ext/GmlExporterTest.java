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
 * GmlExporterTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 23-Dec-2006 : Initial revision (JVS);
 *
 */
package org.jgrapht.ext;

import junit.framework.TestCase;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.StringWriter;


/**
 * .
 *
 * @author John V. Sichi
 */
public class GmlExporterTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    // TODO jvs 23-Dec-2006:  externalized diff-based testing framework

    private static final String UNDIRECTED =
        "Creator \"JGraphT GML Exporter\"" + NL
        + "Version 1" + NL
        + "graph" + NL
        + "[" + NL
        + "\tlabel \"\"" + NL
        + "\tdirected 0" + NL
        + "\tnode" + NL
        + "\t[" + NL
        + "\t\tid 1" + NL
        + "\t]" + NL
        + "\tnode" + NL
        + "\t[" + NL
        + "\t\tid 2" + NL
        + "\t]" + NL
        + "\tnode" + NL
        + "\t[" + NL
        + "\t\tid 3" + NL
        + "\t]" + NL
        + "\tedge" + NL
        + "\t[" + NL
        + "\t\tid 1" + NL
        + "\t\tsource 1" + NL
        + "\t\ttarget 2" + NL
        + "\t]" + NL
        + "\tedge" + NL
        + "\t[" + NL
        + "\t\tid 2" + NL
        + "\t\tsource 3" + NL
        + "\t\ttarget 1" + NL
        + "\t]" + NL
        + "]" + NL;

    private static final GmlExporter<String, DefaultEdge> exporter =
        new GmlExporter<String, DefaultEdge>();

    //~ Methods ----------------------------------------------------------------

    public void testUndirected()
    {
        final UndirectedGraph<String, DefaultEdge> g =
            new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        final StringWriter w = new StringWriter();
        exporter.export(w, g);
        assertEquals(UNDIRECTED, w.toString());
    }
}

// End GmlExporterTest.java
