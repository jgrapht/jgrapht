/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2009, by Barak Naveh and Contributors.
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
 * VertexPair.java
 * -------------------------
 * (C) Copyright 2009-2009, by Soren Davidsen and Contributors
 *
 * Original Author:  Soren Davidsen
 *
 * $Id$
 *
 * Changes
 * -------
 * 03-Dec-2009 : Initial revision (SD);
 *
 */
package org.jgrapht.util;

/**
 * Representation of a pair of vertices; to be replaced by Pair<V,V> if Sun ever
 * gets around to adding Pair to java.util.
 *
 * @author Soren <soren@tanesha.net>
 */
public class VertexPair<V>
{
    //~ Instance fields --------------------------------------------------------

    private final V n1;
    private final V n2;

    //~ Constructors -----------------------------------------------------------

    public VertexPair(final V n1, final V n2)
    {
        this.n1 = n1;
        this.n2 = n2;
    }

    //~ Methods ----------------------------------------------------------------

    public V getFirst()
    {
        return n1;
    }

    public V getSecond()
    {
        return n2;
    }

    /**
     * Assess if this pair contains the vertex.
     *
     * @param v The vertex in question
     *
     * @return true if contains, false otherwise
     */
    public boolean hasVertex(final V v)
    {
        return v.equals(n1) || v.equals(n2);
    }

    public V getOther(final V one)
    {
        if (one.equals(n1)) {
            return n2;
        } else if (one.equals(n2)) {
            return n1;
        } else {
            return null;
        }
    }

    @Override public String toString()
    {
        return n1 + "," + n2;
    }

    @Override public boolean equals(final Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        @SuppressWarnings("unchecked") final VertexPair<V> that = (VertexPair<V>) o;

        if (n1 != null ? !n1.equals(that.n1) : that.n1 != null) {
            return false;
        }
        return !(n2 != null ? !n2.equals(that.n2) : that.n2 != null);

    }

    @Override public int hashCode()
    {
        int result = n1 != null ? n1.hashCode() : 0;
        result = 31 * result + (n2 != null ? n2.hashCode() : 0);
        return result;
    }
}

// End VertexPair.java
