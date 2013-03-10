package org.jgrapht.experimental.alg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.linear.LinearConstraint;
import org.apache.commons.math3.optimization.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optimization.linear.Relationship;
import org.apache.commons.math3.optimization.linear.SimplexSolver;
import org.jgrapht.DirectedGraph;

/**
 * Convert max network flow problem to linear programming problem and solve with
 * simplex solver.
 * 
 * With higher efficiency linear solver, this method has comparable complexity
 * with the Edmonds-Karp algorithm
 * 
 * 
 * @author Yubin Bai
 * 
 * @param <V>
 * @param <E>
 */
public class LinearProgrammingMaxFlow<V, E> {

	DirectedGraph<V, E> graph; // a reference to the flow network
	Set<V> vertices; // vertices in the network
	ArrayList<E> edgesIndex;
	Map<E, Double> maximumFlow; // optimized maximum flow
	Double maximumFlowValue; // current maximum flow value
	int numOfEdges;

	public LinearProgrammingMaxFlow(DirectedGraph<V, E> graph) {

		this.graph = graph;
		this.vertices = graph.vertexSet();
		this.edgesIndex = new ArrayList<E>();
		this.numOfEdges = graph.edgeSet().size();
		this.maximumFlow = new HashMap<E, Double>();
	}

	/**
	 * Invoke the apache commons simplex solver to calculate the max network
	 * flow
	 * 
	 * @param source
	 * @param sink
	 */
	public void calculateMaximumFlow(V source, V sink) {

		// define actual flow as variables for the linear optimization
		// init flow to zero
		int edgeCounter = 0;
		for (E e : graph.edgeSet()) {
			edgesIndex.add(edgeCounter, e);
			edgeCounter++;
			maximumFlow.put(e, 0.0);
		}

		Collection<LinearConstraint> constraints = new ArrayList<LinearConstraint>();

		// set flow capacity constraints
		for (E edge : graph.edgeSet()) {

			double[] coef = new double[this.numOfEdges];
			for (int i = 0; i < this.numOfEdges; i++)
				coef[i] = 0;
			coef[edgesIndex.indexOf(edge)] = 1.0;

			constraints.add(new LinearConstraint(coef, Relationship.GEQ, 0));
			constraints.add(new LinearConstraint(coef, Relationship.LEQ, graph
					.getEdgeWeight(edge)));

		}

		// set flow conservation constraints
		// for vertices other than source and sink, net flow should be zero
		for (V v : vertices)
			if (!v.equals(source) && !v.equals(sink)) {
				double[] coef = new double[this.numOfEdges];
				for (int i = 0; i < this.numOfEdges; i++)
					coef[i] = 0;
				for (V otherV : vertices)
					if (!v.equals(otherV)) {
						coef[edgesIndex.indexOf(graph.getEdge(v, otherV))] = 1;
						coef[edgesIndex.indexOf(graph.getEdge(otherV, v))] = -1;
					}

				constraints.add(new LinearConstraint(coef, Relationship.EQ, 0));

			}

		// setup objective function
		// (maximize the flow out of source - flow into source)
		double[] coef = new double[this.numOfEdges];
		for (int i = 0; i < this.numOfEdges; i++)
			coef[i] = 0;

		for (V otherV : vertices)
			if (!source.equals(otherV)) {
				coef[edgesIndex.indexOf(graph.getEdge(source, otherV))] = 1;
				coef[edgesIndex.indexOf(graph.getEdge(otherV, source))] = -1;
			}

		LinearObjectiveFunction f = new LinearObjectiveFunction(coef, 0);

		// setup solver and solve linear problem
		SimplexSolver solver = new SimplexSolver(1E-15, 100);
		solver.setMaxIterations(Integer.MAX_VALUE);
		PointValuePair solution = solver.optimize(f, constraints,
				GoalType.MAXIMIZE, false);

		// save output
		this.maximumFlowValue = solution.getValue();
		double[] flowValues = new double[this.numOfEdges];
		for (E edge : graph.edgeSet()) {
			maximumFlow.put(edge, flowValues[edgesIndex.indexOf(edge)]);
		}

	}

	public Double getMaximumFlowValue() {

		return this.maximumFlowValue;
	}

	public Map<E, Double> getMaximumFlow() {
		return this.maximumFlow;

	}
}