package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrimalUpdaterTest {
    @org.junit.Test
    public void testGrow1() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        DefaultEdge growEdge = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultEdge matchEdge = Graphs.addEdgeWithVertices(graph, 2, 3, 0);

        BlossomInitializer<Integer, DefaultEdge> initializer = new BlossomInitializer<>(graph);
        State<Integer, DefaultEdge> state = initializer.initialize(BlossomInitializer.InitializationType.NONE);

        PrimalUpdater<Integer, DefaultEdge> primalUpdater = new PrimalUpdater<>(state);
        primalUpdater.augment(state.edgeMap.get(matchEdge));
        primalUpdater.grow(state.edgeMap.get(growEdge));

        assertEquals(1, state.treeNum);
        Node root = state.vertexMap.get(1);
        Node node1 = state.vertexMap.get(2);
        Node node2 = state.vertexMap.get(3);
        Tree tree = root.tree;
        assertSame(tree, node1.getTree());
        assertSame(tree, node2.getTree());

        assertTrue(node1.isMinusNode());
        assertTrue(node2.isPlusNode());

        assertSame(node1.parent, root);
        assertSame(node2.parent, node1);
        assertSame(root.firstTreeChild, node1);
        assertSame(node1.firstTreeChild, node2);

    }

    @org.junit.Test
    public void testGrow2() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        DefaultEdge edge12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultEdge edge23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultEdge edge34 = Graphs.addEdgeWithVertices(graph, 3, 4, 0);
        DefaultEdge edge45 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultEdge edge36 = Graphs.addEdgeWithVertices(graph, 3, 6, 0);
        DefaultEdge edge67 = Graphs.addEdgeWithVertices(graph, 3, 7, 0);

        BlossomInitializer<Integer, DefaultEdge> initializer = new BlossomInitializer<>(graph);
        State<Integer, DefaultEdge> state = initializer.initialize(BlossomInitializer.InitializationType.NONE);
        PrimalUpdater<Integer, DefaultEdge> primalUpdater = new PrimalUpdater<>(state);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node node4 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Node node7 = state.vertexMap.get(7);
        Tree tree = node1.tree;

        primalUpdater.augment(state.edgeMap.get(edge45));
        primalUpdater.augment(state.edgeMap.get(edge23));
        primalUpdater.augment(state.edgeMap.get(edge67));
        primalUpdater.grow(state.edgeMap.get(edge12));

        assertSame(tree, node2.tree);
        assertSame(tree, node3.tree);
        assertSame(tree, node4.tree);
        assertSame(tree, node5.tree);
        assertSame(tree, node6.tree);
        assertSame(tree, node7.tree);

        assertTrue(node2.isMinusNode());
        assertTrue(node4.isMinusNode());
        assertTrue(node6.isMinusNode());
        assertTrue(node3.isPlusNode());
        assertTrue(node5.isPlusNode());
        assertTrue(node7.isPlusNode());

        assertSame(node1.firstTreeChild, node2);
        assertSame(node2.firstTreeChild, node3);
        assertTrue(node3.firstTreeChild == node4 || node3.firstTreeChild == node6);
        assertSame(node4.firstTreeChild, node5);
        assertSame(node6.firstTreeChild, node7);

        assertSame(node2.parent, node1);
        assertSame(node3.parent, node2);
        assertSame(node4.parent, node3);
        assertSame(node5.parent, node4);
        assertSame(node6.parent, node3);
        assertSame(node7.parent, node6);
    }

    @Test
    public void testGrow3() {
        Graph<Integer, DefaultEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultEdge.class);
        DefaultEdge edge12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultEdge edge23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);
        DefaultEdge edge25 = Graphs.addEdgeWithVertices(graph, 2, 5, 0);
        DefaultEdge edge26 = Graphs.addEdgeWithVertices(graph, 2, 6, 0);
        DefaultEdge edge35 = Graphs.addEdgeWithVertices(graph, 3, 5, 0);
        DefaultEdge edge56 = Graphs.addEdgeWithVertices(graph, 3, 6, 1); // if weight 0 -> will be augmented
        DefaultEdge edge36 = Graphs.addEdgeWithVertices(graph, 4, 5, 0);
        DefaultEdge edge45 = Graphs.addEdgeWithVertices(graph, 5, 6, 0);

        BlossomInitializer<Integer, DefaultEdge> initializer = new BlossomInitializer<>(graph);
        State<Integer, DefaultEdge> state = initializer.initialize(BlossomInitializer.InitializationType.NONE);
        PrimalUpdater<Integer, DefaultEdge> primalUpdater = new PrimalUpdater<>(state);
        primalUpdater.augment(state.edgeMap.get(edge23));
        primalUpdater.augment(state.edgeMap.get(edge56));
        primalUpdater.grow(state.edgeMap.get(edge12));


        Node root1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);
        Node root2 = state.vertexMap.get(4);
        Node node5 = state.vertexMap.get(5);
        Node node6 = state.vertexMap.get(6);
        Tree tree1 = root1.tree;
        Tree tree2 = root2.tree;
        Tree.TreeEdgeIterator iterator = tree1.treeEdgeIterator();
        assertTrue(iterator.hasNext());
        TreeEdge treeEdge = iterator.next();
        assertSame(treeEdge.head[iterator.getCurrentDirection()], tree2);


        assertEquals(1, treeEdge.getCurrentPlusMinusHeap(tree2.currentDirection).size());
        assertEquals(1, treeEdge.getCurrentMinusPlusHeap(tree2.currentDirection).size());
        assertEquals(1, treeEdge.plusPlusEdges.size());
    }
}
