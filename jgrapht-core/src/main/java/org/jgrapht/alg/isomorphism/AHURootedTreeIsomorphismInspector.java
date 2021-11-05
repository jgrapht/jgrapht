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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.traverse.*;
import org.jgrapht.util.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * This is an implementation of the AHU algorithm for detecting an (unweighted) isomorphism between
 * two rooted trees. Please see
 * <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">mathworld.wolfram.com</a> for a
 * complete definition of the isomorphism problem for general graphs.
 *
 * <p>
 * The original algorithm was first presented in "Alfred V. Aho and John E. Hopcroft. 1974. The
 * Design and Analysis of Computer Algorithms (1st ed.). Addison-Wesley Longman Publishing Co.,
 * Inc., Boston, MA, USA."
 * </p>
 *
 * <p>
 * This implementation runs in linear time (in the number of vertices of the input trees) while
 * using a linear amount of memory.
 * </p>
 *
 * <p>
 * Note: If the input graph is directed, it effectively considers only the subtree reachable from
 * the specified root.
 * </p>
 *
 * <p>
 * For an implementation that supports unrooted trees see
 * {@link AHUUnrootedTreeIsomorphismInspector}. <br>
 * For an implementation that supports rooted forests see {@link AHUForestIsomorphismInspector}.
 * </p>
 *
 * <p>
 * Note: This inspector only returns a single mapping (chosen arbitrarily) rather than all possible
 * mappings.
 * </p>
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 *
 * @author Alexandru Valeanu
 */
