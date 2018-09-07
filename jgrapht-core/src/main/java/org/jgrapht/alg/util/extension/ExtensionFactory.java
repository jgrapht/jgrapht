/*
 * (C) Copyright 2015-2018, by Alexey Kudinkin and Contributors.
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
package org.jgrapht.alg.util.extension;

/**
 * Factory class which creates extension/encapsulation objects
 * 
 * @param <B> class-type of extension
 */
public interface ExtensionFactory<B extends Extension>
{
    /**
     * Factory method which creates a new object which extends Extension
     * 
     * @return new object which extends Extension
     */
    B create();
}
