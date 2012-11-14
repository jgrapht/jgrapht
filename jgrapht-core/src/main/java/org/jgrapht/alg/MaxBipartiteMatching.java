package org.jgrapht.alg;

import com.google.common.collect.Maps;
import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class is an implementation of the Hopcroft-Karp algorithm which finds a maximum matching in an undirected
 * bipartite graph. The algorithm runs in O(|E|*√|V|) time.
 * The original algorithm is described in:
 * Hopcroft, John E.; Karp, Richard M. (1973), "An n5/2 algorithm for maximum matchings in bipartite graphs",
 * SIAM Journal on Computing 2 (4): 225–231, doi:10.1137/0202019
 * 
 * A course overview of the algorithm is given in: http://en.wikipedia.org/wiki/Hopcroft-Karp_algorithm
 * 
 * Note: the behavior of this class is undefined when the input isn't a bipartite graph, i.e. when there are edges within a single partition!
 * 
 * @author Joris Kinable
 */

public class MaxBipartiteMatching<V,E> {

	private final UndirectedGraph<V, E> graph;
	private final List<V> partition1; //Partitions of bipartite graph
	private final List<V> partition2;
	private final HashSet<E> matching; //Set containing the matchings
	
	private final HashSet<V> unmatchedVertices1; //Set which contains the unmatched vertices in partition 1
	private final HashSet<V> unmatchedVertices2;
	
	
	public MaxBipartiteMatching(final UndirectedGraph<V, E> graph, final List<V> partition1, final List<V> partition2){
		this.graph=graph;
		this.partition1=partition1;
		this.partition2=partition2;
		matching = new HashSet<E>();
		
		unmatchedVertices1=new HashSet<V>(partition1);
		unmatchedVertices2=new HashSet<V>(partition2);

        maxMatching();
	}
	
