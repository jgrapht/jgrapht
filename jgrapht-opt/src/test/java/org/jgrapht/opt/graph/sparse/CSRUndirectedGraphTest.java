package org.jgrapht.opt.graph.sparse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.jgrapht.alg.util.Pair;
import org.junit.Test;

public class CSRUndirectedGraphTest
{

    /**
     * Test undirected sparse CSR
     */
    @Test
    public void testUndirected()
    {
        List<Pair<Integer, Integer>> edges = Arrays
            .asList(
                Pair.of(0, 5), Pair.of(0, 2), Pair.of(3, 4), Pair.of(1, 4), Pair.of(0, 1), Pair.of(3, 1), Pair.of(2, 4));

        CSRUndirectedGraph g = new CSRUndirectedGraph(6, edges);
        
        assertEquals(6, g.vertexSet().size());
        assertTrue(g.containsVertex(0));
        assertTrue(g.containsVertex(1));
        assertTrue(g.containsVertex(2));
        assertTrue(g.containsVertex(3));
        assertTrue(g.containsVertex(4));
        assertTrue(g.containsVertex(5));

        assertEquals(3, g.degreeOf(0));
        assertEquals(3, g.degreeOf(1));
        assertEquals(2, g.degreeOf(2));
        assertEquals(2, g.degreeOf(3));
        assertEquals(3, g.degreeOf(4));
        assertEquals(1, g.degreeOf(5));
        
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
    }

}
