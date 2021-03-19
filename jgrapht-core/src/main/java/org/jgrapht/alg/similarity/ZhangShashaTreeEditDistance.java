/*
 * (C) Copyright 2020-2021, by Semen Chudakov and Contributors.
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
package org.jgrapht.alg.similarity;

import org.jgrapht.Graph;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

/**
 * Dynamic programming algorithm for computing edit distance between trees.
 *
 * <p>
 * The algorithm is originally described in Zhang, Kaizhong & Shasha, Dennis. (1989). Simple Fast
 * Algorithms for the Editing Distance Between Trees and Related Problems. SIAM J. Comput.. 18.
 * 1245-1262. 10.1137/0218082.
 *
 * <p>
 * The time complexity of the algorithm if $O(|T_1|\cdot|T_2|\cdot min(depth(T_1),leaves(T_1)) \cdot
 * min(depth(T_2),leaves(T_2)))$. Space complexity is $O(|T_1|\cdot |T_2|)$, where $|T_1|$ and
 * $|T_2|$ denote number of vertices in trees $T_1$ and $T_2$ correspondingly, $leaves()$ function
 * returns number of leaf vertices in a tree.
 *
 *
 * <p>
 * The tree edit distance problem is defined in a following way. Consider $2$ trees $T_1$ and $T_2$
 * with root vertices $r_1$ and $r_2$ correspondingly. For those trees there are 3 elementary
 * modification actions:
 *
 * <ul>
 * <li>Remove a vertex $v$ from $T_1$.</li>
 * <li>Insert a vertex $v$ into $T_2$.</li>
 * <li>Change vertex $v_1$ in $T_1$ to vertex $v_2$ in $T_2$.</li>
 * </ul>
 *
 * The algorithm assigns a cost to each of those operations which also depends on the vertices. The
 * problem is then to compute a sequence of edit operations which transforms $T_1$ into $T_2$ and
 * has a minimum cost over all such sequences. Here the cost of a sequence of edit operations is
 * defined as sum of costs of individual operations.
 *
 * <p>
 * The algorithm is based on a dynamic programming principle and assigns a label to each vertex in
 * the trees which is equal to its index in post-oder traversal. It also uses a notion of a keyroot
 * which is defined as a vertex in a tree which has a left sibling. Additionally a special $l()$
 * function is introduced with returns for every vertex the index of its leftmost child wrt the
 * post-order traversal in the tree.
 *
 * <p>
 * Solving the tree edit problem distance is divided into computing edit distance for every pair of
 * subtrees rooted at vertices $v_1$ and $v_2$ where $v_1$ is a keyroot in the first tree and $v_2$
 * is a keyroot in the second tree.
 *
 * @param <V> graph vertex type
 * @param <E> graph edge type
 * @author Semen Chudakov
 */
public class ZhangShashaTreeEditDistance<V, E>
{

    /**
     * First tree for which the distance is computed by this algorithm.
     */
    private Graph<V, E> tree1;
    /**
     * Root vertex of the {@code tree1}.
     */
    private V root1;

    /**
     * Second tree for which the distance is computed by this algorithm.
     */
    private Graph<V, E> tree2;
    /**
     * Root vertex of the {@code tree2}.
     */
    private V root2;

    /**
     * Function which computes cost of inserting a particular vertex into {@code tree2}.
     */
    private ToDoubleFunction<V> insertCost;
    /**
     * Function which computes cost of removing a particular vertex from {2code tree1}.
     */
    private ToDoubleFunction<V> removeCost;
    /**
     * Function which computes cost of changing a vertex $v1$ in {@code tree1} to vertex $v2$ in
     * {@code tree2}.
     */
    private ToDoubleBiFunction<V, V> changeCost;

    /**
     * Array with edit distances between subtrees of {@code tree1} and {@code tree2}. Formally,
     * $treeDistances[i][j]$ stores edit distance between subtree of {@code tree1} rooted at vertex
     * $i+1$ and subtree of {@code tree2} rooted at vertex $j+1$, where $i$ and $j$ are vertex
     * indices from the corresponding tree orderings.
     */
    private double[][] treeDistances;
    /**
     * Array with lists of edit operations which transform subtrees of {@code tree1} into subtrees
     * {@code tree2}. Formally, editOperationLists[i][j]$ stores a list of edit operations which
     * transform subtree {@code tree1} rooted at vertex $i$ into subtree of {@code tree2} rooted at
     * vertex $j$, where $i$ and $j$ are vertex indices from the corresponding tree orderings.
     */
    private List<List<List<EditOperation<V>>>> editOperationLists;