public class AHURootedTreeIsomorphismInspector<V, E>
    implements
    IsomorphismInspector<V, E>
{
    private final Graph<V, E> tree1;
    private final Graph<V, E> tree2;

    private V root1;
    private V root2;

    private Map<V, V> forwardMapping;
    private Map<V, V> backwardMapping;

    /**
     * Construct a new AHU rooted tree isomorphism inspector.
     *
     * Note: The constructor does NOT check if the input trees are valid.
     *
     * @param tree1 the first rooted tree
     * @param root1 the root of the first tree
     * @param tree2 the second rooted tree
     * @param root2 the root of the second tree
     * @throws NullPointerException if {@code tree1} or {@code tree2} is {@code null}
     * @throws NullPointerException if {@code root1} or {@code root2} is {@code null}
     * @throws IllegalArgumentException if {@code tree1} or {@code tree2} is empty
     * @throws IllegalArgumentException if {@code root1} or {@code root2} is an invalid vertex
     */
    public AHURootedTreeIsomorphismInspector(Graph<V, E> tree1, V root1, Graph<V, E> tree2, V root2)
    {
        validateTree(tree1, root1);
        this.tree1 = tree1;
        this.root1 = root1;

        validateTree(tree2, root2);
        this.tree2 = tree2;
        this.root2 = root2;
    }

    private void validateTree(Graph<V, E> tree, V root)
    {
        assert GraphTests.isSimple(tree);
        Objects.requireNonNull(tree, "input forest cannot be null");
        Objects.requireNonNull(root, "root cannot be null");

        if (tree.vertexSet().isEmpty()) {
            throw new IllegalArgumentException("tree cannot be empty");
        }

        if (!tree.containsVertex(root)) {
            throw new IllegalArgumentException("root not contained in forest");
        }
    }

    private void bfs(Graph<V, E> graph, V root, List<List<V>> levels)
    {
        BreadthFirstIterator<V, E> bfs = new BreadthFirstIterator<>(graph, root);

        while (bfs.hasNext()) {
            V u = bfs.next();

            if (levels.size() < bfs.getDepth(u) + 1) {
                levels.add(new ArrayList<>());
            }

            levels.get(bfs.getDepth(u)).add(u);
        }
    }

    private List<List<V>> computeLevels(Graph<V, E> graph, V root)
    {
        List<List<V>> levels = new ArrayList<>();
        bfs(graph, root, levels);
        return levels;
    }

    private void matchVerticesWithSameLabel(V root1, V root2, Map<V, Integer>[] canonicalName)
    {
        Queue<Pair<V, V>> queue = new ArrayDeque<>();
        queue.add(Pair.of(root1, root2));

        while (!queue.isEmpty()) {
            Pair<V, V> pair = queue.poll();
            V u = pair.getFirst();
            V v = pair.getSecond();

            forwardMapping.put(u, v);
            backwardMapping.put(v, u);

            Map<Integer, List<V>> labelList =
                CollectionUtil.newHashMapWithExpectedSize(tree1.degreeOf(u));

            for (E edge : tree1.outgoingEdgesOf(u)) {
                V next = Graphs.getOppositeVertex(tree1, edge, u);

                // The check if only needed when the input graph is undirected in order to
                // avoid walking back "up" the tree.
                if (!forwardMapping.containsKey(next)) {
                    labelList
                        .computeIfAbsent(canonicalName[0].get(next), x -> new ArrayList<>())
                        .add(next);
                }
            }

            for (E edge : tree2.outgoingEdgesOf(v)) {
                V next = Graphs.getOppositeVertex(tree2, edge, v);

                if (!backwardMapping.containsKey(next)) {
                    List<V> list = labelList.get(canonicalName[1].get(next));

                    if (list == null || list.isEmpty()) {
                        forwardMapping.clear();
                        backwardMapping.clear();
                        return;
                    }

                    V pairedNext = list.remove(list.size() - 1);
                    queue.add(Pair.of(pairedNext, next));
                }
            }
        }
    }

    private boolean isomorphismExists(V root1, V root2)
    {
        // already computed?
        if (forwardMapping != null) {
            return !forwardMapping.isEmpty();
        }

        this.forwardMapping = new HashMap<>();
        this.backwardMapping = new HashMap<>();

        @SuppressWarnings("unchecked") Map<V, Integer>[] canonicalName =
            (Map<V, Integer>[]) Array.newInstance(Map.class, 2);
        canonicalName[0] = CollectionUtil.newHashMapWithExpectedSize(tree1.vertexSet().size());
        canonicalName[1] = CollectionUtil.newHashMapWithExpectedSize(tree2.vertexSet().size());

        List<List<V>> nodesByLevel1 = computeLevels(tree1, root1);
        List<List<V>> nodesByLevel2 = computeLevels(tree2, root2);

        if (nodesByLevel1.size() != nodesByLevel2.size())
            return false;

        final int maxLevel = nodesByLevel1.size() - 1;

        Map<List<Integer>, Integer> canonicalNameToInt = new HashMap<>();

        int freshName = 0;

        for (int lvl = maxLevel; lvl >= 0; lvl--) {
            @SuppressWarnings("unchecked") List<V>[] level =
                (List<V>[]) Array.newInstance(List.class, 2);

            level[0] = nodesByLevel1.get(lvl);
            level[1] = nodesByLevel2.get(lvl);

            if (level[0].size() != level[1].size()) {
                return false;
            }

            final int n = level[0].size();

            for (int k = 0; k < 2; k++) {
                Graph<V, E> graph = (k == 0) ? tree1 : tree2;

                for (int i = 0; i < n; i++) {
                    V u = level[k].get(i);

                    List<Integer> list = new ArrayList<>();
                    for (E edge : graph.outgoingEdgesOf(u)) {
                        V v = Graphs.getOppositeVertex(graph, edge, u);
                        int name = canonicalName[k].getOrDefault(v, -1);

                        if (name != -1) {
                            list.add(name);
                        }
                    }

                    RadixSort.sort(list);

                    Integer intName = canonicalNameToInt.get(list);

                    if (intName == null) {
                        canonicalNameToInt.put(list, freshName);
                        intName = freshName;
                        freshName++;
                    }

                    canonicalName[k].put(u, intName);
                }
            }
        }

        matchVerticesWithSameLabel(root1, root2, canonicalName);

        if (forwardMapping.size() != tree1.vertexSet().size()) {
            forwardMapping.clear();
            backwardMapping.clear();
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<GraphMapping<V, E>> getMappings()
    {
        GraphMapping<V, E> iterMapping = getMapping();

        if (iterMapping == null)
            return Collections.emptyIterator();
        else
            return Collections.singletonList(iterMapping).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isomorphismExists()
    {
        return isomorphismExists(this.root1, this.root2);
    }

    /**
     * Get an isomorphism between the input trees or {@code null} if none exists.
     *
     * @return isomorphic mapping, {@code null} is none exists
     */
    public IsomorphicGraphMapping<V, E> getMapping()
    {
        if (isomorphismExists())
            return new IsomorphicGraphMapping<>(forwardMapping, backwardMapping, tree1, tree2);
        else
            return null;
    }
}
