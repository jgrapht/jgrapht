package org.jgrapht.alg.shortestpath;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

public class DagAllPathsCounterTest {

    @Test
    public void testDiamondTwoPaths() {
        Graph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addEdge("A", "B");
        g.addEdge("A", "C");
        g.addEdge("B", "D");
        g.addEdge("C", "D");

        BigInteger count = DagAllPathsCounter.countAllPaths(g, "A", "D");
        assertEquals(BigInteger.valueOf(2), count);
    }

    @Test
    public void testSourceEqualsTarget() {
        Graph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        g.addVertex("X");

        BigInteger count = DagAllPathsCounter.countAllPaths(g, "X", "X");
        assertEquals(BigInteger.ONE, count);
    }

    @Test
    public void testNoPath() {
        Graph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        g.addVertex("A");
        g.addVertex("B");
        // no edge
        BigInteger count = DagAllPathsCounter.countAllPaths(g, "A", "B");
        assertEquals(BigInteger.ZERO, count);
    }

    @Test
    public void testRejectCycle() {
        Graph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        g.addVertex("A");
        g.addVertex("B");
        g.addEdge("A", "B");
        g.addEdge("B", "A"); // cycle

        assertThrows(IllegalArgumentException.class, () -> DagAllPathsCounter.countAllPaths(g, "A", "B"));
    }

    @Test
    public void testBigIntegerGrowth() {
        // Build layered DAG: s -> {u1,u2,u3} -> {v1,v2,v3,v4} -> t
        Graph<String, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
        String s = "S"; String t = "T";
        g.addVertex(s);
        g.addVertex(t);
        String[] u = {"U1","U2","U3"};
        String[] v = {"V1","V2","V3","V4"};
        for (String x : u) g.addVertex(x);
        for (String x : v) g.addVertex(x);
        for (String x : u) g.addEdge(s, x);
        for (String x : u) for (String y : v) g.addEdge(x, y);
        for (String x : v) g.addEdge(x, t);

        // number of paths = 3 * 4 = 12
        BigInteger count = DagAllPathsCounter.countAllPaths(g, s, t);
        assertEquals(BigInteger.valueOf(12), count);
    }
}
