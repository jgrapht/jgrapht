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
/* -------------------------
 * ClassBasedVertexFactory.java
 * -------------------------
 * (C) Copyright 2007-2008, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Jun-2007 : Initial revision (GB);
 *
 */
package org.jgrapht.graph;

import org.jgrapht.*;


/**
 * A {@link VertexFactory} for producing vertices by using a class as a factory.
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
public class ClassBasedVertexFactory<V>
    implements VertexFactory<V>
{
    //~ Instance fields --------------------------------------------------------

    private final Class<? extends V> vertexClass;

    //~ Constructors -----------------------------------------------------------

    public ClassBasedVertexFactory(Class<? extends V> vertexClass)
    {
        this.vertexClass = vertexClass;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see VertexFactory#createVertex()
     */
    @Override
    public V createVertex()
    {
        try {
            return this.vertexClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Vertex factory failed", e);
        }
    }
}

// End ClassBasedVertexFactory.java
