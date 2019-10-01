package org.jgrapht.opt.graph.sparse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jgrapht.alg.util.Pair;
import org.junit.Test;

public class SparseUndirectedGraphTest
{

    /**
     * Test undirected sparse CSR
     */
    @Test
    public void testUndirected()
    {
        List<Pair<Integer, Integer>> edges = Arrays
            .asList(
                Pair.of(0, 5), Pair.of(0, 2), Pair.of(3, 4), Pair.of(1, 4), Pair.of(0, 1),
                Pair.of(3, 1), Pair.of(2, 4));

        SparseUndirectedGraph g = new SparseUndirectedGraph(6, edges);

        assertEquals(6, g.vertexSet().size());
        assertTrue(g.containsVertex(0));
        assertTrue(g.containsVertex(1));
        assertTrue(g.containsVertex(2));
        assertTrue(g.containsVertex(3));
        assertTrue(g.containsVertex(4));
        assertTrue(g.containsVertex(5));

        assertEquals(3, g.degreeOf(0));
        assertEquals(3, g.inDegreeOf(0));
        assertEquals(3, g.outDegreeOf(0));
        assertEquals(new HashSet<>(Arrays.asList(0, 1, 4)), g.edgesOf(0));
        assertEquals(new HashSet<>(Arrays.asList(0, 1, 4)), g.incomingEdgesOf(0));
        assertEquals(new HashSet<>(Arrays.asList(0, 1, 4)), g.outgoingEdgesOf(0));

        assertEquals(3, g.degreeOf(1));
        assertEquals(3, g.inDegreeOf(1));
        assertEquals(3, g.outDegreeOf(1));
        assertEquals(new HashSet<>(Arrays.asList(3, 4, 5)), g.edgesOf(1));
        assertEquals(new HashSet<>(Arrays.asList(3, 4, 5)), g.incomingEdgesOf(1));
        assertEquals(new HashSet<>(Arrays.asList(3, 4, 5)), g.outgoingEdgesOf(1));

        assertEquals(2, g.degreeOf(2));
        assertEquals(2, g.inDegreeOf(2));
        assertEquals(2, g.outDegreeOf(2));
        assertEquals(new HashSet<>(Arrays.asList(1, 6)), g.edgesOf(2));
        assertEquals(new HashSet<>(Arrays.asList(1, 6)), g.incomingEdgesOf(2));
        assertEquals(new HashSet<>(Arrays.asList(1, 6)), g.outgoingEdgesOf(2));

        assertEquals(2, g.degreeOf(3));
        assertEquals(2, g.inDegreeOf(3));
        assertEquals(2, g.outDegreeOf(3));
        assertEquals(new HashSet<>(Arrays.asList(2, 5)), g.edgesOf(3));
        assertEquals(new HashSet<>(Arrays.asList(2, 5)), g.incomingEdgesOf(3));
        assertEquals(new HashSet<>(Arrays.asList(2, 5)), g.outgoingEdgesOf(3));

        assertEquals(3, g.degreeOf(4));
        assertEquals(3, g.inDegreeOf(4));
        assertEquals(3, g.outDegreeOf(4));
        assertEquals(new HashSet<>(Arrays.asList(2, 3, 6)), g.edgesOf(4));
        assertEquals(new HashSet<>(Arrays.asList(2, 3, 6)), g.incomingEdgesOf(4));
        assertEquals(new HashSet<>(Arrays.asList(2, 3, 6)), g.outgoingEdgesOf(4));

        assertEquals(1, g.degreeOf(5));
        assertEquals(1, g.inDegreeOf(5));
        assertEquals(1, g.outDegreeOf(5));
        assertEquals(new HashSet<>(Arrays.asList(0)), g.edgesOf(5));
        assertEquals(new HashSet<>(Arrays.asList(0)), g.incomingEdgesOf(5));
        assertEquals(new HashSet<>(Arrays.asList(0)), g.outgoingEdgesOf(5));

        assertEquals(Integer.valueOf(0), g.getEdgeSource(0));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(0));
        assertEquals(Integer.valueOf(0), g.getEdgeSource(1));
        assertEquals(Integer.valueOf(2), g.getEdgeTarget(1));
        assertEquals(Integer.valueOf(3), g.getEdgeSource(2));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(2));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(3));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(3));
        assertEquals(Integer.valueOf(0), g.getEdgeSource(4));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(4));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(5));
        assertEquals(Integer.valueOf(3), g.getEdgeTarget(5));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(6));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(6));

        assertEquals(
            IntStream.range(0, edges.size()).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.edgeSet());
        assertEquals(
            IntStream.range(0, 6).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.vertexSet());
    }

}
