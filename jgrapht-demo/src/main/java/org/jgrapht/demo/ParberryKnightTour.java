/*
 * (C) Copyright 2018-2021, by Kirill Vishnyakov and Contributors.
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

package org.jgrapht.demo;

import org.jgrapht.alg.util.*;

/**
 * Implementation of {@literal <a href = "https://doi.org/10.1016/S0166-218X(96)00010-8">}
 * Parberry's algorithm{@literal </a>} for
 * {@literal <a href = "https://en.wikipedia.org/wiki/Knight%27s_tour">}closed knight's tour
 * problem{@literal </a>}.
 *
 * This algorithm was firstly introduced in "Discrete Applied Mathematics" Volume 73, Issue 3, 21
 * March 1997, Pages 251-260.
 *
 * A knight's tour is a sequence of moves of a knight on a chessboard such that the knight visits
 * every square only once. If the knight ends on a square that is one knight's move from the
 * beginning square (so that it could tour the board again immediately, following the same path),
 * the tour is closed, otherwise it is open.
 *
 * The knight's tour problem is the mathematical problem of finding a knight's tour.
 *
 * The time complexity of the algorithm is linear in the size of the board, i.e. it is equal to
 * $O(n^2)$, where $n$ is one dimension of the board.
 *
 * The Parberry's algorithm finds CLOSED knight's tour for all boards with size $n \times n$ and $n
 * \times n + 2$, where $n$ is even and $n \geq 6$.
 *
 * The knight's tour is said to be structured if it contains the following $8$ UNDIRECTED moves:
 *
 * Knight's tour on board of size $n \times m$ is called structured if it contains the following $8$
 * UNDIRECTED moves: 1). $(1, 0) \to (0, 2)$ - denoted as $1$ on the picture below. 2). $(2, 0) \to
 * (0, 1)$ - denoted as $2$ on the picture below. 3). $(n - 3, 0) \to (n - 1, 1)$ - denoted as $3$
 * on the picture below. 4). $(n - 2, 0) \to (n - 1, 2)$ - denoted as $4$ on the picture below. 5).
 * $(0, m - 3) \to (1, m - 1)$ - denoted as $5$ on the picture below. 6). $(0, m - 2) \to (2, m -
 * 1)$ - denoted as $6$ on the picture below. 7). $(n - 3, m - 1) \to (n - 1, m - 2)$ - denoted as
 * $7$ on the picture below. 8). $(n - 2, m - 1) \to (n - 1, m - 3)$ - denoted as $8$ on the picture
 * below.
 *
 * ######################################### #*12*********************************34*#
 * #2*************************************3# #1*************************************4#
 * #***************************************# #***************************************#
 * #***************************************# #***************************************#
 * #***************************************# #***************************************#
 * #***************************************# #***************************************#
 * #***************************************# #***************************************#
 * #***************************************# #5*************************************7#
 * #4*************************************6# #*54*********************************67*#
 * #########################################
 *
 * If you are confused with the formal definition of the structured knight's tour please refer to
 * the illustration on the page 3 of the paper
 * {@literal <a href = "https://doi.org/10.1016/S0166-218X(96)00010-8">} "An efficient algorithm for
 * the Knight’s tour problem" {@literal </a>} by Ian Parberry.
 *
 * Algorithm description: Split the initial board on $4$ boards as evenly as possible. Solve the
 * problem for these $4$ boards recursively. Delete the edges which contract the start and the
 * finish cell of the tour on each board, so that on each on $4$ boards closed knight's tour became
 * open knight's tour. Contract these $4$ boards by adding $4$ additional edges between the
 * quadrants.
 */

public class ParberryKnightTour
{
    /**
     * Width of the board.
     */

    private int n;

    /**
     * Height of the board.
     */

    private int m;

    /**
     * Constructor.
     * 
     * @param n width of the board.
     * @param m height of the board.
     */

    public ParberryKnightTour(int n, int m)
    {

        /*
         * Theorem 2.1 (page 3 Parberry's paper) For all even n >= 6 there exist a structured
         * knight's tour on n x n board and n x (n + 2) board. Such a tour can be constructed in
         * time O(n^2).
         */

        if (n < 6 || n % 2 != 0) {
            throw new IllegalArgumentException("n has to be greater than 5 and even!");
        }

        if (m != n + 2 && m != n) {
            throw new IllegalArgumentException(
                "n x n and n x (n + 2) are the only possible board configurations!");
        }

        this.n = n;
        this.m = m;
    }

