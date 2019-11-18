/*
 * (C) Copyright 2019-2019, by Amr ALHOSSARY and Contributors.
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
package org.jgrapht.io;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for IntegerComponentNameProvider
 * 
 * @author Amr ALHOSSARY
 */
@Deprecated
public class IntegerComponentNameProviderTest
{

    @Test
    public void testConstructors()
    {
        IntegerComponentNameProvider<Object> provider = new IntegerComponentNameProvider<>();
        String id1 = provider.getName(new Object());
        assertEquals("1", id1);
        String id2 = provider.getName(new Object());
        assertEquals("2", id2);

        provider = new IntegerComponentNameProvider<>(0);
        id1 = provider.getName(new Object());
        assertEquals("0", id1);
        id2 = provider.getName(new Object());
        assertEquals("1", id2);
    
    }

    @Test
    public void testClear()
    {
        IntegerComponentNameProvider<Object> provider = new IntegerComponentNameProvider<>();
        String id1 = provider.getName(new Object());
        assertEquals("1", id1);
        String id2 = provider.getName(new Object());
        assertEquals("2", id2);
        provider.clear();
        id1 = provider.getName(new Object());
        assertEquals("1", id1);
        id2 = provider.getName(new Object());
        assertEquals("2", id2);
    }
}
