package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingDto {
    private long itemId;
    @NonNull
    @FutureOrPresent
    private LocalDateTime start;
    @NonNull
    @FutureOrPresent
    private LocalDateTime end;
}
