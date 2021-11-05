/*
 * (C) Copyright 2018-2021, by Timofey Chudakov and Contributors.
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

import org.jgrapht.alg.util.*;

import java.util.*;
import java.util.function.*;

/**
 * {@code DoublyLinkedList} implements a doubly linked {@link List} data structure, that exposes its
 * {@link ListNode ListNodes} where the data is stored in.
 * <p>
 * An element holding {@code ListNode} can be removed or added to a {@code DoublyLinkedList} in
 * constant time O(1). Other methods that operate on {@code ListNodes} directly also have constant
 * runtime. This is also the case for methods that operate on the first(head) and last(tail) node or
 * element. Random access methods have a runtime O(n) that is linearly dependent on the size of the
 * {@code DoublyLinkedList}.
 * </p>
 * <p>
 * A {@code DoublyLinkedList} supports {@code null} elements but does not support
 * {@code null ListNodes}. This class is not thread safe and needs to be synchronized externally if
 * modified by concurrent threads.
 * </p>
 * <p>
 * The iterators over this list have a <i>fail-fast</i> behavior meaning that they throw a
 * {@link ConcurrentModificationException} after they detect a structural modification of the list,
 * that they're not responsible for.
 * </p>
 * <p>
 * This class is similar to {@link LinkedList}. The general difference is that the {@code ListNodes}
 * of this {@code List} are accessible and can be removed or added directly. To ensure the integrity
 * of the {@code List} nodes of this List have a reference to the List they belong to. This
 * increases the memory occupied by this list implementation compared to {@code LinkedList} for the
 * same elements. Instances of {@code LinkedList.Node} have three references each (the element, next
 * and previous), instances of {@code DoublyLinkedList.ListNode} have four (the element, next,
 * previous and the list).
 * </p>
 *
 * @param <E> the list element type
 * @author Timofey Chudakov
 * @author Hannes Wellmann
 */
