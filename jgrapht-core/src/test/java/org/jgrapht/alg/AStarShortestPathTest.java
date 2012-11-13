package org.jgrapht.alg;

import junit.framework.TestCase;

import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.AStarShortestPath.AStarFunctionProvider;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class AStarShortestPathTest extends TestCase {
	private final int GRAPH_SIZE = 100;
	private WeightedGraph<Vector2, DefaultWeightedEdge> g;
	
	
	@Override public void setUp(){
		g = new SimpleWeightedGraph<Vector2, DefaultWeightedEdge>(
                DefaultWeightedEdge.class);
	}

	/**
	 * Constructs grid GRAPH_SIZE*GRAPH_SIZE and connects from 0,0 to 
	 * GRAPH_SIZE,GRAPH_SIZE thus the path is just about diagonal
	 */
	public void testShortestPath()
    {
        AStarShortestPath<Vector2, DefaultWeightedEdge> path;
        Vector2[] vectors = new Vector2[GRAPH_SIZE*GRAPH_SIZE];
    	for(int y=0; y<GRAPH_SIZE; y++)
    		for(int x=0; x<GRAPH_SIZE; x++){
        		vectors[y*GRAPH_SIZE + x] = new Vector2(x, y);
        		g.addVertex(vectors[y*GRAPH_SIZE + x]);
        		if(x>0)
        			g.addEdge(vectors[y*GRAPH_SIZE + x-1], vectors[y*GRAPH_SIZE + x]);
        		if(y>0)
        			g.addEdge(vectors[(y-1)*GRAPH_SIZE + x], vectors[y*GRAPH_SIZE + x]);
    		}

        path = new AStarShortestPath<Vector2, DefaultWeightedEdge>(g,
                vectors[0],
                vectors[vectors.length-1],
                new AStarAlgorithmProviderImpl());
        assertNotNull(path.getPathEdgeList());
        assertTrue(!path.getPathEdgeList().isEmpty());
        assertEquals(GRAPH_SIZE*2 - 2, path.getPathLength(), .1);
    }

    private class Vector2{
    	public double x,y;
    	
    	public Vector2(double x, double y) {
    		this.x = x;
    		this.y = y;
    	}
    	
    	public double dst (Vector2 goal) {
    		final double x_d = goal.x - x;
    		final double y_d = goal.y - y;
    		return Math.sqrt(x_d * x_d + y_d * y_d);
    	}

    	@Override public boolean equals (Object obj) {
    		if (this == obj) return true;
    		if (obj == null) return false;
    		if (getClass() != obj.getClass()) return false;
    		Vector2 other = (Vector2)obj;
    		if (x != other.x || y != other.y) return false;
    		return true;
    	}
    	
    	public String toString () {
    		return "[" + x + ":" + y + "]";
    	}
    }
    
    private class AStarAlgorithmProviderImpl implements AStarFunctionProvider<Vector2>{
		@Override public double getHeuristicCost(Vector2 neighbor, Vector2 goal) {
			return neighbor.dst(goal);
		}
		/**
		 * I'm lazy programmer-ing this one. Really I should be adding up all 
		 * the weights of the edges that connect neighbor to goal, but for
		 * Vector2 (and my purposes) this is pretty close. For performance, I'd
		 * imagine caching these maps would be in an optimal solution.
		 */
		@Override public double getPathCost(Vector2 neighbor, Vector2 goal) {
			return neighbor.dst(goal);
		}
    }
}
