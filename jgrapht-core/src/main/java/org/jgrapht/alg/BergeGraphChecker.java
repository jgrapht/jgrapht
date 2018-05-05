/*
 * (C) Copyright 2016-2018, by Philipp S. K&aumlsgen and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package main.java.org.jgrapht.alg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.generate.ComplementGraphGenerator;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.SimpleGraph;


/**
 * <p>Checker for Bergeness as described in the famous paper:
 * M. Chudnovsky, G. Cornuejols, X. Liu, P. Seymour, and K. Vuskovic. Recognizing Berge Graphs. Combinatorica 25(2): 143--186, 2003.
 * 
 * <p>Special Thanks to Maria Chudnovsky for her kind help.
 * 
 * @author Philipp S. K&aumlsgen (pkaesgen@freenet.de)
 * @since 2018
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class BergeGraphChecker<V,E>{
	
	/**
	 * Checks whether two vertices are neighbours
	 * @param g
	 * @param v1
	 * @param v2
	 * @return
	 */
	protected boolean hasEdge(Graph<V,E> g,V v1, V v2){
		return g.containsEdge(v1, v2);
	}
	
	/**
	 * Checks whether two paths which both intersect in the vertex m have another common vertex
	 * @param g
	 * @param s1
	 * @param s2
	 * @param m
	 * @return
	 */
	protected boolean haveNoEdgeDisregardingM(Graph<V,E> g,GraphPath<V,E> s1, GraphPath<V,E> s2,V m){
		for (V v1 : s1.getVertexList())
			if (v1 != m)
				for (V v2 : s2.getVertexList())
					if (v2 != m&&!hasEdge(g,v1,v2))
						return true;
						
		return false;
	}
	
	
	/**
	 * Lists the vertices which are covered by two paths
	 * @param g
	 * @param p1
	 * @param p2
	 * @return
	 */
	protected Set<V> intersectGraphPaths(Graph<V,E> g,GraphPath<V,E> p1, GraphPath<V,E> p2){
		Set<V> res = new HashSet<V>();
		res.addAll(p1.getVertexList());
		res.retainAll(p2.getVertexList());
		return res;
	}
	
	/**
	 * Assembles a GraphPath of the Graphs S and T. Required for the Pyramid Checker
	 * @param g
	 * @param S
	 * @param T
	 * @param M
	 * @param m
	 * @param b1
	 * @param b2
	 * @param b3
	 * @param s1
	 * @param s2
	 * @param s3
	 * @return
	 */
	protected GraphPath<V,E> P(Graph<V,E> g,GraphPath<V,E> S, GraphPath<V,E> T, Set<V> M, V m, V b1, V b2, V b3, V s1, V s2, V s3){
		if (S!=null&&T!=null){
			if (s1==b1){
				if (b1==m){
					return new GraphPath<V,E>(){
	
						@Override
						public Graph<V, E> getGraph() {
							return g;
						}
	
						@Override
						public V getStartVertex() {
							return g.getEdgeSource(getEdgeList().get(0));
						}
	
						@Override
						public V getEndVertex() {
							return g.getEdgeTarget(getEdgeList().get(0));
						}
	
						@Override
						public List<E> getEdgeList() {
							List<E> res = new LinkedList<E>();
							res.add(g.getEdge(s1, b1));
							res.add(g.getEdge(b1, s1));
							return res;
						}
	
						@Override
						public double getWeight() {
							return 1;
						}};
				}
				else{
					return null;
				}
			}
			else{
				if (b1!=m){
					M.add(b1);
					Set<V> intersection=intersectGraphPaths(g,S,T);
					M.remove(b1);
					if (!(hasEdge(g,m, b2)||hasEdge(g,m,b3)||hasEdge(g,m,s2)||hasEdge(g,m,s3))&&
							!S.getEdgeList().isEmpty()&&!T.getEdgeList().isEmpty()&&
							intersection.size()==1&&intersection.contains(m)
							&&haveNoEdgeDisregardingM(g,S, T, m)
							){
						List<E> edgeList =new LinkedList<E>();
						edgeList.addAll(S.getEdgeList());
						edgeList.addAll(T.getEdgeList());
						return new GraphPath<V,E>(){

							@Override
							public V getEndVertex() {
								return s1;
							}

							@Override
							public Graph<V, E> getGraph() {
								return g;
							}

							@Override
							public V getStartVertex() {
								return b1;
							}

							@Override
							public double getWeight() {
								return 0;
							}};//g, b1, s1, edgeList, 0)
					}
					else{
						return null;
					}
				}
				else{
					return null;
				}
			}
		}
		else{
			return null;
		}
	}
	
	
	
	/**
	 * Checks whether a graph contains a pyramid. Running time: O(|V(g)|^9)
	 * @param g Graph
	 * @return Either it finds a pyramid (and hence an odd hole) in g, or it determines that g contains no pyramid
	 */
	protected boolean containsPyramid(Graph<V,E> g){
		 /*b3,*/ 
		Set<Set<V>> visitedTriangles = new HashSet<Set<V>>();
		for (E e1 : g.edgeSet()){
			V b1= g.getEdgeSource(e1), b2= g.getEdgeTarget(e1);
			if (b1==b2) continue;
			for (E e2 : g.edgesOf(b1)){
				V b3 = g.getEdgeSource(e2);
				if (b3==b1)
					b3 = g.getEdgeTarget(e2);
				if (b3==b1||b3==b2||!hasEdge(g,b2,b3)) continue;
					
				//Triangle detected
				Set<V> triangles = new HashSet<V>();
				triangles.add(b1);
				triangles.add(b2);
				triangles.add(b3);
				if (visitedTriangles.contains(triangles)){
					continue;
				}
				visitedTriangles.add(triangles);
				
				for (V aCandidate : g.vertexSet()){
					if (//g.edgesOf(aCandidate).size()<3 ||
							aCandidate==b1||aCandidate==b2||aCandidate==b3||
							//a is adjacent to at most one of b1,b2,b3
							hasEdge(g,aCandidate,b1)&&hasEdge(g,aCandidate,b2)||
							hasEdge(g,aCandidate,b2)&&hasEdge(g,aCandidate,b3)||
							hasEdge(g,aCandidate,b1)&&hasEdge(g,aCandidate,b3)){
						continue;
					}
					
					for (E e4 : g.edgesOf(aCandidate)){
						V s1 = g.getEdgeSource(e4);
						if (s1==aCandidate)
							s1 = g.getEdgeTarget(e4);
						if (s1==b2||s1==b3||s1!=b1&&(hasEdge(g,s1,b2)||hasEdge(g,s1,b3))){
							continue;
						}
						
						for (E e5 : g.edgesOf(aCandidate)){
							V s2 = g.getEdgeSource(e5);
							if (s2==aCandidate)
								s2 = g.getEdgeTarget(e5);
							if (hasEdge(g,s1,s2)||s1==s2||s2==b1||s2==b3||s2!=b2&&(hasEdge(g,s2,b1)||hasEdge(g,s2,b3))){
								continue;
							}
							
							for (E e6 : g.edgesOf(aCandidate)){
								V s3 = g.getEdgeSource(e6);
								if (s3==aCandidate)
									s3 = g.getEdgeTarget(e6);
								if (hasEdge(g,s3,s2)||s1==s3||s3==s2||hasEdge(g,s1,s3)||s3==b1||s3==b2||s3!=b3&&(hasEdge(g,s3,b1)||hasEdge(g,s3,b2))){
									continue;
								}
								
								Set<V> M = new HashSet<V>(),M1 = new HashSet<V>(),M2 = new HashSet<V>(),M3 = new HashSet<V>();
								M.addAll(g.vertexSet());
								M.remove(b1);
								M.remove(b2);
								M.remove(b3);
								M.remove(s1);
								M.remove(s2);
								M.remove(s3);
								M1.addAll(M);
								M2.addAll(M);
								M3.addAll(M);
								M1.add(b1);
								M2.add(b2);
								M3.add(b3);
								
								Map<V,GraphPath<V, E>> 	S1=new HashMap<V,GraphPath<V,E>>(),
														S2=new HashMap<V,GraphPath<V,E>>(),
														S3=new HashMap<V,GraphPath<V,E>>(),
														T1=new HashMap<V,GraphPath<V,E>>(),
														T2=new HashMap<V,GraphPath<V,E>>(),
														T3=new HashMap<V,GraphPath<V,E>>();

								for (V m1 : M){
									Set<V> validInterior = new HashSet<V>();
									validInterior.addAll(M);
									Set<V> toRemove = new HashSet<V>();
									for (V i : validInterior){
										if (hasEdge(g,i,b2)||hasEdge(g,i,s2)||hasEdge(g,i,b3)||hasEdge(g,i,s3)){
											toRemove.add(i);
										}
									}
									validInterior.removeAll(toRemove);	
									validInterior.add(s1);
									Graph<V,E> subg = new AsSubgraph<V,E>(g,validInterior,null);
									if (subg.containsVertex(s1)&&subg.containsVertex(m1)){
										S1.put(m1,new DijkstraShortestPath<V, E>(subg).getPath(s1,m1));
										validInterior.remove(s1);
										validInterior.add(b1);
										subg = new AsSubgraph<V,E>(g,validInterior,null);
										if (subg.containsVertex(b1)&&subg.containsVertex(m1)){
											T1.put(m1, new DijkstraShortestPath<V, E>(subg).getPath(b1,m1));
										}
										else {
											S1.remove(m1);
										}
									}
								}
								for (V m2 : M){
									Set<V> validInterior = new HashSet<V>();
									validInterior.addAll(M);
									Set<V> toRemove = new HashSet<V>();
									for (V i : validInterior){
										if (hasEdge(g,i,b1)||hasEdge(g,i,s1)||hasEdge(g,i,b3)||hasEdge(g,i,s3)){
											toRemove.add(i);
										}
									}
									validInterior.removeAll(toRemove);
									validInterior.add(s2);
									Graph<V,E> subg = new AsSubgraph<V,E>(g,validInterior,null);
									if (subg.containsVertex(s2)&&subg.containsVertex(m2)){
										S2.put(m2,new DijkstraShortestPath<V, E>(subg).getPath(s2,m2));
										validInterior.remove(s2);
										validInterior.add(b2);
										subg = new AsSubgraph<V,E>(g,validInterior,null);
										if (subg.containsVertex(b2)&&subg.containsVertex(m2)){
											T2.put(m2,new DijkstraShortestPath<V, E>(subg).getPath(b2,m2));
										}
										else {
											S2.remove(m2);
										}
									}
								}
								for (V m3 : M){
									Set<V> validInterior = new HashSet<V>();
									validInterior.addAll(M);
									Set<V> toRemove = new HashSet<V>();
									for (V i : validInterior){
										if (hasEdge(g,i,b1)||hasEdge(g,i,s1)||hasEdge(g,i,b2)||hasEdge(g,i,s2)){
											toRemove.add(i);
										}
									}
									validInterior.removeAll(toRemove);
									validInterior.add(s3);
									Graph<V,E> subg = new AsSubgraph<V,E>(g,validInterior,null);
									if (subg.containsVertex(s3)&&subg.containsVertex(m3)){
										S3.put(m3,new DijkstraShortestPath<V, E>(subg).getPath(s3,m3));
										validInterior.remove(s3);
										validInterior.add(b3);
										subg = new AsSubgraph<V,E>(g,validInterior,null);
										if (subg.containsVertex(b3)&&subg.containsVertex(m3)){
											T3.put(m3,new DijkstraShortestPath<V, E>(subg).getPath(b3,m3));
										}
										else {
											S3.remove(m3);
										}
									}
								}
								
								
								for (V m1 : S1.keySet()){
									GraphPath<V,E> P1 = P(g,S1.get(m1),T1.get(m1),M,m1,b1,b2,b3,s1,s2,s3);
									if (P1!=null){
										for (V m2 : S2.keySet()){
											GraphPath<V,E> P2 = P(g,S2.get(m2),T2.get(m2),M,m2,b2,b1,b3,s2,s1,s3);
											if (P2!=null){
												for (V m3 : S3.keySet()){
													GraphPath<V,E> P3 = P(g,S3.get(m3),T3.get(m3),M,m3,b3,b1,b2,s3,s1,s2);
													if (P3!=null){
														System.err.println("Pyramid found");
														return true;
													}
												}
											}
										}
									}
								
								}
								
								
							}
							
						}
						
					}
							
						
					
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Finds all Components of a set F contained in V(g)
	 * @param g
	 * @param F
	 * @return
	 */
	protected List<Set<V>> findAllComponents(Graph<V,E> g, Set<V> F){
		return new ConnectivityInspector<V,E>(new AsSubgraph<V,E>(g,F,null)).connectedSets();
	}
	
	/**
	 * Checks whether a graph contains a Jewel. Running time: O(|V(g)|^6)
	 * @param g Graph
	 * @return Decides whether there is a jewel in g
	 */
	protected boolean containsJewel(Graph<V,E> g){
		for (E e23 : g.edgeSet()){
			V v2 = g.getEdgeSource(e23);
			V v3 = g.getEdgeTarget(e23);
			for (V v5 : g.vertexSet()){
				if (v2!=v5&&v3!=v5){
					Set<V> F = new HashSet<V>(),X1 = new HashSet<V>(),X2 = new HashSet<V>();
					for (V f : g.vertexSet()){
						if (f!=v2&&f!=v3&&f!=v5){
							if (!hasEdge(g,v2,f)&&!hasEdge(g,v3,f)&&!hasEdge(g,v5,f)){
								F.add(f);
							}
							if (hasEdge(g,v2,f)&&hasEdge(g,v5,f)&&!hasEdge(g,v3,f)){
								X1.add(f);
							}
							if (hasEdge(g,f,v3)&&hasEdge(g,v5,f)&&!hasEdge(g,v2,f)){
								X2.add(f);
							}
						}
					}
					List<Set<V>> componentsOfF = findAllComponents(g, F);
					
					for (V v1 : X1){
						for (Set<V> FPrime : componentsOfF){
							if (hasANeighbour(g, FPrime, v1)){
								for (V v4 : X2){
									if (v1!=v4&&!hasEdge(g,v1,v4)&&hasANeighbour(g,FPrime,v4)){
										System.err.println("Jewel found");
										return true;
									}
								}
							}
						}
					}	
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks whether a graph contains a clean shortest odd hole. Running time: O(|V(g)|^4)
	 * @param g Graph containing no pyramid or jewel
	 * @return Decides whether g contains a clean shortest odd hole
	 */
	protected boolean containsCleanShortestOddHole(Graph<V,E> g){
		for (V u : g.vertexSet()){
			for (V v : g.vertexSet()){
				if (u==v||hasEdge(g,u,v)) continue;
				
				GraphPath<V, E> puv = new DijkstraShortestPath<V, E>(g).getPath( u, v);
				if (puv==null) continue;
				
				for (V w : g.vertexSet()){
					if (w==u||w==v||hasEdge(g,w,u)||hasEdge(g,w,v)) continue;
					GraphPath<V,E> pvw = new DijkstraShortestPath<V,E>(g).getPath(v, w);
					if (pvw==null) continue;
					GraphPath<V,E> pwu = new DijkstraShortestPath<V,E>(g).getPath(w, u);
					if (pwu==null) continue;
					Set<V> set = new HashSet<V>();
					set.addAll(puv.getVertexList());
					set.addAll(pvw.getVertexList());
					set.addAll(pwu.getVertexList());
					Graph<V,E> subg = new AsSubgraph<>(g,set);
					if (set.size()<7||subg.vertexSet().size()!=set.size()||subg.edgeSet().size()!=subg.vertexSet().size()||subg.vertexSet().size()%2==0) continue;
					boolean isCircle=true;
					for (V t : subg.vertexSet()){
						if (subg.edgesOf(t).size()!=2){
							isCircle=false;
							break;
						}
					}
					if (!isCircle) continue;
					System.err.println("clean shortest odd hole");
					
					return true;
					
				}
				
			}
		}
		return false;
	}
	
	/**
	 * Returns a path in g from start to end avoiding the vertices in X
	 * @param g
	 * @param start
	 * @param end
	 * @param X
	 * @return
	 */
	protected GraphPath<V, E> getPathAvoidingX(Graph<V, E> g, V start, V end, Set<V> X){
		Set<V> vertexSet = new HashSet<V>();
		vertexSet.addAll(g.vertexSet());
		vertexSet.removeAll(X);
		vertexSet.add(start);
		vertexSet.add(end);
		Graph<V,E> subg = new AsSubgraph<V,E>(g,vertexSet,null);
		return new DijkstraShortestPath<V, E>(subg).getPath(start,end);
	}
	
	/**
	 * Checks whether the vertex set of a graph without a vertex set X contains a shortest odd hole. Running time: O(|V(g)|^4)
	 * @param g Graph containing neither pyramid nor jewel
	 * @param X Subset of V(g)
	 * @return Determines whether g has an odd hole such that X is a near-cleaner for it
	 */
	protected boolean containsShortestOddHole(Graph<V,E> g,Set<V> X){
		for (V y1 : g.vertexSet()){
			if (X.contains(y1)) continue;
			
			for(E e13 : g.edgeSet()){
				V x1 = g.getEdgeSource(e13);
				V x3 = g.getEdgeTarget(e13);
				if (x1==x3||x1==y1||x3==y1) continue;
				
				for (E e32 : g.edgesOf(x3)){
					V x2 = g.getEdgeTarget(e32);
					if (x2==x3){
						x2 = g.getEdgeSource(e32);
					}
					if (x2==x3||x2==x1||x2==y1||hasEdge(g,x2,x1)) continue;
					
					GraphPath<V, E> rx1y1 = getPathAvoidingX(g, x1, y1, X);
					GraphPath<V, E> rx2y1 = getPathAvoidingX(g, x2, y1, X);
					
					double n;
					if (rx1y1==null||rx2y1==null) continue;
					
					V y2 = null;
					for (V y2Candidate : rx2y1.getVertexList()){
						if (hasEdge(g,y1,y2Candidate)&&y2Candidate!=x1&&y2Candidate!=x2&&y2Candidate!=x3){
							y2=y2Candidate;
							break;
						}
					}
					if (y2==null) continue;
					
					GraphPath<V, E> rx3y1 = getPathAvoidingX(g, x3, y1, X);
					GraphPath<V, E> rx3y2 = getPathAvoidingX(g, x3, y2, X);
					GraphPath<V, E> rx1y2 = getPathAvoidingX(g, x1, y2, X);
					if (rx3y1!=null&&rx3y2!=null&&rx1y2!=null&& rx2y1.getLength()==(n=rx1y1.getLength()+1) && n==rx1y2.getLength() && rx3y1.getLength()>=n && rx3y2.getLength()>=n){
						System.err.println("shortest odd hole");
						return true;
					}
					
				}
			}
		}
		return false;
	}
	
	/**
	 * Checks whether a clean shortest odd hole is in g or whether X is a cleaner for an amenable shortest odd hole
	 * @param g A graph, containing no pyramid or jewel
	 * @param X A subset X of V(g)
	 * @return Returns whether g has an odd hole or there is no shortest odd hole in C such that X is a near-cleaner for C.
	 */
	protected boolean routine1(Graph<V,E> g,Set<V> X){
		return containsCleanShortestOddHole(g)||containsShortestOddHole(g, X);	
	}
	//=======================================================================================================================
	
	
	/**
	 * Checks whether a graph has a configuration of type 1. A configuration of type 1 in g is a hole of length 5
	 * @param g Graph
	 * @return
	 */
	protected boolean hasConfigurationType1(Graph<V,E> g){
		for (V v1 : g.vertexSet()){
			Set<V> temp = new ConnectivityInspector<V, E>(g).connectedSetOf(v1);
			for (V v2 : temp){
				if (v1==v2||!hasEdge(g,v1,v2)) continue;
				for (V v3 : temp){
					if (v3==v1||v3==v2||!hasEdge(g,v2,v3)||hasEdge(g,v1,v3)) continue;
					for (V v4 : temp){
						if (v4==v1||v4==v2||v4==v3||hasEdge(g,v1,v4)||hasEdge(g,v2,v4)||!hasEdge(g,v3,v4)) continue;
						for (V v5 : temp){
							if (v5==v1||v5==v2||v5==v3||v5==v4||hasEdge(g,v2,v5)||hasEdge(g,v3,v5)||!hasEdge(g,v1,v5)||!hasEdge(g,v4,v5)) continue;
							System.err.println("5-cycle found");
							return true;
						}
					}
				}
			}
		}
		
		
		return false;
	}
	
	
	/**
	 * A vertex y is X-complete if y contained in V(g)\X is adjacent to every vertex in X.
	 * @param g
	 * @param y
	 * @param X
	 * @return
	 */
	protected boolean isYXComplete(Graph<V,E> g, V y,Set<V> X){
		if (g.vertexSet().contains(y)&&!X.contains(y)){
			for (V x : X){
				if (!hasEdge(g,y,x)){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * Returns all anticomponents of a graph and a vertex set. 
	 * (A set X contained in V(G) is connected if G\X is connected (so the empty set is connected); and
	 * anticonnected if the complement graph of G without X is connected)
	 * @param g
	 * @param Y
	 * @return
	 */
	protected List<Set<V>> findAllAnticomponentsOfY(Graph<V,E> g, Set<V> Y){
		Graph<V,E> target;
		if (g.getType().isSimple()) target = new SimpleGraph<>(g.getEdgeFactory());
		else target = new Multigraph<>(g.getEdgeFactory());
		new ComplementGraphGenerator<>(g).generateGraph(target);
		
		return new ConnectivityInspector<V,E>(new AsSubgraph<V,E>(target,Y)).connectedSets();
	}
	
	/**
	 * <p>Checks whether a graph is of configuration type 2. A configuration of type 2 in g is a sequence v1,v2,v3,v4,P,X such that:</p>
	 * <ul>
	 * <li> v1-v2-v3-v4 is a path of g</li>
	 * <li> X is an anticomponent of the set of all {v1,v2,v4}-complete vertices</li>
	 * <li> P is a path in G\(X+{v2,v3}) between v1,v4, and no vertex in P*, i.e. P's interior, is X-complete or adjacent to v2 or adjacent to v3</li>
	 * </ul>
	 * An example is the complement graph of a cycle-7-graph
	 * @param g
	 * @return
	 */
	protected boolean hasConfigurationType2(Graph<V,E> g){
		for (E e12 : g.edgeSet()){
			V v1 = g.getEdgeSource(e12);
			V v2 = g.getEdgeTarget(e12);
			if (v1==v2) continue;
			
			for (E e23 : g.edgesOf(v2)){
				V v3 = g.getEdgeTarget(e23);
				if (v3==v2){
					v3 = g.getEdgeSource(e23);
				}
				if (v3==v2||v1==v3) continue;
				if (hasEdge(g,v2,v3)) continue;//TODO
				
				for (E e34 : g.edgesOf(v3)){
					V v4 = g.getEdgeTarget(e34);
					if(v4==v3){
						v4 = g.getEdgeSource(e34);
					}
					if (v4==v1||v4==v2||v4==v3) continue;
					if (hasEdge(g,v4,v2)||hasEdge(g,v4,v1)) continue; //TODO
					
					Set<V> temp = new HashSet<V>();
					temp.add(v1);
					temp.add(v2);
					temp.add(v4);
					Set<V> Y = new HashSet<V>();
					for (V y : g.vertexSet()){
						if (isYXComplete(g, y, temp)){
							Y.add(y);
						}
					}
					List<Set<V>> anticomponentsOfY = findAllAnticomponentsOfY(g, Y);
					for (Set<V> X : anticomponentsOfY){
						Set<V> v2v3 = new HashSet<V>();
						v2v3.addAll(g.vertexSet());
						v2v3.remove(v2);
						v2v3.remove(v3);
						v2v3.removeAll(X);
						if (!v2v3.contains(v1)||!v2v3.contains(v4))continue;
						
						boolean cont = false;
						for (Set<V> P : new ConnectivityInspector<>(new AsSubgraph<>(g,v2v3)).connectedSets()){
							
							if (!P.contains(v1)||!P.contains(v4)||!(new ConnectivityInspector<>(new AsSubgraph<>(g,v2v3)).pathExists(v1, v4))) continue;
							cont = true;
							for (V p : P){
								if (p!=v1&&p!=v4&&(hasEdge(g,p,v2)||hasEdge(g,p,v3)||isYXComplete(g,p,X))) {
									cont=false; break;
								}
							}
							if (cont){
								System.err.println("T2 found");
								return true;
								
							}
						}
					}
				}
				
			}
		}
		return false;
	}
	
	/**
	 * Reports whether v has at least one neighbour in set
	 * @param g
	 * @param set
	 * @param v
	 * @return
	 */
	protected boolean hasANeighbour(Graph<V,E> g, Set<V> set, V v){
		boolean res = false;
		for (V s : set){
			if (hasEdge(g,s,v)){
				res = true;
				break;
			}
		}
		return res;
	}
	
	/**
	 * For each anticomponent X, find the maximal connected subset F' containing v5 with the 
	 * properties that v1,v2 have no neighbours in F' and no vertex of F'\v5 is X-complete
	 */
	protected Set<V> findMaximalConnectedSubset(Graph<V,E> g, Set<V> X, V v1, V v2, V v5){
		Set<V> FPrime = new ConnectivityInspector<V,E>(g).connectedSetOf(v5);
		Set<V> toBeRemoved = new HashSet<V>();
		for (V f : FPrime){
			if (f!=v5&&isYXComplete(g, f, X)||v1==f||v2==f||hasEdge(g,v1,f)||hasEdge(g,v2,f)){
				toBeRemoved.add(f);
			}
		}
		FPrime.removeAll(toBeRemoved);
		return FPrime;
	}
	
	protected boolean hasANonneighbourInX(Graph<V,E> g, V v, Set<V> X){
		for (V x : X){
			if (!hasEdge(g,v,x)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * <p>Checks whether a graph is of configuration type 3. A configuration of type 3 in g is a sequence v1,...,v6,P,X such that</p>
	 * <ul>
	 * <li> v1,...,v6 are distinct vertices of g</li>
	 * <li> v1v2,v3v4,v2v3,v3v5,v4v6 are edges, and v1v3,v2v4,v1v5,v2v5,v1v6,v2v6,v4v5 are non-edges</li>
	 * <li> X is an anticomponent of the set of all {v1,v2,v5}-complete vertices, and v3,v4 are not X-complete</li>
	 * <li> P is a path of g\(X+{v1,v2,v3,v4}) between v5,v6, and no vertex in P* is X-complete or adjacent to v1 or adjacent to v2</li>
	 * <li> if v5v6 is an edge then v6 is not X-complete</li>
	 * </ul>
	 * @param g
	 * @return
	 */
	protected boolean hasConfigurationType3(Graph<V,E> g){
		for (V v1 : g.vertexSet()){
			for (V v2 : g.vertexSet()){
				if (v1==v2||!hasEdge(g,v1,v2)) continue;
				for (V v5 : g.vertexSet()){
					if (v1==v5||v2==v5||hasEdge(g,v1,v5)||hasEdge(g,v2,v5)) continue;
					Set<V> triple = new HashSet<V>();
					triple.add(v1);
					triple.add(v2);
					triple.add(v5);
					Set<V> Y = new HashSet<V>();
					for (V y : g.vertexSet()){
						if (isYXComplete(g,y,triple)){
							Y.add(y);
						}
					}
					List<Set<V>> anticomponents = findAllAnticomponentsOfY(g, Y);
					for (Set<V> X : anticomponents){
						Set<V> FPrime = findMaximalConnectedSubset(g, X, v1, v2, v5);
						Set<V> F = new HashSet<V>();
						F.addAll(FPrime);
						for (V x : X){
							if (!hasEdge(g,x,v1)&&!hasEdge(g,x,v2)&&!hasEdge(g,x,v5)&&hasANeighbour(g,FPrime,x))
								F.add(x);
						}
						
						for (V v4 : g.vertexSet()){
							if (v4==v1||v4==v2||v4==v5||hasEdge(g,v2,v4)||hasEdge(g,v5,v4)||!hasEdge(g,v1,v4)||
									!hasANeighbour(g, F, v4)||
									!hasANonneighbourInX(g, v4, X)||isYXComplete(g, v4, X)) continue;
							
							for (V v3 : g.vertexSet()){
								if (v3==v1||v3==v2||v3==v4||v3==v5||!hasEdge(g,v2,v3)||!hasEdge(g,v3,v4)||!hasEdge(g,v5,v3)||hasEdge(g,v1,v3)||!hasANonneighbourInX(g, v3, X)||isYXComplete(g, v3, X)) continue;
								for (V v6 : F){
									if (v6==v1||v6==v2||v6==v3||v6==v4||v6==v5||!hasEdge(g,v4,v6)||hasEdge(g,v1,v6)||hasEdge(g,v2,v6)||hasEdge(g,v5,v6)&&!isYXComplete(g, v6, X)) continue;
									Set<V> verticesForPv5v6 = new HashSet<V>();
									verticesForPv5v6.addAll(FPrime);
									verticesForPv5v6.add(v5);
									verticesForPv5v6.add(v6);
									verticesForPv5v6.remove(v1);
									verticesForPv5v6.remove(v2);
									verticesForPv5v6.remove(v3);
									verticesForPv5v6.remove(v4);
		
									if (new ConnectivityInspector<V,E>(new AsSubgraph<V,E>(g,verticesForPv5v6)).pathExists(v6, v5)){
										System.err.println("T3 found");
										return true;
									}
								
								}
								
								
							}
							
						
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * If true, the graph is not Berge. Checks whether g contains a Pyramid, Jewel, configuration type 1, 2 or 3.
	 * @param g
	 * @return
	 */
	protected boolean routine2(Graph<V,E> g){
		return containsPyramid(g)||containsJewel(g)||hasConfigurationType1(g)||hasConfigurationType2(g)||hasConfigurationType3(g);
	}
	//=======================================================================================================================
	
	/**
	 * N(a,b) is the set of all {a,b}-complete vertices
	 * @param g
	 * @param a
	 * @param b
	 * @return
	 */
	protected Set<V> N(Graph<V,E> g, V a, V b){
		Set<V> res = new HashSet<V>();
		Set<V> ab = new HashSet<V>();
		ab.add(a);
		ab.add(b);
		for (V c : g.vertexSet()){
			if (isYXComplete(g, c, ab)) res.add(c);
		}
		return res;
	}
	
	/**
	 * r(a,b,c) is the cardinality of the largest anticomponent of N(a,b) that contains a nonneighbour of c (or 0, if c is N(a,b)-complete)
	 * @param g
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	protected int r(Graph<V,E> g, Set<V> Nab, V c){
		if (isYXComplete(g,c,Nab)) return 0;
		List<Set<V>> anticomponents = findAllAnticomponentsOfY(g, Nab);
		int res = 0;
		for (Set<V> set : anticomponents){
			if (!hasANonneighbourInX(g, c, set)) continue;
			if (set.size()>res) res=set.size();
		}
		return res;
	}
	
	/**
	 * Y(a,b,c) is the union of all anticomponents of N(a,b) that have cardinality strictly greater than r(a,b,c)
	 * @param g
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	protected Set<V> Y(Graph<V,E> g, Set<V> Nab, V c){
		int cutoff = r(g,Nab,c);
		List<Set<V>> anticomponents = findAllAnticomponentsOfY(g, Nab);
		Set<V> res = new HashSet<V>();
		for (Set<V> anticomponent : anticomponents){
			if (anticomponent.size()>cutoff){
				res.addAll(anticomponent);
			}
		}
		return res;
	}
	
	/**
	 * W(a,b,c) is the anticomponent of N(a,b)+{c} that contains c
	 * @param g
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	protected Set<V> W(Graph<V,E> g, Set<V> Nab, V c){
		Set<V> temp = new HashSet<V>();
		temp.addAll(Nab);
		temp.add(c);
		List<Set<V>> anticomponents = findAllAnticomponentsOfY(g, temp);
		for (Set<V> anticomponent : anticomponents)
			if (anticomponent.contains(c))
				return anticomponent;
		return null;
	}
	
	/**
	 * Z(a,b,c) is the set of all (Y(a,b,c)+W(a,b,c))-complete vertices
	 * @param g
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	protected Set<V> Z(Graph<V,E> g, Set<V> Nab, V c){
		Set<V> temp = new HashSet<V>();
		temp.addAll(Y(g,Nab,c));
		temp.addAll(W(g,Nab,c));
		Set<V> res = new HashSet<V>();
		for (V it : g.vertexSet()){
			if (isYXComplete(g, it, temp))
				res.add(it);
		}
		return res;
	}
	
	/**
	 * X(a,b,c)=Y(a,b,c)+Z(a,b,c)
	 * @param g
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	protected Set<V> X(Graph<V,E> g, Set<V> Nab, V c){
		Set<V> res = new HashSet<V>();
		res.addAll(Y(g,Nab,c));
		res.addAll(Z(g,Nab,c));
		return res;
	}
	
	/**
	 * A triple (a,b,c) of vertices is relevant if a,b are distinct and nonadjacent, and c is not contained in N(a,b) (possibly
	 * c is contained in {a,b}).
	 * @param g
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	protected boolean isTripleRelevant(Graph<V,E> g, V a, V b, V c){
		return a!=b&&!hasEdge(g,a,b)&&!N(g,a,b).contains(c);
	}
	
	
	/**
	 * Returns a set of vertex sets that may be near-cleaners for an amenable hole in g.
	 * @param g
	 * @return
	 */
	protected Set<Set<V>> routine3(Graph<V,E> g){
		Set<Set<V>> NuvList = new HashSet<Set<V>>();
		for (V u : g.vertexSet()){
			for (V v : g.vertexSet()){
				if (u==v||!hasEdge(g,u,v)) continue;
				NuvList.add(N(g,u,v));
			}
		}
		
		Set<Set<V>> tripleList = new HashSet<Set<V>>();
		for (V a : g.vertexSet()){
			for (V b : g.vertexSet()){
				if (a==b||hasEdge(g,a,b)) continue;
				Set<V> Nab = N(g,a,b);
				for (V c : g.vertexSet()){
					if (isTripleRelevant(g,a,b,c)){
						tripleList.add(X(g,Nab,c));
					}
				}
			}
		}
		Set<Set<V>> res = new HashSet<Set<V>>();
		for (Set<V> Nuv : NuvList){
			for (Set<V> triple : tripleList){
				Set<V> temp = new HashSet<V>();
				temp.addAll(Nuv);
				temp.addAll(triple);
				res.add(temp);
			}
		}
		return res;
	}
	
	
	/**
	 * Performs the Berge Recognition Algorithm.
	 * <p> First this algorithm is used to test whether g or its complement contain a jewel, a pyramid or a configuration of type
	 * 1, 2 or 3. If so, it is output that g is not Berge. If not, then every shortest odd hole in g is amenable. This asserted, the near-cleaner subsets
	 * of V(g) are determined. For each of them in turn it is checked, if this subset is a near-cleaner and, thus, if there is an odd hole. If 
	 * an odd hole is found, this checker will output that g is not Berge. If no odd hole is found, all near-cleaners for the complement graph are determined
	 * and it will be proceeded as before. If again no odd hole is detected, g is Berge.
	 */
	public boolean isBerge(Graph<V,E> g){
		Graph<V,E> complementGraph;
		if (g.getType().isSimple()) complementGraph = new SimpleGraph<V,E>(g.getEdgeFactory());
		else complementGraph = new Multigraph<V,E>(g.getEdgeFactory());
		new ComplementGraphGenerator<V,E>(g).generateGraph(complementGraph);
		
		if (routine2(g)) {
			System.err.print("Routine 2 failed");
			return false;
		}
		if (routine2(complementGraph)) {
			System.err.println("Routine 2 failed on complement");
			return false;
		}
		
		for (Set<V> it : routine3(g)){
			if (routine1(g,it)) {
				System.err.println("Routine 1 failed");
				return false;
			}
		}
		
		for (Set<V> it : routine3(complementGraph)){
			if (routine1(complementGraph,it)){
				System.err.println("Routine 1 failed on complement");
				return false;
			}
		}
		
		return true;
		
	}
	
	

}
