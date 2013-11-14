/* This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 * or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 */
package org.jgrapht.experimental.alg;

import java.util.*;


public interface ExactAlgorithm<ResultType, V>
{
    //~ Methods ----------------------------------------------------------------

    ResultType getResult(Map<V, Object> optionalData);
}

// End ExactAlgorithm.java
