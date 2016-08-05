package org.jgrapht.alg;

import java.util.HashMap;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.interfaces.MinimumVertexCoverAlgorithm;
import org.jgrapht.alg.interfaces.MinimumWeightedVertexCoverAlgorithm;
import org.jgrapht.alg.interfaces.MinimumVertexCoverAlgorithm.VertexCover;
import org.jgrapht.alg.vertexcover.RecursiveExactVCImpl;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import junit.framework.TestCase;

/**
 * Tests for the maximum weight independent set algorithms.
 *
 * @author Nils Olberg
 */

public class IndependentSetTest extends TestCase {
	 
	/**
     * Tests the algorithms for the maximum weight independent set problem.
     */
	
	public static void testIndependentSet1()
	{
		int[] weightArray = {8, 18, 14, 17, 8, 16, 14, 5, 2, 8, 5, 18, 0, 20, 6, 8, 7, 12, 11, 6, 
			6, 6, 20, 8, 23, 1, 20, 3, 3, 11, 20, 15, 3, 5, 14, 14, 4, 11, 20, 8, 
			0, 20, 16, 9, 0, 12, 20, 1, 19, 6, 8, 6, 3, 7, 15, 10, 23, 2, 14, 6};
	
		int[][] edges = {{2,51}, {4,20}, {4,56}, {4,59}, {5,26}, {6,44}, {11,46}, {12,55}, {13,31}, {13,41}, 
			{13,44}, {13,53}, {14,46}, {15,54}, {16,59}, {19,51}, {23,48}, {24,43}, {25,39}, {26,41}, 
			{26,55}, {27,42}, {29,58}, {37,58}, {42,54}, {42,58}};
	
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
		for (int i = 0; i < weightArray.length; i++)
			graph.addVertex(i);
		for (int[] edge : edges)
			graph.addEdge(edge[0], edge[1]);
	
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		for (int i = 0; i < weightArray.length; i++)
			weights.put(i, Double.valueOf(weightArray[i]));
	
		assertEquals(IndependentSet.getWeight(graph, weights), 485.0);
	}
	
	public static void testIndependentSet2()
	{
		int[] weightArray = {15, 1, 16, 20, 5, 15, 7, 1, 4, 11, 0, 8, 19, 15, 13, 20, 4, 25, 4, 15, 
			21, 18, 9, 21, 16, 2, 18, 21, 0, 21, 13, 4, 19, 6, 24, 3, 4, 19, 23, 12, 
			3, 3, 21, 11, 4, 19, 6, 25, 15, 2, 0, 19, 21, 15, 18, 8, 17, 8, 7, 4};
	
		int[][] edges = {{0,12}, {1,29}, {4,35}, {4,41}, {6,18}, {6,44}, {7,32}, {7,33}, {8,22}, {8,26}, 
			{9,59}, {10,24}, {10,32}, {10,46}, {11,16}, {11,57}, {13,40}, {13,54}, {14,40}, {15,38}, 
			{16,43}, {17,28}, {18,40}, {21,47}, {22,27}, {22,28}, {23,24}, {23,45}, {26,43}, {28,49}, 
			{34,42}, {35,42}, {35,43}};
	
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
		for (int i = 0; i < weightArray.length; i++)
			graph.addVertex(i);
		for (int[] edge : edges)
			graph.addEdge(edge[0], edge[1]);
	
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		for (int i = 0; i < weightArray.length; i++)
			weights.put(i, Double.valueOf(weightArray[i]));
	
		assertEquals(IndependentSet.getWeight(graph, weights), 555.0);
	}
	
	public static void testIndependentSet3()
	{
		int[] weightArray = {3, 17, 25, 16, 21, 8, 18, 16, 22, 14, 25, 22, 25, 25, 21, 7, 25, 11, 7, 16, 
			4, 4, 25, 10, 20, 18, 10, 3, 9, 13, 7, 8, 21, 20, 17, 6, 23, 13, 25, 6, 
			1, 14, 4, 21, 22, 5, 11, 1, 13, 7, 23, 5, 2, 20, 12, 11, 10, 0, 23, 5};
	
		int[][] edges = {{1,14}, {1,28}, {1,43}, {2,15}, {2,19}, {3,29}, {3,33}, {4,9}, {4,45}, {5,24}, 
			{5,54}, {6,13}, {6,14}, {6,29}, {6,30}, {6,42}, {6,54}, {9,11}, {10,42}, {16,29}, 
			{16,30}, {18,24}, {18,40}, {21,39}, {21,45}, {23,38}, {24,39}, {27,49}, {27,52}, {29,44}, 
			{32,37}, {33,56}, {36,44}, {41,51}, {43,48}, {47,51}, {48,53}, {51,52}};
	
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
		for (int i = 0; i < weightArray.length; i++)
			graph.addVertex(i);
		for (int[] edge : edges)
			graph.addEdge(edge[0], edge[1]);
	
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		for (int i = 0; i < weightArray.length; i++)
			weights.put(i, Double.valueOf(weightArray[i]));
	
		assertEquals(IndependentSet.getWeight(graph, weights), 608.0);
	}
	
