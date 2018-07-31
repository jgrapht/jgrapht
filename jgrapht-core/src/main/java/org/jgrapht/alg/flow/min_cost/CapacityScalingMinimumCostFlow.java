package org.jgrapht.alg.flow.min_cost;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.*;

public class CapacityScalingMinimumCostFlow<V, E> implements MinimumCostFlowAlgorithm<V, E> {
    public static final int INFINITY = 1000 * 1000 * 1000;
    private static final String INFEASIBLE_SUPPLY = "Total node supply isn't equal to 0";
    private static final String NO_FEASIBLE_FLOW = "Specified flow network problem has no feasible solution";
    private static final String NEGATIVE_CAPACITY = "Negative edge capacities are not allowed";
    private static final String LOWER_EXCEEDS_UPPER = "Lower edge capacity must not exceed upper edge capacity";
    private static final boolean DEBUG = false;
    private static final int DEFAULT_SCALING_FACTOR = 4;
    private final int scalingFactor;
    private int counter = 1;
    private MinimumCostFlowProblem<V, E> problem;
    private MinimumCostFLow<V, E> minimumCostFLow;
    private MinimumCostFlowState state;
    private int n;
    private int m;

    public CapacityScalingMinimumCostFlow(MinimumCostFlowProblem<V, E> problem) {
        this(problem, DEFAULT_SCALING_FACTOR);
    }

    public CapacityScalingMinimumCostFlow(MinimumCostFlowProblem<V, E> problem, int scalingFactor) {
        if (problem.graph.getType().isUndirected()) {
            throw new IllegalArgumentException("The algorithm doesn't support undirected flow networks");
        }
        this.problem = problem;
        this.scalingFactor = scalingFactor;
        n = problem.graph.vertexSet().size();
        m = problem.graph.edgeSet().size();
    }

    @Override
    public double calculateMinimumCostFlow() {
        return recalculateMinimumCostFlow().getCost();
    }

    @Override
    public V getFlowDirection(E edge) {
        return problem.graph.getEdgeTarget(edge);
    }

    @Override
    public MinimumCostFLow<V, E> getMinimumCostFlow() {
        if (minimumCostFLow == null) {
            recalculateMinimumCostFlow();
        }
        return minimumCostFLow;
    }

    private MinimumCostFlowState init() {
        checkConstraints();

        int supply, upperCap, lowerCap;
        double cost;
        int supplySum = 0;
        Arc arc;
        Node node, opposite;

        Node[] nodes = new Node[n];
        List<V> graphVertices = new ArrayList<>(n);
        Arc[] arcs = new Arc[m];
        List<E> graphEdges = new ArrayList<>(m);

        Map<V, Node> nodeMap = new HashMap<>(n);
        Graph<V, E> graph = problem.graph;


        int i = 0;
        for (V vertex : graph.vertexSet()) {
            supply = problem.supplyMap.getOrDefault(vertex, 0);
            supplySum += supply;
            nodes[i] = new Node();
            graphVertices.add(vertex);
            nodeMap.put(vertex, nodes[i]);
            ++i;
        }
        if (Math.abs(supplySum) > 0) {
            throw new IllegalArgumentException(INFEASIBLE_SUPPLY);
        }
        i = 0;
        for (E edge : graph.edgeSet()) {
            graphEdges.add(edge);
            node = nodeMap.get(graph.getEdgeSource(edge));
            opposite = nodeMap.get(graph.getEdgeTarget(edge));
            upperCap = problem.upperCapacityMap.get(edge);
            lowerCap = problem.lowerCapacityMap.getOrDefault(edge, 0);
            cost = problem.graph.getEdgeWeight(edge);

            // add the cost of sending lowerCap flow across the arc
            if (upperCap < INFINITY) {
                // we have to do a reduction in order to have uncapacitated network
                if (cost < 0) {
                    node.excess -= upperCap - lowerCap;
                    opposite.excess += upperCap - lowerCap;
                    Node t = node;
                    node = opposite;
                    opposite = t;
                }
                // remove non-zero lower capacity
                node.excess -= lowerCap;
                opposite.excess += lowerCap;
                // splitting the arc in order to have uncapacitated network
                Node reductionNode = new Node();
                if (DEBUG) {
                    System.out.println(edge + " -> " + reductionNode);
                }
                arcs[i] = node.addArcTo(reductionNode, Math.abs(cost));
                arc = opposite.addArcTo(reductionNode, 0);
                arc.revArc.increaseResidualCapacity(upperCap - lowerCap);
            } else {
                // this is an uncapacitated arc
                // remove non-zero lower capacity
                node.excess -= lowerCap;
                opposite.excess += lowerCap;
                arcs[i] = node.addArcTo(opposite, Math.abs(cost));
            }
            ++i;
        }
        if (DEBUG) {
            System.out.println("Printing mapping");
            for (Map.Entry<V, Node> entry : nodeMap.entrySet()) {
                System.out.println(entry + " -> " + entry);
            }
        }
        return new MinimumCostFlowState(nodes, graphVertices, arcs, graphEdges);
    }

