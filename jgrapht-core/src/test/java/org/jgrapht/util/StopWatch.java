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
        beforeTime = System.currentTimeMillis();
    }

    public void stopAndReport()
    {
        final long deltaTime = System.currentTimeMillis() - beforeTime;
        if (deltaTime > 9999) {
            final double deltaTimeSec = deltaTime / 1000.0;
            System.out.println(
                "# Performence: " + deltaTimeSec + " full Seconds");
        } else {
            final String timeDesc;
            timeDesc =
                deltaTime <= 10 ? "<10ms [less than minumun measurement time]"
                : String.valueOf(deltaTime);
            System.out.println("# Performence:  in MiliSeconds:" + timeDesc);
        }
    }
}

// End StopWatch.java
