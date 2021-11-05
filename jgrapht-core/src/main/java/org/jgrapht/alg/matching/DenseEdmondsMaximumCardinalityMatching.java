/*
 * (C) Copyright 2017-2021, by Joris Kinable and Contributors.
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
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.util.*;

import java.util.*;
import java.util.stream.*;

/**
 * This implementation of Edmonds' blossom algorithm computes maximum cardinality matchings in
 * undirected graphs. A matching in a graph $G(V,E)$ is a subset of edges $M$ such that no two edges
 * in $M$ have a vertex in common. A matching has at most $\frac{1}{2|V|}$ edges. A node $v$ in $G$
 * is matched by matching $M$ if $M$ contains an edge incident to $v$. A matching is perfect if all
 * nodes are matched. By definition, a perfect matching consists of exactly $\frac{1}{2|V|}$ edges.
 * This algorithm will return a perfect matching if one exists. If no perfect matching exists, then
 * the largest (non-perfect) matching is returned instead. This algorithm does NOT compute a maximum
 * weight matching. In the special case that the input graph is bipartite, consider using
 * {@link HopcroftKarpMaximumCardinalityBipartiteMatching} instead.
 * <p>
 * To compute a maximum cardinality matching, at most $n$ augmenting path computations are
 * performed. Each augmenting path computation takes $O(m \alpha(m,n))$ time, where $\alpha(m,n)$ is
 * an inverse of the Ackerman function, $n$ is the number of vertices, and $m$ the number of edges.
 * This results in a total runtime complexity of O(nm alpha(m,n)). In practice, the number of
 * augmenting path computations performed is far smaller than $n$, since an efficient heuristic is
 * used to compute a near-optimal initial solution. This implementation is highly efficient: a
 * maximum matching in a graph of 2000 vertices and 1.5 million edges is calculated in a few
 * milliseconds on a desktop computer.<br>
 * The runtime complexity of this implementation could be improved to $O(nm)$ when the UnionFind
 * data structure used in this implementation is replaced by the linear-time set union data
 * structure proposed in: Gabow, H.N., Tarjan, R.E. A linear-time algorithm for a special case of
 * disjoint set union. Proc. Fifteenth Annual ACM Symposium on Theory of Computing, 1982, pp.
 * 246-251.
 * <p>
 * Edmonds' original algorithm first appeared in Edmonds, J. Paths, trees, and flowers. Canadian
 * Journal of Mathematics 17, 1965, pp. 449-467, and had a runtime complexity of $O(n^4)$. This
 * implementation however follows more closely the description provided in Tarjan, R.E. Data
 * Structures and Network Algorithms. Society for Industrial and Applied Mathematics, 1983, chapter
 * 9. In addition, the following sources were used for the implementation:
 *
 * <ul>
 * <li><a href=
 * "https://github.com/johnmay/beam/blob/master/core/src/main/java/uk/ac/ebi/beam/MaximumMatching.java">Java
 * implementation by John Mayfield</a></li>
 * <li><a href="http://www.keithschwarz.com/interesting/code/?dir=edmonds-matching">Java
 * implementation by Keith Schwarz</a></li>
 * <li><a href="http://www.boost.org/doc/libs/1_38_0/libs/graph/doc/maximum_matching.html">C++
 * implementation Boost library</a></li>
 * <li>Cook, W.J., Cunningham, W.H., Pulleyblank, W.R., Schrijver, A. Combinatorial Optimization.
 * Wiley 1997, chapter 5</li>
 * <li><a href="https://arxiv.org/pdf/1611.07541.pdf">Gabow, H.N. Data Structures for Weighted
 * Matching and Extensions to b-matching and f-factors, 2016</a></li>
 * </ul>
 * <p>
 * For future reference - A more efficient algorithm than the one implemented in this class exists:
 * Micali, S., Vazirani, V. An $O(\sqrt{n}m)$ algorithm for finding maximum matching in general
 * graphs. Proc. 21st Ann. Symp. on Foundations of Computer Science, IEEE, 1980, pp. 17–27. This is
 * the most efficient algorithm known for computing maximum cardinality matchings in general graphs.
 * More details on this algorithm can be found in:
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
 * @author Joris Kinable
 */
