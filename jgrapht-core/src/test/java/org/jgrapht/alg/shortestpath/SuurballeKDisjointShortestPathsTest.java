/*
 * (C) Copyright 2018-2021, by Assaf Mizrachi and Contributors.
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
package org.jgrapht.alg.shortestpath;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.*;

/**
 * 
 * Tests for the {@link SuurballeKDisjointShortestPaths} class.
 * 
 * @author Assaf Mizrachi
 */
public class SuurballeKDisjointShortestPathsTest
    extends
    KDisjointShortestPathsTestCase
{

    @Override
    protected <V, E> KShortestPathAlgorithm<V, E> getKShortestPathAlgorithm(Graph<V, E> graph)
    {
        return new SuurballeKDisjointShortestPaths<>(graph);
    }
}
