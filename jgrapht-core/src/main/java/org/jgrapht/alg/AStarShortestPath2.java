package org.jgrapht.alg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.*;
import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;
import org.jgrapht.GraphPath;
/**
 * An implementation of <a
 * href="http://en.wikipedia.org/wiki/A*_search_algorithm">A* shortest 
 * path algorithm</a>.
 * <a href="http://de.wikipedia.org/wiki/A*-Algorithmus"> A* shortest path algorithm german Wiki </a>.
 * 
 * Inspired by Jon Robison
 *
 * @author Thomas Breitbart
 * @since Sep, 2012
 */

public final class AStarShortestPath2<V, E> {
	
	//~ Instance fields --------------------------------------------------------

    private GraphPath<V, E> path;

    //~ Constructors -----------------------------------------------------------

    public AStarShortestPath2(SimpleDirectedWeightedGraph<V, E> graph,
            V startVertex,
            V endVertex,
            AStarFunctionProvider2<V> functionProvider) {
    	
    	if (!graph.containsVertex(endVertex))
            throw new IllegalArgumentException("graph must contain the end vertex");
        
    	//Die benötigte OpenList
    	FibonacciHeap<V> openList = new FibonacciHeap<V>();
    	List<V> openListcheck = new ArrayList<V>();
    	//Die benötigte ClosedList
    	List<V> closedList = new ArrayList<V>();
    	//Merken der g-Werte
    	Map<V, Double> gScoreMap = new HashMap<V, Double>();
    	//Karte der Vorgänger/Nachfolger
    	 Map<V,V> cameFrom = new HashMap<V,V>();
    	
    	gScoreMap.put(startVertex, 0.0);
    	openList.insert(new FibonacciHeapNode<V>(startVertex), 0.0);
    	openListcheck.add(startVertex);
    	
    	do {
    		FibonacciHeapNode<V> currentNode = openList.removeMin();
    		//sind wir schon am Ziel?
    		if (currentNode.getData() == endVertex){
    			//baue hier den Pfad
    			path = buildGraphPath(cameFrom, currentNode.getData(), graph, startVertex, endVertex);
        		return;
    		}
    		//Noch nicht am Ziel, also expandiere den Knoten
    		expandNode(currentNode, graph, closedList, openList, gScoreMap, functionProvider, closedList, cameFrom, endVertex);
    		closedList.add(currentNode.getData());
    	} while(!openList.isEmpty());
    	//hier wurde kein Pfad gefunden
    	path=null;
    }
    
    private void expandNode(FibonacciHeapNode<V> currentNode, 
    		SimpleDirectedWeightedGraph<V, E> graph, 
    		List<V> closedList, 
    		FibonacciHeap<V> openList, 
    		Map<V, Double> gScoreMap, 
    		AStarFunctionProvider2<V> functionProvider,
    		List<V> openListcheck, 
    		Map<V,V> cameFrom,
    		V endVertex){
    	
    	for(E edge : graph.edgesOf(currentNode.getData())){
    		//keine Knoten die auf sich selbst verweisen! Erzeugt sonst unter Umständen Loops
    		V target = graph.getEdgeTarget(edge);
    		if (target == currentNode.getData()){
    			continue;
    		}
    		V successor = graph.getEdgeTarget(edge);
    		if (closedList.contains(successor)){
    			continue;
    		}
    		double gScore_current = gScoreMap.get(currentNode.getData());
    		double tentativeGScore = gScore_current + functionProvider.getPathCost(currentNode.getData(), successor);
    		
    		if (openListcheck.contains(successor) && tentativeGScore >= gScoreMap.get(successor)){
    			continue;
    		}
    		cameFrom.put(successor, currentNode.getData());
    		gScoreMap.put(successor, tentativeGScore);
    		
    		double f= tentativeGScore + functionProvider.getHeuristicCost(successor, endVertex);
    		if (openListcheck.contains(successor)) {
    			openList.decreaseKey(new FibonacciHeapNode<V>(successor), f);
    		}
    		else {
    			openList.insert(new FibonacciHeapNode<V>(successor), f);
    			openListcheck.add(successor);
    		}
    	}
    }
    
    private List<V> buildPath(Map<V,V> cameFrom, V currentNode){
    	List<V> path = cameFrom.containsKey(currentNode) ? 
    			buildPath(cameFrom, cameFrom.get(currentNode)) :
    			new ArrayList<V>();
		path.add(currentNode);
		return path;
    }

    private GraphPath<V, E> buildGraphPath(Map<V,V> cameFrom, V currentNode,
    		SimpleDirectedWeightedGraph<V, E> graph, V startVertex, V endVertex){
    	List<V> reconstructed = buildPath(cameFrom, currentNode);
		List<E> edgeList = new ArrayList<E>();
		double weight = 0.0;
		for(int i=0; i<reconstructed.size()-1; i++){
			E edge = graph.getEdge(reconstructed.get(i), reconstructed.get(i+1));
			weight += graph.getEdgeWeight(edge);
			edgeList.add(edge);
		}
		return new GraphPathImpl<V, E>(graph, startVertex, endVertex, edgeList, weight);
    }
    
    public List<E> getPathEdgeList()
    {
        if (path == null) {
            return null;
        } else {
            return path.getEdgeList();
        }
    }

    /**
     * Return the path found.
     *
     * @return path representation, or null if no path exists
     */
    public GraphPath<V, E> getPath()
    {
        return path;
    }

    /**
     * Return the length of the path found.
     *
     * @return path length, or Double.POSITIVE_INFINITY if no path exists
     */
    public double getPathLength()
    {
        if (path == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return path.getWeight();
        }
    }
    
    public interface AStarFunctionProvider2<V>{
    	/**
         * An admissible "heuristic estimate" of the distance from x to the goal 
         * (usually denoted h(x)). This is the good guess function.
         */
    	public double getHeuristicCost(V start, V goal);
    	
    	/**
    	 * Path cost from starting node to current node x (usually denoted g(x))
    	 */
    	public double getPathCost(V neighbor, V goal);
    }
}