public class DenseEdmondsMaximumCardinalityMatching<V, E>
    implements
    MatchingAlgorithm<V, E>
{
    /* The graph we are matching on. */
    private final Graph<V, E> graph;
    /* (Heuristic) matching algorithm used to compute an initial feasible solution */
    private final MatchingAlgorithm<V, E> initializer;

    /* Ordered list of vertices */
    private List<V> vertices;
    /* Mapping of a vertex to their unique position in the ordered list of vertices */
    private Map<V, Integer> vertexIndexMap;

    /* A matching for the input graph (can be an empty set of edges) */
    private SimpleMatching matching;

    /* Number of matched vertices. */
    private int matchedVertices;

    /* -----Algorithm data structures below---------- */

    /** Storage of the forest, even and odd levels */
    private Levels levels;

    /** Special 'NIL' vertex. */
    private static final int NIL = -1;

    /** Queue of 'even' (exposed) vertices */
    private FixedSizeIntegerQueue queue;

    /** Union-Find to store blossoms. */
    private UnionFind<Integer> uf;

    /**
     * For each odd vertex condensed into a blossom, a bridge is defined. Suppose the examination of
     * edge $[v,w]$ causes a blossom to form containing odd vertex $x$. We define bridge(x) to be
     * $[v,w]$ if $x$ is an ancestor of $v$ before the blossom is formed, or $[w,v]$ if $x$ is an
     * ancestor of $w$.
     */
    private final Map<Integer, Pair<Integer, Integer>> bridges = new HashMap<>();

    /** Pre-allocated array which stores augmenting paths. */
    private int[] path;

    /* Pre-allocated bit sets to track paths in the trees. */
    private BitSet vAncestors, wAncestors;

    /**
     * Constructs a new instance of the algorithm. {@link GreedyMaximumCardinalityMatching} is used
     * to quickly generate a near optimal initial solution.
     * 
     * @param graph undirected graph (graph does not have to be simple)
     */
    public DenseEdmondsMaximumCardinalityMatching(Graph<V, E> graph)
    {
        this(graph, new GreedyMaximumCardinalityMatching<>(graph, false));
    }

    /**
     * Constructs a new instance of the algorithm.
     * 
     * @param graph undirected graph (graph does not have to be simple)
     * @param initializer heuristic matching algorithm used to quickly generate a (near optimal)
     *        initial feasible solution.
     */
    public DenseEdmondsMaximumCardinalityMatching(
        Graph<V, E> graph, MatchingAlgorithm<V, E> initializer)
    {
        this.graph = GraphTests.requireUndirected(graph);
        this.initializer = initializer;
    }

    /**
     * Prepares the data structures
     */
    private void init()
    {
        vertices = new ArrayList<>();
        vertices.addAll(graph.vertexSet());
        vertexIndexMap = new HashMap<>();
        for (int i = 0; i < vertices.size(); i++)
            vertexIndexMap.put(vertices.get(i), i);
        this.matching = new SimpleMatching(vertices.size());
        this.matchedVertices = 0;

        this.levels = new Levels(vertices.size());

        this.queue = new FixedSizeIntegerQueue(vertices.size());
        this.uf = new UnionFind<>(new HashSet<>(vertexIndexMap.values()));

        // temp storage of paths in the algorithm
        path = new int[vertices.size()];
        vAncestors = new BitSet(vertices.size());
        wAncestors = new BitSet(vertices.size());
    }

    /**
     * Calculates an initial feasible matching.
     * 
     * @param initializer algorithm used to compute the initial matching
     */
    private void warmStart(MatchingAlgorithm<V, E> initializer)
    {
        Matching<V, E> initialSolution = initializer.getMatching();
        for (E e : initialSolution.getEdges()) {
            V u = graph.getEdgeSource(e);
            V v = graph.getEdgeTarget(e);
            this.matching.match(vertexIndexMap.get(u), vertexIndexMap.get(v));
        }
        matchedVertices = initialSolution.getEdges().size() * 2;
    }

    /**
     * Search for an augmenting path.
     *
     * @return true if an augmenting path was found, false otherwise
     */
    private boolean augment()
    {
        // reset data structures
        levels.reset();
        uf.reset();
        bridges.clear();
        queue.clear();

        Deque<Integer> exposed = new ArrayDeque<>(matching.getExposed());

        while (!exposed.isEmpty()) {
            int root = exposed.pop();

            levels.setEven(root, root);
            queue.enqueue(root);
            // for each exposed vertex, start a bfs search
            while (!queue.isEmpty()) {
                int v = queue.poll(); // Even vertex

                for (V wOrig : Graphs.neighborListOf(graph, vertices.get(v))) {
                    int w = vertexIndexMap.get(wOrig);

                    // vertex w is even: we may have encountered a blossom.
                    if (levels.isEven(uf.find(w))) { // w is an even vertex
                        // if v and w belong to the same blossom, the edge has been shrunken away
                        // and we can ignore it. if not, we found a new blossom. We do not need to
                        // check whether v and w belong to the same tree since each tree is fully
                        // grown before we continue growing a new tree. Consequently, vertex w
                        // can only belong to the same tree as v.
                        if (!uf.inSameSet(v, w))
                            blossom(v, w); // Create a new blossom using bridge edge (v,w)
                    }

                    // vertex w is either odd or unreached. If it is unreached, we have found an
                    // augmenting path. If it is odd, we can grow the tree.
                    else if (levels.isOddOrUnreached(w)) { // w is odd or unreached

                        if (matching.isExposed(w)) { // w is unreached: we found an augmenting path
                            augment(v);
                            augment(w);
                            matching.match(v, w);
                            return true;
                        }

                        // w is an odd vertex: grow the tree
                        levels.setOdd(w, v);
                        int u = matching.opposite(w); // even vertex
                        levels.setEven(u, w);
                        queue.enqueue(u); // continue growing the tree from u
                    }
                }
            }
        }

        // no augmenting paths, matching is maximum
        return false;
    }

    /**
     * Creates a new blossom using bridge $(v,w)$. The blossom is an odd cycle. Nodes $v$ and $w$
     * are both even vertices.
     *
     * @param v endpoint of the bridge
     * @param w another endpoint the bridge
     */
    private void blossom(int v, int w)
    {
        // Compute the base of the blossom. Let p_1, p_2 be the paths from the root of the tree to v
        // resp. w. The base vertex is the last vertex p_1 and p_2 have in common. In a blossom, the
        // base vertex is unique in the sense that it is the only vertex incident to 2 unmatched
        // edges.
        int base = nearestCommonAncestor(v, w);

        // Compute resp the left side (v to base) and right side (w to base) of the blossom.
        blossomSupports(v, w, base);
        blossomSupports(w, v, base);

        // To complete the blossom, combine the left and the right sides.
        uf.union(v, base);
        uf.union(w, base);

        // Blossoms are efficiently stored in a UnionFind data structure uf. Ideally, uf.find(x) for
        // some vertex x returns the base u of the blossom containing x. However, when uf uses rank
        // compression, it cannot be guaranteed that the vertex returned is indeed the base of the
        // blossom. In fact, it can be any vertex of the blossom containing x. We therefore have to
        // ensure that the predecessor of the blossom's representative is the predecessor of the
        // actual base vertex.
        levels.setEven(uf.find(base), levels.getEven(base));
    }

    /**
     * This method creates one side of the blossom: the path from vertex $v$ to the base of the
     * blossom. The vertices encountered on this path are grouped together (union). The odd vertices
     * are added to the processing queue (odd vertices in a blossom become even) and a pointer to
     * the bridge $(v,w)$ is stored for each odd vertex. Notice the orientation of the bridge: the
     * first vertex of the bridge returned by bridge.get(x) is always on the same side of the
     * blossom as $x$.
     *
     * @param v an endpoint of the blossom bridge
     * @param w another endpoint of the blossom bridge
     * @param base the base of the blossom
     */
    private void blossomSupports(int v, int w, int base)
    {
        Pair<Integer, Integer> bridge = new Pair<>(v, w);
        v = uf.find(v);
        int u = v;
        while (v != base) {
            uf.union(v, u);
            u = levels.getEven(v); // odd vertex
            this.bridges.put(u, bridge);
            queue.enqueue(u);
            uf.union(v, u);
            v = uf.find(levels.getOdd(u)); // even vertex
        }
    }

    /**
     * Computes the base of the blossom formed by bridge edge $(v,w)$. The base vertex is the
     * nearest common ancestor of $v$ and $w$.
     * 
     * @param v one side of the bridge
     * @param w other side of the bridge
     * @return base of the blossom
     */
    private int nearestCommonAncestor(int v, int w)
    {
        vAncestors.clear();
        vAncestors.set(uf.find(v));
        wAncestors.clear();
        wAncestors.set(uf.find(w));

        // Walk back from $v$ and $w$ in the direction of the root of the tree, until their paths
        // intersect.
        while (true) {

            v = parent(v);
            vAncestors.set(v);
            w = parent(w);
            wAncestors.set(w);

            // vertex v is an ancestor of w, so v much be the base of the blossom
            if (wAncestors.get(v)) {
                return v;
            }
            // vertex w is an ancestor of v, so w much be the base of the blossom
            else if (vAncestors.get(w)) {
                return w;
            }
        }
    }

    /**
     * Compute the nearest even ancestor of even node $v$. If $v$ is the root of a tree, then this
     * method returns $v$ itself.
     *
     * @param v even vertex
     * @return the nearest even ancestor of $v$
     */
    private int parent(int v)
    {
        v = uf.find(v); // even vertex
        int parent = uf.find(levels.getEven(v)); // odd vertex, or v if v is the root of its tree
        if (parent == v)
            return v; // root of tree
        return uf.find(levels.getOdd(parent));
    }

    /**
     * Construct a path from vertex $v$ to the root of its tree, and use the resulting path to
     * augment the matching.
     *
     * @param v starting vertex (leaf in the tree)
     */
    private void augment(int v)
    {
        int n = buildPath(path, 0, v, NIL);
        for (int i = 2; i < n; i += 2) {
            matching.match(path[i], path[i - 1]);
        }
    }

    /**
     * Builds the path backwards from the specified start vertex to the end vertex. If the path
     * reaches a blossom then the path through the blossom is lifted to the original graph.
     *
     * @param path path storage
     * @param i offset (in path)
     * @param start start vertex
     * @param end end vertex
     * @return the total length of the path.
     */
    private int buildPath(int[] path, int i, int start, int end)
    {
        while (true) {

            // Lift the path through the blossom. The buildPath method always starts from an even
            // vertex. Vertices which were originally odd become even
            // when they are contracted into a blossom. If we start constructing the path from such
            // an odd vertex, we must 'lift' the path through the blossom.
            // To lift the path through the blossom, we have to walk from odd node u in the
            // direction of the bridge, cross the bridge, and then
            // continue in the direction of the tree root.
            while (levels.isOdd(start)) {
                Pair<Integer, Integer> bridge = bridges.get(start);

                // From the start vertex u, walk in the direction of the bridge (v,w). The first
                // edge encountered
                // on the path from u to v is always a matched edge. Notice that the path from u to
                // v leads away from the root of the tree. Since we only store
                // pointers in the direction of the root, we have to compute a path from v to u, and
                // reverse the resulting path.
                int j = buildPath(path, i, bridge.getFirst(), start);
                reverse(path, i, j - 1);
                i = j;

                // walk from the other side of the bridge up in the direction of the root.
                start = bridge.getSecond();
            }
            path[i++] = start; // even vertex

            // root of the tree
            if (matching.isExposed(start))
                return i;

            path[i++] = matching.opposite(start); // odd vertex

            // base case
            if (path[i - 1] == end)
                return i;

            start = levels.getOdd(path[i - 1]); // even vertex
        }
    }

    /**
     * Returns a matching of maximum cardinality. Each time this method is invoked, the matching is
     * computed from scratch. Consequently, it is possible to make changes to the graph and to
     * re-invoke this method on the altered graph.
     * 
     * @return a matching of maximum cardinality.
     */
    @Override
    public Matching<V, E> getMatching()
    {
        this.init();
        if (initializer != null)
            this.warmStart(initializer);

        // Continuously augment the matching until augmentation is no longer possible.
        while (matchedVertices < graph.vertexSet().size() - 1 && augment()) {
            matchedVertices += 2;
        }

        Set<E> edges = new LinkedHashSet<>();
        double cost = 0;
        for (int vx = 0; vx < vertices.size(); vx++) {
            if (matching.isExposed(vx))
                continue;
            V v = vertices.get(vx);
            V w = vertices.get(matching.opposite(vx));
            E edge = graph.getEdge(v, w);
            edges.add(edge);
            cost += 0.5 * graph.getEdgeWeight(edge);
        }

        return new MatchingImpl<>(graph, edges, cost);
    }

    /**
     * Checks whether the given matching is of maximum cardinality. A matching $m$ is maximum if
     * there does not exist a different matching $m'$ in the graph which is of larger cardinality.
     * This method is solely intended for verification purposes. Any matching returned by the
     * {@link #getMatching()} method in this class is guaranteed to be maximum.
     * <p>
     * To attest whether the matching is maximum, we use the Tutte-Berge Formula which provides a
     * tight bound on the cardinality of the matching. The Tutte-Berge Formula states: $m(G) =
     * \frac{1}{2} \min_{X \subseteq V} ( |X| - c_{\text{odd}}(G - X) + |V|), where $m(G)$ is the
     * size of the matching, $X$ a subset of vertices, $G-X$ the induced graph on vertex set $V(G)
     * \setminus X$, and $c_{\text{odd}}(G)$ the number of connected components of odd cardinality
     * in graph $G$.<br>
     * Note: to compute this bound, we do not iterate over all possible subsets $X$ (this would be
     * too expensive). Instead, $X$ is computed as a by-product of Edmonds' algorithm. Consequently,
     * the runtime of this method equals the time required to test for the existence of a single
     * augmenting path.<br>
     * This method does NOT check whether the matching is valid.
     * 
     * @param matching matching
     * @return true if the matching is maximum, false otherwise.
     */
    public boolean isMaximumMatching(Matching<V, E> matching)
    {
        // The matching is maximum if it is perfect, or if it leaves only one node exposed in a
        // graph with an odd number of vertices
        if (matching.getEdges().size() * 2 >= graph.vertexSet().size() - 1)
            return true;

        this.init(); // Reset data structures and use the provided matching as a starting point
        for (E e : matching.getEdges()) {
            V u = graph.getEdgeSource(e);
            V v = graph.getEdgeTarget(e);
            Integer ux = vertexIndexMap.get(u);
            Integer vx = vertexIndexMap.get(v);
            this.matching.match(ux, vx);
        }
        // Search for an augmenting path. If one is found, then clearly the matching is not maximum
        if (augment())
            return false;

        // A side effect of the Edmonds Blossom-Shrinking algorithm is that it computes what is
        // known as the
        // Edmonds-Gallai decomposition of a graph: it decomposes the graph into three disjoint sets
        // of vertices: odd, even, or free.
        // Let D(G) be the set of vertices such that for each v in D(G) there exists a maximum
        // matching missing v. Let A(G) be the set of vertices such that each v in A(G)
        // is a neighbor of D(G), but is not contained in D(G) itself. The set A(G) attains the
        // minimum in the Tutte-Berge Formula. It can be shown that
        // A(G)= {vertices labeled odd in the Edmonds Blossomg-Shrinking algorithm}. Note: we only
        // take odd vertices that are not consumed by blossoms (every blossom is even).
        Set<V> oddVertices = vertexIndexMap
            .values().stream().filter(vx -> levels.isOdd(vx) && !bridges.containsKey(vx))
            .map(vertices::get).collect(Collectors.toSet());
        Set<V> otherVertices = graph
            .vertexSet().stream().filter(v -> !oddVertices.contains(v)).collect(Collectors.toSet());

        Graph<V, E> subgraph = new AsSubgraph<>(graph, otherVertices, null); // Induced subgraph
                                                                             // defined on all
                                                                             // vertices which are
                                                                             // not odd.
        List<Set<V>> connectedComponents = new ConnectivityInspector<>(subgraph).connectedSets();
        long nrOddCardinalityComponents =
            connectedComponents.stream().filter(s -> s.size() % 2 == 1).count();

        return matching
            .getEdges()
            .size() == (graph.vertexSet().size() + oddVertices.size() - nrOddCardinalityComponents)
                / 2.0;
    }

    /**
     * Storage of the forest, even and odd levels.
     * 
     * We explicitly maintain a dirty mark in order to be able to cleanup only the values that we
     * have changed. This is important when the graph is sparse to avoid performing an $O(n)$
     * operation per augmentation.
     */
    private static class Levels
    {
        private int[] even, odd;
        private List<Integer> dirty;

        public Levels(int n)
        {
            this.even = new int[n];
            this.odd = new int[n];
            this.dirty = new ArrayList<>();

            Arrays.fill(even, NIL);
            Arrays.fill(odd, NIL);
        }

        public int getEven(int v)
        {
            return even[v];
        }

        public void setEven(int v, int value)
        {
            even[v] = value;
            if (value != NIL) {
                dirty.add(v);
            }
        }

        public int getOdd(int v)
        {
            return odd[v];
        }

        public void setOdd(int v, int value)
        {
            odd[v] = value;
            if (value != NIL) {
                dirty.add(v);
            }
        }

        public boolean isEven(int v)
        {
            return even[v] != NIL;
        }

        public boolean isOddOrUnreached(int v)
        {
            return odd[v] == NIL;
        }

        public boolean isOdd(int v)
        {
            return odd[v] != NIL;
        }

        public void reset()
        {
            for (int v : dirty) {
                even[v] = NIL;
                odd[v] = NIL;
            }
            dirty.clear();
        }
    }

    /**
     * Simple representation of a matching
     */
    private static class SimpleMatching
    {
        private static final int UNMATCHED = -1;
        private final int[] match;
        private Set<Integer> exposed;

        private SimpleMatching(int n)
        {
            this.match = new int[n];
            this.exposed = CollectionUtil.newHashSetWithExpectedSize(n);

            Arrays.fill(match, UNMATCHED);
            IntStream.range(0, n).forEach(exposed::add);
        }

        /**
         * Test whether a vertex is matched (i.e. incident to a matched edge).
         */
        boolean isMatched(int v)
        {
            return match[v] != UNMATCHED;
        }

        /**
         * Test whether a vertex is exposed (i.e. not incident to a matched edge).
         */
        boolean isExposed(int v)
        {
            return match[v] == UNMATCHED;
        }

        /**
         * For a given vertex v and matched edge (v,w), this function returns vertex w.
         */
        int opposite(int v)
        {
            assert isMatched(v);
            return match[v];
        }

        /**
         * Add the edge $(u,v)$ to the matched edge set.
         */
        void match(int u, int v)
        {
            match[u] = v;
            match[v] = u;
            exposed.remove(u);
            exposed.remove(v);
        }

        Set<Integer> getExposed()
        {
            return exposed;
        }

    }

    /** Utility function to reverse part of an array */
    private void reverse(int[] path, int i, int j)
    {
        while (i < j) {
            int tmp = path[i];
            path[i] = path[j];
            path[j] = tmp;
            i++;
            j--;
        }
    }
}
