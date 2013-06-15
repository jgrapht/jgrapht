package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.graph.AbstractBaseGraph;

public class KShortestPathsX<V, E> {

	//public List<List<E>> paths;
	
	private AbstractBaseGraph<V, E> graphBAK;
	private AbstractBaseGraph<V, E> graph;
	private V source;
	private V target;
	private int nPaths;
	private int nMaxHops;
	public List<List<E>> listA;
	private List<List<E>> listB;
	
	
	/**
	 * Create an object to find the k shortest simple paths for a graph
	 * @param graph Graph on which shortest paths are searched.
	 * @param s The source node.
	 * @param t The target node.
	 * @param k The number of best paths to find.
	 * @param hops The max hops of the paths to find. Assign a negative value to avoid this constraint.
	 */
	public KShortestPathsX(AbstractBaseGraph<V, E> g,
								V s,
								V t,
								int k,
								int hops){
		
		assertKShortestPathsFinder(g, s, t, k);
		
		//graph = (AbstractBaseGraph<V, E>) g.clone();
		graphBAK = g;
		source = s;
		target = t;
		nPaths = k;
		nMaxHops = hops;
		findKShortestPath();
	}
	
	@SuppressWarnings("unchecked")
	private void findKShortestPath(){
		listA = new LinkedList<List<E>>();
		listB = new LinkedList<List<E>>();
		
		
		List<E> shortestPath;
		shortestPath = getSPath(graphBAK, source, target);
		
		//no available path 
		if(shortestPath.size() == 0){
			return;
		}
		listA.add(shortestPath);
		
		while(listA.get(listA.size()-1).size() < nPaths){
			
			List<E> lastPath = listA.get(listA.size()-1);
			for(int i=0; i<lastPath.size(); i++)
			{
				//if(lastPath.size() > 1 && i==lastPath.size()-1)break;
				graph = (AbstractBaseGraph<V, E>) graphBAK.clone();
				List<E> root = createRoot(lastPath, i);
				alterGraph(root);
				
				V midNode;
				midNode = getMidNode(root);
				List<E> subPath;
				
				subPath = getSPath(graph, midNode, target);
				
				if(subPath != null){
					root.addAll(subPath);
					listB.add(root);
				}
			}
			if(listB.size()==0)return;
			listBtoListA();
		}
	}
	
	private void listBtoListA(){
		List<E> sp;
		sp = listB.get(0);
		for(int i=1; i<listB.size(); i++){
			if(sp.size() > listB.get(i).size()){
				sp = listB.get(i);
			}
		}
		
		listA.add(sp);
		listB.remove(sp);
	}
	
	private List<E> getSPath(AbstractBaseGraph<V, E> g, V s, V t){
		BellmanFordShortestPath<V, E> bellmanfordSP;
		
		if(nMaxHops > 0){
			bellmanfordSP = new BellmanFordShortestPath<V, E>(g, s, nMaxHops);
		}
		else{
			bellmanfordSP = new BellmanFordShortestPath<V, E>(g, s);
		}
		return bellmanfordSP.getPathEdgeList(t);
	}
	
	private V getMidNode(List<E> root){
		V A;
		V B;
		
		
		if(root.size()==0){
			return source;
		}
		A = graph.getEdgeSource(root.get(root.size()-1));
		B = graph.getEdgeTarget(root.get(root.size()-1));
		if(root.size() == 1){
			if(A.equals(source)){
				return B;
			}
			else{
				return A;
			}
		}
		else{
			if(A.equals(graph.getEdgeSource(root.get(root.size()-2)))
				||
				A.equals(graph.getEdgeTarget(root.get(root.size()-2)))){
				return B;
			}
			else{
				return A;
			}
		}
	}
	
	private List<E> createRoot(List<E> path, int n){
		List<E> root = new ArrayList<E>();
		
		for(int i=0; i<n; i++)
		{
			root.add(path.get(i));
		}
		return root;
	}
	
	/**
	 * change the graph according to root
	 * @param root
	 */
	private void alterGraph(List<E> root){
		List<E> prePath;
		//remove edges
		for(int i=0; i<listA.size(); i++){
			prePath = listA.get(i);
			if(prePath.size() == 1){
				graph.removeEdge(prePath.get(0));
			}
			else if(coincide(root, prePath)){
				for(int j=0; j<=root.size(); j++)
				{
					graph.removeEdge(prePath.get(j));
				}
			}
		}
		for(int i=0; i<listB.size(); i++){
			prePath = listB.get(i);
			if(prePath.size() == 1){
				graph.removeEdge(prePath.get(0));
			}
			else if(coincide(root, prePath)){
				for(int j=0; j<=root.size(); j++)
				{
					graph.removeEdge(prePath.get(j));
				}
			}
		}
		
		
		if(root.size()==0)return;
		//remove nodes
		List<V> nodes = new ArrayList<V>();
		nodes.add(source);
		V pre = source;
		V A;
		V B;
		for(int i=0; i<root.size()-1; i++){
			A = graph.getEdgeSource(root.get(i));
			B = graph.getEdgeTarget(root.get(i));
			
			if(A.equals(pre)){
				nodes.add(B);
				pre = B;
			}
			else{
				nodes.add(A);
				pre = A;
			}		
		}
		for(int i=0; i<nodes.size(); i++){
			graph.removeVertex(nodes.get(i));
		}
	}
	
	private boolean coincide(List<E> root, List<E> path){
		if(root.size() > path.size()){
			return false;
		}
		if(root.size() == 0){
			return true;
		}
		for(int i=0; i<root.size(); i++){
			if(root.get(i) != path.get(i))return false;
		}
		return true;
	}
	
	private void assertKShortestPathsFinder(AbstractBaseGraph<V, E> g,
										V s,
										V t,
										int k){
		if(g == null){
			throw new NullPointerException("graph is null");
		}
		if(g.vertexSet().contains(s) == false){
			throw new NullPointerException("source node does not exist");
		}
		if(g.vertexSet().contains(t) == false){
			throw new NullPointerException("target node does not exist");
		}
		if(k <= 0){
			throw new NullPointerException("nPaths is negative or 0");
		}
	}
	
	@Override
	public String toString(){
		return listA.toString();
	}
}
