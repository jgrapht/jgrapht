/*
 * (C) Copyright 2020-2020, by Hannes Wellmann and Contributors.
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
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link DoublyLinkedList}.
 * 
 * @author Hannes Wellmann
 *
 */
@RunWith(Parameterized.class)
public class DoublyLinkedListTest
{
    private static final int MAX_LIST_SIZE = 8;

    @Parameters(name = "List with size {0}")
    public static Object[] getListSizes()
    {
        Object[][] parameterSets = new Object[MAX_LIST_SIZE + 1][];
        for (int size = 0; size < MAX_LIST_SIZE + 1; size++) {

            List<Object> elements = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                elements.add("obj" + i);
            }
            if (size >= 6) { // make two elements equal
                elements.set(3, new String("obj2")); // create equal new String-object
            }
            parameterSets[size] = new Object[] { size, Collections.unmodifiableList(elements) };
        }
        return parameterSets;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Parameterized.Parameter(0)
    public int size;
    @Parameterized.Parameter(1)
    public List<String> expectedElements;

    private DoublyLinkedList<String> list;

    @Before
    public void setUp()
    {
        list = createDoublyLinkedList(expectedElements);
    }

    // ------------------------------------------------------------------------
    // test cases

    /** Test for {@link DoublyLinkedList#isEmpty()}. */
    @Test
    public void testIsEmpty()
    {
        assertThat(list.isEmpty(), is(equalTo(size == 0)));
    }

    /** Test for {@link DoublyLinkedList#size()}. */
    @Test
    public void testSize()
    {
        assertThat(list.size(), is(equalTo(size)));
    }

