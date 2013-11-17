/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2012, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
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
/* -------------------------
 * HopcroftKarpBipartiteMatchingTest.java
 * -------------------------
 * (C) Copyright 2012-2012, by Joris Kinable and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s):
 *
 * Changes
 * -------
 * 26-Nov-2012 : Initial revision (JK);
 *
 */
package org.jgrapht.alg;

import junit.framework.TestCase;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;

/**
 * Unit test for the MaxBipartiteMatching class
 * @author Joris Kinable
 *
 */
public class HopcroftKarpBipartiteMatchingTest extends TestCase{

	/**
	 * Random test graph 1
	 */
	public void testBipartiteMatching1(){
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		List<Integer> partition1=Arrays.asList(new Integer[]{0,1,2,3});
		List<Integer> partition2=Arrays.asList(new Integer[]{4,5,6,7});	
		Graphs.addAllVertices(graph, partition1);
		Graphs.addAllVertices(graph,partition2);
		
		DefaultEdge e00=graph.addEdge(partition1.get(0), partition2.get(0));
		DefaultEdge e01=graph.addEdge(partition1.get(0), partition2.get(1));
		DefaultEdge e02=graph.addEdge(partition1.get(0), partition2.get(2));
		
		DefaultEdge e10=graph.addEdge(partition1.get(1), partition2.get(0));
		DefaultEdge e11=graph.addEdge(partition1.get(1), partition2.get(1));
		DefaultEdge e12=graph.addEdge(partition1.get(1), partition2.get(2));
		DefaultEdge e20=graph.addEdge(partition1.get(2), partition2.get(0));
		DefaultEdge e21=graph.addEdge(partition1.get(2), partition2.get(1));
		
		
		HopcroftKarpBipartiteMatching<Integer,DefaultEdge> bm=new HopcroftKarpBipartiteMatching<Integer,DefaultEdge>(graph,new HashSet<Integer>(partition1),new HashSet<Integer>(partition2));
		assertEquals(3, bm.getMatching().size(), 0);
		List<DefaultEdge> l1 = Arrays.asList(new DefaultEdge[] {e11, e02, e20});
	    Set<DefaultEdge> matching = new HashSet<DefaultEdge>(l1);
		assertEquals(matching, bm.getMatching());
	}
	
	/**
	 * Random test graph 2
	 */
	public void testBipartiteMatching2(){
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		List<Integer> partition1=Arrays.asList(new Integer[]{0,1,2,3,4,5});
		List<Integer> partition2=Arrays.asList(new Integer[]{6,7,8,9,10,11});		
		Graphs.addAllVertices(graph, partition1);
		Graphs.addAllVertices(graph,partition2);
			
		DefaultEdge e00=graph.addEdge(partition1.get(0), partition2.get(0));
		DefaultEdge e01=graph.addEdge(partition1.get(0), partition2.get(1));
		DefaultEdge e04=graph.addEdge(partition1.get(0), partition2.get(4));
		DefaultEdge e10=graph.addEdge(partition1.get(1), partition2.get(0));
		DefaultEdge e13=graph.addEdge(partition1.get(1), partition2.get(3));
		DefaultEdge e21=graph.addEdge(partition1.get(2), partition2.get(1));
		DefaultEdge e32=graph.addEdge(partition1.get(3), partition2.get(2));
		DefaultEdge e34=graph.addEdge(partition1.get(3), partition2.get(4));
		DefaultEdge e42=graph.addEdge(partition1.get(4), partition2.get(2));
		DefaultEdge e52=graph.addEdge(partition1.get(5), partition2.get(2));
		DefaultEdge e55=graph.addEdge(partition1.get(5), partition2.get(5));
		
		HopcroftKarpBipartiteMatching<Integer,DefaultEdge> bm=new HopcroftKarpBipartiteMatching<Integer,DefaultEdge>(graph,new HashSet<Integer>(partition1),new HashSet<Integer>(partition2));
		assertEquals(6, bm.getMatching().size(), 0);
		List<DefaultEdge> l1 = Arrays.asList(new DefaultEdge[] {e21, e13, e00, e42, e34, e55});
	    Set<DefaultEdge> matching = new HashSet<DefaultEdge>(l1);
		assertEquals(matching, bm.getMatching());
	}
	
	/**
	 * Find a maximum matching on a graph without edges
	 */
	public void testEmptyMatching(){
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		List<Integer> partition1=Arrays.asList(new Integer[]{0});
		List<Integer> partition2=Arrays.asList(new Integer[]{1});		
		Graphs.addAllVertices(graph, partition1);
		Graphs.addAllVertices(graph,partition2);
		HopcroftKarpBipartiteMatching<Integer,DefaultEdge> bm=new HopcroftKarpBipartiteMatching<Integer,DefaultEdge>(graph,new HashSet<Integer>(partition1),new HashSet<Integer>(partition2));
		assertEquals(Collections.EMPTY_SET, bm.getMatching());
	}
}
