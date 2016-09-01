/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -----------------
 * VertexCovers.java
 * -----------------
 * (C) Copyright 2003-2008, by Joris Kinable and Contributors.
 *
 * Original Author:  Joris Kinable
 * Contributor(s): Nils Olberg
 *
 * $Id$
 *
 * Changes
 * -------
 * 28-Jul-2016 : Initial revision (JK);
 *
 */
package org.jgrapht.alg.vertexcover;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.NeighborIndex;
import org.jgrapht.alg.interfaces.MinimumWeightedVertexCoverAlgorithm;
import org.jgrapht.alg.util.WeightedVertexSet;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Finds a minimum vertex cover in a undirected graph.
 * The implementation relies on a recursive algorithm. At each recursive step,
 * the algorithm picks a unvisited vertex v and distinguishes two cases:
 * either v has to be added to the vertex cover or all of its neighbors.
 *
 * In pseudo code, the algorithm (simplified) looks like this:
 *
 * <pre><code>
 *  VC(G):
 *  if V = ∅ then return ∅
 *  Choose an arbitrary node v ∈ G
 *  G1 := (V − {v}, { e ∈ E | v ∈/ e })
 *  G2 := (V − {v} − N(v), { e ∈ E | e ∩ (N(v) ∪ {v})= ∅ })
 *  if |{v} ∪ VC(G1)| ≤ |N(v) ∪ VC(G2)| then
 *    return {v} ∪ VC(G1)
 *  else
 *    return N(v) ∪ VC(G2)
 * </code></pre>
 *
 * To speed up the implementation, memoization and a bounding procedure are used.
 * The current implementation solves instances with 150-250 vertices efficiently to optimality.
 *
 * TODO JK: determine runtime complexity and add it to class description.
 * TODO JK: run this class through a performance profiler
 *
 *
 *
 * @author Joris Kinable
 */
public class RecursiveExactVCImpl<V,E> implements MinimumWeightedVertexCoverAlgorithm<V,E> {

    /** Input graph **/
    private UndirectedGraph<V,E> graph;
    /** Number of vertices in the graph**/
    private int N;
    /**
     * Neighbor cache
     * TODO JK: It might be worth trying to replace the neighbors index by a bitset view. As such, all operations can be simplified to bitset operations, which may improve the algorithm's performance.
     * **/
    NeighborIndex<V,E> neighborIndex;

    /** Map for memoization **/
    private Map<BitSet, BitSetCover> memo;

    /** Ordered list of vertices which will be iteratively considered to be included in a matching **/
    private List<V> vertices;
    /** Mapping of a vertex to its index in the list of vertices **/
    private Map<V,Integer> vertexIDDictionary;

    /**
     * Maximum weight of the vertex cover. In case there is no weight assigned to the vertices, the weight of
     * the cover equals the cover's cardinality.
     */
    private double upperBoundOnVertexCoverWeight;

    /** Indicates whether we are solving a weighted or unweighted version of the problem **/
    private boolean weighted;

    private Map<V, Double> vertexWeightMap=null;

    @Override
    public WeightedVertexSet<V> getVertexCover(UndirectedGraph<V, E> graph) {
        Map<V,Double> vertexWeightMap=graph.vertexSet().stream().collect(Collectors.toMap(Function.identity() , vertex-> 1.0));
        weighted=false;
        return this.getVertexCover(graph, vertexWeightMap);
    }

    @Override
    public WeightedVertexSet<V> getVertexCover(UndirectedGraph<V, E> graph, Map<V, Double> vertexWeightMap) {

        //Initialize
        this.graph=graph;
        memo=new HashMap<>();
        vertices=new ArrayList<>(graph.vertexSet());
        neighborIndex=new NeighborIndex<>(graph);
        vertexIDDictionary=new HashMap<>();
        this.vertexWeightMap=vertexWeightMap;
        this.weighted=vertexWeightMap != null;

        N=vertices.size();
        //Sort vertices based on their weight/degree ratio in ascending order
        //TODO JK: Are there better orderings?
        Collections.sort(vertices, (V v1, V v2) -> Double.compare(vertexWeightMap.get(v1)/graph.degreeOf(v1), vertexWeightMap.get(v2)/graph.degreeOf(v2)));
        for(int i=0; i<vertices.size(); i++)
            vertexIDDictionary.put(vertices.get(i), i);

        //Calculate a bound on the maximum depth using heuristics and mathematical bounding procedures.
        //TODO JK: Is there a lower bounding procedure which allows us to prematurely terminate the search once a solution is found which is equal to the lower bound? Preferably a bounding procedure which gets better throughout the search.
        upperBoundOnVertexCoverWeight =this.calculateUpperBound();

        //Invoke recursive algorithm
        BitSetCover vertexCover= this.calculateCoverRecursively(0, new BitSet(N), 0);

        //Build solution
        Set<V> verticesInCover=new LinkedHashSet<>();
        for (int i = vertexCover.bitSetCover.nextSetBit(0); i >= 0 && i<N; i = vertexCover.bitSetCover.nextSetBit(i+1))
            verticesInCover.add(vertices.get(i));
        return new WeightedVertexSet<>(verticesInCover, vertexCover.weight);
    }

