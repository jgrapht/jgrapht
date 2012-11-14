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

import org.jgrapht.Graph;


/**
 * Helper class for {@link KShortestPaths}.
 *
 * @author Guillaume Boulmier
 * @since July 5, 2007
 */
final class RankingPathElement<V, E>
    extends AbstractPathElement<V, E>
{
    //~ Instance fields --------------------------------------------------------

    /**
     * Weight of the path.
     */
    private final double weight;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a path element by concatenation of an edge to a path element.
     *
     * @param pathElement
     * @param edge edge reaching the end vertex of the path element created.
     * @param weight total cost of the created path element.
     */
    RankingPathElement(
        final Graph<V, E> graph,
        final RankingPathElement<V, E> pathElement,
        final E edge,
        final double weight)
    {
        super(graph, pathElement, edge);
        this.weight = weight;
    }

    /**
     * Creates an empty path element.
     *
     * @param vertex end vertex of the path element.
     */
    RankingPathElement(final V vertex)
    {
        super(vertex);
        weight = 0;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the weight of the path.
     *
     * @return .
     */
    public double getWeight()
    {
        return weight;
    }

    /**
     * Returns the previous path element.
     *
     * @return <code>null</code> is the path is empty.
     */
    @Override
    public RankingPathElement<V, E> getPrevPathElement()
    {
        return (RankingPathElement<V, E>) super.getPrevPathElement();
    }
}

// End RankingPathElement.java
