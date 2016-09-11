/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2016, by Barak Naveh and Contributors.
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
 * .java
 * -------------------------
 * (C) Copyright 2007-2016, by France Telecom
 *
 * Original Author: Assaf Mizrachi and Contributors.
 * Contributor(s):
 *
 * $Id$
 *
 * Changes
 * -------
 * 11-Sep-2016 : Initial revision (AM);
 *
 */
package org.jgrapht.alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.AsWeightedDirectedGraph;

/**
 * The algorithm determines the k disjoint shortest simple paths in increasing order of
 * weight. Weights can be negative (but no negative cycle is allowed). Only directed graphs
 * are allowed. You can transform an undirected graph to directed using...
 * The algorithm is a variant of Bhandari algorithm so to find an Edge-disjoint shortest
 * paths. In order to find Vertex-disjoint shortest path use SurballeDisjointShortestPaths
 * or use Transformer.
 *
 * <p>
 * The algorithm is using Bellman-Ford to find the shortest path at each step,
 * yielding a complexity of O(k*(k*n*(m^2))) where m is the number of edges and n is the number
 * of vertices.
 *
 * @author Assaf Mizrachi
 * @since September 11, 2016
 */
public class KDisjointShortestPaths<V, E> {
	/**
	 * Graph on which shortest paths are searched.
	 */
	private Graph<V, E> graph;

	private List<List<E>> pathList;

	private int nPaths;

	private V startVertex;

	/**
	 * Creates an object to calculate ranking shortest paths between the start
	 * vertex and others vertices.
	 *
	 * @param graph
	 *            graph on which shortest paths are searched.
	 * @param startVertex
	 *            start vertex of the calculated paths.
	 * @param nPaths
	 *            number of ranking paths between the start vertex and an end
	 *            vertex.
	 *
	 * @throws NullPointerException
	 *             if the specified graph or startVertex is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if nPaths is negative or 0.
	 * @throws IllegalArgumentException
	 *             if nMaxHops is negative or 0.
	 */
	public KDisjointShortestPaths(Graph<V, E> graph, V startVertex, int nPaths) {
		assertKShortestPathsFinder(graph, startVertex, nPaths);

		this.graph = graph;
		this.startVertex = startVertex;
		this.nPaths = nPaths;
	}

	/**
	 * Returns the k shortest simple paths in increasing order of weight.
	 *
	 * @param endVertex
	 *            target vertex of the calculated paths.
	 *
	 * @return list of paths, or <code>null</code> if no path exists between the
	 *         start vertex and the end vertex.
	 */
	public List<GraphPath<V, E>> getPaths(V endVertex) {
		assertGetPaths(endVertex);
		//ensure we have directed weighted graph
		assert(this.graph instanceof DirectedGraph<?, ?>);
		this.graph = new AsWeightedDirectedGraph<V, E>((DirectedGraph<V, E>) graph, new HashMap<>());

		List<E> currentPath;
		this.pathList = new ArrayList<>();
		BellmanFordShortestPath<V, E> bellmanFordShortestPath;
		int cPaths = 1;
		do {
			setUp(cPaths);
			bellmanFordShortestPath = new BellmanFordShortestPath<V, E>(this.graph, this.startVertex);
			currentPath = bellmanFordShortestPath.getPathEdgeList(endVertex);
			if (currentPath != null) {
				cPaths++;				
				pathList.add(currentPath);
			}			
		} while (currentPath != null && cPaths <= this.nPaths);

		return pathList.size() > 0 ? tearDown(endVertex) : null;
	}

	/**
	 * Prepares the graph for a search of the next path:
	 * Replacing the edges of the previous path with reversed edges
	 * with negative weight
	 * 
	 * @param cPath the number of the next path to search 
	 */
	private void setUp(int cPath) {
		//no setup for first path
		if (cPath == 1) {
			return;
		}
		
		V source, target;
		E reversedEdge;
		//replace previous path edges with reversed edges with negative weight
		for (E originalEdge : this.pathList.get(cPath - 2)) {
			source = graph.getEdgeSource(originalEdge);
			target = graph.getEdgeTarget(originalEdge);
			graph.removeEdge(originalEdge);	
			reversedEdge = graph.addEdge(target, source);
			((WeightedGraph<V, E>) graph).setEdgeWeight(reversedEdge, - graph.getEdgeWeight(originalEdge));
		}
	}
	
	/**
	 * At the end of the search we have list intermediate paths - not the complete
	 * paths leading from start to end. Here we go over all, removing overlapping
	 * edges and merging them to valid paths (from start to end). Finally, we sort
	 * them according to their weight.
	 * 
	 * @param endVertex the end vertex
	 * 
	 * @return sorted list of disjoint paths from start to end.
	 */
	private List<GraphPath<V, E>> tearDown(V endVertex) {
		//first we need to remove overlapping edges.		
		removeOverlappingEdges();
		
		//now we might be left with path fragments (not necessarily leading from start to end).
		//We need to merge them to valid paths (from start to end).
		List<GraphPath<V, E>> paths = mergePaths(endVertex);
		
		//sort paths by overall weight (ascending)
		Collections.sort(paths, new Comparator<GraphPath<V, E>>() {

			@Override
			public int compare(GraphPath<V, E> o1, GraphPath<V, E> o2) {
				return (int) (o1.getWeight() - o2.getWeight());
			}
		});
		
		return paths;
	}
	
