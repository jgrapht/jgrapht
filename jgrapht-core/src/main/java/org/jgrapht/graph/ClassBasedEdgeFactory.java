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
    

    private static final long serialVersionUID = 3618135658586388792L;

    

    private final Class<? extends E> edgeClass;

    

    public ClassBasedEdgeFactory(Class<? extends E> edgeClass)
    {
        this.edgeClass = edgeClass;
    }

    

    /**
     * @see EdgeFactory#createEdge(Object, Object)
     */
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
