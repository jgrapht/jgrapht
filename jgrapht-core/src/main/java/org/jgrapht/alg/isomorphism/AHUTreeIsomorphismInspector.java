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
package org.jgrapht.alg.isomorphism;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.shortestpath.TreeMeasurer;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.util.RadixSort;

import java.util.*;

/**
 * This is an implementation of the AHU algorithm for detecting an isomorphism between two trees.
 * Please see <a href="http://mathworld.wolfram.com/GraphIsomorphism.html">mathworld.wolfram.com</a> for a complete
 * definition of the isomorphism problem for general graphs.
 *
 * <p>
 *     The original algorithm was first presented in "Alfred V. Aho and John E. Hopcroft. 1974.
 *     The Design and Analysis of Computer Algorithms (1st ed.). Addison-Wesley Longman Publishing Co., Inc., Boston, MA, USA."
 * </p>
 *
 * <p>
 *     This implementation runs in linear time (in the number of vertices of the input trees)
 *     while using a linear amount of memory.
 * </p>
 *
 * <p>
 *      For an implementation that supports rooted forests see {@link AHUForestIsomorphismInspector}.
 * </p>
 *
 * @param <V> the type of the vertices
 * @param <E> the type of the edges
 *
 * @author Alexandru Valeanu
 */
public class AHUTreeIsomorphismInspector<V, E> {
    private final Graph<V, E> tree1;
    private final Graph<V, E> tree2;

    private V root1;
    private V root2;

    private Map<V, V> forwardMapping;
    private Map<V, V> backwardMapping;

    /**
     * Construct a new AHU unrooted tree isomorphism inspector.
     *
     * Note: The constructor does NOT check if the input trees are valid.
     *
     * @param tree1 the first tree
     * @param tree2 the second tree
     */
    public AHUTreeIsomorphismInspector(Graph<V, E> tree1, Graph<V, E> tree2){
        this(tree1, null, tree2, null);
    }

    /**
     * Construct a new AHU rooted tree isomorphism inspector.
     *
     * Note: The constructor does NOT check if the input trees are valid.
     *
     * @param tree1 the first rooted tree
     * @param root1 the root of the first tree
     * @param tree2 the second rooted tree
     * @param root2 the root of the second tree
     */
    public AHUTreeIsomorphismInspector(Graph<V, E> tree1, V root1, Graph<V, E> tree2, V root2){
        this.tree1 = Objects.requireNonNull(tree1, "tree1 cannot be null");
        this.tree2 = Objects.requireNonNull(tree2, "tree2 cannot be null");

        this.root1 = root1;
        this.root2 = root2;
    }

    private void bfs(Graph<V, E> graph, V root, List<List<V>> levels){
        BreadthFirstIterator<V, E> bfs = new BreadthFirstIterator<>(graph, root);

        while (bfs.hasNext()){
            V u = bfs.next();

            if (levels.size() < bfs.getDepth(u) + 1){
                levels.add(new ArrayList<>());
            }

            levels.get(bfs.getDepth(u)).add(u);
        }
    }

    private List<List<V>> computeLevels(Graph<V, E> graph, V root){
        List<List<V>> levels = new ArrayList<>();
        bfs(graph, root, levels);
        return levels;
    }

