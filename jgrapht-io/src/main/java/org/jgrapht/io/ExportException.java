/*
 * (C) Copyright 2016-2017, by Dimitrios Michail and Contributors.
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
package org.jgrapht.io;

/**
 * An exception that the library throws in case of graph export errors.
 */
public class ExportException
    extends
    Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code ExportException} with {@code null} as its error detail message.
     */
    public ExportException()
    {
        super();
    }

    /**
     * Constructs an {@code ExportException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the
     *        {@link #getMessage()} method)
     */
    public ExportException(String message)
    {
        super(message);
    }

    /**
     * Constructs an {@code ExportException} with the specified detail message and cause.
     *
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically
     * incorporated into this exception's detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the
     *        {@link #getMessage()} method)
     *
     * @param cause The cause (which is saved for later retrieval by the {@link #getCause()}
     *        method). (A null value is permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     */
    public ExportException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructs an {@code ExportException} with the specified cause and a detail message of
     * {@code (cause==null ? null : cause.toString())} (which typically contains the class and
     * detail message of {@code cause}). This constructor is useful for IO exceptions that are
     * little more than wrappers for other throwables.
     *
     * @param cause The cause (which is saved for later retrieval by the {@link #getCause()}
     *        method). (A null value is permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     *
     */
    public ExportException(Throwable cause)
    {
        super(cause);
    }

}
