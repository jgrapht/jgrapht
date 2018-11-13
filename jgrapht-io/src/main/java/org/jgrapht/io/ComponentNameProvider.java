/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
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
 * Provides a name for a component.
 *
 * @param <T> the type of the component
 */
public interface ComponentNameProvider<T>
{

    /**
     * Returns a unique name. This is useful when exporting a graph, as it ensures that all
     * vertices/edges are assigned simple, consistent names.
     *
     * @param component the component to be named
     * @return the name of the component
     */
    String getName(T component);

}
