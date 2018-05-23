package org.jgrapht.alg.lca;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TarjanLCAFinderTest {

    @Test
    public void testBinaryTree()
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("b", "d");
        g.addEdge("d", "e");

        TarjanLCAFinder<String, DefaultEdge> tarjanLCAFinder = new TarjanLCAFinder<>(g, "a");

        Assert.assertEquals("b", tarjanLCAFinder.getLCA("c", "e"));
        Assert.assertEquals("b", tarjanLCAFinder.getLCA("b", "d"));
        Assert.assertEquals("d", tarjanLCAFinder.getLCA("d", "e"));
    }

    @Test
    public void testNonBinaryTree()
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        g.addVertex("i");
        g.addVertex("j");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("d", "e");
        g.addEdge("b", "f");
        g.addEdge("b", "g");
        g.addEdge("c", "h");
        g.addEdge("c", "i");
        g.addEdge("i", "j");

        TarjanLCAFinder<String, DefaultEdge> tarjanLCAFinder = new TarjanLCAFinder<>(g, "a");

        Assert.assertEquals("b", tarjanLCAFinder.getLCA("b", "h"));
        Assert.assertEquals("b", tarjanLCAFinder.getLCA("j", "f"));
        Assert.assertEquals("c", tarjanLCAFinder.getLCA("j", "h"));

        // now all together in one call

        List<Pair<String, String>> queries = new ArrayList<>();
        queries.add(Pair.of("b", "h"));
        queries.add(Pair.of("j", "f"));
        queries.add(Pair.of("j", "h"));

        List<String> lcas = new TarjanLCAFinder<>(g, "a").getLCAs(queries);

        Assert.assertEquals(Arrays.asList("b", "b", "c"), lcas);

        // test it the other way around and starting from b
        Assert.assertEquals("b", new TarjanLCAFinder<>(g, "b").getLCA("h", "b"));
    }

    @Test
    public void testOneNode()
    {
        Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        g.addVertex("a");
        Assert.assertEquals("a", new TarjanLCAFinder<>(g, "a").getLCA("a", "a"));
    }
}