	/**
	 * Greedily match the vertices in partition1 to the vertices in partition2.
	 * For each vertex in partition 1, check whether there is an edge to an unmatched vertex in
	 * partition 2. If so, add the edge to the matching.
	 */
	private void greedyMatch(){
		final HashSet<V> usedVertices=new HashSet<V>();
		
		for(final V vertex1: partition1){
			for(final V vertex2:Graphs.neighborListOf(graph, vertex1)){
				if(!usedVertices.contains(vertex2)){
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
	 * This method is the main method of the class. First it finds a greedy matching. Next it tries
	 * to improve the matching by finding all the augmenting paths. This leads to a maximum matching.
	 */
	private void maxMatching(){
        greedyMatch();
		final List<LinkedList<V>> augmentingPaths= getAugmentingPaths();
		//System.out.println("Augmenting paths: "+augmentingPaths);
		for(final LinkedList<V> augmentingPath: augmentingPaths){
			unmatchedVertices1.remove(augmentingPath.getFirst());
			unmatchedVertices2.remove(augmentingPath.getLast());
            symmetricDifference(augmentingPath);
		}
		//System.out.println("Maximum matching: "+matching);
		//System.out.println("UnmatchedVertices1: "+unmatchedVertices1);
		//System.out.println("UnmatchedVertices2: "+unmatchedVertices2);
	}
	
	/**
	 * Given are the current matching and a new augmenting path p. p.getFirst() and p.getLast() are
	 * newly matched vertices. This method updates the edges which are part of the existing matching with the new augmenting path.
	 * As a result, the size of the matching increases with 1.
	 * @param augmentingPath
	 */
	private void symmetricDifference(final LinkedList<V> augmentingPath){
		int operation=0;
		//The augmenting path alternatingly has an edge which is not part of the matching, and an edge
		//which is part of the matching. Edges which are already part of the matching are removed, the others are added.
		while(!augmentingPath.isEmpty()){
			final E edge=graph.getEdge(augmentingPath.poll(),augmentingPath.peek());
			if(operation%2==0){
				matching.add(edge);
			}else
				matching.remove(edge);
			operation++;
		}
	}
	
	private List<LinkedList<V>> getAugmentingPaths(){
		final List<LinkedList<V>> augmentingPaths=new ArrayList<LinkedList<V>>();
		
		//1. Build data structure
		final Map<V,Set<V>> layeredMap= Maps.newHashMap();
		for(final V vertex:unmatchedVertices1)
			layeredMap.put(vertex, new HashSet<V>());
		
		Set<V> oddLayer=new HashSet<V>(unmatchedVertices1); //Layer L0 contains the unmatchedVertices1.
		Set<V> evenLayer;
		final Set<V> usedVertices=new HashSet<V>(unmatchedVertices1);
		
		boolean finished=false;
		
		//System.out.println("greedy matching: "+matching);
		//System.out.println("oddlayer: "+oddLayer);
		
		do{
			
			
			//Create a new even Layer
			//A new layer can ONLY contain vertices which are not used in the previous layers
			//Edges between odd and even layers can NOT be part of the matching
			evenLayer=new HashSet<V>();
			for(final V vertex: oddLayer){
				//List<V> neighbors=this.getNeighbors(vertex);
				final List<V> neighbors=Graphs.neighborListOf(graph, vertex);
				for(final V neighbor: neighbors){
					if(usedVertices.contains(neighbor) || matching.contains(graph.getEdge(vertex, neighbor))) {
                    }
					else{
						evenLayer.add(neighbor);
						if(!layeredMap.containsKey(neighbor))
							layeredMap.put(neighbor, new HashSet<V>());
						layeredMap.get(neighbor).add(vertex);
					}
				}
			}
			usedVertices.addAll(evenLayer);
			//System.out.println("evenlayer: "+evenLayer);
			//System.out.println("layerMap: "+layeredMap);
			
			//Check whether we are finished generating layers.
			//We are finished if 1. the last layer is empty or 2. if we reached free vertices in partition2.
			if(evenLayer.isEmpty() || interSectionNotEmpty(evenLayer, unmatchedVertices2)){
				finished=true;
				continue;
			}
			
			//Create a new odd Layer
			//A new layer can ONLY contain vertices which are not used in the previous layers
			//Edges between EVEN and ODD layers SHOULD be part of the matching
			oddLayer=new HashSet<V>();
			for(final V vertex: evenLayer){
				//List<V> neighbors=this.getNeighbors(vertex);
				final List<V> neighbors=Graphs.neighborListOf(graph, vertex);
				for(final V neighbor: neighbors){
					if(usedVertices.contains(neighbor) || !matching.contains(graph.getEdge(vertex, neighbor))) {
                    }
					else{
						oddLayer.add(neighbor);
						if(!layeredMap.containsKey(neighbor))
							layeredMap.put(neighbor, new HashSet<V>());
						layeredMap.get(neighbor).add(vertex);
					}
				}
			}
			usedVertices.addAll(oddLayer);
			//System.out.println("oddLayer: "+oddLayer);
			//System.out.println("layerMap: "+layeredMap);
		}while(!finished);
		
		//Check whether there exist augmenting paths. If not, return an empty list.
		//Else, we need to generate the augmenting paths which start at free vertices in
		//the even layer and end at the free vertices at the first odd layer (L0).
        if (evenLayer.isEmpty()) {
            return augmentingPaths;
        }
        evenLayer.retainAll(unmatchedVertices2);

        //System.out.println("last evenlayer: "+evenLayer);
		
		//Finally, do a depth-first search, starting on the free vertices in the last even layer.
		//Objective is to find as many vertex disjoint paths as possible.
		for(final V vertex : evenLayer){
			//Calculate an augmenting path, starting at the given vertex.
			final LinkedList<V> augmentingPath=dfs(vertex,layeredMap);

			//If the augmenting path exists, add it to the list of paths and remove the vertices
			//from the map to enforce that the paths are vertex disjoint, i.e. a vertex cannot occur in
			//more than 1 path.
			if(augmentingPath!=null){
				augmentingPaths.add(augmentingPath);
				for(final V augmentingVertex: augmentingPath)
					layeredMap.remove(augmentingVertex);
			}
		}
		
		return augmentingPaths;
	}
	
	private LinkedList<V> dfs(final V startVertex, final Map<V,Set<V>> layeredMap){
		if(!layeredMap.containsKey(startVertex))
			return null;
		else if(unmatchedVertices1.contains(startVertex)){
			final LinkedList<V> list=new LinkedList<V>();
			list.add(startVertex);
			return list;
		}else{
			LinkedList<V> partialPath=null;
			for(final V vertex: layeredMap.get(startVertex)){
				partialPath=dfs(vertex,layeredMap);
				if(partialPath!=null){
					partialPath.add(startVertex);
					break;
				}
			}
			return partialPath;
		}
	}
	
	/**
	 * Helper method which checks whether the intersection of 2 sets is empty.
	 * @param vertexSet1
	 * @param vertexSet2
	 * @return true if the intersection is NOT empty.
	 */
	private boolean interSectionNotEmpty(final Set<V> vertexSet1, final Set<V> vertexSet2){
		for(final V vertex: vertexSet1)
			if(vertexSet2.contains(vertex))
				return true;
		return false;
	}
	
	/**
	 * Returns the edges which are part of the maximum matching.
	 */
	public Set<E> getMatching() {
		return Collections.unmodifiableSet(matching);
	}

	/**
	 * Returns the number of edges which are part of the maximum matching
	 */
	public int getSize(){
		return matching.size();
	}
}