    /**
     * Helper field which indicates whether the algorithm has already been executed for
     * {@code tree1} and {@code tree2}.
     */
    private boolean algorithmExecuted;

    /**
     * Constructs an instance of the algorithm for the given {@code tree1}, {@code root1},
     * {@code tree2} and {@code root2}. This constructor sets following default values for the
     * distance functions. The {@code insertCost} and {@code removeCost} always return $1.0$, the
     * {@code changeCost} return $0.0$ if vertices are equal and {@code 1.0} otherwise.
     *
     * @param tree1 a tree
     * @param root1 root vertex of {@code tree1}
     * @param tree2 a tree
     * @param root2 root vertex of {@code tree2}
     */
    public ZhangShashaTreeEditDistance(Graph<V, E> tree1, V root1, Graph<V, E> tree2, V root2)
    {
        this(tree1, root1, tree2, root2, v -> 1.0, v -> 1.0, (v1, v2) -> {
            if (v1.equals(v2)) {
                return 0.0;
            }
            return 1.0;
        });
    }

    /**
     * Constructs an instance of the algorithm for the given {@code tree1}, {@code root1},
     * {@code tree2}, {@code root2}, {@code insertCost}, {@code removeCost} and {@code changeCost}.
     *
     * @param tree1 a tree
     * @param root1 root vertex of {@code tree1}
     * @param tree2 a tree
     * @param root2 root vertex of {@code tree2}
     * @param insertCost cost function for inserting a node into {@code tree1}
     * @param removeCost cost function for removing a node from {@code tree2}
     * @param changeCost cost function of changing a node in {@code tree1} to a node in
     *        {@code tree2}
     */
    public ZhangShashaTreeEditDistance(
        Graph<V, E> tree1, V root1, Graph<V, E> tree2, V root2, ToDoubleFunction<V> insertCost,
        ToDoubleFunction<V> removeCost, ToDoubleBiFunction<V, V> changeCost)
    {
        this.tree1 = Objects.requireNonNull(tree1, "graph1 cannot be null!");
        this.root1 = Objects.requireNonNull(root1, "root1 cannot be null!");
        this.tree2 = Objects.requireNonNull(tree2, "graph2 cannot be null!");
        this.root2 = Objects.requireNonNull(root2, "root2 cannot be null!");
        this.insertCost = Objects.requireNonNull(insertCost, "insertCost cannot be null!");
        this.removeCost = Objects.requireNonNull(removeCost, "removeCost cannot be null!");
        this.changeCost = Objects.requireNonNull(changeCost, "changeCost cannot be null!");
        if (!GraphTests.isTree(tree1)) {
            throw new IllegalArgumentException("graph1 must be a tree!");
        }
        if (!GraphTests.isTree(tree2)) {
            throw new IllegalArgumentException("graph2 must be a tree!");
        }

        int m = tree1.vertexSet().size();
        int n = tree2.vertexSet().size();
        treeDistances = new double[m][n];
        editOperationLists = new ArrayList<>(m);
        for (int i = 0; i < m; ++i) {
            editOperationLists.add(new ArrayList<>(Collections.nCopies(n, null)));
        }
    }

    /**
     * Computes edit distance for {@code tree1} and {@code tree2}.
     *
     * @return edit distance between {@code tree1} and {@code tree2}
     */
    public double getDistance()
    {
        lazyRunAlgorithm();
        int m = tree1.vertexSet().size();
        int n = tree2.vertexSet().size();
        return treeDistances[m - 1][n - 1];
    }

    /**
     * Computes a list of edit operations which transform {@code tree1} into {@code tree2}.
     *
     * @return list of edit operations
     */
    public List<EditOperation<V>> getEditOperationLists()
    {
        lazyRunAlgorithm();
        int m = tree1.vertexSet().size();
        int n = tree2.vertexSet().size();
        return Collections.unmodifiableList(editOperationLists.get(m - 1).get(n - 1));
    }

