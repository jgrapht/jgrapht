/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.alg.decomposition;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;
import org.jgrapht.alg.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Heavy-path decomposition of a forest.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class HeavyPathDecomposition<V, E> {
    private final Graph<V, E> graph;
    private final Set<V> roots;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    private int numberOfPaths;
    private int[] sizeSubtree, father, depth;
    private int[] component;
    private int[] path, lengthPath, positionInPath, firstNodeInPath;

    private List<List<V>> paths;

    private Set<E> heavyEdges;

    /**
     * Create an instance with a reference to the graph that we will decompose
     *
     * @param graph the input graph
     * @param root the root of the graph
     */
    public HeavyPathDecomposition(Graph<V, E> graph, V root) {
        assert GraphTests.isForest(graph);

        Set<V> roots = new HashSet<>();
        roots.add(root);

        this.graph = graph;
        this.roots = roots;

        decompose();
    }

    /**
     * Create an instance with a reference to the graph that we will decompose
     *
     * @param graph the input graph
     * @param roots the roots of the graph
     */
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
        firstNodeInPath = new int[n];

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

    private void dfsIterative(int u, int parent, int c){
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        Set<Integer> explored = new HashSet<>();

        stack.push(u);

        while (!stack.isEmpty()){
            u = stack.poll();

            if (!explored.contains(u)){
                explored.add(u);

                // simulate the return from recursion
                stack.push(u);

                component[u] = c;
                sizeSubtree[u] = 1;

                V vertexU = indexList.get(u);
                for (E edge: graph.edgesOf(vertexU)){
                    int son = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

                    if (!explored.contains(son)){
                        father[son] = u;
                        depth[son] = depth[u] + 1;

                        stack.push(son);
                    }
                }
            }
            else{
                int heavySon = -1;
                E heavyEdge = null;

                V vertexU = indexList.get(u);

                for (E edge: graph.edgesOf(vertexU)){
                    int son = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

                    if (son != father[u]){

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
        }
    }

    // TODO: remove?
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

            if (sizeSubtree[u] == 0) {
                numberComponent++;
                dfsIterative(u, -1, numberComponent);
            }
        }

        for (int i = 0; i < graph.vertexSet().size(); i++){
            if (path[i] != -1){
                positionInPath[i] = lengthPath[path[i]] - positionInPath[i] - 1;

                if (positionInPath[i] == 0)
                    firstNodeInPath[path[i]] = i;
            }
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

    /**
     * @return the path decomposition
     */
    public List<List<V>> getPaths(){
        return this.paths;
    }

    /**
     * @return the number of paths in the decomposition
     */
    public int numberOfPaths(){
        return this.paths.size();
    }

    /**
     * @return the set of heavy edges
     */
    public Set<E> getHeavyEdges(){
        return Collections.unmodifiableSet(this.heavyEdges);
    }

    /**
     * @return the set of light edges
     */
    public Set<E> getLightEdges(){
        return Collections.unmodifiableSet(
                graph.edgeSet().stream().filter(n -> !this.heavyEdges.contains(n)).collect(Collectors.toSet()));
    }

    private int[] cloneArray(int[] array){
        int[] clone = new int[array.length];
        System.arraycopy(array, 0, clone, 0, array.length);
        return clone;
    }

    /**
     * @return the father array
     */
    public int[] getFatherArray(){
        return cloneArray(father);
    }

    /**
     * @return the depth array
     */
    public int[] getDepthArray(){
        return cloneArray(depth);
    }

    /**
     * @return the sizeSubtree array
     */
    public int[] getSizeSubtreeArray(){
        return cloneArray(sizeSubtree);
    }

    /**
     * @return the component array
     */
    public int[] getComponentArray(){
        return cloneArray(component);
    }

    /**
     * @return the path array
     */
    public int[] getPathArray(){
        return cloneArray(path);
    }

    /**
     * @return the positionInPath array
     */
    public int[] getPositionInPathArray(){
        return cloneArray(positionInPath);
    }

    /**
     * @return the firstNodeInPath array
     */
    public int[] getFirstNodeInPathArray(){
        return cloneArray(firstNodeInPath);
    }

    /**
     * @param v a vertex
     * @return the father of vertex v in the DFS tree
     */
    public V getFather(V v){
        int index = vertexMap.getOrDefault(v, -1);

        if (index == -1 || father[index] == -1)
            return null;
        else
            return indexList.get(father[index]);
    }

    /**
     * @param v a vertex
     * @return the depth of vertex v in the DFS tree
     */
    public int getDepth(V v){
        int index = vertexMap.getOrDefault(v, -1);

        if (index == -1)
            return -1;
        else
            return depth[index];
    }

    /**
     * @param v a vertex
     * @return the size of vertex v's subtree in the DFS tree
     */
    public int getSizeSubTree(V v){
        int index = vertexMap.getOrDefault(v, -1);

        if (index == -1)
            return 0;
        else
            return sizeSubtree[index];
    }

    /**
     * @param v a vertex
     * @return the component of vertex v in the DFS tree
     */
    public int getComponent(V v){
        int index = vertexMap.getOrDefault(v, -1);

        if (index == -1)
            return 0;
        else
            return component[index];
    }

    /**
     * Returned the normalized version of the input graph.
     *
     * @return a pair which consists of the vertexMap and the indexList
     */
    public Pair<Map<V, Integer>, List<V>> getNormalizedGraph(){
        return Pair.of(Collections.unmodifiableMap(vertexMap), Collections.unmodifiableList(indexList));
    }
}