/*
 * (C) Copyright 2018-2018, by Linda Buisman and Contributors.
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
package org.jgrapht.alg.vertexcover;

import org.jgrapht.alg.interfaces.MinimumVertexCoverAlgorithm;
import org.jgrapht.alg.interfaces.MinimumWeightedVertexCoverAlgorithm;

public class RecursiveExactVCImplTest extends WeightedVertexCoverExactTest {

    @Override
    public <V, E> MinimumVertexCoverAlgorithm<V, E> createSolver() {
        return new RecursiveExactVCImpl<>();
    }

    @Override
    public <V, E> MinimumWeightedVertexCoverAlgorithm<V, E> createWeightedSolver() {
        return new RecursiveExactVCImpl<>();
    }
}