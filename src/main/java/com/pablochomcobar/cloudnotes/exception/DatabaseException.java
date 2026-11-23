package com.pablochomcobar.cloudnotes.exception;

/**
 * Wraps low-level Firestore errors (network problems, permission errors, timeouts)
 * into one simple exception that the rest of the app understands.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
