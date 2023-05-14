package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.enums.State;

@Data
@AllArgsConstructor
public class BookingStateDto {
    private long userId;
    private State state;
}
