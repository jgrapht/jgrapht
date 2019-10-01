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

public class SparseGraphTest
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

    @Test
    public void testDirected()
    {
        List<Pair<Integer, Integer>> edges = Arrays
            .asList(
                Pair.of(0, 1), Pair.of(1, 0), Pair.of(1, 4), Pair.of(1, 5), Pair.of(1, 6),
                Pair.of(2, 4), Pair.of(2, 4), Pair.of(2, 4), Pair.of(3, 4), Pair.of(4, 5),
                Pair.of(5, 6), Pair.of(7, 6), Pair.of(7, 7));

        int vertices = 8;
        SparseDirectedGraph g = new SparseDirectedGraph(vertices, edges);

        assertEquals(vertices, g.vertexSet().size());
        assertEquals(edges.size(), g.edgeSet().size());
        
        assertEquals(
            IntStream.range(0, edges.size()).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.edgeSet());
        assertEquals(
            IntStream.range(0, vertices).mapToObj(Integer::valueOf).collect(Collectors.toSet()),
            g.vertexSet());
        
        for(int i = 0; i < vertices; i++) { 
            assertTrue(g.containsVertex(i));    
        }

        assertEquals(2, g.degreeOf(0));
        assertEquals(1, g.inDegreeOf(0));
        assertEquals(1, g.outDegreeOf(0));
        assertEquals(new HashSet<>(Arrays.asList(0, 1)), g.edgesOf(0));
        assertEquals(new HashSet<>(Arrays.asList(1)), g.incomingEdgesOf(0));
        assertEquals(new HashSet<>(Arrays.asList(0)), g.outgoingEdgesOf(0));
        
        assertEquals(5, g.degreeOf(1));
        assertEquals(1, g.inDegreeOf(1));
        assertEquals(4, g.outDegreeOf(1));
        assertEquals(new HashSet<>(Arrays.asList(0, 1, 2, 3, 4)), g.edgesOf(1));
        assertEquals(new HashSet<>(Arrays.asList(0)), g.incomingEdgesOf(1));
        assertEquals(new HashSet<>(Arrays.asList(1, 2, 3, 4)), g.outgoingEdgesOf(1));
        
        assertEquals(3, g.degreeOf(2));
        assertEquals(0, g.inDegreeOf(2));
        assertEquals(3, g.outDegreeOf(2));
        assertEquals(new HashSet<>(Arrays.asList(5, 6, 7)), g.edgesOf(2));
        assertEquals(new HashSet<>(), g.incomingEdgesOf(2));
        assertEquals(new HashSet<>(Arrays.asList(5, 6, 7)), g.outgoingEdgesOf(2));
        
        assertEquals(1, g.degreeOf(3));
        assertEquals(0, g.inDegreeOf(3));
        assertEquals(1, g.outDegreeOf(3));
        assertEquals(new HashSet<>(Arrays.asList(8)), g.edgesOf(3));
        assertEquals(new HashSet<>(), g.incomingEdgesOf(3));
        assertEquals(new HashSet<>(Arrays.asList(8)), g.outgoingEdgesOf(3));
        
        assertEquals(6, g.degreeOf(4));
        assertEquals(5, g.inDegreeOf(4));
        assertEquals(1, g.outDegreeOf(4));
        assertEquals(new HashSet<>(Arrays.asList(2, 5, 6, 7, 8, 9)), g.edgesOf(4));
        assertEquals(new HashSet<>(Arrays.asList(2, 5, 6, 7, 8)), g.incomingEdgesOf(4));
        assertEquals(new HashSet<>(Arrays.asList(9)), g.outgoingEdgesOf(4));
        
        assertEquals(3, g.degreeOf(5));
        assertEquals(2, g.inDegreeOf(5));
        assertEquals(1, g.outDegreeOf(5));
        assertEquals(new HashSet<>(Arrays.asList(3, 9, 10)), g.edgesOf(5));
        assertEquals(new HashSet<>(Arrays.asList(3, 9)), g.incomingEdgesOf(5));
        assertEquals(new HashSet<>(Arrays.asList(10)), g.outgoingEdgesOf(5));
        
        assertEquals(3, g.degreeOf(6));
        assertEquals(3, g.inDegreeOf(6));
        assertEquals(0, g.outDegreeOf(6));
        assertEquals(new HashSet<>(Arrays.asList(4, 10, 11)), g.edgesOf(6));
        assertEquals(new HashSet<>(Arrays.asList(4, 10, 11)), g.incomingEdgesOf(6));
        assertEquals(new HashSet<>(), g.outgoingEdgesOf(6));
        
        assertEquals(3, g.degreeOf(7));
        assertEquals(1, g.inDegreeOf(7));
        assertEquals(2, g.outDegreeOf(7));
        assertEquals(new HashSet<>(Arrays.asList(11, 12)), g.edgesOf(7));
        assertEquals(new HashSet<>(Arrays.asList(12)), g.incomingEdgesOf(7));
        assertEquals(new HashSet<>(Arrays.asList(11, 12)), g.outgoingEdgesOf(7));
        
        assertEquals(Integer.valueOf(0), g.getEdgeSource(0));
        assertEquals(Integer.valueOf(1), g.getEdgeTarget(0));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(1));
        assertEquals(Integer.valueOf(0), g.getEdgeTarget(1));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(2));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(2));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(3));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(3));
        assertEquals(Integer.valueOf(1), g.getEdgeSource(4));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(4));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(5));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(5));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(6));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(6));
        assertEquals(Integer.valueOf(2), g.getEdgeSource(7));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(7));
        assertEquals(Integer.valueOf(3), g.getEdgeSource(8));
        assertEquals(Integer.valueOf(4), g.getEdgeTarget(8));
        assertEquals(Integer.valueOf(4), g.getEdgeSource(9));
        assertEquals(Integer.valueOf(5), g.getEdgeTarget(9));
        assertEquals(Integer.valueOf(5), g.getEdgeSource(10));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(10));
        assertEquals(Integer.valueOf(7), g.getEdgeSource(11));
        assertEquals(Integer.valueOf(6), g.getEdgeTarget(11));
        assertEquals(Integer.valueOf(7), g.getEdgeSource(12));
        assertEquals(Integer.valueOf(7), g.getEdgeTarget(12));
        
    }
}
