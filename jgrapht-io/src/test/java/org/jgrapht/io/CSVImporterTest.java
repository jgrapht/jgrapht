/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.io.*;

import static org.junit.Assert.*;

/**
 * 
 * @author Dimitrios Michail
 */
public class CSVImporterTest
{
    private static final String NL = System.getProperty("line.separator");

    public <E> CSVImporter<String, E> createImporter(
        Graph<String, E> g, CSVFormat format, Character delimiter)
    {
        return new CSVImporter<>(
            (l, a) -> l, (f, t, l, a) -> g.getEdgeSupplier().get(), format, delimiter);
    }

    public <E> Graph<String, E> readGraph(
        String input, CSVFormat format, Character delimiter, Class<? extends E> edgeClass,
        boolean directed, boolean weighted)
        throws ImportException
    {
        Graph<String, E> g;
        if (directed) {
            if (weighted) {
                g = new DirectedWeightedPseudograph<>(edgeClass);
            } else {
                g = new DirectedPseudograph<>(edgeClass);
            }
        } else {
            if (weighted) {
                g = new WeightedPseudograph<>(edgeClass);
            } else {
                g = new Pseudograph<>(edgeClass);
            }
        }

        CSVImporter<String, E> importer = createImporter(g, format, delimiter);

        if ((format == CSVFormat.EDGE_LIST || format == CSVFormat.ADJACENCY_LIST) && weighted) {
            importer.setParameter(CSVFormat.Parameter.EDGE_WEIGHTS, true);
        }

        importer.importGraph(g, new StringReader(input));

        return g;
    }

