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

import java.util.*;

/*
  Implementation of {@literal <a href = "https://en.wikipedia.org/wiki/Knight%27s_tour#Warnsdorf's_rule">}Warnsdorff's rule{@literal </a>}
  - heuristic for finding a knight's tour on chessboards.

  A knight's tour is a sequence of moves of a knight on a chessboard such that the knight visits every square only once.
  If the knight ends on a square that is one knight's move from the beginning square
  (so that it could tour the board again immediately, following the same path), the tour is closed, otherwise it is open.

  The knight's tour problem is the mathematical problem of finding a knight's tour.

  Description of the Warnsdorff's rule: set a start cell. Always proceed to the cell that have the fewest onward moves.
  In case of a tie(i.e. there exist more than one possible choice for the next cell) go to the cell with largest Euclidean distance from the center of the board.

  This implementation also allows you to find a structured knight's tour.

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
  "An efficient algorithm for the Knight’s tour problem " by Ian Parberry.

  One more feature of this implementation is that it provides an option to return a shifted knight's tour,
  where all cell's coordinates are shifted by some values.
  Basically it is the same as knight's tour of some piece of the board.
 */

/**
 * Class that represents a chessboard cell.
 * @param <F> type of the first attribute.
 * @param <S> type of the second attribute.
 */

class ChessBoardCell<F, S>
{
    F first;
    S second;

    public ChessBoardCell() {}

    public ChessBoardCell (F f, S s) {
        first = f;
        second = s;
    }

    @Override
    public String toString() {
        return "(" + first + "," + second + ")";
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChessBoardCell))
            return false;
        ChessBoardCell<?, ?> that = (ChessBoardCell<?, ?>) o;
        return Objects.equals(first, that.first) && Objects.equals(second, that.second);
    }

    @Override public int hashCode() {
        return Objects.hash(first, second);
    }
}

/**
 * Class that represents a move.
 */

class Move
{

    /**
     * Start cell of a move.
     */

    private final ChessBoardCell<Integer, Integer> from;

    /**
     * End cell of a move.
     */

    private final ChessBoardCell<Integer, Integer> to;

    public Move(ChessBoardCell<Integer, Integer> from, ChessBoardCell<Integer, Integer> to) {
        this.from = from;
        this.to = to;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Move))
            return false;
        Move move = (Move) o;
        return Objects.equals(from, move.from) && Objects.equals(to, move.to);
    }

    @Override public int hashCode() {

        return Objects.hash(from, to);
    }
}

