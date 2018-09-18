/*
 * (C) Copyright 2016-2018, by Barak Naveh and Contributors.
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
package org.jgrapht.alg;

import org.jgrapht.*;
import org.jgrapht.alg.TarjanLowestCommonAncestor.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.util.*;

public class TarjanLowestCommonAncestorTest
{

    @Test
    public void testBinaryTree()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("b", "d");
        g.addEdge("d", "e");

        Assert.assertEquals("b", new TarjanLowestCommonAncestor<>(g).calculate("a", "c", "e"));
        Assert.assertEquals("b", new TarjanLowestCommonAncestor<>(g).calculate("a", "b", "d"));
        Assert.assertEquals("d", new TarjanLowestCommonAncestor<>(g).calculate("a", "d", "e"));
    }

    @Test
    public void testNonBinaryTree()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);

        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("e");
        g.addVertex("f");
        g.addVertex("g");
        g.addVertex("h");
        g.addVertex("i");
        g.addVertex("j");

        g.addEdge("a", "b");
        g.addEdge("b", "c");
        g.addEdge("c", "d");
        g.addEdge("d", "e");
        g.addEdge("b", "f");
        g.addEdge("b", "g");
        g.addEdge("c", "h");
        g.addEdge("c", "i");
        g.addEdge("i", "j");

        Assert.assertEquals("b", new TarjanLowestCommonAncestor<>(g).calculate("a", "b", "h"));
        Assert.assertEquals("b", new TarjanLowestCommonAncestor<>(g).calculate("a", "j", "f"));
        Assert.assertEquals("c", new TarjanLowestCommonAncestor<>(g).calculate("a", "j", "h"));
        // now all together in one call

        LcaRequestResponse<String> bg = new LcaRequestResponse<>("b", "h");
        LcaRequestResponse<String> ed = new LcaRequestResponse<>("j", "f");
        LcaRequestResponse<String> fd = new LcaRequestResponse<>("j", "h");
        List<LcaRequestResponse<String>> list = new LinkedList<>();
        list.add(bg);
        list.add(ed);
        list.add(fd);
        List<String> result = new TarjanLowestCommonAncestor<>(g).calculate("a", list);
        // check that the mutable input parameters have changed
        Assert.assertEquals("b", bg.getLca());
        Assert.assertEquals("b", ed.getLca());
        Assert.assertEquals("c", fd.getLca());
        // check the returned result is correct
        Assert.assertEquals(Arrays.asList(new String[] { "b", "b", "c" }), result);

        // test it the other way around and starting from b
        Assert.assertEquals("b", new TarjanLowestCommonAncestor<>(g).calculate("b", "h", "b"));
    }

    @Test
    public void testOneNode()
    {
        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        g.addVertex("a");
        Assert.assertEquals("a", new TarjanLowestCommonAncestor<>(g).calculate("a", "a", "a"));
    }

}
