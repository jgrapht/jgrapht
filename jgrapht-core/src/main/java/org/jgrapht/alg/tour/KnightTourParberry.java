/*
 * (C) Copyright 2018-2018, by Kirill Vishnyakov and Contributors.
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

package org.jgrapht.alg.tour;

/*
  Implementation of
  {@literal <a href = "https://ac.els-cdn.com/S0166218X96000108/1-s2.0-S0166218X96000108-main.pdf?_tid=9f43a8d3-4b9a-4577-b72c-42a12ec5d33a&acdnat=1523483883_50f911f2faf34ed6fa16811cbd068c42">}
  Parberry's algorithm{@literal </a>} for {@literal <a href = "https://en.wikipedia.org/wiki/Knight%27s_tour">}closed knight's tour problem{</a>}.

  A knight's tour is a sequence of moves of a knight on a chessboard such that the knight visits every square only once.
  If the knight ends on a square that is one knight's move from the beginning square
  (so that it could tour the board again immediately, following the same path), the tour is closed, otherwise it is open.

  The knight's tour problem is the mathematical problem of finding a knight's tour.

  The time complexity of the algorithm is linear in the size of the board, i.e. it is equal to $O(n^2)$, where $n$ is one dimension of the board.

  The Parberry's algorithm finds CLOSED knight's tour for all boards with size $n \times n$ and $n \times n + 2$, where  $n$ is even and $n \geq 6$.

  The knight's tour is said to be structured if it contains the following UNDIRECTED moves:

  Knight's tour on board of size $n \times m$ is called structured if it contains the following $8$ UNDIRECTED moves:
  1). $(1, 0) \to (0, 2)$.
  2). $(2, 0) \to (0, 1)$.
  3). $(n - 3, 0) \to (n - 1, 1)$.
  4). $(n - 2, 0) \to (n - 1, 2)$.
  5). $(0, m - 3) \to (1, m - 1)$.
  6). $(0, m - 2) \to (2, m - 1)$.
  7). $(n - 3, m - 1) \to (n - 1, m - 2)$.
  8). $(n - 2, m - 1) \to (n - 1, m - 3)$.

  #########################################
  #*12*********************************34*#
  #2*************************************3#
  #1*************************************4#
  #***************************************#
  #***************************************#
  #***************************************#
  #***************************************#
  #***************************************#
  #***************************************#
  #***************************************#
  #***************************************#
  #***************************************#
  #***************************************#
  #***************************************#
  #5*************************************7#
  #4*************************************6#
  #*54*********************************67*#
  #########################################

   If you are confused with the formal definition of the structured knight's tour
   please refer to illustration on the page 3 of the paper
   {@literal <a href = "https://ac.els-cdn.com/S0166218X96000108/1-s2.0-S0166218X96000108-main.pdf?_tid=9f43a8d3-4b9a-4577-b72c-42a12ec5d33a&acdnat=1523483883_50f911f2faf34ed6fa16811cbd068c42">}
   "An efficient algorithm for the Knight’s tour problem"
   {@literal </a>} by Ian Parberry.

   Algorithm description:
   Split the initial board on $4$ boards as evenly as possible. Solve the problem for these $4$ boards recursively.
   Delete the edges which contract the start and the finish cell of the tour on each board, so that on each on $4$ boards
   closed knight's tour became open knight's tour. Contract these $4$ boards by adding $4$ additional edges between them.
 */

/**
 * Class that implements Parberry's algorithm for knight's tour problem.
 */

