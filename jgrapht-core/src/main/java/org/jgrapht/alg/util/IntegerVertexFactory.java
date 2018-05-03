/*
 * (C) Copyright 2003-2018, by Assaf Lehr and Contributors. 
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
package org.jgrapht.alg.util;

import org.jgrapht.*;

/**
 * Default implementation of a vertex factory which creates integers. The vertices start by default
 * from zero.
 * 
 * @author Assaf Lehr
 * @deprecated Use suppliers instead
 */
@Deprecated
public class IntegerVertexFactory
    implements VertexFactory<Integer>
{
    private int counter = 0;

    /**
     * Constructs a new IntegerVertexFactory. The first vertex returned has value 0.
     */
    public IntegerVertexFactory()
    {
        this(0);
    }

    /**
     * Constructs a new IntegerVertexFactory.
     * 
     * @param counter Starting value of the first vertex returned.
     */
    public IntegerVertexFactory(int counter)
    {
        this.counter = counter;
    }

    @Override
    public Integer createVertex()
    {
        return counter++;
    }

}
