/*
 * (C) Copyright 2020-2023, by Hannes Wellmann and Contributors.
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

import org.jgrapht.util.DoublyLinkedList.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.abort;

/**
 * Tests for {@link DoublyLinkedList}.
 * 
 * @author Hannes Wellmann
 *
 */
public class DoublyLinkedListTest
{
    private static final int MAX_LIST_SIZE = 8;

    public static Object[][] getListSizes()
    {
        Object[][] parameterSets = new Object[MAX_LIST_SIZE + 1][];
        for (int size = 0; size < MAX_LIST_SIZE + 1; size++) {

            List<String> elements = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                elements.add("obj" + i);
            }
            if (size >= 6) { // make two elements equal
                elements.set(3, new String("obj2")); // create equal new String-object
            }
            parameterSets[size] = new Object[] { size, Collections.unmodifiableList(elements), createDoublyLinkedList(elements) };
        }
        return parameterSets;
    }

    // ------------------------------------------------------------------------
    // test cases

    /** Test for {@link DoublyLinkedList#isEmpty()}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testIsEmpty(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThat(list.isEmpty(), is(equalTo(size == 0)));
    }

    /** Test for {@link DoublyLinkedList#size()}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testSize(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThat(list.size(), is(equalTo(size)));
    }

    /** Test for {@link DoublyLinkedList#clear()}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testClear(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {

        List<ListNode<String>> allNodes = getListNodesOfList(list);

        list.clear();

        assertTrue(list.isEmpty());
        assertThat(list.size(), is(equalTo(0)));

        for (ListNode<String> listNode : allNodes) {
            assertFalse(list.containsNode(listNode));
        }
    }

    // test ListNode methods

    /** Test for {@link DoublyLinkedList#addNodeFirst(DoublyLinkedList.ListNode)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeFirst_freeNode_nodeAddedToList(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String element = "another";
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(0, element);

        ListNode<String> node = createFreeListNode(element);
        list.addNodeFirst(node);

        assertThat(list.getFirstNode(), is(equalTo(node)));
        assertTrue(list.containsNode(node));
        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeFirst_nodeInOtherList_IllegalArgumentException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IllegalArgumentException.class, () -> {
            ListNode<String> node = createListNodeInOtherList();

            list.addNodeFirst(node);
        });
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeFirst_nodeOfThisList_IllegalArgumentException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0)
            abort(); // skip for empty list
        assertThrows(IllegalArgumentException.class, () -> {
            ListNode<String> node = list.getNode(size / 2);

            list.addNodeFirst(node);
        });
    }

    /** Test for {@link DoublyLinkedList#addNodeLast(DoublyLinkedList.ListNode)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeLast_freeNode_nodeAddedToList(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String element = "another";
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(size, element);

        ListNode<String> node = createFreeListNode(element);
        list.addNodeLast(node);

        assertThat(list.getLastNode(), is(equalTo(node)));
        assertTrue(list.containsNode(node));
        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeLast_nodeInOtherList_IllegalArgumentException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IllegalArgumentException.class, () -> {
            ListNode<String> node = createListNodeInOtherList();

            list.addNodeLast(node);
        });
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeLast_nodeOfThisList_IllegalArgumentException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort(); // skip for empty list
        }
        assertThrows(IllegalArgumentException.class, () -> {
            ListNode<String> node = list.getNode(size / 2);

            list.addNodeLast(node);
        });
    }

    /** Test for {@link DoublyLinkedList#addNode(int, DoublyLinkedList.ListNode)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNode_freeNode_nodeAddedToList(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String element = "another";
        int index = size / 2;
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(index, element);

        ListNode<String> node = createFreeListNode(element);
        list.addNode(index, node);

        assertTrue(list.containsNode(node));
        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNode_nodeInOtherList_IllegalArgumentException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IllegalArgumentException.class, () -> {
            ListNode<String> node = createListNodeInOtherList();

            list.addNode(size / 2, node);
        });
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNode_nodeOfThisList_IllegalArgumentException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort(); // skip for empty list
        }
        assertThrows(IllegalArgumentException.class, () -> {
            ListNode<String> node = list.getLastNode();

            list.addNode(size / 2, node);
        });
    }

    /**
     * Test for
     * {@link DoublyLinkedList#addNodeBefore(DoublyLinkedList.ListNode, DoublyLinkedList.ListNode)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeBefore_freeNodeBeforeNodeInList_nodeAddedToList(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort();
        }
        String element = "another";
        int index = size / 2;
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(index, element);

        ListNode<String> node = createFreeListNode(element);
        ListNode<String> beforeNode = list.getNode(index);
        list.addNodeBefore(node, beforeNode);

        assertTrue(list.containsNode(node));
        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeBefore_freeNodeBeforeNodeInOtherList_IllegalArgumentException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IllegalArgumentException.class, () -> {
            ListNode<String> node = createFreeListNode("another");
            ListNode<String> beforeNode = createListNodeInOtherList();

            list.addNodeBefore(node, beforeNode);
        });
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeBefore_freeNodeBeforeFreeNode_IllegalArgumentException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IllegalArgumentException.class, () -> {
            ListNode<String> node = createFreeListNode("another");
            ListNode<String> beforeNode = createFreeListNode("another");

            list.addNodeBefore(node, beforeNode);
        });
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeBefore_nodeInOtherListBeforeNodeOfList_IllegalArgumentException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0)
            abort(); // skip for empty list
        assertThrows(IllegalArgumentException.class, () -> {
            ListNode<String> node = createListNodeInOtherList();
            ListNode<String> beforeNode = list.getNode(size / 2);

            list.addNodeBefore(node, beforeNode);
        });
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddNodeBefore_nodeInThisListBeforeNodeOfList_IllegalArgumentException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            return; // skip for empty list
        }
        assertThrows(IllegalArgumentException.class, () -> {
            ListNode<String> node = list.getFirstNode();
            ListNode<String> beforeNode = list.getNode(size / 2);

            list.addNodeBefore(node, beforeNode);
        });
    }

    /** Test for {@link DoublyLinkedList#getFirstNode()}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testGetFirstNode(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.getFirstNode());
            return;
        }

        ListNode<String> firstNode = list.getFirstNode();

        assertThat(firstNode, is(sameInstance(list.getNode(0))));
        assertThat(firstNode.getValue(), is(sameInstance(expectedElements.get(0))));
    }

    /** Test for {@link DoublyLinkedList#getLastNode()}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testGetLastNode(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.getLastNode());
            return;
        }

        ListNode<String> firstNode = list.getLastNode();

        assertThat(firstNode, is(sameInstance(list.getNode(size - 1))));
        assertThat(firstNode.getValue(), is(sameInstance(expectedElements.get(size - 1))));
    }

    /** Test for {@link DoublyLinkedList#getNode(int)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testGetNode(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        for (int i = 0; i < size; i++) {
            String expectedElement = expectedElements.get(i);

            ListNode<String> node = list.getNode(i);

            assertThat(node.getValue(), is(sameInstance(expectedElement)));
        }
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testGetNode_indexSize_IndexOutOfBoundsException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IndexOutOfBoundsException.class, () -> list.getNode(size));
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testGetNode_indexNegative_IndexOutOfBoundsException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IndexOutOfBoundsException.class, () -> list.getNode(-1));
    }

    /** Test for {@link DoublyLinkedList#indexOfNode(DoublyLinkedList.ListNode)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testIndexOfNode_nodeInList_indexOfNode(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            return;
        }
        int index = size / 3;
        NodeIterator<String> iterator = list.iterator();
        for (int i = 0; i < index; i++) {
            iterator.nextNode(); // do not use getNode(int). Test another program path.
        }
        ListNode<String> node = iterator.nextNode();

        int indexOfNode = list.indexOfNode(node);

        assertThat(indexOfNode, is(equalTo(index)));
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testIndexOfNode_nodeInOtherList_minusOne(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        ListNode<String> node = createListNodeInOtherList();

        int indexOfNode = list.indexOfNode(node);

        assertThat(indexOfNode, is(equalTo(-1)));
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testIndexOfNode_nodeInNoList_minusOne(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        ListNode<String> node = createFreeListNode("another");

        int indexOfNode = list.indexOfNode(node);

        assertThat(indexOfNode, is(equalTo(-1)));
    }

    /** Test for {@link DoublyLinkedList#containsNode(DoublyLinkedList.ListNode)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testContainsNode_nodeInList_true(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            return;
        }
        ListNode<String> node = list.getNode(size / 3);

        boolean contains = list.containsNode(node);

        assertTrue(contains);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testContainsNode_nodeInOtherList_false(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        ListNode<String> node = createListNodeInOtherList();

        boolean contains = list.containsNode(node);

        assertFalse(contains);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testContainsNode_nodeInNoList_false(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        ListNode<String> node = createFreeListNode("another");

        boolean contains = list.containsNode(node);

        assertFalse(contains);
    }

    /**
     * Test for {@link DoublyLinkedList#removeNode(DoublyLinkedList.ListNode)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveNode_nodeInList_nodeRemoved(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort();
        }
        int index = size / 2;
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.remove(index);

        ListNode<String> node = list.getNode(index);
        boolean removed = list.removeNode(node);

        assertTrue(removed);
        assertSameContent(list, expectedList);
        assertFalse(list.containsNode(node));
        if (size == 1) {
            assertTrue(list.isEmpty());
        }
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveNode_nodeNotInList_listUnchanged(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        ListNode<String> nodeNotInList = createListNodeInOtherList();

        boolean removed = list.removeNode(nodeNotInList);

        assertFalse(removed);
        assertSameContent(list, expectedElements); // ensure list did not change
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveNode_removeAllNodesInListFromFront_emptyList(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<ListNode<String>> allNodes = getListNodesOfList(list);

        for (int i = 0; i < size; i++) {
            // remove first node in each iteration
            ListNode<String> nodeToRemove = allNodes.remove(0);

            // test if head was updated correctly in previous remove
            assertThat(list.getFirstNode(), is(sameInstance(nodeToRemove)));

            boolean removed = list.removeNode(nodeToRemove);

            assertTrue(removed);
            assertSameNodes(list, allNodes);
        }
        assertThat(list.size(), is(equalTo(0)));
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveNode_removeAllNodesInListFromEnd_emptyList(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<ListNode<String>> allNodes = getListNodesOfList(list);

        for (int i = 0; i < size; i++) {
            // remove last node in each iteration
            ListNode<String> nodeToRemove = allNodes.remove(allNodes.size() - 1);

            // test if tail was updated correctly in previous remove
            assertThat(list.getLastNode(), is(sameInstance(nodeToRemove)));

            boolean removed = list.removeNode(nodeToRemove);

            assertTrue(removed);
            assertSameNodes(list, allNodes);
        }
        assertThat(list.size(), is(equalTo(0)));
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveNode_removeAllNodesInListFromMiddle_emptyList(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort();
        }
        List<ListNode<String>> allNodes = getListNodesOfList(list);

        ListNode<String> head = allNodes.get(0);
        ListNode<String> tail = allNodes.get(size - 1);

        for (int i = 0; i < size; i++) {
            // remove last node in each iteration
            int index = allNodes.size() / 2;
            ListNode<String> nodeToRemove = allNodes.remove(index);

            // test if head and tail were updated correctly in previous remove
            assertThat(list.getFirstNode(), is(sameInstance(head)));
            assertThat(list.getLastNode(), is(sameInstance(tail)));
            if (!allNodes.isEmpty()) { // if empty this is the last loop
                if (index == 0) {
                    head = allNodes.get(0);
                }
                if (index == allNodes.size()) {
                    tail = allNodes.get(allNodes.size() - 1);
                }
            }

            boolean removed = list.removeNode(nodeToRemove);

            assertTrue(removed);
            assertSameNodes(list, allNodes);
        }
        assertThat(list.size(), is(equalTo(0)));
        assertTrue(list.isEmpty());
    }

    /** Test for {@link DoublyLinkedList#nodeOf(Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testNodeOf_elementInList_nodeOfElement(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String obj2 = "obj2"; // equal String occurs twice in larger lists
        ListNode<String> expectedNode = size <= 2 ? null : list.getNode(2);

        ListNode<String> nodeOfElement = list.nodeOf(obj2);

        assertThat(nodeOfElement, is(sameInstance(expectedNode)));
        if (size > 2) {
            assertThat(nodeOfElement.getValue(), is(equalTo(obj2)));
        }
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testNodeOf_elementNotInList_null(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {

        String otherElement = "another";

        ListNode<String> nodeOfElement = list.nodeOf(otherElement);

        assertThat(nodeOfElement, is(sameInstance(null)));
    }

    /** Test for {@link DoublyLinkedList#lastNodeOf(Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testLastNodeOf(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String obj2 = "obj2"; // equal String occurs twice in larger lists
        ListNode<String> expectedNode;
        if (size <= 2) {
            expectedNode = null;
        } else if (size < 6) {
            expectedNode = list.getNode(2);
        } else {
            expectedNode = list.getNode(3);
        }

        ListNode<String> nodeOfElement = list.lastNodeOf(obj2);
        assertThat(nodeOfElement, is(sameInstance(expectedNode)));
        if (size > 2) {
            assertThat(nodeOfElement.getValue(), is(equalTo(obj2)));
        }
    }

    /** Test for {@link DoublyLinkedList#addElementFirst(Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddElementFirst_nonNullValue_valueAdded(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String another = "another";

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(0, another);

        ListNode<String> addedNode = list.addElementFirst(another);

        assertThat(addedNode, is(sameInstance(list.getFirstNode())));
        assertTrue(list.containsNode(addedNode));
        assertThat(addedNode.getValue(), is(sameInstance(another)));
        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddElementFirst_nullValue_valueAdded(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    { // checks actually only ListNode-constructor, no need to test other addElementX()-methods
        String another = null;

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(0, another);

        ListNode<String> addedNode = list.addElementFirst(another);

        assertThat(addedNode, is(sameInstance(list.getFirstNode())));
        assertTrue(list.containsNode(addedNode));
        assertThat(addedNode.getValue(), is(sameInstance(another)));
        assertSameContent(list, expectedList);
    }

    /** Test for {@link DoublyLinkedList#addElementLast(Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddElementLast(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String another = "another";

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(size, another);

        ListNode<String> addedNode = list.addElementLast(another);

        assertThat(addedNode, is(sameInstance(list.getLastNode())));
        assertTrue(list.containsNode(addedNode));
        assertThat(addedNode.getValue(), is(sameInstance(another)));
        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#addElementBeforeNode(DoublyLinkedList.ListNode, Object)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddElementBeforeNode_sucessorInList_ElementAdded(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            ListNode<String> nodeBefore = null;
            assertThrows(
                NullPointerException.class, () -> list.addElementBeforeNode(nodeBefore, "another"));
            return;
        }

        String another = "another";
        int i = (int) (size / 2.5);

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(i, another);

        ListNode<String> nodeBefore = list.getNode(i);
        ListNode<String> addedNode = list.addElementBeforeNode(nodeBefore, another);

        assertThat(addedNode, is(sameInstance(list.getNode(i))));
        assertTrue(list.containsNode(addedNode));
        assertThat(addedNode.getValue(), is(sameInstance(another)));

        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddElementBeforeNode_sucessorInOtherList_IllegalStateException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IllegalArgumentException.class, () -> {
            String another = "another";

            ListNode<String> nodeBefore = createListNodeInOtherList();
            list.addElementBeforeNode(nodeBefore, another);
        });
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddElementBeforeNode_sucessorInNoList_IllegalStateException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IllegalArgumentException.class, () -> {
            String another = "another";

            ListNode<String> nodeBefore = createFreeListNode("another");
            list.addElementBeforeNode(nodeBefore, another);
        });
    }

    // test List methods

    /** Test for {@link DoublyLinkedList#add(int, Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddInt_atIndex0(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String anotherString = "another";

        List<String> expected = new ArrayList<>(expectedElements);
        expected.add(0, anotherString);

        list.add(0, anotherString);

        assertSameContent(list, expected);
    }

    /** Test for {@link DoublyLinkedList#add(int, Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddInt_inTheMiddle(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String anotherString = "another";

        List<String> expected = new ArrayList<>(expectedElements);
        expected.add(size / 2, anotherString);

        list.add(size / 2, anotherString);

        assertSameContent(list, expected);
    }

    /** Test for {@link DoublyLinkedList#add(int, Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddInt_atIndexSize(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String anotherString = "another";

        List<String> expected = new ArrayList<>(expectedElements);
        expected.add(size, anotherString);

        list.add(size, anotherString);

        assertSameContent(list, expected);
    }

    /** Test for {@link DoublyLinkedList#get(int)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testGetInt(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        for (int i = 0; i < size; i++) {
            String expectedElement = expectedElements.get(i);

            String element = list.get(i);

            assertThat(element, is(sameInstance(expectedElement)));
        }
    }

    /** Test for {@link DoublyLinkedList#remove(int)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveInt_atIndex0(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
            return;
        }
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedRemoved = expectedList.remove(0);

        String removed = list.remove(0);

        assertThat(removed, is(sameInstance(expectedRemoved)));
        assertSameContent(list, expectedList);
    }

    /** Test for {@link DoublyLinkedList#remove(int)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveInt_inTheMiddle(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(size / 2));
            return;
        }
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedRemoved = expectedList.remove(size / 2);

        String removed = list.remove(size / 2);

        assertThat(removed, is(sameInstance(expectedRemoved)));
        assertSameContent(list, expectedList);
    }

    /** Test for {@link DoublyLinkedList#remove(int)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveInt_atIndexSizeMinusOne(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(IndexOutOfBoundsException.class, () -> list.remove(size - 1));
            return;
        }
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedRemoved = expectedList.remove(size - 1);

        String removed = list.remove(size - 1);

        assertThat(removed, is(sameInstance(expectedRemoved)));
        assertSameContent(list, expectedList);
    }

    // test Deque methods

    /**
     * Test for {@link DoublyLinkedList#addFirst(Object)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddFirst(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String anotherString = "another";

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(0, anotherString);

        list.addFirst(anotherString);

        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#addLast(Object)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAddLast(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String anotherString = "another";

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(size, anotherString);

        list.addLast(anotherString);

        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#offerFirst(Object)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testOfferFirst(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String anotherString = "another";

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(0, anotherString);

        list.offerFirst(anotherString);

        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#offerLast(Object)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testOfferLast(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String anotherString = "another";

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(size, anotherString);

        list.offerLast(anotherString);

        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#removeFirst()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveFirst(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.removeFirst());
            return;
        }
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedFirst = size > 0 ? expectedList.remove(0) : null;

        String first = list.removeFirst();

        assertThat(first, is(sameInstance(expectedFirst)));
        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#removeLast()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveLast(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.removeLast());
            return;
        }
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedLast = size > 0 ? expectedList.remove(size - 1) : null;

        String last = list.removeLast();

        assertThat(last, is(sameInstance(expectedLast)));
        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#pollFirst()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testPollFirst(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedFirst = size > 0 ? expectedList.remove(0) : null;

        String first = list.pollFirst();

        assertThat(first, is(sameInstance(expectedFirst)));
        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#pollLast()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testPollLast(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedLast = size > 0 ? expectedList.remove(size - 1) : null;

        String last = list.pollLast();

        assertThat(last, is(sameInstance(expectedLast)));
        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#getFirst()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testGetFirst(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.getFirst());
            return;
        }
        String first = list.getFirst();

        assertThat(first, is(sameInstance(expectedElements.get(0))));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    /**
     * Test for {@link DoublyLinkedList#getLast()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testGetLast(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.getLast());
            return;
        }
        String last = list.getLast();

        assertThat(last, is(sameInstance(expectedElements.get(size - 1))));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    /** Test for {@link DoublyLinkedList#peekFirst()}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testPeekFirst(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String expectedFirst = size > 0 ? expectedElements.get(0) : null;

        String first = list.peekFirst();

        assertThat(first, is(sameInstance(expectedFirst)));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    /** Test for {@link DoublyLinkedList#peekLast()}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testPeekLast(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String expectedLast = size > 0 ? expectedElements.get(size - 1) : null;

        String last = list.peekLast();

        assertThat(last, is(sameInstance(expectedLast)));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    /** Test for {@link DoublyLinkedList#removeFirstOccurrence(Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveFirstOccurrence(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        boolean expectedRemoved = size >= 3;
        List<String> expectedList = new ArrayList<>(expectedElements);
        if (size >= 3) { // if size < 2 no such element, list remains unchanged
            expectedList.remove(2);
        }

        boolean removed = list.removeFirstOccurrence("obj2");

        assertThat(removed, is(equalTo(expectedRemoved)));
        assertSameContent(list, expectedList);
    }

    /** Test for {@link DoublyLinkedList#removeLastOccurrence(Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemoveLastOccurrence(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        boolean expectedRemoved = size >= 3;
        List<String> expectedList = new ArrayList<>(expectedElements);
        if (size >= 6) { // if size < 2 no such element, list remains unchanged
            expectedList.remove(3);
        } else if (size >= 3) {
            expectedList.remove(2);
        }

        boolean removed = list.removeLastOccurrence("obj2");

        assertThat(removed, is(equalTo(expectedRemoved)));
        assertSameContent(list, expectedList);
    }

    // test Queue methods

    /**
     * Test for {@link DoublyLinkedList#offer(Object)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testOffer(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String anotherString = "another";

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(size, anotherString);

        list.offer(anotherString);

        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#remove()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testRemove(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.remove());
            return;
        }
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedFirst = size > 0 ? expectedList.remove(0) : null;

        String first = list.remove();

        assertThat(first, is(sameInstance(expectedFirst)));
        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#poll()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testPoll(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedFirst = size > 0 ? expectedList.remove(0) : null;

        String first = list.poll();

        assertThat(first, is(sameInstance(expectedFirst)));
        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#element()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testElement(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.element());
            return;
        }
        String first = list.element();

        assertThat(first, is(sameInstance(expectedElements.get(0))));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    /**
     * Test for {@link DoublyLinkedList#peek()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testPeek(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String expectedFirst = size > 0 ? expectedElements.get(0) : null;

        String first = list.peek();

        assertThat(first, is(sameInstance(expectedFirst)));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    // test Stack methods

    /** Test for {@link DoublyLinkedList#push(Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testPush(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        String anotherString = "another";

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(0, anotherString);

        list.push(anotherString);

        assertSameContent(list, expectedList);
    }

    /**
     * Test for {@link DoublyLinkedList#pop()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testPop(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.pop());
            return;
        }
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedFirst = size > 0 ? expectedList.remove(0) : null;

        String first = list.pop();

        assertThat(first, is(sameInstance(expectedFirst)));
        assertSameContent(list, expectedList);
    }

    // test special bulk methods

    /**
     * Test for {@link DoublyLinkedList#invert()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testInvert(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        list.invert();

        List<String> expected = new ArrayList<>(expectedElements);
        Collections.reverse(expected);

        assertSameContent(list, expected);
    }

    /**
     * Test for {@link DoublyLinkedList#moveFrom(int, DoublyLinkedList)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testMoveFrom(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        int index = size / 3;
        List<String> other = size < 4 ? Collections.singletonList("another1")
            : Arrays.asList("another1", "another2");

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.addAll(index, other);

        DoublyLinkedList<String> sourceList = createDoublyLinkedList(other);

        list.moveFrom(index, sourceList);

        assertSameContent(list, expectedList);
        assertThat(sourceList.size(), is(equalTo(0)));
        assertTrue(sourceList.isEmpty());
    }

    /**
     * Test for {@link DoublyLinkedList#append(DoublyLinkedList)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testAppend(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<String> other = size < 4 ? Collections.singletonList("another1")
            : Arrays.asList("another1", "another2");

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.addAll(size, other);

        DoublyLinkedList<String> sourceList = createDoublyLinkedList(other);

        list.append(sourceList);

        assertSameContent(list, expectedList);
        assertThat(sourceList.size(), is(equalTo(0)));
        assertTrue(sourceList.isEmpty());
    }

    /** Test for {@link DoublyLinkedList#prepend(DoublyLinkedList)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testPrepend(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<String> other = size < 4 ? Collections.singletonList("another1")
            : Arrays.asList("another1", "another2");

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.addAll(0, other);

        DoublyLinkedList<String> sourceList = createDoublyLinkedList(other);

        list.prepend(sourceList);

        assertSameContent(list, expectedList);
        assertThat(sourceList.size(), is(equalTo(0)));
        assertTrue(sourceList.isEmpty());

    }

    // test iterator

    /**
     * Test for {@link DoublyLinkedList#circularIterator(Object)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testCircularIterator(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.circularIterator("anything"));
            return;
        }

        int startIndex = size / 3;
        String firstElement = expectedElements.get(startIndex);
        List<String> expectedList = new ArrayList<>();
        expectedList.addAll(expectedElements.subList(startIndex, expectedElements.size()));
        expectedList.addAll(expectedElements.subList(0, startIndex));

        NodeIterator<String> wrappingIterator = list.circularIterator(firstElement);
        for (String expectedElement : expectedList) {
            assertTrue(wrappingIterator.hasNext());
            assertThat(wrappingIterator.next(), is(sameInstance(expectedElement)));
        }
        assertFalse(wrappingIterator.hasNext());
    }

    /**
     * Test for {@link DoublyLinkedList#reverseCircularIterator(Object)}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testReverseCircularIterator(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            assertThrows(
                NoSuchElementException.class, () -> list.reverseCircularIterator("anything"));
            return;
        }
        int startIndex = size / 3;

        List<String> expectedList = new ArrayList<>();
        String firstElement = expectedElements.get(startIndex);

        expectedList.addAll(expectedElements.subList(startIndex + 1, size));
        expectedList.addAll(expectedElements.subList(0, startIndex + 1));
        Collections.reverse(expectedList);

        NodeIterator<String> wrappingIterator = list.reverseCircularIterator(firstElement);
        for (String expectedElement : expectedList) {
            assertTrue(wrappingIterator.hasNext());
            assertThat(wrappingIterator.next(), is(sameInstance(expectedElement)));
        }
        assertFalse(wrappingIterator.hasNext());
    }

    /**
     * Test for {@link DoublyLinkedList#descendingIterator()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testDescendingIterator(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        NodeIterator<String> iterator = list.descendingIterator();
        for (int i = size - 1; i >= 0; i--) {
            assertThat(iterator.next(), is(sameInstance(expectedElements.get(i))));
        }
        assertFalse(iterator.hasNext());
    }

    /**
     * Test for {@link DoublyLinkedList#iterator()}.
     */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testIterator(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        NodeIterator<String> iterator = list.iterator();
        for (int i = 0; i < size; i++) {
            assertThat(iterator.next(), is(sameInstance(expectedElements.get(i))));
        }
        assertFalse(iterator.hasNext());
    }

    /** Test for {@link DoublyLinkedList#listIterator(Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorE(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    { // test only if returned ListIterator starts expected position beginning.
        if (size == 0) {
            assertThrows(NoSuchElementException.class, () -> list.listIterator(null));
            return;
        }
        String element = expectedElements.get(size / 3);
        ListNodeIterator<String> listIterator = list.listIterator(element);

        assertTrue(listIterator.hasNext());
        ListNode<String> firstNode = listIterator.nextNode();
        assertThat(firstNode, is(sameInstance(list.getNode(size / 3))));
        assertThat(firstNode.getValue(), is(sameInstance(element)));
    }

    /** Test for {@link DoublyLinkedList#listIterator(int)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorInt_indexInTheMiddle_iteratorAtCorrectIndex(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    { // test only if returned ListIterator starts at the correct position.
        int index = size / 2;

        ListNodeIterator<String> listIterator = list.listIterator(index);

        if (size == 0) {
            assertFalse(listIterator.hasNext());
            assertFalse(listIterator.hasPrevious());
        } else {
            assertThat(listIterator.nextIndex(), is(equalTo(index)));
            assertThat(listIterator.next(), is(sameInstance(expectedElements.get(index))));
        }
    }

    /** Test for {@link DoublyLinkedList.ListNodeIterator#nextNode()}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorNext_iterateForwardTroughCompleteList_ListNodesInOrder(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    { // test only if returned ListIterator starts at the beginning.
        ListNodeIterator<String> listIterator = list.listIterator();
        List<ListNode<String>> listNodes = getListNodesOfList(list);

        assertFalse(listIterator.hasPrevious());
        assertThat(listIterator.previousIndex(), is(equalTo(-1)));

        for (int i = 0; i < size; i++) {
            assertTrue(listIterator.hasNext());
            assertThat(listIterator.nextIndex(), is(equalTo(i)));
            ListNode<String> nextNode = listIterator.nextNode();
            assertThat(nextNode, is(sameInstance(listNodes.get(i))));
            assertThat(nextNode.getValue(), is(sameInstance(expectedElements.get(i))));
        }
        assertFalse(listIterator.hasNext());
        assertThat(listIterator.nextIndex(), is(equalTo(size)));
    }

    /** Test for {@link DoublyLinkedList.ListNodeIterator#previousNode()}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorPrevious_iterateBackwardTroughCompleteList_ListNodesInOrder(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    { // test only if returned ListIterator starts at the beginning.
        ListNodeIterator<String> listIterator = list.listIterator(size);
        List<ListNode<String>> listNodes = getListNodesOfList(list);

        assertFalse(listIterator.hasNext());
        assertThat(listIterator.nextIndex(), is(equalTo(size)));

        for (int i = size - 1; i >= 0; i--) {
            assertTrue(listIterator.hasPrevious());
            assertThat(listIterator.previousIndex(), is(equalTo(i)));
            ListNode<String> nextNode = listIterator.previousNode();
            assertThat(nextNode, is(sameInstance(listNodes.get(i))));
            assertThat(nextNode.getValue(), is(sameInstance(expectedElements.get(i))));
        }
        assertFalse(listIterator.hasPrevious());
        assertThat(listIterator.previousIndex(), is(equalTo(-1)));
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorNextPrevious_forwardBackwardPattern_correctElements(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        int index = size / 3;
        ListNodeIterator<String> listIterator = list.listIterator(index);

        for (; index < size; index++) {
            assertTrue(listIterator.hasNext());
            assertThat(listIterator.nextIndex(), is(equalTo(index)));
            assertThat(listIterator.next(), is(equalTo(expectedElements.get(index))));
            // index++;

            assertTrue(listIterator.hasPrevious());
            assertThat(listIterator.previousIndex(), is(equalTo(index)));
            assertThat(listIterator.previous(), is(equalTo(expectedElements.get(index))));
            // index--;

            assertTrue(listIterator.hasNext());
            assertThat(listIterator.nextIndex(), is(equalTo(index)));
            assertThat(listIterator.next(), is(equalTo(expectedElements.get(index))));
        }
        assertFalse(listIterator.hasNext());
        assertThat(listIterator.nextIndex(), is(equalTo(size)));
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIterator_iterateBehindTail(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        ListNodeIterator<String> iterator = list.listIterator();
        for (int i = 0; i < size; i++) {
            iterator.next();
        }
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, () -> iterator.next());
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIterator_iterateBeforeHead(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        ListNodeIterator<String> iterator = list.listIterator(size);
        for (int i = 0; i < size; i++) {
            iterator.previous();
        }
        assertFalse(iterator.hasPrevious());
        assertThrows(NoSuchElementException.class, () -> iterator.previous());
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIterator_concurrentAdd_ConcurrentModificationException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(ConcurrentModificationException.class, () -> {
            ListNodeIterator<String> listIterator = list.listIterator();

            list.add("another");

            listIterator.next();
        });
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIterator_concurrentRemove_ConcurrentModificationException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort();
        }

        ListNodeIterator<String> listIterator = list.listIterator();
        list.removeLast();

        assertThrows(ConcurrentModificationException.class, () -> listIterator.next());
    }

    /** Test for {@link DoublyLinkedList.ListNodeIterator#remove()}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorRemove_clearListFromTheFront_emptyList(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<ListNode<String>> listNodes = getListNodesOfList(list);
        ListNodeIterator<String> listIterator = list.listIterator();
        for (int i = 0; i < size; i++) {
            ListNode<String> next = listIterator.nextNode();
            assertThat(next, is(sameInstance(listNodes.get(i))));
            listIterator.remove();
        }

        assertFalse(listIterator.hasNext());
        assertThat(list.size(), is(equalTo(0)));
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorRemove_clearListFromTheMiddle_emptyList(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<ListNode<String>> listNodes = getListNodesOfList(list);
        ListNodeIterator<String> listIterator = list.listIterator();
        for (int i = 0; i < size; i++) {
            ListNode<String> next = listIterator.nextNode();
            assertThat(next, is(sameInstance(listNodes.get(i))));
            listIterator.remove();
        }

        assertFalse(listIterator.hasNext());
        assertThat(list.size(), is(equalTo(0)));
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorRemove_clearListFromTheEnd_emptyList(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<ListNode<String>> listNodes = getListNodesOfList(list);
        ListNodeIterator<String> listIterator = list.listIterator(size);
        for (int i = size - 1; i >= 0; i--) {
            ListNode<String> next = listIterator.previousNode();
            assertThat(next, is(sameInstance(listNodes.get(i))));
            listIterator.remove();
        }

        assertFalse(listIterator.hasPrevious());
        assertThat(list.size(), is(equalTo(0)));
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorRemove_notMovedListIterator_IllegalStateException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IllegalStateException.class, () -> list.listIterator().remove());
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorRemove_removeTwiceAfterNext_IllegalStateException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort();
        }
        ListNodeIterator<String> listIterator = list.listIterator();
        listIterator.next();
        listIterator.remove();

        // check if the last-node of the iterator is cleared correctly

        assertThrows(IllegalStateException.class, () -> listIterator.remove());
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorRemove_removeAfterAdd_IllegalStateException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            return;
        }
        ListNodeIterator<String> listIterator = list.listIterator();
        listIterator.next();
        listIterator.add("Another");

        // check if the last-node of the iterator is cleared correctly in add()

        assertThrows(IllegalStateException.class, () -> listIterator.remove());
    }

    /** Test for {@link DoublyLinkedList.ListNodeIterator#add(Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorAdd_addElementsAtFront_listWithAdditionalElements(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<String> toAdd = Arrays.asList("another1", "two", "three", "four");
        List<String> expectedList = new ArrayList<>(expectedElements);

        ListNodeIterator<String> listIterator = list.listIterator(0);

        for (int i = 0; i < toAdd.size(); i++) {
            String add = toAdd.get(i);
            expectedList.add(i, add);
            listIterator.add(add);
            assertSameContent(list, expectedList);
        }
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorAdd_addElementsInTheMiddle_listWithAdditionalElements(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        List<String> toAdd = Arrays.asList("another1", "two", "three", "four");
        List<String> expectedList = new ArrayList<>(expectedElements);

        int index = size / 2;
        ListNodeIterator<String> listIterator = list.listIterator(index);

        for (int i = 0; i < toAdd.size(); i++) {
            String add = toAdd.get(i);
            expectedList.add(index + i, add);
            listIterator.add(add);
            assertSameContent(list, expectedList);
        }
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorAdd_addElementsAtEnd_listWithAdditionalElements(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {

        List<String> toAdd = Arrays.asList("another1", "two", "three", "four");
        List<String> expectedList = new ArrayList<>(expectedElements);

        ListNodeIterator<String> listIterator = list.listIterator(size);

        for (int i = 0; i < toAdd.size(); i++) {
            String add = toAdd.get(i);
            expectedList.add(add);
            listIterator.add(add);
            assertSameContent(list, expectedList);
        }
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorAdd_addElementBeforeEnd_listWithAdditionalElements(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort();
        }

        String element = "another";
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(size - 1, element);

        ListNodeIterator<String> listIterator = list.listIterator(size);
        listIterator.previous();
        listIterator.add(element);

        assertSameContent(list, expectedList);
    }

    /** Test for {@link DoublyLinkedList.ListNodeIterator#set(Object)}. */
    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorSet_replaceElementInTheMiddle_listWithReplacedElement(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort();
        }
        int index = size / 2;
        String element = "another";
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.set(index, element);

        ListNodeIterator<String> listIterator = list.listIterator(index);

        listIterator.next();
        listIterator.set(element);
        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorSet_replaceElementAtFront_listWithReplacedElement(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort();
        }
        String element = "another";
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.set(0, element);

        ListNodeIterator<String> listIterator = list.listIterator(0);

        listIterator.next();
        listIterator.set(element);
        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorSet_replaceElementInAtEnd_listWithReplacedElement(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort();
        }
        String element = "another";
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.set(size - 1, element);

        ListNodeIterator<String> listIterator = list.listIterator(size);

        listIterator.previous();
        listIterator.set(element);
        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorSet_setElementWithSubsequentRemove_listWithReplacedElement(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    { // check if the last node of the iterator is configured right

        if (size == 0) {
            return;
        }
        int index = size / 2;
        String element = "another";
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.remove(index);

        ListNodeIterator<String> listIterator = list.listIterator(index);

        listIterator.next();
        listIterator.set(element);

        listIterator.remove();

        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorSet_setTwice_listWithReplacedElement(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {

        if (size == 0) {
            abort();
        }
        int index = size / 2;
        String element = "another";
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.set(index, element);

        ListNodeIterator<String> listIterator = list.listIterator(index);

        listIterator.next();
        listIterator.set("anotherOne");
        listIterator.set(element);

        assertSameContent(list, expectedList);
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorSet_NotMovedListIterator_IllegalstateException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IllegalStateException.class, () -> {
            ListNodeIterator<String> listIterator = list.listIterator();
            listIterator.set("another");
        });
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorSet_setAfterAdd_IllegalstateException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        assertThrows(IllegalStateException.class, () -> {
            ListNodeIterator<String> listIterator = list.listIterator();
            listIterator.add("another");
            listIterator.set("another");
        });
    }

    @ParameterizedTest(name = "List with size {0}")
    @MethodSource("getListSizes")
    public void testListIteratorSet_setAfterRemove_IllegalstateException(int size, List<String> expectedElements, DoublyLinkedList<String> list)
    {
        if (size == 0) {
            abort();
        }
        ListNodeIterator<String> listIterator = list.listIterator();
        listIterator.next();
        listIterator.remove();
        assertThrows(IllegalStateException.class, () -> listIterator.set("another"));
    }

    /**
     * Tests for {@link DoublyLinkedList.ReversedDoublyLinkedListView}.
     * 
     * @since 1.5.3
     */
    @DisplayName("Reversed view tests")
    @Nested
    class ReversedDoublyLinkedListViewTest {
        
        private DoublyLinkedList<Integer> list;
        private DoublyLinkedList<Integer> reversedList;
        private List<Integer> expected;

        @BeforeEach
        public void setup() {
            expected = List.of(0, 1, 2, 3, 4);
            list = createDoublyLinkedList(expected);
            reversedList = list.reversed();
        }

        @Test
        public void testHead() {
            ListNode<Integer> revHead = reversedList.head();
            assertSame(reversedList, revHead.getList());
            assertEquals(4, revHead.getValue());
        }

        @Test
        public void testTail() {
            ListNode<Integer> revTail = reversedList.tail();
            assertSame(reversedList, revTail.getList());
            assertEquals(0, revTail.getValue());
        }

        @Test
        public void testEmpty() {
            assertFalse(reversedList.isEmpty());
            assertTrue(new DoublyLinkedList<Object>().reversed().isEmpty());
        }

        @Test
        public void testSize() {
            assertEquals(5, reversedList.size());
            assertEquals(0, new DoublyLinkedList<Object>().reversed().size());
        }

        @Test
        public void testModification() {
            assertThrows(UnsupportedOperationException.class, () -> reversedList.clear());
            assertThrows(UnsupportedOperationException.class, () -> reversedList.addNode(0, createFreeListNode(5)));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.addNodeFirst(createFreeListNode(5)));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.addNodeLast(createFreeListNode(-1)));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.addNodeBefore(createFreeListNode(5), reversedList.getNode(0)));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.removeNode(reversedList.getNode(0)));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.addElementFirst(5));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.addElementLast(-1));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.addElementBeforeNode(reversedList.getNode(0), 5));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.add(0, 5));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.remove(0));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.addFirst(5));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.addLast(-1));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.offerFirst(4));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.offerLast(3));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.removeFirst());
            assertThrows(UnsupportedOperationException.class, () -> reversedList.removeLast());
            assertThrows(UnsupportedOperationException.class, () -> reversedList.pollFirst());
            assertThrows(UnsupportedOperationException.class, () -> reversedList.pollLast());
            assertThrows(UnsupportedOperationException.class, () -> reversedList.removeFirstOccurrence(2));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.removeLastOccurrence(1));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.offer(5));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.remove(1));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.poll());
            assertThrows(UnsupportedOperationException.class, () -> reversedList.push(5));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.pop());
            assertThrows(UnsupportedOperationException.class, () -> reversedList.invert());
            assertThrows(UnsupportedOperationException.class, () -> reversedList.moveFrom(0, list));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.append(list));
            assertThrows(UnsupportedOperationException.class, () -> reversedList.prepend(list));
        }

        @Test
        public void testGets() {
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(i, reversedList.getNode(reversedList.size() - (1 + i)).getValue());
                assertEquals(i, reversedList.get(reversedList.size() - (1 + i)));
            }
        }

        @Test
        public void testNodeIterator() {
            NodeIterator<Integer> iterator = reversedList.iterator();
            int expected = 4;
            while (iterator.hasNext()) {
                ListNode<Integer> node = iterator.nextNode();
                assertSame(reversedList, node.getList());
                assertEquals(expected--, node.getValue());
            }
        }
    }

    // utility methods

    private static <E> DoublyLinkedList<E> createDoublyLinkedList(Collection<E> content)
    {
        DoublyLinkedList<E> list = new DoublyLinkedList<>();
        for (E element : content) {
            list.addLast(element); // use simplest method as possible
        }
        return list;
    }

    private static <E> List<ListNode<E>> getListNodesOfList(DoublyLinkedList<E> list)
    {
        List<ListNode<E>> allNodes = new ArrayList<>();
        for (NodeIterator<E> iterator = list.iterator(); iterator.hasNext();) {
            allNodes.add(iterator.nextNode());
        }
        return allNodes;
    }

    /** Returns a {@link ListNode} contained in another {@link DoublyLinkedList}. */
    private static ListNode<String> createListNodeInOtherList()
    {
        return createDoublyLinkedList(Collections.singletonList("another")).getNode(0);
    }

    /** Returns a {@link ListNode} contained in no {@link DoublyLinkedList}. */
    private static <E> ListNode<E> createFreeListNode(E element)
    {

        DoublyLinkedList<E> list = createDoublyLinkedList(Collections.singletonList(element));

        ListNode<E> node = list.getNode(0);
        list.removeNode(node);
        return node;
    }

    private static <E> void assertSameContent(DoublyLinkedList<E> list, List<E> expected)
    {
        assertThat(list.size(), is(equalTo(expected.size())));
        for (int i = 0; i < list.size(); i++) {
            assertThat(list.get(i), is(sameInstance(expected.get(i))));
        }
    }

    private static <E> void assertSameNodes(DoublyLinkedList<E> list, List<ListNode<E>> expected)
    {
        assertThat(list.size(), is(equalTo(expected.size())));
        for (int i = 0; i < list.size(); i++) {
            assertThat(list.getNode(i), is(sameInstance(expected.get(i))));
        }
    }

}
