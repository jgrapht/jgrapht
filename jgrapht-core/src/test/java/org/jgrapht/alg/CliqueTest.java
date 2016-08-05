package org.jgrapht.alg;

import java.util.HashMap;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import junit.framework.TestCase;

/**
 * Tests for the maximum weight clique algorithms.
 *
 * @author Nils Olberg
 */

public class CliqueTest extends TestCase {
	 
	/**
     * Tests the algorithm for the maximum weight clique problem.
     */
	
	public static void testClique1()
	{
		int[] weightArray = {18, 0, 8, 21, 20, 5, 25, 24, 7, 9, 23, 13, 8, 15, 2, 17, 6, 10, 17, 16, 
			15, 0, 17, 13, 23, 1, 0, 20, 12, 10, 13, 1, 21, 18, 16, 17, 17, 15, 19, 19, 
			14, 5, 20, 13, 0, 21, 14, 3, 22, 22, 18, 3, 1, 3, 10, 20, 25, 22, 10, 16};
	
		int[][] edges = {{0,4}, {0,18}, {0,36}, {0,49}, {0,54}, {1,2}, {1,7}, {1,9}, {1,12}, {1,23}, 
			{1,24}, {1,28}, {2,8}, {2,9}, {2,30}, {2,32}, {2,34}, {2,46}, {3,30}, {3,31}, 
			{3,49}, {3,57}, {4,31}, {4,35}, {4,36}, {4,39}, {4,41}, {4,56}, {5,15}, {5,17}, 
			{5,24}, {5,32}, {5,42}, {5,59}, {6,16}, {6,30}, {6,34}, {6,36}, {6,49}, {7,14}, 
			{7,18}, {7,42}, {7,43}, {7,56}, {7,57}, {8,13}, {8,23}, {8,27}, {8,32}, {9,19}, 
			{9,35}, {9,45}, {9,52}, {10,15}, {10,19}, {10,20}, {10,26}, {10,44}, {10,50}, {10,59}, 
			{11,31}, {11,32}, {11,35}, {11,49}, {11,52}, {12,14}, {12,26}, {12,43}, {12,54}, {13,35}, 
			{13,39}, {13,52}, {13,54}, {14,17}, {14,27}, {14,36}, {14,48}, {14,49}, {15,24}, {15,26}, 
			{15,30}, {15,38}, {15,39}, {16,27}, {16,29}, {16,30}, {16,48}, {16,49}, {17,20}, {17,50}, 
			{17,51}, {17,55}, {18,20}, {18,28}, {18,33}, {18,41}, {18,58}, {19,20}, {19,27}, {19,31}, 
			{19,47}, {20,32}, {20,37}, {20,38}, {21,27}, {21,35}, {21,37}, {22,58}, {23,44}, {23,53}, 
			{24,27}, {24,30}, {24,43}, {24,45}, {24,54}, {25,29}, {25,34}, {25,35}, {25,38}, {25,46}, 
			{25,56}, {25,58}, {26,39}, {26,46}, {26,52}, {27,50}, {28,32}, {28,43}, {30,38}, {30,39}, 
			{30,52}, {32,34}, {32,40}, {32,51}, {32,52}, {32,55}, {33,34}, {33,43}, {33,51}, {34,54}, 
			{35,41}, {35,51}, {36,51}, {37,57}, {38,49}, {38,55}, {39,44}, {39,55}, {40,47}, {40,49}, 
			{41,43}, {41,56}, {41,57}, {42,45}, {42,50}, {43,44}, {43,50}, {44,55}, {44,57}, {46,49}, 
			{46,53}, {46,55}, {47,48}, {47,50}, {47,51}, {49,51}, {49,58}, {50,57}, {50,59}, {52,57}, 
			{55,58}, {57,59}};
	
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
		for (int i = 0; i < weightArray.length; i++)
			graph.addVertex(i);
		for (int[] edge : edges)
			graph.addEdge(edge[0], edge[1]);
	
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		for (int i = 0; i < weightArray.length; i++)
			weights.put(i, Double.valueOf(weightArray[i]));
	
		assertEquals(Clique.getWeight(graph, weights), 57.0);
	}
	
