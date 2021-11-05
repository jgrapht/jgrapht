/*
 * (C) Copyright 2017-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.nio.graph6;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.nio.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for Graph6Sparse6Importer Sparse6/Graph6 strings are generated with Sage Math engine
 *
 * @author Joris Kinable
 */
public class Graph6Sparse6ImporterTest
{

    public <E> Graph<Integer, E> readGraph(InputStream in, Class<E> edgeClass, boolean weighted)
        throws ImportException
    {
        Graph<Integer, E> g = GraphTypeBuilder
            .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(weighted)
            .vertexSupplier(SupplierUtil.createIntegerSupplier()).edgeClass(edgeClass).buildGraph();

        Graph6Sparse6Importer<Integer, E> importer = new Graph6Sparse6Importer<>();
        try {
            importer.importGraph(g, new InputStreamReader(in, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // cannot happen
        }
        return g;
    }

    @Test
    public void testExampleGraph()
        throws ImportException
    {
        String input = ":Fa@x^\n";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        assertEquals(7, graph.vertexSet().size());
        assertEquals(4, graph.edgeSet().size());

        int[][] edges = { { 0, 1 }, { 0, 2 }, { 1, 2 }, { 5, 6 } };
        for (int[] edge : edges)
            assertTrue(graph.containsEdge(edge[0], edge[1]));
    }

    @Test
    public void pseudoGraph()
        throws ImportException
    {
        // Klein7RegularGraph
        String input = ":B_`V";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        Graph<Integer, DefaultEdge> orig = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(orig, Arrays.asList(0, 1, 2));
        orig.addEdge(0, 1);
        orig.addEdge(0, 1);
        orig.addEdge(1, 2);
        orig.addEdge(2, 2);
        orig.addEdge(2, 0);

        this.compare(orig, graph);

    }

    @Test
    public void testVertexFactory()
        throws ImportException
    {
        // Klein7RegularGraph
        String input = ":B_`V";

        Graph<String,
            DefaultEdge> graph = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(false)
                .vertexSupplier(SupplierUtil.createStringSupplier())
                .edgeSupplier(SupplierUtil.DEFAULT_EDGE_SUPPLIER).buildGraph();

        Graph6Sparse6Importer<String, DefaultEdge> importer = new Graph6Sparse6Importer<>();
        importer.setVertexFactory(id -> String.valueOf("node" + id));
        try {
            importer
                .importGraph(
                    graph, new InputStreamReader(
                        new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // cannot happen
        }

        Graph<String, DefaultEdge> orig = new Pseudograph<>(DefaultEdge.class);
        Graphs.addAllVertices(orig, Arrays.asList("node0", "node1", "node2"));
        orig.addEdge("node0", "node1");
        orig.addEdge("node0", "node1");
        orig.addEdge("node0", "node2");
        orig.addEdge("node1", "node2");
        orig.addEdge("node2", "node2");

        this.compare(orig, graph);
        assertEquals(orig.toString(), graph.toString());
    }

    @Test
    public void testNumberVertices1()
        throws ImportException
    {
        String input =
            "~??~?????_@?CG??B??@OG?C?G???GO??W@a???CO???OACC?OA?P@G??O??????G??C????c?G?CC?_?@???C_??_?C????PO?C_??AA?OOAHCA___?CC?A?CAOGO??????A??G?GR?C?_o`???g???A_C?OG??O?G_IA????_QO@EG???O??C?_?C@?G???@?_??AC?AO?a???O?????A?_Dw?H???__O@AAOAACd?_C??G?G@??GO?_???O@?_O??W??@P???AG??B?????G??GG???A??@?aC_G@A??O??_?A?????O@Z?_@M????GQ@_G@?C?\n";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        assertEquals(63, graph.vertexSet().size());
    }

    @Test
    public void testNumberVertices2()
        throws ImportException
    {
        String input =
            "_???C?@AA?_?A?O?C??S??O?q_?P?CHD??@?C?GC???C??GG?C_??O?COG????I?J??Q??O?_@@??@??????\n";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        assertEquals(32, graph.vertexSet().size());
    }

    @Test
    public void testGraph6a()
        throws ImportException
    {
        // Klein7RegularGraph
        String input = "WzK[WgIOT@Wq_A?NALPAq?{GDASCCXO?l?OJAGOY_D@__wb";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.klein7RegularGraph(), graph);
    }

    @Test
    public void testGraph6b()
        throws ImportException
    {
        // ellinghamHorton78Graph
        String input =
            "~?@MhEGHC?AG?_PO@?Ga?GA???C??G??G??C??P???G@?G_??????P????_??AG??O@???@C??A?G?????????C????@?????G?????_????P?????@?????G????????????P??????C?????AG????A?G?????_???????H???????G???????_??????@???????@????????_??????AG???????@?????_?@C????????????????AG????????C????????P???????A?G????????G_?????C??G_???????????????????_?????????G?????C???@??????????_?????@????G?????A???????????????_??????????@????@?????AG??????????C????G?????G@?AG@????????????????@??o??????CW????????????C?W?????????????I???????????c?G";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.ellinghamHorton78Graph(), graph);
    }

    @Test
    public void testGraph6c()
        throws ImportException
    {
        // goldnerHararyGraph
        String input = "JntIBcPEA~_";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.goldnerHararyGraph(), graph);
    }

    @Test
    public void testGraph6d()
        throws ImportException
    {
        // buckyBallGraph
        String input =
            "{R??OKGPG??@AA??_???@@?GO?G?????CAGA?OGO??????@???O??C@_??O??G?@?????????W???D????OS??????????????O@????@BG???????????_???_??????@B??@???_??O???g?????????????C????C???????C?W?A????C??_????D_???????????????_????C????????_?@??????O?g??????@@O?A?????????????C?C?_??????A????????OQ????????@O????????B";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.buckyBallGraph(), graph);
    }

