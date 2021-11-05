/*
 * (C) Copyright 2016-2021, by Dimitrios Michail and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.jgrapht.nio.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests
 * 
 * @author Dimitrios Michail
 */
public class CSVImporterTest
{
    private static final String NL = System.getProperty("line.separator");

    public <E> Graph<String, E> readGraph(
        String input, CSVFormat format, Character delimiter, Class<E> edgeClass, boolean directed,
        boolean weighted)
    {
        Graph<String, E> g;

        if (directed) {
            g = GraphTypeBuilder
                .directed().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(weighted)
                .edgeClass(edgeClass).vertexSupplier(SupplierUtil.createStringSupplier(1))
                .buildGraph();
        } else {
            g = GraphTypeBuilder
                .undirected().allowingMultipleEdges(true).allowingSelfLoops(true).weighted(weighted)
                .edgeClass(edgeClass).vertexSupplier(SupplierUtil.createStringSupplier(1))
                .buildGraph();
        }

        CSVImporter<String, E> importer = new CSVImporter<>(format, delimiter);

        if ((format == CSVFormat.EDGE_LIST || format == CSVFormat.ADJACENCY_LIST) && weighted) {
            importer.setParameter(CSVFormat.Parameter.EDGE_WEIGHTS, true);
        }

        importer.importGraph(g, new StringReader(input));

        return g;
    }

    @Test
    public void testEdgeListDirectedUnweighted()
    {
        // @formatter:off
        String input = "1,2\n"
                     + "2,3\n"
                     + "3,4\n"
                     + "4,1\n";
        // @formatter:on

        Graph<String, DefaultEdge> g =
            readGraph(input, CSVFormat.EDGE_LIST, ',', DefaultEdge.class, true, false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(4, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "4"));
        assertTrue(g.containsEdge("4", "1"));
    }

