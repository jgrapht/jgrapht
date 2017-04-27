package org.jgrapht;

import java.io.Serializable;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultGraphType.Builder;

/**
 * Dummy graph which is neither directed nor undirected in the same time.
 * 
 * @author David Janos Csillik
 */
class DummyGraph<V, E>
extends AbstractBaseGraph<V, E> {
	private static final long serialVersionUID = 31158389806032591L;

	public DummyGraph(Class<? extends E> edgeClass)
    {
        this(new ClassBasedEdgeFactory<>(edgeClass));
    }

    protected DummyGraph(EdgeFactory<V, E> ef)
    {
        super(ef, false, false, false, false);
    }

    public boolean isDirected()
    {
        return false;
    }

    public boolean isUndirected()
    {
        return false;
    }

    @Override
    public GraphType getType()
    {
        return new DummyGraphType(false,false,false,false,false,false,false);
    }
}
