/*
 * (C) Copyright 2015-2017, by Wil Selwood and Contributors.
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
