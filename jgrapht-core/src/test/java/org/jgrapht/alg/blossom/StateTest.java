package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.jgrapht.alg.blossom.Initializer.InitializationType.NONE;
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

    @Test
    public void testMoveEdge() {
        Graph<Integer, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);
        DefaultWeightedEdge e12 = Graphs.addEdgeWithVertices(graph, 1, 2, 0);
        DefaultWeightedEdge e13 = Graphs.addEdgeWithVertices(graph, 1, 3, 0);
        DefaultWeightedEdge e23 = Graphs.addEdgeWithVertices(graph, 2, 3, 0);

        Initializer<Integer, DefaultWeightedEdge> initializer = new Initializer<>(graph);
        State<Integer, DefaultWeightedEdge> state = initializer.initialize(NONE);

        Node node1 = state.vertexMap.get(1);
        Node node2 = state.vertexMap.get(2);
        Node node3 = state.vertexMap.get(3);

        Edge edge12 = state.edgeMap.get(e12);
        Edge edge13 = state.edgeMap.get(e13);
        Edge edge23 = state.edgeMap.get(e23);

        state.moveEdge(node2, node3, edge12);
        assertEquals(node3, edge12.getOpposite(node1));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge13)), Debugger.edgesOf(node1));
        assertEquals(new HashSet<>(Collections.singletonList(edge23)), Debugger.edgesOf(node2));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge13, edge23)), Debugger.edgesOf(node3));

        state.moveEdge(node2, node1, edge23);
        assertEquals(node1, edge13.getOpposite(node3));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge13, edge23)), Debugger.edgesOf(node1));
        assertEquals(new HashSet<>(), Debugger.edgesOf(node2));
        assertEquals(new HashSet<>(Arrays.asList(edge12, edge13, edge23)), Debugger.edgesOf(node3));
    }

}
