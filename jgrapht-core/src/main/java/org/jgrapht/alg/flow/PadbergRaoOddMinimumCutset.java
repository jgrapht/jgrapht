/*
 * (C) Copyright 2016-2018, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.flow;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Implementation of the algorithm by Padberg and Rao to compute Odd Minimum Cut-Sets. Let $G=(V,E)$
 * be an undirected, simple weighted graph, where all edge weights are positive. Let $T \subset V$
 * with $|T|$ even, be a set of vertices that are labelled <i>odd</i>. A cut-set $(U:V-U)$ is called
 * odd if $|T \cap U|$ is an odd number. Let $c(U:V-U)$ be the weight of the cut, that is, the sum
 * of weights of the edges which have exactly one endpoint in $U$ and one endpoint in $V-U$. The
 * problem of finding an odd minimum cut-set in $G$ is stated as follows: Find $W \subseteq V$ such
 * that $c(W:V-W)=min(c(U:V-U)|U \subseteq V, |T \cap U|$ is odd).
 *
 * <p>
 * The algorithm has been published in: Padberg, M. Rao, M. Odd Minimum Cut-Sets and b-Matchings.
 * Mathematics of Operations Research, 7(1), p67-80, 1982. A more concise description is published
 * in: Letchford, A. Reinelt, G. Theis, D. Odd minimum cut-sets and b-matchings revisited. SIAM
 * Journal of Discrete Mathematics, 22(4), p1480-1487, 2008.
 *
 * <p>
 * The runtime complexity of this algorithm is dominated by the runtime complexity of the algorithm
 * used to compute A Gomory-Hu tree on graph $G$. Consequently, the runtime complexity of this class
 * is $O(V^4)$.
 *
 * <p>
 * This class does not support changes to the underlying graph. The behavior of this class is
 * undefined when the graph is modified after instantiating this class.
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Joris Kinable
 */
public class PadbergRaoOddMinimumCutset<V, E>
{

    /* Input graph */
    private final Graph<V, E> network;
    /* Set of vertices which are labeled 'odd' (set T in the paper) */
    private Set<V> oddVertices;
    /* Algorithm used to calculate the Gomory-Hu Cut-tree */
    private final GusfieldGomoryHuCutTree<V, E> gusfieldGomoryHuCutTreeAlgorithm;
    /* The Gomory-Hu tree */
    private SimpleWeightedGraph<V, DefaultWeightedEdge> gomoryHuTree;

    /* Weight of the minimum odd cut-set */
    private double minimumCutWeight = Double.MAX_VALUE;
    /* Source partition constituting the minimum odd cut-set */
    private Set<V> sourcePartitionMinimumCut;

    /**
     * Creates a new instance of the PadbergRaoOddMinimumCutset algorithm.
     *
     * @param network input graph
     */
    public PadbergRaoOddMinimumCutset(Graph<V, E> network)
    {
        this(network, MaximumFlowAlgorithmBase.DEFAULT_EPSILON);
    }

    /**
     * Creates a new instance of the PadbergRaoOddMinimumCutset algorithm.
     *
     * @param network input graph
     * @param epsilon tolerance
     */
    public PadbergRaoOddMinimumCutset(Graph<V, E> network, double epsilon)
    {
        this(network, new PushRelabelMFImpl<>(network, epsilon));
    }

    /**
     * Creates a new instance of the PadbergRaoOddMinimumCutset algorithm.
     *
     * @param network input graph
     * @param minimumSTCutAlgorithm algorithm used to calculate the Gomory-Hu tree
     */
    public PadbergRaoOddMinimumCutset(
        Graph<V, E> network, MinimumSTCutAlgorithm<V, E> minimumSTCutAlgorithm)
    {
        this.network = GraphTests.requireUndirected(network);
        gusfieldGomoryHuCutTreeAlgorithm =
            new GusfieldGomoryHuCutTree<>(network, minimumSTCutAlgorithm);
    }