	/**
	 * After removing overlapping edges, each path is not necessarily connecting
	 * start to end vertex. Here we connect the path fragments to valid paths
	 * (from start to end).
	 * 
	 * @param endVertex the end vertex
	 * 
	 * @return list of disjoint paths from start to end.
	 */
	private List<GraphPath<V, E>> mergePaths(V endVertex) {
		List<GraphPath<V, E>> graphPaths = new ArrayList<GraphPath<V, E>>();
		for (int i = 0; i < this.pathList.size(); i++) {			
			V nextHop = this.startVertex;
			List<E> mergedPath = new ArrayList<E>();
			while (! nextHop.equals(endVertex)) {
				E nextEdge = null;
				//search an edge connecting current and next hops at any of the paths
				//once it is found, add it to the current (merged) path and remove it
				//from the original path so it will not be used anymore.
				for (List<E> path : pathList) {
					boolean connectionFound = false;
					Iterator<E> pathIter = path.iterator();
					while (pathIter.hasNext()) {
						E edge = pathIter.next();
						if (graph.getEdgeSource(edge).equals(nextHop) || graph.getEdgeTarget(edge).equals(nextHop)) {
							nextEdge = edge;
							//remove edge so it will not be used again in any other path.
							pathIter.remove();
							connectionFound = true;
							break;
						}
					}
					if (connectionFound) {
						break;
					}
				}
				if (nextEdge == null) {
					throw new IllegalArgumentException("Could not find a path from start to end vertex");
				}
				mergedPath.add(nextEdge);
				nextHop = graph.getEdgeSource(nextEdge).equals(nextHop) ? 
						graph.getEdgeTarget(nextEdge) : graph.getEdgeSource(nextEdge);
			}
			//path is ready, wrap it up.
			graphPaths.add(new PathWrapper(mergedPath, endVertex));
		}
		return graphPaths;
	}
	
	/**
	 * Iterating over all paths to removes overlapping edges (contained
	 * in more than single path). At the end of this method, each path
	 * contains unique edges but not necessarily connecting the start
	 * to end vertex.
	 * 
	 */
	private void removeOverlappingEdges() {
		Iterator<E> path1Iter, path2Iter;
		E e1, e2;
		boolean found;
		//removing overlapping edges
		for (List<E> path1 : pathList) {
			path1Iter = path1.iterator();
			while (path1Iter.hasNext()) {
				e1 = path1Iter.next();
				found = false;
				for (List<E> path2 : pathList) {
					if (path2 == path1) {
						continue;
					}
					path2Iter = path2.iterator();
					while (path2Iter.hasNext()) {
						e2 = path2Iter.next();
						//graph is directed, checking both options.
						if ((graph.getEdgeSource(e1).equals(graph.getEdgeSource(e2)) &&
								graph.getEdgeTarget(e1).equals(graph.getEdgeTarget(e2))) ||
								
								(graph.getEdgeSource(e1).equals(graph.getEdgeTarget(e2)) &&
										graph.getEdgeTarget(e1).equals(graph.getEdgeSource(e2)))) {
							found = true;
							path2Iter.remove();
						}
					}
				}
				if (found) {
					path1Iter.remove();
				}
			}
		}
		
	}

	private void assertGetPaths(V endVertex) {
		if (endVertex == null) {
			throw new NullPointerException("endVertex is null");
		}
		if (endVertex.equals(this.startVertex)) {
			throw new IllegalArgumentException("The end vertex is the same as the start vertex!");
		}
		if (!this.graph.vertexSet().contains(endVertex)) {
			throw new IllegalArgumentException("Graph must contain the end vertex!");
		}
	}

	private void assertKShortestPathsFinder(Graph<V, E> graph, V startVertex, int nPaths) {
		if (graph == null) {
			throw new NullPointerException("graph is null");
		}
		if (startVertex == null) {
			throw new NullPointerException("startVertex is null");
		}
		if (nPaths <= 0) {
			throw new NullPointerException("nPaths is negative or 0");
		}
	}

	private class PathWrapper implements GraphPath<V, E> {

		private List<E> edgeList;
		private V endVertex;
		private double weight;

		PathWrapper(List<E> edgeList, V endVertex) {
			this.edgeList = edgeList;
			this.endVertex = endVertex;
			for (E edge : edgeList) {
				weight += graph.getEdgeWeight(edge);
			}
		}

		// implement GraphPath
		@Override
		public Graph<V, E> getGraph() {
			return graph;
		}

		// implement GraphPath
		@Override
		public V getStartVertex() {
			return startVertex;
		}

		// implement GraphPath
		@Override
		public V getEndVertex() {
			return endVertex;
		}

		// implement GraphPath
		@Override
		public List<E> getEdgeList() {
			return edgeList;
		}

		// implement GraphPath
		@Override
		public double getWeight() {
			return weight;
		}

		// override Object
		@Override
		public String toString() {
			return edgeList.toString();
		}
	}
}

// End .java
