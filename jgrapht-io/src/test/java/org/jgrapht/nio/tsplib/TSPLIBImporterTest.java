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

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.*;
import org.jgrapht.nio.tsplib.TSPLIBImporter.Node;
import org.jgrapht.nio.tsplib.TSPLIBImporter.*;
import org.junit.*;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.*;

public class TSPLIBImporterTest
{

    private static class TestVector
    {
        private final int index;
        private final double[] elements;

        public TestVector(int index, double... elements)
        {
            this.index = index;
            this.elements = elements;
        }

        public int getIndex()
        {
            return index;
        }

        public double[] getElementValues()
        {
            return Arrays.copyOf(elements, elements.length);
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

    private static StringJoiner get3DPointsFileContent(String edgeWeightType)
    {
        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("TYPE : TSP");
        fileContent.add("DIMENSION : 4");
        fileContent.add("EDGE_WEIGHT_TYPE : " + edgeWeightType);
        fileContent.add("NODE_COORD_SECTION");
        fileContent.add("1 10.0 15.0 3.7");
        fileContent.add("2 14.0 15.0 3.7");
        fileContent.add("3 14.0 20.0 3.7");
        fileContent.add("4 14.0 20.0 3.7");
        return fileContent;
    }

    private static List<TestVector> getExpected3DPoints()
    {
        return Arrays
            .asList(
                new TestVector(1, 10.0, 15.0, 3.7), new TestVector(2, 14.0, 15.0, 3.7),
                new TestVector(3, 14.0, 20.0, 3.7), new TestVector(4, 14.0, 20.0, 3.7));
    }

    private static StringJoiner get2DPointsFileContent(String edgeWeightType)
    {
        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("TYPE : TSP");
        fileContent.add("DIMENSION : 4");
        fileContent.add("EDGE_WEIGHT_TYPE : " + edgeWeightType);
        fileContent.add("NODE_COORD_SECTION");
        fileContent.add("1 10.2 15.0");
        fileContent.add("2 14.2 15.0");
        fileContent.add("3 14.8 20.0");
        fileContent.add("4 10.8 20.0");
        fileContent.add("EOF");
        return fileContent;
    }

    private static List<TestVector> getExpected2DPoints()
    {
        return Arrays
            .asList(
                new TestVector(1, 10.2, 15.0), new TestVector(2, 14.2, 15.0),
                new TestVector(3, 14.8, 20.0), new TestVector(4, 10.8, 20.0));
    }

    // ----------------------------------------------------------------------
    // tests

    @Test
    public void testMetaDataValues()
    {
        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("NAME : theNameOfThisFile");
        fileContent.add("COMMENT : The first line of the comment");
        fileContent.add("COMMENT : A second line");
        fileContent.add("TYPE : TSP");
        fileContent.add("DIMENSION : 4");
        fileContent.add("EDGE_WEIGHT_TYPE : EUC_2D");
        fileContent.add("NODE_COORD_TYPE: THREED_COORDS");
        fileContent.add("CAPACITY : 7");
        fileContent.add("EDGE_WEIGHT_FORMAT: FULL_MATRIX");
        fileContent.add("EDGE_DATA_FORMAT: ADJ_LIST");
        fileContent.add("DISPLAY_DATA_TYPE  :   TWOD_DISPLAY");
        fileContent.add("NODE_COORD_SECTION");
        fileContent.add("1 10.2 15.0");
        fileContent.add("EOF");

        Metadata<Object, DefaultWeightedEdge> metaData =
            importGraphFromFile(fileContent).getSecond();

        Specification spec = metaData.getSpecification();
        assertEquals("theNameOfThisFile", spec.getName());
        assertEquals("TSP", spec.getType());
        assertEquals(
            Arrays.asList("The first line of the comment", "A second line"), spec.getComments());
        assertEquals(Integer.valueOf(4), spec.getDimension());
        assertEquals(Integer.valueOf(7), spec.getCapacity());
        assertEquals("EUC_2D", spec.getEdgeWeightType());
        assertEquals("FULL_MATRIX", spec.getEdgeWeightFormat());
        assertEquals("ADJ_LIST", spec.getEdgeDataFormat());
        assertEquals("THREED_COORDS", spec.getNodeCoordType());
        assertEquals("TWOD_DISPLAY", spec.getDisplayDataType());

        assertTrue(metaData.hasDistinctNodeLocations());
        assertTrue(metaData.hasDistinctNeighborDistances());
    }

    @Test
    public void testImportGraph_withNodeCoordSection()
    {
        StringJoiner fileContent = get2DPointsFileContent("EUC_2D");
        Graph<TestVector, DefaultWeightedEdge> expectedGraph =
            getExpectedGraph(getExpected2DPoints());

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> importFile =
            importGraphFromFile(fileContent);
        Graph<Object, DefaultWeightedEdge> graph = importFile.getFirst();
        Metadata<Object, DefaultWeightedEdge> metaData = importFile.getSecond();

        assertGraphVertexNodes(expectedGraph, graph, metaData);

        assertTrue(metaData.hasDistinctNodeLocations());
        assertTrue(metaData.hasDistinctNeighborDistances());
    }

    @Test
    public void testImportGraph_withNodeCoordSectionAndTourSection()
    {

        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("TYPE : TSP");
        fileContent.add("DIMENSION : 4");
        fileContent.add("EDGE_WEIGHT_TYPE : EUC_2D");
        fileContent.add("NODE_COORD_SECTION");
        fileContent.add("7 10.2 15.0");
        fileContent.add("2 14.2 15.0");
        fileContent.add("9 14.8 20.0");
        fileContent.add("4 10.8 20.0");
        fileContent.add("TOUR_SECTION");
        fileContent.add("9");
        fileContent.add("4");
        fileContent.add("7");
        fileContent.add("2");
        fileContent.add("-1");
        fileContent.add("EOF");

        List<TestVector> expectedVectors = new ArrayList<>();
        expectedVectors.add(new TestVector(7, 10.2, 15.0));
        expectedVectors.add(new TestVector(2, 14.2, 15.0));
        expectedVectors.add(new TestVector(9, 14.8, 20.0));
        expectedVectors.add(new TestVector(4, 10.8, 20.0));
        Graph<TestVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(expectedVectors);

        List<Integer> expectedTour = Arrays.asList(9, 4, 7, 2);

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> importData =
            importGraphFromFile(fileContent);
        Graph<Object, DefaultWeightedEdge> graph = importData.getFirst();
        Metadata<Object, DefaultWeightedEdge> metaData = importData.getSecond();

        assertGraphVertexNodes(expectedGraph, graph, metaData);
        assertTrue(metaData.hasDistinctNodeLocations());
        assertTrue(metaData.hasDistinctNeighborDistances());

        assertTour(metaData.getTour(), expectedTour, metaData.getVertexToNodeMapping());
    }

    @Test
    public void testImportTour_onlyWithTourSection()
    {
        StringJoiner otherFileContent = new StringJoiner(System.lineSeparator());
        otherFileContent.add("DIMENSION : 4");
        otherFileContent.add("EDGE_WEIGHT_TYPE : EUC_2D");
        otherFileContent.add("NODE_COORD_SECTION");
        otherFileContent.add("7 10.2 15.0");
        otherFileContent.add("2 14.2 15.0");
        otherFileContent.add("9 14.8 20.0");
        otherFileContent.add("4 10.8 20.0");

        Metadata<Object, DefaultWeightedEdge> otherFilesMetaData =
            importGraphFromFile(otherFileContent).getSecond();

        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("TYPE : TSP");
        fileContent.add("DIMENSION : 4");
        fileContent.add("EDGE_WEIGHT_TYPE : EUC_2D");
        fileContent.add("TOUR_SECTION");
        fileContent.add("9");
        fileContent.add("4");
        fileContent.add("7");
        fileContent.add("2");
        fileContent.add("-1");
        fileContent.add("EOF");

        List<Integer> expectedTour = Arrays.asList(9, 4, 7, 2);

        TSPLIBImporter<Object, DefaultWeightedEdge> importer = new TSPLIBImporter<>();
        StringReader reader = new StringReader(fileContent.toString());
        List<Object> tour = importer.importTour(otherFilesMetaData, reader);

        assertTour(tour, expectedTour, otherFilesMetaData.getVertexToNodeMapping());
    }

    private static <
        T> void assertTour(List<T> vertexTour, List<Integer> expectedTour, Map<T, Node> vertex2node)
    {
        List<Integer> integerTour = vertexTour
            .stream().map(vertex2node::get).map(Node::getNumber).collect(Collectors.toList());
        assertEquals(expectedTour, integerTour);
    }

    // edge weight functions tests

    @Test
    public void testImportGraph_EdgeWeightTypeEUC2D()
    {
        List<TestVector> vertices = getExpected2DPoints();
        Graph<TestVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 7.); // round sqrt(46.16)
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 5.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 5.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 6.); // round sqrt(36.56)
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 4.);

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> graphData =
            importGraphFromFile(get2DPointsFileContent("EUC_2D"));

