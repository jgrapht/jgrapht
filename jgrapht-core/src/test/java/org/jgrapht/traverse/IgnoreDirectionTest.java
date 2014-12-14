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
/* ---------------------------
 * IgnoreDirectionTest.java
 * ---------------------------
 * (C) Copyright 2003-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 08-Aug-2003 : Initial revision (JVS);
 *
 */
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * Tests for the ignoreDirection parameter to XXFirstIterator.
 *
 * <p>NOTE: This test uses hard-coded expected ordering which isn't really
 * guaranteed by the specification of the algorithm. This could cause spurious
 * failures if the traversal implementation changes.</p>
 *
 * @author John V. Sichi
 * @since Aug 8, 2003
 */
public class IgnoreDirectionTest
    extends AbstractGraphIteratorTest
{
    //~ Methods ----------------------------------------------------------------

    @Override
    String getExpectedStr1()
    {
        return "4,9,7,8,2,1,3,6,5";
    }

    @Override
    String getExpectedStr2()
    {
        return "4,9,7,8,2,1,3,6,5,orphan";
    }

    @Override
    String getExpectedFinishString()
    {
        return "5:6:3:1:2:8:7:9:4:orphan:";
    }

    @Override
    AbstractGraphIterator<String, DefaultEdge> createIterator(
        DirectedGraph<String, DefaultEdge> g,
        String vertex)
    {
        // ignore the passed in vertex and always start from v4, since that's
        // the only vertex without out-edges
        UndirectedGraph<String, DefaultEdge> undirectedView =
            new AsUndirectedGraph<String, DefaultEdge>(g);
        AbstractGraphIterator<String, DefaultEdge> i =
            new DepthFirstIterator<String, DefaultEdge>(undirectedView, "4");
        i.setCrossComponentTraversal(true);

        return i;
    }
}

// End IgnoreDirectionTest.java
