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
 * (C) Copyright 2003-2008, by Linda Buisman and Contributors.
 *
 * Original Author:  Linda Buisman
 * Contributor(s):   Barak Naveh
 *                   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 06-Nov-2003 : Initial revision (LB);
 * 07-Jun-2005 : Made generic (CH);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.graph.*;


/**
 * Algorithms to find a vertex cover for a graph. A vertex cover is a set of
 * vertices that touches all the edges in the graph. The graph's vertex set is a
 * trivial cover. However, a <i>minimal</i> vertex set (or at least an
 * approximation for it) is usually desired. Finding a true minimal vertex cover
 * is an NP-Complete problem. For more on the vertex cover problem, see <a
 * href="http://mathworld.wolfram.com/VertexCover.html">
 * http://mathworld.wolfram.com/VertexCover.html</a>
 *
 * @author Linda Buisman
 * @since Nov 6, 2003
 */
public abstract class VertexCovers
{
    /**
     * Finds a 2-approximation for a minimal vertex cover of the specified
     * graph. The algorithm promises a cover that is at most double the size of
     * a minimal cover. The algorithm takes O(|E|) time.
     *
     * <p>For more details see Jenny Walter, CMPU-240: Lecture notes for
     * Language Theory and Computation, Fall 2002, Vassar College, <a
     * href="http://www.cs.vassar.edu/~walter/cs241index/lectures/PDF/approx.pdf">
     * http://www.cs.vassar.edu/~walter/cs241index/lectures/PDF/approx.pdf</a>.
     * </p>
     *
     * @param g the graph for which vertex cover approximation is to be found.
     *
     * @return a set of vertices which is a vertex cover for the specified
     * graph.
     */
    public static <V, E> Set<V> find2ApproximationCover(Graph<V, E> g)
    {
        // C <-- {}
        Set<V> cover = new HashSet<>();

        // G'=(V',E') <-- G(V,E)
        Subgraph<V, E, Graph<V, E>> sg =
                new Subgraph<>(
                        g,
                        null,
                        null);

        // while E' is non-empty
        while (sg.edgeSet().size() > 0) {
            // let (u,v) be an arbitrary edge of E'
            E e = sg.edgeSet().iterator().next();

            // C <-- C U {u,v}
            V u = g.getEdgeSource(e);
            V v = g.getEdgeTarget(e);
            cover.add(u);
            cover.add(v);

            // remove from E' every edge incident on either u or v
            sg.removeVertex(u);
            sg.removeVertex(v);
        }

        return cover; // return C
    }

