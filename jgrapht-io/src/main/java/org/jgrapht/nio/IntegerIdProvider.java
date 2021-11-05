/*
 * (C) Copyright 2005-2021, by Trevor Harmon and Contributors.
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
package org.jgrapht.nio;

import java.util.*;
import java.util.function.*;

/**
 * Assign a unique integer identifier to a set of elements.
 * 
 * Each instance of provider maintains an internal map between every element it has ever seen and
 * the unique integer representing that element.
 * 
 * @param <T> the element type
 *
 * @author Trevor Harmon
 */
public class IntegerIdProvider<T>
    implements
    Function<T, String>
{
    private int nextId = 1;
    private final Map<T, Integer> idMap;

    /**
     * Create a new provider
     */
    public IntegerIdProvider()
    {
        this(1);
    }

    /**
     * Create a new provider.
     * 
     * @param nextId identifier to start from
     */
    public IntegerIdProvider(int nextId)
    {
        this.nextId = nextId;
        this.idMap = new HashMap<>();
    }

    @Override
    public String apply(T t)
    {
        Integer id = idMap.get(t);
        if (id == null) {
            id = nextId++;
            idMap.put(t, id);
        }
        return String.valueOf(id);
    }
}
