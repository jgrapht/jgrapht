/*
 * (C) Copyright 2018-2026, by Kirill Vishnyakov and Contributors.
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
 * Class that represents container for knight's tour.
 */
class KnightTour
{

    /**
     * Enum type that represents two knight's tour types: closed and open.
     */
    enum TourType
    {
        CLOSED,
        OPEN
    }

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
