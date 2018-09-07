/*
 * (C) Copyright 2005-2018, by John V Sichi and Contributors.
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
package org.jgrapht.alg.clique;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.concurrent.*;

/**
 * .
 *
 * @author John V. Sichi
 */
public class PivotBronKerboschCliqueFinderTest
    extends
    BaseBronKerboschCliqueFinderTest
{

    @Override
    protected BaseBronKerboschCliqueFinder<String, DefaultEdge> createFinder1(
        Graph<String, DefaultEdge> graph)
    {
        return new PivotBronKerboschCliqueFinder<>(graph);
    }

    @Override
    protected BaseBronKerboschCliqueFinder<Object, DefaultEdge> createFinder2(
        Graph<Object, DefaultEdge> graph)
    {
        return new PivotBronKerboschCliqueFinder<>(graph);
    }

    @Override
    protected BaseBronKerboschCliqueFinder<Object, DefaultEdge> createFinder2(
        Graph<Object, DefaultEdge> graph, long timeout, TimeUnit unit)
    {
        return new PivotBronKerboschCliqueFinder<>(graph, timeout, unit);
    }

}
