package org.jgrapht.alg.flow;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.MinimumCostFlowAlgorithm;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.*;

import static org.jgrapht.alg.flow.CapacityScalingMinimumCostFlow.LabelType.PERMANENTLY_LABELED;
import static org.jgrapht.alg.flow.CapacityScalingMinimumCostFlow.LabelType.TEMPORARILY_LABELED;

public class CapacityScalingMinimumCostFlow<V, E> implements MinimumCostFlowAlgorithm<V, E> {
    private static final String INFEASIBLE_SUPPLY = "Total node supply isn't equal to 0";
    private static final String NO_FEASIBLE_FLOW = "Specified flow network problem has no feasible solution";
    private static final String NEGATIVE_CAPACITY = "Negative edge capacities are not allowed";
    private static final String LOWER_EXCEEDS_UPPER = "Lower edge capacity must not exceed upper edge capacity";
    private static final double EPS = 10e-12;
    private Graph<V, E> graph;
    private MinimumCostFLow<V, E> minimumCostFLow;
    private Map<E, Double> upperCapacityMap;
    private Map<E, Double> lowerCapacityMap;
    private List<Node> positiveExcessNodes;
    private Node[] nodes;
    private List<V> vertices;
    private Map<E, Pair<Arc, Arc>> edgeMap;
    private double cost;
    private int n;
    private boolean undirected;

    public CapacityScalingMinimumCostFlow(Graph<V, E> graph) {
        this.graph = Objects.requireNonNull(graph);
        undirected = graph.getType().isUndirected();
    }

    @Override
    public double calculateMinimumCostFlow(Map<V, Double> supplyMap, Map<E, Double> upperCapacityMap) {
        return calculateMinimumCostFlow(supplyMap, upperCapacityMap, null);
    }

    @Override
    public double calculateMinimumCostFlow(Map<V, Double> supplyMap, Map<E, Double> upperCapacityMap, Map<E, Double> lowerCapacityMap) {
        init(supplyMap, upperCapacityMap, lowerCapacityMap);
        return recalculateMinimumCostFlow().getCost();
    }

    @Override
    public V getFlowDirection(E edge) {
        if (graph.getType().isUndirected()) {
            Pair<Arc, Arc> arcs = edgeMap.get(edge);
            if (arcs.getFirst().revArc.residualCapacity - EPS < 0) {
                return vertices.get(arcs.getSecond().head.position);
            } else {
                return vertices.get(arcs.getFirst().head.position);
            }
        } else {
            return graph.getEdgeTarget(edge);
        }
    }

    @Override
    public MinimumCostFLow<V, E> getMinimumCostFlow() {
        return minimumCostFLow;
    }

    private void init(Map<V, Double> supplyMap, Map<E, Double> lowerCapacityMap, Map<E, Double> upperCapacityMap) {
        double supply;
        boolean undirected = graph.getType().isUndirected();
        this.lowerCapacityMap = lowerCapacityMap;
        this.upperCapacityMap = upperCapacityMap;
        double supplySum = 0;
        positiveExcessNodes = new ArrayList<>();
        n = graph.vertexSet().size();
        Map<V, Node> nodeMap = new HashMap<>(n);
        vertices = new ArrayList<>(n);
        nodes = new Node[n];
        int i = 0;
        for (V vertex : graph.vertexSet()) {
            supply = supplyMap.get(vertex);
            supplySum += supply;
            nodes[i] = new Node(i, supply);
            nodeMap.put(vertex, nodes[i]);
            vertices.add(vertex);
            if (supply > 0) {
                positiveExcessNodes.add(nodes[i]);
            }
            ++i;
        }
        if (Math.abs(supplySum - EPS) > 0) {
            throw new IllegalArgumentException(INFEASIBLE_SUPPLY);
        }
        Node node, opposite;
        Arc arc, secondArc;
        double capacity;
        edgeMap = new HashMap<>(graph.edgeSet().size());
        for (E edge : graph.edgeSet()) {
            node = nodeMap.get(graph.getEdgeSource(edge));
            opposite = nodeMap.get(graph.getEdgeTarget(edge));
            capacity = upperCapacityMap.get(edge);
            if (lowerCapacityMap != null) {
                capacity -= lowerCapacityMap.get(edge);
            }
            if (capacity < -EPS) {
                if (upperCapacityMap.get(edge) < 0) {
                    throw new IllegalArgumentException(NEGATIVE_CAPACITY);
                } else {
                    throw new IllegalArgumentException(LOWER_EXCEEDS_UPPER);
                }
            }
            arc = node.addArcTo(opposite, capacity, graph.getEdgeWeight(edge));
            if (undirected) {
                secondArc = opposite.addArcTo(node, capacity, graph.getEdgeWeight(edge));
                edgeMap.put(edge, new Pair<>(arc, secondArc));
            } else {
                edgeMap.put(edge, new Pair<>(arc, arc));
            }
        }
    }

