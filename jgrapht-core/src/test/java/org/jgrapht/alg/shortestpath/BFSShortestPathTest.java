/*
 * (C) Copyright 2018-2019, by Karri Sai Satish Kumar Reddy and Contributors.
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

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.*;
import org.jgrapht.graph.*;

public class BFSShortestPathTest
    extends
    UnWeightedShortestPathTestCase
{

    @Override
    protected List<DefaultEdge> findPathBetween(
        Graph<String, DefaultEdge> g, String src, String dest)
    {
        SingleSourcePaths<String, DefaultEdge> tree;
        tree = new BFSShortestPath<>(g).getPaths(src);
        GraphPath<String, DefaultEdge> path = tree.getPath(dest);
        if(path!=null)
            return tree.getPath(dest).getEdgeList();
        else 
            return new LinkedList<DefaultEdge> ();
    }

    @Override
    protected SingleSourcePaths<String, DefaultEdge> getPathsFrom(
        Graph<String, DefaultEdge> g, String src)
    {
        
        return new BFSShortestPath<>(g).getPaths(src);
    }

    @Override
    protected List<DefaultEdge> findPathTo(SingleSourcePaths<String, DefaultEdge> tree, String dest)
    {
        GraphPath<String, DefaultEdge> path = tree.getPath(dest);
        if(path!=null)
            return tree.getPath(dest).getEdgeList();
        else 
            return new LinkedList<DefaultEdge> ();
    }
    

}



