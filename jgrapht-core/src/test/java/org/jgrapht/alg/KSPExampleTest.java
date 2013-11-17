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
 * KSPExampleTest.java
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

import junit.framework.*;

import org.jgrapht.graph.*;


@SuppressWarnings("unchecked")
public class KSPExampleTest
    extends TestCase
{
    //~ Methods ----------------------------------------------------------------

    public void testFourReturnedPathsJGraphT()
    {
        SimpleWeightedGraph graph = new KSPExampleGraph();

        Object sourceVertex = "S";
        KShortestPaths ksp = new KShortestPaths(graph, sourceVertex, 4);

        Object targetVertex = "T";
        assertEquals(3, ksp.getPaths(targetVertex).size());
    }

    public void testThreeReturnedPathsJGraphT()
    {
        SimpleWeightedGraph graph = new KSPExampleGraph();

        Object sourceVertex = "S";
        int nbPaths = 3;
        KShortestPaths ksp = new KShortestPaths(graph, sourceVertex, nbPaths);

        Object targetVertex = "T";
        assertEquals(nbPaths, ksp.getPaths(targetVertex).size());
    }

    public void testTwoReturnedPathsJGraphT()
    {
        SimpleWeightedGraph graph = new KSPExampleGraph();

        Object sourceVertex = "S";
        int nbPaths = 2;
        KShortestPaths ksp = new KShortestPaths(graph, sourceVertex, nbPaths);

        Object targetVertex = "T";
        assertEquals(nbPaths, ksp.getPaths(targetVertex).size());
    }
}

// End $file.name$
