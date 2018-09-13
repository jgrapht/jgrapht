package org.jgrapht.opt.graph.fastutil;

import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.IncomingOutgoingEdgesTest;
import org.jgrapht.opt.graph.fastutil.FastutilMapIntVertexGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Tests for {@link FastutilMapIntVertexGraph}.
 * 
 * @author Dimitrios Michail
 */
public class FastUtilMapIntVertexGraphTest
{
    /**
     * Test in-out edges of directed graph
     */
    @Test
    public void testDirectedGraph()
    {
        IncomingOutgoingEdgesTest.testDirectedGraph(
            () -> new FastutilMapIntVertexGraph<>(
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
            () -> new FastutilMapIntVertexGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(),
                DefaultGraphType.pseudograph()));
    }

}
