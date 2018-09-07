/*
 * (C) Copyright 2003-2018, by Christoph Zauner and Contributors.
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

/**
 * {@link org.jgrapht.graph.DefaultEdge} does not implement hashCode() or equals(). Therefore
 * comparing two graphs does not work as expected out of the box.
 *
 * @author Christoph Zauner
 */
public class TestEdge
    extends
    DefaultEdge
{

    private static final long serialVersionUID = 1L;

    public TestEdge()
    {
        super();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSource() == null) ? 0 : getSource().hashCode());
        result = prime * result + ((getTarget() == null) ? 0 : getTarget().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestEdge other = (TestEdge) obj;
        if (getSource() == null) {
            if (other.getSource() != null)
                return false;
        } else if (!getSource().equals(other.getSource()))
            return false;
        if (getTarget() == null) {
            if (other.getTarget() != null)
                return false;
        } else if (!getTarget().equals(other.getTarget()))
            return false;
        return true;
    }

}

// End TestEdge.java
