/*
 * (C) Copyright 2018-2018, by Oliver Feith, Daniel Mock and Contributors.
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
package org.jgrapht.util;

import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author Ira Justus Fesefeldt
 * @author Daniel Mock
 */
public class SubSplitQueueTest {

    private SubSplitQueue qNoOrder, qBackwardOrder, qMixedOrder;
    private int[] noOrder = {0,1,2,3,4,5,6,7,8,9};
    private int[] backwardOrder = {9,8,7,6,5,4,3,2,1,0};
    private int[] mixedOrder = {0,9,1,8,2,7,3,6,4,5};
    
    @Before
    public void init() {
        qNoOrder = SubSplitQueue.subSplitQueueFactory(10);
        qBackwardOrder = SubSplitQueue.subSplitQueueFactory(10, backwardOrder.clone());
        qMixedOrder = SubSplitQueue.subSplitQueueFactory(10, mixedOrder.clone() );
    }
    
    
    @Test
    public void testPollAndPeek() {
        //to split elements as arrays
        int[] noOrderSplit = {5,6,7};
        int[] backwardSplit = {7,6,5};
        int[] mixedSplit = {7,6,5};
        
        //to split elements as sets
        Set<Integer> elements = new HashSet<>();
        elements.add(5);
        elements.add(6);
        elements.add(7);
        
        //split
        SubSplitQueue qNoOrder2 = qNoOrder.split(noOrderSplit.clone());
        SubSplitQueue qBackwardOrder2 = qBackwardOrder.split(backwardSplit.clone());
        SubSplitQueue qMixedOrder2 = qMixedOrder.split(mixedSplit.clone());

        //test
        testPollAndPeekHelp(qNoOrder, qNoOrder2, noOrder, elements);
        testPollAndPeekHelp(qBackwardOrder, qBackwardOrder2, backwardOrder, elements);
        testPollAndPeekHelp(qMixedOrder, qMixedOrder2, mixedOrder, elements);
    }
    
    public void testPollAndPeekHelp(SubSplitQueue queue, SubSplitQueue queue2, int[] order, Set<Integer> elements) {
        //init size
        int size1 = order.length-elements.size();
        int size2 = elements.size();
        
        //test size
        assertEquals(queue.getSize(),size1);
        assertEquals(queue2.getSize(),size2);
        
        //test elements
        for(int i = 0; i < 10; i++) {
            if(elements.contains(order[i])) {
                //test order[i]
                assertEquals(order[i], queue2.peek());
                assertEquals(order[i], queue2.poll());
                size2--;
                assertFalse(queue.contains(order[i]));
                
                //test size
                assertEquals(queue.getSize(),size1);
                assertEquals(queue2.getSize(),size2);
            } else {
                //test order[i]
                assertEquals(order[i], queue.peek());
                assertEquals(order[i], queue.poll());
                size1--;
                assertFalse(queue.contains(order[i]));
                
                //test size
                assertEquals(queue.getSize(),size1);
                assertEquals(queue2.getSize(),size2);
            }
        }
    }
    
    @Test
    public void testSizeAndArray() {
        //init arrays
        int[] noOrderSplit = {0,1,2,3,4,5};
        int[] backwardSplit = {5,4,3,2,1,0};
        int[] mixedSplit = {0,1,2,3,4,5};
        int[] noOrderRemain = {6,7,8,9};
        int[] backwardRemain = {9,8,7,6};
        int[] mixedRemain = {9,8,7,6};
        
        //split
        SubSplitQueue qNoOrder2 = qNoOrder.split(noOrderSplit.clone());
        SubSplitQueue qBackwardOrder2 = qBackwardOrder.split(backwardSplit.clone());
        SubSplitQueue qMixedOrder2 = qMixedOrder.split(mixedSplit.clone());
        
        //get size test
        assertEquals(4,qNoOrder.getSize());
        assertEquals(4,qBackwardOrder.getSize());
        assertEquals(4,qMixedOrder.getSize());
        assertEquals(6,qNoOrder2.getSize());
        assertEquals(6,qBackwardOrder2.getSize());
        assertEquals(6,qMixedOrder2.getSize());
        
        //test asArray
        assertTrue(Arrays.equals(noOrderRemain ,qNoOrder.asArray()));
        assertTrue(Arrays.equals(backwardRemain ,qBackwardOrder.asArray()));
        assertTrue(Arrays.equals(mixedRemain ,qMixedOrder.asArray()));
        assertTrue(Arrays.equals(noOrderSplit,qNoOrder2.asArray()));
        assertTrue(Arrays.equals(backwardSplit,qBackwardOrder2.asArray()));
        assertTrue(Arrays.equals(mixedSplit,qMixedOrder2.asArray()));        
    }
    
