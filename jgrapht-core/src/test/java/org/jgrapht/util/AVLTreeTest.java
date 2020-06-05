/*
 * (C) Copyright 2020-2020, by Timofey Chudakov and Contributors.
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
package org.jgrapht.util;

import org.junit.*;

import java.util.*;
import java.util.stream.*;

import static org.jgrapht.util.AVLTree.TreeNode;
import static org.junit.Assert.*;

/**
 * Tests for {@link AVLTree}
 *
 * @author Timofey Chudakov
 */
public class AVLTreeTest
{
    private static final Random rng = new Random(17L);

    @Test
    public void testEmpty()
    {
        AVLTree<Integer> tree = new AVLTree<>();

        assertEquals(0, tree.getSize());
        assertNull(tree.getRoot());
    }

    @Test
    public void testOneNode()
    {
        AVLTree<Integer> tree = new AVLTree<>();
        TreeNode<Integer> node = tree.addMax(1);

        assertEquals(1, (int) node.getValue());
        assertEquals(node, tree.getRoot());
        assertEquals(node, node.getRoot());

        assertEquals(node, node.getTreeMax());
        assertEquals(node, node.getTreeMin());

        assertEquals(node, tree.getMin());
        assertEquals(node, tree.getMax());

        assertEquals(1, node.getHeight());

        assertEquals(1, tree.getSize());
    }

    @Test
    public void testAddMax()
    {
        final int testNum = 50;
        for (int nodeNum = 0; nodeNum < testNum; nodeNum++) {
            AVLTree<Integer> tree = new AVLTree<>();
            for (int i = 0; i < nodeNum; i++) {
                tree.addMax(i);
                diagnostic(tree);
            }

            assertEquals(nodeNum, tree.getSize());
        }
    }

    @Test
    public void testAddMin()
    {
        final int testNum = 50;
        for (int nodeNum = 0; nodeNum < testNum; nodeNum++) {
            AVLTree<Integer> tree = new AVLTree<>();
            for (int i = 0; i < nodeNum; i++) {
                tree.addMin(i);
                diagnostic(tree);
            }

            assertEquals(nodeNum, tree.getSize());
        }
    }

    @Test
    public void testMergeAfter()
    {
        for (int leftSize = 0; leftSize < 50; leftSize++) {
            for (int rightSize = 0; rightSize < 50; rightSize++) {
                AVLTree<Integer> left = new AVLTree<>();
                AVLTree<Integer> right = new AVLTree<>();

                fillNodes(left, 0, leftSize);
                fillNodes(right, leftSize, leftSize + rightSize);

                left.mergeAfter(right);

                assertEquals(leftSize + rightSize, left.getSize());
                assertEquals(0, right.getSize());

                testTreeValueRange(left, 0, leftSize + rightSize);
                diagnostic(left);
            }
        }
    }

    @Test
    public void testMergeBefore()
    {
        for (int leftSize = 0; leftSize < 50; leftSize++) {
            for (int rightSize = 0; rightSize < 50; rightSize++) {
                AVLTree<Integer> left = new AVLTree<>();
                AVLTree<Integer> right = new AVLTree<>();

                fillNodes(left, 0, leftSize);
                fillNodes(right, leftSize, leftSize + rightSize);

                right.mergeBefore(left);

                testTreeValueRange(right, 0, leftSize + rightSize);
                diagnostic(right);
            }
        }
    }

    @Test
    public void testSplitAfter()
    {
        for (int treeSize = 1; treeSize < 50; treeSize++) {
            for (int split = 0; split < treeSize; split++) {
                AVLTree<Integer> tree = new AVLTree<>();
                List<TreeNode<Integer>> nodes = fillNodes(tree, 0, treeSize);

                TreeNode<Integer> splitNode = nodes.get(split);

                AVLTree<Integer> right = tree.splitAfter(splitNode);

                testTreeValueRange(tree, 0, split + 1);
                testTreeValueRange(right, split + 1, treeSize);

                diagnostic(tree);
                diagnostic(right);
            }
        }
    }

    @Test
    public void testSplitBefore()
    {
        for (int treeSize = 1; treeSize < 50; treeSize++) {
            for (int split = 0; split < treeSize; split++) {
                AVLTree<Integer> tree = new AVLTree<>();
                List<TreeNode<Integer>> nodes = fillNodes(tree, 0, treeSize);

                TreeNode<Integer> splitNode = nodes.get(split);

                AVLTree<Integer> right = tree.splitBefore(splitNode);

                testTreeValueRange(tree, 0, split);
                testTreeValueRange(right, split, treeSize);

                diagnostic(tree);
                diagnostic(right);
            }
        }
    }

