/**
 * Algorithms related to graph cycles.
 * 
 * <h2>Algorithms for enumeration of simple cycles in graphs</h2>
 * 
 * Contains four different algorithms for the enumeration of simple cycles in directed graphs. The
 * worst case time complexity of the algorithms is:
 * <ol>
 * <li>Szwarcfiter and Lauer - $O(V + EC)$</li>
 * <li>Tarjan - $O(VEC)$</li>
 * <li>Johnson - $O(((V+E)C)$</li>
 * <li>Tiernan - $O(V.const^V)$</li>
 * </ol>
 * where $V$ is the number of vertices, $E$ is the number of edges and $C$ is the number of the
 * simple cycles in the graph. All the above implementations work correctly with loops but not with
 * multiple edges. Space complexity for all cases is $O(V+E)$.
 * 
 * <p>
 * The worst case performance is achieved for graphs with special structure, so on practical
 * workloads an algorithm with higher worst case complexity may outperform an algorithm with lower
 * worst case complexity. Note also that "administrative costs" of algorithms with better worst case
 * performance are higher. Also higher is their memory cost.
 * 
 * <p>
 * See the following papers for details of the above algorithms:
 * <ol>
 * <li>J.C.Tiernan An Efficient Search Algorithm Find the Elementary Circuits of a Graph.,
 * Communications of the ACM, V13, 12, (1970), pp. 722 - 726.</li>
 * <li>R.Tarjan, Depth-first search and linear graph algorithms., SIAM J. Comput. 1 (1972), pp.
 * 146-160.</li>
 * <li>R. Tarjan, Enumeration of the elementary circuits of a directed graph, SIAM J. Comput., 2
 * (1973), pp. 211-216.</li>
 * <li>D. B. Johnson, Finding all the elementary circuits of a directed graph, SIAM J. Comput., 4
 * (1975), pp. 77-84.</li>
 * <li>J. L. Szwarcfiter and P. E. Lauer, Finding the elementary cycles of a directed graph in O(n +
 * m) per cycle, Technical Report Series, #60, May 1974, Univ. of Newcastle upon Tyne, Newcastle
 * upon Tyne, England.</li>
 * <li>L. G. Bezem and J. van Leeuwen, Enumeration in graphs., Technical report RUU-CS-87-7,
 * University of Utrecht, The Netherlands, 1987.</li>
 * </ol>
 *
 * <h2>Algorithms for the computation of undirected cycle basis</h2>
 * 
 * <ol>
 * <li>A variant of Paton's algorithm {@link org.jgrapht.alg.cycle.PatonCycleBase}, performing a BFS
 * using a stack which returns a weakly fundamental cycle basis. Supports graphs with self-loops but
 * not multiple (parallel) edges.</li>
 * <li>A variant of Paton's algorithm {@link org.jgrapht.alg.cycle.StackBFSFundamentalCycleBasis},
 * which returns a fundamental cycle basis. This is a more generic implementation which supports
 * self-loops and multiple (parallel) edges.</li>
 * <li>An algorithm {@link org.jgrapht.alg.cycle.QueueBFSFundamentalCycleBasis} which constructs a
 * fundamental cycle basis using a straightforward implementation of BFS using a queue. The
 * implementation supports graphs with self-loops and multiple (parallel) edges.</li>
 * </ol>
 *
 * The worst case time complexity of all above algorithms is $O(|V|^3)$ since the length of the
 * cycle basis can be that large.
 *
 * <p>
 * See the following papers for details of the above algorithms:
 * <ol>
 * <li>K. Paton, An algorithm for finding a fundamental set of cycles for an undirected linear
 * graph, Comm. ACM 12 (1969), pp. 514-518.</li>
 * <li>Narsingh Deo, G. Prabhu, and M. S. Krishnamoorthy. Algorithms for Generating Fundamental
 * Cycles in a Graph. ACM Trans. Math. Softw. 8, 1, 26-42, 1982.</li>
 * </ol>
 * 
 * <h2>Algorithms for the computation of Eulerian cycles</h2>
 * 
 * <ol>
 * <li>An implementation of {@link org.jgrapht.alg.cycle.HierholzerEulerianCycle Hierholzer}'s
 * algorithm for finding an Eulerian cycle in Eulerian graphs.</li>
 * </ol>
 */
package org.jgrapht.alg.cycle;
