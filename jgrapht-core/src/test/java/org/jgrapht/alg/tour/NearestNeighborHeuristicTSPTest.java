/*
 * (C) Copyright 2020-2021, by Hannes Wellmann and Contributors.
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
package org.jgrapht.alg.tour;

import org.apache.commons.math3.geometry.euclidean.twod.*;
import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link NearestNeighborHeuristicTSP}.
 * <p>
 * The test data used in this test are designed so that for one vertex, the weight of each touching
 * edges is distinct to the other touching edges of that vertex. This has the intended consequence
 * that for a given first vertex the expected tour computed with the
 * {@code NearestNeighborHeuristic} is unambiguous and the result must never change.
 * </p>
 *
 * @author Hannes Wellmann
 *
 */
public class NearestNeighborHeuristicTSPTest
{
    private static List<Vector2D> locations;
    private static Graph<Vector2D, DefaultWeightedEdge> graph;
    private static List<GraphPath<Vector2D, DefaultWeightedEdge>> expectedTours;

    @BeforeClass
    public static void setUpBeforeClass()
        throws Exception
    {
        List<Vector2D> loc = new ArrayList<>();
        loc.add(new Vector2D(235, 170));
        loc.add(new Vector2D(326, 212));
        loc.add(new Vector2D(215, 430));
        loc.add(new Vector2D(511, 693));
        loc.add(new Vector2D(806, 463));
        loc.add(new Vector2D(504, 62));
        loc.add(new Vector2D(434, 742));
        loc.add(new Vector2D(487, 614));
        loc.add(new Vector2D(719, 147));
        loc.add(new Vector2D(182, 449));
        locations = Collections.unmodifiableList(loc);

        // build complete graph
        Graph<Vector2D, DefaultWeightedEdge> g =
            new SimpleWeightedGraph<>(loc.iterator()::next, DefaultWeightedEdge::new);

        new CompleteGraphGenerator<Vector2D, DefaultWeightedEdge>(loc.size()).generateGraph(g);

        // compute edge weights
        for (DefaultWeightedEdge edge : g.edgeSet()) {
            Vector2D source = g.getEdgeSource(edge);
            Vector2D target = g.getEdgeTarget(edge);
            double weight = source.distance(target);
            g.setEdgeWeight(edge, weight);
        }

        graph = new AsUnmodifiableGraph<>(g);

        // build expected tours:
        // For each of the above specified locations the distances to each other location are
        // different. Therefore for a given start-vertex the resulting tour computed according to
        // the NearestNeighbour heuristic is unambiguous.
        List<GraphPath<Vector2D, DefaultWeightedEdge>> tours = new ArrayList<>();
        tours.add(buildTourPath(new int[] { 0, 1, 5, 8, 4, 7, 3, 6, 2, 9 }, graph, loc));
        tours.add(buildTourPath(new int[] { 1, 0, 2, 9, 7, 3, 6, 4, 8, 5 }, graph, loc));
        tours.add(buildTourPath(new int[] { 2, 9, 1, 0, 5, 8, 4, 7, 3, 6 }, graph, loc));
        tours.add(buildTourPath(new int[] { 3, 7, 6, 2, 9, 1, 0, 5, 8, 4 }, graph, loc));
        tours.add(buildTourPath(new int[] { 4, 8, 5, 1, 0, 2, 9, 7, 3, 6 }, graph, loc));
        tours.add(buildTourPath(new int[] { 5, 8, 4, 7, 3, 6, 2, 9, 1, 0 }, graph, loc));
        tours.add(buildTourPath(new int[] { 6, 3, 7, 2, 9, 1, 0, 5, 8, 4 }, graph, loc));
        tours.add(buildTourPath(new int[] { 7, 3, 6, 2, 9, 1, 0, 5, 8, 4 }, graph, loc));
        tours.add(buildTourPath(new int[] { 8, 5, 1, 0, 2, 9, 7, 3, 6, 4 }, graph, loc));
        tours.add(buildTourPath(new int[] { 9, 2, 1, 0, 5, 8, 4, 7, 3, 6 }, graph, loc));
        expectedTours = Collections.unmodifiableList(tours);
    }

