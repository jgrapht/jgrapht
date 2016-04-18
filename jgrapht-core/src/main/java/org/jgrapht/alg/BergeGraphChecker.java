package org.jgrapht.alg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.generate.ComplementGraphGenerator;
import org.jgrapht.graph.GraphPathImpl;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.UndirectedSubgraph;

/**
 * <p>Checker for Bergeness as described in the famous paper "Recognizing Berge Graphs" (2003) by Chudnovsky, Cornuejols, Liu, Seymour and Vuskovic.
 * 
 * <p>Testing for Bergeness may come in handy as this graph class is equivalent to the perfect graph class. If a graph is Berge 
 * many problems such as the Clique Problems or the Maximum Independent Set Problem can be solved in polynomial time.
 * @author Philipp S. K&aumlsgen
 *
 * @param <V> Generic for vertices
 * @param <E> Generic for edges
 */
public class BergeGraphChecker<V,E>{
	private final SimpleGraph<V,E> g;
	
	/**
	 * Constructor of the BergeGraphChecker which requires a SimpleGraph
	 * @param graph
	 */
	public BergeGraphChecker(SimpleGraph<V,E> graph){//maybe also input complementgraph of graph
		super();
		g = graph;
	}
	
	/**
	 * Checks whether two vertices are neighbours
	 * @param g
	 * @param v1
	 * @param v2
	 * @return
	 */
	private boolean hasEdge(SimpleGraph<V,E> g,V v1, V v2){
		return g.containsEdge(v1, v2)||g.containsEdge(v2,v1);
	}
	
	/**
	 * Checks whether two paths which both intersect in the vertex m have another common vertex
	 * @param g
	 * @param s1
	 * @param s2
	 * @param m
	 * @return
	 */
	private boolean haveNoEdgeDisregardingM(SimpleGraph<V,E> g,GraphPath<V,E> s1, GraphPath<V,E> s2,V m){
		for (V v1 : V(g,s1))
			if (v1 != m)
				for (V v2 : V(g,s2))
					if (v2 != m&&!hasEdge(g,v1,v2))
						return true;
						
		return false;
	}
	
	/**
	 * Extracts the vertices of a given Path
	 * @param g
	 * @param p
	 * @return
	 */
	private Set<V> V(UndirectedGraph<V,E> g,GraphPath<V,E> p){
		Set<V> res = new HashSet<V>();
		for (E e : p.getEdgeList()){
			res.add(g.getEdgeSource(e));
			res.add(g.getEdgeTarget(e));
		}
		return res;
	}
	