    /**
     * Performs lazy computations of this algorithm and stores cached data in {@code treeDistances}
     * and {@code editOperationList}.
     */
    private void lazyRunAlgorithm()
    {
        if (!algorithmExecuted) {
            TreeOrdering ordering1 = new TreeOrdering(tree1, root1);
            TreeOrdering ordering2 = new TreeOrdering(tree2, root2);

            for (int keyroot1 : ordering1.keyroots) {
                for (Integer keyroot2 : ordering2.keyroots) {
                    treeDistance(keyroot1, keyroot2, ordering1, ordering2);
                }
            }

            algorithmExecuted = true;
        }
    }

    /**
     * Computes edit distance and list of edit operations for vertex $v1$ from {@code tree1} which
     * has tree ordering index equal to $i$ and vertex $v2$ from {@code tree2} which has tree
     * ordering index equal to $j$. Both $v1$ and $v2$ must be keyroots in the corresponding trees.
     *
     * @param i ordering index of a keyroot in {@code tree1}
     * @param j ordering index of a keywoot in {@code tree2}
     * @param ordering1 ordering of {@code tree1}
     * @param ordering2 ordering of {@code tree2}
     */
    private void treeDistance(int i, int j, TreeOrdering ordering1, TreeOrdering ordering2)
    {
        int li = ordering1.indexToLValueList.get(i);
        int lj = ordering2.indexToLValueList.get(j);

        int m = i - li + 2;
        int n = j - lj + 2;
        double[][] forestdist = new double[m][n];
        List<List<CacheEntry>> cachedOperations = new ArrayList<>(m);
        for (int k = 0; k < m; ++k) {
            cachedOperations.add(new ArrayList<>(Collections.nCopies(n, null)));
        }

        int iOffset = li - 1;
        int jOffset = lj - 1;

        for (int i1 = li; i1 <= i; ++i1) {
            V i1Vertex = ordering1.indexToVertexList.get(i1);
            int iIndex = i1 - iOffset;
            forestdist[iIndex][0] = forestdist[iIndex - 1][0] + removeCost.applyAsDouble(i1Vertex);
            CacheEntry entry = new CacheEntry(
                iIndex - 1, 0, new EditOperation<>(OperationType.REMOVE, i1Vertex, null));
            cachedOperations.get(iIndex).set(0, entry);
        }
        for (int j1 = lj; j1 <= j; ++j1) {
            V j1Vertex = ordering2.indexToVertexList.get(j1);
            int jIndex = j1 - jOffset;
            forestdist[0][jIndex] = forestdist[0][jIndex - 1] + removeCost.applyAsDouble(j1Vertex);
            CacheEntry entry = new CacheEntry(
                0, jIndex - 1, new EditOperation<>(OperationType.INSERT, j1Vertex, null));
            cachedOperations.get(0).set(jIndex, entry);
        }

        for (int i1 = li; i1 <= i; ++i1) {
            V i1Vertex = ordering1.indexToVertexList.get(i1);
            int li1 = ordering1.indexToLValueList.get(i1);

            for (int j1 = lj; j1 <= j; ++j1) {
                V j1Vertex = ordering2.indexToVertexList.get(j1);
                int lj1 = ordering2.indexToLValueList.get(j1);

                int iIndex = i1 - iOffset;
                int jIndex = j1 - jOffset;
                if (li1 == li && lj1 == lj) {
                    double dist1 =
                        forestdist[iIndex - 1][jIndex] + removeCost.applyAsDouble(i1Vertex);
                    double dist2 =
                        forestdist[iIndex][jIndex - 1] + insertCost.applyAsDouble(j1Vertex);
                    double dist3 = forestdist[iIndex - 1][jIndex - 1]
                        + changeCost.applyAsDouble(i1Vertex, j1Vertex);
                    double result = Math.min(dist1, Math.min(dist2, dist3));

                    CacheEntry entry;
                    if (result == dist1) { // remove operation
                        entry = new CacheEntry(
                            iIndex - 1, jIndex,
                            new EditOperation<>(OperationType.REMOVE, i1Vertex, null));
                    } else if (result == dist2) { // insert operation
                        entry = new CacheEntry(
                            iIndex, jIndex - 1,
                            new EditOperation<>(OperationType.INSERT, j1Vertex, null));
                    } else { // result == dist3 => change operation
                        entry = new CacheEntry(
                            iIndex - 1, jIndex - 1,
                            new EditOperation<>(OperationType.CHANGE, i1Vertex, j1Vertex));
                    }
                    cachedOperations.get(iIndex).set(jIndex, entry);

                    forestdist[iIndex][jIndex] = result;
                    treeDistances[i1 - 1][j1 - 1] = result;
                    editOperationLists
                        .get(i1 - 1)
                        .set(j1 - 1, restoreOperationsList(cachedOperations, iIndex, jIndex));
                } else {
                    int i2 = li1 - 1 - iOffset;
                    int j2 = lj1 - 1 - jOffset;
                    double dist1 =
                        forestdist[iIndex - 1][jIndex] + removeCost.applyAsDouble(i1Vertex);
                    double dist2 =
                        forestdist[iIndex][jIndex - 1] + insertCost.applyAsDouble(j1Vertex);
                    double dist3 = forestdist[i2][j2] + treeDistances[i1 - 1][j1 - 1];
                    double result = Math.min(dist1, Math.min(dist2, dist3));
                    forestdist[iIndex][jIndex] = result;

                    CacheEntry entry;
                    if (result == dist1) {
                        entry = new CacheEntry(
                            iIndex - 1, jIndex,
                            new EditOperation<>(OperationType.REMOVE, i1Vertex, null));
                    } else if (result == dist2) {
                        entry = new CacheEntry(
                            iIndex, jIndex - 1,
                            new EditOperation<>(OperationType.INSERT, j1Vertex, null));
                    } else {
                        entry = new CacheEntry(i2, j2, null);
                        entry.treeDistanceI = i1 - 1;
                        entry.treeDistanceJ = j1 - 1;
                    }
                    cachedOperations.get(iIndex).set(jIndex, entry);
                }
            }
        }
    }

