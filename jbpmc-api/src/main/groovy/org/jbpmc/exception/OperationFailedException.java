package org.jbpmc.exception;

/**
 * Thrown to indicate that the operation failed during execution.
 */
public class OperationFailedException extends RuntimeException {

    public OperationFailedException(String message) {
        super(message);
    }

    public OperationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
