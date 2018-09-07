/*
 * (C) Copyright 2015-2017, by Wil Selwood and Contributors.
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
package org.jgrapht.io;

/**
 * An exception that the library throws in case of graph import errors.
 */
public class ImportException
    extends
    Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an {@code ImportException} with {@code null} as its error detail message.
     */
    public ImportException()
    {
        super();
    }

    /**
     * Constructs an {@code ImportException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the
     *        {@link #getMessage()} method)
     */
    public ImportException(String message)
    {
        super(message);
    }

    /**
     * Constructs an {@code ImportException} with the specified detail message and cause.
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
    public ImportException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructs an {@code ImportException} with the specified cause and a detail message of
     * {@code (cause==null ? null : cause.toString())} (which typically contains the class and
     * detail message of {@code cause}). This constructor is useful for IO exceptions that are
     * little more than wrappers for other throwables.
     *
     * @param cause The cause (which is saved for later retrieval by the {@link #getCause()}
     *        method). (A null value is permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     *
     */
    public ImportException(Throwable cause)
    {
        super(cause);
    }

}

// End ImportException.java
