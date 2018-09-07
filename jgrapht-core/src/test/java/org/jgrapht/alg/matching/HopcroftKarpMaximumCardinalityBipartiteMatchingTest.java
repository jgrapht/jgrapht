/*
 * (C) Copyright 2012-2018, by Joris Kinable and Contributors.
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

import java.util.*;

/**
 * Unit test for the HopcroftKarpMaximumCardinalityBipartiteMatching class
 * 
 * @author Joris Kinable
 *
 */
public class HopcroftKarpMaximumCardinalityBipartiteMatchingTest
    extends
    MaximumCardinalityBipartiteMatchingTest
{

    @Override
    public MatchingAlgorithm<Integer, DefaultEdge> getMatchingAlgorithm(
        Graph<Integer, DefaultEdge> graph, Set<Integer> partition1, Set<Integer> partition2)
    {
        return new HopcroftKarpMaximumCardinalityBipartiteMatching<>(graph, partition1, partition2);
    }
}