    /**
     * Restores list of edit operations which have been cached in {@code cachedOperations} during
     * the edit distance computation. Starting from a cache entry at index $(i,j)$.
     *
     * @param cachedOperations 2-dimensional list with cached operations
     * @param i starting operation index
     * @param j starting operation index
     * @return list of edit operations
     */
    private List<EditOperation<V>> restoreOperationsList(
        List<List<CacheEntry>> cachedOperations, int i, int j)
    {
        List<EditOperation<V>> result = new ArrayList<>();

        CacheEntry it = cachedOperations.get(i).get(j);
        while (it != null) {
            if (it.editOperation == null) {
                result.addAll(editOperationLists.get(it.treeDistanceI).get(it.treeDistanceJ));
            } else {
                result.add(it.editOperation);
            }
            it = cachedOperations.get(it.cachePreviousPosI).get(it.cachePreviousPosJ);
        }

        return result;
    }

    /**
     * Auxiliary class which for computes keyroot vertices, tree ordering and $l()$ function for a
     * particular tree.
     *
     * <p>
     * A keyroot of a tree is a vertex which has a left sibling. Ordering of a tree assings an
     * integer index to every its vertex. Indices are assigned using post-order traversal. $l()$
     * function for every vertex in a tree returns ordering index of its leftmost child. For leaf
     * vertex the function returns its own ordering index.
     */
    private class TreeOrdering
    {
        /**
         * Underlying tree of this ordering.
         */
        final Graph<V, E> tree;
        /**
         * Root vertex of {@code tree}.
         */
        final V treeRoot;

        /**
         * List of keyroots of {@code tree}.
         */
        List<Integer> keyroots;

        /**
         * List which at very position $i$ stores a vertex from {@code tree} which has ordering
         * index equal to $i$.
         */
        List<V> indexToVertexList;
        /**
         * List which at every position $i$ stores value of $l()$ function for a vertex from
         * {@code tree} whihc has ordering index equal to $i$.
         */
        List<Integer> indexToLValueList;
        /**
         * Ordering index to be assigned to the next traversed vertex.
         */
        int currentIndex;

