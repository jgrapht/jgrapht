/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
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

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;


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
        edgeST = addEdge("S", "T");
        edgeSA = addEdge("S", "A");
        edgeAD = addEdge("A", "D");
        edgeDE = addEdge("D", "E");
        edgeEC = addEdge("E", "C");
        edgeCB = addEdge("C", "B");
        edgeCT = addEdge("C", "T");
        edgeBT = addEdge("B", "T");

        setEdgeWeight(edgeST, 1);
        setEdgeWeight(edgeSA, 100);
        setEdgeWeight(edgeAD, 1);
        setEdgeWeight(edgeDE, 1);
        setEdgeWeight(edgeEC, 1);
        setEdgeWeight(edgeCB, 1);
        setEdgeWeight(edgeCT, 1);
        setEdgeWeight(edgeBT, 1);
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
