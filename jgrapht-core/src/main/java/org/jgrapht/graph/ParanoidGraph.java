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
    

    /**
     */
    private static final long serialVersionUID = 5075284167422166539L;

    

    public ParanoidGraph(Graph<V, E> g)
    {
        super(g);
    }

    

    /**
     * @see Graph#addEdge(Object, Object, Object)
     */
    public boolean addEdge(V sourceVertex, V targetVertex, E e)
    {
        verifyAdd(edgeSet(), e);
        return super.addEdge(sourceVertex, targetVertex, e);
    }

    /**
     * @see Graph#addVertex(Object)
     */
    public boolean addVertex(V v)
    {
        verifyAdd(vertexSet(), v);
        return super.addVertex(v);
    }

    private static <T> void verifyAdd(Set<T> set, T t)
    {
        for (T o : set) {
            if (o == t) {
                continue;
            }
            if (o.equals(t) && (o.hashCode() != t.hashCode())) {
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