    private void checkConstraints() {
        int lowerCap, upperCap;
        double cost;
        for (E edge : problem.graph.edgeSet()) {
            upperCap = problem.upperCapacityMap.get(edge);
            lowerCap = problem.lowerCapacityMap.getOrDefault(edge, 0);
            cost = problem.graph.getEdgeWeight(edge);

            if (upperCap < 0) {
                throw new IllegalArgumentException(NEGATIVE_CAPACITY);
            } else if (lowerCap > upperCap) {
                throw new IllegalArgumentException(LOWER_EXCEEDS_UPPER);
            } else if (lowerCap >= INFINITY) {
                throw new IllegalArgumentException("The problem is unbounded due to the infinite lower capacity");
            } else if (upperCap >= INFINITY && cost < 0) {
                throw new IllegalArgumentException("The algorithm doesn't support infinite capacity arcs with negative cost");
            }
        }
    }

    private MinimumCostFLow<V, E> recalculateMinimumCostFlow() {
        this.state = init();
        if (scalingFactor > 1) {
            int maxSupply = maxSupply();
            int upperBound = maxSupply + 1;
            int lowerBound = scalingFactor;
            while (maxSupply > lowerBound) {
                lowerBound *= scalingFactor;
            }
            lowerBound /= scalingFactor;
            while (lowerBound > 1) {
                List<Node> positiveExcessNodes = scale(lowerBound, upperBound);
                pushFlow(positiveExcessNodes);
                upperBound = lowerBound;
                lowerBound /= scalingFactor;
            }
            List<Node> positiveExcessNodes = scale(1, upperBound);
            if (!pushFlow(positiveExcessNodes)) {
                throw new IllegalArgumentException("No feasible flow on the specified network");
            }
        } else {
            List<Node> positiveExcessNodes = scale(1, Integer.MAX_VALUE);
            if (!pushFlow(positiveExcessNodes)) {
                throw new IllegalArgumentException("No feasible flow on the specified network");
            }
        }

        return minimumCostFLow = finish();
    }

    private boolean pushFlow(List<Node> positiveExcessNodes) {
        Node node;
        while (!positiveExcessNodes.isEmpty()) {
            node = positiveExcessNodes.get(positiveExcessNodes.size() - 1);
            if (!pushDijkstra(node)) {
                return node.excess == 0 && positiveExcessNodes.size() == 1;
            }
            if (node.excess == 0) {
                positiveExcessNodes.remove(positiveExcessNodes.size() - 1);
            }
        }
        return true;
    }

    private List<Node> scale(int lowerBound, int upperBound) {
        List<Node> positiveExcessNodes = new ArrayList<>();
        int supply, abs;
        for (int i = 0; i < n; i++) {
            supply = problem.supplyMap.getOrDefault(state.graphVertices.get(i), 0);
            abs = Math.abs(supply);
            if (abs >= lowerBound && abs < upperBound) {
                state.nodes[i].excess = supply;
                if (supply > 0) {
                    positiveExcessNodes.add(state.nodes[i]);
                }
            }
        }
        return positiveExcessNodes;
    }

