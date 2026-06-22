package com.pharmadesk.backend.pharmacy.exception;

public class InvalidReturnException extends RuntimeException {
    public InvalidReturnException(String message) {
        super(message);
    }
}
