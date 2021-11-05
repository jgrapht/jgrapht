/*
 * (C) Copyright 2019-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.util.*;

import java.util.*;

/**
 * Edmonds' blossom algorithm for maximum cardinality matching in general undirected graphs.
 * 
 * <p>
 * A matching in a graph $G(V,E)$ is a subset of edges $M$ such that no two edges in $M$ have a
 * vertex in common. A matching has at most $\frac{1}{2}|V|$ edges. A node $v$ in $G$ is matched by
 * matching $M$ if $M$ contains an edge incident to $v$. A matching is perfect if all nodes are
 * matched. By definition, a perfect matching consists of exactly $\frac{1}{2}|V|$ edges. This
 * algorithm will return a perfect matching if one exists. If no perfect matching exists, then the
 * largest (non-perfect) matching is returned instead. In the special case that the input graph is
 * bipartite, consider using {@link HopcroftKarpMaximumCardinalityBipartiteMatching} instead.
 * 
 * <p>
 * To compute a maximum cardinality matching, at most $n$ augmenting path computations are
 * performed. Each augmenting path computation takes $O(m \alpha(m,n))$ time, where $\alpha(m,n)$ is
 * the inverse of the Ackerman function, $n$ is the number of vertices, and $m$ the number of edges.
 * This results in a total runtime complexity of O(n m alpha(m,n)). In practice, the number of
 * augmenting path computations performed is far smaller than $n$, since an efficient heuristic is
 * used to compute a near-optimal initial solution. The heuristic by default is the
 * {@link GreedyMaximumCardinalityMatching} but can be changed using the appropriate constructor.
 * 
 * <p>
 * The runtime complexity of this implementation could be improved to $O(n m)$ when the UnionFind
 * data structure used in this implementation is replaced by the linear-time set union data
 * structure proposed in: Gabow, H.N., Tarjan, R.E. A linear-time algorithm for a special case of
 * disjoint set union. Proc. Fifteenth Annual ACM Symposium on Theory of Computing, 1982, pp.
 * 246-251.
 * <p>
 * Edmonds' original algorithm first appeared in Edmonds, J. Paths, trees, and flowers. Canadian
 * Journal of Mathematics 17, 1965, pp. 449-467, and had a runtime complexity of $O(n^4)$.
 * 
 * <p>
 * This is the algorithm and implementation described in the
 * <a href="https://people.mpi-inf.mpg.de/~mehlhorn/LEDAbook.html">LEDA book</a>. See the LEDA
 * Platform of Combinatorial and Geometric Computing, Cambridge University Press, 1999.
 * 
 * <p>
 * For future reference - A more efficient algorithm exists: S. Micali and V. Vazirani. An
 * $O(\sqrt{n}m)$ algorithm for finding maximum matching in general graphs. Proc. 21st Ann. Symp. on
 * Foundations of Computer Science, IEEE, 1980, pp. 17–27. This is the most efficient algorithm
 * known for computing maximum cardinality matchings in general graphs. More details on this
 * algorithm can be found in:
 * <ul>
 * <li><a href="http://research.microsoft.com/apps/video/dl.aspx?id=171055">Presentation from
 * Vazirani 'Dispelling an Old Myth about an Ancient Algorithm'</a></li>
 * <li><a href="https://arxiv.org/abs/1210.4594">Vazirani, V. A Simplification of the MV Matching
 * Algorithm and its Proof, 2013</a></li>
 * </ul>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @author Joris Kinable
 */
