/*
 * (C) Copyright 2018-2018, by Daniel Mock and Contributors.
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
package org.jgrapht.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A collection of SplitQueues. Each are disjoint. Elements cannot be added or removed (except with poll).
 * This class is not meant to be exposed.
 *
 * @author Daniel Mock
 */
class SuperSplitQueue {
    /**
     * containedIn[i] == k if and only if i is contained in queue k init with 0
     */
    final private int[] containedIn;

    /**
     * previous[i] = j if and only if i and j are in the same queue and j is the direct predecessor
     * of i or -1 if it is not contained in any queue or i is the first element in a queue
     */
    final private int[] previous;

    /**
     * next[i] = j if and only if i and j are in the same queue and j is the direct
     * successor of i or -1 if i is not contained in the queue or i is the last element in a queue
     */
    final private int[] next;

    /**
     * The first element of the queue with the given index.
     */
    final private ArrayList<Integer> firstOfQ;

    /**
     * The last element of the queue with the given index.
     */
    final private ArrayList<Integer> lastOfQ;

    /**
     * The size of the queue with the given index.
     */
    final private ArrayList<Integer> sizeOfQ;

    /**
     * The size of the universe
     */
    final private int universeSize;
    /**
     * Maps the index to the queues.
     */
    final private ArrayList<SubSplitQueue> queueByIndex;
    /**
     * The amount of queues. It equals the maximal queue index + 1.
     */
    private int amountQueues;

    /**
     * Returns a new empty SuperSplitQueue of size universeSize with one SubSplitQueue
     *
     * @param universeSize size of the universe
     */
    private SuperSplitQueue(int universeSize) {
        this.universeSize = universeSize;

        containedIn = new int[universeSize];
        previous = new int[universeSize];
        next = new int[universeSize];

        queueByIndex = new ArrayList<>();

        firstOfQ = new ArrayList<>();
        lastOfQ = new ArrayList<>();
        sizeOfQ = new ArrayList<>();

        // initialize arrays with -1
        for (int i = 0; i < universeSize; i++) {
            previous[i] = -1;
            next[i] = -1;
            containedIn[i] = -1;
        }

        addNewSubSplitQueue();
    }

    /**
     * Returns a full supersplitqueue, ordered by the natural ordering if the isFull is true
     * otherwise it is empty
     *
     * @param universeSize size of the universe
     * @param isFull       true if the new SuperSplitQueue should be filled
     */
    private SuperSplitQueue(int universeSize, boolean isFull) {
        this(universeSize);

        if (!isFull) {
            return;
        }

        for (int i = 0; i < universeSize; i++) {
            addLast(i, 0);
        }
    }

    /**
     * Initializes a SuperSplitQueue with the given ordering
     *
     * @param universeSize size of the universe
     * @param ordering     the ordering
     */
    private SuperSplitQueue(int universeSize, int[] ordering) {
        this(universeSize);

        for (int i : ordering) {
            addLast(i, 0);
        }

    }

    static SubSplitQueue instantiate(int universeSize) {
        return (new SuperSplitQueue(universeSize, true)).queueByIndex.get(0);
    }

    static SubSplitQueue instantiate(int universeSize, int[] sortedElements) {
        return new SuperSplitQueue(universeSize, sortedElements).queueByIndex.get(0);
    }

    /**
     * Adds a new empty SubSplitQueue with consecutive index.
     *
     * @return new empty SubSplitQueue
     */
    private SubSplitQueue addNewSubSplitQueue() {
        SubSplitQueue result = new SubSplitQueue(amountQueues, this);

        firstOfQ.add(-1);
        lastOfQ.add(-1);
        sizeOfQ.add(0);
        queueByIndex.add(result);

        amountQueues++;

        return result;
    }

    /**
     * Returns the index with the lowest order in the SubSplitQueue with index queueIndex. This
     * element won't be removed
     *
     * @param queueIndex the index of the SubSplitQueue
     * @return first item in SubSplitQueue with index queueIndex
     */
    int peek(int queueIndex) {
        if (isEmpty(queueIndex)) {
            throw new NoSuchElementException();
        }
        return firstOfQ.get(queueIndex);
    }

    /**
     * Returns and deletes the index with the lowest order in the SubSplitQueue with index
     * queueIndex.
     *
     * @param queueIndex the index of the SubSplitQueue
     * @return irst item in SubSplitQueue with index queueIndex
     */
    int poll(int queueIndex) {
        int result = peek(queueIndex);
        remove(result, queueIndex);
        return result;
    }

    /**
     * Returns true if and only if the given SubSplitQueue has no elements
     *
     * @param queueIndex Index of the SubSplitQueue
     * @return Returns whether SubSplitQueue is empty
     */
    boolean isEmpty(int queueIndex) {
        return firstOfQ.get(queueIndex) == -1;
    }

