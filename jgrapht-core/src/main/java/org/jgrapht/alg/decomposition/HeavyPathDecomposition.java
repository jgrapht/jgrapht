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
 * Algorithm for computing the heavy path decomposition of a tree/forest.
 *
 * <p>
 *  Heavy path decomposition is a technique for decomposing a rooted tree/forest
 *  into a set of disjoint paths.
 *
 * <p>
 * In a heavy path decomposition,
 * each non-leaf node selects one "heavy edge", the edge to the child that has the greatest
 * number of descendants (breaking ties arbitrarily). The set of these edges form the paths of the decomposition.
 *
 * <p>
 * If the edges of a tree/forest T are partitioned into a set of heavy edges and light edges,
 * with one heavy edge from each non-leaf node to one of its children,
 * then the subgraph formed by the heavy edges consists of a set of paths,
 * with each non-leaf vertex belonging to exactly one path, the one containing its heavy edge.
 * Leaf nodes of the tree that are not the endpoint of a heavy edge may be considered as forming paths of length zero.
 * In this way, each vertex belongs to exactly one of the paths and each path has a head vertex, its topmost vertex.
 *
 * <p>
 *  A benefit on this decomposition is that on any root-to-leaf path of a tree with n nodes,
 *  there can be at most $log_2(n)$ light edges.
 *
 * <p>
 *   This implementation runs in $O(|V|)$ time and requires $O(|V|)$ extra memory, where $|V|$ is the number of
 *   vertices in the tree/forest.
 *
 * @author Alexandru Valeanu
 * @since June 2018
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class HeavyPathDecomposition<V, E> {

    private final Graph<V, E> graph;
    private final Set<V> roots;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    private int[] sizeSubtree, father, depth, component;
    private int[] path, lengthPath, positionInPath, firstNodeInPath;

    private int numberOfPaths;
    private List<List<V>> paths;

    private Set<E> heavyEdges;

    /**
     * Create an instance with a reference to the tree that we will decompose and to the root of the tree.
     *
     * Note: The constructor will NOT check if the input tree is a valid tree.
     *
     * @param tree the input tree
     * @param root the root of the tree
     */
    public HeavyPathDecomposition(Graph<V, E> tree, V root) {
        this(tree, Collections.singleton(Objects.requireNonNull(root, "root cannot be null")));
    }

    /**
     * Create an instance with a reference to the forest that we will decompose and to the sets of roots of the
     * forest (one root per tree).
     *
     * Note: If two roots appear in the same tree, any one can be used as the actual root of that tree.
     * Note: The constructor will NOT check if the input forest is a valid forest.
     *
     * @param forest the input forest
     * @param roots the set of roots of the graph
     */
    public HeavyPathDecomposition(Graph<V, E> forest, Set<V> roots) {
        assert GraphTests.isForest(forest);

        this.graph = Objects.requireNonNull(forest, "input tree/forrest cannot be null");
        this.roots = Objects.requireNonNull(roots, "set of roots cannot be null");

        decompose();
    }

    private void allocateArrays(){
        final int n = graph.vertexSet().size();

        sizeSubtree = new int[n];
        father = new int[n];
        depth = new int[n];
        component = new int[n];

        path = new int[n];
        lengthPath = new int[n];
        positionInPath = new int[n];

        heavyEdges = new HashSet<>();
    }

    private void normalizeGraph(){
        /*
         * Normalize the graph: map each vertex to an integer (using a HashMap) keep the reverse
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

    /**
     * A iterative dfs implementation for computing the paths.
     *
     * For each node u we have to execute two sequences of operations:
     *  1: before the 'recursive' call (the then part of the if-statement)
     *  2: after the 'recursive' call (the else part of the if-statement)
     *
     * @param u the (normalized) vertex
     * @param c the component number to be used for u's tree
     */
    private void dfsIterative(int u, int c){
        /*
            Set of vertices for which the the part of the if has been performed
            (In other words: u ∈ explored iff dfs(u, c') has been called as some point)
         */
        Set<Integer> explored = new HashSet<>();

        ArrayDeque<Integer> stack = new ArrayDeque<>();
        stack.push(u);

        while (!stack.isEmpty()){
            u = stack.poll();

            if (!explored.contains(u)){
                explored.add(u);

                // simulate the return from recursion (the else part for u)
                stack.push(u);

                component[u] = c;
                sizeSubtree[u] = 1;

                V vertexU = indexList.get(u);
                for (E edge: graph.edgesOf(vertexU)){
                    int son = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

                    /*
                        Check if son has not been explored (i.e. dfs(son, c) has not been called)
                     */
                    if (!explored.contains(son)){
                        father[son] = u;
                        depth[son] = depth[u] + 1;
                        stack.push(son);
                    }
                }
            }
            else{
                /*
                    For u compute heavySon. If it exists then u becomes part of heavySon's path.
                    If not then start a new path with u.

                    heavySon = v ∈ children(u) such that sizeSubtree(v) = max{sizeSubtree(v') | v' ∈ children(u)}
                    heavyEdge = edge(u, heavySon)
                 */

                int heavySon = -1;
                E heavyEdge = null;

                V vertexU = indexList.get(u);
                for (E edge: graph.edgesOf(vertexU)){
                    int son = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

                    /*
                        Check if son if a descent of u and not its parent
                     */
                    if (son != father[u]){
                        sizeSubtree[u] += sizeSubtree[son];

                        if (heavySon == -1 || sizeSubtree[heavySon] < sizeSubtree[son]) {
                            heavySon = son;
                            heavyEdge = edge;
                        }
                    }
                }

                if (heavySon == -1)
                    path[u] = numberOfPaths++;
                else {
                    heavyEdges.add(heavyEdge);
                    path[u] = path[heavySon];
                }

                /*
                    Compute the positions in reverse order: the first node in the path is the first one that was
                    added (the order will be reversed in decompose).
                 */
                positionInPath[u] = lengthPath[path[u]]++;
            }
        }
    }

    private void decompose(){
        /*
            If we already have a decomposition stop.
         */
        if (path != null)
            return;

        normalizeGraph();
        allocateArrays();

        Arrays.fill(father, -1);
        Arrays.fill(path, -1);
        Arrays.fill(depth, -1);
        Arrays.fill(component, -1);
        Arrays.fill(positionInPath, -1);

        /*
            Iterative through all roots and compute the paths for each tree individually
         */
        int numberComponent = 0;
        for (V root: roots){
            Integer u = vertexMap.get(root);

            if (u == null)
                throw new IllegalArgumentException("root: " + root + " not contained in graph");

            if (component[u] == -1) {
                dfsIterative(u, numberComponent++);
            }
        }

        firstNodeInPath = new int[numberOfPaths];

        /*
            Reverse the position of all vertices that are present in some path.
            After this the positionInPath[u] = 0 if u is the first node in the path (i.e. the node closest to the root)

            Also compute firstNodeInPath[i] = u such that path[u] = i and positionInPath[u] = 0
         */
        for (int i = 0; i < graph.vertexSet().size(); i++){
            if (path[i] != -1){
                positionInPath[i] = lengthPath[path[i]] - positionInPath[i] - 1;

                if (positionInPath[i] == 0)
                    firstNodeInPath[path[i]] = i;
            }
        }

        /*
            Compute the paths as unmodifiable data structures (list)
         */
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
     * @return (immutable) path decomposition
     */
    public List<List<V>> getPaths(){
        return this.paths;
    }

    /**
     * @return number of paths in the decomposition
     */
    public int numberOfPaths(){
        return this.paths.size();
    }

    /**
     * @return (immutable) set of heavy edges
     */
    public Set<E> getHeavyEdges(){
        return Collections.unmodifiableSet(this.heavyEdges);
    }

    /**
     * @return (immutable) set of light edges
     */
    public Set<E> getLightEdges(){
        return Collections.unmodifiableSet(
                graph.edgeSet().stream().filter(n -> !this.heavyEdges.contains(n)).collect(Collectors.toSet()));
    }

    /**
     * Returns the father of vertex $v$ in the internal DFS tree/forest.
     * If the vertex $v$ has not been explored or it is the root of its tree, $null$ will be returned.
     *
     * @param v vertex
     * @return father of vertex $v$ in the DFS tree/forest
     */
    public V getFather(V v){
        int index = vertexMap.getOrDefault(v, -1);

        if (index == -1 || father[index] == -1)
            return null;
        else
            return indexList.get(father[index]);
    }

    /**
     * Returns the depth of vertex $v$ in the internal DFS tree/forest.
     *
     * <p> The depth of a vertex $v$ is defined as the number of edges traversed on
     * the path from the root of the DFS tree to vertex $v$. The root of each DFS tree has depth 0.
     *
     * <p>
     * If the vertex $v$ has not been explored, $-1$ will be returned.
     *
     * @param v vertex
     * @return depth of vertex $v$ in the DFS tree/forest
     */
    public int getDepth(V v){
        int index = vertexMap.getOrDefault(v, -1);

        if (index == -1)
            return -1;
        else
            return depth[index];
    }

    /**
     * Returns the size of vertex $v$'s subtree in the internal DFS tree/forest.
     *
     * <p>
     * The size of a vertex $v$'s subtree is
     * defined as the number of vertices in the subtree rooted at $v$ (including $v).
     *
     * <p>
     * If the vertex $v$ has not been explored, $0$ will be returned.
     *
     * @param v vertex
     * @return size of vertex $v$'s subtree in the DFS tree/forest
     */
    public int getSizeSubtree(V v){
        int index = vertexMap.getOrDefault(v, -1);

        if (index == -1)
            return 0;
        else
            return sizeSubtree[index];
    }

    /**
     * Returns the component id of vertex $v$ in the internal DFS tree/forest. For two vertices $u$ and $v$,
     * $component[u] = component[v]$ iff $u$ and $v$ are in the same tree.
     *
     * <p>
     * The component ids are numbers between $0$ and $numberOfTrees - 1$.
     *
     * <p>
     * If the vertex $v$ has not been explored, $-1$ will be returned.
     *
     * @param v vertex
     * @return component id of vertex $v$ in the DFS tree/forest
     */
    public int getComponent(V v){
        int index = vertexMap.getOrDefault(v, -1);

        if (index == -1)
            return -1;
        else
            return component[index];
    }

    /**
     * Return the normalized version of the input graph: a map from vertices to unique integers and the reverse
     * mapping as a list.
     *
     * For each vertex $v \in V$, let $vertexMap(v) = x$ such that no two vertices share the same x and all x's are
     * integers between $0$ and $|V| - 1$. Let $indexList(x) = v$ be the reverse mapping from integers to vertices.
     *
     * Note: The two structures returned are immutable.
     *
     * @return a pair which consists of the vertexMap and the indexList
     */
    public Pair<Map<V, Integer>, List<V>> getNormalizedGraph(){
        return Pair.of(Collections.unmodifiableMap(vertexMap), Collections.unmodifiableList(indexList));
    }


    /**
     * Return a copy of the internal father array.
     * For each vertex $v \in V$, $fatherArray[normalizeVertex(v)] = normalizeVertex(u)$ if $getFather(v) = u$ or
     * $-1$ if $getFather(v) = null$.
     *
     * @return internal father array
     */
    public int[] getFatherArray(){
        return father.clone();
    }

    /**
     * Return a copy of the internal depth array.
     * For each vertex $v \in V$, $depthArray[normalizeVertex(v)] = getDepth(v)$
     *
     * @return internal depth array
     */
    public int[] getDepthArray(){
        return depth.clone();
    }

    /**
     * Return a copy of the internal sizeSubtree array.
     * For each vertex $v$, $sizeSubtreeArray[normalizeVertex(v)] = getSizeSubtree(v)$
     *
     * @return internal sizeSubtree array
     */
    public int[] getSizeSubtreeArray(){
        return sizeSubtree.clone();
    }

    /**
     * Return a copy of the internal component array.
     * For each vertex $v$, $componentArray[normalizeVertex(v)] = getComponent(v)$
     *
     * @return internal component array
     */
    public int[] getComponentArray(){
        return component.clone();
    }

    /**
     * Return a copy of the internal path array.
     * For each vertex $v$, $pathArray[normalizeVertex(v)] = i$ iff $v$ appears on path $i$ or $-1$
     * if $v$ doesn't belong to any path.
     *
     * <p>
     * Note: the indexing of paths is consistent with {@link #getPaths()}.
     *
     * @return internal path array
     */
    public int[] getPathArray(){
        return path.clone();
    }

    /**
     * Return a copy of the internal positionInPath array.
     * For each vertex $v$, $positionInPathArray[normalizeVertex(v)] = k$ iff $v$ appears as the $k-th$ vertex on its
     * path (0-indexed) or $-1$ if $v$ doesn't belong to any path.
     *
     * @return internal positionInPath array
     */
    public int[] getPositionInPathArray(){
        return positionInPath.clone();
    }

    /**
     * Return a copy of the internal firstNodeInPath array.
     * For each path $i$, $firstNodeInPath[i] = normalizeVertex(v)$ iff $v$ appears as the first vertex on the path.
     *
     * <p>
     * Note: the indexing of paths is consistent with {@link #getPaths()}.
     *
     * @return internal firstNodeInPath array
     */
    public int[] getFirstNodeInPathArray(){
        return firstNodeInPath.clone();
    }
}
