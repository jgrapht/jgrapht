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
/* ------------------------------
 * GmlImporterTest.java
 * ------------------------------
 * (C) Copyright 2016-2016, by Dimitrios Michail and Contributors.
 *
 * Original Author: Dimitrios Michail
 *
 * Changes
 * -------
 * 16-July-2016 : Initial revision (DM);
 *
 */
package org.jgrapht.ext;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.graph.Pseudograph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.WeightedPseudograph;

import junit.framework.TestCase;

/**
 * .
 * @author Dimitrios Michail
 */
public class GmlImporterTest
    extends TestCase
{

    public <E> Graph<String, E> readGraph(
        String input,
        Class<? extends E> edgeClass,
        boolean directed,
        boolean weighted)
        throws ImportException
    {
        Graph<String, E> g;
        if (directed) {
            if (weighted) {
                g = new DirectedWeightedPseudograph<String, E>(edgeClass);
            } else {
                g = new DirectedPseudograph<String, E>(edgeClass);
            }
        } else {
            if (weighted) {
                g = new WeightedPseudograph<String, E>(edgeClass);
            } else {
                g = new Pseudograph<String, E>(edgeClass);
            }
        }

        VertexProvider<String> vp = new VertexProvider<String>()
        {
            @Override
            public String buildVertex(
                String label,
                Map<String, String> attributes)
            {
                return label;
            }
        };

        EdgeProvider<String, E> ep = new EdgeProvider<String, E>()
        {

            @Override
            public E buildEdge(
                String from,
                String to,
                String label,
                Map<String, String> attributes)
            {
                return g.getEdgeFactory().createEdge(from, to);
            }

        };

        GmlImporter<String, E> importer = new GmlImporter<String, E>(vp, ep);
        importer.importGraph(g, new StringReader(input));

        return g;
    }

    public void testUndirectedUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "  ]\n"                     
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 3\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    public void testIgnoreWeightsUndirectedUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "    weight 2.0\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 2.0\n"
                     + "    target 3\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3.3\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(2, g.vertexSet().size());
        assertEquals(1, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsEdge("1", "2"));
    }

    public void testNoGraph()
        throws ImportException
    {
        // @formatter:off
        String input = "GRAPH [\n"
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g1 = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(0, g1.vertexSet().size());
        assertEquals(0, g1.edgeSet().size());

        Graph<String, DefaultEdge> g2 = readGraph(
            input.toLowerCase(),
            DefaultEdge.class,
            false,
            false);

        assertEquals(0, g2.vertexSet().size());
        assertEquals(0, g2.edgeSet().size());
    }

    public void testIgnore()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  ignore [\n"
                     + "     node [\n"
                     + "       id 5\n"
                     + "     ]"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "    label \"3\""
                     + "  ]\n"
                     + "  node [\n"
                     + "  ]\n"                     
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "  ignore [\n"
                     + "     edge [\n"
                     + "       source 5\n"
                     + "       target 1\n"
                     + "       label \"edge51\""
                     + "     ]"
                     + "  ]\n"                     
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 3\n"
                     + "    label \"23\""                     
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "]"
                     + "node [\n"
                     + "  id 6\n"
                     + "]\n"
                     ;
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    public void testUndirectedUnweightedWrongOrder()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 3\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "  ]\n"                     
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            false,
            false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(3, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "1"));
    }

    public void testDirectedPseudographUnweighted()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "  ]\n"                     
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "  ]\n"                     
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 3\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 1\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 2\n"
                     + "    target 2\n"
                     + "  ]\n"                     
                     + "]";
        // @formatter:on

        Graph<String, DefaultEdge> g = readGraph(
            input,
            DefaultEdge.class,
            true,
            false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(7, g.edgeSet().size());
    }

    public void testDirectedWeighted()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [\n"
                     + "  comment \"Sample Graph\"\n"
                     + "  directed 1\n"
                     + "  node [\n"
                     + "    id 1\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 2\n"
                     + "  ]\n"
                     + "  node [\n"
                     + "    id 3\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 1\n"
                     + "    target 2\n"
                     + "    weight 2.0\n"
                     + "  ]\n"
                     + "  edge [\n"
                     + "    source 3\n"
                     + "    target 1\n"
                     + "    weight 3.0\n"
                     + "  ]\n"
                     + "]";
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g = readGraph(
            input,
            DefaultWeightedEdge.class,
            true,
            true);

        assertEquals(3, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("3", "1"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("1", "2")));
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("3", "1")));
    }

    public void testDirectedWeightedWithComments()
        throws ImportException
    {
        // @formatter:off
            String input = "# A comment line\n" 
            		     + "graph [\n"
                         + "  comment \"Sample Graph\"\n"
                         + "  directed 1\n"
                         + "  node [\n"
                         + "    id 1\n"
                         + "  ]\n"
                         + "  node [\n"
                         + "    id 2\n"
                         + "  ]\n"
                         + "# Another comment line\n"
                         + "  node [\n"
                         + "    id 3\n"
                         + "  ]\n"
                         + "  edge [\n"
                         + "    source 1\n"
                         + "    target 2\n"
                         + "    weight 2.0\n"
                         + "  ]\n"
                         + "  edge [\n"
                         + "    source 3\n"
                         + "    target 1\n"
                         + "    weight 3.0\n"
                         + "  ]\n"
                         + "]";
            // @formatter:on

        Graph<String, DefaultWeightedEdge> g = readGraph(
            input,
            DefaultWeightedEdge.class,
            true,
            true);

        assertEquals(3, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("3", "1"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("1", "2")));
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("3", "1")));
    }

    public void testDirectedWeightedSingleLine()
        throws ImportException
    {
        // @formatter:off
        String input = "graph [ node [ id 1 ] node [ id 2 ] node [ id 3 ] " + 
                       "edge [ source 1 target 2 weight 2.0 ] " + 
                       "edge [ source 3 target 1 weight 3.0 ] ]"; 
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g = readGraph(
            input,
            DefaultWeightedEdge.class,
            true,
            true);

        assertEquals(3, g.vertexSet().size());
        assertEquals(2, g.edgeSet().size());
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("3", "1"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("1", "2")));
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("3", "1")));
    }

    public void testParserError()
    {
        // @formatter:off
        String input = "graph [ [ node ] ]";
        // @formatter:on

        try {
            readGraph(input, DefaultEdge.class, false, false);
            fail("Managed to parse wrong input");
        } catch (ImportException e) {
        }
    }

    public void testMissingVertices()
    {
        // @formatter:off
        String input = "graph [ edge [ source 1 target 2 ] ]";
        // @formatter:on

        try {
            readGraph(input, DefaultEdge.class, false, false);
            fail("Node is missing?");
        } catch (ImportException e) {
        }
    }

    public void testExportImport()
        throws ImportException, ExportException, UnsupportedEncodingException
    {
        DirectedWeightedPseudograph<String, DefaultWeightedEdge> g1 = new DirectedWeightedPseudograph<String, DefaultWeightedEdge>(
            DefaultWeightedEdge.class);
        g1.addVertex("1");
        g1.addVertex("2");
        g1.addVertex("3");
        g1.setEdgeWeight(g1.addEdge("1", "2"), 2.0);
        g1.setEdgeWeight(g1.addEdge("2", "3"), 3.0);
        g1.setEdgeWeight(g1.addEdge("3", "3"), 5.0);

        GmlExporter<String, DefaultWeightedEdge> exporter = new GmlExporter<String, DefaultWeightedEdge>();
        exporter.setParameter(GmlExporter.Parameter.EXPORT_EDGE_WEIGHTS, true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g1, os);
        String output = new String(os.toByteArray(), "UTF-8");

        Graph<String, DefaultWeightedEdge> g2 = readGraph(
            output,
            DefaultWeightedEdge.class,
            true,
            true);

        assertEquals(3, g2.vertexSet().size());
        assertEquals(3, g2.edgeSet().size());
        assertTrue(g2.containsEdge("1", "2"));
        assertTrue(g2.containsEdge("2", "3"));
        assertTrue(g2.containsEdge("3", "3"));
        assertEquals(2.0, g2.getEdgeWeight(g2.getEdge("1", "2")));
        assertEquals(3.0, g2.getEdgeWeight(g2.getEdge("2", "3")));
        assertEquals(5.0, g2.getEdgeWeight(g2.getEdge("3", "3")));
    }

    public void testNotSupportedGraph()
    {
        // @formatter:off
        String input = "graph [ node [ id 1 ] " + 
                       "edge [ source 1 target 1 ] ]"; 
        // @formatter:on

        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        VertexProvider<String> vp = new VertexProvider<String>()
        {
            @Override
            public String buildVertex(
                String label,
                Map<String, String> attributes)
            {
                return label;
            }
        };

        EdgeProvider<String, DefaultEdge> ep = new EdgeProvider<String, DefaultEdge>()
        {

            @Override
            public DefaultEdge buildEdge(
                String from,
                String to,
                String label,
                Map<String, String> attributes)
            {
                return g.getEdgeFactory().createEdge(from, to);
            }

        };

        try {
            GmlImporter<String, DefaultEdge> importer = new GmlImporter<String, DefaultEdge>(
                vp,
                ep);
            importer.importGraph(g, new StringReader(input));
            fail("No!");
        } catch (ImportException e) {
        }

    }

}