    @Test
    public void testIterator() {
        //init arrays
        int[] noOrderSplit = {0};
        int[] backwardSplit = {0};
        int[] mixedSplit = {0};
        int[] noOrderRemain = {1,2,3,4,5,6,7,8,9};
        int[] backwardRemain = {9,8,7,6,5,4,3,2,1};
        int[] mixedRemain = {9,1,8,2,7,3,6,4,5};
        
        //split
        SubSplitQueue qNoOrder2 = qNoOrder.split(noOrderSplit.clone());
        SubSplitQueue qBackwardOrder2 = qBackwardOrder.split(backwardSplit.clone());
        SubSplitQueue qMixedOrder2 = qMixedOrder.split(mixedSplit.clone());
        
        testIteratorHelp(qNoOrder.iterator(), qNoOrder, noOrderRemain);
        testIteratorHelp(qBackwardOrder.iterator(), qBackwardOrder, backwardRemain);
        testIteratorHelp(qMixedOrder.iterator(), qMixedOrder, mixedRemain);
        testIteratorHelp(qNoOrder2.iterator(), qNoOrder2, noOrderSplit);
        testIteratorHelp(qBackwardOrder2.iterator(), qBackwardOrder2, backwardSplit);
        testIteratorHelp(qMixedOrder2.iterator(), qMixedOrder2, mixedSplit);
    }
    
    public void testIteratorHelp(Iterator<Integer> iter, SubSplitQueue queue, int[] arrayOfQueue) {
        int i = 0;
        while(iter.hasNext()) {
            int next = iter.next();
            assertTrue(queue.contains(next));
            assertEquals(arrayOfQueue[i],next);
            i++;
            iter.remove();
        }
        assertEquals(0,queue.getSize());
    }
    
    @Test
    public void testEmptySplit() {
        //split
        SubSplitQueue qNoOrder2 = qNoOrder.split(new int[] {});
        SubSplitQueue qBackwardOrder2 = qBackwardOrder.split(new int[] {});
        SubSplitQueue qMixedOrder2 = qMixedOrder.split(new int[] {});
        
        //size test
        assertEquals(10,qNoOrder.getSize());
        assertEquals(10,qBackwardOrder.getSize());
        assertEquals(10,qMixedOrder.getSize());
        assertEquals(0,qNoOrder2.getSize());
        assertEquals(0,qBackwardOrder2.getSize());
        assertEquals(0,qMixedOrder2.getSize());
        
        //test as array
        assertTrue(Arrays.equals(noOrder ,qNoOrder.asArray()));
        assertTrue(Arrays.equals(backwardOrder ,qBackwardOrder.asArray()));
        assertTrue(Arrays.equals(mixedOrder ,qMixedOrder.asArray()));
        assertTrue(Arrays.equals(new int[] {},qNoOrder2.asArray()));
        assertTrue(Arrays.equals(new int[] {},qBackwardOrder2.asArray()));
        assertTrue(Arrays.equals(new int[] {},qMixedOrder2.asArray()));
        
        //test isEmoty
        assertTrue(qNoOrder2.isEmpty());
        assertTrue(qBackwardOrder2.isEmpty());
        assertTrue(qMixedOrder2.isEmpty());
        assertFalse(qNoOrder.isEmpty());
        assertFalse(qBackwardOrder.isEmpty());
        assertFalse(qMixedOrder.isEmpty());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInputCheckNoOrder() {
        qNoOrder.remove(-1);
        
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInputCheckMixedOrder1() {
        qMixedOrder.remove(11);
    }
    
    @Test(expected=NoSuchElementException.class)
    public void testRemoveNoOrder() {
        qNoOrder.remove(0);
        qNoOrder.remove(0);
    }
    
    @Test(expected=NoSuchElementException.class)
    public void testRemoveMixedOrder1() {
        qMixedOrder.remove(0);
        qMixedOrder.remove(0);
    }
    
}