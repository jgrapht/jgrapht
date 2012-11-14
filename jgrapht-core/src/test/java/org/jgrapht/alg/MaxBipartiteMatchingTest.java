package org.jgrapht.alg;

import junit.framework.TestCase;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit test for the MaxBipartiteMatching class
 * @author Joris Kinable
 *
 */
public class MaxBipartiteMatchingTest extends TestCase{

	/**
	 * Random test graph 1
	 */
	public void testBipartiteMatching1(){
		final UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		final List<Integer> partition1=Arrays.asList(0,1,2,3);
		final List<Integer> partition2=Arrays.asList(4,5,6,7);
		Graphs.addAllVertices(graph, partition1);
		Graphs.addAllVertices(graph,partition2);

		final DefaultEdge e00=graph.addEdge(partition1.get(0), partition2.get(0));
		final DefaultEdge e01=graph.addEdge(partition1.get(0), partition2.get(1));
		final DefaultEdge e02=graph.addEdge(partition1.get(0), partition2.get(2));
		
		final DefaultEdge e10=graph.addEdge(partition1.get(1), partition2.get(0));
		final DefaultEdge e11=graph.addEdge(partition1.get(1), partition2.get(1));
		final DefaultEdge e12=graph.addEdge(partition1.get(1), partition2.get(2));
		final DefaultEdge e20=graph.addEdge(partition1.get(2), partition2.get(0));
		final DefaultEdge e21=graph.addEdge(partition1.get(2), partition2.get(1));
		
		
		final MaxBipartiteMatching<Integer,DefaultEdge> bm=new MaxBipartiteMatching<Integer,DefaultEdge>(graph,partition1,partition2);
		assertEquals(3, bm.getSize(), 0);
		final List<DefaultEdge> l1 = Arrays.asList(e11, e02, e20);
	    final Set<DefaultEdge> matching = new HashSet<DefaultEdge>(l1);
		assertEquals(matching, bm.getMatching());
	}
	
	/**
	 * Random test graph 2
	 */
	public void testBipartiteMatching2(){
		final UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		final List<Integer> partition1=Arrays.asList(0,1,2,3,4,5);
		final List<Integer> partition2=Arrays.asList(6,7,8,9,10,11);
		Graphs.addAllVertices(graph, partition1);
		Graphs.addAllVertices(graph,partition2);
			
		final DefaultEdge e00=graph.addEdge(partition1.get(0), partition2.get(0));
		final DefaultEdge e01=graph.addEdge(partition1.get(0), partition2.get(1));
		final DefaultEdge e04=graph.addEdge(partition1.get(0), partition2.get(4));
		final DefaultEdge e10=graph.addEdge(partition1.get(1), partition2.get(0));
		final DefaultEdge e13=graph.addEdge(partition1.get(1), partition2.get(3));
		final DefaultEdge e21=graph.addEdge(partition1.get(2), partition2.get(1));
		final DefaultEdge e32=graph.addEdge(partition1.get(3), partition2.get(2));
		final DefaultEdge e34=graph.addEdge(partition1.get(3), partition2.get(4));
		final DefaultEdge e42=graph.addEdge(partition1.get(4), partition2.get(2));
		final DefaultEdge e52=graph.addEdge(partition1.get(5), partition2.get(2));
		final DefaultEdge e55=graph.addEdge(partition1.get(5), partition2.get(5));
		
		final MaxBipartiteMatching<Integer,DefaultEdge> bm=new MaxBipartiteMatching<Integer,DefaultEdge>(graph,partition1,partition2);
		assertEquals(6, bm.getSize(), 0);
		final List<DefaultEdge> l1 = Arrays.asList(e21, e13, e00, e42, e34, e55);
	    final Set<DefaultEdge> matching = new HashSet<DefaultEdge>(l1);
		assertEquals(matching, bm.getMatching());
	}
	
	/**
	 * Find a maximum matching on a graph without edges
	 */
	public void testEmptyMatching(){
		final UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		final List<Integer> partition1=Arrays.asList(0);
		final List<Integer> partition2=Arrays.asList(1);
		Graphs.addAllVertices(graph, partition1);
		Graphs.addAllVertices(graph,partition2);
		final MaxBipartiteMatching<Integer,DefaultEdge> bm=new MaxBipartiteMatching<Integer,DefaultEdge>(graph,partition1,partition2);
		assertEquals(Collections.EMPTY_SET, bm.getMatching());
	}
}
