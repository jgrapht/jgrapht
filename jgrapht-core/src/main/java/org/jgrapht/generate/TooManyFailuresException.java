/*
 * (C) Copyright 2019-2021, by Amr ALHOSSARY and Contributors.
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
package org.jgrapht.generate;

/**
 * Raised when the generator fails, too many times in a row, to grow a graph.
 * 
 * @author Amr ALHOSSARY
 *
 */
public class TooManyFailuresException
    extends
    RuntimeException
{

    /** Serial Version ID */
    private static final long serialVersionUID = 7986467967127358163L;

    /**
     * Constructs a new too many failures Exception with null as its detail message. The cause is
     * not initialized, and may subsequently be initialized by a call to initCause.
     */
    public TooManyFailuresException()
    {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to initCause.
     * 
     * @param message the detail message (which is saved for later retrieval by the getMessage()
     *        method).
     */
    public TooManyFailuresException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new too Many Failures exception with the specified detail message and cause.
     * Note that the detail message associated with cause is not automatically incorporated in this
     * runtime exception's detail message.
     * 
     * @param message the detail message (which is saved for later retrieval by the getMessage()
     *        method).
     * @param cause the cause (which is saved for later retrieval by the getCause() method). (A null
     *        value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public TooManyFailuresException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