    /**
     * Generates a closed knight's tour for a piece of the board which is being set by left-upper
     * and right-bottom cells.
     * 
     * @param start left-upper cell of the piece of the original chessboard.
     * @param end right-bottom cell of the piece of the original chessboard.
     * @return closed knight's tour on this piece of the board.
     */

    private KnightTour generateTour(Pair<Integer, Integer> start, Pair<Integer, Integer> end)
    {

        /*
         * Width and height of the board.
         */

        int nDim = end.getFirst() - start.getFirst() + 1;
        int mDim = end.getSecond() - start.getSecond() + 1;

        /*
         * Base case.
         */

        if (Math.max(nDim, mDim) <= 12) {
            return new WarnsdorffRuleKnightTourHeuristic(nDim, mDim)
                .getTour(TourType.CLOSED, true, start.getFirst(), start.getSecond());
        }

        /*
         * Start and end points of each quadrant. The following variables denoted as s1, e1, s2, e2,
         * s3, e3, s4, e4 in the picture below.
         */

        Pair<Integer, Integer> start1, end1, start2, end2, start3, end3, start4, end4;

        int k = nDim / 4;

        /*
         * n can be either of form 4k or 4k + 2. The split is being performed depending on the form
         * of n and board configuration. We want to split the board as evenly as possible. You can
         * read more about split procedure on page 3 of Parberry's paper.
         */

        int rem = nDim % 4;

        /*
         * Need to handle this case separately to achieve the most possible even split.
         */

        if (nDim + 2 == mDim && rem == 2) {
            start1 = new Pair<>(start.getFirst(), start.getSecond());
            end1 = new Pair<>(start.getFirst() + 2 * k - 1, start.getSecond() + mDim / 2 - 1);
        } else {
            start1 = new Pair<>(start.getFirst(), start.getSecond());
            end1 = new Pair<>(start.getFirst() + 2 * k - 1, start.getSecond() + 2 * k - 1);
        }
        start2 = new Pair<>(end1.getFirst() + 1, start1.getSecond());
        end2 = new Pair<>(end.getFirst(), end1.getSecond());

        start3 = new Pair<>(start.getFirst(), end1.getSecond() + 1);
        end3 = new Pair<>(end1.getFirst(), end.getSecond());

        start4 = new Pair<>(end1.getFirst() + 1, end1.getSecond() + 1);
        end4 = new Pair<>(end.getFirst(), end.getSecond());

        /*
         * ######################################### #s1*****************|s2*****************#
         * #*******************|*******************# #*******************|*******************#
         * #******TOUR 1*******|******TOUR 2*******# #*******************|*******************#
         * #*******************|*******************# #*******************|*******************#
         * #*****************e1|*****************e2# #-------------------|-------------------#
         * #s3*****************|s4*****************# #*******************|*******************#
         * #*******************|*******************# #******TOUR 3*******|******TOUR 4 ******#
         * #*******************|*******************# #*******************|*******************#
         * #*******************|*******************# #*****************e3|*****************e4#
         * #########################################
         */

        /*
         * Recursively solving problem for small quadrants.
         */

        KnightTour tour1 = generateTour(start1, end1);
        KnightTour tour2 = generateTour(start2, end2);
        KnightTour tour3 = generateTour(start3, end3);
        KnightTour tour4 = generateTour(start4, end4);

        /*
         * Removing edges A, B, C and D.
         * 
         * ######################################### #*******************|*******************#
         * #*******************|*******************# #*******************|*******************#
         * #******TOUR 1*******|******TOUR 2*******# #*******************|*******************#
         * #*******************|*B*****************# #******************A|*******************#
         * #****************A**|B******************# #-------------------|-------------------#
         * #******************D|**C****************# #*******************|C******************#
         * #*****************D*|*******************# #******TOUR 3*******|******TOUR 4*******#
         * #*******************|*******************# #*******************|*******************#
         * #*******************|*******************# #*******************|*******************#
         * #########################################
         */

        /*
         * Adding edges E, F, G, H to contract the quadrants.
         * 
         * ######################################### #*******************|*******************#
         * #*******************|*******************# #*******************|*******************#
         * #******TOUR 1*******|******TOUR 2*******# #*******************|*******************#
         * #*******************|*F*****************# #******************F|*******************#
         * #****************E**|G******************# #-------------------|-------------------#
         * #******************E|**G****************# #*******************|H******************#
         * #*****************H*|*******************# #******TOUR 3*******|******TOUR 4*******#
         * #*******************|*******************# #*******************|*******************#
         * #*******************|*******************# #*******************|*******************#
         * #########################################
         */

        /*
         * Relation between nodes in structured array and endpoints of the edges to be
         * deleted/added. Note that you don't know the direction of the edges A, B, C, D, so you
         * have to check both options.
         * 
         * ######################################### #**0***************2|**0***************2#
         * #1******************|1******************# #*****************3*|*****************3*#
         * #******TOUR 1*******|******TOUR 2*******# #*******************|*******************#
         * #*4*****************|*4*****************# #******************6|******************6#
         * #5***************7**|5***************7**# #-------------------|-------------------#
         * #**0***************2|**0***************2# #1***************3**|1******************#
         * #*******************|*****************3*# #******TOUR 3*******|******TOUR 4*******#
         * #*******************|*******************# #*4*****************|*4*****************#
         * #******************6|******************6# #5***************7**|5***************7**#
         * ######################################### _________________________________
         * 
         * A.start = tour1.forStructured[6]; A.end = tour1.forStructured[7];
         * 
         * or
         * 
         * A.end = tour1.forStructured[6]; A.start = tour1.forStructured[7];
         * __________________________________
         * 
         * B.start = tour2.forStructured[4]; B.end = tour2.forStructured[5];
         * 
         * or
         * 
         * B.end = tour2.forStructured[4]; B.start = tour2.forStructured[5];
         * __________________________________
         * 
         * C.start = tour4.forStructured[0]; C.end = tour4.forStructured[1];
         * 
         * or
         * 
         * C.end = tour4.forStructured[0]; C.start = tour4.forStructured[1];
         * __________________________________
         * 
         * D.start = tour2.forStructured[2]; D.end = tour2.forStructured[3];
         * 
         * or
         * 
         * D.end = tour2.forStructured[2]; D.start = tour2.forStructured[3];
         * __________________________________
         */

        /*
         * Deleting and simultaneously contracting.
         */

        if (tour1.getStructured().get(7).getNext() == tour1.getStructured().get(6)) {
            tour1.getStructured().get(7).setNext(tour3.getStructured().get(2));
            tour1.getStructured().get(6).setPrev(tour2.getStructured().get(4));
        } else {
            tour1.getStructured().get(7).setPrev(tour3.getStructured().get(2));
            tour1.getStructured().get(6).setNext(tour2.getStructured().get(4));
        }

        if (tour3.getStructured().get(2).getPrev() == tour3.getStructured().get(3)) {
            tour3.getStructured().get(2).setPrev(tour1.getStructured().get(7));
            tour3.getStructured().get(3).setNext(tour4.getStructured().get(1));
        } else {
            tour3.getStructured().get(2).setNext(tour1.getStructured().get(7));
            tour3.getStructured().get(3).setPrev(tour4.getStructured().get(1));
        }

        if (tour4.getStructured().get(1).getPrev() == tour4.getStructured().get(0)) {
            tour4.getStructured().get(1).setPrev(tour3.getStructured().get(3));
            tour4.getStructured().get(0).setNext(tour2.getStructured().get(5));
        } else {
            tour4.getStructured().get(1).setNext(tour3.getStructured().get(3));
            tour4.getStructured().get(0).setPrev(tour2.getStructured().get(5));
        }

        if (tour2.getStructured().get(5).getPrev() == tour2.getStructured().get(4)) {
            tour2.getStructured().get(5).setPrev(tour4.getStructured().get(0));
            tour2.getStructured().get(4).setNext(tour1.getStructured().get(6));
        } else {
            tour2.getStructured().get(5).setNext(tour4.getStructured().get(0));
            tour2.getStructured().get(4).setPrev(tour1.getStructured().get(6));
        }

        /*
         * Update the start node after you've contracted all quadrants.
         */

        tour1.getList().setStartNode(tour3.getStructured().get(2));

        /*
         * Update structured pointers. Note that we do not need to update the first two nodes, since
         * they are already set correctly.
         */

        tour1.getStructured().set(2, tour2.getStructured().get(2));
        tour1.getStructured().set(3, tour2.getStructured().get(3));

        tour1.getStructured().set(4, tour3.getStructured().get(4));
        tour1.getStructured().set(5, tour3.getStructured().get(5));

        tour1.getStructured().set(6, tour4.getStructured().get(6));
        tour1.getStructured().set(7, tour4.getStructured().get(7));

        /*
         * Update size of the list.
         */

        tour1
            .getList().setSize(
                tour1.getList().getSize() + tour2.getList().getSize() + tour3.getList().getSize()
                    + tour4.getList().getSize());

        return tour1;
    }

    /**
     * Returns a closed knight's tour.
     * 
     * @return closed knight's tour.
     */

    public KnightTour getTour()
    {
        return generateTour(new Pair<>(0, 0), new Pair<>(n - 1, m - 1));
    }
}