    /**
     * Calculates the minimum odd cut. The implementation follows Algorithm 1 in the paper Odd
     * minimum cut sets and b-matchings revisited by Adam Letchford, Gerhard Reinelt and Dirk Theis.
     * The original algorithm runs on a compressed Gomory-Hu tree: a cut-tree with the odd vertices
     * as terminal vertices. This tree has $|T|-1$ edges as opposed to $|V|-1$ for a Gomory-Hu tree
     * defined on the input graph $G$. This compression step can however be skipped. If you want to
     * run the original algorithm in the paper, set the parameter <code>useTreeCompression</code> to
     * true. Alternatively, experiment which setting of this parameter produces the fastest results.
     * Both settings are guaranteed to find the optimal cut. Experiments on random graphs showed
     * that setting <code>useTreeCompression</code> to false was on average a bit faster.
     *
     * @param oddVertices Set of vertices which are labeled 'odd'. Note that the number of vertices
     *        in this set must be even!
     * @param useTreeCompression parameter indicating whether tree compression should be used
     *        (recommended: false).
     * @return weight of the minimum odd cut.
     */
    public double calculateMinCut(Set<V> oddVertices, boolean useTreeCompression)
    {
        minimumCutWeight = Double.MAX_VALUE;
        this.oddVertices = oddVertices;

        if (oddVertices.size() % 2 == 1)
            throw new IllegalArgumentException("There needs to be an even number of odd vertices");
        assert network.vertexSet().containsAll(oddVertices); // All odd vertices must be contained
        // in the graph
        // all edge weights mucht be non-negative
        assert network.edgeSet().stream().noneMatch(e -> network.getEdgeWeight(e) < 0);

        gomoryHuTree = gusfieldGomoryHuCutTreeAlgorithm.getGomoryHuTree();

        if (useTreeCompression)
            return calculateMinCutWithTreeCompression();
        else
            return calculateMinCutWithoutTreeCompression();
    }

    /**
     * Modified implementation of the algorithm proposed in Odd Minimum Cut-sets and b-matchings by
     * Padberg and Rao. The optimal cut is directly computed on the Gomory-Hu tree computed for
     * graph $G$. This approach iterates efficiently over all possible cuts of the graph (there are
     * $|V|$ such cuts).
     *
     * @return weight of the minimum odd cut.
     */
    private double calculateMinCutWithoutTreeCompression()
    {
        Set<DefaultWeightedEdge> edges = new LinkedHashSet<>(gomoryHuTree.edgeSet());
        for (DefaultWeightedEdge edge : edges) {
            V source = gomoryHuTree.getEdgeSource(edge);
            V target = gomoryHuTree.getEdgeTarget(edge);
            double edgeWeight = gomoryHuTree.getEdgeWeight(edge);

            if (edgeWeight >= minimumCutWeight)
                continue;

            gomoryHuTree.removeEdge(edge); // Temporarily remove edge
            Set<V> sourcePartition =
                new ConnectivityInspector<>(gomoryHuTree).connectedSetOf(source);
            if (PadbergRaoOddMinimumCutset.isOddVertexSet(sourcePartition, oddVertices)) { // If the
                                                                                           // source
                                                                                           // partition
                                                                                           // forms
                                                                                           // an odd
                                                                                           // cutset,
                                                                                           // check
                                                                                           // whether
                                                                                           // the
                                                                                           // cut
                                                                                           // isn't
                                                                                           // better
                                                                                           // than
                                                                                           // the
                                                                                           // one we
                                                                                           // already
                                                                                           // found.
                minimumCutWeight = edgeWeight;
                sourcePartitionMinimumCut = sourcePartition;
            }
            gomoryHuTree.addEdge(source, target, edge); // Place edge back
        }
        return minimumCutWeight;
    }

    /**
     * Implementation of the algorithm proposed in Odd Minimum Cut-sets and b-matchings by Padberg
     * and Rao. The algorithm evaluates at most $|T|$ cuts in the Gomory-Hu tree.
     *
     * @return weight of the minimum odd cut.
     */
    private double calculateMinCutWithTreeCompression()
    {
        Queue<Set<V>> queue = new LinkedList<>();
        queue.add(oddVertices);

        // Keep splitting the clusters until each resulting cluster containes exactly one vertex.
        while (!queue.isEmpty()) {
            Set<V> nextCluster = queue.poll();
            this.splitCluster(nextCluster, queue);
        }

        return minimumCutWeight;
    }