	public static void testClique2()
	{
		int[] weightArray = {25, 15, 7, 2, 11, 1, 3, 16, 2, 2, 8, 25, 18, 9, 0, 0, 1, 13, 5, 18, 
			7, 14, 18, 10, 19, 12, 14, 2, 3, 24, 6, 12, 15, 8, 18, 10, 22, 18, 22, 24, 
			23, 7, 10, 0, 8, 5, 23, 12, 7, 24, 17, 11, 8, 2, 5, 22, 5, 20, 13, 21};
	
		int[][] edges = {{2,3}, {2,15}, {2,33}, {3,19}, {3,39}, {4,23}, {4,36}, {5,41}, {6,7}, {6,40}, 
			{7,29}, {7,48}, {8,43}, {8,53}, {10,19}, {10,22}, {10,38}, {11,13}, {11,26}, {11,36}, 
			{11,49}, {11,55}, {12,24}, {13,29}, {14,45}, {14,51}, {15,21}, {16,29}, {17,35}, {17,52}, 
			{18,32}, {18,57}, {19,41}, {20,26}, {20,33}, {20,40}, {20,42}, {21,41}, {22,44}, {22,52}, 
			{23,36}, {23,52}, {23,54}, {25,47}, {26,27}, {27,48}, {28,47}, {28,52}, {29,58}, {30,38}, 
			{30,47}, {31,40}, {32,33}, {33,41}, {33,47}, {34,44}, {34,46}, {35,39}, {37,38}, {39,53}, 
			{39,56}, {41,57}, {42,44}, {42,57}, {43,55}, {48,54}, {53,58}, {53,59}, {57,59}};
	
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
		for (int i = 0; i < weightArray.length; i++)
			graph.addVertex(i);
		for (int[] edge : edges)
			graph.addEdge(edge[0], edge[1]);
	
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		for (int i = 0; i < weightArray.length; i++)
			weights.put(i, Double.valueOf(weightArray[i]));
	
		assertEquals(Clique.getWeight(graph, weights), 49.0);
	}
	
	public static void testClique3()
	{
		int[] weightArray = {17, 5, 18, 15, 2, 6, 3, 24, 5, 11, 17, 0, 4, 18, 0, 25, 24, 5, 18, 24, 
			21, 17, 14, 4, 1, 22, 14, 13, 24, 9, 12, 21, 0, 5, 13, 19, 18, 7, 25, 3, 
			20, 21, 12, 14, 24, 18, 19, 17, 22, 8, 24, 6, 15, 24, 10, 19, 13, 9, 8, 1};
	
		int[][] edges = {{0,2}, {1,54}, {4,5}, {7,42}, {10,43}, {11,12}, {13,37}, {18,25}, {21,57}, {22,31}, 
			{22,57}, {26,38}, {28,44}, {33,47}, {34,45}, {36,38}, {42,55}, {49,54}, {57,59}};
	
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
		for (int i = 0; i < weightArray.length; i++)
			graph.addVertex(i);
		for (int[] edge : edges)
			graph.addEdge(edge[0], edge[1]);
	
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		for (int i = 0; i < weightArray.length; i++)
			weights.put(i, Double.valueOf(weightArray[i]));
	
		assertEquals(Clique.getWeight(graph, weights), 48.0);
	}
	
