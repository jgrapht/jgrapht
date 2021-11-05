/*
 * (C) Copyright 2016-2021, by Philipp S. Kaesgen and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.cycle;

import org.jgrapht.*;
import org.jgrapht.alg.connectivity.*;
import org.jgrapht.alg.shortestpath.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;

import java.util.*;
import java.util.stream.*;

/**
 * <p>
 * Tests whether a graph is <a href="http://mathworld.wolfram.com/PerfectGraph.html">perfect</a>. A
 * perfect graph, also known as a Berge graph, is a graph $G$ such that for every induced subgraph
 * of $G$, the clique number $\chi(G)$ equals the chromatic number $\omega(G)$, i.e.,
 * $\omega(G)=\chi(G)$. Another characterization of perfect graphs is given by the Strong Perfect
 * Graph Theorem [M. Chudnovsky, N. Robertson, P. Seymour, R. Thomas. The strong perfect graph
 * theorem Annals of Mathematics, vol 164(1): pp. 51–230, 2006]: A graph $G$ is perfect if neither
 * $G$ nor its complement $\overline{G}$ have an odd hole. A hole in $G$ is an induced subgraph of
 * $G$ that is a cycle of length at least four, and it is odd or even if it has odd (or even,
 * respectively) length.
 * <p>
 * Some special <a href="http://graphclasses.org/classes/gc_56.html">classes</a> of graphs are are
 * known to be perfect, e.g. Bipartite graphs and Chordal graphs. Testing whether a graph is resp.
 * Bipartite or Chordal can be done efficiently using {@link GraphTests#isBipartite} or
 * {@link org.jgrapht.alg.cycle.ChordalityInspector}.
 * <p>
 * The implementation of this class is based on the paper: M. Chudnovsky, G. Cornuejols, X. Liu, P.
 * Seymour, and K. Vuskovic. Recognizing Berge Graphs. Combinatorica 25(2): 143--186, 2003.
 * <p>
 * Special Thanks to Maria Chudnovsky for her kind help.
 * 
 * <p>
 * The runtime complexity of this implementation is $O(|V|^9|)$. This implementation is far more
 * efficient than simplistically testing whether graph $G$ or its complement $\overline{G}$ have an
 * odd cycle, because testing whether one graph can be found as an induced subgraph of another is
 * <a href="https://en.wikipedia.org/wiki/Induced_subgraph_isomorphism_problem">known</a> to be
 * NP-hard.
 * 
 * @author Philipp S. Kaesgen (pkaesgen@freenet.de)
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 */
public class BergeGraphInspector<V, E>
{

    private GraphPath<V, E> certificate = null;
    private boolean certify = false;

    /**
     * Lists the vertices which are covered by two paths
     * 
     * @param p1 A Path in g
     * @param p2 A Path in g
     * @return Set of vertices covered by both p1 and p2
     */
    private List<V> intersectGraphPaths(GraphPath<V, E> p1, GraphPath<V, E> p2)
    {
        List<V> res = new LinkedList<>();
        res.addAll(p1.getVertexList());
        res.retainAll(p2.getVertexList());
        return res;
    }

    /**
     * Assembles a GraphPath of the Paths S and T. Required for the Pyramid Checker
     * 
     * @param g A Graph
     * @param pathS A Path in g
     * @param pathT A Path in g
     * @param m A vertex
     * @param b1 A base vertex
     * @param b2 A base vertex
     * @param b3 A base vertex
     * @param s1 A vertex
     * @param s2 A vertex
     * @param s3 A vertex
     * @return The conjunct path of S and T
     */
    private GraphPath<V, E> p(
        Graph<V, E> g, GraphPath<V, E> pathS, GraphPath<V, E> pathT, V m, V b1, V b2, V b3, V s1, V s2,
        V s3)
    {
        if (s1 == b1) {
            if (b1 == m) {
                List<E> edgeList = new LinkedList<>();
                return new GraphWalk<>(g, s1, b1, edgeList, 0);
            } else
                return null;
        } else {
            if (b1 == m)
                return null;
            if (g.containsEdge(m, b2) || g.containsEdge(m, b3) || g.containsEdge(m, s2)
                || g.containsEdge(m, s3) || pathS == null || pathT == null)
                return null;
            if (pathS
                .getVertexList().stream().anyMatch(
                    t -> g.containsEdge(t, b2) || g.containsEdge(t, b3) || g.containsEdge(t, s2)
                        || g.containsEdge(t, s3))
                || pathT
                    .getVertexList().stream().anyMatch(
                        t -> t != b1 && (g.containsEdge(t, b2) || g.containsEdge(t, b3)
                            || g.containsEdge(t, s2) || g.containsEdge(t, s3))))
                return null;
            List<V> intersection = intersectGraphPaths(pathS, pathT);
            if (intersection.size() != 1 || !intersection.contains(m))
                return null;
            if (pathS
                .getVertexList().stream().anyMatch(
                    s -> s != m && pathT
                        .getVertexList().stream().anyMatch(t -> t != m && g.containsEdge(s, t))))
                return null;
            List<E> edgeList = new LinkedList<>();
            edgeList.addAll(pathT.getEdgeList());
            edgeList.addAll(pathS.getEdgeList());
            double weight = edgeList.stream().mapToDouble(g::getEdgeWeight).sum();
            return new GraphWalk<>(g, b1, s1, edgeList, weight);

        }
    }

