/*
 * (C) Copyright 2018-2018, by Timofey Chudakov and Contributors.
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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This is the implementation of the doubly linked list data structure, which gives access to the internal
 * list nodes the data is stored in. The primary goal of this is to be able to delete an element from this
 * list in $\mathcal{O}(1)$ via the list node it is stored in.
 * <p>
 * The iterators over this list have a <i>fail-fast</i> behaviour meaning that they throw a
 * {@link ConcurrentModificationException} after they detect a structural modification of the list,
 * that they're not responsible for.
 *
 * @param <E> the list element type
 * @author Timofey Chudakov
 */
public class DoublyLinkedList<E> implements Iterable<E> {
    /**
     * The first element of the list
     */
    private ListNode<E> head;
    /**
     * The size of the list
     */
    private int size;
    /**
     * The number of modifications applied to this list. This value is used to exhibit a fail-fast
     * behavior during the iteration over the list when its structure is changed concurrently
     */
    private int modCount;

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }

    /**
     * Returns a list iterator starting from the beginning of this list
     *
     * @return a list iterator starting from the beginning of this list
     */
    public ListIterator<E> listIterator() {
        return new ListIteratorImpl(head);
    }

    /**
     * Returns an iterator over this list, which traverses the list in the reverse direction.
     * The returned iterator will iterate over the <i>entire</i> list, that is, the list is
     * treated as if it were cyclic. The iterator starts from {@code element}, and walks the
     * list backwards, while wrapping around at the beginning of the list. For instance,
     * for the list [1,2,3,4], {@code reverseIteratorFrom(3)} would return 3,2,1,4.
     *
     * This method throws {@link NoSuchElementException} in the case {@code element} doesn't
     * belong to this list. This method invokes {@link #getNode(Object) getNode} to find the
     * list node containing {@code element}.
     *
     * @param element an element to start an iteration from
     * @return an iterator over this list, which starts from the {@code element}
     */
    public Iterator<E> reverseIteratorFrom(E element) {
        ListNode<E> start = getNode(element);
        if (start == null) {
            throw new NoSuchElementException();
        }
        return new ListIteratorImpl(start, true);
    }

    /**
     * Returns true if this list is empty, false otherwise
     *
     * @return true if this list is empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of elements in this list
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Appends the specified value to the end of this list. This method
     * returns the list node the {@code value} is contained in
     *
     * @param value the value to append to this list
     * @return the list node, which contains the {@code value}
     */
    public ListNode<E> add(E value) {
        return addLast(value);
    }

    /**
     * Inverts the list. For instance, calling this method on the list $(a,b,c,\dots,x,y,z)$
     * will result in the list $(z,y,x,\dots,c,b,a)$. This method does only pointer manipulation,
     * meaning that all the list nodes allocated for the previously added elements are valid after
     * this method finishes.
     */
    public void invert() {
        if (size < 2) {
            return;
        }
        ListNode<E> newHead = head.prev;
        ListNode<E> current = head;
        do {
            ListNode<E> t = current.next;
            current.next = current.prev;
            current.prev = t;
            current = current.prev;
        } while (current != head);
        head = newHead;
        ++modCount;
    }

    /**
     * Appends the {@code list} to the end of this list. All the elements from {@code list}
     * are transferred to this list, i.e. the {@code list} is empty after calling this method.
     *
     * @param list the list to append
     */
    public void append(DoublyLinkedList<E> list) {
        if (!list.isEmpty()) {
            if (isEmpty()) {
                head = list.head;
            } else {
                linkBefore(head, list);
            }
            size += list.size;
            list.size = 0;
            list.head = null;
            ++modCount;
        }

    }

    /**
     * Prepends the {@code list} to the beginning of this list. All the elements from {@code list}
     * are transferred to this list, i.e. the {@code list} is empty after calling this method.
     *
     * @param list the list to prepend
     */
    public void prepend(DoublyLinkedList<E> list) {
        if (!list.isEmpty()) {
            if (!isEmpty()) {
                linkBefore(head, list);
            }
            head = list.head;
            size += list.size;

            list.size = 0;
            list.head = null;
            ++modCount;
        }
    }

    /**
     * Appends the {@code value} to the end of this list. Returns the list node allocated for storing
     * the {@code value}
     *
     * @param value the value to add
     * @return the list node allocated for storing the {@code value}
     */
    public ListNode<E> addLast(E value) {
        ListNode<E> result = new ListNode<>(value);
        if (isEmpty()) {
            insertFirst(result);
        } else {
            linkBefore(head, result);
        }
        ++size;
        ++modCount;
        return result;
    }

    /**
     * Adds the {@code value} to the beginning of this list. Returns the list node allocated for storing
     * the {@code value}. The returned value is the new head of the list.
     *
     * @param value the value to add
     * @return the list node allocated for storing the {@code value}
     */
    public ListNode<E> addFirst(E value) {
        ListNode<E> result = addLast(value);
        head = result;
        ++modCount;
        return result;
    }

    /**
     * Removes the {@code node} from this list. The running time of this method is $\mathcal{O}(1)$.
     * The behaviour of this method is undefined if the {@code node} doesn't belong to the list.
     *
     * @param node the node to remove from this list
     */
    public void remove(ListNode<E> node) {
        if (size == 1) {
            head = null;
        } else {
            if (node == head) {
                head = node.next;
            }
        }
        unlink(node);
        --size;
        ++modCount;
    }

    /**
     * Returns the first element in this list. If this list is empty, throws {@link NoSuchElementException}.
     *
     * @return the first element in this list.
     */
    public E getFirstElement() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return head.value;
    }

    /**
     * Returns the first element in this list. If this list is empty, throws {@link NoSuchElementException}
     *
     * @return the first element in this list.
     */
    public E getLastElement() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return head.prev.value;
    }

    /**
     * Finds and returns the list node allocate for the {@code element}. If this list doesn't contain
     * the {@code element}, returns {@code null}. The time complexity of this method is linear in the
     * size of the list under the assumption that the element comparison is done in constant time.
     * Otherwise, the complexity is $\mathcal{O}(n\cdot h)$, where $n$ is the size of the list and $h$
     * is the time needed to compare two elements stored in this list.
     *
     * @param element the element of this list to search the list node of.
     * @return the list node allocate for the {@code element}, or {@code null} if this list doesn't contain
     * the {@code element}
     */
    public ListNode<E> getNode(E element) {
        for (ListIterator<E> iterator = listIterator(); iterator.hasNext(); ) {
            ListNode<E> current = iterator.nextNode();
            if (current.value.equals(element)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Removes the {@code target} from the list structure
     *
     * @param target the list node to remove
     */
    private void unlink(ListNode<E> target) {
        // list is circular, don't have to worry about null values
        target.prev.next = target.next;
        target.next.prev = target.prev;

        target.next = target.prev = null;
    }

    /**
     * Links the {@code target} before the {@code node} in the list structure
     *
     * @param node   the list node from the list to link the {@code target} before
     * @param target the new list node to link before {@code target}
     */
    private void linkBefore(ListNode<E> node, ListNode<E> target) {
        target.next = node;
        target.prev = node.prev;
        node.prev.next = target;
        node.prev = target;
    }

    /**
     * Links the {@code list} before the {@code node}
     *
     * @param node the node to link the {@code list} before
     * @param list the sequence of nodes to link before {@code node}
     */
    private void linkBefore(ListNode<E> node, DoublyLinkedList<E> list) {
        ListNode<E> head = list.head;
        ListNode<E> last = head.prev;
        head.prev = node.prev;
        last.next = node;

        node.prev.next = head;
        node.prev = last;
    }

    /**
     * Makes the {@code target} the first list node in this list,
     *
     * @param target the new first element of the list
     */
    private void insertFirst(ListNode<E> target) {
        target.next = target.prev = target;
        head = target;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{ }";
        }
        StringBuilder builder = new StringBuilder("{");
        ListNode<E> current = head;
        while (current.next != head) {
            builder.append(current.value.toString()).append(", ");
            current = current.next;
        }
        builder.append(current.value.toString()).append("}");
        return builder.toString();
    }

    /**
     * An interface for the iterators over the {@link DoublyLinkedList}
     *
     * @param <E> the list element type
     */
    public interface ListIterator<E> extends Iterator<E> {
        /**
         * Returns the next list node
         *
         * @return the next list node
         */
        ListNode<E> nextNode();

        /**
         * {@inheritDoc}
         */
        @Override
        default E next() {
            return nextNode().value;
        }
    }

    /**
     * An implementation of the {@link DoublyLinkedList.ListIterator} interface.
     */
    public class ListIteratorImpl implements DoublyLinkedList.ListIterator<E> {
        /**
         * The list node this iterator has started from
         */
        ListNode<E> start;
        /**
         * The list node this iterator will return next. This value is {@code null} when
         * the traversal is finished.
         */
        ListNode<E> current;
        /**
         * Whether this iterator should move in the reverse direction
         */
        boolean reversed;
        /**
         * The number of modifications the list have had at the moment when this iterator was created
         */
        int expectedModCount;

        /**
         * Creates new list iterator, which starts from the {@code start} node
         *
         * @param start the node to start the traversal from
         */
        ListIteratorImpl(ListNode<E> start) {
            this(start, false);
        }

        ListIteratorImpl(ListNode<E> start, boolean reversed) {
            this.start = start;
            this.current = start;
            this.reversed = reversed;
            this.expectedModCount = modCount;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return current != null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ListNode<E> nextNode() {
            checkForComodification();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            ListNode<E> result = current;
            if (reversed) {
                current = current.prev;
            } else {
                current = current.next;
            }
            if (current == start) {
                current = null;
            }
            return result;
        }

        /**
         * Verifies that the list structure hasn't been changed since the iteration started
         */
        private void checkForComodification() {
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * The container to store the elements of the list in. This class can be used to perform
     * list operations in $\mathcal{O}(1)$ time
     *
     * @param <V> the list node element type
     */
    public static class ListNode<V> {
        private V value;
        private ListNode<V> next;
        private ListNode<V> prev;

        /**
         * Creates new list node
         *
         * @param value the value this list node stores
         */
        ListNode(V value) {
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("%s -> %s -> %s", prev.value, value, next.value);
        }

        /**
         * Returns the value this list node stores
         *
         * @return the value this list node stores
         */
        public V getValue() {
            return value;
        }
    }

}



