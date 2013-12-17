/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
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
/* -------------------
 * IntrusiveEdge.java
 * -------------------
 * (C) Copyright 2006-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 28-May-2006 : Initial revision (JVS);
 *
 */
package org.jgrapht.graph;

import java.io.*;


/**
 * IntrusiveEdge encapsulates the internals for the default edge implementation.
 * It is not intended to be referenced directly (which is why it's not public);
 * use DefaultEdge for that.
 *
 * @author John V. Sichi
 */
class IntrusiveEdge
    implements Cloneable,
        Serializable
{
    

    private static final long serialVersionUID = 3258408452177932855L;

    

    Object source;

    Object target;

    

    /**
     * @see Object#clone()
     */
    public Object clone()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // shouldn't happen as we are Cloneable
            throw new InternalError();
        }
    }
}

// End IntrusiveEdge.java
