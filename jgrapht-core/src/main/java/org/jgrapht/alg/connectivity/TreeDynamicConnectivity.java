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
package org.jgrapht.alg.connectivity;

import org.jgrapht.util.AVLTree;

import java.util.*;
import java.util.stream.Collectors;

import static org.jgrapht.util.AVLTree.TreeNode;

public class TreeDynamicConnectivity<T> {

    private Map<TreeNode<T>, AVLTree<T>> minToTreeMap;
    private Map<T, Node> vertexMap;
    private Map<AVLTree<T>, Node> treeToRootMap;

    private class Node {
        T value;
        Set<Node> neighbors;
        Node parent;
        TreeNode<T> firstOccurrence;
        TreeNode<T> lastOccurrence;

        public Node(T value, TreeNode<T> firstOccurrence, TreeNode<T> lastOccurrence) {
            this.value = value;
            this.neighbors = new HashSet<>();
            this.firstOccurrence = firstOccurrence;
            this.lastOccurrence = lastOccurrence;
        }

        @Override
        public String toString() {
            return String.format("{%s} -> %s%s", value, neighbors.stream().map(a -> a.value).collect(Collectors.toList()).toString(), parent == null ? " (root)" : "");
        }
    }

    public TreeDynamicConnectivity() {
        minToTreeMap = new HashMap<>();
        vertexMap = new HashMap<>();
        treeToRootMap = new HashMap<>();
    }

    public void add(T element) {
        AVLTree<T> newTree = new AVLTree<>();
        TreeNode<T> firstNode = newTree.addMin(element);

        Node elementNode = new Node(element, firstNode, firstNode);

        vertexMap.put(element, elementNode);
        minToTreeMap.put(firstNode, newTree);
        treeToRootMap.put(newTree, elementNode);
    }

    public void remove(T element) {
        Node node = vertexMap.get(element);
        List<T> neighbors = node.neighbors.stream().map(a -> a.firstOccurrence.getValue()).collect(Collectors.toList());
        for (T neighbor : neighbors) {
            cut(element, neighbor);
        }

        treeToRootMap.remove(getTree(element));
        minToTreeMap.remove(node.firstOccurrence);
        vertexMap.remove(element);
    }

    public void link(T first, T second) {
        AVLTree<T> parentTree = getTree(first);
        AVLTree<T> childTree = getTree(second);
//        System.out.println("Parent = \n" + parentTree + "\n");
//        System.out.println("Child = \n" + childTree + "\n");

        Node firstNode = vertexMap.get(first);
        Node secondNode = vertexMap.get(second);

        makeRoot(childTree, second);
//        diagnostic();
//        System.out.println("After make root:\n" + childTree);

        minToTreeMap.remove(childTree.getMin());
        treeToRootMap.remove(childTree);

        AVLTree<T> right = parentTree.splitAfter(firstNode.lastOccurrence);
        firstNode.lastOccurrence = right.addMin(first);

        parentTree.mergeAfter(childTree);
        parentTree.mergeAfter(right);

        secondNode.parent = firstNode;

        firstNode.neighbors.add(secondNode);
        secondNode.neighbors.add(firstNode);
    }

    private void updateTree(AVLTree<T> tree, Node prevRoot, Node valueNode){
        for (Node prev = valueNode, cur = valueNode.parent; cur != null; ) {
            Node next = cur.parent;
            cur.parent = prev;
            prev = cur;
            cur = next;
        }
        valueNode.parent = null;
        Node nodeBeforeRoot = prevRoot.parent;

        prevRoot.firstOccurrence = tree.successor(nodeBeforeRoot.lastOccurrence);
        if (prevRoot.firstOccurrence == tree.getMax()) {
            prevRoot.firstOccurrence = tree.getMin();
        }
        prevRoot.lastOccurrence = tree.predecessor(nodeBeforeRoot.firstOccurrence);

        for(Node cur = prevRoot.parent; cur != valueNode; cur = cur.parent){
            cur.firstOccurrence = tree.successor(cur.parent.lastOccurrence);
            cur.lastOccurrence = tree.predecessor(cur.parent.firstOccurrence);
        }
    }

    private void makeRoot(AVLTree<T> tree, T value) {
        Node valueNode = vertexMap.get(value);

        if (valueNode.parent == null) {
            // value is already a root
            return;
        }
        Node prevRoot = treeToRootMap.put(tree, valueNode);
        assert prevRoot != null;

        updateTree(tree, prevRoot, valueNode);

        // updating minToTreeMap
        minToTreeMap.remove(tree.getMin());
        minToTreeMap.put(valueNode.firstOccurrence, tree);

        tree.removeMax();
        AVLTree<T> right = tree.splitBefore(valueNode.firstOccurrence);
        valueNode.lastOccurrence = tree.addMax(value);

        tree.mergeBefore(right);
    }

    public void cut(T first, T second) {
        Node firstNode = vertexMap.get(first);
        Node secondNode = vertexMap.get(second);

        if (firstNode.parent == secondNode) {
            cut(second, first);
            return;
        } else if (secondNode.parent != firstNode) {
            throw new IllegalArgumentException("Nodes are not adjacent");
        }


        AVLTree<T> tree = getTree(firstNode.firstOccurrence);

        AVLTree<T> center = tree.splitBefore(secondNode.firstOccurrence);
        AVLTree<T> right = center.splitAfter(secondNode.lastOccurrence);

        TreeNode<T> removed = right.removeMin();
        if(firstNode.lastOccurrence == removed){
            // second was the last child of the first
            // need to update firstNode.lastOccurrence
            firstNode.lastOccurrence = tree.getMax();
        }

        tree.mergeAfter(right);

        minToTreeMap.put(center.getMin(), center);

        treeToRootMap.put(center, secondNode);

        secondNode.parent = null;

        firstNode.neighbors.remove(secondNode);
        secondNode.neighbors.remove(firstNode);
    }

    void diagnostic() {
        for (AVLTree<T> tree : treeToRootMap.keySet()) {
            List<T> stack = new ArrayList<>();
            Map<T, TreeNode<T>> values = new HashMap<>();
            for (Iterator<TreeNode<T>> nodeIterator = tree.nodeIterator(); nodeIterator.hasNext(); ) {
                TreeNode<T> treeNode = nodeIterator.next();
                T value = treeNode.getValue();
                Node node = vertexMap.get(value);

                if (!values.containsKey(value)) {
                    assert treeNode == node.firstOccurrence;
                }

                values.put(value, treeNode);

                if (stack.size() > 1 && stack.get(stack.size() - 2).equals(value)) {
                    T removeValue =  stack.remove(stack.size() - 1);
                } else {
                    stack.add(value);
                }
            }

            for (Map.Entry<T, TreeNode<T>> entry : values.entrySet()) {
                Node node = vertexMap.get(entry.getKey());
                assert node.lastOccurrence == entry.getValue();
            }

            assert stack.size() == 1;
        }
    }

    public boolean connected(T first, T second) {
        return getMinNode(first) == getMinNode(second);
    }

    private TreeNode<T> getMinNode(T element) {
        return vertexMap.get(element).firstOccurrence.getTreeMin();
    }

    private AVLTree<T> getTree(T element) {
        return minToTreeMap.get(getMinNode(element));
    }

    private AVLTree<T> getTree(TreeNode<T> node) {
        return minToTreeMap.get(node.getRoot().getSubtreeMin());
    }


}