public class DoublyLinkedList<E>
    extends
    AbstractSequentialList<E>
    implements
    Deque<E>
{
    /** The first element of the list, {@code null} if this list is empty. */
    private ListNodeImpl<E> head = null;
    private int size;

    private ListNodeImpl<E> tail()
    {
        return head.prev;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return head == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        if (!isEmpty()) {
            ListNodeImpl<E> node = head;
            do {
                ListNodeImpl<E> next = node.next;
                boolean removed = removeListNode(node); // clears all links of removed node
                assert removed;
                node = next;
            } while (node != head);

            head = null;
            assert size == 0;
        }
    }

    // internal modification methods

    /**
     * Adds the given {@link ListNode} to this {@code List}.
     * <p>
     * Sets the {@code list} reference of {@code node} to this list, increases this lists
     * {@code size} and {@code modcount} by one.
     * </p>
     * 
     * @param node the node to add to this list
     * @throws IllegalArgumentException if {@code node} is already contained in this or another
     *         {@code DoublyLinkedList}
     */
    private void addListNode(ListNodeImpl<E> node)
    { // call this before any modification of this list is done
        if (node.list != null) {
            String list = (node.list == this) ? "this" : "other";
            throw new IllegalArgumentException(
                "Node <" + node + "> already contained in " + list + " list");
        }
        node.list = this;
        size++;
        modCount++;
    }

    /**
     * Atomically moves all {@link ListNode ListNodes} from {@code list} to this list as if each
     * node was removed with {@link #removeListNode(ListNodeImpl)} from {@code list} and
     * subsequently added to this list by {@link #addListNode(ListNodeImpl)}.
     */
    private void moveAllListNodes(DoublyLinkedList<E> list)
    { // call this before any modification of this list is done

        for (ListNodeIteratorImpl it = list.new ListNodeIteratorImpl(0); it.hasNext();) {
            ListNodeImpl<E> node = it.nextNode();
            assert node.list == list;
            node.list = this;
        }
        size += list.size;
        list.size = 0;
        modCount++;
        list.modCount++;
    }

    /**
     * Removes the given {@link ListNode} from this {@code List}, if it is contained in this
     * {@code List}.
     * <p>
     * If {@code node} is contained in this list, sets the {@code list}, {@code next} and
     * {@code prev} reference of {@code node} to {@code null} decreases this list's {@code size} and
     * increases the {@code modcount} by one.
     * </p>
     * 
     * @param node the node to remove from this list
     * @return true if {@code node} was removed from this list, else false
     */
    private boolean removeListNode(ListNodeImpl<E> node)
    { // call this before any modification of this list is done
        if (node.list == this) {

            node.list = null;
            node.next = null;
            node.prev = null;

            size--;
            modCount++;
            return true;
        }
        return false;
    }

    /**
     * Establishes the links between the given {@link ListNodeImpl nodes} in such a way that the
     * {@code predecessor} is linked before the {@code successor}.
     * 
     * @param predecessor the first node linked before the other
     * @param successor the second node linked after the other
     */
    private void link(ListNodeImpl<E> predecessor, ListNodeImpl<E> successor)
    {
        predecessor.next = successor;
        successor.prev = predecessor;
    }

    /** Insert non null {@code node} before non null {@code successor} into the list. */
    private void linkBefore(ListNodeImpl<E> node, ListNodeImpl<E> successor)
    {
        addListNode(node);
        link(successor.prev, node);
        link(node, successor);
    }

    /** Insert non null {@code node} as last node into the list. */
    private void linkLast(ListNodeImpl<E> node)
    {
        if (isEmpty()) { // node will be the first and only one
            addListNode(node);
            link(node, node); // self link
            head = node;
        } else {
            linkBefore(node, head);
        }
    }

    /** Insert non null {@code list} before node at {@code index} into the list. */
    private void linkListIntoThisBefore(int index, DoublyLinkedList<E> list)
    {
        int previousSize = size;
        moveAllListNodes(list);

        // link list's node into this list
        if (previousSize == 0) {
            head = list.head; // head and tail already linked together
        } else {
            ListNodeImpl<E> refNode = (index == previousSize) ? head : getNodeAt(index);

            ListNodeImpl<E> listTail = list.tail();
            link(refNode.prev, list.head); // changes list.tail()
            link(listTail, refNode);

            if (index == 0) {
                head = list.head;
            }
        }
        // clear list but do not call list.clear(), since their nodes are still used
        list.head = null;
    }

    /** Remove the non null {@code node} from the list. */
    private boolean unlink(ListNodeImpl<E> node)
    {
        ListNodeImpl<E> prev = node.prev;
        ListNodeImpl<E> next = node.next;
        if (removeListNode(node)) { // clears prev and next of node
            if (size == 0) {
                head = null;
            } else {
                // list is circular, don't have to worry about null values
                link(prev, next);

                if (head == node) {
                    head = next;
                }
            }
            return true;
        }
        return false;
    }

    // ----------------------------------------------------------------------------
    // public modification and access methods

    // ListNode methods:
    // Base methods to access, add and remove nodes to/from this list.
    // Used by all public methods if possible

    /**
     * Inserts the specified {@link ListNode node} at the specified position in this list.
     * <p>
     * This method has a linear runtime complexity O(n) that depends linearly on the distance of the
     * index to the nearest end. Adding {@code node} as first or last takes only constant time O(1).
     * </p>
     * 
     * @param index index at which the specified {@code node} is to be inserted
     * @param node the node to add
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index > size()})
     * @throws IllegalArgumentException if {@code node} is already part of this or another
     *         {@code DoublyLinkedList}
     * @throws NullPointerException if {@code node} is {@code null}
     */
    public void addNode(int index, ListNode<E> node)
    {
        ListNodeImpl<E> nodeImpl = (ListNodeImpl<E>) node;
        if (index == size) { // also true if this is empty
            linkLast(nodeImpl);
        } else {
            ListNodeImpl<E> successor = index == 0 ? head : getNodeAt(index);
            linkBefore(nodeImpl, successor);
            if (head == successor) {
                head = nodeImpl;
            }
        }
    }

    /**
     * Inserts the specified {@link ListNode node} at the front of this list.
     * <p>
     * This method has constant runtime complexity O(1).
     * </p>
     * 
     * @param node the node to add
     * @throws IllegalArgumentException if {@code node} is already part of this or another
     *         {@code DoublyLinkedList}
     * @throws NullPointerException if {@code node} is {@code null}
     */
    public void addNodeFirst(ListNode<E> node)
    {
        addNode(0, node);
    }

    /**
     * Inserts the specified {@link ListNode node} at the end of this list.
     * <p>
     * This method has constant runtime complexity O(1).
     * </p>
     * 
     * @param node the node to add
     * @throws IllegalArgumentException if {@code node} is already part of this or another
     *         {@code DoublyLinkedList}
     * @throws NullPointerException if {@code node} is {@code null}
     */
    public void addNodeLast(ListNode<E> node)
    {
        addNode(size, node);
    }

    /**
     * Inserts the specified {@link ListNode node} before the specified {@code successor} in this
     * list.
     * <p>
     * This method has constant runtime complexity O(1).
     * </p>
     * 
     * @param node the node to add
     * @param successor {@code ListNode} before which the {@code node} is inserted
     * @throws IllegalArgumentException if {@code node} is already contained in this or another
     *         {@code DoublyLinkedList} or {@code successor} is not contained in this list
     * @throws NullPointerException if {@code successor} or {@code node} is {@code null}
     */
    public void addNodeBefore(ListNode<E> node, ListNode<E> successor)
    {
        ListNodeImpl<E> successorImpl = (ListNodeImpl<E>) successor;
        ListNodeImpl<E> nodeImpl = (ListNodeImpl<E>) node;

        if (successorImpl.list != this) {
            throw new IllegalArgumentException("Node <" + successorImpl + "> not in this list");
        }
        linkBefore(nodeImpl, successorImpl);
        if (head == successorImpl) {
            head = nodeImpl;
        }
    }

    /**
     * Returns the first {@link ListNode node} of this list.
     * <p>
     * This method has constant runtime complexity O(1).
     * </p>
     * 
     * @return the first {@code ListNode} of this list
     * @throws NoSuchElementException if this list is empty
     */
    public ListNode<E> getFirstNode()
    {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return head;
    }

    /**
     * Returns the last {@link ListNode node} of this list.
     * <p>
     * This method has constant runtime complexity O(1).
     * </p>
     * 
     * @return the last {@code ListNode} of this list
     * @throws NoSuchElementException if this list is empty
     */
    public ListNode<E> getLastNode()
    {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return tail();
    }

    /**
     * Returns the {@link ListNode node} at the specified position in this list.
     * <p>
     * This method has linear runtime complexity O(n).
     * </p>
     * 
     * @param index index of the {@code ListNode} to return
     * @return the {@code ListNode} at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    public ListNode<E> getNode(int index)
    {
        return getNodeAt(index);
    }

    /**
     * Returns the {@link ListNodeImpl node} at the specified position in this list.
     * 
     * @param index index of the {@code ListNodeImpl} to return
     * @return the {@code ListNode} at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    private ListNodeImpl<E> getNodeAt(int index)
    {
        if (index < 0 || size <= index) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        ListNodeImpl<E> node;
        if (index < size / 2) {
            node = head;
            for (int i = 0; i < index; i++) {
                node = node.next;
            }
        } else {
            node = tail();
            for (int i = size - 1; index < i; i--) {
                node = node.prev;
            }
        }
        return node;
    }

    /**
     * Returns the index of the specified {@link ListNode node} in this list, or -1 if this list
     * does not contain the {@code node}.
     * <p>
     * More formally, returns the index {@code i} such that {@code node == getNode(i)}, or -1 if
     * there is no such index. Because a {@code ListNode} is contained in at most one list exactly
     * once, the returned index (if not -1) is the only occurrence of that {@code node}.
     * </p>
     * <p>
     * This method has linear runtime complexity O(n) to find {@code node} but returns in constant
     * time O(1) if {@code node} is not {@link #containsNode(ListNode) contained} in this list.
     * </p>
     * 
     * @param node the node to search for
     * @return the index of the specified {@code node} in this list, or -1 if this list does not
     *         contain {@code node}
     * @throws NullPointerException if {@code node} is {@code null}
     */
    public int indexOfNode(ListNode<E> node)
    {
        if (!containsNode(node)) {
            return -1;
        }
        ListNodeImpl<E> current = head;
        for (int i = 0; i < size; i++) {
            if (current == node) {
                return i;
            }
            current = current.next;
        }
        // should never happen:
        throw new IllegalStateException("Node contained in list not found: " + node);
    }

    /**
     * Returns true if this {@code DoublyLinkedList} contains the specified {@link ListNode}.
     * <p>
     * This method has constant runtime complexity O(1).
     * </p>
     * 
     * @param node the node whose presence in this {@code DoublyLinkedList} is to be tested
     * @return true if this {@code DoublyLinkedList} contains the {@link ListNode}
     * @throws NullPointerException if {@code node} is {@code null}
     */
    public boolean containsNode(ListNode<E> node)
    {
        return ((ListNodeImpl<E>) node).list == this;
    }

    /**
     * Removes the {@link ListNode node} from this list. Returns true if {@code node} was in this
     * list and is now removed. If {@code node} is not contained in this list, the list is left
     * unchanged.
     * <p>
     * This method has constant runtime complexity O(1).
     * </p>
     *
     * @param node the node to remove from this list
     * @return true if node was removed from this list
     * @throws NullPointerException if {@code node} is {@code null}
     */
    public boolean removeNode(ListNode<E> node)
    {
        return unlink((ListNodeImpl<E>) node);
    }

    /**
     * Returns the first {@link ListNode node} holding the specified {@code element} in this list.
     * More formally, returns the first {@code ListNode} such that
     * {@code Objects.equals(element, node.getValue())}, or {@code null} if there is no such node.
     * <p>
     * This method has linear runtime complexity O(n).
     * </p>
     * 
     * @param element the element whose {@code ListNode} is to return
     * @return the first {@code ListNode} holding the {@code element} or null if no node was found
     */
    public ListNode<E> nodeOf(Object element)
    {
        return searchNode(() -> head, n -> n.next, element).getFirst();
    }

    /**
     * Returns the last {@link ListNode node} holding the specified {@code element} in this list.
     * More formally, returns the last {@code ListNode} such that
     * {@code Objects.equals(element, node.getValue())}, or {@code null} if there is no such node.
     * <p>
     * This method has linear runtime complexity O(n).
     * </p>
     * 
     * @param element the element whose {@code ListNode} is to return
     * @return the last {@code ListNode} holding the {@code element} or null if no node was found
     */
    public ListNode<E> lastNodeOf(Object element)
    {
        return searchNode(this::tail, n -> n.prev, element).getFirst();
    }

    /**
     * Returns a {@link Pair} of the first encountered {@link ListNode} in this list, whose
     * {@code value} is equal to the given {@code element}, and its index. Or if this list does not
     * contain such node a Pair of {@code null} and {@code -1};
     * <p>
     * The search starts at the node supplied by {@code first} and advances in the direction induced
     * by the specified {@code next} operator.
     * </p>
     * 
     * @param first supplier of the first node to check if this list is not empty
     * @param next {@code Function} to get from the current node the next node to check
     * @param element the element for that the first node with equal value is searched.
     * @return a {@link Pair} of the first encountered {@code ListNode} holding a {@code value}
     *         equal to {@code element} and its index, or if no such node was found a
     *         {@code Pair.of(null, -1)}
     */
    private Pair<ListNodeImpl<E>, Integer> searchNode(
        Supplier<ListNodeImpl<E>> first, UnaryOperator<ListNodeImpl<E>> next, Object element)
    {
        if (!isEmpty()) {
            int index = 0;
            ListNodeImpl<E> firstNode = first.get();
            ListNodeImpl<E> node = firstNode;
            do {
                if (Objects.equals(node.value, element)) {
                    return Pair.of(node, index);
                }
                index++;
                node = next.apply(node);
            } while (node != firstNode);
        }
        return Pair.of(null, -1);
    }

    /**
     * Inserts the specified element at the front of this list. Returns the {@link ListNode}
     * allocated to store the {@code value}. The returned {@code ListNode} is the new head of the
     * list.
     * <p>
     * This method is equivalent to {@link #addFirst(Object)} but returns the allocated
     * {@code ListNode}.
     * </p>
     * 
     * @param element the element to add
     * @return the {@code ListNode} allocated to store the {@code value}
     */
    public ListNode<E> addElementFirst(E element)
    {
        ListNode<E> node = new ListNodeImpl<>(element);
        addNode(0, node);
        return node;
    }

    /**
     * Inserts the specified element at the end of this list. Returns the {@link ListNode} allocated
     * to store the {@code value}. The returned {@code ListNode} is the new tail of the list.
     * <p>
     * This method is equivalent to {@link #addLast(Object)} but returns the allocated
     * {@code ListNode}.
     * </p>
     * 
     * @param element the element to add
     * @return the {@code ListNode} allocated to store the {@code value}
     */
    public ListNode<E> addElementLast(E element)
    {
        ListNode<E> node = new ListNodeImpl<>(element);
        addNode(size, node);
        return node;
    }

    /**
     * Inserts the specified element before the specified {@link ListNode successor} in this list.
     * Returns the {@code ListNode} allocated to store the {@code value}.
     *
     * @param successor {@code ListNode} before which the node holding {@code value} is inserted
     * @param element the element to add
     * @return the {@code ListNode} allocated to store the {@code value}
     * @throws IllegalArgumentException if {@code successor} is not contained in this list
     * @throws NullPointerException if {@code successor} is {@code null}
     */
    public ListNode<E> addElementBeforeNode(ListNode<E> successor, E element)
    {
        ListNode<E> node = new ListNodeImpl<>(element);
        addNodeBefore(node, successor);
        return node;
    }

    // List methods (shortcut for most commonly used methods to avoid iterator creation)

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int index, E element)
    {
        if (index == size) { // also true if this is empty
            addElementLast(element);
        } else {
            addElementBeforeNode(getNode(index), element);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(int index)
    {
        return getNodeAt(index).value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E remove(int index)
    {
        ListNode<E> node = getNode(index);
        removeNode(node);
        return node.getValue();
    }

    // Deque methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFirst(E e)
    {
        addElementFirst(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLast(E e)
    {
        addElementLast(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean offerFirst(E e)
    {
        addElementFirst(e);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean offerLast(E e)
    {
        addElementLast(e);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E removeFirst()
    {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        ListNode<E> node = head;
        removeNode(node); // changes head
        return node.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E removeLast()
    {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }

        ListNode<E> node = tail();
        removeNode(node); // changes tail
        return node.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E pollFirst()
    {
        if (isEmpty()) {
            return null;
        }
        ListNode<E> node = head;
        removeNode(node); // changes head
        return node.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E pollLast()
    {
        if (isEmpty()) {
            return null;
        }
        ListNode<E> node = tail();
        removeNode(node); // changes tail()
        return node.getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getFirst()
    {
        return getFirstNode().getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getLast()
    {
        return getLastNode().getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E peekFirst()
    {
        return isEmpty() ? null : getFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E peekLast()
    {
        return isEmpty() ? null : getLast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeFirstOccurrence(Object o)
    {
        ListNode<E> node = nodeOf(o);
        if (node != null) {
            removeNode(node);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeLastOccurrence(Object o)
    {
        ListNode<E> node = lastNodeOf(o);
        if (node != null) {
            removeNode(node);
            return true;
        }
        return false;
    }

    // Queue methods

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean offer(E e)
    {
        return offerLast(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E remove()
    {
        return removeFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E poll()
    {
        return pollFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E element()
    {
        return getFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E peek()
    {
        return peekFirst();
    }

    // Stack methods

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(E e)
    {
        addFirst(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E pop()
    {
        return removeFirst();
    }

    // special bulk methods

    /**
     * Inverts the list. For instance, calling this method on the list $(a,b,c,\dots,x,y,z)$ will
     * result in the list $(z,y,x,\dots,c,b,a)$. This method does only pointer manipulation, meaning
     * that all the list nodes allocated for the previously added elements are valid after this
     * method finishes.
     */
    public void invert()
    {
        if (size < 2) {
            return;
        }
        ListNodeImpl<E> newHead = tail();
        ListNodeImpl<E> current = head;
        do {
            ListNodeImpl<E> next = current.next;

            current.next = current.prev;
            current.prev = next;

            current = next;
        } while (current != head);
        head = newHead;
        ++modCount;
    }

    /**
     * Moves all {@link ListNode ListNodes} of the given {@code sourceList} to this list and inserts
     * them all before the node previously at the given position. All the {@code nodes} of
     * {@code movedList} are moved to this list. When this method terminates this list contains all
     * nodes of {@code movedList} and {@code movedList} is empty.
     *
     * @param index index of the first element of {@code list} in this {@code list} after it was
     *        added
     * @param movedList the {@code DoublyLinkedList} to move to this one
     * @throws NullPointerException if {@code movedList} is {@code null}
     */
    public void moveFrom(int index, DoublyLinkedList<E> movedList)
    {
        linkListIntoThisBefore(index, movedList);
    }

    /**
     * Appends the {@code movedList} to the end of this list. All the elements from
     * {@code movedList} are transferred to this list, i.e. the {@code list} is empty after calling
     * this method.
     *
     * @param movedList the {@code DoublyLinkedList} to append to this one
     * @throws NullPointerException if {@code movedList} is {@code null}
     */
    public void append(DoublyLinkedList<E> movedList)
    {
        moveFrom(size, movedList);
    }

    /**
     * Prepends the {@code movedList} to the beginning of this list. All the elements from
     * {@code movedList} are transferred to this list, i.e. the {@code movedList} is empty after
     * calling this method.
     *
     * @param movedList the {@code DoublyLinkedList} to prepend to this one
     * @throws NullPointerException if {@code movedList} is {@code null}
     */
    public void prepend(DoublyLinkedList<E> movedList)
    {
        moveFrom(0, movedList);
    }

    // ----------------------------------------------------------------------------
    // (List)Iterators

    /**
     * Returns a {@link NodeIterator} that starts at the first {@link ListNode} of this list that is
     * equal to the specified {@code firstElement}, iterates in forward direction over the end of
     * this list until the first node.
     * <p>
     * The first call to {@link NodeIterator#nextNode()} returns the first {@code node} that holds a
     * value such that {@code Objects.equals(node.getValue, firstElement)} returns {@code true}. The
     * returned {@code NodeIterator} iterates in forward direction returning the respective next
     * element in subsequent calls to {@code next(Node)}. The returned iterator ignores the actual
     * bounds of this {@code DoublyLinkedList} and iterates until the node before the first one is
     * reached. Its {@link NodeIterator#hasNext() hasNext()} returns {@code false} if the next node
     * would be the first one.
     * </p>
     * 
     * @param firstElement the element equal to the first {@code next()}
     * @return a circular {@code NodeIterator} iterating forward from {@code firstElement}
     */
    public NodeIterator<E> circularIterator(E firstElement)
    {
        ListNodeImpl<E> startNode = (ListNodeImpl<E>) nodeOf(firstElement);
        if (startNode == null) {
            throw new NoSuchElementException();
        }
        return new ListNodeIteratorImpl(0, startNode);
    }

    /**
     * Returns a {@link NodeIterator} that starts at the first {@link ListNode} of this list that is
     * equal to the specified {@code firstElement}, iterates in reverse direction over the end of
     * this list until the first node.
     * <p>
     * The first call to {@link NodeIterator#nextNode()} returns the first {@code node} that holds a
     * value such that {@code Objects.equals(node.getValue, firstElement)} returns {@code true}. The
     * returned {@code NodeIterator} iterates in reverse direction returning the respective previous
     * element in subsequent calls to {@code next(Node)}. The returned iterator ignores the actual
     * bounds of this {@code DoublyLinkedList} and iterates until the node before the first one is
     * reached. Its {@link NodeIterator#hasNext() hasNext()} returns {@code false} if the next node
     * would be the first one.
     * </p>
     * 
     * @param firstElement the element equal to the first {@code next()}
     * @return a circular {@code NodeIterator} iterating backwards from {@code firstElement}
     */
    public NodeIterator<E> reverseCircularIterator(E firstElement)
    {
        ListNodeImpl<E> startNode = (ListNodeImpl<E>) nodeOf(firstElement);
        if (startNode == null) {
            throw new NoSuchElementException();
        }
        return reverseIterator(new ListNodeIteratorImpl(size, startNode.next));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeIterator<E> descendingIterator()
    {
        return reverseIterator(listIterator(size));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeIterator<E> iterator()
    {
        return listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListNodeIterator<E> listIterator()
    {
        return listIterator(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListNodeIterator<E> listIterator(int index)
    {
        return new ListNodeIteratorImpl(index);
    }

    /**
     * Returns a {@link ListNodeIterator} over the elements in this list (in proper sequence)
     * starting with the first {@link ListNode} whose value is equal to the specified
     * {@code element}.
     *
     * @param element the first element to be returned from the list iterator (by a call to the
     *        {@code next} method)
     * @return a list iterator over the elements in this list (in proper sequence)
     * @throws NoSuchElementException if {@code element} is not in the list
     */
    public ListNodeIterator<E> listIterator(E element)
    {
        Pair<ListNodeImpl<E>, Integer> startPair = searchNode(() -> head, n -> n.next, element);
        ListNodeImpl<E> startNode = startPair.getFirst();
        int startIndex = startPair.getSecond();
        if (startNode == null) {
            throw new NoSuchElementException();
        }
        return new ListNodeIteratorImpl(startIndex, startNode);
    }

    /**
     * An extension of the {@link Iterator} interface for {@link DoublyLinkedList DoublyLinkedLists}
     * exposing their {@link ListNode ListNodes}.
     *
     * @param <E> the list element type
     */
    public interface NodeIterator<E>
        extends
        Iterator<E>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        default E next()
        {
            return nextNode().getValue();
        }

        /**
         * Returns the next {@link ListNode} in the list and advances the cursor position.
         *
         * @return the next {@code ListNode}
         * @see ListIterator#next()
         */
        ListNode<E> nextNode();

    }

    /**
     * An extension of the {@link ListIterator} interface for {@link DoublyLinkedList
     * DoublyLinkedLists} exposing their {@link ListNode ListNodes}.
     *
     * @param <E> the list element type
     */
    public interface ListNodeIterator<E>
        extends
        ListIterator<E>,
        NodeIterator<E>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        default E next()
        {
            return nextNode().getValue();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default E previous()
        {
            return previousNode().getValue();
        }

        /**
         * Returns the previous {@link ListNode} in the list and moves the cursor position
         * backwards.
         *
         * @return the previous {@code ListNode}
         * @see ListIterator#previous()
         */
        ListNode<E> previousNode();

    }

    /**
     * An implementation of the {@link DoublyLinkedList.ListNodeIterator} interface.
     */
    private class ListNodeIteratorImpl
        implements
        ListNodeIterator<E>
    {
        /** Index in this list of the ListNode returned next. */
        private int nextIndex;
        /** ListNode this iterator will return next. Null if this list is empty. */
        private ListNodeImpl<E> next;
        /** ListNode this iterator returned last. */
        private ListNodeImpl<E> last = null;

        /**
         * The number of modifications the list have had at the moment when this iterator was
         * created
         */
        private int expectedModCount = modCount;

        private ListNodeIteratorImpl(int startIndex)
        {
            this.nextIndex = startIndex;
            if (startIndex == size) {
                this.next = isEmpty() ? null : head;
            } else {
                this.next = getNodeAt(startIndex);
            }
        }

        private ListNodeIteratorImpl(int startIndex, ListNodeImpl<E> startNode)
        {
            this.nextIndex = startIndex;
            this.next = startNode;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext()
        {
            return nextIndex < size;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasPrevious()
        {
            return nextIndex > 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int nextIndex()
        {
            return nextIndex;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int previousIndex()
        {
            return nextIndex - 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ListNodeImpl<E> nextNode()
        {
            checkForComodification();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            last = next;
            next = next.next;
            nextIndex++;
            return last;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ListNode<E> previousNode()
        {
            checkForComodification();
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            last = next = next.prev;
            nextIndex--;
            return last;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void add(E e)
        {
            checkForComodification();

            if (nextIndex == size) {
                addElementLast(e); // sets head to new node of e if was empty
                if (size == 1) { // was empty
                    next = head; // jump over head threshold, so cursor is at the end
                }
            } else {
                addElementBeforeNode(next, e);
            }
            last = null;
            nextIndex++;
            expectedModCount++;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void set(E e)
        {
            if (last == null) {
                throw new IllegalStateException();
            }
            checkForComodification();
            // replace node returned last with a new node holding e

            ListNode<E> nextNode = last.next;
            boolean wasLast = last == tail();
            removeNode(last);
            if (wasLast) { // or the sole node
                last = (ListNodeImpl<E>) addElementLast(e);
            } else {
                last = (ListNodeImpl<E>) addElementBeforeNode(nextNode, e);
            }
            expectedModCount += 2; // because of unlink and add
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove()
        {
            if (last == null) {
                throw new IllegalStateException();
            }
            checkForComodification();

            ListNodeImpl<E> lastsNext = last.next;
            removeNode(last);
            if (next == last) { // previousNode() called before
                // removed element after cursor (which would have been next)
                next = lastsNext;
            } else { // nextNode() called before
                // removed element before cursor (next is unaffected but the index decreases)
                nextIndex--;
            }
            last = null;
            expectedModCount++;
        }

        /**
         * Verifies that the list structure hasn't been changed since the iteration started
         */
        private void checkForComodification()
        {
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link NodeIterator} that iterates in reverse order, assuming the cursor of the
     * specified {@link ListNodeIterator} is behind the tail of the list.
     */
    private static <E> NodeIterator<E> reverseIterator(ListNodeIterator<E> listIterator)
    {
        return new NodeIterator<E>()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext()
            {
                return listIterator.hasPrevious();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public ListNode<E> nextNode()
            {
                return listIterator.previousNode();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void remove()
            {
                listIterator.remove();
            }
        };
    }

    /**
     * Container for the elements stored in a {@link DoublyLinkedList}.
     * <p>
     * A {@link ListNode} is either contained exactly once in exactly one {@code DoublyLinkedList}
     * or contained in no {@code DoublyLinkedList}.
     * </p>
     * 
     * @param <V> the type of the element stored in this node
     */
    public interface ListNode<V>
    {
        /**
         * Returns the immutable value this {@code ListNode} contains.
         *
         * @return the value this list node contains
         */
        V getValue();

        /**
         * Returns the next node in the list structure with respect to this node
         *
         * @return the next node in the list structure with respect to this node
         */
        ListNode<V> getNext();

        /**
         * Returns the previous node in the list structure with respect to this node
         *
         * @return the previous node in the list structure with respect to this node
         */
        ListNode<V> getPrev();
    }

    /**
     * The default {@link ListNode} implementation that enables checks and enforcement of a single
     * container list policy.
     */
    private static class ListNodeImpl<V>
        implements
        ListNode<V>
    {
        private final V value;
        private DoublyLinkedList<V> list = null;
        private ListNodeImpl<V> next = null;
        private ListNodeImpl<V> prev = null;

        /**
         * Creates new list node
         *
         * @param value the value this list node stores
         */
        ListNodeImpl(V value)
        {
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            if (list == null) {
                return " - " + value + " - "; // not in a list
            } else {
                return prev.value + " -> " + value + " -> " + next.value;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V getValue()
        {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ListNodeImpl<V> getNext()
        {
            return next;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ListNodeImpl<V> getPrev()
        {
            return prev;
        }
    }
}
