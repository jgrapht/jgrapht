/*
 * (C) Copyright 2021-2021, by Hannes Wellmann and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.util;

import java.util.function.*;

/**
 * Exception thrown to indicate that a {@link Supplier} is in an invalid state.
 * 
 * @author Hannes Wellmann
 */
public class SupplierException
    extends
    IllegalArgumentException
{
    private static final long serialVersionUID = -8192314371524515620L;

    public SupplierException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
