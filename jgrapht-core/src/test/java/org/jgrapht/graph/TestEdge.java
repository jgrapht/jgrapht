/*
 * (C) Copyright 2003-2018, by Christoph Zauner and Contributors.
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSource() == null) ? 0 : getSource().hashCode());
        result = prime * result + ((getTarget() == null) ? 0 : getTarget().hashCode());
        return result;
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
        if (getSource() == null) {
            if (other.getSource() != null)
                return false;
        } else if (!getSource().equals(other.getSource()))
            return false;
        if (getTarget() == null) {
            if (other.getTarget() != null)
                return false;
        } else if (!getTarget().equals(other.getTarget()))
            return false;
        return true;
    }

}
