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
 * DOTExporterTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Trevor Harmon
 *
 */
package org.jgrapht.ext;

import com.google.common.collect.Maps;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * .
 *
 * @author Trevor Harmon
 */
public class DOTExporterTest
    extends TestCase
{
    //~ Static fields/initializers ---------------------------------------------

    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    // TODO jvs 23-Dec-2006:  externalized diff-based testing framework

    private static final String UNDIRECTED =
        "graph G {" + NL
        + "  1 [ label=\"a\" ];" + NL
        + "  2 [ x=\"y\" ];" + NL
        + "  3;" + NL
        + "  1 -- 2;" + NL
        + "  3 -- 1;" + NL
        + "}" + NL;

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
        final ComponentAttributeProvider<String> vertexAttributeProvider =
            new ComponentAttributeProvider<String>() {
                @Override
                public Map<String, String> getComponentAttributes(final String v)
                {
                    Map<String, String> map = Maps.newLinkedHashMap();
                    if (v.equals(V1)) {
                        map.put("label", "a");
                    } else if (v.equals(V2)) {
                        map.put("x", "y");
                    } else {
                        map = null;
                    }
                    return map;
                }
            };

        final DOTExporter<String, DefaultEdge> exporter =
            new DOTExporter<String, DefaultEdge>(
                new IntegerNameProvider<String>(),
                null,
                null,
                vertexAttributeProvider,
                null);
        exporter.export(w, g);
        assertEquals(UNDIRECTED, w.toString());
    }

    public void testValidNodeIDs()
    {
        final DOTExporter<String, DefaultEdge> exporter =
            new DOTExporter<String, DefaultEdge>(
                new StringNameProvider<String>(),
                new StringNameProvider<String>(),
                null);

        final List<String> validVertices =
            Arrays.asList(
                "-9.78",
                "-.5",
                "12",
                "a",
                "12",
                "abc_78",
                "\"--34asdf\"");
        for (final String vertex : validVertices) {
            final Graph<String, DefaultEdge> graph =
                new DefaultDirectedGraph<String, DefaultEdge>(
                    DefaultEdge.class);
            graph.addVertex(vertex);

            exporter.export(new StringWriter(), graph);
        }

        final List<String> invalidVertices =
            Arrays.asList("2test", "--4", "foo-bar", "", "t:32");
        for (final String vertex : invalidVertices) {
            final Graph<String, DefaultEdge> graph =
                new DefaultDirectedGraph<String, DefaultEdge>(
                    DefaultEdge.class);
            graph.addVertex(vertex);

            try {
                exporter.export(new StringWriter(), graph);
                Assert.fail(vertex);
            } catch (RuntimeException re) {
                // this is a negative test so exception is expected
            }
        }
    }
}

// End DOTExporterTest.java
