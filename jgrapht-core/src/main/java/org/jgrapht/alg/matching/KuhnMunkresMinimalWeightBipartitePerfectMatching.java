/*
 * (C) Copyright 2013-2018, by Alexey Kudinkin and Contributors.
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
package org.jgrapht.alg.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

import java.util.*;

/**
 * Kuhn-Munkres algorithm (named in honor of Harold Kuhn and James Munkres) solving <i>assignment
 * problem</i> also known as <a href=http://en.wikipedia.org/wiki/Hungarian_algorithm>hungarian
 * algorithm</a> (in the honor of hungarian mathematicians Dénes K?nig and Jen? Egerváry). It's
 * running time $O(V^3)$.
 *
 * <p>
 * <i>Assignment problem</i> could be set as follows:
 *
 * <p>
 * Given <a href=http://en.wikipedia.org/wiki/Complete_bipartite_graph> complete bipartite graph</a>
 * $G = (S, T; E)$, such that $|S| = |T|$, and each edge has <i>non-negative</i> cost <i>c(i,
 * j)</i>, find <i>perfect</i> matching of <i>minimal cost</i>.
 * </p>
 * 
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Alexey Kudinkin
 */
public class KuhnMunkresMinimalWeightBipartitePerfectMatching<V, E>
    implements
    MatchingAlgorithm<V, E>
{
    private final Graph<V, E> graph;
    private Set<? extends V> partition1;
    private Set<? extends V> partition2;

    /**
     * Construct a new instance of the algorithm.
     * 
     * @param graph the input graph
     * @param partition1 the first partition of the vertex set
     * @param partition2 the second partition of the vertex set
     */
    public KuhnMunkresMinimalWeightBipartitePerfectMatching(
        Graph<V, E> graph, Set<? extends V> partition1, Set<? extends V> partition2)
    {
        if (graph == null) {
            throw new IllegalArgumentException("Input graph cannot be null");
        }
        this.graph = graph;
        if (partition1 == null) {
            throw new IllegalArgumentException("Partition 1 cannot be null");
        }
        this.partition1 = partition1;
        if (partition2 == null) {
            throw new IllegalArgumentException("Partition 2 cannot be null");
        }
        this.partition2 = partition2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matching<V, E> getMatching()
    {
        // Validate graph being complete bipartite with equally-sized partitions
        if (partition1.size() != partition2.size()) {
            throw new IllegalArgumentException(
                "Graph supplied isn't complete bipartite with equally sized partitions!");
        }

        if (!GraphTests.isBipartitePartition(graph, partition1, partition2)) {
            throw new IllegalArgumentException("Invalid bipartite partition provided");
        }

        int partition = partition1.size();
        int edges = graph.edgeSet().size();
        if (edges != (partition * partition)) {
            throw new IllegalArgumentException(
                "Graph supplied isn't complete bipartite with equally sized partitions!");
        }

        if (!GraphTests.isSimple(graph)) {
            throw new IllegalArgumentException("Only simple graphs supported");
        }

        List<? extends V> firstPartition = new ArrayList<>(partition1);
        List<? extends V> secondPartition = new ArrayList<>(partition2);

        // Expected behavior for an empty graph is to return an empty set, so
        // we check this last
        int[] matching;
        if (graph.vertexSet().isEmpty()) {
            matching = new int[] {};
        } else {
            matching = new KuhnMunkresMatrixImplementation<>(graph, firstPartition, secondPartition)
                .buildMatching();
        }

        Set<E> edgeSet = new HashSet<>();
        double weight = 0d;
        for (int i = 0; i < matching.length; ++i) {
            E e = graph.getEdge(firstPartition.get(i), secondPartition.get(matching[i]));
            weight += graph.getEdgeWeight(e);
            edgeSet.add(e);
        }

        return new MatchingImpl<>(graph, edgeSet, weight);
    }

    /**
     * The actual implementation.
     */
    static class KuhnMunkresMatrixImplementation<V, E>
    {
        /**
         * Cost matrix
         */
        private double[][] costMatrix;

        /**
         * Excess matrix
         */
        private double[][] excessMatrix;

        /**
         * Rows coverage bit-mask
         */
        boolean[] rowsCovered;

        /**
         * Columns coverage bit-mask
         */
        boolean[] columnsCovered;

        /**
         * ``columnMatched[i]'' is the column # of the ZERO matched at the $i$-th row
         */
        private int[] columnMatched;

        /**
         * ``rowMatched[j]'' is the row # of the ZERO matched at the $j$-th column
         */
        private int[] rowMatched;

        /**
         * Construct new instance
         * 
         * @param G the input graph
         * @param S first partition of the vertex set
         * @param T second partition of the vertex set
         */
        public KuhnMunkresMatrixImplementation(
            final Graph<V, E> G, final List<? extends V> S, final List<? extends V> T)
        {
            int partition = S.size();

            // Build an excess-matrix corresponding to the supplied weighted
            // complete bipartite graph

            costMatrix = new double[partition][];

            for (int i = 0; i < S.size(); ++i) {
                V source = S.get(i);
                costMatrix[i] = new double[partition];
                for (int j = 0; j < T.size(); ++j) {
                    V target = T.get(j);
                    if (source.equals(target)) {
                        continue;
                    }
                    costMatrix[i][j] = G.getEdgeWeight(G.getEdge(source, target));
                }
            }
        }

        /**
         * Gets costs-matrix as input and returns assignment of tasks (designated by the columns of
         * cost-matrix) to the workers (designated by the rows of the cost-matrix) so that to
         * MINIMIZE total tasks-tackling costs
         * 
         * @return assignment of tasks
         */
        protected int[] buildMatching()
        {
            int height = costMatrix.length, width = costMatrix[0].length;

            // Make an excess-matrix
            excessMatrix = makeExcessMatrix();

            // Build row/column coverage
            rowsCovered = new boolean[height];
            columnsCovered = new boolean[width];

            // Cached values of zeroes' indices in particular columns/rows
            columnMatched = new int[height];
            rowMatched = new int[width];

            Arrays.fill(columnMatched, -1);
            Arrays.fill(rowMatched, -1);

            // Find perfect matching corresponding to the optimal assignment

            while (buildMaximalMatching() < width) {
                buildVertexCoverage();
                extendEqualityGraph();
            }

            // Almost done!

            return Arrays.copyOf(columnMatched, height);
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * Composes excess-matrix corresponding to the given cost-matrix
         */
        double[][] makeExcessMatrix()
        {
            double[][] excessMatrix = new double[costMatrix.length][];

            for (int i = 0; i < excessMatrix.length; ++i) {
                excessMatrix[i] = Arrays.copyOf(costMatrix[i], costMatrix[i].length);
            }

            // Subtract minimal costs across the rows

            for (int i = 0; i < excessMatrix.length; ++i) {
                double cheapestTaskCost = Double.MAX_VALUE;

                for (int j = 0; j < excessMatrix[i].length; ++j) {
                    if (cheapestTaskCost > excessMatrix[i][j]) {
                        cheapestTaskCost = excessMatrix[i][j];
                    }
                }

                for (int j = 0; j < excessMatrix[i].length; ++j) {
                    excessMatrix[i][j] -= cheapestTaskCost;
                }
            }

            // Subtract minimal costs across the columns
            //
            // NOTE: Makes nothing if there is any worker that can more
            // (cost-)effectively tackle this task than any other, i.e. there
            // is any row having zero in this column. However, if there is no
            // one, reduce the cost-demands of each worker to the size of the
            // min-cost (we can easily do this, since we have particular
            // interest of the relative values only), i.e. subtract the value
            // of the minimum cost-demands to highlight (with zero) the
            // worker that can tackle this task demanding lowest reward.

            for (int j = 0; j < excessMatrix[0].length; ++j) {
                double cheapestWorkerCost = Double.MAX_VALUE;

                for (int i = 0; i < excessMatrix.length; ++i) {
                    if (cheapestWorkerCost > excessMatrix[i][j]) {
                        cheapestWorkerCost = excessMatrix[i][j];
                    }
                }

                for (int i = 0; i < excessMatrix.length; ++i) {
                    excessMatrix[i][j] -= cheapestWorkerCost;
                }
            }

            return excessMatrix;
        }

        /**
         * Builds maximal matching corresponding to the given excess-matrix
         *
         * @return size of a maximal matching built
         */
        int buildMaximalMatching()
        {
            // Match all zeroes non-staying in the same column/row

            int matchingSizeLowerBound = 0;

            for (int i = 0; i < columnMatched.length; ++i) {
                if (columnMatched[i] != -1) {
                    ++matchingSizeLowerBound;
                }
            }

            // Compose first-approximation by matching zeroes in a greed fashion

            for (int j = 0; j < excessMatrix[0].length; ++j) {
                if (rowMatched[j] == -1) {
                    for (int i = 0; i < excessMatrix.length; ++i) {
                        if ((excessMatrix[i][j] == 0) && (columnMatched[i] == -1)) {
                            ++matchingSizeLowerBound;
                            columnMatched[i] = j;
                            rowMatched[j] = i;
                            break;
                        }
                    }
                }
            }

            // Check whether perfect matching is found

            if (matchingSizeLowerBound == excessMatrix[0].length) {
                return matchingSizeLowerBound;
            }

            //
            // As to E. Burge: Matching is maximal iff bipartite graph doesn't
            // contain any matching-augmenting paths.
            //
            // Try to find any match-augmenting path

            boolean[] rowsVisited = new boolean[excessMatrix.length];
            boolean[] colsVisited = new boolean[excessMatrix[0].length];

            int matchingSize = 0;

            boolean extending = true;

            while (extending && (matchingSize < excessMatrix.length)) {
                Arrays.fill(rowsVisited, false);
                Arrays.fill(colsVisited, false);

                extending = false;

                for (int j = 0; j < excessMatrix.length; ++j) {
                    if ((rowMatched[j] == -1) && !colsVisited[j]) {
                        extending |= new MatchExtender(rowsVisited, colsVisited)
                            .extend(j); /* Try to extend matching */
                    }
                }

                matchingSize = 0;

                for (int j = 0; j < rowMatched.length; ++j) {
                    if (rowMatched[j] != -1) {
                        ++matchingSize;
                    }
                }
            }

            return matchingSize;
        }

        /**
         * Builds vertex-cover given built up matching
         */
        void buildVertexCoverage()
        {
            Arrays.fill(columnsCovered, false);
            Arrays.fill(rowsCovered, false);

            boolean[] invertible = new boolean[rowsCovered.length];

            for (int i = 0; i < excessMatrix.length; ++i) {
                if (columnMatched[i] != -1) {
                    invertible[i] = true;
                    continue;
                }

                for (int j = 0; j < excessMatrix[i].length; ++j) {
                    if (Double.compare(excessMatrix[i][j], 0.) == 0) {
                        rowsCovered[i] = invertible[i] = true;
                        break;
                    }
                }
            }

            boolean cont = true;

            while (cont) {
                for (int i = 0; i < excessMatrix.length; ++i) {
                    if (rowsCovered[i]) {
                        for (int j = 0; j < excessMatrix[i].length; ++j) {
                            if ((Double.compare(excessMatrix[i][j], 0.) == 0)
                                && !columnsCovered[j])
                            {
                                columnsCovered[j] = true;
                            }
                        }
                    }
                }

                // Shall we continue?

                cont = false;

                for (int j = 0; j < columnsCovered.length; ++j) {
                    if (columnsCovered[j] && (rowMatched[j] != -1)) {
                        if (!rowsCovered[rowMatched[j]]) {
                            cont = true;
                            rowsCovered[rowMatched[j]] = true;
                        }
                    }
                }
            }

            // Invert covered rows selection

            for (int i = 0; i < rowsCovered.length; ++i) {
                if (invertible[i]) {
                    rowsCovered[i] ^= true;
                }
            }

            assert uncovered(excessMatrix, rowsCovered, columnsCovered) == 0;
            assert minimal(rowMatched, rowsCovered, columnsCovered);
        }

        /**
         * Extends equality-graph subtracting minimal excess from all the COLUMNS UNCOVERED and
         * adding it to the all ROWS COVERED
         */
        void extendEqualityGraph()
        {
            double minExcess = Double.MAX_VALUE;

            for (int i = 0; i < excessMatrix.length; ++i) {
                if (rowsCovered[i]) {
                    continue;
                }
                for (int j = 0; j < excessMatrix[i].length; ++j) {
                    if (columnsCovered[j]) {
                        continue;
                    }
                    if (minExcess > excessMatrix[i][j]) {
                        minExcess = excessMatrix[i][j];
                    }
                }
            }

            // Add up to all elements of the COVERED ROWS

            for (int i = 0; i < excessMatrix.length; ++i) {
                if (!rowsCovered[i]) {
                    continue;
                }
                for (int j = 0; j < excessMatrix[i].length; ++j) {
                    excessMatrix[i][j] += minExcess;
                }
            }

            // Subtract from all elements of the UNCOVERED COLUMNS

            for (int j = 0; j < excessMatrix[0].length; ++j) {
                if (columnsCovered[j]) {
                    continue;
                }
                for (int i = 0; i < excessMatrix.length; ++i) {
                    excessMatrix[i][j] -= minExcess;
                }
            }
        }

        /**
         * Assures given column-n-rows-coverage/zero-matching to be minimal/maximal
         *
         * @param match zero-matching to check
         * @param rowsCovered rows coverage to check
         * @param colsCovered columns coverage to check
         *
         * @return true if given matching and coverage are maximal and minimal respectively
         */
        private static boolean minimal(
            final int[] match, final boolean[] rowsCovered, final boolean[] colsCovered)
        {
            int matched = 0;
            for (int i = 0; i < match.length; ++i) {
                if (match[i] != -1) {
                    ++matched;
                }
            }

            int covered = 0;
            for (int i = 0; i < rowsCovered.length; ++i) {
                if (rowsCovered[i]) {
                    ++covered;
                }
                if (colsCovered[i]) {
                    ++covered;
                }
            }

            return matched == covered;
        }

        /**
         * Accounts for zeroes being uncovered
         *
         * @param excessMatrix target excess-matrix
         * @param rowsCovered rows coverage to check
         * @param colsCovered columns coverage to check
         */
        private static int uncovered(
            final double[][] excessMatrix, final boolean[] rowsCovered, final boolean[] colsCovered)
        {
            int uncoveredZero = 0;

            for (int i = 0; i < excessMatrix.length; ++i) {
                if (rowsCovered[i]) {
                    continue;
                }
                for (int j = 0; j < excessMatrix[i].length; ++j) {
                    if (colsCovered[j]) {
                        continue;
                    }
                    if (Double.compare(excessMatrix[i][j], 0.) == 0) {
                        ++uncoveredZero;
                    }
                }
            }

            return uncoveredZero;
        }

        /**
         * Aggregates utilities to extend matching
         */
        protected class MatchExtender
        {
            private final boolean[] rowsVisited;
            private final boolean[] colsVisited;

            private MatchExtender(final boolean[] rowsVisited, final boolean[] colsVisited)
            {
                this.rowsVisited = rowsVisited;
                this.colsVisited = colsVisited;
            }

            /**
             * Performs DFS to seek after matching-augmenting path starting at the initial-vertex
             *
             * @param initialCol column # of initial-vertex
             * @return true when some augmenting-path found, false otherwise
             */
            public boolean extend(int initialCol)
            {
                return extendMatchingEL(initialCol);
            }

            /**
             * DFS helper #1 (applicable for ODD-LENGTH paths ONLY)
             *
             * @param pathTailRow row # of tail of the matching-augmenting path
             * @param pathTailCol column # of tail of the matching-augmenting path
             *
             * @return true if matching-augmenting path found, false otherwise
             */
            private boolean extendMatchingOL(int pathTailRow, int pathTailCol)
            {
                // Seek after already matched zero

                // Check whether row occupied by the 'tail' vertex isn't matched

                if (columnMatched[pathTailRow] == -1) {
                    // There is no way to continue: mark zero as matched,
                    // trigger along-side flipping
                    columnMatched[pathTailRow] = pathTailCol;
                    rowMatched[pathTailCol] = pathTailRow;

                    return true;
                } else {
                    // Add next matched zero: mark current vertex as "visited"
                    rowsVisited[pathTailRow] = true;

                    // Check whether we can proceed
                    if (colsVisited[columnMatched[pathTailRow]]) {
                        return false;
                    }

                    boolean extending = extendMatchingEL(columnMatched[pathTailRow]);

                    if (extending) {
                        columnMatched[pathTailRow] = pathTailCol;
                        rowMatched[pathTailCol] = pathTailRow;
                    }

                    return extending;
                }
            }

            /**
             * DFS helper #1 (applicable for ODD-LENGTH paths ONLY)
             *
             * @param pathTailCol column # of tail of the matching-augmenting path
             *
             * @return true if matching-augmenting path found, false otherwise
             */
            private boolean extendMatchingEL(int pathTailCol)
            {
                colsVisited[pathTailCol] = true;

                for (int i = 0; i < excessMatrix.length; ++i) {
                    if ((excessMatrix[i][pathTailCol] == 0) && !rowsVisited[i]) {
                        boolean extending = extendMatchingOL(
                            i, // New tail to continue
                            pathTailCol //
                        );
                        if (extending) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }
    }
}