	public static void testIndependentSet4()
	{
		int[] weightArray = {23, 11, 6, 6, 8, 10, 7, 1, 16, 5, 7, 5, 2, 18, 4, 3, 13, 1, 2, 18, 
			0, 8, 10, 10, 25, 9, 18, 3, 19, 2, 15, 24, 15, 17, 22, 10, 10, 24, 5, 0, 
			18, 16, 7, 25, 18, 18, 15, 1, 15, 19, 22, 21, 11, 24, 22, 22, 18, 4, 8, 1};
	
		int[][] edges = {{0,14}, {1,7}, {1,8}, {2,36}, {2,41}, {2,57}, {3,4}, {3,51}, {4,35}, {4,38}, 
			{4,48}, {4,53}, {4,54}, {5,28}, {6,13}, {6,18}, {6,33}, {7,18}, {7,57}, {9,26}, 
			{10,25}, {10,56}, {12,45}, {12,50}, {13,19}, {13,20}, {13,28}, {13,34}, {13,53}, {13,59}, 
			{14,19}, {14,23}, {14,25}, {14,44}, {15,39}, {16,18}, {17,29}, {17,35}, {19,31}, {22,39}, 
			{23,24}, {23,28}, {23,49}, {25,52}, {25,56}, {25,58}, {26,41}, {27,40}, {28,32}, {30,38}, 
			{30,50}, {30,54}, {30,59}, {31,41}, {31,57}, {32,54}, {35,47}, {36,59}, {38,52}, {41,50}, 
			{44,56}, {45,51}, {49,56}, {52,59}};
	
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
		for (int i = 0; i < weightArray.length; i++)
			graph.addVertex(i);
		for (int[] edge : edges)
			graph.addEdge(edge[0], edge[1]);
	
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		for (int i = 0; i < weightArray.length; i++)
			weights.put(i, Double.valueOf(weightArray[i]));
	
		assertEquals(IndependentSet.getWeight(graph, weights), 507.0);
	}
	
	public static void testIndependentSet5()
	{
		int[] weightArray = {5, 16, 5, 19, 8, 24, 25, 23, 9, 4, 6, 4, 13, 14, 14, 13, 12, 0, 18, 21, 
			23, 0, 17, 24, 24, 21, 15, 14, 8, 11, 19, 21, 20, 20, 14, 14, 22, 5, 20, 12, 
			4, 22, 12, 12, 13, 10, 3, 24, 3, 2, 15, 11, 2, 14, 19, 22, 25, 11, 10, 18};
	
		int[][] edges = {{0,24}, {1,29}, {1,32}, {1,59}, {2,11}, {2,23}, {2,42}, {3,47}, {4,16}, {4,34}, 
			{4,42}, {5,48}, {5,51}, {5,58}, {5,59}, {6,24}, {7,15}, {7,17}, {8,22}, {8,26}, 
			{8,49}, {9,55}, {10,18}, {10,44}, {10,47}, {11,37}, {11,38}, {12,53}, {13,23}, {14,22}, 
			{14,45}, {14,56}, {14,57}, {14,58}, {15,37}, {15,55}, {16,21}, {16,52}, {17,19}, {17,30}, 
			{17,43}, {18,26}, {18,30}, {18,34}, {18,42}, {18,53}, {19,57}, {20,21}, {20,47}, {21,24}, 
			{21,43}, {21,56}, {22,51}, {24,56}, {25,31}, {25,44}, {25,59}, {26,27}, {27,34}, {27,36}, 
			{29,51}, {30,39}, {30,43}, {30,51}, {30,54}, {32,46}, {33,38}, {33,50}, {34,41}, {34,43}, 
			{34,55}, {36,38}, {36,50}, {37,56}, {38,40}, {40,53}, {42,45}, {43,56}, {44,50}, {48,59}, 
			{49,50}, {51,59}, {56,57}};
	
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
		for (int i = 0; i < weightArray.length; i++)
			graph.addVertex(i);
		for (int[] edge : edges)
			graph.addEdge(edge[0], edge[1]);
	
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		for (int i = 0; i < weightArray.length; i++)
			weights.put(i, Double.valueOf(weightArray[i]));
	
		assertEquals(IndependentSet.getWeight(graph, weights), 477.0);
	}
}