        /**
         * Constructs an instance of the tree ordering for the given {@code graph} and
         * {@code treeRoot}.
         *
         * @param tree a tree
         * @param treeRoot root vertex of {@code tree}
         */
        public TreeOrdering(Graph<V, E> tree, V treeRoot)
        {
            this.tree = tree;
            this.treeRoot = treeRoot;

            int numberOfVertices = tree.vertexSet().size();
            keyroots = new ArrayList<>();
            indexToVertexList = new ArrayList<>(Collections.nCopies(numberOfVertices + 1, null));
            indexToLValueList = new ArrayList<>(Collections.nCopies(numberOfVertices + 1, null));
            currentIndex = 1;

            computeKeyrootsAndMapping(treeRoot);
        }

        /**
         * Runs post-order DFS on {@code tree} starting at {@code treeRoot}. Assigns consecutive
         * integer index to every traversed vertex and computes keyroots for {@code tree}.
         *
         * @param treeRoot root vertex of {@code tree}
         */
        private void computeKeyrootsAndMapping(V treeRoot)
        {
            List<StackEntry> stack = new ArrayList<>();
            stack.add(new StackEntry(treeRoot, true));

            while (!stack.isEmpty()) {
                StackEntry entry = stack.get(stack.size() - 1);
                if (entry.state == 0) {
                    if (stack.size() > 1) {
                        entry.vParent = stack.get(stack.size() - 2).v;
                    }
                    entry.vChildIterator = Graphs.successorListOf(tree, entry.v).iterator();
                    entry.state = 1;
                } else if (entry.state == 1) {
                    if (entry.vChildIterator.hasNext()) {
                        entry.vChild = entry.vChildIterator.next();
                        if (entry.vParent == null || !entry.vChild.equals(entry.vParent)) {
                            stack.add(new StackEntry(entry.vChild, entry.isKeyrootArg));
                            entry.state = 2;
                        }
                    } else {
                        entry.state = 3;
                    }
                } else if (entry.state == 2) {
                    entry.isKeyrootArg = true;
                    if (entry.lValue == -1) {
                        entry.lValue = entry.lVChild;
                    }
                    entry.state = 1;
                } else if (entry.state == 3) {
                    if (entry.lValue == -1) {
                        entry.lValue = currentIndex;
                    }
                    if (entry.isKeyroot) {
                        keyroots.add(currentIndex);
                    }
                    indexToVertexList.set(currentIndex, entry.v);
                    indexToLValueList.set(currentIndex, entry.lValue);
                    ++currentIndex;
                    if (stack.size() > 1) {
                        stack.get(stack.size() - 2).lVChild = entry.lValue;
                    }
                    stack.remove(stack.size() - 1);
                }
            }
        }

        /**
         * Auxiliary class which stores all needed variables to emulate recursive execution of DFS
         * algorithm in {@code computeKeyrootsAndMapping()} method.
         */
        private class StackEntry
        {
            /**
             * A vertex from {@code tree}.
             */
            V v;
            /**
             * Indites if {@code v} is a keyroot wrt {@code tree}.
             */
            boolean isKeyroot;

            /**
             * Parent vertex of {@code v} in {@code tree} or $null$ if {@code v} is root of
             * {@code tree}.
             */
            V vParent;
            /**
             * Indicates if the next vertex returned by {@code vChildIterator} will be a keyroot.
             */
            boolean isKeyrootArg;
            /**
             * Value of the $l()$ function for {@code v};
             */
            int lValue;
            /**
             * Iterates over children of $v$ in {@code tree}.
             */
            Iterator<V> vChildIterator;
            /**
             * Current child vertex of {@code v}.
             */
            V vChild;
            /**
             * Value of $l()$ function for {@code vChild}.
             */
            int lVChild;

            /**
             * Auxiliary field which helps to identify which part of the recursive procedure should
             * be executed next for this stack entry.
             */
            int state;

            /**
             * Constructs an instance of the stack entry for the given {@code v} and
             * {@code isKeyroot}
             *
             * @param v a vertex from {@code tree}
             * @param isKeyroot true iff {@code v} is a keyroot
             */
            public StackEntry(V v, boolean isKeyroot)
            {
                this.v = v;
                this.isKeyroot = isKeyroot;
                this.lValue = -1;
            }
        }
    }

