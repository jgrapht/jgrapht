package org.jgrapht.alg.flow.min_cost;

import org.jgrapht.Graph;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jgrapht.alg.flow.min_cost.MinCostFlow.LabelType.PERMANENTLY_LABELED;

public class MinCostFlow<V, E> {
    private static final String NO_FEASIBLE_FLOW = "Specified flow network problem has no feasible solution";
    Node[] nodes;

    public MinCostFlow(Graph<V, E> graph, Map<V, Double> supplyMap, Map<E, Double> lowerCapacityMap, Map<E, Double> upperCapacityMap) {

    }

    private double pushDijkstra(Node start) {
        Node varNode;
        FibonacciHeapNode<Node> varFibNode;
        double distance;
        FibonacciHeap<Node> heap = new FibonacciHeap<>();
        List<Node> permanentlyLabeled = new LinkedList<>();
        insertIntoHeap(heap, start, 0);
        while (!heap.isEmpty()) {
            varFibNode = heap.removeMin();
            varNode = varFibNode.getData();
            distance = varFibNode.getKey();
            if (varNode.excess < 0) {
                double delta = augmentPath(start, varNode);
                for (Node node : permanentlyLabeled) {
                    node.potential += distance;
                }
                return delta;
            }
            varNode.labelType = PERMANENTLY_LABELED;
            permanentlyLabeled.add(varNode); // varNode becomes permanently labeled
            varNode.potential -= distance;

        }
        throw new IllegalArgumentException(NO_FEASIBLE_FLOW);
    }

    private double augmentPath(Node start, Node end) {
        double delta = Math.min(start.excess, -end.excess);
        for (Arc arc = end.parentArc; arc != null; arc = arc.revArc.head.parentArc) {
            delta = Math.min(delta, arc.residualCapacity);
        }
        end.excess += delta;
        for (Arc arc = end.parentArc; arc != null; arc = arc.head.parentArc) {
            arc.decreaseResidualCapacity(delta);
            arc = arc.revArc;
            arc.increaseResidualCapacity(delta);
        }
        start.excess -= delta;
        return delta;
    }

    private void insertIntoHeap(FibonacciHeap<Node> heap, Node node, double value) {
        node.fibNode = new FibonacciHeapNode<>(node);
        heap.insert(node.fibNode, value);
    }

    enum LabelType {
        PERMANENTLY_LABELED, TEMPORARILY_LABELED,
    }

    private class Node {
        FibonacciHeapNode<Node> fibNode;
        Arc parentArc;
        LabelType labelType;
        double excess;
        double potential;
    }

    private class Arc {
        Node head;
        Arc prev;
        Arc next;
        Arc revArc;
        double cost;
        double residualCapacity;

        public void decreaseResidualCapacity(double delta) {

        }

        public void increaseResidualCapacity(double delta) {

        }
    }
}
