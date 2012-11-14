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
/* ------------------------------
 * DijkstraShortestPathTest.java
 * ------------------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 03-Sept-2003 : Initial revision (JVS);
 * 14-Jan-2006 : Factored out ShortestPathTestCase (JVS);
 *
 */
package org.jgrapht.alg;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * .
 *
 * @author John V. Sichi
 */
public class DijkstraShortestPathTest
    extends ShortestPathTestCase
{
    //~ Methods ----------------------------------------------------------------

    /**
     * .
     */
    public void testConstructor()
    {
        DijkstraShortestPath<String, DefaultWeightedEdge> path;
        Graph<String, DefaultWeightedEdge> g = create();

        path =
            new DijkstraShortestPath<String, DefaultWeightedEdge>(
                g,
                V3,
                V4,
                Double.POSITIVE_INFINITY);
        assertEquals(
            Arrays.asList(
                new DefaultEdge[] {
                    e13,
                    e12,
                    e24
                }),
            path.getPathEdgeList());
        assertEquals(10.0, path.getPathLength(), 0);

        path =
            new DijkstraShortestPath<String, DefaultWeightedEdge>(
                g,
                V3,
                V4,
                7);
        assertNull(path.getPathEdgeList());
        assertEquals(Double.POSITIVE_INFINITY, path.getPathLength(), 0);
    }

    @Override
    protected List findPathBetween(
        Graph<String, DefaultWeightedEdge> g,
        String src,
        String dest)
    {
        return DijkstraShortestPath.findPathBetween(g, src, dest);
    }
}

// End DijkstraShortestPathTest.java
