package jgraph_v1;

import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Pseudograph;

/**
 * Test for the BipartiteColoring class.
 * 
 * @author Meghana M Reddy
 *
 */
public class BipartiteColoringTest {
	public void test1() {
		Graph<String, DefaultEdge> dg = new Pseudograph<>(DefaultEdge.class);
	
		String a = new String("a");
		String b = new String("b");
		String c = new String("c");
		String d = new String("d");
		String e = new String("e");
		String f = new String("f");
		String g = new String("g");
		
		dg.addVertex(a);
		dg.addVertex(b);
		dg.addVertex(c);
		dg.addVertex(d);
		dg.addVertex(e);
		dg.addVertex(f);
		dg.addVertex(g);
		dg.addEdge(a, d);
		dg.addEdge(a, e);
		dg.addEdge(g, a);
		dg.addEdge(c, e);
		dg.addEdge(c, f);
		dg.addEdge(b, g);
		
		BipartiteColoring<String, DefaultEdge> bipartiteColorer = new BipartiteColoring<>(dg);
		Map<String, Integer> ccs = bipartiteColorer.findTwoColoring(dg);
		System.out.println(ccs);
	}
	
	public static void main(String[] args) {
		BipartiteColoringTest tester = new BipartiteColoringTest();
		tester.test1();
	}
}
