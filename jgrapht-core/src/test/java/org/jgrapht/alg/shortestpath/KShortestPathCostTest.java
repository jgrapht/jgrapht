/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
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
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

/**
 */
public class KShortestPathCostTest
{
    // ~ Methods ----------------------------------------------------------------

    @Test
    public void testKShortestPathCompleteGraph4()
    {
        int nbPaths = 5;

        KShortestPathCompleteGraph4 graph = new KShortestPathCompleteGraph4();

        KShortestSimplePaths<String, DefaultWeightedEdge> pathFinder =
            new KShortestSimplePaths<>(graph);
        List<GraphPath<String, DefaultWeightedEdge>> pathElements =
            pathFinder.getPaths("vS", "v3", nbPaths);

        assertEquals(
            "[[(vS : v1), (v1 : v3)], [(vS : v2), (v2 : v3)],"
                + " [(vS : v2), (v1 : v2), (v1 : v3)], " + "[(vS : v1), (v1 : v2), (v2 : v3)], "
                + "[(vS : v3)]]",
            pathElements.toString());

        assertEquals(5, pathElements.size(), 0);
        GraphPath<String, DefaultWeightedEdge> pathElement = pathElements.get(0);
        assertEquals(2, pathElement.getWeight(), 0);

        assertEquals(
            Arrays.asList(new Object[] { graph.eS1, graph.e13 }), pathElement.getEdgeList());
    }

    @Test
    public void testPicture1Graph()
    {
        Picture1Graph picture1Graph = new Picture1Graph();

        int maxSize = 10;

        KShortestSimplePaths<String, DefaultWeightedEdge> pathFinder =
            new KShortestSimplePaths<>(picture1Graph);

        // assertEquals(2, pathFinder.getPaths("v5").size());

        List<GraphPath<String, DefaultWeightedEdge>> pathElements =
            pathFinder.getPaths("vS", "v5", maxSize);
        GraphPath<String, DefaultWeightedEdge> pathElement = pathElements.get(0);
        assertEquals(
            Arrays.asList(new Object[] { picture1Graph.eS1, picture1Graph.e15 }),
            pathElement.getEdgeList());

        List<String> vertices = pathElement.getVertexList();
        assertEquals(Arrays.asList(new Object[] { "vS", "v1", "v5" }), vertices);

        pathElement = pathElements.get(1);
        assertEquals(
            Arrays.asList(new Object[] { picture1Graph.eS2, picture1Graph.e25 }),
            pathElement.getEdgeList());

        vertices = pathElement.getVertexList();
        assertEquals(Arrays.asList(new Object[] { "vS", "v2", "v5" }), vertices);

        pathElements = pathFinder.getPaths("vS", "v7", maxSize);
        pathElement = pathElements.get(0);
        double lastCost = pathElement.getWeight();
        for (int i = 0; i < pathElements.size(); i++) {
            pathElement = pathElements.get(i);
            double cost = pathElement.getWeight();

            assertTrue(lastCost <= cost);
            lastCost = cost;
        }
    }

    @Test
    public void testShortestPathsInIncreasingOrder()
    {
        BiconnectedGraph biconnectedGraph = new BiconnectedGraph();
        verifyShortestPathsInIncreasingOrderOfWeight(biconnectedGraph);

        KShortestPathCompleteGraph4 kSPCompleteGraph4 = new KShortestPathCompleteGraph4();
        verifyShortestPathsInIncreasingOrderOfWeight(kSPCompleteGraph4);

        KShortestPathCompleteGraph5 kSPCompleteGraph5 = new KShortestPathCompleteGraph5();
        verifyShortestPathsInIncreasingOrderOfWeight(kSPCompleteGraph5);

        KShortestPathCompleteGraph6 kSPCompleteGraph6 = new KShortestPathCompleteGraph6();
        verifyShortestPathsInIncreasingOrderOfWeight(kSPCompleteGraph6);

        KSPExampleGraph kSPExampleGraph = new KSPExampleGraph();
        verifyShortestPathsInIncreasingOrderOfWeight(kSPExampleGraph);

        NotBiconnectedGraph notBiconnectedGraph = new NotBiconnectedGraph();
        verifyShortestPathsInIncreasingOrderOfWeight(notBiconnectedGraph);

        Picture1Graph picture1Graph = new Picture1Graph();
        verifyShortestPathsInIncreasingOrderOfWeight(picture1Graph);
    }

    @Test
    public void testShortestPathsWeightsWithMaxSizeIncreases()
    {
        BiconnectedGraph biconnectedGraph = new BiconnectedGraph();
        verifyShortestPathsWeightsWithMaxSizeIncreases(biconnectedGraph);

        KShortestPathCompleteGraph4 kSPCompleteGraph4 = new KShortestPathCompleteGraph4();
        verifyShortestPathsWeightsWithMaxSizeIncreases(kSPCompleteGraph4);

        KShortestPathCompleteGraph5 kSPCompleteGraph5 = new KShortestPathCompleteGraph5();
        verifyShortestPathsWeightsWithMaxSizeIncreases(kSPCompleteGraph5);

        KShortestPathCompleteGraph6 kSPCompleteGraph6 = new KShortestPathCompleteGraph6();
        verifyShortestPathsWeightsWithMaxSizeIncreases(kSPCompleteGraph6);

        KSPExampleGraph kSPExampleGraph = new KSPExampleGraph();
        verifyShortestPathsWeightsWithMaxSizeIncreases(kSPExampleGraph);

        NotBiconnectedGraph notBiconnectedGraph = new NotBiconnectedGraph();
        verifyShortestPathsWeightsWithMaxSizeIncreases(notBiconnectedGraph);

        Picture1Graph picture1Graph = new Picture1Graph();
        verifyShortestPathsWeightsWithMaxSizeIncreases(picture1Graph);
    }

