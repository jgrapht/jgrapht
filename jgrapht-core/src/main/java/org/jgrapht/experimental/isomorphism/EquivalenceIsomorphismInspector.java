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
 * EquivalenceIsomorphismInspector.java
 * -----------------
 * (C) Copyright 2005-2008, by Assaf Lehr and Contributors.
 *
 * Original Author:  Assaf Lehr
 * Contributor(s):   -
 *
 * $Id: EquivalenceIsomorphismInspector.java 485 2006-06-26 09:12:14Z
 * perfecthash $
 *
 * Changes
 * -------
 */
package org.jgrapht.experimental.isomorphism;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.experimental.equivalence.*;
import org.jgrapht.experimental.permutation.*;


/**
 * The current implementation uses the vertexComparator to greatly increase the
 * test speed by dividing the vertexes into equivalent groups and permuting
 * inside them only. The EdgeComparator is used to test edges, but not to make a
 * finer division, thus it adds overhead. Use it only when needed.
 *
 * @author Assaf
 * @since Jul 29, 2005
 */
class EquivalenceIsomorphismInspector<V, E>
    extends AbstractExhaustiveIsomorphismInspector<V, E>
{
    

    /**
     * @param graph1
     * @param graph2
     * @param vertexChecker eq. group checker for vertexes. If null,
     * UniformEquivalenceComparator will be used as default (always return true)
     * @param edgeChecker eq. group checker for edges. If null,
     * UniformEquivalenceComparator will be used as default (always return true)
     */
    public EquivalenceIsomorphismInspector(
        Graph<V, E> graph1,
        Graph<V, E> graph2,

        // XXX hb 060128: FOllowing parameter may need Graph<? super V,? super
        // E>
        EquivalenceComparator<? super V, ? super Graph<? super V, ? super E>> vertexChecker,
        EquivalenceComparator<? super E, ? super Graph<? super V, ? super E>> edgeChecker)
    {
        super(graph1, graph2, vertexChecker, edgeChecker);
    }

    /**
     * Constructor which uses the default comparators.
     *
     * @see ExhaustiveIsomorphismInspector(Graph,Graph,EquivalenceComparator,EquivalenceComparator)
     */
    public EquivalenceIsomorphismInspector(
        Graph<V, E> graph1,
        Graph<V, E> graph2)
    {
        super(graph1, graph2);
    }

    

    /**
     * Creates the permutation iterator according to equivalance class.
     *
     * <p>1. Get the eq.group (ordered by size) array of the source vertex set
     * (vertexSet1)
     *
     * <p>2. Get the eq.group ordered array of vertexSet2.
     *
     * <p>3. Reorder the second array to match the group order of the first
     * array sets. 4. Use CompoundPermutationIter (and not regular
     * IntegerPermutationIter) to permute only inside groups.
     *
     * <p>
     * <p>That's it. If the eq.group comaparator is strong enough to provide
     * small groups, this algortihm will produce a small possible permutations
     * numbers. example: G1: [A,B,F,X,Y] [A->B,B->X,X->Y]
     *
     * <p>G2: [D,Z,C,U,F] [D->C,Z->C,U->Z]
     *
     * <p>vertexEq: three groups , one all letters A-E , second all letters S-Z
     * , third the letter 'f'. 1. [(f)size=1, (X,Y)size=2 , (A,B)size=2] 2.
     * [(f)size=1 ,(C,D)size=2 , (Z,U)size=2] 3. the match is done by reordering
     * the second array to have the equiviavlant order :##[(f)size=1 ,
     * (Z,U)size=2 , (C,D)size=2]## 4.for example G2 will not do all 5!=120
     * permutations , but 2!x2!x1!=4 permutations only which are: (of the 3rd
     * array) [ F, Z , U , C , D ] [ F, Z , U , D , C ] [ F, U , Z, C , D ] [ F,
     * U , Z , D , C ]
     *
     * @return null, if the eq.group do not match (there cannot be any
     * permutation for eq.groups) or the sets do not match in size; otherwise,
     * the permutationiterator otherwise
     *
     * @see AbstractExhaustiveIsomorphismInspector#createPermutationIterator(Set,
     * Set)
     */
    @SuppressWarnings("unchecked")
    protected CollectionPermutationIter<V> createPermutationIterator(
        Set<V> vertexSet1,
        Set<V> vertexSet2)
    {
        if (vertexSet1.size() != vertexSet2.size()) {
            // throw new IllegalArgumentException("the two vertx-sets
            // parameters must be of"
            // +"the same size. The first size was:"+vertexSet1.size()
            // +" the other size was:" +vertexSet2.size() );
            return null; // only instead of exception
        }

        // 1//
        EquivalenceSet [] eqGroupArray1 =
            EquivalenceSetCreator.createEqualityGroupOrderedArray(
                vertexSet1,
                this.vertexComparator,
                this.graph1);

        // 2//
        EquivalenceSet [] eqGroupArray2 =
            EquivalenceSetCreator.createEqualityGroupOrderedArray(
                vertexSet2,
                this.vertexComparator,
                this.graph2);

        // 3//
        boolean reorderSuccess =
            reorderTargetArrayToMatchSourceOrder(eqGroupArray1, eqGroupArray2); // 2 is the target
        if (!reorderSuccess) {
            // if reordering fail , no match can be done
            return null;
        }

        // reorder set1 (source), so when we work with the flat array of the
        // second array,
        // the permutations will be relevant.
        // note that it does not start in any way related to eqGroup sizes.

        V [] reorderingVertexSet1Temp = (V []) new Object[vertexSet1.size()];
        fillElementsflatArray(eqGroupArray1, reorderingVertexSet1Temp);
        vertexSet1.clear();
        vertexSet1.addAll(Arrays.asList(reorderingVertexSet1Temp));

        // 4//use CompoundPermutationIter to permute only inside groups.
        // the CollectionPermutationIter needs a array/set of objects and a
        // permuter which will
        // work on that set/array order. lets make these two:
        // 1. create array of the vertexes , by flattening the eq.group array
        // contents

        V [] flatVertexArray = (V []) new Object[vertexSet2.size()];
        fillElementsflatArray(eqGroupArray2, flatVertexArray);

        // 2. make the permuter according to the groups size
        int [] groupSizesArray = new int[eqGroupArray1.length];

        // iterate over the EqualityGroup array
        for (
            int eqGroupCounter = 0;
            eqGroupCounter < eqGroupArray2.length;
            eqGroupCounter++)
        {
            // now for (.2.) size count
            groupSizesArray[eqGroupCounter] =
                eqGroupArray2[eqGroupCounter].size();
        }

        ArrayPermutationsIter arrayPermIter =
            PermutationFactory.createByGroups(groupSizesArray);
        CollectionPermutationIter<V> vertexPermIter =
            new CollectionPermutationIter<V>(
                Arrays.asList(flatVertexArray),
                arrayPermIter);

        return vertexPermIter;
    }

    /**
     * Reorders inplace targetArray
     *
     * <p>rules:
     * <li>try to match only group of the same size and then hashcode
     * <li>it is enough to choose one from each group to see if a match exist.
     *
     * <p>Algorithm: hold counters in the two arrays. [a,b,c,d,e] assume groups
     * are:a,(b,c,d),e [a,c,d,b,e] c1=0 , c2=0 check if eqvivalent . if not ,
     * advance , as long as both size and hashcode are the same. if found a
     * match , swap the group positions in array2. if not , throws
     * IllegalArgumentExcpetion. Assumption: array size is the same. not
     * checked.
     *
     * @param sourceArray
     * @param targetArray
     *
     * @return true if the array was reordered successfully. false if not(It
     * will happen if there is no complete match between the groups)
     */
    private boolean reorderTargetArrayToMatchSourceOrder(
        EquivalenceSet [] sourceArray,
        EquivalenceSet [] targetArray)
    {
        boolean result = true;
        for (
            int sourceIndex = 0;
            sourceIndex < sourceArray.length;
            sourceIndex++)
        {
            int currTargetIndex = sourceIndex;

            // if they are already equivalent do nothing.
            EquivalenceSet sourceEqGroup = sourceArray[sourceIndex];
            EquivalenceSet targetEqGroup = targetArray[currTargetIndex];
            if (!sourceEqGroup.equals(targetEqGroup)) {
                // iterate through the next group in the targetArray until
                // a new size or hashcode is seen
                boolean foundMatch = false;
                int sourceSize = sourceEqGroup.size();
                int sourceHashCode = sourceEqGroup.hashCode();
                while (
                    (targetEqGroup.size() == sourceSize)
                    && (targetEqGroup.hashCode() == sourceHashCode)
                    && (currTargetIndex < (targetArray.length - 1)))
                {
                    currTargetIndex++;
                    targetEqGroup = targetArray[currTargetIndex];
                    if (targetEqGroup.equals(sourceEqGroup)) {
                        foundMatch = true;

                        // swap . targetEqGroup will serve as the temp
                        // variable.
                        targetArray[currTargetIndex] = targetArray[sourceIndex];
                        targetArray[sourceIndex] = targetEqGroup;
                    }
                }
                if (!foundMatch) {
                    // a match was not found
                    // throw new IllegalArgumentException("could not reorder
                    // the array , because the groups don`t match");
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @param eqGroupArray
     * @param flatArray an empy array with the proper size
     */
    protected void fillElementsflatArray(
        EquivalenceSet [] eqGroupArray,
        Object [] flatVertexArray)
    {
        int flatVertexArrayNextFree = 0; // the next free place in the array

        // iterate over the EqualityGroup array
        for (
            int eqGroupCounter = 0;
            eqGroupCounter < eqGroupArray.length;
            eqGroupCounter++)
        {
            Object [] currGroupArray = eqGroupArray[eqGroupCounter].toArray();

            // copy this small array to the free place in the big
            // flatVertexArray
            System.arraycopy(
                currGroupArray, // src
                0, // srcPos
                flatVertexArray, // dest
                flatVertexArrayNextFree, // destPos
                currGroupArray.length // length
                );
            flatVertexArrayNextFree += currGroupArray.length;
        }
    }

    /**
     * We know for sure, that the sets are alreay checked for equivalence , so
     * it will return true without any further checks.
     *
     * @see AbstractExhaustiveIsomorphismInspector#areVertexSetsOfTheSameEqualityGroup(
     * Set, Set)
     */
    protected boolean areVertexSetsOfTheSameEqualityGroup(
        Set vertexSet1,
        Set vertexSet2)
    {
        return true;
    }
}

// End EquivalenceIsomorphismInspector.java
