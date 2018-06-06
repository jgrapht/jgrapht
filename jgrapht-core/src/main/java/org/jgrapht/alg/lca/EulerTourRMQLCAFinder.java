package org.jgrapht.alg.lca;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.alg.interfaces.LCAAlgorithm;

import java.util.*;

import static org.jgrapht.alg.lca.BinaryLiftingLCAFinder.log2;

public class EulerTourRMQLCAFinder<V, E> implements LCAAlgorithm<V> {
    private final Graph<V, E> graph;
    private final Set<V> roots;
    private final int MAX_LEVEL;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    private int[] eulerTour;
    private int sizeTour;
    private boolean[] visited;

    private int[] level;
    private int[] representative;

    private int[][] rmq;
    private int[] log2;

    public EulerTourRMQLCAFinder(Graph<V, E> graph, V root){
        assert GraphTests.isForest(graph);

        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Collections.singleton(Objects.requireNonNull(root, "Root cannot be null"));
        this.MAX_LEVEL = log2(graph.vertexSet().size());
    }

    public EulerTourRMQLCAFinder(Graph<V, E> graph, Set<V> roots){
        assert GraphTests.isForest(graph);

        this.graph = Objects.requireNonNull(graph, "Graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "Roots cannot be null");
        this.MAX_LEVEL = log2(graph.vertexSet().size());

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("Roots cannot be empty");
    }

    private void normalizeGraph(){
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
    }

    private void dfs(int u, int parent, int lvl) {
        visited[u] = true;
        eulerTour[sizeTour] = u;
        level[sizeTour] = lvl;
        sizeTour++;

        V vertexU = indexList.get(u);
        for (E edge: graph.edgesOf(vertexU)){
            int v = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

            if (v != parent){
                dfs(v, u, lvl + 1);

                eulerTour[sizeTour] = u;
                level[sizeTour] = lvl;
                sizeTour++;

            }
        }
    }

    private void computeRMQ(){
        rmq = new int[MAX_LEVEL + 1][sizeTour];
        log2 = new int[sizeTour + 1];

        for (int i = 0; i < sizeTour; i++)
            rmq[0][i] = i;

        for (int i = 1; (1 << i) <= sizeTour; i++)
            for (int j = 0; j + ( 1 << i ) - 1 < sizeTour; j++) {
                int p = 1 << (i - 1);

                if (level[rmq[i - 1][j]] < level[rmq[i - 1][j + p]]){
                    rmq[i][j] = rmq[i - 1][j];
                }
                else{
                    rmq[i][j] = rmq[i - 1][j + p];
                }
            }

        for (int i = 2; i <= sizeTour; ++i)
            log2[i] = log2[i / 2] + 1;
    }

    private void computeAncestorsStructure(){
        if (rmq != null)
            return;

        normalizeGraph();

        eulerTour = new int[2 * graph.vertexSet().size()];
        level = new int[2 * graph.vertexSet().size()];
        representative = new int[graph.vertexSet().size()];
        visited = new boolean[graph.vertexSet().size()];

        for (V root: roots){
            int u = vertexMap.get(root);

            if (!visited[u])
                dfs(u, -1, 0);
        }

        Arrays.fill(representative, -1);
        for (int i = 0; i < sizeTour; i++){
            if (representative[eulerTour[i]] == -1){
                representative[eulerTour[i]] = i;
            }
        }

        computeRMQ();
    }

    @Override
    public V getLCA(V a, V b) {
        computeAncestorsStructure();

        int x = vertexMap.get(a);
        int y = vertexMap.get(b);

        if (x == y)
            return a;

        x = representative[x];
        y = representative[y];

        if (x == -1 || y == -1)
            return null;

        if (x > y) {
            int t = x;
            x = y;
            y = t;
        }

        int l = log2[y - x + 1];
        int pwl = 1 << l;
        int sol = rmq[l][x];

        if(level[sol] > level[rmq[l][y - pwl + 1]])
            sol = rmq[l][y - pwl + 1];

        return indexList.get(eulerTour[sol]);
    }
}
