package com.finalProjectLedZeppelin.common.error;

/**
 * Exception thrown when a requested resource cannot be found.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Creates a new {@code NotFoundException} with the specified message.
     *
     * @param message detail message describing the missing resource
     */
    public NotFoundException(String message) {
        super(message);
    }
}
