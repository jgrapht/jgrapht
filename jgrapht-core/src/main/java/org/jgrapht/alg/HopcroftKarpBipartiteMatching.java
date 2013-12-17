/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2012, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* -------------------------
 * HopcroftKarpBipartiteMatching.java
 * -------------------------
 * (C) Copyright 2012-2012, by Joris Kinable and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s):
 *
 * Changes
 * -------
 * 26-Nov-2012 : Initial revision (JK);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;


/**
 * This class is an implementation of the Hopcroft-Karp algorithm which finds a
 * maximum matching in an undirected simple bipartite graph. The algorithm runs
 * in O(|E|*√|V|) time. The original algorithm is described in: Hopcroft, John
 * E.; Karp, Richard M. (1973), "An n5/2 algorithm for maximum matchings in
 * bipartite graphs", SIAM Journal on Computing 2 (4): 225–231,
 * doi:10.1137/0202019 A coarse overview of the algorithm is given in:
 * http://en.wikipedia.org/wiki/Hopcroft-Karp_algorithm Note: the behavior of
 * this class is undefined when the input isn't a bipartite graph, i.e. when
 * there are edges within a single partition!
 *
 * @author Joris Kinable
 */

public class HopcroftKarpBipartiteMatching<V, E>
    implements MatchingAlgorithm<V, E>
{
    

    private final UndirectedGraph<V, E> graph;
    private final Set<V> partition1; //Partitions of bipartite graph
    private final Set<V> partition2;
    private Set<E> matching; //Set containing the matchings

    private final Set<V> unmatchedVertices1; //Set which contains the unmatched
                                             //vertices in partition 1
    private final Set<V> unmatchedVertices2;

    

    public HopcroftKarpBipartiteMatching(
        UndirectedGraph<V, E> graph,
        Set<V> partition1,
        Set<V> partition2)
    {
        this.graph = graph;
        this.partition1 = partition1;
        this.partition2 = partition2;
        matching = new HashSet<E>();

        unmatchedVertices1 = new HashSet<V>(partition1);
        unmatchedVertices2 = new HashSet<V>(partition2);

        assert this.checkInputData();
        this.maxMatching();
    }

    

    /**
     * Checks whether the input data meets the requirements: simple undirected
     * graph and bipartite partitions.
     */
    private boolean checkInputData()
    {
        if (graph instanceof Multigraph) {
            throw new IllegalArgumentException(
                "Multi graphs are not allowed as input, only simple graphs!");
        }

        //Test the bipartite-ness
        Set<V> neighborsSet1 = new HashSet<V>();
        for (V v : partition1) {
            neighborsSet1.addAll(Graphs.neighborListOf(graph, v));
        }
        if (interSectionNotEmpty(partition1, neighborsSet1)) {
            throw new IllegalArgumentException(
                "There are edges within partition 1, i.e. not a bipartite graph");
        }
        Set<V> neighborsSet2 = new HashSet<V>();
        for (V v : partition2) {
            neighborsSet2.addAll(Graphs.neighborListOf(graph, v));
        }
        if (interSectionNotEmpty(partition2, neighborsSet2)) {
            throw new IllegalArgumentException(
                "There are edges within partition 2, i.e. not a bipartite graph");
        }
        return true;
    }

    /**
     * Greedily match the vertices in partition1 to the vertices in partition2.
     * For each vertex in partition 1, check whether there is an edge to an
     * unmatched vertex in partition 2. If so, add the edge to the matching.
     */
    private void greedyMatch()
    {
        HashSet<V> usedVertices = new HashSet<V>();

        for (V vertex1 : partition1) {
            for (V vertex2 : Graphs.neighborListOf(graph, vertex1)) {
                if (!usedVertices.contains(vertex2)) {
                    usedVertices.add(vertex2);
                    unmatchedVertices1.remove(vertex1);
                    unmatchedVertices2.remove(vertex2);
                    matching.add(graph.getEdge(vertex1, vertex2));
                    break;
                }
            }
        }
    }

    /**
     * This method is the main method of the class. First it finds a greedy
     * matching. Next it tries to improve the matching by finding all the
     * augmenting paths. This leads to a maximum matching.
     */
    private void maxMatching()
    {
        this.greedyMatch();

        List<LinkedList<V>> augmentingPaths = this.getAugmentingPaths(); //Get a list with augmenting paths
        while (!augmentingPaths.isEmpty()) {
            for (
                Iterator<LinkedList<V>> it = augmentingPaths.iterator();
                it.hasNext();)
            { //Process all augmenting paths
                LinkedList<V> augmentingPath = it.next();
                unmatchedVertices1.remove(augmentingPath.getFirst());
                unmatchedVertices2.remove(augmentingPath.getLast());
                this.symmetricDifference(augmentingPath);
                it.remove();
            }
            augmentingPaths.addAll(this.getAugmentingPaths()); //Check whether there are new augmenting paths available
        }
    }

    /**
     * Given are the current matching and a new augmenting path p. p.getFirst()
     * and p.getLast() are newly matched vertices. This method updates the edges
     * which are part of the existing matching with the new augmenting path. As
     * a result, the size of the matching increases with 1.
     *
     * @param augmentingPath
     */
    private void symmetricDifference(LinkedList<V> augmentingPath)
    {
        int operation = 0;

        //The augmenting path alternatingly has an edge which is not part of the
        //matching, and an edge which is part of the matching. Edges which are
        //already part of the matching are removed, the others are added.
        while (augmentingPath.size() > 0) {
            E edge =
                graph.getEdge(augmentingPath.poll(), augmentingPath.peek());
            if ((operation % 2) == 0) {
                matching.add(edge);
            } else {
                matching.remove(edge);
            }
            operation++;
        }
    }

    private List<LinkedList<V>> getAugmentingPaths()
    {
        List<LinkedList<V>> augmentingPaths = new ArrayList<LinkedList<V>>();

        //1. Build data structure
        Map<V, Set<V>> layeredMap = new HashMap<V, Set<V>>();
        for (V vertex : unmatchedVertices1) {
            layeredMap.put(vertex, new HashSet<V>());
        }

        Set<V> oddLayer = new HashSet<V>(unmatchedVertices1); //Layer L0 contains the unmatchedVertices1.
        Set<V> evenLayer;
        Set<V> usedVertices = new HashSet<V>(unmatchedVertices1);

        while (true) {
            //Create a new even Layer A new layer can ONLY contain vertices
            //which are not used in the previous layers Edges between odd and
            //even layers can NOT be part of the matching
            evenLayer = new HashSet<V>();
            for (V vertex : oddLayer) {
                //List<V> neighbors=this.getNeighbors(vertex);
                List<V> neighbors = Graphs.neighborListOf(graph, vertex);
                for (V neighbor : neighbors) {
                    if (usedVertices.contains(neighbor)) {
                        // Vertices placed into odd-layer may not be matched by
                        // any other vertices except for the one we came from
                        continue;
                    } else {
                        evenLayer.add(neighbor);
                        if (!layeredMap.containsKey(neighbor)) {
                            layeredMap.put(neighbor, new HashSet<V>());
                        }
                        layeredMap.get(neighbor).add(vertex);
                    }
                }
            }
            usedVertices.addAll(evenLayer);

            //Check whether we are finished generating layers. We are finished
            //if 1. the last layer is empty or 2. if we reached free vertices
            //in partition2.
            if ((evenLayer.size() == 0)
                || this.interSectionNotEmpty(evenLayer, unmatchedVertices2))
            {
                break;
            }

            //Create a new odd Layer A new layer can ONLY contain vertices which
            //are not used in the previous layers Edges between EVEN and ODD
            //layers SHOULD be part of the matching
            oddLayer = new HashSet<V>();
            for (V vertex : evenLayer) {
                List<V> neighbors = Graphs.neighborListOf(graph, vertex);
                for (V neighbor : neighbors) {
                    if (usedVertices.contains(neighbor)
                        || !matching.contains(
                            graph.getEdge(vertex, neighbor)))
                    {
                        continue;
                    } else {
                        oddLayer.add(neighbor);
                        if (!layeredMap.containsKey(neighbor)) {
                            layeredMap.put(neighbor, new HashSet<V>());
                        }
                        layeredMap.get(neighbor).add(vertex);
                    }
                }
            }
            usedVertices.addAll(oddLayer);
        }

        //Check whether there exist augmenting paths. If not, return an empty
        //list. Else, we need to generate the augmenting paths which start at
        //free vertices in the even layer and end at the free vertices at the
        //first odd layer (L0).
        if (evenLayer.size() == 0) {
            return augmentingPaths;
        } else {
            evenLayer.retainAll(unmatchedVertices2);
        }

        //Finally, do a depth-first search, starting on the free vertices in the
        //last even layer. Objective is to find as many vertex disjoint paths
        //as possible.
        for (V vertex : evenLayer) {
            //Calculate an augmenting path, starting at the given vertex.
            LinkedList<V> augmentingPath = dfs(vertex, layeredMap);

            //If the augmenting path exists, add it to the list of paths and
            //remove the vertices from the map to enforce that the paths are
            //vertex disjoint, i.e. a vertex cannot occur in more than 1 path.
            if (augmentingPath != null) {
                augmentingPaths.add(augmentingPath);
                for (V augmentingVertex : augmentingPath) {
                    layeredMap.remove(augmentingVertex);
                }
            }
        }

        return augmentingPaths;
    }

    private LinkedList<V> dfs(V startVertex, Map<V, Set<V>> layeredMap)
    {
        if (!layeredMap.containsKey(startVertex)) {
            return null;
        } else if (unmatchedVertices1.contains(startVertex)) {
            LinkedList<V> list = new LinkedList<V>();
            list.add(startVertex);
            return list;
        } else {
            LinkedList<V> partialPath = null;
            for (V vertex : layeredMap.get(startVertex)) {
                partialPath = dfs(vertex, layeredMap);
                if (partialPath != null) {
                    partialPath.add(startVertex);
                    break;
                }
            }
            return partialPath;
        }
    }

    /**
     * Helper method which checks whether the intersection of 2 sets is empty.
     *
     * @param vertexSet1
     * @param vertexSet2
     *
     * @return true if the intersection is NOT empty.
     */
    private boolean interSectionNotEmpty(Set<V> vertexSet1, Set<V> vertexSet2)
    {
        for (V vertex : vertexSet1) {
            if (vertexSet2.contains(vertex)) {
                return true;
            }
        }
        return false;
    }

    @Override public Set<E> getMatching()
    {
        return Collections.unmodifiableSet(matching);
    }
}

// End HopcroftKarpBipartiteMatching.java
