/*
 * (C) Copyright 2019-2019, by Dimitrios Michail and Contributors.
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
package org.jgrapht.opt.graph.sparse2;

import org.junit.Test;

/**
 * Tests
 * 
 * @author Dimitrios Michail
 */
public class SparseGraphTest
{

    @Test
    public void testUndirected()
    {
        org.jgrapht.opt.graph.sparse.SparseGraphTest
            .testUndirected((vc, edges) -> new SparseUndirectedGraph(vc, edges));
    }

    @Test
    public void testUndirectedWithLoops()
    {
        org.jgrapht.opt.graph.sparse.SparseGraphTest
            .testUndirectedWithLoops((vc, edges) -> new SparseUndirectedGraph(vc, edges));
    }

    @Test
    public void testUndirectedWeighted()
    {
        org.jgrapht.opt.graph.sparse.SparseGraphTest
            .testUndirectedWeighted((vc, edges) -> new SparseUndirectedWeightedGraph(vc, edges));
    }

    @Test
    public void testDirected()
    {
        org.jgrapht.opt.graph.sparse.SparseGraphTest
            .testDirected((vc, edges) -> new SparseDirectedGraph(vc, edges));
    }

    @Test
    public void testDirectedWeighted()
    {
        org.jgrapht.opt.graph.sparse.SparseGraphTest
            .testDirectedWeighted((vc, edges) -> new SparseDirectedWeightedGraph(vc, edges));
    }

}
