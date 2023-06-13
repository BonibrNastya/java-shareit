package ru.practicum.shareit.booking.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class ErrorResponse extends RuntimeException {
    private String error;
    private long timestamp;
}
