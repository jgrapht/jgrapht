/*
 * (C) Copyright 2012-2021, by Joris Kinable and Contributors.
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
