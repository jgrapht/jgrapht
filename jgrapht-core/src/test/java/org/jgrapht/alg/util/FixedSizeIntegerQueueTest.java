/*
 * (C) Copyright 2017-2021, by Joris Kinable and Contributors.
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
package org.jgrapht.alg.util;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Tests for FixedSizeIntegerQueue
 * 
 * @author Joris Kinable
 */
public class FixedSizeIntegerQueueTest
{

    @Test
    public void testQueue()
    {
        FixedSizeIntegerQueue queue = new FixedSizeIntegerQueue(10);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());

        queue.enqueue(1);
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.size());

        int v = queue.poll();
        assertEquals(1, v);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());

        queue.enqueue(2);
        assertFalse(queue.isEmpty());
        queue.clear();
        assertTrue(queue.isEmpty());
    }
}
