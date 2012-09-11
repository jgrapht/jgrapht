/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* -------------------------
 * AStarShortestPath.java
 * -------------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  Jon Robison
 *
 * $Id$
 *
 * Changes
 * -------
 * 10-Sep-2012 : Initial revision (JR);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * An implementation of <a
 * href="http://en.wikipedia.org/wiki/A*_search_algorithm">A* shortest 
 * path algorithm</a>.
 *
 * @author Jon Robison
 * @since Sep 10, 2012
 */
public final class AStarShortestPath<V, E>
{
    //~ Instance fields --------------------------------------------------------

    private GraphPath<V, E> path;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates and executes a new AStarShortestPath algorithm instance. An
     * instance is only good for a single search; after construction, it can be
     * accessed to retrieve information about the path found.
     * 
     * This is mostly a copy of the wikipedia pseudocode entry, with I suppose
     * the only notable exception being the use of treemap and its 
     * corresponding comparator. That was used to more closely parallel the 
     * wikis code.
     * 
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     * @param functionProvider of f(x) and g(x), estimating costs
     */
    public AStarShortestPath(WeightedGraph<V, E> graph,
        V startVertex,
        V endVertex,
        AStarFunctionProvider<V> functionProvider) {
        if (!graph.containsVertex(endVertex))
            throw new IllegalArgumentException("graph must contain the end vertex");
        List<V> closedSet = new ArrayList<V>(),
        		openSet = new ArrayList<V>();
        Map<V,V> cameFrom = new HashMap<V,V>();
        Map<V, Double> gScoreMap = new HashMap<V, Double>();
        openSet.add(startVertex);
        TreeMap<V, Double> fScoreMap = new TreeMap<V, Double>(new VertexComparator(functionProvider, endVertex));
        gScoreMap.put(startVertex, 0.0);
        fScoreMap.put(startVertex, functionProvider.getHeuristicCost(startVertex, endVertex));

        while(!openSet.isEmpty()){
        	V current = fScoreMap.firstKey();
        	fScoreMap.remove(current);
        	if(current == endVertex){
        		path = buildGraphPath(cameFrom, current, graph, startVertex, endVertex);
        		return;
        	}
        	openSet.remove(current);
        	closedSet.add(current);
        	for(E edge : graph.edgesOf(current)){
        		V neighbor = graph.getEdgeTarget(edge);
        		if(closedSet.contains(neighbor))
        			continue;
        		double tentativeGScore = gScoreMap.get(current) + functionProvider.getPathCost(current, neighbor);
        		if(!openSet.contains(neighbor) || tentativeGScore < gScoreMap.get(neighbor)){
        			if(!openSet.contains(neighbor))
        				openSet.add(neighbor);
        			cameFrom.put(neighbor, current);
        			gScoreMap.put(neighbor, tentativeGScore);
        			fScoreMap.put(neighbor, gScoreMap.get(neighbor) + functionProvider.getHeuristicCost(neighbor, endVertex));
        		}
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
    		WeightedGraph<V, E> graph, V startVertex, V endVertex){
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

    /**
     * Return the edges making up the path found.
     *
     * @return List of Edges, or null if no path exists
     */
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

    /**
     * Convenience method to find the shortest path via a single static method
     * call. If you need a more advanced search (e.g. limited by radius, or
     * computation of the path length), use the constructor instead.
     *
     * @param graph the graph to be searched
     * @param startVertex the vertex at which the path should start
     * @param endVertex the vertex at which the path should end
     * @param functionProvider of f(x) and g(x), estimating costs
     *
     * @return List of Edges, or null if no path exists
     */
    public static <V, E> List<E> findPathBetween(
        WeightedGraph<V, E> graph,
        V startVertex,
        V endVertex,
        AStarFunctionProvider<V> functionProvider)
    {
        AStarShortestPath<V, E> alg =
            new AStarShortestPath<V, E>(
                graph,
                startVertex,
                endVertex,
                functionProvider);

        return alg.getPathEdgeList();
    }
    
    public interface AStarFunctionProvider<V>{
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
    
    private class VertexComparator implements Comparator<V>{
    	private final AStarFunctionProvider<V> provider;
    	private final V goal;
    	
    	private VertexComparator(AStarFunctionProvider<V> provider, V goal){
    		this.provider = provider;
    		this.goal = goal;
    	}
    	
		@Override public int compare(V o1, V o2) {
			Double o1Distance = provider.getPathCost(o1, goal),
				o2Distance = provider.getPathCost(o2, goal);
			return o1Distance.compareTo(o2Distance);
		}
    }
}

// End AStarShortestPath.java
