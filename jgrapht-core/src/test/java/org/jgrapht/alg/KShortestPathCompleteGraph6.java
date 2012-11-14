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
 * KShortestPathCompleteGraph6.java
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
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
@SuppressWarnings("unchecked")
public class KShortestPathCompleteGraph6
    extends SimpleWeightedGraph
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = 6310990195071210970L;

    //~ Instance fields --------------------------------------------------------

    public Object e12;

    public Object e13;

    public Object e14;

    public Object e23;

    public Object e24;

    public Object e34;

    public Object eS1;

    public Object eS2;

    public Object eS3;

    public Object eS4;

    private Object e15;

    private Object e25;

    private Object e35;

    private Object e45;

    private Object eS5;

    //~ Constructors -----------------------------------------------------------

    public KShortestPathCompleteGraph6()
    {
        super(DefaultWeightedEdge.class);

        addVertices();
        addEdges();
    }

    //~ Methods ----------------------------------------------------------------

    private void addEdges()
    {
        eS1 = addEdge("vS", "v1");
        eS2 = addEdge("vS", "v2");
        eS3 = addEdge("vS", "v3");
        eS4 = addEdge("vS", "v4");
        eS5 = addEdge("vS", "v5");

        e12 = addEdge("v1", "v2");
        e13 = addEdge("v1", "v3");
        e14 = addEdge("v1", "v4");
        e15 = addEdge("v1", "v5");

        e23 = addEdge("v2", "v3");
        e24 = addEdge("v2", "v4");
        e25 = addEdge("v2", "v5");

        e34 = addEdge("v3", "v4");
        e35 = addEdge("v3", "v5");

        e45 = addEdge("v4", "v5");

        setEdgeWeight(eS1, 1.0);
        setEdgeWeight(eS2, 1.0);
        setEdgeWeight(eS3, 1.0);
        setEdgeWeight(eS4, 1.0);
        setEdgeWeight(eS5, 1000.0);

        setEdgeWeight(e12, 1.0);
        setEdgeWeight(e13, 1.0);
        setEdgeWeight(e14, 1.0);
        setEdgeWeight(e15, 1.0);

        setEdgeWeight(e23, 1.0);
        setEdgeWeight(e24, 1.0);
        setEdgeWeight(e25, 1.0);

        setEdgeWeight(e34, 1.0);
        setEdgeWeight(e35, 1.0);

        setEdgeWeight(e45, 1.0);
    }

    private void addVertices()
    {
        addVertex("vS");
        addVertex("v1");
        addVertex("v2");
        addVertex("v3");
        addVertex("v4");
        addVertex("v5");
    }
}

// End KShortestPathCompleteGraph6.java
