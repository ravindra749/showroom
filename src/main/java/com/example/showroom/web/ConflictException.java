package com.example.showroom.web;

/**
 * A request breaks a business rule against existing data; the API answers 409.
 * The optional field names the request field the client should correct.
 */
public class ConflictException extends RuntimeException {

    private final String field;

    public ConflictException(String message) {
        this(message, null);
    }

    public ConflictException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
