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
import org.jgrapht.generate.*;
import org.jgrapht.nio.*;
import org.jgrapht.util.*;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Arrays.asList;

/**
 * Importer for files in the
 * <a href="http://comopt.ifi.uni-heidelberg.de/software/TSPLIB95/">TSPLIB95</a> format.
 * 
 * <p>
 * This importer reads the nodes of a <em>Symmetric travelling salesman problem</em> instance from a
 * file and creates a {@link GraphTests#isComplete(Graph) complete graph} and provides further data
 * from the file and about the imported graph.
 * </p>
 * <p>
 * This implementation does not cover the full TSPLIB95 standard and only implements the subset of
 * capabilities required at the time of creation. All keywords of <em>The specification part</em> in
 * chapter 1.1 of the <em>TSPLIB95</em> standard are considered. Their values can be obtained from
 * the corresponding getters of the {@link Specification}. But only the following
 * <li>EDGE_WEIGHT_TYPE</li> values are supported for a NODE_DATA_SECTION:
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
 * </p>
 * <p>
 * The following data sections of <em>The data part</em> in chapter 1.2 of the <em>TSPLIB95</em>
 * standard are supported:
 * <ul>
 * <li>NODE_COORD_SECTION</li>
 * <li>TOUR_SECTION</li>
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
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 */
public class TSPLIBImporter<V, E>
    implements
    GraphImporter<V, E>
{
    private static final String NAME = "NAME";
    private static final String TYPE = "TYPE";
    private static final String COMMENT = "COMMENT";
    private static final String DIMENSION = "DIMENSION";
    private static final String CAPACITY = "CAPACITY";
    private static final String EDGE_WEIGHT_TYPE = "EDGE_WEIGHT_TYPE";
    private static final String EDGE_WEIGHT_FORMAT = "EDGE_WEIGHT_FORMAT";
    private static final String EDGE_DATA_FORMAT = "EDGE_DATA_FORMAT";
    private static final String NODE_COORD_TYPE = "NODE_COORD_TYPE";
    private static final String DISPLAY_DATA_TYPE = "DISPLAY_DATA_TYPE";

    private static final String NODE_COORD_SECTION = "NODE_COORD_SECTION";
    private static final String TOUR_SECTION = "TOUR_SECTION";

    private static final List<String> VALID_TYPES =
        asList("TSP", "ATSP", "SOP", "HCP", "CVRP", "TOUR");
    private static final List<String> VALID_EDGE_WEIGHT_TYPES = asList(
        "EXPLICIT", "EUC_2D", "EUC_3D", "MAX_2D", "MAX_3D", "MAN_2D", "MAN_3D", "CEIL_2D", "GEO",
        "ATT", "XRAY1", "XRAY2", "SPECIAL");
    private static final List<String> VALID_EDGE_WEIGHT_FORMATS = asList(
        "FUNCTION", "FULL_MATRIX", "UPPER_ROW", "LOWER_ROW", "UPPER_DIAG_ROW", "LOWER_DIAG_ROW",
        "UPPER_COL", "LOWER_COL", "UPPER_DIAG_COL", "LOWER_DIAG_COL");
    private static final List<String> VALID_EDGE_DATA_FORMATS = asList("EDGE_LIST", "ADJ_LIST");
    private static final List<String> VALID_NODE_COORD_TYPES =
        asList("TWOD_COORDS", "THREED_COORDS", "NO_COORDS");
    private static final List<String> VALID_DISPLAY_DATA_TYPE =
        asList("COORD_DISPLAY", "TWOD_DISPLAY", "NO_DISPLAY");

    /**
     * Container for the entry values read from <em>the specification part</em> of a file in
     * <em>TSPLIB95</em> format.
     * 
     * @author Hannes Wellmann
     */
    public static class Specification
    {
        private String name;
        private String type;
        private final List<String> comment = new ArrayList<>();
        private Integer dimension;
        private Integer capacity;
        private String edgeWeightType;
        private String edgeWeightFormat;
        private String edgeDataFormat;
        private String nodeCoordType;
        private String displayDataType;

        Specification()
        {
        }

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
         * Returns the {@link List} of values for the <em>COMMENT</em> keyword in the imported file.
         * 
         * @return the value of the <em>COMMENT</em> keyword
         */
        public List<String> getComments()
        {
            return Collections.unmodifiableList(comment);
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
         * Returns the value of the <em>CAPACITY</em> keyword in the imported file.
         * 
         * @return the value of the <em>CAPACITY</em> keyword
         */
        public Integer getCapacity()
        {
            return capacity;
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
         * Returns the value of the <em>EDGE_WEIGHT_FORMAT</em> keyword in the imported file.
         * 
         * @return the value of the <em>EDGE_WEIGHT_FORMAT</em> keyword
         */
        public String getEdgeWeightFormat()
        {
            return edgeWeightFormat;
        }

        /**
         * Returns the value of the <em>EDGE_DATA_FORMAT</em> keyword in the imported file.
         * 
         * @return the value of the <em>EDGE_DATA_FORMAT</em> keyword
         */
        public String getEdgeDataFormat()
        {
            return edgeDataFormat;
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
         * Returns the value of the <em>DISPLAY_DATA_TYPE</em> keyword in the imported file.
         * 
         * @return the value of the <em>DISPLAY_DATA_TYPE</em> keyword
         */
        public String getDisplayDataType()
        {
            return displayDataType;
        }
    }

    /**
     * Container for the meta data of an imported <em>TSPLIB95</em> file.
     * 
     * @author Hannes Wellmann
     * @param <V> the graph vertex type
     * @param <E> the graph edge type
     */
    public static class Metadata<V, E>
    {
        private final Specification spec = new Specification();
        private Map<V, Node> vertex2node;
        private Graph<V, E> graph;
        private List<V> tour;

        private Boolean hasDistinctLocations;
        private Boolean hasDistinctNeighborDistances;

        private Metadata()
        {
        }

        /**
         * Returns the {@link Specification} instance containing all values from the specification
         * part of a <em>TSPLIB95</em> file.
         * 
         * @return the {@code Specification} of an imported <em>TSPLIB95</em> file
         */
        public Specification getSpecification()
        {
            return spec;
        }

        /**
         * Returns the mapping of vertex to corresponding node imported from the
         * <em>NODE_COORD_SECTION</em> of a <em>TSPLIB95</em> file.
         * 
         * @return the mapping of vertex to corresponding node
         */
        public Map<V, Node> getVertexToNodeMapping()
        {
            return vertex2node;
        }

        /**
         * Returns the {@link List} of vertices in the order of the tour defined in an imported
         * <em>TSPLIB95</em> file or null if no tour was imported.
         * <p>
         * Note that a tour can be imported by {@link TSPLIBImporter#importGraph(Graph, Reader)} or
         * {@link TSPLIBImporter#importTour(Metadata, Reader)} .
         * </p>
         * 
         * @return the vertex tour from the file or null
         */
        public List<V> getTour()
        {
            return tour;
        }

        /**
         * Returns true if for the imported graph all vertices have distinct coordinates and non of
         * them have {@link Arrays#equals(Object) equal} {@link Node#getCoordinates() coordinate
         * values} , else false.
         * 
         * @return true if no equally located nodes were imported from the file, else false
         * @throws IllegalStateException if no graph was imported
         */
        public boolean hasDistinctNodeLocations()
        {
            if (graph == null) {
                throw new IllegalStateException("No graph imported");
            }
            if (hasDistinctLocations == null) {
                hasDistinctLocations = Boolean.TRUE;

                Set<List<Double>> distinctCoordinates =
                    CollectionUtil.newHashSetWithExpectedSize(vertex2node.size());
                for (Node node : vertex2node.values()) {
                    double[] coordinates = node.getCoordinates();
                    // Arrays.equals checks identity. Conversion to a List<Double> and use of a
                    // HashSet has linear runtime. Unlike with a TreeSet using a comparator.
                    Double[] coordinateObj = new Double[coordinates.length];
                    for (int i = 0; i < coordinates.length; i++) {
                        coordinateObj[i] = Double.valueOf(coordinates[i]);
                    }
                    if (!distinctCoordinates.add(Arrays.asList(coordinateObj))) {
                        hasDistinctLocations = Boolean.FALSE;
                        return hasDistinctLocations;
                    }
                }
            }
            return hasDistinctLocations;
        }

        /**
         * Returns true if for the imported graph each vertex all touching edges have different
         * weights.
         * <p>
         * If this method returns true this means for the TSP that for each location each other
         * location has a different distance, so there are no two other locations that have the same
         * distance from that location.
         * </p>
         * 
         * @return true if all touching edges of each vertex have different weight, else false
         * @throws IllegalStateException if no graph was imported
         */
        public boolean hasDistinctNeighborDistances()
        {
            if (graph == null) {
                throw new IllegalStateException("No graph imported");
            }
            if (hasDistinctNeighborDistances == null) {
                hasDistinctNeighborDistances = Boolean.TRUE;

                Set<V> vertices = graph.vertexSet(); // each vertex has vertices.size()-1 edges
                Set<Double> weights =
                    CollectionUtil.newHashSetWithExpectedSize(vertices.size() - 1);
                for (V v : vertices) {
                    weights.clear();
                    for (E edge : graph.edgesOf(v)) {
                        if (!weights.add(graph.getEdgeWeight(edge))) {
                            hasDistinctNeighborDistances = Boolean.FALSE;
                            return hasDistinctNeighborDistances;
                        }
                    }
                }
            }
            return hasDistinctNeighborDistances;
        }
    }

    /**
     * A node imported from the <em>NODE_COORD_SECTION</em> of a <em>TSPLIB95</em>-file.
     * 
     * @author Hannes Wellmann
     */
    public static class Node
    {
        /** The one based number of this node. */
        private final int number;
        /** The coordinates of this node. */
        private final double[] coordinates;

        Node(int number, double[] coordinates)
        {
            this.number = number;
            this.coordinates = coordinates;
        }

        /**
         * Returns the number of this node as specified in the source <em>TSPLIB95</em>-file.
         * 
         * @return the number of this node
         */
        public int getNumber()
        {
            return number;
        }

        /**
         * Returns the number of elements the coordinates of this node have (either two or three).
         * 
         * @return the number of coordinate elements of this node
         */
        public int getCoordinatesLength()
        {
            return coordinates.length;
        }

        /**
         * Returns the value of the coordinate element with zero-based index <em>i</em> of this
         * node.
         * 
         * @param i the index of the coordinate element
         * @return the value of the <em>i-th</em> coordinate element
         */
        public double getCoordinateValue(int i)
        {
            return coordinates[i];
        }

        /**
         * Returns a copy of the coordinates of this node.
         * 
         * @return the coordinates of this node
         */
        public double[] getCoordinates()
        {
            return Arrays.copyOf(coordinates, coordinates.length);
        }

        @Override
        public String toString()
        {
            return number + " " + Arrays
                .stream(coordinates).mapToObj(Double::toString).collect(Collectors.joining(" "));
        }
    }

    private int vectorLength = -1;
    private Metadata<V, E> metadata;

    /** Constructs a new importer. */
    public TSPLIBImporter()
    { // NoOp
    }

    /**
     * Returns the {@link Metadata} of the latest imported file or null, if no import completed yet
     * or the latest import failed.
     * 
     * @return {@code TSPLIBFileData} of the latest import
     */
    public Metadata<V, E> getMetadata()
    {
        return metadata;
    }

    // read of node data section

    /**
     * {@inheritDoc}
     * <p>
     * The given {@link Graph} must be weighted. Also the graph should be empty, otherwise the
     * behavior is unspecified which could lead to exceptions.
     * </p>
     * <p>
     * The source of the given Reader should contain a <em>NODE_COORD_SECTION</em> (if not the graph
     * is not changed) and can contain a <em>TOUR_SECTION</em>. If a <em>TOUR_SECTION</em> is
     * present a corresponding <em>NODE_COORD_SECTION</em> is mandatory and the read vertex numbers
     * are referred to the <em>NODE_COORD_SECTION</em> in the same source.
     * </p>
     * <p>
     * {@link Metadata} of the import can be obtained with {@link #getMetadata()} after this method
     * returns. If the readers source contains a <em>TOUR_SECTION</em> the imported tour can be
     * obtained from {@link Metadata#getTour()}.
     * </p>
     * <p>
     * This implementation is not thread-safe and must be synchronized externally if called by
     * concurrent threads.
     * </p>
     * 
     * @param graph the graph into which this importer writes, must weighted.
     * @throws IllegalArgumentException if the specified {@code graph} is not weighted
     */
    @Override
    public void importGraph(Graph<V, E> graph, Reader in)
    {
        metadata = null;
        try {
            Iterator<String> lines = getLineIterator(in);
            metadata = readContentForGraph(lines, graph);
        } catch (Exception e) {
            throw getImportException(e, "graph");
        }
    }

    private Metadata<V, E> readContentForGraph(Iterator<String> lines, Graph<V, E> graph)
    {
        if (!graph.getType().isWeighted()) {
            throw new IllegalArgumentException("Graph must be weighted");
        }
        vectorLength = -1;
        Metadata<V, E> data = new Metadata<>();
        List<Integer> tour = null;

        while (lines.hasNext()) {
            String[] keyValue = lines.next().split(":");
            String key = getKey(keyValue);

            if (readSpecificationSection(key, data.spec, keyValue)) {
                // some specification element was read. Continue with next line.

            } else if (NODE_COORD_SECTION.equals(key)) {
                requireNotSet(data.graph, NODE_COORD_SECTION);
                data.graph = graph;
                data.vertex2node = readNodeCoordinateSection(lines, data);

            } else if (TOUR_SECTION.equals(key)) {
                requireNotSet(tour, TOUR_SECTION);
                tour = readTourSection(lines, data.spec.dimension);
            }
        }
        if (tour != null) {
            data.tour = getVertexTour(tour, data.vertex2node);
        }
        return data;
    }

    /**
     * Reads all nodes of the NODE_COORD_SECTION and fills the graph of the data accordingly.
     * 
     * @return a mapping from created graph {@link V vertex} to corresponding imported {@link Node}
     */
    private Map<V, Node> readNodeCoordinateSection(Iterator<String> lines, Metadata<V, E> data)
    {
        requireSet(data.spec.edgeWeightType, NODE_COORD_SECTION);
        requireSet(data.spec.dimension, DIMENSION); // DIMENSION specifies the number of nodes

        ToIntBiFunction<Node, Node> edgeWeightFunction =
            getEdgeWeightFunction(data.spec.edgeWeightType);

        List<Node> nodes = readNodes(lines, data.spec.dimension);

        // create vertices for all imported nodes
        Map<V, Node> vertex2node = CollectionUtil.newHashMapWithExpectedSize(nodes.size());
        Graph<V, E> graph = data.graph;
        for (Node node : nodes) {
            V v = graph.addVertex();
            vertex2node.put(v, node);
        }

        // create edges for each possible pair of vertices and compute their weights
        new CompleteGraphGenerator<V, E>().generateGraph(graph, null);

        graph.edgeSet().forEach(e -> {
            Node s = vertex2node.get(graph.getEdgeSource(e));
            Node t = vertex2node.get(graph.getEdgeTarget(e));

            double weight = edgeWeightFunction.applyAsInt(s, t);
            graph.setEdgeWeight(e, weight);
        });
        return Collections.unmodifiableMap(vertex2node);
    }

    private ToIntBiFunction<Node, Node> getEdgeWeightFunction(String edgeWeightType)
    {
        switch (edgeWeightType) {
        case "EUC_2D":
            vectorLength = 2;
            return this::computeEuclideanDistance;

        case "EUC_3D":
            vectorLength = 3;
            return this::computeEuclideanDistance;

        case "MAX_2D":
            vectorLength = 2;
            return this::computeMaximumDistance;

        case "MAX_3D":
            vectorLength = 3;
            return this::computeMaximumDistance;

        case "MAN_2D":
            vectorLength = 2;
            return this::computeManhattanDistance;

        case "MAN_3D":
            vectorLength = 3;
            return this::computeManhattanDistance;

        case "CEIL_2D":
            vectorLength = 2;
            return this::compute2DCeilingEuclideanDistance;

        case "GEO":
            vectorLength = 2;
            return this::compute2DGeographicalDistance;

        case "ATT":
            vectorLength = 2;
            return this::compute2DPseudoEuclideanDistance;

        default:
            throw new IllegalStateException(
                "Unsupported EDGE_WEIGHT_TYPE <" + edgeWeightType + ">");
        }
    }

    private List<Node> readNodes(Iterator<String> lines, int dimension)
    {
        List<Node> nodes = new ArrayList<>(dimension);
        for (int i = 0; i < dimension && lines.hasNext(); i++) {
            String line = lines.next();
            Node node = parseNode(line);
            nodes.add(node);
        }
        return nodes;
    }

    private Node parseNode(String line)
    {
        String[] elements = line.split(" ");
        if (elements.length != vectorLength + 1) {
            throw new IllegalArgumentException(
                "Unexpected number of elements <" + elements.length + "> in line: " + line);
        }
        int number = Integer.parseInt(elements[0]);
        double[] coordinates =
            Arrays.stream(elements, 1, elements.length).mapToDouble(Double::parseDouble).toArray();

        return new Node(number, coordinates);
    }

    // read of tour data section

    /**
     * Imports a tour described by a {@link List} of {@link V vertices} using the given Reader.
     * <p>
     * It is the callers responsibility to ensure the {@code Reader} is closed after this method
     * returned.
     * </p>
     * <p>
     * The source of the given Reader should contain a <em>TOUR_SECTION</em> (if not null is
     * returned). The vertices specified by their number in the <em>TOUR_SECTION</em> are referred
     * to the nodes respectively vertices in the given {@code metadata}.
     * </p>
     * <p>
     * The {@link Metadata} of the import can be obtained with {@link #getMetadata()} after this
     * method returns. The {@code Metadata#getVertexToNodeMapping() vertexToNodeMapping} in the
     * metadata of this import is the same as in the given {@code metadata}.
     * </p>
     * <p>
     * This implementation is not thread-safe and must be synchronized externally if called by
     * concurrent threads.
     * </p>
     * 
     * @param referenceMetadata the {@code Metadata} defining the available vertices and their
     *        {@code Nodes}.
     * @param in the input reader
     * @return the imported tour or null, if no tour was imported
     */
    public List<V> importTour(Metadata<V, E> referenceMetadata, Reader in)
    {
        metadata = null;
        try {
            Iterator<String> lines = getLineIterator(in);
            metadata = readContentForTour(lines, referenceMetadata.vertex2node);
            return metadata.tour;
        } catch (Exception e) {
            throw getImportException(e, "tour");
        }
    }

    private Metadata<V, E> readContentForTour(Iterator<String> lines, Map<V, Node> vertex2node)
    {
        Metadata<V, E> data = new Metadata<>();

        while (lines.hasNext()) {
            String[] keyValue = lines.next().split(":");
            String key = getKey(keyValue);

            if (readSpecificationSection(key, data.spec, keyValue)) {
                // some specification element was read. Continue with next line.

            } else if (TOUR_SECTION.equals(key)) {
                requireNotSet(data.tour, TOUR_SECTION);
                List<Integer> tour = readTourSection(lines, data.spec.dimension);
                data.tour = getVertexTour(tour, vertex2node);
            }
        }
        data.vertex2node = vertex2node;
        return data;
    }

    /**
     * Reads a tour of the TOUR_SECTION and returns the List of ordered vertex numbers describing
     * the tour.
     * 
     * @return the list of vertex number describing the tour
     */
    private List<Integer> readTourSection(Iterator<String> lines, Integer dimension)
    {
        List<Integer> tour = dimension != null ? new ArrayList<>(dimension) : new ArrayList<>();

        while (lines.hasNext()) {
            String lineContent = lines.next();
            if ("-1".equals(lineContent)) {
                break;
            }
            tour.add(Integer.valueOf(lineContent));
        }
        return tour;
    }

    private List<V> getVertexTour(List<Integer> tour, Map<V, Node> vertex2node)
    {
        requireSet(vertex2node, TOUR_SECTION);
        List<V> orderedVertices = getOrderedVertices(vertex2node);

        List<V> vertexTour = new ArrayList<>(orderedVertices.size());
        for (Integer vertexNumber : tour) { // number may be zero or one based (its more a id)
            V v = vertexNumber < orderedVertices.size() ? orderedVertices.get(vertexNumber) : null;
            if (v == null) {
                throw new IllegalStateException("Missing vertex with number " + vertexNumber);
            }
            vertexTour.add(v);
        }
        return vertexTour;
    }

    private List<V> getOrderedVertices(Map<V, Node> vertex2node)
    {
        int maxNumber = vertex2node.values().stream().mapToInt(Node::getNumber).max().getAsInt();
        @SuppressWarnings("unchecked") V[] orderedVertices = (V[]) new Object[maxNumber + 1];
        vertex2node.forEach((v, n) -> orderedVertices[n.number] = v);
        return asList(orderedVertices);
    }

    // read of specification

    private boolean readSpecificationSection(String key, Specification spec, String[] lineElements)
    {
        // only read value if it is sure that there should be a value
        switch (key) {
        case NAME:
            requireNotSet(spec.name, NAME);
            spec.name = getValue(lineElements);
            return true;

        case TYPE:
            requireNotSet(spec.type, TYPE);
            String type = getValue(lineElements);
            spec.type = requireValidValue(type, VALID_TYPES, TYPE);
            return true;

        case COMMENT:
            String comment = getValue(lineElements);
            spec.comment.add(comment);
            return true;

        case DIMENSION:
            requireNotSet(spec.dimension, DIMENSION);
            String dimension = getValue(lineElements);
            spec.dimension = parseInteger(dimension, DIMENSION);
            return true;

        case CAPACITY:
            requireNotSet(spec.capacity, CAPACITY);
            String capacity = getValue(lineElements);
            spec.capacity = parseInteger(capacity, CAPACITY);
            return true;

        case EDGE_WEIGHT_TYPE:
            requireNotSet(spec.edgeWeightType, EDGE_WEIGHT_TYPE);
            String edgeWeightType = getValue(lineElements);
            spec.edgeWeightType =
                requireValidValue(edgeWeightType, VALID_EDGE_WEIGHT_TYPES, EDGE_WEIGHT_TYPE);
            return true;

        case EDGE_WEIGHT_FORMAT:
            requireNotSet(spec.edgeWeightFormat, EDGE_WEIGHT_FORMAT);
            String edgeWeightFormat = getValue(lineElements);
            spec.edgeWeightFormat =
                requireValidValue(edgeWeightFormat, VALID_EDGE_WEIGHT_FORMATS, EDGE_WEIGHT_FORMAT);
            return true;

        case EDGE_DATA_FORMAT:
            requireNotSet(spec.edgeDataFormat, EDGE_DATA_FORMAT);
            String edgeDataFormat = getValue(lineElements);
            spec.edgeDataFormat =
                requireValidValue(edgeDataFormat, VALID_EDGE_DATA_FORMATS, EDGE_DATA_FORMAT);
            return true;

        case NODE_COORD_TYPE:
            requireNotSet(spec.nodeCoordType, NODE_COORD_TYPE);
            String nodeCoordType = getValue(lineElements);
            spec.nodeCoordType =
                requireValidValue(nodeCoordType, VALID_NODE_COORD_TYPES, NODE_COORD_TYPE);
            return true;

        case DISPLAY_DATA_TYPE:
            requireNotSet(spec.displayDataType, DISPLAY_DATA_TYPE);
            String displayDataType = getValue(lineElements);
            spec.displayDataType =
                requireValidValue(displayDataType, VALID_DISPLAY_DATA_TYPE, DISPLAY_DATA_TYPE);
            return true;

        default:
            return false;
        }
    }

    private String requireValidValue(String value, List<String> validValues, String valueType)
    {
        for (String validValue : validValues) {
            if (validValue.equalsIgnoreCase(value)) {
                return validValue; // always use the upper case version
            }
        }
        throw new IllegalArgumentException("Invalid " + valueType + " value <" + value + ">");
    }

    private Integer parseInteger(String valueStr, String valueType)
    {
        try {
            return Integer.valueOf(valueStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "Invalid " + valueType + " integer value <" + valueStr + ">", e);
        }
    }

    // read utilities

    private static Iterator<String> getLineIterator(Reader in)
    {
        BufferedReader reader = new BufferedReader(in);
        return Stream.iterate(readLine(reader), Objects::nonNull, l -> readLine(reader)).iterator();
    }

    private static String readLine(BufferedReader reader)
    {
        try {
            String line = reader.readLine();
            if (line != null) {
                line = line.trim();
                return "EOF".equals(line) ? null : line;
            }
            return null;
        } catch (IOException e) {
            throw new IllegalStateException("I/O exception while reading line of TSPLIB file", e);
        }
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

    private void requireSet(Object requirement, String target)
    {
        if (requirement == null) {
            throw new IllegalStateException("Missing data to read <" + target + ">");
        }
    }

    private static ImportException getImportException(Exception e, String target)
    {
        return new ImportException(
            "Failed to import " + target + " from TSPLIB-file: " + e.getMessage(), e);
    }

    // distance computations

    // all of the following methods are implemented in accordance to
    // section "2. The distance functions" of TSPLIB95

    /**
     * Computes the distance of the two nodes n1 and n2 according to the {@code EUC_2D} or
     * {@code EUC_3D} metric depending on their dimension. The used metric is also known as L2-norm.
     * 
     * @param n1 a {@code Node} with two or three dimensional coordinates
     * @param n2 a {@code Node} with two or three dimensional coordinates
     * @return the {@code EUC_2D} or {@code EUC_3D} edge weight for nodes n1 and n2
     */
    int computeEuclideanDistance(Node n1, Node n2)
    { // according to TSPLIB95 distances are rounded to next integer value
        return (int) Math.round(getL2Distance(n1, n2));
    }

    /**
     * Computes the distance of the two nodes n1 and n2 according to the {@code MAX_2D} or
     * {@code MAX_3D} metric depending on their dimension. The used metric is also known as
     * L&infin;-norm.
     * 
     * @param n1 a {@code Node} with two or three dimensional coordinates
     * @param n2 a {@code Node} with two or three dimensional coordinates
     * @return the {@code MAX_2D} or {@code MAX_3D} edge weight for nodes n1 and n2
     */
    int computeMaximumDistance(Node n1, Node n2)
    { // according to TSPLIB95 distances are rounded to next integer value
        return (int) Math.round(getLInfDistance(n1, n2));
    }

    /**
     * Computes the distance of the two nodes n1 and n2 according to the {@code MAN_2D} or
     * {@code MAN_3D} metric depending on their dimension. The used metric is also known as L1-norm.
     * 
     * @param n1 a {@code Node} with two or three dimensional coordinates
     * @param n2 a {@code Node} with two or three dimensional coordinates
     * @return the {@code MAN_2D} or {@code MAN_3D} edge weight for nodes n1 and n2
     */
    int computeManhattanDistance(Node n1, Node n2)
    { // according to TSPLIB95 distances are rounded to next integer value
        return (int) Math.round(getL1Distance(n1, n2));
    }

    /**
     * Computes the distance of the two nodes n1 and n2 according to the {@code CEIL_2D} metric, the
     * round up version of {@code EUC_2D}. The points must have dimension two.
     * 
     * @param n1 a {@code Node} with two or three dimensional coordinates
     * @param n2 a {@code Node} with two or three dimensional coordinates
     * @return the {@code CEIL_2D} edge weight for nodes n1 and n2
     * @see #computeEuclideanDistance(RealVector, RealVector)
     */
    int compute2DCeilingEuclideanDistance(Node n1, Node n2)
    {
        return (int) Math.ceil(getL2Distance(n1, n2));
    }

    /**
     * Computes the distance of the two nodes n1 and n2 according to the {@code GEO} metric. The
     * used metric computes the distance between two points on a earth-like sphere, while the point
     * coordinates describe their geographical latitude and longitude. The points must have
     * dimension two.
     * 
     * @param n1 a {@code Node} with two or three dimensional coordinates
     * @param n2 a {@code Node} with two or three dimensional coordinates
     * @return the {@code GEO} edge weight for nodes n1 and n2
     */
    int compute2DGeographicalDistance(Node n1, Node n2)
    {
        double latitude1 = computeRadiansAngle(n1.getCoordinateValue(0));
        double longitude1 = computeRadiansAngle(n1.getCoordinateValue(1));

        double latitude2 = computeRadiansAngle(n2.getCoordinateValue(0));
        double longitude2 = computeRadiansAngle(n2.getCoordinateValue(1));

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
     * Computes the distance of two the two nodes n1 and n2 according to the {@code ATT} metric. The
     * nodes must have two dimensional coordinates.
     * 
     * @param n1 a {@code Node} with two dimensional coordinates
     * @param n2 a {@code Node} with two dimensional coordinates
     * @return the {@code ATT} edge weight for nodes n1 and n2
     */
    int compute2DPseudoEuclideanDistance(Node n1, Node n2)
    {
        double xd = n1.getCoordinateValue(0) - n2.getCoordinateValue(0);
        double yd = n1.getCoordinateValue(1) - n2.getCoordinateValue(1);
        double rij = Math.sqrt((xd * xd + yd * yd) / 10.0);
        double tij = Math.round(rij);
        if (tij < rij) {
            return (int) (tij + 1);
        } else {
            return (int) tij;
        }
    }

    private double getL1Distance(Node n1, Node n2)
    {
        double elementSum = 0;
        for (int i = 0; i < vectorLength; i++) {
            double delta = n1.getCoordinateValue(i) - n2.getCoordinateValue(i);
            elementSum += Math.abs(delta);
        }
        return elementSum;
    }

    private double getL2Distance(Node n1, Node n2)
    {
        double elementSum = 0;
        for (int i = 0; i < vectorLength; i++) {
            double delta = n1.getCoordinateValue(i) - n2.getCoordinateValue(i);
            elementSum += delta * delta;
        }
        return Math.sqrt(elementSum);
    }

    private double getLInfDistance(Node n1, Node n2)
    {
        double maxElement = 0;
        for (int i = 0; i < vectorLength; i++) {
            double delta = n1.getCoordinateValue(i) - n2.getCoordinateValue(i);
            maxElement = Math.max(maxElement, Math.abs(delta));
        }
        return maxElement;
    }
}
