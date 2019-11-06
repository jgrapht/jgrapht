package org.jgrapht.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

public class DirectedScaleFreeGraphGeneratorTest
{
    @Test
    public void testBadParameters()
    {
        try {
            new DirectedScaleFreeGraphGenerator<>(-0.5f, 0.33f, 0.5f, 0.5f, 500, 500);
            fail("Bad alpha checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.33f, -0.5f, 0.5f, 0.5f, 500, 500);
            fail("Bad gamma checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.66f, 0.66f, 0.5f, 0.5f, 500, 500);
            fail("Bad alpha + gamma checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, -0.5f, 0.5f, 500, 500);
            fail("Bad deltaIn checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, -0.5f, 500, 500);
            fail("Bad deltaOut checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, -1, -1);
            fail("Bad target checking");
        } catch (IllegalArgumentException e) {
        }
        try {
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, 500, 500, null);
            fail("Bad random number generator checking");
        } catch (NullPointerException e) {
        }

    }

    @Test
    public void testNumberOfEdges()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> generator =
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, 500, 0);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        assertEquals(500, g.edgeSet().size());
    }

    @Test
    public void testNumberOfNodes()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> generator =
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, -1, 500);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        assertEquals(500, g.vertexSet().size());
    }
}