        // Check coordinates, weights and complete connection
        assertEqualGraphData(expectedGraph, graphData);

        assertTrue(graphData.getSecond().hasDistinctNodeLocations());
        assertTrue(graphData.getSecond().hasDistinctNeighborDistances());
    }

    @Test
    public void testImportGraph_EdgeWeightTypeEUC3D()
    {
        List<TestVector> vertices = getExpected3DPoints();
        Graph<TestVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 6.); // round sqrt(41)
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 6.); // round sqrt(41)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 0.);

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> graphData =
            importGraphFromFile(get3DPointsFileContent("EUC_3D"));

        // Check coordinates, weights and complete connection
        assertEqualGraphData(expectedGraph, graphData);

        assertFalse(graphData.getSecond().hasDistinctNodeLocations());
        assertFalse(graphData.getSecond().hasDistinctNeighborDistances());
    }

    @Test
    public void testImportGraph_EdgeWeightTypeMAX2D()
    {
        List<TestVector> vertices = getExpected2DPoints();
        Graph<TestVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 4.);

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> graphData =
            importGraphFromFile(get2DPointsFileContent("MAX_2D"));

        // Check coordinates, weights and complete connection
        assertEqualGraphData(expectedGraph, graphData);

        assertTrue(graphData.getSecond().hasDistinctNodeLocations());
        assertFalse(graphData.getSecond().hasDistinctNeighborDistances());
    }

    @Test
    public void testImportGraph_EdgeWeightTypeMAX3D()
    {
        List<TestVector> vertices = getExpected3DPoints();
        Graph<TestVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 0.);

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> graphData =
            importGraphFromFile(get3DPointsFileContent("MAX_3D"));

        // Check coordinates, weights and complete connection
        assertEqualGraphData(expectedGraph, graphData);

        assertFalse(graphData.getSecond().hasDistinctNodeLocations());
        assertFalse(graphData.getSecond().hasDistinctNeighborDistances());
    }

    @Test
    public void testImportGraph_EdgeWeightTypeMAN2D()
    {
        List<TestVector> vertices = getExpected2DPoints();
        Graph<TestVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 10.0);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 6.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 6.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 8.);
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 4.);

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> graphData =
            importGraphFromFile(get2DPointsFileContent("MAN_2D"));

        // Check coordinates, weights and complete connection
        assertEqualGraphData(expectedGraph, graphData);

        assertTrue(graphData.getSecond().hasDistinctNodeLocations());
        assertTrue(graphData.getSecond().hasDistinctNeighborDistances());
    }

    @Test
    public void testImportGraph_EdgeWeightTypeMAN3D()
    {
        List<TestVector> vertices = getExpected3DPoints();
        Graph<TestVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 9.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 9.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 5.);
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 0.);

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> graphData =
            importGraphFromFile(get3DPointsFileContent("MAN_3D"));

        // Check coordinates, weights and complete connection
        assertEqualGraphData(expectedGraph, graphData);

        assertFalse(graphData.getSecond().hasDistinctNodeLocations());
        assertFalse(graphData.getSecond().hasDistinctNeighborDistances());
    }

    @Test
    public void testImportGraph_EdgeWeightTypeCEIL2D()
    {
        List<TestVector> vertices = getExpected2DPoints();
        Graph<TestVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 4.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 7.); // round sqrt(46.16)
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 6.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 6.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 7.); // round sqrt(36.56)
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 4.);

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> graphData =
            importGraphFromFile(get2DPointsFileContent("CEIL_2D"));

        // Check coordinates, weights and complete connection
        assertEqualGraphData(expectedGraph, graphData);

        assertTrue(graphData.getSecond().hasDistinctNodeLocations());
        assertTrue(graphData.getSecond().hasDistinctNeighborDistances());
    }

    @Test
    public void testImportGraph_EdgeWeightTypeGEO()
    {
        List<TestVector> vertices = getExpected2DPoints();
        Graph<TestVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 446);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 727);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 549);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 541);
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 680);
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 446);

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> graphData =
            importGraphFromFile(get2DPointsFileContent("GEO"));

        // Check coordinates, weights and complete connection
        assertEqualGraphData(expectedGraph, graphData);

        assertTrue(graphData.getSecond().hasDistinctNodeLocations());
        assertTrue(graphData.getSecond().hasDistinctNeighborDistances());
    }

    @Test
    public void testCompute2DGeographicalDistance()
    {
        TSPLIBImporter<TestVector, DefaultWeightedEdge> importer = new TSPLIBImporter<>();

        int halfCircleCircumfence = (int) (TSPLIBImporter.PI * TSPLIBImporter.RRR);
        int quarterCircleCircumfence = (int) (TSPLIBImporter.PI * TSPLIBImporter.RRR / 2);

        int d0 = importer.compute2DGeographicalDistance(node(0.0, 0.0), node(0.0, 90.0));
        assertEquals(quarterCircleCircumfence, d0, 1.0);

        int d1 = importer.compute2DGeographicalDistance(node(23.0, 15.0), node(-23.0, 105.0));
        assertEquals(10997, d1, 1.0);

        int d2 = importer.compute2DGeographicalDistance(node(0.0, -90.2), node(0.0, 89.8));
        assertEquals(halfCircleCircumfence, d2, 1.0);

        int d3 = importer.compute2DGeographicalDistance(node(20.0, -90.7), node(-20.0, 89.3));
        assertEquals(halfCircleCircumfence, d3, 1.0);

        int d4 = importer.compute2DGeographicalDistance(node(20.0, -70.0), node(-20.0, 110.0));
        assertEquals(halfCircleCircumfence, d4, 1.0);

        int d5 = importer.compute2DGeographicalDistance(node(40.48, -74.0), node(52.3, 13.24));
        assertEquals(6386, d5, 1.0);

        int d6 = importer.compute2DGeographicalDistance(node(1.48, 113.24), node(-6.36, -65.24));
        assertEquals(19488, d6, 1.0);
    }

    private static Node node(double... elements)
    {
        return new Node(-1, elements);
    }

    @Test
    public void testImportGraph_EdgeWeightTypeATT()
    {
        List<TestVector> vertices = getExpected2DPoints();
        Graph<TestVector, DefaultWeightedEdge> expectedGraph = getExpectedGraph(vertices);

        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(1), 2.);
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(2), 3.); // round sqrt(46.16)
        Graphs.addEdge(expectedGraph, vertices.get(0), vertices.get(3), 2.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(2), 2.); // round sqrt(25.36)
        Graphs.addEdge(expectedGraph, vertices.get(1), vertices.get(3), 2.); // round sqrt(36.56)
        Graphs.addEdge(expectedGraph, vertices.get(2), vertices.get(3), 2.);

        Pair<Graph<Object, DefaultWeightedEdge>, Metadata<Object, DefaultWeightedEdge>> graphData =
            importGraphFromFile(get2DPointsFileContent("ATT"));

        assertEqualGraphData(expectedGraph, graphData);

        assertTrue(graphData.getSecond().hasDistinctNodeLocations());
        assertFalse(graphData.getSecond().hasDistinctNeighborDistances());
    }

    // exception tests

    @Test
    public void testImportGraph_ProvideNotWeightedGraph_ImportException()
    {
        Graph<TestVector, DefaultWeightedEdge> graph =
            new SimpleGraph<>(null, DefaultWeightedEdge::new, false);

        RuntimeException expectedCause = new IllegalArgumentException("Graph must be weighted");

        TSPLIBImporter<TestVector, DefaultWeightedEdge> importer = new TSPLIBImporter<>();
        expectGraphImportFailedException(
            () -> importer.importGraph(graph, new StringReader("")), expectedCause);
    }

    @Test
    public void testImportGraph_MissingValue_ImportException()
    {
        String fileContent = "NAME : ";

        RuntimeException expectedCause = new IllegalStateException("Missing value for key NAME");

        expectGraphImportFailedException(() -> importGraphFromFile(fileContent), expectedCause);
    }

    @Test
    public void testImportGraph_MultipleValues_ImportException()
    {
        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("TYPE : TSP");
        fileContent.add("TYPE : TSP");

        RuntimeException expectedCause = new IllegalStateException("Multiple values for key TYPE");

        expectGraphImportFailedException(() -> importGraphFromFile(fileContent), expectedCause);
    }

    @Test
    public void testImportGraph_invalidSpecificationValue_ImportException()
    {
        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("EDGE_WEIGHT_FORMAT : some String");

        RuntimeException expectedCause =
            new IllegalArgumentException("Invalid EDGE_WEIGHT_FORMAT value <some String>");

        expectGraphImportFailedException(() -> importGraphFromFile(fileContent), expectedCause);
    }

    @Test
    public void testImportGraph_nonNumberDimension_ImportException()
    {
        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("DIMENSION : A_STRING");

        RuntimeException expectedCause =
            new IllegalArgumentException("Invalid DIMENSION integer value <A_STRING>");

        expectGraphImportFailedException(() -> importGraphFromFile(fileContent), expectedCause);
    }

    @Test
    public void testImportGraph_NotSupportedEdgeWeightType_ImportException()
    {
        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("EDGE_WEIGHT_TYPE : XRAY1");
        fileContent.add("DIMENSION:1");
        fileContent.add("NODE_COORD_SECTION");
        fileContent.add("1 10.2 15.0");

        RuntimeException expectedCause =
            new IllegalStateException("Unsupported EDGE_WEIGHT_TYPE <XRAY1>");

        expectGraphImportFailedException(() -> importGraphFromFile(fileContent), expectedCause);
    }

    @Test
    public void testImportGraph_OnlyNodeCoordSection_ImportException()
    {
        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("NODE_COORD_SECTION");
        fileContent.add("1 10.2 15.0");
        fileContent.add("2 14.2 15.0");

        RuntimeException expectedCause =
            new IllegalStateException("Missing data to read <NODE_COORD_SECTION>");

        expectGraphImportFailedException(() -> importGraphFromFile(fileContent), expectedCause);
    }

    @Test
    public void testImportGraph_WrongNodeCoordinateElementCount_ImportException()
    {
        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("EDGE_WEIGHT_TYPE : EUC_3D");
        fileContent.add("DIMENSION: 1");
        fileContent.add("NODE_COORD_SECTION");
        fileContent.add("1 10.2 15.0");

        RuntimeException expectedCause =
            new IllegalArgumentException("Unexpected number of elements <3> in line: 1 10.2 15.0");

        expectGraphImportFailedException(() -> importGraphFromFile(fileContent), expectedCause);
    }

    @Test
    public void testImportTour_missingVertexInTour_ImportException()
    {

        Metadata<Object, DefaultWeightedEdge> otherMetaData =
            importGraphFromFile(get2DPointsFileContent("EUC_2D")).getSecond();

        StringJoiner fileContent = new StringJoiner(System.lineSeparator());
        fileContent.add("DIMENSION: 1");
        fileContent.add("TOUR_SECTION");
        fileContent.add("8");
        fileContent.add("-1");

        RuntimeException expectedCause = new IllegalStateException("Missing vertex with number 8");

        TSPLIBImporter<Object, DefaultWeightedEdge> importer = new TSPLIBImporter<>();

        expectTourImportFailedException(
            () -> importer.importTour(otherMetaData, new StringReader(fileContent.toString())),
            expectedCause);
    }

    // utility methods

    private static Pair<Graph<Object, DefaultWeightedEdge>,
        Metadata<Object, DefaultWeightedEdge>> importGraphFromFile(StringJoiner fileContent)
    {
        return importGraphFromFile(fileContent.toString());
    }

    private static Pair<Graph<Object, DefaultWeightedEdge>,
        Metadata<Object, DefaultWeightedEdge>> importGraphFromFile(String fileContent)
    {
        Graph<Object, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(Object::new, DefaultWeightedEdge::new);

        TSPLIBImporter<Object, DefaultWeightedEdge> importer = new TSPLIBImporter<>();
        importer.importGraph(graph, new StringReader(fileContent));
        return Pair.of(graph, importer.getMetadata());
    }

    private static Graph<TestVector, DefaultWeightedEdge> getExpectedGraph(
        List<TestVector> vertices)
    {
        Graph<TestVector, DefaultWeightedEdge> expectedGraph =
            new SimpleWeightedGraph<>(null, DefaultWeightedEdge::new);
        Graphs.addAllVertices(expectedGraph, vertices);
        return expectedGraph;
    }

    // assertions

    /** Check coordinates, weights and complete connection. */
    private static <V> void assertEqualGraphData(
        Graph<TestVector, DefaultWeightedEdge> expectedGraph,
        Pair<Graph<V, DefaultWeightedEdge>, Metadata<V, DefaultWeightedEdge>> actualData)
    {
        Graph<V, DefaultWeightedEdge> graph = actualData.getFirst();
        Metadata<V, DefaultWeightedEdge> metadata = actualData.getSecond();

        // assert if the read numbers are as expected

        assertGraphVertexNodes(expectedGraph, graph, metadata);

        // assert if the computed edge weights/ distances are as expected
        Set<DefaultWeightedEdge> expectedEdgeSet = expectedGraph.edgeSet();
        assertEquals("Unequal edgeSet size", expectedEdgeSet.size(), graph.edgeSet().size());

        Map<Integer, V> number2vertex = new HashMap<>();
        metadata.getVertexToNodeMapping().forEach((v, n) -> number2vertex.put(n.getNumber(), v));

        for (DefaultWeightedEdge expectedEdge : expectedEdgeSet) {
            int sourceNumber = expectedGraph.getEdgeSource(expectedEdge).getIndex();
            int targetNumber = expectedGraph.getEdgeTarget(expectedEdge).getIndex();
            V source = number2vertex.get(sourceNumber);
            V target = number2vertex.get(targetNumber);

            DefaultWeightedEdge actualEdge = graph.getEdge(source, target);

            assertTrue(actualEdge != null);
            assertEquals(
                expectedGraph.getEdgeWeight(expectedEdge), graph.getEdgeWeight(actualEdge), 1e-5);
        }
    }

    private static <T> void assertGraphVertexNodes(
        Graph<TestVector, DefaultWeightedEdge> expectedGraph, Graph<T, DefaultWeightedEdge> graph,
        Metadata<T, DefaultWeightedEdge> metadata)
    {

        Map<T, Node> vertex2node = metadata.getVertexToNodeMapping();
        List<Node> sortedVertexNodes =
            graph.vertexSet().stream().map(vertex2node::get).collect(Collectors.toList());
        sortedVertexNodes.sort((n1, n2) -> Integer.compare(n1.getNumber(), n2.getNumber()));

        List<TestVector> expectedSortedVectors = new ArrayList<>(expectedGraph.vertexSet());
        expectedSortedVectors.sort((v1, v2) -> Integer.compare(v1.getIndex(), v2.getIndex()));

        // assert if the read coordinates are as expected
        assertEquals(expectedSortedVectors.size(), sortedVertexNodes.size());
        for (int i = 0; i < expectedSortedVectors.size(); i++) {
            TestVector expectedVector = expectedSortedVectors.get(i);
            Node actualNode = sortedVertexNodes.get(i);
            assertEquals(expectedVector.getIndex(), actualNode.getNumber());
            assertEqualElements(expectedVector.getElementValues(), actualNode.getCoordinates());
        }
    }

    private static void assertEqualElements(double[] expected, double[] actual)
    {
        if (!Arrays.equals(actual, expected)) {
            fail(
                "Expected is " + Arrays.toString(expected) + " but was " + Arrays.toString(actual));
        }
    }

    private void expectGraphImportFailedException(Runnable action, RuntimeException expectedCause)
    {
        expectImportFailedException(action, expectedCause, "graph");
    }

    private void expectTourImportFailedException(Runnable action, RuntimeException expectedCause)
    {
        expectImportFailedException(action, expectedCause, "tour");
    }

    private void expectImportFailedException(
        Runnable action, RuntimeException expectedCause, String importTarget)
    {
        try {
            action.run();
            fail("Expected exception: " + ImportException.class.getName());
        } catch (ImportException e) {
            Throwable cause = e.getCause();
            assertEquals(
                "Failed to import " + importTarget + " from TSPLIB-file: " + cause.getMessage(),
                e.getMessage());
            assertEquals(expectedCause.getClass(), cause.getClass());
            assertEquals(expectedCause.getMessage(), cause.getMessage());
        }
    }
}
