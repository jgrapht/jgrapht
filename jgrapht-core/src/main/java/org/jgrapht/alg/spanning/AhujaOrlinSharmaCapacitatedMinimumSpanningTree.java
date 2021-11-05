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
import org.jgrapht.alg.cycle.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;

import java.util.*;

/**
 * Implementation of an algorithm for the capacitated minimum spanning tree problem using a cyclic
 * exchange neighborhood, based on Ravindra K. Ahuja, James B. Orlin, Dushyant Sharma, A composite
 * very large-scale neighborhood structure for the capacitated minimum spanning tree problem,
 * Operations Research Letters, Volume 31, Issue 3, 2003, Pages 185-194, ISSN 0167-6377,
 * https://doi.org/10.1016/S0167-6377(02)00236-5.
 * (http://www.sciencedirect.com/science/article/pii/S0167637702002365)
 * <p>
 * A <a href="https://en.wikipedia.org/wiki/Capacitated_minimum_spanning_tree">Capacitated Minimum
 * Spanning Tree</a> (CMST) is a rooted minimal cost spanning tree that satisfies the capacity
 * constrained on all trees that are connected to the designated root. The problem is NP-hard. The
 * hard part of the problem is the implicit partition defined by the subtrees. If one can find the
 * correct partition, the MSTs can be calculated in polynomial time.
 * <p>
 * This algorithm is a very large scale neighborhood search algorithm using a cyclic exchange
 * neighborhood until a local minimum is found. It makes frequently use of a MST algorithm and the
 * algorithm for subset disjoint cycles by Ahuja et al. That is, the algorithm may run in
 * exponential time. This algorithm is implemented in two different version: a local search and a
 * tabu search. In both cases we have to find the best neighbor of the current capacitated spanning
 * tree.
 *
 * @param <V> the vertex type
 * @param <E> the edge type
 * @author Christoph Grüne
 * @since July 11, 2018
 */
