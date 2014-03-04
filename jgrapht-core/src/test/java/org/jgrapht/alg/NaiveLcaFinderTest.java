package org.jgrapht.alg;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.Assert;
import org.junit.Test;

public class NaiveLcaFinderTest {

    @Test
    public void testNormalCases() {
	SimpleDirectedGraph<String, DefaultEdge> g = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

	g.addVertex("a");
	g.addVertex("b");
	g.addVertex("c");
	g.addVertex("d");
	g.addVertex("e");
	g.addVertex("f");
	g.addVertex("g");
	g.addVertex("h");

	g.addEdge("a", "b");
	g.addEdge("b", "c");
	g.addEdge("c", "d");
	g.addEdge("d", "e");
	g.addEdge("b", "f");
	g.addEdge("b", "g");
	g.addEdge("f", "e");
	g.addEdge("e", "h");

	NaiveLcaFinder<String, DefaultEdge> finder = new NaiveLcaFinder<String, DefaultEdge>(g);

	Assert.assertEquals("f", finder.findLca("f", "h"));
	Assert.assertEquals("f", finder.findLca("h", "f"));
	Assert.assertEquals("b", finder.findLca("g", "h"));
	Assert.assertEquals("c", finder.findLca("c", "c"));
	Assert.assertEquals("a", finder.findLca("a", "e")); // tests one path not descending
    }

    @Test
    public void testNoLca() {
	SimpleDirectedGraph<String, DefaultEdge> g = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

	g.addVertex("a");
	g.addVertex("b");
	g.addVertex("c");
	g.addVertex("d");
	g.addVertex("e");
	g.addVertex("f");
	g.addVertex("g");
	g.addVertex("h");
	g.addVertex("i");

	g.addEdge("a", "b");
	g.addEdge("b", "c");
	g.addEdge("c", "d");
	g.addEdge("d", "e");
	g.addEdge("f", "g");
	g.addEdge("f", "h");
	g.addEdge("g", "i");
	g.addEdge("h", "i");

	NaiveLcaFinder<String, DefaultEdge> finder = new NaiveLcaFinder<String, DefaultEdge>(g);

	Assert.assertEquals(null, finder.findLca("i", "e"));
    }
    
    @Test
    public void testLoops() {
	DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

	g.addVertex("a");
	g.addVertex("b");
	g.addVertex("c");
	g.addVertex("d");
	g.addVertex("e");
	g.addVertex("f");
	g.addVertex("g");
	g.addVertex("h");
	g.addVertex("i");
	
	g.addEdge("a", "b");
	g.addEdge("b", "c");
	g.addEdge("c", "d");
	g.addEdge("d", "e");
	g.addEdge("b", "f");
	g.addEdge("b", "g");
	g.addEdge("f", "e");
	g.addEdge("e", "h");
	g.addEdge("h", "e");
	g.addEdge("h", "h");
	g.addEdge("i", "i");
	NaiveLcaFinder<String, DefaultEdge> finder = new NaiveLcaFinder<String, DefaultEdge>(g);

	Assert.assertEquals("f", finder.findLca("h", "f"));
	Assert.assertEquals(null, finder.findLca("a", "i"));
    }
    
    @Test
    public void testArrivalOrder() {
	DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

	g.addVertex("a");
	g.addVertex("b");
	g.addVertex("c");
	g.addVertex("g");
	g.addVertex("e");
	g.addVertex("h");

	g.addEdge("a", "b");
	g.addEdge("b", "c");
	g.addEdge("a", "g");
	g.addEdge("b", "g");
	g.addEdge("g", "e");
	g.addEdge("e", "h");
	NaiveLcaFinder<String, DefaultEdge> finder = new NaiveLcaFinder<String, DefaultEdge>(g);

	Assert.assertEquals("b", finder.findLca("b", "h"));
	Assert.assertEquals("b", finder.findLca("c", "e"));
    }


}
