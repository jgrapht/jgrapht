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

import com.google.common.collect.Maps;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.GraphPathImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    public AStarShortestPath(final WeightedGraph<V, E> graph,
        final V startVertex,
        final V endVertex,
        final AStarFunctionProvider<V> functionProvider) {
        if (!graph.containsVertex(endVertex))
            throw new IllegalArgumentException("graph must contain the end vertex");
        final List<V> closedSet = new ArrayList<V>();
        final List<V> openSet = new ArrayList<V>();
        final Map<V,V> cameFrom = Maps.newHashMap();
        final Map<V, Double> gScoreMap = Maps.newHashMap();
        openSet.add(startVertex);
        final TreeMap<V, Double> fScoreMap
            = Maps.newTreeMap(new VertexComparator(functionProvider, endVertex));
        gScoreMap.put(startVertex, 0.0);
        fScoreMap.put(startVertex, functionProvider.getHeuristicCost(startVertex, endVertex));

        while(!openSet.isEmpty()){
        	final V current = fScoreMap.firstKey();
        	fScoreMap.remove(current);
        	if(current == endVertex){
        		path = buildGraphPath(cameFrom, current, graph, startVertex, endVertex);
        		return;
        	}
        	openSet.remove(current);
        	closedSet.add(current);
        	for(final E edge : graph.edgesOf(current)){
        		final V neighbor = graph.getEdgeTarget(edge);
        		if(closedSet.contains(neighbor))
        			continue;
        		final double tentativeGScore = gScoreMap.get(current) + functionProvider.getPathCost(current, neighbor);
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

    private List<V> buildPath(final Map<V,V> cameFrom, final V currentNode){
    	final List<V> path = cameFrom.containsKey(currentNode) ?
    			buildPath(cameFrom, cameFrom.get(currentNode)) :
    			new ArrayList<V>();
		path.add(currentNode);
		return path;
    }

    private GraphPath<V, E> buildGraphPath(final Map<V,V> cameFrom, final V currentNode,
    		final WeightedGraph<V, E> graph, final V startVertex, final V endVertex){
    	final List<V> reconstructed = buildPath(cameFrom, currentNode);
		final List<E> edgeList = new ArrayList<E>();
		double weight = 0.0;
		for(int i=0; i<reconstructed.size()-1; i++){
			final E edge = graph.getEdge(reconstructed.get(i), reconstructed.get(i+1));
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
        final WeightedGraph<V, E> graph,
        final V startVertex,
        final V endVertex,
        final AStarFunctionProvider<V> functionProvider)
    {
        final AStarShortestPath<V, E> alg =
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
        double getHeuristicCost(V start, V goal);
    	
    	/**
    	 * Path cost from starting node to current node x (usually denoted g(x))
    	 */
        double getPathCost(V neighbor, V goal);
    }
    
    private class VertexComparator implements Comparator<V>{
    	private final AStarFunctionProvider<V> provider;
    	private final V goal;
    	
    	private VertexComparator(
            final AStarFunctionProvider<V> provider, final V goal){
    		this.provider = provider;
    		this.goal = goal;
    	}
    	
		@Override public int compare(final V o1, final V o2) {
			final Double o1Distance = provider.getPathCost(o1, goal);
            final Double o2Distance = provider.getPathCost(o2, goal);
            return o1Distance.compareTo(o2Distance);
		}
    }
}

// End AStarShortestPath.java
