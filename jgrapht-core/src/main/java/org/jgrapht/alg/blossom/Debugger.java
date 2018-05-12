package org.jgrapht.alg.blossom;

import java.util.HashSet;
import java.util.Set;

public class Debugger {
    public static Set<Edge> edgesOf(Node node) {
        Set<Edge> edges = new HashSet<>();
        node.forAllEdges((edge, dir) -> edges.add(edge));
        return edges;
    }

    public static Set<TreeEdge> treeEdgesOf(Tree tree) {
        Set<TreeEdge> result = new HashSet<>();
        for (Tree.TreeEdgeIterator iterator = tree.treeEdgeIterator(); iterator.hasNext(); ) {
            result.add(iterator.next());
        }
        return result;
    }

    public static void setCurrentEdges(Tree tree) {
        tree.forEachTreeEdge((treeEdge, dir) -> {
            Tree opposite = treeEdge.head[dir];
            opposite.currentEdge = treeEdge;
            opposite.currentDirection = dir;
        });
    }

    public static TreeEdge getTreeEdge(Tree from, Tree to) {
        for (Tree.TreeEdgeIterator iterator = from.treeEdgeIterator(); iterator.hasNext(); ) {
            TreeEdge treeEdge = iterator.next();
            if (treeEdge.head[iterator.getCurrentDirection()] == to) {
                return treeEdge;
            }
        }
        return null;
    }

    public static Tree getOppositeTree(TreeEdge treeEdge, Tree tree) {
        return treeEdge.head[0] == tree ? treeEdge.head[1] : treeEdge.head[0];
    }

    public static Set<Node> childrenOf(Node node) {
        Set<Node> children = new HashSet<>();
        for (Node child = node.firstTreeChild; child != null; child = child.treeSiblingNext) {
            children.add(child);
        }
        return children;
    }

    public static <V, E> Set<Node> treeRoots(State<V, E> state) {
        Set<Node> treeRoots = new HashSet<>();
        for (Node root = state.nodes[state.nodeNum].treeSiblingNext; root != null; root = root.treeSiblingNext) {
            treeRoots.add(root);
        }
        return treeRoots;
    }

    public static int dirToOpposite(Edge edge, Node node) {
        return edge.head[0] == node ? 1 : 0;
    }

    public static int dirToOpposite(TreeEdge treeEdge, Tree tree) {
        return treeEdge.head[0] == tree ? 1 : 0;
    }

    public static Node getOpposite(Edge edge, Node node) {
        return edge.head[0] == node ? edge.head[1] : edge.head[0];
    }
}
