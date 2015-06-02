package org.jbpmc.exception;

/**
 * Thrown to indicate that the operation invoked is not supported by this JBPMC implementation.
 */
public class OperationNotSupportedException extends RuntimeException {

    public OperationNotSupportedException() {
        super("The operation is not supported by this BPM connector. If you want to help provide the implementation " +
                "for this operation, please contact one of the developers!");
    }
}
