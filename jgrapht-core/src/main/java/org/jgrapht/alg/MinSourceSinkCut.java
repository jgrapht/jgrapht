package org.jgrapht.alg;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Given a directed, weighted graph G(V,E). This class computes a minimum s-t cut. For this, it relies on 
 * the EdmondsKarpMaximumFlow implementation.
 * Note: it is not recommended to use this class to calculate the overall minimum cut in a graph by iteratively invoking
 * this class for all source-sink pairs. This is computationally expensive. Instead, use the StoerWagnerMinimumCut implementation.
 * @author Joris Kinable
 */
public class MinSourceSinkCut<V,E> {
	
	final EdmondsKarpMaximumFlow<V,E> ekMaxFlow;
	Set<V> minCut=null;
	final DirectedGraph<V, E> graph;
	double cutWeight;
	V source=null;
	V sink=null;
	double epsilon=EdmondsKarpMaximumFlow.DEFAULT_EPSILON;
	
	public MinSourceSinkCut(final DirectedGraph<V, E> graph){
        ekMaxFlow =new EdmondsKarpMaximumFlow<V,E>(graph);
		this.graph=graph;
	}
	public MinSourceSinkCut(final DirectedGraph<V, E> graph, final double epsilon){
        ekMaxFlow =new EdmondsKarpMaximumFlow<V,E>(graph);
		this.graph=graph;
		this.epsilon=epsilon;
	}
	
	/**
	 * Compute a minimum s-t cut
	 * @param source
	 * @param sink
	 * @return One partition of the minimum s-t cut
	 */
	public void computeMinCut(final V source, final V sink){
		this.source=source;
		this.sink=sink;
		minCut=new HashSet<V>();
		//First compute a maxFlow from source to sink
		ekMaxFlow.calculateMaximumFlow(source, sink);
        cutWeight =ekMaxFlow.getMaximumFlowValue();
		final Map<E, Double> maxFlow=ekMaxFlow.getMaximumFlow();
		
		final Queue<V> processQueue=new LinkedList<V>();
		processQueue.add(source);
		
		while(!processQueue.isEmpty()){
			final V vertex=processQueue.remove();
            if (minCut.contains(vertex))
                continue;
            minCut.add(vertex);
            //1. Get the forward edges with residual capacity
			final Set<E> outEdges=new HashSet<E>(graph.outgoingEdgesOf(vertex));
			for(Iterator<E> it=outEdges.iterator(); it.hasNext();){
				final E edge=it.next();
				final double edgeCapacity=graph.getEdgeWeight(edge);
				final double flowValue=maxFlow.get(edge);
				if(Math.abs(edgeCapacity-flowValue)<=epsilon) //No residual capacity on the edge
					it.remove();
			}
			for(final E edge: outEdges){
				processQueue.add(Graphs.getOppositeVertex(graph, edge, vertex));
			}
			
			//2. Get the backward edges with non-zero flow
			final Set<E> inEdges=new HashSet<E>(graph.incomingEdgesOf(vertex));
			for(Iterator<E> it=inEdges.iterator(); it.hasNext();){
				final E edge=it.next();
				//double edgeCapacity=graph.getEdgeWeight(edge);
				final double flowValue=maxFlow.get(edge);
				if(flowValue<=epsilon) //There is no flow on this edge
					it.remove();
			}
			for(final E edge: outEdges){
				processQueue.add(Graphs.getOppositeVertex(graph, edge, vertex));
			}
		}
		
	}
	
	/**
	 * 
	 * @return Returns the min cut partition containing the source, or null if there was no call to computeMinCut(V source, V sink)
	 */
	public Set<V> getSourcePartition(){
		return Collections.unmodifiableSet(minCut);
	}
	
	/**
	 * Returns the min cut partition containing the sink 
	 * @return returns the min cut partition containing the sink
	 */
	public Set<V> getSinkPartition(){
		if(minCut==null)
			return null;
		final Set<V> set=new HashSet<V>(graph.vertexSet());
		set.removeAll(minCut);
		return Collections.unmodifiableSet(set);
	}
	
	/**
	 * Get the cut weight. This is equal to the max s-t flow
	 * @return cut weight
	 */
	public double getCutWeight(){
		return cutWeight;
	}
	
	/**
	 * Let S be the set containing the source, and T be the set containing the sink, i.e. T=V\S.
	 * This method returns the edges which have their tail in S, and their head in T
	 * @return all edges which have their tail in S, and their head in T. If computeMinCut(V source, V sink) has not been invoked, this method returns null.
	 */
	public Set<E> getCutEdges(){
		if(minCut==null)
			return null;
		final Set<E> cutEdges=new HashSet<E>();
		for(final V vertex: minCut){
			for(final E edge: graph.outgoingEdgesOf(vertex)){
				if(!minCut.contains(Graphs.getOppositeVertex(graph, edge, vertex)))
					cutEdges.add(edge);
			}
		}
		return Collections.unmodifiableSet(cutEdges);
	}
	
	/**
	 * Returns the source of the last call
	 * @return source of last minCut call, null if there was no call
	 */
	public V getCurrentSource(){
		return source;
	}
	
	/**
	 * Returns the sink of the last call
	 * @return sink of last minCut call, null if there was no call
	 */
	public V getCurrentSink(){
		return sink;
	}
	
}
