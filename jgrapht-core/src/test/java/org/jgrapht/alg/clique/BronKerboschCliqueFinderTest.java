/*
 * (C) Copyright 2005-2021, by John V Sichi and Contributors.
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
package org.jgrapht.alg.clique;

import org.jgrapht.*;
import org.jgrapht.graph.*;

import java.util.concurrent.*;

/**
 * .
 *
 * @author John V. Sichi
 */
public class BronKerboschCliqueFinderTest
    extends
    BaseBronKerboschCliqueFinderTest
{

    @Override
    protected BaseBronKerboschCliqueFinder<String, DefaultEdge> createFinder1(
        Graph<String, DefaultEdge> graph)
    {
        return new BronKerboschCliqueFinder<>(graph);
    }

    @Override
    protected BaseBronKerboschCliqueFinder<Object, DefaultEdge> createFinder2(
        Graph<Object, DefaultEdge> graph)
    {
        return new BronKerboschCliqueFinder<>(graph);
    }

    @Override
    protected BaseBronKerboschCliqueFinder<Object, DefaultEdge> createFinder2(
        Graph<Object, DefaultEdge> graph, long timeout, TimeUnit unit)
    {
        return new BronKerboschCliqueFinder<>(graph, timeout, unit);
    }

}