    private void bfOddHoleCertificate(Graph<V, E> g)
    {
        for (V start : g.vertexSet()) {
            if (g.degreeOf(start) < 2)
                continue;
            Set<V> set = new HashSet<>();
            set.addAll(g.vertexSet());
            for (V neighborOfStart : g.vertexSet()) {
                if (neighborOfStart == start || !g.containsEdge(start, neighborOfStart)
                    || g.degreeOf(neighborOfStart) != 2)
                    continue;
                set.remove(neighborOfStart);
                Graph<V, E> subg = new AsSubgraph<>(g, set);
                for (V neighborsNeighbor : g.vertexSet()) {
                    if (neighborsNeighbor == start || neighborsNeighbor == neighborOfStart
                        || !g.containsEdge(neighborsNeighbor, neighborOfStart)
                        || g.containsEdge(neighborsNeighbor, start)
                        || g.degreeOf(neighborsNeighbor) < 2)
                        continue;
                    GraphPath<V, E> path =
                        new DijkstraShortestPath<>(subg).getPath(start, neighborsNeighbor);
                    if (path == null || path.getLength() < 3 || path.getLength() % 2 == 0)
                        continue;
                    List<E> edgeList = new LinkedList<>();
                    edgeList.addAll(path.getEdgeList());
                    edgeList.add(g.getEdge(neighborsNeighbor, neighborOfStart));
                    edgeList.add(g.getEdge(neighborOfStart, start));
                    double weight = edgeList.stream().mapToDouble(g::getEdgeWeight).sum();
                    certificate = new GraphWalk<>(g, start, start, edgeList, weight);
                    break;
                }
                if (certificate != null)
                    break;
            }
            if (certificate != null)
                break;
        }
    }

