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
/* -----------------
 * TypeUtil.java
 * -----------------
 * (C) Copyright 2006-2008, by John V. Sichi and Contributors.
 *
 * Original Author:  John V. Sichi
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 07-May-2006 : Initial version (JVS);
 */
package org.jgrapht.util;

/**
 * TypeUtil isolates type-unsafety so that code which uses it for legitimate
 * reasons can stay warning-free.
 *
 * @author John V. Sichi
 */
public class TypeUtil<T>
{
    //~ Methods ----------------------------------------------------------------

    /**
     * Casts an object to a type.
     *
     * @param o object to be cast
     * @param typeDecl conveys the target type information; the actual value is
     * unused and can be null since this is all just stupid compiler tricks
     *
     * @return the result of the cast
     */
    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(final Object o, final TypeUtil<T> typeDecl)
    {
        return (T) o;
    }
}

// End TypeUtil.java