public class SparseEdmondsMaximumCardinalityMatching<V, E>
    implements
    MatchingAlgorithm<V, E>
{
    private final Graph<V, E> graph;
    private MatchingAlgorithm<V, E> initializer;
    private Matching<V, E> result;
    private Map<V, Integer> oddSetCover;

    /**
     * Constructs a new instance of the algorithm. {@link GreedyMaximumCardinalityMatching} is used
     * to quickly generate a near optimal initial solution.
     * 
     * @param graph the input graph
     */
    public SparseEdmondsMaximumCardinalityMatching(Graph<V, E> graph)
    {
        this(graph, new GreedyMaximumCardinalityMatching<>(graph, false));
    }

    /**
     * Constructs a new instance of the algorithm.
     * 
     * @param graph undirected graph (graph does not have to be simple)
     * @param initializer heuristic matching algorithm used to quickly generate a (near optimal)
     *        initial feasible solution
     */
    public SparseEdmondsMaximumCardinalityMatching(
        Graph<V, E> graph, MatchingAlgorithm<V, E> initializer)
    {
        this.graph = GraphTests.requireUndirected(graph);
        this.initializer = initializer;
    }

    /**
     * The actual implementation as an inner class. We use this pattern in order to free the work
     * memory after computation. The outer class can cache the result but can also release all the
     * auxiliary memory.
     * 
     * @param <V> the vertex type
     * @param <E> the edge type
     */
    private static class Algorithm<V, E>
    {
        private static final int NULL = -1;

        /**
         * Even, odd and unlabeled labels.
         */
        private enum Label
        {
            EVEN,
            ODD,
            UNLABELED
        }

        private final Graph<V, E> graph;
        private MatchingAlgorithm<V, E> initializer;

        private int nodes;
        private Map<V, Integer> vertexIndexMap;
        private V[] vertexMap;

        private int[] mate;
        private Label[] label;
        private int[] pred;
        double strue;
        private double[] path1;
        private double[] path2;
        private int[] sourceBridge;
        private int[] targetBridge;

        private VertexPartition base;
        private FixedSizeIntegerQueue queue;
        private List<Integer> labeledNodes;

        public Algorithm(Graph<V, E> graph, MatchingAlgorithm<V, E> initializer)
        {
            this.graph = graph;
            this.initializer = initializer;
        }

        @SuppressWarnings("unchecked")
        private void initialize()
        {
            // index graph
            this.nodes = graph.vertexSet().size();
            this.vertexIndexMap = CollectionUtil.newHashMapWithExpectedSize(nodes);
            this.vertexMap = (V[]) new Object[nodes];
            int vIndex = 0;
            for (V vertex : graph.vertexSet()) {
                vertexIndexMap.put(vertex, vIndex);
                vertexMap[vIndex] = vertex;
                vIndex++;
            }

            this.mate = new int[nodes];
            this.base = new VertexPartition(nodes);
            this.label = new Label[nodes];
            this.pred = new int[nodes];
            this.path1 = new double[nodes];
            this.path2 = new double[nodes];
            this.sourceBridge = new int[nodes];
            this.targetBridge = new int[nodes];

            for (int i = 0; i < nodes; i++) {
                this.mate[i] = NULL;
                this.label[i] = Label.EVEN;
                this.pred[i] = NULL;
                this.path1[i] = 0d;
                this.path2[i] = 0d;
                this.sourceBridge[i] = NULL;
                this.targetBridge[i] = NULL;
            }
            this.strue = 0d;
            this.queue = new FixedSizeIntegerQueue(nodes);
            this.labeledNodes = new ArrayList<>();
        }

        private void runInitializer()
        {
            if (initializer == null) {
                return;
            }

            for (E e : initializer.getMatching()) {
                V u = graph.getEdgeSource(e);
                V v = graph.getEdgeTarget(e);

                int uIndex = vertexIndexMap.get(u);
                int vIndex = vertexIndexMap.get(v);

                mate[uIndex] = vIndex;
                label[uIndex] = Label.UNLABELED;
                mate[vIndex] = uIndex;
                label[vIndex] = Label.UNLABELED;
            }
        }

        private void findPath(Deque<Integer> p, int x, int y)
        {
            if (x == y) {
                p.add(x);
                return;
            }
            if (label[x] == Label.EVEN) {
                p.add(x);
                p.add(mate[x]);
                findPath(p, pred[mate[x]], y);
                return;
            }
            // x is ODD
            p.add(x);
            Deque<Integer> p2 = new ArrayDeque<>();
            findPath(p2, sourceBridge[x], mate[x]);
            while (!p2.isEmpty()) {
                p.add(p2.removeLast());
            }
            findPath(p, targetBridge[x], y);
        }

        private void shrinkPath(int b, int v, int w)
        {
            int x = base.find(v);
            while (x != b) {
                base.union(x, b);
                x = mate[x];
                base.union(x, b);
                base.name(b); // make sure b is called the same
                queue.enqueue(x);
                sourceBridge[x] = v;
                targetBridge[x] = w;
                x = base.find(pred[x]);
            }
        }

        public Set<E> computeMatching()
        {
            initialize();
            runInitializer();

            for (int i = 0; i < nodes; i++) {
                if (mate[i] != NULL) {
                    continue;
                }

                queue.clear();
                queue.enqueue(i);

                labeledNodes.clear();
                labeledNodes.add(i);

                boolean breakThrough = false;

                while (!breakThrough && !queue.isEmpty()) { // grow tree
                    int v = queue.poll();
                    V vAsVertex = vertexMap[v];
                    for (E e : graph.edgesOf(vAsVertex)) {
                        V wAsVertex = Graphs.getOppositeVertex(graph, e, vAsVertex);
                        int w = vertexIndexMap.get(wAsVertex);

                        if (base.find(v) == base.find(w) || label[base.find(w)] == Label.ODD) {
                            continue;
                        }

                        if (label[w] == Label.UNLABELED) { // grow tree
                            label[w] = Label.ODD;
                            labeledNodes.add(w);
                            pred[w] = v;
                            label[mate[w]] = Label.EVEN;
                            labeledNodes.add(mate[w]);
                            queue.enqueue(mate[w]);
                        } else { // augment or shrink blossom
                            int hv = base.find(v);
                            int hw = base.find(w);
                            strue += 1d;
                            path1[hv] = strue;
                            path2[hw] = strue;

                            while ((path1[hw] != strue && path2[hv] != strue)
                                && (mate[hv] != NULL || mate[hw] != NULL))
                            {
                                if (mate[hv] != NULL) {
                                    hv = base.find(pred[mate[hv]]);
                                    path1[hv] = strue;
                                }
                                if (mate[hw] != NULL) {
                                    hw = base.find(pred[mate[hw]]);
                                    path2[hw] = strue;
                                }
                            }
                            if (path1[hw] == strue || path2[hv] == strue) {
                                // shrink blossom
                                int b = (path1[hw] == strue) ? hw : hv; // base
                                shrinkPath(b, v, w);
                                shrinkPath(b, w, v);
                            } else {
                                // augment
                                Deque<Integer> p = new ArrayDeque<>();
                                findPath(p, v, hv);
                                p.addFirst(w);
                                while (!p.isEmpty()) {
                                    int a = p.pop();
                                    int b = p.pop();
                                    mate[a] = b;
                                    mate[b] = a;
                                }
                                labeledNodes.add(w);

                                for (Integer k : labeledNodes) {
                                    label[k] = Label.UNLABELED;
                                }
                                base.split(labeledNodes);

                                breakThrough = true;
                                break;
                            }
                        }
                    }
                }
            }

            // compute resulting matching
            Set<E> matching = new HashSet<>();
            for (E e : graph.edgeSet()) {
                V u = graph.getEdgeSource(e);
                V v = graph.getEdgeTarget(e);

                if (u.equals(v)) {
                    continue;
                }

                int uIndex = vertexIndexMap.get(u);
                int vIndex = vertexIndexMap.get(v);

                if (uIndex != vIndex && mate[uIndex] == vIndex) {
                    matching.add(e);
                    // cleanup
                    mate[uIndex] = uIndex;
                    mate[vIndex] = vIndex;
                }
            }

            return matching;
        }

        public Map<V, Integer> computeOddSetCover()
        {
            int[] osc = new int[nodes];
            Arrays.fill(osc, -1);
            int numberOfUnlabeled = 0;
            int arbUNode = -1;

            for (int v = 0; v < nodes; v++) {
                if (label[v] == Label.UNLABELED) {
                    numberOfUnlabeled++;
                    arbUNode = v;
                }
            }

            if (numberOfUnlabeled > 0) {
                osc[arbUNode] = 1;
                int lambda = (numberOfUnlabeled == 2 ? 0 : 2);
                for (int v = 0; v < nodes; v++) {
                    if (label[v] == Label.UNLABELED && v != arbUNode) {
                        osc[v] = lambda;
                    }
                }
            }

            int kappa = (numberOfUnlabeled <= 2 ? 2 : 3);
            for (int v = 0; v < nodes; v++) {
                if (base.find(v) != v && osc[base.find(v)] == -1) {
                    osc[base.find(v)] = kappa++;
                }
            }

            for (int v = 0; v < nodes; v++) {
                if (base.find(v) == v && osc[v] == -1) {
                    if (label[v] == Label.EVEN) {
                        osc[v] = 0;
                    }
                    if (label[v] == Label.ODD) {
                        osc[v] = 1;
                    }
                }
                if (base.find(v) != v) {
                    osc[v] = osc[base.find(v)];
                }
            }

            Map<V, Integer> oddSetCover = new HashMap<>();
            for (int v = 0; v < nodes; v++) {
                oddSetCover.put(vertexMap[v], osc[v]);
            }

            return oddSetCover;
        }

    }

    @Override
    public Matching<V, E> getMatching()
    {
        if (result == null) {
            Algorithm<V, E> alg = new Algorithm<>(graph, initializer);
            Set<E> matchingEdges = alg.computeMatching();

            int cardinality = matchingEdges.size();
            result = new MatchingImpl<>(graph, matchingEdges, cardinality);

            oddSetCover = alg.computeOddSetCover();
        }
        return result;
    }

    /**
     * Get an odd set cover which proves the optimality of the computed matching.
     * 
     * <p>
     * In order to check for optimality one needs to check that the odd-set-cover is a node labeling
     * that (a) covers the graph and (b) whose capacity is equal to the cardinality of the matching.
     * For (a) we check that every edge is either incident to a node with label 1 or connects two
     * nodes labeled $i$ for some $i \ge 2$. For (b) we count for each $i$ the number $n_i$ of nodes
     * with label $i$ and compute $S = n_1 + \sum_{i \ge 2} \floor{n_i/2}$.
     * 
     * <p>
     * Method {{@link #isOptimalMatching(Graph, Set, Map)} performs this check given a matching and
     * an odd-set-cover.
     * 
     * @return an odd set cover whose capacity is the same as the matching's cardinality
     */
    public Map<V, Integer> getOddSetCover()
    {
        getMatching();
        return oddSetCover;
    }

    /**
     * Check whether a matching is optimal.
     * 
     * The method first checks whether the matching is indeed a matching. Then it checks whether the
     * odd-set-cover provided is a node labeling that covers the graph and whose capacity is equal
     * to the cardinality of the matching.
     * 
     * First, we count for each $i$ the number $n_i$ of nodes with label $i$, and then compute $S =
     * n_1 + \sum_{i \ge 2} \floor{n_i/2}$. $S$ should be equal to the size of the matching. Then,
     * we check that every edge is incident to a node label one or connects two nodes labeled $i$
     * for some $i \ge 2$.
     * 
     * This method runs in linear time.
     * 
     * @param graph the graph
     * @param matching a matching
     * @param oddSetCover an odd set cover
     * @return true if the matching is optimal, false otherwise
     * 
     * @param <V> graph vertex type
     * @param <E> graph edge type
     */
    public static <V, E> boolean isOptimalMatching(
        Graph<V, E> graph, Set<E> matching, Map<V, Integer> oddSetCover)
    {
        // check matching
        Set<V> matched = new HashSet<>();
        for (E e : matching) {
            V s = graph.getEdgeSource(e);
            if (!matched.add(s)) {
                return false;
            }

            V t = graph.getEdgeTarget(e);
            if (!matched.add(t)) {
                return false;
            }
        }

        // check optimality
        int n = Math.max(2, graph.vertexSet().size());
        int kappa = 1;
        int[] count = new int[n];
        for (int i = 0; i < n; i++) {
            count[i] = 0;
        }

        for (V v : graph.vertexSet()) {
            Integer osc = oddSetCover.get(v);
            if (osc < 0 || osc >= n) {
                return false;
            }
            count[osc]++;
            if (osc > kappa) {
                kappa = osc;
            }
        }

        int s = count[1];
        for (int i = 2; i <= kappa; i++) {
            s += count[i] / 2;
        }
        if (s != matching.size()) {
            return false;
        }

        for (E e : graph.edgeSet()) {
            V v = graph.getEdgeSource(e);
            V w = graph.getEdgeTarget(e);

            int oscv = oddSetCover.get(v);
            int oscw = oddSetCover.get(w);

            if (v.equals(w) || oscv == 1 || oscw == 1 || (oscv == oscw && oscv >= 2)) {
                continue;
            }
            return false;
        }

        return true;
    }

    /**
     * Special integer vertex union-find.
     * 
     * @author Dimitrios Michail
     */
    private static class VertexPartition
    {
        private Item[] items;

        public VertexPartition(int n)
        {
            this.items = new Item[n];
            for (int i = 0; i < n; i++) {
                items[i] = new Item(i);
            }
        }

        public int find(int e)
        {
            return findItem(e).rep;
        }

        public void union(int a, int b)
        {
            assert a >= 0 && a < items.length;
            assert b >= 0 && b < items.length;

            Item ia = findItem(a);
            Item ib = findItem(b);

            // check if the elements are already in the same set
            if (ia == ib) {
                return;
            }

            // union by rank
            if (ia.rank > ib.rank) {
                ib.parent = ia;
            } else if (ia.rank < ib.rank) {
                ia.parent = ib;
            } else {
                ib.parent = ia;
                ia.rank += 1;
            }
        }

        /**
         * Name the representative of the group where e belongs as e.
         * 
         * @param e a vertex
         */
        public void name(int e)
        {
            Item ie = findItem(e);
            ie.rep = e;
        }

        /**
         * Split a partition. Assumes that it contains all members, otherwise bad things may happen.
         * 
         * @param toSplit all members of a partition
         */
        public void split(List<Integer> toSplit)
        {
            for (int i : toSplit) {
                Item item = items[i];
                item.parent = item;
                item.rep = i;
                item.rank = 0;
            }
        }

        private Item findItem(int e)
        {
            assert e >= 0 && e < items.length;

            // lookup
            Item current = items[e];
            while (true) {
                Item parent = current.parent;
                if (parent.equals(current)) {
                    break;
                }
                current = parent;
            }

            // path compression
            final Item root = current;
            current = items[e];
            while (!current.equals(root)) {
                Item parent = current.parent;
                current.parent = root;
                current = parent;
            }

            return root;
        }

        // the item class
        private static class Item
        {
            public int rep;
            public int rank;
            Item parent;

            public Item(int rep)
            {
                this.rep = rep;
                this.rank = 0;
                this.parent = this;
            }
        }

    }

}