    private <E> void verifyShortestPathsInIncreasingOrderOfWeight(Graph<String, E> graph)
    {
        int maxSize = 20;

        for (String sourceVertex : graph.vertexSet()) {
            for (String targetVertex : graph.vertexSet()) {
                if (targetVertex != sourceVertex) {
                    KShortestSimplePaths<String, E> pathFinder = new KShortestSimplePaths<>(graph);

                    List<GraphPath<String, E>> pathElements =
                        pathFinder.getPaths(sourceVertex, targetVertex, maxSize);
                    if (pathElements.isEmpty()) {
                        // no path exists between the start vertex and the end
                        // vertex
                        continue;
                    }
                    GraphPath<String, E> pathElement = pathElements.get(0);
                    double lastWeight = pathElement.getWeight();
                    for (int i = 0; i < pathElements.size(); i++) {
                        pathElement = pathElements.get(i);
                        double weight = pathElement.getWeight();
                        assertTrue(lastWeight <= weight);
                        lastWeight = weight;
                    }
                    assertTrue(pathElements.size() <= maxSize);
                }
            }
        }
    }

    private <E> void verifyShortestPathsWeightsWithMaxSizeIncreases(Graph<String, E> graph)
    {
        int maxSizeLimit = 10;

        for (String sourceVertex : graph.vertexSet()) {
            for (String targetVertex : graph.vertexSet()) {
                if (targetVertex != sourceVertex) {
                    KShortestSimplePaths<String, E> pathFinder = new KShortestSimplePaths<>(graph);
                    List<GraphPath<String, E>> prevPathElementsResults =
                        pathFinder.getPaths(sourceVertex, targetVertex, 1);

                    if (prevPathElementsResults.isEmpty()) {
                        // no path exists between the start vertex and the
                        // end vertex
                        continue;
                    }

                    for (int maxSize = 2; maxSize < maxSizeLimit; maxSize++) {
                        pathFinder = new KShortestSimplePaths<>(graph);
                        List<GraphPath<String, E>> pathElementsResults =
                            pathFinder.getPaths(sourceVertex, targetVertex, maxSize);

                        verifyWeightsConsistency(prevPathElementsResults, pathElementsResults);
                    }
                }
            }
        }
    }

    /**
     * Verify weights consistency between the results when the max-size argument increases.
     *
     * @param prevPathElementsResults results obtained with a max-size argument equal to
     *        <code>k</code>
     * @param pathElementsResults results obtained with a max-size argument equal to
     *        <code>k+1</code>
     */
    private <E> void verifyWeightsConsistency(
        List<GraphPath<String, E>> prevPathElementsResults,
        List<GraphPath<String, E>> pathElementsResults)
    {
        for (int i = 0; i < prevPathElementsResults.size(); i++) {
            GraphPath<String, E> pathElementResult = pathElementsResults.get(i);
            GraphPath<String, E> prevPathElementResult = prevPathElementsResults.get(i);
            assertTrue(pathElementResult.getWeight() == prevPathElementResult.getWeight());
        }
    }

    /**
     * Currently disabled since it takes more than a few seconds to run. Also need to actually check
     * the output instead of printing.
     *
     * @see <a href=
     *      "http://jgrapht-users.107614.n3.nabble.com/quot-graph-must-contain-the-start-vertex-quot-when-running-KShortestPaths-td4024797.html">bug
     *      description</a>.
     */
    public void _testIllegalArgumentExceptionGraphNotThrown()
        throws Exception
    {
        SimpleWeightedGraph<String, DefaultWeightedEdge> graph =
            new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        InputStream fstream = getClass().getClassLoader().getResourceAsStream("edges.txt");
        BufferedReader in = new BufferedReader(new InputStreamReader(fstream));

        String[] edgeText;
        DefaultWeightedEdge ed;
        String line = in.readLine();
        while (line != null) {
            edgeText = line.split("\t");

            graph.addVertex(edgeText[0]);
            graph.addVertex(edgeText[1]);
            ed = graph.addEdge(edgeText[0], edgeText[1]);
            graph.setEdgeWeight(ed, Double.parseDouble(edgeText[2]));

            line = in.readLine();
        }

        // Close the input stream
        in.close();

        DefaultWeightedEdge src = graph.getEdge("M013", "M014");

        KShortestSimplePaths<String, DefaultWeightedEdge> kPaths =
            new KShortestSimplePaths<>(graph);
        List<GraphPath<String, DefaultWeightedEdge>> paths;

        try {
            paths = kPaths.getPaths(graph.getEdgeSource(src), graph.getEdgeTarget(src), 5);
            for (GraphPath<String, DefaultWeightedEdge> path : paths) {
                for (DefaultWeightedEdge edge : path.getEdgeList()) {
                    System.out.print(
                        "<" + graph.getEdgeSource(edge) + "\t" + graph.getEdgeTarget(edge) + "\t"
                            + edge + ">\t");
                }
                System.out.println(": " + path.getWeight());
            }
        } catch (IllegalArgumentException e) {
            fail("IllegalArgumentException thrown");
        }
    }
}
