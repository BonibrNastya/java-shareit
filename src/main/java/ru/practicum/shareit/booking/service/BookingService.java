package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {

    Booking create(BookingDto bookingDto, long userId);

    Booking updateApprove(long bookingId, boolean approved, long userId);

    Booking getById(long bookingId, long userId);

    List<Booking> getAllByState(BookingStateDto bookingStateDto);

    List<Booking> getAllByOwner(BookingStateDto bookingStateDto);
}