    private void matchVerticesWithSameLabel(V root1, V root2,
                                            Map<V, Integer>[] canonicalName){
        Queue<Pair<V, V>> queue = new ArrayDeque<>();
        queue.add(Pair.of(root1, root2));

        while (!queue.isEmpty()){
            Pair<V, V> pair = queue.poll();
            V u = pair.getFirst();
            V v = pair.getSecond();

            forwardMapping.put(u, v);
            backwardMapping.put(v, u);

            Map<Integer, List<V>> labelList = new HashMap<>(tree1.degreeOf(u));

            for (E edge: tree1.edgesOf(u)){
                V next = Graphs.getOppositeVertex(tree1, edge, u);

                if (!forwardMapping.containsKey(next)){
                    labelList.computeIfAbsent(canonicalName[0].get(next), x -> new ArrayList<>()).add(next);
                }
            }

            for (E edge: tree2.edgesOf(v)){
                V next = Graphs.getOppositeVertex(tree2, edge, v);

                if (!backwardMapping.containsKey(next)){
                    List<V> list = labelList.get(canonicalName[1].get(next));

                    if (list == null || list.isEmpty()){
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

    private boolean isomorphismExists(V root1, V root2){
        this.forwardMapping = new HashMap<>();
        this.backwardMapping = new HashMap<>();

        // Are both graphs empty?
        if (Objects.isNull(root1) && Objects.isNull(root2))
            return true;

        @SuppressWarnings("unchecked")
        Map<V, Integer>[] canonicalName = new Map[2];
        canonicalName[0] = new HashMap<>(tree1.vertexSet().size());
        canonicalName[1] = new HashMap<>(tree2.vertexSet().size());

        List<List<V>> nodesByLevel1 = computeLevels(tree1, root1);
        List<List<V>> nodesByLevel2 = computeLevels(tree2, root2);

        if (nodesByLevel1.size() != nodesByLevel2.size())
            return false;

        final int MAX_LEVEL = nodesByLevel1.size() - 1;

        @SuppressWarnings("unchecked")
        Map<String, Integer> canonicalNameToInt = new HashMap<>();

        List<Map<Integer, List<V>>> sameLabelBagsByLevel = new ArrayList<>(nodesByLevel1.size());

        int freshName = 0;

        for (int lvl = MAX_LEVEL; lvl >= 0; lvl--) {
            @SuppressWarnings("unchecked")
            List<V>[] level = new List[2];

            level[0] = nodesByLevel1.get(lvl);
            level[1] = nodesByLevel2.get(lvl);

            if (level[0].size() != level[1].size()) {
                return false;
            }

            final int n = level[0].size();

            @SuppressWarnings("unchecked")
            Map<Integer, List<V>> sameLabelBags = new HashMap<>(n);

            for (int k = 0; k < 2; k++) {
                for (int i = 0; i < n; i++) {
                    V u = level[k].get(i);

                    Graph<V, E> graph = tree1;

                    if (k == 1)
                        graph = tree2;

                    List<Integer> list = new ArrayList<>();
                    for (E edge: graph.edgesOf(u)){
                        V v = Graphs.getOppositeVertex(graph, edge, u);
                        int name = canonicalName[k].getOrDefault(v, -1);

                        if (name != -1){
                            list.add(name);
                        }
                    }

                    RadixSort.sort(list);

                    StringBuilder sb = new StringBuilder();
                    sb.append(1).append(",");

                    for (int x: list)
                        sb.append(x).append(",");

                    sb.append(0);

                    String name = sb.toString();
                    Integer intName = canonicalNameToInt.get(name);

                    if (intName == null){
                        canonicalNameToInt.put(name, freshName);
                        intName = freshName;
                        freshName++;
                    }

                    canonicalName[k].put(u, intName);

                    if (k == 1){
                        List<V> bag = sameLabelBags.computeIfAbsent(intName, x -> new ArrayList<>());
                        bag.add(u);
                    }
                }
            }

            sameLabelBagsByLevel.add(sameLabelBags);
        }

        Collections.reverse(sameLabelBagsByLevel);

        matchVerticesWithSameLabel(root1, root2, canonicalName);

        if (forwardMapping.size() != tree1.vertexSet().size()){
            forwardMapping.clear();
            backwardMapping.clear();
            return false;
        }

        return true;
    }


    public boolean isomorphismExists(){
        if (this.root1 == null || this.root2 == null){
            if (tree1.vertexSet().isEmpty() && tree2.vertexSet().isEmpty())
                return isomorphismExists(null, null);

            TreeMeasurer<V, E> treeMeasurer1 = new TreeMeasurer<>(tree1);
            V[] centers1 = (V[]) treeMeasurer1.getGraphCenter().toArray();

            TreeMeasurer<V, E> treeMeasurer2 = new TreeMeasurer<>(tree2);
            V[] centers2 = (V[]) treeMeasurer2.getGraphCenter().toArray();

            if (centers1.length == 1 && centers2.length == 1){
                return isomorphismExists(centers1[0], centers2[0]);
            }
            else if (centers1.length == 2 && centers2.length == 2){
                if (!isomorphismExists(centers1[0], centers2[0])){
                    return isomorphismExists(centers1[1], centers2[1]);
                }
                else{
                    return true;
                }
            }
            else {
                return false;
            }
        }

        return isomorphismExists(this.root1, this.root2);
    }

    public IsomorphicTreeMapping<V, E> getIsomorphism(){
        if (forwardMapping == null){
            if (isomorphismExists())
                return new IsomorphicTreeMapping<>(forwardMapping, backwardMapping, tree1, tree2);
            else
                return null;
        }

        if (forwardMapping.size() != tree1.vertexSet().size())
            return null;
        else
            return new IsomorphicTreeMapping<>(forwardMapping, backwardMapping, tree1, tree2);
    }
}
