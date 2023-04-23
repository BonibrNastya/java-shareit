package ru.practicum.shareit.booking.model;

import lombok.Data;
import lombok.NonNull;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
public class Booking {
    private long id;
    @NonNull
    private LocalDateTime start;
    @NonNull
    private LocalDateTime end;
    private Item item;
    private User booker;
    private Status status;
}