    @Test
    public void testEdgeListDirectedUnweighted()
        throws ImportException
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
        throws ImportException
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
        throws ImportException
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
        throws ImportException
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
        throws ImportException
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
        throws ImportException
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
        assertTrue(g.containsVertex("'john doe'"));
        assertTrue(g.containsVertex("fred"));
        assertTrue(g.containsVertex("fred\n\"21\""));
        assertTrue(g.containsVertex("who;;"));
        assertTrue(g.containsEdge("'john doe'", "fred"));
        assertTrue(g.containsEdge("fred", "fred\n\"21\""));
        assertTrue(g.containsEdge("fred\n\"21\"", "who;;"));
        assertTrue(g.containsEdge("who;;", "'john doe'"));
    }

    @Test
    public void testDirectedMatrixNoNodeIdZeroNoEdgeWeighted()
        throws ImportException
    {
        // @formatter:off
        String input =
            "0;1.0;13.0;0;0" + NL
          + "0;0;0;0;0" + NL
          + "1.0;0;0;1.0;0" + NL
          + "0;0;0;0;1.0" + NL
          + "1.0;1.0;53.0;1.0;1.0" + NL;
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        CSVImporter<String, DefaultWeightedEdge> importer =
            createImporter(g, CSVFormat.MATRIX, ';');
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
        throws ImportException
    {
        // @formatter:off
        String input = 
            ",1.0,13.0,," + NL
          + ",,,," + NL
          + "1.0,,,1.0," + NL
          + ",,,,1.0" + NL
          + "1.0,1.0,53.0,1.0,1.0" + NL;
        // @formatter:on

        Graph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        CSVImporter<String, DefaultWeightedEdge> importer =
            createImporter(g, CSVFormat.MATRIX, ',');
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
        throws ImportException
    {
        // @formatter:off
        String input =
            "0;1;1;0;0" + NL
          + "0;0;0;0;0" + NL
          + "1;0;0;1;0" + NL
          + "0;0;0;0;1" + NL
          + "1;1;1;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        CSVImporter<String, DefaultEdge> importer = createImporter(g, CSVFormat.MATRIX, ';');
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
        throws ImportException
    {
        // @formatter:off
        String input =
            ";1;1;;" + NL
          + ";;;;" + NL
          + "1;;;1;" + NL
          + ";;;;1" + NL
          + "1;1;1;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        CSVImporter<String, DefaultEdge> importer = createImporter(g, CSVFormat.MATRIX, ';');
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
        throws ImportException
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

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        CSVImporter<String, DefaultEdge> importer = createImporter(g, CSVFormat.MATRIX, ';');
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("A"));
        assertTrue(g.containsVertex("B"));
        assertTrue(g.containsVertex("C"));
        assertTrue(g.containsVertex("D"));
        assertTrue(g.containsVertex("E"));
        assertTrue(g.containsEdge("A", "B"));
        assertTrue(g.containsEdge("A", "C"));
        assertTrue(g.containsEdge("C", "A"));
        assertTrue(g.containsEdge("C", "D"));
        assertTrue(g.containsEdge("D", "E"));
        assertTrue(g.containsEdge("E", "A"));
        assertTrue(g.containsEdge("E", "B"));
        assertTrue(g.containsEdge("E", "C"));
        assertTrue(g.containsEdge("E", "D"));
        assertTrue(g.containsEdge("E", "E"));

    }

    @Test
    public void testDirectedMatrixNodeIdZeroNoEdgeShuffled()
        throws ImportException
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

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        CSVImporter<String, DefaultEdge> importer = createImporter(g, CSVFormat.MATRIX, ';');
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("A"));
        assertTrue(g.containsVertex("B"));
        assertTrue(g.containsVertex("C"));
        assertTrue(g.containsVertex("D"));
        assertTrue(g.containsVertex("E"));
        assertTrue(g.containsEdge("A", "B"));
        assertTrue(g.containsEdge("A", "C"));
        assertTrue(g.containsEdge("C", "A"));
        assertTrue(g.containsEdge("C", "D"));
        assertTrue(g.containsEdge("D", "E"));
        assertTrue(g.containsEdge("E", "A"));
        assertTrue(g.containsEdge("E", "B"));
        assertTrue(g.containsEdge("E", "C"));
        assertTrue(g.containsEdge("E", "D"));
        assertTrue(g.containsEdge("E", "E"));
    }

    @Test
    public void testDirectedMatrixNodeIdZeroNoEdgeWeightedShuffledZeroWeightsAsDouble()
        throws ImportException
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

        Graph<String, DefaultWeightedEdge> g =
            new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);

        CSVImporter<String, DefaultWeightedEdge> importer =
            createImporter(g, CSVFormat.MATRIX, ';');
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        importer.setParameter(CSVFormat.Parameter.EDGE_WEIGHTS, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("A"));
        assertTrue(g.containsVertex("B"));
        assertTrue(g.containsVertex("C"));
        assertTrue(g.containsVertex("D"));
        assertTrue(g.containsVertex("E"));
        assertTrue(g.containsEdge("A", "B"));
        assertTrue(g.containsEdge("A", "C"));
        assertTrue(g.containsEdge("C", "A"));
        assertTrue(g.containsEdge("C", "D"));
        assertTrue(g.containsEdge("D", "C"));
        assertEquals(0d, g.getEdgeWeight(g.getEdge("D", "C")), 0.0001);
        assertTrue(g.containsEdge("D", "E"));
        assertEquals(1d, g.getEdgeWeight(g.getEdge("D", "E")), 0.0001);
        assertTrue(g.containsEdge("E", "A"));
        assertTrue(g.containsEdge("E", "B"));
        assertTrue(g.containsEdge("E", "D"));
        assertTrue(g.containsEdge("E", "E"));
    }

    @Test
    public void testDoubleOnUnweighted()
        throws ImportException
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

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        CSVImporter<String, DefaultEdge> importer = createImporter(g, CSVFormat.MATRIX, ';');
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
        throws ImportException
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

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        CSVImporter<String, DefaultEdge> importer = createImporter(g, CSVFormat.MATRIX, ';');
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
        throws ImportException
    {
        // @formatter:off
        String input =
            ";1;1;;" + NL
          + ";;;;" + NL
          + "1;;;1;" + NL
          + ";;;1" + NL
          + "1;1;1;1;1" + NL;
        // @formatter:on

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        CSVImporter<String, DefaultEdge> importer = createImporter(g, CSVFormat.MATRIX, ';');
        try {
            importer.importGraph(g, new StringReader(input));
            fail("No!");
        } catch (ImportException e) {
            // nothing
        }
    }

    @Test
    public void testDirectedMatrixNodeIdZeroNoEdgeShuffledAndTabDelimiter()
        throws ImportException
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

        Graph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);

        CSVImporter<String, DefaultEdge> importer = createImporter(g, CSVFormat.MATRIX, '\t');
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_NODEID, true);
        importer.setParameter(CSVFormat.Parameter.MATRIX_FORMAT_ZERO_WHEN_NO_EDGE, true);
        importer.importGraph(g, new StringReader(input));

        assertEquals(5, g.vertexSet().size());
        assertEquals(10, g.edgeSet().size());
        assertTrue(g.containsVertex("A"));
        assertTrue(g.containsVertex("B"));
        assertTrue(g.containsVertex("C\tC"));
        assertTrue(g.containsVertex("D"));
        assertTrue(g.containsVertex("E"));
        assertTrue(g.containsEdge("A", "B"));
        assertTrue(g.containsEdge("A", "C\tC"));
        assertTrue(g.containsEdge("C\tC", "A"));
        assertTrue(g.containsEdge("C\tC", "D"));
        assertTrue(g.containsEdge("D", "E"));
        assertTrue(g.containsEdge("E", "A"));
        assertTrue(g.containsEdge("E", "B"));
        assertTrue(g.containsEdge("E", "C\tC"));
        assertTrue(g.containsEdge("E", "D"));
        assertTrue(g.containsEdge("E", "E"));
    }

}
