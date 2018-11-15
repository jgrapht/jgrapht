package org.jgrapht.alg.dense;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link MaximumDensitySubgraphAlgorithm}
 *
 * @author Andre Immig
 */


public class MaximumDensitySubgraphAlgorithmTest {
    @Test
    public void testMinimal(){
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        g.addVertex(0);
        g.addVertex(1);
        DefaultEdge e1 = g.addEdge(0,1);
        g.setEdgeWeight(e1,10);
        MaximumDensitySubgraphAlgorithm<Integer, DefaultEdge> solver = new MaximumDensitySubgraphAlgorithm<>(DefaultEdge.class,g,-1,-2);
        test(g, solver, 5, new LinkedHashSet<>(Arrays.asList(0,1)));
    }

    public void testSmall(){
        WeightedMultigraph<Integer, DefaultEdge> g = new WeightedMultigraph<>(DefaultEdge.class);
        for (int i=0; i<=4; i++){
            g.addVertex(i);
        }
        DefaultEdge e;
        e = g.addEdge(0,3);
        g.setEdgeWeight(e,2);
        e = g.addEdge(0,1);
        g.setEdgeWeight(e,1);
        e = g.addEdge(0,2);
        g.setEdgeWeight(e,1);
        e = g.addEdge(4,2);
        g.setEdgeWeight(e,1);
        e = g.addEdge(0,4);
        g.setEdgeWeight(e,3);
        e = g.addEdge(2,3);
        g.setEdgeWeight(e,1);

        MaximumDensitySubgraphAlgorithm<Integer, DefaultEdge> solver = new MaximumDensitySubgraphAlgorithm<>(DefaultEdge.class,g,-1,-2);
        test(g, solver, 2, new LinkedHashSet<>(Arrays.asList(0,2,3,4)));
    }

    public void test(Graph<Integer,DefaultEdge> g, MaximumDensitySubgraphAlgorithm<Integer, DefaultEdge> solver, double expectedDensity, Set<Integer> expectedVertices){
        Graph<Integer,DefaultEdge> computed = solver.calculateDensestExact();
        assertEquals(expectedDensity, solver.getDensity(), Math.pow(10,-5));
        Graph<Integer, DefaultEdge> expected = new AsSubgraph<>(g, expectedVertices);
        assertEquals(expected, computed);
    }
}
