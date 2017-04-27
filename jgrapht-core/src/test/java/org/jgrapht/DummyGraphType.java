package org.jgrapht;

import java.io.Serializable;

import org.jgrapht.graph.DefaultGraphType.Builder;

/**
 * Dummy graph type to test cases where the graph is neither directed nor undirected
 * 
 * @author David Janos Csillik
 *
 */
class DummyGraphType
implements GraphType, Serializable
{
    private static final long serialVersionUID = 4291049312119347475L;

    private final boolean directed;
    private final boolean undirected;
    private final boolean selfLoops;
    private final boolean multipleEdges;
    private final boolean weighted;
    private final boolean allowsCycles;
    private final boolean modifiable;

    public DummyGraphType(
        boolean directed, boolean undirected, boolean selfLoops, boolean multipleEdges,
        boolean weighted, boolean allowsCycles, boolean modifiable)
    {
        this.directed = directed;
        this.undirected = undirected;
        this.selfLoops = selfLoops;
        this.multipleEdges = multipleEdges;
        this.weighted = weighted;
        this.allowsCycles = allowsCycles;
        this.modifiable = modifiable;
    }

    @Override
    public boolean isDirected()
    {
        return directed;
    }

    @Override
    public boolean isUndirected()
    {
        return undirected;
    }

    @Override
    public boolean isMixed()
    {
        return undirected && directed;
    }

    @Override
    public boolean isAllowingMultipleEdges()
    {
        return multipleEdges;
    }

    @Override
    public boolean isAllowingSelfLoops()
    {
        return selfLoops;
    }

    @Override
    public boolean isWeighted()
    {
        return weighted;
    }

    @Override
    public boolean isAllowingCycles()
    {
        return allowsCycles;
    }

    @Override
    public boolean isModifiable()
    {
        return modifiable;
    }

    @Override
    public boolean isSimple()
    {
        return !isAllowingMultipleEdges() && !isAllowingSelfLoops();
    }

    @Override
    public boolean isPseudograph()
    {
        return isAllowingMultipleEdges() && isAllowingSelfLoops();
    }

    @Override
    public boolean isMultigraph()
    {
        return isAllowingMultipleEdges() && !isAllowingSelfLoops();
    }

    @Override
    public GraphType asDirected()
    {
        return new Builder(this).directed().build();
    }

    @Override
    public GraphType asUndirected()
    {
        return new Builder(this).undirected().build();
    }

    @Override
    public GraphType asMixed()
    {
        return new Builder(this).mixed().build();
    }

    @Override
    public GraphType asUnweighted()
    {
        return new Builder(this).weighted(false).build();
    }

    @Override
    public GraphType asWeighted()
    {
        return new Builder(this).weighted(true).build();
    }

    @Override
    public GraphType asModifiable()
    {
        return new Builder(this).modifiable(true).build();
    }

    @Override
    public GraphType asUnmodifiable()
    {
        return new Builder(this).modifiable(false).build();
    }
}
