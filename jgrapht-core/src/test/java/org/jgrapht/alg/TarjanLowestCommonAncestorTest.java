/* This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.TarjanLowestCommonAncestor.LcaRequestResponse;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Assert;
import org.junit.Test;

public class TarjanLowestCommonAncestorTest  extends TestCase{

    @Test
    public void testBinaryTree() {
	DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

	g.addVertex("a");
	g.addVertex("b");
	g.addVertex("c");
	g.addVertex("d");
	g.addVertex("e");

	g.addEdge("a", "b");
	g.addEdge("b", "c");
	g.addEdge("b", "d");
	g.addEdge("d", "e");

	Assert.assertEquals("b", new TarjanLowestCommonAncestor<String, DefaultEdge>(g).calculate("a", "c", "e"));
	Assert.assertEquals("b", new TarjanLowestCommonAncestor<String, DefaultEdge>(g).calculate("a", "b", "d"));
	Assert.assertEquals("d", new TarjanLowestCommonAncestor<String, DefaultEdge>(g).calculate("a", "d", "e"));
    }

    @Test
    public void testNonBinaryTree() {
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

	Assert.assertEquals("b", new TarjanLowestCommonAncestor<String, DefaultEdge>(g).calculate("a", "b", "h"));
	Assert.assertEquals("b", new TarjanLowestCommonAncestor<String, DefaultEdge>(g).calculate("a", "j", "f"));
	Assert.assertEquals("c", new TarjanLowestCommonAncestor<String, DefaultEdge>(g).calculate("a", "j", "h"));
	// now all together in one call
	
	LcaRequestResponse<String> bg = new LcaRequestResponse<String>("b", "h");
	LcaRequestResponse<String> ed = new LcaRequestResponse<String>("j", "f");
	LcaRequestResponse<String> fd = new LcaRequestResponse<String>("j", "h");
	List<LcaRequestResponse<String>> list = new LinkedList<LcaRequestResponse<String>> ();
	list.add(bg);
	list.add(ed);
	list.add(fd);
	List<String> result = new TarjanLowestCommonAncestor<String, DefaultEdge>(g).calculate("a", list);
	// check that the mutable input parameters have changed
	Assert.assertEquals("b",bg.getLca());
	Assert.assertEquals("b",ed.getLca());
	Assert.assertEquals("c",fd.getLca());
	// check the returned result is correct
	Assert.assertEquals(Arrays.asList(new String[]{"b","b","c"}),result);
	
	// test it the other way around and starting from b
	Assert.assertEquals("b", new TarjanLowestCommonAncestor<String, DefaultEdge>(g).calculate("b", "h", "b"));
    }

    
    @Test
    public void testOneNode() {
	DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

	g.addVertex("a");

	Assert.assertEquals("a", new TarjanLowestCommonAncestor<String, DefaultEdge>(g).calculate("a", "a", "a"));

    }

}
