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
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, 1000, 0);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        assertEquals(1000, g.edgeSet().size());
    }

    @Test
    public void testNumberOfNodes()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> generator =
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, -1, 1000);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        assertEquals(1000, g.vertexSet().size());
    }

    @Test
    public void testZeroCases()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> generator =
            new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, -1, 0);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        assertEquals(0, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());

        generator = new DirectedScaleFreeGraphGenerator<>(0.33f, 0.33f, 0.5f, 0.5f, 0, 0);
        g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        assertEquals(0, g.vertexSet().size());
        assertEquals(0, g.edgeSet().size());
    }

    @Test
    public void testNoOutDegreeZero()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> generator =
            new DirectedScaleFreeGraphGenerator<>(0.3f, 0.0f, 0.5f, 0.5f, -1, 1000);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        long outDegreeZero = g.vertexSet().stream().filter(v -> g.outDegreeOf(v) == 0).count();
        assertEquals(0, outDegreeZero);
    }
    
    @Test
    public void testNoInDegreeZero()
    {
        GraphGenerator<Integer, DefaultEdge, Integer> generator =
            new DirectedScaleFreeGraphGenerator<>(0.0f, 0.3f, 0.5f, 0.5f, -1, 1000);
        Graph<Integer, DefaultEdge> g = new DefaultDirectedGraph<>(
            SupplierUtil.createIntegerSupplier(), SupplierUtil.DEFAULT_EDGE_SUPPLIER, false);
        generator.generateGraph(g);
        long inDegreeZero = g.vertexSet().stream().filter(v -> g.inDegreeOf(v) == 0).count();
        assertEquals(0, inDegreeZero);
    }
}