	public static void testClique4()
	{
		int[] weightArray = {25, 15, 15, 10, 21, 3, 21, 3, 13, 24, 10, 13, 14, 17, 23, 10, 23, 7, 25, 1, 
			14, 15, 12, 24, 23, 16, 1, 25, 18, 21, 16, 24, 18, 25, 10, 25, 23, 6, 16, 22, 
			23, 22, 18, 8, 1, 16, 25, 7, 24, 17, 20, 7, 25, 16, 3, 11, 24, 0, 14, 24};
	
		int[][] edges = {{0,12}, {1,58}, {12,24}, {12,50}, {15,41}, {16,21}, {19,41}, {22,36}, {25,26}, {26,33}};
	
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
		for (int i = 0; i < weightArray.length; i++)
			graph.addVertex(i);
		for (int[] edge : edges)
			graph.addEdge(edge[0], edge[1]);
	
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		for (int i = 0; i < weightArray.length; i++)
			weights.put(i, Double.valueOf(weightArray[i]));
	
		assertEquals(Clique.getWeight(graph, weights), 39.0);
	}
	
	public static void testClique5()
	{
		int[] weightArray = {11, 16, 4, 24, 18, 21, 8, 5, 11, 6, 23, 24, 16, 22, 18, 9, 17, 22, 13, 25, 
			5, 22, 17, 9, 8, 7, 8, 0, 14, 23, 12, 10, 14, 4, 19, 22, 15, 23, 9, 9, 
			13, 21, 8, 5, 17, 1, 22, 15, 11, 21, 8, 10, 17, 0, 10, 11, 4, 1, 3, 12};
	
		int[][] edges = {{0,4}, {0,10}, {1,18}, {1,22}, {1,25}, {1,34}, {1,38}, {1,55}, {2,5}, {2,15}, 
			{2,21}, {2,24}, {2,34}, {2,35}, {2,47}, {3,7}, {3,16}, {3,21}, {3,23}, {3,25}, 
			{3,58}, {4,7}, {4,24}, {4,51}, {4,58}, {5,43}, {5,45}, {5,49}, {5,57}, {6,7}, 
			{6,45}, {7,25}, {7,26}, {7,51}, {7,53}, {8,24}, {8,51}, {9,16}, {9,26}, {9,27}, 
			{9,46}, {9,50}, {9,56}, {10,12}, {10,21}, {10,39}, {11,13}, {11,19}, {11,22}, {11,28}, 
			{11,39}, {12,34}, {12,37}, {12,40}, {12,46}, {12,48}, {12,51}, {13,23}, {13,49}, {13,52}, 
			{13,57}, {13,59}, {14,25}, {14,38}, {14,50}, {14,55}, {15,26}, {15,42}, {15,45}, {15,49}, 
			{15,56}, {16,24}, {16,33}, {17,30}, {18,26}, {18,54}, {18,56}, {18,58}, {19,37}, {19,41}, 
			{19,46}, {19,52}, {19,54}, {19,55}, {19,59}, {20,22}, {20,28}, {20,38}, {20,46}, {21,30}, 
			{21,51}, {21,55}, {22,27}, {22,43}, {22,46}, {23,44}, {23,47}, {23,48}, {23,53}, {23,57}, 
			{24,35}, {24,51}, {24,58}, {25,32}, {25,37}, {25,51}, {26,31}, {26,53}, {27,39}, {27,44}, 
			{27,46}, {27,51}, {28,42}, {28,56}, {30,39}, {30,40}, {30,44}, {30,52}, {31,41}, {31,47}, 
			{31,57}, {31,59}, {32,44}, {32,47}, {32,57}, {33,36}, {33,43}, {33,58}, {34,41}, {34,51}, 
			{34,55}, {35,37}, {35,52}, {35,57}, {36,42}, {37,41}, {38,44}, {40,50}, {41,53}, {42,44}, 
			{42,47}, {42,51}, {42,55}, {42,59}, {44,55}, {46,50}, {47,53}, {47,55}, {50,58}, {53,58}, 
			{54,57}};
	
		UndirectedGraph<Integer, DefaultEdge> graph = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
	
		for (int i = 0; i < weightArray.length; i++)
			graph.addVertex(i);
		for (int[] edge : edges)
			graph.addEdge(edge[0], edge[1]);
	
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		for (int i = 0; i < weightArray.length; i++)
			weights.put(i, Double.valueOf(weightArray[i]));
	
		assertEquals(Clique.getWeight(graph, weights), 69.0);
	}
}
