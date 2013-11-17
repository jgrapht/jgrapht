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
 * BiconnectedGraph.java
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
public class BiconnectedGraph
    extends SimpleGraph
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = 6007460525580983710L;

    //~ Constructors -----------------------------------------------------------

    public BiconnectedGraph()
    {
        super(DefaultEdge.class);

        addVertices();
        addEdges();
    }

    //~ Methods ----------------------------------------------------------------

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