    /**
     * Finds a greedy approximation for a minimal vertex cover of a specified
     * graph. At each iteration, the algorithm picks the vertex with the highest
     * degree and adds it to the cover, until all edges are covered.
     *
     * <p>The algorithm works on undirected graphs, but can also work on
     * directed graphs when their edge-directions are ignored. To ignore edge
     * directions you can use {@link org.jgrapht.Graphs#undirectedGraph(Graph)}
     * or {@link org.jgrapht.graph.AsUndirectedGraph}.</p>
     *
     * @param g the graph for which vertex cover approximation is to be found.
     *
     * @return a set of vertices which is a vertex cover for the specified
     * graph.
     */
    public static <V, E> Set<V> findGreedyCover(UndirectedGraph<V, E> g)
    {
        // C <-- {}
        Set<V> cover = new HashSet<>();

        // G' <-- G
        UndirectedGraph<V, E> sg = new UndirectedSubgraph<>(g, null, null);

        // compare vertices in descending order of degree
        VertexDegreeComparator<V, E> comp =
                new VertexDegreeComparator<>(sg);

        // while G' != {}
        while (sg.edgeSet().size() > 0) {
            // v <-- vertex with maximum degree in G'
            V v = Collections.max(sg.vertexSet(), comp);

            // C <-- C U {v}
            cover.add(v);

            // remove from G' every edge incident on v, and v itself
            sg.removeVertex(v);
        }

        return cover;
    }

//    public static int counter1=0;
//    public static <V, E> Set<V> findVertexCover(UndirectedGraph<V, E> g)
//    {
//
//        List<V> vertices=new ArrayList<>(g.vertexSet());
//        Collections.sort(vertices, new VertexDegreeComparator<>(g, false));
//        Collections.sort(vertices, (v1, v2) -> Integer.compare(g.degreeOf(v1), g.degreeOf(v2)));
//
//        counter1++;
//
//        V v = null;
//        List<V> neighbors=null;
//        Iterator<V> it=g.vertexSet().iterator();
//        while(v == null && it.hasNext()){
//            V candidate=it.next();
//            neighbors=Graphs.neighborListOf(g, candidate);
//            if(!neighbors.isEmpty())
//                v=candidate;
//        }
//
//        if (v==null) {
//            return new HashSet<>();
//        }
//
//        // G1 = (V',E') <-- G(V,E)
//        UndirectedSubgraph<V, E> g1 =
//                new UndirectedSubgraph<>(
//                        g,
//                        null,
//                        null);
//
//        UndirectedSubgraph<V, E> g2 =
//                new UndirectedSubgraph<>(
//                        g,
//                        null,
//                        null);
//
//        g1.removeVertex(v);
//        g2.removeAllVertices(neighbors);
//        g2.removeVertex(v);
//
//        Set<V> cover1 = findVertexCover(g1);
//        Set<V> cover2 = findVertexCover(g2);
//
//        cover1.add(v);
//        cover2.addAll(neighbors);
//
//        if (cover1.size() < cover2.size()) {
//            return cover1;
//        } else {
//            return cover2;
//        }
//    }
//
//    public static int counter2=0;
//    public static <V, E> Set<V> findVertexCover2(Graph<V, E> g)
//    {
//        counter2++;
//
//        Set<E> edgeSet=g.edgeSet();
//
//        if(edgeSet.isEmpty())
//            return new HashSet<>();
//
//        //Find edge to branch on.
//        E e=edgeSet.iterator().next();
//        V source=g.getEdgeSource(e);
//        V target=g.getEdgeTarget(e);
//
//        // G1 = (V',E') <-- G(V,E)
//        Subgraph<V, E, Graph<V, E>> g1 =
//                new Subgraph<>(
//                        g,
//                        null,
//                        null);
//
//        Subgraph<V, E, Graph<V, E>> g2 =
//                new Subgraph<>(
//                        g,
//                        null,
//                        null);
//
//        g1.removeVertex(source);
//        g2.removeVertex(target);
//
//        Set<V> cover1 = findVertexCover2(g1);
//        Set<V> cover2 = findVertexCover2(g2);
//
//        cover1.add(source);
//        cover2.add(target);
//
//        if (cover1.size() < cover2.size()) {
//            return cover1;
//        } else {
//            return cover2;
//        }
//    }

    private static Map<String, BitSet> memo;
    private static List<List<Integer>> neighbors;

    /**
     * Finds a minimum vertex cover of a specified graph. At each iteration,
     * the algorithm picks a random vertex v and distinguishes two cases:
     * either v has to be added to the vertex cover or all neighbors of v.
     *
     * Let k be the size of a minimum vertex cover. Then the algorithm requires
     * O(2^k * poly(n)) time to terminate. Thus, this algorithm achieves polynomial
     * running time on graph classes where k = O(log^c n) for any constant c.
     *
     * There are more advanced algorithms for the minimum vertex cover problem,
     * e.g., by Chen, Kanj, Xia (2005) with a running time of O(kn + 1.274^k).
     *
     * @param g the graph for which vertex cover approximation is to be found.
     *
     * @return a set of vertices which is a minimum vertex cover for the specified
     * graph.
     */
    public static <V, E> Set<V> findMinimumVertexCover(Graph<V, E> g)
    {
        memo = new HashMap<String, BitSet>();

        // sort vertices by descending degree in vertices list
        List<V> vertices = new ArrayList<V>();
        List<V> sortHelpList = new ArrayList<V>();
        sortHelpList.addAll(g.vertexSet());

        while (!sortHelpList.isEmpty()) {
            int maxDegree = 0;
            int next = 0;

            for (int i = 0; i < sortHelpList.size(); i++) {
                V v = sortHelpList.get(i);
                int degree = Graphs.neighborListOf(g, v).size();

                if (maxDegree < degree) {
                    maxDegree = degree;
                    next = i;
                }
            }
            vertices.add(sortHelpList.remove(next));
        }

        int n = vertices.size();

        // create adjacency list for the whole graph
        neighbors = new ArrayList<List<Integer>>();

        for (int i = 0; i < n; i++) {
            neighbors.add(new ArrayList<Integer>());

            for (int j = 0; j < n; j++) {
                if (Graphs.neighborListOf(g, vertices.get(i)).contains(vertices.get(j))) {
                    neighbors.get(i).add(j);
                }
            }
        }

        // store subgraph configuration as BitSet of removed vertices
        BitSet removed = new BitSet(n);

        // findMinimumVertexCoverRecursion returns a BitSet that describes which vertices are in cover
        BitSet cover = findMinimumVertexCoverRecursion(g, removed);

        // Set<V> result is then constructed from BitSet solution
        Set<V> result = new HashSet<V>();
        for (int i = 0; i < n; i++) {
            if (cover.get(i)) {
                result.add(vertices.get(i));
            }
        }

        return result;
    }