    @Test
    public void testIterator()
    {

        for (int treeSize = 1; treeSize < 50; treeSize++) {
            AVLTree<Integer> tree = new AVLTree<>();
            List<TreeNode<Integer>> nodes = fillNodes(tree, 0, treeSize);

            Iterator<TreeNode<Integer>> iterator = tree.nodeIterator();

            for (TreeNode<Integer> expected : nodes) {
                assertTrue(iterator.hasNext());
                TreeNode<Integer> actual = iterator.next();
                assertEquals(expected, actual);
            }
            assertFalse(iterator.hasNext());
        }
    }

    private void testTreeValueRange(AVLTree<Integer> tree, int from, int to)
    {
        assertEquals(to - from, tree.getSize());
        Iterator<TreeNode<Integer>> it = tree.nodeIterator();
        for (int i = from; i < to; i++) {
            assertTrue(it.hasNext());
            TreeNode<Integer> node = it.next();

            assertEquals(i, node.getValue().intValue());
        }
    }

    private List<TreeNode<Integer>> fillNodes(AVLTree<Integer> tree)
    {
        final int nodeNum = 100;
        return fillNodes(tree, 0, nodeNum);
    }

    private List<TreeNode<Integer>> fillNodes(AVLTree<Integer> tree, int from, int to)
    {
        Deque<TreeNode<Integer>> nodes = new ArrayDeque<>();
        int middle = (from + to) / 2;
        Deque<Integer> minValues =
            IntStream.range(from, middle).boxed().collect(Collectors.toCollection(ArrayDeque::new));
        Deque<Integer> maxValues =
            IntStream.range(middle, to).boxed().collect(Collectors.toCollection(ArrayDeque::new));
        for (int i = from; i < to; i++) {
            int rand = rng.nextInt(2);
            if ((rand == 0 && !minValues.isEmpty()) || maxValues.isEmpty()) {
                nodes.addFirst(tree.addMin(minValues.removeLast()));
            } else {
                nodes.addLast(tree.addMax(maxValues.removeFirst()));
            }
            diagnostic(tree);
        }
        return new ArrayList<>(nodes);
    }

    void diagnostic(AVLTree<Integer> tree)
    {
        TreeNode<Integer> root = tree.getRoot();
        if (root != null) {
            TreeNode<Integer> virtualRoot = root.getParent();
            assertEquals(virtualRoot.getLeft(), root);
            diagnostic(virtualRoot.left);
        }
    }

    DiagnosticInfo diagnostic(TreeNode<Integer> node)
    {
        if (node == null) {
            return new DiagnosticInfo(null, null, 0, 0);
        }
        DiagnosticInfo leftInfo = diagnostic(node.getLeft());
        DiagnosticInfo rightInfo = diagnostic(node.getRight());

        assertEquals(node.getHeight(), Math.max(leftInfo.height, rightInfo.height) + 1);
        assertEquals(node.getSubtreeSize(), leftInfo.size + rightInfo.size + 1);

        assertTrue(Math.abs(node.getLeftHeight() - node.getRightHeight()) < 2);

        if (node.getLeft() == null) {
            assertEquals(node.getSubtreeMin(), node);
        } else {
            assertEquals(node.getLeft().getParent(), node);

            assertEquals(node.getSubtreeMin(), leftInfo.subtreeMin);
            assertEquals(node.getPredecessor(), leftInfo.subtreeMax);
            assertEquals(leftInfo.subtreeMax.getSuccessor(), node);
        }

        if (node.getRight() == null) {
            assert node.getSubtreeMax() == node;
        } else {
            assertEquals(node.getRight().getParent(), node);
            assertEquals(node.getSubtreeMax(), rightInfo.subtreeMax);
            assertEquals(node.getSuccessor(), rightInfo.subtreeMin);
            assertEquals(rightInfo.subtreeMin.predecessor, node);
        }

        return new DiagnosticInfo(
            node.getSubtreeMin(), node.getSubtreeMax(), node.getHeight(), node.getSubtreeSize());
    }

    private static class DiagnosticInfo
    {
        TreeNode<Integer> subtreeMin;
        TreeNode<Integer> subtreeMax;
        int height;
        int size;

        public DiagnosticInfo(
            TreeNode<Integer> subtreeMin, TreeNode<Integer> subtreeMax, int height, int size)
        {
            this.subtreeMin = subtreeMin;
            this.subtreeMax = subtreeMax;
            this.height = height;
            this.size = size;
        }
    }

}
