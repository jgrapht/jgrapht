package org.jgrapht.graph;

public class IntrusiveEdgeException
    extends
    IllegalStateException
{
    private static final long serialVersionUID = 7261763645809925025L;

    public <V> IntrusiveEdgeException(V source, V target)
    {
        super("Edge already associated with source <" + source + "> and target <" + target + ">");
    }
}
