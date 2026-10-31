package com.example.showroom.web;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Turns errors into JSON with a readable "message" and, when useful, per-field "errors". */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException exception) {
        return body(HttpStatus.NOT_FOUND, exception.getMessage(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException exception) {
        Map<String, String> errors = exception.getField() == null
                ? null
                : Map.of(exception.getField(), exception.getMessage());
        return body(HttpStatus.CONFLICT, exception.getMessage(), errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationFailure(
            MethodArgumentNotValidException exception) {
        Map<String, String> errors = new TreeMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return body(HttpStatus.BAD_REQUEST, "Validation failed", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableBody(
            HttpMessageNotReadableException exception) {
        return body(HttpStatus.BAD_REQUEST,
                "The request body is not valid. Check the JSON format and the allowed values.",
                null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleBadParameter(
            MethodArgumentTypeMismatchException exception) {
        return body(HttpStatus.BAD_REQUEST,
                "Invalid value '" + exception.getValue() + "' for '" + exception.getName() + "'.",
                null);
    }

    // The database constraints still guard simultaneous requests that both
    // pass the service checks, such as two vehicles with the same VIN.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataConflict(
            DataIntegrityViolationException exception) {
        return body(HttpStatus.CONFLICT, "The change conflicts with existing data.", null);
    }

    private ResponseEntity<Map<String, Object>> body(
            HttpStatus status, String message, Map<String, String> errors) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        if (errors != null && !errors.isEmpty()) {
            response.put("errors", errors);
        }
        return ResponseEntity.status(status).body(response);
    }
}
