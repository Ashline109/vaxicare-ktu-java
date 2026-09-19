package com.vaxicare.exception;

/**
 * Exception thrown when user authentication or permission checks fail.
 */
public class AuthenticationException extends VaxiCareException {
    public AuthenticationException(String message) {
        super(message, "ERR_AUTH_FAILED");
    }
}
