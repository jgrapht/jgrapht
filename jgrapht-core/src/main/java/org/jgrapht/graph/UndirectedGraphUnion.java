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
 * UndirectedGraphUnion.java
 * -------------------------
 * (C) Copyright 2009-2009, by Ilya Razenshteyn
 *
 * Original Author:  Ilya Razenshteyn and Contributors.
 *
 * $Id$
 *
 * Changes
 * -------
 * 02-Feb-2009 : Initial revision (IR);
 *
 */
package org.jgrapht.graph;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.util.*;


public class UndirectedGraphUnion<V, E>
    extends GraphUnion<V, E, UndirectedGraph<V, E>>
    implements UndirectedGraph<V, E>
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = -740199233080172450L;

    //~ Constructors -----------------------------------------------------------

    UndirectedGraphUnion(
        UndirectedGraph<V, E> g1,
        UndirectedGraphUnion<V, E> g2,
        WeightCombiner operator)
    {
        super(g1, g2, operator);
    }

    UndirectedGraphUnion(
        UndirectedGraph<V, E> g1,
        UndirectedGraphUnion<V, E> g2)
    {
        super(g1, g2);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public int degreeOf(V vertex)
    {
        Set<E> res = edgesOf(vertex);
        return res.size();
    }
}

// End UndirectedGraphUnion.java
