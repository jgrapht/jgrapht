package org.jgrapht.alg.lca;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.LCAAlgorithm;

import java.util.*;

public class BinaryLiftingLCAFinder<V, E> implements LCAAlgorithm<V> {
    private final Graph<V, E> graph;
    private final V root;
    private final int MAX_LEVEL;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    private int[][] ancestors;
    private int[] timeIn, timeOut;
    private int clock = 0;

    public BinaryLiftingLCAFinder(Graph<V, E> graph, V root){
        assert GraphTests.isForest(graph);

        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.root = Objects.requireNonNull(root, "Root cannot be null");
        this.MAX_LEVEL = log2(graph.vertexSet().size());
    }

    private void dfs(int u, int parent){
        timeIn[u] = ++clock;

        ancestors[0][u] = parent;
        for (int l = 1; l < MAX_LEVEL; l++) {
            if (ancestors[l - 1][u] != -1)
                ancestors[l][u] = ancestors[l - 1][ancestors[l - 1][u]];
        }

        V vertexU = indexList.get(u);
        for (E edge: graph.edgesOf(vertexU)){
            int v = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

            if (v != parent){
                dfs(v, u);
            }
        }

        timeOut[u] = ++clock;
    }

    private boolean isAncestor(int ancestor, int descendant) {
        return timeIn[ancestor] <= timeIn[descendant] && timeOut[descendant] <= timeOut[ancestor];
    }

    @Override
    public V getLCA(V a, V b) {
        computeAncestorMatrix();

        int x = vertexMap.get(a);
        int y = vertexMap.get(b);

        if (x == y)
            return a;

        if (timeIn[x] == 0 || timeIn[y] == 0)
            return null;

        if (isAncestor(x, y))
            return a;

        if (isAncestor(y, x))
            return b;

        for (int l = MAX_LEVEL - 1; l >= 0; l--)
            if (ancestors[l][x] != -1 && !isAncestor(ancestors[l][x], y))
                x = ancestors[l][x];

        if (ancestors[0][x] == -1)
            return null;
        else
            return indexList.get(ancestors[0][x]);
    }

    public static int log2(int n){
        int result = 1;

        while ((1 << result) <= n)
            ++result;

        return result;
    }

    private void computeAncestorMatrix(){
        if (ancestors != null)
            return;

        ancestors = new int[MAX_LEVEL][graph.vertexSet().size()];

        for (int l = 0; l < MAX_LEVEL; l++) {
            Arrays.fill(ancestors[l], -1);
        }

        timeIn = new int[graph.vertexSet().size()];
        timeOut = new int[graph.vertexSet().size()];

        /*
         * Normalize the graph map each vertex to an integer (using a HashMap) keep the reverse
         * mapping (using an ArrayList)
         */
        vertexMap = new HashMap<>(graph.vertexSet().size());
        indexList = new ArrayList<>(graph.vertexSet().size());

        for (V v : graph.vertexSet()) {
            if (!vertexMap.containsKey(v)) {
                vertexMap.put(v, vertexMap.size());
                indexList.add(v);
            }
        }

        dfs(vertexMap.get(root), -1);
    }
}
