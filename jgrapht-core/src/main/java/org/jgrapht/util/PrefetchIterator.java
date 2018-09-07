/*
 * (C) Copyright 2005-2018, by Assaf Lehr and Contributors.
 *
 * JGraphT : a free Java graph-theory library
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
package org.jgrapht.util;

import java.util.*;

/**
 * Utility class to help implement an iterator/enumerator in which the hasNext() method needs to
 * calculate the next elements ahead of time.
 *
 * <p>
 * Many classes which implement an iterator face a common problem: if there is no easy way to
 * calculate hasNext() other than to call getNext(), then they save the result for fetching in the
 * next call to getNext(). This utility helps in doing just that.
 *
 * <p>
 * <b>Usage:</b> The new iterator class will hold this class as a member variable and forward the
 * hasNext() and next() to it. When creating an instance of this class, you supply it with a functor
 * that is doing the real job of calculating the next element.
 *
 * <pre>
 * <code>
 *  //This class supplies enumeration of integer till 100.
 *  public class IteratorExample implements Enumeration{
 *  private int counter=0;
 *  private PrefetchIterator nextSupplier;
 *
 *      IteratorExample()
 *      {
 *          nextSupplier = new PrefetchIterator(new PrefetchIterator.NextElementFunctor(){
 *
 *              public Object nextElement() throws NoSuchElementException {
 *                  counter++;
 *                  if (counter &lt;= 100)
 *                      throw new NoSuchElementException();
 *                  else
 *                      return new Integer(counter);
 *              }
 *
 *          });
 *      }
 *      
 *      // forwarding to nextSupplier and return its returned value
 *      public boolean hasMoreElements() {
 *          return this.nextSupplier.hasMoreElements();
 *      }
 *      
 *      // forwarding to nextSupplier and return its returned value
 *      public Object nextElement() {
 *          return this.nextSupplier.nextElement();
 *      }
 *  }</code>
 * </pre>
 * 
 * @param <E> the element type
 *
 * @author Assaf Lehr
 */
public class PrefetchIterator<E>
    implements
    Iterator<E>,
    Enumeration<E>
{
    private NextElementFunctor<E> innerEnum;
    private E getNextLastResult;
    private boolean isGetNextLastResultUpToDate = false;
    private boolean endOfEnumerationReached = false;
    private boolean flagIsEnumerationStartedEmpty = true;
    private int innerFunctorUsageCounter = 0;

    /**
     * Construct a new prefetch iterator.
     * 
     * @param aEnum the next element functor
     */
    public PrefetchIterator(NextElementFunctor<E> aEnum)
    {
        innerEnum = aEnum;
    }

    /**
     * Serves as one contact place to the functor; all must use it and not directly the
     * NextElementFunctor.
     */
    private E getNextElementFromInnerFunctor()
    {
        innerFunctorUsageCounter++;
        E result = this.innerEnum.nextElement();

        // if we got here , an exception was not thrown, so at least
        // one time a good value returned
        flagIsEnumerationStartedEmpty = false;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E nextElement()
    {
        /*
         * 1. Retrieves the saved value or calculates it if it does not exist 2. Changes
         * isGetNextLastResultUpToDate to false. (Because it does not save the NEXT element now; it
         * saves the current one!)
         */
        E result;
        if (this.isGetNextLastResultUpToDate) {
            result = this.getNextLastResult;
        } else {
            result = getNextElementFromInnerFunctor();
        }

        this.isGetNextLastResultUpToDate = false;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMoreElements()
    {
        /*
         * If (isGetNextLastResultUpToDate==true) returns true else 1. calculates getNext() and
         * saves it 2. sets isGetNextLastResultUpToDate to true.
         */
        if (endOfEnumerationReached) {
            return false;
        }

        if (isGetNextLastResultUpToDate) {
            return true;
        } else {
            try {
                this.getNextLastResult = getNextElementFromInnerFunctor();
                this.isGetNextLastResultUpToDate = true;
                return true;
            } catch (NoSuchElementException noSuchE) {
                endOfEnumerationReached = true;
                return false;
            }
        } // else
    } // method

    /**
     * Tests whether the enumeration started as an empty one. It does not matter if it
     * hasMoreElements() now, only at initialization time. Efficiency: if nextElements(),
     * hasMoreElements() were never used, it activates the hasMoreElements() once. Else it is
     * immediately(O(1))
     * 
     * @return true if the enumeration started as an empty one, false otherwise.
     */
    public boolean isEnumerationStartedEmpty()
    {
        if (this.innerFunctorUsageCounter == 0) {
            return !hasMoreElements();
        } else // it is not the first time , so use the saved value
               // which was initilaizeed during a call to
               // getNextElementFromInnerFunctor
        {
            return flagIsEnumerationStartedEmpty;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext()
    {
        return this.hasMoreElements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E next()
    {
        return this.nextElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove()
        throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * A functor for the calculation of the next element.
     * 
     * @param <EE> the element type
     */
    public interface NextElementFunctor<EE>
    {
        /**
         * Return the next element or throw a {@link NoSuchElementException} if there are no more
         * elements.
         * 
         * @return the next element
         * @throws NoSuchElementException in case there is no next element
         */
        EE nextElement()
            throws NoSuchElementException;
    }
}

// End PrefetchIterator.java