	/**
	 * Lists the vertices which are covered by two paths
	 * @param g
	 * @param p1
	 * @param p2
	 * @return
	 */
	private Set<V> intersectGraphPaths(SimpleGraph<V,E> g,GraphPath<V,E> p1, GraphPath<V,E> p2){
		Set<V> res = V(g,p1);
		Set<V> temp = V(g,p2);
		res.retainAll(temp);
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
	private GraphPath<V,E> P(SimpleGraph<V,E> g,GraphPath<V,E> S, GraphPath<V,E> T, Set<V> M, V m, V b1, V b2, V b3, V s1, V s2, V s3){
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
							List<E> res = new ArrayList<E>();
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
						List<E> edgeList =new ArrayList<E>();
						edgeList.addAll(S.getEdgeList());
						edgeList.addAll(T.getEdgeList());
						return new GraphPathImpl<V,E>(g, b1, s1, edgeList, 0);
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
	 * @param g SimpleGraph
	 * @return Either it finds a pyramid (and hence an odd hole) in g, or it determines that g contains no pyramid
	 */
	private boolean containsPyramid(SimpleGraph<V,E> g){
		 /*b3,*/ 
		Set<Set<E>> visitedTriangles = new HashSet<Set<E>>();
		for (E e1 : g.edgeSet()){
			V b1= g.getEdgeSource(e1), b2= g.getEdgeTarget(e1);
			for (E e2 : g.edgesOf(b1)){
				V b3 = g.getEdgeSource(e2);
				if (b3==b1)
					b3 = g.getEdgeTarget(e2);
				if (e1!=e2&&hasEdge(g,b2,b3)){//Triangle detected
					Set<E> triangles = new HashSet<E>();
					triangles.add(e1);
					triangles.add(e2);
					triangles.add(g.getEdge(b3, b2));
					triangles.add(g.getEdge(b2, b3));
					if (!visitedTriangles.contains(triangles)){
					visitedTriangles.add(triangles);
					
					for (V aCandidate : g.vertexSet()){
						if (g.edgesOf(aCandidate).size()>2){
							for (E e4 : g.edgesOf(aCandidate)){
								V s1 = g.getEdgeSource(e4);
								if (s1==aCandidate)
									s1 = g.getEdgeTarget(e4);
								if (b1==s1||!(hasEdge(g,s1,b2)||hasEdge(g,s1,b3))){
									for (E e5 : g.edgesOf(aCandidate)){
										if (e5!=e4){
											V s2 = g.getEdgeSource(e5);
											if (s2==aCandidate)
												s2 = g.getEdgeTarget(e5);
											if (b1!=s1&&b2==s2||!(hasEdge(g,s2,b1)||hasEdge(g,s2,b3)||hasEdge(g,s2,s1))){
												for (E e6 : g.edgesOf(aCandidate)){
													if (e6!=e5&&e6!=e4){
														V s3 = g.getEdgeSource(e6);
														if (s3==aCandidate)
															s3 = g.getEdgeTarget(e6);
														if (b1!=s1&&b2!=s2&&b3==s3||!(hasEdge(g,s3,b1)||hasEdge(g,s3,b2)||hasEdge(g,s3,s1)||hasEdge(g,s3,s2))){
															Set<V> M = new HashSet<V>(),M1 = new HashSet<V>(),M2 = new HashSet<V>(),M3 = new HashSet<V>();
															M.addAll(g.vertexSet());
															M.remove(b1);
															M.remove(b2);
															M.remove(b3);
															M.remove(s1);
															M.remove(s2);
															M.remove(s3);
															M.remove(aCandidate);
															M1.addAll(M);
															M2.addAll(M);
															M3.addAll(M);
															M1.add(b1);
															M2.add(b2);
															M3.add(b3);
															
															Map<V,DijkstraShortestPath<V, E>> S1=new HashMap<V,DijkstraShortestPath<V,E>>(),S2=new HashMap<V,DijkstraShortestPath<V,E>>(),S3=new HashMap<V,DijkstraShortestPath<V,E>>(),T1=new HashMap<V,DijkstraShortestPath<V,E>>(),T2=new HashMap<V,DijkstraShortestPath<V,E>>(),T3=new HashMap<V,DijkstraShortestPath<V,E>>();

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
																UndirectedSubgraph<V,E> subg = new UndirectedSubgraph<V,E>(g,validInterior,null);
																if (subg.containsVertex(s1)&&subg.containsVertex(m1)){
																	S1.put(m1,new DijkstraShortestPath<V, E>(subg,s1,m1));
																	validInterior.remove(s1);
																	validInterior.add(b1);
																	subg = new UndirectedSubgraph<V,E>(g,validInterior,null);
																	if (subg.containsVertex(b1)&&subg.containsVertex(m1)){
																		T1.put(m1, new DijkstraShortestPath<V, E>(subg,b1,m1));
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
																UndirectedSubgraph<V,E> subg = new UndirectedSubgraph<V,E>(g,validInterior,null);
																if (subg.containsVertex(s2)&&subg.containsVertex(m2)){
																	S2.put(m2,new DijkstraShortestPath<V, E>(subg,s2,m2));
																	validInterior.remove(s2);
																	validInterior.add(b2);
																	subg = new UndirectedSubgraph<V,E>(g,validInterior,null);
																	if (subg.containsVertex(b2)&&subg.containsVertex(m2)){
																		T2.put(m2,new DijkstraShortestPath<V, E>(subg,b2,m2));
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
																UndirectedSubgraph<V,E> subg = new UndirectedSubgraph<V,E>(g,validInterior,null);
																if (subg.containsVertex(s3)&&subg.containsVertex(m3)){
																	S3.put(m3,new DijkstraShortestPath<V, E>(subg,s3,m3));
																	validInterior.remove(s3);
																	validInterior.add(b3);
																	subg = new UndirectedSubgraph<V,E>(g,validInterior,null);
																	if (subg.containsVertex(b3)&&subg.containsVertex(m3)){
																		T3.put(m3,new DijkstraShortestPath<V, E>(subg,b3,m3));
																	}
																	else {
																		S3.remove(m3);
																	}
																}
															}
															
															
															//Set<VertexPair<V>> good12Pairs=new HashSet<VertexPair<V>>(),good13Pairs=new HashSet<VertexPair<V>>(),good23Pairs=new HashSet<VertexPair<V>>();
															for (V m1 : S1.keySet()){
																GraphPath<V,E> P1 = P(g,S1.get(m1).getPath(),T1.get(m1).getPath(),M,m1,b1,b2,b3,s1,s2,s3);
																if (P1!=null){
																	for (V m2 : S2.keySet()){
																		GraphPath<V,E> P2 = P(g,S2.get(m2).getPath(),T2.get(m2).getPath(),M,m2,b2,b1,b3,s2,s1,s3);
																		if (P2!=null){
																			//good12Pairs.add(new VertexPair<V>(m1,m2));
																			for (V m3 : S3.keySet()){
																				GraphPath<V,E> P3 = P(g,S3.get(m3).getPath(),T3.get(m3).getPath(),M,m3,b3,b1,b2,s3,s1,s2);
																				if (P3!=null){
																					//good13Pairs.add(new VertexPair<V>(m1,m3));
																					System.err.println("Pyramid detected :"+b1+b2+b3+s1+s2+s3+aCandidate);
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
	private List<Set<V>> findAllComponents(SimpleGraph<V,E> g, Set<V> F){
		return new ConnectivityInspector<V,E>(new UndirectedSubgraph<V,E>(g,F,null)).connectedSets();
	}
	

	private boolean hasANeighbourInF(SimpleGraph<V,E> g, V v, Set<V> F){
		for (V f : F){
			if (hasEdge(g,v,f)){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Checks whether a graph contains a Jewel. Running time: O(|V(g)|^6)
	 * @param g SimpleGraph
	 * @return Decides whether there is a jewel in g
	 */
	private boolean containsJewel(final SimpleGraph<V,E> g){
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
							if (hasANeighbourInF(g, v1, FPrime)){
								for (V v4 : X2){
									if (v1!=v4&&!hasEdge(g,v1,v4)&&hasANeighbourInF(g,v4,FPrime)){
										System.err.println("Jewel detected: "+v1+", "+v2+", "+v3+", "+v4+", "+v5+", "+FPrime);
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
	 * @param g SimpleGraph containing no pyramid or jewel
	 * @return Decides whether g contains a clean shortest odd hole
	 */
	private boolean containsCleanShortestOddHole(SimpleGraph<V,E> g){
		Set<V> inTheRing = new HashSet<V>();
		for (V u : g.vertexSet()){
			for (V v : g.vertexSet()){
				if (u!=v){
					DijkstraShortestPath<V, E> puv = new DijkstraShortestPath<V, E>(g, u, v);
					if (puv.getPath()!=null){
						inTheRing.addAll(V(g,puv.getPath()));
						for (V w : g.vertexSet()){
							if (u!=w&&v!=w&&!inTheRing.contains(w)){
								DijkstraShortestPath<V, E> pvw = getPathAvoidingX(g, v, w, inTheRing);//new DijkstraShortestPath<V, E>(g, v, w);
								if (pvw.getPath()!=null){
									inTheRing.addAll(V(g,pvw.getPath()));
									
									DijkstraShortestPath<V, E> pwu = getPathAvoidingX(g, w, u, inTheRing);//new DijkstraShortestPath<V, E>(subg, w, u);
									if (pwu.getPath()!=null){
										Set<V> uvwVertices = new HashSet<V>();
										uvwVertices.addAll(V(g,puv.getPath()));
										uvwVertices.addAll(V(g,pvw.getPath()));
										uvwVertices.addAll(V(g,pwu.getPath()));
										UndirectedSubgraph<V,E> subg = new UndirectedSubgraph<V,E>(g,uvwVertices,null);
										
										int length = subg.edgeSet().size();
										
										if (length==subg.vertexSet().size()&&length>6&&length%2==1){
											System.err.println("Clean shortest odd hole detected: "+u+", "+v+", "+w+": "+puv.getPathEdgeList()+pvw.getPathEdgeList()+pwu.getPathEdgeList());
											return true;
										}
									}
									inTheRing.removeAll(V(g,pvw.getPath()));
								}
							}
						}
						inTheRing.clear();
					}
				}
			}
		}
		return false;
	}
	
	
	private DijkstraShortestPath<V, E> getPathAvoidingX(SimpleGraph<V, E> g, V start, V end, Set<V> X){
		Set<V> vertexSet = new HashSet<V>();
		vertexSet.addAll(g.vertexSet());
		vertexSet.removeAll(X);
		vertexSet.add(start);
		vertexSet.add(end);
		UndirectedSubgraph<V,E> subg = new UndirectedSubgraph<V,E>(g,vertexSet,null);
		return new DijkstraShortestPath<V, E>(subg,start,end);
	}
	
	/**
	 * Checks whether the vertex set of a graph without a vertex set X contains a shortest odd hole. Running time: O(|V(g)|^4)
	 * @param g SimpleGraph containing no pyramid or jewel
	 * @param X Subset of V(g)
	 * @return Determines whether g has an odd hole such that X is a near-cleaner for it
	 */
	private boolean containsShortestOddHole(SimpleGraph<V,E> g,Set<V> X){
		for (V y1 : g.vertexSet()){
			if (!X.contains(y1)){
				for(E e13 : g.edgeSet()){
					V x1 = g.getEdgeSource(e13);
					V x3 = g.getEdgeTarget(e13);
					if (x1!=y1&&x3!=y1){
						for (E e32 : g.edgesOf(x3)){
							V x2 = g.getEdgeTarget(e32);
							if (x2==x3){
								x2 = g.getEdgeSource(e32);
							}
							if (x2!=x1&&x2!=y1&&!hasEdge(g,x2,x1)){
								DijkstraShortestPath<V, E> rx1y1 = getPathAvoidingX(g, x1, y1, X);
								DijkstraShortestPath<V, E> rx2y1 = getPathAvoidingX(g, x2, y1, X);
								
								double n;
								if (rx1y1.getPath()!=null&&rx2y1.getPath()!=null){
									V y2 = null;
									for (V y2Candidate : V(g,rx2y1.getPath())){
										if (hasEdge(g,y1,y2Candidate)&&y2Candidate!=x1&&y2Candidate!=x2&&y2Candidate!=x3){
											y2=y2Candidate;
											break;
										}
									}
									if (y2!=null){
										DijkstraShortestPath<V, E> rx3y1 = getPathAvoidingX(g, x3, y1, X);
										DijkstraShortestPath<V, E> rx3y2 = getPathAvoidingX(g, x3, y2, X);
										DijkstraShortestPath<V, E> rx1y2 = getPathAvoidingX(g, x1, y2, X);
										if (rx3y1.getPath()!=null&&rx3y2.getPath()!=null&&rx1y2.getPath()!=null&& rx2y1.getPathLength()==(n=rx1y1.getPathLength()+1) && n==rx1y2.getPathLength() && rx3y1.getPathLength()>=n && rx3y2.getPathLength()>=n){
											System.err.println("Shortest odd hole detected: "+y1+", "+y2+", "+x1+", "+x2+", "+x3+": "+V(g,rx1y1.getPath())+V(g,rx2y1.getPath())+V(g,rx3y1.getPath())+V(g,rx1y2.getPath())+V(g,rx3y2.getPath())+" with near-scleaner "+X);
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
		return false;
	}
	
	/**
	 * Checks whether a clean shortest odd hole is in g or whether X is a cleaner for an amenable shortest odd hole
	 * @param g A graph, containing no pyramid or jewel
	 * @param X A subset X of V(g)
	 * @return Returns whether g has an odd hole or there is no shortest odd hole in C such that X is a near-cleaner for C.
	 */
	private boolean routine1(SimpleGraph<V,E> g,Set<V> X){
		return containsCleanShortestOddHole(g)||containsShortestOddHole(g, X);	
	}
	//=======================================================================================================================
	
	
	/**
	 * Checks whether a graph has a configuration of type 1. A configuration of type 1 in g is a hole of length 5
	 * @param g SimpleGraph
	 * @return
	 */
	private boolean hasConfigurationType1(SimpleGraph<V,E> g){
		Set<V> inTheRing = new HashSet<V>();
		for (E e12 : g.edgeSet()){
			V v1 = g.getEdgeSource(e12), v2 = g.getEdgeTarget(e12);
			inTheRing.add(v1);
			inTheRing.add(v2);
			for (E e23 : g.edgesOf(v2)){
				V v3 = g.getEdgeSource(e23);
				if (v2==v3)
					v3 = g.getEdgeTarget(e23);
				if (!inTheRing.contains(v3)&&!hasEdge(g,v1,v3)){
					inTheRing.add(v3);
					for (E e34 : g.edgesOf(v3)){
						V v4 = g.getEdgeSource(e34);
						if (v4==v3)
							v4 = g.getEdgeTarget(e34);
						if (!inTheRing.contains(v4)&&!hasEdge(g,v4,v1)&&!hasEdge(g,v4,v2)){
							inTheRing.add(v4);
							for (E e45 : g.edgesOf(v4)){
								V v5 = g.getEdgeSource(e45);
								if (v5==v4)
									v5 = g.getEdgeTarget(e45);
								if (!inTheRing.contains(v5)&&!hasEdge(g,v5,v2)&&!hasEdge(g,v5,v3)&&hasEdge(g,v5,v1)){
									inTheRing.add(v5);
									System.err.println("5-Cycle detected: "+inTheRing);
									return true;
								}
							}
							inTheRing.remove(v4);
						}
					}
					inTheRing.remove(v3);
				}
			}
			inTheRing.clear();
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
	private boolean isYXComplete(SimpleGraph<V,E> g, V y,Set<V> X){
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
	 * Returns all anticomponents of a graph and a vertex set. (A set X contained in V(G) is connected if G\X is connected (so the empty set is connected); and
	 * anticonnected if the complement graph of G without X is connected)
	 * @param g
	 * @param Y
	 * @return
	 */
	private List<Set<V>> findAllAnticomponentsOfY(SimpleGraph<V,E> g, Set<V> Y){
		SimpleGraph<V,E> temp = new SimpleGraph<V,E>(g.getEdgeFactory());
		new ComplementGraphGenerator<V,E>(new UndirectedSubgraph<V,E>(g, Y, null)).generateGraph(temp, null, null);
		return findAllComponents(temp, Y);
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
	private boolean hasConfigurationType2(SimpleGraph<V,E> g){
		for (E e12 : g.edgeSet()){
			V v1 = g.getEdgeSource(e12);
			V v2 = g.getEdgeTarget(e12);
			
			for (E e23 : g.edgesOf(v2)){
				V v3 = g.getEdgeTarget(e23);
				if (v3==v2){
					v3 = g.getEdgeSource(e23);
				}
				if (v3!=v1){
					for (E e34 : g.edgesOf(v3)){
						V v4 = g.getEdgeTarget(e34);
						if(v4==v3){
							v4 = g.getEdgeSource(e34);
						}
						if (v4!=v1&&v4!=v2){
							Set<V> temp = new HashSet<V>();
							temp.add(v1);
							temp.add(v2);
							temp.add(v4);
							Set<V> Y = new HashSet<V>();
							for (V y : g.vertexSet()){
								if (y!=v3&&isYXComplete(g, y, temp)){
									Y.add(y);
								}
							}
							List<Set<V>> anticomponentsOfY = findAllAnticomponentsOfY(g, Y);
							for (Set<V> X : anticomponentsOfY){
								Set<V> v2v3 = new HashSet<V>();
								v2v3.add(v2);
								v2v3.add(v3);
								v2v3.addAll(X);
								DijkstraShortestPath<V, E> P = getPathAvoidingX(g, v1, v4, v2v3);
								if (P.getPath()!=null&&P.getPathLength()>1){
									Set<V> nodesOfP = V(g,P.getPath());
									nodesOfP.remove(v1);
									nodesOfP.remove(v4);
									boolean nonadjacentToV2V3AndNotXComplete = false;
									for (V p : nodesOfP){
										nonadjacentToV2V3AndNotXComplete = !hasEdge(g,p,v2)&&!hasEdge(g,p,v3)&&!isYXComplete(g, p, X);
										if (!nonadjacentToV2V3AndNotXComplete) break;
									}
									if (nonadjacentToV2V3AndNotXComplete){
										if (P.getPathLength()%2==0){
											if (!hasEdge(g,v1,v3)&&!hasEdge(g,v2,v4)){
												System.err.println("Configuration Type 2 detected: "+v1+v2+v3+v4+P.getPathEdgeList()+", i.e. path nodes "+nodesOfP+", with anticomponent "+X);
												return true;
											}											
										}
										else{
											System.err.println("Configuration Type 2 detected: "+v1+v2+v3+v4+P.getPathEdgeList()+", i.e. path nodes "+nodesOfP+", with anticomponent "+X);
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
		return false;
	}
	
	private boolean hasEdge(SimpleGraph<V,E> g, Set<V> set, V v){
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
	 * For each anticomponent X, find the maximal connected subset F' containing v5 with the properties that v1,v2 have no neighbours in F'
	 * and no vertex of F'\v5 is X-complete
	 */
	private Set<V> findMaximalConnectedSubset(SimpleGraph<V,E> g, Set<V> X, V v1, V v2, V v5){
		UndirectedSubgraph<V,E> subg = new UndirectedSubgraph<V,E>(g,X, null);
		List<Set<V>> listOfConnectedVertexSets = new ConnectivityInspector<V,E>(subg).connectedSets();
		Set<V> FPrime = new HashSet<V>();
		for (Set<V> FPrimeCandidate : listOfConnectedVertexSets){
			if (FPrimeCandidate.size()>FPrime.size()&&FPrimeCandidate.contains(v5)&&hasEdge(g,FPrimeCandidate,v5)){
				V fail = null;
				for (V f : FPrimeCandidate){
					if (f!=v5&&isYXComplete(g, f, X)){
						fail = f;
						break;
					}
				}
				if (fail==null){
					FPrime=FPrimeCandidate;
				}
			}
		}
		return FPrime;
	}
	
	private boolean hasANonneighbourInX(SimpleGraph<V,E> g, V v, Set<V> X){
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
	private boolean hasConfigurationType3(SimpleGraph<V,E> g){
		for (E e12 : g.edgeSet()){
			V v1 = g.getEdgeSource(e12), v2 = g.getEdgeTarget(e12);
			for (V v5 : g.vertexSet()){
				if (v1!=v5&&v2!=v5&&!hasEdge(g,v1,v5)&&!hasEdge(g,v2,v5)){
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
						for (V toAdd : g.vertexSet()){
							if (isYXComplete(g, toAdd, X)&&!hasEdge(g,v1,toAdd)&&!hasEdge(g,v2,toAdd)&&!hasEdge(g,v5,toAdd)&&hasANeighbourInF(g, toAdd, FPrime)){
								F.add(toAdd);
							}
						}
						for (E e41 : g.edgesOf(v1)){
							V v4 = g.getEdgeSource(e41);
							if (v4==v1)
								v4 = g.getEdgeTarget(e41);
							if (!hasEdge(g,v2,v4)&&!hasEdge(g,v5,v4)&&hasANeighbourInF(g, v4, F)&&hasANonneighbourInX(g, v4, X)){
								for (E e23 : g.edgesOf(v2)){
									V v3 = g.getEdgeSource(e23);
									if (v3==v2)
										v3 = g.getEdgeTarget(e23);
									if (hasEdge(g,v3,v4)&&hasEdge(g,v5,v3)&&!hasEdge(g,v1,v3)&&hasANonneighbourInX(g, v3, X)){
										for (V v6 : F){
											if (v6!=v1&&v6!=v2&&v6!=v3&&v6!=v4&&v6!=v5&&hasEdge(g,v4,v6)){
												Set<V> verticesForPv5v6 = new HashSet<V>();
												verticesForPv5v6.addAll(FPrime);
												verticesForPv5v6.add(v5);
												verticesForPv5v6.add(v6);
												UndirectedSubgraph<V,E> subg = new UndirectedSubgraph<V,E>(g,verticesForPv5v6, null);
												DijkstraShortestPath<V, E> P = new DijkstraShortestPath<V, E>(subg, v6, v5);
												if (P!=null){
													System.err.println("Configuration Type 3 detected: "+v1+", "+v2+", "+v3+", "+v4+", "+v5+", "+v6+" and Path vertices "+V(g,P.getPath())+" and anticomponent "+X);
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
		return false;
	}
	
	/**
	 * If true, the graph is not Berge. Checks whether g contains a Pyramid, Jewel, configuration type 1, 2 or 3.
	 * @param g
	 * @return
	 */
	private boolean routine2(SimpleGraph<V,E> g){
		return hasConfigurationType1(g)||hasConfigurationType2(g)/*||hasConfigurationType3(g)*/||containsPyramid(g)||containsJewel(g);
	}
	//=======================================================================================================================
	
	/**
	 * N(a,b) is the set of all {a,b}-complete vertices
	 * @param g
	 * @param a
	 * @param b
	 * @return
	 */
	private Set<V> N(SimpleGraph<V,E> g, V a, V b){
		Set<V> res = new HashSet<V>();
		for (E ea : g.edgesOf(a)){
			if (hasEdge(g,b,g.getEdgeSource(ea))){
				res.add(g.getEdgeSource(ea));
			}
			if (hasEdge(g,b,g.getEdgeTarget(ea))){
				res.add(g.getEdgeTarget(ea));
			}
		}
		res.remove(a);
		res.remove(b);
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
	private int r(SimpleGraph<V,E> g, Set<V> Nab, V c){
		List<Set<V>> anticomponents = findAllAnticomponentsOfY(g, Nab);
		int res = 0;
		for (Set<V> set : anticomponents){
			Iterator<V> it = set.iterator();
			V nonneighbour = null;
			while (it.hasNext()&&hasEdge(g,(nonneighbour = it.next()),c)){};
			if (!hasEdge(g,nonneighbour,c)&&res<set.size())
				res = set.size();
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
	private Set<V> Y(SimpleGraph<V,E> g, Set<V> Nab, V c){
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
	private Set<V> W(SimpleGraph<V,E> g, Set<V> Nab, V c){
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
	private Set<V> Z(SimpleGraph<V,E> g, Set<V> Nab, V c){
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
	private Set<V> X(SimpleGraph<V,E> g, Set<V> Nab, V c){
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
	private boolean isTripleRelevant(SimpleGraph<V,E> g, V a, V b, V c){
		return a!=b&&!hasEdge(g,a,b)&&!N(g,a,b).contains(c);
	}
	
	
	/**
	 * Returns a set of vertex sets that may be near-cleaners for an amenable hole in g.
	 * @param g
	 * @return
	 */
	private Set<Set<V>> routine3(SimpleGraph<V,E> g){
		Set<Set<V>> NuvList = new HashSet<Set<V>>();
		for (E euv : g.edgeSet()){
			NuvList.add(N(g,g.getEdgeSource(euv),g.getEdgeTarget(euv)));			
		}
		Set<Set<V>> tripleList = new HashSet<Set<V>>();
		for (V a : g.vertexSet()){
			for (V b : g.vertexSet()){
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
	 * Performs the Berge Test for the graph specified during the instance construction.
	 * <p> First this algorithm is used to test whether g or its complement contain a jewel, a pyramid or a configuration of type
	 * 1, 2 or 3. If so, it is output that g is not Berge. If not, then every shortest odd hole in g is amenable. This asserted the near-cleaner subsets
	 * of V(g) are determined. For each of them in turn it is checked if this subset is a near-cleaner and thus if there is an odd hole. If 
	 * an odd hole is found this checker will output that g is not Berge. If no odd hole is found all near-cleaners for the complement graph are determined
	 * and it will be proceeded as before. If again no odd hole is detected g is Berge.
	 */
	public boolean isBerge(){
		long startTime = System.currentTimeMillis();
		boolean isBerge = true;
		SimpleGraph<V,E> complementGraph = new SimpleGraph<V,E>(g.getEdgeFactory());
		new ComplementGraphGenerator<V,E>(g).generateGraph(complementGraph, null, null);
		System.out.print(Calendar.getInstance().getTime()+"  ");
		System.out.println("Phase 1...  Checking for Pyramids and Jewels");
		if ((isBerge=!routine2(g)&&!routine2(complementGraph))){
			System.out.print(Calendar.getInstance().getTime()+"  ");
			System.out.println("Phase 2...  Gathering possible Near-Cleaners");
			Iterator<Set<V>> it = routine3(g).iterator();
			System.out.print(Calendar.getInstance().getTime()+"  ");
			System.out.println("Phase 3...  Checking for Odd Holes");
			while (it.hasNext()&&(isBerge =!routine1(g,it.next()))){}
			if (isBerge){
				System.out.print(Calendar.getInstance().getTime()+"  ");
				System.out.println("Phase 4...  Gathering possible Near-Cleaners of the Complement Graph");
				it = routine3(complementGraph).iterator();
				System.out.print(Calendar.getInstance().getTime()+"  ");
				System.out.println("Phase 5...  Checking for Odd Holes within the Complement Graph");
				while (it.hasNext()&&(isBerge =!routine1(complementGraph,it.next()))){}
				
			}
		}
		System.out.println("Elapsed Time: "+(System.currentTimeMillis()-startTime)+"ms");
		if (isBerge){
			System.out.println("Graph is Berge and therefor perfect");
		}
		else{
			System.out.println("Graph is not Berge and therefor not perfect");
		}
		return isBerge;
		
	}
	
	

}
