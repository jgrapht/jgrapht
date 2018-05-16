package org.jgrapht.alg.blossom;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

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

        int dir12 = edge12.getDirTo(node1);
        int dir31 = edge13.getDirTo(node1);
        int dir23 = edge23.getDirTo(node2);

        state.moveEdge(node2, node3, edge12);
        assertEquals(node3, edge12.getOpposite(node1));


        state.moveEdge(node3, node2, edge13);
        assertEquals(node2, edge13.getOpposite(node3));
    }

}
