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

import java.util.concurrent.*;

/**
 * A very simple stop watch.
 * 
 * @author Assaf Lehr
 */
public class StopWatch
{
    private long startTime;

    /**
     * Construct a new stop watch and start it.
     */
    public StopWatch()
    {
        start();
    }

    /**
     * Restart.
     */
    public void start()
    {
        this.startTime = System.nanoTime();
    }

    /**
     * Get the elapsed time from the last restart.
     * 
     * @param timeUnit the time unit
     * @return the elapsed time in the given time unit
     */
    public long getElapsed(TimeUnit timeUnit)
    {
        return timeUnit.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
    }

}

// End StopWatch.java
