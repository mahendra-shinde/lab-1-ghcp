package com.mahendra.orderservice.exception;

public class DiscontinuedItemException extends RuntimeException {
    public DiscontinuedItemException(String message) {
        super(message);
    }
}
