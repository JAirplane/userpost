package com.airplane.userpost.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	//Controller validation exceptions handling
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationArgumentException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		
		log.error("Validation errors found in Controller: {}", errors.size());
		errors.forEach((field, msg) -> log.error("Field: '{}': {}", field, msg));

        return ResponseEntity.badRequest().body(errors);
    }

	//Service validation exception handling
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getConstraintViolations()
                .forEach(constraintViolation -> {
                    String path = constraintViolation.getPropertyPath().toString();
                    String[] paths = path.split("\\.");
                    errors.put(paths[paths.length - 1], constraintViolation.getMessage());
                });

		log.error("Validation errors found in Service: {}", errors.size());
		errors.forEach((field, msg) -> log.error("Field: '{}': {}", field, msg));
		
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("Error", exception.getMessage()));
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePostNotFoundException(PostNotFoundException exception) {
        log.warn(exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("Error", exception.getMessage()));
    }

    @ExceptionHandler(MapperException.class)
    public ResponseEntity<Map<String, String>> handleMapperException(MapperException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("Error", exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("Error", "Unique index or primary key violation."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("Error", "Invalid format: " + exception.getValue()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleEmptyRequestBody(HttpMessageNotReadableException exception) {
        log.error(exception.getMessage());
        return ResponseEntity
                .badRequest()
                .body(Map.of("Error", "Request body is null."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleCommonException(Exception exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("Error", exception.getMessage()));
    }
}
