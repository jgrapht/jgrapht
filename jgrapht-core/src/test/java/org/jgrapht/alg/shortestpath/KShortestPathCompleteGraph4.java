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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.graph.*;

/**
 * @since July 5, 2007
 */
public class KShortestPathCompleteGraph4
    extends
    SimpleWeightedGraph<String, DefaultWeightedEdge>
{
    // ~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = -4091707260999013100L;

    // ~ Instance fields --------------------------------------------------------

    public DefaultWeightedEdge e12;

    public DefaultWeightedEdge e13;

    public DefaultWeightedEdge e23;

    public DefaultWeightedEdge eS1;

    public DefaultWeightedEdge eS2;

    public DefaultWeightedEdge eS3;

    // ~ Constructors -----------------------------------------------------------

    public KShortestPathCompleteGraph4()
    {
        super(DefaultWeightedEdge.class);
        addVertices();
        addEdges();
    }

    // ~ Methods ----------------------------------------------------------------

    private void addEdges()
    {
        this.eS1 = addEdge("vS", "v1");
        this.eS2 = addEdge("vS", "v2");
        this.eS3 = addEdge("vS", "v3");
        this.e12 = addEdge("v1", "v2");
        this.e13 = addEdge("v1", "v3");
        this.e23 = addEdge("v2", "v3");

        setEdgeWeight(this.eS1, 1.0);
        setEdgeWeight(this.eS2, 1.0);
        setEdgeWeight(this.eS3, 1000.0);
        setEdgeWeight(this.e12, 1.0);
        setEdgeWeight(this.e13, 1.0);
        setEdgeWeight(this.e23, 1.0);
    }

    private void addVertices()
    {
        addVertex("vS");
        addVertex("v1");
        addVertex("v2");
        addVertex("v3");
    }
}

// End KShortestPathCompleteGraph4.java