public class KnightTourParberry
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
     * @param n width of the board.
     * @param m height of the board.
     */

    public KnightTourParberry(int n, int m) {

        /*
         \textbf{Theorem 2.1}(page 3 Parberry's paper)
         For all even $n \geq 6$ there exist
         a structured knight's tour on $n \times n$ board and $n \times (n + 2)$ board.
         Such a tour can be constructed in time $O(n^2)$.
         */

        if (n < 3 || m < 3 || n % 2 != 0) {
            throw new UnsupportedOperationException("You set incorrect configuration of the board!");
        }
        this.n = n;
        if (m != n + 2 && m != n) {
            throw new UnsupportedOperationException("n x n and n x (n + 2) are the only possible board configurations!");
        }
        this.m = m;
    }

    private boolean checkExistence() {
        return n % 2 == 0;
    }

    /**
     * Generates a closed knight's tour for a piece of the board which is being set by left-upper and right-bottom cells.
     * @param start left-upper cell of the piece of the original chessboard.
     * @param end right-bottom cell of the piece of the original chessboard.
     * @return closed knight's tour on this piece of the board.
     */

    private KnightTourContainer generateTour(ChessBoardCell<Integer, Integer> start, ChessBoardCell<Integer, Integer> end) {

        /*
         Width and height of the board.
         */

        int nDim = end.first - start.first + 1;
        int mDim = end.second - start.second + 1;

        /*
         Base case.
         */

        if (Math.max(nDim, mDim) <= 12) {
            return new WarnsdorffRule(nDim, mDim).generateTour(TourType.CLOSED, true, start.first, start.second);
        }

        /*
         Start and end points of each quadrant.
         The following variables denoted as s1, e1, s2, e2, s3, e3, s4, e4 in the picture below.
         */

        ChessBoardCell<Integer, Integer> start1, end1, start2, end2, start3, end3, start4, end4;

        int k = nDim / 4;

        /*
         $n$ can be either of form $4k$ or $4k + 2$.
         The split is being performed depending on the form of $n$ and board configuration.
         We want to split the board as evenly as possible.
         You can read more about split procedure on page 3 of Parberry's paper.
         */

        int rem = nDim % 4;

        /*
         Need to handle this case separately to achieve the most possible even split.
         */

        if (nDim + 2 == mDim && rem == 2) {
            start1 = new ChessBoardCell<>(start.first, start.second);
            end1 = new ChessBoardCell<>(start.first + 2 * k - 1, start.second + mDim / 2 - 1);
        }
        else {
            start1 = new ChessBoardCell<>(start.first, start.second);
            end1 = new ChessBoardCell<>(start.first + 2 * k - 1, start.second + 2 * k - 1);
        }
        start2 = new ChessBoardCell<>(end1.first + 1, start1.second);
        end2 = new ChessBoardCell<>(end.first, end1.second);

        start3 = new ChessBoardCell<>(start.first, end1.second + 1);
        end3 = new ChessBoardCell<>(end1.first, end.second);

        start4 = new ChessBoardCell<>(end1.first + 1, end1.second + 1);
        end4 = new ChessBoardCell<>(end.first, end.second);

        /*
         #########################################
         #s1*****************|s2*****************#
         #*******************|*******************#
         #*******************|*******************#
         #******TOUR 1*******|******TOUR 2*******#
         #*******************|*******************#
         #*******************|*******************#
         #*******************|*******************#
         #*****************e1|*****************e2#
         #-------------------|-------------------#
         #s3*****************|s4*****************#
         #*******************|*******************#
         #*******************|*******************#
         #******TOUR 3*******|******TOUR 4 ******#
         #*******************|*******************#
         #*******************|*******************#
         #*******************|*******************#
         #*****************e3|*****************e4#
         #########################################
         */

        /*
         Recursively solving problem for small quadrants.
         */

        KnightTourContainer tour1 = generateTour(start1, end1);
        KnightTourContainer tour2 = generateTour(start2, end2);
        KnightTourContainer tour3 = generateTour(start3, end3);
        KnightTourContainer tour4 = generateTour(start4, end4);

        /*
         Removing edges A, B, C and D.

         #########################################
         #*******************|*******************#
         #*******************|*******************#
         #*******************|*******************#
         #******TOUR 1*******|******TOUR 2*******#
         #*******************|*******************#
         #*******************|*B*****************#
         #******************A|*******************#
         #****************A**|B******************#
         #-------------------|-------------------#
         #******************D|**C****************#
         #*******************|C******************#
         #*****************D*|*******************#
         #******TOUR 3*******|******TOUR 4*******#
         #*******************|*******************#
         #*******************|*******************#
         #*******************|*******************#
         #*******************|*******************#
         #########################################
         */

        /*
         Adding edges E, F, G, H to contract the quadrants.

         #########################################
         #*******************|*******************#
         #*******************|*******************#
         #*******************|*******************#
         #******TOUR 1*******|******TOUR 2*******#
         #*******************|*******************#
         #*******************|*F*****************#
         #******************F|*******************#
         #****************E**|G******************#
         #-------------------|-------------------#
         #******************E|**G****************#
         #*******************|H******************#
         #*****************H*|*******************#
         #******TOUR 3*******|******TOUR 4*******#
         #*******************|*******************#
         #*******************|*******************#
         #*******************|*******************#
         #*******************|*******************#
         #########################################
         */

        /*
         Relation between nodes in structured array and endpoints of the edges to be deleted/added.
         Note that you don't know the direction of the edges A, B, C, D, so you have to check both options.

         #########################################
         #**0***************2|**0***************2#
         #1******************|1******************#
         #*****************3*|*****************3*#
         #******TOUR 1*******|******TOUR 2*******#
         #*******************|*******************#
         #*4*****************|*4*****************#
         #******************6|******************6#
         #5***************7**|5***************7**#
         #-------------------|-------------------#
         #**0***************2|**0***************2#
         #1***************3**|1******************#
         #*****************H*|*****************3*#
         #******TOUR 3*******|******TOUR 4*******#
         #*******************|*******************#
         #*4*****************|*4*****************#
         #******************6|******************6#
         #5***************7**|5***************7**#
         #########################################
         _________________________________

         A.start = tour1.forStructured[6];
         A.end = tour1.forStructured[7];

         or

         A.end = tour1.forStructured[6];
         A.start = tour1.forStructured[7];
         __________________________________

         B.start = tour2.forStructured[4];
         B.end = tour2.forStructured[5];

         or

         B.end = tour2.forStructured[4];
         B.start = tour2.forStructured[5];
         __________________________________

         C.start = tour4.forStructured[0];
         C.end = tour4.forStructured[1];

         or

         C.end = tour4.forStructured[0];
         C.start = tour4.forStructured[1];
         __________________________________

         D.start = tour2.forStructured[2];
         D.end = tour2.forStructured[3];

         or

         D.end = tour2.forStructured[2];
         D.start = tour2.forStructured[3];
         __________________________________
         */

        /*
         Deleting and simultaneously contracting.
         */

        if (tour1.getStructured().get(7).getNext() == tour1.getStructured().get(6)) {
            tour1.getStructured().get(7).setNext(tour3.getStructured().get(2));
            tour1.getStructured().get(6).setPrev(tour2.getStructured().get(4));
        }
        else {
            tour1.getStructured().get(7).setPrev(tour3.getStructured().get(2));
            tour1.getStructured().get(6).setNext(tour2.getStructured().get(4));
        }

        if (tour3.getStructured().get(2).getPrev() == tour3.getStructured().get(3)) {
            tour3.getStructured().get(2).setPrev(tour1.getStructured().get(7));
            tour3.getStructured().get(3).setNext(tour4.getStructured().get(1));
        }
        else {
            tour3.getStructured().get(2).setNext(tour1.getStructured().get(7));
            tour3.getStructured().get(3).setPrev(tour4.getStructured().get(1));
        }

        if (tour4.getStructured().get(1).getPrev() == tour4.getStructured().get(0)) {
            tour4.getStructured().get(1).setPrev(tour3.getStructured().get(3));
            tour4.getStructured().get(0).setNext(tour2.getStructured().get(5));
        }
        else {
            tour4.getStructured().get(1).setNext(tour3.getStructured().get(3));
            tour4.getStructured().get(0).setPrev(tour2.getStructured().get(5));
        }

        if (tour2.getStructured().get(5).getPrev() == tour2.getStructured().get(4)) {
            tour2.getStructured().get(5).setPrev(tour4.getStructured().get(0));
            tour2.getStructured().get(4).setNext(tour1.getStructured().get(6));
        }
        else {
            tour2.getStructured().get(5).setNext(tour4.getStructured().get(0));
            tour2.getStructured().get(4).setPrev(tour1.getStructured().get(6));
        }

        /*
         Update the start node after you've contracted all quadrants.
         */

        tour1.getList().setStartNode(tour3.getStructured().get(2));

        /*
         Update structured pointers.
         Note that we do not need to update the first two nodes, since they are already set correctly.
         */

        tour1.getStructured().set(2, tour2.getStructured().get(2));
        tour1.getStructured().set(3, tour2.getStructured().get(3));

        tour1.getStructured().set(4, tour3.getStructured().get(4));
        tour1.getStructured().set(5, tour3.getStructured().get(5));

        tour1.getStructured().set(6, tour4.getStructured().get(6));
        tour1.getStructured().set(7, tour4.getStructured().get(7));

        /*
         Update size of the list.
         */

        tour1.getList().setSize(tour1.getList().getSize() + tour2.getList().getSize() + tour3.getList().getSize() + tour4.getList().getSize());

        return tour1;
    }

    /**
     * Returns a closed knight's tour.
     * @return closed knight's tour.
     */

    public KnightTourContainer buildTour() {
        if (!checkExistence()) {
            throw new UnsupportedOperationException("n must be even!");
        }

        ChessBoardCell<Integer, Integer> cell1 = new ChessBoardCell<>(0, 0);
        ChessBoardCell<Integer, Integer> cell2 = new ChessBoardCell<>(n - 1, m - 1);

        return generateTour(cell1, cell2);
    }
}
