package com.petsplace;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Turns errors into {"error": "..."} which is what js/app.js expects. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiError.class)
    public ResponseEntity<Map<String, String>> apiError(ApiError e) {
        return ResponseEntity.status(e.status).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> unreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "That request couldn't be read. Please check the form and try again."));
    }

    /** Database trouble (e.g. MySQL stopped). Other errors keep Spring's normal 404/405 handling. */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> database(DataAccessException e) {
        e.printStackTrace();
        return ResponseEntity.status(500)
                .body(Map.of("error", "Something went wrong on our side. Please try again."));
    }
}
