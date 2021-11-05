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

import java.util.*;

/**
 * Enum type that represents two knight's tour types: closed and open.
 */

enum TourType
{
    CLOSED,
    OPEN
}

/**
 * Class that represents container for knight's tour.
 */

class KnightTour
{

    /**
     * Implementation of a doubly linked list data structure that is being used for storing a tour.
     *
     * @param <E> type of a value storing in a node.
     */

    class DoublyLinkedList<E>
    {

        /**
         * Pointer to the head of the list.
         */

        private Node<E> head;

        /**
         * Pointer to the tail of the list.
         */

        private Node<E> tail;

        /**
         * Pointer to the start node. Start node is the node from which we start any traversal
         * operation on the list.
         */

        private Node<E> startNode;

        /**
         * Size of the list.
         */

        private int size;

        public DoublyLinkedList()
        {
            head = null;
            tail = null;
            startNode = null;
            size = 0;
        }

        public int getSize()
        {
            return size;
        }

        public boolean isEmpty()
        {
            return head == null;
        }

        /**
         * Adds element to the end of the list.
         * 
         * @param element we want to add.
         */

        public void add(E element)
        {
            Node<E> node = new Node<>(element);
            size++;
            if (isEmpty()) {
                node.next = null;
                node.prev = null;
                head = node;
                tail = node;
                return;
            }
            tail.next = node;
            node.prev = tail;
            node.next = null;
            tail = node;
        }

        /**
         * Removes tail element.
         */

        public void remove()
        {
            if (isEmpty()) {
                throw new IndexOutOfBoundsException("The list is empty!");
            }
            size--;
            if (tail.prev == null) {
                head = null;
                tail = null;
                return;
            }
            tail = tail.prev;
            tail.next = null;
        }

        public Node<E> getHead()
        {
            return head;
        }

        public Node<E> getTail()
        {
            return tail;
        }

        public void clear()
        {
            head = null;
            tail = null;
            size = 0;
        }

        public void setStartNode(Node<E> startNode)
        {
            this.startNode = startNode;
        }

        public Node<E> getStartNode()
        {
            return startNode;
        }

        public void setSize(int i)
        {
            size = i;
        }
    }

    /**
     * Static class that represents a node.
     * 
     * @param <E> type of the value stored in the node.
     *
     */

    static class Node<E>
    {

        /**
         * Pointer to the next node.
         */

        private Node<E> next;

        /**
         * Pointer to the previous node.
         */

        private Node<E> prev;

        /**
         * Value that is being stored in the node.
         */

        private E value;

        /**
         * Boolean flag that is being used in traversal function, such as toList. True if the node
         * was visited, otherwise false.
         */

        private boolean visited = false;

        public Node(E value)
        {
            this.value = value;
        }

        public Node()
        {
        }

        public boolean isVisited()
        {
            return !visited;
        }

        public void setVisited(boolean visited)
        {
            this.visited = visited;
        }

        public E getValue()
        {
            return value;
        }

        public Node<E> getNext()
        {
            return next;
        }

        public Node<E> getPrev()
        {
            return prev;
        }

        public void setPrev(Node<E> prev)
        {
            this.prev = prev;
        }

        public void setNext(Node<E> next)
        {
            this.next = next;
        }
    }

    /**
     * Doubly linked list that stores nodes in order of their appearance in the knight's tour.
     */

    private final DoublyLinkedList<Pair<Integer, Integer>> list;

    /*
     * Let's call each of the following 8 cells structured:
     * 
     * (enumeration starts with 0 to make the relation between cells and indices in structured array
     * more clear)
     * 
     * 0). (2, 0); 1). (0, 1); 2). (n - 1, 0); 3). (n - 2, 2); 4). (1, m - 3); 5). (0, m - 1); 6).
     * (n - 1, m - 2); 7). (n - 3, m - 1);
     * 
     * ######################################### #**0***********************************2#
     * #1**************************************# #*************************************3*#
     * #***************************************# #***************************************#
     * #***************************************# #***************************************#
     * #***************************************# #***************************************#
     * #***************************************# #***************************************#
     * #***************************************# #***************************************#
     * #***************************************# #*4*************************************#
     * #**************************************6# #5***********************************7**#
     * #########################################
     * 
     * Structured cells are needed in the the merging procedure in the Parberry's algorithm.
     */

    /**
     * ArrayList that stores pointers on the structured cells.
     */

    private final ArrayList<KnightTour.Node<Pair<Integer, Integer>>> structured;

    /**
     * Used in toList function.
     */

    private List<Pair<Integer, Integer>> arrayList;

