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
 * EquivalenceComparator.java
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
package org.jgrapht.experimental.equivalence;

/**
 * This interface distinguishes between Equivalence sets.
 *
 * <p>It is similar, in concept, to the Object.hashcode() and Object.equals()
 * methods, but instead of checking whether two objects are equal, it is used to
 * check whether they are part of the same Equivalence group, where the
 * definition of an "equivalence" is defined by the implementation of this
 * interface.
 *
 * <p>A specific usage of it is shown below, but it may be used outside of the
 * graph-theory class library.
 *
 * <p>In Isomorphism, edges/vertexes matching may relay on none/some/all of the
 * vertex/edge properties. For example, if a vertex representing a person
 * contains two properties: gender(male/female) and person name(string), we can
 * decide that to check isomorphism in vertex groups of gender only. Meaning if
 * this is the graph:
 *
 * <p>(male,"Don")---->(female,"Dana")--->(male,"John")
 *
 * <p>if there is no equivalence set at all , this graph can be described as:
 * (1)---->(2)---->(3)
 *
 * <p>if the equivalence set is determined only by the gender property :
 * (male)---->(female)---->(male)
 *
 * <p>and if it is determined by both properties: (the original figure) The
 * isomorphism inspection may return different result according to this choice.
 * If the other graph is: (male,"Don")--->(male,"Sunny")---->(male,"Jo") In no
 * eq.set they are Isomorphic, but for the two other cases they are not. Other
 * examples: Nodes with the same degree, Edges with the same weight, Graphs with
 * the same number of nodes and edges.
 *
 * @param <E> the type of the elements in the set
 * @param <C> the type of the context the element is compared against, e.g. a
 * Graph
 *
 * @author Assaf
 * @since Jul 15, 2005
 */
public interface EquivalenceComparator<E, C>
{
    

    public boolean equivalenceCompare(
        E arg1,
        E arg2,
        C context1,
        C context2);

    public int equivalenceHashcode(E arg1, C context);
}

// End EquivalenceComparator.java
