package org.jgrapht.alg.blossom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StateTest {

    @Test
    public void testAddTreeEdge() {
        Tree tree1 = new Tree(new Node());
        Tree tree2 = new Tree(new Node());
        TreeEdge treeEdge = State.addTreeEdge(tree1, tree2);
        int currentDir = tree2.currentDirection;
        assertEquals(tree2, treeEdge.head[currentDir]);
        assertEquals(tree1, treeEdge.head[1 - currentDir]);
    }

}