    /**
     * Constructor of knight's tour container.
     */

    public KnightTour()
    {
        structured = new ArrayList<>(Collections.nCopies(8, new KnightTour.Node<>()));
        list = new DoublyLinkedList<>();
        arrayList = null;
    }

    /**
     * Converts knight's tour represented as DoublyLinkedList to ArrayList.
     * 
     * @return ArrayList that contains knight's tour.
     */

    public List<Pair<Integer, Integer>> toList()
    {
        if (arrayList != null) {
            return arrayList;
        }

        Node<Pair<Integer, Integer>> startNode = list.getStartNode();
        startNode.setVisited(true);
        arrayList = new ArrayList<>();
        arrayList.add(startNode.getValue());

        /*
         * Traverse of the list.
         */

        while (startNode.getNext().isVisited() || startNode.getPrev().isVisited()) {
            if (startNode.getNext().isVisited())
                startNode = startNode.getNext();
            else {
                startNode = startNode.getPrev();
            }
            arrayList.add(startNode.getValue());
            startNode.setVisited(true);
        }

        return arrayList;
    }

    public DoublyLinkedList<Pair<Integer, Integer>> getList()
    {
        return list;
    }

    public ArrayList<Node<Pair<Integer, Integer>>> getStructured()
    {
        return structured;
    }
}

/**
 * Implementation of {@literal <a href =
 *  "https://en.wikipedia.org/wiki/Knight%27s_tour#Warnsdorf's_rule">}Warnsdorff's
 * rule{@literal </a>} - heuristic for finding a knight's tour on chessboards.
 *
 * A knight's tour is a sequence of moves of a knight on a chessboard such that the knight visits
 * every square only once. If the knight ends on a square that is one knight's move from the
 * beginning square (so that it could tour the board again immediately, following the same path),
 * the tour is closed, otherwise it is open.
 *
 * The knight's tour problem is the mathematical problem of finding a knight's tour.
 *
 * Description of the Warnsdorff's rule: set a start cell. Always proceed to the cell that have the
 * fewest onward moves. In case of a tie(i.e. there exist more than one possible choice for the next
 * cell) go to the cell with largest Euclidean distance from the center of the board.
 *
 * This implementation also allows you to find a structured knight's tour.
 *
 * Knight's tour on board of size $n \times m$ is called structured if it contains the following $8$
 * UNDIRECTED moves:
 *
 * 1). $(1, 0) \to (0, 2)$ - denoted as $1$ on the picture below. 2). $(2, 0) \to (0, 1)$ - denoted
 * as $2$ on the picture below. 3). $(n - 3, 0) \to (n - 1, 1)$ - denoted as $3$ on the picture
 * below. 4). $(n - 2, 0) \to (n - 1, 2)$ - denoted as $4$ on the picture below. 5). $(0, m - 3) \to
 * (1, m - 1)$ - denoted as $5$ on the picture below. 6). $(0, m - 2) \to (2, m - 1)$ - denoted as
 * $6$ on the picture below. 7). $(n - 3, m - 1) \to (n - 1, m - 2)$ - denoted as $7$ on the picture
 * below. 8). $(n - 2, m - 1) \to (n - 1, m - 3)$ - denoted as $8$ on the picture below.
 *
 * ######################################### #*12*********************************34*#
 * #2*************************************3# #1*************************************4#
 * #***************************************# #***************************************#
 * #***************************************# #***************************************#
 * #***************************************# #***************************************#
 * #***************************************# #***************************************#
 * #***************************************# #***************************************#
 * #***************************************# #6*************************************8#
 * #5*************************************7# #*65*********************************78*#
 * #########################################
 *
 * If you are confused with the formal definition of the structured knight's tour please refer to
 * illustration on the page $3$ of the paper "An efficient algorithm for the Knight’s tour problem "
 * by Ian Parberry.
 *
 * One more feature of this implementation is that it provides an option to return a shifted
 * knight's tour, where all cell's coordinates are shifted by some values. Basically it is the same
 * as knight's tour of some piece of the board.
 */

