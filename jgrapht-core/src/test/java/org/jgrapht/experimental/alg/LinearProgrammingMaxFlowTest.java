/**
 * 
 */
package org.jgrapht.experimental.alg;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import org.jgrapht.alg.EdmondsKarpMaximumFlow;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

/**
 * @author Yubin Bai
 * 
 */
public class LinearProgrammingMaxFlowTest extends TestCase {
	int numOfVertices = 10;
	int vertexNumMultiplier = 2;

	public void testLinearProgrammingMaxFlow() {

		DirectedWeightedMultigraph<String, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<String, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		for (int i = 0; i < this.numOfVertices; i++)
			graph.addVertex("v" + i);

		Set<String> vertices = graph.vertexSet();

		Random rand = new Random();
		for (String start : vertices)
			for (String end : vertices)
				if (!start.equals(end) && !graph.containsEdge(start, end)) {
					Double newDouble = new Double(rand.nextDouble());
					DefaultWeightedEdge e = graph.addEdge(start, end);
					graph.setEdgeWeight(e, newDouble.doubleValue());

					// System.out.println(graph.getEdgeWeight(e));
				}

		// System.out.println(graph.vertexSet().toString());

		LinearProgrammingMaxFlow<String, DefaultWeightedEdge> lpnf = new LinearProgrammingMaxFlow<String, DefaultWeightedEdge>(
				graph);
		Iterator<String> iter = vertices.iterator();
		String source = iter.next();
		String sink = iter.next();
		lpnf.calculateMaximumFlow(source, sink);

		// compare with the Edmonds Karp method
		EdmondsKarpMaximumFlow<String, DefaultWeightedEdge> eknf = new EdmondsKarpMaximumFlow<String, DefaultWeightedEdge>(
				graph);
		eknf.calculateMaximumFlow(source, sink);

		assertEquals(lpnf.getMaximumFlowValue(), eknf.getMaximumFlowValue(),
				1E-6);

		Map<DefaultWeightedEdge, Double> m = eknf.getMaximumFlow();
		Map<DefaultWeightedEdge, Double> m2 = lpnf.getMaximumFlow();
		for (DefaultWeightedEdge e : graph.edgeSet()) {
			assertTrue(m.get(e) >= 0);
			assertTrue(m.get(e) <= graph.getEdgeWeight(e));
			assertTrue(m2.get(e) >= 0);
			assertTrue(m2.get(e) <= graph.getEdgeWeight(e));
		}

//		System.out.println("The flow is " + lpnf.getMaximumFlowValue());
//		System.out.println("The flow is " + eknf.getMaximumFlowValue());
	}

	public void testLPEfficiency() {
		DirectedWeightedMultigraph<String, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<String, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		for (int i = 0; i < this.numOfVertices * this.vertexNumMultiplier; i++)
			graph.addVertex("v" + i);

		Set<String> vertices = graph.vertexSet();

		Random rand = new Random();
		for (String start : vertices)
			for (String end : vertices)
				if (!start.equals(end) && !graph.containsEdge(start, end)) {
					Double newDouble = new Double(rand.nextDouble());
					DefaultWeightedEdge e = graph.addEdge(start, end);
					graph.setEdgeWeight(e, newDouble.doubleValue());

					// System.out.println(graph.getEdgeWeight(e));
				}

		// System.out.println(graph.vertexSet().toString());

		LinearProgrammingMaxFlow<String, DefaultWeightedEdge> lpnf = new LinearProgrammingMaxFlow<String, DefaultWeightedEdge>(
				graph);
		Iterator<String> iter = vertices.iterator();
		String source = iter.next();
		String sink = iter.next();
		lpnf.calculateMaximumFlow(source, sink);
		// System.out.println("The flow is " + lpnf.getMaximumFlowValue());
	}

	public void testEKEfficiency() {
		DirectedWeightedMultigraph<String, DefaultWeightedEdge> graph = new DirectedWeightedMultigraph<String, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		for (int i = 0; i < this.numOfVertices * this.vertexNumMultiplier; i++)
			graph.addVertex("v" + i);

		Set<String> vertices = graph.vertexSet();

		Random rand = new Random();
		for (String start : vertices)
			for (String end : vertices)
				if (!start.equals(end) && !graph.containsEdge(start, end)) {
					Double newDouble = new Double(rand.nextDouble());
					DefaultWeightedEdge e = graph.addEdge(start, end);
					graph.setEdgeWeight(e, newDouble.doubleValue());

					// System.out.println(graph.getEdgeWeight(e));
				}

		// System.out.println(graph.vertexSet().toString());

		Iterator<String> iter = vertices.iterator();
		String source = iter.next();
		String sink = iter.next();

		// compare with the Edmonds Karp method
		EdmondsKarpMaximumFlow<String, DefaultWeightedEdge> eknf = new EdmondsKarpMaximumFlow<String, DefaultWeightedEdge>(
				graph);
		eknf.calculateMaximumFlow(source, sink);

		// System.out.println("The flow is " + eknf.getMaximumFlowValue());

	}

}
