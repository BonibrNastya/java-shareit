package ru.practicum.shareit.booking;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking create(@RequestHeader(REQUEST_HEADER) Long userId,
                          @Valid @RequestBody BookingDto bookingDto) {
        return bookingService.create(bookingDto, userId);
    }

    @PatchMapping("/{id}")
    public Booking update(@RequestHeader(REQUEST_HEADER) Long userId,
                          @PathVariable("id") long bookingId,
                          @RequestParam boolean approved) {
        return bookingService.updateApprove(bookingId, approved, userId);
    }

    @GetMapping("/{id}")
    public Booking getById(@RequestHeader(REQUEST_HEADER) Long userId,
                           @PathVariable("id") long bookingId) {
        return bookingService.getById(bookingId, userId);
    }

    @GetMapping
    public List<Booking> getAllByState(@RequestHeader(REQUEST_HEADER) Long userId,
                                       @Valid @RequestParam(defaultValue = "ALL") State state,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return bookingService.getAllByState(new BookingStateDto(userId, state), from, size);
    }

    @GetMapping("/owner")
    public List<Booking> getAllByOwner(@RequestHeader(REQUEST_HEADER) Long userId,
                                       @RequestParam(defaultValue = "ALL") State state,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return bookingService.getAllByOwner(new BookingStateDto(userId, state), from, size);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public Map<String, String> handleStateError(final MethodArgumentTypeMismatchException e) {
        return Map.of("error", String.format("Unknown state: %s", e.getValue()));
    }
}