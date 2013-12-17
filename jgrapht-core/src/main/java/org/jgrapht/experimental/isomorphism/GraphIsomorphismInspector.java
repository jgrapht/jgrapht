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
/* -----------------
 * GraphIsomorphismInspector.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism;

import java.util.*;


/**
 * <b>Isomorphism Overview</b>
 *
 * <p>Isomorphism is the problem of testing whether two graphs are topologically
 * the same. Suppose we are given a collection of graphs and must perform some
 * operation on each of them. If we can identify which of the graphs are
 * duplicates, they can be discarded so as to avoid redundant work.
 *
 * <p>In Formal Math: <i>Input description:</i> Two graphs, G and H. <i>Problem
 * description:</i> Find a (or all) mappings f of the vertices of G to the
 * vertices of H such that G and H are identical; i.e. (x,y) is an edge of G iff
 * (f(x),f(y)) is an edge of H. <a
 * href="http://www2.toki.or.id/book/AlgDesignManual/BOOK/BOOK4/NODE180.HTM">
 * http://www2.toki.or.id/book/AlgDesignManual/BOOK/BOOK4/NODE180.HTM</a>.
 *
 * <p><i>Efficiency:</i> The general algorithm is not polynomial, however
 * polynomial algorithms are known for special cases, like acyclic graphs,
 * planar graphs etc. There are several heuristic algorithms which gives quite
 * good results (polynomial) in general graphs, for most but not all cases.
 *
 * <p><b>Usage:</b>
 *
 * <ol>
 * <li>Choose comparators for the vertexes and edges. You may use the default
 * comparator by sending null parameters for them to the constructor. Example:
 * Assume Your graphs are of human relations. Each vertex is either a man or a
 * woman and also has the person name. You may decide that isomorphism is
 * checked according to gender, but not according to the specific name. So you
 * will create a comparator that distinguishes vertexes only according to
 * gender.
 * <li>Use the isIsomorphic() method as a boolean test for isomorphism
 * <li>Use the Iterator interface to iterate through all the possible
 * isomorphism ordering.
 * </ol>
 *
 * @author Assaf Lehr
 * @since Jul 15, 2005
 */
// REVIEW jvs 5-Sept-2005:  Since we're using JDK1.5 now, we should be
// able to declare this as Iterator<GraphMapping>, correct?  Otherwise
// the caller doesn't even know what they're getting back.
public interface GraphIsomorphismInspector<E>
    extends Iterator<E>
{
    

    /**
     * @return <code>true</code> iff the two graphs are isomorphic
     */
    public boolean isIsomorphic();
}

// End GraphIsomorphismInspector.java
