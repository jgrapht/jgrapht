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
 * StopWatch.java
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
package org.jgrapht.util;

/**
 * @author Assaf
 * @since May 30, 2005
 */
public class StopWatch
{
    //~ Instance fields --------------------------------------------------------

    long beforeTime;

    //~ Methods ----------------------------------------------------------------

    public void start()
    {
        this.beforeTime = System.currentTimeMillis();
    }

    public void stopAndReport()
    {
        long deltaTime = System.currentTimeMillis() - beforeTime;
        if (deltaTime > 9999) {
            double deltaTimeSec = deltaTime / 1000.0;
            System.out.println(
                "# Performence: " + deltaTimeSec + " full Seconds");
        } else {
            String timeDesc;
            timeDesc =
                (deltaTime <= 10) ? "<10ms [less than minumun measurement time]"
                : String.valueOf(deltaTime);
            System.out.println("# Performence:  in MiliSeconds:" + timeDesc);
        }
    }
}

// End StopWatch.java
