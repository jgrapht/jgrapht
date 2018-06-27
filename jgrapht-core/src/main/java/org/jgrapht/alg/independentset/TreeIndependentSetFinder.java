package org.jgrapht.alg.independentset;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.IndependentSetAlgorithm;
import org.jgrapht.alg.vertexcover.TreeVCImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TreeIndependentSetFinder<V, E> implements IndependentSetAlgorithm<V> {

    private final Graph<V, E> graph;
    private final Set<V> roots;

    private IndependentSetAlgorithm.IndependentSet<V> maxIndSet = null;

    public TreeIndependentSetFinder(Graph<V, E> graph, V root){
        this(graph, Collections.singleton(Objects.requireNonNull(root, "Root cannot be null")));
    }

    public TreeIndependentSetFinder(Graph<V, E> graph, Set<V> roots) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "Set of roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("Set of roots cannot be empty");
    }

    @Override
    public IndependentSet<V> getIndependentSet() {
        if (maxIndSet == null){
            Set<V> vertices = new HashSet<>(graph.vertexSet());
            vertices.removeAll(new TreeVCImpl<>(graph, roots).getVertexCover());
            maxIndSet = new IndependentSetImpl<>(vertices);
        }

        return maxIndSet;
    }
}