public class AhujaOrlinSharmaCapacitatedMinimumSpanningTree<V, E>
    extends
    AbstractCapacitatedMinimumSpanningTree<V, E>
{

    /**
     * the maximal length of the cycle in the neighborhood
     */
    private final int lengthBound;

    /**
     * contains whether the best (if true) or the first improvement (if false) is returned in the
     * neighborhood search
     */
    private final boolean bestImprovement;

    /**
     * the number of the most profitable operations considered in the GRASP procedure for the
     * initial solution.
     */
    private final int numberOfOperationsParameter;

    /**
     * the initial solution
     */
    private CapacitatedSpanningTree<V, E> initialSolution;

    /**
     * contains whether the local search uses the vertex operation
     */
    private final boolean useVertexOperation;

    /**
     * contains whether the local search uses the subtree operation
     */
    private final boolean useSubtreeOperation;

    /**
     * contains whether a tabu search is used
     */
    private final boolean useTabuSearch;

    /**
     * the tabu time that is the number of iterations an element is in the tabu list
     */
    private final int tabuTime;

    /**
     * the upper limit of non-improving exchanges, this is the stopping criterion in the tabu search
     */
    private final int upperLimitTabuExchanges;

    /**
     * contains whether the algorithm was executed
     */
    private boolean isAlgorithmExecuted;

    /**
     * Constructs a new instance of this algorithm.
     *
     * @param graph the base graph
     * @param root the designated root of the CMST
     * @param capacity the edge capacity constraint
     * @param demands the demands of the vertices
     * @param lengthBound the length bound of the cycle detection algorithm
     * @param numberOfOperationsParameter the number of operations that are considered in the
     *        randomized Esau-Williams algorithm
     *        {@link EsauWilliamsCapacitatedMinimumSpanningTree} @see
     *        EsauWilliamsCapacitatedMinimumSpanningTree
     */
    public AhujaOrlinSharmaCapacitatedMinimumSpanningTree(
        Graph<V, E> graph, V root, double capacity, Map<V, Double> demands, int lengthBound,
        int numberOfOperationsParameter)
    {
        this(
            graph, root, capacity, demands, lengthBound, false, numberOfOperationsParameter, true,
            true, true, 10, 50);
    }

    /**
     * Constructs a new instance of this algorithm with the proposed initial solution.
     *
     * @param initialSolution the initial solution
     * @param graph the base graph
     * @param root the designated root of the CMST
     * @param capacity the edge capacity constraint
     * @param demands the demands of the vertices
     * @param lengthBound the length bound of the cycle detection algorithm
     */
    public AhujaOrlinSharmaCapacitatedMinimumSpanningTree(
        CapacitatedSpanningTree<V, E> initialSolution, Graph<V, E> graph, V root, double capacity,
        Map<V, Double> demands, int lengthBound)
    {
        this(
            initialSolution, graph, root, capacity, demands, lengthBound, false, true, true, true,
            10, 50);
    }

    /**
     * Constructs a new instance of this algorithm.
     *
     * @param graph the base graph
     * @param root the designated root of the CMST
     * @param capacity the edge capacity constraint
     * @param demands the demands of the vertices
     * @param lengthBound the length bound of the cycle detection algorithm
     * @param bestImprovement contains whether the best (if true) or the first improvement (if
     *        false) is returned in the neighborhood search
     * @param numberOfOperationsParameter the number of operations that are considered in the
     *        randomized Esau-Williams algorithm
     *        {@link EsauWilliamsCapacitatedMinimumSpanningTree} @see
     *        EsauWilliamsCapacitatedMinimumSpanningTree
     * @param useVertexOperation contains whether the local search uses the vertex operation
     * @param useSubtreeOperation contains whether the local search uses the subtree operation
     * @param useTabuSearch contains whether a tabu search is used
     * @param tabuTime the tabu time that is the number of iterations an element is in the tabu list
     * @param upperLimitTabuExchanges the upper limit of non-improving exchanges, this is the
     *        stopping criterion in the tabu search
     */
    public AhujaOrlinSharmaCapacitatedMinimumSpanningTree(
        Graph<V, E> graph, V root, double capacity, Map<V, Double> demands, int lengthBound,
        boolean bestImprovement, int numberOfOperationsParameter, boolean useVertexOperation,
        boolean useSubtreeOperation, boolean useTabuSearch, int tabuTime,
        int upperLimitTabuExchanges)
    {
        super(graph, root, capacity, demands);
        this.lengthBound = lengthBound;
        this.bestImprovement = bestImprovement;
        this.numberOfOperationsParameter = numberOfOperationsParameter;
        if (!useSubtreeOperation && !useVertexOperation) {
            throw new IllegalArgumentException(
                "At least one of the options has to be enabled, otherwise it is not possible to excute the local search: useVertexOperation and useSubtreeOperation.");
        }
        this.useVertexOperation = useVertexOperation;
        this.useSubtreeOperation = useSubtreeOperation;
        this.useTabuSearch = useTabuSearch;
        this.tabuTime = tabuTime;
        this.upperLimitTabuExchanges = upperLimitTabuExchanges;

        this.isAlgorithmExecuted = false;
    }

    /**
     * Constructs a new instance of this algorithm with the proposed initial solution.
     *
     * @param initialSolution the initial solution
     * @param graph the base graph
     * @param root the designated root of the CMST
     * @param capacity the edge capacity constraint
     * @param demands the demands of the vertices
     * @param lengthBound the length bound of the cycle detection algorithm
     * @param bestImprovement contains whether the best (if true) or the first improvement (if
     *        false) is returned in the neighborhood search
     * @param useVertexOperation contains whether the local search uses the vertex operation
     * @param useSubtreeOperation contains whether the local search uses the subtree operation
     * @param useTabuSearch contains whether a tabu search is used
     * @param tabuTime the tabu time that is the number of iterations an element is in the tabu list
     * @param upperLimitTabuExchanges the upper limit of non-improving exchanges, this is the
     *        stopping criterion in the tabu search
     */
    public AhujaOrlinSharmaCapacitatedMinimumSpanningTree(
        CapacitatedSpanningTree<V, E> initialSolution, Graph<V, E> graph, V root, double capacity,
        Map<V, Double> demands, int lengthBound, boolean bestImprovement,
        boolean useVertexOperation, boolean useSubtreeOperation, boolean useTabuSearch,
        int tabuTime, int upperLimitTabuExchanges)
    {
        this(
            graph, root, capacity, demands, lengthBound, bestImprovement, 0, useVertexOperation,
            useSubtreeOperation, useTabuSearch, tabuTime, upperLimitTabuExchanges);
        if (!initialSolution.isCapacitatedSpanningTree(graph, root, capacity, demands)) {
            throw new IllegalArgumentException(
                "The initial solution is not a valid capacitated spanning tree.");
        }
        this.initialSolution = initialSolution;
    }

    @Override
    public CapacitatedSpanningTree<V, E> getCapacitatedSpanningTree()
    {

        if (isAlgorithmExecuted) {
            return bestSolution.calculateResultingSpanningTree();
        }

        // calculates initial solution on which we base the local search
        bestSolution = getInitialSolution();

        // map that contains all spanning trees of the current partition
        Map<Integer, SpanningTreeAlgorithm.SpanningTree<E>> partitionSpanningTrees =
            new HashMap<>();
        // map that contains the subtrees of all vertices
        Map<V, Pair<Set<V>, Double>> subtrees = new HashMap<>();
        // set that contains all part of the partition that were affected by an exchange operation
        Pair<Set<Integer>, Set<V>> affected = Pair.of(bestSolution.getLabels(), new HashSet<>());
        // the improvement graph
        ImprovementGraph improvementGraph = new ImprovementGraph(bestSolution);
        // tabu list
        Set<V> tabuList = new HashSet<>();
        // tabu time list
        Map<Integer, Set<V>> tabuTimeList = new HashMap<>();
        // tabu timer
        int tabuTimer = 0;
        // number of tabu echanges
        int numberOfTabuExchanges = 0;

        // the solution int he current iteration
        CapacitatedSpanningTreeSolutionRepresentation currentSolution = bestSolution;
        // the difference from the current solution and the best solution
        double costDifference = 0;

        double currentCost;

        // do local improvement steps
        while (true) {

            partitionSpanningTrees = calculateSpanningTrees(
                currentSolution, partitionSpanningTrees, affected.getFirst());
            if (useSubtreeOperation) {
                subtrees = calculateSubtreesOfVertices(
                    currentSolution, subtrees, partitionSpanningTrees, affected.getFirst());
            }

            improvementGraph
                .updateImprovementGraph(
                    currentSolution, subtrees, partitionSpanningTrees, affected.getFirst(),
                    tabuList);

            AhujaOrlinSharmaCyclicExchangeLocalAugmentation<
                Pair<Integer, ImprovementGraphVertexType>,
                DefaultWeightedEdge> ahujaOrlinSharmaCyclicExchangeLocalAugmentation =
                    new AhujaOrlinSharmaCyclicExchangeLocalAugmentation<>(
                        improvementGraph.improvementGraph, lengthBound,
                        improvementGraph.cycleAugmentationLabels, bestImprovement);

            GraphWalk<Pair<Integer, ImprovementGraphVertexType>, DefaultWeightedEdge> cycle =
                ahujaOrlinSharmaCyclicExchangeLocalAugmentation.getLocalAugmentationCycle();
            currentCost = cycle.getWeight();
            costDifference += currentCost;

            if (useTabuSearch) { // do tabu search step
                if (currentCost < 0) {
                    affected = executeNeighborhoodOperation(
                        currentSolution, improvementGraph.improvementGraphVertexMapping,
                        improvementGraph.pathExchangeVertexMapping, subtrees, cycle);
                    if (costDifference < 0) {
                        bestSolution = currentSolution;
                        costDifference = 0;
                    }
                } else {
                    if (upperLimitTabuExchanges <= numberOfTabuExchanges) {
                        break;
                    }

                    // clone solution such that a non-improving exchange does not override a good
                    // solution
                    if (currentSolution == bestSolution) {
                        currentSolution = currentSolution.clone();
                    }

                    affected = executeNeighborhoodOperation(
                        currentSolution, improvementGraph.improvementGraphVertexMapping,
                        improvementGraph.pathExchangeVertexMapping, subtrees, cycle);

                    // update tabu list
                    tabuList.addAll(affected.getSecond());
                    tabuTimeList.put(tabuTimer, affected.getSecond());
                    numberOfTabuExchanges++;
                }

                // update tabu list
                Set<V> set = tabuTimeList.remove(tabuTimer - tabuTime - 1);
                if (set != null) {
                    tabuList.removeAll(set);
                }
                tabuTimer++;

            } else { // do normal local search step
                if (currentCost < 0) {
                    affected = executeNeighborhoodOperation(
                        currentSolution, improvementGraph.improvementGraphVertexMapping,
                        improvementGraph.pathExchangeVertexMapping, subtrees, cycle);
                } else {
                    break;
                }
            }
        }

        this.isAlgorithmExecuted = true;
        return bestSolution.calculateResultingSpanningTree();
    }

    /**
     * Calculates an initial solution depending on whether an initial solution was transferred while
     * construction of the algorithm. If no initial solution was proposed, the algorithm of
     * Esau-Williams is used.
     *
     * @return an initial solution
     */
    private CapacitatedSpanningTreeSolutionRepresentation getInitialSolution()
    {
        if (initialSolution != null) {
            return new CapacitatedSpanningTreeSolutionRepresentation(
                initialSolution.getLabels(), initialSolution.getPartition());
        }
        return new EsauWilliamsCapacitatedMinimumSpanningTree<>(
            graph, root, capacity, demands, numberOfOperationsParameter).getSolution();
    }

    /**
     * Executes the move operations induced by the calculated cycle in the improvement graph. It
     * returns the set of labels of the subsets that were affected by the move operations.
     *
     * @param improvementGraphVertexMapping the mapping from the index of the improvement graph
     *        vertex to the correspondent vertex in the base graph
     * @param pathExchangeVertexMapping the mapping from the improvement graph pseudo vertices to
     *        their subset that they represent
     * @param subtrees the map containing the subtree for every vertex
     * @param cycle the calculated cycle in the improvement graph
     * @return the set of affected labels of subsets that were affected by the move operations
     */
    private Pair<Set<Integer>, Set<V>> executeNeighborhoodOperation(
        CapacitatedSpanningTreeSolutionRepresentation currentSolution,
        Map<Integer, V> improvementGraphVertexMapping,
        Map<Pair<Integer, ImprovementGraphVertexType>, Integer> pathExchangeVertexMapping,
        Map<V, Pair<Set<V>, Double>> subtrees,
        GraphWalk<Pair<Integer, ImprovementGraphVertexType>, DefaultWeightedEdge> cycle)
    {
        Set<V> affectedVertices = new HashSet<>();
        Set<Integer> affectedLabels = new HashSet<>();

        Iterator<Pair<Integer, ImprovementGraphVertexType>> it = cycle.getVertexList().iterator();
        if (it.hasNext()) {
            Pair<Integer, ImprovementGraphVertexType> cur = it.next();
            Integer firstLabel;
            switch (cur.getSecond()) {
            case SINGLE:
                firstLabel =
                    currentSolution.getLabel(improvementGraphVertexMapping.get(cur.getFirst()));
                break;
            case SUBTREE:
                firstLabel =
                    currentSolution.getLabel(improvementGraphVertexMapping.get(cur.getFirst()));
                break;
            default:
                firstLabel = -1;
            }
            while (it.hasNext()) {
                Pair<Integer, ImprovementGraphVertexType> next = it.next();

                switch (cur.getSecond()) {
                /*
                 * A vertex is moved form the part of cur to the part of next. Therefore, both parts
                 * are affected. We only consider the label of cur to be affected for now, the label
                 * of next will be add to the affected set in the next iteration.
                 */
                case SINGLE: {
                    V curVertex = improvementGraphVertexMapping.get(cur.getFirst());
                    Integer curLabel = currentSolution.getLabel(curVertex);
                    Integer nextLabel;
                    if (it.hasNext()) {
                        switch (next.getSecond()) {
                        case SINGLE:
                            nextLabel = currentSolution
                                .getLabel(improvementGraphVertexMapping.get(next.getFirst()));
                            break;
                        case SUBTREE:
                            nextLabel = currentSolution
                                .getLabel(improvementGraphVertexMapping.get(next.getFirst()));
                            break;
                        case PSEUDO:
                            nextLabel = pathExchangeVertexMapping.get(next);
                            break;
                        default:
                            throw new IllegalStateException(
                                "This is a bug. There are invalid types of vertices in the cycle.");
                        }
                    } else {
                        nextLabel = firstLabel;
                    }
                    affectedVertices.add(curVertex);
                    affectedLabels.add(curLabel);

                    currentSolution.moveVertex(curVertex, curLabel, nextLabel);
                    break;
                }
                /*
                 * A subtree is moved from the part of cur to the part of next. Therefore, the part
                 * of cur is affected.
                 */
                case SUBTREE: {
                    V curVertex = improvementGraphVertexMapping.get(cur.getFirst());
                    Integer curLabel = currentSolution.getLabel(curVertex);
                    Integer nextLabel;
                    if (it.hasNext()) {
                        switch (next.getSecond()) {
                        case SINGLE:
                            nextLabel = currentSolution
                                .getLabel(improvementGraphVertexMapping.get(next.getFirst()));
                            break;
                        case SUBTREE:
                            nextLabel = currentSolution
                                .getLabel(improvementGraphVertexMapping.get(next.getFirst()));
                            break;
                        case PSEUDO:
                            nextLabel = pathExchangeVertexMapping.get(next);
                            break;
                        default:
                            throw new IllegalStateException(
                                "This is a bug. There are invalid types of vertices in the cycle.");
                        }
                    } else {
                        nextLabel = firstLabel;
                    }
                    affectedVertices.add(curVertex);
                    affectedLabels.add(curLabel);

                    // get the whole subtree that has to be moved
                    Set<V> subtreeToMove = subtrees.get(curVertex).getFirst();
                    currentSolution.moveVertices(subtreeToMove, curLabel, nextLabel);
                    break;
                }
                /*
                 * cur is the end of a path exchange. Thus, the part of cur is affected because
                 * vertices were inserted.
                 */
                case PSEUDO: {
                    Integer curLabel = pathExchangeVertexMapping.get(cur);
                    affectedLabels.add(curLabel);
                    break;
                }
                /*
                 * This is the beginning of a path exchange. We have nothing to do.
                 */
                case ORIGIN: {
                    break;
                }
                default:
                    throw new IllegalStateException(
                        "This is a bug. There are invalid types of vertices in the cycle.");
                }

                cur = next;
            }

        }

        /*
         * The subsets in the partition may include more than one subtree rooted at root. We create
         * a subset for all subtrees rooted at root.
         */
        Set<Integer> moreAffectedLabels = new HashSet<>();
        Iterator<Integer> affectedLabelIterator = affectedLabels.iterator();
        while (affectedLabelIterator.hasNext()) {
            int label = affectedLabelIterator.next();
            Set<V> vertexSubset = currentSolution.getPartitionSet(label);
            if (vertexSubset.isEmpty()) {
                affectedLabelIterator.remove();
            } else {
                moreAffectedLabels
                    .addAll(currentSolution.partitionSubtreesOfSubset(vertexSubset, label));
            }
        }
        affectedLabels.addAll(moreAffectedLabels);

        // clean up the partition such that only current subsets are represented
        currentSolution.cleanUp();

        return Pair.of(affectedLabels, affectedVertices);
    }

    /**
     * Updates the map containing the MSTs for every subset of the partition.
     *
     * @param partitionSpanningTrees the map containing the MST for every subset of the partition
     * @param affectedLabels the labels of the subsets of the partition that were changed due to the
     *        multi-exchange
     * @return the updated map containing the MST for every subset of the partition
     */
    private Map<Integer, SpanningTreeAlgorithm.SpanningTree<E>> calculateSpanningTrees(
        CapacitatedSpanningTreeSolutionRepresentation currentSolution,
        Map<Integer, SpanningTreeAlgorithm.SpanningTree<E>> partitionSpanningTrees,
        Set<Integer> affectedLabels)
    {
        for (Integer label : affectedLabels) {
            Set<V> set = currentSolution.getPartitionSet(label);
            currentSolution.getPartitionSet(label).add(root);
            partitionSpanningTrees
                .put(
                    label,
                    new PrimMinimumSpanningTree<>(new AsSubgraph<>(graph, set)).getSpanningTree());
            currentSolution.getPartitionSet(label).remove(root);
        }
        return partitionSpanningTrees;
    }

    /**
     * Updates the map containing the subtrees of all vertices in the graph with respect to the MST
     * in the partition and returns them in map.
     *
     * @param subtrees the subtree map to update
     * @param partitionSpanningTree the map containing the MST for every subset of the partition
     * @param affectedLabels the labels of the subsets of the partition that were changed due to the
     *        multi-exchange
     * @return the updated map of vertices to their subtrees
     */
    private Map<V, Pair<Set<V>, Double>> calculateSubtreesOfVertices(
        CapacitatedSpanningTreeSolutionRepresentation currentSolution,
        Map<V, Pair<Set<V>, Double>> subtrees,
        Map<Integer, SpanningTreeAlgorithm.SpanningTree<E>> partitionSpanningTree,
        Set<Integer> affectedLabels)
    {
        for (Integer label : affectedLabels) {
            Set<V> modifiableSet = new HashSet<>(currentSolution.getPartitionSet(label));
            modifiableSet.add(root);
            for (V v : currentSolution.getPartitionSet(label)) {
                Pair<Set<V>, Double> currentSubtree =
                    subtree(currentSolution, modifiableSet, v, partitionSpanningTree);
                subtrees.put(v, currentSubtree);
            }
        }
        return subtrees;
    }

    /**
     * Calculates the subtree of <code>v</code> with respect to the MST given in
     * <code>partitionSpanningTree</code>.
     *
     * @param v the vertex to calculate the subtree for
     * @param partitionSpanningTree the map from labels to spanning trees of the partition.
     * @return the subtree of <code>v</code> with respect to the MST given in
     *         <code>partitionSpanningTree</code>.
     */
    private Pair<Set<V>, Double> subtree(
        CapacitatedSpanningTreeSolutionRepresentation currentSolution, Set<V> modifiableSet, V v,
        Map<Integer, SpanningTreeAlgorithm.SpanningTree<E>> partitionSpanningTree)
    {
        /*
         * initializes graph that is the MST of the current subset rooted
         */
        SpanningTreeAlgorithm.SpanningTree<E> partSpanningTree =
            partitionSpanningTree.get(currentSolution.getLabel(v));
        Graph<V, E> spanningTree =
            new AsSubgraph<>(graph, modifiableSet, partSpanningTree.getEdges());

        /*
         * calculate subtree rooted at v
         */
        Set<V> subtree = new HashSet<>();
        double subtreeWeight = 0;

        Iterator<V> depthFirstIterator = new DepthFirstIterator<>(spanningTree, v);
        Set<V> currentPath = new HashSet<>();
        double currentWeight = 0;

        boolean storeCurrentPath = true;
        while (depthFirstIterator.hasNext()) {
            V next = depthFirstIterator.next();
            if (spanningTree.containsEdge(next, v)) {
                storeCurrentPath = true;

                subtree.addAll(currentPath);
                subtreeWeight += currentWeight;

                currentPath = new HashSet<>();
                currentWeight = 0;
            }
            /*
             * This part of the subtree is connected to the root, thus, this particular tree is not
             * part of the subtree of the current vertex v.
             */
            if (next.equals(root)) {
                storeCurrentPath = false;

                currentPath = new HashSet<>();
                currentWeight = 0;
            }
            if (storeCurrentPath) {
                currentPath.add(next);
                currentWeight += demands.get(next);
            }
        }
        return Pair.of(subtree, subtreeWeight);
    }

    /**
     * This enums contains the vertex types of the improvement graph.
     */
    private enum ImprovementGraphVertexType
    {
        SINGLE,
        SUBTREE,
        PSEUDO,
        ORIGIN
    }

    /**
     * This class realises the improvement graph for the composite multi-exchange large neighborhood
     * search. The improvement graph encodes two exchange classes: - cyclic exchange (on vertices
     * and subtrees) - path exchange (on vertices and subtrees)
     * <p>
     * DEFINITION EXCHANGES Let T[i] be the subtree rooted at i of the MST implicitly defined by the
     * vertex partition. Cyclic Exchange: A cyclic exchange is defined on vertices i_1, ..., i_r,
     * i_1, where the vertices represent either itself in the base graph or the subtrees rooted at
     * i_k for k = 1, ..., r, where T[i_a] != T[i_b] for a != b. The cyclic exchange on i_1, ...,
     * i_r, i_1 moves the i_a (or T[i_a]) to the subset of i_b, where b = a+1 mod r+1. Such a cyclic
     * exchange is feasible if the capacity constraint is not violated. We can represent the cost of
     * the cyclic exchange by the following formulas: Let S[i_k] be the subset of i_k in the
     * implicitly defined partition. - exchange of vertices: $$ c(T_new) - c(T) = \sum_{a = 1}^{r}
     * c(\{i_{a - 1}\} \cup S[i_{i_a}] \setminus \{i_a\}] $$ - exchange of rooted subtrees: $$
     * c(T_new) - c(T) = \sum_{a = 1}^{r} c(T[i_{a - 1}] \cup S[i_{i_a}] \setminus T[i_a]] $$ where
     * c is the given edge cost function and T_new is the CMST resulting by executing the cyclic
     * exchange. Thus, an exchange is profitable if c(T_new) - c(T) < 0.
     * <p>
     * Path Exchange: A path exchange follows the same idea as the cyclic exchange but it does not
     * end at the same vertex. That is, the path exchange is defined on i_1, ..., i_r. The cost
     * function has to be adapted at the start and end point of the path.
     * <p>
     * DEFINITION NEIGHBORHOOD Furthermore, we have to define the neighborhood. These are all
     * capacitated spanning trees that are reachable by using such an exchange as given above.
     * <p>
     * DEFINITION IMPROVEMENT GRAPH The improvement graph is based on a feasible capacitated
     * spanning tree and uses a one-to-one correspondence between the vertices in the base graph and
     * the vertices in the improvement graph. We want to define the arc set of the improvement graph
     * such that each subset disjoint directed cycle (see construction) correspond to a cyclic
     * exchange (or a path exchange, we come to that later). Furthermore, the cost of the cycle in
     * the improvement graph and the cost of the corresponding cyclic exchange has to be equal.
     * <p>
     * CONSTRUCTION OF THE IMPROVEMENT GRAPH The improvement graph IG = (V, A) has the vertex set V,
     * which is equal to the vertex set of the base graph. The arc set A is defined in the
     * following: A directed arc (i, j) in IG represents that we move the node i (or the subtree
     * T[i]) to the subset in which vertex j is. That is, vertex i and j are removed from their
     * subset and i (or the subtree T[i]) is moved to the subset of j. This arc only exists if the
     * exchange is feasible. Then, the cost can be defined as $$ c(\{T[i]\} \cup S[j] \setminus
     * \{T[j]\}) - c(S[j]). $$ A directed cycle i_1, ..., i_r, i_1 in this graph subset disjoint if
     * the subsets of the nodes are pairwise disjoint. By this definition, there is a one-to-one
     * cost-preserving correspondence between the cyclic exchanges and the subset disjoint directed
     * cycles in the improvement graph IG.
     * <p>
     * Identifying path exchanges: For the conversion of path exchanges into subset disjoint cycles,
     * we have to introduce two more node types in the improvement graph: pseudo nodes and a origin
     * node. On the one hand, pseudo nodes represent a subset of the implicitly defined partition
     * and mark the end of the end of a path exchange. On the other hand, the origin node marks a
     * beginning of a path exchange. Therefore, the pseudo node are connected to the origin node to
     * induce subset disjoint cycles. The costs of the arcs from and to the pseudo nodes and the
     * origin nodes are defined as follows: We denoted the original nodes in the improvement graph
     * as regular nodes - c(p, o) = 0 for all pseudo nodes p and origin node o - c(o, r) = c(S[j]
     * \setminus \{T[j]\}) - c(S[j]) for origin node o and for all regular nodes r - c(r, p) =
     * c(\{T[i]\} \cup S[j]) - c(S[j]) for all regular nodes r and for all pseudo nodes p Again,
     * those arc exists only if the exchange is feasible.
     * <p>
     * IDENTIFYING SUBSET DISJOINT CYCLES This is done via a heuristic which can be found here
     * {@link AhujaOrlinSharmaCyclicExchangeLocalAugmentation} @see
     * AhujaOrlinSharmaCyclicExchangeLocalAugmentation.
     */
    private class ImprovementGraph
    {

        /**
         * the improvement graph itself
         */
        Graph<Pair<Integer, ImprovementGraphVertexType>, DefaultWeightedEdge> improvementGraph;

        /**
         * the current solution corresponding to the improvement graph
         */
        CapacitatedSpanningTreeSolutionRepresentation capacitatedSpanningTreeSolutionRepresentation;

        /**
         * mapping form all improvement graph vertices to their labels corresponding to the base
         * graph for the CMST problem
         */
        Map<Pair<Integer, ImprovementGraphVertexType>, Integer> cycleAugmentationLabels;

        /**
         * mapping from the vertex index in the improvement graph to the vertex in the base graph
         */
        Map<Integer, V> improvementGraphVertexMapping;
        /**
         * mapping from the base graph vertex to the vertex index in the improvement graph
         */
        Map<V, Integer> initialVertexMapping;
        /**
         * mapping from the label of the subsets to the corresponding vertex mapping
         */
        Map<Integer, Pair<Integer, ImprovementGraphVertexType>> pseudoVertexMapping;
        /**
         * mapping from the pseudo vertices to the label of the subset they are representing
         */
        Map<Pair<Integer, ImprovementGraphVertexType>, Integer> pathExchangeVertexMapping;
        /**
         * the origin vertex
         */
        Pair<Integer, ImprovementGraphVertexType> origin;
        /**
         * dummy label of the origin vertex
         */
        final Integer originVertexLabel = -1;

        /**
         * Constructs an new improvement graph object for this CMST algorithm instance.
         */
        public ImprovementGraph(
            CapacitatedSpanningTreeSolutionRepresentation capacitatedSpanningTreeSolutionRepresentation)
        {
            this.capacitatedSpanningTreeSolutionRepresentation =
                capacitatedSpanningTreeSolutionRepresentation;
            this.improvementGraphVertexMapping = new HashMap<>();
            this.initialVertexMapping = new HashMap<>();
            this.pseudoVertexMapping = new HashMap<>();
            this.pathExchangeVertexMapping = new HashMap<>();
            /*
             * We initialize this map such that it can be used in the subset-disjoint cycle
             * detection algorithm. This map redirects the getters to the corresponding maps in this
             * improvement graph such that it realises the correct functionality.
             */
            this.cycleAugmentationLabels = getImprovementGraphLabelMap();

            this.improvementGraph = createImprovementGraph();
        }

        /**
         * Initializes the improvement graph, i.e. adds single, subtree and pseudo vertices as well
         * as the origin vertex. Furthermore, it initializes all mappings.
         *
         * @return the improvement graph itself.
         */
        public Graph<Pair<Integer, ImprovementGraphVertexType>,
            DefaultWeightedEdge> createImprovementGraph()
        {
            Graph<Pair<Integer, ImprovementGraphVertexType>, DefaultWeightedEdge> improvementGraph =
                new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

            int counter = 0;

            for (V v : graph.vertexSet()) {

                if (v.equals(root)) {
                    continue;
                }

                if (useVertexOperation) {
                    Pair<Integer, ImprovementGraphVertexType> singleVertex =
                        new Pair<>(counter, ImprovementGraphVertexType.SINGLE);
                    improvementGraph.addVertex(singleVertex);
                }
                if (useSubtreeOperation) {
                    Pair<Integer, ImprovementGraphVertexType> subtreeVertex =
                        new Pair<>(counter, ImprovementGraphVertexType.SUBTREE);
                    improvementGraph.addVertex(subtreeVertex);
                }

                // we have to add these only once
                improvementGraphVertexMapping.put(counter, v);
                initialVertexMapping.put(v, counter);

                counter++;
            }

            Pair<Integer, ImprovementGraphVertexType> origin =
                new Pair<>(counter, ImprovementGraphVertexType.ORIGIN);
            improvementGraph.addVertex(origin);
            this.origin = origin;
            pathExchangeVertexMapping.put(origin, originVertexLabel);

            for (Integer label : capacitatedSpanningTreeSolutionRepresentation.getLabels()) {
                Pair<Integer, ImprovementGraphVertexType> pseudoVertex =
                    new Pair<>(origin.getFirst() + label + 1, ImprovementGraphVertexType.PSEUDO);
                pseudoVertexMapping.put(label, pseudoVertex);
                pathExchangeVertexMapping.put(pseudoVertex, label);
                improvementGraph.addVertex(pseudoVertex);
            }

            /*
             * connection of pseudo nodes and origin node
             */
            for (Pair<Integer, ImprovementGraphVertexType> v : pseudoVertexMapping.values()) {
                improvementGraph.setEdgeWeight(improvementGraph.addEdge(v, origin), 0);
            }

            return improvementGraph;
        }

        /**
         * Updates the improvement graph. It updates the vertices and edges in the parts specified
         * in <code>labelsToUpdate</code>.
         *
         * @param currentSolution the current solution
         * @param subtrees the mapping from vertices to their subtree
         * @param partitionSpanningTrees the mapping from labels of subsets to their spanning tree
         * @param labelsToUpdate the labels of all subsets that has to be updated (because of the
         *        multi-exchange operation)
         */
        public void updateImprovementGraph(
            CapacitatedSpanningTreeSolutionRepresentation currentSolution,
            Map<V, Pair<Set<V>, Double>> subtrees,
            Map<Integer, SpanningTreeAlgorithm.SpanningTree<E>> partitionSpanningTrees,
            Set<Integer> labelsToUpdate, Set<V> tabuList)
        {

            this.capacitatedSpanningTreeSolutionRepresentation = currentSolution;
            this.cycleAugmentationLabels = getImprovementGraphLabelMap();

            updatePseudoNodesOfNewLabels(currentSolution);

            for (V v1 : graph.vertexSet()) {

                if (v1.equals(root)) {
                    continue;
                }

                Pair<Integer, ImprovementGraphVertexType> vertexOfV1Single =
                    Pair.of(initialVertexMapping.get(v1), ImprovementGraphVertexType.SINGLE);
                Pair<Integer, ImprovementGraphVertexType> vertexOfV1Subtree =
                    Pair.of(initialVertexMapping.get(v1), ImprovementGraphVertexType.SUBTREE);

                if (updateTabuVertices(tabuList, v1, vertexOfV1Single, vertexOfV1Subtree)) {
                    continue;
                }

                updateOriginNodeConnections(
                    currentSolution, subtrees, partitionSpanningTrees, labelsToUpdate, v1,
                    vertexOfV1Single, vertexOfV1Subtree);

                /*
                 * update the connections to regular nodes and pseudo nodes
                 */
                for (Integer label : currentSolution.getLabels()) {

                    /*
                     * only update if there is a change induced by a changed part. This potentially
                     * saves a lot of time.
                     */
                    if (label.equals(currentSolution.getLabel(v1))
                        || (!labelsToUpdate.contains(currentSolution.getLabel(v1))
                            && !labelsToUpdate.contains(label)))
                    {
                        continue;
                    }

                    Pair<Integer, ImprovementGraphVertexType> pseudoVertex =
                        pseudoVertexMapping.get(label);

                    Set<V> modifiableSet = new HashSet<>(currentSolution.getPartitionSet(label));
                    // add root to the set for MST calculations
                    modifiableSet.add(root);

                    double oldWeight = partitionSpanningTrees.get(label).getWeight();

                    updateSingleNode(
                        currentSolution, subtrees, tabuList, label, oldWeight, modifiableSet,
                        pseudoVertex, v1, vertexOfV1Single);
                    updateSubtreeNode(
                        currentSolution, subtrees, tabuList, label, oldWeight, modifiableSet,
                        pseudoVertex, v1, vertexOfV1Subtree);
                }
            }
        }

        /**
         * Updates the pseudo nodes corresponding to new subsets in the partition. That is, new
         * pseudo nodes for new labels in the label set are added and pseudo nodes of labels that
         * are no more in the label set are removed.
         *
         * @param currentSolution the current solution in the iteration
         */
        private void updatePseudoNodesOfNewLabels(
            CapacitatedSpanningTreeSolutionRepresentation currentSolution)
        {
            if (!currentSolution.getLabels().equals(pseudoVertexMapping.keySet())) {
                for (Integer label : currentSolution.getLabels()) {
                    if (!pseudoVertexMapping.keySet().contains(label)) {
                        Pair<Integer, ImprovementGraphVertexType> pseudoVertex = new Pair<>(
                            origin.getFirst() + label + 1, ImprovementGraphVertexType.PSEUDO);
                        pseudoVertexMapping.put(label, pseudoVertex);
                        pathExchangeVertexMapping.put(pseudoVertex, label);
                        improvementGraph.addVertex(pseudoVertex);
                        DefaultWeightedEdge newEdge =
                            improvementGraph.addEdge(pseudoVertex, origin);
                        improvementGraph.setEdgeWeight(newEdge, 0);
                    }
                }
                if (currentSolution.getLabels().size() != pseudoVertexMapping.keySet().size()) {
                    Iterator<Integer> labelIterator = pseudoVertexMapping.keySet().iterator();
                    while (labelIterator.hasNext()) {
                        int label = labelIterator.next();
                        if (!currentSolution.getLabels().contains(label)) {
                            Pair<Integer, ImprovementGraphVertexType> pseudoVertex = new Pair<>(
                                origin.getFirst() + label + 1, ImprovementGraphVertexType.PSEUDO);
                            labelIterator.remove();
                            pathExchangeVertexMapping.remove(pseudoVertex);
                            improvementGraph.removeVertex(pseudoVertex);
                        }
                    }
                }
            }
        }

        /**
         * Updates all nodes that correspond to <code>v1</code> and returns if the vertex
         * <code>v1</code>. That is, all incident edges of <code>v1</code> are removed if
         * <code>v1</code> is in the tabu list.
         *
         * @param tabuList the tabu list of the current iteration
         * @param v1 the vertex to update the nodes in the improvement graph for
         * @param vertexOfV1Single the node in the improvement graph representing the exchange of
         *        the vertex <code>v1</code>
         * @param vertexOfV1Subtree the node in the improvement graph representing the exchange of
         *        the subtree rooted at <code>v1</code>
         * @return true iff <code>v1</code> is in the tabu list
         */
        private boolean updateTabuVertices(
            Set<V> tabuList, V v1, Pair<Integer, ImprovementGraphVertexType> vertexOfV1Single,
            Pair<Integer, ImprovementGraphVertexType> vertexOfV1Subtree)
        {

            if (tabuList.contains(v1)) {
                // remove all edges from the vertex
                if (useVertexOperation) {
                    improvementGraph.removeVertex(vertexOfV1Single);
                    improvementGraph.addVertex(vertexOfV1Single);
                }
                if (useSubtreeOperation) {
                    improvementGraph.removeVertex(vertexOfV1Subtree);
                    improvementGraph.addVertex(vertexOfV1Subtree);
                }
                return true;
            }

            return false;
        }

        /**
         * Updates the edges to the origin vertex.
         *
         * @param currentSolution the current solution in the iteration
         * @param subtrees the mapping from vertices to their subtree
         * @param partitionSpanningTrees the mapping from labels of subsets to their spanning tree
         * @param labelsToUpdate the labels of all subsets that has to be updated (because of the
         *        multi-exchange operation)
         * @param v1 the vertex to update the nodes in the improvement graph for
         * @param vertexOfV1Single the node in the improvement graph representing the exchange of
         *        the vertex <code>v1</code>
         * @param vertexOfV1Subtree the node in the improvement graph representing the exchange of
         *        the subtree rooted at <code>v1</code>
         */
        private void updateOriginNodeConnections(
            CapacitatedSpanningTreeSolutionRepresentation currentSolution,
            Map<V, Pair<Set<V>, Double>> subtrees,
            Map<Integer, SpanningTreeAlgorithm.SpanningTree<E>> partitionSpanningTrees,
            Set<Integer> labelsToUpdate, V v1,
            Pair<Integer, ImprovementGraphVertexType> vertexOfV1Single,
            Pair<Integer, ImprovementGraphVertexType> vertexOfV1Subtree)
        {
            double newWeight, oldWeight;
            SpanningTreeAlgorithm.SpanningTree<E> spanningTree;

            /*
             * update connections to origin node
             */
            if (labelsToUpdate.contains(currentSolution.getLabel(v1))) {
                oldWeight = partitionSpanningTrees.get(currentSolution.getLabel(v1)).getWeight();
                /*
                 * edge for v1 vertex remove operation
                 */
                Set<V> partitionSetOfV1 =
                    currentSolution.getPartitionSet(currentSolution.getLabel(v1));
                partitionSetOfV1.add(root);
                if (useVertexOperation) {
                    partitionSetOfV1.remove(v1);
                    spanningTree =
                        new PrimMinimumSpanningTree<>(new AsSubgraph<>(graph, partitionSetOfV1))
                            .getSpanningTree();
                    if (spanningTree.getEdges().size() == partitionSetOfV1.size() - 1) {
                        newWeight = spanningTree.getWeight();
                    } else {
                        newWeight = Double.NaN;
                    }
                    updateImprovementGraphEdge(origin, vertexOfV1Single, 0, newWeight - oldWeight);
                    partitionSetOfV1.add(v1);
                }
                /*
                 * edge for v1 subtree remove operation If the subtree of v1 contains only the
                 * vertex itself, it is the same operation as removing v1 as vertex. Thus, do not
                 * add edges.
                 */
                if (useSubtreeOperation) {
                    if (subtrees.get(v1).getFirst().size() > 1 || !useVertexOperation) {
                        partitionSetOfV1.removeAll(subtrees.get(v1).getFirst());
                        spanningTree =
                            new PrimMinimumSpanningTree<>(new AsSubgraph<>(graph, partitionSetOfV1))
                                .getSpanningTree();
                        if (spanningTree.getEdges().size() == partitionSetOfV1.size() - 1) {
                            newWeight = spanningTree.getWeight();
                        } else {
                            newWeight = Double.NaN;
                        }
                        updateImprovementGraphEdge(
                            origin, vertexOfV1Subtree, 0, newWeight - oldWeight);
                        partitionSetOfV1.addAll(subtrees.get(v1).getFirst());
                    } else {
                        improvementGraph.removeVertex(vertexOfV1Subtree);
                        improvementGraph.addVertex(vertexOfV1Subtree);
                    }
                }
                partitionSetOfV1.remove(root);
            }
        }

        /**
         * Updates all edges from <code>vertexOfV1Single</code> to nodes in the subset represented
         * by <code>label</code>.
         *
         * @param currentSolution the current solution in the iteration
         * @param subtrees the mapping from vertices to their subtree
         * @param tabuList the tabu list of the current iteration
         * @param label the current label to update the edges for
         * @param oldWeight the old weight of the subset
         * @param modifiableSet a modifiable version of the subset of nodes represented by label
         *        inclusive the root node
         * @param pseudoVertex the pseudo vertex representing the subset represented by label
         * @param v1 the vertex to update the nodes in the improvement graph for
         * @param vertexOfV1Single the node in the improvement graph representing the exchange of
         *        the vertex <code>v1</code>
         */
        private void updateSingleNode(
            CapacitatedSpanningTreeSolutionRepresentation currentSolution,
            Map<V, Pair<Set<V>, Double>> subtrees, Set<V> tabuList, int label, double oldWeight,
            Set<V> modifiableSet, Pair<Integer, ImprovementGraphVertexType> pseudoVertex, V v1,
            Pair<Integer, ImprovementGraphVertexType> vertexOfV1Single)
        {
            double newCapacity, newWeight;
            SpanningTreeAlgorithm.SpanningTree<E> spanningTree;

            // add v1 to the set for MST calculations
            modifiableSet.add(v1);

            /*
             * Adding of edges for v1 vertex replacing an object in v2. We need to considers this
             * only if vertex operations should be used.
             */
            if (useVertexOperation) {
                for (V v2 : currentSolution.getPartitionSet(label)) {

                    if (v2.equals(root)) {
                        throw new IllegalStateException(
                            "The root is in the partition. This is a bug.");
                    }

                    if (tabuList.contains(v2)) {
                        continue;
                    }

                    /*
                     * edge for v1 vertex replacing v2 vertex
                     */
                    modifiableSet.remove(v2);
                    spanningTree = new PrimMinimumSpanningTree<>(
                        new AsSubgraph<>(graph, modifiableSet, graph.edgeSet())).getSpanningTree();
                    if (spanningTree.getEdges().size() == modifiableSet.size() - 1) {
                        newCapacity = calculateMaximumDemandOfSubtrees(
                            modifiableSet, spanningTree, currentSolution.getPartitionWeight(label)
                                + demands.get(v1) - demands.get(v2));
                        newWeight = spanningTree.getWeight();
                    } else {
                        newCapacity = Double.NaN;
                        newWeight = Double.NaN;
                    }
                    updateImprovementGraphEdge(
                        vertexOfV1Single,
                        Pair.of(initialVertexMapping.get(v2), ImprovementGraphVertexType.SINGLE),
                        newCapacity, newWeight - oldWeight);
                    modifiableSet.add(v2);
                    // end edge for v1 vertex replacing v2 vertex

                    /*
                     * edge for v1 vertex replacing v2 subtree If the subtree of v2 contains only
                     * the vertex itself and both operations are used, it is the same operation as
                     * moving v2 as vertex. Thus, do not add edges.
                     */
                    if (useSubtreeOperation) {
                        if (subtrees.get(v2).getFirst().size() > 1) {
                            modifiableSet.removeAll(subtrees.get(v2).getFirst());
                            spanningTree = new PrimMinimumSpanningTree<>(
                                new AsSubgraph<>(graph, modifiableSet, graph.edgeSet()))
                                    .getSpanningTree();
                            if (spanningTree.getEdges().size() == modifiableSet.size() - 1) {
                                newCapacity = calculateMaximumDemandOfSubtrees(
                                    modifiableSet, spanningTree,
                                    currentSolution.getPartitionWeight(label) + demands.get(v1)
                                        - subtrees.get(v2).getSecond());
                                newWeight = spanningTree.getWeight();
                            } else {
                                newCapacity = Double.NaN;
                                newWeight = Double.NaN;
                            }
                            updateImprovementGraphEdge(
                                vertexOfV1Single,
                                Pair
                                    .of(
                                        initialVertexMapping.get(v2),
                                        ImprovementGraphVertexType.SUBTREE),
                                newCapacity, newWeight - oldWeight);
                            modifiableSet.addAll(subtrees.get(v2).getFirst());
                        }
                    }
                    // end edge for v1 vertex replacing v2 subtree
                }

                /*
                 * edge for v1 vertex replacing no object
                 */
                spanningTree = new PrimMinimumSpanningTree<>(
                    new AsSubgraph<>(graph, modifiableSet, graph.edgeSet())).getSpanningTree();
                if (spanningTree.getEdges().size() == modifiableSet.size() - 1) {
                    newCapacity = calculateMaximumDemandOfSubtrees(
                        modifiableSet, spanningTree,
                        currentSolution.getPartitionWeight(label) + demands.get(v1));
                    newWeight = spanningTree.getWeight();
                } else {
                    newCapacity = Double.NaN;
                    newWeight = Double.NaN;
                }
                updateImprovementGraphEdge(
                    vertexOfV1Single, pseudoVertex, newCapacity, newWeight - oldWeight);
                // end edge for v1 vertex replacing no object

                // remove v1 from the set
                modifiableSet.remove(v1);
            }
        }

        /**
         * Updates all edges from <code>vertexOfV1Single</code> to nodes in the subset represented
         * by <code>label</code>. This method does adds the subtree of v1 to
         * <code>modifiableSet</code>.
         *
         * @param currentSolution the current solution in the iteration
         * @param subtrees the mapping from vertices to their subtree
         * @param tabuList the tabu list of the current iteration
         * @param label the current label to update the edges for
         * @param oldWeight the old weight of the subset
         * @param modifiableSet a modifiable version of the subset of nodes represented by label
         * @param pseudoVertex the pseudo vertex representing the subset represented by label
         * @param v1 the vertex to update the nodes in the improvement graph for
         * @param vertexOfV1Subtree the node in the improvement graph representing the exchange of
         *        the subtree rooted at <code>v1</code>
         */
        private void updateSubtreeNode(
            CapacitatedSpanningTreeSolutionRepresentation currentSolution,
            Map<V, Pair<Set<V>, Double>> subtrees, Set<V> tabuList, int label, double oldWeight,
            Set<V> modifiableSet, Pair<Integer, ImprovementGraphVertexType> pseudoVertex, V v1,
            Pair<Integer, ImprovementGraphVertexType> vertexOfV1Subtree)
        {
            double newCapacity, newWeight;
            SpanningTreeAlgorithm.SpanningTree<E> spanningTree;

            /*
             * Adding of edges for v1 subtree replacing an object in v2. We need to considers this
             * only if subtree operations should be used.
             *
             * If the subtree of v1 contains only the vertex itself and both operations are used, it
             * is the same operation as moving v1 as vertex. Thus, do not add edges.
             */
            if (useSubtreeOperation
                && (subtrees.get(v1).getFirst().size() > 1 || !useVertexOperation))
            {

                // add the subtree of v1 to the set for MST calculations
                modifiableSet.addAll(subtrees.get(v1).getFirst());

                for (V v2 : currentSolution.getPartitionSet(label)) {

                    if (v2.equals(root)) {
                        throw new IllegalStateException(
                            "The root is in the partition. This is a bug.");
                    }

                    if (tabuList.contains(v2)) {
                        continue;
                    }

                    /*
                     * edge for v1 subtree replacing v2 vertex
                     */
                    if (useVertexOperation) {
                        modifiableSet.remove(v2);
                        spanningTree = new PrimMinimumSpanningTree<>(
                            new AsSubgraph<>(graph, modifiableSet, graph.edgeSet()))
                                .getSpanningTree();
                        if (spanningTree.getEdges().size() == modifiableSet.size() - 1) {
                            newCapacity = calculateMaximumDemandOfSubtrees(
                                modifiableSet, spanningTree,
                                currentSolution.getPartitionWeight(label)
                                    + subtrees.get(v1).getSecond() - demands.get(v2));
                            newWeight = spanningTree.getWeight();
                        } else {
                            newCapacity = Double.NaN;
                            newWeight = Double.NaN;
                        }
                        updateImprovementGraphEdge(
                            vertexOfV1Subtree,
                            Pair
                                .of(
                                    initialVertexMapping.get(v2),
                                    ImprovementGraphVertexType.SINGLE),
                            newCapacity, newWeight - oldWeight);
                        modifiableSet.add(v2);
                    }
                    // end edge for v1 subtree replacing v2 vertex

                    /*
                     * edge for v1 subtree replacing v2 subtree
                     */
                    modifiableSet.removeAll(subtrees.get(v2).getFirst());
                    spanningTree = new PrimMinimumSpanningTree<>(
                        new AsSubgraph<>(graph, modifiableSet, graph.edgeSet())).getSpanningTree();
                    if (spanningTree.getEdges().size() == modifiableSet.size() - 1) {
                        newCapacity = calculateMaximumDemandOfSubtrees(
                            modifiableSet, spanningTree,
                            currentSolution.getPartitionWeight(currentSolution.getLabel(v2))
                                + subtrees.get(v1).getSecond() - subtrees.get(v2).getSecond());
                        newWeight = spanningTree.getWeight();
                    } else {
                        newCapacity = Double.NaN;
                        newWeight = Double.NaN;
                    }
                    updateImprovementGraphEdge(
                        vertexOfV1Subtree,
                        Pair.of(initialVertexMapping.get(v2), ImprovementGraphVertexType.SUBTREE),
                        newCapacity, newWeight - oldWeight);
                    modifiableSet.addAll(subtrees.get(v2).getFirst());
                    // end edge for v1 subtree replacing v2 subtree
                }

                /*
                 * edge for v1 subtree replacing no object
                 */
                spanningTree = new PrimMinimumSpanningTree<>(
                    new AsSubgraph<>(graph, modifiableSet, graph.edgeSet())).getSpanningTree();
                if (spanningTree.getEdges().size() == modifiableSet.size() - 1) {
                    newCapacity = calculateMaximumDemandOfSubtrees(
                        modifiableSet, spanningTree,
                        currentSolution.getPartitionWeight(label) + subtrees.get(v1).getSecond());
                    newWeight = spanningTree.getWeight();
                } else {
                    newCapacity = Double.NaN;
                    newWeight = Double.NaN;
                }
                updateImprovementGraphEdge(
                    vertexOfV1Subtree, pseudoVertex, newCapacity, newWeight - oldWeight);
                // end edge for v1 subtree replacing no object
            }
        }

        /**
         * Adds an edge between <code>v1</code> and <code>v2</code> to the improvement graph if
         * <code>newCapacity</code> does not exceed the capacity constraint. The weight of the edge
         * is <code>newCost</code>.
         *
         * @param v1 start vertex (the vertex or subtree induced by <code>v1</code> that will be
         *        moved to the subset of <code>v2</code>)
         * @param v2 end vertex (the vertex or subtree induced by <code>v2</code> that will be
         *        removed from the subset of <code>v2</code>)
         * @param newCapacity the used capacity by adding the vertex or subtree induced by
         *        <code>v1</code> to the subset of <code>v2</code> and deleting the vertex or
         *        subtree induced by <code>v2</code>
         * @param newCost the cost of the edge (the cost induced by the operation induced by
         *        <code>v1</code> and <code>v2</code>)
         */
        public void updateImprovementGraphEdge(
            Pair<Integer, ImprovementGraphVertexType> v1,
            Pair<Integer, ImprovementGraphVertexType> v2, double newCapacity, double newCost)
        {
            if (!Double.isNaN(newCapacity) && newCapacity <= capacity && !Double.isNaN(newCost)) {
                DefaultWeightedEdge edge;
                edge = improvementGraph.getEdge(v1, v2);
                if (edge == null) {
                    edge = improvementGraph.addEdge(v1, v2);
                }
                improvementGraph.setEdgeWeight(edge, newCost);
            } else {
                improvementGraph.removeEdge(v1, v2);
            }
        }

        /**
         * Calculates the maximum demand over all new subtrees induced by the minimum spanning tree
         * <code>spanningTree</code>. A spanning tree induces more than one subset in the partition
         * if the root vertex of the base graph connects more than one subtree of the spanning tree.
         *
         * @param vertexSubset the vertex subset <code>spanning Tree is defined on</code>
         * @param spanningTree the spanning tree
         * @param totalDemand the total demand of the whole spanning tree
         * @return the maximum demand over all new subtrees induced by the minimum spanning tree
         *         <code>spanningTree</code>
         */
        public double calculateMaximumDemandOfSubtrees(
            Set<V> vertexSubset, SpanningTreeAlgorithm.SpanningTree<E> spanningTree,
            double totalDemand)
        {

            Graph<V, E> spanningTreeGraph =
                new AsSubgraph<>(graph, vertexSubset, spanningTree.getEdges());

            /*
             * The subtree does not evolve to more than 1 partition subsets, thus, we can return the
             * total demand.
             */
            int degreeOfRoot = spanningTreeGraph.degreeOf(root);
            if (degreeOfRoot == 1) {
                return totalDemand;
            }

            double maximumDemand = 0;

            DepthFirstIterator<V, E> depthFirstIterator =
                new DepthFirstIterator<>(spanningTreeGraph, root);
            if (depthFirstIterator.hasNext()) {
                depthFirstIterator.next();
            }

            int numberOfRootEdgesExplored = 0;

            double exploredVerticesDemand = 0;
            double currentDemand = 0;

            while (depthFirstIterator.hasNext()) {
                V next = depthFirstIterator.next();

                // exploring new subtree
                if (spanningTreeGraph.containsEdge(root, next)) {

                    exploredVerticesDemand += currentDemand;

                    if (maximumDemand < currentDemand) {
                        maximumDemand = currentDemand;
                    }

                    // we can stop the exploration
                    if (maximumDemand >= 0.5 * totalDemand
                        || exploredVerticesDemand + maximumDemand >= totalDemand)
                    {
                        return maximumDemand;
                    }

                    // we can stop the exploration, all subtrees but one are explored
                    if (numberOfRootEdgesExplored + 1 == degreeOfRoot) {
                        return Math.max(maximumDemand, totalDemand - exploredVerticesDemand);
                    }

                    numberOfRootEdgesExplored++;

                    currentDemand = 0;
                }

                currentDemand += demands.get(next);
            }

            return maximumDemand;
        }

        /**
         * Returns the mapping that is used in the valid cycle detection algorithm, i.e. the vertex
         * label map.
         *
         * @return the vertex label map used in the valid cycle detection algorithm
         */
        private Map<Pair<Integer, ImprovementGraphVertexType>,
            Integer> getImprovementGraphLabelMap()
        {
            return new AbstractMap<Pair<Integer, ImprovementGraphVertexType>, Integer>()
            {
                @Override
                public int size()
                {
                    return improvementGraphVertexMapping.size() + pathExchangeVertexMapping.size()
                        + (origin == null ? 0 : 1);
                }

                @Override
                public boolean isEmpty()
                {
                    return improvementGraphVertexMapping.isEmpty()
                        && pathExchangeVertexMapping.isEmpty() && origin == null;
                }

                @Override
                public boolean containsKey(Object key)
                {
                    if (key instanceof Pair) {
                        return improvementGraphVertexMapping.containsKey(((Pair) key).getFirst())
                            || pathExchangeVertexMapping.containsKey(key) || key.equals(origin);
                    }
                    return false;
                }

                @Override
                public boolean containsValue(Object value)
                {
                    return improvementGraphVertexMapping.containsValue(value)
                        || pathExchangeVertexMapping.containsValue(value)
                        || value.equals(originVertexLabel);
                }

                @Override
                public Integer get(Object key)
                {
                    if (key instanceof Pair) {
                        if (improvementGraphVertexMapping.containsKey(((Pair) key).getFirst())) {
                            return capacitatedSpanningTreeSolutionRepresentation
                                .getLabel(
                                    improvementGraphVertexMapping.get(((Pair) key).getFirst()));
                        }
                        if (key.equals(origin)) {
                            return originVertexLabel;
                        }
                    }
                    return pathExchangeVertexMapping.get(key);
                }

                @Override
                public Integer put(Pair<Integer, ImprovementGraphVertexType> key, Integer value)
                {
                    throw new IllegalStateException();
                }

                @Override
                public Integer remove(Object key)
                {
                    throw new IllegalStateException();
                }

                @Override
                public void putAll(
                    Map<? extends Pair<Integer, ImprovementGraphVertexType>, ? extends Integer> m)
                {
                    throw new IllegalStateException();
                }

                @Override
                public void clear()
                {
                    throw new IllegalStateException();
                }

                @Override
                public Set<Pair<Integer, ImprovementGraphVertexType>> keySet()
                {
                    Set<Pair<Integer, ImprovementGraphVertexType>> keySet = new HashSet<>();
                    for (Integer i : improvementGraphVertexMapping.keySet()) {
                        if (useVertexOperation) {
                            keySet.add(Pair.of(i, ImprovementGraphVertexType.SINGLE));
                        }
                        if (useSubtreeOperation) {
                            keySet.add(Pair.of(i, ImprovementGraphVertexType.SUBTREE));
                        }
                    }
                    keySet.addAll(pathExchangeVertexMapping.keySet());
                    keySet.add(origin);
                    return keySet;
                }

                @Override
                public Collection<Integer> values()
                {
                    return capacitatedSpanningTreeSolutionRepresentation.getLabels();
                }

                @Override
                public Set<Entry<Pair<Integer, ImprovementGraphVertexType>, Integer>> entrySet()
                {

                    Set<Entry<Pair<Integer, ImprovementGraphVertexType>, Integer>> entrySet =
                        new HashSet<>();
                    for (Integer i : improvementGraphVertexMapping.keySet()) {
                        Integer label = capacitatedSpanningTreeSolutionRepresentation
                            .getLabel(improvementGraphVertexMapping.get(i));
                        if (useVertexOperation) {
                            entrySet
                                .add(
                                    new AbstractMap.SimpleEntry<>(
                                        Pair.of(i, ImprovementGraphVertexType.SINGLE), label));
                        }
                        if (useSubtreeOperation) {
                            entrySet
                                .add(
                                    new AbstractMap.SimpleEntry<>(
                                        Pair.of(i, ImprovementGraphVertexType.SUBTREE), label));
                        }
                    }
                    for (Pair<Integer,
                        ImprovementGraphVertexType> pseudoVertex : pathExchangeVertexMapping
                            .keySet())
                    {
                        entrySet
                            .add(
                                new AbstractMap.SimpleEntry<>(
                                    pseudoVertex, pathExchangeVertexMapping.get(pseudoVertex)));
                    }
                    entrySet.add(new AbstractMap.SimpleEntry<>(origin, originVertexLabel));
                    return entrySet;
                }
            };
        }
    }
}