public class WarnsdorffRuleKnightTourHeuristic
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
     * 2d array that stores information whether or not the cell has been visited.
     */

    private boolean[][] chessBoard;

    /**
     * Auxiliary array for offset in x coordinate when performing a move.
     */

    private final static int[] DX = new int[] { 1, 2, 2, 1, -1, -2, -2, -1 };

    /**
     * Auxiliary array for offset in y coordinate when performing a move.
     */

    private final static int[] DY = new int[] { 2, 1, -1, -2, -2, -1, 1, 2 };

    /**
     * Constructor.
     * 
     * @param n width and height of the board.
     */

    public WarnsdorffRuleKnightTourHeuristic(int n)
    {
        if (n < 3) {
            throw new IllegalArgumentException("Incorrect board size!");
        }
        this.n = n;
        this.m = n;
        chessBoard = new boolean[n][n];
    }

    /**
     * Constructor.
     * 
     * @param n width of the board.
     * @param m height of the board.
     */

    public WarnsdorffRuleKnightTourHeuristic(int n, int m)
    {
        if ((n < 3 && m < 3) || n <= 1 || m <= 1) {
            throw new IllegalArgumentException("Incorrect board size!");
        }
        this.n = n;
        this.m = m;
        chessBoard = new boolean[n][m];
    }

    /**
     * Calculates the number of the unvisited neighbours of the given cell.
     * 
     * @param currentCell represents cell for which we want to find the unvisited neighbours.
     * @return number of unvisited edges.
     */

    private int getNumberOfUnusedNeighbours(Pair<Integer, Integer> currentCell)
    {
        int ans = 0;

        for (int i = 0; i < 8; i++) {
            int newX = currentCell.getFirst() + DX[i];
            int newY = currentCell.getSecond() + DY[i];
            if (newX >= 0 && newX < n && newY >= 0 && newY < m && !chessBoard[newX][newY]) {
                ans++;
            }
        }

        return ans;
    }

    /**
     * Function for handling a tie case. In case of a tie the next cell will be the cell with the
     * largest Euclidean distance from the center of the board.
     * 
     * @param array that stores the cells with equal number of unvisited neighbours.
     * @return index of the next cell in the input array.
     */

    private int handleTie(ArrayList<Pair<Integer, Integer>> array)
    {
        int index = -1;
        int distance = -1;
        int xCenter = n / 2;
        int yCenter = m / 2;

        for (int i = 0; i < array.size(); i++) {
            int x = array.get(i).getFirst();
            int y = array.get(i).getSecond();
            if ((x - xCenter) * (x - xCenter) + (y - yCenter) * (y - yCenter) > distance) {
                distance = (x - xCenter) * (x - xCenter) + (y - yCenter) * (y - yCenter);
                index = i;
            }
        }

        return index;
    }

    /**
     * Finds the next cell to move.
     * 
     * @param cell represents start point of the move.
     * @return cell represents end point of the move.
     */

    private Pair<Integer, Integer> getMoveWarnsdorff(Pair<Integer, Integer> cell)
    {
        int curValue = Integer.MAX_VALUE;
        Pair<Integer, Integer> currentCell = new Pair<>(-1, -1);
        Pair<Integer, Integer> nextCell = new Pair<>(-1, -1);
        ArrayList<Pair<Integer, Integer>> tie = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            int newX = cell.getFirst() + DX[i];
            int newY = cell.getSecond() + DY[i];
            currentCell.setFirst(newX);
            currentCell.setSecond(newY);
            if (newX >= 0 && newX < n && newY >= 0 && newY < m && !chessBoard[newX][newY]) {
                int adjValue = getNumberOfUnusedNeighbours(currentCell);
                if (adjValue < curValue) {
                    curValue = adjValue;
                    nextCell.setFirst(currentCell.getFirst());
                    nextCell.setSecond(currentCell.getSecond());
                    tie.clear();
                    tie.add(new Pair<>(currentCell.getFirst(), currentCell.getSecond()));
                } else if (adjValue == curValue) {
                    tie.add(new Pair<>(newX, newY));
                }
            }
        }

        if (tie.size() > 1) {
            int index = handleTie(tie);
            nextCell.setFirst(tie.get(index).getFirst());
            nextCell.setSecond(tie.get(index).getSecond());
        }

        return nextCell;
    }

    /**
     * Checks type of the found tour.
     * 
     * @param startX start coordinate on x-axis.
     * @param startY start coordinate on y-axis.
     * @param endX end coordinate on x-axis.
     * @param endY end coordinate on y-axis.
     * @param type type of the tour we want to find.
     * @return true, if the found tour satisfies the required invariants, otherwise false.
     */

    private boolean checkType(int startX, int startY, int endX, int endY, TourType type)
    {
        if (type == TourType.CLOSED) {
            return Math.abs(startX - endX) == 1 && Math.abs(startY - endY) == 2
                || Math.abs(startX - endX) == 2 && Math.abs(startY - endY) == 1;
        }
        return !(Math.abs(startX - endX) == 1 && Math.abs(startY - endY) == 2
            || Math.abs(startX - endX) == 2 && Math.abs(startY - endY) == 1);
    }

    /**
     * Checks if the found tour is structured. Note, we don't know the direction of the edges in the
     * knight's tour, so we have to check both options, i.e. $a \to b$ and $b \to a$.
     * 
     * @param moves preformed in the tour.
     * @param structured true if user asked to find a structured knight's tour, false otherwise.
     * @return true if the user didn't ask to find a structured knight's tour or if the tour
     *         contains all the moves needed for tour to be structured, false otherwise.
     */

    private boolean checkStructured(
        HashSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> moves, boolean structured)
    {
        return !structured || ((moves.contains(new Pair<>(new Pair<>(1, 0), new Pair<>(0, 2)))
            || moves.contains(new Pair<>(new Pair<>(0, 2), new Pair<>(1, 0))))
            && moves.contains(new Pair<>(new Pair<>(2, 0), new Pair<>(0, 1)))
            || moves.contains(new Pair<>(new Pair<>(0, 1), new Pair<>(2, 0)))

                &&

                moves.contains(new Pair<>(new Pair<>(n - 3, 0), new Pair<>(n - 1, 1)))
            || moves.contains(new Pair<>(new Pair<>(n - 1, 1), new Pair<>(n - 3, 0)))
                && moves.contains(new Pair<>(new Pair<>(n - 2, 0), new Pair<>(n - 1, 2)))
            || moves.contains(new Pair<>(new Pair<>(n - 1, 2), new Pair<>(n - 2, 0)))

                &&

                moves.contains(new Pair<>(new Pair<>(0, m - 3), new Pair<>(1, m - 1)))
            || moves.contains(new Pair<>(new Pair<>(1, m - 1), new Pair<>(0, m - 3)))
                && moves.contains(new Pair<>(new Pair<>(0, m - 2), new Pair<>(2, m - 1)))
            || moves.contains(new Pair<>(new Pair<>(2, m - 1), new Pair<>(0, m - 2)))

                &&

                moves.contains(new Pair<>(new Pair<>(n - 3, m - 1), new Pair<>(n - 1, m - 2)))
            || moves.contains(new Pair<>(new Pair<>(n - 1, m - 2), new Pair<>(n - 3, m - 1)))
                && moves.contains(new Pair<>(new Pair<>(n - 2, m - 1), new Pair<>(n - 1, m - 3)))
            || moves.contains(new Pair<>(new Pair<>(n - 1, m - 3), new Pair<>(n - 2, m - 2))));
    }

    /**
     * Converts doubly linked list of chessboard cells to the set of moves.
     * 
     * @param tour we have found.
     * @return set of moves of the input tour.
     */

    private HashSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> getMoves(
        KnightTour.DoublyLinkedList<Pair<Integer, Integer>> tour)
    {
        HashSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> moves = new HashSet<>();
        KnightTour.Node<Pair<Integer, Integer>> headNode = tour.getHead();
        KnightTour.Node<Pair<Integer, Integer>> nextNode = headNode.getNext();
        while (nextNode != null) {
            moves.add(new Pair<>(headNode.getValue(), nextNode.getValue()));
            headNode = headNode.getNext();
            nextNode = nextNode.getNext();
        }
        return moves;
    }

    /**
     * Checks existence of the knight's tour.
     * 
     * @param type of the tour.
     * @return true if the tour exists, otherwise false.
     */

    private boolean checkExistence(TourType type)
    {
        int newN = Math.min(n, m);
        int newM = Math.max(n, m);

        /*
         * Allen Schwenk, 1991 Which Rectangular Chessboards Have a Knight's Tour?.
         * 
         * Theorem: An n x m chessboard with n <= m has a closed knight's tour unless one or more of
         * these three condition holds: (a) n and m are both odd; (b) n = 1, 2, 4; (c) n = 3 and m =
         * 4, 6, 8.
         */

        if (type == TourType.CLOSED) {
            return !((newN % 2 == 1 && newM % 2 == 1) || newN == 1 || newN == 2 || newN == 4
                || (newN == 3 && (newM == 4 || newM == 6 || newM == 8)));
        }

        /*
         * Regarding open knight's tour existence, refer to
         * http://gaebler.us/share/Knight_tour.html.
         * 
         * Rob Gaebler, Tsu-wang Yang, Knight's Tours (August 13, 1999).
         */

        return (newN == 3 && newM == 4 || newN == 3 && newM >= 7 || newN >= 4 && newM >= 5);
    }

    /**
     * Updates the pointer on the cell in structured array if the last added cell was structured. If
     * it is a non-structured cell then returns -1.
     * 
     * @param cell last added to the tour cell.
     * @return the index of the corresponding cell in the structured array and -1 if the last added
     *         cell is not a structured cell .
     */

    private int updateStructuredPosition(Pair<Integer, Integer> cell)
    {
        if (cell.getFirst() == 2 && cell.getSecond() == 0) {
            return 0;
        } else if (cell.getFirst() == 0 && cell.getSecond() == 1) {
            return 1;
        } else if (cell.getFirst() == n - 1 && cell.getSecond() == 0) {
            return 2;
        } else if (cell.getFirst() == n - 2 && cell.getSecond() == 2) {
            return 3;
        } else if (cell.getFirst() == 1 && cell.getSecond() == m - 3) {
            return 4;
        } else if (cell.getFirst() == 0 && cell.getSecond() == m - 1) {
            return 5;
        } else if (cell.getFirst() == n - 1 && cell.getSecond() == m - 2) {
            return 6;
        } else if (cell.getFirst() == n - 3 && cell.getSecond() == m - 1) {
            return 7;
        }
        return -1;
    }

    /**
     * Generates a knight's tour that satisfies the input parameters.
     *
     * Warnsdorff's rule heuristic is an example of a greedy method, which we use to select the next
     * cell to move, and thus may fail to find a tour. However, another greedy heuristic is used to
     * prevent failing: in case of a tie we will select a cell with the largest euclidean distance
     * from the center of the board. Such combination of greedy methods significantly increases our
     * chances to find a tour.
     *
     * @param type of the tour.
     * @param structured true if we want the tour to be structured, otherwise false.
     * @param shiftX the value will be added to each cell's x-coordinate to reach effect of
     *        shifting.
     * @param shiftY the value will be added to each cell's t-coordinate to reach effect of
     *        shifting.
     * @return knight's tour.
     */

    public KnightTour getTour(TourType type, boolean structured, int shiftX, int shiftY)
    {

        if (shiftX < 0 || shiftY < 0) {
            throw new IllegalArgumentException("Incorrect shift value!");
        }

        if (!checkExistence(type)) {
            throw new IllegalArgumentException("No solution exist for such configuration!");
        }

        KnightTour tour = new KnightTour();
        Random rand = new Random();
        int startX, startY;
        Pair<Integer, Integer> currentCell = new Pair<>(-1, -1);
        int visited;
        int run = 0;

        boolean[][] wasStartingVertex = new boolean[n][m];

        boolean found = false;
        while (!found) {
            visited = 0;

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    chessBoard[i][j] = false;
                }
            }

            tour.getList().clear();

            startX = rand.nextInt(n);
            startY = rand.nextInt(m);

            currentCell.setFirst(startX);
            currentCell.setSecond(startY);

            while (wasStartingVertex[startX][startY]) {
                startX = rand.nextInt(n);
                startY = rand.nextInt(m);
                currentCell.setFirst(startX);
                currentCell.setSecond(startY);
            }

            wasStartingVertex[startX][startY] = true;
            run++;

            while (visited < n * m) {
                chessBoard[currentCell.getFirst()][currentCell.getSecond()] = true;
                tour.getList().add(currentCell);

                /*
                 * If we have added the structured cell then update pointer on that cell in the
                 * structured array.
                 */

                if (structured) {
                    int val = updateStructuredPosition(currentCell);
                    if (val != -1) {
                        tour.getStructured().set(val, tour.getList().getTail());
                    }
                }

                visited++;
                currentCell = getMoveWarnsdorff(currentCell);
                if (currentCell.getFirst() == -1) {
                    break;
                }
            }

            Pair<Integer, Integer> endCell = tour.getList().getTail().getValue();
            if (visited == n * m
                && checkType(startX, startY, endCell.getFirst(), endCell.getSecond(), type))
            {
                HashSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> moves =
                    getMoves(tour.getList());
                if (checkStructured(moves, structured)) {
                    found = true;
                }
            }

            /*
             * Try again if there is no unused start cells are left.
             */

            if (run == (n * m) && !found) {
                return null;
            }

        }

        /*
         * Perform shifting.
         */

        KnightTour.Node<Pair<Integer, Integer>> node = tour.getList().getHead();
        while (node != null) {
            node.getValue().setFirst(node.getValue().getFirst() + shiftX);
            node.getValue().setSecond(node.getValue().getSecond() + shiftY);
            node = node.getNext();
        }

        /*
         * Make the list cyclic.
         */

        tour.getList().getHead().setPrev(tour.getList().getTail());
        tour.getList().getTail().setNext(tour.getList().getHead());

        /*
         * Set the start node.
         */

        tour.getList().setStartNode(tour.getList().getHead());

        return tour;
    }
}
