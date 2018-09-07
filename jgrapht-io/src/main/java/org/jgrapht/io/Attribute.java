/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
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
package org.jgrapht.io;

/**
 * An attribute
 * 
 * @author Dimitrios Michail
 */
public interface Attribute
{
    /**
     * Get the value of the attribute
     * 
     * @return the value of the attribute
     */
    String getValue();

    /**
     * Get the type of the attribute
     * 
     * @return the type of the attribute
     */
    AttributeType getType();

}
