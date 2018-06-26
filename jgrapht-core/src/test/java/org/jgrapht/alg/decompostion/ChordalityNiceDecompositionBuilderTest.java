package org.jgrapht.alg.decompostion;

import org.jgrapht.*;
import org.jgrapht.alg.decomposition.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.graph.builder.*;
import org.junit.*;

public class ChordalityNiceDecompositionBuilderTest
{

    private Graph<Integer, DefaultEdge> makeCompleteGraph(int n)
    {
        Graph<Integer, DefaultEdge> graph =
            new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
        CompleteGraphGenerator<Integer, DefaultEdge> cgGen = new CompleteGraphGenerator<>(n);
        cgGen.generateGraph(graph, new IntegerVertexFactory(), null);
        return graph;
    }

    @Test
    public void testCompleteGraph()
    {
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultEdge> graph = makeCompleteGraph(i);
            ChordalNiceDecompositionBuilder<Integer, DefaultEdge> decompBuild =
                ChordalNiceDecompositionBuilder.create(graph);
            NiceDecompositionBuilderTestUtil
                .testDecomposition(graph, decompBuild.getDecomposition(), decompBuild.getMap());
            NiceDecompositionBuilderTestUtil.testNiceDecomposition(
                decompBuild.getDecomposition(), decompBuild.getMap(), decompBuild.getRoot());
        }
    }

    private Graph<Integer, DefaultEdge> makeGraphsWithTriangles(int n)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        if (n == 0)
            return graph;
        int freeVertex = 1;
        graph.addVertex(freeVertex++);
        graph.addVertex(freeVertex++);
        graph.addVertex(freeVertex++);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 1);
        for (int i = 1; i < n; i++) {
            graph.addVertex(freeVertex++);
            graph.addEdge(freeVertex - 3, freeVertex - 1);
            graph.addEdge(freeVertex - 2, freeVertex - 1);
        }
        return graph;
    }

    @Test
    public void testGraphsWithTriangles()
    {
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultEdge> graph = makeGraphsWithTriangles(i);
            ChordalNiceDecompositionBuilder<Integer, DefaultEdge> decompBuild =
                ChordalNiceDecompositionBuilder.create(graph);
            NiceDecompositionBuilderTestUtil
                .testDecomposition(graph, decompBuild.getDecomposition(), decompBuild.getMap());
            NiceDecompositionBuilderTestUtil.testNiceDecomposition(
                decompBuild.getDecomposition(), decompBuild.getMap(), decompBuild.getRoot());
        }
    }

    private Graph<Integer, DefaultEdge> makeUnconnectedGraph(int n)
    {
        Graph<Integer, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (int i = 0; i < n; i++)
            graph.addVertex(i);
        return graph;
    }

    @Test
    public void testUnconnectedGraph()
    {
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultEdge> graph = makeUnconnectedGraph(i);
            ChordalNiceDecompositionBuilder<Integer, DefaultEdge> decompBuild =
                ChordalNiceDecompositionBuilder.create(graph);
            NiceDecompositionBuilderTestUtil
                .testDecomposition(graph, decompBuild.getDecomposition(), decompBuild.getMap());
            NiceDecompositionBuilderTestUtil.testNiceDecomposition(
                decompBuild.getDecomposition(), decompBuild.getMap(), decompBuild.getRoot());
        }
    }

    public Graph<Integer, DefaultEdge> makeLekkerkerkerBolandFamily(int n)
    {
        GraphBuilder<Integer, DefaultEdge, ? extends SimpleGraph<Integer, DefaultEdge>> builder =
            SimpleGraph.createBuilder(DefaultEdge.class);

        builder.addEdge(0, 1);

        for (int i = 3; i < n; i++) {
            builder.addEdge(1, i);
            builder.addEdge(i - 1, i);
        }

        builder.addEdge(n - 1, n);

        return builder.build();
    }

    @Test
    public void testLekkerkerkerBolandFamily()
    {
        for (int i = 0; i < 10; i++) {
            Graph<Integer, DefaultEdge> graph = makeLekkerkerkerBolandFamily(i);
            ChordalNiceDecompositionBuilder<Integer, DefaultEdge> decompBuild =
                ChordalNiceDecompositionBuilder.create(graph);
            NiceDecompositionBuilderTestUtil
                .testDecomposition(graph, decompBuild.getDecomposition(), decompBuild.getMap());
            NiceDecompositionBuilderTestUtil.testNiceDecomposition(
                decompBuild.getDecomposition(), decompBuild.getMap(), decompBuild.getRoot());
        }
    }

}