    /**
     * Checks whether a graph contains a pyramid. Running time: O(|V(g)|^9)
     * 
     * @param g Graph
     * @return Either it finds a pyramid (and hence an odd hole) in g, or it determines that g
     *         contains no pyramid
     */
    boolean containsPyramid(Graph<V, E> g)
    {
        /*
         * A pyramid looks like this:
         * 
         * b2-(T2)-m2-(S2)-s2 / | \ b1---(T1)-m1-(S1)-s1--a \ | / b3-(T3)-m3-(S3)-s3
         * 
         * Note that b1, b2, and b3 are connected and all names in parentheses are paths
         * 
         */
        Set<Set<V>> visitedTriangles = new HashSet<>();
        for (V b1 : g.vertexSet()) {
            for (V b2 : g.vertexSet()) {
                if (b1 == b2 || !g.containsEdge(b1, b2))
                    continue;
                for (V b3 : g.vertexSet()) {
                    if (b3 == b1 || b3 == b2 || !g.containsEdge(b2, b3) || !g.containsEdge(b1, b3))
                        continue;

                    // Triangle detected for the pyramid base
                    Set<V> triangles = new HashSet<>();
                    triangles.add(b1);
                    triangles.add(b2);
                    triangles.add(b3);
                    if (visitedTriangles.contains(triangles)) {
                        continue;
                    }
                    visitedTriangles.add(triangles);

                    for (V aCandidate : g.vertexSet()) {
                        if (aCandidate == b1 || aCandidate == b2 || aCandidate == b3 ||
                        // a is adjacent to at most one of b1,b2,b3
                            g.containsEdge(aCandidate, b1) && g.containsEdge(aCandidate, b2)
                            || g.containsEdge(aCandidate, b2) && g.containsEdge(aCandidate, b3)
                            || g.containsEdge(aCandidate, b1) && g.containsEdge(aCandidate, b3))
                        {
                            continue;
                        }

                        // aCandidate could now be the top of the pyramid
                        for (V s1 : g.vertexSet()) {
                            if (s1 == aCandidate || !g.containsEdge(s1, aCandidate) || s1 == b2
                                || s1 == b3
                                || s1 != b1 && (g.containsEdge(s1, b2) || g.containsEdge(s1, b3)))
                            {
                                continue;
                            }

                            for (V s2 : g.vertexSet()) {
                                if (s2 == aCandidate || !g.containsEdge(s2, aCandidate)
                                    || g.containsEdge(s1, s2) || s1 == s2 || s2 == b1 || s2 == b3
                                    || s2 != b2
                                        && (g.containsEdge(s2, b1) || g.containsEdge(s2, b3)))
                                {
                                    continue;
                                }

                                for (V s3 : g.vertexSet()) {
                                    if (s3 == aCandidate || !g.containsEdge(s3, aCandidate)
                                        || g.containsEdge(s3, s2) || s1 == s3 || s3 == s2
                                        || g.containsEdge(s1, s3) || s3 == b1 || s3 == b2
                                        || s3 != b3
                                            && (g.containsEdge(s3, b1) || g.containsEdge(s3, b2)))
                                    {
                                        continue;
                                    }

                                    // s1, s2, s3 could now be the closest vertices to the top
                                    // vertex of the pyramid
                                    Set<V> setM = new HashSet<>();
                                    setM.addAll(g.vertexSet());
                                    setM.remove(b1);
                                    setM.remove(b2);
                                    setM.remove(b3);
                                    setM.remove(s1);
                                    setM.remove(s2);
                                    setM.remove(s3);

                                    Map<V, GraphPath<V, E>> mapS1 = new HashMap<>(),
                                        mapS2 = new HashMap<>(), mapS3 = new HashMap<>(),
                                        mapT1 = new HashMap<>(), mapT2 = new HashMap<>(),
                                        mapT3 = new HashMap<>();

                                    // find paths which could be the edges of the pyramid
                                    for (V m1 : setM) {
                                        Set<V> validInterior = new HashSet<>();
                                        validInterior.addAll(setM);
                                        validInterior
                                            .removeIf(
                                                i -> g.containsEdge(i, b2) || g.containsEdge(i, s2)
                                                    || g.containsEdge(i, b3)
                                                    || g.containsEdge(i, s3));

                                        validInterior.add(m1);
                                        validInterior.add(s1);
                                        Graph<V, E> subg = new AsSubgraph<>(g, validInterior);
                                        mapS1
                                            .put(
                                                m1,
                                                new DijkstraShortestPath<>(subg).getPath(m1, s1));
                                        validInterior.remove(s1);
                                        validInterior.add(b1);
                                        subg = new AsSubgraph<>(g, validInterior);
                                        mapT1
                                            .put(
                                                m1,
                                                new DijkstraShortestPath<>(subg).getPath(b1, m1));

                                    }
                                    for (V m2 : setM) {
                                        Set<V> validInterior = new HashSet<>();
                                        validInterior.addAll(setM);
                                        validInterior
                                            .removeIf(
                                                i -> g.containsEdge(i, b1) || g.containsEdge(i, s1)
                                                    || g.containsEdge(i, b3)
                                                    || g.containsEdge(i, s3));
                                        validInterior.add(m2);
                                        validInterior.add(s2);
                                        Graph<V, E> subg = new AsSubgraph<>(g, validInterior);
                                        mapS2
                                            .put(
                                                m2,
                                                new DijkstraShortestPath<>(subg).getPath(m2, s2));
                                        validInterior.remove(s2);
                                        validInterior.add(b2);
                                        subg = new AsSubgraph<>(g, validInterior);
                                        mapT2
                                            .put(
                                                m2,
                                                new DijkstraShortestPath<>(subg).getPath(b2, m2));

                                    }
                                    for (V m3 : setM) {
                                        Set<V> validInterior = new HashSet<>();
                                        validInterior.addAll(setM);
                                        validInterior
                                            .removeIf(
                                                i -> g.containsEdge(i, b1) || g.containsEdge(i, s1)
                                                    || g.containsEdge(i, b2)
                                                    || g.containsEdge(i, s2));
                                        validInterior.add(m3);
                                        validInterior.add(s3);

                                        Graph<V, E> subg = new AsSubgraph<>(g, validInterior);
                                        mapS3
                                            .put(
                                                m3,
                                                new DijkstraShortestPath<>(subg).getPath(m3, s3));
                                        validInterior.remove(s3);
                                        validInterior.add(b3);
                                        subg = new AsSubgraph<>(g, validInterior, null);
                                        mapT3
                                            .put(
                                                m3,
                                                new DijkstraShortestPath<>(subg).getPath(b3, m3));
                                    }

                                    // Check if all edges of a pyramid are valid
                                    Set<V> setM1 = new HashSet<>();
                                    setM1.addAll(setM);
                                    setM1.add(b1);
                                    for (V m1 : setM1) {
                                        GraphPath<V, E> pathP1 = p(
                                            g, mapS1.get(m1), mapT1.get(m1), m1, b1, b2, b3, s1, s2, s3);
                                        if (pathP1 == null)
                                            continue;
                                        Set<V> setM2 = new HashSet<>();
                                        setM2.addAll(setM);
                                        setM2.add(b2);
                                        for (V m2 : setM) {
                                            GraphPath<V,
                                                E> pathP2 = p(
                                                    g, mapS2.get(m2), mapT2.get(m2), m2, b2, b1, b3, s2,
                                                    s1, s3);
                                            if (pathP2 == null)
                                                continue;
                                            Set<V> setM3 = new HashSet<>();
                                            setM3.addAll(setM);
                                            setM3.add(b3);
                                            for (V m3 : setM3) {
                                                GraphPath<V,
                                                    E> pathP3 = p(
                                                        g, mapS3.get(m3), mapT3.get(m3), m3, b3, b1, b2,
                                                        s3, s1, s2);
                                                if (pathP3 == null)
                                                    continue;
                                                if (certify) {
                                                    if ((pathP1.getLength() + pathP2.getLength())
                                                        % 2 == 0)
                                                    {
                                                        Set<V> set = new HashSet<>();
                                                        set.addAll(pathP1.getVertexList());
                                                        set.addAll(pathP2.getVertexList());
                                                        set.add(aCandidate);
                                                        bfOddHoleCertificate(
                                                            new AsSubgraph<>(g, set));
                                                    } else if ((pathP1.getLength() + pathP3.getLength())
                                                        % 2 == 0)
                                                    {
                                                        Set<V> set = new HashSet<>();
                                                        set.addAll(pathP1.getVertexList());
                                                        set.addAll(pathP3.getVertexList());
                                                        set.add(aCandidate);
                                                        bfOddHoleCertificate(
                                                            new AsSubgraph<>(g, set));
                                                    } else {
                                                        Set<V> set = new HashSet<>();
                                                        set.addAll(pathP3.getVertexList());
                                                        set.addAll(pathP2.getVertexList());
                                                        set.add(aCandidate);
                                                        bfOddHoleCertificate(
                                                            new AsSubgraph<>(g, set));
                                                    }
                                                }
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

        return false;
    }

    /**
     * Finds all Components of a set F contained in V(g)
     * 
     * @param g A graph
     * @param f A vertex subset of g
     * @return Components of F in g
     */
    private List<Set<V>> findAllComponents(Graph<V, E> g, Set<V> f)
    {
        return new ConnectivityInspector<>(new AsSubgraph<>(g, f)).connectedSets();
    }

    /**
     * Checks whether a graph contains a Jewel. Running time: O(|V(g)|^6)
     * 
     * @param g Graph
     * @return Decides whether there is a jewel in g
     */
    boolean containsJewel(Graph<V, E> g)
    {
        for (V v2 : g.vertexSet()) {
            for (V v3 : g.vertexSet()) {
                if (v2 == v3 || !g.containsEdge(v2, v3))
                    continue;
                for (V v5 : g.vertexSet()) {
                    if (v2 == v5 || v3 == v5)
                        continue;

                    Set<V> setF = new HashSet<>();
                    for (V f : g.vertexSet()) {
                        if (f == v2 || f == v3 || f == v5 || g.containsEdge(f, v2)
                            || g.containsEdge(f, v3) || g.containsEdge(f, v5))
                            continue;
                        setF.add(f);
                    }

                    List<Set<V>> componentsOfF = findAllComponents(g, setF);

                    Set<V> setX1 = new HashSet<>();
                    for (V x1 : g.vertexSet()) {
                        if (x1 == v2 || x1 == v3 || x1 == v5 || !g.containsEdge(x1, v2)
                            || !g.containsEdge(x1, v5) || g.containsEdge(x1, v3))
                            continue;
                        setX1.add(x1);
                    }
                    Set<V> setX2 = new HashSet<>();
                    for (V x2 : g.vertexSet()) {
                        if (x2 == v2 || x2 == v3 || x2 == v5 || g.containsEdge(x2, v2)
                            || !g.containsEdge(x2, v5) || !g.containsEdge(x2, v3))
                            continue;
                        setX2.add(x2);
                    }

                    for (V v1 : setX1) {
                        if (g.containsEdge(v1, v3))
                            continue;
                        for (V v4 : setX2) {
                            if (v1 == v4 || g.containsEdge(v1, v4) || g.containsEdge(v2, v4))
                                continue;
                            for (Set<V> fPrime : componentsOfF) {
                                if (hasANeighbour(g, fPrime, v1) && hasANeighbour(g, fPrime, v4)) {
                                    if (certify) {
                                        Set<V> validSet = new HashSet<>();
                                        validSet.addAll(fPrime);
                                        validSet.add(v1);
                                        validSet.add(v4);
                                        GraphPath<V, E> p = new DijkstraShortestPath<>(
                                            new AsSubgraph<>(g, validSet)).getPath(v1, v4);
                                        List<E> edgeList = new LinkedList<>();
                                        edgeList.addAll(p.getEdgeList());
                                        if (p.getLength() % 2 == 1) {
                                            edgeList.add(g.getEdge(v4, v5));
                                            edgeList.add(g.getEdge(v5, v1));

                                        } else {
                                            edgeList.add(g.getEdge(v4, v3));
                                            edgeList.add(g.getEdge(v3, v2));
                                            edgeList.add(g.getEdge(v2, v1));

                                        }

                                        double weight =
                                            edgeList.stream().mapToDouble(g::getEdgeWeight).sum();
                                        certificate = new GraphWalk<>(g, v1, v1, edgeList, weight);
                                    }
                                    return true;
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
     * 
     * @param g Graph containing no pyramid or jewel
     * @return Decides whether g contains a clean shortest odd hole
     */
    boolean containsCleanShortestOddHole(Graph<V, E> g)
    {
        /*
         * Find 3 Paths which are an uneven odd hole when conjunct
         */
        for (V u : g.vertexSet()) {
            for (V v : g.vertexSet()) {
                if (u == v || g.containsEdge(u, v))
                    continue;

                GraphPath<V, E> puv = new DijkstraShortestPath<>(g).getPath(u, v);
                if (puv == null)
                    continue;

                for (V w : g.vertexSet()) {
                    if (w == u || w == v || g.containsEdge(w, u) || g.containsEdge(w, v))
                        continue;
                    GraphPath<V, E> pvw = new DijkstraShortestPath<>(g).getPath(v, w);
                    if (pvw == null)
                        continue;
                    GraphPath<V, E> pwu = new DijkstraShortestPath<>(g).getPath(w, u);
                    if (pwu == null)
                        continue;
                    Set<V> set = new HashSet<>();
                    set.addAll(puv.getVertexList());
                    set.addAll(pvw.getVertexList());
                    set.addAll(pwu.getVertexList());
                    Graph<V, E> subg = new AsSubgraph<>(g, set);
                    // Look for holes with more than 6 edges and uneven length
                    if (set.size() < 7 || subg.vertexSet().size() != set.size()
                        || subg.edgeSet().size() != subg.vertexSet().size()
                        || subg.vertexSet().size() % 2 == 0
                        || subg.vertexSet().stream().anyMatch(t -> subg.degreeOf(t) != 2))
                        continue;

                    if (certify) {
                        List<E> edgeList = new LinkedList<>();
                        edgeList.addAll(puv.getEdgeList());
                        edgeList.addAll(pvw.getEdgeList());
                        edgeList.addAll(pwu.getEdgeList());

                        double weight = edgeList.stream().mapToDouble(g::getEdgeWeight).sum();
                        certificate = new GraphWalk<>(g, u, u, edgeList, weight);
                    }
                    return true;

                }

            }
        }
        return false;
    }

    /**
     * Returns a path in g from start to end avoiding the vertices in X
     * 
     * @param g A Graph
     * @param start start vertex
     * @param end end vertex
     * @param x set of vertices which should not be in the graph
     * @return A Path in G\X
     */
    private GraphPath<V, E> getPathAvoidingX(Graph<V, E> g, V start, V end, Set<V> x)
    {
        Set<V> vertexSet = new HashSet<>();
        vertexSet.addAll(g.vertexSet());
        vertexSet.removeAll(x);
        vertexSet.add(start);
        vertexSet.add(end);
        Graph<V, E> subg = new AsSubgraph<>(g, vertexSet, null);
        return new DijkstraShortestPath<>(subg).getPath(start, end);
    }

    /**
     * Checks whether the vertex set of a graph without a vertex set X contains a shortest odd hole.
     * Running time: O(|V(g)|^4)
     * 
     * @param g Graph containing neither pyramid nor jewel
     * @param x Subset of V(g) and a possible Cleaner for an odd hole
     * @return Determines whether g has an odd hole such that X is a near-cleaner for it
     */
    private boolean containsShortestOddHole(Graph<V, E> g, Set<V> x)
    {
        for (V y1 : g.vertexSet()) {
            if (x.contains(y1))
                continue;

            for (V x1 : g.vertexSet()) {
                if (x1 == y1)
                    continue;
                GraphPath<V, E> rx1y1 = getPathAvoidingX(g, x1, y1, x);
                for (V x3 : g.vertexSet()) {
                    if (x3 == x1 || x3 == y1 || !g.containsEdge(x1, x3))
                        continue;
                    for (V x2 : g.vertexSet()) {
                        if (x2 == x3 || x2 == x1 || x2 == y1 || g.containsEdge(x2, x1)
                            || !g.containsEdge(x3, x2))
                            continue;

                        GraphPath<V, E> rx2y1 = getPathAvoidingX(g, x2, y1, x);

                        double n;
                        if (rx1y1 == null || rx2y1 == null)
                            continue;

                        V y2 = null;
                        for (V y2Candidate : rx2y1.getVertexList()) {
                            if (g.containsEdge(y1, y2Candidate) && y2Candidate != x1
                                && y2Candidate != x2 && y2Candidate != x3 && y2Candidate != y1)
                            {
                                y2 = y2Candidate;
                                break;
                            }
                        }
                        if (y2 == null)
                            continue;

                        GraphPath<V, E> rx3y1 = getPathAvoidingX(g, x3, y1, x);
                        GraphPath<V, E> rx3y2 = getPathAvoidingX(g, x3, y2, x);
                        GraphPath<V, E> rx1y2 = getPathAvoidingX(g, x1, y2, x);
                        if (rx3y1 != null && rx3y2 != null && rx1y2 != null
                            && rx2y1.getLength() == (n = rx1y1.getLength() + 1)
                            && n == rx1y2.getLength() && rx3y1.getLength() >= n
                            && rx3y2.getLength() >= n)
                        {
                            if (certify) {
                                List<E> edgeList = new LinkedList<>();
                                edgeList.addAll(rx1y1.getEdgeList());
                                for (int i = rx2y1.getLength() - 1; i >= 0; i--)
                                    edgeList.add(rx2y1.getEdgeList().get(i));
                                edgeList.add(g.getEdge(x2, x3));
                                edgeList.add(g.getEdge(x3, x1));

                                double weight =
                                    edgeList.stream().mapToDouble(g::getEdgeWeight).sum();
                                certificate = new GraphWalk<>(g, x1, x1, edgeList, weight);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether a clean shortest odd hole is in g or whether X is a cleaner for an amenable
     * shortest odd hole
     * 
     * @param g A graph, containing no pyramid or jewel
     * @param x A subset X of V(g) and a possible Cleaner for an odd hole
     * @return Returns whether g has an odd hole or there is no shortest odd hole in C such that X
     *         is a near-cleaner for C.
     */
    private boolean routine1(Graph<V, E> g, Set<V> x)
    {
        return containsCleanShortestOddHole(g) || containsShortestOddHole(g, x);
    }

    /**
     * Checks whether a graph has a configuration of type T1. A configuration of type T1 in g is a
     * hole of length 5
     * 
     * @param g A Graph
     * @return whether g contains a configuration of Type T1 (5-cycle)
     */
    private boolean hasConfigurationType1(Graph<V, E> g)
    {
        for (V v1 : g.vertexSet()) {
            Set<V> temp = new ConnectivityInspector<>(g).connectedSetOf(v1);
            for (V v2 : temp) {
                if (v1 == v2 || !g.containsEdge(v1, v2))
                    continue;
                for (V v3 : temp) {
                    if (v3 == v1 || v3 == v2 || !g.containsEdge(v2, v3) || g.containsEdge(v1, v3))
                        continue;
                    for (V v4 : temp) {
                        if (v4 == v1 || v4 == v2 || v4 == v3 || g.containsEdge(v1, v4)
                            || g.containsEdge(v2, v4) || !g.containsEdge(v3, v4))
                            continue;
                        for (V v5 : temp) {
                            if (v5 == v1 || v5 == v2 || v5 == v3 || v5 == v4
                                || g.containsEdge(v2, v5) || g.containsEdge(v3, v5)
                                || !g.containsEdge(v1, v5) || !g.containsEdge(v4, v5))
                                continue;
                            if (certify) {
                                List<E> edgeList = new LinkedList<>();
                                edgeList.add(g.getEdge(v1, v2));
                                edgeList.add(g.getEdge(v2, v3));
                                edgeList.add(g.getEdge(v3, v4));
                                edgeList.add(g.getEdge(v4, v5));
                                edgeList.add(g.getEdge(v5, v1));

                                double weight =
                                    edgeList.stream().mapToDouble(g::getEdgeWeight).sum();
                                certificate = new GraphWalk<>(g, v1, v1, edgeList, weight);
                            }
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
     * 
     * @param g A Graph
     * @param y Vertex whose X-completeness is to assess
     * @param x Set of vertices
     * @return whether y is X-complete
     */
    boolean isYXComplete(Graph<V, E> g, V y, Set<V> x)
    {
        return x.stream().allMatch(t -> g.containsEdge(t, y));
    }

    /**
     * Returns all anticomponents of a graph and a vertex set.
     * 
     * @param g A Graph
     * @param y A set of vertices
     * @return List of anticomponents of Y in g
     */
    private List<Set<V>> findAllAnticomponentsOfY(Graph<V, E> g, Set<V> y)
    {
        Graph<V, E> target;
        if (g.getType().isSimple())
            target = new SimpleGraph<>(
                g.getVertexSupplier(), g.getEdgeSupplier(), g.getType().isWeighted());
        else
            target = new Multigraph<>(
                g.getVertexSupplier(), g.getEdgeSupplier(), g.getType().isWeighted());
        new ComplementGraphGenerator<>(g).generateGraph(target);

        return findAllComponents(target, y);
    }

    /**
     * <p>
     * Checks whether a graph is of configuration type T2. A configuration of type T2 in g is a
     * sequence v1,v2,v3,v4,P,X such that:
     * </p>
     * <ul>
     * <li>v1-v2-v3-v4 is a path of g</li>
     * <li>X is an anticomponent of the set of all {v1,v2,v4}-complete vertices</li>
     * <li>P is a path in G\(X+{v2,v3}) between v1,v4, and no vertex in P*, i.e. P's interior, is
     * X-complete or adjacent to v2 or adjacent to v3</li>
     * </ul>
     * An example is the complement graph of a cycle-7-graph
     * 
     * @param g A Graph
     * @return whether g contains a configuration of Type T2
     */
    boolean hasConfigurationType2(Graph<V, E> g)
    {
        for (V v1 : g.vertexSet()) {
            for (V v2 : g.vertexSet()) {
                if (v1 == v2 || !g.containsEdge(v1, v2))
                    continue;

                for (V v3 : g.vertexSet()) {
                    if (v3 == v2 || v1 == v3 || g.containsEdge(v1, v3) || !g.containsEdge(v2, v3))
                        continue;

                    for (V v4 : g.vertexSet()) {
                        if (v4 == v1 || v4 == v2 || v4 == v3 || g.containsEdge(v4, v2)
                            || g.containsEdge(v4, v1) || !g.containsEdge(v3, v4))
                            continue;

                        Set<V> temp = new HashSet<>();
                        temp.add(v1);
                        temp.add(v2);
                        temp.add(v4);
                        Set<V> setY = new HashSet<>();
                        for (V y : g.vertexSet()) {
                            if (isYXComplete(g, y, temp)) {
                                setY.add(y);
                            }
                        }
                        List<Set<V>> anticomponentsOfY = findAllAnticomponentsOfY(g, setY);
                        for (Set<V> setX : anticomponentsOfY) {
                            Set<V> v2v3 = new HashSet<>();
                            v2v3.addAll(g.vertexSet());
                            v2v3.remove(v2);
                            v2v3.remove(v3);
                            v2v3.removeAll(setX);
                            if (!v2v3.contains(v1) || !v2v3.contains(v4))
                                continue;

                            GraphPath<V, E> path =
                                new DijkstraShortestPath<>(new AsSubgraph<>(g, v2v3))
                                    .getPath(v1, v4);
                            if (path == null)
                                continue;
                            List<V> listP = path.getVertexList();
                            if (!listP.contains(v1) || !listP.contains(v4))
                                continue;

                            boolean cont = true;
                            for (V p : listP) {
                                if (p != v1 && p != v4 && (g.containsEdge(p, v2)
                                    || g.containsEdge(p, v3) || isYXComplete(g, p, setX)))
                                {
                                    cont = false;
                                    break;
                                }
                            }
                            if (cont) {
                                if (certify) {
                                    List<E> edgeList = new LinkedList<>();
                                    if (path.getLength() % 2 == 0) {
                                        edgeList.add(g.getEdge(v1, v2));
                                        edgeList.add(g.getEdge(v2, v3));
                                        edgeList.add(g.getEdge(v3, v4));
                                        edgeList.addAll(path.getEdgeList());
                                    } else {
                                        edgeList.addAll(path.getEdgeList());
                                        V x = setX.iterator().next();
                                        edgeList.add(g.getEdge(v4, x));
                                        edgeList.add(g.getEdge(x, v1));
                                    }

                                    double weight =
                                        edgeList.stream().mapToDouble(g::getEdgeWeight).sum();
                                    certificate = new GraphWalk<>(g, v1, v1, edgeList, weight);
                                }
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
     * 
     * @param g A Graph
     * @param set A set of vertices
     * @param v A vertex
     * @return whether v has at least one neighbour in set
     */
    private boolean hasANeighbour(Graph<V, E> g, Set<V> set, V v)
    {
        return set.stream().anyMatch(s -> g.containsEdge(s, v));
    }

    /**
     * For each anticomponent X, find the maximal connected subset F' containing v5 with the
     * properties that v1,v2 have no neighbours in F' and no vertex of F'\v5 is X-complete
     * 
     * @param g A Graph
     * @param setX A set of vertices
     * @param v1 A vertex
     * @param v2 A vertex
     * @param v5 A Vertex
     * @return The maximal connected vertex subset containing v5, no neighbours of v1 and v2, and no
     *         X-complete vertex except v5
     */
    private Set<V> findMaximalConnectedSubset(Graph<V, E> g, Set<V> setX, V v1, V v2, V v5)
    {
        Set<V> fPrime = new ConnectivityInspector<>(g).connectedSetOf(v5);
        fPrime
            .removeIf(
                t -> t != v5 && isYXComplete(g, t, setX) || v1 == t || v2 == t || g.containsEdge(v1, t)
                    || g.containsEdge(v2, t));
        return fPrime;
    }

    /**
     * Reports whether a vertex has at least one nonneighbour in X
     * 
     * @param g A Graph
     * @param v A Vertex
     * @param setX A set of vertices
     * @return whether v has a nonneighbour in X
     */
    private boolean hasANonneighbourInX(Graph<V, E> g, V v, Set<V> setX)
    {
        return setX.stream().anyMatch(x -> !g.containsEdge(v, x));
    }

    /**
     * <p>
     * Checks whether a graph is of configuration type T3. A configuration of type T3 in g is a
     * sequence v1,...,v6,P,X such that
     * </p>
     * <ul>
     * <li>v1,...,v6 are distinct vertices of g</li>
     * <li>v1v2,v3v4,v2v3,v3v5,v4v6 are edges, and v1v3,v2v4,v1v5,v2v5,v1v6,v2v6,v4v5 are
     * non-edges</li>
     * <li>X is an anticomponent of the set of all {v1,v2,v5}-complete vertices, and v3,v4 are not
     * X-complete</li>
     * <li>P is a path of g\(X+{v1,v2,v3,v4}) between v5,v6, and no vertex in P* is X-complete or
     * adjacent to v1 or adjacent to v2</li>
     * <li>if v5v6 is an edge then v6 is not X-complete</li>
     * </ul>
     * 
     * @param g A Graph
     * @return whether g contains a configuration of Type T3
     */
    boolean hasConfigurationType3(Graph<V, E> g)
    {
        for (V v1 : g.vertexSet()) {
            for (V v2 : g.vertexSet()) {
                if (v1 == v2 || !g.containsEdge(v1, v2))
                    continue;
                for (V v5 : g.vertexSet()) {
                    if (v1 == v5 || v2 == v5 || g.containsEdge(v1, v5) || g.containsEdge(v2, v5))
                        continue;
                    Set<V> triple = new HashSet<>();
                    triple.add(v1);
                    triple.add(v2);
                    triple.add(v5);
                    Set<V> setY = new HashSet<>();
                    for (V y : g.vertexSet()) {
                        if (isYXComplete(g, y, triple)) {
                            setY.add(y);
                        }
                    }
                    List<Set<V>> anticomponents = findAllAnticomponentsOfY(g, setY);
                    for (Set<V> setX : anticomponents) {
                        Set<V> fPrime = findMaximalConnectedSubset(g, setX, v1, v2, v5);
                        Set<V> setF = new HashSet<>();
                        setF.addAll(fPrime);
                        for (V x : setX) {
                            if (!g.containsEdge(x, v1) && !g.containsEdge(x, v2)
                                && !g.containsEdge(x, v5) && hasANeighbour(g, fPrime, x))
                                setF.add(x);
                        }

                        for (V v4 : g.vertexSet()) {
                            if (v4 == v1 || v4 == v2 || v4 == v5 || g.containsEdge(v2, v4)
                                || g.containsEdge(v5, v4) || !g.containsEdge(v1, v4)
                                || !hasANeighbour(g, setF, v4) || !hasANonneighbourInX(g, v4, setX)
                                || isYXComplete(g, v4, setX))
                                continue;

                            for (V v3 : g.vertexSet()) {
                                if (v3 == v1 || v3 == v2 || v3 == v4 || v3 == v5
                                    || !g.containsEdge(v2, v3) || !g.containsEdge(v3, v4)
                                    || !g.containsEdge(v5, v3) || g.containsEdge(v1, v3)
                                    || !hasANonneighbourInX(g, v3, setX) || isYXComplete(g, v3, setX))
                                    continue;
                                for (V v6 : setF) {
                                    if (v6 == v1 || v6 == v2 || v6 == v3 || v6 == v4 || v6 == v5
                                        || !g.containsEdge(v4, v6) || g.containsEdge(v1, v6)
                                        || g.containsEdge(v2, v6)
                                        || g.containsEdge(v5, v6) && !isYXComplete(g, v6, setX))
                                        continue;
                                    Set<V> verticesForPv5v6 = new HashSet<>();
                                    verticesForPv5v6.addAll(fPrime);
                                    verticesForPv5v6.add(v5);
                                    verticesForPv5v6.add(v6);
                                    verticesForPv5v6.remove(v1);
                                    verticesForPv5v6.remove(v2);
                                    verticesForPv5v6.remove(v3);
                                    verticesForPv5v6.remove(v4);

                                    if (new ConnectivityInspector<>(
                                        new AsSubgraph<>(g, verticesForPv5v6)).pathExists(v6, v5))
                                    {
                                        if (certify) {
                                            List<E> edgeList = new LinkedList<>();
                                            edgeList.add(g.getEdge(v1, v4));
                                            edgeList.add(g.getEdge(v4, v6));
                                            GraphPath<V, E> path =
                                                new DijkstraShortestPath<>(g).getPath(v6, v5);
                                            edgeList.addAll(path.getEdgeList());
                                            if (path.getLength() % 2 == 1) {
                                                V x = setX.iterator().next();
                                                edgeList.add(g.getEdge(v5, x));
                                                edgeList.add(g.getEdge(x, v1));
                                            } else {
                                                edgeList.add(g.getEdge(v5, v3));
                                                edgeList.add(g.getEdge(v3, v4));
                                                edgeList.add(g.getEdge(v4, v1));
                                            }

                                            double weight = edgeList
                                                .stream().mapToDouble(g::getEdgeWeight).sum();
                                            certificate =
                                                new GraphWalk<>(g, v1, v1, edgeList, weight);
                                        }
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
     * If true, the graph is not Berge. Checks whether g contains a Pyramid, Jewel, configuration
     * type 1, 2 or 3.
     * 
     * @param g A Graph
     * @return whether g contains a pyramid, a jewel, a T1, a T2, or a T3
     */
    private boolean routine2(Graph<V, E> g)
    {
        return containsPyramid(g) || containsJewel(g) || hasConfigurationType1(g)
            || hasConfigurationType2(g) || hasConfigurationType3(g);
    }

    /**
     * N(a,b) is the set of all {a,b}-complete vertices
     * 
     * @param g A Graph
     * @param a A Vertex
     * @param b A Vertex
     * @return The set of all {a,b}-complete vertices
     */
    private Set<V> n(Graph<V, E> g, V a, V b)
    {
        return g
            .vertexSet().stream().filter(t -> g.containsEdge(t, a) && g.containsEdge(t, b))
            .collect(Collectors.toSet());
    }

    /**
     * r(a,b,c) is the cardinality of the largest anticomponent of N(a,b) that contains a
     * nonneighbour of c (or 0, if c is N(a,b)-complete)
     * 
     * @param g a Graph
     * @param nAB The set of all {a,b}-complete vertices
     * @param c A vertex
     * @return The cardinality of the largest anticomponent of N(a,b) that contains a nonneighbour
     *         of c (or 0, if c is N(a,b)-complete)
     */
    private int r(Graph<V, E> g, Set<V> nAB, V c)
    {
        if (isYXComplete(g, c, nAB))
            return 0;
        List<Set<V>> anticomponents = findAllAnticomponentsOfY(g, nAB);
        return anticomponents.stream().mapToInt(Set::size).max().getAsInt();
    }

    /**
     * Y(a,b,c) is the union of all anticomponents of N(a,b) that have cardinality strictly greater
     * than r(a,b,c)
     * 
     * @param g A graph
     * @param nAB The set of all {a,b}-complete vertices
     * @param c A vertex
     * @return A Set of vertices with cardinality greater r(a,b,c)
     */
    private Set<V> y(Graph<V, E> g, Set<V> nAB, V c)
    {
        int cutoff = r(g, nAB, c);
        List<Set<V>> anticomponents = findAllAnticomponentsOfY(g, nAB);
        Set<V> res = new HashSet<>();
        for (Set<V> anticomponent : anticomponents) {
            if (anticomponent.size() > cutoff) {
                res.addAll(anticomponent);
            }
        }
        return res;
    }

    /**
     * W(a,b,c) is the anticomponent of N(a,b)+{c} that contains c
     * 
     * @param g A graph
     * @param nAB The set of all {a,b}-complete vertices
     * @param c A vertex
     * @return The anticomponent of N(a,b)+{c} containing c
     */
    private Set<V> w(Graph<V, E> g, Set<V> nAB, V c)
    {
        Set<V> temp = new HashSet<>();
        temp.addAll(nAB);
        temp.add(c);
        List<Set<V>> anticomponents = findAllAnticomponentsOfY(g, temp);
        for (Set<V> anticomponent : anticomponents)
            if (anticomponent.contains(c))
                return anticomponent;
        return null;
    }

    /**
     * Z(a,b,c) is the set of all (Y(a,b,c)+W(a,b,c))-complete vertices
     * 
     * @param g A graph
     * @param nAB The set of all {a,b}-complete vertices
     * @param c A vertex
     * @return A set of vertices
     */
    private Set<V> z(Graph<V, E> g, Set<V> nAB, V c)
    {
        Set<V> temp = new HashSet<>();
        temp.addAll(y(g, nAB, c));
        temp.addAll(w(g, nAB, c));
        Set<V> res = new HashSet<>();
        for (V it : g.vertexSet()) {
            if (isYXComplete(g, it, temp))
                res.add(it);
        }
        return res;
    }

    /**
     * X(a,b,c)=Y(a,b,c)+Z(a,b,c)
     * 
     * @param g A graph
     * @param nAB The set of all {a,b}-complete vertices
     * @param c A vertex
     * @return The union of Y(a,b,c) and Z(a,b,c)
     */
    private Set<V> x(Graph<V, E> g, Set<V> nAB, V c)
    {
        Set<V> res = new HashSet<>();
        res.addAll(y(g, nAB, c));
        res.addAll(z(g, nAB, c));
        return res;
    }

    /**
     * A triple (a,b,c) of vertices is relevant if a,b are distinct and nonadjacent, and c is not
     * contained in N(a,b) (possibly c is contained in {a,b}).
     * 
     * @param g A graph
     * @param a A vertex
     * @param b A vertex
     * @param c A vertex
     * @return Assessement whether a,b,c is a relevant triple
     */
    private boolean isTripleRelevant(Graph<V, E> g, V a, V b, V c)
    {
        return a != b && !g.containsEdge(a, b) && !n(g, a, b).contains(c);
    }

    /**
     * Returns a set of vertex sets that may be near-cleaners for an amenable hole in g.
     * 
     * @param g A graph
     * @return possible near-cleaners
     */
    Set<Set<V>> routine3(Graph<V, E> g)
    {
        Set<Set<V>> nUVList = new HashSet<>();
        for (V u : g.vertexSet()) {
            for (V v : g.vertexSet()) {
                if (u == v || !g.containsEdge(u, v))
                    continue;
                nUVList.add(n(g, u, v));
            }
        }

        Set<Set<V>> tripleList = new HashSet<>();
        for (V a : g.vertexSet()) {
            for (V b : g.vertexSet()) {
                if (a == b || g.containsEdge(a, b))
                    continue;
                Set<V> nAB = n(g, a, b);
                for (V c : g.vertexSet()) {
                    if (isTripleRelevant(g, a, b, c)) {
                        tripleList.add(x(g, nAB, c));
                    }
                }
            }
        }
        Set<Set<V>> res = new HashSet<>();
        for (Set<V> nUV : nUVList) {
            for (Set<V> triple : tripleList) {
                Set<V> temp = new HashSet<>();
                temp.addAll(nUV);
                temp.addAll(triple);
                res.add(temp);
            }
        }
        return res;
    }

    /**
     * Performs the Berge Recognition Algorithm.
     * <p>
     * First this algorithm is used to test whether $G$ or its complement contain a jewel, a pyramid
     * or a configuration of type 1, 2 or 3. If so, it is output that $G$ is not Berge. If not, then
     * every shortest odd hole in $G$ is amenable. This asserted, the near-cleaner subsets of $V(G)$
     * are determined. For each of them in turn it is checked, if this subset is a near-cleaner and,
     * thus, if there is an odd hole. If an odd hole is found, this checker will output that $G$ is
     * not Berge. If no odd hole is found, all near-cleaners for the complement graph are determined
     * and it will be proceeded as before. If again no odd hole is detected, $G$ is Berge.
     * 
     * <p>
     * A certificate can be obtained through the {@link BergeGraphInspector#getCertificate} method,
     * if <code>computeCertificate</code> is <code>true</code>.
     * <p>
     * Running this method takes $O(|V|^9)$, and computing the certificate takes $O(|V|^5)$.
     * 
     * @param g A graph
     * @param computeCertificate toggles certificate computation
     * @return whether g is Berge and, thus, perfect
     */
    public boolean isBerge(Graph<V, E> g, boolean computeCertificate)
    {
        GraphTests.requireDirectedOrUndirected(g);
        Graph<V, E> complementGraph;
        if (g.getType().isSimple())
            complementGraph = new SimpleGraph<>(
                g.getVertexSupplier(), g.getEdgeSupplier(), g.getType().isWeighted());
        else
            complementGraph = new Multigraph<>(
                g.getVertexSupplier(), g.getEdgeSupplier(), g.getType().isWeighted());
        new ComplementGraphGenerator<>(g).generateGraph(complementGraph);

        certify = computeCertificate;
        if (routine2(g) || routine2(complementGraph)) {
            certify = false;
            return false;
        }

        for (Set<V> it : routine3(g)) {
            if (routine1(g, it)) {
                certify = false;
                return false;
            }
        }

        for (Set<V> it : routine3(complementGraph)) {
            if (routine1(complementGraph, it)) {
                certify = false;
                return false;
            }
        }
        certify = false;
        return true;

    }

    /**
     * Performs the Berge Recognition Algorithm.
     * <p>
     * First this algorithm is used to test whether $G$ or its complement contain a jewel, a pyramid
     * or a configuration of type 1, 2 or 3. If so, it is output that $G$ is not Berge. If not, then
     * every shortest odd hole in $G$ is amenable. This asserted, the near-cleaner subsets of $V(G)$
     * are determined. For each of them in turn it is checked, if this subset is a near-cleaner and,
     * thus, if there is an odd hole. If an odd hole is found, this checker will output that $G$ is
     * not Berge. If no odd hole is found, all near-cleaners for the complement graph are determined
     * and it will be proceeded as before. If again no odd hole is detected, $G$ is Berge.
     * 
     * <p>
     * This method by default does not compute a certificate. For obtaining a certificate, call
     * {@link BergeGraphInspector#isBerge} with <code>computeCertificate=true</code>.
     * <p>
     * Running this method takes $O(|V|^9)$.
     * 
     * @param g A graph
     * @return whether g is Berge and, thus, perfect
     */
    public boolean isBerge(Graph<V, E> g)
    {
        return this.isBerge(g, false);
    }

    /**
     * Returns the certificate in the form of a hole or anti-hole in the inspected graph, when the
     * {@link BergeGraphInspector#isBerge} method is previously called with
     * <code>computeCertificate=true</code>. Returns null if the inspected graph is perfect.
     *
     * @return a <a href="http://graphclasses.org/smallgraphs.html#holes">hole</a> or
     *         <a href="http://graphclasses.org/smallgraphs.html#antiholes">anti-hole</a> in the
     *         inspected graph, null if the graph is perfect
     */
    public GraphPath<V, E> getCertificate()
    {
        return certificate;
    }

}
