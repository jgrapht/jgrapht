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
 * KSPExampleGraph.java
 * -------------------------
 * (C) Copyright 2007-2008, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 *
 * $Id$
 *
 * Changes
 * -------
 * 23-Sep-2007 : Initial revision (GB);
 *
 */
package org.jgrapht.alg;

import org.jgrapht.graph.*;


/**
 * <img src="./KSPExample.png">
 */
@SuppressWarnings("unchecked")
public class KSPExampleGraph
    extends SimpleWeightedGraph
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = -1850978181764235655L;

    //~ Instance fields --------------------------------------------------------

    public Object edgeAD;

    public Object edgeBT;

    public Object edgeCB;

    public Object edgeCT;

    public Object edgeDE;

    public Object edgeEC;

    public Object edgeSA;

    public Object edgeST;

    //~ Constructors -----------------------------------------------------------

    /**
     * <img src="./Picture1.jpg">
     */
    public KSPExampleGraph()
    {
        super(DefaultWeightedEdge.class);

        addVertices();
        addEdges();
    }

    //~ Methods ----------------------------------------------------------------

    private void addEdges()
    {
        this.edgeST = this.addEdge("S", "T");
        this.edgeSA = this.addEdge("S", "A");
        this.edgeAD = this.addEdge("A", "D");
        this.edgeDE = this.addEdge("D", "E");
        this.edgeEC = this.addEdge("E", "C");
        this.edgeCB = this.addEdge("C", "B");
        this.edgeCT = this.addEdge("C", "T");
        this.edgeBT = this.addEdge("B", "T");

        setEdgeWeight(this.edgeST, 1);
        setEdgeWeight(this.edgeSA, 100);
        setEdgeWeight(this.edgeAD, 1);
        setEdgeWeight(this.edgeDE, 1);
        setEdgeWeight(this.edgeEC, 1);
        setEdgeWeight(this.edgeCB, 1);
        setEdgeWeight(this.edgeCT, 1);
        setEdgeWeight(this.edgeBT, 1);
    }

    private void addVertices()
    {
        addVertex("S");
        addVertex("T");
        addVertex("A");
        addVertex("B");
        addVertex("C");
        addVertex("D");
        addVertex("E");
    }
}

// End KSPExampleGraph.java
