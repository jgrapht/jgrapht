/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.alg.connectivity;

import org.jgrapht.graph.*;

/**
 * @since July 5, 2007
 */
public class BiconnectedGraph
    extends
    SimpleGraph<String, DefaultEdge>
{
    // ~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = 6007460525580983710L;

    // ~ Constructors -----------------------------------------------------------

    public BiconnectedGraph()
    {
        super(DefaultEdge.class);

        addVertices();
        addEdges();
    }

    // ~ Methods ----------------------------------------------------------------

    private void addEdges()
    {
        addEdge("0", "1");
        addEdge("1", "2");
        addEdge("2", "3");
        addEdge("3", "4");
        addEdge("4", "5");
        addEdge("5", "0");
    }

    private void addVertices()
    {
        addVertex("0");
        addVertex("1");
        addVertex("2");
        addVertex("3");
        addVertex("4");
        addVertex("5");
    }
}

// End BiconnectedGraph.java
