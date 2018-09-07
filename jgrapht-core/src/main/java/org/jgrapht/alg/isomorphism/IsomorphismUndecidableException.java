/*
 * (C) Copyright 2018-2018, by Christoph Grüne and Contributors.
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
package org.jgrapht.alg.isomorphism;

/**
 * Implementation of IsomorphismUndecidableException to indicate undecidable isomorphism cases in isomorphism inspectors
 */
public class IsomorphismUndecidableException extends RuntimeException {

    /**
     * Constructs a new exception with null as its detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to Throwable.initCause(java.lang.Throwable).
     */
    public IsomorphismUndecidableException() {}

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized,
     * and may subsequently be initialized by a call to Throwable.initCause(java.lang.Throwable).
     *
     * @param message the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
     */
    public IsomorphismUndecidableException(String message) {
        super (message);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message of (cause==null ? null : cause.toString())
     * (which typically contains the class and detail message of cause). This constructor is useful for exceptions that are
     * little more than wrappers for other throwables (for example, PrivilegedActionException).
     *
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method).
     *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public IsomorphismUndecidableException(Throwable cause) {
        super (cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * Note that the detail message associated with cause is not automatically incorporated in this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the Throwable.getMessage() method).
     * @param cause the cause (which is saved for later retrieval by the Throwable.getCause() method).
     *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public IsomorphismUndecidableException(String message, Throwable cause) {
        super (message, cause);
    }
}
