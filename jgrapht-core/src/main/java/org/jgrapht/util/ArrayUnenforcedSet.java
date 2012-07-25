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
 * ArrayUnenforcedSet.java
 * -----------------
 * (C) Copyright 2006-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 07-May-2006 : Initial version (JVS);
 */
package org.jgrapht.util;

import java.util.*;


/**
 * Helper for efficiently representing small sets whose elements are known to be
 * unique by construction, implying we don't need to enforce the uniqueness
 * property in the data structure itself. Use with caution.
 *
 * <p>Note that for equals/hashCode, the class implements the Set behavior
 * (unordered), not the list behavior (ordered); the fact that it subclasses
 * ArrayList should be considered an implementation detail.
 *
 * @author John V. Sichi
 */
public class ArrayUnenforcedSet<E>
    extends ArrayList<E>
    implements Set<E>
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = -7413250161201811238L;

    //~ Constructors -----------------------------------------------------------

    public ArrayUnenforcedSet()
    {
        super();
    }

    public ArrayUnenforcedSet(Collection<? extends E> c)
    {
        super(c);
    }

    public ArrayUnenforcedSet(int n)
    {
        super(n);
    }

    //~ Methods ----------------------------------------------------------------

    public boolean equals(Object o)
    {
        return new SetForEquality().equals(o);
    }

    public int hashCode()
    {
        return new SetForEquality().hashCode();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Multiple inheritance helper.
     */
    private class SetForEquality
        extends AbstractSet<E>
    {
        @Override
        public Iterator<E> iterator()
        {
            return ArrayUnenforcedSet.this.iterator();
        }

        @Override
        public int size()
        {
            return ArrayUnenforcedSet.this.size();
        }
    }
}

// End ArrayUnenforcedSet.java