    @Test
    public void testEdgeListDirectedWeighted()
    {
        // @formatter:off
        String input = "1,2,1.0\n"
                     + "2,3,2.0\n"
                     + "3,4,3.0\n"
                     + "4,1,4.0\n";
        // @formatter:on

        Graph<String, DefaultEdge> g =
            readGraph(input, CSVFormat.EDGE_LIST, ',', DefaultEdge.class, true, true);

        assertEquals(4, g.vertexSet().size());
        assertEquals(4, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
        assertTrue(g.containsEdge("2", "3"));
        assertEquals(2.0, g.getEdgeWeight(g.getEdge("2", "3")), 1e-9);
        assertTrue(g.containsEdge("3", "4"));
        assertEquals(3.0, g.getEdgeWeight(g.getEdge("3", "4")), 1e-9);
        assertTrue(g.containsEdge("4", "1"));
        assertEquals(4.0, g.getEdgeWeight(g.getEdge("4", "1")), 1e-9);
    }

    @Test
    public void testEdgeListDirectedUnweightedWithSemicolon()
    {
        // @formatter:off
        String input = "1;2\n"
                     + "2;3\n"
                     + "3;4\n"
                     + "4;1\n";
        // @formatter:on

        Graph<String, DefaultEdge> g =
            readGraph(input, CSVFormat.EDGE_LIST, ';', DefaultEdge.class, true, false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(4, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "4"));
        assertTrue(g.containsEdge("4", "1"));
    }

    @Test
    public void testAdjacencyListDirectedUnweightedWithSemicolon()
    {
        // @formatter:off
        String input = "1;2;3;4\n"
                     + "2;3\n"
                     + "3;4;5;6\n"
                     + "4;1;5;6\n";
        // @formatter:on

        Graph<String, DefaultEdge> g =
            readGraph(input, CSVFormat.ADJACENCY_LIST, ';', DefaultEdge.class, true, false);

        assertEquals(6, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsVertex("6"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("1", "4"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "4"));
        assertTrue(g.containsEdge("3", "5"));
        assertTrue(g.containsEdge("3", "6"));
        assertTrue(g.containsEdge("4", "1"));
        assertTrue(g.containsEdge("4", "5"));
        assertTrue(g.containsEdge("4", "6"));
    }

    @Test
    public void testAdjacencyListDirectedWeightedWithSemicolon()
    {
        // @formatter:off
        String input = "1;2;2.1;3;3.1;4;4.1\n"
                     + "2;3;3.1\n"
                     + "3;4;4.1;5;5.1;6;6.1\n"
                     + "4;1;1.1;5;5.1;6;6.1\n";
        // @formatter:on

        Graph<String, DefaultEdge> g =
            readGraph(input, CSVFormat.ADJACENCY_LIST, ';', DefaultEdge.class, true, true);

        assertEquals(6, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsVertex("6"));
        assertTrue(g.containsEdge("1", "2"));
        assertEquals(2.1, g.getEdgeWeight(g.getEdge("1", "2")), 1e-9);
        assertTrue(g.containsEdge("1", "3"));
        assertEquals(3.1, g.getEdgeWeight(g.getEdge("1", "3")), 1e-9);
        assertTrue(g.containsEdge("1", "4"));
        assertEquals(4.1, g.getEdgeWeight(g.getEdge("1", "4")), 1e-9);
        assertTrue(g.containsEdge("2", "3"));
        assertEquals(3.1, g.getEdgeWeight(g.getEdge("2", "3")), 1e-9);
        assertTrue(g.containsEdge("3", "4"));
        assertEquals(4.1, g.getEdgeWeight(g.getEdge("3", "4")), 1e-9);
        assertTrue(g.containsEdge("3", "5"));
        assertEquals(5.1, g.getEdgeWeight(g.getEdge("3", "5")), 1e-9);
        assertTrue(g.containsEdge("3", "6"));
        assertEquals(6.1, g.getEdgeWeight(g.getEdge("3", "6")), 1e-9);
        assertTrue(g.containsEdge("4", "1"));
        assertEquals(1.1, g.getEdgeWeight(g.getEdge("4", "1")), 1e-9);
        assertTrue(g.containsEdge("4", "5"));
        assertEquals(5.1, g.getEdgeWeight(g.getEdge("4", "5")), 1e-9);
        assertTrue(g.containsEdge("4", "6"));
        assertEquals(6.1, g.getEdgeWeight(g.getEdge("4", "6")), 1e-9);
    }

    @Test
    public void testEdgeListWithStringsDirectedUnweightedWithSemicolon()
    {
        // @formatter:off
        String input = "'john doe';fred\n"
                     + "fred;\"fred\n\"\"21\"\"\"\n"
                     + "\"fred\n\"\"21\"\"\";\"who;;\"\n"
                     + "\"who;;\";'john doe'\n";
        // @formatter:on

        Graph<String, DefaultEdge> g =
            readGraph(input, CSVFormat.EDGE_LIST, ';', DefaultEdge.class, true, false);

        assertEquals(4, g.vertexSet().size());
        assertEquals(4, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("2", "3"));
        assertTrue(g.containsEdge("3", "4"));
        assertTrue(g.containsEdge("4", "1"));
    }

    @Test
    public void testDirectedMatrixNoNodeIdZeroNoEdgeWeighted()
    {
        // @formatter:off
        String input =
            "0;1.0;13.0;0;0" + NL
          + "0;0;0;0;0" + NL
          + "1.0;0;0;1.0;0" + NL
          + "0;0;0;0;1.0" + NL
          + "1.0;1.0;53.0;1.0;1.0" + NL;
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g = new DirectedWeightedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);

        CSVImporter<String, DefaultWeightedEdge> importer =
            new CSVImporter<>(CSVFormat.MATRIX, ';');
        importer.setParameter(CSVFormat.Parameter.EDGE_WEIGHTS, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsEdge("1", "2"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("1", "2")), 0.0001);
        assertTrue(g.containsEdge("1", "3"));
        assertEquals(13.0, g.getEdgeWeight(g.getEdge("1", "3")), 0.0001);
        assertTrue(g.containsEdge("3", "1"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("3", "1")), 0.0001);
        assertTrue(g.containsEdge("3", "4"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("3", "4")), 0.0001);
        assertTrue(g.containsEdge("4", "5"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("4", "5")), 0.0001);
        assertTrue(g.containsEdge("5", "1"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("5", "1")), 0.0001);
        assertTrue(g.containsEdge("5", "2"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("5", "2")), 0.0001);
        assertTrue(g.containsEdge("5", "3"));
        assertEquals(53.0, g.getEdgeWeight(g.getEdge("5", "3")), 0.0001);
        assertTrue(g.containsEdge("5", "4"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("5", "4")), 0.0001);
        assertTrue(g.containsEdge("5", "5"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("5", "5")), 0.0001);
    }

    @Test
    public void testDirectedMatrixNoNodeIdWeighted()
    {
        // @formatter:off
        String input = 
            ",1.0,13.0,," + NL
          + ",,,," + NL
          + "1.0,,,1.0," + NL
          + ",,,,1.0" + NL
          + "1.0,1.0,53.0,1.0,1.0" + NL;
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g = new DirectedWeightedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);

        CSVImporter<String, DefaultWeightedEdge> importer =
            new CSVImporter<>(CSVFormat.MATRIX, ',');
        importer.setParameter(CSVFormat.Parameter.EDGE_WEIGHTS, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsEdge("1", "2"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("1", "2")), 0.0001);
        assertTrue(g.containsEdge("1", "3"));
        assertEquals(13.0, g.getEdgeWeight(g.getEdge("1", "3")), 0.0001);
        assertTrue(g.containsEdge("3", "1"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("3", "1")), 0.0001);
        assertTrue(g.containsEdge("3", "4"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("3", "4")), 0.0001);
        assertTrue(g.containsEdge("4", "5"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("4", "5")), 0.0001);
        assertTrue(g.containsEdge("5", "1"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("5", "1")), 0.0001);
        assertTrue(g.containsEdge("5", "2"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("5", "2")), 0.0001);
        assertTrue(g.containsEdge("5", "3"));
        assertEquals(53.0, g.getEdgeWeight(g.getEdge("5", "3")), 0.0001);
        assertTrue(g.containsEdge("5", "4"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("5", "4")), 0.0001);
        assertTrue(g.containsEdge("5", "5"));
        assertEquals(1.0, g.getEdgeWeight(g.getEdge("5", "5")), 0.0001);
    }

