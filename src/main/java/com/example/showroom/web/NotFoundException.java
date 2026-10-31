package com.example.showroom.web;

/** A requested record does not exist; the API answers 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
