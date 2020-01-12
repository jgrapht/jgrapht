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

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.nio.*;
import org.jgrapht.util.*;

/**
 * Importer for files in the
 * <a href="http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/">TSPLIB95</a> format.
 * <p>
 * This importer reads the nodes of a <em>Symmetric travelling salesman problem</em> instance from a
 * file and creates a {@link GraphTests#isComplete(Graph) complete graph} and provides further data
 * from the file and about the imported graph.
 * </p>
 * <p>
 * This implementation does not cover the full TSPLIB95 standard and only implements the subset of
 * capabilities required at the time of creation. The following keywords of <em>The specification
 * part</em> in chapter 1.1 of the <em>TSPLIB95</em> standard are supported:
 * <ul>
 * <li>NAME</li>
 * <li>TYPE</li>
 * <li>COMMENT</li>
 * <li>DIMENSION</li>
 * <li>EDGE_WEIGHT_TYPE</li> Only the following edge weight types are supported
 * <ul>
 * <li>EUC_2D</li>
 * <li>EUC_3D</li>
 * <li>MAX_2D</li>
 * <li>MAX_3D</li>
 * <li>MAN_2D</li>
 * <li>MAN_3D</li>
 * <li>CEIL2D</li>
 * <li>GEO</li>
 * <li>ATT</li>
 * </ul>
 * </ul>
 * The values of all supported keywords can be obtained from corresponding getters.
 * </p>
 * <p>
 * The following data sections of <em>The data part</em> in chapter 1.2 of the <em>TSPLIB95</em>
 * standard are supported:
 * <ul>
 * <li>NODE_COORD_SECTION</li>
 * </ul>
 * </p>
 * <p>
 * It was attempted to make the structure of this implementation generic so further keywords from
 * the specification part or other data sections can be considered if required by broaden this
 * class. Currently this implementation only reads <em>Symmetric travelling salesman problems</em>
 * with a NODE_COORD_SECTION and on of the supported EDGE_WEIGHT_TYPE.
 * </p>
 * <p>
 * The website of the TSPLIB standard already contains a large library of different TSP instances
 * provided as files in TSPLIB format. The
 * <a href="http://www.math.uwaterloo.ca/tsp/data/index.html">TSPLIB library of the University of
 * Waterlo</a> provides more problem instances, among others a World TSP and instances based on
 * cities of different countries.
 * </p>
 * 
 * @author Hannes Wellmann
 * @param <V> the type of vertices created by this importer
 *
 */
