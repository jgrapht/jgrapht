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
 * BellmanFordPathElement.java
 * -------------------------
 * (C) Copyright 2006-2008, by France Telecom and Contributors.
 *
 * Original Author:  Guillaume Boulmier and Contributors.
 * Contributor(s):   John V. Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 05-Jan-2006 : Initial revision (GB);
 * 14-Jan-2006 : Added support for generics (JVS);
 *
 */
package org.jgrapht.alg;

import org.jgrapht.*;


/**
 * Helper class for {@link BellmanFordShortestPath}; not intended for general
 * use.
 */
final class BellmanFordPathElement<V, E>
    extends AbstractPathElement<V, E>
{
    

    private double cost = 0;
    private double epsilon;

    

    /**
     * Creates a path element by concatenation of an edge to a path element.
     *
     * @param pathElement
     * @param edge edge reaching the end vertex of the path element created.
     * @param cost total cost of the created path element.
     * @param epsilon tolerance factor.
     */
    protected BellmanFordPathElement(
        Graph<V, E> graph,
        BellmanFordPathElement<V, E> pathElement,
        E edge,
        double cost,
        double epsilon)
    {
        super(graph, pathElement, edge);

        this.cost = cost;
        this.epsilon = epsilon;
    }

    /**
     * Copy constructor.
     *
     * @param original source to copy from
     */
    BellmanFordPathElement(BellmanFordPathElement<V, E> original)
    {
        super(original);
        this.cost = original.cost;
        this.epsilon = original.epsilon;
    }

    /**
     * Creates an empty path element.
     *
     * @param vertex end vertex of the path element.
     * @param epsilon tolerance factor.
     */
    protected BellmanFordPathElement(V vertex, double epsilon)
    {
        super(vertex);

        this.cost = 0;
        this.epsilon = epsilon;
    }

    

    /**
     * Returns the total cost of the path element.
     *
     * @return .
     */
    public double getCost()
    {
        return this.cost;
    }

    /**
     * Returns <code>true</code> if the path has been improved, <code>
     * false</code> otherwise. We use an "epsilon" precision to check whether
     * the cost has been improved (because of many roundings, a formula equal to
     * 0 could unfortunately be evaluated to 10^-14).
     *
     * @param candidatePrevPathElement
     * @param candidateEdge
     * @param candidateCost
     *
     * @return .
     */
    protected boolean improve(
        BellmanFordPathElement<V, E> candidatePrevPathElement,
        E candidateEdge,
        double candidateCost)
    {
        // to avoid improvement only due to rounding errors.
        if (candidateCost < (getCost() - epsilon)) {
            this.prevPathElement = candidatePrevPathElement;
            this.prevEdge = candidateEdge;
            this.cost = candidateCost;
            this.nHops = candidatePrevPathElement.getHopCount() + 1;

            return true;
        } else {
            return false;
        }
    }
}

// End BellmanFordPathElement.java
