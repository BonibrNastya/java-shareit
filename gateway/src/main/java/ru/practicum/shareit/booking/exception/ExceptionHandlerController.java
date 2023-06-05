package ru.practicum.shareit.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    private ErrorResponse exceptionHandler(final MethodArgumentNotValidException e) {
        return new ErrorResponse(e.getMessage(), System.currentTimeMillis());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    private ErrorResponse handleThrowable(final Throwable e) {
        return new ErrorResponse(e.getMessage(), System.currentTimeMillis());
    }
}
