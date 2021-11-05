/*
 * (C) Copyright 2018-2021, by Christoph Grüne and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.spanning;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * Implementation of a randomized version of the Esau-Williams heuristic, a greedy randomized
 * adaptive search heuristic (GRASP) for the capacitated minimum spanning tree (CMST) problem. It
 * calculates a suboptimal CMST. The original version can be found in L. R. Esau and K. C. Williams.
 * 1966. On teleprocessing system design: part II a method for approximating the optimal network.
 * IBM Syst. J. 5, 3 (September 1966), 142-147. DOI=http://dx.doi.org/10.1147/sj.53.0142 This
 * implementation runs in polynomial time O(|V|^3).
 * <p>
 * This implementation is a randomized version described in Ahuja, Ravindra K., Orlin, James B., and
 * Sharma, Dushyant, (1998). New neighborhood search structures for the capacitated minimum spanning
 * tree problem, No WP 4040-98. Working papers, Massachusetts Institute of Technology (MIT), Sloan
 * School of Management.
 * <p>
 * This version runs in polynomial time dependent on the number of considered operations per
 * iteration <code>numberOfOperationsParameter</code> (denoted by p), such that runs is in $O(|V|^3
 * + p|V|) = O(|V|^3)$ since $p \leq |V|$.
 * <p>
 * A <a href="https://en.wikipedia.org/wiki/Capacitated_minimum_spanning_tree">Capacitated Minimum
 * Spanning Tree</a> (CMST) is a rooted minimal cost spanning tree that satisfies the capacity
 * constrained on all trees that are connected to the designated root. The problem is NP-hard.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 *
 * @author Christoph Grüne
 * @since July 12, 2018
 */
