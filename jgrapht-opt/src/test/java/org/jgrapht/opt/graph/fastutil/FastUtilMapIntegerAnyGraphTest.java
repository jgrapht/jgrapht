package org.jgrapht.opt.graph.fastutil;

import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.graph.IncomingOutgoingEdgesTest;
import org.jgrapht.opt.graph.fastutil.FastutilMapIntegerAnyGraph;
import org.jgrapht.util.SupplierUtil;
import org.junit.Test;

/**
 * Tests for {@link FastutilIntegerAnyMapGraph}.
 * 
 * @author Dimitrios Michail
 */
public class FastUtilMapIntegerAnyGraphTest
{
    /**
     * Test in-out edges of directed graph
     */
    @Test
    public void testDirectedGraph()
    {
        IncomingOutgoingEdgesTest.testDirectedGraph(
            () -> new FastutilMapIntegerAnyGraph<>(
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
            () -> new FastutilMapIntegerAnyGraph<>(
                SupplierUtil.createIntegerSupplier(), SupplierUtil.createDefaultEdgeSupplier(),
                DefaultGraphType.pseudograph()));
    }

}
