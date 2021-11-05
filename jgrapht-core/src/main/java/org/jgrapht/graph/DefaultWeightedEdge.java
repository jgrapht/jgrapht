/*
 * (C) Copyright 2006-2021, by John V Sichi and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.graph;

/**
 * A default implementation for edges in a weighted graph. All access to the weight of an edge must
 * go through the graph interface, which is why this class doesn't expose any public methods.
 *
 * @author John V. Sichi
 */
public class DefaultWeightedEdge
    extends
    IntrusiveWeightedEdge
{
    private static final long serialVersionUID = -3259071493169286685L;

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

    /**
     * Retrieves the weight of this edge. This is protected, for use by subclasses only (e.g. for
     * implementing toString).
     *
     * @return weight of this edge
     */
    protected double getWeight()
    {
        return weight;
    }

    @Override
    public String toString()
    {
        return "(" + source + " : " + target + ")";
    }

}
