/*
 * (C) Copyright 2005-2017, by Trevor Harmon and Contributors.
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

/**
 * Generates names by invoking {@link #toString()} on them. This assumes that the object's
 * {@link #toString()} method returns a unique string representation.
 *
 * @param <T> the component type
 * 
 * @author Trevor Harmon
 */
public class StringComponentNameProvider<T>
    implements
    ComponentNameProvider<T>
{

    /**
     * Returns the string representation of a component.
     *
     * @param component the component
     * @return a unique string representation
     */
    @Override
    public String getName(T component)
    {
        return component.toString();
    }
}
