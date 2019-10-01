package org.jgrapht.opt.graph.sparse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.jgrapht.GraphType;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.DefaultGraphType;
import org.jgrapht.util.UnmodifiableUnionSet;

/**
 * Base implementation of a sparse unmodifiable directed graph.
 *
 * <p>
 * Assuming the graph has $n$ vertices, the vertices are numbered from $0$ to $n-1$. Similarly,
 * edges are numbered from $0$ to $m-1$ where $m$ is the total number of edges.
 * 
 * <p>
 * It stores two boolean incidence matrix of the graph (rows are vertices and columns are edges) as
 * Compressed Sparse Row (CSR). This is a classic format for write-once read-many use cases. Thus,
 * the graph is unmodifiable.
 * 
 * @author Dimitrios Michail
 */
public abstract class BaseSparseDirectedGraph
    extends
    AbstractGraph<Integer, Integer>
{
    protected static final String UNMODIFIABLE = "this graph is unmodifiable";

    protected CSRBooleanMatrix outIncidenceMatrix;
    protected CSRBooleanMatrix inIncidenceMatrix;

    /**
     * Create a new graph from an edge list.
     * 
     * @param numVertices the number of vertices
     * @param edges the edge list
     */
    public BaseSparseDirectedGraph(int numVertices, List<Pair<Integer, Integer>> edges)
    {
        List<Pair<Integer, Integer>> outgoing = new ArrayList<>();
        List<Pair<Integer, Integer>> incoming = new ArrayList<>();

        int eIndex = 0;
        for (Pair<Integer, Integer> e : edges) {
            outgoing.add(Pair.of(e.getFirst(), eIndex));
            incoming.add(Pair.of(e.getSecond(), eIndex));
            eIndex++;
        }

        outIncidenceMatrix = new CSRBooleanMatrix(numVertices, edges.size(), outgoing);
        inIncidenceMatrix = new CSRBooleanMatrix(numVertices, edges.size(), incoming);
    }

    @Override
    public Supplier<Integer> getVertexSupplier()
    {
        return null;
    }

    @Override
    public Supplier<Integer> getEdgeSupplier()
    {
        return null;
    }

    @Override
    public Integer addEdge(Integer sourceVertex, Integer targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean addEdge(Integer sourceVertex, Integer targetVertex, Integer e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Integer addVertex()
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean addVertex(Integer v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean containsEdge(Integer e)
    {
        return e >= 0 && e < outIncidenceMatrix.columns();
    }

    @Override
    public boolean containsVertex(Integer v)
    {
        return v >= 0 && v < outIncidenceMatrix.rows();
    }

    @Override
    public Set<Integer> edgeSet()
    {
        return new IntegerSet(outIncidenceMatrix.columns());
    }

    @Override
    public int degreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return outIncidenceMatrix.nonZeros(vertex) + inIncidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> edgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return new UnmodifiableUnionSet<>(
            outIncidenceMatrix.rowSet(vertex), inIncidenceMatrix.rowSet(vertex));
    }

    @Override
    public int inDegreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return inIncidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> incomingEdgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return inIncidenceMatrix.rowSet(vertex);
    }

    @Override
    public int outDegreeOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return outIncidenceMatrix.nonZeros(vertex);
    }

    @Override
    public Set<Integer> outgoingEdgesOf(Integer vertex)
    {
        assertVertexExist(vertex);
        return outIncidenceMatrix.rowSet(vertex);
    }

    @Override
    public Integer removeEdge(Integer sourceVertex, Integer targetVertex)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeEdge(Integer e)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeVertex(Integer v)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Set<Integer> vertexSet()
    {
        return new IntegerSet(outIncidenceMatrix.rows());
    }

    @Override
    public GraphType getType()
    {
        return new DefaultGraphType.Builder()
            .directed().weighted(false).modifiable(false).allowMultipleEdges(true)
            .allowSelfLoops(true).build();
    }

    @Override
    public double getEdgeWeight(Integer e)
    {
        return 1.0;
    }

    @Override
    public void setEdgeWeight(Integer e, double weight)
    {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Integer getEdge(Integer sourceVertex, Integer targetVertex)
    {
        if (sourceVertex < 0 || sourceVertex >= outIncidenceMatrix.rows()) {
            return null;
        }
        if (targetVertex < 0 || targetVertex >= outIncidenceMatrix.rows()) {
            return null;
        }

        Iterator<Integer> it = outIncidenceMatrix.nonZerosIterator(sourceVertex);
        while (it.hasNext()) {
            int eId = it.next();
            if (getEdgeTarget(eId) == targetVertex) {
                return eId;
            }
        }
        return null;
    }

    @Override
    public Set<Integer> getAllEdges(Integer sourceVertex, Integer targetVertex)
    {
        if (sourceVertex < 0 || sourceVertex >= outIncidenceMatrix.rows()) {
            return null;
        }
        if (targetVertex < 0 || targetVertex >= outIncidenceMatrix.rows()) {
            return null;
        }

        Set<Integer> result = new HashSet<>();

        Iterator<Integer> it = outIncidenceMatrix.nonZerosIterator(sourceVertex);
        while (it.hasNext()) {
            int eId = it.next();

            if (getEdgeTarget(eId) == targetVertex) {
                result.add(eId);
            }
        }
        return result;
    }

    protected boolean assertVertexExist(Integer v)
    {
        if (v >= 0 && v < outIncidenceMatrix.rows()) {
            return true;
        } else {
            throw new IllegalArgumentException("no such vertex in graph: " + v.toString());
        }
    }

    protected boolean assertEdgeExist(Integer e)
    {
        if (e >= 0 && e < outIncidenceMatrix.columns()) {
            return true;
        } else {
            throw new IllegalArgumentException("no such edge in graph: " + e.toString());
        }
    }

}
