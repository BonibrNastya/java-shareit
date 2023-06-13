package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingStateDto;
import ru.practicum.shareit.booking.dto.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public Booking create(BookingDto bookingDto, long userId) {
        if (isNull(bookingDto.getStart()) || isNull(bookingDto.getEnd())) {
            throw new ResponseStatusException((HttpStatus.BAD_REQUEST));
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().isEqual(bookingDto.getEnd())) {
            throw new ResponseStatusException(HttpStatus
                    .BAD_REQUEST, "Неверные даты бронирования.");
        }
        Item item = getItemOrException(bookingDto.getItemId());
        if (item.getOwner().getId() == userId) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Нельзя бронировать свои вещи.");
        }
        User user = getUserOrException(userId);
        if (item.getAvailable()) {
            Booking booking = Booking.builder()
                    .start(bookingDto.getStart())
                    .end(bookingDto.getEnd())
                    .item(item)
                    .booker(user)
                    .status(Status.WAITING)
                    .build();
            booking = bookingRepository.save(booking);
            return booking;
        } else {
            throw new ResponseStatusException(HttpStatus
                    .BAD_REQUEST, "Вещь недоступна для бронирования. Id = " + item.getId());
        }
    }

    @Override
    public Booking updateApprove(long bookingId, boolean approved, long userId) {
        Booking booking = getBookingOrException(bookingId);
        if (booking.getItem().getOwner().getId() == userId) {
            if (approved) {
                if (booking.getStatus() != Status.APPROVED) {
                    booking.setStatus(Status.APPROVED);
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Текущий статус уже APPROVED.");
                }
            } else if (booking.getStatus() != Status.REJECTED) {
                booking.setStatus(Status.REJECTED);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Текущий статус уже REJECTED.");
            }
            return bookingRepository.save(booking);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Изменят статус может только собственник вещи.");
        }
    }

    @Override
    public Booking getById(long bookingId, long userId) {
        Booking booking = getBookingOrException(bookingId);
        Item item = getItemOrException(booking.getItem().getId());
        if (booking.getBooker().getId() == userId || item.getOwner().getId() == userId) {
            return bookingRepository.findById(bookingId).get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Данные о бронировании доступны только авторам бронирования и владельцам вещи.");
        }
    }

    @Override
    public List<Booking> getAllByState(BookingStateDto bookingStateDto, int from, int size) {
        getUserOrException(bookingStateDto.getUserId());
        if (from >= 0 && size > 0) {
            PageRequest page = PageRequest.of(from > 0 ? from / size : 0,
                    size, Sort.by("start").descending());
            long bookerId = bookingStateDto.getUserId();
            switch (bookingStateDto.getState()) {
                case CURRENT:
                    return bookingRepository
                            .findCurrentBooking(bookerId, LocalDateTime.now(), page).getContent();
                case PAST:
                    return bookingRepository
                            .findByBooker_IdAndEndIsBefore(bookerId, LocalDateTime.now(), page).getContent();
                case FUTURE:
                    return bookingRepository
                            .findByBooker_IdAndStartIsAfter(bookerId, LocalDateTime.now(), page).getContent();
                case ALL:
                    return bookingRepository.findByBooker_Id(bookerId, page).getContent();
                default:
                    return bookingRepository
                            .findBookingByStatus(bookerId,
                                    Status.valueOf(bookingStateDto.getState().toString()), page)
                            .getContent();
            }
        } else {
            throw new ArithmeticException("Неверный индекс или количество элементов.");
        }
    }

    @Override
    public List<Booking> getAllByOwner(BookingStateDto bookingStateDto, int from, int size) {
        getUserOrException(bookingStateDto.getUserId());
        if (from >= 0 && size > 0) {
            PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
            long bookerId = bookingStateDto.getUserId();
            switch (bookingStateDto.getState()) {
                case CURRENT:
                    return bookingRepository
                            .findByOwnerCurrentBooking(bookerId, LocalDateTime.now(), page).getContent();
                case PAST:
                    return bookingRepository
                            .findByOwnerPastBooking(bookerId, LocalDateTime.now(), page).getContent();
                case FUTURE:
                    return bookingRepository
                            .findByOwnerFutureBooking(bookerId, LocalDateTime.now(), page).getContent();
                case ALL:
                    return bookingRepository.findByOwner(bookerId, page).getContent();
                default:
                    return bookingRepository
                            .findByOwnerByStatus(bookerId,
                                    Status.valueOf(bookingStateDto.getState().toString()), page)
                            .getContent();
            }
        } else {
            throw new ArithmeticException("Неверный индекс или количество элементов.");
        }
    }

    private Item getItemOrException(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена. " +
                        "Добавление/обновление бронирования невозможно."));
    }

    private Booking getBookingOrException(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь не найдена." +
                        "Добавление/обновление бронирования невозможно."));
    }

    private User getUserOrException(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден. " +
                        "Добавление/обновление бронирования невозможно."));
    }
}