    private int maxSupply() {
        int maxSupply = 0;
        V vertex;
        int supply;
        for (int i = 0; i < n; i++) {
            vertex = state.graphVertices.get(i);
            supply = problem.supplyMap.getOrDefault(vertex, 0);
            if (Math.abs(supply) > maxSupply) {
                maxSupply = Math.abs(supply);
            }
        }
        return maxSupply;
    }

    private boolean pushDijkstra(Node start) {
        Node currentNode, opposite;
        Arc currentArc;
        double distance;
        FibonacciHeapNode<Node> currentFibNode;
        int TEMPORARILY_LABELED = counter++;
        int PERMANENTLY_LABELED = counter++;
        FibonacciHeap<Node> heap = new FibonacciHeap<>();
        List<Node> permanentlyLabeled = new LinkedList<>();
        start.parentArc = null;
        insertIntoHeap(heap, start, 0);
        while (!heap.isEmpty()) {
            currentFibNode = heap.removeMin();
            currentNode = currentFibNode.getData();
            distance = currentFibNode.getKey();
            if (currentNode.excess < 0) {
                augmentPath(start, currentNode);
                for (Node node : permanentlyLabeled) {
                    node.potential += distance;
                }
                return true;
            }
            currentNode.labelType = PERMANENTLY_LABELED;
            permanentlyLabeled.add(currentNode); // varNode becomes permanently labeled
            for (currentArc = currentNode.firstNonsaturated; currentArc != null; currentArc = currentArc.next) {
                opposite = currentArc.head;
                if (opposite.labelType != PERMANENTLY_LABELED) {
                    if (opposite.labelType == TEMPORARILY_LABELED) {
                        if (distance + currentArc.getReducedCost() < opposite.fibNode.getKey()) {
                            heap.decreaseKey(opposite.fibNode, distance + currentArc.getReducedCost());
                            opposite.parentArc = currentArc;
                        }
                    } else {
                        opposite.labelType = TEMPORARILY_LABELED;
                        insertIntoHeap(heap, opposite, distance + currentArc.getReducedCost());
                        opposite.parentArc = currentArc;
                    }
                }
            }
            currentNode.potential -= distance;
        }
        return false;
    }

    private void augmentPath(Node start, Node end) {
        int delta = Math.min(start.excess, -end.excess);
        /*for (Arc arc = end.parentArc; arc != null; arc = arc.revArc.head.parentArc) {
            delta = Math.min(delta, arc.residualCapacity);
        }*/
        for (Arc arc = end.parentArc; arc != null; arc = arc.revArc.head.parentArc) {
            delta = Math.min(delta, arc.residualCapacity);
        }
        if (DEBUG) {
            ArrayList<Node> stack = new ArrayList<>();
            for (Arc arc = end.parentArc; arc != null; arc = arc.revArc.head.parentArc) {
                stack.add(arc.head);
            }
            stack.add(start);
            System.out.println("Printing augmenting path");
            for (int i = stack.size() - 1; i > 0; i--) {
                System.out.print(stack.get(i).id + " -> ");
            }
            System.out.println(stack.get(0).id + ", delta = " + delta);
        }
        end.excess += delta;
        for (Arc arc = end.parentArc; arc != null; arc = arc.head.parentArc) {
            arc.decreaseResidualCapacity(delta);
            arc = arc.revArc;
            arc.increaseResidualCapacity(delta);
        }
        start.excess -= delta;
    }

    private void insertIntoHeap(FibonacciHeap<Node> heap, Node node, double value) {
        node.fibNode = new FibonacciHeapNode<>(node);
        heap.insert(node.fibNode, value);
    }

    private MinimumCostFLow<V, E> finish() {
        Map<E, Integer> flowMap = new HashMap<>(m);
        E graphEdge;
        Arc arc;
        int flowOnArc;
        double totalCost = 0;
        for (int i = 0; i < m; i++) {
            graphEdge = state.graphEdges.get(i);
            arc = state.arcs[i];
            flowOnArc = arc.revArc.residualCapacity;
            flowOnArc += problem.lowerCapacityMap.getOrDefault(graphEdge, 0);
            if (problem.graph.getEdgeWeight(graphEdge) < 0) {
                flowOnArc = problem.upperCapacityMap.get(graphEdge) - flowOnArc;
            }
            flowMap.put(graphEdge, flowOnArc);
            totalCost += flowOnArc * problem.graph.getEdgeWeight(graphEdge);
        }
        return new MinimumCostFlowImpl<>(totalCost, flowMap);
    }