    @Test
    public void testConstructorWithRandomNumberGenerator()
        throws URISyntaxException,
        IOException
    {
        int randomSeed = 0;
        int tours = graph.vertexSet().size();
        // the following order is used in within the heuristic
        List<Vector2D> orderedVertices = new ArrayList<>(graph.vertexSet());

        Random testRnd = new Random(randomSeed);

        HamiltonianCycleAlgorithm<Vector2D, DefaultWeightedEdge> alg =
            new NearestNeighborHeuristicTSP<>(new Random(randomSeed));

        for (int i = 0; i < tours; i++) {
            Vector2D expectedStartVertex = orderedVertices.get(testRnd.nextInt(tours));

            GraphPath<Vector2D, DefaultWeightedEdge> tour = alg.getTour(graph);

            assertStartVertex(tour, expectedStartVertex);
        }
    }

    @Test
    public void testConstructorWithFirst()
    {
        Vector2D first = locations.get(2);
        HamiltonianCycleAlgorithm<Vector2D, DefaultWeightedEdge> alg =
            new NearestNeighborHeuristicTSP<>(first);

        GraphPath<Vector2D, DefaultWeightedEdge> tour = alg.getTour(graph);
        assertStartVertex(tour, first);
    }

    @Test
    public void testConstructorWithInitialVertices()
    {
        List<Vector2D> initalVertices = new ArrayList<>(graph.vertexSet());
        long seed = stringBytesAsLong("JGraphT"); // a fixed seed
        Collections.shuffle(initalVertices, new Random(seed));

        HamiltonianCycleAlgorithm<Vector2D, DefaultWeightedEdge> alg =
            new NearestNeighborHeuristicTSP<>(initalVertices);

        for (Vector2D expectedStartVertex : initalVertices) {
            GraphPath<Vector2D, DefaultWeightedEdge> tour = alg.getTour(graph);
            assertStartVertex(tour, expectedStartVertex);
        }
    }

    @Test
    public void testGetTour()
    {
        for (int i = 0; i < locations.size(); i++) {
            Vector2D startVertex = locations.get(i);
            GraphPath<Vector2D, DefaultWeightedEdge> expectedTour = expectedTours.get(i);

            GraphPath<Vector2D, DefaultWeightedEdge> tour =
                new NearestNeighborHeuristicTSP<Vector2D, DefaultWeightedEdge>(startVertex)
                    .getTour(graph);

            assertThat(tour, is(equalTo(expectedTour)));
        }
    }

    // utilities

    private static <V, E> void assertStartVertex(GraphPath<V, E> tour, V expectedStartVertex)
    {
        assertThat(tour.getStartVertex(), is(sameInstance(expectedStartVertex)));
    }

    private static <V, E> GraphPath<V, E> buildTourPath(
        int[] tourVertexIndices, Graph<V, E> graph, List<V> vertexList)
    {
        List<V> tour = Arrays.stream(tourVertexIndices).mapToObj(vertexList::get).collect(toList());
        tour.add(tour.get(0)); // close path

        double weight = 0;
        for (int i = 1; i < tourVertexIndices.length; i++) {
            E edge = graph.getEdge(tour.get(i - 1), tour.get(i));
            weight += graph.getEdgeWeight(edge);
        }
        return new GraphWalk<>(graph, tour, weight);
    }

    private static long stringBytesAsLong(String str)
    {
        int length = str.length(); // if longer than 8, bytes are lost
        long l = 0;
        for (int i = 0; i < length; i++) {
            l += ((long) str.charAt(length - 1 - i)) << (8 * i);
        }
        return l;
    }
}
