/*
 * (C) Copyright 2026-2026, by Fotis Karagiannis and Contributors.
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
package org.jgrapht.alg.clustering;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.ClusteringAlgorithm;
import org.jgrapht.generate.EgonetGraphGenerator;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.util.ConcurrencyUtil;
import java.util.*;
import java.util.concurrent.*;

/**
 * Implementation of the Overlapping Clustering Algorithm.
 *
 * <p>
 * The algorithm is described in the paper: Bradley S. Rees & Keith B. Gallagher (2010),
 * Overlapping Community Detection by Collective Friendship Group Inference.
 *
 * <p>
 * The algorithm is split into three distinct parts: finding friendship groups, removing proper subsets,
 * and finally merging close sets.
 *
 * <p>
 * Friendship groups are found by generating the egonet graphs of each vertex.
 *
 * <p>
 * Two sets are considered close when, with |B| >= |A|, the condition |A| - |A ∩ B| <= 1 holds.
 *
 * <p>
 * Parallel execution is supported for finding friendship groups and removing proper subsets.
 *
 * @author Fotis Karagiannis
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class OverlappingClustering<V, E> implements ClusteringAlgorithm<V>
{
    /**
     * Default parallelism value
     */
    public static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();
    /**
     * Default constructor radius
     */
    public static final int DEFAULT_RADIUS = 1;

    private final Graph<V, E> graph;
    private final int parallelism;
    private final ThreadPoolExecutor executor;
    private final int radius;

    // Access to friendshipGroups for testing & parallelization purposes
    // set used to avoid duplicates
    private final List<Set<V>> friendshipGroups;
    private final List<Set<V>> superGroups;
    private List<Set<V>> mergedGroups;

    // Get methods only exist for testing purposes
    List<Set<V>> getFriendshipGroups()
    {
        return friendshipGroups;
    }
    List<Set<V>> getSuperGroups()
    {
        return superGroups;
    }

    /**
     * Create a new clustering algorithm
     *
     * @param graph the given graph
     * @param executor the executor that will be used for parallelization
     * @param radius the limit on path length for the egonets
     */
    public OverlappingClustering(Graph<V, E> graph, ThreadPoolExecutor executor, int radius)
    {
        this.graph = Objects.requireNonNull(graph);
        this.parallelism = executor.getMaximumPoolSize();
        this.executor = executor;
        this.radius = radius;
        this.friendshipGroups = new ArrayList<>();
        this.superGroups = new ArrayList<>();
        this.mergedGroups = new ArrayList<>();
    }

    public OverlappingClustering(Graph<V, E> graph, int parallelism, int radius)
    {
        this(graph, ConcurrencyUtil.createThreadPoolExecutor(parallelism), radius);
    }

    /**
     * Create a new clustering algorithm with default radius and parallelism
     *
     * @param graph the given graph
     * @param k the desired number of clusters
     */
    public OverlappingClustering(Graph<V, E> graph, int k)
    {
        this(graph, DEFAULT_PARALLELISM, DEFAULT_RADIUS);
    }

    @Override
    public Clustering<V> getClustering()
    {
        // Find friendship groups
        findFriendshipGroups();

        // Remove proper subsets
        removeProperSubsets();

        // Merge close sets
        mergeCloseSets();

        return new ClusteringImpl<>(mergedGroups);
    }

    void findFriendshipGroups()
    {
        CompletionService<Set<Set<V>>> completionService = new ExecutorCompletionService<>(executor);
        // Set used because duplicates are not allowed
        Set<Set<V>> groups = new HashSet<>();

        // Submit tasks
        for(V v : graph.iterables().vertices())
        {
            completionService.submit(new FriendshipGroupsTask(v));
        }

        // Wait for all results
        for(int i=0; i<graph.vertexSet().size(); i++)
        {
            try
            {
                // If any thread finishes its work, results are added, otherwise main thread waits
                groups.addAll(completionService.take().get());
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }

        friendshipGroups.addAll(groups);
    }

    void removeProperSubsets()
    {
        CompletionService<Optional<Integer>> completionService = new ExecutorCompletionService<>(executor);
        boolean[] keep = new boolean[friendshipGroups.size()];
        Arrays.fill(keep, false);

        // Remove proper subjects
        // Submit tasks
        for(int i=0; i<friendshipGroups.size(); i++)
        {
            completionService.submit(new ProperSubsetsTask(i));
        }
        // Wait for all results
        for(int i=0; i<friendshipGroups.size(); i++)
        {
            try
            {
                Optional<Integer> result = completionService.take().get();
                if(result.isPresent())
                {
                    keep[result.get()] = true;
                }
            }
            catch (InterruptedException | ExecutionException e)
            {
                e.printStackTrace();
            }
        }
        // Sets after proper subset removal
        for(int i=0; i<keep.length; i++)
        {
            if(keep[i])
            {
                superGroups.add(friendshipGroups.get(i));
            }
        }
    }

    void mergeCloseSets()
    {
        List<Set<V>> currentGroups = superGroups;

        boolean mergeOccurred;
        do
        {
            int size = currentGroups.size();
            boolean[] keep = new boolean[currentGroups.size()];
            Arrays.fill(keep, true);
            mergeOccurred = false;

            for(int i=0;i<size;i++)
            {
                if(!keep[i])
                {
                    continue;
                }
                Set<V> setA = currentGroups.get(i);
                for(int j=0; j<size;j++)
                {
                    if(i!=j && keep[j])
                    {
                        Set<V> setB = currentGroups.get(j);
                        if(areCloseBy(setA, setB))
                        {
                            setA.addAll(setB);
                            keep[j] = false;
                            mergeOccurred = true;
                            break;
                        }
                    }
                }
            }

            List<Set<V>> newGroups = new ArrayList<>();
            for(int i=0;i<size;i++)
            {
                if(keep[i])
                {
                    newGroups.add(currentGroups.get(i));
                }
            }
            currentGroups = newGroups;
        }
        while(mergeOccurred);

        mergedGroups = currentGroups;
    }

    boolean isProperSubset(Set<V> setA, Set<V> setB)
    {
        return setB.containsAll(setA) && !setB.equals(setA);
    }

    boolean areCloseBy(Set<V> setA, Set<V> setB)
    {
        // Ensure setA is smaller than setB
        if (setA.size() > setB.size())
        {
            Set<V> tmp = setA;
            setA = setB;
            setB = tmp;
        }
        // Sets are close by when, with |setB|>=|setA|, |setA|-|setB ∩ setA|<=1 is true
        Set<V> intersection = new HashSet<>(setB);
        intersection.retainAll(setA);
        int proximity = setA.size() - intersection.size();

        return proximity<=1;
    }

    /**
     * Task used during the algorithm to find the friendship groups of a given vertex.
     */
    class FriendshipGroupsTask implements Callable<Set<Set<V>>>
    {
        private final V vertex;

        public FriendshipGroupsTask(V vertex)
        {
            this.vertex = vertex;
        }

        @Override
        public Set<Set<V>> call()
        {
            // Calculate egonet
            Graph<V, E> target = GraphTypeBuilder
                    .forGraphType(graph.getType()).edgeSupplier(graph.getEdgeSupplier())
                    .vertexSupplier(graph.getVertexSupplier()).buildGraph();
            GraphGenerator<V, E, V> gen = new EgonetGraphGenerator<>(graph, vertex, radius, false); // We don't need the ego vertex
            gen.generateGraph(target);

            // Find friendship-groups
            Set<Set<V>> groups = new HashSet<>();
            ConnectivityInspector<V, E> inspector = new ConnectivityInspector<>(target);
            for(Set<V> set : inspector.connectedSets())
            {
                set.add(vertex);
                groups.add(set);
            }

            return groups;
        }
    }

    /**
     * Task used during the algorithm to detect proper subsets of a given friendship group.
     * Returns the set index as an integer if the set is not a proper subset of any other set or an empty optional.
     */
    class ProperSubsetsTask implements Callable<Optional<Integer>>
    {
        private final int index;

        ProperSubsetsTask(int n)
        {
            this.index = n;
        }

        public int getIndex()
        {
            return index;
        }

        @Override
        public Optional<Integer> call()
        {
            Set<V> setA = friendshipGroups.get(index);

            for(Set<V> setB : friendshipGroups)
            {
                if(isProperSubset(setA, setB))
                {
                    return Optional.empty();
                }
            }

            return Optional.of(index);
        }
    }
}