    /**
     * Represents elementary action which changes the structure of a tree.
     *
     * @param <V> tree vertex type
     */
    public static class EditOperation<V>
    {
        /**
         * Type of this operation.
         */
        private final OperationType type;
        /**
         * Vertex of a tree which is the first operand of this operations.
         */
        private final V firstOperand;
        /**
         * Vertex of a tree which is a second operand of this operation. For
         * {@code OperationsType.INSERT} and {@code OperationsType.REMOVE} this field is null.
         */
        private final V secondOperand;

        /**
         * Returns type of this operation.
         *
         * @return oeration type
         */
        public OperationType getType()
        {
            return type;
        }

        /**
         * Returns first operand of this operation
         *
         * @return first operand
         */
        public V getFirstOperand()
        {
            return firstOperand;
        }

        /**
         * Returns second operand of this operation.
         *
         * @return second operand
         */
        public V getSecondOperand()
        {
            return secondOperand;
        }

        /**
         * Constructs an instance of edit operation for the given {@code type}, {@code firstOperand}
         * and {@code secondOperand}.
         *
         * @param type type of the operation
         * @param firstOperand first operand of the operation
         * @param secondOperand second operand of the operation
         */
        public EditOperation(OperationType type, V firstOperand, V secondOperand)
        {
            this.type = type;
            this.firstOperand = firstOperand;
            this.secondOperand = secondOperand;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            EditOperation<?> editOperation = (EditOperation<?>) o;

            if (type != editOperation.type)
                return false;
            if (!firstOperand.equals(editOperation.firstOperand))
                return false;
            return secondOperand != null ? secondOperand.equals(editOperation.secondOperand)
                : editOperation.secondOperand == null;
        }

        @Override
        public int hashCode()
        {
            int result = type.hashCode();
            result = 31 * result + firstOperand.hashCode();
            result = 31 * result + (secondOperand != null ? secondOperand.hashCode() : 0);
            return result;
        }

        @Override
        public String toString()
        {
            if (type.equals(OperationType.INSERT) || type.equals(OperationType.REMOVE)) {
                return type + " " + firstOperand;
            }
            return type + " " + firstOperand + " -> " + secondOperand;
        }
    }

    /**
     * Type of an edit operation.
     */
    public enum OperationType
    {
        /**
         * Indicates that an edit operation is inserting a vertex into a tree.
         */
        INSERT,
        /**
         * Indicates that an edit operation is removing a vertex into a tree.
         */
        REMOVE,
        /**
         * Indicates that an edit operation is changing a vertex in one tree to a vertex in another
         * three.
         */
        CHANGE
    }

    /**
     * Auxiliary class which is used in {@code treeDistance()} function to store intermediate edit
     * operations during dynamic programming computation.
     */
    private class CacheEntry
    {
        /**
         * Outer index of the previous entry which is part of the computed optimal solution.
         */
        int cachePreviousPosI;
        /**
         * Inner index of the previous entry which is part of the computed optimal solution.
         */
        int cachePreviousPosJ;
        /**
         * Edit operation stored in this entry. Is this field is $null$ this indicates that
         * operations from $editOperationLists[treeDistanceI][treeDistanceJ]$.
         */
        EditOperation<V> editOperation;
        /**
         * Outer index of an entry in $editOperationLists$ which should be taken in case
         * {@code editOperation} is $null$.
         */
        int treeDistanceI;
        /**
         * Inner index of an entry in $editOperationLists$ which should be taken in case
         * {@code editOperation} is $null$.
         */
        int treeDistanceJ;

        /**
         * Constructs an instance of entry for the given {@code cachePreviousPosI}
         * {@code cachePreviousPosJ} and {@code editOperation}.
         *
         * @param cachePreviousPosI outer index of the previous cache entry
         * @param cachePreviousPosJ inner index of the previous cache entry
         * @param editOperation edit operation of this entry
         */
        public CacheEntry(
            int cachePreviousPosI, int cachePreviousPosJ, EditOperation<V> editOperation)
        {
            this.cachePreviousPosI = cachePreviousPosI;
            this.cachePreviousPosJ = cachePreviousPosJ;
            this.editOperation = editOperation;
        }
    }
}
