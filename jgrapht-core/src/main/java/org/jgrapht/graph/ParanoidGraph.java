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
/* -------------------
 * ParanoidGraph.java
 * -------------------
 * (C) Copyright 2007-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 8-Nov-2007 : Initial revision (JVS);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;


/**
 * ParanoidGraph provides a way to verify that objects added to a graph obey the
 * standard equals/hashCode contract. It can be used to wrap an underlying graph
 * to be verified. Note that the verification is very expensive, so
 * ParanoidGraph should only be used during debugging.
 *
 * @author John Sichi
 * @version $Id$
 */
public class ParanoidGraph<V, E>
    extends GraphDelegator<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    /**
     */
    private static final long serialVersionUID = 5075284167422166539L;

    //~ Constructors -----------------------------------------------------------

    public ParanoidGraph(final Graph<V, E> g)
    {
        super(g);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see Graph#addEdge(Object, Object, Object)
     */
    @Override
    public boolean addEdge(final V sourceVertex, final V targetVertex, final E e)
    {
        verifyAdd(edgeSet(), e);
        return super.addEdge(sourceVertex, targetVertex, e);
    }

    /**
     * @see Graph#addVertex(Object)
     */
    @Override
    public boolean addVertex(final V v)
    {
        verifyAdd(vertexSet(), v);
        return super.addVertex(v);
    }

    private static <T> void verifyAdd(final Set<T> set, final T t)
    {
        for (final T o : set) {
            if (o == t) {
                continue;
            }
            if (o.equals(t) && o.hashCode() != t.hashCode()) {
                throw new IllegalArgumentException(
                    "ParanoidGraph detected objects "
                    + "o1 (hashCode=" + o.hashCode()
                    + ") and o2 (hashCode=" + t.hashCode()
                    + ") where o1.equals(o2) "
                    + "but o1.hashCode() != o2.hashCode()");
            }
        }
    }
}

// End ParanoidGraph.java
