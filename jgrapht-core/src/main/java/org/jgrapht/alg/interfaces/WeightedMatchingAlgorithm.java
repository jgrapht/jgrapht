/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://org.org.jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2013, by Barak Naveh and Contributors.
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
 * MinimumSpanningTree.java
 * -------------------------
 *
 * Original Author:  Alexey Kudinkin
 * Contributor(s):
 *
 */
package org.jgrapht.alg.interfaces;

/**
 * Allows to derive weighted matching from <i>general</i> graph
 *
 * @param <V>
 * @param <E>
 *
 * @see MatchingAlgorithm
 */
public interface WeightedMatchingAlgorithm<V, E>
    extends MatchingAlgorithm<V, E>
{
    

    /**
     * Returns weight of a matching found
     *
     * @return weight of a matching found
     */
    public double getMatchingWeight();
}

// End WeightedMatchingAlgorithm.java