    private static class Node {
        private static int ID = 1;
        FibonacciHeapNode<Node> fibNode;
        Arc parentArc;
        int labelType;
        int excess;
        int potential;
        Arc firstSaturated;
        Arc firstNonsaturated;
        private int id = ID++;

        public Node() {
            this.excess = 0;
        }

        Arc addArcTo(Node opposite, double cost) {
            Arc forwardArc = new Arc(opposite, INFINITY, cost);
            if (INFINITY < 0) {
                // forward arc becomes the first arc in the linked list of saturated arcs
                if (firstSaturated != null) {
                    firstSaturated.prev = forwardArc;
                }
                forwardArc.next = firstSaturated;
                firstSaturated = forwardArc;
            } else {
                if (firstNonsaturated != null) {
                    firstNonsaturated.prev = forwardArc;
                }
                forwardArc.next = firstNonsaturated;
                firstNonsaturated = forwardArc;
            }
            Arc reverseArc = new Arc(this, 0, -cost);
            if (opposite.firstSaturated != null) {
                opposite.firstSaturated.prev = reverseArc;
            }
            reverseArc.next = opposite.firstSaturated;
            opposite.firstSaturated = reverseArc;

            forwardArc.revArc = reverseArc;
            reverseArc.revArc = forwardArc;

            return forwardArc;
        }

        @Override
        public String toString() {
            return String.format("Id = %d, excess = %d, potential = %d", id, excess, potential);
        }
    }

    private static class Arc {
        final Node head;
        final double cost;
        Arc revArc;
        Arc prev;
        Arc next;
        int residualCapacity;

        Arc(Node head, int residualCapacity, double cost) {
            this.head = head;
            this.cost = cost;
            this.residualCapacity = residualCapacity;
        }

        double getReducedCost() {
            return cost + head.potential - revArc.head.potential;
        }

        void decreaseResidualCapacity(int delta) {
            if (residualCapacity >= INFINITY) {
                return;
            }
            residualCapacity -= delta;
            if (residualCapacity == 0) { // epsilon precision
                // need to move this arc from list of non-saturated arcs to list of saturated arcs
                Node tail = revArc.head;
                if (next != null) {
                    next.prev = prev;
                }
                if (prev != null) {
                    prev.next = next;
                } else {
                    tail.firstNonsaturated = next;
                }
                next = tail.firstSaturated;
                if (tail.firstSaturated != null) {
                    tail.firstSaturated.prev = this;
                }
                tail.firstSaturated = this;
                prev = null;
            }
        }

        void increaseResidualCapacity(int delta) {
            if (residualCapacity >= INFINITY) {
                return;
            }
            if (residualCapacity == 0) { // epsilon precision
                // need to move this arc from list of saturated arcs to list of non-saturated arcs
                Node tail = revArc.head;
                if (next != null) {
                    next.prev = prev;
                }
                if (prev != null) {
                    prev.next = next;
                } else {
                    tail.firstSaturated = next;
                }
                next = tail.firstNonsaturated;
                if (tail.firstNonsaturated != null) {
                    tail.firstNonsaturated.prev = this;
                }
                tail.firstNonsaturated = this;
                prev = null;
            }
            residualCapacity += delta;
        }

        @Override
        public String toString() {
            return String.format("(%d, %d), reduced cost = %.1f, cost = %.1f", revArc.head.id, head.id, getReducedCost(), cost);
        }
    }

    private class MinimumCostFlowState {
        private Node[] nodes;
        private List<V> graphVertices;
        private Arc[] arcs;
        private List<E> graphEdges;

        public MinimumCostFlowState(Node[] nodes, List<V> graphVertices, Arc[] arcs, List<E> graphEdges) {
            this.nodes = nodes;
            this.graphVertices = graphVertices;
            this.arcs = arcs;
            this.graphEdges = graphEdges;
        }
    }
}
