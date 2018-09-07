/*
 * (C) Copyright 2017-2018, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.color;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;
import org.jgrapht.graph.*;

import java.util.*;

/**
 * Coloring tests
 * 
 * @author Dimitrios Michail
 */
public class RandomGreedyColoringTest
    extends
    BaseColoringTest
{

    final long seed = 13;

    @Override
    protected VertexColoringAlgorithm<Integer> getAlgorithm(Graph<Integer, DefaultEdge> graph)
    {
        return new RandomGreedyColoring<>(graph, new Random(seed));
    }

    @Override
    protected int getExpectedResultOnDSaturNonOptimalGraph()
    {
        return 3;
    }

}
