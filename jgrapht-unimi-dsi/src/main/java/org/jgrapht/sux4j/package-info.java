/**
 * Immutable graphs stored using <a href="http://sux4j.di.unimi.it/">Sux4J</a>'s quasi-succinct data
 * structures.
 *
 * <p>
 * This package contains implementations of immutable graphs based on the
 * {@linkplain it.unimi.dsi.sux4j.util.EliasFanoIndexedMonotoneLongBigList Elias&ndash;Fano
 * quasi-succinct representation of monotone sequences}. The positions of the nonzero entries of the
 * adjacency matrix of a graph are represented as a monotone sequence of natural numbers.
 *
 * <p>
 * The memory footprint of these implementation is close to the information-theoretical lower bound
 * in the undirected case, and close to twice the information-theoretical lower bound in the
 * directed case, because the transposed graph must be stored separately, but in the latter case you
 * have the choice to not support incoming edges and obtain, again, footprint close to the
 * information-theoretical lower bound. The actual space used can be easily measured as all
 * implementations are serializable, and their in-memory footprint is very close to the on-disk
 * footprint. Usually the size is a few times smaller than that of a
 * {@link org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph
 * SparseIntDirectedGraph}/{@link org.jgrapht.opt.graph.sparse.SparseIntUndirectedGraph
 * SparseIntUndirectedGraph}.
 *
 * <p>
 * We provide two classes mimicking {@link org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph
 * SparseIntDirectedGraph} and {@link org.jgrapht.opt.graph.sparse.SparseIntUndirectedGraph
 * SparseIntUndirectedGraph}, in the sense that both vertices and edges are integers (and they are
 * numbered contiguously). Thus, by definition these classes cannot represent graphs with more than
 * {@link java.lang.Integer#MAX_VALUE} edges.
 *
 * <ul>
 * <li>{@link org.jgrapht.sux4j.SuccinctIntDirectedGraph} is an implementation for directed graphs.
 * {@linkplain org.jgrapht.GraphIterables#outgoingEdgesOf(Object) Enumeration of outgoing edges} is
 * quite fast, but {@linkplain org.jgrapht.GraphIterables#incomingEdgesOf(Object) enumeration of
 * incoming edges} is very slow. {@linkplain org.jgrapht.Graph#containsEdge(Object) Adjacency tests}
 * are very fast and happen in almost constant time.
 * <li>{@link org.jgrapht.sux4j.SuccinctIntUndirectedGraph} is an implementation for undirected
 * graphs. {@linkplain org.jgrapht.GraphIterables#edgesOf(Object) Enumeration of edges} is very
 * slow. {@linkplain org.jgrapht.Graph#containsEdge(Object) Adjacency tests} are very fast and
 * happen in almost constant time.
 * </ul>
 *
 * <p>
 * The sometimes slow behavior of the previous classes is due to a clash between JGraphT's design
 * and the need of representing an edge with an {@link java.lang.Integer Integer}, which cannot be
 * extended: there is no information that can be carried by the object representing the edge. This
 * limitation forces the two classes above to compute two expensive functions that are one the
 * inverse of the other.
 *
 * <p>
 * As an alternative, we provide classes {@link org.jgrapht.sux4j.SuccinctDirectedGraph
 * SuccinctDirectedGraph} and {@link org.jgrapht.sux4j.SuccinctUndirectedGraph
 * SuccinctUndirectedGraph} using the same amount of space, but having edges represented by pairs of
 * integers stored in an {@link it.unimi.dsi.fastutil.ints.IntIntPair IntIntPair} (for directed
 * graphs) or an {@link it.unimi.dsi.fastutil.ints.IntIntSortedPair IntIntSortedPair} (for
 * undirected graphs). Storing the edges explicitly avoids the cumbersome back-and-forth
 * computations of the previous classes. All accessors are extremely fast. There is no limitation on
 * the number of edges.
 *
 * <p>
 * Both classes provide methods
 * {@link org.jgrapht.sux4j.SuccinctDirectedGraph#getEdgeFromIndex(long) getEdgeFromIndex()} and
 * {@link org.jgrapht.sux4j.SuccinctDirectedGraph#getIndexFromEdge(it.unimi.dsi.fastutil.ints.IntIntPair)
 * getIndexFromEdge()} that map bijectively the edge set into a contiguous set of longs. In this way
 * the user can choose when and how to use the feature (e.g., to store compactly data associated to
 * edges).
 *
 * <p>
 * Finally, note that the best performance and compression can be obtained by representing the graph
 * using <a href="http://webgraph.di.unimi.it/">WebGraph</a>'s {@link it.unimi.dsi.webgraph.EFGraph
 * EFGraph} format and then accessing the graph using the suitable {@linkplain org.jgrapht.webgraph
 * adapter}; in particular, one can represent graphs with more than {@link java.lang.Integer#MAX_VALUE}
 * vertices. However, the adapters do not provide methods mapping bijectively edges into a
 * contiguous set of integers.
 *
 * <h2>Building and serializing with limited memory</h2>
 *
 * <p>
 * All implementations provide a copy constructor taking a {@link org.jgrapht.Graph Graph} and a
 * constructor accepting a list of edges; the latter just builds a sparse graph and delegates to the
 * copy constructor. Both methods can be inconvenient if the graph to be represented is large, as
 * the list of edges might have too large a footprint.
 *
 * <p>
 * There is however a simple strategy that makes it possible to build succinct representations using
 * a relatively small amount of additional memory with respect to the representation itself:
 * <ol>
 * <li>{@linkplain it.unimi.dsi.webgraph convert your graph to a WebGraph format} such as
 * {@link it.unimi.dsi.big.webgraph.BVGraph BVGraph} or {@link it.unimi.dsi.webgraph.EFGraph
 * EFGraph};
 * <li>if your graph is directed, use {@link it.unimi.dsi.webgraph.Transform Transform} to store the
 * transpose of your graph in the same way;
 * <li>use a {@linkplain org.jgrapht.webgraph suitable adapter} to get a {@link org.jgrapht.Graph
 * Graph} representing your graph, taking care of loading the WebGraph representations using
 * {@link it.unimi.dsi.webgraph.ImmutableGraph#loadMapped(java.lang.CharSequence)}  ImmutableGraph.loadMapped()};
 * <li>use the copy constructor to obtain a quasi-succinct representation.
 * </ol>
 */
package org.jgrapht.sux4j;
