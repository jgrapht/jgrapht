package org.jgrapht.alg.decomposition;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class HeavyPathDecomposition<V, E> {
    private final Graph<V, E> graph;
    private final Set<V> roots;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    private int numberOfPaths;
    private int[] sizeSubtree, father, depth;
    private int[] component;
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

        component = new int[n];
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

    private void dfs(int u, int parent, int c) {
        component[u] = c;

        sizeSubtree[u] = 1;
        int heavySon = -1;
        E heavyEdge = null;

        V vertexU = indexList.get(u);
        for (E edge: graph.edgesOf(vertexU)){
            int son = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

            if (son != parent){
                father[son] = u;
                depth[son] = depth[u] + 1;

                dfs(son, u, c);

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

        int numberComponent = 0;

        for (V root: roots){
            int u = vertexMap.get(root);

            if (father[u] == -1) {
                numberComponent++;
                dfs(u, -1, numberComponent);
            }
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

    public V getFather(V vertex){
        int index = vertexMap.getOrDefault(vertex, -1);

        if (index == -1 || father[index] == -1)
            return null;
        else
            return indexList.get(father[index]);
    }

    public Map<V, Integer> getDepth(){
        Map<V, Integer> map = new HashMap<>();

        for (int i = 0; i < graph.vertexSet().size(); i++){
            map.put(indexList.get(i), depth[i]);
        }

        return map;
    }

    public int getDepth(V vertex){
        int index = vertexMap.getOrDefault(vertex, -1);

        if (index == -1)
            return -1;
        else
            return depth[index];
    }

    public Map<V, Integer> getSizeSubtree(){
        Map<V, Integer> map = new HashMap<>();

        for (int i = 0; i < graph.vertexSet().size(); i++){
            map.put(indexList.get(i), sizeSubtree[i]);
        }

        return map;
    }

    public int getSizeSubTree(V vertex){
        int index = vertexMap.getOrDefault(vertex, -1);

        if (index == -1)
            return 0;
        else
            return sizeSubtree[index];
    }

    public Map<V, Integer> getComponent(){
        Map<V, Integer> map = new HashMap<>();

        for (int i = 0; i < graph.vertexSet().size(); i++){
            map.put(indexList.get(i), component[i]);
        }

        return map;
    }

    public int getComponent(V vertex){
        int index = vertexMap.getOrDefault(vertex, -1);

        if (index == -1)
            return 0;
        else
            return component[index];
    }

    public Pair<Map<V, Integer>, List<V>> getNormalizedGraph(){
        return Pair.of(vertexMap, indexList);
    }
}
