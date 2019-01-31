/*
 * (C) Copyright 2007-2018, by France Telecom and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.alg.shortestpath;

import org.jgrapht.graph.*;

/**
 * <img src="./Picture1.jpg">
 *
 */
public class Picture1Graph
    extends
    SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>
{
    // ~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = 5587737522611531029L;

    // ~ Instance fields --------------------------------------------------------

    public DefaultWeightedEdge e15;

    public DefaultWeightedEdge e25;

    public DefaultWeightedEdge e27;

    public DefaultWeightedEdge e37;

    public DefaultWeightedEdge e47;

    public DefaultWeightedEdge e56;

    public DefaultWeightedEdge e57;

    public DefaultWeightedEdge e67;

    public DefaultWeightedEdge eS1;

    public DefaultWeightedEdge eS2;

    public DefaultWeightedEdge eS3;

    public DefaultWeightedEdge eS4;

    public DefaultWeightedEdge eS7;

    // ~ Constructors -----------------------------------------------------------

    /**
     * <img src="./Picture1.jpg">
     */
    public Picture1Graph()
    {
        super(DefaultWeightedEdge.class);

        addVertices();
        addEdges();
    }

    // ~ Methods ----------------------------------------------------------------

    private void addEdges()
    {
        this.eS1 = this.addEdge("vS", "v1");
        this.eS2 = this.addEdge("vS", "v2");
        this.eS3 = this.addEdge("vS", "v3");
        this.eS4 = this.addEdge("vS", "v4");
        this.eS7 = this.addEdge("vS", "v7");
        this.e15 = this.addEdge("v1", "v5");
        this.e25 = this.addEdge("v2", "v5");
        this.e27 = this.addEdge("v2", "v7");
        this.e37 = this.addEdge("v3", "v7");
        this.e47 = this.addEdge("v4", "v7");
        this.e56 = this.addEdge("v5", "v6");
        this.e57 = this.addEdge("v5", "v7");
        this.e67 = this.addEdge("v6", "v7");

        setEdgeWeight(this.eS1, 3.0);
        setEdgeWeight(this.eS2, 2.0);
        setEdgeWeight(this.eS3, 10.0);
        setEdgeWeight(this.eS4, 15.0);
        setEdgeWeight(this.eS7, 15.0);
        setEdgeWeight(this.e15, 3.0);
        setEdgeWeight(this.e25, 6.0);
        setEdgeWeight(this.e27, 10.0);
        setEdgeWeight(this.e37, 20.0);
        setEdgeWeight(this.e47, 5.0);
        setEdgeWeight(this.e56, -3.0);
        setEdgeWeight(this.e57, 4.0);
        setEdgeWeight(this.e67, 5.0);
    }

    private void addVertices()
    {
        addVertex("vS");
        addVertex("v1");
        addVertex("v2");
        addVertex("v3");
        addVertex("v4");
        addVertex("v5");
        addVertex("v6");
        addVertex("v7");
    }
}