    @Test
    public void testGraph6f()
        throws ImportException
    {
        // heawoodGraph
        String input = "MhEGHC@AI?_PC@_G_";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.heawoodGraph(), graph);
    }

    @Test
    public void testSparse6a()
        throws ImportException
    {
        // Klein7RegularGraph
        String input =
            ":W__@`AaBbC_CDbDcE`F_AG_@DEH_IgHIJbFGIKaFHILeFGHMdFKN_EKOPaCNPQ`HOQRcGLRS`BKMSTdJKLPTU\n";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.klein7RegularGraph(), graph);
    }

    @Test
    public void testSparse6b()
        throws ImportException
    {
        // ellinghamHorton78Graph
        String input =
            ":~?@M_GEA_w?C`WGEaOOGaWWI_OmGBGKL`w}OcXINCxQGCPUWCp]WdPeOEh[Zc`q^Fh}_gXwagyAfGaYfhAa^IYEgIyqlji}ojREqfa{rlbCtljKvjbatMYWv_Jq|hBy{hSAdn{M\\OCRAeRtEa_wVlSHBhagjkBgzpCY}OSr";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.ellinghamHorton78Graph(), graph);
    }

    @Test
    public void testSparse6c()
        throws ImportException
    {
        // goldnerHararyGraph
        String input = ":J`E?POAMHGpCKsrrHCXAeM`N";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.goldnerHararyGraph(), graph);
    }

    @Test
    public void testSparse6d()
        throws ImportException
    {
        // buckyBallGraph
        String input =
            ":{`?GGIKCa`gcCIGdag_iXNFPPsK`RHP`PIMMHtqtM]VKShXiyZMUBTWw]pDcDpAa`XI}@IeghHyXPjTV[IlXLTQtay@ooWUUT_qtkU[vSucLmJ]Aw_MVV";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.buckyBallGraph(), graph);
    }

    @Test
    public void testSparse6f()
        throws ImportException
    {
        // heawoodGraph
        String input = ":M`ESwCjGtyGaeqhj_`f";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.heawoodGraph(), graph);
    }

    @Test
    public void testSparse6g()
        throws ImportException
    {
        // ellinghamHorton78Graph generated by mathematica 10.0
        String input =
            ">>sparse6<<:~?@M__EC?GEA_wQD`g]DAGOH`oiEAwqLbg}?CGCP_`IBCxCSc@URDhGV_ocXaG?IEgkZfXuWgiA^GQMaHIEhHA]eII[igAabIYaoJAuqJi}pizIrlJUrLjGvlRasMZiznJumNi{~kSAoOZ|AncN@PK@DkRXEls]wQCmnMSf~~~~~";

        Graph<Integer,
            DefaultEdge> graph = readGraph(
                new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), DefaultEdge.class,
                false);

        this.compare(NamedGraphGenerator.ellinghamHorton78Graph(), graph);
    }

    @Test
    public void testFromFile()
        throws ImportException,
        IOException
    {
        InputStream fstream =
            getClass().getClassLoader().getResourceAsStream("ellinghamHorton78Graph.s6");
        Graph<Integer, DefaultEdge> g = new Pseudograph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        Graph6Sparse6Importer<Integer, DefaultEdge> importer = new Graph6Sparse6Importer<>();
        importer.importGraph(g, fstream);

        this.compare(NamedGraphGenerator.ellinghamHorton78Graph(), g);
    }

    private <V, E> void compare(Graph<V, E> orig, Graph<V, E> g)
    {
        assertEquals(orig.vertexSet().size(), g.vertexSet().size());
        assertEquals(orig.edgeSet().size(), g.edgeSet().size());

        // The original and output graph cannot be compared 1:1 since sparse6/graph6 encodings do
        // not preserve vertex labels
        // Testing for graph isomorphism is hard, so we compare characteristics.
        int[] degeeVectorOrig = new int[orig.edgeSet().size()];
        for (V v : orig.vertexSet())
            degeeVectorOrig[orig.degreeOf(v)]++;

        int[] degeeVectorG = new int[g.edgeSet().size()];
        for (V v : g.vertexSet())
            degeeVectorG[g.degreeOf(v)]++;

        assertTrue(Arrays.equals(degeeVectorOrig, degeeVectorG));

        assertEquals(GraphMetrics.getRadius(orig), GraphMetrics.getRadius(g), 0.00000001);
        assertEquals(GraphMetrics.getDiameter(orig), GraphMetrics.getDiameter(g), 0.00000001);
        assertEquals(GraphMetrics.getGirth(orig), GraphMetrics.getGirth(g), 0.00000001);
    }

}