public class TSPLIBImporter<V>
    implements
    GraphImporter<V, DefaultWeightedEdge>
{
    /**
     * Container for data of an imported <em>TSPLIB95</em> file.
     * 
     * @author Hannes Wellmann
     * @param <V> the type of vertices in the imported graph
     */
    public static class TSPLIBFileData<V>
    {

        private String name;
        private String type;
        private String comment;
        private Integer dimension;
        private String edgeWeightType;
        private String nodeCoordType;
        private boolean hasDistinctLocations;
        private Boolean hasDistinctEdges;

        private Graph<V, DefaultWeightedEdge> graph;
        private List<Integer> tour;

        // getters for read public data

        /**
         * Returns the value of the <em>NAME</em> keyword in the imported file.
         * 
         * @return the value of the <em>NAME</em> keyword
         */
        public String getName()
        {
            return name;
        }

        /**
         * Returns the value of the <em>TYPE</em> keyword in the imported file.
         * 
         * @return the value of the <em>TYPE</em> keyword
         */
        public String getType()
        {
            return type;
        }

        /**
         * Returns the joined value of the <em>COMMENT</em> keyword in the imported file.
         * 
         * @return the value of the <em>COMMENT</em> keyword
         */
        public String getComment()
        {
            return comment;
        }

        /**
         * Returns the value of the <em>DIMENSION</em> keyword in the imported file.
         * 
         * @return the value of the <em>DIMENSION</em> keyword
         */
        public Integer getDimension()
        {
            return dimension;
        }

        /**
         * Returns the value of the <em>EDGE_WEIGHT_TYPE</em> keyword in the imported file.
         * 
         * @return the value of the <em>EDGE_WEIGHT_TYPE</em> keyword
         */
        public String getEdgeWeightType()
        {
            return edgeWeightType;
        }

        /**
         * Returns the value of the <em>NODE_COORD_TYPE</em> keyword in the imported file.
         * 
         * @return the value of the <em>NODE_COORD_TYPE</em> keyword
         */
        public String getNodeCoordType()
        {
            return nodeCoordType;
        }

        /**
         * Returns the complete {@link Graph} of location connections built from the imported file
         * or null if no graph was imported.
         * 
         * @return the complete {@code Graph} of location connections
         */
        public Graph<V, DefaultWeightedEdge> getGraph()
        {
            return graph;
        }

        /**
         * Returns the {@link List} of {@link Integer Integers} describing vertex order of the tour
         * from the imported file or null if no tour was imported.
         * 
         * @return the tour described by the list of vertex indices
         */
        public List<Integer> getTour()
        {
            return tour;
        }

        /**
         * Returns true if all read vertices were distinct and non of them were
         * {@link Object#equals(Object) equal}, else false.
         * <p>
         * The read {@link #getGraph() Graph} does not contain equal vertices because the vertices
         * are stored in a {@link Set}. So in case the imported file contains duplicated locations
         * all duplicates are discarded. This method tells if this was the case.
         * </p>
         * 
         * @return true if only not equal vertices were imported from the file, else false
         */
        public boolean hasDistinctVertices()
        {
            return hasDistinctLocations;
        }

        /**
         * Returns true if for each vertex all touching edges have different weights.
         * <p>
         * If this method returns true this means for the TSP that for each location each other
         * location has a different distance, so there are no two other locations that have the same
         * distance from that location.
         * </p>
         * 
         * @return true if all touching edges of each vertex have different weight, else false
         */
        public boolean hasDistinctEdgesPerVertex()
        {
            if (hasDistinctEdges != null) {
                return hasDistinctEdges;
            }
            hasDistinctEdges = Boolean.TRUE;

            if (graph != null) {
                for (V v : graph.vertexSet()) {
                    Set<DefaultWeightedEdge> edgesOf = graph.edgesOf(v);

                    Set<Double> weights = CollectionUtil.newHashSetWithExpectedSize(edgesOf.size());
                    for (DefaultWeightedEdge edge : edgesOf) {
                        weights.add(graph.getEdgeWeight(edge));
                    }
                    if (weights.size() != edgesOf.size()) {
                        hasDistinctEdges = Boolean.FALSE;
                        break;
                    }
                }
            }
            return hasDistinctEdges;
        }
    }

    /**
     * A getter to obtain element values of Vector data structures.
     * 
     * @author Hannes Wellmann
     *
     * @param <V> the type of handled vectors
     */
    public interface ElementGetter<V>
    {
        /**
         * Returns the value of the element at the specified {@code index} in the given {@link V
         * vector}.
         * 
         * @param vector from which an element value is to return
         * @param index of the element to return
         * @return the value of the specified element in the given vector
         */
        double getElement(V vector, int index);
    }

    private final Function<double[], V> vectorFactory;
    private final ElementGetter<V> elementGetter;
    private int vectorLength = -1;

    /**
     * Create a {@link TSPLIBImporter} that uses the given {@code vectorFactory} to create its
     * vertices with certain element values and later uses the specified {@code elementGetter} to
     * obtain the element values and compute the distances between the vertices.
     * 
     * @param vectorFactory used to create vector vertices with certain element values
     * @param elementGetter used to obtain element values from the created vertex-objects
     */
    public TSPLIBImporter(Function<double[], V> vectorFactory, ElementGetter<V> elementGetter)
    {
        this.vectorFactory = vectorFactory;
        this.elementGetter = elementGetter;
    }

    // import data

    private TSPLIBFileData<V> lastImportData = null;

    /**
     * Returns the {@link TSPLIBFileData} of the latest imported file or null, if no import
     * completed yet or the latest import failed.
     * 
     * @return {@code TSPLIBFileData} of the latest import
     */
    public TSPLIBFileData<V> getLastImportData()
    {
        return lastImportData;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The given {@link Graph}, if not null, must have a {@link GraphType#isSimple() simple type}
     * and must be weighted and empty. If the given {@code Graph} is null, a suitable one is
     * created.
     * </p>
     * <p>
     * This implementation is not thread-safe and must be synchronized externally if called by
     * concurrent threads.
     * </p>
     * <p>
     * {@link TSPLIBFileData} of the previous import can be obtained from
     * {@link #getLastImportData()}.
     * </p>
     * 
     * @param graph into which this importer writes, must be simple, weighted and empty or may be
     *        null.
     * @throws IllegalArgumentException if the specified {@code graph} is not simple or is not empty
     */
    @Override
    public void importGraph(Graph<V, DefaultWeightedEdge> graph, Reader in)
    {
        lastImportData = null;
        vectorLength = -1;
        if (graph == null) {
            @SuppressWarnings("unchecked") Supplier<DefaultWeightedEdge> edgeSupplier =
                (Supplier<DefaultWeightedEdge> & Serializable) DefaultWeightedEdge::new;
            graph = new SimpleWeightedGraph<>(null, edgeSupplier);
        } else if (!graph.getType().isSimple() || !graph.getType().isWeighted()
            || !graph.vertexSet().isEmpty())
        {
            throw new IllegalArgumentException(
                "Provided graph must be empty and must have simple weighted type");
        }

        try (BufferedReader reader = new BufferedReader(in)) {
            lastImportData = readContent(reader, graph);
        } catch (IOException e) {
            throw new ImportException("Import of TSPLib file failed", e);
        }
    }

    private TSPLIBFileData<V> readContent(
        BufferedReader reader, Graph<V, DefaultWeightedEdge> graph)
        throws IOException
    {
        TSPLIBFileData<V> data = new TSPLIBFileData<>();

        StringJoiner multiLineComment = new StringJoiner(System.lineSeparator());
        Function<String, V> vertexFactory = null;
        ToIntBiFunction<V, V> edgeWeightFunction = null;

        for (String line; (line = reader.readLine()) != null;) {
            String[] keyValue = line.trim().split(":");

            switch (getKey(keyValue)) {
            case "NAME":
                requireNotSet(data.name, "NAME");
                data.name = getValue(keyValue);
                break;

            case "TYPE":
                requireNotSet(data.type, "TYPE");
                data.type = getValue(keyValue);
                break;

            case "COMMENT":
                multiLineComment.add(getValue(keyValue));
                break;

            case "DIMENSION":
                requireNotSet(data.dimension, "DIMENSION");
                data.dimension = Integer.parseInt(getValue(keyValue));
                break;

            case "EDGE_WEIGHT_TYPE":
                requireNotSet(data.edgeWeightType, "EDGE_WEIGHT_TYPE");
                data.edgeWeightType = getValue(keyValue);

                switch (data.edgeWeightType) {
                case "EUC_2D":
                    vectorLength = 2;
                    vertexFactory = this::createVector2D;
                    edgeWeightFunction = this::computeEuclideanDistance;
                    break;

                case "EUC_3D":
                    vectorLength = 3;
                    vertexFactory = this::createVector3D;
                    edgeWeightFunction = this::computeEuclideanDistance;
                    break;

                case "MAX_2D":
                    vectorLength = 2;
                    vertexFactory = this::createVector2D;
                    edgeWeightFunction = this::computeMaximumDistance;
                    break;

                case "MAX_3D":
                    vectorLength = 3;
                    vertexFactory = this::createVector3D;
                    edgeWeightFunction = this::computeMaximumDistance;
                    break;

                case "MAN_2D":
                    vectorLength = 2;
                    vertexFactory = this::createVector2D;
                    edgeWeightFunction = this::computeManhattanDistance;
                    break;

                case "MAN_3D":
                    vectorLength = 3;
                    vertexFactory = this::createVector3D;
                    edgeWeightFunction = this::computeManhattanDistance;
                    break;

                case "CEIL_2D":
                    vectorLength = 2;
                    vertexFactory = this::createVector2D;
                    edgeWeightFunction = this::compute2DCeilingEuclideanDistance;
                    break;

                case "GEO":
                    vectorLength = 2;
                    vertexFactory = this::createVector2D;
                    edgeWeightFunction = this::compute2DGeographicalDistance;
                    break;

                case "ATT":
                    vectorLength = 2;
                    vertexFactory = this::createVector2D;
                    edgeWeightFunction = this::compute2DPseudoEuclideanDistance;
                    break;

                default:
                    throw new IllegalStateException(
                        "Unsupported EDGE_WEIGHT_TYPE <" + data.edgeWeightType + ">");
                }
                break;

            case "NODE_COORD_TYPE":
                requireNotSet(data.nodeCoordType, "NODE_COORD_TYPE");
                data.nodeCoordType = getValue(keyValue);
                break;

            case "NODE_COORD_SECTION":
                requireNotSet(data.graph, "NODE_COORD_SECTION");
                if (vertexFactory != null && edgeWeightFunction != null) {
                    data.hasDistinctLocations = readCompleteNodeGraphData(
                        graph, reader, vertexFactory, edgeWeightFunction, data.dimension);
                    data.graph = graph;
                } else {
                    throw new IllegalStateException("Missing data to read <NODE_COORD_SECTION>");
                }
                break;

            case "TOUR_SECTION":
                requireNotSet(data.tour, "TOUR_SECTION");
                data.tour = readTourData(reader, data.dimension);
                break;

            default:
                break;
            }
        }
        if (multiLineComment.length() > 0) {
            data.comment = multiLineComment.toString();
        }
        return data;
    }

    private static String getKey(String[] keyValue)
    {
        return keyValue[0].trim().toUpperCase();
    }

    private String getValue(String[] keyValue)
    {
        if (keyValue.length < 2) {
            throw new IllegalStateException("Missing value for key " + getKey(keyValue));
        }
        return keyValue[1].trim();
    }

    private void requireNotSet(Object target, String keyName)
    {
        if (target != null) {
            throw new IllegalStateException("Multiple values for key " + keyName);
        }
    }

    // distance computations

    // all of the following methods are implemented in accordance to
    // section "2. The distance functions" of TSPLIB95

    /**
     * Computes the distance of two positions p1 and p2 according to the {@code EUC_2D} or
     * {@code EUC_3D} metric depending on their dimension. The used metric is also known as L2-norm.
     * 
     * @param p1 a two or three dimensional point
     * @param p2 a two or three dimensional point
     * @return the {@code EUC_2D} or {@code EUC_3D} edge weight for points p1 and p2
     */
    int computeEuclideanDistance(V p1, V p2)
    { // according to TSPLIB95 distances are rounded to next integer value
        return (int) Math.round(getL2Distance(p1, p2));
    }

    /**
     * Computes the distance of two positions p1 and p2 according to the {@code MAX_2D} or
     * {@code MAX_3D} metric depending on their dimension. The used metric is also known as
     * L&infin;-norm.
     * 
     * @param p1 a two or three dimensional point
     * @param p2 a two or three dimensional point
     * @return the {@code MAX_2D} or {@code MAX_3D} edge weight for points p1 and p2
     */
    int computeMaximumDistance(V p1, V p2)
    { // according to TSPLIB95 distances are rounded to next integer value
        return (int) Math.round(getLInfDistance(p1, p2));
    }

    /**
     * Computes the distance of two positions p1 and p2 according to the {@code MAN_2D} or
     * {@code MAN_3D} metric depending on their dimension. The used metric is also known as L1-norm.
     * 
     * @param p1 a two or three dimensional point
     * @param p2 a two or three dimensional point
     * @return the {@code MAN_2D} or {@code MAN_3D} edge weight for points p1 and p2
     */
    int computeManhattanDistance(V p1, V p2)
    { // according to TSPLIB95 distances are rounded to next integer value
        return (int) Math.round(getL1Distance(p1, p2));
    }

    /**
     * Computes the distance of two positions p1 and p2 according to the {@code CEIL_2D} metric, the
     * round up version of {@code EUC_2D}. The points must have dimension two.
     * 
     * @param p1 a two or three dimensional point
     * @param p2 a two or three dimensional point
     * @return the {@code CEIL_2D} edge weight for points p1 and p2
     * @see #computeEuclideanDistance(RealVector, RealVector)
     */
    int compute2DCeilingEuclideanDistance(V p1, V p2)
    {
        return (int) Math.ceil(getL2Distance(p1, p2));
    }

    /**
     * Computes the distance of two positions p1 and p2 according to the {@code GEO} metric. The
     * used metric computes the distance between two points on a earth-like sphere, while the point
     * coordinates describe their geographical latitude and longitude. The points must have
     * dimension two.
     * 
     * @param p1 a two or three dimensional point
     * @param p2 a two or three dimensional point
     * @return the {@code GEO} edge weight for points p1 and p2
     */
    int compute2DGeographicalDistance(V p1, V p2)
    {
        double latitude1 = computeRadiansAngle(elementGetter.getElement(p1, 0));
        double longitude1 = computeRadiansAngle(elementGetter.getElement(p1, 1));

        double latitude2 = computeRadiansAngle(elementGetter.getElement(p2, 0));
        double longitude2 = computeRadiansAngle(elementGetter.getElement(p2, 1));

        double q1 = Math.cos(longitude1 - longitude2);
        double q2 = Math.cos(latitude1 - latitude2);
        double q3 = Math.cos(latitude1 + latitude2);
        return (int) (RRR * Math.acos(0.5 * ((1.0 + q1) * q2 - (1.0 - q1) * q3)) + 1.0);
    }

    static final double PI = 3.141592; // constants according to TSPLIB95
    static final double RRR = 6378.388; // constants according to TSPLIB95

    private static double computeRadiansAngle(double x)
    { // computation according to TSPLIB95 chapter 2.4 - Geographical distance
      // First computes decimal angle from degrees and minutes, then converts it into radian
        double deg = Math.round(x);
        double min = x - deg;
        return PI * (deg + 5.0 * min / 3.0) / 180.0;
    }

    /**
     * Computes the distance of two positions p1 and p2 according to the {@code ATT} metric. The
     * points must have dimension two.
     * 
     * @param p1 a two or three dimensional point
     * @param p2 a two or three dimensional point
     * @return the {@code ATT} edge weight for points p1 and p2
     */
    int compute2DPseudoEuclideanDistance(V p1, V p2)
    {
        double xd = elementGetter.getElement(p1, 0) - elementGetter.getElement(p2, 0);
        double yd = elementGetter.getElement(p1, 1) - elementGetter.getElement(p2, 1);
        double rij = Math.sqrt((xd * xd + yd * yd) / 10.0);
        double tij = Math.round(rij);
        if (tij < rij) {
            return (int) (tij + 1);
        } else {
            return (int) tij;
        }
    }

    private double getL1Distance(V p1, V p2)
    {
        double elementSum = 0;
        for (int i = 0; i < vectorLength; i++) {
            double delta = elementGetter.getElement(p1, i) - elementGetter.getElement(p2, i);
            elementSum += Math.abs(delta);
        }
        return elementSum;
    }

    private double getL2Distance(V p1, V p2)
    {
        double elementSum = 0;
        for (int i = 0; i < vectorLength; i++) {
            double delta = elementGetter.getElement(p1, i) - elementGetter.getElement(p2, i);
            elementSum += delta * delta;
        }
        return Math.sqrt(elementSum);
    }

    private double getLInfDistance(V p1, V p2)
    {
        double maxElement = 0;
        for (int i = 0; i < vectorLength; i++) {
            double delta = elementGetter.getElement(p1, i) - elementGetter.getElement(p2, i);
            maxElement = Math.max(maxElement, Math.abs(delta));
        }
        return maxElement;
    }

    // node build

    private V createVector2D(String line)
    {
        String[] elements = splitLineElements(line, 3);
        // index of the point in elements[0] is not necessary
        double x = Double.parseDouble(elements[1]);
        double y = Double.parseDouble(elements[2]);
        return vectorFactory.apply(new double[] { x, y });
    }

    private V createVector3D(String line)
    {
        String[] elements = splitLineElements(line, 4);
        // index of the point in elements[0] is not necessary
        double x = Double.parseDouble(elements[1]);
        double y = Double.parseDouble(elements[2]);
        double z = Double.parseDouble(elements[3]);
        return vectorFactory.apply(new double[] { x, y, z });
    }

    private static String[] splitLineElements(String line, int expectedLineElements)
    {
        String[] elements = line.split(" ");
        if (elements.length != expectedLineElements) {
            throw new IllegalArgumentException(
                "Unexpected number of line elements: " + elements.length + " in line: " + line);
        }
        return elements;
    }

    // read of coordinates and graph computation

    /**
     * Read the complete {@link Graph} of node locations and returns true if all locations are
     * distinct.
     */
    private boolean readCompleteNodeGraphData(
        Graph<V, DefaultWeightedEdge> graph, BufferedReader reader,
        Function<String, V> vectorFactory, ToIntBiFunction<V, V> edgeWeightFunction,
        Integer dimension)
        throws IOException
    {
        List<V> nodeCoordinates = readNodeCoordinateData(reader, vectorFactory, dimension);

        buildCompleteGraph(graph, nodeCoordinates, edgeWeightFunction);
        return graph.vertexSet().size() == nodeCoordinates.size();
    }

    private List<V> readNodeCoordinateData(
        BufferedReader reader, Function<String, V> vectorFactory, Integer dimension)
        throws IOException
    {
        List<V> coordinates = dimension != null ? new ArrayList<>(dimension) : new ArrayList<>();

        for (String line; (line = reader.readLine()) != null;) {
            String lineContent = line.trim();
            if ("EOF".equals(lineContent)) {
                break;
            }
            coordinates.add(vectorFactory.apply(lineContent));
        }
        return coordinates;
    }

    private static <V, E> void buildCompleteGraph(
        Graph<V, E> graph, List<V> locations, ToIntBiFunction<V, V> edgeWeightFunction)
    {
        locations.forEach(graph::addVertex);

        new CompleteGraphGenerator<V, E>().generateGraph(graph, null);

        // Compute edge weights:
        // Enable parallel streams. Synchronization not necessary, because DefaultWeightedEdge are
        // IntrusiveEdges and modified directly.

        graph.edgeSet().parallelStream().forEach(e -> {
            V s = graph.getEdgeSource(e);
            V t = graph.getEdgeTarget(e);
            double weight = edgeWeightFunction.applyAsInt(s, t);
            graph.setEdgeWeight(e, weight);
        });
    }

    private List<Integer> readTourData(BufferedReader reader, Integer dimension)
        throws IOException
    {
        List<Integer> tour = dimension != null ? new ArrayList<>(dimension) : new ArrayList<>();

        for (String line; (line = reader.readLine()) != null;) {
            String lineContent = line.trim();
            if ("-1".equals(lineContent)) {
                break;
            }
            tour.add(Integer.valueOf(lineContent));
        }
        return tour;
    }
}
