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
/* -------------------------
 * RankingPathElement.java
 * -------------------------
 * (C) Copyright 2007-2008, by France Telecom
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Jun-2007 : Initial revision (GB);
 * 05-Jul-2007 : Added support for generics (JVS);
 *
 */
package org.jgrapht.alg;

import org.jgrapht.*;


/**
 * Helper class for {@link KShortestPaths}.
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
final class RankingPathElement<V, E>
    extends AbstractPathElement<V, E>
{
    

    /**
     * Weight of the path.
     */
    private double weight;

    

    /**
     * Creates a path element by concatenation of an edge to a path element.
     *
     * @param pathElement
     * @param edge edge reaching the end vertex of the path element created.
     * @param weight total cost of the created path element.
     */
    RankingPathElement(
        Graph<V, E> graph,
        RankingPathElement<V, E> pathElement,
        E edge,
        double weight)
    {
        super(graph, pathElement, edge);
        this.weight = weight;
    }

    /**
     * Creates an empty path element.
     *
     * @param vertex end vertex of the path element.
     */
    RankingPathElement(V vertex)
    {
        super(vertex);
        this.weight = 0;
    }

    

    /**
     * Returns the weight of the path.
     *
     * @return .
     */
    public double getWeight()
    {
        return this.weight;
    }

    /**
     * Returns the previous path element.
     *
     * @return <code>null</code> is the path is empty.
     */
    public RankingPathElement<V, E> getPrevPathElement()
    {
        return (RankingPathElement<V, E>) super.getPrevPathElement();
    }
}

// End RankingPathElement.java