    private void pushDijkstra(Node start) {
        Node currentNode, opposite;
        Arc currentArc;
        FibonacciHeapNode<Node> currentFibNode;
        double distance;
        FibonacciHeap<Node> heap = new FibonacciHeap<>();
        List<Node> permanentlyLabeled = new LinkedList<>();
        insertIntoHeap(heap, start, 0);
        while (!heap.isEmpty()) {
            currentFibNode = heap.removeMin();
            currentNode = currentFibNode.getData();
            distance = currentFibNode.getKey();
            if (currentNode.excess < 0) {
                double delta = augmentPath(start, currentNode);
                for (Node node : permanentlyLabeled) {
                    node.potential += distance;
                }
                cost += delta * (distance + start.potential - currentNode.potential);
                return;
            }
            currentNode.labelType = PERMANENTLY_LABELED;
            permanentlyLabeled.add(currentNode); // varNode becomes permanently labeled
            currentNode.potential -= distance;
            for (currentArc = currentNode.firstNonsaturated; currentArc != null; currentArc = currentArc.next) {
                opposite = currentArc.head;
                if (opposite.labelType != PERMANENTLY_LABELED) {
                    if (opposite.labelType == TEMPORARILY_LABELED) {
                        if (distance + currentArc.getReducedCost() < opposite.fibNode.getKey()) {
                            insertIntoHeap(heap, opposite, distance + currentArc.getReducedCost());
                            opposite.parentArc = currentArc;
                        }
                    } else {
                        opposite.labelType = TEMPORARILY_LABELED;
                        insertIntoHeap(heap, opposite, distance + currentArc.getReducedCost());
                        opposite.parentArc = currentArc;
                    }
                }
            }
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

    private MinimumCostFLow<V, E> recalculateMinimumCostFlow() {
        while (!positiveExcessNodes.isEmpty()) {
            Node node = positiveExcessNodes.get(positiveExcessNodes.size() - 1);
            pushDijkstra(node);
        }
        return minimumCostFLow = finish();
    }

    private MinimumCostFLow<V, E> finish() {
        Map<E, Double> flowMap = new HashMap<>(graph.edgeSet().size());
        Pair<Arc, Arc> pair;
        Arc forward, backward;
        for (E edge : graph.edgeSet()) {
            pair = edgeMap.get(edge);
            forward = pair.getFirst();
            backward = pair.getSecond();
            double flow = 0;
            if (lowerCapacityMap != null) {
                flow = lowerCapacityMap.get(edge);
            }
            if (undirected) {
                flow += forward.revArc.residualCapacity += backward.revArc.residualCapacity;
                flowMap.put(edge, flow);
            } else {
                flow += forward.revArc.residualCapacity;
                flowMap.put(edge, flow);
            }
        }
        return new MinimumCostFlowImpl<>(cost, flowMap);
    }

    enum LabelType {
        PERMANENTLY_LABELED, TEMPORARILY_LABELED,
    }

    private static class Node {
        final int position;
        FibonacciHeapNode<Node> fibNode;
        Arc parentArc;
        LabelType labelType;
        double excess;
        double potential;
        Arc firstSaturated;
        Arc firstNonsaturated;

        public Node(int position, double excess) {
            this.position = position;
            this.excess = excess;
        }

        Arc addArcTo(Node opposite, double capacity, double cost) {
            Arc forwardArc = new Arc(opposite, capacity, cost);
            if (capacity < EPS) {
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
                firstNonsaturated = forwardArc.next;
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
    }

    private static class Arc {
        final Node head;
        final double cost;
        Arc revArc;
        Arc prev;
        Arc next;
        double residualCapacity;

        Arc(Node head, double residualCapacity, double cost) {
            this.head = head;
            this.cost = cost;
            this.residualCapacity = residualCapacity;
        }

        double getReducedCost() {
            return cost + head.potential - revArc.head.potential;
        }

        void decreaseResidualCapacity(double delta) {
            residualCapacity -= delta;
            if (residualCapacity < EPS) { // epsilon precision
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

        void increaseResidualCapacity(double delta) {
            if (residualCapacity < EPS) { // epsilon precision
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
    }
}
