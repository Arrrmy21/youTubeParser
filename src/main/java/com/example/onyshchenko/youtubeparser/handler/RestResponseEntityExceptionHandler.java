package com.example.onyshchenko.youtubeparser.handler;

import com.jayway.jsonpath.PathNotFoundException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(ContentNotFountException.class)
    public ResponseEntity<Object> resourceNotFoundException(ContentNotFountException ex, WebRequest request) {
        String errorMessage = "Content was not found by url.";

        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {InterruptedException.class, ExecutionException.class})
    public ResponseEntity<Object> internalError(RuntimeException ex, WebRequest request) {
        String errorMessage = "Internal Server Error occurred.";

        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {ConstraintViolationException.class, IllegalStateException.class, PathNotFoundException.class})
    public ResponseEntity<Object> pathVariableException(RuntimeException ex, WebRequest request) {
        String errorMessage = new ArrayList<>(((ConstraintViolationException) ex)
                .getConstraintViolations()).get(0).getMessage();
        String propertyPath = ((PathImpl) new ArrayList<>(((ConstraintViolationException) ex)
                .getConstraintViolations()).get(0).getPropertyPath()).getLeafNode().asString();

        String start = "Validation error. Incorrect value of parameter: " + propertyPath + ". ";
        String result = start + "Tips: " + errorMessage;

        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }
}