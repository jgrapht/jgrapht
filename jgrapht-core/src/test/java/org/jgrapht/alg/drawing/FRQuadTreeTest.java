/*
 * (C) Copyright 2018-2021, by Dimitrios Michail and Contributors.
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
package org.jgrapht.alg.drawing;

import org.jgrapht.alg.drawing.FRQuadTree.*;
import org.jgrapht.alg.drawing.model.*;
import org.junit.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test {@link FRQuadTree}.
 * 
 * @author Dimitrios Michail
 */
public class FRQuadTreeTest
{

    @Test
    public void testQuadTree()
    {
        double width = 100;
        double height = 100;
        int points = 10000;
        Box2D region = Box2D.of(0, 0, width, height);
        FRQuadTree tree = new FRQuadTree(region);

        Random rng = new Random(17);

        for (int i = 0; i < points; i++) {
            Point2D p = Point2D.of(rng.nextDouble() * width, rng.nextDouble() * height);
            tree.insert(p);
        }

        Deque<Node> queue = new ArrayDeque<>();
        Node root = tree.getRoot();
        assertEquals(root.getNumberOfPoints(), points);
        queue.addLast(root);

        while (!queue.isEmpty()) {
            Node cur = queue.poll();
            if (cur.hasPoints()) {
                assertTrue(Points.equals(cur.getCentroid(), centroid(cur.getPoints())));
            }
            int totalPoints = cur.getNumberOfPoints();

            if (!cur.isLeaf()) {
                int childrenPoints = 0;
                for (Node c : cur.getChildren()) {
                    queue.addLast(c);
                    childrenPoints += c.getNumberOfPoints();
                }
                assertEquals(totalPoints, childrenPoints);
            }
        }

    }

    private Point2D centroid(List<Point2D> points)
    {
        double x = 0d;
        double y = 0d;
        for (Point2D p : points) {
            x += p.getX();
            y += p.getY();
        }
        int n = points.size();
        return Point2D.of(x / n, y / n);
    }

}
