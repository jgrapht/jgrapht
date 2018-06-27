package org.jgrapht.alg.vertexcover;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.VertexCoverAlgorithm;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;
import java.util.stream.Collectors;

public class TreeVCImpl<V, E> implements VertexCoverAlgorithm<V> {

    private final Graph<V, E> graph;
    private final Set<V> roots;

    private VertexCover<V> minVertexCover = null;

    public TreeVCImpl(Graph<V, E> graph, V root){
        this(graph, Collections.singleton(Objects.requireNonNull(root, "Root cannot be null")));
    }

    public TreeVCImpl(Graph<V, E> graph, Set<V> roots) {
        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "Set of roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("Set of roots cannot be empty");
    }


    private void bfs(V root, Set<V> visited, Map<V, V> parent){
        BreadthFirstIterator<V, E> bfs = new BreadthFirstIterator<>(graph, root);

        while (bfs.hasNext()) {
            V u = bfs.next();
            visited.add(u);
            V p = bfs.getParent(u);

            if (p != null)
                parent.put(u, p);
        }
    }

    private void computeMinimumVertexCover(){
        Map<V, V> parent =  new HashMap<>();
        Set<V> visited = new HashSet<>();

        for (V root: roots)
            if (!visited.contains(root))
                bfs(root, visited, parent);

        Set<V> leaves = new HashSet<>();
        Set<V> vc = new HashSet<>();
        Set<V> deleted = new HashSet<>();

        for (V v: graph.vertexSet())
            if (!parent.values().contains(v))
                leaves.add(v);
        do {
            Set<V> parents = new HashSet<>();
            Set<V> grandparents = new HashSet<>();

            for (V leaf: leaves){
                V p = parent.get(leaf);

                if (p != null) {
                    parents.add(p);
                    V pp = parent.get(p);

                    if (pp != null && !deleted.contains(pp))
                        grandparents.add(pp);
                }
            }

            deleted.addAll(leaves);
            deleted.addAll(parents);

            vc.addAll(parents);

            grandparents.removeAll(parents);
            grandparents.removeAll(grandparents.stream().map(parent::get).collect(Collectors.toSet()));

            leaves = grandparents;

        } while (!leaves.isEmpty());

        minVertexCover = new VertexCoverImpl<>(vc);
    }

    @Override
    public VertexCover<V> getVertexCover() {
        if (minVertexCover == null)
            computeMinimumVertexCover();

        return minVertexCover;
    }
}
