/*
 * (C) Copyright 2003-2017, by Barak Naveh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.graph;

import org.jgrapht.*;

/**
 * A default implementation for edges in a {@link Graph}.
 *
 * @author Barak Naveh
 * @since Jul 14, 2003
 */
public class DefaultEdge
    extends IntrusiveEdge
{
    private static final long serialVersionUID = 3258408452177932855L;

    /**
     * Retrieves the source of this edge. This is protected, for use by subclasses only (e.g. for
     * implementing toString).
     *
     * @return source of this edge
     */
    protected Object getSource()
    {
        return source;
    }

    /**
     * Retrieves the target of this edge. This is protected, for use by subclasses only (e.g. for
     * implementing toString).
     *
     * @return target of this edge
     */
    protected Object getTarget()
    {
        return target;
    }

    @Override
    public String toString()
    {
        return "(" + source + " : " + target + ")";
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DefaultEdge)) {
            return false;
        }

        DefaultEdge other = (DefaultEdge) obj;

        boolean sourceE = false;
        if(null == source) {
            sourceE = (null == other.source);
        } else {
            sourceE = source.equals(other.source);
        }

        boolean targetE = false;
        if(null == target) {
            targetE = (null == other.target);
        } else {
            targetE = target.equals(other.target);
        }
        return sourceE && targetE;
    }

    @Override public int hashCode() {
        int hash = 1;
        hash = hash *  17 + (null == source ? 0: source.hashCode());
        hash = hash * 31 + (null == target ? 0 : target.hashCode());
        return hash;
    }
}

// End DefaultEdge.java
