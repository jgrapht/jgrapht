/*
 * (C) Copyright 2003-2018, by John V Sichi and Contributors.
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
package org.jgrapht.traverse;

import org.jgrapht.*;
import org.jgrapht.graph.*;

/**
 * Tests for the ignoreDirection parameter to XXFirstIterator.
 *
 * <p>
 * NOTE: This test uses hard-coded expected ordering which isn't really guaranteed by the
 * specification of the algorithm. This could cause spurious failures if the traversal
 * implementation changes.
 * </p>
 *
 * @author John V. Sichi
 */
public class IgnoreDirectionTest
    extends
    AbstractGraphIteratorTest
{
    // ~ Methods ----------------------------------------------------------------

    @Override
    String getExpectedStr1()
    {
        return "4,9,7,8,2,1,3,6,5";
    }

    @Override
    String getExpectedStr2()
    {
        return "4,9,7,8,2,1,3,6,5,orphan";
    }

    @Override
    String getExpectedFinishString()
    {
        return "5:6:3:1:2:8:7:9:4:orphan:";
    }

    @Override
    AbstractGraphIterator<String, DefaultWeightedEdge> createIterator(
        Graph<String, DefaultWeightedEdge> g, String vertex)
    {
        // ignore the passed in vertex and always start from v4, since that's
        // the only vertex without out-edges
        Graph<String, DefaultWeightedEdge> undirectedView = new AsUndirectedGraph<>(g);
        AbstractGraphIterator<String, DefaultWeightedEdge> i =
            new DepthFirstIterator<>(undirectedView, "4");
        i.setCrossComponentTraversal(true);

        return i;
    }
}
