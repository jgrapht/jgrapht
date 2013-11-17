/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -------------------------
 * Picture1Graph.java
 * -------------------------
 * (C) Copyright 2007-2008, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Jun-2007 : Initial revision (GB);
 *
 */
package org.jgrapht.alg;

import org.jgrapht.graph.*;


/**
 * <img src="./Picture1.jpg">
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
@SuppressWarnings("unchecked")
public class Picture1Graph
    extends SimpleDirectedWeightedGraph
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = 5587737522611531029L;

    //~ Instance fields --------------------------------------------------------

    public Object e15;

    public Object e25;

    public Object e27;

    public Object e37;

    public Object e47;

    public Object e56;

    public Object e57;

    public Object e67;

    public Object eS1;

    public Object eS2;

    public Object eS3;

    public Object eS4;

    public Object eS7;

    //~ Constructors -----------------------------------------------------------

    /**
     * <img src="./Picture1.jpg">
     */
    public Picture1Graph()
    {
        super(DefaultWeightedEdge.class);

        addVertices();
        addEdges();
    }

    //~ Methods ----------------------------------------------------------------

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

// End Picture1Graph.java
