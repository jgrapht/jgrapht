/*
 * (C) Copyright 2018-2021, by Alexandru Valeanu and Contributors.
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
package org.jgrapht.alg.lca;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * Tarjan's offline algorithm for computing lowest common ancestors in rooted trees and forests.
 *
 * <p>
 * See the article on <a href=
 * "https://en.wikipedia.org/wiki/Tarjan%27s_off-line_lowest_common_ancestors_algorithm">wikipedia</a>
 * for more information on the algorithm.
 *
 * </p>
 *
 * <p>
 * The original algorithm can be found in <i>Gabow, H. N.; Tarjan, R. E. (1983), "A linear-time
 * algorithm for a special case of disjoint set union", Proceedings of the 15th ACM Symposium on
 * Theory of Computing (STOC), pp. 246–251, doi:10.1145/800061.808753</i>
 * </p>
 *
 * <p>
 * Preprocessing Time complexity: $O(1)$<br>
 * Preprocessing Space complexity: $O(1)$<br>
 * Query Time complexity: $O(|V| log^{*}(|V|) + |Q|)$ where $|Q|$ is the number of queries<br>
 * Query Space complexity: $O(|V| + |Q|)$ where $|Q|$ is the number of queries<br>
 * </p>
 *
 * <p>
 * For small (i.e. less than 100 vertices) trees or forests, all implementations behave similarly.
 * For larger trees/forests with less than 50,000 queries you can use either
 * {@link BinaryLiftingLCAFinder}, {@link HeavyPathLCAFinder} or {@link EulerTourRMQLCAFinder}. Fo
 * more than that use {@link EulerTourRMQLCAFinder} since it provides $O(1)$ per query.<br>
 * Space-wise, {@link HeavyPathLCAFinder} and {@link TarjanLCAFinder} only use a linear amount while
 * {@link BinaryLiftingLCAFinder} and {@link EulerTourRMQLCAFinder} require linearithmic space.<br>
 * For DAGs, use {@link NaiveLCAFinder}.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexandru Valeanu
 */
public class TarjanLCAFinder<V, E>
    implements
    LowestCommonAncestorAlgorithm<V>
{
    private Graph<V, E> graph;
    private Set<V> roots;

    private UnionFind<V> unionFind;

    private Map<V, V> ancestors;

    private Set<V> blackNodes;

    private HashMap<V, Set<Integer>> queryOccurs;
    private List<V> lowestCommonAncestors;

    private List<Pair<V, V>> queries;

    /**
     * Construct a new instance of the algorithm.
     *
     * <p>
     * Note: The constructor will NOT check if the input graph is a valid tree.
     *
     * @param graph the input graph
     * @param root the root of the graph
     */
    public TarjanLCAFinder(Graph<V, E> graph, V root)
    {
        this(graph, Collections.singleton(Objects.requireNonNull(root, "root cannot be null")));
    }

    /**
     * Construct a new instance of the algorithm.
     *
     * <p>
     * Note: If two roots appear in the same tree, an error will be thrown.
     *
     * <p>
     * Note: The constructor will NOT check if the input graph is a valid forest.
     *
     * @param graph the input graph
     * @param roots the set of roots of the graph
     */
    public TarjanLCAFinder(Graph<V, E> graph, Set<V> roots)
    {
        this.graph = Objects.requireNonNull(graph, "graph cannot be null");
        this.roots = Objects.requireNonNull(roots, "roots cannot be null");

        if (this.roots.isEmpty())
            throw new IllegalArgumentException("roots cannot be empty");

        if (!graph.vertexSet().containsAll(roots))
            throw new IllegalArgumentException("at least one root is not a valid vertex");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V getLCA(V a, V b)
    {
        return getBatchLCA(Collections.singletonList(Pair.of(a, b))).get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<V> getBatchLCA(List<Pair<V, V>> queries)
    {
        return computeTarjan(queries);
    }

    private void initialize()
    {
        unionFind = new UnionFind<>(Collections.emptySet());
        ancestors = new HashMap<>();
        blackNodes = new HashSet<>();
    }

    private void clear()
    {
        unionFind = null;
        ancestors = null;
        blackNodes = null;
        queryOccurs = null;

        queries = null;
        lowestCommonAncestors = null;
    }

    private List<V> computeTarjan(List<Pair<V, V>> queries)
    {
        initialize();

        this.queries = queries;
        this.lowestCommonAncestors = new ArrayList<>(queries.size());

        this.queryOccurs = new HashMap<>();

        for (int i = 0; i < queries.size(); i++) {
            V a = this.queries.get(i).getFirst();
            V b = this.queries.get(i).getSecond();

            if (!graph.containsVertex(a))
                throw new IllegalArgumentException("invalid vertex: " + a);

            if (!graph.containsVertex(b))
                throw new IllegalArgumentException("invalid vertex: " + b);

            if (a.equals(b))
                this.lowestCommonAncestors.add(a);
            else {
                queryOccurs.computeIfAbsent(a, x -> new HashSet<>()).add(i);
                queryOccurs.computeIfAbsent(b, x -> new HashSet<>()).add(i);

                this.lowestCommonAncestors.add(null);
            }
        }

        Set<V> visited = new HashSet<>();

        for (V root : roots) {
            if (visited.contains(root))
                throw new IllegalArgumentException("multiple roots in the same tree");

            blackNodes.clear();
            computeTarjanOLCA(root, null, visited);
        }

        List<V> tmpRef = lowestCommonAncestors;
        clear();

        return tmpRef;
    }

    private void computeTarjanOLCA(V u, V p, Set<V> visited)
    {
        visited.add(u);
        unionFind.addElement(u);
        ancestors.put(u, u);

        for (E edge : graph.outgoingEdgesOf(u)) {
            V v = Graphs.getOppositeVertex(graph, edge, u);

            if (!v.equals(p)) {
                computeTarjanOLCA(v, u, visited);
                unionFind.union(u, v);
                ancestors.put(unionFind.find(u), u);
            }
        }

        blackNodes.add(u);

        for (int index : queryOccurs.computeIfAbsent(u, x -> new HashSet<>())) {
            Pair<V, V> query = queries.get(index);
            V v;

            if (query.getFirst().equals(u))
                v = query.getSecond();
            else
                v = query.getFirst();

            if (blackNodes.contains(v)) {
                lowestCommonAncestors.set(index, ancestors.get(unionFind.find(v)));
            }
        }
    }

    /**
     * Note: This operation is not supported.<br>
     *
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException if the method is called
     */
    @Override
    public Set<V> getLCASet(V a, V b)
    {
        throw new UnsupportedOperationException();
    }
}
