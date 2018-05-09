package org.jgrapht.alg.blossom;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

public class NodeTest {

    @org.junit.Test
    public void testAddChild1() {
        Node parent = new Node();
        Node child = new Node();
        parent.addChild(child);
        assertSame(parent.firstTreeChild, child);
        assertSame(child.parent, parent);
        assertNull(child.treeSiblingNext);
        assertSame(child.treeSiblingPrev, child);
    }

    @Test
    public void testAddChild2(){
        Node parent = new Node();
        Node firstChild = new Node();
        Node secondChild = new Node();
        parent.addChild(firstChild);
        parent.addChild(secondChild);

        assertSame(parent.firstTreeChild, secondChild);
        assertSame(firstChild.parent, parent);
        assertSame(secondChild.parent, parent);
        assertNull(firstChild.treeSiblingNext);
        assertSame(firstChild.treeSiblingPrev, secondChild);
        assertSame(secondChild.treeSiblingNext, firstChild);
        assertSame(secondChild.treeSiblingPrev, firstChild);
    }

    @Test
    public void testEdgeAddition() {
        Node from = new Node();
        Node to = new Node();
        Edge nodeEdge = new Edge(from, to, 0);

        from.addEdge(nodeEdge, 0);
        to.addEdge(nodeEdge, 1);

        assertSame(from.first[0], nodeEdge);
        assertSame(to.first[1], nodeEdge);

        assertNull(from.first[1]);
        assertNull(to.first[0]);

        assertSame(nodeEdge.head[0], to);
        assertSame(nodeEdge.head[1], from);

        from.forAllEdges((edge, dir) -> {
            assertSame(edge.head[dir], to);
        });
        to.forAllEdges((edge, dir) -> {
            assertSame(edge.head[dir], from);
        });
    }

    @Test
    public void testAdjacentEdgeIterator1(){
        Node node1 = new Node();
        Node node2 = new Node();
        Node node3 = new Node();
        Edge edge1 = State.addEdge(node1, node2, 0);
        Edge edge2 = State.addEdge(node1, node3, 0);
        assertEquals(new HashSet<>(Arrays.asList(edge1, edge2)), State.edgesOf(node1));
    }

}