    private BitSetCover calculateCoverRecursively(int indexNextCandidate, BitSet visited, double accumulatedWeight){
        //Check memoization table
        if(memo.containsKey(visited)) {
            return memo.get(visited).copy(); //Cache hit
        }

        //Find the next unvisited vertex WITH neighbors (if a vertex has no neighbors, then we don't need to select it
        //because it doesn't cover any edges)
        int indexNextVertex=-1;
        Set<V> neighbors=Collections.emptySet();
        for(int index=visited.nextClearBit(indexNextCandidate);
            index >=0 && index<N;
            index=visited.nextClearBit(index+1)){

            neighbors=new LinkedHashSet<>(neighborIndex.neighborsOf(vertices.get(index)));
            for(Iterator<V> it=neighbors.iterator(); it.hasNext(); ) //Exclude all visited vertices
                if(visited.get(vertexIDDictionary.get(it.next())))
                    it.remove();
            if(!neighbors.isEmpty()) {
                indexNextVertex = index;
                break;
            }
        }

        //Base case 1: all vertices have been visited
        if(indexNextVertex==-1){ //We've visited all vertices, return the base case
            BitSetCover vertexCover=new BitSetCover(N, 0);
            if(accumulatedWeight <= upperBoundOnVertexCoverWeight) { //Found new a solution that matches our bound. Tighten the bound.
                upperBoundOnVertexCoverWeight = accumulatedWeight - 1;
            }
            return vertexCover;
            //Base case 2 (pruning): this vertex cover can never be better than the best cover we already have. Return a cover with a large weight, such that the other branch will be preferred over this branch.
        }else if(accumulatedWeight >= upperBoundOnVertexCoverWeight){
            return new BitSetCover(N, N);
        }

        //Recursion
        //TODO JK: Can we use a lower bound or estimation which of these 2 branches produces a better solution? If one of them is more likely to produce a better solution,
        //then that branch should be explored first! Futhermore, if the lower bound+accumulated cost > upperBoundOnVertexCoverWeight, then we may prune.

        //Create 2 branches (N(v) denotes the set of neighbors of v. G_{v} indicates the graph obtained by removing vertex v and all vertices incident to it.):

        //Right branch (N(v) are added to the cover, and we solve for G_{N(v) \cup v }.):
        BitSet visitedRightBranch= (BitSet) visited.clone();
        visitedRightBranch.set(indexNextVertex);
        for(V v : neighbors)
            visitedRightBranch.set(vertexIDDictionary.get(v));

        double weight=this.getWeight(neighbors);
        BitSetCover rightCover=calculateCoverRecursively(indexNextVertex+1, visitedRightBranch, accumulatedWeight+weight);
        rightCover.addAllVertices(
                neighbors.stream().mapToInt(vertexIDDictionary::get).boxed().collect(Collectors.toList()),
                weight);

        //Left branch (vertex v is added to the cover, and we solve for G_{v}):
        BitSet visitedLeftBranch= (BitSet) visited.clone();
        visitedLeftBranch.set(indexNextVertex);

        weight=vertexWeightMap.get(vertices.get(indexNextVertex));
        BitSetCover leftCover=calculateCoverRecursively(indexNextVertex+1, visitedLeftBranch, accumulatedWeight+weight);
        leftCover.addVertex(indexNextVertex, weight); //Delayed update of the left cover


        //Return the best branch
        if(leftCover.weight <= rightCover.weight){
            memo.put(visited, leftCover.copy());
            return leftCover;
        }else{

            memo.put(visited, rightCover.copy());
            return rightCover;
        }
    }


    /**
     * Returns the weight of a collection of vertices. In case of the unweighted vertex cover problem, the return value
     * is the cardinality of the collection. In case of the weighted version, the return value is the sum of the weights
     * of the vertices
     * @param vertices vertices
     * @return the total weight of the vertices in the collection.
     */
    private double getWeight(Collection<V> vertices){
        if(weighted)
            return vertices.stream().mapToDouble(vertexWeightMap::get).sum();
        else
            return vertices.size();
    }

    /**
     * Calculates a cheap upper bound on the optimum solution. Currently, we return the best solution found by either
     * the greedy heuristic, or Clarkson's 2-approximation. Neither of these 2 algorithms dominates the other.
     * //TODO JK: Are there better bounding procedures?
     */
    private double calculateUpperBound(){
        return Math.min(
                        new GreedyVCImpl<V,E>().getVertexCover(graph, vertexWeightMap).getWeight(),
                        new ClarksonTwoApproxVCImpl<V,E>().getVertexCover(graph, vertexWeightMap).getWeight()
                    );
    }

    /**
     * Helper class which represents a vertex cover as a space efficient BitSet
     */
    protected class BitSetCover{
        protected BitSet bitSetCover;
        protected double weight;

        protected BitSetCover(int size, int initialWeight){
            bitSetCover=new BitSet(size);
            this.weight=initialWeight;
        }

        //Copy constructor
        protected BitSetCover(BitSetCover vertexCover){
            this.bitSetCover= (BitSet) vertexCover.bitSetCover.clone();
            this.weight=vertexCover.weight;
        }

        protected BitSetCover copy(){
            return new BitSetCover(this);
        }

        protected void addVertex(int vertexIndex, double weight){
            bitSetCover.set(vertexIndex);
            this.weight+=weight;
        }

        protected void addAllVertices(List<Integer> vertexIndices, double totalWeight){
            vertexIndices.forEach(bitSetCover::set);
            this.weight+=totalWeight;
        }
    }

}
