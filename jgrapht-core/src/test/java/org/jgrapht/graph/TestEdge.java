/*
 * (C) Copyright 2003-2021, by Christoph Zauner and Contributors.
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
package org.jgrapht.graph;

import java.util.Objects;

/**
 * {@link org.jgrapht.graph.DefaultEdge} does not implement hashCode() or equals(). Therefore
 * comparing two graphs does not work as expected out of the box.
 *
 * @author Christoph Zauner
 */
public class TestEdge
    extends
    DefaultEdge
{

    private static final long serialVersionUID = 1L;

    public TestEdge()
    {
        super();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(source, target);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestEdge other = (TestEdge) obj;
        return Objects.equals(source, other.source) && Objects.equals(target, other.target);
    }

}
