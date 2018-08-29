package org.jgrapht.opt.graph.fastutil;

import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.IncomingOutgoingEdgesTest;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Tests for {@link FastutilMapGraph}.
 * 
 * @author Dimitrios Michail
 */
public class FastUtilMapGraphTest
{
    /**
     * Test in-out edges of directed graph
     */
    @Test
    public void testDirectedGraph()
    {
        IncomingOutgoingEdgesTest.testDirectedGraph(
            () -> new FastutilMapGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(),
                DefaultGraphType.directedPseudograph()));
    }

    /**
     * Test in-out edges of undirected graph
     */
    @Test
    public void testUndirectedGraph()
    {
        IncomingOutgoingEdgesTest.testUndirectedGraph(
            () -> new FastutilMapGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(),
                DefaultGraphType.pseudograph()));
    }

}
