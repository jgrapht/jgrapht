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

import org.jgrapht.VertexFactory;


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

    public IntegerVertexFactory(final int oneBeforeFirstValue)
    {
        counter = oneBeforeFirstValue;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Integer createVertex()
    {
        counter++;
        return new Integer(counter);
    }
}

// End IntegerVertexFactory.java