    private static <V, E> BitSet findMinimumVertexCoverRecursion(Graph<V, E> g, BitSet removed)
    {
        int n = g.vertexSet().size();
        String subgraphID = removed.toString();

        // base case
        if (removed.cardinality() == n) {
            return new BitSet(n);
        }

        // check whether subproblem already occurred
        if (memo.containsKey(subgraphID)) {
            BitSet best = new BitSet(n);
            best.or(memo.get(subgraphID));
            return best;
        }

        // pick arbitrary unconsidered vertex
        int vIndex = removed.nextClearBit(0);

        // create two subgraphs, one without v and one without v & N(v)
        BitSet removed1 = new BitSet(n);
        removed1.or(removed);
        removed1.set(vIndex);

        BitSet removed2 = new BitSet(n);
        removed2.or(removed1);

        for (Integer neighborID : neighbors.get(vIndex)) {
            removed2.set(neighborID);
        }

        BitSet cover1 = findMinimumVertexCoverRecursion(g, removed1);
        BitSet cover2 = findMinimumVertexCoverRecursion(g, removed2);

        cover1.set(vIndex);

        for (Integer neighborID : neighbors.get(vIndex)) {
            if (!removed.get(neighborID)) {
                cover2.set(neighborID);
            }
        }

        //choose the smaller of the two subgraph vertex covers
        if (cover1.cardinality() < cover2.cardinality()) {
            memo.put(subgraphID, cover1);
            return cover1;
        } else {
            memo.put(subgraphID, cover2);
            return cover2;
        }
    }

}

// End VertexCovers.java

/*
Notation:
V(G)= set of vertices in graph G
E(G)= set of edges in graph G
G\{v1, v2,...}= graph where vertices v1, v2, ... and all edges incident to it have been deleted.
N(G,v)= set of all neighbor vertices of vertex v in graph G.



Computing a minimum vertex cover


Recursive algorithm 1:
-Idea: choose a vertex v. Either v is part of the vertex cover, or N(v) are all part of the vertex cover

Set alg1(Graph G){
   if(E(g) == empty) //Terminate when there are no edges left
    return {};

   select vertex v in V(G);

   U1=alg1(G\{v}) \cup v;
   U2=alg1(G\{v \cup N(G,v)}) \cup N(G,v);

   if(|U1| <= |U2|)
    return U1;
   else
    return U2;
}

Recursive algorithm 2:
-Idea: for each edge (u,v), at least one of {u,v} must be part of the vertex cover. If we select u, then we can solve for the subgraph obtained by deleting vertex u and all edges incident to it. Alternatively, if we select v, then we can solve for the subgraph obtained by deleting vertex v and all edges incident to it.

-Pseudo code:

Set alg2(Graph G){
   if(E(G) == empty) //Terminate when there are no edges left
    return {};

   select edge (u,v) in E(G);

   U1=alg2(G_u) \cup u;
   U2=alg2(G_v) \cup v;

   if(|U1| <= |U2|)
    return U1;
   else
    return U2;
}
*/