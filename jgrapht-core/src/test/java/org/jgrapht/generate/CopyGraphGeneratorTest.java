package org.jgrapht.generate;

import junit.framework.Assert;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

/**
 * Daneel Yaitskov
 */
public class CopyGraphGeneratorTest {


    @Test
    public void test() {
        UndirectedGraph<String, String> g =
                new SimpleGraph<String, String>(String.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");

        g.addEdge("a", "b", "e1");
        g.addEdge("c", "b", "e2");

        CopyGraphGenerator<String, String, Object> cgg =
                new CopyGraphGenerator<String, String, Object>(g);

        UndirectedGraph<String, String> copy =
                new SimpleGraph<String, String>(String.class);

        cgg.generateGraph(copy, null, null);

        Assert.assertEquals("vertexes different", g.vertexSet(), copy.vertexSet());
        Assert.assertEquals("edges different", g.edgeSet(), copy.edgeSet());

        for (String e : g.edgeSet()) {
            Assert.assertEquals("source vertex different",
                    g.getEdgeSource(e), copy.getEdgeSource(e));
            Assert.assertEquals("target vertex different",
                    g.getEdgeTarget(e), copy.getEdgeTarget(e));

        }
    }
}