    /** Test for {@link DoublyLinkedList#clear()}. */
    @Test
    public void testClear()
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
    @Test
    public void testAddNodeFirst_freeNode_nodeAddedToList()
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

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeFirst_nodeInOtherList_IllegalArgumentException()
    {
        ListNode<String> node = createListNodeInOtherList();

        list.addNodeFirst(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeFirst_nodeOfThisList_IllegalArgumentException()
    {
        if (size == 0) {
            throw new IllegalArgumentException(); // throw expected exception to skip for empty list
        }
        ListNode<String> node = list.getNode(size / 2);

        list.addNodeFirst(node);
    }

    /** Test for {@link DoublyLinkedList#addNodeLast(DoublyLinkedList.ListNode)}. */
    @Test
    public void testAddNodeLast_freeNode_nodeAddedToList()
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

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeLast_nodeInOtherList_IllegalArgumentException()
    {
        ListNode<String> node = createListNodeInOtherList();

        list.addNodeLast(node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeLast_nodeOfThisList_IllegalArgumentException()
    {
        if (size == 0) {
            throw new IllegalArgumentException(); // throw expected exception to skip for empty list
        }
        ListNode<String> node = list.getNode(size / 2);

        list.addNodeLast(node);
    }

    /** Test for {@link DoublyLinkedList#addNode(int, DoublyLinkedList.ListNode)}. */
    @Test
    public void testAddNode_freeNode_nodeAddedToList()
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

    @Test(expected = IllegalArgumentException.class)
    public void testAddNode_nodeInOtherList_IllegalArgumentException()
    {
        ListNode<String> node = createListNodeInOtherList();

        list.addNode(size / 2, node);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNode_nodeOfThisList_IllegalArgumentException()
    {
        if (size == 0) {
            throw new IllegalArgumentException(); // throw expected exception to skip for empty list
        }
        ListNode<String> node = list.getLastNode();

        list.addNode(size / 2, node);
    }

    /**
     * Test for
     * {@link DoublyLinkedList#addNodeBefore(DoublyLinkedList.ListNode, DoublyLinkedList.ListNode)}.
     */
    @Test
    public void testAddNodeBefore_freeNodeBeforeNodeInList_nodeAddedToList()
    {
        if (size == 0) {
            return;
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

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeBefore_freeNodeBeforeNodeInOtherList_IllegalArgumentException()
    {
        ListNode<String> node = createFreeListNode("another");
        ListNode<String> beforeNode = createListNodeInOtherList();

        list.addNodeBefore(node, beforeNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeBefore_freeNodeBeforeFreeNode_IllegalArgumentException()
    {
        ListNode<String> node = createFreeListNode("another");
        ListNode<String> beforeNode = createFreeListNode("another");

        list.addNodeBefore(node, beforeNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeBefore_nodeInOtherListBeforeNodeOfList_IllegalArgumentException()
    {
        if (size == 0) {
            throw new IllegalArgumentException(); // throw expected exception to skip for empty list
        }
        ListNode<String> node = createListNodeInOtherList();
        ListNode<String> beforeNode = list.getNode(size / 2);

        list.addNodeBefore(node, beforeNode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNodeBefore_nodeInThisListBeforeNodeOfList_IllegalArgumentException()
    {
        if (size == 0) {
            throw new IllegalArgumentException(); // throw expected exception to skip for empty list
        }
        ListNode<String> node = list.getFirstNode();
        ListNode<String> beforeNode = list.getNode(size / 2);

        list.addNodeBefore(node, beforeNode);
    }

    /** Test for {@link DoublyLinkedList#getFirstNode()}. */
    @Test
    public void testGetFirstNode()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
        }

        ListNode<String> firstNode = list.getFirstNode();

        assertThat(firstNode, is(sameInstance(list.getNode(0))));
        assertThat(firstNode.getValue(), is(sameInstance(expectedElements.get(0))));
    }

    /** Test for {@link DoublyLinkedList#getLastNode()}. */
    @Test
    public void testGetLastNode()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
        }

        ListNode<String> firstNode = list.getLastNode();

        assertThat(firstNode, is(sameInstance(list.getNode(size - 1))));
        assertThat(firstNode.getValue(), is(sameInstance(expectedElements.get(size - 1))));
    }

    /** Test for {@link DoublyLinkedList#getNode(int)}. */
    @Test
    public void testGetNode()
    {
        for (int i = 0; i < size; i++) {
            String expectedElement = expectedElements.get(i);

            ListNode<String> node = list.getNode(i);

            assertThat(node.getValue(), is(sameInstance(expectedElement)));
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNode_indexSize_IndexOutOfBoundsException()
    {
        list.getNode(size);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNode_indexNegative_IndexOutOfBoundsException()
    {
        list.getNode(-1);
    }

    /** Test for {@link DoublyLinkedList#indexOfNode(DoublyLinkedList.ListNode)}. */
    @Test
    public void testIndexOfNode_nodeInList_indexOfNode()
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

    @Test
    public void testIndexOfNode_nodeInOtherList_minusOne()
    {
        ListNode<String> node = createListNodeInOtherList();

        int indexOfNode = list.indexOfNode(node);

        assertThat(indexOfNode, is(equalTo(-1)));
    }

    @Test
    public void testIndexOfNode_nodeInNoList_minusOne()
    {
        ListNode<String> node = createFreeListNode("another");

        int indexOfNode = list.indexOfNode(node);

        assertThat(indexOfNode, is(equalTo(-1)));
    }

    /** Test for {@link DoublyLinkedList#containsNode(DoublyLinkedList.ListNode)}. */
    @Test
    public void testContainsNode_nodeInList_true()
    {
        if (size == 0) {
            return;
        }
        ListNode<String> node = list.getNode(size / 3);

        boolean contains = list.containsNode(node);

        assertTrue(contains);
    }

    @Test
    public void testContainsNode_nodeInOtherList_false()
    {
        ListNode<String> node = createListNodeInOtherList();

        boolean contains = list.containsNode(node);

        assertFalse(contains);
    }

    @Test
    public void testContainsNode_nodeInNoList_false()
    {
        ListNode<String> node = createFreeListNode("another");

        boolean contains = list.containsNode(node);

        assertFalse(contains);
    }

    /**
     * Test for {@link DoublyLinkedList#removeNode(DoublyLinkedList.ListNode)}.
     */
    @Test
    public void testRemoveNode_nodeInList_nodeRemoved()
    {
        if (size == 0) {
            return;
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

    @Test
    public void testRemoveNode_nodeNotInList_listUnchanged()
    {
        ListNode<String> nodeNotInList = createListNodeInOtherList();

        boolean removed = list.removeNode(nodeNotInList);

        assertFalse(removed);
        assertSameContent(list, expectedElements); // ensure list did not change
    }

    @Test
    public void testRemoveNode_removeAllNodesInListFromFront_emptyList()
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

    @Test
    public void testRemoveNode_removeAllNodesInListFromEnd_emptyList()
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

    @Test
    public void testRemoveNode_removeAllNodesInListFromMiddle_emptyList()
    {
        if (size == 0) {
            return;
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
    @Test
    public void testNodeOf_elementInList_nodeOfElement()
    {
        String obj2 = "obj2"; // equal String occurs twice in larger lists
        ListNode<String> expectedNode = size <= 2 ? null : list.getNode(2);

        ListNode<String> nodeOfElement = list.nodeOf(obj2);

        assertThat(nodeOfElement, is(sameInstance(expectedNode)));
        if (size > 2) {
            assertThat(nodeOfElement.getValue(), is(equalTo(obj2)));
        }
    }

    @Test
    public void testNodeOf_elementNotInList_null()
    {
        String otherElement = "another";

        ListNode<String> nodeOfElement = list.nodeOf(otherElement);

        assertThat(nodeOfElement, is(sameInstance(null)));
    }

    /** Test for {@link DoublyLinkedList#lastNodeOf(Object)}. */
    @Test
    public void testLastNodeOf()
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
    @Test
    public void testAddElementFirst_nonNullValue_valueAdded()
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

    @Test
    public void testAddElementFirst_nullValue_valueAdded()
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
    @Test
    public void testAddElementLast()
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
    @Test
    public void testAddElementBeforeNode_sucessorInList_ElementAdded()
    {
        if (size == 0) {
            thrown.expect(NullPointerException.class);
        }
        String another = "another";
        int i = (int) (size / 2.5);

        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.add(i, another);

        ListNode<String> nodeBefore = size > 0 ? list.getNode(i) : null;
        ListNode<String> addedNode = list.addElementBeforeNode(nodeBefore, another);

        assertThat(addedNode, is(sameInstance(list.getNode(i))));
        assertTrue(list.containsNode(addedNode));
        assertThat(addedNode.getValue(), is(sameInstance(another)));

        assertSameContent(list, expectedList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddElementBeforeNode_sucessorInOtherList_IllegalStateException()
    {
        String another = "another";

        ListNode<String> nodeBefore = createListNodeInOtherList();
        list.addElementBeforeNode(nodeBefore, another);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddElementBeforeNode_sucessorInNoList_IllegalStateException()
    {
        String another = "another";

        ListNode<String> nodeBefore = createFreeListNode("another");
        list.addElementBeforeNode(nodeBefore, another);
    }

    // test List methods

    /** Test for {@link DoublyLinkedList#add(int, Object)}. */
    @Test
    public void testAddInt_atIndex0()
    {
        String anotherString = "another";

        List<String> expected = new ArrayList<>(expectedElements);
        expected.add(0, anotherString);

        list.add(0, anotherString);

        assertSameContent(list, expected);
    }

    /** Test for {@link DoublyLinkedList#add(int, Object)}. */
    @Test
    public void testAddInt_inTheMiddle()
    {
        String anotherString = "another";

        List<String> expected = new ArrayList<>(expectedElements);
        expected.add(size / 2, anotherString);

        list.add(size / 2, anotherString);

        assertSameContent(list, expected);
    }

    /** Test for {@link DoublyLinkedList#add(int, Object)}. */
    @Test
    public void testAddInt_atIndexSize()
    {
        String anotherString = "another";

        List<String> expected = new ArrayList<>(expectedElements);
        expected.add(size, anotherString);

        list.add(size, anotherString);

        assertSameContent(list, expected);
    }

    /** Test for {@link DoublyLinkedList#get(int)}. */
    @Test
    public void testGetInt()
    {
        for (int i = 0; i < size; i++) {
            String expectedElement = expectedElements.get(i);

            String element = list.get(i);

            assertThat(element, is(sameInstance(expectedElement)));
        }
    }

    /** Test for {@link DoublyLinkedList#remove(int)}. */
    @Test
    public void testRemoveInt_atIndex0()
    {
        if (size == 0) {
            thrown.expect(IndexOutOfBoundsException.class);
        }
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedRemoved = expectedList.remove(0);

        String removed = list.remove(0);

        assertThat(removed, is(sameInstance(expectedRemoved)));
        assertSameContent(list, expectedList);
    }

    /** Test for {@link DoublyLinkedList#remove(int)}. */
    @Test
    public void testRemoveInt_inTheMiddle()
    {
        if (size == 0) {
            thrown.expect(IndexOutOfBoundsException.class);
        }
        List<String> expectedList = new ArrayList<>(expectedElements);
        String expectedRemoved = expectedList.remove(size / 2);

        String removed = list.remove(size / 2);

        assertThat(removed, is(sameInstance(expectedRemoved)));
        assertSameContent(list, expectedList);
    }

    /** Test for {@link DoublyLinkedList#remove(int)}. */
    @Test
    public void testRemoveInt_atIndexSizeMinusOne()
    {
        if (size == 0) {
            thrown.expect(IndexOutOfBoundsException.class);
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
    @Test
    public void testAddFirst()
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
    @Test
    public void testAddLast()
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
    @Test
    public void testOfferFirst()
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
    @Test
    public void testOfferLast()
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
    @Test
    public void testRemoveFirst()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
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
    @Test
    public void testRemoveLast()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
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
    @Test
    public void testPollFirst()
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
    @Test
    public void testPollLast()
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
    @Test
    public void testGetFirst()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
        }
        String first = list.getFirst();

        assertThat(first, is(sameInstance(expectedElements.get(0))));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    /**
     * Test for {@link DoublyLinkedList#getLast()}.
     */
    @Test
    public void testGetLast()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
        }
        String last = list.getLast();

        assertThat(last, is(sameInstance(expectedElements.get(size - 1))));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    /** Test for {@link DoublyLinkedList#peekFirst()}. */
    @Test
    public void testPeekFirst()
    {
        String expectedFirst = size > 0 ? expectedElements.get(0) : null;

        String first = list.peekFirst();

        assertThat(first, is(sameInstance(expectedFirst)));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    /** Test for {@link DoublyLinkedList#peekLast()}. */
    @Test
    public void testPeekLast()
    {
        String expectedLast = size > 0 ? expectedElements.get(size - 1) : null;

        String last = list.peekLast();

        assertThat(last, is(sameInstance(expectedLast)));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    /** Test for {@link DoublyLinkedList#removeFirstOccurrence(Object)}. */
    @Test
    public void testRemoveFirstOccurrence()
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
    @Test
    public void testRemoveLastOccurrence()
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
    @Test
    public void testOffer()
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
    @Test
    public void testRemove()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
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
    @Test
    public void testPoll()
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
    @Test
    public void testElement()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
        }
        String first = list.element();

        assertThat(first, is(sameInstance(expectedElements.get(0))));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    /**
     * Test for {@link DoublyLinkedList#peek()}.
     */
    @Test
    public void testPeek()
    {
        String expectedFirst = size > 0 ? expectedElements.get(0) : null;

        String first = list.peek();

        assertThat(first, is(sameInstance(expectedFirst)));
        assertSameContent(list, expectedElements); // ensure content did not change
    }

    // test Stack methods

    /** Test for {@link DoublyLinkedList#push(Object)}. */
    @Test
    public void testPush()
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
    @Test
    public void testPop()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
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
    @Test
    public void testInvert()
    {
        list.invert();

        List<String> expected = new ArrayList<>(expectedElements);
        Collections.reverse(expected);

        assertSameContent(list, expected);
    }

    /**
     * Test for {@link DoublyLinkedList#moveFrom(int, DoublyLinkedList)}.
     */
    @Test
    public void testMoveFrom()
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
    @Test
    public void testAppend()
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
    @Test
    public void testPrepend()
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
    @Test
    public void testCircularIterator()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
            list.circularIterator("anything");
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
    @Test
    public void testReverseCircularIterator()
    {
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
            list.reverseCircularIterator("anything");
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
    @Test
    public void testDescendingIterator()
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
    @Test
    public void testIterator()
    {
        NodeIterator<String> iterator = list.iterator();
        for (int i = 0; i < size; i++) {
            assertThat(iterator.next(), is(sameInstance(expectedElements.get(i))));
        }
        assertFalse(iterator.hasNext());
    }

    /** Test for {@link DoublyLinkedList#listIterator(Object)}. */
    @Test
    public void testListIteratorE()
    { // test only if returned ListIterator starts expected position beginning.
        String element;
        if (size == 0) {
            thrown.expect(NoSuchElementException.class);
            element = null;
        } else {
            element = expectedElements.get(size / 3);
        }

        ListNodeIterator<String> listIterator = list.listIterator(element);

        assertTrue(listIterator.hasNext());
        ListNode<String> firstNode = listIterator.nextNode();
        assertThat(firstNode, is(sameInstance(list.getNode(size / 3))));
        assertThat(firstNode.getValue(), is(sameInstance(element)));
    }

    /** Test for {@link DoublyLinkedList#listIterator(int)}. */
    @Test
    public void testListIteratorInt_indexInTheMiddle_iteratorAtCorrectIndex()
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
    @Test
    public void testListIteratorNext_iterateForwardTroughCompleteList_ListNodesInOrder()
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
    @Test
    public void testListIteratorPrevious_iterateBackwardTroughCompleteList_ListNodesInOrder()
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

    @Test
    public void testListIteratorNextPrevious_forwardBackwardPattern_correctElements()
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

    @Test(expected = NoSuchElementException.class)
    public void testListIterator_iterateBehindTail()
    {
        ListNodeIterator<String> iterator = list.listIterator();
        for (int i = 0; i < size; i++) {
            iterator.next();
        }
        assertFalse(iterator.hasNext());
        iterator.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void testListIterator_iterateBeforeHead()
    {
        ListNodeIterator<String> iterator = list.listIterator(size);
        for (int i = 0; i < size; i++) {
            iterator.previous();
        }
        assertFalse(iterator.hasPrevious());
        iterator.previous();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testListIterator_concurrentAdd_ConcurrentModificationException()
    {
        ListNodeIterator<String> listIterator = list.listIterator();

        list.add("another");

        listIterator.next();
    }

    @Test
    public void testListIterator_concurrentRemove_ConcurrentModificationException()
    {
        if (size == 0) {
            return;
        }
        thrown.expect(ConcurrentModificationException.class);

        ListNodeIterator<String> listIterator = list.listIterator();
        list.removeLast();

        listIterator.next();
    }

    /** Test for {@link DoublyLinkedList.ListNodeIterator#remove()}. */
    @Test
    public void testListIteratorRemove_clearListFromTheFront_emptyList()
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

    @Test
    public void testListIteratorRemove_clearListFromTheMiddle_emptyList()
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

    @Test
    public void testListIteratorRemove_clearListFromTheEnd_emptyList()
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

    @Test(expected = IllegalStateException.class)
    public void testListIteratorRemove_notMovedListIterator_IllegalStateException()
    {
        list.listIterator().remove();
    }

    @Test
    public void testListIteratorRemove_removeTwiceAfterNext_IllegalStateException()
    {
        if (size == 0) {
            return;
        }
        ListNodeIterator<String> listIterator = list.listIterator();
        listIterator.next();
        listIterator.remove();

        // check if the last-node of the iterator is cleared correctly

        thrown.expect(IllegalStateException.class);

        listIterator.remove();
    }

    @Test
    public void testListIteratorRemove_removeAfterAdd_IllegalStateException()
    {
        if (size == 0) {
            return;
        }
        ListNodeIterator<String> listIterator = list.listIterator();
        listIterator.next();
        listIterator.add("Another");

        // check if the last-node of the iterator is cleared correctly in add()

        thrown.expect(IllegalStateException.class);

        listIterator.remove();
    }

    /** Test for {@link DoublyLinkedList.ListNodeIterator#add(Object)}. */
    @Test
    public void testListIteratorAdd_addElementsAtFront_listWithAdditionalElements()
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

    @Test
    public void testListIteratorAdd_addElementsInTheMiddle_listWithAdditionalElements()
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

    @Test
    public void testListIteratorAdd_addElementsAtEnd_listWithAdditionalElements()
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

    @Test
    public void testListIteratorAdd_addElementBeforeEnd_listWithAdditionalElements()
    {
        if (size == 0) {
            return;
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
    @Test
    public void testListIteratorSet_replaceElementInTheMiddle_listWithReplacedElement()
    {
        if (size == 0) {
            return;
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

    @Test
    public void testListIteratorSet_replaceElementAtFront_listWithReplacedElement()
    {
        if (size == 0) {
            return;
        }
        String element = "another";
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.set(0, element);

        ListNodeIterator<String> listIterator = list.listIterator(0);

        listIterator.next();
        listIterator.set(element);
        assertSameContent(list, expectedList);
    }

    @Test
    public void testListIteratorSet_replaceElementInAtEnd_listWithReplacedElement()
    {
        if (size == 0) {
            return;
        }
        String element = "another";
        List<String> expectedList = new ArrayList<>(expectedElements);
        expectedList.set(size - 1, element);

        ListNodeIterator<String> listIterator = list.listIterator(size);

        listIterator.previous();
        listIterator.set(element);
        assertSameContent(list, expectedList);
    }

    @Test
    public void testListIteratorSet_setElementWithSubsequentRemove_listWithReplacedElement()
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

    @Test
    public void testListIteratorSet_setTwice_listWithReplacedElement()
    {
        if (size == 0) {
            return;
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

    @Test(expected = IllegalStateException.class)
    public void testListIteratorSet_NotMovedListIterator_IllegalstateException()
    {
        ListNodeIterator<String> listIterator = list.listIterator();
        listIterator.set("another");
    }

    @Test(expected = IllegalStateException.class)
    public void testListIteratorSet_setAfterAdd_IllegalstateException()
    {
        ListNodeIterator<String> listIterator = list.listIterator();
        listIterator.add("another");
        listIterator.set("another");
    }

    @Test
    public void testListIteratorSet_setAfterRemove_IllegalstateException()
    {
        if (size == 0) {
            return;
        } else {
            thrown.expect(IllegalStateException.class);
        }
        ListNodeIterator<String> listIterator = list.listIterator();
        listIterator.next();
        listIterator.remove();
        listIterator.set("another");
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
    private static ListNode<String> createFreeListNode(String element)
    {

        DoublyLinkedList<String> list = createDoublyLinkedList(Collections.singletonList(element));

        ListNode<String> node = list.getNode(0);
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
