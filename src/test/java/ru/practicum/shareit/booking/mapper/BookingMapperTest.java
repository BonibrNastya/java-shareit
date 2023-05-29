package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingMapperTest {

    @Test
    void bookingItemDto() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        Item item = Item.builder()
                .id(1L)
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .build();
        User user = User.builder()
                .id(1L)
                .name("User")
                .email("name@mail.ru")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(user)
                .status(Status.WAITING)
                .build();

        BookingItemDto bookingItemDto = BookingMapper.bookingItemDto(booking);

        assertEquals(booking.getId(), bookingItemDto.getId());
        assertEquals(booking.getBooker().getId(), bookingItemDto.getBookerId());
    }
}