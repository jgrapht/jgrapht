package org.jgrapht.alg.blossom;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

public class TreeEdgeTest {

    @Test
    public void testGetCurrentPlusMinusHeap() {
        Node root1 = new Node();
        Node root2 = new Node();
        Tree tree1 = new Tree(root1);
        Tree tree2 = new Tree(root2);
        TreeEdge treeEdge = Tree.addTreeEdge(tree1, tree2);

        assertNotSame(treeEdge.getCurrentMinusPlusHeap(0), treeEdge.getCurrentPlusMinusHeap(0));
        assertNotSame(treeEdge.getCurrentMinusPlusHeap(1), treeEdge.getCurrentPlusMinusHeap(1));
        assertSame(treeEdge.getCurrentPlusMinusHeap(0), treeEdge.getCurrentMinusPlusHeap(1));
        assertSame(treeEdge.getCurrentMinusPlusHeap(0), treeEdge.getCurrentPlusMinusHeap(1));
    }
}