    @Test
    public void testDirectedMatrixNoNodeIdZeroNoEdge()
    {
        // @formatter:off
        String input =
            "0;1;1;0;0" + NL
          + "0;0;0;0;0" + NL
          + "1;0;0;1;0" + NL
          + "0;0;0;0;1" + NL
          + "1;1;1;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CSVImporter<String, DefaultEdge> importer = new CSVImporter<>(CSVFormat.MATRIX, ';');
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("3", "1"));
        assertTrue(g.containsEdge("3", "4"));
        assertTrue(g.containsEdge("4", "5"));
        assertTrue(g.containsEdge("5", "1"));
        assertTrue(g.containsEdge("5", "2"));
        assertTrue(g.containsEdge("5", "3"));
        assertTrue(g.containsEdge("5", "4"));
        assertTrue(g.containsEdge("5", "5"));
    }

    @Test
    public void testDirectedMatrixNoNodeId()
    {
        // @formatter:off
        String input =
            ";1;1;;" + NL
          + ";;;;" + NL
          + "1;;;1;" + NL
          + ";;;;1" + NL
          + "1;1;1;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CSVImporter<String, DefaultEdge> importer = new CSVImporter<>(CSVFormat.MATRIX, ';');
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("3", "1"));
        assertTrue(g.containsEdge("3", "4"));
        assertTrue(g.containsEdge("4", "5"));
        assertTrue(g.containsEdge("5", "1"));
        assertTrue(g.containsEdge("5", "2"));
        assertTrue(g.containsEdge("5", "3"));
        assertTrue(g.containsEdge("5", "4"));
        assertTrue(g.containsEdge("5", "5"));

    }

    @Test
    public void testDirectedMatrixNodeIdZeroNoEdge()
    {
        // @formatter:off
        String input =
              ";A;B;C;D;E" + NL
            + "A;0;1;1;0;0" + NL
            + "B;0;0;0;0;0" + NL
            + "C;1;0;0;1;0" + NL
            + "D;0;0;0;0;1" + NL
            + "E;1;1;1;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CSVImporter<String, DefaultEdge> importer = new CSVImporter<>(CSVFormat.MATRIX, ';');
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("3", "1"));
        assertTrue(g.containsEdge("3", "4"));
        assertTrue(g.containsEdge("4", "5"));
        assertTrue(g.containsEdge("5", "1"));
        assertTrue(g.containsEdge("5", "2"));
        assertTrue(g.containsEdge("5", "3"));
        assertTrue(g.containsEdge("5", "4"));
        assertTrue(g.containsEdge("5", "5"));

    }

    @Test
    public void testDirectedMatrixNodeIdZeroNoEdgeShuffled()
    {
        // @formatter:off
        String input =
              ";A;B;C;D;E" + NL
            + "C;1;0;0;1;0" + NL
            + "D;0;0;0;0;1" + NL
            + "B;0;0;0;0;0" + NL
            + "A;0;1;1;0;0" + NL
            + "E;1;1;1;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CSVImporter<String, DefaultEdge> importer = new CSVImporter<>(CSVFormat.MATRIX, ';');

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        importer.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.get(p.getFirst());
            if (map == null) {
                map = new HashMap<>();
                attrs.put(p.getFirst(), map);
            }
            map.put(p.getSecond(), a);
        });

        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("3", "1"));
        assertTrue(g.containsEdge("3", "4"));
        assertTrue(g.containsEdge("4", "5"));
        assertTrue(g.containsEdge("5", "1"));
        assertTrue(g.containsEdge("5", "2"));
        assertTrue(g.containsEdge("5", "3"));
        assertTrue(g.containsEdge("5", "4"));
        assertTrue(g.containsEdge("5", "5"));

        assertEquals(attrs.get("1").get("ID").getValue(), "A");
        assertEquals(attrs.get("2").get("ID").getValue(), "B");
        assertEquals(attrs.get("3").get("ID").getValue(), "C");
        assertEquals(attrs.get("4").get("ID").getValue(), "D");
        assertEquals(attrs.get("5").get("ID").getValue(), "E");
    }

    @Test
    public void testDirectedMatrixNodeIdZeroNoEdgeWeightedShuffledZeroWeightsAsDouble()
    {
        // @formatter:off
        String input =
              ";A;B;C;D;E" + NL
            + "C;1;0;0;1;0" + NL
            + "D;0;0;0.0;0;1" + NL
            + "B;0;0;0;0;0" + NL
            + "A;0;1;1;0;0" + NL
            + "E;1;1;0;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g = new DirectedWeightedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_WEIGHTED_EDGE_SUPPLIER);

        CSVImporter<String, DefaultWeightedEdge> importer =
            new CSVImporter<>(CSVFormat.MATRIX, ';');
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        importer.setParameter(CSVFormat.Parameter.EDGE_WEIGHTS, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("3", "1"));
        assertTrue(g.containsEdge("3", "4"));
        assertTrue(g.containsEdge("4", "3"));
        assertEquals(0d, g.getEdgeWeight(g.getEdge("4", "3")), 0.0001);
        assertTrue(g.containsEdge("4", "5"));
        assertEquals(1d, g.getEdgeWeight(g.getEdge("4", "5")), 0.0001);
        assertTrue(g.containsEdge("5", "1"));
        assertTrue(g.containsEdge("5", "2"));
        assertTrue(g.containsEdge("5", "4"));
        assertTrue(g.containsEdge("5", "5"));
    }

    @Test
    public void testDoubleOnUnweighted()
    {
        // @formatter:off
        String input =
              ";A;B;C;D;E" + NL
            + "C;1;0;0;1;0" + NL
            + "D;0;0;0.0;0;1" + NL
            + "B;0;0;0;0;0" + NL
            + "A;0;1;1;0;0" + NL
            + "E;1;1;0;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CSVImporter<String, DefaultEdge> importer = new CSVImporter<>(CSVFormat.MATRIX, ';');
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        try {
            importer.importGraph(g, new StringReader(input));
            fail("No!");
        } catch (ImportException e) {
            // nothing
        }
    }

    @Test
    public void testWrongHeaderNodeIds()
    {
        // @formatter:off
        String input =
              ";A;B;  ;D;E" + NL
            + "C;1;0;0;1;0" + NL
            + "D;0;0;0.0;0;1" + NL
            + "B;0;0;0;0;0" + NL
            + "A;0;1;1;0;0" + NL
            + "E;1;1;0;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CSVImporter<String, DefaultEdge> importer = new CSVImporter<>(CSVFormat.MATRIX, ';');
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        try {
            importer.importGraph(g, new StringReader(input));
            fail("No!");
        } catch (ImportException e) {
            // nothing
        }
    }

    @Test
    public void testDirectedMatrixNoNodeIdMissingEntries()
    {
        // @formatter:off
        String input =
            ";1;1;;" + NL
          + ";;;;" + NL
          + "1;;;1;" + NL
          + ";;;1" + NL
          + "1;1;1;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CSVImporter<String, DefaultEdge> importer = new CSVImporter<>(CSVFormat.MATRIX, ';');
        try {
            importer.importGraph(g, new StringReader(input));
            fail("No!");
        } catch (ImportException e) {
            // nothing
        }
    }

    @Test
    public void testDirectedMatrixNodeIdZeroNoEdgeShuffledAndTabDelimiter()
    {
        // @formatter:off
        String input =
              "\tA\tB\t\"C\tC\"\tD\tE" + NL
            + "\"C\tC\"\t1\t0\t0\t1\t0" + NL
            + "D\t0\t0\t0\t0\t1" + NL
            + "B\t0\t0\t0\t0\t0" + NL
            + "A\t0\t1\t1\t0\t0" + NL
            + "E\t1\t1\t1\t1\t1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(
            SupplierUtil.createStringSupplier(1), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);

        CSVImporter<String, DefaultEdge> importer = new CSVImporter<>(CSVFormat.MATRIX, '\t');
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("1"));
        assertTrue(g.containsVertex("2"));
        assertTrue(g.containsVertex("3"));
        assertTrue(g.containsVertex("4"));
        assertTrue(g.containsVertex("5"));
        assertTrue(g.containsEdge("1", "2"));
        assertTrue(g.containsEdge("1", "3"));
        assertTrue(g.containsEdge("3", "1"));
        assertTrue(g.containsEdge("3", "4"));
        assertTrue(g.containsEdge("4", "5"));
        assertTrue(g.containsEdge("5", "1"));
        assertTrue(g.containsEdge("5", "2"));
        assertTrue(g.containsEdge("5", "3"));
        assertTrue(g.containsEdge("5", "4"));
        assertTrue(g.containsEdge("5", "5"));
    }

}
