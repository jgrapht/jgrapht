package org.jgrapht.alg.decomposition;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;

import java.util.*;
import java.util.stream.Collectors;

public class HeavyPathDecomposition<V, E> {
    private final Graph<V, E> graph;
    private final Set<V> roots;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    private int numberOfPaths;
    private int[] sizeSubtree, father, depth;
    private int[] path, lengthPath, positionInPath, firstNodePath;

    private List<List<V>> paths;

    private Set<E> heavyEdges;

    public HeavyPathDecomposition(Graph<V, E> graph, V root) {
        assert GraphTests.isForest(graph);

        Set<V> roots = new HashSet<>();
        roots.add(root);

        this.graph = graph;
        this.roots = roots;

        decompose();
    }

    public HeavyPathDecomposition(Graph<V, E> graph, Set<V> roots) {
        assert GraphTests.isForest(graph);

        this.graph = graph;
        this.roots = roots;

        decompose();
    }

    private void initialize(){
        int n = graph.vertexSet().size();

        sizeSubtree = new int[n];
        father = new int[n];
        depth = new int[n];

        path = new int[n];
        lengthPath = new int[n];
        positionInPath = new int[n];
        firstNodePath = new int[n];

        heavyEdges = new HashSet<>();
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

    private void dfs(int u, int parent) {
        sizeSubtree[u] = 1;
        int heavySon = -1;
        E heavyEdge = null;

        V vertexU = indexList.get(u);
        for (E edge: graph.edgesOf(vertexU)){
            int son = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

            if (son != parent){
                father[son] = u;
                depth[son] = depth[u] + 1;

                dfs(son, u);

                sizeSubtree[u] += sizeSubtree[son];

                if (heavySon == -1 || sizeSubtree[heavySon] < sizeSubtree[son]) {
                    heavySon = son;
                    heavyEdge = edge;
                }
            }
        }

        if (heavyEdge != null)
            heavyEdges.add(heavyEdge);

        if (heavySon == -1)
            path[u] = numberOfPaths++;
        else
            path[u] = path[heavySon];

        positionInPath[u] = lengthPath[path[u]]++;
    }

    private void decompose(){
        if (path != null)
            return;

        normalizeGraph();
        initialize();

        Arrays.fill(father, -1);
        Arrays.fill(path, -1);
        Arrays.fill(depth, -1);

        for (V root: roots){
            int u = vertexMap.get(root);

            if (father[u] == -1)
                dfs(u, -1);
        }

        for (int i = 0; i < graph.vertexSet().size(); i++){
            if (path[i] != -1){
                positionInPath[i] = lengthPath[path[i]] - positionInPath[i] - 1;

                if (positionInPath[i] == 0)
                    firstNodePath[path[i]] = i;
            }
//            System.out.println(i + " " + indexList.get(i) + " " + positionInPath[i]);
        }

        List<List<V>> paths = new ArrayList<>(numberOfPaths);

        for (int i = 0; i < numberOfPaths; i++) {
            List<V> path = new ArrayList<>(lengthPath[i]);

            for (int j = 0; j < lengthPath[i]; j++) {
                path.add(null);
            }

            paths.add(path);
        }

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            if (path[i] != -1){
                paths.get(path[i]).set(positionInPath[i], indexList.get(i));
            }
        }

        for (int i = 0; i < numberOfPaths; i++) {
            paths.set(i, Collections.unmodifiableList(paths.get(i)));
        }

        this.paths = Collections.unmodifiableList(paths);
    }

    public List<List<V>> getPaths(){
        return this.paths;
    }

    public int numberOfPaths(){
        return this.paths.size();
    }

    public Set<E> getHeavyEdges(){
        return this.heavyEdges;
    }

    public Set<E> getLightEdges(){
        return graph.edgeSet().stream().filter(n -> !this.heavyEdges.contains(n)).collect(Collectors.toSet());
    }

    public Map<V, V> getFather(){
        Map<V, V> map = new HashMap<>();

        for (int i = 0; i < graph.vertexSet().size(); i++){
            if (father[i] != -1)
                map.put(indexList.get(i), indexList.get(father[i]));
            else
                map.put(indexList.get(i), null);
        }

        return map;
    }

    public Map<V, Integer> getDepth(){
        Map<V, Integer> map = new HashMap<>();

        for (int i = 0; i < graph.vertexSet().size(); i++){
            map.put(indexList.get(i), depth[i]);
        }

        return map;
    }

    public Map<V, Integer> getSizeSubtree(){
        Map<V, Integer> map = new HashMap<>();

        for (int i = 0; i < graph.vertexSet().size(); i++){
            map.put(indexList.get(i), sizeSubtree[i]);
        }

        return map;
    }
}
