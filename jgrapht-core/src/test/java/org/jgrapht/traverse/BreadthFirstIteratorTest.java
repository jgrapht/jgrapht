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
/* -----------------------------
 * BreadthFirstIteratorTest.java
 * -----------------------------
 * (C) Copyright 2003-2008, by Liviu Rau and Contributors.
 *
 * Original Author:  Liviu Rau
 * Contributor(s):   Barak Naveh
 *
 * $Id$
 *
 * Changes
 * -------
 * 30-Jul-2003 : Initial revision (LR);
 * 06-Aug-2003 : Test traversal listener & extract a shared superclass (BN);
 *
 */
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.graph.*;


/**
 * Tests for the {@link BreadthFirstIterator} class.
 *
 * <p>NOTE: This test uses hard-coded expected ordering isn't really guaranteed
 * by the specification of the algorithm. This could cause false failures if the
 * traversal implementation changes.</p>
 *
 * @author Liviu Rau
 * @since Jul 30, 2003
 */
public class BreadthFirstIteratorTest
    extends AbstractGraphIteratorTest
{
    //~ Methods ----------------------------------------------------------------

    @Override
    String getExpectedStr1()
    {
        return "1,2,3,4,5,6,7,8,9";
    }

    @Override
    String getExpectedStr2()
    {
        return "1,2,3,4,5,6,7,8,9,orphan";
    }

    @Override
    AbstractGraphIterator<String, DefaultEdge> createIterator(
        DirectedGraph<String, DefaultEdge> g,
        String vertex)
    {
        AbstractGraphIterator<String, DefaultEdge> i =
            new BreadthFirstIterator<String, DefaultEdge>(g, vertex);
        i.setCrossComponentTraversal(true);

        return i;
    }
}

// End BreadthFirstIteratorTest.java
