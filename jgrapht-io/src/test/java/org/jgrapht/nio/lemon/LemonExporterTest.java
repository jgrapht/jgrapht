/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.nio.lemon;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Tests for {@link LemonExporter}
 * 
 * @author Dimitrios Michail
 */
public class LemonExporterTest
{
    private static final String V1 = "v1";
    private static final String V2 = "v2";
    private static final String V3 = "v3";

    private static final String NL = System.getProperty("line.separator");

    // @formatter:off
    private static final String UNDIRECTED =
            "#Creator: JGraphT Lemon (LGF) Exporter" + NL
            + "#Version: 1" + NL
            + NL
            + "@nodes" + NL
            + "label" + NL
            + "1" + NL
            + "2" + NL
            + "3" + NL            
            + NL
            + "@arcs" + NL
            + "\t\t-" + NL
            + "1\t2" + NL
            + "3\t1" + NL            
            + NL;
    
    private static final String UNDIRECTED_DEFAULT_WEIGHTS =
            "#Creator: JGraphT Lemon (LGF) Exporter" + NL
            + "#Version: 1" + NL
            + NL
            + "@nodes" + NL
            + "label" + NL
            + "1" + NL
            + "2" + NL
            + "3" + NL            
            + NL
            + "@arcs" + NL
            + "\t\tweight" + NL
            + "1\t2\t1.0" + NL
            + "3\t1\t1.0" + NL            
            + NL;
    
    private static final String UNDIRECTED_WEIGHTED =
            "#Creator: JGraphT Lemon (LGF) Exporter" + NL
            + "#Version: 1" + NL
            + NL
            + "@nodes" + NL
            + "label" + NL
            + "1" + NL
            + "2" + NL
            + "3" + NL            
            + NL
            + "@arcs" + NL
            + "\t\tweight" + NL
            + "1\t2\t2.0" + NL
            + "3\t1\t5.0" + NL            
            + NL;

    private static final String UNDIRECTED_WITH_ESCAPE =
        "#Creator: JGraphT Lemon (LGF) Exporter" + NL
        + "#Version: 1" + NL
        + NL
        + "@nodes" + NL
        + "label" + NL
        + "\"1\"" + NL
        + "\"2\"" + NL
        + "\"3\"" + NL            
        + NL
        + "@arcs" + NL
        + "\t\t-" + NL
        + "\"1\"\t\"2\"" + NL
        + "\"3\"\t\"1\"" + NL            
        + NL;
    
    @Test
    public void testUndirected()
        throws UnsupportedEncodingException
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        LemonExporter<String, DefaultEdge> exporter = new LemonExporter<String, DefaultEdge>();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals(UNDIRECTED, res);
    }

    @Test
    public void testUnweightedUndirected()
        throws UnsupportedEncodingException
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        LemonExporter<String, DefaultEdge> exporter = new LemonExporter<>();
        exporter.setParameter(LemonExporter.Parameter.EXPORT_EDGE_WEIGHTS, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals(UNDIRECTED_DEFAULT_WEIGHTS, res);
    }

    @Test
    public void testWeightedUndirected()
        throws UnsupportedEncodingException
    {
        SimpleGraph<String, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addVertex(V3);
        DefaultWeightedEdge e1 = g.addEdge(V1, V2);
        g.setEdgeWeight(e1, 2.0);
        DefaultWeightedEdge e2 = g.addEdge(V3, V1);
        g.setEdgeWeight(e2, 5.0);

        LemonExporter<String, DefaultWeightedEdge> exporter = new LemonExporter<>();
        exporter.setParameter(LemonExporter.Parameter.EXPORT_EDGE_WEIGHTS, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals(UNDIRECTED_WEIGHTED, res);
    }
    
    @Test
    public void testUndirectedWithEscape()
        throws UnsupportedEncodingException
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        g.addVertex(V1);
        g.addVertex(V2);
        g.addEdge(V1, V2);
        g.addVertex(V3);
        g.addEdge(V3, V1);

        LemonExporter<String, DefaultEdge> exporter = new LemonExporter<String, DefaultEdge>();
        exporter.setParameter(LemonExporter.Parameter.ESCAPE_STRINGS_AS_JAVA, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        String res = new String(os.toByteArray(), "UTF-8");
        assertEquals(UNDIRECTED_WITH_ESCAPE, res);
    }

    @Test
    public void testParameters()
    {
        LemonExporter<String, DefaultWeightedEdge> exporter =
            new LemonExporter<String, DefaultWeightedEdge>();
        assertFalse(exporter.isParameter(LemonExporter.Parameter.EXPORT_EDGE_WEIGHTS));
        exporter.setParameter(LemonExporter.Parameter.EXPORT_EDGE_WEIGHTS, true);
        assertTrue(exporter.isParameter(LemonExporter.Parameter.EXPORT_EDGE_WEIGHTS));
        exporter.setParameter(LemonExporter.Parameter.EXPORT_EDGE_WEIGHTS, false);
        assertFalse(exporter.isParameter(LemonExporter.Parameter.EXPORT_EDGE_WEIGHTS));
        
        assertFalse(exporter.isParameter(LemonExporter.Parameter.ESCAPE_STRINGS_AS_JAVA));
        exporter.setParameter(LemonExporter.Parameter.ESCAPE_STRINGS_AS_JAVA, true);
        assertTrue(exporter.isParameter(LemonExporter.Parameter.ESCAPE_STRINGS_AS_JAVA));
        exporter.setParameter(LemonExporter.Parameter.ESCAPE_STRINGS_AS_JAVA, false);
        assertFalse(exporter.isParameter(LemonExporter.Parameter.ESCAPE_STRINGS_AS_JAVA));
    }

}
