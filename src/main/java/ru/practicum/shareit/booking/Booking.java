package ru.practicum.shareit.booking;

import lombok.Data;
import lombok.NonNull;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

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
