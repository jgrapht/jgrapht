package org.jgrapht.opt.graph.fastutil;

import java.util.function.Supplier;

import org.jgrapht.GraphType;
import org.jgrapht.graph.AbstractBaseGraph;

/**
 * A graph implementation using fastutil's map implementations for storage which has 
 * integer vertices and any data type for edges.
 *
 * @param <E> the graph edge type
 * 
 * @author Dimitrios Michail
 */
public class FastutilMapIntegerAnyGraph<E>
    extends
    AbstractBaseGraph<Integer, E>
{
    private static final long serialVersionUID = 6432747838839788559L;

    /**
     * Construct a new graph.
     *
     * @param vertexSupplier the vertex supplier, can be null
     * @param edgeSupplier the edge supplier, can be null
     * @param type the graph type
     * @param fastLookups whether to index vertex pairs to allow (expected) constant time edge
     *        lookups (by vertex endpoints)
     * @throws IllegalArgumentException if the graph type is not supported by this implementation
     */
    public FastutilMapIntegerAnyGraph(
        Supplier<Integer> vertexSupplier, Supplier<E> edgeSupplier, GraphType type, boolean fastLookups)
    {
        super(
            vertexSupplier, edgeSupplier, type,
            fastLookups ? new FastutilFastLookupIntegerAnyGraphSpecificsStrategy<>()
                : new FastutilIntegerAnyGraphSpecificsStrategy<>());
    }

    /**
     * Construct a new graph.
     *
     * @param vertexSupplier the vertex supplier, can be null
     * @param edgeSupplier the edge supplier, can be null
     * @param type the graph type
     * @throws IllegalArgumentException if the graph type is not supported by this implementation
     */
    public FastutilMapIntegerAnyGraph(Supplier<Integer> vertexSupplier, Supplier<E> edgeSupplier, GraphType type)
    {
        this(vertexSupplier, edgeSupplier, type, true);
    }

}
