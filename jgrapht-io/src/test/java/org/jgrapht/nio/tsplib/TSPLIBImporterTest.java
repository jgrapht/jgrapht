/*
 * (C) Copyright 2020-2020, by Hannes Wellmann and Contributors.
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

package org.jgrapht.nio.tsplib;

import static org.junit.Assert.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.mutable.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.tsplib.TSPLIBImporter.*;
import org.junit.*;
import org.junit.rules.*;

public class TSPLIBImporterTest
{

    private static String get3DPointsFileContent(String edgeWeightType)
    {
        StringJoiner c = new StringJoiner(System.lineSeparator());
        c.add("NAME : theNameOfThisFile");
        c.add("COMMENT : The first line of the comment");
        c.add("COMMENT : A second line");
        c.add("TYPE : TSP");
        c.add("DIMENSION : 4");
        c.add("EDGE_WEIGHT_TYPE : " + edgeWeightType);
        c.add("NODE_COORD_TYPE : THREED_COORDS");
        c.add("NODE_COORD_SECTION");
        c.add("1 10.0 15.0 3.7");
        c.add("2 14.0 15.0 3.7");
        c.add("3 14.0 20.0 3.7");
        c.add("4 14.0 20.0 3.7");
        return c.toString();
    }

    private static String get2DPointsFileContent(String edgeWeightType)
    {
        StringJoiner c = new StringJoiner(System.lineSeparator());
        c.add("NAME : theNameOfThisFile");
        c.add("COMMENT : The first line of the comment");
        c.add("COMMENT : A second line");
        c.add("TYPE : TSP");
        c.add("DIMENSION : 4");
        c.add("EDGE_WEIGHT_TYPE : " + edgeWeightType);
        c.add("NODE_COORD_TYPE : TWOD_COORDS");
        c.add("NODE_COORD_SECTION");
        c.add("1 10.2 15.0");
        c.add("2 14.2 15.0");
        c.add("3 14.8 20.0");
        c.add("4 10.8 20.0");
        c.add("EOF");
        return c.toString();
    }

    private static class ArrayVector
    {
        private final int index;
        private final double[] elements;

        public ArrayVector(int index, double... elements)
        {
            this.index = index;
            this.elements = elements;
        }

        public ArrayVector(double... elements)
        {
            this(-1, elements);
        }

        public double getElementValue(int i)
        {
            return elements[i];
        }

        @Override
        public int hashCode()
        {
            return 31 + Arrays.hashCode(elements);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ArrayVector)) {
                return false;
            }
            return Arrays.equals(elements, ((ArrayVector) obj).elements);
        }

        private static DecimalFormat indexFormat = new DecimalFormat("0000");
        private static DecimalFormat coordinateFormat =
            new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        @Override
        public String toString()
        {
            String indexStr = index >= 0 ? indexFormat.format(index) + " " : "";
            return indexStr + Arrays
                .stream(elements).mapToObj(coordinateFormat::format)
                .collect(Collectors.joining(" "));
        }
    }

    private static List<ArrayVector> getExpected2DPoints()
    {
        return Arrays
            .asList(vector(10.2, 15.0), vector(14.2, 15.0), vector(14.8, 20.0), vector(10.8, 20.0));
    }

    private static List<ArrayVector> getExpected3DPoints()
    {
        return Arrays
            .asList(
                vector(10.0, 15.0, 3.7), vector(14.0, 15.0, 3.7), vector(14.0, 20.0, 3.7),
                vector(14.0, 20.0, 3.7));
    }

    private static ArrayVector vector(double... elements)
    {
        return new ArrayVector(elements);
    }

    /** Assert content equal graphs */
    private static <V, E> void assertEqualGraphs(Graph<V, E> expectedGraph, Graph<V, E> actualGraph)
    {
        assertEquals(actualGraph.vertexSet(), expectedGraph.vertexSet());

        Set<E> actualEdgeSet = actualGraph.edgeSet();
        Set<E> expectedEdgeSet = expectedGraph.edgeSet();
        assertEquals("Unequal edgeSet size", expectedEdgeSet.size(), actualEdgeSet.size());

        for (E expectedEdge : expectedEdgeSet) {

            E actualEdge = actualGraph
                .getEdge(
                    expectedGraph.getEdgeSource(expectedEdge),
                    expectedGraph.getEdgeTarget(expectedEdge));

            assertTrue(actualEdge != null);

            assertEquals(
                expectedGraph.getEdgeWeight(expectedEdge), actualGraph.getEdgeWeight(actualEdge),
                1e-5);
        }
    }

    // ----------------------------------------------------------------------
    // Tests

    @Test
    public void testTSPLIBFileDataValues()
    {
        String fileContent = get3DPointsFileContent("EUC_3D");

        TSPLIBImporter<ArrayVector> importer =
            new TSPLIBImporter<>(ArrayVector::new, ArrayVector::getElementValue);
        importer
            .importGraph(
                new SimpleWeightedGraph<>(null, DefaultWeightedEdge::new),
                new StringReader(fileContent));
        TSPLIBFileData<ArrayVector> fileData = importer.getLastImportData();

        assertEquals("theNameOfThisFile", fileData.getName());
        assertEquals("TSP", fileData.getType());
        assertEquals(
            "The first line of the comment" + System.lineSeparator() + "A second line",
            fileData.getComment());
        assertEquals(Integer.valueOf(4), fileData.getDimension());
        assertEquals("EUC_3D", fileData.getEdgeWeightType());
        assertEquals("THREED_COORDS", fileData.getNodeCoordType());

        assertFalse(fileData.hasDistinctVertices());
        assertTrue(fileData.hasDistinctEdgesPerVertex());
    }

    // edge weight functions tests

    @Test
    public void testImportGraph_EUC2DEdgeWeightType()
    {
        List<ArrayVector> vertices = getExpected2DPoints();
        Graph<ArrayVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 7.); // round sqrt(46.16)
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 5.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 5.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 6.); // round sqrt(36.56)
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 4.);

        TSPLIBFileData<ArrayVector> fileData = importFile(get2DPointsFileContent("EUC_2D"));

        // Check weights and complete connection
        assertEqualGraphs(expectedGraph, fileData.getGraph());

        assertTrue(fileData.hasDistinctVertices());
        assertTrue(fileData.hasDistinctEdgesPerVertex());
    }

    @Test
    public void testImportGraph_EUC3DEdgeWeightType()
    {
        List<ArrayVector> vertices = getExpected3DPoints();
        Graph<ArrayVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 6.); // round sqrt(41)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 5.);

        TSPLIBFileData<ArrayVector> fileData = importFile(get3DPointsFileContent("EUC_3D"));

        // Check weights
        assertEqualGraphs(expectedGraph, fileData.getGraph());

        assertFalse(fileData.hasDistinctVertices());
        assertTrue(fileData.hasDistinctEdgesPerVertex());

    }

    @Test
    public void testImportGraph_MAX2DEdgeWeightType()
    {
        List<ArrayVector> vertices = getExpected2DPoints();
        Graph<ArrayVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 4.);

        TSPLIBFileData<ArrayVector> fileData = importFile(get2DPointsFileContent("MAX_2D"));

        // Check weights
        assertEqualGraphs(expectedGraph, fileData.getGraph());

        assertTrue(fileData.hasDistinctVertices());
        assertFalse(fileData.hasDistinctEdgesPerVertex());

    }

    @Test
    public void testImportGraph_MAX3DEdgeWeightType()
    {
        List<ArrayVector> vertices = getExpected3DPoints();
        Graph<ArrayVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 5.);

        TSPLIBFileData<ArrayVector> fileData = importFile(get3DPointsFileContent("MAX_3D"));

        // Check weights
        assertEqualGraphs(expectedGraph, fileData.getGraph());

        assertFalse(fileData.hasDistinctVertices());
        assertFalse(fileData.hasDistinctEdgesPerVertex());

    }

    @Test
    public void testImportGraph_MAN2DEdgeWeightType()
    {
        List<ArrayVector> vertices = getExpected2DPoints();
        Graph<ArrayVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 10.0);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 6.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 6.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 8.);
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 4.);

        TSPLIBFileData<ArrayVector> fileData = importFile(get2DPointsFileContent("MAN_2D"));

        // Check weights
        assertEqualGraphs(expectedGraph, fileData.getGraph());

        assertTrue(fileData.hasDistinctVertices());
        assertTrue(fileData.hasDistinctEdgesPerVertex());

    }

    @Test
    public void testImportGraph_MAN3DEdgeWeightType()
    {
        List<ArrayVector> vertices = getExpected3DPoints();
        Graph<ArrayVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 9.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 5.);

        TSPLIBFileData<ArrayVector> fileData = importFile(get3DPointsFileContent("MAN_3D"));

        // Check weights
        assertEqualGraphs(expectedGraph, fileData.getGraph());

        assertFalse(fileData.hasDistinctVertices());
        assertTrue(fileData.hasDistinctEdgesPerVertex());
    }

    @Test
    public void testImportGraph_CEIL2DEdgeWeightType()
    {
        List<ArrayVector> vertices = getExpected2DPoints();
        Graph<ArrayVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 7.); // round sqrt(46.16)
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 6.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 6.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 7.); // round sqrt(36.56)
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 4.);

        TSPLIBFileData<ArrayVector> fileData = importFile(get2DPointsFileContent("CEIL_2D"));

        // Check weights and complete connection
        assertEqualGraphs(expectedGraph, fileData.getGraph());

        assertTrue(fileData.hasDistinctVertices());
        assertTrue(fileData.hasDistinctEdgesPerVertex());
    }

    @Test
    public void testImportGraph_GEOEdgeWeightType()
    {
        List<ArrayVector> vertices = getExpected2DPoints();
        Graph<ArrayVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 446);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 727);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 549);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 541);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 680);
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 446);

        TSPLIBFileData<ArrayVector> fileData = importFile(get2DPointsFileContent("GEO"));

        // Check weights and complete connection
        assertEqualGraphs(expectedGraph, fileData.getGraph());

        assertTrue(fileData.hasDistinctVertices());
        assertTrue(fileData.hasDistinctEdgesPerVertex());
    }

    @Test
    public void testCompute2DGeographicalDistance()
    {
        MutableInt index = new MutableInt(0);
        TSPLIBImporter<ArrayVector> importer = new TSPLIBImporter<>(
            e -> new ArrayVector(index.getAndIncrement(), e), ArrayVector::getElementValue);

        int halfCircleCircumfence = (int) (TSPLIBImporter.PI * TSPLIBImporter.RRR);
        int quarterCircleCircumfence = (int) (TSPLIBImporter.PI * TSPLIBImporter.RRR / 2);

        int d0 = importer.compute2DGeographicalDistance(vector(0.0, 0.0), vector(0.0, 90.0));
        assertEquals(quarterCircleCircumfence, d0, 1.0);

        int d1 = importer.compute2DGeographicalDistance(vector(23.0, 15.0), vector(-23.0, 105.0));
        assertEquals(10997, d1, 1.0);

        int d2 = importer.compute2DGeographicalDistance(vector(0.0, -90.2), vector(0.0, 89.8));
        assertEquals(halfCircleCircumfence, d2, 1.0);

        int d3 = importer.compute2DGeographicalDistance(vector(20.0, -90.7), vector(-20.0, 89.3));
        assertEquals(halfCircleCircumfence, d3, 1.0);

        int d4 = importer.compute2DGeographicalDistance(vector(20.0, -70.0), vector(-20.0, 110.0));
        assertEquals(halfCircleCircumfence, d4, 1.0);

        int d5 = importer.compute2DGeographicalDistance(vector(40.48, -74.0), vector(52.3, 13.24));
        assertEquals(6386, d5, 1.0);

        int d6 =
            importer.compute2DGeographicalDistance(vector(1.48, 113.24), vector(-6.36, -65.24));
        assertEquals(19488, d6, 1.0);
    }

    @Test
    public void testImportGraph_ATTEdgeWeightType()
    {
        List<ArrayVector> vertices = getExpected2DPoints();
        Graph<ArrayVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 2.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 3.); // round sqrt(46.16)
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 2.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 2.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 2.); // round sqrt(36.56)
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 2.);

        TSPLIBFileData<ArrayVector> fileData = importFile(get2DPointsFileContent("ATT"));

        // Check weights and complete connection
        assertEqualGraphs(expectedGraph, fileData.getGraph());

        assertTrue(fileData.hasDistinctVertices());
        assertFalse(fileData.hasDistinctEdgesPerVertex());
    }

    // exception tests

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testImportGraph_NotSupportedEdgeWeightType_IllegalStateException()
    {
        String fileContent = "EDGE_WEIGHT_TYPE : XRAY1";

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Unsupported EDGE_WEIGHT_TYPE <XRAY1>");

        importFile(fileContent);
    }

    @Test
    public void testImportGraph_OnlyNodeCoordSection_IllegalStateException()
    {
        StringJoiner c = new StringJoiner(System.lineSeparator());
        c.add("NODE_COORD_SECTION");
        c.add("1 10.2 15.0");
        c.add("2 14.2 15.0");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Missing data to read <NODE_COORD_SECTION>");

        importFile(c.toString());
    }

    @Test
    public void testImportGraph_MissingValue_IllegalStateException()
    {
        String fileContent = "NAME : ";

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Missing value for key NAME");

        importFile(fileContent);
    }

    @Test
    public void testImportGraph_MultipleValues_IllegalStateException()
    {
        StringJoiner c = new StringJoiner(System.lineSeparator());
        c.add("TYPE : TSP");
        c.add("TYPE : TSP");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Multiple values for key TYPE");

        importFile(c.toString());
    }

    @Test
    public void testImportGraph_WrongNodeCoordinateElementCount_IllegalArgumentException()
    {
        StringJoiner c = new StringJoiner(System.lineSeparator());
        c.add("EDGE_WEIGHT_TYPE : EUC_3D");
        c.add("NODE_COORD_SECTION");
        c.add("1 10.2 15.0");

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unexpected number of line elements: 3 in line: 1 10.2 15.0");

        importFile(c.toString());
    }

    @Test
    public void testImportGraph_ProvidePseudograph_IllegalArgumentException()
    {
        Graph<ArrayVector, DefaultWeightedEdge> graph =
            new Pseudograph<>(null, DefaultWeightedEdge::new, true);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Provided graph must be empty and must have simple weighted type");

        new TSPLIBImporter<>(ArrayVector::new, ArrayVector::getElementValue)
            .importGraph(graph, new StringReader(""));
    }

    @Test
    public void testImportGraph_ProvideSimpleNotWeightedGraph_IllegalArgumentException()
    {
        Graph<ArrayVector, DefaultWeightedEdge> graph =
            new SimpleGraph<>(null, DefaultWeightedEdge::new, false);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Provided graph must be empty and must have simple weighted type");

        new TSPLIBImporter<>(ArrayVector::new, ArrayVector::getElementValue)
            .importGraph(graph, new StringReader(""));
    }

    @Test
    public void testImportGraph_ProvideNotEmptySimpleWeightedGraph_IllegalArgumentException()
    {
        Graph<ArrayVector, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(null, DefaultWeightedEdge::new);
        graph.addVertex(new ArrayVector(0, 0, 0));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Provided graph must be empty and must have simple weighted type");

        new TSPLIBImporter<>(ArrayVector::new, ArrayVector::getElementValue)
            .importGraph(graph, new StringReader(""));
    }

    // utility methods

    private static TSPLIBFileData<ArrayVector> importFile(String fileContent)
    {
        TSPLIBImporter<ArrayVector> importer =
            new TSPLIBImporter<>(ArrayVector::new, ArrayVector::getElementValue);

        importer.importGraph(null, new StringReader(fileContent));

        return importer.getLastImportData();
    }

    private static Graph<ArrayVector, DefaultWeightedEdge> getExpectedGraph(
        List<ArrayVector> vertices)
    {
        Graph<ArrayVector, DefaultWeightedEdge> expectedGraph =
            new SimpleWeightedGraph<>(null, DefaultWeightedEdge::new);
        Graphs.addAllVertices(expectedGraph, vertices);

        return expectedGraph;
    }
}
