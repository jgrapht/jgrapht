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
/* -----------------
 * IntegerVertexFactory.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism;

import org.jgrapht.*;


/**
 * Implements createVertex() by producing a sequence of Integers; their values
 * start with the successor to the constructor value.
 *
 * <p>for example : IntegerVertexFactory(10); the first createVertex() will
 * return Integer=11
 *
 * @author Assaf
 * @since May 25, 2005
 */
public class IntegerVertexFactory
    implements VertexFactory<Integer>
{
    //~ Instance fields --------------------------------------------------------

    private int counter;

    //~ Constructors -----------------------------------------------------------

    /**
     * Equivalent to IntegerVertexFactory(0);
     *
     * @author Assaf
     * @since Aug 6, 2005
     */
    public IntegerVertexFactory()
    {
        this(0);
    }

    public IntegerVertexFactory(int oneBeforeFirstValue)
    {
        this.counter = oneBeforeFirstValue;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Integer createVertex()
    {
        this.counter++;
        return new Integer(this.counter);
    }
}

// End IntegerVertexFactory.java
