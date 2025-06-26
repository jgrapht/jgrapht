package org.jgrapht.alg.steiner;

import java.util.*;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import org.jgrapht.alg.interfaces.SteinerTreeAlgorithm;

public class KouMarkowskyBermanAlgorithm<V, E extends DefaultWeightedEdge> implements SteinerTreeAlgorithm<V, E> {

	private final Graph<V, E> graph;

	public KouMarkowskyBermanAlgorithm(Graph<V, E> graph) {
		this.graph = graph;
	}

	@Override
	public SteinerTree<E> getSpanningTree(Set<V> steinerPoints) {
		// Step 1: Create the complete distance graph of selected vertices
		DijkstraShortestPath<V, E> dijkstraAlg = new DijkstraShortestPath<>(graph);

		Graph<V, E> completeGraph = new SimpleWeightedGraph<V, E>(graph.getVertexSupplier(), graph.getEdgeSupplier());
		for (V vertex : steinerPoints) {
			completeGraph.addVertex(vertex);
		}

		Map<Pair<V, V>, GraphPath<V, E>> storePaths = new HashMap<>();
		List<V> selectedList = new ArrayList<>(steinerPoints);

		for (int i = 0; i < selectedList.size(); i++) {
			for (int j = i + 1; j < selectedList.size(); j++) {
				V source = selectedList.get(i);
				V target = selectedList.get(j);
				GraphPath<V, E> path = dijkstraAlg.getPath(source, target);
				storePaths.put(Pair.of(source, target), path);

				E edge = completeGraph.addEdge(source, target);
				if (edge != null && path != null) {
					completeGraph.setEdgeWeight(edge, Math.round(path.getWeight() * 10) / 10.0);
				}
			}
		}

		// Step 2: MST of complete distance graph
		KruskalMinimumSpanningTree<V, E> kruskal = new KruskalMinimumSpanningTree<>(completeGraph);
		Graph<V, E> mstGraph = new SimpleWeightedGraph<V, E>(graph.getVertexSupplier(), graph.getEdgeSupplier());

		for (V vertex : completeGraph.vertexSet()) {
			mstGraph.addVertex(vertex);
		}
		for (E edge : kruskal.getSpanningTree().getEdges()) {
			V source = completeGraph.getEdgeSource(edge);
			V target = completeGraph.getEdgeTarget(edge);
			E newEdge = mstGraph.addEdge(source, target);
			if (newEdge != null) {
				mstGraph.setEdgeWeight(newEdge, completeGraph.getEdgeWeight(edge));
			}
		}

		// Step 3: Recreate full paths
		Graph<V, E> mstPathGraph = new SimpleWeightedGraph<V, E>(graph.getVertexSupplier(), graph.getEdgeSupplier());

		for (E edge : mstGraph.edgeSet()) {
			V source = mstGraph.getEdgeSource(edge);
			V target = mstGraph.getEdgeTarget(edge);

			GraphPath<V, E> path = storePaths.get(Pair.of(source, target));
			if (path == null)
				path = storePaths.get(Pair.of(target, source));
			if (path == null)
				continue;

			List<V> vertices = path.getVertexList();
			for (int i = 0; i < vertices.size() - 1; i++) {
				V v1 = vertices.get(i);
				V v2 = vertices.get(i + 1);

				mstPathGraph.addVertex(v1);
				mstPathGraph.addVertex(v2);

				if (!mstPathGraph.containsEdge(v1, v2)) {
					E originalEdge = graph.getEdge(v1, v2);
					if (originalEdge != null) {
						E newEdge = mstPathGraph.addEdge(v1, v2);
						if (newEdge != null) {
							mstPathGraph.setEdgeWeight(newEdge, graph.getEdgeWeight(originalEdge));
						}
					}
				}
			}
		}

		// Step 4: Final MST of expanded graph
		KruskalMinimumSpanningTree<V, E> kruskal1 = new KruskalMinimumSpanningTree<>(mstPathGraph);
		Graph<V, E> finalMST = new SimpleWeightedGraph<V, E>(graph.getVertexSupplier(), graph.getEdgeSupplier());

		for (V vertex : mstPathGraph.vertexSet()) {
			finalMST.addVertex(vertex);
		}
		for (E edge : kruskal1.getSpanningTree().getEdges()) {
			V source = mstPathGraph.getEdgeSource(edge);
			V target = mstPathGraph.getEdgeTarget(edge);
			E newEdge = finalMST.addEdge(source, target);
			if (newEdge != null) {
				finalMST.setEdgeWeight(newEdge, mstPathGraph.getEdgeWeight(edge));
			}
		}

		// Step 5: Prune non-Steiner leaves
		Set<V> leaves = MSTLeavesFinder.findLeaves(finalMST);
		while (true) {
			int removed = 0;
			for (V leaf : new HashSet<>(leaves)) {
				if (!steinerPoints.contains(leaf)) {
					finalMST.removeVertex(leaf);
					removed++;
				}
			}
			if (removed == 0)
				break;
			leaves = MSTLeavesFinder.findLeaves(finalMST);
		}

		double totalWeight = finalMST.edgeSet().stream().mapToDouble(finalMST::getEdgeWeight).sum();

		return new SpeinerTreeImpl<>(finalMST.edgeSet(), totalWeight);
	}

	private static class MSTLeavesFinder {
		public static <V, E> Set<V> findLeaves(Graph<V, E> graph) {
			Set<V> leaves = new HashSet<>();
			for (V vertex : graph.vertexSet()) {
				if (graph.degreeOf(vertex) == 1) {
					leaves.add(vertex);
				}
			}
			return leaves;
		}
	}

}
