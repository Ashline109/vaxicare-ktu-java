package com.vaxicare.exception;

/**
 * Base checked exception class for all VaxiCare clinical application errors.
 * Demonstrates: Exception hierarchy, user-defined exceptions in KTU S3 Java syllabus.
 */
public class VaxiCareException extends Exception {
    private final String errorCode;

    public VaxiCareException(String message) {
        super(message);
        this.errorCode = "VAXI_GENERIC_ERR";
    }

    public VaxiCareException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public VaxiCareException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "VAXI_UNDERLYING_ERR";
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "VaxiCareException [" + errorCode + "]: " + getMessage();
    }
}