    /**
     * Takes a set of odd vertices with cardinality $2$ or more, and splits them into $2$ new
     * non-empty sets.
     * 
     * @param cluster group of odd vertices
     * @param queue clusters with cardinality $2$ or more
     */
    private void splitCluster(Set<V> cluster, Queue<Set<V>> queue)
    {
        assert cluster.size() >= 2;

        // Choose 2 random odd nodes
        Iterator<V> iterator = cluster.iterator();
        V oddNode1 = iterator.next();
        V oddNode2 = iterator.next();

        // Calculate the minimum cut separating these two nodes.
        double cutWeight = gusfieldGomoryHuCutTreeAlgorithm.calculateMinCut(oddNode1, oddNode2);
        Set<V> sourcePartition = null;

        if (cutWeight < minimumCutWeight) {
            sourcePartition = gusfieldGomoryHuCutTreeAlgorithm.getSourcePartition();
            if (PadbergRaoOddMinimumCutset.isOddVertexSet(sourcePartition, oddVertices)) {
                this.minimumCutWeight = cutWeight;
                this.sourcePartitionMinimumCut = sourcePartition;
            }
        }

        if (cluster.size() == 2)
            return;

        if (sourcePartition == null)
            sourcePartition = gusfieldGomoryHuCutTreeAlgorithm.getSourcePartition();

        Set<V> split1 = this.intersection(cluster, sourcePartition);
        Set<V> split2 = new HashSet<>(cluster);
        split2.removeAll(split1);

        if (split1.size() > 1)
            queue.add(split1);
        if (split2.size() > 1)
            queue.add(split2);
    }

    /**
     * Efficient way to compute the intersection between two sets
     * 
     * @param set1 set $1$
     * @param set2 set $2$
     * @return intersection of set $1$ and $2$
     */
    private Set<V> intersection(Set<V> set1, Set<V> set2)
    {
        Set<V> a;
        Set<V> b;
        if (set1.size() <= set2.size()) {
            a = set1;
            b = set2;
        } else {
            a = set2;
            b = set1;
        }

        return a.stream().filter(b::contains).collect(Collectors.toSet());
    }

    /**
     * Convenience method which test whether the given set contains an odd number of odd-labeled
     * nodes.
     *
     * @param <V> vertex type
     * @param vertices input set
     * @param oddVertices subset of vertices which are labeled odd
     * @return true if the given set contains an odd number of odd-labeled nodes.
     */
    public static <V> boolean isOddVertexSet(Set<V> vertices, Set<V> oddVertices)
    {
        if (vertices.size() < oddVertices.size())
            return vertices.stream().filter(oddVertices::contains).count() % 2 == 1;
        else
            return oddVertices.stream().filter(vertices::contains).count() % 2 == 1;
    }

    /**
     * Returns partition $W$ of the cut obtained after the last invocation of
     * {@link #calculateMinCut(Set, boolean)}
     *
     * @return partition $W$
     */
    public Set<V> getSourcePartition()
    {
        return sourcePartitionMinimumCut;
    }

    /**
     * Returns partition $V-W$ of the cut obtained after the last invocation of
     * {@link #calculateMinCut(Set, boolean)}
     *
     * @return partition $V-W$
     */
    public Set<V> getSinkPartition()
    {
        Set<V> sinkPartition = new LinkedHashSet<>(network.vertexSet());
        sinkPartition.removeAll(sourcePartitionMinimumCut);
        return sinkPartition;
    }

    /**
     * Returns the set of edges which run from the source partition to the sink partition, in the
     * $s-t$ cut obtained after the last invocation of {@link #calculateMinCut(Set, boolean)}
     *
     * @return set of edges which have one endpoint in the source partition and one endpoint in the
     *         sink partition.
     */
    public Set<E> getCutEdges()
    {
        Predicate<E> predicate = e -> sourcePartitionMinimumCut.contains(network.getEdgeSource(e))
            ^ sourcePartitionMinimumCut.contains(network.getEdgeTarget(e));
        return network.edgeSet().stream().filter(predicate).collect(
            Collectors.toCollection(LinkedHashSet::new));
    }

}
