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

import java.util.Iterator;

/**
 * A queue on a bounded set of integers that is splittable. The order of the elements cannot be
 * changed after instantiation. An element cannot be added after instantiation.
 *
 * @author Daniel Mock
 */
public class SubSplitQueue
        implements
        Iterable<Integer> {
    final public SuperSplitQueue parent;
    final private int ownIndex;

    /**
     * Returns a new SubSplitQueue initialized with its index and its parent SuperSplitQueue Not to
     * be used externally.
     *
     * @param ownIndex index for this queue
     * @param parent   parent
     */
    SubSplitQueue(int ownIndex, SuperSplitQueue parent) {
        this.ownIndex = ownIndex;
        this.parent = parent;
    }

    /**
     * Returns a SubSplitQueue with elements from 0 to universeSize - 1 Runs in O(universeSize) The
     * order is the natural order.
     *
     * @param universeSize the size of the universe
     * @return new SubSplitQueue
     */
    public static SubSplitQueue subSplitQueueFactory(int universeSize) {
        return SuperSplitQueue.instantiate(universeSize);
    }

    /**
     * Returns a SubSplitQueue with elements from ordering. SortedElements has to a subset of {0,
     * ..., universeSize - 1}, sorted by desired order, and every entry should be unique
     *
     * @param universeSize the size of the universe
     * @param ordering     the elements to be stored (subject to the above-mentioned restrictions)
     * @return new SubSplitQueue
     */
    public static SubSplitQueue subSplitQueueFactory(int universeSize, int[] ordering) {
        return SuperSplitQueue.instantiate(universeSize, ordering);
    }

    /**
     * Returns its own index.
     *
     * @return index
     */
    int getOwnIndex() {
        return ownIndex;
    }

    /**
     * Returns the lowest element in the queue without side effects. Throws NoSuchElementException
     * if it is empty. Runs in constant time.
     *
     * @return the lowest element in the queue
     */
    public int peek() {
        return parent.peek(ownIndex);
    }

    /**
     * Returns and removes the lowest element in the queue. Throws NoSuchElementException if it is
     * empty. Runs in constant time.
     *
     * @return the lowest element in the queue
     */
    public int poll() {
        return parent.poll(ownIndex);
    }

    /**
     * Returns true iff the queue does not contain any elements. Runs in constant time.
     *
     * @return whether it is empty
     */
    public boolean isEmpty() {
        return parent.isEmpty(ownIndex);
    }

    /**
     * Removes the given element and keeps the data structure consistent. Runs in constant time.
     *
     * @param element the element to be removed
     */
    public void remove(int element) {
        parent.remove(element, ownIndex);
    }

    /**
     * Splits this queue in two: Elements in splitters are removed and transferred to a new queue,
     * the others stay. Run time: O(splitters size).
     *
     * @param splitters The elements which have to be transferred to the new returned SubSplitQueue.
     *                  They have to be sorted by the initial order.
     * @return a new SplitQueue holding the split off elements
     */
    public SubSplitQueue split(int[] splitters) {
        return parent.split(splitters, ownIndex);
    }

    /**
     * Returns true iff the queue contains element. Runs in constant time.
     *
     * @param element the element to be checked
     * @return whether it contains element
     */
    public boolean contains(int element) {
        return parent.contains(element, ownIndex);
    }

    /**
     * Iterates over the contained elements. Every iteration takes constant time.
     *
     * @return the iterator
     */
    @Override
    public Iterator<Integer> iterator() {
        return parent.iterator(ownIndex);
    }

    /**
     * Returns the elements contained in this object as Array, ordered by the initial order. Runs in
     * O(queue size).
     *
     * @return an array containing the elements of this queue
     */
    public int[] asArray() {
        return parent.asArray(ownIndex);
    }

    /**
     * Returns the number of elements in this object. Runs in constant time.
     *
     * @return the amount of contained elements
     */
    public int getSize() {
        return parent.getSize(ownIndex);
    }
}
