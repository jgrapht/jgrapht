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
 * MatchingAlgorithm.java
 * -------------------------
 *
 * Original Author:  Alexey Kudinkin
 * Contributor(s):
 *
 */
package org.jgrapht.alg.interfaces;

import java.util.*;


/**
 * Allows to derive <a
 * href="http://en.wikipedia.org/wiki/Matching_(graph_theory)">matching</a> from
 * given graph
 *
 * @param <V> vertex concept type
 * @param <E> edge concept type
 */
public abstract interface MatchingAlgorithm<V, E>
{
    

    /**
     * Returns set of edges making up the matching
     */
    public Set<E> getMatching();
}

// End MatchingAlgorithm.java
