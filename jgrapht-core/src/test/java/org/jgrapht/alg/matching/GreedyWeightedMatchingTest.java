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
package org.jgrapht.alg.matching;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

/**
 * Unit tests for the GreedyWeightedMatching algorithm.
 * 
 * @author Dimitrios Michail
 */
public class GreedyWeightedMatchingTest
    extends
    ApproximateWeightedMatchingTest
{

    @Override
    public MatchingAlgorithm<Integer, DefaultWeightedEdge> getApproximationAlgorithm(
        Graph<Integer, DefaultWeightedEdge> graph)
    {
        return new GreedyWeightedMatching<>(graph, false);
    };

}

// End GreedyWeightedMatchingTest.java
