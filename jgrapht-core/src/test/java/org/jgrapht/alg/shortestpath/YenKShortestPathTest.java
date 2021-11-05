/*
 * (C) Copyright 2019-2021, by Semen Chudakov and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the {@link YenKShortestPath}.
 */
public class YenKShortestPathTest
    extends
    BaseKShortestPathTest
{
    /**
     * Seed value which is used to generate random graphs by
     * {@code getRandomGraph(Graph, int, double)} method.
     */
    private static final long SEED = 13l;

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeK()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        Graphs.addEdgeWithVertices(graph, 1, 2);
        new YenKShortestPath<>(graph).getPaths(1, 2, -1);
    }

    /**
     * If k equals to $0$ and there is no paths in the graph between source and target, no exception
     * should be thrown and an empty list should be returned.
     */
    @Test
    public void testKEqualsZero()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        graph.addVertex(2);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths =
            new YenKShortestPath<>(graph).getPaths(1, 2, 0);
        assertEquals(0, paths.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSourceGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(2);
        new YenKShortestPath<>(graph).getPaths(1, 2, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoSinkGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        graph.addVertex(1);
        new YenKShortestPath<>(graph).getPaths(1, 2, 1);
    }

    @Test
    public void testCyclicGraph()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, cyclicGraph3);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths =
            new YenKShortestPath<>(graph).getPaths(1, 3, 1);
        List<Double> weights = Collections.singletonList(2.0);

        assertSameWeights(paths, weights);
    }

    /**
     * If the specified k is greater than the total amount of paths between source and target, a
     * list of all existing paths should be returned and no exception should be thrown.
     */
    @Test
    public void testLessThanKPaths()
    {
        Graph<Integer, DefaultWeightedEdge> graph =
            new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        readGraph(graph, simpleGraph1);
        List<GraphPath<Integer, DefaultWeightedEdge>> paths =
            new YenKShortestPath<>(graph).getPaths(1, 12, 12);
        List<Double> weights =
            Arrays.asList(55.0, 58.0, 59.0, 61.0, 62.0, 64.0, 65.0, 68.0, 68.0, 71.0);

        assertSameWeights(paths, weights);
    }

    @Test
    public void testOnRandomGraphs()
    {
        Random random = new Random(SEED);
        int n = 25;
        double p = 0.1;
        int numberOfRandomEdges = 5;
        for (int i = 0; i < 50; i++) {
            DirectedWeightedPseudograph<Integer, DefaultWeightedEdge> graph =
                new DirectedWeightedPseudograph<>(DefaultWeightedEdge.class);
            graph.setVertexSupplier(SupplierUtil.createIntegerSupplier());
            getRandomGraph(graph, n, p, random);
            Integer source = (int) (random.nextDouble() * n);
            Integer target = (int) (random.nextDouble() * n);
            Set<DefaultWeightedEdge> randomEdges = getRandomEdges(graph, numberOfRandomEdges);
            PathValidator<Integer, DefaultWeightedEdge> pathValidator =
                (path, edge) -> !randomEdges.contains(edge);
            testOnRandomGraph(graph, source, target, pathValidator);
        }
    }

    /**
     * Computes all simple shortest paths between {@code source} and {@code target} without
     * {@code pathValidator}. Then computes all shortest paths between {@code source} and
     * {@code target} with {@code pathValidator}. Finally, checks that only valid paths are returned
     * using {@code isValidPath}.
     *
     *
     * @param graph graph the iterator is being tested on
     * @param source source vertex
     * @param target target vertex
     * @param pathValidator validator for returned paths paths
     */
    private void testOnRandomGraph(
        Graph<Integer, DefaultWeightedEdge> graph, Integer source, Integer target,
        PathValidator<Integer, DefaultWeightedEdge> pathValidator)
    {
        int maximumNumberOfPaths = Integer.MAX_VALUE; // iterate all paths

        List<GraphPath<Integer, DefaultWeightedEdge>> paths =
            new YenKShortestPath<>(graph).getPaths(source, target, maximumNumberOfPaths);
        List<GraphPath<Integer, DefaultWeightedEdge>> validatedPaths =
            new YenKShortestPath<>(graph, pathValidator)
                .getPaths(source, target, maximumNumberOfPaths);

        Set<GraphPath<Integer, DefaultWeightedEdge>> uniquePaths = new HashSet<>();

        int pathsIndex = 0;
        int validatedPathsIndex = 0;
        while (pathsIndex < paths.size() || validatedPathsIndex < validatedPaths.size()) {
            GraphPath<Integer, DefaultWeightedEdge> path = paths.get(pathsIndex);
            if (!isValidPath(path, pathValidator)) {
                ++pathsIndex;
            } else {
                GraphPath<Integer, DefaultWeightedEdge> validatedPath =
                    validatedPaths.get(validatedPathsIndex);

                assertEquals(validatedPath, path);
                ((GraphWalk<Integer, DefaultWeightedEdge>) validatedPath).verify();

                uniquePaths.add(validatedPath);

                ++pathsIndex;
                ++validatedPathsIndex;
            }
        }

        assertEquals(validatedPaths.size(), uniquePaths.size());
    }

    /**
     * Checks that {@code path} is valid w.r.t. the {@code pathValidator}. For the original path
     * $source = v_1, ... ,v_n = target$ for every pair of consecutive intermediate vertices $v_i,
     * v_{i+1}$ checks that the edge $(v_i, v_{i+1})$ can be added to the subpath $v1, ..., v_i$.
     *
     * @param path path to be checked for correctness
     * @param pathValidator validator
     * @return {@code true} iff {@code path} is correct w.r.t. {@code pathValidator}
     */
    private boolean isValidPath(
        GraphPath<Integer, DefaultWeightedEdge> path,
        PathValidator<Integer, DefaultWeightedEdge> pathValidator)
    {
        Graph<Integer, DefaultWeightedEdge> graph = path.getGraph();
        List<Integer> vertices = path.getVertexList();
        List<DefaultWeightedEdge> edges = path.getEdgeList();

        int startVertex = vertices.get(0);
        double weight = 0.0;

        for (int i = 0; i < vertices.size() - 1; ++i) {
            int endVertex = vertices.get(i);
            DefaultWeightedEdge edge = edges.get(i);

            List<Integer> verticesSublist = vertices.subList(0, i + 1);
            List<DefaultWeightedEdge> edgesSublist = edges.subList(0, i);

            GraphPath<Integer, DefaultWeightedEdge> subpath = new GraphWalk<>(
                graph, startVertex, endVertex, verticesSublist, edgesSublist, weight);

            if (!pathValidator.isValidPath(subpath, edge)) {
                return false;
            }

            weight += graph.getEdgeWeight(edge);
        }

        return true;
    }

    /**
     * Generates random graph from the $G(n, p)$ model.
     *
     * @param graph graph instance for the generator
     * @param n the number of nodes
     * @param p the edge probability
     */
    private void getRandomGraph(
        Graph<Integer, DefaultWeightedEdge> graph, int n, double p, Random random)
    {
        GnpRandomGraphGenerator<Integer, DefaultWeightedEdge> generator =
            new GnpRandomGraphGenerator<>(n, p, random, false);
        generator.generateGraph(graph);

        graph.edgeSet().forEach(e -> graph.setEdgeWeight(e, random.nextDouble()));
    }

    /**
     * Computes a set of random vertices of {@code graph}. The size of the set is
     * {@code numberOfEdges}.
     *
     * @param graph a graph
     * @param numberOfEdges number of random vertices
     * @return set of random vertices
     */
    private Set<DefaultWeightedEdge> getRandomEdges(
        Graph<Integer, DefaultWeightedEdge> graph, int numberOfEdges)
    {
        Set<DefaultWeightedEdge> result = CollectionUtil.newHashSetWithExpectedSize(numberOfEdges);
        Object[] edges = graph.edgeSet().toArray();
        Random random = new Random(SEED);
        while (result.size() != numberOfEdges) {
            result.add((DefaultWeightedEdge) edges[random.nextInt(edges.length)]);
        }
        return result;
    }

    /**
     * Checks that {@code paths} has weights identical to {@code weights} in the same order.
     *
     * @param paths graph paths
     * @param weights expected weights
     */
    private void assertSameWeights(
        List<GraphPath<Integer, DefaultWeightedEdge>> paths, List<Double> weights)
    {
        assertEquals(weights.size(), paths.size());
        for (int i = 0; i < paths.size(); i++) {
            assertEquals(weights.get(i), paths.get(i).getWeight(), 1e-9);
        }
    }
}
