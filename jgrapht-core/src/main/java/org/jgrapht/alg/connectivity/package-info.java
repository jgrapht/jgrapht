/**
 * Algorithms dealing with various connectivity aspects of a graph.
 *
 * A graph is connected when there is a path between every pair of vertices. In a connected graph,
 * there are no unreachable vertices. A graph that is not connected is disconnected. A connected
 * component is a maximal connected subgraph of $G$. Each vertex belongs to exactly one connected
 * component, as does each edge.
 * <p>
 * A directed graph is called weakly connected if replacing all of its directed edges with
 * undirected edges produces a connected (undirected) graph. It is strongly connected if it contains
 * a directed path from $u$ to $v$ and a directed path from $v$ to $u$ for every pair of vertices
 * $u$, $v$. The strong components are the maximal strongly connected subgraphs.
 *
 */
package org.jgrapht.alg.connectivity;
