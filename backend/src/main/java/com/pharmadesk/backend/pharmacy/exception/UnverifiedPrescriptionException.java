package com.pharmadesk.backend.pharmacy.exception;

public class UnverifiedPrescriptionException extends RuntimeException {
    public UnverifiedPrescriptionException(String message) {
        super(message);
    }
}
