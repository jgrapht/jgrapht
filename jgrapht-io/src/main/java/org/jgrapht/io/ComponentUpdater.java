/*
 * (C) Copyright 2015-2017, by Wil Selwood and Contributors.
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

import java.util.*;

/**
 * Type to handle updates to a component when an import gets more information about it after it has
 * been created.
 *
 * @param <T> the component type
 */
public interface ComponentUpdater<T>
{
    /**
     * Update component with the extra attributes.
     *
     * @param component to update
     * @param attributes to add to the component
     */
    void update(T component, Map<String, Attribute> attributes);
}

// End ComponentUpdater.java
