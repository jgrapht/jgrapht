/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */
/* ------------------
 * ComponentAttributeProvider.java
 * ------------------
 * (C) Copyright 2010-2010, by John Sichi and Contributors.
 *
 * Original Author:  John Sichi
 *
 * $Id$
 *
 * Changes
 * -------
 * 12-Jun-2010 : Initial Version (JVS);
 *
 */
package org.jgrapht.ext;

import java.util.*;


/**
 * Provides display attributes for vertices and/or edges in a graph.
 *
 * @author John Sichi
 * @version $Id$
 */
public interface ComponentAttributeProvider<T>
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Returns a set of attribute key/value pairs for a vertex or edge. If order
     * is important in the output, be sure to use an order-deterministic map
     * implementation.
     *
     * @param component vertex or edge for which attributes are to be obtained
     *
     * @return key/value pairs, or null if no attributes should be supplied
     */
    Map<String, String> getComponentAttributes(T component);
}

// End ComponentAttributeProvider.java