    /**
     * Removes the given element and keeps the data structure consistent Runs in constant time
     *
     * @param element the element to be removed
     */
    void remove(int element, int queueIndex) {
        inputChecker(element, queueIndex);
        if (containedIn[element] != queueIndex) {
            throw new NoSuchElementException("Element " + element + " not in specified queue");
        }

        // we want to remove the element, so the we have to connect the links between the
        // predecessor and successor of element

        // adapt the link from the previous to the next element
        if (previous[element] != -1) { // nothing to update if it is the first element
            next[previous[element]] = next[element];
        } else if (firstOfQ.get(queueIndex) == element) {
            firstOfQ.set(queueIndex, next[element]);
        } else {
            throw new RuntimeException();
        }

        // adapt the link from the next to the previous element
        if (next[element] != -1) { // nothing to update if it is the last element
            previous[next[element]] = previous[element];
        } else if (lastOfQ.get(queueIndex) == element) {
            lastOfQ.set(queueIndex, previous[element]);
        }

        // reset element's entries
        containedIn[element] = -1;
        previous[element] = -1;
        next[element] = -1;

        sizeOfQ.set(queueIndex, sizeOfQ.get(queueIndex) - 1);
    }

    /**
     * Splits this queue in two: Elements in splitters are removed and transferred to a new queue,
     * the others stay. Run time: splitters.length
     *
     * @param splitters HAVE TO BE SORTED
     * @return the index of the new queue
     */
    SubSplitQueue split(int[] splitters, int queue) {
        SubSplitQueue newQueue = addNewSubSplitQueue();

        for (int i : splitters) {
            if (containedIn[i] == queue) {
                moveElementTo(i, queue, newQueue.getOwnIndex());
            }
        }

        return newQueue;
    }

    /**
     * Adds the given element at the end of the queue. This element should come later in the
     * ordering than the last element of the SubSplitQueue
     *
     * @param element    element to add
     * @param queueIndex index of SubSplitQueue
     */
    private void addLast(int element, int queueIndex) {
        inputChecker(element, queueIndex);
        if (containedIn[element] != -1) {
            throw new IllegalArgumentException();
        }

        int last = lastOfQ.get(queueIndex);
        int first = firstOfQ.get(queueIndex);

        containedIn[element] = queueIndex;
        // adapt the entries for this element
        previous[element] = last;
        if (last != -1) {
            next[last] = element;
        }
        last = element;

        if (first == -1) {
            first = element;
        }

        lastOfQ.set(queueIndex, last);
        firstOfQ.set(queueIndex, first);
        sizeOfQ.set(queueIndex, sizeOfQ.get(queueIndex) + 1);
    }

    /**
     * Moves the given element from oldQueue to newQueue
     *
     * @param element  element to move
     * @param oldQueue index of the queue where element should be removed
     * @param newQueue index of the queue where element should be added
     */
    private void moveElementTo(int element, int oldQueue, int newQueue) {
        remove(element, oldQueue);
        addLast(element, newQueue);
    }

    /**
     * Returns whether the SubSplitQueue with queueIndex contains the given element
     *
     * @param element    the element to check
     * @param queueIndex the index of the Queue
     * @return whether the element is contained
     */
    boolean contains(int element, int queueIndex) {
        if (element < 0 || element >= universeSize) {
            return false;
        }
        return containedIn[element] == queueIndex;
    }

    /**
     * Throws an IllegalArgumentException if the element or the queueIndex are not in universe/index
     * range
     *
     * @param element    element to be checked
     * @param queueIndex index to be checked
     */
    private void inputChecker(int element, int queueIndex) {
        if (element < 0 || element >= universeSize) {
            throw new IllegalArgumentException("Element not suitable for this queue in general");
        }
        if (queueIndex < 0 || queueIndex >= amountQueues) {
            throw new IllegalArgumentException("Queue does not exist");
        }
    }

    /**
     * Returns an iterator which iterates over the given SubSplitQueue
     *
     * @param ownIndex the index of the iterated SubSplitQueue
     * @return Iterator
     */
    Iterator<Integer> iterator(int ownIndex) {
        return new Iterator<Integer>() {
            private int currentIndex = firstOfQ.get(ownIndex);
            private int previous = -1;

            @Override
            public boolean hasNext() {
                return currentIndex != -1;
            }

            @Override
            public Integer next() {
                if (currentIndex == -1) {
                    throw new NoSuchElementException();
                }

                previous = currentIndex;
                currentIndex = next[currentIndex];
                return previous;
            }

            @Override
            public void remove() {
                SuperSplitQueue.this.remove(previous, ownIndex);
            }
        };
    }

    /**
     * Returns the elements contained in the SubSplitQueue as array in the given order
     *
     * @param queueIndex index of SubSplitQueue
     * @return the array of elements
     */
    int[] asArray(int queueIndex) {
        int currentIndex = firstOfQ.get(queueIndex);
        int[] result = new int[getSize(queueIndex)];
        for (int i = 0; i < result.length; i++) {
            result[i] = currentIndex;
            currentIndex = next[currentIndex];
        }

        return result;
    }

    /**
     * Returns the amount of elements in the SubSplitQueue
     *
     * @param queueIndex index of SubSplitQueue
     * @return the size
     */
    int getSize(int queueIndex) {
        return sizeOfQ.get(queueIndex);
    }
}
