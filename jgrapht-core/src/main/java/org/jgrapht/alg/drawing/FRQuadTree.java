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

import org.jgrapht.alg.drawing.model.*;
import org.jgrapht.alg.util.*;

import java.util.*;

/**
 * A simple <a href="https://en.wikipedia.org/wiki/Quadtree">QuadTree</a> for indexing during force
 * calculations in the Fruchterman and Reingold Force-Directed Placement Algorithm.
 *
 * <p>
 * See the following paper for the definition of a QuadTree.
 * <ul>
 * <li>Raphael Finkel and J.L. Bentley. Quad Trees: A Data Structure for Retrieval on Composite
 * Keys. Acta Informatica, 4(1):1--9, 1974.</li>
 * </ul>
 * 
 * <p>
 * The tree supports adding points one by one and maintains the centroid and total number of points
 * on each tree node.
 * 
 * @author Dimitrios Michail
 */
class FRQuadTree
{
    private static final int NW = 0;
    private static final int NE = 1;
    private static final int SW = 2;
    private static final int SE = 3;

    private Node root;

    /**
     * Create a new tree for a certain area.
     * 
     * @param box the area
     */
    public FRQuadTree(Box2D box)
    {
        this.root = new Node(box);
    }

    /**
     * Insert a new point.
     * 
     * @param p the new point
     */
    public void insert(Point2D p)
    {
        Node cur = root;
        while (true) {
            if (cur.isLeaf()) {
                if (cur.points.size() == 0) {
                    cur.points.add(p);
                    return;
                }

                // split
                Box2D rect = cur.getBox();
                Pair<Box2D, Box2D> xsplit = Boxes.splitAlongXAxis(rect);
                Pair<Box2D, Box2D> west = Boxes.splitAlongYAxis(xsplit.getFirst());
                Pair<Box2D, Box2D> east = Boxes.splitAlongYAxis(xsplit.getSecond());

                // create 4 children
                cur.children = new Node[4];
                cur.children[NW] = new Node(west.getSecond());
                cur.children[NE] = new Node(east.getSecond());
                cur.children[SW] = new Node(west.getFirst());
                cur.children[SE] = new Node(east.getFirst());

                // distribute old points and compute centroid
                double centroidX = 0, centroidY = 0;
                for (Point2D point : cur.points) {
                    if (Boxes.containsPoint(cur.children[NW].getBox(), point)) {
                        cur.children[NW].points.add(point);
                    } else if (Boxes.containsPoint(cur.children[NE].getBox(), point)) {
                        cur.children[NE].points.add(point);
                    } else if (Boxes.containsPoint(cur.children[SW].getBox(), point)) {
                        cur.children[SW].points.add(point);
                    } else if (Boxes.containsPoint(cur.children[SE].getBox(), point)) {
                        cur.children[SE].points.add(point);
                    }
                    centroidX += point.getX();
                    centroidY += point.getY();
                }
                cur.totalPoints = cur.points.size();
                cur.centroid = Point2D.of(centroidX / cur.totalPoints, centroidY / cur.totalPoints);

                // change from leaf to internal node
                cur.points = null;
            }

            // here we are not a leaf

            // count new point and update centroid
            cur.totalPoints++;
            cur.centroid = Point2D
                .of(
                    (cur.centroid.getX() * (cur.totalPoints - 1) + p.getX()) / cur.totalPoints,
                    (cur.centroid.getY() * (cur.totalPoints - 1) + p.getY()) / cur.totalPoints);

            // non-leaf
            if (Boxes.containsPoint(cur.children[NW].getBox(), p)) {
                cur = cur.children[NW];
            } else if (Boxes.containsPoint(cur.children[NE].getBox(), p)) {
                cur = cur.children[NE];
            } else if (Boxes.containsPoint(cur.children[SW].getBox(), p)) {
                cur = cur.children[SW];
            } else if (Boxes.containsPoint(cur.children[SE].getBox(), p)) {
                cur = cur.children[SE];
            } else {
                throw new IllegalArgumentException();
            }

        }
    }

    /**
     * Get the root node of the tree.
     * 
     * @return the root
     */
    public Node getRoot()
    {
        return root;
    }

    /**
     * The Quad-Tree node.
     * 
     * @author Dimitrios Michail
     */
    public class Node
    {
        // node region
        Box2D box;

        // internal node
        int totalPoints;
        Point2D centroid;
        Node[] children;

        // leaf node
        List<Point2D> points;

        /**
         * Create a new node for a given area
         * 
         * @param box the area
         */
        public Node(Box2D box)
        {
            this.box = Objects.requireNonNull(box);
            this.points = new ArrayList<>();
        }

        /**
         * Check if a node is a leaf.
         * 
         * @return true if leaf, false otherwise
         */
        public boolean isLeaf()
        {
            return points != null;
        }

        /**
         * Get a list of all points contained in this node.
         * 
         * @return a list of points
         */
        public List<Point2D> getPoints()
        {
            if (points != null) {
                return points;
            } else {
                List<Point2D> result = new ArrayList<>();
                getChildren().forEach(node -> {
                    result.addAll(node.getPoints());
                });
                return result;
            }
        }

        /**
         * Check if the node contains any points.
         * 
         * @return true if the node contains points, false otherwise
         */
        public boolean hasPoints()
        {
            if (points != null) {
                return points.size() != 0;
            } else {
                return totalPoints != 0;
            }
        }

        /**
         * Get the area represented by this node.
         * 
         * @return the area of the node
         */
        public Box2D getBox()
        {
            return box;
        }

        /**
         * Get the total number of points under this node.
         * 
         * @return the total number of points
         */
        public int getNumberOfPoints()
        {
            if (points != null) {
                return points.size();
            } else {
                return totalPoints;
            }
        }

        /**
         * Get the centroid of all points contained in this node.
         * 
         * @return the centroid of all points contained in this node
         */
        public Point2D getCentroid()
        {
            if (points != null) {
                int numPoints = points.size();
                if (numPoints == 0) {
                    throw new IllegalArgumentException("No points");
                }
                double x = 0, y = 0;
                for (Point2D p : points) {
                    x += p.getX();
                    y += p.getY();
                }
                return Point2D.of(x / numPoints, y / numPoints);
            } else {
                return centroid;
            }
        }

        /**
         * Get the children of this node as a list.
         * 
         * @return a list containing the children of this node
         */
        public List<Node> getChildren()
        {
            if (children == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(children);
        }

    }

}
