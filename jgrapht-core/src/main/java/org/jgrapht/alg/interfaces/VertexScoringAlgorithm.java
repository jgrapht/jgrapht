/*
 * (C) Copyright 2016-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.interfaces;

import java.util.*;

/**
 * An interface for all algorithms which assign scores to vertices of a graph.
 * 
 * @param <V> the vertex type
 * @param <D> the score type
 * 
 * @author Dimitrios Michail
 * @since August 2016
 */
public interface VertexScoringAlgorithm<V, D>
{

    /**
     * Get a map with the scores of all vertices
     * 
     * @return a map with all scores
     */
    Map<V, D> getScores();

    /**
     * Get a vertex score
     * 
     * @param v the vertex
     * @return the score
     */
    D getVertexScore(V v);

}
