/*
 * (C) Copyright 2018-2018, by Alexandru Valeanu and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.decomposition;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Algorithm for computing the heavy path decomposition of a rooted tree/forest.
 *
 * <p>
 * Heavy path decomposition is a technique for decomposing a rooted tree/forest into a set of
 * disjoint paths.
 *
 * <p>
 * The techniques was first introduced in <i>Sleator, D. D.; Tarjan, R. E. (1983). "A Data Structure
 * for Dynamic Trees". Proceedings of the thirteenth annual ACM symposium on Theory of computing -
 * STOC '81 doi:10.1145/800076.802464 </i>
 * </p>
 *
 * <p>
 * In a heavy path decomposition, the edges set is partitioned into two sets, a set of heavy edges
 * and a set of light ones according to the relative number of nodes in the vertex's subtree.
 *
 * We define the size of a vertex v in the forest, denoted by size(v), to be the number of
 * descendants of v, including v itself. We define a tree edge (v,parent(v)) to be heavy if
 * $2*size(v)$ &gt; $size(parent(v))$ and light, otherwise.
 *
 * The set of heavy edges form the edges of the decomposition.
 *
 * <p>
 * A benefit of this decomposition is that on any root-to-leaf path of a tree with n nodes, there
 * can be at most $log_2(n)$ light edges.
 *
 * <p>
 * This implementation runs in $O(|V|)$ time and requires $O(|V|)$ extra memory, where $|V|$ is the
 * number of vertices in the tree/forest.
 *
 * <p>
 * Note: If an edge is not reachable from any of the roots provided, then that edge is neither light
 * nor heavy.
 * </p>
 *
 * @author Alexandru Valeanu
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class HeavyPathDecomposition<V, E>
    implements
    TreeToPathDecompositionAlgorithm<V, E>
{

    private final Graph<V, E> graph;
    private final Set<V> roots;

    private Map<V, Integer> vertexMap;
    private List<V> indexList;

    private int[] sizeSubtree, parent, depth, component;
    private int[] path, lengthPath, positionInPath, firstNodeInPath;

    private int numberOfPaths;
    private List<List<V>> paths;

    private Set<E> heavyEdges;
    private Set<E> lightEdges;

    /**
     * Create an instance with a reference to the tree that we will decompose and to the root of the
     * tree.
     *
     * Note: The constructor will NOT check if the input graph is a valid tree.
     *
     * @param tree the input tree
     * @param root the root of the tree
     */
    public HeavyPathDecomposition(Graph<V, E> tree, V root)
    {
        this(tree, Collections.singleton(Objects.requireNonNull(root, "root cannot be null")));
    }

    /**
     * Create an instance with a reference to the forest that we will decompose and to the sets of
     * roots of the forest (one root per tree).
     *
     * Note: If two roots appear in the same tree, an error will be thrown. Note: The constructor
     * will NOT check if the input graph is a valid forest.
     *
     * @param forest the input forest
     * @param roots the set of roots of the graph
     */
    public HeavyPathDecomposition(Graph<V, E> forest, Set<V> roots)
    {
        this.graph = Objects.requireNonNull(forest, "input tree/forrest cannot be null");
        this.roots = Objects.requireNonNull(roots, "set of roots cannot be null");

        decompose();
    }

    private void allocateArrays()
    {
        final int n = graph.vertexSet().size();

        sizeSubtree = new int[n];
        parent = new int[n];
        depth = new int[n];
        component = new int[n];

        path = new int[n];
        lengthPath = new int[n];
        positionInPath = new int[n];

        heavyEdges = new HashSet<>();
        lightEdges = new HashSet<>();
    }

    private void normalizeGraph()
    {
        /*
         * Normalize the graph by mapping each vertex to an integer.
         */
        VertexToIntegerMapping<V> vertexToIntegerMapping = Graphs.getVertexToIntegerMapping(graph);
        vertexMap = vertexToIntegerMapping.getVertexMap();
        indexList = vertexToIntegerMapping.getIndexList();
    }

    /**
     * An iterative dfs implementation for computing the paths.
     *
     * For each node u we have to execute two sequences of operations: 1: before the 'recursive'
     * call (the then part of the if-statement) 2: after the 'recursive' call (the else part of the
     * if-statement)
     *
     * @param u the (normalized) vertex
     * @param c the component number to be used for u's tree
     */
    private void dfsIterative(int u, int c)
    {
        // Set of vertices for which the the part of the if has been performed
        // (In other words: u ∈ explored iff dfs(u, c') has been called as some point)
        Set<Integer> explored = new HashSet<>();

        ArrayDeque<Integer> stack = new ArrayDeque<>();
        stack.push(u);

        while (!stack.isEmpty()) {
            u = stack.poll();

            if (!explored.contains(u)) {
                explored.add(u);

                // simulate the return from recursion (the else part for u)
                stack.push(u);

                component[u] = c;
                sizeSubtree[u] = 1;

                V vertexU = indexList.get(u);
                for (E edge : graph.edgesOf(vertexU)) {
                    int child = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

                    // Check if child has not been explored (i.e. dfs(child, c) has not been called)
                    if (!explored.contains(child)) {
                        parent[child] = u;
                        depth[child] = depth[u] + 1;
                        stack.push(child);
                    }
                }
            } else {
                // For u compute pathChild. If it exists then u becomes part of pathChild's path.
                // If not then start a new path with u.
                //
                // pathChild = v ∈ children(u) such that sizeSubtree(v) = max{sizeSubtree(v') | v' ∈
                // children(u)}

                int pathChild = -1;
                E pathEdge = null;

                V vertexU = indexList.get(u);
                for (E edge : graph.edgesOf(vertexU)) {
                    int child = vertexMap.get(Graphs.getOppositeVertex(graph, edge, vertexU));

                    // Check if child is a descendant of u and not its parent
                    if (child != parent[u]) {
                        sizeSubtree[u] += sizeSubtree[child];

                        if (pathChild == -1 || sizeSubtree[pathChild] < sizeSubtree[child]) {
                            pathChild = child;
                            pathEdge = edge;
                        }

                        // assume all edges are light
                        lightEdges.add(edge);
                    }
                }

                if (pathChild == -1)
                    path[u] = numberOfPaths++;
                else {
                    path[u] = path[pathChild];

                    // Is pathEdge=(pathChild, u) a heavy edge?
                    if (2 * sizeSubtree[pathChild] > sizeSubtree[u]) {
                        heavyEdges.add(pathEdge);

                        // assumption was wrong => remove pathEdge from light-edges set
                        lightEdges.remove(pathEdge);
                    }
                }

                // Compute the positions in reverse order: the first node in the path is the first
                // one that was
                // added (the order will be reversed in decompose).
                positionInPath[u] = lengthPath[path[u]]++;
            }
        }
    }

    private void decompose()
    {
        // If we already have a decomposition stop.
        if (path != null)
            return;

        normalizeGraph();
        allocateArrays();

        Arrays.fill(parent, -1);
        Arrays.fill(path, -1);
        Arrays.fill(depth, -1);
        Arrays.fill(component, -1);
        Arrays.fill(positionInPath, -1);

        // Iterate through all roots and compute the paths for each tree individually
        int numberComponent = 0;
        for (V root : roots) {
            Integer u = vertexMap.get(root);

            if (u == null) {
                throw new IllegalArgumentException("root: " + root + " not contained in graph");
            }

            if (component[u] == -1) {
                dfsIterative(u, numberComponent++);
            } else {
                throw new IllegalArgumentException("multiple roots in the same tree");
            }
        }

        firstNodeInPath = new int[numberOfPaths];

        // Reverse the position of all vertices that are present in some path.
        // After this the positionInPath[u] = 0 if u is the first node in the path (i.e. the node
        // closest to the root)
        //
        // Also compute firstNodeInPath[i] = u such that path[u] = i and positionInPath[u] = 0
        for (int i = 0; i < graph.vertexSet().size(); i++) {
            if (path[i] != -1) {
                positionInPath[i] = lengthPath[path[i]] - positionInPath[i] - 1;

                if (positionInPath[i] == 0)
                    firstNodeInPath[path[i]] = i;
            }
        }

        // Compute the paths as unmodifiable data structures (list)
        List<List<V>> paths = new ArrayList<>(numberOfPaths);

        for (int i = 0; i < numberOfPaths; i++) {
            List<V> path = new ArrayList<>(lengthPath[i]);

            for (int j = 0; j < lengthPath[i]; j++) {
                path.add(null);
            }

            paths.add(path);
        }

        for (int i = 0; i < graph.vertexSet().size(); i++) {
            if (path[i] != -1) {
                paths.get(path[i]).set(positionInPath[i], indexList.get(i));
            }
        }

        for (int i = 0; i < numberOfPaths; i++) {
            paths.set(i, Collections.unmodifiableList(paths.get(i)));
        }

        this.paths = Collections.unmodifiableList(paths);
        this.heavyEdges = Collections.unmodifiableSet(this.heavyEdges);
    }

    /**
     * Set of heavy edges.
     *
     * @return (immutable) set of heavy edges
     */
    public Set<E> getHeavyEdges()
    {
        return this.heavyEdges;
    }

    /**
     * Set of light edges.
     *
     * @return (immutable) set of light edges
     */
    public Set<E> getLightEdges()
    {
        return this.lightEdges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathDecomposition<V, E> getPathDecomposition()
    {
        return new PathDecompositionImpl<>(graph, getHeavyEdges(), this.paths);
    }

    /**
     * Return the internal representation of the data.
     *
     * Note: this data representation is intended only for use by other components within JGraphT
     *
     * @return the internal state representation
     */
    public InternalState getInternalState()
    {
        return new InternalState();
    }

    /**
     * Internal representation of the data
     */
    public class InternalState
    {
        /**
         * Returns the parent of vertex $v$ in the internal DFS tree/forest. If the vertex $v$ has
         * not been explored or it is the root of its tree, $null$ will be returned.
         *
         * @param v vertex
         * @return parent of vertex $v$ in the DFS tree/forest
         */
        public V getParent(V v)
        {
            int index = vertexMap.getOrDefault(v, -1);

            if (index == -1 || parent[index] == -1)
                return null;
            else
                return indexList.get(parent[index]);
        }

        /**
         * Returns the depth of vertex $v$ in the internal DFS tree/forest.
         *
         * <p>
         * The depth of a vertex $v$ is defined as the number of edges traversed on the path from
         * the root of the DFS tree to vertex $v$. The root of each DFS tree has depth 0.
         *
         * <p>
         * If the vertex $v$ has not been explored, $-1$ will be returned.
         *
         * @param v vertex
         * @return depth of vertex $v$ in the DFS tree/forest
         */
        public int getDepth(V v)
        {
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
         * The size of a vertex $v$'s subtree is defined as the number of vertices in the subtree
         * rooted at $v$ (including $v).
         *
         * <p>
         * If the vertex $v$ has not been explored, $0$ will be returned.
         *
         * @param v vertex
         * @return size of vertex $v$'s subtree in the DFS tree/forest
         */
        public int getSizeSubtree(V v)
        {
            int index = vertexMap.getOrDefault(v, -1);

            if (index == -1)
                return 0;
            else
                return sizeSubtree[index];
        }

        /**
         * Returns the component id of vertex $v$ in the internal DFS tree/forest. For two vertices
         * $u$ and $v$, $component[u] = component[v]$ iff $u$ and $v$ are in the same tree.
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
        public int getComponent(V v)
        {
            int index = vertexMap.getOrDefault(v, -1);

            if (index == -1)
                return -1;
            else
                return component[index];
        }

        /**
         * Return the vertex map, a mapping from vertices to unique integers.
         *
         * For each vertex $v \in V$, let $vertexMap(v) = x$ such that no two vertices share the
         * same x and all x's are integers between $0$ and $|V| - 1$. Let $indexList(x) = v$ be the
         * reverse mapping from integers to vertices.
         *
         * Note: The structure returned is immutable.
         *
         * @return the vertexMap
         */
        public Map<V, Integer> getVertexMap()
        {
            return Collections.unmodifiableMap(vertexMap);
        }

        /**
         * Return the index list, a mapping from unique integers to vertices.
         *
         * For each vertex $v \in V$, let $vertexMap(v) = x$ such that no two vertices share the
         * same x and all x's are integers between $0$ and $|V| - 1$. Let $indexList(x) = v$ be the
         * reverse mapping from integers to vertices.
         *
         * Note: The structure returned is immutable.
         *
         * @return the indexList
         */
        public List<V> getIndexList()
        {
            return Collections.unmodifiableList(indexList);
        }

        /**
         * Return the internal depth array. For each vertex $v \in V$,
         * $depthArray[normalizeVertex(v)] = getDepth(v)$
         *
         * @return internal depth array
         */
        public int[] getDepthArray()
        {
            return depth;
        }

        /**
         * Return the internal sizeSubtree array. For each vertex $v$,
         * $sizeSubtreeArray[normalizeVertex(v)] = getSizeSubtree(v)$
         *
         * @return internal sizeSubtree array
         */
        public int[] getSizeSubtreeArray()
        {
            return sizeSubtree;
        }

        /**
         * Return the internal component array. For each vertex $v$,
         * $componentArray[normalizeVertex(v)] = getComponent(v)$
         *
         * @return internal component array
         */
        public int[] getComponentArray()
        {
            return component;
        }

        /**
         * Return the internal path array. For each vertex $v$, $pathArray[normalizeVertex(v)] = i$
         * iff $v$ appears on path $i$ or $-1$ if $v$ doesn't belong to any path.
         *
         * <p>
         * Note: the indexing of paths is consistent with {@link PathDecomposition#getPaths()}.
         *
         * @return internal path array
         */
        public int[] getPathArray()
        {
            return path;
        }

        /**
         * Return the internal positionInPath array. For each vertex $v$,
         * $positionInPathArray[normalizeVertex(v)] = k$ iff $v$ appears as the $k-th$ vertex on its
         * path (0-indexed) or $-1$ if $v$ doesn't belong to any path.
         *
         * @return internal positionInPath array
         */
        public int[] getPositionInPathArray()
        {
            return positionInPath;
        }

        /**
         * Return the internal firstNodeInPath array. For each path $i$, $firstNodeInPath[i] =
         * normalizeVertex(v)$ iff $v$ appears as the first vertex on the path.
         *
         * <p>
         * Note: the indexing of paths is consistent with {@link PathDecomposition#getPaths()}.
         *
         * @return internal firstNodeInPath array
         */
        public int[] getFirstNodeInPathArray()
        {
            return firstNodeInPath;
        }

        /**
         * Return the internal parent array. For each vertex $v \in V$,
         * $parentArray[normalizeVertex(v)] = normalizeVertex(u)$ if $getParent(v) = u$ or $-1$ if
         * $getParent(v) = null$.
         *
         * @return internal parent array
         */
        public int[] getParentArray()
        {
            return parent;
        }
    }
}
