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
/* -------------
 * AllTests.java
 * -------------
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * Original Author:  Barak Naveh
 * Contributor(s):   -
 *
 * $Id$
 *
 * Changes
 * -------
 * 24-Jul-2003 : Initial revision (BN);
 *
 */
package org.jgrapht;

import org.jgrapht.alg.*;
import org.jgrapht.alg.util.*;
import org.jgrapht.generate.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.jgrapht.util.*;
import org.junit.runner.*;
import org.junit.runners.*;

/**
 * Runs all unit tests of the JGraphT library.
 *
 * @author Barak Naveh
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AllAlgTests.class,
    AllAlgUtilTests.class,
    AllGenerateTests.class,
    AllGraphTests.class,
    AllTraverseTests.class,
    AllUtilTests.class
})
public final class AllTests
{
}
// End AllTests.java
