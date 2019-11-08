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
    private static final long serialVersionUID = 1L;

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
