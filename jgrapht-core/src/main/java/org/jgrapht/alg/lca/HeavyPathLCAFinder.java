package org.jgrapht.alg.lca;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.alg.decomposition.HeavyPathDecomposition;
import org.jgrapht.alg.interfaces.LCAAlgorithm;

import java.util.*;

public class HeavyPathLCAFinder<V, E> implements LCAAlgorithm<V> {
    private final Graph<V, E> graph;
    private final Set<V> roots;
    private HeavyPathDecomposition<V, E> heavyPath;

    public HeavyPathLCAFinder(Graph<V, E> graph, V root){
        assert GraphTests.isForest(graph);

        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Collections.singleton(Objects.requireNonNull(root, "Root cannot be null"));
    }

    public HeavyPathLCAFinder(Graph<V, E> graph, Set<V> roots){
        assert GraphTests.isForest(graph);

        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "Roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("Roots cannot be empty");
    }

    private Map<V, V> father;
    private Map<V, Integer> depth;

    private Map<V, Integer> path;
    private Map<V, Integer> positionInPath;
    private List<V> firstNode;

    private void computeHeavyPathDecomposition(){
        if (heavyPath != null)
            return;

        heavyPath = new HeavyPathDecomposition<>(graph, roots);

        father = heavyPath.getFather();
        depth = heavyPath.getDepth();

        path = new HashMap<>();
        positionInPath = new HashMap<>();
        firstNode = new ArrayList<>();

        List<List<V>> paths = heavyPath.getPaths();

        for (int i = 0; i < paths.size(); i++){
            List<V> p = paths.get(i);
            firstNode.add(p.get(0));

            for (int j = 0; j < p.size(); j++) {
                positionInPath.put(p.get(j), j);
                path.put(p.get(j), i);
            }
        }
    }

//    int lca( int x, int y )
//    {
//        while ( path[x] != path[y] )
//        {
//            if ( depth[ first_node[ path[x] ] ] < depth[ first_node[ path[y] ] ] )
//                y = father[ first_node[ path[y] ] ];
//            else
//                x = father[ first_node[ path[x] ] ];
//        }
//
//        return pos_in_path[x] < pos_in_path[y] ? x : y;
//    }

    @Override
    public V getLCA(V a, V b) {
        computeHeavyPathDecomposition();

        if (a.equals(b))
            return a;

        while (true){
            if (!path.containsKey(a) || !path.containsKey(b))
                return null;

            int pathA = path.get(a);
            int pathB = path.get(b);

            if (pathA == pathB)
                break;

            V firstNodePathA = firstNode.get(pathA);
            V firstNodePathB = firstNode.get(pathB);
            
            if (depth.get(firstNodePathA) < depth.get(firstNodePathB))
                b = father.get(firstNodePathB);
            else
                a = father.get(firstNodePathA);
        }

        return positionInPath.get(a) < positionInPath.get(b) ? a : b;
    }
}
