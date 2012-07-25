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
/* ------------------
 * ClassBasedEdgeFactory.java
 * ------------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   Christian Hammer
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 * 04-Aug-2003 : Renamed from EdgeFactoryFactory & made utility class (BN);
 * 03-Nov-2003 : Made edge factories serializable (BN);
 * 11-Mar-2004 : Made generic (CH);
 * 28-May-2006 : Moved connectivity info from edge to graph (JVS);
 *
 */
package org.jgrapht.graph;

import java.io.*;

import org.jgrapht.*;


/**
 * An {@link EdgeFactory} for producing edges by using a class as a factory.
 *
 * @author Barak Naveh
 * @since Jul 14, 2003
 */
public class ClassBasedEdgeFactory<V, E>
    implements EdgeFactory<V, E>,
        Serializable
{
    //~ Static fields/initializers ---------------------------------------------

    private static final long serialVersionUID = 3618135658586388792L;

    //~ Instance fields --------------------------------------------------------

    private final Class<? extends E> edgeClass;

    //~ Constructors -----------------------------------------------------------

    public ClassBasedEdgeFactory(Class<? extends E> edgeClass)
    {
        this.edgeClass = edgeClass;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * @see EdgeFactory#createEdge(Object, Object)
     */
    @Override
    public E createEdge(V source, V target)
    {
        try {
            return edgeClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Edge factory failed", ex);
        }
    }
}

// End ClassBasedEdgeFactory.java
