package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ItemWithDateDto {
    private long id;
    @NonNull
    private String name;
    @NonNull
    private String description;
    @NonNull
    private Boolean available;
    private BookingItemDto lastBooking;
    private BookingItemDto nextBooking;
    private Set<CommentDto> comments;
}