public class EsauWilliamsCapacitatedMinimumSpanningTree<V, E>
    extends
    AbstractCapacitatedMinimumSpanningTree<V, E>
{

    /**
     * the number of the most profitable operations for every iteration considered in the procedure.
     */
    private final int numberOfOperationsParameter;

    /**
     * contains whether the algorithm was executed
     */
    private boolean isAlgorithmExecuted;

    /**
     * Constructs an Esau-Williams GRASP algorithm instance.
     *
     * @param graph the graph
     * @param root the root of the CMST
     * @param capacity the capacity constraint of the CMST
     * @param weights the weights of the vertices
     * @param numberOfOperationsParameter the parameter how many best vertices are considered in the
     *        procedure
     */
    public EsauWilliamsCapacitatedMinimumSpanningTree(
        Graph<V, E> graph, V root, double capacity, Map<V, Double> weights,
        int numberOfOperationsParameter)
    {
        super(graph, root, capacity, weights);
        this.numberOfOperationsParameter = numberOfOperationsParameter;
        this.isAlgorithmExecuted = false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a capacitated spanning tree computed by the Esau-Williams algorithm.
     */
    @Override
    public CapacitatedSpanningTree<V, E> getCapacitatedSpanningTree()
    {
        if (isAlgorithmExecuted) {
            return bestSolution.calculateResultingSpanningTree();
        }
        bestSolution = getSolution();
        CapacitatedSpanningTree<V, E> cmst = bestSolution.calculateResultingSpanningTree();
        isAlgorithmExecuted = true;
        if (!cmst.isCapacitatedSpanningTree(graph, root, capacity, demands)) {
            throw new IllegalArgumentException(
                "This graph does not have a capacitated minimum spanning tree with the given capacity and demands.");
        }
        return cmst;
    }

    /**
     * Calculates a partition representation of the capacitated spanning tree. With that, it is
     * possible to calculate the to the partition corresponding capacitated spanning tree in
     * polynomial time by calculating the MSTs. The labels of the partition that are returned are
     * non-negative.
     *
     * @return a representation of the partition of the capacitated spanning tree that has
     *         non-negative labels.
     */
    protected CapacitatedSpanningTreeSolutionRepresentation getSolution()
    {
        /*
         * labeling of the improvement graph vertices. There are two vertices in the improvement
         * graph for every vertex in the input graph: the vertex indicating the vertex itself and
         * the vertex indicating the subtree.
         */
        Map<V, Integer> labels = new HashMap<>();
        /*
         * the implicit partition defined by the subtrees
         */
        Map<Integer, Pair<Set<V>, Double>> partition = new HashMap<>();

        /*
         * initialize labels and partitions by assigning every vertex to a new part and create
         * solution representation
         */
        int counter = 0;
        for (V v : graph.vertexSet()) {
            if (v != root) {
                labels.put(v, counter);
                Set<V> currentPart = new HashSet<>();
                currentPart.add(v);
                partition.put(counter, Pair.of(currentPart, demands.get(v)));
                counter++;
            }
        }
        /*
         * construct a new solution representation with the initialized labels and partition
         */
        bestSolution = new CapacitatedSpanningTreeSolutionRepresentation(labels, partition);

        /*
         * map that contains the current savings for all vertices
         */
        Map<V, Double> savings = new HashMap<>();
        /*
         * map that contains the current closest vertex for all vertices
         */
        Map<V, V> closestVertex = new HashMap<>();
        /*
         * map that contains all labels of partition the vertex cannot be assigned because the
         * capacity would be exceeded
         */
        Map<V, Set<Integer>> restrictionMap = new HashMap<>();
        /*
         * map that contains the vertex that is nearest to the root vertex for all labels of the
         * partition
         */
        Map<Integer, V> shortestGate = new HashMap<>();
        /*
         * set of vertices that have to be considered in the current iteration
         */
        Set<V> vertices = new HashSet<>(graph.vertexSet());
        vertices.remove(root);

        while (true) {

            for (Iterator<V> it = vertices.iterator(); it.hasNext();) {

                V v = it.next();

                V closestVertexToV = calculateClosestVertex(v, restrictionMap, shortestGate);

                if (closestVertexToV == null) {
                    // there is not valid closest vertex to connect with, i.e. v will not be
                    // connected to any vertex
                    it.remove();
                    savings.remove(v);
                    continue;
                }

                // store closest vertex to v1
                closestVertex.put(v, closestVertexToV);
                // store the maximum saving and the corresponding vertex
                savings
                    .put(
                        v, getDistance(shortestGate.getOrDefault(bestSolution.getLabel(v), v), root)
                            - getDistance(v, closestVertexToV));
            }

            // calculate list of best operations
            LinkedList<V> bestVertices = getListOfBestOptions(savings);

            if (!bestVertices.isEmpty()) {
                V vertexToMove = bestVertices.get((int) (Math.random() * bestVertices.size()));

                // update shortestGate
                Integer labelOfVertexToMove = bestSolution.getLabel(vertexToMove);
                V closestMoveVertex = closestVertex.get(vertexToMove);
                Integer labelOfClosestMoveVertex = bestSolution.getLabel(closestMoveVertex);

                V shortestGate1 = shortestGate.getOrDefault(labelOfVertexToMove, vertexToMove);
                V shortestGate2 =
                    shortestGate.getOrDefault(labelOfClosestMoveVertex, closestMoveVertex);

                /*
                 * Do improving move. The case distinction is important such that the the
                 * restriction map uses minimal space. If the restriction map contains the label of
                 * the part with bigger weight, i.e. the vertex cannot be connected to this part,
                 * the label is still correct and is not the label of the old part, which will be
                 * deleted by the move operation.
                 */
                if (bestSolution.getPartitionWeight(labelOfVertexToMove) < bestSolution
                    .getPartitionWeight(labelOfClosestMoveVertex))
                {
                    bestSolution
                        .moveVertices(
                            bestSolution.getPartitionSet(labelOfVertexToMove), labelOfVertexToMove,
                            labelOfClosestMoveVertex);

                    if (getDistance(shortestGate1, root) < getDistance(shortestGate2, root)) {
                        shortestGate.put(labelOfClosestMoveVertex, shortestGate1);
                    } else {
                        shortestGate.put(labelOfClosestMoveVertex, shortestGate2);
                    }
                } else {
                    bestSolution
                        .moveVertices(
                            bestSolution.getPartitionSet(labelOfClosestMoveVertex),
                            labelOfClosestMoveVertex, labelOfVertexToMove);

                    if (getDistance(shortestGate1, root) < getDistance(shortestGate2, root)) {
                        shortestGate.put(labelOfVertexToMove, shortestGate1);
                    } else {
                        shortestGate.put(labelOfVertexToMove, shortestGate2);
                    }
                }

            } else {
                break;
            }
        }

        CapacitatedSpanningTreeSolutionRepresentation result =
            new CapacitatedSpanningTreeSolutionRepresentation(labels, partition);

        result.cleanUp();
        Set<Integer> labelSet = new HashSet<>(result.getLabels());
        for (Integer label : labelSet) {
            result.partitionSubtreesOfSubset(result.getPartitionSet(label), label);
        }
        return result;
    }

    /**
     * Returns the list of the best options as stored in <code>savings</code>.
     *
     * @param savings the savings calculated in the algorithm (see getSolution())
     * @return the list of the <code>numberOfOperationsParameter</code> best options
     */
    private LinkedList<V> getListOfBestOptions(Map<V, Double> savings)
    {
        LinkedList<V> bestVertices = new LinkedList<>();

        for (Map.Entry<V, Double> entry : savings.entrySet()) {
            /*
             * insert current tradeOffFunction entry at the position such that the list is order by
             * the tradeOff and the size of the list is at most numberOfOperationsParameter
             */
            int position = 0;
            for (V v : bestVertices) {
                if (savings.get(v) < entry.getValue()) {
                    break;
                }
                position++;
            }
            if (bestVertices.size() == numberOfOperationsParameter) {
                if (position < bestVertices.size()) {
                    bestVertices.removeLast();
                    bestVertices.add(position, entry.getKey());
                }
            } else {
                bestVertices.addLast(entry.getKey());
            }
        }

        return bestVertices;
    }

    /**
     * Calculates the closest vertex to <code>vertex</code> such that the connection of
     * <code>vertex</code> to the subtree of the closest vertex does not violate the capacity
     * constraint and the savings are positive. Otherwise null is returned.
     *
     * @param vertex the vertex to find a valid closest vertex for
     * @param restrictionMap the set of labels of sets of the partition, in which the capacity
     *        constraint is violated.
     * @return the closest valid vertex and null, if no valid vertex exists
     */
    private V calculateClosestVertex(
        V vertex, Map<V, Set<Integer>> restrictionMap, Map<Integer, V> shortestGate)
    {
        V closestVertexToV1 = null;

        double distanceToRoot;
        V shortestGateOfV = shortestGate.get(bestSolution.getLabel(vertex));
        if (shortestGateOfV != null) {
            distanceToRoot = getDistance(shortestGateOfV, root);
        } else {
            distanceToRoot = getDistance(vertex, root);
        }

        // calculate closest vertex to v1
        for (Integer label : bestSolution.getLabels()) {
            Set<Integer> restrictionSet = restrictionMap.get(vertex);
            if (restrictionSet == null || !restrictionSet.contains(label)) {
                Set<V> part = bestSolution.getPartitionSet(label);
                if (!part.contains(vertex)) {
                    for (V v2 : part) {
                        if (graph.containsEdge(vertex, v2)) {
                            double newWeight = bestSolution
                                .getPartitionWeight(bestSolution.getLabel(v2))
                                + bestSolution.getPartitionWeight(bestSolution.getLabel(vertex));
                            if (newWeight <= capacity) {
                                double currentEdgeWeight = getDistance(vertex, v2);
                                if (currentEdgeWeight < distanceToRoot) {
                                    closestVertexToV1 = v2;
                                    distanceToRoot = currentEdgeWeight;
                                }
                            } else {
                                /*
                                 * the capacity would be exceeded if the vertex would be assigned to
                                 * this part, so add the part to the restricted parts
                                 */
                                Set<Integer> restriction =
                                    restrictionMap.computeIfAbsent(vertex, k -> new HashSet<>());
                                restriction.add(bestSolution.getLabel(v2));
                                break;
                            }
                        }
                    }
                }
            }
        }

        return closestVertexToV1;
    }

    private double getDistance(V v1, V v2)
    {
        E e = graph.getEdge(v1, v2);
        if (e == null) {
            return Double.MAX_VALUE;
        }
        return graph.getEdgeWeight(e);
    }
}