/**
 * Implementation of a doubly linked list data structure that is being used for storing a tour.
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
     * Pointer to the start node.
     * Start node is the node from which we start any traversal operation on the list.
     */

    private Node<E> startNode;

    /**
     * Size of the list.
     */

    private int size;

    public DoublyLinkedList() {
        head = null;
        tail = null;
        startNode = null;
        size = 0;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Adds element to the end of the list.
     * @param element we want to add.
     */

    public void add(E element) {
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

    public void remove() {
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

    public Node<E> getHead() {
        return head;
    }

    public Node<E> getTail() {
        return tail;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    public void setStartNode(Node<E> startNode) {
        this.startNode = startNode;
    }

    public Node<E> getStartNode() {
        return startNode;
    }

    public void setSize(int i) {
        size = i;
    }

    /**
     * Nested static class that represents a node of the doubly linked list.
     * @param <E> type of the value stored in the node.
     */

    public static class Node<E> {

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
         * Boolean flag that is being used in traversal function, such as toArrayList.
         * True if the node was visited, otherwise false.
         */

        private boolean visited = false;

        public Node(E value) {
            this.value = value;
        }

        public Node() {}

        public boolean isVisited() {
            return !visited;
        }

        public void setVisited(boolean visited) {
            this.visited = visited;
        }

        public E getValue() {
            return value;
        }

        public Node<E> getNext() {
            return next;
        }

        public Node<E> getPrev() {
            return prev;
        }

        public void setPrev(Node<E> prev) {
            this.prev = prev;
        }

        public void setNext(Node<E> next) {
            this.next = next;
        }
    }
}

/**
 * Enum type that represents two knight's tour types: closed and open.
 */

enum TourType {
    CLOSED,
    OPEN
}

/**
 * Class that represents container for knight's tour.
 */

class KnightTourContainer {

    /**
     * Doubly linked list that stores nodes in order of their appearance in the knight's tour.
     */

    private final DoublyLinkedList<ChessBoardCell<Integer, Integer>> list;

    /*
      Let's call each of the following $8$ cells structured:
     
      (enumeration starts with $0$ to keep relation between cells and indices in structured array)
     
      0). (2, 0);
      1). (0, 1);
      2). (n - 1, 0);
      3). (n - 2, 2);
      4). (1, m - 3);
      5). (0, m - 1);
      6). (n - 1, m - 2);
      7). (n - 3, m - 1);
     
      #########################################
      #**0***********************************2#
      #1**************************************#
      #*************************************3*#
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
      #*4*************************************#
      #**************************************6#
      #5***********************************7**#
      #########################################
      
      Structured cells are needed to implement the merging procedure in the Parberry's algorithm.
     */

    /**
     * Array that stores pointers on the structured cells.
     */

    private final ArrayList<DoublyLinkedList.Node<ChessBoardCell<Integer, Integer>>> structured;

    /**
     * Used in toArrayList function.
     */

    private ArrayList<ChessBoardCell<Integer, Integer>> arrayList;

    public KnightTourContainer() {
        structured = new ArrayList<>(Collections.nCopies(8, new DoublyLinkedList.Node<>()));
        list = new DoublyLinkedList<>();
        arrayList = null;
    }

    public ArrayList<ChessBoardCell<Integer, Integer>> toArrayList() {
        if (arrayList != null) {
            return arrayList;
        }

        DoublyLinkedList.Node<ChessBoardCell<Integer, Integer>> startNode = list.getStartNode();
        startNode.setVisited(true);
        arrayList = new ArrayList<>();
        arrayList.add(startNode.getValue());

        startNode = startNode.getNext();

        arrayList.add(startNode.getValue());
        startNode.setVisited(true);

        /*
         Traverse of the list.
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

    public DoublyLinkedList<ChessBoardCell<Integer, Integer>> getList() {
        return list;
    }

    public ArrayList<DoublyLinkedList.Node<ChessBoardCell<Integer, Integer>>> getStructured() {
        return structured;
    }
}

/**
 * Implementation of the Warnsdorff's rule.
 */

public class WarnsdorffRule {

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

    private final static int[] DX = new int[]{1, 2, 2, 1, -1, -2, -2, -1};

    /**
     * Auxiliary array for offset in y coordinate when performing a move.
     */

    private final static int[] DY = new int[]{2, 1, -1, -2, -2, -1, 1, 2};

    /**
     * Constructor.
     * @param n width and height of the board.
     */

    public WarnsdorffRule(int n) {
        if (n < 3) {
            throw new UnsupportedOperationException("You set incorrect board size!");
        }
        this.n = n;
        this.m = n;
        chessBoard = new boolean[n][n];
    }

    /**
     * Constructor.
     * @param n width of the board.
     * @param m height of the board.
     */

    public WarnsdorffRule(int n, int m) {
        if ((n < 3 && m < 3) || n <= 1 || m <= 1) {
            throw new UnsupportedOperationException("You set incorrect board size!");
        }
        this.n = n;
        this.m = m;
        chessBoard = new boolean[n][m];
    }

    /**
     * Calculates the number of the unvisited neighbours of the given cell.
     * @param currentCell represents cell for which we want to find the unvisited neighbours.
     * @return number of unvisited edges.
     */

    private int getNumberOfUnusedNeighbours(ChessBoardCell<Integer, Integer> currentCell) {
        int ans = 0;

        for (int i = 0; i < 8; i++) {
            int newX = currentCell.first + DX[i];
            int newY = currentCell.second + DY[i];
            if (newX >= 0 && newX < n &&  newY >= 0 && newY < m && !chessBoard[newX][newY]) {
                ans++;
            }
        }

        return ans;
    }

    /**
     * Function for handling a tie case.
     * In case of a tie the next cell will be the cell with the largest Euclidean distance from the center of the board.
     * @param array that stores the cells with equal number of unvisited neighbours.
     * @return index of the next cell in the input array.
     */

    private int handleTie(ArrayList<ChessBoardCell<Integer, Integer>> array) {
        int index = -1;
        int distance = -1;
        int xCenter = n / 2;
        int yCenter = m / 2;

        for (int i = 0; i < array.size(); i++) {
            int x = array.get(i).first;
            int y = array.get(i).second;
            if ((x - xCenter) * (x - xCenter) + (y - yCenter) * (y - yCenter) > distance) {
                distance = (x - xCenter) * (x - xCenter) + (y - yCenter) * (y - yCenter);
                index = i;
            }
        }

        return index;
    }

    /**
     * Finds the next cell to move.
     * @param cell represents start point of the move.
     * @return cell represents end point of the move.
     */

    private ChessBoardCell<Integer, Integer> getMoveWarnsdorff(ChessBoardCell<Integer, Integer> cell) {
        int curValue = Integer.MAX_VALUE;
        ChessBoardCell<Integer, Integer> currentCell = new ChessBoardCell<>();
        ChessBoardCell<Integer, Integer> nextCell = new ChessBoardCell<>(-1, -1);
        ArrayList<ChessBoardCell<Integer, Integer>> tie = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            int newX = cell.first + DX[i];
            int newY = cell.second + DY[i];
            currentCell.first = newX;
            currentCell.second = newY;
            if (newX >= 0 && newX < n && newY >= 0 && newY < m && !chessBoard[newX][newY]) {
                int adjValue = getNumberOfUnusedNeighbours(currentCell);
                if (adjValue < curValue) {
                    curValue = adjValue;
                    nextCell.first = currentCell.first;
                    nextCell.second = currentCell.second;
                    tie.clear();
                    tie.add(new ChessBoardCell<>(currentCell.first, currentCell.second));
                }
                else if (adjValue == curValue) {
                    tie.add(new ChessBoardCell<>(newX, newY));
                }
            }
        }

        if (tie.size() > 1) {
            int index = handleTie(tie);
            nextCell.first = tie.get(index).first;
            nextCell.second = tie.get(index).second;
        }

        return nextCell;
    }

    /**
     * Checks type of the found tour.
     * @param startX start coordinate on x-axis.
     * @param startY start coordinate on y-axis.
     * @param endX end coordinate on x-axis.
     * @param endY end coordinate on y-axis.
     * @param type type of the tour we want to find.
     * @return true, if the found tour satisfies the required invariants, otherwise false.
     */

    private boolean checkType(int startX, int startY, int endX, int endY, TourType type) {
        if (type == TourType.CLOSED) {
            return Math.abs(startX - endX) == 1 && Math.abs(startY - endY) == 2 || Math.abs(startX - endX) == 2 && Math.abs(startY - endY) == 1;
        }
        return !(Math.abs(startX - endX) == 1 && Math.abs(startY - endY) == 2 || Math.abs(startX - endX) == 2 && Math.abs(startY - endY) == 1);
    }

    /**
     * Checks if the found tour is structured.
     * Note, we don't know the direction of the edges in the knight's tour, so we have to check both options, i.e. $a \to b$ and $b \to a$.
     * @param moves preformed in the tour.
     * @param structured true if user asked to find a structured knight's tour, false otherwise.
     * @return true if the user didn't ask to find a structured knight's tour
     * or if the tour contains all the moves needed for tour to be structured, false otherwise.
     */

    private boolean checkStructured(HashSet<Move> moves, boolean structured) {
        return !structured || (
            (moves.contains(new Move(new ChessBoardCell<>(1, 0), new ChessBoardCell<>(0, 2)))
                || moves.contains(new Move(new ChessBoardCell<>(0, 2), new ChessBoardCell<>(1, 0))))
                &&
                moves.contains(new Move(new ChessBoardCell<>(2, 0), new ChessBoardCell<>(0, 1)))
                || moves.contains(new Move(new ChessBoardCell<>(0, 1), new ChessBoardCell<>(2, 0)))

                &&

                moves.contains(new Move(new ChessBoardCell<>(n - 3, 0), new ChessBoardCell<>(n - 1, 1)))
                || moves.contains(new Move(new ChessBoardCell<>(n - 1, 1), new ChessBoardCell<>(n - 3, 0)))
                &&
                moves.contains(new Move(new ChessBoardCell<>(n - 2, 0), new ChessBoardCell<>(n - 1, 2)))
                || moves.contains(new Move(new ChessBoardCell<>(n - 1, 2), new ChessBoardCell<>(n - 2, 0)))

                &&

                moves.contains(new Move(new ChessBoardCell<>(0, m - 3), new ChessBoardCell<>(1, m - 1)))
                || moves.contains(new Move(new ChessBoardCell<>(1, m - 1), new ChessBoardCell<>(0, m - 3)))
                &&
                moves.contains(new Move(new ChessBoardCell<>(0, m - 2), new ChessBoardCell<>(2, m - 1)))
                || moves.contains(new Move(new ChessBoardCell<>(2, m - 1), new ChessBoardCell<>(0, m - 2)))

                &&

                moves.contains(new Move(new ChessBoardCell<>(n - 3, m - 1), new ChessBoardCell<>(n - 1, m - 2)))
                || moves.contains(new Move(new ChessBoardCell<>(n - 1, m - 2), new ChessBoardCell<>(n - 3, m - 1)))
                &&
                moves.contains(new Move(new ChessBoardCell<>(n - 2, m - 1), new ChessBoardCell<>(n - 1, m - 3)))
                || moves.contains(new Move(new ChessBoardCell<>(n - 1, m - 3), new ChessBoardCell<>(n - 2, m - 2))));
    }

    /**
     * Converts doubly linked list of chessboard cells to the set of moves.
     * @param tour we have found.
     * @return set of moves of the input tour.
     */

    private HashSet<Move> getMoves(DoublyLinkedList<ChessBoardCell<Integer, Integer>> tour) {
        HashSet<Move> moves = new HashSet<>();
        DoublyLinkedList.Node<ChessBoardCell<Integer, Integer>> headNode = tour.getHead();
        DoublyLinkedList.Node<ChessBoardCell<Integer, Integer>> nextNode = headNode.getNext();
        while (nextNode != null) {
            moves.add(new Move(headNode.getValue(), nextNode.getValue()));
            headNode = headNode.getNext();
            nextNode = nextNode.getNext();
        }
        return moves;
    }

    /**
     * Checks existence of the knight's tour.
     * @param type of the tour.
     * @return true if the tour exists, otherwise false.
     */

    private boolean checkExistence(TourType type) {
        int newN = Math.min(n, m);
        int newM = Math.max(n, m);

        /*
         Allen Schwenk, 1991
         {@literal <a href = "https://pdfs.semanticscholar.org/c3f5/e69e771771de1be50a8a8bf2561804026d69.pdf">}Which Rectangular Chessboards Have a Knight's Tour?{@literal </a>}.

         \textbf{Theorem:} An $n \times m$ chessboard with $n \leq m$ has a closed knight's tour
         unless one or more of these three condition holds:
         (a) $n$ and $m$ are both odd;
         (b) $n = 1, 2, 4$;
         (c) $n = 3$ and $m = 4, 6, 8$.
         */

        if (type == TourType.CLOSED) {
            return !((newN % 2 == 1 && newM % 2 == 1) || newN == 1 || newN == 2 || newN == 4 || (newN == 3 && (newM == 4 || newM == 6 || newM == 8)));
        }

        /*
         Regarding open knight's tour existence, refer to {@literal <a href = "http://gaebler.us/share/Knight_tour.html">}this{@literal </a>} page.
         */

        return (newN == 3 && newM == 4 || newN == 3 && newM >= 7 || newN >= 4 && newM >= 5);
    }

    /**
     * Updates the pointer on the cell in structured array if the last added cell was structured.
     * If it is a non-structured cell then returns -1.
     * @param cell last added to the tour cell.
     * @return the index of the corresponding cell in the structured array and -1 if the last added cell is not a structured cell .
     */

    private int updateStructuredPosition(ChessBoardCell<Integer, Integer> cell) {
        if (cell.first == 2 && cell.second == 0) {
            return 0;
        }
        else if (cell.first == 0 && cell.second == 1) {
            return 1;
        }
        else if (cell.first == n - 1 && cell.second == 0) {
            return 2;
        }
        else if (cell.first == n - 2 && cell.second == 2) {
            return 3;
        }
        else if (cell.first == 1 && cell.second == m - 3) {
            return 4;
        }
        else if (cell.first == 0 && cell.second == m - 1) {
            return 5;
        }
        else if (cell.first == n - 1 && cell.second == m - 2) {
            return 6;
        }
        else if (cell.first == n - 3 && cell.second == m - 1) {
            return 7;
        }
        return -1;
    }

    /**
     * Generates a knight's tour that satisfies the input parameters.
     * @param type of the tour.
     * @param structured true if we want the tour to be structured, otherwise false.
     * @param shiftX the value will be added to each cell's x-coordinate to reach effect of shifting.
     * @param shiftY the value will be added to each cell's t-coordinate to reach effect of shifting.
     * @return knight's tour.
     */

    public KnightTourContainer generateTour(TourType type, boolean structured, int shiftX, int shiftY) {

        if (shiftX < 0 || shiftY < 0) {
            throw new UnsupportedOperationException("You have set incorrect shift!");
        }

        if (!checkExistence(type)) {
            throw new UnsupportedOperationException("No solution exist for such configuration!");
        }

        KnightTourContainer tour = new KnightTourContainer();
        Random rand = new Random();
        int startX, startY;
        ChessBoardCell<Integer, Integer> currentCell = new ChessBoardCell<>();
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

            currentCell.first = startX;
            currentCell.second = startY;

            while (wasStartingVertex[startX][startY]) {
                startX = rand.nextInt(n);
                startY = rand.nextInt(m);
                currentCell.first = startX;
                currentCell.second = startY;
            }

            wasStartingVertex[startX][startY] = true;
            run++;

            while (visited < n * m) {
                chessBoard[currentCell.first][currentCell.second] = true;
                tour.getList().add(currentCell);

                /*
                 If we have added the structured cell then update pointer on that cell in the structured array.
                 */

                if (structured) {
                    int val = updateStructuredPosition(currentCell);
                    if (val != -1) {
                        tour.getStructured().set(val, tour.getList().getTail());
                    }
                }

                visited++;
                currentCell = getMoveWarnsdorff(currentCell);
                if (currentCell.first == -1) {
                    break;
                }
            }

            ChessBoardCell<Integer, Integer> endCell = tour.getList().getTail().getValue();
            if (visited == n * m && checkType(startX, startY, endCell.first, endCell.second, type)) {
                HashSet<Move> moves = getMoves(tour.getList());
                if (checkStructured(moves, structured)) {
                    found = true;
                }
            }

            /*
             Try again if there is no unused start cells are left.
             */

            if (run % (n * m) == 0) {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < m; j++) {
                        wasStartingVertex[i][j] = false;
                    }
                }
            }

        }

        /*
         Perform shifting.
         */

        DoublyLinkedList.Node<ChessBoardCell<Integer, Integer>> node = tour.getList().getHead();
        while (node != null) {
            node.getValue().first += shiftX;
            node.getValue().second += shiftY;
            node = node.getNext();
        }

        /*
         Make the list cyclic.
         */

        tour.getList().getHead().setPrev(tour.getList().getTail());
        tour.getList().getTail().setNext(tour.getList().getHead());

        /*
         Set the start node.
         */
        
        tour.getList().setStartNode(tour.getList().getHead());

        return tour;
    }
}
